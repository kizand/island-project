package com.javarush.island.config;

import java.util.Map;

public class OrganismConfig {

    private String icon;
    private double maxWeight;
    private int maxSpeed;
    private double completeSaturation;
    private Map<String, Integer> preys;

    public String getIcon() {
        return icon;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public double getCompleteSaturation() {
        return completeSaturation;
    }

    public Map<String, Integer> getPreys() {
        return preys;
    }
}
