import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        String x;
        int port,timeout;
        while(true) {
            Scanner in = new Scanner(System.in);
            System.out.println("Enter get/set cmd: ");
            x = in.nextLine();
            System.out.println("Enter partition port: ");
            port = in.nextInt();
            System.out.println("Enter time delay(ms)(0 to skip): ");
            timeout = in.nextInt();
            if(timeout!=0){
                Thread.sleep(timeout);
            }
            DatagramSocket soc = new DatagramSocket();
            DatagramPacket p = new DatagramPacket(x.getBytes(StandardCharsets.UTF_8), x.length(), InetAddress.getLocalHost(), port);
            soc.send(p);
            byte[] recvBuff = new byte[256];
            p = new DatagramPacket(recvBuff, recvBuff.length);
            soc.receive(p);
            x = new String(p.getData(),0,p.getLength());
            System.out.println(x);
        }
    }
}
