package KVstore;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
public class Server {
    private ServerSocket serverSocket;
    private LookUpCache<String,String> cache;
    public Server(int port,String fileDir){
        try {
            this.serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            System.out.println("Could not open Server Socket :( ");
            e.printStackTrace();
        }
        try{
             cache = new LookUpCache<>(fileDir);
        }catch (IOException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    public void runServer() {
        try {
            while (true) {
                System.out.println("Waiting for connection at: "+serverSocket.getLocalSocketAddress());
                new Thread(new ClientHandle(this.serverSocket.accept(), this.cache)).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8080,"C:\\Users\\Avinash\\Desktop\\Assingments\\DS\\Ass-1\\cache.json");
        server.runServer();
    }
}
