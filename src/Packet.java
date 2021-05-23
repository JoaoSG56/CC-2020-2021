import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Packet {

    private int packetID;

    private String transferKey;

    private int type;

    private int offset;

    private byte[] payload;

    public Packet(int packetID, String transferKey, int type, int offset, byte[] payload) {
        this.packetID = packetID;
        this.transferKey = transferKey;
        this.type = type;
        this.offset = offset;
        this.payload = payload;
    }

    // Id TransferKey[IP:transferID:port] Type Offset Payload

    public Packet(byte[] datagram) {
        this.packetID = ByteBuffer.wrap(datagram, 0, 4).getInt();

        byte[] destIp = new byte[4];
        String tKey;
        try {
            System.arraycopy(datagram, 4, destIp, 0, 4);
            //System.out.println(Arrays.toString(destIp));
            //System.out.println("[PACKET]: " + InetAddress.getByAddress(destIp));
            tKey = InetAddress.getByAddress(destIp).getHostAddress();
            tKey += ":" + ByteBuffer.wrap(datagram, 8, 4).getInt();
            tKey += ":" + ByteBuffer.wrap(datagram, 12, 4).getInt();
            this.transferKey = tKey;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.type = ByteBuffer.wrap(datagram, 16, 4).getInt();
        this.offset = ByteBuffer.wrap(datagram, 20, 4).getInt();

        byte[] data = new byte[datagram.length - 24];

        System.arraycopy(datagram, 24, data, 0, datagram.length - 24);

        this.payload = data;

    }

    public int getPort(){
        return Integer.parseInt(this.transferKey.split(":")[2]);
    }

    public int getPacketID(){
        return this.packetID;
    }

    public InetAddress getAddr() throws UnknownHostException {
        return InetAddress.getByName(this.transferKey.split(":")[0]);
    }

    public int getOffset(){
        return this.offset;
    }

    public int getFlag(){
        return Integer.parseInt(this.transferKey.split(":")[1]);
    }

    public byte[] packetToBytes() {
        try {
            byte[] id = intToByteArray(this.packetID);
            String[] tkey = this.transferKey.split(":");
            System.out.println("[PACKET to Bytes]: "+ tkey[0] + "\n"+InetAddress.getByName(tkey[0]).getHostAddress());
            byte[] ip_address = InetAddress.getByName(tkey[0]).getAddress();
            byte[] transferID = intToByteArray(Integer.parseInt(tkey[1]));
            byte[] port = intToByteArray(Integer.parseInt(tkey[2]));

            byte[] type = intToByteArray(this.type);
            byte[] offset = intToByteArray(this.offset);

            int length = 0;
            if(this.payload != null)
                length = this.payload.length;

            byte[] data = new byte[24 + length];

            System.arraycopy(id,0,data,0,4);
            System.arraycopy(ip_address,0,data,4,4);
            System.arraycopy(transferID,0,data,8,4);
            System.arraycopy(port,0,data,12,4);
            System.arraycopy(type,0,data,16,4);
            System.arraycopy(offset,0,data,20,4);
            if(length != 0)
                System.arraycopy(this.payload,0,data,24,length);
            return data;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] intToByteArray(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    public String toString() {

        return "ID: " + this.packetID + '\n' +
                "TransferKey: " + this.transferKey + '\n' +
                "Type: " + this.type + '\n' +
                "Offset: " + this.offset + '\n' +
                "PAYLOAD: \n" + new String(this.payload, StandardCharsets.UTF_8);

    }

    public int getType(){
        return this.type;
    }

    public String getPayloadStr(){
        return new String(this.payload,StandardCharsets.UTF_8).replace("\0","");
    }

    public int getLength(){
        return this.payload.length + 24;
    }


}
