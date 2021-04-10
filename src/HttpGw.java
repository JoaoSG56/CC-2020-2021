import java.io.BufferedReader;
import java.io.IOException;
import java.net.*;

public class HttpGw {
    private int port;
    private ServersInfo servidores;
    private int idPacket;

    public HttpGw(int port) {
        this.servidores = new ServersInfo();
        this.port = port;
    }

    public void start() throws IOException {
        System.out.println("Listening on port " + port);
        System.out.println("I am " + InetAddress.getByName("localhost"));
        DatagramSocket socket = new DatagramSocket(port);
        boolean runing = true;
        while (runing) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf,buf.length);
            socket.receive(packet);
            System.out.println("Received connection from " + packet.getAddress());
            Packet fsChunk = new Packet(packet.getData());
            System.out.println(fsChunk.toString());
        }
    }


    public static void main(String[] args) throws IOException {
        HttpGw server = new HttpGw(80);
        server.start();
    }

    /*

        public void start() throws IOException {
        ServerSocket socket = new ServerSocket(port);
        System.out.println("Listening on port " + port);
        Socket client;
        while ((client = socket.accept()) != null) {
            System.out.println("Received connection from " + client.getRemoteSocketAddress().toString());
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String path = in.readLine().split(" ")[1];
            System.out.println("PATH: " + path);

        }


     */
}
