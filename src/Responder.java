import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;

// thread responsável por responder ao cliente
public class Responder implements Runnable {
    private BufferedWriter out;
    private Set<Packet> packetSet;
    private StackShared stack;
    Condition isNotEmpty;


    public Responder(BufferedWriter out, StackShared stack,Condition isNotEmpty) {
        this.out = out;
        this.stack = stack;
        this.packetSet = new TreeSet<>(new PacketComparatorByOffset());
        this.isNotEmpty = isNotEmpty;
    }

    /*
    verificar que existe flag == 0 e offset == 0
       ou
    verificar que existe flag == 0 e offsets seguidos
    */
    public static boolean checkIfItsFull(Set<Packet> packets){
        if(packets.size() == 1 || packets.size() == 0)
            return false;
        int lastOffSet = 0;
        int lastLPacket = 0;
        boolean noFlag = false;
        for(Packet p: packets){
            System.out.println("[ClientHttpHandler-thread]: " +
                    "\n\tlastOffSet+lastLPacket: " +
                    lastOffSet+lastLPacket +
                    "\n\tp.getOffset(): " +
                    p.getOffset());
            if(lastOffSet+lastLPacket != p.getOffset()) return false;
            lastOffSet = p.getOffset();
            lastLPacket = p.getLength();
            if(p.getFlag() == 0) noFlag = true;

        }

        return noFlag;
    }

    public void run() {

        try {
            Packet packet;
            while (true) {
                if ((packet = (Packet) stack.pop()) != null) {
                    packetSet.add(packet);
                    if (checkIfItsFull(packetSet))
                        break;

                } else {
                    Thread.sleep(1000);
                }
            }
            for (Packet aux : packetSet) {
                out.write(aux.getPayloadStr().replace("\0", ""));
                out.flush();
            }
            out.close();

        } catch (IOException e) {
            System.out.println("[ClientHttpHandler-Thread] Error:");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

