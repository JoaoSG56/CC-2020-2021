import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AcksToConfirm {
    private ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();
    private Set<Integer> idWaitingForAck = new TreeSet<>();

    public void addPacketId(int id){
        wl.lock();
        try {
            this.idWaitingForAck.add(id);
        }finally {
            wl.unlock();
        }
    }

    public boolean wasReceived(int id){
        rl.lock();
        try {
            return !this.idWaitingForAck.contains(id);
        }finally {
            rl.unlock();
        }
    }

    public void removePacketId(int id){
        wl.lock();
        try {
            this.idWaitingForAck.remove(id);
        }finally {
            wl.unlock();
        }
    }
}
