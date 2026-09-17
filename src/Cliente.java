import java.util.concurrent.ThreadLocalRandom;

public class Cliente extends Thread {
    private int id;
    private String nome;
    private int tamanhoCabelo; //Define tempo de corte
    private int tempoChegada;
    private Barbearia barbearia;

    Cliente(int id, String nome, Barbearia barbearia){
        this.id = id;
        this.nome = nome;
        this.barbearia = barbearia;

        tamanhoCabelo = ThreadLocalRandom.current().nextInt(100, 1000);
    }

    public String getNome(){ return nome; }
    public int getTamanhoCabelo(){ return tamanhoCabelo;}

    @Override
    public String toString() {
        return "Cliente: " + this.id + " - " + this.nome;
    }

    public void run() {
        try {
            barbearia.entrar(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
