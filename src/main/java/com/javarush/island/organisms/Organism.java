package com.javarush.island.organisms;

import com.javarush.island.component.Utils;
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

    public void removeFromCell() {
        cell.removeOrganism(this);
    }

    public void movement() {
        if (!isAlive()) {
            return;
        }
        int maxSpeed = config.getMaxSpeed();
        if (maxSpeed == 0) {
            throw new RuntimeException("Max speed has to be greater than 0");
        }
        Set<Cell> visitedCells = new HashSet<>();
        int speed = Utils.random(1, (maxSpeed + 1));
        for (int i = 0; i < speed; i++) {
            List<Cell> nextCells = cell.cloneNextCells();
            Collections.shuffle(nextCells, Utils.getThreadLocalRandom());
            Cell previousCell = cell;
            for (Cell nextCell : nextCells) {
                if (!visitedCells.contains(nextCell)) {
                    if (nextCell.addOrganism(this)) {
                        cell.removeOrganism(this);
                        visitedCells.add(cell);
                        cell = nextCell;
                        break;
                    }
                }
            }
            if (cell == previousCell) {
                break;
            }
        }
    }

    public void eating(List<Organism> disappearedOrganisms) {
        boolean thisOrganismIsDisappeared;
        boolean thisOrganismIsDead;
        boolean thisOrganismIsPrey;
        synchronized (this) {
            thisOrganismIsDisappeared = isDisappeared();
            thisOrganismIsDead = isDead();
            thisOrganismIsPrey = becamePrey;
        }
        if (thisOrganismIsDisappeared) {
            disappearedOrganisms.add(this);
            return;
        }
        if (thisOrganismIsDead || thisOrganismIsPrey) {
            dissolution(disappearedOrganisms);
            return;
        }

        starvation();

        synchronized (this) {
            thisOrganismIsDisappeared = isDisappeared();
            thisOrganismIsDead = isDead();
        }
        if (thisOrganismIsDisappeared) {
            disappearedOrganisms.add(this);
            return;
        }
        if (thisOrganismIsDead) {
            return;
        }

        catchPreys();
    }

    public List<Organism> reproduction(Collection<Organism> organisms) {
        int countOfWellFed = 0;
        for (Organism organism : organisms) {
            if (organism.isAlive() && (organism.saturationKilos > 0) && !Utils.isZero(organism.saturationKilos)) {
                countOfWellFed++;
            }
        }
        if (countOfWellFed > 1) {
            int childrenNum = countOfWellFed / 2;
            return createChildren(childrenNum);
        } else {
            return null;
        }
    }

    protected boolean isDisappeared() {
        return Utils.isZero(weightKilos) || Utils.isNegative(weightKilos);
    }

    protected List<Organism> createChildren(int childrenNum) {
        List<Organism> children = new ArrayList<>(childrenNum);
        Organism prototype = OrganismCreator.create(name);
        for (int i = 0; i < childrenNum; i++) {
            Organism clone = prototype.clone();
            clone.weightKilos = config.getMaxWeight() / 3.0;
            children.add(clone);
        }
        return children;
    }

    private boolean isAlive() {
        return !becamePrey && !isDead() && !isDisappeared();
    }

    private void dissolution(List<Organism> disappearedOrganisms) {
        dyingTickCount++;
        if (dyingTickCount >= dyingTicks) {
            disappearedOrganisms.add(this);
        }
    }

    private void starvation() {
        if (Utils.isZero(saturationKilos)) {
            synchronized (this) {
                if (!becamePrey) {
                    weightKilos -= lossWeightKilos;
                }
            }
        } else {
            double realStarvationKilos;
            double newSaturationKilos = saturationKilos - starvationKilos;
            if (Utils.isZero(newSaturationKilos) || Utils.isNegative(newSaturationKilos)) {
                realStarvationKilos = saturationKilos;
                saturationKilos = 0;
            } else {
                realStarvationKilos = starvationKilos;
                saturationKilos = newSaturationKilos;
            }
            double maxWeight = config.getMaxWeight();
            double weightGain = realStarvationKilos * 0.5;
            synchronized (this) {
                if (!becamePrey) {
                    weightKilos += weightGain;
                    if (weightKilos > maxWeight) {
                        weightKilos = maxWeight;
                    }
                }
            }
        }
    }

    private void catchPreys() {
        double completeSaturation = config.getCompleteSaturation();
        if (completeSaturation == 0) {
            throw new RuntimeException("CompleteSaturation has to be greater than 0");
        }
        Set<String> preysNames = config.getPreys().keySet();
        List<Organism> preys = cell.collectPreys(preysNames);
        Collections.shuffle(preys, Utils.getThreadLocalRandom());
        for (Organism prey : preys) {
            if (!tryToCatchPrey(prey)) {
                continue;
            }
            double needSaturation = completeSaturation - saturationKilos;
            double howMuchFoodWasEaten;
            synchronized (this) {
                if (becamePrey) {
                    return;
                }
            }
            synchronized (prey) {
                if (prey.isDisappeared()) {
                    continue;
                }
                howMuchFoodWasEaten = prey.becomePrey(needSaturation);
            }
            saturationKilos += howMuchFoodWasEaten;
            if (Utils.isEqual(saturationKilos, completeSaturation) || (saturationKilos > completeSaturation)) {
                saturationKilos = completeSaturation;
                return;
            }
        }
    }

    private boolean tryToCatchPrey(Organism prey) {
        if (prey.isWounded()) {
            return true;
        }
        Integer chance = config.getPreys().get(prey.name);
        checkChance(chance, prey.name);
        if (chance < 100) {
            boolean isPreyCaught = Utils.random(1, (100 + 1)) <= chance;
            return isPreyCaught;
        } else {
            return true;
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

    private double becomePrey(double needSaturation) {
        becamePrey = true;
        double newWeightKilos = weightKilos - needSaturation;
        if (Utils.isZero(newWeightKilos) || Utils.isNegative(newWeightKilos)) {
            double oldWeightKilos = weightKilos;
            weightKilos = 0;
            return oldWeightKilos;
        } else {
            weightKilos = newWeightKilos;
            return needSaturation;
        }
    }
}
