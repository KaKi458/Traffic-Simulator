package com.trafficsimulator;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Obstacle {
    
    private int x, y;
    private final Road road;
    private RoadLane lane;
    private final int length = 10;
    private final int width = 10;
    private Rectangle contour;
    private boolean isDragging = false;
    
    public Obstacle(Road road) {
        
        this.road = road;
        
        Random random = new Random();
        int laneNo = random.nextInt(road.getNoOfLanes());
        this.lane = road.getLane(laneNo);
        
        this.x = 1200 + random.nextInt(200);
        this.y = lane.getY() + (RoadLane.WIDTH - width) / 2;

        contour = new Rectangle(x, y, length, width);
    }   
    
    public Obstacle(RoadLane extraLane) {
        
        this.road = null;
        this.lane = extraLane;
        
        this.x = extraLane.getXEnd();
        this.y = extraLane.getY();
        
        contour = new Rectangle(x, y, 1800 - x, RoadLane.WIDTH);
        
    }   
    
    public Obstacle(RoadLane lane, int x) {
        
        this.road = null;
        this.lane = lane;
        this.x = x;
    }
    
    public void render(Graphics g) {
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.black);
        g2d.fill(contour);
    }
    
    public void changePosition(Point point, List<Car> cars) {
               
        for(int i = 0; i < road.getNoOfLanes(); i++) {
            
            if(road.getLane(i).getY() < point.y && road.getLane(i).getY() + RoadLane.WIDTH > point.y) {
                
                this.lane = road.getLane(i);
                this.x = point.x;
                this.y = lane.getY() + (RoadLane.WIDTH - width) / 2;
                
                for(Car car : cars) {
                    if(car.getLane() == lane) {
                        car.setToRemove();
                    }
                }
            } 
        }
        
        contour.x = this.x;
        contour.y = this.y;

        isDragging = false;
    }
    
    public void drag(Point point) {
        contour.x = point.x;
        contour.y = point.y;
    }
    
    public void setDragging() { isDragging = true; }
    
    public boolean isDragging() { return isDragging; }
    
    public int getX() { return x; }
    
    public RoadLane getLane() { return lane; }
    
    public int getLaneNumber() { return lane.getNumber(); }
    
    public Rectangle getContour() { return contour; }
}
