import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ClientHttpHandler extends Thread {
    private PacketStack packetStack;
    Map<Socket, Stack<Packet>> clientStacks;

    public ClientHttpHandler(PacketStack packetStack) {
        this.packetStack = packetStack;
        this.clientStacks = new HashMap<>();
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

                    if(!clientStacks.containsKey(s.getClientSocket())) {

                        Stack<Packet> cStack = new Stack<>();
                        this.clientStacks.put(s.getClientSocket(), cStack);

                        System.out.println("a");
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getClientSocket().getOutputStream()));
                        System.out.println("b");

                        Packet p = s.getData();
                        cStack.push(p);

                        new Thread(new Responder(out,cStack,null)).start();
                    } else{
                        this.clientStacks.get(s.getClientSocket()).push(s.getData());
                    }

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
