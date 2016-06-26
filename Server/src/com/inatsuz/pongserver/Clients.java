package com.inatsuz.pongserver;

import java.net.InetAddress;

public class Clients {
    
    protected InetAddress ip;
    protected int PORT;
    
    public Clients(InetAddress ip, int port){
        this.ip = ip;
        this.PORT = port;
    }
    
}