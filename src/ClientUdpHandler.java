import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

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

    private void sendServiceUnavailable(Socket s) {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

            out.write("HTTP/1.0 503 Service Unavailable\n");
            out.write("Connection: close\n");
            out.flush();
            out.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            FastFileSrv f;
            int timeOut = 0;
            if ((f = this.servidores.getFastFileSrv()) != null) {
                System.out.println("ClientUdpHandler] Server found : " + f.getName());
                byte[] data = this.request.getPathRequest().getBytes();
                Packet p = new Packet(this.request.getId(), this.address.getHostAddress() + ":" + 0 + ":" + this.port, 5, 0,Packet.getCRC32Checksum(data) ,data);
                byte[] buf = p.packetToBytes();


                DatagramPacket packet = new DatagramPacket(buf, buf.length, f.getIp(), f.getPort());
                this.acksToConfirm.addPacketId(this.request.getId());
                do {
                    if (timeOut > 5) {
                        System.out.println("[ClientUdpHandler] Server unnable to respond!");
                        this.acksToConfirm.removePacketId(this.request.getId());
                        this.servidores.decrementOcupacao(f.getName());
                        // inserir mensagem
                        Socket s = this.clientInfo.getClient(request.getId());
                        sendServiceUnavailable(s);


                        this.clientInfo.removeClient(this.request.getId());
                        return;
                    }

                    socket.send(packet);
                    timeOut++;
                    System.out.println("[ClientUdpHandler] Packet sent");
                    Thread.sleep(2000);
                } while (!this.acksToConfirm.wasReceived(this.request.getId()));
            } else {
                System.out.println("[ClientUdpHandler] Server not found");
                Socket s = this.clientInfo.getClient(request.getId());
                sendServiceUnavailable(s);

                this.clientInfo.removeClient(this.request.getId());
            }


        } catch (IOException | InterruptedException e) {
            System.out.println("exceção");
            e.printStackTrace();
        }
    }
}
