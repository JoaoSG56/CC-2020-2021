import java.net.UnknownHostException;

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
            if (p.getType() == 2) {
                this.servers.renewServer(p.getPayloadStr(),p.getAddr(),p.getPort());
            }
        }
    }

    private void sweepServers(){
        for (FastFileSrv f : this.servers.getFastFileSrvs()) {
            if (f.decrementTimeUp(1) <= 0) {
                if(f.getOcupacao()==0)
                    this.servers.removeServer(f.getName());
            }
        }
    }

    public void run() {
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
