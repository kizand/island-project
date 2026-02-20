package com.javarush.island.organisms.plant;

import com.javarush.island.config.OrganismConfig;
import com.javarush.island.organisms.Organism;

import java.util.Collection;
import java.util.List;

public class Plant extends Organism {

    protected Plant(String name, OrganismConfig config) {
        super(name, config);
    }

    protected void growth() {
        double maxWeight = getConfig().getMaxWeight();
        double weightGain = maxWeight / 4.0;
        double newWeight;
        synchronized (this) {
            newWeight = getWeightKilos() + weightGain;
            if (newWeight > maxWeight) {
                newWeight = maxWeight;
            }
            setWeightKilos(newWeight);
        }
    }
}
