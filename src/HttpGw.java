import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpGw {
    public static void main(String[] args) throws IOException {
    final int port = 80;
        ServerSocket ss = new ServerSocket(port);
        System.out.println("Listening on port " + port);
        Socket s;
        while ((s = ss.accept()) != null) {
            System.out.println("Conection from " + s.getRemoteSocketAddress().toString());
        }

        ss.close();
    }
}
