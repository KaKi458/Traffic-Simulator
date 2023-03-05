package com.trafficsimulator;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Car {

  static final int LENGTH = 12;
  static final int WIDTH = 6;
  private final double minSafeDist = (2.0 * LENGTH) / Simulation.pixelsPerMeter;
  private double x, y;
  private double speed;
  private double acceleration;
  private FuzzyModel fuzzyModel;
  private Road road;
  private RoadLane currentLane;
  private Car neighbourFront,
      neighbourBackLeft,
      neighbourBackRight,
      neighbourFrontLeft,
      neighbourFrontRight;
  private double distance, distFrontLeft, distFrontRight, distBackLeft, distBackRight;
  private double speedDifference,
      speedDiffFrontLeft,
      speedDiffFrontRight,
      speedDiffBackLeft,
      speedDiffBackRight;
  private double safeDistRatio;
  private double dynamics;
  private double rush;

  private RoadLane lastLane;
  private Car lastNeighbourFront;

  private int isLaneChanging = 0;
  private boolean isCollision = false;
  private boolean isBrake = false;
  private boolean zipperWant = false;
  private boolean zipperMerge = false;
  private boolean canZipper = true;
  private boolean isToRemove = false;
  private boolean isCarInfo = false;
  private Color trafficLight = null;
  private int yellowStop = 0;
  private int destination = 0;

  private long time, time2;
  private Color color;
  private Random random;

  public Car(Road road, int lane, FuzzyModel fuzzyModel) {

    initCar(road, lane, fuzzyModel);

    Car first = road.getFirstOnLane(currentLane);
    this.x = -LENGTH;

    if (first == null || first.getX() > 150) {

      if (first != null && first.getSpeed() < 50) this.speed = first.getSpeed() + 10;
      else this.speed = road.getMaxSpeed(x);
    } else {
      this.speed = first.getSpeed();
    }
  }

  public Car(Road road, int lane, double x, double speed, FuzzyModel fuzzyModel) {

    initCar(road, lane, fuzzyModel);
    this.x = x;
    this.speed = speed;
  }

  private void initCar(Road road, int lane, FuzzyModel fuzzyModel) {

    this.road = road;
    this.currentLane = road.getLane(lane);
    this.fuzzyModel = fuzzyModel;
    this.y = currentLane.getY() + (RoadLane.WIDTH - WIDTH) / 2.0;

    acceleration = 0;

    random = new Random();

    setDestination(road.getDestinationPercentage());

    safeDistRatio = 0.5 + 0.1 * random.nextGaussian();
    if (safeDistRatio < 0.3) {
      safeDistRatio = 0.3;
    }
    if (safeDistRatio > 0.6) {
      safeDistRatio = 0.6;
    }

    rush = 1 + 0.15 * random.nextGaussian();
    if (rush < 0.81) {
      rush = 0.81;
    }
    if (rush > 1.2) {
      rush = 1.2;
    }

    dynamics = 0.5 + 1.5 * random.nextDouble();
  }

  public void moveX() {

    double t = Simulation.timeStep;
    double v = speed * Simulation.pixelsPerMeter / 3600; // [pixels] / [ms]
    double a = acceleration * Simulation.pixelsPerMeter / 1000000; // [pixels] / [ms^2]

    x += v * t + 0.5 * a * t * t;
    speed += (acceleration * 3.6) * (0.001 * t); //  [m/s] to [km/h]
  }

  public void moveY() {

    if (isLaneChanging == 1) {
      y -= 0.35;
    } else if (isLaneChanging == -1) {
      y += 0.35;
    }
    if (y <= (currentLane.getY() + (RoadLane.WIDTH - WIDTH) / 2.0 + 0.3)
        && y >= currentLane.getY() + (RoadLane.WIDTH - WIDTH) / 2.0 - 0.3) {

      if (destination == 0) {
        isLaneChanging = 3;
        time = System.currentTimeMillis();
      } else isLaneChanging = 0;
    }
  }

  public void accelerate() {

    neighbourFront = road.getNeighbourFront(this);
    distance = 1000;
    speedDifference = 1000;

    if (neighbourFront != null) {
      distance = (neighbourFront.getX() - (x + LENGTH)) / Simulation.pixelsPerMeter;
      speedDifference = neighbourFront.getSpeed() - speed;
    }

    if (road.getObstacle(this) != null) {
      Obstacle obstacle = road.getObstacle(this);
      if (obstacle.getLane() == currentLane
          && x < obstacle.getX()
          && (obstacle.getX() - x) / Simulation.pixelsPerMeter < 150
          && (neighbourFront == null || neighbourFront.getX() > obstacle.getX())) {
        distance = (obstacle.getX() - (x + LENGTH)) / Simulation.pixelsPerMeter;
        speedDifference = -speed;
      }
    }

//    if (road.getTrafficLights() != null && trafficLight != null) {
//
//      double tLX = road.getTrafficLights().getX();
//      if ((neighbourFront != null && neighbourFront.getX() > tLX)
//          || (neighbourFront == null && tLX > x)) {
//
//        if (trafficLight == Color.RED) {
//
//          distance = (tLX - (x + 2 * LENGTH)) / Simulation.pixelsPerMeter;
//          speedDifference = -speed;
//        }
//      }
//    }

    double distanceRatio = 0;
    if (speed > 0) {
      distanceRatio = distance / speed - safeDistRatio;
    }

    acceleration = fuzzyModel.computeValue(speed, distanceRatio, speedDifference);

    if (acceleration > 0) {
      acceleration = acceleration * dynamics;
    }

    int maxSpeed = road.getMaxSpeed(x);
    if (currentLane.getNumber() == -1 && x < 900) maxSpeed = 50;

    if (speed > rush * maxSpeed) {

      double a = -2;
      if (acceleration > a) {
        acceleration = a;
      }
    }
  }

  public void correct() {

    if (zipperMerge && acceleration > -2) {
      acceleration = -2;
    }

    if (distance < 5) {
      acceleration = -6;
    }

    if (isBrake) {
      acceleration = -6;
      if (speed <= 10) isBrake = false;
    }

//    if (road.getTrafficLights() != null && trafficLight != null) {
//
//      double tLDistance = (road.getTrafficLights().getX() - x) / Simulation.pixelsPerMeter;
//
//      if (yellowStop == 1 && tLDistance <= 200) {
//
//        double yellowTime = road.getMaxSpeed(x) / 1.5;
//        double acc = speed / yellowTime;
//
//        if (!(acceleration < -acc)) {
//          acceleration = -acc;
//        }
//
//        if (tLDistance < 5) {
//          acceleration = -6;
//        }
//      }
//
//      if (trafficLight == Color.RED) {
//
//        if ((x + 2 * LENGTH) > road.getTrafficLights().getX()) {
//          acceleration = -6;
//        }
//      }
//    }

    if (speed < 0) {
      speed = 0;
    }

    if (speed == 0 && acceleration < 0) {
      acceleration = 0;
    }

    if (isCollision) {
      speed = 0;
      acceleration = 0;
      isLaneChanging = 0;
    }
  }

  private double getSafeDistance(double speedDifference) {

    double safeSpeedRatio = 0.6;

    return Math.max(safeSpeedRatio * speedDifference, minSafeDist);
  }

  private boolean isLaneChangePossible() {

    boolean decision = false;

    boolean basicCondition = isLaneChanging == 0;
    boolean[] conditions = new boolean[7];
    conditions[0] = distance < 30 && speedDifference <= -10;
    conditions[1] = distance < 8 && speedDifference <= 0;
    conditions[2] =
        road.getObstacle(this) != null
            && road.getObstacle(this).getLane() == currentLane
            && x < road.getObstacle(this).getX()
            && road.getObstacle(this).getX() - x <= 50;
    conditions[3] = currentLane.getNumber() == -1;
    conditions[4] = zipperMerge;
    conditions[5] = destination != currentLane.getSide();
    conditions[6] =
        neighbourFrontRight != null && neighbourFrontRight.getLaneNumber() == -1 && x > 1000;

    for (boolean condition : conditions) {
      if (condition) {
        decision = basicCondition;
        break;
      }
    }

    return decision;
  }

  private boolean isHigherSpeedPossible() {

    boolean condition1 = isLaneChanging == 0;
    boolean condition2 = speed < 0.7 * rush * road.getMaxSpeed(x);
    boolean condition3 =
        speed < road.getPossibleSpeed(currentLane.getNumber() + 1, x)
            || speed < road.getPossibleSpeed(currentLane.getNumber() - 1, x);

    return condition1 && condition2 && condition3;
  }

  public void changeLane() {

    findNeighbours();
    updateLaneChangingStatus();

    if (isLaneChangePossible()) {

      if (tryTurnLeft()) {
        turnLeft();
      } else if (tryTurnRight()) {
        turnRight();
      } else {
        checkZipperWant();
      }
    } else if (isHigherSpeedPossible() && random.nextDouble() >= 0.7) {

      if (tryTurnLeft() && speed < road.getPossibleSpeed(currentLane.getNumber() + 1, x)) {
        turnLeft();
      } else if (tryTurnRight() && speed < road.getPossibleSpeed(currentLane.getNumber() - 1, x)) {
        turnRight();
      }
    }
  }

  private void findNeighbours() {

    lastNeighbourFront = neighbourFront;

    neighbourFront = road.getNeighbourFront(this);
    neighbourBackLeft = road.getNeighbourLeftBack(this);
    neighbourFrontLeft = road.getNeighbourLeftFront(this);
    neighbourBackRight = road.getNeighbourRightBack(this);
    neighbourFrontRight = road.getNeighbourRightFront(this);

    Obstacle obstacle = road.getObstacle(this);

    if (neighbourBackLeft != null) {
      speedDiffBackLeft = neighbourBackLeft.getSpeed() - speed;
      distBackLeft = (x - (neighbourBackLeft.getX() + LENGTH)) / Simulation.pixelsPerMeter;
    }

    if (neighbourFrontLeft != null) {
      speedDiffFrontLeft = neighbourFrontLeft.getSpeed() - speed;
      distFrontLeft = (neighbourFrontLeft.getX() - (x + LENGTH)) / Simulation.pixelsPerMeter;
    }

    if (neighbourBackRight != null) {
      speedDiffBackRight = neighbourBackRight.getSpeed() - speed;
      distBackRight = (x - (neighbourBackRight.getX() + LENGTH)) / Simulation.pixelsPerMeter;
    }

    if (neighbourFrontRight != null) {
      speedDiffFrontRight = neighbourFrontRight.getSpeed() - speed;
      distFrontRight = (neighbourFrontRight.getX() - (x + LENGTH)) / Simulation.pixelsPerMeter;
    }

    if (neighbourFront != null) {
      speedDifference = neighbourFront.getSpeed() - speed;
      distance = ((neighbourFront.getX() - (x + LENGTH))) / Simulation.pixelsPerMeter;
    }

    if (obstacle != null
        && obstacle.getLane() == currentLane
        && (neighbourFront == null || obstacle.getX() < neighbourFront.getX())) {
      distance = (obstacle.getX() - (x + LENGTH)) / Simulation.pixelsPerMeter;
      speedDifference = -speed;
    }
  }

  private void updateLaneChangingStatus() {

    if (isLaneChanging == 3 && System.currentTimeMillis() - time >= 3000) {
      isLaneChanging = 0;
    }

    if (currentLane == road.getLane(-1) && x < 1200) {
      isLaneChanging = 3;
    }

    if (lastNeighbourFront != neighbourFront || lastLane != currentLane) {
      canZipper = false;
      zipperMerge = false;
      time2 = System.currentTimeMillis();
    }
    lastLane = currentLane;
  }

  private void checkZipperWant() {

    if (speed < 5
        && trafficLight != Color.RED
        && (road.getObstacle(this) != null && road.getObstacle(this).getLane() == currentLane)) {
      // && ((obstacle != null && obstacle.getLane() == currentLane) || (neighbourBackRight != null
      // && neighbourBackRight.getSpeed() > speed + 10) || (neighbourBackLeft != null &&
      // neighbourBackLeft.getSpeed() > speed + 10))){
      zipperWant = true;
    }
  }

  private boolean tryTurnLeft() {

    boolean decision = false;

    Obstacle obstacle = road.getObstacle(this);

    boolean condition1 = currentLane.getNumber() < road.getNoOfLanes() - 1;
    boolean condition2 =
        !(obstacle != null
            && obstacle.getLaneNumber() == currentLane.getNumber() + 1
            && x < obstacle.getX());
    boolean condition3 = !(destination == 1);

    if (condition1 && condition2 && condition3) {
      if (neighbourFrontLeft == null && neighbourBackLeft == null) decision = true;
      else if (neighbourFrontLeft == null) {

        if ((speedDiffBackLeft <= 0 && distBackLeft > minSafeDist)
            || (speedDiffBackLeft > 0 && distBackLeft >= getSafeDistance(speedDiffBackLeft)))
          decision = true;
      } else if (neighbourBackLeft == null) {

        if ((speedDiffFrontLeft >= 0 && distFrontLeft > minSafeDist)
            || (speedDiffFrontLeft < 0
                && distFrontLeft >= getSafeDistance(Math.abs(speedDiffFrontLeft)))) decision = true;
      } else if (neighbourBackLeft != null) {

        if (((speedDiffBackLeft <= 0 && distBackLeft > minSafeDist)
                || (speedDiffBackLeft > 0 && distBackLeft >= getSafeDistance(speedDiffBackLeft)))
            && ((speedDiffFrontLeft >= 0 && distFrontLeft > minSafeDist)
                || (speedDiffFrontLeft < 0
                    && distFrontLeft >= getSafeDistance(Math.abs(speedDiffFrontLeft)))))
          decision = true;
      }
    }

    return decision;
  }

  private boolean tryTurnRight() {

    boolean decision = false;

    Obstacle obstacle = road.getObstacle(this);

    boolean condition1 = currentLane.getNumber() > 0;
    boolean condition2 =
        !(obstacle != null
            && obstacle.getLaneNumber() == currentLane.getNumber() - 1
            && x < obstacle.getX());
    boolean condition3 = !(destination == -1);

    if (condition1 && condition2 && condition3) {

      if (neighbourFrontRight == null && neighbourBackRight == null) decision = true;
      else if (neighbourFrontRight == null) {

        if ((speedDiffBackRight <= 0 && distBackRight > minSafeDist)
            || (speedDiffBackRight > 0 && distBackRight >= getSafeDistance(speedDiffBackRight)))
          decision = true;
      } else if (neighbourBackRight == null) {

        if ((speedDiffFrontRight >= 0 && distFrontRight > minSafeDist)
            || (speedDiffFrontRight < 0
                && distFrontRight >= getSafeDistance(Math.abs(speedDiffFrontRight))))
          decision = true;
      } else if (neighbourBackRight != null) {

        if (((speedDiffBackRight <= 0 && distBackRight > minSafeDist)
                || (speedDiffBackRight > 0 && distBackRight >= getSafeDistance(speedDiffBackRight)))
            && ((speedDiffFrontRight >= 0 && distFrontRight > minSafeDist)
                || (speedDiffFrontRight < 0
                    && distFrontRight >= getSafeDistance(Math.abs(speedDiffFrontRight)))))
          decision = true;
      }
    }

    return decision;
  }

  private void turnLeft() {

    currentLane = road.getLane(currentLane.getNumber() + 1);
    isLaneChanging = 1;
  }

  private void turnRight() {

    currentLane = road.getLane(currentLane.getNumber() - 1);
    isLaneChanging = -1;
  }

  public void render(Graphics g) {

    Graphics2D g2d = (Graphics2D) g;
    setColor();
    g2d.setColor(color);
    g2d.fill(new Rectangle((int) x, (int) y, LENGTH, WIDTH));
  }

  public double getX() {
    return x;
  }

  public double getSpeed() {
    return speed;
  }

  public double getAcceleration() {
    return acceleration;
  }

  public RoadLane getLane() {
    return currentLane;
  }

  public int getLaneNumber() {
    return currentLane.getNumber();
  }

  public Car getNeighbourFront() {
    return neighbourFront;
  }

  public Car getNeighbourLeftBack() {
    return neighbourBackLeft;
  }

  public Car getNeighbourRightBack() {
    return neighbourBackRight;
  }

  public Car getNeighbourLeftFront() {
    return neighbourFrontLeft;
  }

  public Car getNeighbourRightFront() {
    return neighbourFrontRight;
  }

  public double getDistance() {
    return distance;
  }

  public double getSpeedDifference() {
    return speedDifference;
  }

  public Rectangle getContour() {
    return new Rectangle((int) x, (int) y, LENGTH, WIDTH);
  }

  public void setCollision() {

    isCollision = true;
    color = Color.blue;
  }

  public boolean isCollision() {
    return isCollision;
  }

  public int isLaneChanging() {
    return isLaneChanging;
  }

  private void setColor() {

    if (isCarInfo) {
      color = Color.BLACK;
    } else {
      int maxSpeed = road.getMaxSpeed(x);
      if (speed > 1.1 * maxSpeed) color = Color.MAGENTA;
      else if (speed >= 0.8 * maxSpeed) color = Color.GREEN;
      else if (speed < 0.8 * maxSpeed && speed >= 0.6 * maxSpeed) color = Color.YELLOW;
      else if (speed < 0.6 * maxSpeed && speed >= 0.3 * maxSpeed) color = Color.ORANGE;
      else if (speed < 0.3 * maxSpeed) color = Color.RED;

      if (destination == 1) color = Color.pink;
      else if (destination == -1) color = Color.CYAN;
    }

    if (isCollision) {
      color = Color.BLUE;
    }
  }

  public void setToRemove() {
    isToRemove = true;
  }

  public boolean isToRemove() {
    return isToRemove;
  }

  public void setCarInfo(boolean w) {
    isCarInfo = w;
  }

  public boolean readZipperWant() {
    return zipperWant;
  }

  public void decideZipperMerge(List<Car> cars) {

    if (canZipper) {
      zipperMerge = false;
      for (Car car : cars) {
        if (car.getLaneNumber() == currentLane.getNumber() - 1
            && car.getX() > x + 5 * LENGTH
            && car.getX() < x + 10 * LENGTH
            && car.getSpeed() < speed) {
          if (car.readZipperWant()) {
            double chances = random.nextDouble();
            if (chances > 0.3) zipperMerge = true;
            break;
          }
        }
        if (!zipperMerge) {
          if (car.getLaneNumber() == currentLane.getNumber() + 1
              && car.getX() > x + 5 * LENGTH
              && car.getX() < x + 10 * LENGTH
              && car.getSpeed() < speed) {
            if (car.readZipperWant()) {
              double chances = random.nextDouble();
              if (chances > 0.3) zipperMerge = true;
              break;
            }
          }
        }
      }
    } else {
      if (System.currentTimeMillis() - time2 >= 10000) {
        canZipper = true;
      }
    }
  }

//  public void lookTrafficLights() {
//
//    TrafficLights tL = road.getTrafficLights();
//
//    if (tL != null && tL.getX() > x) {
//      trafficLight = tL.getLight();
//    } else {
//      trafficLight = null;
//      yellowStop = 0;
//    }
//
//    if (trafficLight == Color.YELLOW && yellowStop == 0) {
//
//      double yellowDistance = (road.getAverageSpeed() * 5 / 18) * 5;
//
//      if (x <= tL.getX() - yellowDistance * Simulation.pixelsPerMeter) {
//        yellowStop = 1;
//      } else yellowStop = -1;
//    }
//
//    if (trafficLight == Color.RED) yellowStop = 0;
//  }

  public int getDestination() {
    return destination;
  }

  public void setDestination(int destinationPercentage) {

    if (road.isCrossroad()) {

      int k = random.nextInt(100);
      if (k >= destinationPercentage) destination = 1;
      else destination = -1;
    }
  }

  public void clearDestination() {
    destination = 0;
  }

  public double getSafeDistRatio() {
    return safeDistRatio;
  }

  public void setSafeDistRatio(double ratio) {

    if (ratio >= 0.4 && ratio <= 0.6) safeDistRatio = ratio;
  }

  public double getDynamics() {
    return dynamics;
  }

  public void setDynamics(double ratio) {

    if (ratio >= 0.5 && ratio <= 2) dynamics = ratio;
  }

  public double getRush() {
    return rush;
  }

  public void setRush(double ratio) {

    if (ratio > 0.8 && ratio <= 1.2) rush = ratio;
  }

  public void brake() {
    isBrake = true;
  }
}
