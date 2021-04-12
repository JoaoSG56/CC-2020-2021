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
            System.out.println("[6] HttpClientHandler:> ready to send");
            boolean running = true;
            Response s;
            while (running) {
                if ((s = this.packetStack.pop_clientResponse()) != null) { // tem resposta
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getClientSocket().getOutputStream()));
                    out.write(s.getData().toString());
                    out.flush();
                }
                else{
                    System.out.println("[26 HttpClientHandler:> Sleeping...");
                    Thread.sleep(2000);
                }
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
