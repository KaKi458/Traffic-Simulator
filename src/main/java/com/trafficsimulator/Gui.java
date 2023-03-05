//package com.trafficsimulator;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//public class Gui extends JPanel {
//
//  private final TrafficSimulatorApp simulator;
//  private final ButtonGroup maxSpeedGroup;
//  private final ButtonGroup trafficSituationsGroup;
//  private JButton lanesNoIncButton, lanesNoDecButton;
//  private JButton randomizeButton, pauseButton;
//  private JRadioButton noneButton;
//  private JSlider carsInflowSlider,
//      extraInflowSlider,
//      destinationSlider,
//      greenLightSlider,
//      redLightSlider;
//  private ButtonGroup secondMaxSpeedGroup;
//  private JPanel upperPanel, middlePanel, lowerPanel;
//
//  public Gui(TrafficSimulatorApp simulator) {
//
//    super();
//
//    this.simulator = simulator;
//    //this.road = simulator.getRoad();
//    createPanels();
//
//    addRandomizeButton();
//    addLanesNoButtons();
//    addCarsInflowSlider();
//
//    maxSpeedGroup = new ButtonGroup();
//    for (int i = 0; i < 6; i++) {
//      addSpeedLimitButton(i, upperPanel, maxSpeedGroup);
//    }
//
//    addPauseButton();
//
//    trafficSituationsGroup = new ButtonGroup();
////    addTrafficLightsButton();
////    addExtraLaneButton();
////    addCrossroadButton();
////    addSecondSpeedLimitButton();
////    addObstacleButton();
////    addNoneButton();
//  }
//
////  private void addLanesNoButtons() {
////
////    lanesNoIncButton = new JButton("Lanes++");
////    lanesNoDecButton = new JButton("Lanes--");
////
////    lanesNoIncButton.addActionListener(new LanesNoListener());
////    lanesNoDecButton.addActionListener(new LanesNoListener());
////
////    upperPanel.add(lanesNoIncButton);
////    upperPanel.add(lanesNoDecButton);
////  }
//
////  private void addCarsInflowSlider() {
////
////    carsInflowSlider = new JSlider(50, 350, Simulation.initialCarsInflow);
////    carsInflowSlider.setMajorTickSpacing(100);
////    carsInflowSlider.setMinorTickSpacing(50);
////    carsInflowSlider.setPaintTicks(true);
////    carsInflowSlider.setPaintLabels(true);
////    carsInflowSlider.addChangeListener(
////        e -> simulator.getSimulation().changeCarsInflow(carsInflowSlider.getValue()));
////    upperPanel.add(carsInflowSlider);
////  }
//
////  private void addSpeedLimitButton(int i, JPanel panel, ButtonGroup group) {
////
////    int speed;
////    if (group == maxSpeedGroup) speed = 50 + 10 * i;
////    else speed = 30 + 10 * i;
////
////    JRadioButton rButton = new JRadioButton(speed + "km/h");
////
////    rButton.addActionListener(
////        e -> {
////          if (group == maxSpeedGroup) road.setMaxSpeed(speed);
////          else if (group == secondMaxSpeedGroup) {
////            road.setSecondSpeedLimit(speed);
////          }
////        });
////
////    group.add(rButton);
////    panel.add(rButton);
////
////    if (group == maxSpeedGroup && Simulation.initialMaxSpeed == speed) {
////      rButton.setSelected(true);
////    }
////  }
//
////  private void addTrafficLightsButton() {
////
////    JRadioButton rButton = new JRadioButton("Traffic Lights");
////    rButton.addActionListener(
////        e -> {
////          simulator.setDefault();
////          simulator.addTrafficLights();
////          clear();
////          addGreenLightSlider();
////          addRedLightSlider();
////          lowerPanel.validate();
////          lowerPanel.repaint();
////        });
////
////    trafficSituationsGroup.add(rButton);
////    middlePanel.add(rButton);
////  }
//
////  private void addExtraLaneButton() {
////
////    JRadioButton rButton = new JRadioButton("Extra Lane");
////    rButton.addActionListener(
////        e -> {
////          simulator.setDefault();
////          road.addExtraLane(1500);
////          simulator.getSimulation().setExtraLane(true);
////          clear();
////          addExtraInflowSlider();
////        });
////    trafficSituationsGroup.add(rButton);
////    middlePanel.add(rButton);
////  }
////
////  private void addCrossroadButton() {
////
////    JRadioButton rButton = new JRadioButton("Crossroad");
////    rButton.addActionListener(
////        e -> {
////          simulator.setDefault();
////          road.setCrossroad(true);
////          clear();
////          addDestinationSlider();
////        });
////    trafficSituationsGroup.add(rButton);
////    middlePanel.add(rButton);
////  }
////
////  private void addSecondSpeedLimitButton() {
////
////    JRadioButton rButton = new JRadioButton("Second Speed Limit");
////    rButton.addActionListener(
////        e -> {
////          simulator.setDefault();
////          clear();
////          secondMaxSpeedGroup = new ButtonGroup();
////          for (int i = 0; i < 5; i++) {
////            addSpeedLimitButton(i, lowerPanel, secondMaxSpeedGroup);
////
////            lowerPanel.validate();
////            lowerPanel.repaint();
////          }
////        });
////    trafficSituationsGroup.add(rButton);
////    middlePanel.add(rButton);
////  }
////
////  private void addObstacleButton() {
////
////    JRadioButton rButton = new JRadioButton("Obstacle");
////    rButton.addActionListener(
////        e -> {
////          simulator.setDefault();
////          road.addObstacle();
////          clear();
////        });
////    trafficSituationsGroup.add(rButton);
////    middlePanel.add(rButton);
////  }
////
////  private void addNoneButton() {
////
////    JRadioButton rButton = new JRadioButton("None");
////    noneButton = rButton;
////    rButton.addActionListener(
////        e -> {
////          simulator.setDefault();
////          clear();
////        });
////    trafficSituationsGroup.add(rButton);
////    middlePanel.add(rButton);
////    rButton.setSelected(true);
////  }
////
////  private void addExtraInflowSlider() {
////
////    extraInflowSlider = new JSlider(0, 70, 0);
////    extraInflowSlider.setMajorTickSpacing(20);
////    extraInflowSlider.setMinorTickSpacing(5);
////    extraInflowSlider.setPaintTicks(true);
////    extraInflowSlider.setPaintLabels(true);
////    extraInflowSlider.addChangeListener(
////        e -> simulator.getSimulation().changeExtraCarsInflow(extraInflowSlider.getValue()));
////    lowerPanel.add(extraInflowSlider);
////    lowerPanel.validate();
////    lowerPanel.repaint();
////  }
////
////  private void addDestinationSlider() {
////
////    destinationSlider = new JSlider(0, 100, 50);
////    destinationSlider.setMajorTickSpacing(50);
////    destinationSlider.setMinorTickSpacing(10);
////    destinationSlider.setPaintTicks(true);
////    destinationSlider.setPaintLabels(true);
////    destinationSlider.addChangeListener(
////        e -> road.changeDestinationPercentage(destinationSlider.getValue()));
////    lowerPanel.add(destinationSlider);
////    lowerPanel.validate();
////    lowerPanel.repaint();
////  }
////
////  private void addGreenLightSlider() {
////
////    greenLightSlider = new JSlider(10, 30, 10);
////    greenLightSlider.setForeground(Color.WHITE);
////    greenLightSlider.setMajorTickSpacing(5);
////    greenLightSlider.setMinorTickSpacing(1);
////    greenLightSlider.setPaintTicks(true);
////    greenLightSlider.setPaintLabels(true);
////    greenLightSlider.setBackground(Color.GREEN);
////    greenLightSlider.addChangeListener(
////        e -> simulator.getTrafficLights().setGreenLightPeriod(greenLightSlider.getValue()));
////    lowerPanel.add(greenLightSlider);
////  }
////
////  private void addRedLightSlider() {
////
////    redLightSlider = new JSlider(10, 30, 10);
////    redLightSlider.setForeground(Color.WHITE);
////    redLightSlider.setMajorTickSpacing(5);
////    redLightSlider.setMinorTickSpacing(1);
////    redLightSlider.setPaintTicks(true);
////    redLightSlider.setPaintLabels(true);
////    redLightSlider.setBackground(Color.RED);
////    redLightSlider.addChangeListener(
////        e -> simulator.getTrafficLights().setRedLightPeriod(redLightSlider.getValue()));
////    lowerPanel.add(redLightSlider);
////  }
//
//  private void clear() {
//
//    lowerPanel.removeAll();
//    lowerPanel.validate();
//    lowerPanel.repaint();
//  }
//
//  private void createPanels() {
//
//    upperPanel = new JPanel();
//    middlePanel = new JPanel();
//    lowerPanel = new JPanel();
//
//    upperPanel.setBackground(Color.GRAY);
//    middlePanel.setBackground(Color.GRAY);
//    lowerPanel.setBackground(Color.GRAY);
//
//    this.setLayout(new GridLayout(3, 1));
//
//    add(upperPanel);
//    add(middlePanel);
//    add(lowerPanel);
//  }
//
//  private void addPauseButton() {
//
//    pauseButton = new JButton("PAUSE");
//    pauseButton.setBackground(Color.YELLOW);
//
//    pauseButton.addActionListener(e -> clickPauseButton());
//
//    upperPanel.add(pauseButton);
//  }
//
//  public void clickPauseButton() {
//
//    if (pauseButton.getText().equals("PAUSE")) {
//      simulator.getSimulation().pause();
//      pauseButton.setText("RESUME");
//      lanesNoDecButton.setEnabled(false);
//      lanesNoIncButton.setEnabled(false);
//
//    } else {
//      simulator.getSimulation().resume();
//      pauseButton.setText("PAUSE");
//      lanesNoDecButton.setEnabled(true);
//      lanesNoIncButton.setEnabled(true);
//    }
//  }
//
//  private void addRandomizeButton() {
//
//    randomizeButton = new JButton("Start Randomize");
//    randomizeButton.setBackground(Color.YELLOW);
//
//    randomizeButton.addActionListener(
//        e -> {
//          if (randomizeButton.getText().equals("Start Randomize")) {
//            simulator.getSimulation().setRandomize(true);
//            randomizeButton.setText("Stop Randomize");
//          } else if (randomizeButton.getText().equals("Stop Randomize")) {
//            simulator.getSimulation().setRandomize(false);
//            randomizeButton.setText("Start Randomize");
//          }
//        });
//
//    upperPanel.add(randomizeButton);
//  }
//
////  public void updateCarsInflowSlider(int inflow) {
////    carsInflowSlider.setValue(inflow);
////  }
////
////  private class LanesNoListener implements ActionListener {
////
////    @Override
////    public void actionPerformed(ActionEvent e) {
////      if (e.getSource() == lanesNoIncButton) {
////
////        road.incrementNoOfLanes();
////
////        if (road.getNoOfLanes() == 6) lanesNoIncButton.setEnabled(false);
////
////        if (road.getNoOfLanes() > 2) lanesNoDecButton.setEnabled(true);
////      }
////
////      if (e.getSource() == lanesNoDecButton) {
////
////        if (road.getObstacle(null) != null
////            && road.getObstacle(null).getLaneNumber() == road.getNoOfLanes() - 1) {
////          road.removeObstacle();
////          noneButton.setSelected(true);
////        }
////
////        road.decrementNoOfLanes();
////
////        if (road.getNoOfLanes() == 2) lanesNoDecButton.setEnabled(false);
////
////        if (road.getNoOfLanes() < 6) lanesNoIncButton.setEnabled(true);
////      }
////    }
////  }
//}
