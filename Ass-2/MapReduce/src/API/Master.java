package API;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Master {

    private static final HashMap<Long, Pair> clusterMap;
    static {
        clusterMap = new HashMap<>();
    }
    public Master(){

    }
    public static long createCluster(int maps,int reds) throws InterruptedException {
        long currMills = 0;
        synchronized (clusterMap) {
            currMills = System.nanoTime();
        }
        clusterMap.put(currMills, new Pair(maps,reds));
        return currMills;
    }
    public static void runCluster(long id,List<String> inputPaths,String mapFn,String redFn,String output)throws IOException{
        new Thread(new ExecuteCluster(id,inputPaths,mapFn,redFn,output,clusterMap.get(id))).start();
    }
    public static void runCluster(long id,String input,String mapFn,String redFn,String output,long max_words) throws IOException {
        new Thread(new ExecuteCluster(id,input,mapFn,redFn,output,max_words,clusterMap.get(id))).start();
        return ;
    }
    // public static boolean shut
}
