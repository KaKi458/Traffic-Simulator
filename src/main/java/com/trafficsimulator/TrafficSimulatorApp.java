package com.trafficsimulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class TrafficSimulatorApp extends JFrame {

  static final int WIDTH = 1800;
  static final int HEIGHT = 800;
  private final Canvas canvas;
  //private final Gui gui;
  private final Simulation simulation;
//  private final Random random;
  private boolean isObstacleDragged = false;
  private boolean isTLDragged = false;
  private TrafficLights trafficLights;
  private Car carInfo = null;

  public TrafficSimulatorApp() {

    setTitle("Simulator");
    setSize(WIDTH, HEIGHT);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    setLayout(new GridLayout(2, 1));

    JPanel visualisation = new JPanel();

    canvas = new Canvas();
    canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT / 2));
    canvas.setMaximumSize(new Dimension(WIDTH, HEIGHT / 2));
    canvas.setMinimumSize(new Dimension(WIDTH, HEIGHT / 2));
//    canvas.addMouseListener(new SimulatorMouseAdapter());
//    canvas.addMouseMotionListener(new SimulatorMouseMotionAdapter());
//    canvas.addKeyListener(new SimulatorKeyAdapter());
    visualisation.add(canvas);
    
    // fuzzyModel.displayCharts();



//    gui = new Gui(this);
    add(visualisation);
//    add(gui);
    pack();

    simulation = new Simulation();
    simulation.setDefault();
    simulation.start();
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        render();
      }
    }, 0, 16);
  }

  public static void main(String[] args) {

    EventQueue.invokeLater(() -> new TrafficSimulatorApp().setVisible(true));
  }

  public void render() {

    Graphics2D g2d;
    BufferStrategy bs = canvas.getBufferStrategy();
    if (bs == null) {
      canvas.createBufferStrategy(3);
      return;
    }
    g2d = (Graphics2D) bs.getDrawGraphics();
    g2d.clearRect(0, 0, WIDTH, HEIGHT);

    simulation.render(g2d);
    displayInfo(g2d);

    bs.show();
    g2d.dispose();
  }

  public Simulation getSimulation() {
    return simulation;
  }

  private void displayInfo(Graphics2D g2d) {

    g2d.setColor(Color.black);
    g2d.drawString("Average speed =  " + simulation.getAverageSpeed(), 200, 300);
    g2d.drawString("Number of cars:  " + simulation.getNoOfCars(), 200, 320);
    g2d.drawString("Cars Inflow:  " + simulation.getCarsInflow(), 200, 50);
    g2d.drawString("Real CarsInflow:  " + simulation.getRealCarsInflow(), 200, 70);
    if (simulation.isExtraLane()) {
      g2d.drawString("Extra CarsInflow:  " + simulation.getExtraCarsInflow(), 350, 50);
      g2d.drawString("Real Extra CarsInflow:  " + simulation.getRealExtraCarsInflow(), 350, 70);
    }
//    if (road.isSecondSpeedLimit()) {
//      g2d.drawString("II Average speed =  " + simulation.getSecondAverageSpeed(), 350, 300);
//    }

    if (carInfo != null) {

      displayCarInfo(g2d);

      if (carInfo.isToRemove() || carInfo.getX() > WIDTH) {
        carInfo.setCarInfo(false);
        carInfo = null;
      }
    }
  }

  private void displayCarInfo(Graphics2D g2d) {

    DecimalFormat df = new DecimalFormat("0.00");

    g2d.drawString("Speed =  " + (int) carInfo.getSpeed(), 1200, 20);
    g2d.drawString("Distance:  " + (int) carInfo.getDistance(), 1200, 40);
    g2d.drawString("Speed Difference:  " + (int) carInfo.getSpeedDifference(), 1200, 60);
    g2d.drawString("Acceleration: ", 1200, 100);

    g2d.drawString("Safe Dist Ratio:  " + df.format(carInfo.getSafeDistRatio()), 1400, 20);
    g2d.drawString("Dynamics:  " + df.format(carInfo.getDynamics()), 1400, 40);
    g2d.drawString("Rush:  " + df.format(carInfo.getRush()), 1400, 60);

    g2d.drawString("X =  " + (int) carInfo.getX(), 1400, 100);

    if (carInfo.getAcceleration() > 0.5) g2d.setColor(Color.blue);
    else if (carInfo.getAcceleration() < -0.5) g2d.setColor(Color.RED);
    else g2d.setColor(Color.GRAY);

    double a = carInfo.getAcceleration();
    if (a < 0 && a > -0.5) a = 0;
    g2d.drawString("" + df.format(a), 1300, 100);
  }

//  public void updateGui(int inflow) {
//    gui.updateCarsInflowSlider(inflow);
//  }

//  public void clickCar(Point clickedPoint, MouseEvent e) {
//
//    for (Car car : cars) {
//      if (car.getContour().contains(clickedPoint)
//          || car.getContour()
//              .intersects(new Rectangle(clickedPoint.x - 10, clickedPoint.y - 10, 30, 30))) {
//
//        if (carInfo != null) {
//          carInfo.setCarInfo(false);
//          carInfo = null;
//        }
//
//        car.setCarInfo(true);
//        carInfo = car;
//        if (e.getButton() == MouseEvent.BUTTON3) {
//          if (!simulation.isPaused()) {
//            carInfo.brake();
//          } else {
//            new CarSetupFrame(carInfo);
//          }
//        }
//        break;
//      }
//    }
//  }
//
//  public void clickObstacle(Point clickedPoint) {
//
//    Obstacle obstacle = road.getObstacle(null);
//    if (obstacle != null) {
//      if (obstacle.getContour().contains(clickedPoint)) {
//        isObstacleDragged = true;
//        obstacle.setDragging();
//      }
//    }
//  }
//
//  public void clickTrafficLights(Point clickedPoint) {
//
//    if (trafficLights != null) {
//      if (trafficLights.getContour().contains(clickedPoint)) {
//        isTLDragged = true;
//      }
//    }
//  }
//
//  private class SimulatorMouseAdapter extends MouseAdapter {
//
//    @Override
//    public void mousePressed(MouseEvent e) {
//
//      Point clickedPoint = e.getPoint();
//
//      clickCar(clickedPoint, e);
//      clickObstacle(clickedPoint);
//      clickTrafficLights(clickedPoint);
//    }
//
//    @Override
//    public void mouseReleased(MouseEvent e) {
//
//      if (isObstacleDragged) {
//        road.getObstacle(null).changePosition(e.getPoint(), cars);
//        isObstacleDragged = false;
//      } else if (isTLDragged) {
//        trafficLights.changePosition();
//        isTLDragged = false;
//      } else road.changeSecondSpeedLimitX();
//    }
//  }
//
//  private class SimulatorMouseMotionAdapter extends MouseMotionAdapter {
//
//    @Override
//    public void mouseDragged(MouseEvent e) {
//
//      if (isObstacleDragged) {
//        road.getObstacle(null).drag(e.getPoint());
//      } else if (isTLDragged) {
//        trafficLights.drag(e.getX());
//      } else road.dragSecondSpeedLimitImg(e.getX());
//    }
//  }
//
//  private class SimulatorKeyAdapter extends KeyAdapter {
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//
//      if (e.getKeyCode() == KeyEvent.VK_SPACE) {
//
//        gui.clickPauseButton();
//      }
//    }
//  }
}
