package com.inatsuz.pongserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class PongServer {

    protected static boolean running = false;

    private int MAX_CLIENTS = 2;
    private Clients[] clients = new Clients[MAX_CLIENTS];

    protected static final int PORT = 2509;
    private DatagramSocket socket;

    private final int BALL_SPEED = 12;

    private Thread receive, send;

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
    }

    public static void main(String[] args) {
        PongServer pongServer = new PongServer();
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
                    System.out.println(i);
                    return;
                }
            }
            System.out.println("Server Is Full");
            sendPacket("Server Is Full", packet.getAddress(), packet.getPort());
        } else if (new String(packet.getData()).trim().startsWith("co/")) {
            String[] strings = new String[3];
            strings = new String(packet.getData()).trim().split("/");
            for (int i = 0; i <= MAX_CLIENTS; i++) {
                System.out.println(strings[i]);
            }
            send(Integer.parseInt(strings[1].trim()), packet);
        } else if (new String(packet.getData()).trim().startsWith("ba/")) {
            String[] strings = new String[3];
            int direction = 0, speedX, speedY;
            double radians;
            strings = new String(packet.getData()).trim().split("/");
            System.out.println("");
            radians = Math.toRadians(Integer.parseInt(strings[1]));
            if (Integer.parseInt(strings[2]) == 0) {
                direction = -1;
            } else if (Integer.parseInt(strings[2]) == 1) {
                direction = 1;
            }
            speedX = (int) (BALL_SPEED * Math.cos(radians) * direction);
            speedY = (int) (BALL_SPEED * Math.sin(radians));
            for(int i = 0; i < MAX_CLIENTS; i++){
                sendPacket("ba/" + String.valueOf(speedX) + "/" + String.valueOf(speedY), clients[i].ip, clients[i].PORT);
                speedX = -speedX;
            }
            System.out.println("BA");
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

    private void send(String message) {
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

}
