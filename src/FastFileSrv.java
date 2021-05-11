import java.net.InetAddress;

public class FastFileSrv {
    private String name;
    private InetAddress ip;
    private int timeUp;
    private boolean occupied;
    private int port;

    public FastFileSrv(String name, InetAddress ip){
        this.name = name;
        this.ip = ip;
        this.timeUp = 20;
        this.port = 80;
        this.occupied = false;
    }

    public FastFileSrv(String name, InetAddress ip, int timeUp, int port){
        this.name = name;
        this.ip = ip;
        this.timeUp = timeUp;
        this.occupied = false;
        this.port = port;
    }

    public FastFileSrv(FastFileSrv f){
        this.name = f.getName();
        this.ip = f.getIp();
        this.timeUp = f.getTimeUp();
        this.occupied = f.isOccupied();
        this.port = f.getPort();
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

    public boolean isOccupied(){
        return this.occupied;
    }

    public void setOccupied(boolean oc){
        this.occupied = oc;
    }

    public int decrementTimeUp(int value){
        this.timeUp-=value;
        return this.timeUp;
    }

    public FastFileSrv clone(){
        return new FastFileSrv(this);
    }

}
