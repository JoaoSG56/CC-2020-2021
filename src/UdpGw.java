import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpGw implements Runnable {

    private int port;
    private ServersInfo servidores;

    private PacketStack dataStack;

    public UdpGw(PacketStack d, ServersInfo s) {
        this.dataStack = d;
        this.servidores = s;
        this.port = 80;
    }

    @Override
    public void run() {
        try {
            //System.out.println("UdpGw Listening on port " + port);
            //System.out.println("I am UdpGw on " + InetAddress.getLocalHost().getHostAddress());
            //this.socket = new DatagramSocket(port);
            boolean runing = true;
            Request cReq;
            while (runing) {
                while ((cReq = this.dataStack.pop_clientRequest()) == null) {
                    System.out.println("im sleeping");
                    Thread.sleep(1000);
                }
                new Thread(new ClientHandler(cReq,this.servidores)).start();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
