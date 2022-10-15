package com.trafficsimulator;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class CarSetupFrame extends JDialog {

  private final Car carInfo;
  private final DecimalFormat df = new DecimalFormat("0.00");
  private JSlider carefulnessSlider, dynamicsSlider, rushSlider;
  private JLabel carefulnessLabel, dynamicsLabel, rushLabel;
  private double carefulness, dynamics, rush;

  public CarSetupFrame(Car car) {

    super();

    setTitle("Car Setup");
    setModal(true);
    setAlwaysOnTop(true);
    setModalityType(ModalityType.APPLICATION_MODAL);

    this.carInfo = car;

    carefulness = carInfo.getSafeDistRatio();
    dynamics = carInfo.getDynamics();
    rush = carInfo.getRush();

    setBounds(800, 200, 200, 350);
    setLayout(null);
    setResizable(false);

    addCarefulnessSlider();
    addDynamicsSlider();
    addRushSlider();
    addOkButton();

    setVisible(true);
  }

  private void addCarefulnessSlider() {

    int value = (int) (100 * (carefulness - 0.4) / 0.2);
    carefulnessSlider = new JSlider(0, 100, value);
    carefulnessSlider.setBounds(15, 50, 150, 30);
    this.add(carefulnessSlider);

    carefulnessLabel = new JLabel("Safe Dist Ratio =  " + df.format(carefulness));
    carefulnessLabel.setFont(new Font("Serif", Font.BOLD, 14));
    carefulnessLabel.setBounds(25, 20, 150, 22);
    this.add(carefulnessLabel);

    carefulnessSlider.addChangeListener(
        e -> {
          carefulness = 0.4 + 0.2 * carefulnessSlider.getValue() / 100;
          if (carefulness > 0.6) carefulness = 0.6;

          carefulnessLabel.setText("Safe Dist Ratio =  " + df.format(carefulness));
        });
  }

  private void addDynamicsSlider() {

    int value = (int) (100 * (dynamics - 0.5) / 1.5);
    dynamicsSlider = new JSlider(0, 100, value);
    dynamicsSlider.setBounds(15, 130, 150, 30);
    this.add(dynamicsSlider);

    dynamicsLabel = new JLabel("Dynamics =  " + df.format(dynamics));
    dynamicsLabel.setFont(new Font("Serif", Font.BOLD, 14));
    dynamicsLabel.setBounds(50, 100, 120, 22);
    this.add(dynamicsLabel);

    dynamicsSlider.addChangeListener(
        e -> {
          dynamics = 0.5 + 1.5 * dynamicsSlider.getValue() / 100;
          if (dynamics > 2) dynamics = 2;

          dynamicsLabel.setText("Dynamics =  " + df.format(dynamics));
        });
  }

  private void addRushSlider() {

    int value = (int) (100 * (rush - 0.81) / 0.39);
    rushSlider = new JSlider(0, 100, value);
    rushSlider.setBounds(15, 210, 150, 30);
    this.add(rushSlider);

    rushLabel = new JLabel("Rush =  " + df.format(rush));
    rushLabel.setFont(new Font("Serif", Font.BOLD, 14));
    rushLabel.setBounds(60, 180, 120, 22);
    this.add(rushLabel);

    rushSlider.addChangeListener(
        e -> {
          rush = 0.81 + 0.39 * rushSlider.getValue() / 100;
          if (rush > 1.2) rush = 1.2;

          rushLabel.setText("Rush =  " + df.format(rush));
        });
  }

  private void addOkButton() {

    JButton okButton = new JButton();
    okButton.setFont(new Font("Serif", Font.BOLD, 10));
    okButton.setBounds(100, 260, 70, 30);
    okButton.setText("OK");

    okButton.addActionListener(
        e -> {
          carInfo.setSafeDistRatio(carefulness);
          carInfo.setDynamics(dynamics);
          carInfo.setRush(rush);
          dispose();
        });

    this.add(okButton);
  }
}
