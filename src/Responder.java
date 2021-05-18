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
        if(packets.size() == 0)
            return false;
        int lastOffSet = 0;
        int lastLPacket = 0;
        int aux;
        boolean noFlag = false;
        int i = 0;
        for(Packet p: packets){
            aux = lastOffSet+lastLPacket;
            System.out.println("\n[Responder]: " +
                            "\nindice : " + i +
                            "\noffset : " + p.getOffset() +
                            "\nlastOffSet: " + lastOffSet +
                            "\nlastLPacket: " + lastLPacket);

            if(lastOffSet+lastLPacket != p.getOffset()) return false;
            lastOffSet = p.getOffset();
            lastLPacket = p.getLength();
            System.out.println("[DEBUG]: " + p.getLength());
            if(p.getFlag() == 0) noFlag = true;

        }

        return noFlag;
    }

    public void run() {

        try {
            Packet packet;
            while (true) {
                if ((packet = (Packet) stack.pop()) != null) {
                    System.out.println("[Responder]: Pacote adicionado!");
                    packetSet.add(packet);
                    if (checkIfItsFull(packetSet)) {
                        System.out.println("[Responder]: Todos os pacotes ok!");
                        break;
                    }

                } else { // adicionar timeout?
                    System.out.println("Falta algum pacote");
                    Thread.sleep(1000);
                }
            }
            for (Packet aux : packetSet) {
                out.write(aux.getPayloadStr().replace("\0", ""));
                out.flush();
            }
            System.out.println("[Responder] Closing Socket ...");
            out.close();

        } catch (IOException e) {
            System.out.println("[ClientHttpHandler-Thread] Error:");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

