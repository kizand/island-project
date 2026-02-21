package com.javarush.island.component;

import com.javarush.island.config.Settings;
import com.javarush.island.island.Cell;
import com.javarush.island.organisms.Organism;

import java.util.Iterator;
import java.util.Map;

public class OrganismGroupsIterator {

    private final int rows = Settings.get().getIslandConfig().getRows();
    private final int cols = Settings.get().getIslandConfig().getCols();
    private final int lastCellIndex = rows * cols - 1;
    private final Cell[][] cells;
    private int cellIndex = 0;
    private Cell cell;
    private Iterator<Map<Integer, Organism>> groupsIterator;

    public OrganismGroupsIterator(Cell[][] cells) {
        this.cells = cells;
    }

    public synchronized boolean hasNextGroup() {
        return cellIndex <= lastCellIndex;
    }

    public void reset() {
        cellIndex = 0;
        cell = cells[0][0];
        groupsIterator = cell.getGroupsIterator();
    }
}
