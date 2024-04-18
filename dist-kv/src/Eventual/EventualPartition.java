package Eventual;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class EventualPartition extends Partition implements Runnable{
    private ReentrantLock lock = new ReentrantLock(true);
    public EventualPartition(int id, String kvHost, int kvPort, String multicastGroup, int multicastPort, int totalPartitons) throws IOException {
        super(id, kvHost, kvPort, multicastGroup, multicastPort, totalPartitons);
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    byte[] multicastBuf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(multicastBuf, multicastBuf.length);
                    try {
                        multicastReceiver.receive(packet);
                        String[] received = new String(packet.getData(), 0, packet.getLength()).split(" ");
                        System.out.println(id + " " + Arrays.toString(received));
                        if (received[0].equalsIgnoreCase("BRD")) {
                            if (received.length > 6) {
                                setKv(received[5],received[6]);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();
    }

    @Override
    void close() throws IOException {
        this.multicastReceiver.close();
        this.multicastReceiver.close();
        this.clientServer.close();
        this.closeKv();
    }

    @Override
    public void run() {
        while(true) {
            byte[] clientBuf = new byte[256];
            String recv = null;
            long timeStamp;
            DatagramPacket packet = new DatagramPacket(clientBuf, clientBuf.length);
            try {
                clientServer.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            recv = new String(packet.getData(), 0, packet.getLength()).trim();

            try {
                String[] received = recv.split(" ");
                System.out.println(Arrays.toString(received));
                if(received[0].equalsIgnoreCase("GET")){
                    byte[] responseBuffer = getKv(received[1]).getBytes(StandardCharsets.UTF_8);
                    clientServer.send(new DatagramPacket(responseBuffer,responseBuffer.length,packet.getAddress(),packet.getPort()));

                }else if(received[0].equalsIgnoreCase("SET")){
                    boolean setFlag = setKv(received[1],received[2]);
                    if(setFlag){
                        System.out.println("Stored");
                        clientServer.send(new DatagramPacket("Stored".getBytes(StandardCharsets.UTF_8), 6, packet.getAddress(), packet.getPort()));
                    }
                    timeStamp = System.currentTimeMillis();
                    String uuid = UUID.randomUUID().toString();
                    broadcast(recv, timeStamp, uuid);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

