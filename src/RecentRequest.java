import java.net.Socket;

public class RecentRequest {
    private Socket socket;
    private int timePassed;
    private int requestNumber;

    public RecentRequest(RecentRequest rr) {
        this.socket = rr.getSocket();
        this.timePassed = rr.getTimePassed();
        this.requestNumber = rr.getRequestNumber();
    }

    public RecentRequest(Socket s) {
        this.socket = s;
        this.timePassed = 1;
        this.requestNumber = 1;
    }

    public float getRequestPTime() {
        return (float) this.requestNumber / this.timePassed;

    }

    public void addRequest() {
        this.requestNumber += 1;
    }

    public void add1Sec() {
        this.timePassed += 1;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getTimePassed() {
        return timePassed;
    }


    public int getRequestNumber() {
        return requestNumber;
    }

    public RecentRequest clone() {
        return new RecentRequest(this);
    }

}
