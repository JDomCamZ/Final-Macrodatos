import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.broadcast.Broadcast
import org.apache.log4j.{Level, Logger}

val sparkLogger = Logger.getLogger("org.apache.spark")
sparkLogger.setLevel(Level.ERROR)

val ssc = new StreamingContext(sc, Seconds(2))

var W: Array[Double] = Array.fill(4)(0.0)
var b: Double = 0.0
val lrate: Double = 0.00001

// Inicializa la conexión en el driver
val resultSocket = new java.net.Socket("192.168.0.16", 4444)
val resultOut = new java.io.PrintWriter(resultSocket.getOutputStream(), true)

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

  // Guarda los resultados en un sistema de almacenamiento compartido
  // (puedes ajustar esto según tu entorno, por ejemplo, HDFS, Cassandra, etc.)
  // Aquí usaremos un archivo en el sistema de archivos local para fines ilustrativos.
  val resultFilePath = "/tmp/results.txt"
  val resultFile = sc.textFile(resultFilePath)
  resultFile.saveAsTextFile(resultFilePath)

  // Envía la ubicación del archivo de resultados al servidor a través de la conexión en el driver
  resultOut.println(s"ResultsFilePath: $resultFilePath")
}

val lines = ssc.socketTextStream("192.168.0.16", 4444)
lines.foreachRDD(rdd => processRDD(rdd))

ssc.start()
ssc.awaitTermination()