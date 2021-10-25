package wangxw.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wangxw
 * @Date: 2021/08/12
 * @Description:
 */
@Slf4j
public class SchedulerTest {

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);


    @Test
    public void test() throws InterruptedException {
        long start = System.currentTimeMillis();
        Flux.just("1", "2", "3", "4")
                .flatMap(x -> Mono.fromCallable(()->str(x))
                        .subscribeOn(Schedulers.boundedElastic()))
                .log()
                .subscribe(log::info);
        log.warn("latency time <{}>ms",System.currentTimeMillis() - start);
        TimeUnit.SECONDS.sleep(5);
    }

    public Boolean predicate(String s) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (s.equals("2")) {
            return false;
        }
        return true;
    }

    public String str(String s) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return s + "x";
    }


    @Test
    public void test1() throws InterruptedException {
        Flux.just("1", "2", "3", "4")
                .map(this::str)
                .log()
                .flatMap(x -> Mono.just(str(x))
                        .subscribeOn(Schedulers.boundedElastic()))
                .subscribe(log::info);
        TimeUnit.SECONDS.sleep(5);
    }


    public Mono<String> str1(String s) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        if (s.equals("2")) {
//            throw new RuntimeException();
//        }
        String x = s + "s";

        return Mono.just(x);
    }

    @Test
    public void testScheduling() throws InterruptedException {
//        Flux.range(0, 10)
////                .log()
//                .publishOn(Schedulers.newParallel("myParallel"))
//                .log()
//                .subscribeOn(Schedulers.boundedElastic())
////                .log()
//                .blockLast();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executor.execute(() -> {
                System.out.println("---");
                if (finalI > 3) {
                    try {
                        Thread.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        Thread.sleep(3);
        System.out.println(executor.getTaskCount() + "--" + executor.getCompletedTaskCount());
    }
}
