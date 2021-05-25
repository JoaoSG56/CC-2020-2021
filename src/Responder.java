import java.io.BufferedWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;
import java.util.TreeSet;

/**
 * Thread responsável por responder ao cliente
 */
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

    /**
     * Construtor parameterizado
     * @param id id relativo ao packet ao qual a classe está responsável
     * @param out BufferedWriter do Cliente
     * @param stack Stack partilhada com pacotes dirigidos à classe
     * @param socket Socket do Servidor para ser usada para pedir novos fragmentos
     * @param server InetAddress do FastFileServer
     * @param port porta do FastFileServer
     * @param serversInfo lista de Servidores ativos
     * @param myServerPort porta do Servidor Principal
     */
    public Responder(int id, BufferedWriter out, StackShared stack, DatagramSocket socket, InetAddress server, int port, ServersInfo serversInfo, int myServerPort) {
        this.packetID = id;
        this.out = out;
        this.stack = stack;
        this.packetSet = new TreeSet<>(new PacketComparatorByOffset());
        this.socket = socket;
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

    /**
     * Método estático que verifica se estão presentes todos os packets
     * @param packets Set com os packets
     * @return int
     */
    public static int checkIfItsFull(Set<Packet> packets) {
        if (packets.size() == 0)
            return 0;
        int lastOffSet = 0;
        int lastLPacket = 0;

        boolean noFlag = false;
        for (Packet p : packets) {
            if (lastOffSet + lastLPacket != p.getOffset())
                return lastOffSet + lastLPacket; // retornar o packet que falta
            lastOffSet = p.getOffset();
            lastLPacket = p.getLength();
            if (p.getFlag() == 0) noFlag = true;

        }
        if (noFlag)
            return -1;
        else {
            return 0;

        }
    }

    /**
     * Método para envio de um Packet para o FastFileServer
     * @param p Packet
     * @throws IOException exeção
     */
    private void sendACK(Packet p) throws IOException {
        byte[] buf = p.packetToBytes();

        DatagramPacket ackPacket = new DatagramPacket(buf, buf.length, this.fromServer, this.fromPort);
        this.socket.send(ackPacket);
    }

    /**
     * Thread que trata de responder ao Cliente
     */
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
                    if (packet.getType() == 3) {
                        System.out.println("[Responder] Conection Ended");
                        return;
                    }
                    /*
                    Verificar checksum
                     */
                    if (packet.getChecksum() != Packet.getCRC32Checksum(packet.getPayloadBytes())) {
                        System.out.println("[Responder] BAD CHECKSUM");
                        Packet p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress() + ":" + 1 + ":" + this.myServerPort, 1, packet.getOffset(), 0, null);
                        sendACK(p);
                        System.out.println("[Responder] ACK Sended");
                    } else {
                        // acrescentar todos os packets da stack ao Set;
                        System.out.println("[Responder] Good checkSum");
                        do {
                            packetSet.add(packet);
                            packet = (Packet) stack.pop();
                        } while (packet != null);


                        if ((pStatus = checkIfItsFull(packetSet)) == -1) {
                            System.out.println("[Responder]: Todos os pacotes ok!");
                            Packet p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress() + ":" + 0 + ":" + this.myServerPort, 1, pStatus, 0, null);
                            sendACK(p);
                            System.out.println("[Responder] Freeing Server ...");
                            this.servidores.freeServer(this.fromServer, this.fromPort);
                            break;
                        }
                    }
                    packet = (Packet) stack.pop();

                } else { // adicionar timeout?
                    System.out.println("[Responder] FALTA O PACKET COM O OFFSET: " + pStatus);
                    Packet p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress() + ":" + 1 + ":" + this.myServerPort, 1, pStatus, 0, null);
                    sendACK(p);
                    System.out.println("[Responder] ACK Sended");
                    timeOut++;
                    if (timeOut == 5) {
                        p = new Packet(this.packetID, InetAddress.getLocalHost().getHostAddress() + ":" + 0 + ":" + this.myServerPort, 3, pStatus, 0, null);
                        sendACK(p);
                        out.write("HTTP/1.0 503 Service Unavailable\n");
                        out.write("Connection: close\n");
                        out.flush();
                        out.close();

                        System.out.println("[Responder] Freeing Server ...");
                        this.servidores.freeServer(this.fromServer, this.fromPort);

                        return;
                    }
                    packet = (Packet) stack.iWannaPop();

                }
            }
            int length = 0;
            for (Packet a : packetSet) {
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

