package com.javarush.island.island;

import com.javarush.island.component.Children;
import com.javarush.island.component.GlobalOrganismList;
import com.javarush.island.component.OrganismGroupsIterator;
import com.javarush.island.component.Utils;
import com.javarush.island.config.Settings;
import com.javarush.island.organisms.Organism;
import com.javarush.island.organisms.OrganismCreator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Island {

    private final GlobalOrganismList globalOrganismList = new GlobalOrganismList();
    private final Map<String, OrganismCounter> organismsCounters = new HashMap<>();
    private final int rows = Settings.get().getIslandConfig().getRows();
    private final int cols = Settings.get().getIslandConfig().getCols();
    private final Cell[][] cells = new Cell[rows][cols];
    private final OrganismGroupsIterator organismGroupsIterator = new OrganismGroupsIterator(cells);

    public Island() {
        createOrganismsCounters();
        createCells();
        organismGroupsIterator.reset();
    }

    public GlobalOrganismList getGlobalOrganismList() {
        return globalOrganismList;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public OrganismGroupsIterator getOrganismGroupsIterator() {
        return organismGroupsIterator;
    }

    public void populate() {
        Map<String, Integer> population = Settings.get().getIslandConfig().getPopulation();
        population.forEach((name, num) -> populate(OrganismCreator.create(name), num));
    }

    public void add(Children children) {
        List<Organism> list = children.list();
        Cell cell = children.cell();
        List<Organism> extraOrganisms = add(list, cell);
        if (extraOrganisms != null) {
            List<Cell> nextCells = cell.cloneNextCells();
            Collections.shuffle(nextCells, Utils.getThreadLocalRandom());
            for (Cell nextCell : nextCells) {
                extraOrganisms = add(extraOrganisms, nextCell);
                if (extraOrganisms == null) {
                    break;
                }
            }
        }
        int lastIndex = (extraOrganisms == null) ? list.size() - 1 : list.size() - 1 - extraOrganisms.size();
        for (int i = 0; i <= lastIndex; i++) {
            globalOrganismList.safeAdd(list.get(i));
        }
        OrganismCounter organismCounter = organismsCounters.get(list.getFirst().getName());
        if (organismCounter == null) {
            throw new IllegalArgumentException();
        }
        organismCounter.add(lastIndex + 1);
    }

    public void resetGlobalOrganismIndex() {
        globalOrganismList.resetOrganismIndex();
    }

    public void remove(Organism organism) {
        organism.removeFromCell();
        globalOrganismList.remove(organism);
        OrganismCounter organismCounter = organismsCounters.get(organism.getName());
        if (organismCounter == null) {
            throw new IllegalArgumentException();
        }
        organismCounter.decrement();
    }

    public String[] collectStatistics() {
        String[] lines = new String[organismsCounters.size()];
        int i = 0;
        for (OrganismCounter counter : organismsCounters.values()) {
            lines[i++] = counter.getIcon() + "(" + counter.getName() + "):" + counter.getCounter();
        }
        return lines;
    }

    private void createOrganismsCounters() {
        Set<String> names = OrganismCreator.getPrototypesNames();
        for (String name : names) {
            String icon = OrganismCreator.getIcon(name);
            organismsCounters.put(name, new OrganismCounter(icon, name));
        }
    }

    private void createCells() {
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                cells[row][col] = new Cell();
            }
        }
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                cells[row][col].updateNextCells(cells, row, col);
            }
        }
    }

    private void populate(Organism prototype, int population) {
        for (int i = 0; i < population; i++) {
            int rowIndex = Utils.random(0, cells.length);
            int colIndex = Utils.random(0, cells[0].length);
            Organism clone = prototype.clone();
            Cell randomCell = cells[rowIndex][colIndex];
            if (randomCell.addOrganism(clone)) {
                clone.setCell(randomCell);
                globalOrganismList.add(clone);
            } else {
                i--;
            }
        }
        OrganismCounter organismCounter = organismsCounters.get(prototype.getName());
        if (organismCounter == null) {
            throw new IllegalArgumentException();
        }
        organismCounter.add(population);
    }

    private List<Organism> add(List<Organism> organismList, Cell cell) {
        List<Organism> extraOrganisms = cell.addOrganismList(organismList);
        int addedOrganismNum = (extraOrganisms == null)
                ? organismList.size()
                : organismList.size() - extraOrganisms.size();
        for (int i = 0; i < addedOrganismNum; i++) {
            organismList.get(i).setCell(cell);
        }
        return extraOrganisms;
    }

    private static class OrganismCounter {

        private final String icon;
        private final String name;
        private final AtomicInteger counter = new AtomicInteger(0);

        public OrganismCounter(String icon, String name) {
            this.icon = icon;
            this.name = name;
        }

        public String getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public int getCounter() {
            return counter.get();
        }

        public void add(int population) {
            counter.addAndGet(population);
        }

        public void decrement() {
            counter.decrementAndGet();
        }
    }
}
