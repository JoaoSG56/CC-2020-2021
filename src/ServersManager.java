import java.util.Set;

public class ServersManager extends Thread {
    private ServersInfo servers;

    public ServersManager(ServersInfo servers){
        this.servers = servers;
    }

    public void run() {
        Set<FastFileSrv> s;
        while (true) {
            try {
                s = servers.getFastFileSrvs();
                if(s.isEmpty()) {
                    return;
                }
                for (FastFileSrv f : s) {
                    if (f.decrementTimeUp(1) <= 0) {
                        this.servers.removeServer(f.getName());
                    }
                }
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
