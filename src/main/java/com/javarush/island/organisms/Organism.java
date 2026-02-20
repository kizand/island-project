package com.javarush.island.organisms;

import com.javarush.island.config.GlobalOrganismConfig;
import com.javarush.island.config.OrganismConfig;
import com.javarush.island.config.Settings;
import com.javarush.island.island.Cell;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Organism implements Cloneable {

    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final String name;
    private final OrganismConfig config;
    private final double starvationKilos;
    private final double lossWeightKilos;
    private final double minWeightKilos;
    private final int dyingTicks;
    private Integer id = idCounter.getAndIncrement();
    private Cell cell;
    private int globalListIndex;
    private double weightKilos;
    private double saturationKilos;
    private boolean becamePrey;
    private int dyingTickCount;

    public Organism(String name, OrganismConfig config) {
        this.name = name;
        this.config = config;
        GlobalOrganismConfig globalConfig = Settings.get().getGlobalOrganismConfig();
        starvationKilos = config.getCompleteSaturation() * globalConfig.getStarvation() * 0.01;
        lossWeightKilos = config.getMaxWeight() * globalConfig.getLossWeight() * 0.01;
        minWeightKilos = config.getMaxWeight() / 4.0;
        dyingTicks = globalConfig.getDying();
        weightKilos = config.getMaxWeight();
        saturationKilos = config.getCompleteSaturation() * 0.5;
    }

    public String getName() {
        return name;
    }

    public OrganismConfig getConfig() {
        return config;
    }

    public Integer getId() {
        return id;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public int getGlobalListIndex() {
        return globalListIndex;
    }

    public void setGlobalListIndex(int globalListIndex) {
        this.globalListIndex = globalListIndex;
    }

    public double getWeightKilos() {
        return weightKilos;
    }

    public void setWeightKilos(double weightKilos) {
        this.weightKilos = weightKilos;
    }

    @Override
    public Organism clone() {
        Organism clone;
        try {
            clone = (Organism) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.id = idCounter.getAndIncrement();
        clone.weightKilos = config.getMaxWeight();
        return clone;
    }

    private void dissolution(List<Organism> disappearedOrganisms) {
        dyingTickCount++;
        if (dyingTickCount >= dyingTicks) {
            disappearedOrganisms.add(this);
        }
    }

    private synchronized boolean isWounded() {
        return becamePrey;
    }

    private void checkChance(Integer chance, String preyName) {
        if (chance == null) {
            throw new RuntimeException("Unknown prey " + preyName + ", organism " + name);
        }
        if (chance == 0) {
            throw new RuntimeException("Chance for prey " + preyName + " has to be greater than 0" + ", organism " + name);
        }
    }

    private boolean isDead() {
        return weightKilos < minWeightKilos;
    }
}
