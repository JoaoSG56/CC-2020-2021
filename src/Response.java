import java.net.InetAddress;
import java.net.Socket;

public class Response {
    private int id;
    private Socket clientSocket;
    private Packet data;
    private InetAddress fromServer;

    public Response(int id, Socket clientSocket, Packet data,InetAddress fromServer) {
        this.id = id;
        this.clientSocket = clientSocket;
        this.data = data;
        this.fromServer = fromServer;
    }

    public int getId(){
        return this.id;
    }

    public InetAddress getServer(){
        return this.fromServer;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Packet getData(){
        return this.data;
    }

}
