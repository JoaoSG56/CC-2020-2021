import java.util.Comparator;

/**
 * Classe Comparador usada para comparar 2 Packets pelo Offset
 */
public class PacketComparatorByOffset implements Comparator<Packet> {

    /**
     * Método de comparação
     * @param o1 Packet 1
     * @param o2 Packet 2
     * @return int
     */
    @Override
    public int compare(Packet o1, Packet o2) {
        return o1.getOffset() - o2.getOffset();
    }
}
