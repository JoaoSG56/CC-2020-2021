import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class HttpGw {
    private int port;
    private ServersInfo servidores;
    private StackShared packetStack;
    private ClientInfo clientInfo;
    private int idPacket; // not used for now

    public HttpGw(int port) {
        this.packetStack = new StackShared();
        this.servidores = new ServersInfo(); // não precisa do acesso
        this.port = port;
        this.idPacket = 0;
        this.clientInfo = new ClientInfo();
    }

    public void start() throws IOException {
        DatagramSocket ds = new DatagramSocket(this.port);
        new Thread(new UdpGw(ds,this.clientInfo,this.packetStack, this.servidores,this.port)).start(); // inicializar UdpGw

        /*
         inicializar ServersManager - responsável por
         manter servidores ativos
         */
        new ServersManager(this.servidores).start();

        // falta inicializar thread responsável por devolver resposta ao cliente
        new ClientHttpHandler(this.packetStack).start();



        int idRequest = 0;
        ServerSocket socket = new ServerSocket(port);
        System.out.println("HttpGw:> Listening on port " + port);
        Socket client;
        while ((client = socket.accept()) != null) {
            System.out.println("Received connection from " + client.getRemoteSocketAddress().toString());
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String path = in.readLine().split(" ")[1];
            System.out.println("PATH: " + path);
            if (!path.equals("/")) {
                System.out.println("[HTTPGW] VAI ACRESCENTAR : " + idRequest + " Cliente : " + client);
                this.clientInfo.addClient(idRequest,client);
                new Thread(new ClientUdpHandler(ds,new Request(idRequest++, path), this.servidores, InetAddress.getLocalHost(), this.port)).start();
            }
            else System.out.println("path impossível");
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
