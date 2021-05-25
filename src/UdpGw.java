import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.*;


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
                //System.out.println("[UdpGw] Received connection from " + packet.getAddress());
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
                        /*
                        push packet para que a thread acabe
                                        ou
                                acrescentar flag
                         */
                        System.out.println("Recebi end connection");
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
                        //System.out.println("[UdpGw]\n"+fsChunk.toString());
                        Socket s;
                        if((s =this.clientInfo.getClient(fsChunk.getPacketID())) != null) {
                            //System.out.println("[UdpGw] problem solved - " + this.clientInfo.getLength());
                            this.dataStack.push(new Response(fsChunk.getPacketID(), s, fsChunk));
                        }
                        /*
                        if(fsChunk.getFlag() == 0) {
                            System.out.println("freeing server ...");
                            serversInfo.freeServer(fsChunk.getAddr(), fsChunk.getPort());
                        }
                        */
                        break;


                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

