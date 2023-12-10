import scala.io.Source

// Ruta al archivo txt
val rutaArchivo = "repos.txt"

// Leer las líneas del archivo
 val lineas = Source.fromFile(rutaArchivo).getLines()

  // Procesar cada línea (considerando que es formato CSV)
lineas.foreach { linea =>
  val columnas = linea.split(",").map(_.trim) // Separar por comas y quitar espacios
  // Hacer algo con las columnas, por ejemplo, imprimir
  println(columnas.mkString(", "))
}

// Cerrar el recurso después de su uso
Source.fromFile(rutaArchivo).close()

//////leer

val filePath = "repos.txt"
val textRDD = spark.sparkContext.textFile(filePath)

// Dividir cada línea por comas para obtener columnas
val columnsRDD = textRDD.map(line => line.split(","))

// Obtener los valores de la primera columna en un array
val arrayColumn1 = columnsRDD.map(columns => columns(0)).collect()
// Obtener los valores de la segunda columna en un array
val arrayColumn2 = columnsRDD.map(columns => columns(1)).collect()

// Imprimir los arrays
arrayColumn1.foreach(println)
arrayColumn2.foreach(println)

// Imprimir el primer elemento del arrayColumn1
println("Primer elemento de la Columna 1: " + arrayColumn1(0))

// Imprimir el segundo elemento del arrayColumn2
println("Segundo elemento de la Columna 2: " + arrayColumn2(1))

// Obtener la longitud de arrayColumn1
val longitudColumn1 = arrayColumn1.length
println(s"La longitud de la Columna 1 es: $longitudColumn1")

// Obtener la longitud de arrayColumn2
val longitudColumn2 = arrayColumn2.length
println(s"La longitud de la Columna 2 es: $longitudColumn2")
////////////////////////////////////////////////////////////////////////
// Función para convertir un string a un array de valores ASCII
def stringToAsciiArray(str: String): Array[Int] = {
  str.map(_.toInt).toArray
}

// Convertir arrayColumn1 a un nuevo array que contiene arrays de valores ASCII
val asciiArrays = arrayColumn1.map(stringToAsciiArray)

// Imprimir los nuevos arrays de valores ASCII
asciiArrays.foreach(println)


// Eliminar el primer elemento de arrayColumn1
val nuevoArrayColumn1 = arrayColumn1.tail

// Eliminar el primer elemento de arrayColumn2
val nuevoArrayColumn2 = arrayColumn2.tail

// Imprimir los nuevos arrays sin el primer elemento
println("Nuevo arrayColumn1 sin el primer elemento:")
nuevoArrayColumn1.foreach(println)

println("\nNuevo arrayColumn2 sin el primer elemento:")
nuevoArrayColumn2.foreach(println)