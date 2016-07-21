package com.inatsuz.pongclient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Ball {

    private int x = 700 / 2 - 10, y = 700 / 2 - 10;
    private final int DIAMETER = 20;

    private PongClient pongClient;

    public Ball(PongClient pongClient) {
        this.pongClient = pongClient;
    }

    protected void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        g2.fillOval(x, y, DIAMETER, DIAMETER);
    }
    
    public void setCoords(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    public void setX(int x){
        this.x = x;
    }
    
    public void setY(int y){
        this.y = y;
    }

}