import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;

public class ClientHttpHandler extends Thread {
    private int port;
    //private ServerSocket ss;
    private PacketStack packetStack;

    public ClientHttpHandler(PacketStack packetStack, int port) {
        this.port = port;
        this.packetStack = packetStack;
    }

    public void run() {
        try {
            //this.ss = new ServerSocket(port);
            System.out.println("[1] HttpClientHandler:> ready to send");
            boolean running = true;
            Response s;
            while (running) {
                if ((s = this.packetStack.pop_clientResponse()) != null) { // tem resposta
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getClientSocket().getOutputStream()));
                    out.write("[1] teste : " + s.getData().toString());
                    out.flush();
                }
                else{
                    System.out.println("[1] HttpClientHandler:> Sleeping...");
                    Thread.sleep(2000);
                }
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}