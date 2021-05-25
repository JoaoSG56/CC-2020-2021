/**
 * Classe responsável por armazenar dados relativos a um Request
 */
public class Request {
    private int idRequest;

    private String pathRequest;

    /**
     * Construtor por parâmetros
     * @param id id do Request
     * @param path String do ficheiro
     */
    public Request(int id, String path) {
        this.idRequest = id;

        this.pathRequest = path;
    }

    /**
     * Método que retorna o id do Request
     * @return int
     */
    public int getId() {
        return this.idRequest;
    }

    /**
     * Método que retorna o Path do ficheiro relativo ao Request
     * @return String
     */
    public String getPathRequest(){
        return this.pathRequest;
    }
}
