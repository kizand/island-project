package com.javarush.island.thread;

import com.javarush.island.component.Children;
import com.javarush.island.component.GlobalOrganismList;
import com.javarush.island.component.OrganismGroup;
import com.javarush.island.component.OrganismGroupsIterator;
import com.javarush.island.island.Island;
import com.javarush.island.organisms.Organism;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class ThreadWorker extends ThreadAction {

    private static Island island;
    private static GlobalOrganismList globalOrganismList;
    private static OrganismGroupsIterator organismGroupsIterator;
    private static OnCompletedGlobalListIteration onCompletedGlobalListIteration;
    private static OnCompletedOrganismGroupsIteration onCompletedOrganismGroupsIteration;
    private static CyclicBarrier tickBarrier;
    private static CyclicBarrier movementBarrier;
    private static CyclicBarrier eatingBarrier;
    private static CyclicBarrier clearingBarrier;
    private static CyclicBarrier reproductionBarrier;
    private static CyclicBarrier populationBarrier;
    private final List<Organism> disappearedOrganisms = new ArrayList<>();
    private final List<Children> childrenList = new ArrayList<>();

    public ThreadWorker(Island island, CyclicBarrier tickBarrier, int threadsNum) {
        if (ThreadWorker.island == null) {
            ThreadWorker.island = island;
        }
        if (globalOrganismList == null) {
            globalOrganismList = island.getGlobalOrganismList();
        }
        if (organismGroupsIterator == null) {
            organismGroupsIterator = island.getOrganismGroupsIterator();
        }
        if (ThreadWorker.tickBarrier == null) {
            ThreadWorker.tickBarrier = tickBarrier;
        }
        if (onCompletedGlobalListIteration == null) {
            onCompletedGlobalListIteration = new OnCompletedGlobalListIteration(globalOrganismList);
        }
        if (onCompletedOrganismGroupsIteration == null) {
            onCompletedOrganismGroupsIteration = new OnCompletedOrganismGroupsIteration(organismGroupsIterator, globalOrganismList);
        }
        if (movementBarrier == null) {
            movementBarrier = new CyclicBarrier(threadsNum, onCompletedGlobalListIteration);
        }
        if (eatingBarrier == null) {
            eatingBarrier = new CyclicBarrier(threadsNum, onCompletedGlobalListIteration);
        }
        if (clearingBarrier == null) {
            clearingBarrier = new CyclicBarrier(threadsNum);
        }
        if (reproductionBarrier == null) {
            reproductionBarrier = new CyclicBarrier(threadsNum, onCompletedOrganismGroupsIteration);
        }
        if (populationBarrier == null) {
            populationBarrier = new CyclicBarrier(threadsNum, onCompletedGlobalListIteration);
        }
    }

    @Override
    protected void doAction() throws Throwable {
        Thread currentThread = Thread.currentThread();
        while (!currentThread.isInterrupted()) {
            tickBarrier.await();

            movement();
            eating();
            reproduction();
        }
    }

    private void movement() throws Throwable {
        while (globalOrganismList.hasNextOrganism()) {
            Organism organism = globalOrganismList.nextOrganism();
            if (organism != null) {
                organism.movement();
            }
        }
        movementBarrier.await();
        Throwable throwable = onCompletedGlobalListIteration.getThrowable();
        if (throwable != null) {
            throw throwable;
        }
    }

    private void eating() throws Throwable {
        while (globalOrganismList.hasNextOrganism()) {
            Organism organism = globalOrganismList.nextOrganism();
            if (organism != null) {
                organism.eating(disappearedOrganisms);
            }
        }
        eatingBarrier.await();
        Throwable throwable = onCompletedGlobalListIteration.getThrowable();
        if (throwable != null) {
            throw throwable;
        }
        clearing();
    }

    private void clearing() throws Throwable {
        for (Organism deadOrganism : disappearedOrganisms) {
            island.remove(deadOrganism);
        }
        disappearedOrganisms.clear();
        clearingBarrier.await();
    }

    private void reproduction() throws Throwable {
        while (organismGroupsIterator.hasNextGroup()) {
            OrganismGroup group = organismGroupsIterator.nextGroup();
            if (group != null) {
                Children children = group.reproduction();
                if (children != null) {
                    if (children.isEmpty()) {
                        throw new IllegalStateException("There are no children groups in the group");
                    }
                    childrenList.add(children);
                }
            }
        }
        reproductionBarrier.await();
        Throwable throwable = onCompletedOrganismGroupsIteration.getThrowable();
        if (throwable != null) {
            throw throwable;
        }
        population();
    }

    private void population() throws Throwable {
        for (Children children : childrenList) {
            island.add(children);
        }
        childrenList.clear();
        populationBarrier.await();
        Throwable throwable = onCompletedGlobalListIteration.getThrowable();
        if (throwable != null) {
            throw throwable;
        }
    }

    private static class OnCompletedGlobalListIteration extends ThreadAction {

        private final GlobalOrganismList globalOrganismList;

        public OnCompletedGlobalListIteration(GlobalOrganismList globalOrganismList) {
            this.globalOrganismList = globalOrganismList;
        }

        @Override
        protected void doAction() {
            globalOrganismList.resetOrganismIndex();
        }
    }

    private static class OnCompletedOrganismGroupsIteration extends ThreadAction {

        private final OrganismGroupsIterator organismGroupsIterator;
        private final GlobalOrganismList globalOrganismList;

        private OnCompletedOrganismGroupsIteration(OrganismGroupsIterator organismGroupsIterator, GlobalOrganismList globalOrganismList) {
            this.organismGroupsIterator = organismGroupsIterator;
            this.globalOrganismList = globalOrganismList;
        }

        @Override
        protected void doAction() throws Throwable {
            organismGroupsIterator.reset();
            globalOrganismList.resetOrganismIndex();
        }
    }
}