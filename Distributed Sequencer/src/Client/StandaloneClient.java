package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class StandaloneClient implements Runnable{
    private Socket[] sockets;
    private int clientId;
    private static final Random rand = new Random();
    private static Integer globalReceiverCount = 0;
    private static final int totalIterations = 2;

    public StandaloneClient(int clientId,String hostIp,int[] sequencerPorts) throws IOException {
        sockets = new Socket[sequencerPorts.length];
        for(int i=0;i<sequencerPorts.length;i++){
            sockets[i] = new Socket(hostIp,sequencerPorts[i]);
        }
        this.clientId = clientId;
    }
    @Override
    public void run(){
        try{
            BufferedReader[] in = new BufferedReader[sockets.length];
            PrintWriter[] out = new PrintWriter[sockets.length];
            for(int i=0;i<sockets.length;i++){
                in[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
                out[i] = new PrintWriter(sockets[i].getOutputStream(),true);
            }
            for (int dummy = 0; dummy < this.totalIterations; dummy++) {
                // we can change the total requests here
                int req=5;
                for (int i = 0; i < req; i++) {
                    synchronized (this.globalReceiverCount) {
                        int ind = rand.nextInt(sockets.length);
                        System.out.println("Sequencer-"+ind);
                        out[ind].println("get-id");
                        this.globalReceiverCount+=1;
                        System.out.println("In client-"+this.clientId +"Global counter-" + this.globalReceiverCount.intValue() + " and ID as - " + in[ind].readLine());
                        System.out.println();
                    }

                }
                for (int i = 0; i < req; i++) {

                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        for(int i=0;i<sockets.length;i++){
            try {
                sockets[i].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
