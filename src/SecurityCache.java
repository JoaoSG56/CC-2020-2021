import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SecurityCache {
    private final int maxRequestsPTime;
    private final Lock l = new ReentrantLock();
    Map<String, RecentRequest> recentRequests;
    Set<String> blackList;

    public SecurityCache(int maxRequestsPTime) {
        this.maxRequestsPTime = maxRequestsPTime;
        this.recentRequests = new HashMap<>();
        this.blackList = new TreeSet<>();
    }

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

    public void removeRequest(String s) {
        this.l.lock();
        try {
            this.recentRequests.remove(s);
        } finally {
            this.l.unlock();
        }
    }

    public void addOnBlackList(String s) {
        this.l.lock();
        try {
            this.blackList.add(s);
            System.out.println("[Security]: Client added to the Blacklist!");
        } finally {
            this.l.unlock();
        }
    }

    public void sweep() {
        this.l.lock();
        try {
            for (Map.Entry<String, RecentRequest> entry : this.recentRequests.entrySet()) {
                System.out.println("Percorrendo ...");
                if (entry.getValue().getRequestPTime() > maxRequestsPTime) {
                    addOnBlackList(entry.getKey());
                    removeRequest(entry.getKey());
                } else if (entry.getValue().getTimePassed() >= 20) {
                    removeRequest(entry.getKey());
                } else {
                    incrementTime(entry.getKey());
                }
            }
        } finally {
            this.l.unlock();
        }
    }

    public void incrementTime(String s) {
        this.l.lock();
        try {
            this.recentRequests.get(s).add1Sec();
        } finally {
            this.l.unlock();
        }
    }

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
