package com.javarush.island.config;

import java.util.Map;

public class IslandConfig {

    private int rows;
    private int cols;
    private Map<String, Integer> population;
    private Map<String, Integer> cellCapacity;

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Map<String, Integer> getPopulation() {
        return population;
    }

    public Map<String, Integer> getCellCapacity() {
        return cellCapacity;
    }
}
