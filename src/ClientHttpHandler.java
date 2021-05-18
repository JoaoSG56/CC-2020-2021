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
    Map<Integer,StackShared> clientStacks;

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

                    if(!clientStacks.containsKey(s.getId())) { // se não contém

                        StackShared cStack = new StackShared();
                        this.clientStacks.put(s.getId(), cStack);

                        System.out.println("a");
                        if(s.getClientSocket() == null) System.out.println("[ClientHttpHandler] - getClientSocket");
                        System.out.println(s.getClientSocket().toString());
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getClientSocket().getOutputStream()));
                        System.out.println("b");

                        Packet p = s.getData();
                        if(p == null) System.out.println("whaat");
                        cStack.push(p);

                        new Thread(new Responder(out,cStack,null),"Responder").start();
                    } else{
                        this.clientStacks.get(s.getId()).push(s.getData());
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
