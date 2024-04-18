import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientHandle implements Runnable{
    private static final ReadWriteLock rdLock =  new ReentrantReadWriteLock();
    private static final Lock writeLock = rdLock.writeLock(),
    readLock = rdLock.readLock();
    private Socket connection;
    private LookUpCache<String,String> cache;
    private String info;
    public ClientHandle(Socket s, LookUpCache<String,String> map){
        this.connection = s;
        this.cache = map;
        info = s.getInetAddress().getCanonicalHostName()+"::"+s.getPort();
    }
    public void run(){
        System.out.println("in run client handle");

        InputStreamReader in = null;
        BufferedReader br = null;
        PrintWriter out = null;
        try {
            in = new InputStreamReader(connection.getInputStream());
            br = new BufferedReader(in);
            out = new PrintWriter(connection.getOutputStream(),true);

            String[] queryList = br.readLine().split(" ");
            System.out.println(info+" "+ Arrays.toString(queryList));

            do {
                System.out.println("in loop");
                if (queryList[0].equals("GET") || queryList[0].equals("get")) {
                    readLock.lock();
                    try {
                        String res = cache.get(queryList[1]);
                        if (res == null) {
                            res = "Error";
                        }
                        out.println("VALUE "+queryList[1]+" 0 "+res.getBytes(StandardCharsets.UTF_8).length);
                        out.println(res);
                        out.println("END");
                    }finally {
                        readLock.unlock();
                    }

                } else if (queryList[0].equals("SET") || queryList[0].equals("set")) {
                    writeLock.lock();
                    try {
                        String data = br.readLine();
                        System.out.println(data);
                        try {
                            cache.put(queryList[1], data);
                            data = "STORED";
                            cache.flushToJSON();
                        } catch (Exception e) {
                            e.printStackTrace();
                            data = "NOT-STORED";
                        }
                        out.println(data);
                    }finally {
                        writeLock.unlock();
                    }
                }else{
                    out.println("Invalid Query "+String.join(" ",queryList));
                }
                String txt = br.readLine();
                if(txt==null){
                    queryList = new String[]{};
                }else{
                    queryList = txt.split(" ");
                }
            }while(queryList.length>0 && !queryList[0].contentEquals("!q"));

        }catch(IOException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }catch(NullPointerException e) {
            System.out.println("Error with connection "+info);
            e.printStackTrace();
        }finally{
            boolean fl = false;
            while(!fl) {
                try {
                    br.close();
                    out.close();
                    connection.close();
                    cache.flushToJSON();
                    fl = true;
                } catch (IOException e) {
                    System.out.println("Re-trying connection close....");
                    e.printStackTrace();
                }
            }
            System.out.println("Closed : "+info);
        }
    }
}
