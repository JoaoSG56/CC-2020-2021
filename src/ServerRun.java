import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;

/*
    Classe correspondende aos FastFileServers
 */

class ServerRun {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private InetAddress connectedServer;

    private String name;

    ServerRun(String name, InetAddress connectedServer, int port) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        this.address = InetAddress.getLocalHost();
        this.port = port;
        this.name = name;
        this.connectedServer = connectedServer;
    }

    private void handleRequest(Packet fsChunk) {
        try {
            String path = "~/Files/";

            File filePath = new File(path + fsChunk.getPayloadStr());

            byte[] bytes = Files.readAllBytes(filePath.toPath());
            Packet fsChunkPacket = new Packet(fsChunk.getPacketID(), this.address.getHostAddress() + ":" + 0 + ":" + this.port, 4, 0, bytes);

            System.out.println("[10 DEBUG - handleRequest]:\n" + fsChunkPacket.toString());
            byte[] buf = fsChunkPacket.packetToBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);

            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void main(String[] args) throws SocketException, UnknownHostException {
        System.out.println(args[0]+ " " + args[1]);
        ServerRun sr = new ServerRun(args[0], InetAddress.getByName(args[1]), Integer.parseInt(args[2]));


        System.out.println("[ServerRun] : {");
        System.out.println(InetAddress.getLocalHost());
        System.out.println(InetAddress.getByName("192.168.1.110").getHostAddress());
        System.out.println("}");


        Thread t = new Thread("aux") { // thread responsável por manter servidor vivo
            private DatagramSocket socket = sr.socket;
            private byte[] buf = new Packet(-1, sr.address.getHostAddress() + ":" + 0 + ":" + sr.port, 2, 0, sr.name.getBytes()).packetToBytes();
            private DatagramPacket packet = new DatagramPacket(buf, buf.length, sr.connectedServer, sr.port);

            public void run() {
                boolean running = true;
                while (running) {
                    try {
                        socket.send(packet);
                        Thread.sleep(10000);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                        running = false;
                        socket.close();
                    }
                }
            }
        };
        t.start();

        /*
         receber pedidos!!
         */
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
                switch (fsChunk.getType()) {
                    case 5:
                        sr.handleRequest(fsChunk);
                        break;
                    default:
                        System.out.println("[10 DEBUG]Something went wrong:\n" +
                                "[10 DEBUG]\n" + fsChunk.toString());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        {

        }


        t.interrupt();
    }

}