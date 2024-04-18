package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLOutput;
import java.util.Scanner;
import java.net.Socket;
import java.util.Arrays;
public class ClientTemplate {
    private static Scanner scanner = new Scanner(System.in);
    private static int search(int[] sequencerPorts,int port){
        for(int i=0;i<sequencerPorts.length;i++){
            if(sequencerPorts[i]==port){
                return i;
            }
        }
        return 0;
    }
    public static void main(String[] args) throws IOException {
        int[] sequencerPorts = new int[args.length];
        for(int i=0;i< args.length;i++){
            sequencerPorts[i] = Integer.parseInt(args[i]);
        }
        Socket[] sockets = new Socket[sequencerPorts.length];
        BufferedReader[] in = new BufferedReader[sequencerPorts.length];
        PrintWriter[] out = new PrintWriter[sequencerPorts.length];
        for(int i=0;i< sequencerPorts.length;i++){
            sockets[i] = new Socket("localhost",sequencerPorts[i]);
            sockets[i].setReuseAddress(true);
            in[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
            out[i] = new PrintWriter(sockets[i].getOutputStream(),true);
        }
        int port,ind;
        while(true) {
            System.out.println("Sequencer ports: " + Arrays.toString(sequencerPorts));
            System.out.println("Enter the sequencer port to request an ID(0 to exit)");
            port = scanner.nextInt();
            if(port==0){
                break;
            }
            ind = search(sequencerPorts,port);
            out[ind].println("get-id");
            System.out.println("ID is - " + in[ind].readLine());
            System.out.println();
        }
    }
}
