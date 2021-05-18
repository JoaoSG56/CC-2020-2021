import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class ClientHttpHandler extends Thread {
    private PacketStack packetStack;

    public ClientHttpHandler(PacketStack packetStack) {
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
                    System.out.println("[ClientHttpHandler] encontrada resposta");


                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getClientSocket().getOutputStream()));
                    Packet p = s.getData();

                    new Thread("aux") { // thread responsável por responder ao cliente
                        private final BufferedWriter bwOut = out;
                        private final Packet packet = p;

                        public void run() {
                            try {
                                bwOut.write(packet.getPayloadStr().replace("\0", ""));
                                bwOut.flush();
                                bwOut.close();
                            } catch (IOException e) {
                                System.out.println("[ClientHttpHandler-Thread] Error:");
                                e.printStackTrace();
                            }
                        }
                    }.start();


                } else {
                    //System.out.println("[1] HttpClientHandler:> Sleeping...");
                    Thread.sleep(2000);
                }
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
