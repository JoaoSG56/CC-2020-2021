import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Response {
    private int id;
    private Socket clientSocket;
    private Packet data;


    public Response(int id, Socket clientSocket, Packet data) {
        this.id = id;
        this.clientSocket = clientSocket;
        this.data = data;
    }

    public int getId(){
        return this.id;
    }

    public InetAddress getServer(){
        try {
            return this.data.getAddr();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getPort(){
        return this.data.getPort();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Packet getData(){
        return this.data;
    }

}
