package com.javarush.island.component;

import com.javarush.island.island.Cell;
import com.javarush.island.organisms.Organism;

import java.util.List;

public record Children(List<Organism> list, Cell cell) {

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
