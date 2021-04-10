import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class HttpGw {
    private int port;
    private Map<InetAddress, DatagramSocket> servidores;

    public HttpGw(int port) {
        this.port = port;
    }

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
    }


    public static void main(String[] args) throws IOException {
        HttpGw server = new HttpGw(80);
        FFSrv s1 = new FFSrv("s1");
        FFSrv s2 = new FFSrv("s1");
        server.start();
    }
}
