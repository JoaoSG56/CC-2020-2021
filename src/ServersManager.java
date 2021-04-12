import java.net.UnknownHostException;
import java.util.Set;

/*
    Classe responsável por "limpar" os servidores inativos e/ou renovar
 */

public class ServersManager extends Thread {
    private ServersInfo servers;

    public ServersManager(ServersInfo servers){
        this.servers = servers;
    }

    private void handlePendingPackets() throws UnknownHostException {
        Packet p;
        while((p = this.servers.popPacket()) != null) {
            if (p.getType() == 2) { // just making sure
                this.servers.renewServer(p.getPayloadStr(),p.getAddr());
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
        System.out.println("Im Server Manager");
        while (true) {
            try {

                handlePendingPackets();

                sweepServers();

                sleep(1000);
            } catch (InterruptedException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
}
