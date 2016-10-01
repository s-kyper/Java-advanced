package ru.ifmo.ctddev.kupriyanov.mapper;

/**
 * Created by pinkdonut on 28.03.2016.
 */

import java.util.List;
import java.util.function.Predicate;

/**
 * check if any element in the list match predicate
 * @param <T>   generic type
 */
public class AnyChecker<T> implements Runnable{
    private List<? extends T> list;
    private Predicate<? super T> predicate;
    private boolean result = false;

    /**
     * constructor
     * @param list
     * @param predicate
     */
    public AnyChecker(List<? extends T> list, Predicate<? super T> predicate) {
        this.list = list;
        this.predicate = predicate;
    }

    /**
     *
     * @return result of matching predicate
     */
    public boolean getResult() {
        return result;
    }

    /**
     * writes max element from list in result
     */
    @Override
    public void run() {
        result = list.stream().anyMatch(predicate);
    }
}
