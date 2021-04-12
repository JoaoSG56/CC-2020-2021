import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;

public class HttpGw {
    private int port;
    private ServersInfo servidores;
    private PacketStack packetStack;
    private int idPacket; // not used for now

    public HttpGw(int port) {
        this.packetStack = new PacketStack();
        this.servidores = new ServersInfo(); // não precisa do acesso
        this.port = port;
        this.idPacket = 0;
    }

    public void start() throws IOException {

        new Thread(new UdpGw(this.packetStack, this.servidores)).start(); // inicializar UdpGw

        /*
         inicializar ServersManager - responsável por
         manter servidores ativos
         */
        new ServersManager(this.servidores).start();

        // falta inicializar thread responsável por devolver resposta ao cliente

        int idRequest = 0;
        ServerSocket socket = new ServerSocket(port);
        System.out.println("HttpGw:> Listening on port " + port);
        Socket client;
        while ((client = socket.accept()) != null) {
            System.out.println("Received connection from " + client.getRemoteSocketAddress().toString());
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String path = in.readLine().split(" ")[1];
            System.out.println("PATH: " + path);

            this.packetStack.push_clientRequest(new Request(idRequest++,client,path));

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
