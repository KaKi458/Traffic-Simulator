package com.trafficsimulator;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import static com.trafficsimulator.TrafficSimulatorApp.WIDTH;

public class Simulation {

  static final int fps = 60;
  static final double oneSecondPeriod = 1000 * 1000000;
  static final double displayPeriod = oneSecondPeriod / fps;
  static final double timeStep = (displayPeriod * 2) / 1000000;
  static final int pixelsPerMeter = 3;
  static final int initialCarsInflow = 150;
  static final int initialNoOfCars = 50;
  static final int initialNoOfLanes = 4;
  static final int initialMaxSpeed = 100;
  private final int[] counts;
  private final int[] extraCounts;
  private final List<Car> cars;
  private final Road road;
  private final Random random;
  private final FuzzyModel fuzzyModel;
  private TrafficLights trafficLights;
  private Car carInfo = null;
  private boolean isRun = false;
  private boolean isPaused = false;
  private boolean isExtraLane = false;
  private boolean isRandomize = false;
  private long lastTime, now;
  private long pauseTime;
  private Thread thread;
  private double carsInflowPeriod = (oneSecondPeriod / (4 * initialCarsInflow)) * 60;
  private double extraCarsInflowPeriod = 0;
  private int carsInflow = initialCarsInflow;
  private int extraCarsInflow = 0;
  private int realCarsInflow;
  private int realExtraCarsInflow;
  private int noOfCars = initialNoOfCars;
  private int averageSpeed = 0;
  private int secondAverageSpeed = 0;
  private int counter = 0;
  private int extraCounter = 0;
  private int second = 0;
  private int inflowPurpose;
  private int carAddingCounter = 0;

  public Simulation() {
    
    cars = new ArrayList<>();
    road = new Road(0, 200, WIDTH, initialMaxSpeed, initialNoOfLanes, cars);
    random = new Random();
    fuzzyModel = new FuzzyModel();
    initCars();
    counts = new int[12];
    extraCounts = new int[12];
    for (int i = 0; i < 12; i++) {
      counts[i] = 0;
      extraCounts[i] = 0;
    }
  }

  private class KinematicsTask extends TimerTask {
    @Override
    public void run() {
      computeKinematics();

    }
  }

  private class DriversTask extends TimerTask {
    @Override
    public void run() {
      changeLanes();
      checkTrafficLights();
    }
  }

  private class CarsInflowTask extends TimerTask {
    @Override
    public void run() {
      addCar();
    }
  }

  private class CleanTask extends TimerTask {
    @Override
    public  void run() {
      removeCars();
      checkCollision();
    }
  }

  private class TrafficLightsTask extends TimerTask {
    @Override
    public void run() {
      changeLight();
    }
  }

  private class MeasurementTask extends TimerTask {
    @Override
    public void run() {
      measure();
    }
  }

  private void measure() {
  }


  private void changeLight() {
  }


  public void addTrafficLights() {
    trafficLights = new TrafficLights(1200, 10, 10, road);
  }

  public void setDefault() {
    trafficLights = null;
    //road.removeExtraLane();
    setExtraLane(false);
    //road.setCrossroad(false);
    road.setSecondSpeedLimit(0);
    road.removeObstacle();
  }

  public void start() {
    new Timer().schedule(new KinematicsTask(), 0, 16);
    new Timer().schedule(new DriversTask(), 0, 40);
    new Timer().schedule(new CleanTask(),0, 100);
    new Timer().schedule(new CarsInflowTask(), 0, 100);
  }

  private synchronized void computeKinematics() {
    for (Car car : cars) {
      car.accelerate();
      car.correct();
      car.moveX();
      if (car.isLaneChanging() == -1 || car.isLaneChanging() == 1) {
        car.moveY();
      }
    }
  }

  private synchronized void changeLanes() {
    for (Car car : cars) {
      car.changeLane();
      car.decideZipperMerge(cars);
    }
  }

  private synchronized void checkTrafficLights() {
//    for (Car car : cars) {
//      car.lookTrafficLights();
//    }
  }

  private synchronized void checkCollision() {
    Collision.checkCollisions(cars, road.getObstacle(null));
  }

  private void countNoOfCars() {
    noOfCars = 0;
    int sumOfSpeed = 0;
    for (Car car : cars) {
      if (car.getX() > 0 && car.getX() < 1800 && car.getLaneNumber() != -1) {
        noOfCars++;
        sumOfSpeed += car.getSpeed();
      }
    }
    if (noOfCars != 0) averageSpeed = sumOfSpeed / noOfCars;
    else averageSpeed = 0;

    if (road.isSecondSpeedLimit()) {

      int sum1 = 0, sum2 = 0;
      int n1 = 0, n2 = 0;
      for (Car car : cars) {
        if (car.getX() > 0 && car.getX() < road.getSecondSpeedLimitX()) {
          n1++;
          sum1 += car.getSpeed();
        } else if (car.getX() > road.getSecondSpeedLimitX() && car.getX() < 1800) {
          n2++;
          sum2 += car.getSpeed();
        }
      }
      if (n1 != 0) averageSpeed = sum1 / n1;
      else averageSpeed = 0;

      if (n2 != 0) secondAverageSpeed = sum2 / n2;
      else secondAverageSpeed = 0;
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
    for (int i = 0; i < cars.size(); i += 5) {
      Car c = cars.get(i);
      System.out.println(
          "v : "
              + (int) c.getSpeed()
              + " dist :  "
              + (int) c.getDistance()
              + "  speedDiff :  "
              + (int) c.getSpeedDifference()
              + "  a :  "
              + df.format(c.getAcceleration()));
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
    carsInflowPeriod = (oneSecondPeriod / (4 * carsInflow)) * 60;
  }

  public int getCarsInflow() {
    return carsInflow;
  }

  public void changeExtraCarsInflow(int newCarsInflow) {

    extraCarsInflow = newCarsInflow;
    if (newCarsInflow != 0) extraCarsInflowPeriod = oneSecondPeriod / newCarsInflow * 60;
    else extraCarsInflowPeriod = 0;
  }

  public int getExtraCarsInflow() {
    return extraCarsInflow;
  }

  public boolean isExtraLane() {
    return isExtraLane;
  }

  public void setExtraLane(boolean w) {

    isExtraLane = w;

    if (!w) {

      realExtraCarsInflow = 0;
      for (int i = 0; i < 12; i++) {
        extraCounts[i] = 0;
      }
    }
  }

  public void setRandomize(boolean w) {
    isRandomize = w;
    if (w) randomize();
  }

  private void randomize() {
    if (inflowPurpose > 125) inflowPurpose = 50 + (new Random()).nextInt(75);
    else inflowPurpose = 125 + (new Random()).nextInt(75);
  }

  private void updateCarsInflow() {

    if (carsInflow != inflowPurpose) {
      if (carsInflow < inflowPurpose) carsInflow++;
      else carsInflow--;
    }
  }

  private void initCars() {

    int space =
            (road.getLength() - Simulation.initialNoOfCars * Car.LENGTH) / Simulation.initialNoOfCars;
    for (int i = 0; i < Simulation.initialNoOfCars; i++) {
      int lane = random.nextInt(road.getNoOfLanes());
      cars.add(
              new Car(
                      road,
                      lane,
                      road.getXStart() + i * (Car.LENGTH + space),
                      0.7 * road.getMaxSpeed(0),
                      fuzzyModel));
    }
  }

  public void addCar() {

    for (int i = 0; i < 4; i++) {
      int lane = random.nextInt(road.getNoOfLanes());
      Car first = road.getFirstOnLane(road.getLane(lane));
      if (first == null || first.getX() > 50) {
        synchronized (this) {
          cars.add(new Car(road, lane, fuzzyModel));
        }
        incrementCounter();
        break;
      }
    }
  }

  public void addExtraCar() {

    Car first = road.getFirstOnLane(road.getLane(-1));
    if (first == null || first.getX() > 30) {
      cars.add(new Car(road, -1, -Car.LENGTH, 45, fuzzyModel));
      incrementExtraCounter();
    }
  }

  public synchronized void removeCars() {
    cars.removeIf(car -> car.getX() > WIDTH + 500 || car.isToRemove());
  }

  public synchronized void render(Graphics2D g2d) {
    road.render(g2d);
    if (road.isExtraLane()) {
      g2d.setColor(Color.BLACK);
      g2d.fillRect(road.getXStart(), road.getY() + RoadLane.WIDTH, 1200, 3);
    }
    synchronized (this) {
      for (Car car : cars) {
        car.render(g2d);
      }
    }

    if (trafficLights != null) trafficLights.render(g2d);
  }
}
