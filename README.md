# Manual de Usuario: Simulador de Autómatas

Esta herramienta permite diseñar, evaluar y visualizar el comportamiento de Autómatas Finitos Deterministas (DFA) y No Deterministas (NFA).

## 1. Cómo ingresar un Autómata (Manualmente)

Para crear un autómata desde cero en la interfaz, diríjase al panel de **Configuración** (lado izquierdo de la pantalla) y siga estos pasos:

1. **Seleccione el Tipo:** En el menú desplegable "Tipo de Autómata", elija entre `DFA` (Determinista) o `NFA` (No Determinista).
2. **Defina el Alfabeto:** En el campo de texto "Alfabeto", ingrese los símbolos válidos separados por comas (por ejemplo: `a,b,c` o `0,1`).
3. **Inicie la Creación:** Haga clic en el botón azul **"Crear Autómata"**.


> **Nota:** *(Si tu programa requiere dibujar o llenar una tabla de transiciones después de esto, puedes agregar aquí una breve instrucción extra sobre cómo hacerlo).*

## 📂 2. Cómo cargar archivos (Autómatas o Pruebas)

Si ya tiene un autómata guardado o un lote de pruebas en un archivo, puede cargarlos directamente sin escribirlos a mano:

1. Diríjase a la barra superior y seleccione la opción **Archivo > Cargar...**
2. Se abrirá el explorador de archivos de su computadora. Busque y seleccione su archivo de configuración (formatos soportados: `.txt`, `.json`, etc.).
3. Haga clic en **"Abrir"**. El sistema leerá el documento y cargará el autómata o las palabras de prueba automáticamente en los paneles correspondientes.

## 🧪 3. Cómo evaluar palabras

Una vez ingresado o cargado el autómata, el panel de **Pruebas** se activará:

1. **Ingreso por lote:** Escriba las cadenas que desea probar en el área de texto (una palabra por línea). Si desea evaluar la palabra vacía, utilice el botón **"ε"**.
2. **Evaluación:** Haga clic en **"Evaluar Todas"**.
3. **Resultados:** En la lista inferior aparecerá cada palabra evaluada y su estado (Aceptada / Rechazada).
4. **Simulación:** Seleccione un resultado de la lista y use los botones **"Siguiente Paso"** o **"Reproducir"** para ver cómo el autómata procesa la cadena símbolo por símbolo.