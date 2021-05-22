import java.io.IOException;
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

    public int getLength(){
        rl.lock();
        try {
            return this.clients.size();
        }finally {
            rl.unlock();
        }
    }

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
            this.clients.remove(key).close();
        } catch (IOException e) {
            System.out.println("[ClientInfo] Erro ao fechar socket!");
            e.printStackTrace();
        } finally {
            wl.unlock();
        }
    }
}
