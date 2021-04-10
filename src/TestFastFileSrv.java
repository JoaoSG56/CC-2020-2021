import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestFastFileSrv {

    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public TestFastFileSrv() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void sendEcho(String file) throws IOException {
        String path = "/Users/joao/Desktop/Universidade/CC-2020-2021/src/";

        File filePath = new File(path+file);

        byte[] bytes = Files.readAllBytes(filePath.toPath());


        Packet fsChunkpacket = new Packet(1,this.address.getHostAddress()+":"+1+":"+80,5,0,bytes);
        System.out.println(fsChunkpacket.toString());
        buf = fsChunkpacket.packetToBytes();
        DatagramPacket packet = new DatagramPacket(buf,buf.length,address,80);

        socket.send(packet);

        close();
    }

    public void close(){
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        TestFastFileSrv s = new TestFastFileSrv();

        String file = "testFile.txt";

        s.sendEcho(file);



    }

}
