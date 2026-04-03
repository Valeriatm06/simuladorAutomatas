package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.SimbolosAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.layout.StackPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Motor de dibujado: Responsable de renderizar estados, transiciones y etiquetas en el canvas.
 */
public class AutomataViewDrawing {
    private static final double RADIO_ESTADO = 30;
    private static final double CANVAS_LOGICAL_WIDTH = 900;
    private static final double CANVAS_LOGICAL_HEIGHT = 620;

    private Pane panelDibujo;
    private final List<double[]> zonasEtiquetasTransicion = new ArrayList<>();
    private double escala = 1.0;

    public AutomataViewDrawing(Pane panelDibujo) {
        this.panelDibujo = panelDibujo;
    }

    public void redibujar(Automata automata, String estadoSeleccionado,
                         Set<String> estadosResaltados, Set<String> estadosFinales,
                         Boolean ultimoResultadoAceptado) {
        panelDibujo.getChildren().clear();
        zonasEtiquetasTransicion.clear();
        calcularEscala();

        for (Transicion transicion : automata.getTransiciones()) {
            dibujarTransicion(transicion, estadoSeleccionado);
        }
        for (Estado estado : automata.getEstados()) {
            dibujarEstado(estado, estadoSeleccionado, estadosResaltados, estadosFinales, ultimoResultadoAceptado);
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

        // Halo brillante si está seleccionado
        if (seleccionado) {
            Circle halo = new Circle(x, y, radio + 12, Color.TRANSPARENT);
            halo.setStroke(Color.web("#2563EB"));
            halo.setStrokeWidth(2.0);
            halo.setStyle("-fx-stroke-dash-array: 4 2;");
            panelDibujo.getChildren().add(halo);

            Circle brillo = new Circle(x, y, radio + 6, Color.TRANSPARENT);
            brillo.setStroke(Color.web("rgba(37, 99, 235, 0.3)"));
            brillo.setStrokeWidth(3.0);
            panelDibujo.getChildren().add(brillo);
        }

        if (resaltado) {
            Circle haloEvaluacion = new Circle(x, y, radio + 9, Color.TRANSPARENT);
            haloEvaluacion.setStroke(Color.web("#F59E0B"));
            haloEvaluacion.setStrokeWidth(2.4);
            panelDibujo.getChildren().add(haloEvaluacion);
        }

        if (estadoFinal && ultimoResultadoAceptado != null) {
            Circle haloFinal = new Circle(x, y, radio + 10, Color.TRANSPARENT);
            haloFinal.setStroke(ultimoResultadoAceptado ? Color.web("#16A34A") : Color.web("#DC2626"));
            haloFinal.setStrokeWidth(2.8);
            panelDibujo.getChildren().add(haloFinal);
        }

        Color colorBase = resaltado ? Color.web("#FEF3C7") : Color.web("#F8FAFC");
        if (estadoFinal && ultimoResultadoAceptado != null) {
            colorBase = ultimoResultadoAceptado ? Color.web("#DCFCE7") : Color.web("#FEE2E2");
        }
        
        Circle principal = new Circle(x, y, radio, colorBase);
        principal.setStroke(seleccionado ? Color.web("#2563EB") : Color.web("#1F2937"));
        principal.setStrokeWidth(seleccionado ? 3.0 : 1.7);

        if (estado.isEsInicial() && !estadoFinal) {
            principal.setFill(resaltado ? Color.web("#FDE68A") : Color.web("#E0F2FE"));
        }

        panelDibujo.getChildren().add(principal);

        if (estado.isEsAceptacion()) {
            Circle interno = new Circle(x, y, radio - 6, Color.TRANSPARENT);
            interno.setStroke(seleccionado ? Color.web("#2563EB") : Color.web("#059669"));
            interno.setStrokeWidth(1.8);
            panelDibujo.getChildren().add(interno);
        }

        if (estado.isEsInicial()) {
            Line flecha = new Line(x - (radio + 32), y, x - radio, y);
            flecha.setStroke(Color.web("#0F172A"));
            Polygon punta = new Polygon(
                    x - radio, y,
                    x - radio - 11, y - 6,
                    x - radio - 11, y + 6
            );
            punta.setFill(Color.web("#0F172A"));
            panelDibujo.getChildren().addAll(flecha, punta);
        }

        Text texto = new Text(estado.getNombre());
        texto.setFont(Font.font(14));
        texto.setFill(Color.web("#0F172A"));
        texto.setX(x - (texto.getLayoutBounds().getWidth() / 2));
        texto.setY(y + 4);
        panelDibujo.getChildren().add(texto);
    }

    private void dibujarTransicion(Transicion transicion, String estadoSeleccionado) {
        Estado origen = transicion.getEstadoOrigen();
        Estado destino = transicion.getEstadoDestino();
        double radio = radioEscalado();
        double ox = logicoAVistaX(origen.getX());
        double oy = logicoAVistaY(origen.getY());
        double dx = logicoAVistaX(destino.getX());
        double dy = logicoAVistaY(destino.getY());

        if (origen.equals(destino)) {
            Circle loop = new Circle(ox, oy - radio - 18, 16, Color.TRANSPARENT);
            loop.setStroke(Color.web("#334155"));
            loop.setStrokeWidth(1.6);
            Text etiqueta = new Text(formatearSimboloVisual(transicion.getSimbolo()));
            etiqueta.setFont(Font.font(13));
            double textoW = etiqueta.getLayoutBounds().getWidth();
            double textoH = etiqueta.getLayoutBounds().getHeight();
            double[] pos = ajustarPosicionEtiqueta(ox, oy - radio - 38, textoW, textoH, 0, -1);
            StackPane chip = crearChipEtiqueta(etiqueta, pos[0], pos[1]);
            panelDibujo.getChildren().addAll(loop, chip);
            return;
        }

        double deltaX = dx - ox;
        double deltaY = dy - oy;
        double longitud = Math.hypot(deltaX, deltaY);
        if (longitud == 0) return;

        double ux = deltaX / longitud;
        double uy = deltaY / longitud;
        double px = -uy;
        double py = ux;

        double inicioX = ox + ux * radio;
        double inicioY = oy + uy * radio;
        double finX = dx - ux * radio;
        double finY = dy - uy * radio;

        double curvatura = 18 + Math.min(26, longitud * 0.07);
        double controlX = (inicioX + finX) / 2 + px * curvatura;
        double controlY = (inicioY + finY) / 2 + py * curvatura;

        QuadCurve curva = new QuadCurve(inicioX, inicioY, controlX, controlY, finX, finY);
        curva.setFill(Color.TRANSPARENT);
        curva.setStroke(Color.web("#334155"));
        curva.setStrokeWidth(1.7);

        double tx = finX - controlX;
        double ty = finY - controlY;
        double tLen = Math.hypot(tx, ty);
        if (tLen == 0) return;
        
        double tux = tx / tLen;
        double tuy = ty / tLen;

        Polygon punta = new Polygon(
                finX, finY,
                finX - tux * 14 - tuy * 8, finY - tuy * 14 + tux * 8,
                finX - tux * 14 + tuy * 8, finY - tuy * 14 - tux * 8
        );
        punta.setFill(Color.web("#334155"));

        Text etiqueta = new Text(formatearSimboloVisual(transicion.getSimbolo()));
        etiqueta.setFont(Font.font(13));
        double textoW = etiqueta.getLayoutBounds().getWidth();
        double textoH = etiqueta.getLayoutBounds().getHeight();

        double medioX = (inicioX + (2 * controlX) + finX) / 4.0;
        double medioY = (inicioY + (2 * controlY) + finY) / 4.0;
        double etiquetaOffset = Math.max(14.0, radio * 0.45);
        double baseEtiquetaX = medioX + (px * etiquetaOffset);
        double baseEtiquetaY = medioY + (py * etiquetaOffset);
        double[] pos = ajustarPosicionEtiqueta(baseEtiquetaX, baseEtiquetaY, textoW, textoH, px, py);
        StackPane chip = crearChipEtiqueta(etiqueta, pos[0], pos[1]);

        panelDibujo.getChildren().addAll(curva, punta, chip);
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
}
