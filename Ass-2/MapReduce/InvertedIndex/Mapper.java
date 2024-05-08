
import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.Socket;

public class Mapper extends Slave {
    private HashMap<String,String> table;

    public Mapper(String slaveName, long masterId) throws IOException {
        super(slaveName, masterId,true);
        table = new HashMap<>();

    }
    public void run(){
        try(BufferedReader in = new BufferedReader(new FileReader(this.inputPath))){
            String words = "";

            while((words=in.readLine())!=null){
                words = words.strip();
                if(words.equals("")){
                    continue;
                }
                for(String word:words.split(" ")) {
//                    System.out.println(word+this.inputPath);
                    table.put(word, this.inputPath);
                }
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        System.out.println(table.size());       
        HashMap<Integer,Socket> redMap = new HashMap<>();
        try(Socket kvSocket = new Socket(this.properties.getProperty("kv.ip"),Integer.parseInt(this.properties.getProperty("kv.port")))){

            BufferedReader in = new BufferedReader(new InputStreamReader(kvSocket.getInputStream()));
            PrintWriter out = new PrintWriter(kvSocket.getOutputStream(),true);
            for(int i=1;i<=reds;i++){
                String[] keys = new String[]{masterId+"-red-"+i+"-ip",masterId+"-red-"+i+"-port"};
                int ind=0;
                String response = "error";
                for(String key:keys) {
                    while (response.equalsIgnoreCase("Error")) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(key);
                        out.println("GET " + key);
                        System.out.println(in.readLine()+" read");
                        response = in.readLine();
                        System.out.println(response+" res");
                    }
                    keys[ind] = response;
                    System.out.println(keys[ind]);
                    // System.out.println(in.readLine());
                    response = "Error";
                    ind++;
                }
                boolean fl = true;
                Socket socket = null;
                while(fl){
                    try{
                        socket = new Socket(keys[0],Integer.parseInt(keys[1]));
                        fl=false;
                    }catch(IOException e){
                        System.out.println(e.getMessage());
                    }
                }
                redMap.put(i,socket);
            }
            
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

        HashMap<Integer,PrintWriter> outMap = new HashMap<>();
        for(Map.Entry<Integer,Socket> entry:redMap.entrySet()){
            try{
            outMap.put(entry.getKey(),new PrintWriter(entry.getValue().getOutputStream(),true));
            }catch (IOException e){
                System.out.println(e.getMessage());
            }
        }
        System.out.println("before send"+table.size());
        for(Map.Entry<String,String> it: table.entrySet()){
            if(it.getKey().matches("\\s+") || it.getKey().length()==0)continue;
            int ind = ((int)(Character.toLowerCase(it.getKey().charAt(0))-'a')%reds)+1;

            if(outMap.get(ind)==null)continue;
            outMap.get(ind).println(it.getKey()+" "+it.getValue());
        }
        for(Map.Entry<Integer,PrintWriter> entry :outMap.entrySet()) {
            entry.getValue().println("!q");
            
        }
        System.out.println("mapper finished");
        for(Map.Entry<Integer, Socket> entry : redMap.entrySet()){
            try {
                entry.getValue().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Mapper map = new Mapper(args[0],Long.parseLong(args[1]));
            map.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
