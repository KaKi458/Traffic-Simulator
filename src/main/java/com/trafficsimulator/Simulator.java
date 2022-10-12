package com.trafficsimulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulator extends JFrame {

    private final Canvas canvas;
    private final Gui gui;
    
    private boolean isObstacleDragged = false;
    private boolean isTLDragged = false;
    
    final static int WIDTH = 1800;
    final static int HEIGHT = 800;
   
    private final Simulation simulation;
    private final FuzzyModel fuzzyModel;
    private final Road road;
    private TrafficLights trafficLights;
    private final List<Car> cars;
    private Car carInfo = null;
    private final Random random;

    public Simulator() {

        setTitle("Simulator");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new GridLayout(2,1));

        JPanel visualisation = new JPanel();

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT / 2));
        canvas.setMaximumSize(new Dimension(WIDTH, HEIGHT / 2));
        canvas.setMinimumSize(new Dimension(WIDTH, HEIGHT / 2));
        canvas.addMouseListener(new SimulatorMouseAdapter());
        canvas.addMouseMotionListener(new SimulatorMouseMotionAdapter());
        canvas.addKeyListener(new SimulatorKeyAdapter());
        visualisation.add(canvas);
        
        random = new Random();

        fuzzyModel = new FuzzyModel();
        //fuzzyModel.displayCharts();
        
        road = new Road(0, 200, WIDTH, Simulation.initialMaxSpeed, Simulation.initialNoOfLanes); 
        cars = new ArrayList<>();
        initCars();
        road.setCars(cars);

        gui = new Gui(this);
        add(visualisation);
        add(gui);
        pack();
        setVisible(true);
        
        simulation = new Simulation(this);
        setDefault();
        simulation.start();
    }	
    
    public void render(){
        
        Graphics2D g2d;
        BufferStrategy bs = canvas.getBufferStrategy();
        if(bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        g2d = (Graphics2D) bs.getDrawGraphics();
        g2d.clearRect(0, 0, WIDTH, HEIGHT);  
        
        road.render(g2d);
        if(road.isExtraLane()) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(road.getXStart(), road.getY() + RoadLane.WIDTH, 1200, 3);
        }
        
        for(Car car : cars) {
            car.render(g2d);
        }
        if(trafficLights != null)
            trafficLights.render(g2d);
        
        displayInfo(g2d);
        
        bs.show();
        g2d.dispose();  
    }	
 
    
    private void initCars() {
             
        int space = (road.getLength() - Simulation.initialNoOfCars * Car.LENGTH) / Simulation.initialNoOfCars;
        for(int i = 0; i < Simulation.initialNoOfCars; i++) {
            int lane = random.nextInt(road.getNoOfLanes());
            cars.add(new Car(road, lane, road.getXStart() + i*(Car.LENGTH + space), 0.7 * road.getMaxSpeed(0), fuzzyModel));
        }
    }
    
    public boolean addCar() {
        
       boolean isCarAdded = false;
        for(int i = 0; i < 4; i++) {
            int lane = random.nextInt(road.getNoOfLanes());
            Car first = road.getFirstOnLane(road.getLane(lane));
            if(first == null || first.getX() > 50) {
                cars.add(new Car(road, lane, fuzzyModel)); 
                simulation.incrementCounter();
                isCarAdded = true;
                break;
            }
        }
        return isCarAdded;
        
    }
    
    public void addExtraCar() {
  
        Car first = road.getFirstOnLane(road.getLane(-1));
        if(first == null || first.getX() > 30) { 
            cars.add(new Car(road, -1, -Car.LENGTH, 45, fuzzyModel));
            simulation.incrementExtraCounter();
        }
    }
    
      
    public void removeCars() {
        
        for(int i = cars.size() - 1; i >= 0; i-- ) {
            Car car = cars.get(i);
            if(car.getX() > Simulator.WIDTH + 500 || car.isToRemove()) {
                cars.remove(car);
            }
        } 
    }
    
    public List<Car> getCars() {
        return cars;
    }
    
    public Road getRoad() {
        return road;
    }
   
    public TrafficLights getTrafficLights() {
        return trafficLights;
    }
    
    public void addTrafficLights() {
        
        trafficLights = new TrafficLights(1200, 10, 10, road);
        road.setTrafficLights(trafficLights);
    }
    
    public void setDefault() {
       
        road.setTrafficLights(null);
        trafficLights = null;
        road.removeExtraLane();
        simulation.setExtraLane(false);
        road.setCrossroad(false);
        road.setSecondSpeedLimit(0);
        road.removeObstacle();
    }
    
    public Simulation getSimulation() {
        return simulation;
    }
    
    
    private void displayInfo(Graphics2D g2d) {
     
        g2d.setColor(Color.black);
        g2d.drawString("Average speed =  " + simulation.getAverageSpeed(), 200, 300);
        g2d.drawString("Number of cars:  " + simulation.getNoOfCars(), 200, 320);
        g2d.drawString("Cars Inflow:  "     + simulation.getCarsInflow(), 200, 50);
        g2d.drawString("Real CarsInflow:  " + simulation.getRealCarsInflow(), 200, 70);
        if(simulation.isExtraLane()) {
            g2d.drawString("Extra CarsInflow:  "     + simulation.getExtraCarsInflow(), 350, 50);
            g2d.drawString("Real Extra CarsInflow:  " + simulation.getRealExtraCarsInflow(), 350, 70);
        }
        if(road.isSecondSpeedLimit()) {
            g2d.drawString("II Average speed =  " + simulation.getSecondAverageSpeed(), 350, 300);
        }
        
        if(carInfo != null) {

            displayCarInfo(g2d);
            
            if(carInfo.isToRemove() || carInfo.getX() > WIDTH) {
                carInfo.setCarInfo(false);
                carInfo = null;
            }
        }
    }
    
    private void displayCarInfo(Graphics2D g2d) {
        
        DecimalFormat df = new DecimalFormat("0.00");
        
        g2d.drawString("Speed =  " + (int)carInfo.getSpeed(), 1200, 20);
        g2d.drawString("Distance:  " + (int)carInfo.getDistance(), 1200, 40);
        g2d.drawString("Speed Difference:  " + (int)carInfo.getSpeedDifference(), 1200, 60);
        g2d.drawString("Acceleration: ", 1200, 100);

        g2d.drawString("Safe Dist Ratio:  " + df.format(carInfo.getSafeDistRatio()), 1400, 20);
        g2d.drawString("Dynamics:  " + df.format(carInfo.getDynamics()), 1400, 40);
        g2d.drawString("Rush:  " + df.format(carInfo.getRush()), 1400, 60);
                                      
        g2d.drawString("X =  " + (int)carInfo.getX(), 1400, 100);
        
        if(carInfo.getAcceleration() > 0.5)
            g2d.setColor(Color.blue);
        else if(carInfo.getAcceleration() < -0.5)
            g2d.setColor(Color.RED);
        else
            g2d.setColor(Color.GRAY);
        
        double a = carInfo.getAcceleration();
        if(a < 0 && a > -0.5)
            a = 0;
        g2d.drawString("" + df.format(a), 1300, 100);
    }
    
    public void updateGui(int inflow) {
        gui.updateCarsInflowSlider(inflow);
    }


    public static void main(String[] args) {

        EventQueue.invokeLater(
                () -> new Simulator().setVisible(true));
    }

    public void clickCar(Point clickedPoint, MouseEvent e) {
        
        for(Car car : cars) {
            if (car.getContour().contains(clickedPoint) || car.getContour().intersects(new Rectangle(clickedPoint.x - 10, clickedPoint.y - 10, 30, 30))) {

                if(carInfo != null) {
                    carInfo.setCarInfo(false);
                    carInfo = null;
                }
                
                car.setCarInfo(true);
                carInfo = car;
                if(e.getButton() == MouseEvent.BUTTON3) {
                    if(!simulation.isPaused()) {
                       carInfo.brake(); 
                    }
                    else {
                        new CarSetupFrame(carInfo);
                    }
                }               
                break;
            }
        }
        
    }
    
    public void clickObstacle(Point clickedPoint) {
        
        Obstacle obstacle = road.getObstacle(null);
        if(obstacle != null) {
            if(obstacle.getContour().contains(clickedPoint)) {
                isObstacleDragged = true;
                obstacle.setDragging();
            }
        }
    }
    
    public void clickTrafficLights(Point clickedPoint) {
        
        if(trafficLights != null) {
            if(trafficLights.getContour().contains(clickedPoint)) {
                isTLDragged = true;
            }
        }
    }
    
    private class SimulatorMouseAdapter extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {

            Point clickedPoint = e.getPoint();

            clickCar(clickedPoint, e);
            clickObstacle(clickedPoint);
            clickTrafficLights(clickedPoint);
            
            

        }
            
        @Override
        public void mouseReleased(MouseEvent e) {
            
            if(isObstacleDragged) {
                road.getObstacle(null).changePosition(e.getPoint(), cars);
                isObstacleDragged = false;
            }
            
            else if(isTLDragged) {
                trafficLights.changePosition();
                isTLDragged = false;
            }
            
            else road.changeSecondSpeedLimitX();
            
            
        }
    }
    
    private class SimulatorMouseMotionAdapter extends MouseMotionAdapter {
        
        @Override
        public void mouseDragged(MouseEvent e) {
            
            if(isObstacleDragged) {
                road.getObstacle(null).drag(e.getPoint());
            }
            
            else if(isTLDragged) {
                trafficLights.drag(e.getX());
            }
            else
                road.dragSecondSpeedLimitImg(e.getX());
        }
    }
        
    private class SimulatorKeyAdapter extends KeyAdapter {
        
        @Override
        public void keyPressed(KeyEvent e) {
            
            if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            
                gui.clickPauseButton();
            }
        }
    }
}
