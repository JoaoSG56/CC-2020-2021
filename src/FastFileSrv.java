import java.net.InetAddress;

/**
 * Classe de armazenamento de toda a informação relativa a um FastFileServer
 */
public class FastFileSrv {
    private String name;
    private InetAddress ip;
    private int timeUp;
    private int port;

    private float ocupacao;
    private int n_threads;

    /**
     * Construtor por argumentos
     * @param name Nome do Servidor
     * @param ip Ip do Servidor
     * @param timeUp Tempo máximo permitido sem novo pedido de conexão
     * @param port Porta do Servidor
     * @param n_threads Número de threads relativas ao servidor
     */
    public FastFileSrv(String name, InetAddress ip, int timeUp, int port,int n_threads){
        this.name = name;
        this.ip = ip;
        this.timeUp = timeUp;
        this.port = port;
        this.ocupacao = 0;
        this.n_threads = n_threads;
    }

    /**
     * Construtor por cópia
     * @param f FastFileServer
     */
    public FastFileSrv(FastFileSrv f){
        this.name = f.getName();
        this.ip = f.getIp();
        this.timeUp = f.getTimeUp();
        this.port = f.getPort();
        this.ocupacao = f.getOcupacao();
        this.n_threads = f.getN_threads();
    }

    /**
     * Método que retorna a ocupação
     * @return float com o número de servidores usados/ número de servidores totais
     */
    public float getOcupacao(){
        return this.ocupacao;
    }

    /**
     * Método que incrementa a ocupação pelo fator 1/número de threads
     */
    public void incrementOcupacao(){
        this.ocupacao += (float)1 / this.n_threads;
    }

    /**
     * Método que decrementa a ocupação pelo fator 1/número de threads
     */
    public void decrementOcupacao(){
        this.ocupacao -= (float)1 / this.n_threads;
    }

    /**
     * Método que retorna número de threads
     */
    public int getN_threads(){
        return this.n_threads;
    }

    /**
     * Método que retorna o tempo que ainda lhe falta sem precisar de uma nova conexão
     * @return int Tempo que lhe falta
     */
    public int getTimeUp(){
        return timeUp;
    }

    /**
     * Método que retorna o InetAddress do FastFileServer
     * @return InetAddress
     */
    public InetAddress getIp(){
        return this.ip;
    }

    /**
     * Método que retorna a porta do FastFileServer
     * @return int
     */
    public int getPort(){
        return this.port;
    }

    /**
     * Método que retorna nome do FastFileServer
     * @return String
     */
    public String getName(){
        return this.name;
    }

    /**
     * Método que muda o tempo para um determinado valor
     * @param value int
     */
    public void setTimeUp(int value){
        this.timeUp = value;
    }

    /**
     * Método que decrementa o tempo por um determinado valor
     * @param value int
     * @return int com novo tempo
     */
    public int decrementTimeUp(int value){
        this.timeUp-=value;
        return this.timeUp;
    }

    /**
     * Método de clone
     * @return FastFileServer
     */
    public FastFileSrv clone(){
        return new FastFileSrv(this);
    }

}
