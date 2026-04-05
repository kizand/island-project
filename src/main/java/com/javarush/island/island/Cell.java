package com.javarush.island.island;

import com.javarush.island.config.Settings;
import com.javarush.island.organisms.Organism;
import com.javarush.island.organisms.OrganismCreator;

import java.util.*;

public class Cell {

    private final List<Cell> nextCells = new ArrayList<>();
    private final Map<String, Map<Integer, Organism>> groups = new HashMap<>();

    public Cell() {
        Set<String> names = OrganismCreator.getPrototypesNames();
        for (String name : names) {
            groups.put(name, new HashMap<>());
        }
    }

    public Map<String, Map<Integer, Organism>> getGroups() {
        return groups;
    }

    public Iterator<Map<Integer, Organism>> getGroupsIterator() {
        return groups.values().iterator();
    }

    public void updateNextCells(Cell[][] cells, int row, int col) {
        if (row > 0) {
            nextCells.add(cells[row - 1][col]);
        }
        if (col > 0) {
            nextCells.add(cells[row][col - 1]);
        }
        if (row < (cells.length - 1)) {
            nextCells.add(cells[row + 1][col]);
        }
        if (col < (cells[0].length - 1)) {
            nextCells.add(cells[row][col + 1]);
        }
    }

    public List<Cell> cloneNextCells() {
        return new ArrayList<>(nextCells);
    }

    public boolean addOrganism(Organism organism) {
        String organismName = organism.getName();
        Integer organismId = organism.getId();
        int cellCapacity = getCellCapacity(organismName);
        Map<Integer, Organism> group = getGroup(organismName);
        synchronized (group) {
            if (group.size() < cellCapacity) {
                group.put(organismId, organism);
            } else {
                return false;
            }
        }
        return true;
    }

    public List<Organism> addOrganismList(List<Organism> organismList) {
        String organismName = organismList.getFirst().getName();
        int cellCapacity = getCellCapacity(organismName);
        Map<Integer, Organism> group = getGroup(organismName);
        int addedOrganismsCount = 0;
        for (Organism organism : organismList) {
            Integer organismId = organism.getId();
            synchronized (group) {
                if (group.size() < cellCapacity) {
                    group.put(organismId, organism);
                } else {
                    break;
                }
            }
            addedOrganismsCount++;
        }
        if (addedOrganismsCount == organismList.size()) {
            return null;
        } else {
            return organismList.subList(addedOrganismsCount, organismList.size());
        }
    }

    public void removeOrganism(Organism organism) {
        String organismName = organism.getName();
        Integer organismId = organism.getId();
        Map<Integer, Organism> group = groups.get(organismName);
        if (group == null) {
            throw new IllegalArgumentException("Unknown organism name: " + organismName);
        }
        Organism removedOrganism;
        synchronized (group) {
            removedOrganism = group.remove(organismId);
        }
        if (organism != removedOrganism) {
            throw new IllegalArgumentException("Name: " + organism.getName() + ", removed: " + removedOrganism);
        }
    }

    public List<Organism> collectPreys(Set<String> preysNames) {
        List<Organism> preys = new ArrayList<>();
        for (String preyName : preysNames) {
            Map<Integer, Organism> map = groups.get(preyName);
            if (map == null) {
                throw new IllegalArgumentException("Unknown prey name: " + preyName);
            }
            Collection<Organism> organisms;
            synchronized (map) {
                organisms = map.values();
            }
            preys.addAll(organisms);
        }
        return preys;
    }

    private int getCellCapacity(String organismName) {
        Integer cellCapacity = Settings
                .get()
                .getIslandConfig()
                .getCellCapacity()
                .get(organismName);
        if (cellCapacity == null) {
            throw new IllegalArgumentException("Unknown organism name: " + organismName);
        }
        return cellCapacity;
    }

    private Map<Integer, Organism> getGroup(String organismName) {
        Map<Integer, Organism> group = groups.get(organismName);
        if (group == null) {
            throw new IllegalArgumentException("Unknown organism name: " + organismName);
        }
        return group;
    }
}

