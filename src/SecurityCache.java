import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
