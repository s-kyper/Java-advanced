package ru.ifmo.ctddev.kupriyanov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by pinkdonut on 28.03.2016.
 */
public class IterativeParallelism implements ScalarIP {

    /**
     * returns max element in list using threadsNum of threads with give comparator
     *
     * @param threadsNum    number of threads
     * @param list  given list
     * @param comparator    given comparator
     * @param <T>   generic type
     * @return  maximum in list
     * @throws InterruptedException
     */
    @Override
    public <T> T maximum(int threadsNum, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        List< List<? extends T> > pList = partition(list, Math.min(threadsNum, list.size()));
        List<MaxSearcher<T>> runList = new ArrayList<>(pList.size());
        pList.forEach(o -> runList.add(new MaxSearcher<T>(o, comparator)));
        startThreads(runList);
        return runList.stream().map(MaxSearcher::getResult).max(comparator).get();
    }

    /**
     * search minimum element in list
     * @param threadsNum
     * @param list
     * @param comparator
     * @param <T>   generic type
     * @return  min element in list
     * @throws InterruptedException
     */
    @Override
    public <T> T minimum(int threadsNum, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threadsNum, list, comparator.reversed());
    }

    /**
     * check if all elements in the list matches predicate
     * @param threadsNum    number of threads
     * @param list  given list
     * @param predicate given predicate
     * @param <T>   generic type
     * @return true- if all elements matches predicate
     * @throws InterruptedException
     */
    @Override
    public <T> boolean all(int threadsNum, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threadsNum, list, predicate.negate());
    }

    /**
     * check if any elements in the list matches predicate
     * @param threadsNum    number of threads
     * @param list  given list
     * @param predicate given predicate
     * @param <T>   generic type
     * @return true- if any element match predicate
     * @throws InterruptedException
     */
    @Override
    public <T> boolean any(int threadsNum, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<List<? extends T>> pList = partition(list, threadsNum);
        List<AnyChecker<T>> runList = new ArrayList<>(pList.size());
        pList.forEach(o -> runList.add(new AnyChecker<T>(o, predicate)));
        startThreads(runList);
        return runList.stream().map(AnyChecker::getResult).anyMatch(Predicate.isEqual(true));
    }

    private <T> List<List<? extends T>> partition(List<? extends T> list, int cnt) {
        int elementsLeft = list.size();
        List<List<? extends T>> newList = new ArrayList<>();
        int leftIndex = 0;
        while (cnt > 0) {
            int rightIndex = leftIndex + elementsLeft/cnt;
            newList.add(list.subList(leftIndex, rightIndex));
            elementsLeft -= rightIndex - leftIndex;
            leftIndex = rightIndex;
            cnt--;
        }
        return newList;
    }

    public static <T> void startThreads(List<? extends Runnable> runList) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        runList.forEach(o -> threadList.add(new Thread(o)));
        for (Runnable runnable : runList) {
            Thread currentThread = new Thread(runnable);
            threadList.add(currentThread);
            currentThread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
    }
}
