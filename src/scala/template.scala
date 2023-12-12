import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

val sparkLogger = Logger.getLogger("org.apache.spark")
sparkLogger.setLevel(Level.ERROR)

val ssc = new StreamingContext(sc, Seconds(2))

var W: Array[Double] = Array.fill(4)(0.0)
var b: Double = 0.0
val lrate:Double = 0.00001

def calcularGrad(xy: (Array[Double], Double), w: Array[Double], bias: Double): (Array[Double], Double, Double) = {
  val x = xy._1
  val y = xy._2

  val yPred = (x.zip(w)).map { case (xi, wi) => xi * wi }.sum + bias
  val error = yPred - y
  println(s" y: $y yPred: $yPred ")

  val gradW = x.map(xi => error * xi)
  val gradB = error
  val error2 = (error * error) / 2

  (gradW, gradB, error2)
}

def processRDD(rdd: RDD[String]): Unit = {
  val resultSocket = new java.net.Socket("192.168.0.19", 4444)
  val resultOut = new java.io.PrintWriter(resultSocket.getOutputStream(), true)

  try {
    val size = rdd.count()
    val data: RDD[(Array[Double], Double)] = rdd.map { str =>
      val parts = str.split("#")
      val features = parts(0).split(",").map(_.toDouble)
      val label = parts(1).toDouble
      (features, label)
    }

    val grads = data.map(xy => calcularGrad(xy, W, b))
    val gradSum = grads.reduce((g1, g2) => (g1._1.zip(g2._1).map { case (x, y) => x + y }, g1._2 + g2._2, g1._3 + g2._3))
    val gradAvg = (gradSum._1.map(_ / size), gradSum._2 / size)
    val errorAvg = gradSum._3 / size

    // Envía los resultados al servidor
    resultOut.println(s"Grad: gB: ${gradAvg._2} gW: ${gradAvg._1.mkString(" ")} error = $errorAvg")

    println(s"Results sent to server: Grad: gB: ${gradAvg._2} gW: ${gradAvg._1.mkString(" ")} error = $errorAvg")

    // Envía el mensaje DISCONNECT antes de cerrar el socket
    resultOut.println("DISCONNECTED")
    resultOut.println("DISCONNECT")
  } finally {
    // Cerrar el socket después de usarlo
    resultOut.close()
    resultSocket.close()
  }
}

val lines = ssc.socketTextStream("192.168.0.19", 4444)
lines.foreachRDD(rdd => processRDD(rdd))

ssc.start()
ssc.awaitTermination()