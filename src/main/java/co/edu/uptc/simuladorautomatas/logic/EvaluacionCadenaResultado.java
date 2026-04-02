package co.edu.uptc.simuladorautomatas.logic;

public class EvaluacionCadenaResultado {
    private final String cadena;
    private final boolean aceptada;
    private final String traza;

    public EvaluacionCadenaResultado(String cadena, boolean aceptada, String traza) {
        this.cadena = cadena;
        this.aceptada = aceptada;
        this.traza = traza;
    }

    public String getCadena() {
        return cadena;
    }

    public boolean isAceptada() {
        return aceptada;
    }

    public String getTraza() {
        return traza;
    }

    public String getEstadoTexto() {
        return aceptada ? "ACEPTADA" : "RECHAZADA";
    }
}

