package API;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReducerHandle implements Runnable{
    private static final ReadWriteLock rdLock =  new ReentrantReadWriteLock();
    private static final Lock writeLock = rdLock.writeLock(),
            readLock = rdLock.readLock();
    private Socket connection;
    private static ArrayList<String> list;
    public ReducerHandle(Socket s, ArrayList<String> list) throws IOException{
        this.connection = s;
        this.list = list;
    }
    public void run(){
        System.out.println("reducer response started");

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
                writeLock.lock();
                list.add(response);
//                System.out.println("writing "+response);
//                out.println("respose recv");
                writeLock.unlock();
            }while(true);

        }catch(IOException e){
            System.out.println(e.getMessage());
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
