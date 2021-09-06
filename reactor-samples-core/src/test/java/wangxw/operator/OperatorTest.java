package wangxw.operator;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wangxw.utils.PrintUtil;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Author: wangxw
 * @Date: 2021/08/31
 * @Description:
 */
@Slf4j
public class OperatorTest {

    @Test
    public void emptyTest() {
        PrintUtil.println("Before");
        Flux.empty()
                .subscribe(PrintUtil::println, PrintUtil::println, () -> PrintUtil.println("complete"));
        PrintUtil.println("After");
    }

    @Test
    public void neverTest() {
        PrintUtil.println("Before");
        Flux.never()
                .subscribe(PrintUtil::println, PrintUtil::println, () -> PrintUtil.println("complete"));
        PrintUtil.println("After");
    }

    @Test
    public void errorTest() {
        PrintUtil.println("Before");
        Flux.error(new RuntimeException("emitter an error"))
                .subscribe(PrintUtil::println, PrintUtil::println, () -> PrintUtil.println("complete"));
        PrintUtil.println("After");
    }


    @Test
    public void rangeTest() {
        PrintUtil.println("Before");
        Flux.range(3, 3)
                .subscribe(PrintUtil::println);
        PrintUtil.println("After");
    }

    @Test
    public void cacheTest() {
        Flux<Integer> flux = Flux.just(3)
                .doOnSubscribe(x -> PrintUtil.println("onSubcribe"));

        PrintUtil.println("Start");
        flux.subscribe(PrintUtil::println);
        flux.subscribe(PrintUtil::println);
        PrintUtil.println("Exit");

        PrintUtil.println("-------------------Cache---------------");
        Flux<Integer> fluxcache = Flux.just(3)
                .doOnSubscribe(x -> PrintUtil.println("onSubcribe"))
                .cache();

        PrintUtil.println("Start");
        fluxcache.subscribe(PrintUtil::println);
        fluxcache.subscribe(PrintUtil::println);
        PrintUtil.println("Exit");
    }


    @Test
    public void delayTest() throws InterruptedException {
        Flux.just("1", "2").delayElements(Duration.ofSeconds(2))
                .subscribe(log::info);

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void intervalTest() throws InterruptedException {
        Flux.interval(Duration.ofSeconds(1))
                .map(input -> {
                    if (input < 3) return "tick " + input;
                    throw new RuntimeException("boom");
                })
                .onErrorReturn("Uh oh")
                .subscribe(log::info);

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void mapTest() throws InterruptedException {
        Flux.just(1, 2, 3, 4)
                .map(i -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return i * 2 + "";
                })
                .log()
                .subscribe(log::info);
    }

    @Test
    public void flatMapTest() throws InterruptedException {
        Function<Integer, Publisher<String>> mapper = i -> Flux.just(i * 2 + "").delayElements(Duration.ofSeconds(1));

        Flux.just(1, 2, 3, 4)
                .flatMap(mapper)
                .subscribe(log::info);

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void flatMap1Test() throws InterruptedException {
        Function<String, Publisher<String>> mapper = s -> Flux.just(s.toUpperCase().split(""));

        Flux.just("baeldung", ".", "com")
                .flatMap(mapper)
                .log()
                .subscribe(log::info);
    }

    @Test
    public void flatMap2Test() throws InterruptedException {
        Flux.just(3L, 1L)
                .flatMap(x -> Flux.just(x).delayElements(Duration.ofSeconds(x)))
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void flatMap3Test() throws InterruptedException {
        Flux.just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
                .flatMap(this::loadRecordFor)
                .subscribe(log::info);

        TimeUnit.SECONDS.sleep(5);
    }

    private Flux<String> loadRecordFor(DayOfWeek dow) {
        switch (dow) {
            case SUNDAY:
                return Flux.interval(Duration.ofMillis(90))
                        .take(5)
                        .map(i -> "Sun" + i);
            case MONDAY:
                return Flux.interval(Duration.ofMillis(65))
                        .take(5)
                        .map(i -> "Mon" + i);
            default:
                return Flux.empty();
        }
    }

    @Test
    public void flatMap4Test() {
        List<User> users = new ArrayList<>();
        Flux.fromIterable(users)
                .flatMap(User::loadProfile, 5);

    }

    @Test
    public void merge() throws InterruptedException {
        Flux<String> flux1 = Flux.interval(Duration.ofMillis(300)).map(x -> {
            if (x == 3) throw new RuntimeException("test error");
            return "p1: " + x;
        });
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(500)).map(x -> "p2: " + x);

        Flux<String> mergeFlux = Flux.mergeDelayError(1, flux2,flux1);

        mergeFlux.subscribe(log::info, e -> log.error("err", e));
        TimeUnit.SECONDS.sleep(2);

    }

    @Test
    public void mergeTest() throws InterruptedException {
        Flux<String> flux1 = Flux.interval(Duration.ofMillis(300)).map(x -> "p1: " + x);
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(500)).map(x -> "p2: " + x);
        Flux<String> mergeFlux = Flux.merge(flux1, flux2);

        mergeFlux.subscribe(log::info);

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    public void form() {
        Mono<RuntimeException> mono = Mono.fromCallable(() -> new RuntimeException("xxx"));
        mono.subscribe(PrintUtil::println);
    }

    static class User {

        public Flux<Profile> loadProfile() {
            // 发送HTTP请求
            return Flux.empty();
        }


    }

    static class Profile {


    }
}
