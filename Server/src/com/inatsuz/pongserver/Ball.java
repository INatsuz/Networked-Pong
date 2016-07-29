package com.inatsuz.pongserver;

public class Ball {

    private int x, y;
    private int speedX, speedY;
    private int hits = 0;
    private final int SPEED = 15;
    private final int DIAMETER = 20;

    protected boolean ballMoving = false;

    private Clients[] clients;
    private PongServer pongServer;

    public Ball(int x, int y, Clients[] clients, PongServer pongServer) {
        this.x = x;
        this.y = y;
        this.clients = clients;
        this.pongServer = pongServer;
    }

    private void move() {
        if (x + speedX >= 0 && x + DIAMETER + speedX <= 700) {
            x += speedX;
        } else if (x + speedX < 0) {
            x = 0;
        } else if (x + DIAMETER + speedX > 700) {
            x = 700 - DIAMETER;
        }
        if (y + speedY >= 0 && y + DIAMETER + speedY <= 700) {
            y += speedY;
        } else if (y + speedY < 0) {
            y = 0;
            speedY = -speedY;
        } else if (y + DIAMETER + speedY > 700) {
            y = 700 - DIAMETER;
            speedY = -speedY;
        }
    }

    private void checkPaddleCollision() {
        if (y + DIAMETER / 2 >= clients[0].getY() && y + DIAMETER / 2 <= clients[0].getY() + clients[0].getHeight()) {
            if (x <= clients[0].getX() + clients[0].getWidth()) {
                double ratio, radians;
                ratio = (float) (y + DIAMETER / 2 - clients[0].getY()) / (float) clients[0].getHeight();
                radians = (float) (-Math.PI / 4 + Math.PI / 2 * ratio);
                System.out.println(ratio + ":" + radians);
                speedX = (int) ((SPEED + hits / 4) * Math.cos(radians));
                speedY = (int) ((SPEED + hits / 4) * Math.sin(radians));
                hits++;
            }
        }
        if (y + DIAMETER / 2 >= clients[1].getY() && y + DIAMETER / 2 <= clients[1].getY() + clients[1].getHeight()) {
            if (x + DIAMETER >= clients[1].getX()) {
                double ratio, radians;
                ratio = (float) (y + DIAMETER / 2 - clients[1].getY()) / (float) clients[1].getHeight();
                radians = (float) (-Math.PI / 4 + Math.PI / 2 * ratio);
                System.out.println(ratio + ":" + radians);
                speedX = -((int) ((SPEED + hits / 5) * Math.cos(radians)));
                speedY = (int) ((SPEED + hits / 5) * Math.sin(radians));
                hits++;
            }
        }
    }

    private void checkScore() {
        if (x <= 0 || x >= 700 - DIAMETER) {
            if (x <= 0) {
                clients[1].score++;
            } else if (x >= 700 - DIAMETER) {
                clients[0].score++;
            }
            ballMoving = false;
            hits = 0;
        }
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

    public void setSpeeds(int speedX, int speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    protected void update() {
        checkScore();
        if (ballMoving) {
            if (clients[0] != null && clients[1] != null) {
                checkPaddleCollision();
            }
            move();
        } else {
            x = 700 / 2 - 10;
            y = 700 / 2 - 10;
        }
    }

}
