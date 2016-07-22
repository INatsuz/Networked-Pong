package com.inatsuz.pongserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import javax.swing.Timer;

public class PongServer implements ActionListener {

    protected static boolean running = false;

    private int MAX_CLIENTS = 2;
    private Clients[] clients = new Clients[MAX_CLIENTS];

    protected static final int PORT = 2509;
    private DatagramSocket socket;

    private final int BALL_SPEED = 12;
    private final int WIDTH = 700, HEIGHT = 700;
    private Ball ball;

    private Thread receive, send;

    private static PongServer pongServer;

    public PongServer() {
        System.out.println("Server Starting");
        running = true;
        try {
            socket = new DatagramSocket(PORT);
            receive();
        } catch (SocketException ex) {
            System.out.println("Server Failed To Start");
            ex.printStackTrace();
        }
        System.out.println("Server Started");
        ball = new Ball(WIDTH / 2 - 10, HEIGHT / 2 - 10, clients, pongServer);
        Timer timer = new Timer(20, this);
        timer.start();
    }

    public static void main(String[] args) {
        pongServer = new PongServer();
    }

    private void receive() {
        receive = new Thread("Receive") {
            public void run() {
                while (running) {
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try {
                        socket.receive(packet);
                        process(packet);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(new String(data));
                }
            }
        };
        receive.start();
    }

    private void sendID(String ID, InetAddress ip, int port) {
        byte[] data = ID.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
        try {
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void process(DatagramPacket packet) {
        if (new String(packet.getData()).trim().startsWith("c/")) {
            for (int i = 0; i < MAX_CLIENTS; i++) {
                if (clients[i] == null) {
                    clients[i] = new Clients(packet.getAddress(), packet.getPort());
                    sendID(String.valueOf(i), packet.getAddress(), packet.getPort());
                    clients[i].setX(700 * i - 50 * i);
                    System.out.println(i);
                    return;
                }
            }
            System.out.println("Server Is Full");
            sendPacket("Server Is Full", packet.getAddress(), packet.getPort());
        } else if (new String(packet.getData()).trim().startsWith("co/")) {
            String[] strings = new String[3];
            strings = new String(packet.getData()).trim().split("/");
            clients[Integer.parseInt(strings[1])].setY(Integer.parseInt(strings[2]));
            send(Integer.parseInt(strings[1].trim()), packet);
        } else if (new String(packet.getData()).trim().startsWith("bs/")) {
            String[] strings = new String[3];
            send("bm/true");
            ball.ballMoving = true;
            int direction = 0, speedX, speedY;
            double radians;
            strings = new String(packet.getData()).trim().split("/");
            radians = Math.toRadians(Integer.parseInt(strings[1]));
            if (Integer.parseInt(strings[2]) == 0) {
                direction = -1;
            } else if (Integer.parseInt(strings[2]) == 1) {
                direction = 1;
            }
            speedX = (int) (BALL_SPEED * Math.cos(radians)) * direction;
            speedY = (int) (BALL_SPEED * Math.sin(radians));
            ball.setSpeeds(speedX, speedY);
        } else if (new String(packet.getData()).trim().startsWith("rs/")) {
            reset();
        }
    }

    private void send(int ID, DatagramPacket packet) {
        send = new Thread("Send") {
            public void run() {
                for (int i = 0; i < MAX_CLIENTS; i++) {
                    if (i != ID) {
                        if (clients[i] != null) {
                            sendPacket(packet, clients[i].ip, clients[i].PORT);
                        }
                    }
                }
            }
        };
        send.start();
    }

    protected void send(String message) {
        send = new Thread("Send") {
            public void run() {
                for (int i = 0; i < MAX_CLIENTS; i++) {
                    if (clients[i] != null) {
                        sendPacket(message, clients[i].ip, clients[i].PORT);
                    }
                }
            }
        };
        send.start();
    }

    private void sendPacket(String message, InetAddress ip, int port) {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
        try {
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendPacket(DatagramPacket iPacket, InetAddress ip, int port) {
        byte[] data = iPacket.getData();
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
        try {
            socket.send(packet);
            System.out.println("Packet Sent to: " + ip + ":" + port);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (ball.ballMoving) {
            ball.update();
        } else {
            send("bm/false");
            if (clients[0] != null && clients[1] != null) {
                send("sc/" + String.valueOf(clients[0].score) + "/" + String.valueOf(clients[1].score));
            }
        }
        for (int i = 0; i < MAX_CLIENTS; i++) {
            if (clients[i] != null) {
                if (i == 0) {
                    sendPacket("bc/" + ball.getX() + "/" + ball.getY(), clients[i].ip, clients[i].PORT);
                } else {
                    sendPacket("bc/" + (700 - 20 - ball.getX()) + "/" + ball.getY(), clients[i].ip, clients[i].PORT);
                }
            }
        }
    }

    private void reset() {
        ball.setX(WIDTH / 2 - 10);
        ball.setY(HEIGHT / 2 - 10);
        send("bm/false");
        ball.ballMoving = false;
        for (int i = 0; i < MAX_CLIENTS; i++) {
            clients[i].score = 0;
        }
    }

}


//Handle The Reset Packet
