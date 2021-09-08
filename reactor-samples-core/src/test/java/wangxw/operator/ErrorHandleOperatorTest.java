package wangxw.operator;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.util.Objects;

/**
 * @Author: wangxw
 * @Date: 2021/09/06
 * @Description:
 */
@Slf4j
public class ErrorHandleOperatorTest {


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
