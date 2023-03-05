package com.trafficsimulator;

import java.awt.*;
import java.util.*;

public class RoadLane {

  static final int WIDTH = 18;
  private final int xStart;
  private final int xEnd;
  private final int y;
  private final int length;
  private final int number;
  private int side;

  private final LinkedList<Car> cars;

  public RoadLane(int x, int y, int length, int number) {

    xStart = x;
    xEnd = xStart + length;
    this.y = y;
    this.length = length;
    this.number = number;
    side = 0;
    cars = new LinkedList<>();
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

  public int getSide() {
    return side;
  }

  public void setSide(int s) {
    side = s;
  }

  public synchronized Car getFrontCar(Car car) {
    int next = cars.indexOf(car) + 1;
    return cars.getLast() != car ? cars.get(next) : null;
  }

  public synchronized Car getBackCar(Car car) {
    int previous = cars.indexOf(car) - 1;
    return cars.getFirst() != car ? cars.get(previous) : null;
  }

  public synchronized Car getFirstCarFromX(double x) {
    Iterator<Car> it = cars.iterator();
    while (it.hasNext()) {
      Car car = it.next();
      if (car.getX() >= x)
        return car;
    }
    return null;
  }

  public synchronized Car getLastCarBeforeX(double x) {
    Iterator<Car> it = cars.descendingIterator();
    while (it.hasNext()) {
      Car car = it.next();
      if (car.getX() < x)
        return car;
    }
    return null;
  }

  public Car getFirstCar() {
    return cars.getFirst();
  }

  public Car getLastCar() {
    return cars.getLast();
  }
}
