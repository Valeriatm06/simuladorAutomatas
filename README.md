# Simulador y Analizador de Automatas Finitos (DFA/NFA)

Proyecto Java + JavaFX con arquitectura MVC para crear, visualizar, guardar, cargar y evaluar automatas finitos deterministas y no deterministas.

## Estructura

- `src/main/java/co/edu/uptc/simuladorautomatas/model`: clases `Estado`, `Transicion`, `Automata`, `TipoAutomata`
- `src/main/java/co/edu/uptc/simuladorautomatas/logic`: validacion, evaluacion de cadenas y trazabilidad
- `src/main/java/co/edu/uptc/simuladorautomatas/persistence`: serializacion JSON con Gson
- `src/main/java/co/edu/uptc/simuladorautomatas/ui`: interfaz JavaFX
- `src/main/java/co/edu/uptc/simuladorautomatas/controller`: conexion UI - logica
- `src/main/resources/test-data`: 2 ejemplos DFA en JSON

## Requisitos

- Java 17
- Maven 3.9+

## Ejecutar

```powershell
mvn clean javafx:run
```

## Probar

```powershell
mvn test
```

## Casos DFA incluidos

- `src/main/resources/test-data/dfa_binario_par_0.json`
  - Acepta: `1010`, `11`, `` (cadena vacia)
  - Rechaza: `100`, `0`
- `src/main/resources/test-data/dfa_termina_ab.json`
  - Acepta: `ab`, `aaab`, `babab`
  - Rechaza: `aba`, `bbb`, `a`

## Flujo de uso UI

1. Definir tipo (DFA/NFA) y alfabeto.
2. Crear estados con boton y click en el panel izquierdo.
3. Marcar inicial/aceptacion y crear transiciones.
4. Evaluar hasta 10 cadenas (una por linea).
5. Generar traza para una cadena especifica.
6. Guardar o cargar automata en JSON.

