package com.inatsuz.pongserver;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;

public class PongServer implements ActionListener, KeyListener {

    protected static boolean running = false;

    private int MAX_CLIENTS = 2;
    private Clients[] clients = new Clients[MAX_CLIENTS];

    protected static int PORT = 2509;
    private String ip;
    private DatagramSocket socket;

    private final int BALL_SPEED = 12;
    private final int WIDTH = 700, HEIGHT = 700;
    private Ball ball;

    private JFrame jFrame;
    private JTextPane textPane;
    private JTextPane commandPane;
    private JScrollPane scrollPane;

    private Thread receive, send;

    private static PongServer pongServer;

    public PongServer() {
        textPane = new JTextPane();
        commandPane = new JTextPane();
        scrollPane = new JScrollPane(textPane);
        textPane.setEditable(false);
        textPane.setBackground(Color.GRAY.darker().darker().darker());
        textPane.setForeground(Color.GREEN);
        commandPane.setBackground(Color.GRAY.darker().darker().darker());
        commandPane.setForeground(Color.WHITE);
        commandPane.setCaretColor(Color.WHITE);
        commandPane.setSize(650, 100);
        commandPane.setLocation(18, 525);
        scrollPane.setSize(650, 500);
        scrollPane.setLocation(18, 10);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY.darker().darker()));
        System.out.println("Server Starting");
        textPane.setText(textPane.getText() + " Server Starting \n");
        running = true;
        try {
            ip = JOptionPane.showInputDialog("Insert IP (Leave empty for local IP)");
            PORT = Integer.parseInt(JOptionPane.showInputDialog("Insert Port (If using local IP you will need to forward this port)"));
            if (!ip.equalsIgnoreCase("")) {
                socket = new DatagramSocket(null);
                InetSocketAddress address = new InetSocketAddress(ip, PORT);
                socket.bind(address);
            } else {
                socket = new DatagramSocket(PORT);
            }
            jFrame = new JFrame("Server");
            jFrame.setSize(700, 700);
            jFrame.getContentPane().setBackground(Color.GRAY.darker());
            jFrame.setLayout(null);
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setLocationRelativeTo(null);
            jFrame.add(scrollPane);
            jFrame.add(commandPane);
            commandPane.addKeyListener(this);
            jFrame.setVisible(true);
            receive();
        } catch (SocketException ex) {
            System.out.println("Server Failed To Start");
            ex.printStackTrace();
        }
        System.out.println("Server Started");
        textPane.setText(textPane.getText() + " Server Started \n");
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

    private void process(DatagramPacket packet) {
        if (new String(packet.getData()).trim().startsWith("c/")) {
            for (int i = 0; i < MAX_CLIENTS; i++) {
                if (clients[i] == null) {
                    clients[i] = new Clients(packet.getAddress(), packet.getPort());
                    sendPacket("id/" + i, packet.getAddress(), packet.getPort());
                    clients[i].setX(700 * i - 50 * i);
                    System.out.println(i);
                    textPane.setText(textPane.getText() + " User Joined \n");
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

    private void checkConsoleCommand(String command) {
        if (command.equalsIgnoreCase("/stop")) {
            textPane.setText(textPane.getText() + " " + "Server Stopping" + "\n");
            System.exit(0);
        } else if(command.equalsIgnoreCase("/ip")){
            textPane.setText(textPane.getText() + " " + ip + "\n");
        }else {
            textPane.setText(textPane.getText() + " " + "That command does not exist" + "\n");
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            textPane.setForeground(Color.WHITE);
            e.consume();
            String command = commandPane.getText();
            textPane.setText(textPane.getText() + " " + command + "\n");
            commandPane.setText(null);
            commandPane.setCaretPosition(0);
            if (command.startsWith("/")) {
                checkConsoleCommand(command);
            }
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

}

//Handle The Reset Packet