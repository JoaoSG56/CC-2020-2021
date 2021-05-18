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

    /*
     verificar que existe flag == 0 e offset == 0
        ou
     verificar que existe flag == 0 e offsets seguidos
     */
    public static boolean checkIfItsFull(Set<Packet> packets){
        if(packets.size() == 1 || packets.size() == 0)
            return false;
        int lastOffSet = 0;
        int lastLPacket = 0;
        boolean noFlag = false;
        for(Packet p: packets){
            System.out.println("[ClientHttpHandler-thread]: " +
                    "\n\tlastOffSet+lastLPacket: " +
                    lastOffSet+lastLPacket +
                    "\n\tp.getOffset(): " +
                    p.getOffset());
           if(lastOffSet+lastLPacket != p.getOffset()) return false;
           lastOffSet = p.getOffset();
           lastLPacket = p.getLength();
           if(p.getFlag() == 0) noFlag = true;

        }

        return noFlag;
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
                    Stack<Packet> cStack = new Stack<>();
                    this.clientStacks.put(s.getClientSocket(),cStack);

                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getClientSocket().getOutputStream()));
                    Packet p = s.getData();
                    cStack.push(p);
                    new Thread("responder") { // thread responsável por responder ao cliente
                        private final BufferedWriter bwOut = out;
                        private Set<Packet> packetSet = new TreeSet<>(new PacketComparatorByOffset());
                        private Stack<Packet> stack = cStack;
                        public void run() {
                            try {
                                Packet packet;
                                while((packet = stack.pop()) != null){
                                    packetSet.add(packet);
                                    if(checkIfItsFull(packetSet))
                                        break;
                                }
                                for(Packet aux: packetSet) {
                                    bwOut.write(aux.getPayloadStr().replace("\0", ""));
                                    bwOut.flush();
                                }
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
