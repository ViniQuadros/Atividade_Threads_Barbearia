import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Coleta e imprime as métricas de tempo da barbearia 
 * 
 * O relatório é impresso automaticamente quando a JVM encerra (shutdown hook),
 * ou seja, logo depois da última linha que a Main imprime.
 *
 * Todos os tempos são medidos com System.nanoTime() 
 */
public final class Metricas {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final Object LOCK = new Object();

    /** Marcas de tempo de cada cliente (ns). */
    private static final class Registro {
        long entrouEmPe;
        long sentouNoSofa;
        long pediuCaixa;
    }

    private static final Map<Cliente, Registro> registros = new HashMap<>();

    /** Tempo ocupado de cada barbeiro: [0] = início do atendimento atual, [1] = acumulado (ns). */
    private static final Map<String, long[]> barbeiros = new TreeMap<>();

    // Acumuladores das médias (ns) e contadores
    private static long somaEsperaPe, qtdEsperaPe;
    private static long somaEsperaSofa, qtdEsperaSofa;
    private static long somaEsperaPos, qtdEsperaPos;
    private static int clientesEntraram, clientesAtendidos;

    // Janela de tempo do experimento: primeira chegada -> fim do último pagamento
    private static boolean iniciou = false;
    private static long tInicio, tFim;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Metricas::imprimir, "Metricas-Relatorio"));
    }

    private Metricas() {
    }

    // ---------------------------------------------------------------- eventos

    /** Cliente entrou e foi para a fila em pé. */
    public static void chegou(Cliente c) {
        long agora = System.nanoTime();
        synchronized (LOCK) {
            if (!iniciou) {
                iniciou = true;
                tInicio = agora;
            }
            clientesEntraram++;
            Registro r = new Registro();
            r.entrouEmPe = agora;
            registros.put(c, r);
        }
    }

    /** Cliente saiu da fila em pé e sentou no sofá. */
    public static void sentouNoSofa(Cliente c) {
        long agora = System.nanoTime();
        synchronized (LOCK) {
            Registro r = registros.get(c);
            if (r == null)
                return;
            r.sentouNoSofa = agora;
            somaEsperaPe += agora - r.entrouEmPe;
            qtdEsperaPe++;
        }
    }

    /** Cliente saiu do sofá, chamado pelo barbeiro (a thread atual é o próprio Barbeiro). */
    public static void chamado(Cliente c, Thread thread) {
        long agora = System.nanoTime();
        synchronized (LOCK) {
            Registro r = registros.get(c);
            if (r != null) {
                somaEsperaSofa += agora - r.sentouNoSofa;
                qtdEsperaSofa++;
            }
            if (thread instanceof Barbeiro) {
                long[] b = barbeiros.computeIfAbsent(((Barbeiro) thread).getNome(), k -> new long[2]);
                b[0] = agora; // barbeiro passa a ficar ocupado com esse cliente
            }
        }
    }

    /** Cliente terminou o corte e entrou na fila da POS (vai tentar pegar o caixa). */
    public static void pediuCaixa(Cliente c) {
        long agora = System.nanoTime();
        synchronized (LOCK) {
            Registro r = registros.get(c);
            if (r != null)
                r.pediuCaixa = agora;
        }
    }

    /** Cliente conseguiu a POS (terminou a espera na fila do caixa). */
    public static void obteveCaixa(Cliente c) {
        long agora = System.nanoTime();
        synchronized (LOCK) {
            Registro r = registros.get(c);
            if (r == null)
                return;
            somaEsperaPos += agora - r.pediuCaixa;
            qtdEsperaPos++;
        }
    }

    /** Pagamento concluído: barbeiro fica livre de novo e o cliente vai embora. */
    public static void terminou(Cliente c, Barbeiro barbeiro) {
        long agora = System.nanoTime();
        synchronized (LOCK) {
            long[] b = barbeiros.computeIfAbsent(barbeiro.getNome(), k -> new long[2]);
            b[1] += agora - b[0];
            clientesAtendidos++;
            tFim = agora;
            registros.remove(c);
        }
    }

    // ------------------------------------------------------------- relatório

    private static String ms(long somaNs, long qtd) {
        double media = qtd == 0 ? 0 : (somaNs / 1_000_000.0) / qtd;
        return String.format(PT_BR, "%.2f ms", media);
    }

    private static void imprimir() {
        synchronized (LOCK) {
            if (!iniciou || clientesAtendidos == 0)
                return;

            long janela = tFim - tInicio;

            StringBuilder util = new StringBuilder();
            for (Map.Entry<String, long[]> e : barbeiros.entrySet()) {
                double pct = janela == 0 ? 0 : 100.0 * e.getValue()[1] / janela;
                if (util.length() > 0)
                    util.append(" | ");
                util.append(String.format(PT_BR, "B%s: %.1f%%", e.getKey(), pct));
            }

            System.out.println();
            System.out.println("================ MÉTRICAS DE TEMPO ================");
            System.out.println(String.format(PT_BR, "Tempo total para atender %d clientes : %.3f s",
                    clientesAtendidos, janela / 1_000_000_000.0));
            System.out.println("Tempo médio de espera em pé          : " + ms(somaEsperaPe, qtdEsperaPe));
            System.out.println("Tempo médio de espera no sofá        : " + ms(somaEsperaSofa, qtdEsperaSofa));
            System.out.println("Tempo médio de espera na fila da POS : " + ms(somaEsperaPos, qtdEsperaPos));
            System.out.println("Taxa de utilização dos barbeiros     : " + util);
            System.out.println("===================================================");

            if (clientesEntraram > clientesAtendidos) {
                System.out.println("Obs.: " + (clientesEntraram - clientesAtendidos)
                        + " cliente(s) entraram na barbearia mas não chegaram a ser atendidos"
                        + " (a barbearia fechou antes).");
            }
        }
    }
}