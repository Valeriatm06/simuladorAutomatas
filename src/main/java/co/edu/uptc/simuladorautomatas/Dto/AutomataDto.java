package co.edu.uptc.simuladorautomatas.Dto;

import java.util.List;

public class AutomataDto {
    public String tipo;
    public List<String> alfabeto;
    public List<EstadoDto> estados;
    public List<TransicionDto> transiciones;
}

