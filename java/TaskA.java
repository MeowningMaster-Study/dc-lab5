import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskA {
    public static void main(String[] args) {
        int n = 100;
        var rand = new Random();
        // false = left, true = right
        ArrayList<Boolean> recruits = new ArrayList<>(n);
        for (int i = 0; i < n; i += 1) {
            recruits.add(rand.nextBoolean());
        }

        int parties = (int) Math.ceil((float) n / 50);

        ExecutorService exs = Executors.newFixedThreadPool(parties);

        AtomicBoolean changed = new AtomicBoolean(false);
        var barrier = new CustomBarrier(parties, () -> {
            if (!changed.getAndSet(false)) {
                for(var r : recruits) {
                    System.out.print(r ? "R" : "L");
                }
                System.out.println();
                exs.shutdownNow();
            }
        });

        for (int i = 0; i < parties; i += 1) {
            int s = i * 50;
            int f = (i + 1) == parties ? n : i * 50 + 50;
            exs.submit(() -> {
                while (!Thread.interrupted()) {
                    for (int j = s; j < f; j += 1) {
                        var v = recruits.get(j);
                        int faced = j + (v ? 1 : -1);
                        if (faced > 0 && faced < n) {
                            var fv = recruits.get(faced);
                            if (v != fv) {
                                recruits.set(j, !v);
                                recruits.set(faced, !fv);
                                changed.set(true);
                            }
                        }
                    }
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
