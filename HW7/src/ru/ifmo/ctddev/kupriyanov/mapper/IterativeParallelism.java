package ru.ifmo.ctddev.kupriyanov.mapper;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by pinkdonut on 08.04.2016.
 */
public class IterativeParallelism implements ScalarIP {
    private ParallelMapper mapper;

    /**
     * Constructor
     * @param mapper
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

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
        if (mapper != null) {
            Function<List<? extends T>, T> maxFunction = o -> o.stream().max(comparator).get();
            return maxFunction.apply(mapper.map(maxFunction, pList));
        } else {
            List<MaxSearcher<T>> runnableList = pList.stream().map(x -> new MaxSearcher<T>(x, comparator)).collect(Collectors.toList());
            startThreads(runnableList);
            return runnableList.stream().map(MaxSearcher::getResult).max(comparator).get();
        }
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
        if (mapper != null) {
            Function<List<? extends T>, Boolean> anyFunction = o -> o.stream().anyMatch(predicate);
            return mapper.map(anyFunction, pList).contains(Boolean.TRUE);
        } else {
            List<AnyChecker<T>> runList = pList.stream().map(x -> new AnyChecker<T>(x, predicate)).collect(Collectors.toList());
            startThreads(runList);
            return runList.stream().map(AnyChecker::getResult).anyMatch(Predicate.isEqual(true));
        }
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

