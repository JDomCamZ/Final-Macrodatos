import pandas as pd

# Cargar el archivo CSV
archivo_csv = "repositories.csv"
data_frame = pd.read_csv(archivo_csv, nrows=5000)  # Lee solo las primeras 2000 filas

columnas_seleccionadas = data_frame.iloc[:, [0, 1, 2]]

# Limpiar filas con caracteres ASCII no comunes
def limpiar_caracteres(fila):
    return ''.join(c for c in str(fila) if 32 <= ord(c) <= 126)

def limpiar_comillas(fila):
    return ''.join(c for c in str(fila) if ord(c) != 44)
columnas_limpias = columnas_seleccionadas.applymap(limpiar_caracteres)
columnas_limpias = columnas_limpias.applymap(limpiar_comillas)

# Eliminar espacios innecesarios
columnas_limpias = columnas_limpias.applymap(lambda x: ' '.join(x.split()))
# Limitar a 1000 filas
filas_limitadas = columnas_limpias.head(1000)

# Guardar el nuevo DataFrame en un nuevo archivo CSV
filas_limitadas.to_csv("repos.csv", index=False)