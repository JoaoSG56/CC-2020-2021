import java.io.IOException;
import java.net.*;
import java.util.*;



/*
    Classe correspondende aos FastFileServers
 */

class ServerRun {
    private DatagramSocket socket;
    private InetAddress address;
    private int portConnected;
    private InetAddress connectedServer;
    private int port;
    private String name;
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
            byte[] data = (sr.name + ";" + sr.nThreads).getBytes();
            private byte[] buf = new Packet(-1, sr.address.getHostAddress() + ":" + 0 + ":" + sr.port, 2, 0, Packet.getCRC32Checksum(data),data).packetToBytes();
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

        new Thread(new ServerRunReceiver(packets,distributedPackets,nThreads,
                sr.socket, sr.port,sr.address,
                sr.connectedServer,sr.portConnected)).start();

        for (int i = 0; i < sr.nThreads; i++) {
            StackShared stackShared = new StackShared(); // inicializar stack por cada thread
            distributedPackets.put(i, -1); // inicializar array
            packets.put(i, stackShared); // adicionar stack ao map

            // Thread ... .start()
            new Thread(new ServerRunThread(stackShared,sr.socket,sr.port,sr.address,sr.connectedServer,sr.portConnected)).start();
        }

    }

}
