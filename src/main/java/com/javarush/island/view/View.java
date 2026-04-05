package com.javarush.island.view;

import com.javarush.island.island.Cell;
import com.javarush.island.island.Island;

import java.util.stream.Collectors;

public abstract class View {

    protected final int cellIconCount;
    protected Island island;

    protected View(int cellIconCount) {
        this.cellIconCount = cellIconCount;
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    public abstract void show();
    public abstract void showThrowable(Throwable throwable);

    protected String getIcons(Cell cell) {
        return cell
                .getGroups()
                .values()
                .stream()
                .filter(map -> !map.isEmpty())
                .sorted((map1, map2) -> map2.size() - map1.size())
                .limit(cellIconCount)
                .map(map -> map.get(map.keySet()
                                .stream()
                                .findFirst()
                                .get())
                        .getConfig()
                        .getIcon())
                .collect(Collectors.joining());
    }
}
