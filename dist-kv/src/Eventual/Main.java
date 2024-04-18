package Eventual;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        int nPartitions = Integer.parseInt(args[0]);
        EventualPartition[] sps = new EventualPartition[nPartitions];
        ExecutorService es = Executors.newFixedThreadPool(nPartitions);
        for(int i=0;i<sps.length;i++){
            sps[i] = new EventualPartition(i,"127.0.0.1", 8080, "234.0.0.0", 4446, nPartitions);
            es.execute(new Thread(sps[i]));
        }
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                es.shutdownNow();
                for(int i=0;i<nPartitions;i++){
                    try {
                        sps[i].close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Terminated all running threads");
            }
        });
    }
}
