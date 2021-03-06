import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
/**
 * Classe responsável por armazenar a informação relativa a um packet
 */
public class Packet {

    private int packetID;

    private String transferKey;

    private int type;

    private int offset;

    private long checksum;

    private byte[] payload;

    /*
     * Construtor
     * @param packetID id do pacote
     * @param transferKey identificador de tranferência
     * @param type tipo de pacote
     * @param offset
     * @param checksum
     * @param payload dados
     */
    public Packet(int packetID, String transferKey, int type, int offset, long checksum,byte[] payload) {
        this.packetID = packetID;
        this.transferKey = transferKey;
        this.type = type;
        this.offset = offset;
        this.checksum = checksum;
        this.payload = payload;
    }

    /**
     * Dado um array de bytes retorna o checksum relativo ao array
     * @param bytes
     * @return checksum do array
     */
    public static long getCRC32Checksum(byte[] bytes){
        Checksum crc32 = new CRC32();
        crc32.update(bytes,0,bytes.length);
        return bytes.length;
    }

    /**
     * Construtor que recebe um datagrama com argumento
     * @param datagram dados do packet
     */
    public Packet(byte[] datagram) {
        this.packetID = ByteBuffer.wrap(datagram, 0, 4).getInt();

        byte[] destIp = new byte[4];
        String tKey;
        try {
            System.arraycopy(datagram, 4, destIp, 0, 4);

            tKey = InetAddress.getByAddress(destIp).getHostAddress();
            tKey += ":" + ByteBuffer.wrap(datagram, 8, 4).getInt();
            tKey += ":" + ByteBuffer.wrap(datagram, 12, 4).getInt();
            this.transferKey = tKey;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.type = ByteBuffer.wrap(datagram, 16, 4).getInt();
        this.offset = ByteBuffer.wrap(datagram, 20, 4).getInt();
        this.checksum = ByteBuffer.wrap(datagram, 24, 8).getLong();

        byte[] data = new byte[datagram.length - 32];

        System.arraycopy(datagram, 32, data, 0, datagram.length - 32);

        this.payload = data;

    }

    /**
     * Método que devolve a porta do servidor de origem
     * @return porta
     */
    public int getPort(){
        return Integer.parseInt(this.transferKey.split(":")[2]);
    }

    /**
     * Método que devolve o ID do pacote
     * @return id
     */
    public int getPacketID(){
        return this.packetID;
    }

    public InetAddress getAddr() throws UnknownHostException {
        return InetAddress.getByName(this.transferKey.split(":")[0]);
    }

    /**
     * Devolve o checksum do pacote
     * @return checksum
     */
    public long getChecksum(){
        return this.checksum;
    }

    /**
     * Devolve o offset do pacote
     * @return offset
     */
    public int getOffset(){
        return this.offset;
    }

    /**
     * Devolve a flag do pacote
     * @return flag (0 ou 1)
     */
    public int getFlag(){
        return Integer.parseInt(this.transferKey.split(":")[1]);
    }

    /**
     * Converte o pacote num array de bytes
     * @return array de bytes
     */
    public byte[] packetToBytes() {
        try {
            byte[] id = intToByteArray(this.packetID);
            String[] tkey = this.transferKey.split(":");
            byte[] ip_address = InetAddress.getByName(tkey[0]).getAddress();
            byte[] transferID = intToByteArray(Integer.parseInt(tkey[1]));
            byte[] port = intToByteArray(Integer.parseInt(tkey[2]));

            byte[] type = intToByteArray(this.type);
            byte[] offset = intToByteArray(this.offset);

            byte[] checksum = floatToByteArray(this.checksum);

            int length = 0;
            if(this.payload != null)
                length = this.payload.length;

            byte[] data = new byte[32 + length];

            System.arraycopy(id,0,data,0,4);
            System.arraycopy(ip_address,0,data,4,4);
            System.arraycopy(transferID,0,data,8,4);
            System.arraycopy(port,0,data,12,4);
            System.arraycopy(type,0,data,16,4);
            System.arraycopy(offset,0,data,20,4);
            System.arraycopy(checksum,0,data,24,8);

            if(length != 0)
                System.arraycopy(this.payload,0,data,32,length);
            return data;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converte um int num array de bytes
     * @param i int
     * @return array de bytes
     */
    private byte[] intToByteArray(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    /**
     * Convert um float num array de bytes
     * @param i int
     * @return array de bytes
     */
    private byte[] floatToByteArray(final long i){
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(i);
        return bb.array();
    }

    /**
     * converte o pacote numa string
     * @return string
     */
    public String toString() {
        if(this.payload != null)
            return "ID: " + this.packetID + '\n' +
                    "TransferKey: " + this.transferKey + '\n' +
                    "Type: " + this.type + '\n' +
                    "Offset: " + this.offset + '\n' +
                    "Checksum: " + this.checksum + '\n' +
                    "PAYLOAD: \n" + new String(this.payload, StandardCharsets.UTF_8);
        else
            return "ID: " + this.packetID + '\n' +
                    "TransferKey: " + this.transferKey + '\n' +
                    "Type: " + this.type + '\n' +
                    "Offset: " + this.offset + '\n' +
                    "Checksum: " + this.checksum + '\n' +
                    "PAYLOAD: null\n";
    }

    /**
     * devolve o tipo do pacote
     * @return tipo
     */
    public int getType(){
        return this.type;
    }

    /**
     * Devolve o payload do pacote
     * @return string correspondente ao payload
     */
    public String getPayloadStr(){
        return new String(this.payload,StandardCharsets.UTF_8).replace("\0","");
    }

    /**
     * Devolve o payload do pacote
     * @return array de bytes correspondente ao payload
     */
    public byte[] getPayloadBytes(){
        return new String(this.payload).replace("\0","").getBytes();
    }

    /**
     * Devolve tamanho do payload
     * @return int
     */
    public int getPayloadLength(){
        return this.getPayloadStr().length();
    }

    /**
     * Devolve tamanho do pacote
     * @return int tamanho
     */
    public int getLength(){
        return this.payload.length + 32;
    }


}
