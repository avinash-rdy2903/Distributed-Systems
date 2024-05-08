
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
public class Reducer extends Slave{
    
    private final HashMap<String,List<String>> table = new HashMap<>();
    public Reducer(String slaveName,long masterId,boolean isMap) throws IOException{
        super(slaveName,masterId,isMap);
    }
    @Override
    public void run(){
        List<Future<?>> futures = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(reds);
        for(int i=0;i<maps;i++){
            try {
                futures.add(pool.submit(new MapHandle(this.socket.accept(),table)));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("for loop");
        }
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println(table.size()+" table");
        try{
            Socket s = new Socket(this.masterIp, this.masterPort);
            PrintWriter w = new PrintWriter(s.getOutputStream(),true);
            for(Map.Entry<String,List<String>> it: table.entrySet()){
                w.println(it.getKey()+"="+it.getValue().toString());
                // in.readLine();
            }
            w.println("!q");
            s.close();
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        pool.shutdown();
        try {
            this.socket.close();
        }catch(IOException e){
            System.out.println(e.getMessage()+" err closing reducer socket");
        }

    }
    public static void main(String[] args) throws IOException{
        Reducer red = new Reducer(args[0],Long.parseLong(args[1]),false);
        red.run();
    }
}
