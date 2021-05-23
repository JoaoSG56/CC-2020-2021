import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;

import java.util.*;

public class ClientHttpHandler extends Thread {
    private StackShared packetStack;
    private DatagramSocket socket;
    private Map<Integer,StackShared> clientStacks;
    private ServersInfo servidores;
    private int serverPort;

    public ClientHttpHandler(StackShared packetStack,DatagramSocket socket,ServersInfo servidores,int serverPort) {
        this.packetStack = packetStack;
        this.clientStacks = new HashMap<>();
        this.socket = socket;
        this.servidores = servidores;
        this.serverPort = serverPort;
    }


    public void run() {

        try {
            //this.ss = new ServerSocket(port);
            System.out.println("[1] HttpClientHandler:> ready to send");
            boolean running = true;
            Response s;
            while (running) {
                if ((s = (Response) this.packetStack.pop()) != null) { // tem resposta
                    //System.out.println("[ClientHttpHandler] encontrada resposta");

                    if(!clientStacks.containsKey(s.getId())) { // se não contém
                        // criar nova entrada para o packet com o id s.getId()
                        StackShared cStack = new StackShared();
                        this.clientStacks.put(s.getId(), cStack);

                        if(s.getClientSocket() == null) System.out.println("[ClientHttpHandler] - getClientSocket");
                        System.out.println(s.getClientSocket().toString());
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getClientSocket().getOutputStream()));

                        Packet p = s.getData();
                        if(p == null) System.out.println("whaat");
                        cStack.push(p);

                        new Thread(new Responder(s.getId(),out,cStack,this.socket,s.getServer(),s.getPort(),servidores,this.serverPort),"Responder").start();
                    } else{
                        this.clientStacks.get(s.getId()).push(s.getData());
                    }

                } else {
                    //System.out.println("[1] HttpClientHandler:> Sleeping...");
                    // await e signal //
                    //System.out.println("já não deve ser preciso isto");
                    Thread.sleep(2000);
                }
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
