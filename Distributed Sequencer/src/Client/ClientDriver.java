package Client;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientDriver implements Closeable {
    private static int[] sequencerPorts;
    private static int nClients;
    private static String hostIp;
    private ExecutorService service;
    public ClientDriver(int n,String hostIp,int[] sequencerPorts) throws IOException {
        this.nClients = n;
        this.sequencerPorts = sequencerPorts;
        this.hostIp = hostIp;
        this.service = Executors.newFixedThreadPool(this.nClients);
    }
    public void start() throws IOException {
        for(int i=0;i<this.nClients;i++){
            service.execute(new Thread(new StandaloneClient(i,hostIp,sequencerPorts)));
        }
    }
    @Override
    public void close() throws IOException {
        service.shutdown();
        System.out.println("All the client requests have served");
    }

    public static void main(String[] args) {
        int nClients = Integer.parseInt(args[0]);
        int[] sequencerPorts = new int[args.length-1];
        for(int i=0;i< args.length-1;i++){
            sequencerPorts[i] = Integer.parseInt(args[i+1]);
        }
        System.out.println(Arrays.toString(sequencerPorts)+" "+nClients);
        // since all the sequencers are in the local network, I'm mentioning the host address as "localhost"
        try(ClientDriver driver = new ClientDriver(nClients, "localhost", sequencerPorts)){
            driver.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
