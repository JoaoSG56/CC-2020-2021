import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;

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
        this.socket = new DatagramSocket(80);
        this.address = InetAddress.getLocalHost();
        this.port = port;
        this.name = name;
        this.connectedServer = connectedServer;
    }

    private void handleRequest(Packet fsChunk) {
        try {
            String path = "/home/core/Files";

            System.out.println(fsChunk.getPayloadStr());
            System.out.println(fsChunk.getPayloadStr().length());


            String a = fsChunk.getPayloadStr().replace("\0","");
            System.out.println(a);
            System.out.println(a.length());

            File filePath = new File(path + a);


            byte[] bytes = Files.readAllBytes(filePath.toPath());
            int chunks = (bytes.length%256 == 0)? bytes.length/256 : bytes.length/256 + 1;
            int atualOffset = 0;
            int lastOffsetArray = 0;
            System.out.println("[ServerRun - handleRequest]:> sending " + chunks + " packets");
            for(int i = 0; i < chunks;i++){
                int flag = (i == chunks-1)? 0 : 1; // última iteração
                int end = (i==chunks-1) ? bytes.length - lastOffsetArray : 256;
                int aux = lastOffsetArray+end;
                System.out.println("[ServerRun - handleRequest]:>\n\tatualOffset: " +
                        atualOffset +
                        "\n\tatualoffset+end: " + aux +
                        "\n\tlastOffsetArray: " + lastOffsetArray +
                        "\n\tend: " + end +
                        "\n\tlength bytes: " + bytes.length+
                        "\n\ti: " + i + " : " + chunks);
                byte[] bytesChunk = Arrays.copyOfRange(bytes,lastOffsetArray,lastOffsetArray+end);
                Packet fsChunkPacket = new Packet(fsChunk.getPacketID(), this.address.getHostAddress() + ":" + flag + ":" + this.port, 4, atualOffset, bytesChunk);

                //System.out.println("[ServerRun - handleRequest]:\n" + fsChunkPacket.toString());
                byte[] buf = fsChunkPacket.packetToBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, this.connectedServer, this.port);

                this.socket.send(packet);

                atualOffset += packet.getLength(); // offset para o packet
                lastOffsetArray += end; // offset do array

            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void main(String[] args) throws SocketException, UnknownHostException {
        System.out.println(args[0]+ " " + args[1]);
        ServerRun sr = new ServerRun(args[0], InetAddress.getByName(args[1]), Integer.parseInt(args[2]));


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

        t.interrupt();
    }

}
