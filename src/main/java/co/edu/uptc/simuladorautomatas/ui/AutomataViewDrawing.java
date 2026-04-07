package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.SimbolosAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Motor de dibujado: Responsable de renderizar estados, transiciones y etiquetas en el canvas.
 * Refactorizado para usar el máximo potencial de JavaFX (Grupos, Capas y Drag dinámico).
 */
public class AutomataViewDrawing {
    private static final double RADIO_ESTADO = 30;
    private static final double CANVAS_LOGICAL_WIDTH = 900;
    private static final double CANVAS_LOGICAL_HEIGHT = 620;

    private Pane panelDibujo;
    private final List<double[]> zonasEtiquetasTransicion = new ArrayList<>();
    private double escala = 1.0;

    // CAPAS DE RENDERIZADO: Permiten redibujar flechas sin destruir los estados
    private final Group capaTransiciones = new Group();
    private final Group capaEstados = new Group();
    
    // CACHÉ DE REFERENCIA: Para poder redibujar las líneas en tiempo real mientras se arrastra
    private Automata automataActual;
    
    // Referencia al gestor de simulación para obtener colores de transiciones
    private AutomataViewSimulation simulationManager;

    public AutomataViewDrawing(Pane panelDibujo) {
        this.panelDibujo = panelDibujo;
    }

    public void setSimulationManager(AutomataViewSimulation simulationManager) {
        this.simulationManager = simulationManager;
    }

    public void redibujar(Automata automata, String estadoSeleccionado,
                          Set<String> estadosResaltados, Set<String> estadosFinales,
                          Boolean ultimoResultadoAceptado) {
        this.automataActual = automata;
        
        // Configuramos las capas por primera vez o reemplazamos el contenido anterior
        panelDibujo.getChildren().setAll(capaTransiciones, capaEstados);
        capaEstados.getChildren().clear();

        calcularEscala();

        // 1. Dibujamos los estados en su capa superior
        for (Estado estado : automata.getEstados()) {
            dibujarEstado(estado, estadoSeleccionado, estadosResaltados, estadosFinales, ultimoResultadoAceptado);
        }

        // 2. Dibujamos las transiciones en la capa inferior
        redibujarTransiciones();
        
        // 3. Agregamos el ícono de información en la esquina superior
        agregarIconoInformacion(automata);
    }

    /**
     * Agrega un ícono de información en la esquina superior del panel
     */
    private void agregarIconoInformacion(Automata automata) {
        Label iconoInfo = new Label("ⓘ");
        iconoInfo.setStyle("-fx-font-size: 18px; -fx-text-fill: #2563EB; -fx-cursor: hand;");
        iconoInfo.setLayoutX(10);
        iconoInfo.setLayoutY(10);
        
        String quintupla = generarQuintupla(automata);
        Tooltip tooltip = new Tooltip(quintupla);
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setHideDelay(Duration.millis(100));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(400);
        tooltip.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        Tooltip.install(iconoInfo, tooltip);
        
        capaEstados.getChildren().add(iconoInfo);
    }

    /**
     * Genera la quintupla del autómata en formato legible
     */
    private String generarQuintupla(Automata automata) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Quintupla del Autómata\n");
        sb.append("═════════════════════════\n\n");
        
        // Tipo de autómata
        sb.append("Tipo: ").append(automata.getTipo().name()).append("\n\n");
        
        // Alfabeto
        sb.append("Σ (Alfabeto):\n");
        List<String> alfabeto = automata.getAlfabeto();
        if (alfabeto.isEmpty()) {
            sb.append("  (vacío)\n");
        } else {
            sb.append("  {").append(String.join(", ", alfabeto)).append("}\n");
        }
        sb.append("\n");
        
        // Conjunto de estados
        sb.append("Q (Estados):\n");
        List<Estado> estados = automata.getEstados();
        if (estados.isEmpty()) {
            sb.append("  (vacío)\n");
        } else {
            sb.append("  {").append(String.join(", ", 
                    estados.stream().map(Estado::getNombre).toList())).append("}\n");
        }
        sb.append("\n");
        
        // Estado inicial
        sb.append("q₀ (Estado Inicial):\n");
        Estado inicial = automata.getEstadoInicial();
        if (inicial != null) {
            sb.append("  ").append(inicial.getNombre()).append("\n");
        } else {
            sb.append("  (no definido)\n");
        }
        sb.append("\n");
        
        // Estados de aceptación
        sb.append("F (Estados de Aceptación):\n");
        List<Estado> aceptacion = estados.stream()
                .filter(Estado::isEsAceptacion)
                .toList();
        if (aceptacion.isEmpty()) {
            sb.append("  (ninguno)\n");
        } else {
            sb.append("  {").append(String.join(", ", 
                    aceptacion.stream().map(Estado::getNombre).toList())).append("}\n");
        }
        
        return sb.toString();
    }

    /**
     * Extraído para poder llamar solo a la actualización de flechas durante el arrastre (Drag)
     */
    private void redibujarTransiciones() {
        capaTransiciones.getChildren().clear();
        zonasEtiquetasTransicion.clear();
        
        if (automataActual == null) return;

        for (Map.Entry<AristaKey, String> entrada : agruparTransiciones(automataActual.getTransiciones()).entrySet()) {
            AristaKey arista = entrada.getKey();
            dibujarTransicion(arista.origen, arista.destino, entrada.getValue());
        }
    }

    private void dibujarEstado(Estado estado, String estadoSeleccionado,
                               Set<String> estadosResaltados, Set<String> estadosFinales,
                               Boolean ultimoResultadoAceptado) {
        boolean seleccionado = estado.getNombre().equals(estadoSeleccionado);
        boolean resaltado = estadosResaltados.contains(estado.getNombre());
        boolean estadoFinal = estadosFinales.contains(estado.getNombre());
        
        double x = logicoAVistaX(estado.getX());
        double y = logicoAVistaY(estado.getY());
        double radio = radioEscalado();

        // CONTENEDOR MAESTRO: Todo se dibuja relativo al centro (0,0) del grupo
        Group grupoEstado = new Group();
        grupoEstado.setLayoutX(x);
        grupoEstado.setLayoutY(y);

        if (seleccionado) {
            Circle halo = new Circle(0, 0, radio + 12, Color.TRANSPARENT);
            halo.setStroke(Color.web("#2563EB"));
            halo.setStrokeWidth(2.0);
            halo.setStyle("-fx-stroke-dash-array: 4 2;");
            
            Circle brillo = new Circle(0, 0, radio + 6, Color.TRANSPARENT);
            brillo.setStroke(Color.web("rgba(37, 99, 235, 0.3)"));
            brillo.setStrokeWidth(3.0);
            
            grupoEstado.getChildren().addAll(halo, brillo);
        }

        if (resaltado) {
            Circle haloEvaluacion = new Circle(0, 0, radio + 9, Color.TRANSPARENT);
            haloEvaluacion.setStroke(Color.web("#F59E0B"));
            haloEvaluacion.setStrokeWidth(2.4);
            grupoEstado.getChildren().add(haloEvaluacion);
        }

        if (estadoFinal && ultimoResultadoAceptado != null) {
            Circle haloFinal = new Circle(0, 0, radio + 10, Color.TRANSPARENT);
            haloFinal.setStroke(ultimoResultadoAceptado ? Color.web("#16A34A") : Color.web("#DC2626"));
            haloFinal.setStrokeWidth(2.8);
            grupoEstado.getChildren().add(haloFinal);
        }

        Color colorBase = resaltado ? Color.web("#FEF3C7") : Color.web("#F8FAFC");
        if (estadoFinal && ultimoResultadoAceptado != null) {
            colorBase = ultimoResultadoAceptado ? Color.web("#DCFCE7") : Color.web("#FEE2E2");
        }
        
        Circle principal = new Circle(0, 0, radio, colorBase);
        principal.setStroke(seleccionado ? Color.web("#2563EB") : Color.web("#1F2937"));
        principal.setStrokeWidth(seleccionado ? 3.0 : 1.7);

        if (estado.isEsInicial() && !estadoFinal) {
            principal.setFill(resaltado ? Color.web("#FDE68A") : Color.web("#E0F2FE"));
        }

        grupoEstado.getChildren().add(principal);

        if (estado.isEsAceptacion()) {
            Circle interno = new Circle(0, 0, radio - 6, Color.TRANSPARENT);
            interno.setStroke(seleccionado ? Color.web("#2563EB") : Color.web("#059669"));
            interno.setStrokeWidth(1.8);
            grupoEstado.getChildren().add(interno);
        }

        if (estado.isEsInicial()) {
            Line flecha = new Line(-(radio + 32), 0, -radio, 0);
            flecha.setStroke(Color.web("#0F172A"));
            Polygon punta = new Polygon(
                    -radio, 0,
                    -radio - 11, -6,
                    -radio - 11, 6
            );
            punta.setFill(Color.web("#0F172A"));
            grupoEstado.getChildren().addAll(flecha, punta);
        }

        Text texto = new Text(estado.getNombre());
        texto.setFont(Font.font(14));
        texto.setFill(Color.web("#0F172A"));
        // Centrado del texto relativo al 0,0
        texto.setX(-texto.getLayoutBounds().getWidth() / 2);
        texto.setY(4);
        grupoEstado.getChildren().add(texto);

        // --- SISTEMA DE ARRASTRE (DRAG & DROP DINÁMICO) ---
        final double[] dragOffset = new double[2];

        grupoEstado.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX() - grupoEstado.getLayoutX();
            dragOffset[1] = e.getSceneY() - grupoEstado.getLayoutY();
            grupoEstado.getScene().setCursor(Cursor.CLOSED_HAND);
            e.consume();
        });

        grupoEstado.setOnMouseDragged(e -> {
            double nuevoLayoutX = e.getSceneX() - dragOffset[0];
            double nuevoLayoutY = e.getSceneY() - dragOffset[1];

            // 1. Movemos visualmente el grupo
            grupoEstado.setLayoutX(nuevoLayoutX);
            grupoEstado.setLayoutY(nuevoLayoutY);

            // 2. Actualizamos el modelo real con las nuevas coordenadas lógicas
            estado.setX(vistaALogicoX(nuevoLayoutX));
            estado.setY(vistaALogicoY(nuevoLayoutY));

            // 3. Redibujamos SOLO las flechas para que sigan al nodo mágicamente
            redibujarTransiciones();
            e.consume();
        });

        grupoEstado.setOnMouseReleased(e -> grupoEstado.getScene().setCursor(Cursor.HAND));
        grupoEstado.setOnMouseEntered(e -> {
            if (!e.isPrimaryButtonDown()) panelDibujo.getScene().setCursor(Cursor.HAND);
        });
        grupoEstado.setOnMouseExited(e -> {
            if (!e.isPrimaryButtonDown()) panelDibujo.getScene().setCursor(Cursor.DEFAULT);
        });

        capaEstados.getChildren().add(grupoEstado);
    }

    private void dibujarTransicion(Estado origen, Estado destino, String etiquetaSimbolos) {
        double radio = radioEscalado();
        double ox = logicoAVistaX(origen.getX());
        double oy = logicoAVistaY(origen.getY());
        double dx = logicoAVistaX(destino.getX());
        double dy = logicoAVistaY(destino.getY());
        
        // Obtener color de la transición si está resaltada; caso contrario, color por defecto
        Color colorStroke = Color.web("#334155");
        if (simulationManager != null) {
            Color colorResaltado = simulationManager.obtenerColorTransicion(origen.getNombre(), destino.getNombre());
            if (colorResaltado != null) {
                colorStroke = colorResaltado;
            }
        }

        if (origen.equals(destino)) {
            // --- TRANSICIÓN BUCLE (SELF-LOOP) MÁS PEQUEÑA ---
            // Ángulos más cerrados hacia arriba para que la base del bucle sea más angosta
            double anguloSalida = Math.toRadians(-115); 
            double anguloEntrada = Math.toRadians(-65);

            double startX = ox + radio * Math.cos(anguloSalida);
            double startY = oy + radio * Math.sin(anguloSalida);
            double endX = ox + radio * Math.cos(anguloEntrada);
            double endY = oy + radio * Math.sin(anguloEntrada);

            // Reducimos drásticamente la altura y la anchura del lazo
            double alturaLazo = radio * 1.8; // Antes era 3.5
            double anchuraLazo = radio * 0.8; // Antes era 1.5
            
            double c1X = startX - anchuraLazo;
            double c1Y = startY - alturaLazo;
            
            double c2X = endX + anchuraLazo;
            double c2Y = endY - alturaLazo;

            javafx.scene.shape.CubicCurve loop = new javafx.scene.shape.CubicCurve(
                    startX, startY, c1X, c1Y, c2X, c2Y, endX, endY);
            loop.setFill(Color.TRANSPARENT);
            loop.setStroke(colorStroke);
            loop.setStrokeWidth(1.6);

            double arrDirX = endX - c2X;
            double arrDirY = endY - c2Y;
            double arrLen = Math.hypot(arrDirX, arrDirY);
            double tux = arrDirX / arrLen;
            double tuy = arrDirY / arrLen;

            Polygon flecha = new Polygon(
                    endX, endY,
                    endX - tux * 12 - tuy * 6, endY - tuy * 12 + tux * 6,
                    endX - tux * 12 + tuy * 6, endY - tuy * 12 - tux * 6
            );
            flecha.setFill(colorStroke);

            Text etiqueta = new Text(etiquetaSimbolos);
            etiqueta.setFont(Font.font(13));
            double textoW = etiqueta.getLayoutBounds().getWidth();
            double textoH = etiqueta.getLayoutBounds().getHeight();
            
            // Ajustamos también la posición de la etiqueta para que baje junto con el bucle
            double[] pos = ajustarPosicionEtiqueta(ox, oy - alturaLazo + (radio * 0.3), textoW, textoH, 0, -1);
            StackPane chip = crearChipEtiqueta(etiqueta, pos[0], pos[1]);
            
            capaTransiciones.getChildren().addAll(loop, flecha, chip);
            return;
        }

        // --- TRANSICIÓN CURVA ENTRE DOS ESTADOS ---
        double deltaX = dx - ox;
        double deltaY = dy - oy;
        double distCentroACentro = Math.hypot(deltaX, deltaY);
        if (distCentroACentro == 0) return;

        double nx = deltaX / distCentroACentro;
        double ny = deltaY / distCentroACentro;
        
        double px = -ny;
        double py = nx;

        double curvatura = 18 + Math.min(26, distCentroACentro * 0.07);
        double controlX = (ox + dx) / 2 + px * curvatura;
        double controlY = (oy + dy) / 2 + py * curvatura;

        double vecInicioX = controlX - ox;
        double vecInicioY = controlY - oy;
        double distInicio = Math.hypot(vecInicioX, vecInicioY);
        double inicioX = ox + (vecInicioX / distInicio) * radio;
        double inicioY = oy + (vecInicioY / distInicio) * radio;

        double vecFinX = controlX - dx;
        double vecFinY = controlY - dy;
        double distFin = Math.hypot(vecFinX, vecFinY);
        double finX = dx + (vecFinX / distFin) * radio;
        double finY = dy + (vecFinY / distFin) * radio;

        javafx.scene.shape.QuadCurve curva = new javafx.scene.shape.QuadCurve(inicioX, inicioY, controlX, controlY, finX, finY);
        curva.setFill(Color.TRANSPARENT);
        curva.setStroke(colorStroke);
        curva.setStrokeWidth(1.7);

        double tx = finX - controlX;
        double ty = finY - controlY;
        double tLen = Math.hypot(tx, ty);
        if (tLen == 0) return;
        
        double tux = tx / tLen;
        double tuy = ty / tLen;

        Polygon punta = new Polygon(
                finX, finY,
                finX - tux * 12 - tuy * 6, finY - tuy * 12 + tux * 6,
                finX - tux * 12 + tuy * 6, finY - tuy * 12 - tux * 6
        );
        punta.setFill(colorStroke);

        Text etiqueta = new Text(etiquetaSimbolos);
        etiqueta.setFont(Font.font(13));
        double textoW = etiqueta.getLayoutBounds().getWidth();
        double textoH = etiqueta.getLayoutBounds().getHeight();

        double medioX = (inicioX + (2 * controlX) + finX) / 4.0;
        double medioY = (inicioY + (2 * controlY) + finY) / 4.0;
        double etiquetaOffset = 18.0; 
        double baseEtiquetaX = medioX + (px * etiquetaOffset);
        double baseEtiquetaY = medioY + (py * etiquetaOffset);
        
        double[] pos = ajustarPosicionEtiqueta(baseEtiquetaX, baseEtiquetaY, textoW, textoH, px, py);
        StackPane chip = crearChipEtiqueta(etiqueta, pos[0], pos[1]);

        capaTransiciones.getChildren().addAll(curva, punta, chip);
    }

    private Map<AristaKey, String> agruparTransiciones(List<Transicion> transiciones) {
        Map<AristaKey, LinkedHashSet<String>> agrupadas = new LinkedHashMap<>();
        for (Transicion transicion : transiciones) {
            AristaKey key = new AristaKey(transicion.getEstadoOrigen(), transicion.getEstadoDestino());
            String simbolo = formatearSimboloVisual(transicion.getSimbolo());
            agrupadas.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(simbolo);
        }

        Map<AristaKey, String> etiquetas = new LinkedHashMap<>();
        for (Map.Entry<AristaKey, LinkedHashSet<String>> entrada : agrupadas.entrySet()) {
            etiquetas.put(entrada.getKey(), String.join(", ", entrada.getValue()));
        }
        return etiquetas;
    }

    private StackPane crearChipEtiqueta(Text etiquetaTexto, double centerX, double centerY) {
        etiquetaTexto.setFill(Color.web("#1D4ED8"));
        double textoW = etiquetaTexto.getLayoutBounds().getWidth();
        double textoH = etiquetaTexto.getLayoutBounds().getHeight();

        Rectangle fondo = new Rectangle(textoW + 10, textoH + 6);
        fondo.setArcWidth(10);
        fondo.setArcHeight(10);
        fondo.setFill(Color.web("rgba(248, 250, 252, 0.95)"));
        fondo.setStroke(Color.web("#CBD5E1"));
        fondo.setStrokeWidth(0.7);

        StackPane chip = new StackPane(fondo, etiquetaTexto);
        chip.setLayoutX(centerX - ((textoW + 10) / 2));
        chip.setLayoutY(centerY - ((textoH + 6) / 2));
        return chip;
    }

    private double[] ajustarPosicionEtiqueta(double centerX, double centerY, double textoW, double textoH,
                                             double normalX, double normalY) {
        double x = centerX;
        double y = centerY;
        double ancho = textoW + 10;
        double alto = textoH + 6;

        for (int intento = 0; intento < 7; intento++) {
            double left = x - (ancho / 2.0);
            double top = y - (alto / 2.0);
            double right = left + ancho;
            double bottom = top + alto;

            if (!colisionaEtiqueta(left, top, right, bottom)) {
                zonasEtiquetasTransicion.add(new double[] {left, top, right, bottom});
                return new double[] {x, y};
            }

            double signo = (intento % 2 == 0) ? 1.0 : -1.0;
            double paso = 10 + (intento * 5);
            x += normalX * paso * signo;
            y += normalY * paso * signo;
        }

        double left = x - (ancho / 2.0);
        double top = y - (alto / 2.0);
        zonasEtiquetasTransicion.add(new double[] {left, top, left + ancho, top + alto});
        return new double[] {x, y};
    }

    private boolean colisionaEtiqueta(double left, double top, double right, double bottom) {
        for (double[] zona : zonasEtiquetasTransicion) {
            boolean separada = right < zona[0] || left > zona[2] || bottom < zona[1] || top > zona[3];
            if (!separada) {
                return true;
            }
        }
        return false;
    }

    private void calcularEscala() {
        if (panelDibujo.getWidth() <= 0 || panelDibujo.getHeight() <= 0) {
            escala = 1.0;
        } else {
            double sx = panelDibujo.getWidth() / CANVAS_LOGICAL_WIDTH;
            double sy = panelDibujo.getHeight() / CANVAS_LOGICAL_HEIGHT;
            escala = Math.max(0.72, Math.min(sx, sy));
        }
    }

    private double radioEscalado() {
        return RADIO_ESTADO * escala;
    }

    private double logicoAVistaX(double x) {
        return x * escala;
    }

    private double logicoAVistaY(double y) {
        return y * escala;
    }

    public double vistaALogicoX(double x) {
        return x / escala;
    }

    public double vistaALogicoY(double y) {
        return y / escala;
    }

    public double getRadioEscalado() {
        return radioEscalado();
    }

    public double getEscala() {
        return escala;
    }

    private String formatearSimboloVisual(String simbolo) {
        String normalizado = SimbolosAutomata.normalizarSimboloTransicion(simbolo);
        return SimbolosAutomata.esEpsilon(normalizado) ? SimbolosAutomata.EPSILON : normalizado;
    }

    private static final class AristaKey {
        private final Estado origen;
        private final Estado destino;

        private AristaKey(Estado origen, Estado destino) {
            this.origen = origen;
            this.destino = destino;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AristaKey that)) {
                return false;
            }
            return origen.equals(that.origen) && destino.equals(that.destino);
        }

        @Override
        public int hashCode() {
            return 31 * origen.hashCode() + destino.hashCode();
        }
    }
}