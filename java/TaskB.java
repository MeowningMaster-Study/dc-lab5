import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskB {
    static class StringHolder {
        String s;

        StringHolder(String s) {
            this.s = s;
        }
    }

    static int n = 4;
    static int len = 10;
    static int minEq = 3;
    static char[] chars = {'A', 'B', 'C', 'D'};

    static String genString() {
        var rand = new Random();
        var c = new char[len];
        for (int i = 0; i < len; i += 1) {
            c[i] = chars[rand.nextInt(chars.length)];
        }
        return new String(c);
    }

    static char switchChar(char c) {
        return switch (c) {
            case 'A' -> 'C';
            case 'C' -> 'A';
            case 'B' -> 'D';
            case 'D' -> 'B';
            default -> throw new RuntimeException("Unknown character");
        };
    }

    static boolean checkString(String s) {
        var cr = s.toCharArray();
        int a = 0, b = 0;
        for (var c: cr) {
            switch (c) {
                case 'A' -> a += 1;
                case 'B' -> b += 1;
                default -> {}
            }
        }
        return a == b;
    }

    public static void main(String[] args) {
        ArrayList<StringHolder> strings = new ArrayList<>(n);
        for (int i = 0; i < n; i += 1) {
            strings.add(new StringHolder(genString()));
        }

        ExecutorService exs = Executors.newFixedThreadPool(n);
        var barrier = new CyclicBarrier(n, () -> {
            int checksPassed = 0;
            for (var s : strings) {
                if (checkString(s.s)) {
                    checksPassed += 1;
                }
            }
            if (checksPassed >= minEq) {
                for (var s : strings) {
                    System.out.println(s.s);
                }
                exs.shutdownNow();
            }
        });

        var rand = new Random();
        for (int i = 0; i < n; i += 1) {
            var string = strings.get(i);
            exs.submit(() -> {
                while (!Thread.interrupted()) {
                    var c = string.s.toCharArray();
                    for (int k = 0; k < c.length; k += 1) {
                        if (rand.nextBoolean()) {
                            c[k] = switchChar(c[k]);
                        }
                    }
                    string.s = new String(c);
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
