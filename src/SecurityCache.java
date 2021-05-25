import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe responsavel pela segurança do sistema
 */
public class SecurityCache {
    private final int maxRequestsPTime;
    private final Lock l = new ReentrantLock();
    Map<String, RecentRequest> recentRequests;
    Set<String> blackList;

    /**
     * Contrutor
     * @param maxRequestsPTime tempo máximo
     */
    public SecurityCache(int maxRequestsPTime) {
        this.maxRequestsPTime = maxRequestsPTime;
        this.recentRequests = new HashMap<>();
        this.blackList = new TreeSet<>();
    }

    /**
     * verifica se o cliente está na blacklist
     * @param s cliente
     * @param socket socket
     * @return contains
     */
    public boolean containsOnBlackList(String s,Socket socket) {
        this.l.lock();
        try {
            if(this.blackList.contains(s)) return true;
            else{
                this.addOnRecentRequests(s,socket);
                return this.blackList.contains(s);
            }

        } finally {
            this.l.unlock();
        }
    }

    /**
     * Método responsável por remover um Request
     * @param s String
     */
    public void removeRequest(String s) {
        this.l.lock();
        try {
            this.recentRequests.remove(s);
        } finally {
            this.l.unlock();
        }
    }

    /**
     * Adicionacliente à blacklist
     *
     *  @param s Cliente adicionado
     */
    public void addOnBlackList(String s) {
        this.l.lock();
        try {
            this.blackList.add(s);
            System.out.println("[Security]: Client added to the Blacklist!");
        } finally {
            this.l.unlock();
        }
    }

    /**
     * Método responsável por percorrer os RecentRequests e detetar anomalias
     */
    public void sweep() {
        this.l.lock();
        try {
            Iterator<Map.Entry<String,RecentRequest>> iter = this.recentRequests.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, RecentRequest> entry = iter.next();
                if (entry.getValue().getRequestPTime() > maxRequestsPTime) {
                    addOnBlackList(entry.getKey());
                    iter.remove();
                } else if (entry.getValue().getTimePassed() >= 20) {
                    iter.remove();
                } else {
                    incrementTime(entry.getKey());
                }

            }
        } finally {
            this.l.unlock();
        }
    }

    /**
     * Método respondável pelo incremento de tempo
     * @param s String
     */
    public void incrementTime(String s) {
        this.l.lock();
        try {
            this.recentRequests.get(s).add1Sec();
        } finally {
            this.l.unlock();
        }
    }

    /**
     * Método responsável por adicionar um request recente
     * @param s String
     * @param socket Socket
     */
    public void addOnRecentRequests(String s, Socket socket) {
        this.l.lock();
        try {
            if (this.recentRequests.containsKey(s)) {
                this.recentRequests.get(s).addRequest();
                if(this.recentRequests.get(s).getRequestPTime()>maxRequestsPTime){
                    this.addOnBlackList(s);
                    this.removeRequest(s);

                }

            } else {
                this.recentRequests.put(s, new RecentRequest(socket));
            }
        } finally {
            this.l.unlock();
        }
    }
}
