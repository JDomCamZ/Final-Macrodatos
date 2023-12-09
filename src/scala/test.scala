import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

val sparkLogger = Logger.getLogger("org.apache.spark")
sparkLogger.setLevel(Level.ERROR)

val ssc = new StreamingContext(sc, Seconds(2))

// Función para esperar el mensaje inicial y enviar un mensaje de respuesta
def waitForStartMessage(socket: String, port: Int, expectedMessage: String, responseMessage: String): Unit = {
  var messageReceived = false
  while (!messageReceived) {
    val socketStream = ssc.socketTextStream(socket, port)
    socketStream.foreachRDD { rdd =>
      rdd.collect().foreach { message =>
        if (message == expectedMessage) {
          messageReceived = true
          // Enviar un mensaje de respuesta "spark"
          val responseSocket = new java.net.Socket(socket, port)
          val responseOut = new java.io.PrintWriter(responseSocket.getOutputStream(), true)
          responseOut.println(responseMessage)
          responseSocket.close()
        }
      }
    }
    Thread.sleep(1000) // Espera un segundo entre intentos
  }
}

// Esperar el mensaje "es cliente o spark?" y enviar "spark" como respuesta
waitForStartMessage("192.168.0.16", 4444, "Is Spark or Client?", "Spark")

// Resto de tu código de Spark
var W: Array[Double] = Array.fill(4)(0.0)
var b: Double = 0.0
val lrate: Double = 0.00001

def calcularGrad(xy: (Array[Double], Double), w: Array[Double], bias: Double): (Array[Double], Double, Double) = {
  // Lógica de calcularGrad
}

def processRDD(rdd: RDD[String]): Unit = {
  // Lógica de processRDD
}

lines.foreachRDD(rdd => processRDD(rdd))

ssc.start()
ssc.awaitTermination()

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

val sparkLogger = Logger.getLogger("org.apache.spark")
sparkLogger.setLevel(Level.ERROR)

val ssc = new StreamingContext(sc, Seconds(2))
val lines = ssc.socketTextStream("192.168.0.16", 4444)

// Variable para indicar si el mensaje ya fue recibido
@volatile var messageReceived = false

def waitForStartMessage(socket: String, port: Int, expectedMessage: String, responseMessage: String): Unit = {
  // Utilizar la variable de estado para asegurarse de que solo se ejecute una vez
  synchronized {
    if (!messageReceived) {
      val socketStream = ssc.socketTextStream(socket, port)
      socketStream.foreachRDD { rdd =>
        rdd.collect().foreach { message =>
          if (message == expectedMessage) {
            messageReceived = true
            // Enviar un mensaje de respuesta "spark"
            //val responseSocket = new java.net.Socket(socket, port)
            //val responseOut = new java.io.PrintWriter(responseSocket.getOutputStream(), true)
            //responseOut.println(responseMessage)
            //responseSocket.close()
          }
        }
      }
    }
  }
}

// Esperar el mensaje "es cliente o spark?" y enviar "spark" como respuesta
val waitForStartThread = new Thread {
  override def run() {
    waitForStartMessage("192.168.0.16", 4444, "Is Spark or Client?", "Spark")
  }
}

// Iniciar la espera del mensaje en un hilo separado
waitForStartThread.start()

var W: Array[Double] = Array.fill(4)(0.0)
var b: Double = 0.0
val lrate:Double = 0.00001

def calcularGrad( xy: (Array[Double],Double), w: Array[Double], bias: Double ): (Array [Double],Double, Double) = {
    val x = xy._1
    val y = xy._2

    val yPred = ( x.zip(w)).map{ case (xi,wi) => xi*wi }.sum + bias
    val error = yPred - y
    println(s" y: $y yPred: $yPred ")

    val gradW = x.map( xi => error * xi )
    val gradB = error
    val error2 = (error*error)/2

    (gradW,gradB,error2)
}


def processRDD(rdd:RDD[String]): Unit = {
    //rdd.foreach(println)
    val size = rdd.count()

    val data: RDD[(Array[Double], Double)] = rdd.map { str =>
                                                              val parts = str.split("#")
                                                              val features = parts(0).split(",").map(_.toDouble)
                                                              val label = parts(1).toDouble
                                                              (features, label)
                                                      }

    val grads = data.map(xy => calcularGrad(xy,W,b))
    val gradSum = grads.reduce((g1,g2) => (g1._1.zip(g2._1).map{ case (x,y) => x + y } , g1._2 + g2._2 , g1._3 + g2._3))
    val gradAvg = (gradSum._1.map(_ / size) , gradSum._2/ size)
    val errorAvg = gradSum._3/size

    val wStro = W.mkString(" ")
    println(s"OLD $size b= $b w= $wStro")

    W = W.zip(gradAvg._1).map{case (wi, gw) => wi - lrate* gw }
    b = b - lrate * gradAvg._2

    val wStr = W.mkString(" ")
    val gradWAvgStr = gradAvg._1.mkString(" ")
    val gradBAvgStr = gradAvg._2

    println(s"Grad: gB: $gradBAvgStr gW: $gradWAvgStr error = $errorAvg")
    println(s"New BW : $size b= $b w= $wStr")

    println("==============================================")
    //data.foreach(println)
}


lines.foreachRDD(rdd=>processRDD(rdd))

// Iniciar el contexto de transmisión y esperar hasta que el contexto termine o alcance el tiempo de espera
ssc.start()
ssc.awaitTerminationOrTimeout(60000)