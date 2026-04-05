package com.javarush.island.component;

import com.javarush.island.organisms.Organism;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalOrganismList {

    private final List<Organism> list = new ArrayList<>();
    private final AtomicInteger organismIndex = new AtomicInteger(0);
    private final AtomicBoolean endOfList = new AtomicBoolean(false);

    public boolean hasNextOrganism() {
        return organismIndex.get() < list.size();
    }

    public Organism nextOrganism() {
        int index = organismIndex.getAndIncrement();
        if (index < list.size()) {
            return list.get(index);
        } else {
            return null;
        }
    }

    public void resetOrganismIndex() {
        organismIndex.set(0);
        endOfList.set(false);
    }

    public void add(Organism organism) {
        list.add(organism);
        organism.setGlobalListIndex(list.size() -1);
    }

    public void safeAdd(Organism organism) {
        if (!endOfList.get()) {
            int listSize;
            synchronized (list) {
                listSize = list.size();
            }
            while (true) {
                int index = organismIndex.getAndIncrement();
                if (index < listSize) {
                    Organism item;
                    synchronized (list) {
                        item = list.get(index);
                    }
                    if (item == null) {
                        synchronized (list) {
                            list.set(index, organism);
                        }
                        organism.setGlobalListIndex(index);
                        return;
                    }
                } else {
                    endOfList.set(true);
                    break;
                }
            }
        }
        int listSize;
        synchronized (list) {
            list.add(organism);
            listSize = list.size();
        }
        organism.setGlobalListIndex(listSize - 1);
    }

    public void remove(Organism organism) {
        Organism removedOrganism = list.set(organism.getGlobalListIndex(), null);
        if (organism != removedOrganism) {
            throw new IllegalArgumentException();
        }
    }

    public int size() {
        return list.size();
    }
}
