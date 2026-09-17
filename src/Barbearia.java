import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Barbearia {
    BlockingQueue<Cliente> filaEmPe = new ArrayBlockingQueue<>(13);
    BlockingQueue<Cliente> filaSofa = new ArrayBlockingQueue<>(4);
    BlockingQueue<Cliente> filaPagamento = new ArrayBlockingQueue<>(2);

    private boolean barbAberta;

    public boolean getBarbAberta() {
        return barbAberta;
    }

    public static synchronized void log(String log) {
        System.out.println(log);
    }

    public synchronized void entrar(Cliente cliente) throws InterruptedException {

    }

    public Cliente chamarProximo() throws InterruptedException {
        Cliente cliente = filaSofa.poll(1, TimeUnit.SECONDS);
        if (cliente == null)
            return null;

        log(cliente.getNome() + " foi chamado para cortar o cabelo");

        filaSofa.remove();

        Cliente emPe = filaEmPe.remove();
        if (emPe != null) {
            filaSofa.peek();
            log(emPe.getNome() + " saiu da fila e sentou no sofá");
            filaSofa.add(emPe);
        }

        return cliente;
    }
}