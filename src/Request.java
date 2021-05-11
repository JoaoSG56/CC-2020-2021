import java.net.Socket;

public class Request {
    private int idRequest;
    private Socket clientSocket;
    private String pathRequest;

    public Request(int id, Socket socket, String path) {
        this.idRequest = id;
        this.clientSocket = socket;
        this.pathRequest = path;
    }

    public int getId() {
        return this.idRequest;
    }

    public Socket getSocket(){
        return this.clientSocket;
    }

    public String getPathRequest(){
        return this.pathRequest;
    }
}
