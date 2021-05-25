import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Classe que contém a informação de um servidor
 */

public class ServersInfo {
    private final ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();
    private final Condition condition = wl.newCondition();
    private Map<String, FastFileSrv> servers;
    private Stack<Packet> packetsToProcess; // renew packets
    private final int timeUpServer = 20;


    /**
     * Construtor da classe
     */
    public ServersInfo() {
        this.servers = new HashMap<>();
        this.packetsToProcess = new Stack<>();
    }

    /**
     * Construtor parametrizado da classe
     *
     * @param s     Map que contém vários servidores
     */
    public ServersInfo(Map<String, FastFileSrv> s) {
        this.servers = s;
    }

    /**
     * Método que decrementa a ocupação de um servidor
     *
     * @param serverName    Servidor cuja ocupação será decrementada
     */
    public void decrementOcupacao(String serverName){
        this.servers.get(serverName).decrementOcupacao();
    }

    /**
     * Método que adiciona um servidor ao map de servidores
     *
     * @param n         Nome do servidor
     * @param ip        Ip do servidor
     * @param port      Porta do servidor
     * @param n_threads Número de threads que o servidor consegue suportar
     */
    public void addServer(String n, InetAddress ip, int port, int n_threads) {
        this.wl.lock();
        try {
            this.servers.put(n, new FastFileSrv(n, ip, timeUpServer,port,n_threads));
        } finally {
            this.wl.unlock();
        }
    }

    /**
     * Método que remove um servidor do map de sevidores
     *
     * @param name  Nome do sevidor a ser removido
     */
    public void removeServer(String name) {
        this.wl.lock();
        try {
            this.servers.remove(name);
        } finally {
            this.wl.unlock();
        }
    }

    /**
     * Método que liberta um servidor
     *
     * @param add   Address do servidor
     * @param port  Porta do sevidor
     */
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
    /**
     * Método que obtém os fast file servers
     *
     * @return  Lista dos fast file Servers obtidos
     */
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

    /**
     * Método que obtém o melhor fast file server disponível
     *
     * @return  Melhor fast file server disponível
     */
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

    /**
     * M+etodo que renova um servidor
     *
     * @param payloadStr    String do payload
     * @param address       Address do servidor
     * @param port          Porta do servidor
     */
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

    /**
     * Método que dá push de um packet
     *
     * @param p Packet
     */
    public void pushPacket(Packet p) {
        this.wl.lock();
        try {
            this.packetsToProcess.push(p);
        } finally {
            this.wl.unlock();
        }
    }

    /**
     * Método que dá pop
     *
     * @return  Packet obtido pelo pop
     */
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
