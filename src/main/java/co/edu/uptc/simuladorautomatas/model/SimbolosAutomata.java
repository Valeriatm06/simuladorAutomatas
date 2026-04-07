package co.edu.uptc.simuladorautomatas.model;

import java.util.Locale;
/**
 * Contiene las constantes con la palabra vacía,
 * la normalización de los simbolos de entrada y transición, y
 * valicadión de si un símbolo o entrada es epsilon
 */

public final class SimbolosAutomata {
    public static final String EPSILON = "\u03b5";
    private static final String LAMBDA = "\u03bb";

    private SimbolosAutomata() {
    }

    public static boolean esEpsilon(String simbolo) {
        if (simbolo == null) {
            return false;
        }
        String valor = simbolo.trim();
        if (valor.isEmpty()) {
            return false;
        }
        String minuscula = valor.toLowerCase(Locale.ROOT);
        return EPSILON.equals(valor)
                || LAMBDA.equals(valor)
                || "epsilon".equals(minuscula)
                || "lambda".equals(minuscula);
    }

    public static String normalizarSimboloTransicion(String simbolo) {
        String valor = simbolo == null ? "" : simbolo.trim();
        if (esEpsilon(valor)) {
            return EPSILON;
        }
        return valor;
    }

    public static String normalizarCadenaEntrada(String cadena) {
        String valor = cadena == null ? "" : cadena.trim();
        if (esEpsilon(valor)) {
            return "";
        }
        return valor;
    }
}

