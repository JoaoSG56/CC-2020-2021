import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ClientHandler extends Thread{
    private Request request;
    private ServersInfo servidores;
    private DatagramSocket socket;
    private int port;

    public ClientHandler(Request r,ServersInfo servidores){
        this.request = r;
        this.servidores = servidores;
        this.port = 80;
    }

    @Override
    public void run() {
        try {
            System.out.println("I am ClientHandler for Request " + request);
            this.socket = new DatagramSocket(port);
            boolean runing = true;
            while (runing) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                System.out.println("Received connection from " + packet.getAddress());
                Packet fsChunk = new Packet(packet.getData());
                System.out.println(fsChunk.toString());
            }
        } catch (IOException e) {
            System.out.println("exceção");
            e.printStackTrace();
        }
    }
}
