package com.trafficsimulator;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;

public class FuzzyModel {

  private final FunctionBlock fb;

  public FuzzyModel() {

    FIS fis = FIS.load("src/main/resources/fuzzy/acceleration.fcl", true);

    if (fis == null) {
      System.err.println("Can't load file");
      System.exit(1);
    }

    fb = fis.getFunctionBlock(null);
  }

  public double computeValue(double speed, double distanceRatio, double speedDifference) {

    fb.setVariable("speed", speed);
    fb.setVariable("distance", distanceRatio);
    fb.setVariable("speedDifference", speedDifference);
    fb.evaluate();
    fb.getVariable("acceleration").defuzzify();

    return fb.getVariable("acceleration").getValue();
  }

  public void displayCharts() {
    JFuzzyChart.get().chart(fb);
  }
}
