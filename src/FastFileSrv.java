import java.net.InetAddress;

public class FastFileSrv {
    private String name;
    private InetAddress ip;
    private int timeUp;
    private boolean occupied;
    private int port;

    private float ocupacao;
    private int n_threads;


    public FastFileSrv(String name, InetAddress ip, int timeUp, int port){
        this.name = name;
        this.ip = ip;
        this.timeUp = timeUp;
        this.occupied = false;
        this.port = port;
        this.ocupacao = 0;
        this.n_threads = 1;

    }

    public FastFileSrv(String name, InetAddress ip, int timeUp, int port,int n_threads){
        this.name = name;
        this.ip = ip;
        this.timeUp = timeUp;
        this.port = port;
        this.ocupacao = 0;
        this.n_threads = n_threads;
    }

    public FastFileSrv(FastFileSrv f){
        this.name = f.getName();
        this.ip = f.getIp();
        this.timeUp = f.getTimeUp();
        this.port = f.getPort();
        this.ocupacao = f.getOcupacao();
        this.n_threads = f.getN_threads();
    }

    public float getOcupacao(){
        return this.ocupacao;
    }

    public void incrementOcupacao(){
        this.ocupacao += (float)1 / this.n_threads;
    }

    public void decrementOcupacao(){
        this.ocupacao -= (float)1 / this.n_threads;
    }

    public int getN_threads(){
        return this.n_threads;
    }

    public int getTimeUp(){
        return timeUp;
    }

    public InetAddress getIp(){
        return this.ip;
    }

    public int getPort(){
        return this.port;
    }

    public String getName(){
        return this.name;
    }

    public void setTimeUp(int value){
        this.timeUp = value;
    }

    public int decrementTimeUp(int value){
        this.timeUp-=value;
        return this.timeUp;
    }

    public FastFileSrv clone(){
        return new FastFileSrv(this);
    }

}
