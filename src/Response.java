import java.net.Socket;

public class Response {
    private Socket clientSocket;
    private Packet data;

    public Response(Socket clientSocket, Packet data) {
        this.clientSocket = clientSocket;
        this.data = data;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Packet getData(){
        return this.data;
    }

}
