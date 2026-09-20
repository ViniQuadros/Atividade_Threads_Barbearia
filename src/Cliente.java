import java.util.concurrent.ThreadLocalRandom;

public class Cliente implements Runnable {
    private int id;
    private String nome;
    private int tamanhoCabelo;
    private Barbearia barbearia;

    Cliente(int id, String nome, Barbearia barbearia) {
        this.id = id;
        this.nome = nome;
        this.barbearia = barbearia;

        //Tempo que vai levar cada corte de cabelo
        tamanhoCabelo = ThreadLocalRandom.current().nextInt(100, 500);
    }

    public String getNome() {
        return nome;
    }

    public int getTamanhoCabelo() {
        return tamanhoCabelo;
    }

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
