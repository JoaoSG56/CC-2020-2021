import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class ClientUdpHandler extends Thread{
    private Request request;
    private ServersInfo servidores;
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public ClientUdpHandler(Request r, ServersInfo servidores, InetAddress address, int port){
        this.request = r;
        this.servidores = servidores;
        this.port = port;
        this.address = address;
    }

    private List<FastFileSrv> getServersToUse(){
        return this.servidores.getFastFileSrv(1);

    }

    @Override
    public void run() {
        try {
            System.out.println("I am ClientHandler for Request " + request);
            this.socket = new DatagramSocket(port);
            FastFileSrv f;
            if((f = this.servidores.getFastFileSrv()) != null){
                Packet p = new Packet(this.request.getId(),this.address.getHostAddress()+":"+0+":"+this.port,5,0,this.request.getPathRequest().getBytes());
                byte[] buf = p.packetToBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length,f.getIp(),f.getPort());
                socket.send(packet);

                socket.close(); // ack?
            }
        } catch (IOException e) {
            System.out.println("exceção");
            e.printStackTrace();
        }
    }
}
