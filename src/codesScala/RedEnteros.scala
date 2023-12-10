////modificado para ingreso de enteros
import scala.util.Random

class Rna01(ci: Int, co: Int, cs: Int) {
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

  private def usored(dtest: Array[Int]): Unit = {
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
    println("-----------****Inicio Test****----------")
    println("prueba" + datatest.mkString("[", ", ", "] "))
    println("salida" + y.slice(c(1), c(1) + c(2)).mkString("[", ", ", "] "))
    println("-----------****Fin Test****----------")
  }
///como el método prueba pero retorna el mejor indice
  def mejorIndice(datatest: Array[Int]): Int = {// Convierte el array de entrada a un array de Double
    var datatestDouble = datatest.map(_.toDouble)
    // Realiza la propagación hacia adelante
    var ii = 0
    var pls = 0.0
    for (i <- 0 until c(1)) {
      for (j <- 0 until c(0)) {
        pls += w(ii) * datatestDouble(j)
        ii += 1
      }
      s(i) = pls
      y(i) = fun(s(i))
      pls = 0.0
    }
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
    // Encuentra el índice del valor máximo en la capa de salida
    val indiceMaximo = c(1) + y.indexOf(y.slice(c(1), c(1) + c(2)).max)
    indiceMaximo
  }

  def prueba(pruebas: Array[Array[Int]]): Unit = {
    for (prueba <- pruebas) {
      usored(prueba)
    }
  }
}
///prueba con enteros
val rn = new Rna01(3, 2, 2)

val ingreso = Array(
    Array(0, 1, 0),
    Array(0, 1, 1),
    Array(1, 0, 0),
    Array(1, 0, 1)
)
val salida = Array(
    Array(1),
    Array(0),
    Array(1),
    Array(0)
)
val evaluar = Array(
    Array(0, 1, 0),
    Array(0, 1, 1),
    Array(1, 0, 0),
    Array(1, 0, 1),
    Array(1, 1, 1),
    Array(0, 0, 0),
    Array(1, 1, 0)
)

rn.entrenamiento(ingreso, salida, 1000)
rn.prueba(evaluar)
///////////////


  def generarDatosEntrada(cantidad: Int, longitud: Int): Array[Array[Int]] = {
    Array.fill(cantidad)(Array.fill(longitud)(util.Random.nextInt(40) + 1))
  }

  // Función para generar datos de salida (arrays de arrays de longitud 1 con números enteros del 1 al 30)
  def generarDatosSalida(cantidad: Int, longitud: Int): Array[Array[Int]] = {
    Array.fill(cantidad)(Array(util.Random.nextInt(30) + 1))
  }
val rn = new Rna01(20, 10, 1000)  // Ejemplo con capa de entrada de 20 nodos y capa de salida de 1 nodo

    // Genera datos de entrenamiento y prueba
    val ingresoEntrenamiento = generarDatosEntrada(100, 20)
    val salidaEntrenamiento = generarDatosSalida(100, 1)
    val ingresoPrueba = generarDatosEntrada(10, 20)

    rn.entrenamiento(ingresoEntrenamiento, salidaEntrenamiento, 1000)

    // Realiza pruebas con datos de prueba
    for (dato <- ingresoPrueba) {
      println("-----------****Inicio Test****----------")
      println("Entrada: " + dato.mkString("[", ", ", "]"))
      rn.prueba(Array[dato])
      println("-----------****Fin Test****----------")
    }
    rn.prueba(ingresoEntrenamiento)


//pruebas trabajo
val rn = new Rna01(20, 1000,1)


rn.entrenamiento(ingreso, salida, 1000)
rn.prueba(evaluar)