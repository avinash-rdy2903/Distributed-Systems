
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
public class MapHandle implements Runnable {
    private static final ReadWriteLock rdLock =  new ReentrantReadWriteLock();
    private static final Lock writeLock = rdLock.writeLock();
    private Socket connection;
    private static HashMap<String,List<String>> reducer;
    public MapHandle(Socket s, HashMap<String,List<String>> list) throws IOException{
        this.connection = s;
        this.reducer = list;
    }
    @Override
    public void run(){
        System.out.println("mapper response started");

        InputStreamReader in = null;
        BufferedReader br = null;
        PrintWriter out = null;
        try {
            in = new InputStreamReader(connection.getInputStream());
            br = new BufferedReader(in);
            out = new PrintWriter(connection.getOutputStream(),true);
            String response = "";
            do {
                response = br.readLine();
                if(response.equals("!q")){
                    break;
                }
                // System.out.println(response);
                String[] keys = response.split(" ");
                writeLock.lock();
                List<String> t = reducer.getOrDefault(keys[0], new LinkedList<>());
                t.add(keys[1]);
                reducer.put(keys[0], t);
                writeLock.unlock();
            }while(true);

        }catch(IOException e){
            System.out.println(e.getMessage()+" err in map handle");
            e.printStackTrace();
        }catch(NullPointerException e) {
            System.out.println("Error with connection ");
            e.printStackTrace();
        }finally{
            boolean fl = false;
            while(!fl) {
                try {
                    br.close();
                    out.close();
                    connection.close();
                    fl = true;
                } catch (IOException e) {
                    System.out.println("Re-trying connection close....");
                    e.printStackTrace();
                }
            }
        }
    }
}