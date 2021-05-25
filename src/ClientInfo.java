import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Classe responsável por armazenar todas as informações dos Clientes que fazem um Request.
 */
public class ClientInfo {
    private Map<Integer, Socket> clients;
    private ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();

    /**
     * Construtor da class.
     */
    public ClientInfo() {
        this.clients = new HashMap<>();
    }

    /**
     * Método que adiciona cliente à estrutura de armazenamento.
     * @param key int relativo ao id do Pacote ao qual foi atríbuido.
     * @param client Socket relativo ao cliente.
     */
    public void addClient(int key, Socket client) {
        wl.lock();
        try {
            this.clients.put(key, client);
        } finally {
            wl.unlock();
        }
    }

    /*

     */
    public Socket getClient(int key) {
        wl.lock();
        try {
            if(this.clients.containsKey(key))
                return this.clients.get(key);
            return null;
        } finally {
            wl.unlock();
        }
    }

    public void removeClient(int key){
        wl.lock();
        try {
            this.clients.remove(key);
        } finally {
            wl.unlock();
        }
    }
}
