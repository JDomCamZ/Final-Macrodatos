import scala.io.Source
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
////////////////////////Funciones de lectura de datos
import org.apache.spark.sql.SparkSession
///////////////////////////////Imports de comunicación
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}
////////////////////////////////////
val filePath = "repos.txt"
val textRDD = spark.sparkContext.textFile(filePath)

// Dividir cada línea por comas para obtener columnas
val columnsRDD = textRDD.map(line => line.split(","))

// Obtener los valores de la primera columna en un array (solo minusculas)
val arrayColumn1 = columnsRDD.map(columns => columns(0).toLowerCase).collect().tail
val normalColumn1 = columnsRDD.map(columns => columns(0)).collect().tail
// Obtener los valores de la segunda columna en un array
val arrayColumn2 = columnsRDD.map(columns => columns(1)).collect().tail

// Imprimir los arrays
//arrayColumn1.foreach(println)
//arrayColumn2.foreach(println)

//////////////////////////////////////////////ascii
////un string a código ascii
def stringToAscii(str: String): Array[Int] = {
  val maxLength = 20
  val paddedStr = if (str.length < maxLength) {
    str + " " * (maxLength - str.length) // Añadir espacios para alcanzar la longitud de 20
  } else {
    str.substring(0, maxLength) // Tomar solo los primeros 20 caracteres
  }
  paddedStr.map(_.toInt).toArray
}


////de un array de strings a un array de arrays en código acci
////maxima longitud en entrada igual 20
def ToAscii(strings: Array[String]): Array[Array[Int]] = {
  val maxLength = 20
  strings.map { str =>
    val paddedStr = if (str.length < maxLength) {
      str + " " * (maxLength - str.length) // Añadir espacios para alcanzar la longitud de 20
    } else {
      str.substring(0, maxLength) // Tomar solo los primeros 20 caracteres
    }
    paddedStr.map(_.toInt).toArray
  }
}

var entradas = ToAscii(arrayColumn1)
//val salida = Array.tabulate(1000)(i => Array(i))
//salida.foreach(arr => println(arr.mkString("Array(", ", ", ")")))

def ArraySalida(x: Int, y: Int): Array[Array[Int]] = {
  // Inicializar un array vacío para almacenar los arrays internos
  val resultado = new Array[Array[Int]](x)

  // Generar cada array interno
  for (i <- 0 until x) {
    // Inicializar un array interno con longitud y lleno de ceros
    val arrayInterno = Array.fill(y)(0)
    
    // Colocar un 1 en la posición i del array interno
    arrayInterno(i) = 1
    
    // Agregar el array interno al resultado
    resultado(i) = arrayInterno
  }

  resultado
}
val salida = ArraySalida(1000,1000)

//////////////////////////////////RED NEURONAL
////modificado para ingreso de enteros
import scala.util.Random

class Rna01(ci: Int, co: Int, cs: Int) extends Serializable {
  private val rand = new Random
  private var xin: Array[Array[Double]] = _
  private var xout: Array[Array[Double]] = _
  private var y: Array[Double] = _
  private var s: Array[Double] = _
  private var g: Array[Double] = _
  private var w: Array[Double] = _
  private var c: Array[Int] = _

  initialize()

  private def initialize(): Unit = {
    y = new Array[Double](co + cs)
    s = new Array[Double](co + cs)
    g = new Array[Double](co + cs)
    w = new Array[Double](ci * co + co * cs)
    c = Array(ci, co, cs)

    for (i <- y.indices) y(i) = 0
    for (i <- s.indices) s(i) = 0
    for (i <- g.indices) g(i) = 0
    for (i <- w.indices) w(i) = getRandom
  }

  private def getRandom: Double = rand.nextDouble() * 2 - 1

  private def fun(d: Double): Double = 1 / (1 + Math.exp(-d))

  def entrenamiento(in: Array[Array[Int]], sal: Array[Array[Int]], veces: Int): Unit = {
    xin = in.map(_.map(_.toDouble))
    xout = sal.map(_.map(_.toDouble))
    for (_ <- 0 until veces; _ <- xin.indices) {
      entreno()
    }
  }

  private def entreno(): Unit = {
    var ii: Int = 0
    var pls: Double = 0
    var ci: Int = 0

    // Ida
    // Capa 1
    ci = 0
    ii = 0
    pls = 0
    for (i <- 0 until c(1)) {
      for (j <- 0 until c(0)) {
        pls += w(ii) * xin(ci)(j)
        ii += 1
      }
      s(i) = pls
      y(i) = fun(s(i))
      pls = 0
    }

    // Capa 2
    pls = 0
    ii = c(0) * c(1)
    for (i <- 0 until c(2)) {
      for (j <- 0 until c(1)) {
        pls += w(ii) * y(j)
        ii += 1
      }
      s(i + c(1)) = pls
      y(i + c(1)) = fun(s(i + c(1)))
      pls = 0
    }

    // Vuelta
    // Capa 2 g
    for (i <- 0 until c(2)) {
      g(i + c(1)) = (xout(ci)(i) - y(i + c(1))) * y(i + c(1)) * (1 - y(i + c(1)))
    }

    // Capa 1 g
    pls = 0
    for (i <- 0 until c(1)) {
      for (j <- 0 until c(2)) {
        pls += w(c(0) * c(1) + j * c(1) + i) * g(c(1) + j)
      }
      g(i) = y(i) * (1 - y(i)) * pls
      pls = 0
    }

    // Capa 2 w
    ii = c(0) * c(1)
    for (i <- 0 until c(2)) {
      for (j <- 0 until c(1)) {
        w(ii) += g(i + c(1)) * y(j)
        ii += 1
      }
    }

    // Capa 1 w
    ii = 0
    for (i <- 0 until c(1)) {
      for (j <- 0 until c(0)) {
        w(ii) += g(i) * xin(ci)(j)
        ii += 1
      }
    }
  }

  def usored(dtest: Array[Int]): Array[Double] = {
    var datatest: Array[Double] = dtest.map(_.toDouble)
    var ii: Int = 0
    var pls: Double = 0
    var ci: Int = 0

    // Ida
    // Capa 1
    ii = 0
    pls = 0
    ci = 0
    for (i <- 0 until c(1)) {
      for (j <- 0 until c(0)) {
        pls += w(ii) * datatest(j)
        ii += 1
      }
      s(i) = pls
      y(i) = fun(s(i))
      pls = 0
    }

    // Capa 2
    pls = 0
    ii = c(0) * c(1)
    for (i <- 0 until c(2)) {
      for (j <- 0 until c(1)) {
        pls += w(ii) * y(j)
        ii += 1
      }
      s(i + c(1)) = pls
      y(i + c(1)) = fun(s(i + c(1)))
      pls = 0
    }

    // Salida
    //println("-----------****Inicio Test****----------")
    //println("prueba" + datatest.mkString("[", ", ", "] "))
    //println("salida" + y.slice(c(1), c(1) + c(2)).mkString("[", ", ", "] "))
    //println("-----------****Fin Test****----------")
    var result = (y.slice(c(1), c(1) + c(2)))
    return result
  }

  def prueba(pruebas: Array[Array[Int]]): Unit = {
    for (prueba <- pruebas) {
      usored(prueba)
    }
  }
}
///prueba con enteros
val rn = new Rna01(20, 100, 1000)
rn.entrenamiento(entradas, salida, 100)

////////////COLOCARLO EN EL SOCKET PARA DEVOLVER
////luego de entrenar
////esto debería ser parte del mensaje recivido
var pruebaStr = "freeCodeCamp"   
//pasarlo a ascii de máximo 20 caracteres
///tienes que convertir la entrada a todo minusculas
var asc = stringToAscii(pruebaStr.toLowerCase)
var resu = rn.usored(asc)
//var indice = (100000*resu(0)).toInt
//var buscado = arrayColumn2(indice)
///////////////
var iMax = resu.indexOf(resu.max)
var resultado = "Name: " + normalColumn1(iMax)+ "   DESCRIPTION:" + arrayColumn2(iMax)

//S--IDCliente--resultado
//Comunicación

val sparkLogger = Logger.getLogger("org.apache.spark")
sparkLogger.setLevel(Level.ERROR)

val ssc = new StreamingContext(sc, Seconds(2))
val lines = ssc.socketTextStream("192.168.0.19", 4444)

def processRDD(rdd: RDD[String]): Unit = {
  // Lógica de procesamiento de RDD después de recibir el mensaje
  Thread.sleep(1500)
  val resultSocket = new java.net.Socket("192.168.0.19", 4444)
  val resultOut = new java.io.PrintWriter(resultSocket.getOutputStream(), true)

  try {
    val data: RDD[(Array[Int], String)] = rdd.map { str =>
      val parts = str.split("--")
      val clientId = parts(0)  // ID del cliente
      val inputString = parts(1)  // Cadena a procesar
      
      // Convertir la cadena a ASCII
      val asciiInput = stringToAscii(inputString.toLowerCase())
      
      // Ejecutar la red neuronal entrenada
      val result = rn.usored(asciiInput)
      
      // Encontrar el índice del resultado máximo
      val iMax = result.indexOf(result.max)
      
      // Construir el mensaje de resultado
      val resultMessage = s"S--$clientId--Name: ${normalColumn1(iMax)}   DESCRIPTION: ${arrayColumn2(iMax)}"
      
      // Enviar el mensaje al servidor
      println(s"Result sent to server: $resultMessage")
      
      // Retornar el resultado al cliente
      (asciiInput, resultMessage)
    }

    data.collect().foreach(println)
    // Obtener el primer elemento del RDD
    val primerElemento = data.first()

    // Acceder al segundo valor del primer elemento
    val segundoValor = primerElemento._2

    // Imprimir o utilizar el segundo valor
    println(s"Segundo valor del primer elemento: $segundoValor")

    resultOut.println(segundoValor)

  } finally {
    //Thread.sleep(1000)
    resultOut.println("DISCONNECTED")
    resultOut.println("DISCONNECT")
    // Cerrar el socket después de usarlo
    resultOut.close()
    resultSocket.close()
  }
}

// Iniciar el contexto de transmisión y esperar hasta que el contexto termine o alcance el tiempo de espera
lines.foreachRDD(rdd => processRDD(rdd))

ssc.start()
ssc.awaitTermination()
