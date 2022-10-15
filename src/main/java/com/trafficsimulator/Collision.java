package com.trafficsimulator;

import java.util.ArrayList;
import java.util.List;

public class Collision {

  private static final List<Collision> collisions = new ArrayList<>();
  private final Car car;
  private final long time;

  public Collision(Car car) {

    this.car = car;
    time = System.currentTimeMillis();
  }

  public static void checkCollisions(List<Car> cars, Obstacle obstacle) {

    for (Car car1 : cars) {
      for (Car car2 : cars) {
        if (car1 != car2 && car1.getContour().intersects(car2.getContour())) {

          if (!car1.isCollision()) {
            collisions.add(new Collision(car1));
            car1.setCollision();
          }
          if (!car2.isCollision()) {
            car2.setCollision();
            collisions.add(new Collision(car2));
          }
        }
      }
      if (obstacle != null
          && !obstacle.isDragging()
          && car1.getContour().intersects(obstacle.getContour())) {
        if (!car1.isCollision()) {
          collisions.add(new Collision(car1));
          car1.setCollision();
        }
      }
    }
    for (Collision collision : collisions) {
      if (System.currentTimeMillis() - collision.time > 5000) {
        collision.car.setToRemove();
      }
    }
  }
}
