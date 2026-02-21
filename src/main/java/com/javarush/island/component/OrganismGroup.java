package com.javarush.island.component;

import com.javarush.island.island.Cell;
import com.javarush.island.organisms.Organism;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OrganismGroup {

    private final Map<Integer, Organism> group;
    private final Cell cell;

    public OrganismGroup(Map<Integer, Organism> group, Cell cell) {
        this.group = group;
        this.cell = cell;
    }

    public Cell getCell() {
        return cell;
    }
}