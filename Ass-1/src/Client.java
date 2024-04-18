import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private final static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        Socket clientSocket = null;
        InputStreamReader in = null;
        BufferedReader br = null;
        PrintWriter out = null;
        try {
            clientSocket = new Socket(args[0], Integer.parseInt(args[1]));
            in = new InputStreamReader(clientSocket.getInputStream());
            br = new BufferedReader(in);
            out = new PrintWriter(clientSocket.getOutputStream(),true);
            System.out.println("Enter the Queries:\nEscape character is !q");
            String test = scanner.nextLine();
            do {
                out.println(test);
                if (test.charAt(0) == 'S') {
                    test = scanner.nextLine();
                    out.println(test);
                    System.out.println(br.readLine());
                    test = scanner.nextLine();
                    continue;
                }
                String[] result = br.readLine().split(" ");
                System.out.println(result[0]+" "+result[1]+" "+result[3]);
                System.out.println(br.readLine());
                System.out.println(br.readLine());
                test = scanner.nextLine();
            }while(!test.equals("!q"));

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            boolean fl = false;
            while(!fl) {
                try {
                    clientSocket.close();
                    fl = true;
                } catch (IOException e) {
                    System.out.println("Re-trying connection close....");
                    e.printStackTrace();
                }
            }
            System.out.println("Closed client");
        }
    }
}
