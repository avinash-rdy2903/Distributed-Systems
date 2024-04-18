
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class LinearizePartition extends Partition implements Runnable{
    HashMap<String, MapValue> acks = new HashMap<>();
    private ReentrantLock lock = new ReentrantLock(true);
    private int mode;
    public LinearizePartition(int id, String kvHost, int kvPort, String multicastGroup, int multicastPort, int totalPartitons) throws IOException {
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
                        if (received[0].equalsIgnoreCase("BRD")) {
                            Message m;
                            if (received.length > 6) {
                                m = new Message(received[1], Long.parseLong(received[2]), Integer.parseInt(received[3]), received[5], received[6], packet.getAddress(), packet.getPort(), true);
                            } else {
                                m = new Message(received[1], Long.parseLong(received[2]), Integer.parseInt(received[3]), received[5], "", packet.getAddress(), packet.getPort(), false);
                            }
                            lock.lock();
                            pq.add(m);
                            lock.unlock();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();
        (new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket packet;
                while(true){
                    byte[] multicastBuf;
                    lock.lock();
                    if(!pq.isEmpty() && !pq.peek().sentAck){
                        multicastBuf = ("ACK "+pq.peek().id).getBytes(StandardCharsets.UTF_8);
                        packet = new DatagramPacket(multicastBuf,multicastBuf.length,pq.peek().senderAddr, pq.peek().senderPort);
                        try {
                            multicastSender.send(packet);
                            if(pq.peek().processId==id){
                                pq.peek().sentAck = true;
                            }else {
                                pq.poll();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(!pq.isEmpty() && acks.containsKey(pq.peek().id)){
                        if(acks.get(pq.peek().id).acks==totalPartitons){
                            Message temp = pq.poll();
                            String value;
                            MapValue v = acks.get(temp.id);
                            if(temp.isSet){
                                try {
                                    setKv(temp.key,temp.value);
                                    packet = new DatagramPacket("1".getBytes(StandardCharsets.UTF_8), 1,v.addr,v.port);
                                    clientServer.send(packet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                try {
                                    value = getKv(temp.key);
                                    packet = new DatagramPacket(value.getBytes(StandardCharsets.UTF_8), value.length(),v.addr,v.port);
                                    clientServer.send(packet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            acks.remove(temp.id, v);
                        }
                    }
                    lock.unlock();
                }
            }
        })).start();
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    byte[] recv = new byte[256];
                    DatagramPacket packet = new DatagramPacket(recv, recv.length);
                    try {
                        multicastSender.receive(packet);
                        String[] received = new String(packet.getData(), 0, packet.getLength()).split(" ");
                        lock.lock();
                        MapValue v = acks.get(received[1]);
                        v.acks+=1;
                        lock.unlock();
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
                // System.out.println("recv");
            } catch (IOException e) {
                e.printStackTrace();
            }
            recv = new String(packet.getData(), 0, packet.getLength());
            timeStamp = System.currentTimeMillis();
            String uuid = UUID.randomUUID().toString();
            lock.lock();
            acks.put(uuid, new MapValue(0, packet.getAddress(), packet.getPort()));
            lock.unlock();
            try {
                broadcast(recv, timeStamp, uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
