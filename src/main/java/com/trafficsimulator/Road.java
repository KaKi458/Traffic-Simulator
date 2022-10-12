package com.trafficsimulator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Road {
    
    private final int xStart;
    private final int y;
    private final int length;
    
    private int noOfLanes;
    private int maxSpeed;
    
    private int secondMaxSpeed = 0;
    private int secondSpeedLimitX = 1200;
    private Image secondSpeedLimitImg;
    private int secondSpeedLimitImgX = secondSpeedLimitX;

    private final Map<Integer, Image> speedLimitsImages = new HashMap<>();

    
    private final List<RoadLane> lanes;
    private List<Car> cars;
    private Obstacle obstacle = null;
    private TrafficLights trafficLights = null;
    
    private boolean isCrossroad = false;
    private int destinationPercentage;
    private RoadLane extraLane = null;
    
    
    public Road(int x, int y, int length, int maxSpeed, int noOfLanes) {
        
        xStart = x;
        this.y = y;
        this.length = length;
        this.maxSpeed = maxSpeed;
        this.noOfLanes = noOfLanes;

        lanes = new ArrayList<>();
        for(int i = 0; i < noOfLanes; i++) {
            int laneXStart = xStart - i * RoadLane.WIDTH;
            int laneY = y - i * RoadLane.WIDTH;
            lanes.add(new RoadLane(laneXStart, laneY, length, i));
        }

        try {
            readSpeedLimitImages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void render(Graphics g) {
        
        Graphics2D g2d = (Graphics2D) g;  
        
        g2d.setColor(Color.GRAY);
        for(RoadLane lane : lanes) {
            lane.render(g2d);
        }   
        if(extraLane != null)
            extraLane.render(g2d);
        
        g2d.setColor(Color.white);
        for(RoadLane lane : lanes) {
           g2d.drawLine(lane.getXStart(), lane.getY(), lane.getXEnd(), lane.getY());      
        }  
        if(extraLane != null)
           g2d.drawLine(extraLane.getXStart(), extraLane.getY(), extraLane.getXEnd(), extraLane.getY());      

        if(isCrossroad) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(1200, lanes.get(noOfLanes/2 - 1).getY(), 600, 3);
        }
        
        if(secondMaxSpeed != 0) {
            g2d.drawImage(secondSpeedLimitImg, secondSpeedLimitImgX, lanes.get(noOfLanes - 1).getY() - 50, null);
        }

        if(obstacle != null) {
            obstacle.render(g);
        }
    }
    
    public void setCars(List<Car> cars) { this.cars = cars; }
    
    public void setTrafficLights(TrafficLights tL) { this.trafficLights = tL; }
    
    public TrafficLights getTrafficLights() { return trafficLights; }
      
    public int getXStart() { return xStart; }
    
    public int getY() { return y; }

    public int getLength() { return length; }
    
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }
    
    public int getMaxSpeed(double x) {
        
        if(secondMaxSpeed == 0)
            return maxSpeed;
        else {
            if(x < secondSpeedLimitX)
                return maxSpeed;
            else
                return secondMaxSpeed;
        }            
    }
    
    public Car getNeighbourFront(Car car) {
        
        Car neighbour = null;
        for(Car otherCar : cars) {
            if(otherCar.getLane() == car.getLane() && otherCar.getX() - car.getX() > 0) {
                if(neighbour == null || neighbour.getX() - car.getX() > otherCar.getX() - car.getX()) {
                    neighbour = otherCar;
                } 
            }
        }
        return neighbour;
    }
    
    
    public Car getNeighbourLeftBack(Car car) {
        
        Car neighbour = null;
        for(Car otherCar : cars) {
            if(otherCar.getLane().getNumber() == (car.getLane().getNumber() + 1) && otherCar.getX() < car.getX()) {
                if(neighbour == null || car.getX() - neighbour.getX() > car.getX() - otherCar.getX()) {
                    neighbour = otherCar;
                } 
            }
        }
        return neighbour;
    }
    
    public Car getNeighbourLeftFront(Car car) {
        
        Car neighbour = null;
        if(car.getNeighbourLeftBack() != null)
            neighbour = car.getNeighbourLeftBack().getNeighbourFront();
        else {
            for(Car otherCar : cars) {
                if(otherCar.getLane().getNumber() == (car.getLane().getNumber() + 1) && otherCar.getX() > car.getX()) {
                    if(neighbour == null || neighbour.getX() - car.getX() > otherCar.getX() - car.getX()) {
                        neighbour = otherCar;
                    } 
                }
            } 
        }   
        return neighbour;
    }
    
    public Car getNeighbourRightBack(Car car) {
          
        Car neighbour = null;
        for(Car otherCar : cars) {
            if(otherCar.getLane().getNumber() == (car.getLane().getNumber() - 1) && otherCar.getX() < car.getX()) {
                if(neighbour == null || car.getX() - neighbour.getX() > car.getX() - otherCar.getX()) {
                    neighbour = otherCar;
                } 
            }
        }
        return neighbour;
    }
    
    public Car getNeighbourRightFront(Car car) {
        
        Car neighbour = null;
        if(car.getNeighbourRightBack() != null)
            neighbour = car.getNeighbourRightBack().getNeighbourFront();
        else {
            for(Car otherCar : cars) {
                if(otherCar.getLane().getNumber() == (car.getLane().getNumber() - 1) && otherCar.getX() > car.getX()) {
                    if(neighbour == null || neighbour.getX() - car.getX() > otherCar.getX() - car.getX()) {
                        neighbour = otherCar;
                    } 
                }
            } 
        }   
        return neighbour;
    }
        
    public double getAverageSpeed() {
        
        double averageSpeed = 0;
        for(Car car : cars) {
            if(car.getX() > 0 && car.getX() < 1800)
                averageSpeed += car.getSpeed();
        }
        averageSpeed = averageSpeed / cars.size();
        return averageSpeed;
    }
    
    public double getPossibleSpeed(int lane, double x) {
        
        double possibleSpeed = 0;
        int noOfCarsOnLane = 0;
        for(Car car : cars) {
            if(car.getLane() == getLane(lane) && car.getX() > x && car.getX() < x + 600) {
                possibleSpeed += car.getSpeed();
                noOfCarsOnLane++;
            }
        }
        possibleSpeed = possibleSpeed / noOfCarsOnLane;
        
        if(noOfCarsOnLane == 0)
            possibleSpeed = maxSpeed;
        
        return possibleSpeed; 
    }
    
    public Car getFirstOnLane(RoadLane lane) {
        
        Car first = null;
        for(Car car : cars) {
            if(car.getLane() == lane) {
                if(first == null || first.getX() > car.getX()) {
                    first = car;
                }
            }
        }
        return first;
    }

    public RoadLane getLane(int lane) {

        if(lane >= 0 && lane < lanes.size())
            return lanes.get(lane);
        else if(lane == -1)
            return extraLane;
        return null;
    }
    
    public int getNoOfLanes() { return noOfLanes; }
    
    public void incrementNoOfLanes() {
        
        int laneXStart = lanes.get(noOfLanes - 1).getXStart() - RoadLane.WIDTH;
        int laneY = lanes.get(noOfLanes - 1).getY() - RoadLane.WIDTH;
        lanes.add(new RoadLane(laneXStart, laneY, length, noOfLanes));
        noOfLanes++;
        
        if(isCrossroad) {
            for(RoadLane lane : lanes) {
                if(lane.getNumber() > noOfLanes/2 - 1)
                    lane.setSide(-1);
                else
                    lane.setSide(1);
            }
        }
    }
    
    public void decrementNoOfLanes() {
        
        for(Car car : cars) {
            if(car.getLane() == lanes.get(noOfLanes - 1)) {
                car.setToRemove();
            }
        }
        if(obstacle != null && obstacle.getLane() == lanes.get(noOfLanes - 1))
            obstacle = null;
        lanes.remove(noOfLanes - 1);
        noOfLanes--;
        
        if(isCrossroad) {
            for(RoadLane lane : lanes) {
                if(lane.getNumber() > noOfLanes/2 - 1)
                    lane.setSide(-1);
                else
                    lane.setSide(1);
            }
        }
    }
    
    public void addObstacle() {
        
        obstacle = new Obstacle(this);
        for(Car car : cars) {
            if(car.getLane() == obstacle.getLane()) {
                car.setToRemove();
            }
        }
    }
    
    public void removeObstacle() {
        obstacle = null;
    }
    
    public Obstacle getObstacle(Car car) {
        
        if(car == null)
            return obstacle;
        
        if(car.getDestination() == 1) {
            if(car.getLaneNumber() > noOfLanes/2 - 1) {
                return new Obstacle(car.getLane(), 1170);
            }
            else
                return null;
        }
        else if(car.getDestination() == -1) { 
            if(car.getLaneNumber() <= noOfLanes/2 - 1) {
                return new Obstacle(car.getLane(), 1120);
            }
            else
                return null;
        }
        else
            return obstacle;
    }
    
    public void addExtraLane(int length) {
        
        extraLane = new RoadLane(xStart, y + RoadLane.WIDTH, length, -1); 
        obstacle = new Obstacle(extraLane);
    }
    
    public void removeExtraLane() {
        
         extraLane = null;
         obstacle = null;
         for(Car car : cars) {
             if(car.getLaneNumber() == -1)
                 car.setToRemove();
         }
    }
    
    public boolean isExtraLane() { return extraLane != null; }
    
    public void setSecondSpeedLimit(int secondMaxSpeed)   {
        this.secondMaxSpeed = secondMaxSpeed;
        secondSpeedLimitImg = speedLimitsImages.get(secondMaxSpeed);
    }

    private void readSpeedLimitImages() throws IOException {

        speedLimitsImages.put(
                30, ImageIO.read(Objects.requireNonNull(getClass()
                        .getResource("/img/speed30.png"))));

        speedLimitsImages.put(
                40,
                ImageIO.read(Objects.requireNonNull(getClass()
                        .getResource("/img/speed40.png"))));

        speedLimitsImages.put(
                50, ImageIO.read(Objects.requireNonNull(getClass()
                        .getResource("/img/speed50.png"))));

        speedLimitsImages.put(
                60, ImageIO.read(Objects.requireNonNull(getClass()
                        .getResource("/img/speed60.png"))));

        speedLimitsImages.put(
                70, ImageIO.read(Objects.requireNonNull(getClass()
                        .getResource("/img/speed70.png"))));
    }

    public void changeSecondSpeedLimitX() { secondSpeedLimitX = secondSpeedLimitImgX; }
    
    public void dragSecondSpeedLimitImg(double x) {
        
        if(x >= 800 && x <= 1500) 
            secondSpeedLimitImgX = (int)x;
       
        else if(x < 800)
            secondSpeedLimitImgX = 800;
        
        else
            secondSpeedLimitImgX = 1500;
    }
    
    public boolean isCrossroad() { return isCrossroad; }
    
    public void setCrossroad(boolean w) {
        
        isCrossroad = w;
        if(w) {
            destinationPercentage = 50;
            obstacle = null;
            for(RoadLane lane : lanes) {
                if(lane.getNumber() > noOfLanes/2 - 1)
                    lane.setSide(-1);
                else
                    lane.setSide(1);
            }
            for(Car car : cars) {
                if(car.getX() < 800)
                    car.setDestination(destinationPercentage);
            }
        }
        else {
            for(Car car : cars) {
                car.clearDestination();
            }

            for(RoadLane lane : lanes) {
                lane.setSide(0);
            }
        }
    }
    
    public void changeDestinationPercentage(int percentage) { destinationPercentage = percentage; }
    
    public int getDestinationPercentage() { return destinationPercentage; }
    
    public boolean isSecondSpeedLimit() { return secondMaxSpeed != 0; }
    
    public int getSecondSpeedLimitX() { return secondSpeedLimitX; }
}