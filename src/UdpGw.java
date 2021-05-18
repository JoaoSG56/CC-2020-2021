import java.io.IOException;
import java.net.*;


public class UdpGw implements Runnable {

    private int port;
    private DatagramSocket socket;
    private PacketStack dataStack;
    private ClientInfo clientInfo;
    private ServersInfo serversInfo;

    public UdpGw(DatagramSocket socket,ClientInfo clientInfo,PacketStack d, ServersInfo serversInfo,int port) {
        this.clientInfo = clientInfo;
        this.dataStack = d;
        this.serversInfo = serversInfo;
        this.port = port;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("UdpGw Listening on port " + port);
            System.out.println("I am UdpGw on " + InetAddress.getLocalHost().getHostAddress());
            boolean runing = true;
            while (runing) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf,buf.length);
                socket.receive(packet);
                System.out.println("Received connection from " + packet.getAddress());
                Packet fsChunk = new Packet(packet.getData());

//                System.out.println(fsChunk.toString());
//                System.out.print("\n\n");


                switch (fsChunk.getType()){
                    case 2:
                        // renew
                        this.serversInfo.pushPacket(fsChunk);
                        break;
                    case 4:
                        // data
                        System.out.println(fsChunk.toString());
                        System.out.print("\n\n");
                        serversInfo.freeServer(fsChunk.getAddr(),fsChunk.getPort());
                        this.dataStack.push_clientResponse(new Response(this.clientInfo.getClient(fsChunk.getPacketID()),fsChunk));
                        this.clientInfo.removeClient(fsChunk.getPacketID());
                        break;


                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

