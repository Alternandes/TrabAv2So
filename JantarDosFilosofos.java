import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * O Problema do Jantar dos Filósofos, resolvido com um garçom.
 *
 * Compilar: javac -encoding UTF-8 JantarDosFilosofos.java
 * Executar: java JantarDosFilosofos
 */
public class JantarDosFilosofos {

    static final int N = 5;                // filósofos e garfos
    static final int REFEICOES = 10;       // quantas vezes cada filósofo come
    static final int PENSAR_MAX_MS = 500;
    static final int COMER_MAX_MS = 400;

    // Para ver o impasse (deadlock), use USAR_GARCOM = false e PAUSA_ENTRE_GARFOS_MS = 500.
    // O programa para de escrever e precisa ser encerrado com Ctrl + C.
    static final boolean USAR_GARCOM = true;
    static final int PAUSA_ENTRE_GARFOS_MS = 0;

    // Cada garfo é um semáforo com 1 permissão: só um filósofo o segura por vez.
    static final Semaphore[] garfos = new Semaphore[N];

    // O garçom deixa no máximo N-1 filósofos disputarem os garfos ao mesmo tempo.
    // O "true" faz os semáforos atenderem por ordem de chegada, o que evita a inanição.
    static final Semaphore garcom = new Semaphore(N - 1, true);

    static class Filosofo extends Thread {

        private final int numero;
        private final int garfoEsquerdo;
        private final int garfoDireito;

        Filosofo(int numero) {
            this.numero = numero;
            this.garfoEsquerdo = numero;
            this.garfoDireito = (numero + 1) % N;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < REFEICOES; i++) {
                    pensar();
                    pegarGarfos();
                    comer();
                    devolverGarfos();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void pensar() throws InterruptedException {
            log("começa a pensar");
            aguardar(PENSAR_MAX_MS);
        }

        private void pegarGarfos() throws InterruptedException {
            log("tenta pegar os garfos " + garfoEsquerdo + " e " + garfoDireito);

            // Com no máximo 4 filósofos disputando 5 garfos, a espera circular não se fecha:
            // sobra um garfo ao lado de alguém, que come e depois libera os outros.
            if (USAR_GARCOM) {
                garcom.acquire();
            }

            garfos[garfoEsquerdo].acquire();
            if (PAUSA_ENTRE_GARFOS_MS > 0) {
                Thread.sleep(PAUSA_ENTRE_GARFOS_MS);
            }
            garfos[garfoDireito].acquire();
        }

        private void comer() throws InterruptedException {
            log("consegue comer");
            aguardar(COMER_MAX_MS);
        }

        private void devolverGarfos() {
            // O log vem antes da liberação para nunca mostrar o vizinho comendo
            // antes de este filósofo aparecer devolvendo o garfo.
            log("devolve os garfos " + garfoEsquerdo + " e " + garfoDireito);
            garfos[garfoDireito].release();
            garfos[garfoEsquerdo].release();
            if (USAR_GARCOM) {
                garcom.release();
            }
        }

        private void log(String acao) {
            System.out.println("Filósofo " + numero + " " + acao);
        }

        /** Dorme um tempo aleatório entre 0 e maxMs milissegundos. */
        private void aguardar(int maxMs) throws InterruptedException {
            Thread.sleep(ThreadLocalRandom.current().nextInt(maxMs + 1));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < N; i++) {
            garfos[i] = new Semaphore(1, true);
        }

        Filosofo[] filosofos = new Filosofo[N];
        for (int i = 0; i < N; i++) {
            filosofos[i] = new Filosofo(i);
            filosofos[i].start();
        }

        // Só chega ao fim se todos os filósofos comerem todas as refeições.
        for (Filosofo f : filosofos) {
            f.join();
        }
        System.out.println("Jantar encerrado: cada filósofo comeu " + REFEICOES + " vezes.");
    }
}
