import java.io.IOException;
import java.net.*;

/*
    Classe correspondende aos FastFileServers
 */

class ServerRun {
    private DatagramSocket socket;
    private InetAddress address;

    private InetAddress connectedServer;

    private String name;

    ServerRun(String name, InetAddress connectedServer) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName("localhost");

        this.name = name;
        this.connectedServer = connectedServer;
    }


    public static void main(String[] args) throws SocketException, UnknownHostException {
        System.out.println(args[0]);
        ServerRun sr = new ServerRun(args[0], InetAddress.getByName(args[1]));

        Thread t = new Thread("aux") { // thread responsável por manter servidor vivo
            private DatagramSocket socket = sr.socket;
            private byte[] buf = new Packet(-1, sr.address.getHostAddress() + ":" + 0 + ":" + 80, 2, 0, sr.name.getBytes()).packetToBytes();
            private DatagramPacket packet = new DatagramPacket(buf, buf.length, sr.connectedServer, 80);
            public void run() {
                boolean running = true;
                while (running) {
                    try {
                        socket.send(packet);
                        Thread.sleep(10000);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                        running = false;
                        socket.close();
                    }
                }
            }
        };
        t.start();

        /*
         receber pedidos!!
         */








        t.interrupt();
    }

}
