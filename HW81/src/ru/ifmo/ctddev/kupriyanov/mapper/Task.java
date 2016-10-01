package ru.ifmo.ctddev.kupriyanov.mapper;

import java.util.function.Function;

/**
 * Created by pinkdonut on 08.04.2016.
 */
public class Task<T, R> {
    private Function<? super T, ? extends R> function;
    private T arg;
    private R res;
    private boolean executed = false;

    /**
     * Constructor
     * @param function
     * @param arg
     */
    public Task(Function<? super T, ? extends R> function, T arg) {
        this.function = function;
        this.arg = arg;
    }

    /**
     * Apply arguments to function
     */
    synchronized void execute() {
        res = function.apply(arg);
        executed = true;
        this.notifyAll();
    }

    synchronized R get() {
        while (!executed) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                return null;
            }
        }
        return res;
    }
}
