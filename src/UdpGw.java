import java.io.IOException;
import java.net.*;


public class UdpGw implements Runnable {

    private int port;
    private DatagramSocket socket;
    private StackShared dataStack;
    private ClientInfo clientInfo;
    private ServersInfo serversInfo;

    public UdpGw(DatagramSocket socket,ClientInfo clientInfo,StackShared d, ServersInfo serversInfo,int port) {
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
                //System.out.println("[UdpGw] Received connection from " + packet.getAddress());
                Packet fsChunk = new Packet(packet.getData());

//                System.out.println(fsChunk.toString());
//                System.out.print("\n\n");


                switch (fsChunk.getType()){
                    case 2:
                        // renew
                        this.serversInfo.pushPacket(fsChunk);
                        break;
                    case 4:
                        //System.out.println("[UdpGw] Packet ID : " + fsChunk.getPacketID());
                        Socket s;
                        if((s =this.clientInfo.getClient(fsChunk.getPacketID())) != null) {
                            //System.out.println("[UdpGw] problem solved - " + this.clientInfo.getLength());
                            this.dataStack.push(new Response(fsChunk.getPacketID(), s, fsChunk,packet.getAddress()));
                        }
                        if(fsChunk.getFlag() == 0) {
                            System.out.println("freeing server ...");
                            serversInfo.freeServer(fsChunk.getAddr(), fsChunk.getPort());
                        }

                        break;


                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

