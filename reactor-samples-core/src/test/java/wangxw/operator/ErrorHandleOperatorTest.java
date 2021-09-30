package wangxw.operator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: wangxw
 * @Date: 2021/09/06
 * @Description:
 */
@Slf4j
public class ErrorHandleOperatorTest {
    AtomicBoolean isDisposed = new AtomicBoolean();
    Disposable disposableInstance = new Disposable() {
        @Override
        public void dispose() {
            isDisposed.set(true);
        }

        @Override
        public String toString() {
            return "DISPOSABLE";
        }
    };

    @Test
    public void using() {
        Flux<String> flux =
                Flux.using(
                        () -> disposableInstance, // 创建资源
                        disposable -> Flux.just(disposable.toString()), // 处理资源
                        Disposable::dispose // 清理资源
                );

        flux.subscribe(log::info);
    }

    @Test
    public void staticValueFallback() {
        Flux<String> flux = Flux.just(10)
                .map(this::doSomethingDangerous)
                .onErrorReturn("RECOVERED");
        flux.subscribe(log::info, e -> log.error("err", e));
    }

    @Test
    public void staticValueFallbackPredicate() {
        Flux<String> flux = Flux.just(1, 10, 2)
                .map(this::doSomethingDangerous)
                .onErrorReturn(BusinessException.class, "RECOVERED");
        flux.subscribe(log::info, e -> log.error("err", e));
    }

    @Test
    public void methodFallback() {
        Flux<String> flux = Flux.just("key1", "key2")
                .flatMap(key -> callExternalService(key)
                        .doOnError(e -> log.error("uh oh, falling back, service failed for key", e))
                        .onErrorResume(e -> getFromCache(key)));
        flux.subscribe(log::info, e -> log.error("error", e));
    }

    @Test
    public void rethrowTest() {
        Flux<String> flux = Flux.just("key1", "key2")
                .flatMap(key -> callExternalService(key)
                        .onErrorMap(e -> new BusinessException("catch and rethrow")));
        flux.subscribe(log::info, e -> log.error("error", e));
    }

    @Test
    public void finalyTest() {
        Flux<String> flux = Flux.just(10)
                .doFinally(signalType -> log.info("finaly: " + signalType.toString()))
                .map(this::doSomethingDangerous);
        flux.subscribe(log::info, e -> log.error("error", e));
    }

    @Test
    public void staticValueFallbackc() {
        Flux<String> flux = Flux.just(10, 1, 2)
                .map(this::doSomethingDangerous)
                .onErrorContinue((e, x) -> {
                    log.error("error <{}>", x, e);

                });
        flux.subscribe(log::info, e -> log.error("err", e));
    }


    @SneakyThrows
    @Test
    public void retryTest() {
        Flux.interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3) return "tick " + input;
                    throw new RuntimeException("boom");
                })
                .retry(1)
                .elapsed()
                .subscribe(res -> log.info(res.toString()), e -> log.error("error", e));

        Thread.sleep(2100);
    }

    @Test
    @SneakyThrows
    public void restryWhen() {
        Flux.interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3) return "tick " + input;
                    throw new RuntimeException("boom");
                })
                .doOnError(e -> log.error("on error", e))
                .retryWhen(Retry.from(companion ->
                        companion.doOnNext(retrySignal -> log.info("retry times {} ", retrySignal.totalRetries())).
                                take(3)))
                .retry(3)
                .elapsed()
                .subscribe(res -> log.info(res.toString()), e -> log.error("error", e));
        Thread.sleep(50000);
    }

    @Test
    @SneakyThrows
    public void restryWhen1() {
        Flux.<String>error( new RuntimeException("boom"))
                .doOnError(e -> System.err.println("on error"))
//                .retryWhen(Retry.from(companion ->
//                        companion
////                                .doOnNext(retrySignal -> log.info("retry times {} ", retrySignal.totalRetries()))
//                                .take(3)))
                .retry(3)
                .subscribe(System.out::println, System.err::println);
    }


    @Test
    @SneakyThrows
    public void restryWhen2() {
        Flux.<String>error(new IllegalArgumentException())
                .doOnError(e -> log.error("on error", e))
                .retryWhen(Retry.from(companion ->
                        companion.map(rs -> {
                            if (rs.totalRetries() < 3) {
                                return rs.totalRetries();
                            } else {
                                throw Exceptions.propagate(rs.failure());
                            }
                        }).doOnNext(times -> log.info("retry times {} ", times))
                ))
                .subscribe(log::info, throwable -> log.error("throwable", throwable));
        Thread.sleep(50000);

    }

    public String doSomethingDangerous(int i) {
        if (i == 10) {
            throw new BusinessException();
        }
        return i + "";
    }

    public Flux<String> callExternalService(String key) {
        if (Objects.equals(key, "key1")) {
            return Flux.error(new BusinessException("key1 error "));
        }
        return Flux.just(key);
    }

    public Flux<String> getFromCache(String key) {
        return Flux.just(key + ": cahce");
    }
}
