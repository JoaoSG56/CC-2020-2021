
public class Request {
    private int idRequest;

    private String pathRequest;

    public Request(int id, String path) {
        this.idRequest = id;

        this.pathRequest = path;
    }

    public int getId() {
        return this.idRequest;
    }

    public String getPathRequest(){
        return this.pathRequest;
    }
}
