import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.*;


/*
    Classe correspondende aos FastFileServers
 */

class ServerRun {
    private final String path = "/Users/joao";
    private DatagramSocket socket;
    private InetAddress address;
    private int portConnected;
    private InetAddress connectedServer;
    private int port;
    private String name;
    private String atualPath;
    private int nThreads;


    ServerRun(String name, InetAddress connectedServer, int portConnected, int nThreads) throws SocketException, UnknownHostException {
        this.port = 80;
        this.address = InetAddress.getLocalHost();
        this.portConnected = portConnected;
        this.name = name;
        this.connectedServer = connectedServer;
        this.socket = new DatagramSocket(this.port);
        this.nThreads = nThreads;
    }


    public static void main(String[] args) throws SocketException, UnknownHostException {

        // java ServerRun s1 10.1.1.1:80 4

        if (args.length < 2)
            System.out.println("Argumentos insuficientes\n" +
                    "java ServerRun <nome> <ip:porta> <nThreads(optional)>");

        System.out.println(args[0] + " " + args[1]);
        int nThreads = (args.length == 3) ? Integer.parseInt(args[2]) : 1;
        String[] argsaux = args[1].split(":");
        ServerRun sr = new ServerRun(args[0], InetAddress.getByName(argsaux[0]), Integer.parseInt(argsaux[1]), nThreads);


        Thread t = new Thread("aux") { // thread responsável por manter servidor vivo
            private DatagramSocket socket = sr.socket;
            private byte[] buf = new Packet(-1, sr.address.getHostAddress() + ":" + 0 + ":" + sr.port, 2, 0, (sr.name + ";" + sr.nThreads).getBytes()).packetToBytes();
            private DatagramPacket packet = new DatagramPacket(buf, buf.length, sr.connectedServer, sr.portConnected);

            public void run() {
                boolean running = true;
                while (running) {
                    try {
                        socket.send(packet);
                        Thread.sleep(10000);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                        running = false;
                        System.out.println("[ServerRun] Closing Socket ...");
                        socket.close();
                    }
                }
            }
        };
        t.start();

        /*
         receber pedidos!!
         */

        Map<Integer, StackShared> packets = new HashMap<>(); // nº de thread -> stack partilhada
        Map<Integer, Integer> distributedPackets = new HashMap<>(); // nº de Thread -> lista de id packets que já/está a tratar
        Random random = new Random();

        for (int i = 0; i < sr.nThreads; i++) {
            StackShared stackShared = new StackShared(); // inicializar stack por cada thread
            distributedPackets.put(i, -1); // inicializar array
            packets.put(i, stackShared); // adicionar stack ao map

            // Thread ... .start()
            new Thread(new ServerRunThread(stackShared,sr.socket,sr.port,sr.address,sr.connectedServer,sr.portConnected)).start();
        }


        try {
            boolean running = true;
            System.out.println("[10] Waiting for Requests!");
            while (running) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                System.out.println("[10] Ready for packet");
                sr.socket.receive(packet);
                System.out.println("[10] Got a Packet");
                System.out.println("[10] ServerRun:> Received connection from :" + packet.getAddress());
                Packet fsChunk = new Packet(packet.getData());

                if((fsChunk.getType() == 1 && fsChunk.getFlag() == 0) || fsChunk.getType() == 3){ // ACK / end connection
                    for (Map.Entry<Integer, Integer> entry : distributedPackets.entrySet()) {
                        if (entry.getValue() == fsChunk.getPacketID()) {
                            distributedPackets.replace(entry.getKey(),-1);
                        }
                    }
                }
                boolean flag = false;
                for (Map.Entry<Integer, Integer> entry : distributedPackets.entrySet()) {
                    if (entry.getValue() == fsChunk.getPacketID()) {
                        packets.get(entry.getKey()).push(fsChunk);
                        flag = true;
                    }
                }
                if(!flag){
                    // random
                    int nThread;
                    do {
                        nThread = random.nextInt(nThreads);
                    }while (distributedPackets.get(nThreads) != -1); // encontrar um livre

                    System.out.println("[ServerRun] Escolhido o Servidor: " + nThread);
                    packets.get(nThread).push(fsChunk);
                    distributedPackets.replace(nThread,fsChunk.getPacketID());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        t.interrupt();
    }

}
