import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ServersInfo {
    private ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();
    private Map<String, FastFileSrv> servers;

    public ServersInfo() {
        this.servers = new HashMap<>();
    }

    public ServersInfo(Map<String, FastFileSrv> s) {
        this.servers = s;
    }

    public void addServer(String n, InetAddress ip) {
        this.wl.lock();
        try {
            this.servers.put(n, new FastFileSrv(n, ip));
        } finally {
            this.wl.unlock();
        }
    }

    public boolean containsServer(String n) {
        this.rl.lock();
        try {
            return this.servers.containsKey(n);
        } finally {
            this.rl.unlock();
        }
    }

}
