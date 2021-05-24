import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ServerRunReceiver implements Runnable {
    private Map<Integer, StackShared> packets; // nº de thread -> stack partilhada
    private Map<Integer, Integer> distributedPackets; // nº de Thread -> lista de id packets que já/está a tratar
    private final Random random = new Random();

    private DatagramSocket socket;
    private int port;
    private InetAddress address;

    private InetAddress connectedServer;
    private int portConnected;

    private int nThreads;

    public ServerRunReceiver(Map<Integer, StackShared> packets, Map<Integer, Integer> distributedPackets, int nThreads,
                             DatagramSocket socket, int port, InetAddress address,
                             InetAddress connectedServer, int portConnected) {
        this.packets = packets;
        this.distributedPackets = distributedPackets;
        this.nThreads = nThreads;
        this.socket = socket;
        this.port = port;
        this.address = address;
        this.connectedServer = connectedServer;
        this.portConnected = portConnected;

    }


    public void run() {

        try {
            boolean running = true;
            System.out.println("[10] Waiting for Requests!");
            while (running) {
                byte[] buf = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                System.out.println("[10] Ready for packet");
                this.socket.receive(packet);
                System.out.println("[10] Got a Packet");
                System.out.println("[10] ServerRun:> Received connection from :" + packet.getAddress());
                Packet fsChunk = new Packet(packet.getData());

                if ((fsChunk.getType() == 1 && fsChunk.getFlag() == 0) || fsChunk.getType() == 3) { // ACK / end connection
                    for (Map.Entry<Integer, Integer> entry : distributedPackets.entrySet()) {
                        if (entry.getValue() == fsChunk.getPacketID()) {
                            distributedPackets.replace(entry.getKey(), -1);
                        }
                    }
                } else if (fsChunk.getType() == 5) {
                    Packet ack = new Packet(fsChunk.getPacketID(), this.address.getHostAddress() + ":" + 0 + ":" + this.port, 1, 0, null);
                    byte[] buffer = ack.packetToBytes();
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length, this.connectedServer, this.portConnected);
                    System.out.println("[ServerRun] ACK Sent!");
                    this.socket.send(dp);
                }
                boolean flag = false;
                for (Map.Entry<Integer, Integer> entry : distributedPackets.entrySet()) {
                    if (entry.getValue() == fsChunk.getPacketID()) {
                        packets.get(entry.getKey()).push(fsChunk);
                        flag = true;
                    }
                }
                if (!flag) {
                    // random
                    int nThread;
                    do {
                        nThread = random.nextInt(nThreads);
                    } while (distributedPackets.get(nThread) != -1); // encontrar um livre

                    System.out.println("[ServerRun] Escolhido o Servidor: " + nThread);
                    packets.get(nThread).push(fsChunk);
                    distributedPackets.replace(nThread, fsChunk.getPacketID());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
