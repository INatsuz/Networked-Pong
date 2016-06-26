package com.inatsuz.pongclient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Ball {

    private int x = 0, y = 0;
    private final int DIAMETER = 20;
    private final int SPEED = 12;
    private int speedX, speedY;
    
    private PongClient pongClient;

    public Ball(int x, int y, PongClient pongClient) {
        this.x = x;
        this.y = y;
    }
    
    protected void update(){
        move();
    }
    
    protected void render(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        g2.fillOval(x, y, DIAMETER, DIAMETER);
    }
    
    private void move(){
        if(x + speedX >= 0 && x + DIAMETER + speedX <= 700){
            x += speedX;
        }else if(x + speedX < 0){
            x = 0;
        }else if(x + DIAMETER + speedX > 700){
            x = 700 - DIAMETER;
        }
        if(y + speedY >= 0 && y + DIAMETER + speedY <= 700){
            y += speedY;
        }else if(y + speedY < 0){
            y = 0;
        }else if(y + DIAMETER + speedY > 700){
            y = 700 - DIAMETER;
        }
    }
    
    public void setSpeeds(int speedX, int speedY){
        this.speedX = speedX;
        this.speedY = speedY;
    }
    
    public void setSpeedX(int speedX){
        this.speedX = speedX;
    }
    
    public void setSpeedY(int speedY){
        this.speedY = speedY;
    }

}

//Fazer As Colisoes Com Trigonometria e o Movimento Da Bola