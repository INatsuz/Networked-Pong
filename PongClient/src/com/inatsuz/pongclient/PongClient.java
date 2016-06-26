package com.inatsuz.pongclient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class PongClient implements ActionListener, KeyListener {

    protected static PongClient pongClient;

    protected final int WIDTH = 700, HEIGHT = 700;
    private JFrame jFrame;
    private GamePanel gamePanel;

    private boolean up = false, down = false;

    private boolean ballMoving = false;
    private int gameState = 0;
    private final Paddle paddle;
    private final Paddle enemyPaddle;
    private final Ball ball;

    private DatagramSocket socket;
    private InetAddress ip;
    private int PORT;
    private String ID;

    private final Random random;

    Thread receive, send;

    public PongClient() {
        Timer timer = new Timer(20, this);

        try {
            socket = new DatagramSocket();
            ip = InetAddress.getByName(JOptionPane.showInputDialog("Insert IP"));
            PORT = Integer.parseInt(JOptionPane.showInputDialog("Insert Port"));
            sendPacket("c/");
            ID = receiveID().trim();
            if (ID != null) {
                System.out.println(ID);
                if (Integer.parseInt(ID) == 0 || Integer.parseInt(ID) == 1) {
                    receive();
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }

        random = new Random();

        paddle = new Paddle(0, HEIGHT / 2 - 100, this);
        enemyPaddle = new Paddle(WIDTH - 50, HEIGHT / 2 - 100, this);
        ball = new Ball(WIDTH / 2 - 10, HEIGHT / 2 - 10, this);

        jFrame = new JFrame("Pong");
        gamePanel = new GamePanel();

        jFrame.setVisible(true);
        jFrame.setSize(WIDTH + 6, HEIGHT + 26);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(jFrame.EXIT_ON_CLOSE);
        jFrame.add(gamePanel);
        jFrame.addKeyListener(this);

        timer.start();
    }

    public static void main(String[] args) {
        pongClient = new PongClient();
    }

    protected void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE);
        g2.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
        g2.drawOval(WIDTH / 2 - 100, HEIGHT / 2 - 100, 200, 200);
        paddle.render(g);
        enemyPaddle.render(g);
        ball.render(g);
    }

    private void update() {
        send("co/" + ID + "/" + paddle.getY());
        if (ballMoving) {
            if (up) {
                paddle.move(true);
            } else if (down) {
                paddle.move(false);
            }
            ball.update();
        }
        gamePanel.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        update();
    }

    public void keyTyped(KeyEvent e) {

    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) {
            up = true;
        } else if (key == KeyEvent.VK_S) {
            down = true;
        }
        if (key == KeyEvent.VK_SPACE && !ballMoving) {
            send("ba/" + String.valueOf(random.nextInt(91) - 45) + "/" + random.nextInt(2));
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) {
            up = false;
        } else if (key == KeyEvent.VK_S) {
            down = false;
        }
    }

    private void sendPacket(String message) {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, PORT);
        try {
            socket.send(packet);
            System.out.println("Packet Sent");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String receiveID() {
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            socket.receive(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (new String(data).trim().equalsIgnoreCase("Server Is Full")) {
            return new String(data).trim();
        } else if (Integer.parseInt(new String(data).trim()) == 1 || Integer.parseInt(new String(data).trim()) == 0) {
            return new String(data).trim();
        }
        return null;
    }

    private void receive() {
        receive = new Thread("Receive") {
            public void run() {
                while (true) {
                    DatagramPacket packet = receivePacket();
                    if (new String(packet.getData()).trim().startsWith("co/")) {
                        String[] strings = new String[3];
                        strings = new String(packet.getData()).trim().split("/");
                        if (ballMoving) {
                            enemyPaddle.setY(Integer.parseInt(strings[2]));
                        }
                    } else if (new String(packet.getData()).trim().startsWith("ba/")) {
                        String[] strings = new String[3];
                        strings = new String(packet.getData()).trim().split("/");
                        ballMoving = true;
                        ball.setSpeeds(Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
                    }
                }
            }
        };
        receive.start();
        System.out.println("Thread Receive");
    }

    private void send(String message) {
        send = new Thread("Send") {
            public void run() {
                sendPacket(message);
            }
        };
        send.start();
    }

    private DatagramPacket receivePacket() {
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            socket.receive(packet);
            System.out.println("Packet");
            return packet;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}

//Come Up With A Format For The Ball Moving Packet
