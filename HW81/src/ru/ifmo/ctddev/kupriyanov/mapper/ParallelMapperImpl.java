package ru.ifmo.ctddev.kupriyanov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by pinkdonut on 08.04.2016.
 */

public class ParallelMapperImpl implements ParallelMapper {
    private ArrayList<Thread> threadPool;
    private Queue<Task> taskQueue;

    /**
     * constructor of threads worker
     * @param threadsNum    number of threads
     */
    public ParallelMapperImpl(int threadsNum) {
        threadPool = new ArrayList<>(threadsNum);
        taskQueue = new ArrayDeque<>();
        for (int i = 0; i < threadsNum; i++) {
            Thread thread = new Thread(new Worker(taskQueue));
            thread.start();
            threadPool.add(thread);
        }
    }

    /**
     * calculates function from all arguments
     * @param function  given function
     * @param list  arguments
     * @param <T>   arguments type
     * @param <R>   return type
     * @return  result of calculating
     * @throws InterruptedException
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<Task<T, R>> tasks = list.stream().map(arg -> new Task<T, R>(function, arg)).collect(Collectors.toList());
        synchronized (taskQueue) {
            taskQueue.addAll(tasks);
            taskQueue.notifyAll();
        }

        return tasks.stream().map(Task::get).collect(Collectors.toList());
    }

    /**
     * close all worker threads
     * @throws InterruptedException
     */
    @Override
    public void close() throws InterruptedException {
        threadPool.forEach(Thread::interrupt);
    }
}