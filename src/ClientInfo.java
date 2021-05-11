import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientInfo {
    private Map<Integer, Socket> clients;
    private ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();

    public ClientInfo() {
        this.clients = new HashMap<>();
    }

    public void addClient(int key, Socket client) {
        wl.lock();
        try {
            this.clients.put(key, client);
        } finally {
            wl.unlock();
        }
    }

    public Socket getClient(int key) {
        wl.lock();
        try {
            return this.clients.remove(key);
        } finally {
            wl.unlock();
        }
    }

    public void removeClient(int key){
        wl.lock();
        try {
            this.clients.remove(key);
        }finally {
            wl.unlock();
        }
    }
}
