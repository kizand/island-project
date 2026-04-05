package com.javarush.island.organisms;

import com.javarush.island.config.OrganismConfig;
import com.javarush.island.config.Settings;
import com.javarush.island.organisms.herbivore.*;
import com.javarush.island.organisms.insect.Caterpillar;
import com.javarush.island.organisms.plant.Grass;
import com.javarush.island.organisms.predator.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OrganismCreator {

    private static final Class<?>[] types = {
            Bear.class,
            Boa.class,
            Boar.class,
            Buffalo.class,
            Caterpillar.class,
            Deer.class,
            Duck.class,
            Eagle.class,
            Fox.class,
            Goat.class,
            Grass.class,
            Horse.class,
            Mouse.class,
            Rabbit.class,
            Sheep.class,
            Wolf.class
    };

    private static final Map<String, Organism> prototypes = createPrototypes();

    public static String getIcon(String name) {
        Organism organism = prototypes.get(name);
        if (organism == null) {
            throw new IllegalArgumentException("No such organism: " + name);
        }
        return organism.getConfig().getIcon();
    }

    public static Set<String> getPrototypesNames() {
        return prototypes.keySet();
    }

    public static Organism create(String name) {
        Organism organism = prototypes.get(name);
        if (organism == null) {
            throw new IllegalArgumentException("No such organism: " + name);
        }
        return organism.clone();
    }

    private static Map<String, Organism> createPrototypes() {
        Map<String, Organism> organisms = new HashMap<>();
        Map<String, OrganismConfig> configs = Settings.get().getOrganisms();
        for (Class<?> type : types) {
            String name = type.getSimpleName();
            OrganismConfig config = configs.get(name);
            if (config == null) {
                throw new IllegalArgumentException("No such organism: " + name);
            }
            Organism organism = generatePrototype(type, name, config);
            organisms.put(name, organism);
        }
        return organisms;
    }

    private static Organism generatePrototype(Class<?> type, String name, OrganismConfig config) {
        try {
            Constructor<?> constructor = type.getConstructor(String.class, OrganismConfig.class);
            return (Organism) constructor.newInstance(name, config);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
