package co.edu.uptc.simuladorautomatas.persistence;

import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.SimbolosAutomata;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutomataJsonRepository {
    private final Gson gson;

    public AutomataJsonRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void guardar(Path archivo, Automata automata) throws IOException {
        AutomataDto dto = toDto(automata);
        try (Writer writer = Files.newBufferedWriter(archivo)) {
            gson.toJson(dto, writer);
        }
    }

    public Automata cargar(Path archivo) throws IOException {
        try (Reader reader = Files.newBufferedReader(archivo)) {
            AutomataDto dto = gson.fromJson(reader, AutomataDto.class);
            if (dto == null) {
                throw new IOException("Archivo JSON vacio o invalido");
            }
            return fromDto(dto);
        }
    }

    private AutomataDto toDto(Automata automata) {
        AutomataDto dto = new AutomataDto();
        dto.tipo = automata.getTipo().name();
        dto.alfabeto = new ArrayList<>(automata.getAlfabeto());
        dto.estados = automata.getEstados().stream().map(e -> {
            EstadoDto estadoDto = new EstadoDto();
            estadoDto.nombre = e.getNombre();
            estadoDto.esInicial = e.isEsInicial();
            estadoDto.esAceptacion = e.isEsAceptacion();
            estadoDto.x = e.getX();
            estadoDto.y = e.getY();
            return estadoDto;
        }).toList();
        dto.transiciones = automata.getTransiciones().stream().map(t -> {
            TransicionDto transicionDto = new TransicionDto();
            transicionDto.origen = t.getEstadoOrigen().getNombre();
            transicionDto.simbolo = SimbolosAutomata.normalizarSimboloTransicion(t.getSimbolo());
            transicionDto.destino = t.getEstadoDestino().getNombre();
            return transicionDto;
        }).toList();
        return dto;
    }

    private Automata fromDto(AutomataDto dto) {
        Automata automata = new Automata(TipoAutomata.valueOf(dto.tipo));
        automata.setAlfabeto(dto.alfabeto == null ? List.of() : dto.alfabeto);

        Map<String, Estado> estadosPorNombre = new HashMap<>();
        if (dto.estados != null) {
            for (EstadoDto estadoDto : dto.estados) {
                Estado estado = new Estado(estadoDto.nombre, estadoDto.esInicial, estadoDto.esAceptacion, estadoDto.x, estadoDto.y);
                automata.agregarEstado(estado);
                estadosPorNombre.put(estado.getNombre(), estado);
            }
        }

        if (dto.transiciones != null) {
            for (TransicionDto transicionDto : dto.transiciones) {
                Estado origen = estadosPorNombre.get(transicionDto.origen);
                Estado destino = estadosPorNombre.get(transicionDto.destino);
                if (origen == null || destino == null) {
                    throw new IllegalStateException("Transicion con estado inexistente en JSON");
                }
                String simbolo = SimbolosAutomata.normalizarSimboloTransicion(transicionDto.simbolo);
                automata.agregarTransicion(new Transicion(origen, simbolo, destino));
            }
        }
        return automata;
    }

    private static class AutomataDto {
        String tipo;
        List<String> alfabeto;
        List<EstadoDto> estados;
        List<TransicionDto> transiciones;
    }

    private static class EstadoDto {
        String nombre;
        boolean esInicial;
        boolean esAceptacion;
        double x;
        double y;
    }

    private static class TransicionDto {
        String origen;
        String simbolo;
        String destino;
    }
}

