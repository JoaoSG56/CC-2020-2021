import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
   * Classe responsável por armazenar e organizar os id's aos quais o servidor está à espera de um ACK
 */
public class AcksToConfirm {
    private final ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();
    private Set<Integer> idWaitingForAck = new TreeSet<>();

    /**
     * Método que adiciona um id de um Packet ao Set.
     * @param id int correspondente ao id do Packet.
     */
    public void addPacketId(int id){
        wl.lock();
        try {
            this.idWaitingForAck.add(id);
        }finally {
            wl.unlock();
        }
    }

    /**
     * Método que verifica se o id se encontra no Set. Caso isto se verifique, o mesmo é retirado.
     * @param id int correspondente ao id do Packet.
     * @return boolean.
     */
    public boolean wasReceived(int id){
        rl.lock();
        try {
            return !this.idWaitingForAck.contains(id);
        }finally {
            rl.unlock();
        }
    }
    /**
     * Método que remove um id de um Packet do Set.
     * @param id int correspondente ao id do Packet.
     */
    public void removePacketId(int id){
        wl.lock();
        try {
            this.idWaitingForAck.remove(id);
        }finally {
            wl.unlock();
        }
    }
}
