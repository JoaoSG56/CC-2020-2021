import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;


public class UdpGw implements Runnable {

    private int port;
    private DatagramSocket socket;
    private StackShared dataStack;
    private ClientInfo clientInfo;
    private ServersInfo serversInfo;
    private AcksToConfirm acksToConfirm;

    public UdpGw(DatagramSocket socket,ClientInfo clientInfo,StackShared d, ServersInfo serversInfo,int port,AcksToConfirm acksToConfirm) {
        this.clientInfo = clientInfo;
        this.dataStack = d;
        this.serversInfo = serversInfo;
        this.port = port;
        this.socket = socket;
        this.acksToConfirm = acksToConfirm;
    }

    @Override
    public void run() {
        try {
            System.out.println("UdpGw Listening on port " + port);
            System.out.println("I am UdpGw on " + InetAddress.getLocalHost().getHostAddress());
            boolean running = true;
            while (running) {
                byte[] buf = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buf,buf.length);
                socket.receive(packet);
                Packet fsChunk = new Packet(packet.getData());

                switch (fsChunk.getType()){
                    case 1:
                        this.acksToConfirm.removePacketId(fsChunk.getPacketID());
                        break;
                    case 2:
                        // renew
                        this.serversInfo.pushPacket(fsChunk);
                        break;
                    case 3:
                        // end connection
                        Socket aux = this.clientInfo.getClient(fsChunk.getPacketID());

                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(aux.getOutputStream()));
                        out.write("HTTP/1.0 404 NOT FOUND\n");
                        out.write("Connection: close\n");
                        out.flush();
                        out.close();

                        // para que Responder caso exista, dê exit
                        this.dataStack.push(new Response(fsChunk.getPacketID(),aux,fsChunk));

                        // diminuir ocupação do servidor
                        this.serversInfo.freeServer(fsChunk.getAddr(),fsChunk.getPort());

                        // remover cliente
                        this.clientInfo.removeClient(fsChunk.getPacketID());
                        break;
                    case 4:
                        Socket s;
                        if((s =this.clientInfo.getClient(fsChunk.getPacketID())) != null) {
                            this.dataStack.push(new Response(fsChunk.getPacketID(), s, fsChunk));
                        }
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

