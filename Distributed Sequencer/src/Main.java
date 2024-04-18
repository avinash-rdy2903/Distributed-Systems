import sequencer.Sequencer;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        int nSequencers = Integer.parseInt(args[0]);
        ServerSocket[] sockets = new ServerSocket[nSequencers];
        for (int i=0;i<nSequencers;i++){
            sockets[i] = new ServerSocket(0);
            sockets[i].setReuseAddress(true);
            System.out.println("Sequencer-"+ i+1+" running at address: "+sockets[i].getInetAddress()+" and Port: "+sockets[i].getLocalPort());
            System.out.println();
        }
        ExecutorService service = Executors.newFixedThreadPool(nSequencers);
        for(int i=nSequencers-1;i>=0;i--){
            service.execute(new Thread(new Sequencer(i+1,
                    sockets[i],
                    sockets[(i+1)%nSequencers].getLocalSocketAddress(),
                    (i==0))));
        }
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                service.shutdownNow();
                for(int i=0;i<nSequencers;i++){
                    try {
                        sockets[i].close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Terminated all running threads");
            }
        });
    }
}
