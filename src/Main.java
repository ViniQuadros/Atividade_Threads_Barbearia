import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Barbearia barbearia = new Barbearia();

        int idCLiente = 1;

        //Cliente implementa Runnable, então precisa ser considero também
        //Em uma lista separada de Threads
        ArrayList<Cliente> listaClientes = new ArrayList<>();
        ArrayList<Thread> threadsClientes = new ArrayList<>();

        Barbeiro barbeiro1 = new Barbeiro("1", barbearia);
        Barbeiro barbeiro2 = new Barbeiro("2", barbearia);
        Barbeiro barbeiro3 = new Barbeiro("3", barbearia);

        Barbeiro barbeiros[] = new Barbeiro[3];

        barbeiros[0] = barbeiro1;
        barbeiros[1] = barbeiro2;
        barbeiros[2] = barbeiro3;

        String[] nomeClientes = {
                "Bruno", "Carlos", "Daniel", "Eduardo",
                "Felipe", "Gabriel", "Henrique", "Igor", "João",
                "Kleber", "Lucas", "Marcos", "Nicolas", "Otávio",
                "Pedro", "Rafael", "Samuel", "Thiago", "Vitor",
                "William", "Yuri", "Zé", "Arthur", "Bruno",
                "Caio", "Daniel", "Enzo", "Felipe", "Gustavo",
                "Hugo", "Ian", "João", "Kai", "Leonardo",
                "Matheus", "Nathan", "Oliver", "Paulo", "Quentin",
                "Ricardo", "Stefan", "Thomas", "Ulisses", "Victor",
                "Wesley", "Xavier", "Yago", "Zeca", "André"
        };

        int numeroNome = 0;

        while (idCLiente <= 50) {
            Cliente cliente = new Cliente(idCLiente, nomeClientes[numeroNome % nomeClientes.length], barbearia);
            listaClientes.add(cliente);
            threadsClientes.add(new Thread(cliente, "Cliente-" + idCLiente));

            numeroNome++;
            idCLiente++;
        }

        for (Barbeiro barbeiro : barbeiros) {
            barbeiro.start();
        }

        //Tempo entre chegada dos clientes
        for (Thread t : threadsClientes) {
            t.start();
            Thread.sleep((int) (Math.random() * 100) + 100);
        }

        //Controle do fechamento da barbearia
        //Join espera a thread terminar
        try {
            for (Thread t : threadsClientes) {
                t.join();
            }

            barbearia.fechar();

            for (Barbeiro barbeiro : barbeiros) {
                barbeiro.join();
            }
        } catch (InterruptedException i) {
            System.out.println("Erro na Thread " + i.getMessage());
        }

        System.out.println("Barbearia encerrada com: " + barbearia.getTotalClientes() + " clientes atendidos!");

    }
}