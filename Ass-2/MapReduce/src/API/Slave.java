package API;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;


public abstract class Slave{
    protected String slaveName;
    protected Properties properties;
    protected String inputPath;
    protected long masterId;
    protected int maps;
    protected int reds;
    public Slave(String slaveName,long masterId) throws IOException {
        System.out.println(slaveName);
        this.slaveName = slaveName;
        this.properties = Configure.getProperties();
        Socket s = new Socket(properties.getProperty("kv.ip"),Integer.parseInt(properties.getProperty("kv.port")));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter out = new PrintWriter(s.getOutputStream());
        String response = "Error";
        String[] keys = new String[]{slaveName,masterId+"-maps",masterId+"-reds"};
        int i=0;
        for(String key:keys) {
            while (response.equalsIgnoreCase("Error")) {
                out.println("GET " + key);
                response = in.readLine();
            }
            keys[i] = response;
            i++;
        }
        inputPath = keys[0];
        maps = Integer.parseInt(keys[1]);
        reds = Integer.parseInt(keys[2]);
    }
    protected abstract void run();
}
