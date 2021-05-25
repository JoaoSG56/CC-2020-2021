import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ServersInfo {
    private final ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();
    private final Condition condition = wl.newCondition();
    private Map<String, FastFileSrv> servers;
    private Stack<Packet> packetsToProcess; // renew packets
    private final int timeUpServer = 20;


    public ServersInfo() {
        this.servers = new HashMap<>();
        this.packetsToProcess = new Stack<>();
    }

    public ServersInfo(Map<String, FastFileSrv> s) {
        this.servers = s;
    }

    public void decrementOcupacao(String serverName){
        this.servers.get(serverName).decrementOcupacao();
    }

    public void addServer(String n, InetAddress ip, int port, int n_threads) {
        this.wl.lock();
        try {
            this.servers.put(n, new FastFileSrv(n, ip, timeUpServer,port,n_threads));
        } finally {
            this.wl.unlock();
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

    public void freeServer(InetAddress add, int port){
        this.wl.lock();
        try {
            for(FastFileSrv f: this.servers.values()){
                if(f.getIp().equals(add) && f.getPort()==port){
                    System.out.println("[ServersInfo] Found server: Freeing");
                    f.decrementOcupacao();
                    return;
                }
            }
            this.condition.signalAll();
        }finally {
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

    public FastFileSrv getFastFileSrv(){
        this.wl.lock();
        try {
            FastFileSrv best = null;
            int timeOut = 0;
            do {
                float ocupation = 1;
                for (FastFileSrv f : this.servers.values()) {
                    if (f.getOcupacao() < ocupation) {
                        ocupation = f.getOcupacao();
                        best = f;
                    }else if(f.getOcupacao() == ocupation && ocupation != 1){
                        Random random = new Random();
                        if(random.nextInt(2) == 1){
                            ocupation = f.getOcupacao();
                            best = f;
                        }
                    }
                }
                if (best != null)
                    best.incrementOcupacao();
                else {
                    timeOut++;
                    if(timeOut == 5)
                        return null;
                    this.condition.await(2, TimeUnit.SECONDS);
                }
            }while (best == null);
            return best;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            this.wl.unlock();
        }
    }


    public void renewServer(String payloadStr, InetAddress address, int port) {
        // name
        this.wl.lock();
        try {
            String[] args = payloadStr.split(";");
            int n_threads = (args.length==2)? Integer.parseInt(args[1]) : 1;
            if (this.servers.containsKey(args[0]))
                this.servers.get(args[0]).setTimeUp(this.timeUpServer);
            else
                addServer(args[0], address,port,n_threads);
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
