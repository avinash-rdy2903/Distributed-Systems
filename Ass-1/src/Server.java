
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;

public class Server{
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
            System.exit(0);
        }
    }
    public void runServer() {
        try {
            while (true) {
                new Thread(new ClientHandle(this.serverSocket.accept(), this.cache)).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }finally{
            try{
                serverSocket.close();
            }catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(Integer.parseInt(args[0]),args[1]);
        server.runServer();
    }
}
