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

    public FastFileSrv(String name, InetAddress ip, int timeUp){
        this.name = name;
        this.ip = ip;
        this.timeUp = timeUp;
    }

    public FastFileSrv(FastFileSrv f){
        this.name = f.getName();
        this.ip = f.getIp();
        this.timeUp = f.getTimeUp();
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
