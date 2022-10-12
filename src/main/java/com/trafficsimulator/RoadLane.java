package com.trafficsimulator;

import java.awt.*;

public class RoadLane {
    
    final static int WIDTH = 18;
    private final int xStart;
    private final int xEnd;
    private final int y;
    private final int length;
    private final int number;
    private int side;
    
    public RoadLane(int x, int y, int length, int number) {
        
        xStart = x;
        xEnd = xStart + length;
        this.y = y;
        this.length = length; 
        this.number = number;
        side = 0;
    }
    
    public int getXStart() {
        return xStart;
    }
    
    public int getY() {
        return y;
    }
    
    public int getXEnd() {
        return xEnd;
    }
    
    public int getNumber() {
        return number;
    }
    
    public void render(Graphics2D g2d) {
        g2d.fillRect(xStart, y, length, WIDTH);
    }
    
    public void setSide(int s) {
        side = s;
    }
    
    public int getSide() {
        return side;
    }
    
}
