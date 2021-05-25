import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Classe responsável por armazenar os dados de uma Response
 */
public class Response {
    private int id;
    private Socket clientSocket;
    private Packet data;

    /**
     * Construtor da classe
     *
     * @param id            ID do packet
     * @param clientSocket  Socket do cliente
     * @param data          Informação do packet
     */
    public Response(int id, Socket clientSocket, Packet data) {
        this.id = id;
        this.clientSocket = clientSocket;
        this.data = data;
    }

    /**
     * Método que devolve o ID da resposta
     *
     * @return  Id da resposta
     */
    public int getId(){
        return this.id;
    }

    /**
     * Método que obtém a InetAddress de um servidor
     *
     * @return  InetAddress do servidor
     */
    public InetAddress getServer(){
        try {
            return this.data.getAddr();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Método que obtém a porta
     *
     * @return  Porta obtida
     */
    public int getPort(){
        return this.data.getPort();
    }

    /**
     * Método que devolve o socket do cliente
     *
     * @return  socket do cliente
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Método que devolve a data
     *
     * @return  pacote de dados
     */
    public Packet getData(){
        return this.data;
    }

}
