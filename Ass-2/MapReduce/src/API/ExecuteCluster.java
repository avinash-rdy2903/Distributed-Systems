package API;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExecuteCluster implements Runnable{
    private final long clusterId;
    private final String input;
    private final List<String> inputs;
    private final String output;
    private final long max_words;
    private final int maps;
    private final int reds;
    private final String mapPath;
    private final String redPath;
    private final Properties properties;
    private final HashMap<String,Process> processMap;
    private final ServerSocket socket;
    private final ArrayList<Process> processes;

    public ExecuteCluster(long clusterId, String input, String mapPath, String redPath, String output, long max_words, Pair pair) throws IOException {
        this.clusterId = clusterId;
        this.input = input;
        this.inputs = null;
        this.output = output;
        this.max_words = max_words;
        this.maps = pair.maps;
        this.reds = pair.reds;
        this.mapPath = mapPath;
        this.redPath = redPath;
        properties = Configure.getProperties();
        this.processMap = new HashMap<>();
        socket = new ServerSocket(0);
        processes = new ArrayList<>(maps+reds);
    }
    public ExecuteCluster(long clusterId, List<String> inputs, String mapPath, String redPath, String output, Pair pair) throws IOException {
        this.clusterId = clusterId;
        this.input = "input";
        this.inputs = inputs;
        this.output = output;
        this.max_words = -1;
        this.maps = pair.maps;
        this.reds = pair.reds;
        this.mapPath = mapPath;
        this.redPath = redPath;
        properties = Configure.getProperties();
        this.processMap = new HashMap<>();
        socket = new ServerSocket(0);
        processes = new ArrayList<>(maps+reds);
    }


    private List<String> splitFile() throws IOException {
        List<String> res = new ArrayList<>();
        String[] words;
        String line;
        File f = new File(this.input);
        System.out.println(f.getAbsolutePath());
        int split = 1;
        System.out.println("creating new file");
        File out = new File(this.clusterId+"-map-"+split+".txt");
        out.createNewFile();
        FileWriter writer = new FileWriter(out);
        long count = 0;
        System.out.println("reading lines");
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.getAbsolutePath()),"UTF-8"))){
            while((line=br.readLine())!=null){
                words = line.replaceAll("[^a-zA-Z]", " ").toLowerCase().split("\\s+");
                if(words.length==0 || words[0].equals("")){
                    continue;
                }
                for(String w:words){
                    writer.write(w+"\n");
                    count++;
                }
                if(count>=this.max_words && split<this.maps){
                    split++;
                    System.out.println(out.getAbsolutePath());
                    res.add(out.getAbsolutePath());
                    out = new File(this.clusterId+"-map-"+split+".txt");
                    out.createNewFile();
                    writer = new FileWriter(out);
                    count=0;
                }
            }
            res.add(out.getAbsolutePath());
        }catch(IOException e){
            System.out.println(e.getMessage()+"here");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return res;
    }
    private boolean pushCredsToKV(List<String> paths,List<String> names){
        System.out.println("Pushing splitted file paths to KV server");
        try(Socket socket = new Socket(this.properties.getProperty("kv.ip"),Integer.parseInt(this.properties.getProperty("kv.port")))){

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            int length = paths.size();
            for(int i=0;i<length;i++){
                do{
                    String cmd = "SET "+names.get(i)+" "+paths.get(i).getBytes(StandardCharsets.UTF_8).length;
                    out.println(cmd);
                    out.println(paths.get(i));
                    System.out.println(cmd);
                }while(!in.readLine().equals("STORED"));
            }
            String[] keys = new String[]{"maps","reds","port","ip"};
            int ind=0;
            for(String i:new String[]{Long.toString(maps),Long.toString(reds), Integer.toString(this.socket.getLocalPort()),this.socket.getInetAddress().getCanonicalHostName()}){
                do {
                    String cmd = "SET " + this.clusterId + "-" + keys[ind] + " " + i.getBytes(StandardCharsets.UTF_8).length;
                    out.println(cmd);
                    out.println(i);
                } while (!in.readLine().equals("STORED"));
                ind++;
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    private boolean startSlave(String slaveName,String file){
        try{
            int sepInd = this.mapPath.lastIndexOf(properties.getProperty("path.separator"));
            String classPath = file.substring(0,sepInd);
            String className = file.substring(sepInd+1,file.lastIndexOf(".java"));
            Runtime r = Runtime.getRuntime();
            String  cmd2 = "javac "+classPath+properties.getProperty("path.separator")+"*.java",
                    cmd3 = "java -cp "+classPath+" "+className+" "+slaveName+" "+clusterId;

//            Process p0 = r.exec(cmd1);
//            p0.waitFor();
            System.out.println(cmd2);
            Process p = r.exec(cmd2);
            p.waitFor();
            System.out.println(cmd3);
            Process p2 = r.exec(cmd3);

            BufferedReader in = new BufferedReader(new InputStreamReader(p2.getInputStream()));
//            System.out.println(new InputStreamReader(p2.getErrorStream()).read());
            if(!in.readLine().contentEquals(slaveName)){
                return false;
            }
            processes.add(p2);
            System.out.println("slave created");
            processMap.put(slaveName, p2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
    @Override
    public void run() {
        try {
            List<String> mapperInputPaths = inputs;
            if(mapperInputPaths==null){
                mapperInputPaths = splitFile();
            }
            System.out.println(mapperInputPaths.toString());
            List<String> slaveNames = new ArrayList<>(this.maps);
            for(int i=1;i<=this.maps;i++){
                slaveNames.add(this.clusterId+"-map-"+i);
            }
            while(!pushCredsToKV(mapperInputPaths,slaveNames));
            ArrayList<String> dummy = new ArrayList<>(reds);
            slaveNames.clear();
            for(int i=1;i<=reds;i++){
                slaveNames.add(this.clusterId+"-red-"+i);
                dummy.add(" ");
            }
            while(!pushCredsToKV(dummy,slaveNames));
            System.out.println("Starting Mappers");
            int l = Math.min(this.maps,mapperInputPaths.size());
            for(int i=1;i<=reds;i++){
                startSlave(clusterId+"-red-"+i,redPath);
            }
            try{
                Thread.sleep(3000);
            }catch (InterruptedException e){
                System.out.println(e.getMessage());
            }
            for(int i=1;i<=l;i++){
                startSlave(clusterId+"-map-"+i,mapPath);
            }

//            BufferedReader in = new BufferedReader(new InputStreamReader(processes.get(2).getInputStream()));
//            BufferedReader in1= new BufferedReader(new InputStreamReader(processes.get(3).getInputStream()));
//            BufferedReader in2 = new BufferedReader(new InputStreamReader(processes.get(4).getInputStream()));
            BufferedReader in1 = new BufferedReader(new InputStreamReader(processes.get(0).getInputStream()));
            BufferedReader in2 = new BufferedReader(new InputStreamReader(processes.get(1).getInputStream()));
            BufferedReader in3 = new BufferedReader(new InputStreamReader(processes.get(2).getInputStream()));

            String res = "";
            while((res=in3.readLine())!=null) {
                System.out.println(res);
            }
//            }while((res=in1.readLine())!=null){
//                System.out.println(res);
//            }while((res=in2.readLine())!=null){
//                System.out.println(res);
//            }
            ArrayList<String> list = new ArrayList<>();

            List<Future<?>> futures = new ArrayList<>();
            ExecutorService pool = Executors.newFixedThreadPool(reds);
            for(int i=0;i<reds;i++){
                futures.add(pool.submit(new ReducerHandle(this.socket.accept(), list)));
            }
            for (Future<?> f : futures) {
                f.get();
            }
            File t = new File(output);
            if(t.delete()){
                t.createNewFile();
            }
            FileWriter f = new FileWriter((output));
            for(String i:list){
//                System.out.println(i);
                f.write(i+"\n");
            }
            f.flush();
            if(inputs==null) {
                for (String path : mapperInputPaths) {
                    File t = new File(path);
                    if (t.exists()) {
                        t.delete();
                    }
                }
            }
            for(Process p:processes){
                p.destroyForcibly();
            }
            pool.shutdown();
            System.out.println("CLuster finished");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return;
    }
}
