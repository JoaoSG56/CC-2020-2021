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

    public ClientUdpHandler(DatagramSocket socket,Request r, ServersInfo servidores, InetAddress address, int port){
        this.request = r;
        this.servidores = servidores;
        this.port = port;
        this.address = address;
        this.socket = socket;
    }

    private List<FastFileSrv> getServersToUse(){
        return this.servidores.getFastFileSrv(1);

    }

    @Override
    public void run() {
        try {
            System.out.println("[3] I am ClientHandlerUDP for Request " + request);
            FastFileSrv f;
            if((f = this.servidores.getFastFileSrv()) != null){
                System.out.println("[5] - ClientUdpHandler] Server found : " + f.getName());
                Packet p = new Packet(this.request.getId(),this.address.getHostAddress()+":"+0+":"+this.port,5,0,this.request.getPathRequest().getBytes());
                byte[] buf = p.packetToBytes();
                System.out.println("[5] - ClientUdpHandler] IP: " + f.getIp().getHostAddress()+"\nPort: " + f.getPort());

                DatagramPacket packet = new DatagramPacket(buf, buf.length,f.getIp(),f.getPort());
                socket.send(packet);
                System.out.println("[5] - ClientUdpHandler] Packet sent");
            } else {
                System.out.println("[ClientUdpHandler] Server not found");
            }
        } catch (IOException e) {
            System.out.println("exceção");
            e.printStackTrace();
        }
    }
}
