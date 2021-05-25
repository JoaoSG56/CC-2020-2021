import java.net.UnknownHostException;

/**
 * Classe responsável por "limpar" os servidores inativos e/ou renovar
 */

public class ServersManager extends Thread {
    private ServersInfo servers;

    /**
     * Construtor parametrizado da classe
     *
     * @param servers   Informação dos servidores
     */
    public ServersManager(ServersInfo servers){
        this.servers = servers;
    }

    /**
     * Método que obtém os packets relativos a novas conexões e renova os servidores
     *
     * @throws UnknownHostException Exceção de host desconhecido
     */
    private void handlePendingPackets() throws UnknownHostException {
        Packet p;
        while((p = this.servers.popPacket()) != null) {
            if (p.getType() == 2) {
                this.servers.renewServer(p.getPayloadStr(),p.getAddr(),p.getPort());
            }
        }
    }

    /**
     * Método que elimina os servidores inativos
     */
    private void sweepServers(){
        for (FastFileSrv f : this.servers.getFastFileSrvs()) {
            if (f.decrementTimeUp(1) <= 0) {
                if(f.getOcupacao()!=1)
                    this.servers.removeServer(f.getName());
            }
        }
    }

    /**
     * Thread que lida com a gestão dos servers
     */
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
