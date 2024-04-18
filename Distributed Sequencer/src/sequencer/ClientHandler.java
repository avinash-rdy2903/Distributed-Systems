package sequencer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ClientHandler implements Runnable{
    private Socket connection;
    private BufferedReader in;
    private PrintWriter out;
    private Deque<Request> requestQueue;
    private final ReentrantLock lock;

    public ClientHandler(Socket connection,ReentrantLock lock,Deque<Request> requestQueue) throws IOException {
        this.connection = connection;
        connection.setSoTimeout(1000);
        this.requestQueue = requestQueue;
        this.lock = lock;
    }
    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new PrintWriter(connection.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true){
            try{
                if (in.readLine().toUpperCase().equals("GET-ID")) {
                    System.out.println("read from client");
                    lock.lock();
                    requestQueue.add(new Request(out));
                }
            } catch (SocketTimeoutException e){
                continue;
            }
            catch (IOException e){
                e.printStackTrace();
                break;
            } catch(Exception e){
                continue;
            } finally {
                if(lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
