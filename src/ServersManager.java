import java.util.Set;

public class ServersManager extends Thread {
    private ServersInfo servers;

    public ServersManager(ServersInfo servers){
        this.servers = servers;
    }

    private void handlePendingPackets(){
        Packet p;
        while((p = this.servers.popPacket()) != null) {
            if (p.getType() == 2) { // just making sure
                this.servers.renewServer(p.getPayloadStr());
            }
        }
    }

    private void sweepServers(){
        for (FastFileSrv f : this.servers.getFastFileSrvs()) {
            if (f.decrementTimeUp(1) <= 0) {
                this.servers.removeServer(f.getName());
            }
        }
    }

    public void run() {
        Set<FastFileSrv> s;
        while (true) {
            try {
                handlePendingPackets();

                sweepServers();

                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
