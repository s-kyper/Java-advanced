package ru.ifmo.ctddev.kupriyanov.concurrent;

/**
 * Created by pinkdonut on 28.03.2016.
 */

import java.util.Comparator;
import java.util.List;

/**
 * this class search max element in list
 * @param <T>   generic type
 */
public class MaxSearcher<T> implements Runnable{
    private List<? extends T> list;
    private Comparator<? super T> cmp;
    private T result = null;

    /**
     * constructor
     * @param list
     * @param cmp
     */
    public MaxSearcher(List<? extends T> list, Comparator<? super T> cmp) {
        this.list = list;
        this.cmp = cmp;
    }

    /**
     *
     * @return max element in list
     */
    public T getResult() {
        return result;
    }

    /**
     * writes max element from list in result
     */
    @Override
    public void run() {
        result = list.stream().max(cmp).get();
    }
}
