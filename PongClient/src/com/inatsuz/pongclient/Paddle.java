package com.inatsuz.pongclient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Paddle {

    private final int WIDTH = 50, HEIGHT = 200;
    private int x, y;
    private int SPEED = 20;
    private int score = 0;

    private PongClient pongClient;

    public Paddle(int x, int y, PongClient pongClient) {
        this.x = x;
        this.y = y;
        this.pongClient = pongClient;
    }

    protected void move(boolean up) {
        if (up) {
            if (y - SPEED >= 0) {
                y -= SPEED;
            } else {
                y = 0;
            }
        } else {
            if (y + SPEED <= pongClient.HEIGHT - 200) {
                y += SPEED;
            } else {
                y = pongClient.HEIGHT - 200;
            }
        }
    }

    protected void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, WIDTH, HEIGHT);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int getWidth() {
        return WIDTH;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public boolean checkWin() {
        return score >= 5;
    }
    
    public void setSpeed(int speed){
        this.SPEED = speed;
    }

}
