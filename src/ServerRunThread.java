import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;

public class ServerRunThread implements Runnable{
    private final String path = "/home/core/Files";
    private DatagramSocket socket;

    private InetAddress connectedServer;
    private int portConnected;


    private InetAddress address;
    private int port;

    private String atualPath;

    private StackShared packets;

    public ServerRunThread(StackShared packets,DatagramSocket ds, int port, InetAddress add, InetAddress connectedServer, int portConnected) throws SocketException, UnknownHostException {
        this.packets = packets;
        this.socket = ds;
        this.address = add;
        this.port = port;
        this.connectedServer = connectedServer;
        this.portConnected = portConnected;
    }

    private String getTransferID(int flag){
        return this.address.getHostAddress() + ":"+flag+":"+this.port;
    }

    private int sendPacket(int id, int flag, int offset, byte[] bytesChunk) throws IOException {
        Packet fsChunkPacket = new Packet(id, getTransferID(flag), 4, offset, Packet.getCRC32Checksum(bytesChunk),bytesChunk);

        byte[] buf = fsChunkPacket.packetToBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.connectedServer, this.portConnected);

        this.socket.send(packet);

        return packet.getLength();

    }

    private void handleRequest(Packet fsChunk, int offset) throws IOException, InterruptedException {
        System.out.println("Failed to Send");

        File filePath = new File(path + this.atualPath);
        int maxLength = 4096 - 32;

        int actualOffset = (offset/ fsChunk.getLength()) * maxLength;
        System.out.println("Offset received: " + offset + "\nACTUAL OFFSET = " + actualOffset);

        byte[] bytes = Files.readAllBytes(filePath.toPath());

        byte[] bytesChunk = Arrays.copyOfRange(bytes,actualOffset,maxLength);


        int flag = (filePath.length()==offset+bytesChunk.length) ? 0 : 1;

        sendPacket(fsChunk.getPacketID(),flag,offset,bytesChunk);

    }


    private void handleRequest(Packet fsChunk) {
        try {


            System.out.println(fsChunk.getPayloadStr());
            System.out.println(fsChunk.getPayloadStr().length());


            this.atualPath = fsChunk.getPayloadStr().replace("\0","");

            File filePath = new File(path + this.atualPath);

            int maxLength = 4096 - 32;

            byte[] bytes = Files.readAllBytes(filePath.toPath());
            int chunks = (bytes.length%maxLength == 0)? bytes.length/maxLength : bytes.length/maxLength + 1;
            int atualOffset = 0;
            int lastOffsetArray = 0;
            System.out.println("[ServerRun - handleRequest]:> sending " + chunks + " packets");
            for(int i = 0; i < chunks;i++){
                int flag = (i == chunks-1)? 0 : 1; // última iteração
                int end = (i==chunks-1) ? bytes.length - lastOffsetArray : maxLength;
                byte[] bytesChunk = Arrays.copyOfRange(bytes,lastOffsetArray,lastOffsetArray+end);
                int length = sendPacket(fsChunk.getPacketID(),flag,atualOffset,bytesChunk);

                atualOffset += length;
                lastOffsetArray += end; // offset do array

            }


        } catch (IOException e) {
            /*
            -> Não encontrou ficheiro / algo correu mal
            -> Enviar end connection
             */
            System.out.println("[ServerRun] Ficheiro não encontrado!");
            Packet pToSend = new Packet(fsChunk.getPacketID(),this.getTransferID(0),3,0,0,null);

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
    public void run() {
        try {
        Packet packet;
        while (true) {
            while (((packet = (Packet) this.packets.iWannaPop()) == null));

            switch (packet.getType()) {
                case 1:
                    System.out.println("RECEIVED ACK");
                    if(packet.getFlag() == 1){
                        handleRequest(packet,packet.getOffset());
                    }
                    break;
                case 5:
                    handleRequest(packet);
                    break;
                default:
                    System.out.println("[ServerRunThread]Something went wrong:\n" +
                            "[ServerRunThread]\n" + packet.toString());
            }

        }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
