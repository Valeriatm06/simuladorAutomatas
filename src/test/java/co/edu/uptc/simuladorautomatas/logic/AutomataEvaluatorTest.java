package co.edu.uptc.simuladorautomatas.logic;

import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import co.edu.uptc.simuladorautomatas.persistence.AutomataJsonRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomataEvaluatorTest {

    private final AutomataEvaluator evaluator = new AutomataEvaluator();

    @Test
    void dfaParCerosAceptaYRechaza() throws Exception {
        AutomataJsonRepository repository = new AutomataJsonRepository();
        Path archivo = Path.of("src", "main", "resources", "test-data", "dfa_binario_par_0.json");
        Automata automata = repository.cargar(archivo);

        EvaluacionCadenaResultado aceptada = evaluator.evaluar(automata, "11", true);
        assertTrue(aceptada.isAceptada());
        assertEquals(2, aceptada.getPasos().size());
        assertEquals("q0", aceptada.getEstadosIniciales().get(0));
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
        assertEquals(3, resultado.getPasos().size());
        assertTrue(resultado.getPasos().get(0).getEstadosDestino().contains("q1"));
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
}


