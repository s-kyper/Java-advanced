import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Created by pinkdonut on 31.05.2016.
 */
public class BeetleRun {

    // constant
    final int MAXCNT = 100000;

    private Judge judge;
    private ArrayList<Beetle> beetles;

    /*
        first arg is N-number of beetles(threads). second arg is M-count of stages
     */
    public static void main(String[] args) {
        if (args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Wrong usage of args");
            return;
        }
        int n = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        new BeetleRun().start(n, m);
    }

    /*
        Starts BeetleRun game
     */
    private void start(int n, int m) {
        judge = new Judge(n, m);
        beetles = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            beetles.add(new Beetle(i, m));
        }
        Thread judgeThread = new Thread(judge);
        judgeThread.start();
        ArrayList<Thread> beetlesThread = new ArrayList<>();
        for (int i = 0; i < n; i ++) {
            beetlesThread.add(new Thread(beetles.get(i)));
            beetlesThread.get(i).start();
        }

        try {
            judgeThread.join();
            for (Thread t : beetlesThread) {
                t.join();
            }
        } catch (InterruptedException e) { // ignore
        }
    }

    /*
        private class Beetle
     */
    private class Beetle extends Thread {
        int[] numbers;
        int num;
        int stagesCnt;
        long time;
        boolean isReady;

        Beetle(int num, int stagesCnt) {
            this.num = num;
            this.stagesCnt = stagesCnt;
            this.numbers = new int[MAXCNT];
            this.isReady = false;
        }

        public void run() {
            for (int i = 0; i < stagesCnt; i++) {
                waitUntilDone();
                Random random = new Random();
                for (int j = 0; j < MAXCNT; j++) {
                    numbers[j] = random.nextInt();
                }
                judge.ready();
                Arrays.sort(numbers);
                time = System.nanoTime();
                judge.ready();
                isReady = false;
            }
        }

        synchronized void waitUntilDone() {
            while (!isReady) {
                try {
                    this.wait();
                } catch (InterruptedException e) { // ignore
                }
            }
        }

        public synchronized void ready() {
            isReady = true;
            notifyAll();
        }
    }

    /*
        private class Judge
     */
    private class Judge extends Thread {
        int beetlesCnt;
        int registeredCnt;
        int stagesCnt;
        boolean isReady;
        int[] sum;
        int[] stage;

        Judge(int beetlesCnt, int stagesCnt) {
            this.beetlesCnt = beetlesCnt;
            this.stagesCnt = stagesCnt;
            this.registeredCnt = 0;
            this.sum = new int[beetlesCnt];
            Arrays.fill(sum, 0);
            this.stage = new int[beetlesCnt];
            Arrays.fill(stage, 0);
            this.isReady = false;
        }

        @Override
        public void run() {
            for (int i = 0; i < stagesCnt; i++) {
                for (int j = 0; j < beetlesCnt; j++) {
                    beetles.get(j).ready();
                }
                waitUntilDone();
                beetles.sort((o1, o2) -> Long.compare(o1.time, o2.time));
                for (int j = 0; j < beetlesCnt; j++) {
                    stage[beetles.get(j).num] = j;
                }
                for (int j = 0; j < beetlesCnt; j++) {
                    sum[j] += stage[j];
                    System.out.println("Beetle " + j + " got " + stage[j] + " points on this stage");
                }
                for (int j = 0; j < beetlesCnt; j++) {
                    System.out.println("Beetle " + j + " has " + sum[j] + " points on all stages");
                }
                registeredCnt = 0;
            }
        }

        synchronized void waitUntilDone() {
            while (!isReady) {
                try {
                    this.wait();
                } catch (InterruptedException e) { // ignore
                }
            }
        }

        synchronized void ready() {
            registeredCnt++;
            if (registeredCnt == beetlesCnt) {
                isReady = true;
                notifyAll();
            }
        }
    }
}
