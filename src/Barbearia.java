import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

public class Barbearia {
    private final ArrayDeque<Cliente> filaEmPe = new ArrayDeque<>();
    private final ArrayDeque<Cliente> filaSofa = new ArrayDeque<>();

    private final Semaphore caixa = new Semaphore(1, true);

    private static final int capacidadeSofa = 4;
    private static final int capacidadePe = 13;
    private static final int cadeirasBarbeiro = 3;
    private static final int capacidadeTotal = capacidadeSofa + capacidadePe + cadeirasBarbeiro;

    private boolean barbAberta = true;
    private int totalClientes;

    public boolean getBarbAberta() {
        return barbAberta;
    }

    public int getTotalClientes() {
        return totalClientes;
    }

    public synchronized void fechar() {
        barbAberta = false;
        notifyAll();
    }

    public static synchronized void log(String log) {
        System.out.println(log);
    }

    public synchronized void registrarAtendimento() {
        totalClientes++;
    }

    public synchronized void entrar(Cliente cliente) throws InterruptedException {
        boolean estaLotado = (filaEmPe.size() + filaSofa.size() + cadeirasBarbeiro) >= capacidadeTotal;

        while (!barbAberta || estaLotado) {
            log(cliente + " achou a barbearia cheia/fechada e foi embora");
            return;
        }

        filaEmPe.addLast(cliente);
        Metricas.chegou(cliente); // [MÉTRICAS] início da espera em pé
        log(cliente + " entrou e está esperando");

        while (filaSofa.size() >= capacidadeSofa && filaEmPe.peekFirst() != cliente) {
            wait(); // espera vaga no sofá
        }

        filaEmPe.remove(cliente);
        filaSofa.addLast(cliente);
        Metricas.sentouNoSofa(cliente); // [MÉTRICAS] fim da espera em pé / início da espera no sofá
        log(cliente.getNome() + " sentou no sofá");

        notifyAll();
    }

    public synchronized Cliente chamarProximo() throws InterruptedException {
        while (filaSofa.isEmpty() && barbAberta) {
            wait(1000); // espera por alguém no sofá
        }
        if (filaSofa.isEmpty())
            return null;

        Cliente cliente = filaSofa.pollFirst();
        Metricas.chamado(cliente, Thread.currentThread()); // [MÉTRICAS] fim da espera no sofá / barbeiro ocupado
        log(cliente.getNome() + " foi chamado para cortar o cabelo");
        notifyAll(); // libera vaga no sofá pra quem está em pé

        return cliente;
    }

    public void pagar(Cliente cliente, Barbeiro barbeiro) throws InterruptedException {
        Metricas.pediuCaixa(cliente); // [MÉTRICAS] início da espera na fila da POS
        caixa.acquire();
        Metricas.obteveCaixa(cliente); // [MÉTRICAS] fim da espera na fila da POS

        try {
            log(cliente.getNome() + " está pagando com " + barbeiro.getNome());
            Thread.sleep(100);
            log(cliente.getNome() + " terminou de pagar e saiu");
            Metricas.terminou(cliente, barbeiro); // [MÉTRICAS] barbeiro livre / fim do atendimento
        } finally {
            caixa.release();
        }
    }
}