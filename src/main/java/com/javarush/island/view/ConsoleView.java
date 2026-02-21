package com.javarush.island.view;

import com.javarush.island.config.Settings;
import com.javarush.island.island.Cell;

public class ConsoleView extends View {

    private static final String EMPTY_POSITION = "◾";

    public ConsoleView() {
        super(Settings.get().getConsoleConfig().getCellIconCount());
    }

    @Override
    public void show() {
        showIsland();
        showStatistics();
    }

    @Override
    public void showThrowable(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void showIsland() {
        StringBuilder builder = new StringBuilder();
        Cell[][] cells = island.getCells();
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                appendResidents(builder, cell);
                builder.append(' ');
            }
            builder.append("\n\n");
        }
        System.out.println(builder);
    }

    private void showStatistics() {
        String[] counters = island.collectStatistics();
        for (String counter : counters) {
            System.out.print(counter + ' ');
        }
        System.out.println();
    }

    private void appendResidents(StringBuilder builder, Cell cell) {
        String icons = getIcons(cell);
        builder.append(icons);
        int iconsCount = (int) cell.getGroups()
                .values()
                .stream()
                .filter(map -> !map.isEmpty())
                .limit(cellIconCount)
                .count();
        if (iconsCount < cellIconCount) {
            builder.append(EMPTY_POSITION.repeat(cellIconCount - iconsCount));
        }
    }
}
