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
    private DatagramSocket socket;
    private int packetID;
    private InetAddress fromServer;
    private int fromPort;
    private ServersInfo servidores;
    private int myServerPort;


    public Responder(int id, BufferedWriter out, StackShared stack, DatagramSocket socket, InetAddress server,int port,ServersInfo serversInfo,int myServerPort) {
        this.packetID = id;
        this.out = out;
        this.stack = stack;
        this.packetSet = new TreeSet<>(new PacketComparatorByOffset());
        this.socket =socket;
        this.fromServer = server;
        this.fromPort = port;
        this.servidores = serversInfo;
        this.myServerPort = myServerPort;
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
            /*
            System.out.println("\n[Responder]: " +
                            "\nindice : " + i +
                            "\noffset : " + p.getOffset() +
                            "\nlastOffSet: " + lastOffSet +
                            "\nlastLPacket: " + lastLPacket);
             */
            if(lastOffSet+lastLPacket != p.getOffset()) return lastOffSet+lastLPacket; // retornar o packet que falta
            lastOffSet = p.getOffset();
            lastLPacket = p.getLength();
            //System.out.println("[DEBUG]: " + p.getLength());
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

        DatagramPacket ackPacket = new DatagramPacket(buf,buf.length,this.fromServer,this.fromPort);
        this.socket.send(ackPacket);
    }

    public void run() {

        try {
            Packet packet;
            int pStatus = 0;
            packet = (Packet) stack.pop();
            int timeOut = 0;
            while (true) {
                if (packet != null) {
                    /*
                    Vericicar que é end connection
                     */


                    // acrescentar todos os packets da stack ao Set;
                    do {
                        //System.out.println("[Responder]: Pacote adicionado!");
                        packetSet.add(packet);
                        packet = (Packet) stack.pop();
                    }while (packet != null);


                    if ((pStatus = checkIfItsFull(packetSet)) == -1) {
                        System.out.println("[Responder]: Todos os pacotes ok!");
                        Packet p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress()+":"+0+":"+this.myServerPort,1,pStatus,("está tudo").getBytes());
                        sendACK(p);
                        System.out.println("[Responder] Freeing Server ...");
                        this.servidores.freeServer(this.fromServer,this.fromPort);
                        break;
                    }
                    packet = (Packet) stack.pop();

                } else { // adicionar timeout?
                    System.out.println("[Responder] FALTA O PACKET COM O OFFSET: " + pStatus);
                    Packet p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress()+":"+1+":"+this.myServerPort,1,pStatus,("Falta 1 packet").getBytes());
                    sendACK(p);
                    System.out.println("[Responder] ACK Sended");
                    System.out.println(":\n" + p.toString());
                    System.out.println("VOU DORMIR 5 SEGUNDOS PELO MENOS");
                    timeOut++;
                    if(timeOut == 5){
                        p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress()+":"+0+":"+this.myServerPort,3,pStatus,("TimeOut").getBytes());
                        sendACK(p);
                        out.close();
                        return;
                    }
                    packet = (Packet) stack.iWannaPop();
                    System.out.println("ACORDEI");

                }
            }
            int length = 1;
            for(Packet a: packetSet) {
                length += a.getPayloadLength();
            }

            out.write("HTTP/1.0 200 OK\n");
            out.write("Connection: close\n");
            out.write("Content-Length: " + length + "\n");
            out.write("Content-Type: text/txt\n");
            out.write("\n");
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

