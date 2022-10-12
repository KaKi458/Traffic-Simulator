package com.trafficsimulator;


import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Simulation implements Runnable{

    private final TrafficSimulatorApp simulator;
    
    private boolean isRun = false; 
    private boolean isPaused = false;
    private boolean isExtraLane = false;
    private boolean isRandomize = false;
    private long lastTime, now;
    private long pauseTime;
    private Thread thread;
    
    final static int fps = 60;
    final static double oneSecondPeriod = 1000 * 1000000;
    final static double displayPeriod = oneSecondPeriod / fps;
    private double carsInflowPeriod = (oneSecondPeriod / (4 *  initialCarsInflow)) * 60;
    private double extraCarsInflowPeriod = 0;
    
    final static double timeStep = (displayPeriod * 2) / 1000000;
 
    final static int pixelsPerMeter = 3;
    
    final static int initialCarsInflow = 150; 
    final static int initialNoOfCars = 50;
    final static int initialNoOfLanes = 4;
    final static int initialMaxSpeed = 100;

    private int carsInflow = initialCarsInflow;
    private int extraCarsInflow = 0;
    private int realCarsInflow;
    private int realExtraCarsInflow;
    private int noOfCars = initialNoOfCars;
    private int averageSpeed = 0;
    private int secondAverageSpeed = 0;
    private int counter = 0;
    private int extraCounter = 0;
    private final int[] counts;
    private final int[] extraCounts;
    private int second = 0;
    private int inflowPurpose;
    
    private boolean isCarAdded = false;
    private int carAddingCounter = 0;


    private final List<Car> cars;
    
    public Simulation(TrafficSimulatorApp simulator) {
        
        this.simulator = simulator;
        this.cars = simulator.getCars();

        counts = new int [12];
        extraCounts = new int [12];
        for(int i = 0; i < 12; i++) {
            counts[i] = 0;
            extraCounts[i] = 0;
        }
    }

    @Override
    public void run() {
    
        long dt;
        double displayDelta = 0;
        double carsInflowDelta = 0;
        double extraCarsInflowDelta = 0;
        double otherDelta = 0;
        double realInflowDelta = 0;

        lastTime = System.nanoTime();
        
        while(isRun){
            
            if(!isPaused) {
                now = System.nanoTime();
                dt = now - lastTime;

                displayDelta += dt / displayPeriod;
                double otherPeriod = 300 * 1000000;
                otherDelta += dt / otherPeriod;
                carsInflowDelta += dt / carsInflowPeriod;
                
                if(isExtraLane && extraCarsInflowPeriod != 0)
                    extraCarsInflowDelta += dt / extraCarsInflowPeriod;
                            
                realInflowDelta += dt / oneSecondPeriod; 

                lastTime = now;          

                if(displayDelta >= 1) {

                    simulator.render();
                    computeKinematics();   
                    
                    if(isRandomize)
                        updateCarsInflow();
                    
                    displayDelta--;
                }

                
                if(otherDelta >= 1) {

                    changeLanes();
                    simulator.removeCars();
                    checkTrafficLights();
                    checkCollision();          
                    countNoOfCars();
                    
                    if(simulator.getTrafficLights() != null)
                        simulator.getTrafficLights().lightChange(otherPeriod / 1000000);
                    
                    otherDelta--;
                }

                if(carsInflowDelta >= 1) {

                    addCar();
                    carsInflowDelta--;
                }
                
                if(extraCarsInflowDelta >= 1) {

                    simulator.addExtraCar();
                    extraCarsInflowDelta--;
                }

                if(realInflowDelta >= 1) {

                    countRealCarsInflow();
                    
                    if(isExtraLane)
                        countRealExtraCarsInflow();

                    second++;
                    if(second > 11)
                        second = 0;

                    if(isRandomize && (second == 4 || second == 8)) {
                        randomize();
                    }
                        
                    realInflowDelta --;
                }
            }
            else
                simulator.render();
        }
        stop();
    }

    public synchronized void start(){
        
        if(isRun)
            return;
        isRun = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop(){
        
        if(!isRun)
            return;
        isRun = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void computeKinematics() {
        
        for(Car car : cars) {
            car.accelerate();
            car.correct();  
            car.moveX();
            if(car.isLaneChanging() == -1 || car.isLaneChanging() == 1) {
                car.moveY();
            }
        }
    }
    
    private void changeLanes() {
        
        for(Car car : cars) {
            car.changeLane();
            car.decideZipperMerge(cars);
        }
    }  
    
    private void checkTrafficLights() {

        for(Car car : cars) {
            car.lookTrafficLights();
        }
    }
    
    private void checkCollision() {               
        Collision.checkCollisions(cars, simulator.getRoad().getObstacle(null));
    }
    
    private void addCar() {
        
        if(!isCarAdded) {
            isCarAdded = simulator.addCar();
        }
        
        carAddingCounter++;
        
        if(carAddingCounter == 4) {
            carAddingCounter = 0;
            isCarAdded = false;
        } 
    }
    
    
    private void countNoOfCars() {
        
        noOfCars = 0;
        int sumOfSpeed = 0;
        for(Car car : cars) {
            if(car.getX() > 0 && car.getX() < 1800 && car.getLaneNumber() != -1) {
                noOfCars++;
                sumOfSpeed += car.getSpeed();
            }
        }
        if(noOfCars != 0)
            averageSpeed = sumOfSpeed / noOfCars;
        else
            averageSpeed = 0;
        
        if(simulator.getRoad().isSecondSpeedLimit()) {
            
            int sum1 = 0, sum2 = 0;
            int n1 = 0, n2 = 0;
            for(Car car : cars) {
                if(car.getX() > 0 && car.getX() < simulator.getRoad().getSecondSpeedLimitX()) {
                    n1++;
                    sum1 += car.getSpeed();
                }
                else if(car.getX() > simulator.getRoad().getSecondSpeedLimitX() && car.getX() < 1800){
                    n2++;
                    sum2 += car.getSpeed();
                }
            }
            if(n1 != 0)
                averageSpeed = sum1 / n1;
            else
                averageSpeed = 0;
            
            if(n2 != 0)
                secondAverageSpeed = sum2 / n2;
            else 
                secondAverageSpeed = 0;
        }
    }
    
    private void countRealCarsInflow() {
        
        realCarsInflow = 5 * (counter - counts[second]);
        counts[second] = counter;
    }
    
    private void countRealExtraCarsInflow() {
        
        realExtraCarsInflow = 5 * (extraCounter - extraCounts[second]);
        extraCounts[second] = extraCounter;
    }
    
    public void incrementCounter() {
        counter++;
    }
        
    public void incrementExtraCounter() {
        extraCounter++;
    }
    
    public int getRealCarsInflow() {
        return realCarsInflow;
    }
    
    public int getRealExtraCarsInflow() {
        return realExtraCarsInflow;
    }
    
    public int getAverageSpeed() {
        return averageSpeed;
    }
    
    public int getSecondAverageSpeed() {
        return secondAverageSpeed;
    }
    
    public int getNoOfCars() {
        return noOfCars;
    } 
    
    private void test() {
        
        DecimalFormat df = new DecimalFormat("0.00");
        for(int i = 0; i < cars.size(); i+=5) {
            Car c = cars.get(i);
            System.out.println("v : " + (int)c.getSpeed() + " dist :  " + (int)c.getDistance() + "  speedDiff :  " + (int)c.getSpeedDifference() + "  a :  " + df.format(c.getAcceleration()) );
        }
    }
    
    public void pause() {
        pauseTime = System.nanoTime();
        isPaused = true;
    }
    
    public void resume() {

        long resumeTime = System.nanoTime();
        lastTime += resumeTime - pauseTime;
        now += resumeTime - pauseTime;
        isPaused = false;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void changeCarsInflow(int carsInflow) {
        
        this.carsInflow = carsInflow;
        carsInflowPeriod = (oneSecondPeriod / (4* carsInflow)) * 60;
    }
    
    public int getCarsInflow() {
        return carsInflow;
    }
    
    public void changeExtraCarsInflow(int newCarsInflow) {
        
        extraCarsInflow = newCarsInflow;
        if(newCarsInflow != 0)
            extraCarsInflowPeriod = oneSecondPeriod / newCarsInflow * 60;
        else
            extraCarsInflowPeriod = 0;
    }
    
    public int getExtraCarsInflow() {
        return extraCarsInflow;
    }
    
    public void setExtraLane(boolean w) {
        
        isExtraLane = w;
        
        if(!w) {
                  
            realExtraCarsInflow = 0;
            for(int i = 0; i < 12; i++) {
                extraCounts[i] = 0;
            }  
        }
    }
    
    public boolean isExtraLane() {
        return isExtraLane;
    }
    
    public void setRandomize(boolean w) {
        isRandomize = w; 
        if(w)
            randomize();
    }
    
    private void randomize() {
        if(inflowPurpose > 125)
            inflowPurpose = 50 + (new Random()).nextInt(75);
        else
            inflowPurpose = 125 + (new Random()).nextInt(75);
    }
    
    private void updateCarsInflow() {
        
        if(carsInflow != inflowPurpose) {
            if(carsInflow < inflowPurpose)
                carsInflow++;
            else
                carsInflow--;
            
            simulator.updateGui(carsInflow);
        }   
    }
}
