

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.net.ServerSocket;


public abstract class Slave{
    protected String slaveName;
    protected Properties properties;
    protected String inputPath;
    protected long masterId;
    protected int maps;
    protected int reds;
    protected String masterIp;
    protected int masterPort;
    protected ServerSocket socket;
    public Slave(String slaveName,long masterId,boolean map) throws IOException {
        System.out.println(slaveName);
        this.slaveName = slaveName;
        this.properties = Configure.getProperties();
        this.masterId = masterId;
        Socket s = new Socket(properties.getProperty("kv.ip"),Integer.parseInt(properties.getProperty("kv.port")));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter out = new PrintWriter(s.getOutputStream(),true);
        String response = "Error";
        socket = null;
        String[] keys = new String[]{slaveName,masterId+"-maps",masterId+"-reds",masterId+"-ip",masterId+"-port"};
        int i=0;
        for(String key:keys) {
            while (response.equalsIgnoreCase("Error")) {
                
                System.out.println(key);
                out.println("GET " + key);
                System.out.println(in.readLine()+" read");
                response = in.readLine();
                System.out.println(response+" res");
            }
            keys[i] = response;
            // System.out.println(keys[i]);
            // System.out.println(in.readLine());
            response = "Error";
            i++;
        }
        inputPath = keys[0];
        maps = Integer.parseInt(keys[1]);
        reds = Integer.parseInt(keys[2]);
        masterIp = keys[3];
        masterPort = Integer.parseInt(keys[4]);
        if(!map){
            try{
                socket = new ServerSocket(0);
                    
                keys = new String[]{"port","ip"};
                int ind=0;
                for(String key:new String[]{Integer.toString(this.socket.getLocalPort()),this.socket.getInetAddress().getCanonicalHostName()}){
                    do {
                        String cmd = "SET " + slaveName + "-" + keys[ind] + " " + key.getBytes(StandardCharsets.UTF_8).length;
                        System.out.println(cmd);
                        out.println(cmd);
                        System.out.println(key);
                        out.println(key);

                    } while (!in.readLine().equals("STORED"));
                    ind++;
                }
                
            }catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
        s.close();
    }
    protected abstract void run();
}
