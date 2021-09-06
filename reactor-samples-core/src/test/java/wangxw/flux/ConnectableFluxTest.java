package wangxw.flux;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wangxw
 * @Date: 2021/09/02
 * @Description:
 */
@Slf4j
public class ConnectableFluxTest {

    @Test
    public void connectTest() throws InterruptedException {
        Flux<String> source = Flux.range(1, 3)
                .map(Object::toString)
                .doOnSubscribe(s -> log.info("subscribed to source"));

        ConnectableFlux<String> co = source.publish();

        co.subscribe(log::info);
        co.subscribe(log::info);

        log.info("done subscribing");
        TimeUnit.SECONDS.sleep(1);

        log.info("will now connect");
        co.connect();
    }

    @Test
    public void autoConnect() throws InterruptedException {
        Flux<String> source = Flux.range(1, 3)
                .map(Object::toString)
                .doOnSubscribe(s -> log.info("subscribed to source"));

        Flux<String> co = source.publish().autoConnect(2);

        log.info("subscribed first");
        co.subscribe(log::info);

        TimeUnit.SECONDS.sleep(1);

        log.info("subscribing second");
        co.subscribe(log::info);
    }

    @Test
    public void refCountTest() throws InterruptedException {
        Flux<String> source = Flux.interval(Duration.ofMillis(500))
                .map(Object::toString)
                .doOnSubscribe(s -> log.info("doOnSubscribe"))
                .doOnCancel(() -> log.info("doOnCancel"));

        Flux<String> flux = source.publish().refCount(2, Duration.ofSeconds(2));

        log.info("subscribed first");
        Disposable s1 = flux.subscribe(x -> log.info("s1:" + x));

        TimeUnit.SECONDS.sleep(1);
        log.info("subscribed second");
        Disposable s2 = flux.subscribe(x -> log.info("s2:" + x));

        TimeUnit.SECONDS.sleep(1);
        log.info("subscribed first disposable");
        s1.dispose();

        TimeUnit.SECONDS.sleep(1);
        log.info("subscribed second disposable");
        s2.dispose();

        TimeUnit.SECONDS.sleep(1);
        log.info("subscribed third");
        Disposable s3 = flux.subscribe(x -> log.info("s3:" + x));

        TimeUnit.SECONDS.sleep(1);
        log.info("subscribed third disposable");
        s3.dispose();

        TimeUnit.SECONDS.sleep(3);
        log.info("subscribed fourth");
        Disposable sub4 = flux.subscribe(l -> log.info("s4: " + l));
        TimeUnit.SECONDS.sleep(1);
        log.info("subscribed  fifth");
        Disposable sub5 = flux.subscribe(l -> log.info("s5: " + l));
        TimeUnit.SECONDS.sleep(2);
    }
}
