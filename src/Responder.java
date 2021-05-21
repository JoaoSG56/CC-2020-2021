import java.io.BufferedWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
    private DatagramSocket socket;
    private int packetID;
    private InetAddress fromServer;


    public Responder(int id, BufferedWriter out, StackShared stack, Condition isNotEmpty, DatagramSocket socket, InetAddress server) {
        this.packetID = id;
        this.out = out;
        this.stack = stack;
        this.packetSet = new TreeSet<>(new PacketComparatorByOffset());
        this.isNotEmpty = isNotEmpty;
        this.socket =socket;
        this.fromServer = server;
    }

    /*
    verificar que existe flag == 0 e offset == 0
       ou
    verificar que existe flag == 0 e offsets seguidos
    */

    // Tuplo (chunk, offset)
    public static int checkIfItsFull(Set<Packet> packets){
        if(packets.size() == 0)
            return 0;
        int lastOffSet = 0;
        int lastLPacket = 0;

        boolean noFlag = false;
        int i = 0;
        for(Packet p: packets){
            System.out.println("\n[Responder]: " +
                            "\nindice : " + i +
                            "\noffset : " + p.getOffset() +
                            "\nlastOffSet: " + lastOffSet +
                            "\nlastLPacket: " + lastLPacket);

            if(lastOffSet+lastLPacket != p.getOffset()) return lastOffSet+lastLPacket; // retornar o packet que falta
            lastOffSet = p.getOffset();
            lastLPacket = p.getLength();
            System.out.println("[DEBUG]: " + p.getLength());
            if(p.getFlag() == 0) noFlag = true;

        }
        if(noFlag)
            return -1;
        else {
            System.out.println("\n\n\n\n\n *chegou aqui* \n\n\n\n\n");
            return 0;

        }
    }

    private void sendACK(Packet p) throws IOException {
        byte[] buf = p.packetToBytes();

        DatagramPacket ackPacket = new DatagramPacket(buf,buf.length,this.fromServer,80);
        this.socket.send(ackPacket);
    }

    public void run() {

        try {
            Packet packet;
            int pStatus = 0;
            while (true) {
                if ((packet = (Packet) stack.pop()) != null) {
                    System.out.println("[Responder]: Pacote adicionado!");
                    packetSet.add(packet);
                    if ((pStatus = checkIfItsFull(packetSet)) == -1) {
                        System.out.println("[Responder]: Todos os pacotes ok!");
                        Packet p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress()+":"+0+":"+80,1,pStatus,("está tudo").getBytes());
                        sendACK(p);
                        break;
                    }

                } else { // adicionar timeout?
                    System.out.println("[Responder] FALTA O PACKET COM O OFFSET: " + pStatus);
                    Packet p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress()+":"+1+":"+80,1,pStatus,("Falta 1 packet").getBytes());
                    sendACK(p);
                    System.out.println("[Responder] ACK Sended");
                    System.out.println(":\n" + p.toString() );
                    Thread.sleep(8000);
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

