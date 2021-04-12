import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ServersInfo {
    private ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();
    private Map<String, FastFileSrv> servers;
    private Stack<Packet> packetsToProcess; // renew packets
    private final int timeUpServer = 20;


    public ServersInfo() {
        this.servers = new HashMap<>();
        this.packetsToProcess = new Stack<>();
        //this.packetsToProcess = new Stack<>();
    }

    public ServersInfo(Map<String, FastFileSrv> s) {
        this.servers = s;
        //this.packetsToProcess = new Stack<>();
    }

    public void addServer(String n, InetAddress ip, String port) {
        this.wl.lock();
        try {
            this.servers.put(n, new FastFileSrv(n, ip, timeUpServer,Integer.parseInt(port)));
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

    public void removeServer(String name) {
        this.wl.lock();
        try {
            this.servers.remove(name);
        } finally {
            this.wl.unlock();
        }
    }

    public List<FastFileSrv> getFastFileSrvs() {
        List<FastFileSrv> r = new ArrayList<>();
        this.rl.lock();
        try {
            r.addAll(this.servers.values());
        } finally {
            this.rl.unlock();
        }
        return r;
    }

    public FastFileSrv getFastFileSrv(String s) {
        this.rl.lock();
        try {
            return this.servers.get(s);
        } finally {
            this.rl.unlock();
        }
    }

    public FastFileSrv getFastFileSrv(){
        this.wl.lock();
        try {
            for(FastFileSrv f: this.servers.values()){
                if(!f.isOccupied()){
                    f.setOccupied(true);
                    return f;
                }
            }
            return null;
        } finally {
            this.wl.unlock();
        }
    }

    /*
    classe que retorna n servidores que estejam livres
    caso contrário, retorna null
     */

    public List<FastFileSrv> getFastFileSrv(int n) {
        List<FastFileSrv> l = new ArrayList<>();
        this.wl.lock();
        try {
            int i = 0;
            for (FastFileSrv f : this.servers.values()) {
                if (!f.isOccupied()) {
                    f.setOccupied(true);
                    l.add(f);
                }
                if (i >= n)
                    return l;
            }
            // não foi satisfeita a condição
            for (FastFileSrv f : l) {
                f.setOccupied(false);
            }
            return null;
        } finally {
            this.wl.unlock();
        }
    }


    public void renewServer(String server, InetAddress address) {
        String[] args = server.split(";"); // name;ip;port
        this.wl.lock();
        try {
            if (this.servers.containsKey(server))
                this.servers.get(args[0]).setTimeUp(this.timeUpServer);
            else
                addServer(args[0], InetAddress.getByName(args[1]),args[2]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
            this.wl.unlock();
        }
    }


    public void pushPacket(Packet p) {
        this.wl.lock();
        try {
            this.packetsToProcess.push(p);
        } finally {
            this.wl.unlock();
        }
    }

    public Packet popPacket() {
        this.wl.lock();
        try {
            if (this.packetsToProcess.empty()) return null;
            return this.packetsToProcess.pop();
        } finally {
            this.wl.unlock();
        }
    }


}
