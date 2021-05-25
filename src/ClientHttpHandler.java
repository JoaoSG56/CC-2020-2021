import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class ClientHttpHandler extends Thread {
    private StackShared packetStack;
    private DatagramSocket socket;
    private Map<Integer, StackShared> clientStacks;
    private ServersInfo servidores;
    private int serverPort;

    public ClientHttpHandler(StackShared packetStack, DatagramSocket socket, ServersInfo servidores, int serverPort) {
        this.packetStack = packetStack;
        this.clientStacks = new HashMap<>();
        this.socket = socket;
        this.servidores = servidores;
        this.serverPort = serverPort;
    }

    public void run() {

        try {
            System.out.println("[HttpClientHandler]:> ready to send");
            boolean running = true;
            Response s;
            while (running) {
                if ((s = (Response) this.packetStack.iWannaPop()) != null) { // tem resposta
                    if (!clientStacks.containsKey(s.getId())) { // se não contém
                        // criar nova entrada para o packet com o id s.getId()

                        if (s.getData().getType() != 3) {
                            StackShared cStack = new StackShared();
                            this.clientStacks.put(s.getId(), cStack);

                            if (s.getClientSocket() == null)
                                System.out.println("[ClientHttpHandler] - getClientSocket");
                            System.out.println(s.getClientSocket().toString());
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getClientSocket().getOutputStream()));

                            Packet p = s.getData();
                            cStack.push(p);

                            new Thread(new Responder(s.getId(), out, cStack, this.socket, s.getServer(), s.getPort(), servidores, this.serverPort), "Responder").start();
                        }
                    } else {
                        if (s.getData().getType() == 3) {
                            this.clientStacks.get(s.getId()).push(s.getData());
                            this.clientStacks.remove(s.getId());
                        } else
                            this.clientStacks.get(s.getId()).push(s.getData());
                    }

                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
