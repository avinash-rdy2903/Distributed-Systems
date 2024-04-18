
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;

public abstract class Partition {
    private int multicastPort;
    private InetAddress multicastGroup;
    private Socket kvStrore;
    private BufferedReader in;
    private PrintWriter out;
    protected DatagramSocket clientServer;
    protected MulticastSocket multicastReceiver;
    protected DatagramSocket multicastSender;
    protected int id;
    protected PriorityQueue<Message> pq;
    protected int totalPartitions;
    public Partition(int id,String kvHost,int kvPort,String multicastGroup,int multicastPort,int totalPartitons) throws IOException {
        this.id = id;
        this.multicastGroup = InetAddress.getByName(multicastGroup);
        this.multicastPort = multicastPort;
        this.multicastSender = new DatagramSocket();
        this.multicastReceiver = new MulticastSocket(multicastPort);
        // this.multicastReceiver.setReuseAddress(true);
        this.multicastReceiver.joinGroup(this.multicastGroup);
        clientServer = new DatagramSocket();
        System.out.println("server-"+(id+1)+" running at Port: "+clientServer.getLocalPort());
        this.kvStrore = new Socket(kvHost,kvPort);
        in = new BufferedReader(new InputStreamReader(kvStrore.getInputStream()));
        out = new PrintWriter(kvStrore.getOutputStream(), true);
        this.pq = new PriorityQueue<>();
        this.totalPartitions = totalPartitons;
    }
    protected boolean setKv(String key,String value) throws IOException {
        out.println("SET "+key);
        out.println(value);
        return in.readLine().equalsIgnoreCase("STORED");
    }
    protected String getKv(String key) throws IOException {
        out.println("GET "+key);
        String result = in.readLine();
        return in.readLine();
    }
    protected void broadcast(String message,long timestamp,String uuid) throws IOException {
        String broadcastMessage = "BRD "+uuid+" "+timestamp+" "+id+" "+message;
        DatagramPacket packet = new DatagramPacket(broadcastMessage.getBytes(StandardCharsets.UTF_8),broadcastMessage.length(),this.multicastGroup,this.multicastPort);
        this.multicastSender.send(packet);
    }
    protected void closeKv() throws IOException {
        this.kvStrore.close();
    }
    abstract void close() throws IOException;
}
