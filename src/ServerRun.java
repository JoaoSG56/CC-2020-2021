import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;


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

    ServerRun(String name, InetAddress connectedServer, int portConnected) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(80);
        this.address = InetAddress.getLocalHost();
        this.portConnected = portConnected;
        this.name = name;
        this.connectedServer = connectedServer;
        this.port = 80;
    }

    private String getTransferID(int flag){
        return this.address.getHostAddress() + ":"+flag+":"+this.port;
    }

    private void handleRequest(Packet fsChunk, int offset) throws IOException, InterruptedException {
        System.out.println("Failed to Send");

        File filePath = new File(path + this.atualPath);
        int maxLength = 256 - 24;

        int actualOffset = (offset/ fsChunk.getLength()) * maxLength;
        System.out.println("Offset received: " + offset + "\nACTUAL OFFSET = " + actualOffset);

        byte[] bytes = Files.readAllBytes(filePath.toPath());

        byte[] bytesChunk = Arrays.copyOfRange(bytes,offset,maxLength);


        int flag = (filePath.length()==offset+bytesChunk.length) ? 0 : 1;
        Packet fsChunkPacket = new Packet(fsChunk.getPacketID(), this.address.getHostAddress() + ":" + flag + ":" + this.port, 4, offset, bytesChunk);

        //System.out.println("[ServerRun - handleRequest]:\n" + fsChunkPacket.toString());
        byte[] bufToSend = fsChunkPacket.packetToBytes();
        DatagramPacket packet = new DatagramPacket(bufToSend, bufToSend.length, this.connectedServer, this.portConnected);

        this.socket.send(packet);


        System.out.println("SENT : \n\n"  + fsChunkPacket.toString());

        Thread.sleep(8000);


    }


    private void handleRequest(Packet fsChunk) {
        try {


            System.out.println(fsChunk.getPayloadStr());
            System.out.println(fsChunk.getPayloadStr().length());


            String a = fsChunk.getPayloadStr().replace("\0","");
            this.atualPath = a;
//            System.out.println(a);
//            System.out.println(a.length());

            File filePath = new File(path + a);

            int maxLength = 256 - 24;

            byte[] bytes = Files.readAllBytes(filePath.toPath());
            int chunks = (bytes.length%maxLength == 0)? bytes.length/maxLength : bytes.length/maxLength + 1;
            int atualOffset = 0;
            int lastOffsetArray = 0;
            System.out.println("[ServerRun - handleRequest]:> sending " + chunks + " packets");
            for(int i = 0; i < chunks;i++){
                int flag = (i == chunks-1)? 0 : 1; // última iteração
                int end = (i==chunks-1) ? bytes.length - lastOffsetArray : maxLength;
                int aux = lastOffsetArray+end;
                /*
                System.out.println("[ServerRun - handleRequest]:>\n\tatualOffset: " +
                        atualOffset +
                        "\n\tatualoffset+end: " + aux +
                        "\n\tlastOffsetArray: " + lastOffsetArray +
                        "\n\tend: " + end +
                        "\n\tlength bytes: " + bytes.length+
                        "\n\ti: " + i + " : " + chunks);

                 */
                byte[] bytesChunk = Arrays.copyOfRange(bytes,lastOffsetArray,lastOffsetArray+end);
                Packet fsChunkPacket = new Packet(fsChunk.getPacketID(), this.address.getHostAddress() + ":" + flag + ":" + this.port, 4, atualOffset, bytesChunk);

                //System.out.println("[ServerRun - handleRequest]:\n" + fsChunkPacket.toString());
                byte[] buf = fsChunkPacket.packetToBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, this.connectedServer, this.portConnected);

                this.socket.send(packet);

                atualOffset += packet.getLength(); // offset para o packet
                lastOffsetArray += end; // offset do array

            }


        } catch (IOException e) {
            /*
            -> Não encontrou ficheiro / algo correu mal
            -> Enviar end connection
             */
            System.out.println("[ServerRun] Ficheiro não encontrado!");
            Packet pToSend = new Packet(fsChunk.getPacketID(),this.getTransferID(0),3,0,null);

            byte[] buf = pToSend.packetToBytes();
            DatagramPacket packet = new DatagramPacket(buf,buf.length,this.connectedServer,this.portConnected );
            try {
                System.out.println("[ServerRun] A fechar ligação ...");
                this.socket.send(packet);
            } catch (IOException ioException) {
                System.out.println("[ServerRun] ERROR!");
                ioException.printStackTrace();
            }

        }


    }


    public static void main(String[] args) throws SocketException, UnknownHostException {
        System.out.println(args[0]+ " " + args[1]);
        ServerRun sr = new ServerRun(args[0], InetAddress.getByName(args[1]), Integer.parseInt(args[2]));


        Thread t = new Thread("aux") { // thread responsável por manter servidor vivo
            private DatagramSocket socket = sr.socket;
            private byte[] buf = new Packet(-1, sr.address.getHostAddress() + ":" + 0 + ":" + sr.port, 2, 0, sr.name.getBytes()).packetToBytes();
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
                    case 1:
                        System.out.println("RECEIVED ACK");
                        if(fsChunk.getFlag() == 1){
                            sr.handleRequest(fsChunk,fsChunk.getOffset());
                        }
                        break;
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t.interrupt();
    }

}
