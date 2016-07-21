package com.inatsuz.pongserver;

import java.net.InetAddress;

public class Clients {

    protected InetAddress ip;
    protected int PORT;
    private int x, y;
    private final int HEIGHT = 200, WIDTH = 50;

    public Clients(InetAddress ip, int port) {
        this.ip = ip;
        this.PORT = port;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public int getHeight(){
        return HEIGHT;
    }
    
    public int getWidth(){
        return WIDTH;
    }

}