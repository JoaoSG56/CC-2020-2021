import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class ClientUdpHandler extends Thread {
    private Request request;
    private ServersInfo servidores;
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private ClientInfo clientInfo;
    private AcksToConfirm acksToConfirm;

    public ClientUdpHandler(DatagramSocket socket, Request r, ServersInfo servidores, InetAddress address, int port, ClientInfo clientInfo, AcksToConfirm acksToConfirm) {
        this.request = r;
        this.servidores = servidores;
        this.port = port;
        this.address = address;
        this.socket = socket;
        this.clientInfo = clientInfo;
        this.acksToConfirm = acksToConfirm;
    }

    @Override
    public void run() {
        try {
            System.out.println("[3] I am ClientHandlerUDP for Request " + request);
            FastFileSrv f;
            int timeOut = 0;
            if ((f = this.servidores.getFastFileSrv()) != null) {
                    System.out.println("[5] - ClientUdpHandler] Server found : " + f.getName());
                    Packet p = new Packet(this.request.getId(), this.address.getHostAddress() + ":" + 0 + ":" + this.port, 5, 0, this.request.getPathRequest().getBytes());
                    byte[] buf = p.packetToBytes();
                    System.out.println("[5] - ClientUdpHandler] IP: " + f.getIp().getHostAddress() + "\nPort: " + f.getPort());

                    DatagramPacket packet = new DatagramPacket(buf, buf.length, f.getIp(), f.getPort());
                    this.acksToConfirm.addPacketId(this.request.getId());
                do {
                    if(timeOut>5){
                        this.acksToConfirm.removePacketId(this.request.getId());
                        this.servidores.decrementOcupacao(f.getName());
                        this.clientInfo.removeClient(this.request.getId());
                        System.out.println("[ClientUdpHandler] Server unnable to respond!");
                        return;
                    }

                    socket.send(packet);
                    timeOut++;
                    System.out.println("[5] - ClientUdpHandler] Packet sent");
                    Thread.sleep(2000);
                } while (!this.acksToConfirm.wasReceived(this.request.getId()));
            } else {
                // falta aqui um await e signal na outra parte //
                this.clientInfo.removeClient(this.request.getId());
                System.out.println("[ClientUdpHandler] Server not found");
            }


        } catch (IOException | InterruptedException e) {
            System.out.println("exceção");
            e.printStackTrace();
        }
    }
}
