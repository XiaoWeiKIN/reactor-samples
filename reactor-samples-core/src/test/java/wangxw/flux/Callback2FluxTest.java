package wangxw.flux;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @Author: wangxw
 * @Date: 2021/08/31
 * @Description:
 */
@Slf4j
public class Callback2FluxTest {
    TwitterStream twitterStream;

    @Before
    public void setup() {
        twitterStream = TwitterStreamFactory.getSingleton();
    }

    public void add() throws InterruptedException {
        twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                log.info("Status: {} ", status);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {
                log.error("Error callback ", e);

            }
        });

        twitterStream.sample();
        TimeUnit.SECONDS.sleep(10);
        twitterStream.shutdown();
    }

    void consume(Consumer<Status> onStatus, Consumer<Exception> onException) throws InterruptedException {
        twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                onStatus.accept(status);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {
                onException.accept(e);

            }
        });

        twitterStream.sample();
        TimeUnit.SECONDS.sleep(10);
        twitterStream.shutdown();
    }

    @Test
    public void refCounted() {
        Flux<Status> flux = Flux.<Status>create(emmiter -> {
            log.info("Establishing connection");
            twitterStream.addListener(new StatusListener() {
                @Override
                public void onStatus(Status status) {
                    emmiter.next(status);
                }

                @Override
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

                }

                @Override
                public void onTrackLimitationNotice(int i) {

                }

                @Override
                public void onScrubGeo(long l, long l1) {

                }

                @Override
                public void onStallWarning(StallWarning stallWarning) {

                }

                @Override
                public void onException(Exception e) {
                    emmiter.error(e);

                }
            });
            emmiter.onDispose(() -> {
                log.info("Disconnecting");
                twitterStream.shutdown();
            });
        }).doOnSubscribe(s -> log.info("doOnSubscribe"))
                .doOnComplete(() -> log.info("doOnComplete"));

        Flux<Status> refCounted = flux.publish().refCount();


        Disposable s1 = refCounted.subscribe(status -> log.info("Status: {} ", status),
                ex -> log.error("Error callback ", ex));
        Disposable s2 = refCounted.subscribe(status -> log.info("Status: {} ", status),
                ex -> log.error("Error callback ", ex));

        s1.dispose();
        s2.dispose();
    }

    @Test
    public void lazy() {
        LazyTwitterFlux lazyTwitterFlux = new LazyTwitterFlux();

        Flux<Status> flux1 = lazyTwitterFlux.flux()
                .doOnSubscribe(s -> log.info("doOnSubscribe"));
        Flux<Status> flux2 = lazyTwitterFlux.flux()
                .doOnSubscribe(s -> log.info("doOnSubscribe"));

        flux1.subscribe(status -> log.info("Status: {} ", status),
                ex -> log.error("Error callback ", ex));
        flux2.subscribe(status -> log.info("Status: {} ", status),
                ex -> log.error("Error callback ", ex));
    }



}
