package com.inatsuz.pongclient;

import java.awt.Graphics;
import javax.swing.JPanel;

public class GamePanel extends JPanel{
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        PongClient.pongClient.render(g);
    }
    
}