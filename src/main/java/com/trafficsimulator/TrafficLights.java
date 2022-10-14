package com.trafficsimulator;

import java.awt.*;

public class TrafficLights {

  private final int y;
  private final int width = 40;
  private final int height = 80;
  private final Road road;
  private final Color rDark;
  private final Color yDark;
  private final Color gDark;
  private int x;
  private int imgX;
  private Color light;
  private long time;
  private int redLightPeriod, greenLightPeriod, yellowLightPeriod;
  private boolean prepareToGreen = false;

  public TrafficLights(int x, int redLightPeriod, int greenLightPeriod, Road road) {

    this.x = x;
    this.y = road.getY() + RoadLane.WIDTH + 10;

    this.imgX = x - width / 2;

    this.road = road;

    rDark = new Color(70, 10, 10);
    yDark = new Color(60, 60, 15);
    gDark = new Color(20, 50, 10);

    this.greenLightPeriod = greenLightPeriod * 1000;
    this.redLightPeriod = redLightPeriod * 1000;
    this.yellowLightPeriod = 3 * 1000;

    light = Color.GREEN;
    time = 0;
  }

  public void lightChange(double dt) {

    time += dt;

    if (light == Color.RED) {
      if (time >= redLightPeriod - 1000) prepareToGreen = true;
      if (time >= redLightPeriod) {
        prepareToGreen = false;
        time = 0;
        light = Color.GREEN;
      }
    } else if (light == Color.GREEN) {
      if (time >= greenLightPeriod) {
        time = 0;
        light = Color.YELLOW;
      }
    } else if (light == Color.YELLOW) {
      if (time >= yellowLightPeriod) {
        time = 0;
        light = Color.RED;
      }
    }
  }

  public Color getLight() {
    return light;
  }

  public int getX() {
    return x;
  }

  public void render(Graphics g) {

    Graphics2D g2d = (Graphics2D) g;

    g2d.setColor(Color.BLACK);
    g2d.fillRect(imgX, y, width, height);

    drawRedLight(g2d);
    drawYellowLight(g2d);
    drawGreenLight(g2d);

    g2d.setColor(Color.WHITE);
    g2d.drawLine(x, road.getY() + RoadLane.WIDTH, x, road.getLane(road.getNoOfLanes() - 1).getY());
  }

  private void drawRedLight(Graphics2D g2d) {

    if (light == Color.RED) g2d.setColor(Color.RED);
    else g2d.setColor(rDark);

    g2d.fillOval(imgX + width / 4, y + width / 4, width / 2, width / 2);
  }

  private void drawYellowLight(Graphics2D g2d) {

    if (light == Color.YELLOW || prepareToGreen) g2d.setColor(Color.YELLOW);
    else g2d.setColor(yDark);

    g2d.fillOval(imgX + width / 4, y + 3 * width / 4, width / 2, width / 2);
  }

  private void drawGreenLight(Graphics2D g2d) {

    if (light == Color.GREEN) g2d.setColor(Color.GREEN);
    else g2d.setColor(gDark);

    g2d.fillOval(imgX + width / 4, y + 5 * width / 4, width / 2, width / 2);
  }

  public Rectangle getContour() {
    return new Rectangle(imgX, y, width, height);
  }

  public void drag(double x) {

    if (x >= 800 && x <= 1500) imgX = (int) x;
    else if (x < 800) imgX = 800;
    else imgX = 1500;
  }

  public void changePosition() {
    x = imgX + width / 2;
  }

  public void setGreenLightPeriod(int seconds) {
    greenLightPeriod = seconds * 1000;
  }

  public void setRedLightPeriod(int seconds) {
    redLightPeriod = seconds * 1000;
  }
}
