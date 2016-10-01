package ru.ifmo.ctddev.kupriyanov.mapper;

import java.util.Queue;

/**
 * Created by pinkdonut on 08.04.2016.
 */
public class Worker implements Runnable {
    private Queue<Task> taskQueue;

    /**
     * Constructor
     * @param taskQueue
     */
    public Worker(Queue<Task> taskQueue) {
        this.taskQueue = taskQueue;
    }

    /**
     * Takes a task from queue and execute it
     * If the task ueue is empty-waits for notification
     * Method ends, if the current thread is empty
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Task task;
            synchronized (taskQueue) {
                while (taskQueue.isEmpty()) {
                    try {
                        taskQueue.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                task = taskQueue.poll();
            }
            task.execute();
        }
    }
}
