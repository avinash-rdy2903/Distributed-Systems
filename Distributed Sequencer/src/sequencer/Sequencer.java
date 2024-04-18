package sequencer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Sequencer implements Runnable{
    private final boolean isInitial;
    private int sequencerId;
    private ServerSocket receiver;
    private Socket sender;
    private PrintWriter out;
    private BufferedReader in;
    private SocketAddress leftAddr;
    private String token = "1";
    private int id = 1;
    public Sequencer(int id,ServerSocket receiver,SocketAddress leftAddr,boolean isInitial) throws IOException {
        this.sequencerId = id;
        this.receiver = receiver;
        this.sender = new Socket();
        this.leftAddr = leftAddr;
        this.isInitial = isInitial;
    }

    private void connectFromRight() throws IOException {
        this.in = new BufferedReader(new InputStreamReader(receiver.accept().getInputStream()));
//        System.out.println("Connection from right accepted in Sequencer-"+this.sequencerId);
    }

    private void connectToLeft() throws IOException {
        sender.connect(this.leftAddr);
//        System.out.println("Connection to left is sent in Sequencer-"+this.sequencerId);
        this.out = new PrintWriter(sender.getOutputStream(),true);
    }

    @Override
    public void run() {
        if (!isInitial) {
            try {
                this.connectFromRight();
                this.connectToLeft();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            try {
                this.connectToLeft();
                Thread.sleep(1000);
                this.connectFromRight();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            out.println("token");
            out.println(this.id);
        }
        ReentrantLock lock = new ReentrantLock(true);
        Deque<Request> requestQueue = new LinkedList<>();
        // lets create a new thread that listens for a new connection;

        new Thread(() -> {
            try {
                while(true) {
                    (new Thread(new ClientHandler(receiver.accept(), lock, requestQueue))).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        while (true) {
            try {
                if (in.readLine().toUpperCase().equals("TOKEN")) {
                    this.id = Integer.parseInt(in.readLine());
//                        Thread.sleep(50);
                        lock.lock();
//                        System.out.println(this.sequencerId+" has token");
                    do {
                        Request req = requestQueue.poll();
                        if (req == null) {
                            break;
                        }
                        req.sendResponse(this.id);
                        this.id += 1;
                    }while(true);
                    if(lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
                out.println("token");
                out.println(this.id);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
