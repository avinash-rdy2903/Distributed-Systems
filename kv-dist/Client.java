import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        String x;
        int port;
        while(true) {
            Scanner in = new Scanner(System.in);
            x = in.nextLine();
            port = in.nextInt();
            DatagramSocket soc = new DatagramSocket();
            System.out.println("send server");
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
