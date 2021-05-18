import java.util.Comparator;

public class PacketComparatorByOffset implements Comparator<Packet> {

    @Override
    public int compare(Packet o1, Packet o2) {
        return o1.getOffset() - o2.getOffset();
    }
}
