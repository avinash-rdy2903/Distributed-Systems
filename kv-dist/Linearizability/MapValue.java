package Linearizability;

import java.net.InetAddress;

public class MapValue {
    public int acks;
    public InetAddress addr;
    public int port;

    public MapValue(int acks, InetAddress addr, int port) {
        this.acks = acks;
        this.addr = addr;
        this.port = port;
    }
}
