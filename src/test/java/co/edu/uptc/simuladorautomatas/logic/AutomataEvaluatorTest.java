package co.edu.uptc.simuladorautomatas.logic;

import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import co.edu.uptc.simuladorautomatas.persistence.AutomataJsonRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomataEvaluatorTest {

    private final AutomataEvaluator evaluator = new AutomataEvaluator();

    @Test
    void dfaParCerosAceptaYRechaza() throws Exception {
        AutomataJsonRepository repository = new AutomataJsonRepository();
        Path archivo = Path.of("src", "main", "resources", "test-data", "dfa_binario_par_0.json");
        Automata automata = repository.cargar(archivo);

        assertTrue(evaluator.evaluar(automata, "11", false).isAceptada());
        assertTrue(!evaluator.evaluar(automata, "10", false).isAceptada());
    }

    @Test
    void nfaBasicoFuncionaConTraza() {
        Automata nfa = new Automata(TipoAutomata.NFA);
        nfa.setAlfabeto(List.of("a", "b"));

        Estado q0 = new Estado("q0", true, false, 0, 0);
        Estado q1 = new Estado("q1", false, true, 0, 0);
        nfa.agregarEstado(q0);
        nfa.agregarEstado(q1);
        nfa.agregarTransicion(new Transicion(q0, "a", q0));
        nfa.agregarTransicion(new Transicion(q0, "a", q1));
        nfa.agregarTransicion(new Transicion(q1, "b", q1));

        EvaluacionCadenaResultado resultado = evaluator.evaluar(nfa, "aab", true);
        assertTrue(resultado.isAceptada());
        assertTrue(resultado.getTraza().contains("(q0, a)"));
    }

    @Test
    void evaluacionLoteLimitaADiez() {
        Automata dfa = new Automata(TipoAutomata.DFA);
        dfa.setAlfabeto(List.of("a"));
        Estado q0 = new Estado("q0", true, true, 0, 0);
        dfa.agregarEstado(q0);
        dfa.agregarTransicion(new Transicion(q0, "a", q0));

        List<EvaluacionCadenaResultado> resultados = evaluator.evaluarLote(dfa, List.of("", "a", "aa"));
        assertEquals(3, resultados.size());
        assertTrue(resultados.stream().allMatch(EvaluacionCadenaResultado::isAceptada));
    }

    @Test
    void palabraVaciaSePuedeIngresarComoEpsilonOLambda() {
        Automata dfa = new Automata(TipoAutomata.DFA);
        dfa.setAlfabeto(List.of("a"));
        Estado q0 = new Estado("q0", true, true, 0, 0);
        dfa.agregarEstado(q0);
        dfa.agregarTransicion(new Transicion(q0, "a", q0));

        assertTrue(evaluator.evaluar(dfa, "epsilon", false).isAceptada());
        assertTrue(evaluator.evaluar(dfa, "lambda", false).isAceptada());
        assertTrue(evaluator.evaluar(dfa, "\u03b5", false).isAceptada());
    }

    @Test
    void nfaConTransicionEpsilonEvaluaCierreYdFARechazaEpsilon() {
        Automata nfa = new Automata(TipoAutomata.NFA);
        nfa.setAlfabeto(List.of("a"));

        Estado q0 = new Estado("q0", true, false, 0, 0);
        Estado q1 = new Estado("q1", false, true, 0, 0);
        nfa.agregarEstado(q0);
        nfa.agregarEstado(q1);
        nfa.agregarTransicion(new Transicion(q0, "epsilon", q1));

        assertTrue(evaluator.evaluar(nfa, "", false).isAceptada());
        assertTrue(evaluator.evaluar(nfa, "\u03b5", false).isAceptada());

        Automata dfa = new Automata(TipoAutomata.DFA);
        dfa.setAlfabeto(List.of("a"));
        Estado d0 = new Estado("d0", true, false, 0, 0);
        Estado d1 = new Estado("d1", false, true, 0, 0);
        dfa.agregarEstado(d0);
        dfa.agregarEstado(d1);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> dfa.agregarTransicion(new Transicion(d0, "lambda", d1))
        );
        assertFalse(error.getMessage().isBlank());
    }

    @Test
    void persistenciaJsonMantieneTransicionEpsilon() throws Exception {
        Automata nfa = new Automata(TipoAutomata.NFA);
        nfa.setAlfabeto(List.of("a", "epsilon"));

        Estado q0 = new Estado("q0", true, false, 0, 0);
        Estado q1 = new Estado("q1", false, true, 0, 0);
        nfa.agregarEstado(q0);
        nfa.agregarEstado(q1);
        nfa.agregarTransicion(new Transicion(q0, "lambda", q1));

        AutomataJsonRepository repository = new AutomataJsonRepository();
        Path archivo = Files.createTempFile("automata-epsilon-", ".json");
        try {
            repository.guardar(archivo, nfa);
            Automata cargado = repository.cargar(archivo);

            assertEquals(1, cargado.getTransiciones().size());
            assertEquals("ε", cargado.getTransiciones().get(0).getSimbolo());
            assertTrue(cargado.getAlfabeto().stream().noneMatch(s -> s.equalsIgnoreCase("epsilon") || s.equals("ε")));
            assertTrue(evaluator.evaluar(cargado, "", false).isAceptada());
        } finally {
            Files.deleteIfExists(archivo);
        }
    }
}


