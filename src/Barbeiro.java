public class Barbeiro extends Thread {
    private String nome;
    private Barbearia barbearia;
    private int totalTrabalhado;

    public Barbeiro(String nome, Barbearia barb) {
        this.nome = nome;
        this.barbearia = barb;
    }

    public String getNome() {
        return nome;
    }

    public synchronized void cortar(Cliente cliente) throws InterruptedException {
        int duraçãoCorte = cliente.getTamanhoCabelo();

        Barbearia.log("Barbeiro " + nome + " está cortando o cabelo do cliente " + cliente.getNome());
        Thread.sleep(duraçãoCorte);
        Barbearia.log("Barbeiro " + nome + " terminou o corte do cliente " + cliente.getNome());
    }

    @Override
    public void run() {
        try {
            while (barbearia.getBarbAberta()) {
                Cliente proximo = barbearia.chamarProximo();

                if (proximo == null)
                    continue;

                cortar(proximo);
                // barbearia.pagar(proximo, this);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
