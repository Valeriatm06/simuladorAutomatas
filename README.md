# Manual de Usuario: Simulador de Autómatas

Esta herramienta permite diseñar, evaluar y visualizar el comportamiento de Autómatas Finitos Deterministas (DFA) y No Deterministas (NFA).

## 1. Cómo ingresar un Autómata (Manualmente)

Para crear un autómata desde cero en la interfaz, diríjase al panel de **Configuración** (lado derecho de la pantalla) y siga estos pasos:

<img width="500" height="700" alt="conf" src="https://github.com/user-attachments/assets/33a76a0e-4a67-4147-ad0d-fbe125a9624c" />

1. **Seleccione el Tipo:** En el menú desplegable "Tipo de Autómata", elija entre `DFA` (Determinista) o `NFA` (No Determinista).
2. **Defina el Alfabeto:** En el campo de texto "Alfabeto", ingrese los símbolos válidos separados por comas (por ejemplo: `a,b,c` o `0,1`).
3. **Inicie la Creación:** Haga clic en el botón azul **"Crear Autómata"**.
4. **Agregar un estado:** Dirijase al panel de la izquierda, donde encontrara los botones presentados en la figura.
   
<img width="298" height="459" alt="esta" src="https://github.com/user-attachments/assets/140c170e-3ad5-4439-a9c1-15b28912810a" />

Al hacer click en el botón que tiene un circulo, una ventana aparecerá para indicar el nombre del estado, si es inicial o de aceptación. Luego de llenar la infomación de click en aceptar

<img width="414" height="341" alt="Captura de pantalla 2026-04-06 212446" src="https://github.com/user-attachments/assets/2e65a8a7-3d04-4eca-904d-b699404d344d" />

5. **Agregar una transición:** En el panel izquierdo de botones, de click en el botón que contiene una flecha. Aperecerá una venta en la que debe indicar el estado de origen, el estado de destino y el simbolo. Luego de ingresar los datos, de click en aceptar
   
   <img width="453" height="460" alt="Captura de pantalla 2026-04-06 212558" src="https://github.com/user-attachments/assets/dc9f5888-f8d4-45db-9eb1-b9fcd8e73b9a" />

## 2. Cómo cargar archivos (Autómatas o Pruebas)

Si ya tiene un autómata guardado o un lote de pruebas en un archivo, puede cargarlos directamente sin escribirlos a mano:

1. Diríjase a al panel izquierdo y de click en el botón que tiene la imagen de una carpeta abierta.
2. Se abrirá el explorador de archivos de su computadora. Busque y seleccione su archivo de configuración en formato json.
   
<img width="500" height="700" alt="Captura de pantalla 2026-04-06 212916" src="https://github.com/user-attachments/assets/28312144-610d-41a0-9100-21881c105fab" />

4. Haga clic en **"Abrir"**. El sistema leerá el documento y cargará el autómata en el panel correspondiente indicando un mensaje de exito.

<img width="500" height="747" alt="Captura de pantalla 2026-04-06 212924" src="https://github.com/user-attachments/assets/49b127c4-1ae5-43f2-bf82-928a3fa369c2" />

## 3. Cómo evaluar palabras

Una vez ingresado o cargado el autómata, el panel de **Pruebas** se activará:

1. **Ingreso por lote:** Escriba las cadenas que desea probar en el área de texto (una palabra por línea). Si desea evaluar la palabra vacía, utilice el botón **"ε"**.
2. **Evaluación:** Haga clic en **"Evaluar Todas"**.
3. **Resultados:** En la lista inferior aparecerá cada palabra evaluada y su estado (Aceptada / Rechazada).
4. **Simulación:** Seleccione un resultado de la lista y use los botones **"Siguiente Paso"** o **"Reproducir"** para ver cómo el autómata procesa la cadena símbolo por símbolo.
   
<img width="500" height="700" alt="Captura de pantalla 2026-04-07 060519" src="https://github.com/user-attachments/assets/b8eaa745-458a-466e-800d-46b1a7dec2ce" />

5. Para ver la **función de transición** de una cadena de soble click sobre la cadena, y luego en el botón de `ver δ*`. Aparecerá una ventana que muestra la función de transición y permite copiar en portapapeles
   
<img width="751" height="691" alt="Captura de pantalla 2026-04-07 060631" src="https://github.com/user-attachments/assets/ce0f86ae-d07d-4e83-8795-ad367a273733" />

