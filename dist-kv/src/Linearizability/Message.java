package Linearizability;

import java.net.InetAddress;
import java.util.Comparator;

public class Message implements Comparable<Message> {
    public long timestamp;
    public int processId;
    public String id;
    public String key;

    @Override
    public int compareTo(Message o) {
        if(timestamp< o.timestamp){
            return -1;
        }
        if(timestamp>o.timestamp){
            return 1;
        }
        int order=1;
        if(timestamp==o.timestamp){
            if(processId<o.processId){
                order= -1;
            }
        }
        return order;
    }

    public String value;
    public InetAddress senderAddr;
    public int senderPort;
    public boolean sentAck;
    public boolean isSet;

    public Message(String id,long timestamp, int processId,String key,String value,InetAddress senderAddr,int senderPort,boolean isSet) {
        this.timestamp = timestamp;
        this.processId = processId;
        this.id = id;
        this.key = key;
        this.value = value;
        this.senderAddr = senderAddr;
        this.senderPort = senderPort;
        this.sentAck = false;
        this.isSet = isSet;
    }
}
