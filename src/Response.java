import java.net.Socket;

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

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Packet getData(){
        return this.data;
    }

}
