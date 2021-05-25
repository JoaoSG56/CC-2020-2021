import java.net.Socket;

/**
 * Classe responsável por armazenar um pedido que foi feito recentemente
 */
public class RecentRequest {
    private Socket socket;
    private int timePassed;
    private int requestNumber;

    /**
     * Construtor de cópia
     * @param rr RecentRequest
     */
    public RecentRequest(RecentRequest rr) {
        this.socket = rr.getSocket();
        this.timePassed = rr.getTimePassed();
        this.requestNumber = rr.getRequestNumber();
    }

    /**
     * Construtor por parâmetros
     * @param s Socket do Cliente
     */
    public RecentRequest(Socket s) {
        this.socket = s;
        this.timePassed = 1;
        this.requestNumber = 1;
    }

    /**
     * Método que retorna uma fração do número de requests por tempo
     * @return float
     */
    public float getRequestPTime() {
        return (float) this.requestNumber / this.timePassed;

    }

    /**
     * Método que adiciona ao número de requests por 1
     */
    public void addRequest() {
        this.requestNumber += 1;
    }

    /**
     * Método que adiciona ao tempo passado por 1
     */
    public void add1Sec() {
        this.timePassed += 1;
    }

    /**
     * Método que retorna o Socket do Cliente
     * @return Socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Método que retorna o tempo passado
     * @return int
     */
    public int getTimePassed() {
        return timePassed;
    }

    /**
     * Método que retorna o número de Requests
     * @return int
     */
    public int getRequestNumber() {
        return requestNumber;
    }

    /**
     * Método de clone
     * @return RecentRequest
     */
    public RecentRequest clone() {
        return new RecentRequest(this);
    }

}
