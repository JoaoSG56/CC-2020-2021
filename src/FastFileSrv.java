import java.net.InetAddress;

public class FastFileSrv {
    private String name;
    private InetAddress ip;
    private int timeUp;

    public FastFileSrv(String name, InetAddress ip){
        this.name = name;
        this.ip = ip;
        this.timeUp = 20;
    }

    public int getTimeUp(){
        return timeUp;
    }

    public InetAddress getIp(){
        return this.ip;
    }

    public String getName(){
        return this.name;
    }
}
