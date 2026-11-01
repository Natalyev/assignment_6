package delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class ProducerRunner {
    private final int threads;
    ProducerRunner(int threads) { if (threads < 1) throw new IllegalArgumentException("threads"); this.threads = threads; }

    void run(int producers, int eventsPerProducer, Subject<Integer> source) throws InterruptedException {
        if (producers < 3 || eventsPerProducer < 0) throw new IllegalArgumentException();
        var executor = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Void>> jobs = new ArrayList<>();
            for (int producer = 0; producer < producers; producer++) {
                final int id = producer;
                jobs.add(() -> {
                    for (int i = 0; i < eventsPerProducer; i++)
                        source.publish(new Event<>(EventType.ORDER_STATUS, id * eventsPerProducer + i));
                    return null;
                });
            }
            for (var future : executor.invokeAll(jobs)) {
                try { future.get(); }
                catch (ExecutionException e) { throw new IllegalStateException("Producer failed", e.getCause()); }
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                throw new IllegalStateException("Producers did not stop");
            }
        }
    }
}
