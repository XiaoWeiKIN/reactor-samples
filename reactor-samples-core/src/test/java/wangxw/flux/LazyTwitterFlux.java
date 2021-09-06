package wangxw.flux;

import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: wangxw
 * @Date: 2021/09/01
 * @Description:
 */
public class LazyTwitterFlux {

    private final Set<FluxSink<? super Status>> fluxSinks = new CopyOnWriteArraySet<>();

    private final Flux<Status> flux = Flux.create(emmiter -> {
        registrer(emmiter);
        emmiter.onDispose(() -> unregistrer(emmiter));
    });

    private final TwitterStream twitterStream;

    public LazyTwitterFlux() {
        this.twitterStream = TwitterStreamFactory.getSingleton();
        twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                fluxSinks.forEach(sink -> sink.next(status));
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
                fluxSinks.forEach(fluxSink -> fluxSink.error(e));

            }
        });
    }

    public Flux<Status> flux() {
        return flux;
    }

    private synchronized void registrer(FluxSink<? super Status> fluxSink) {
        fluxSinks.add(fluxSink);
        if (fluxSinks.isEmpty()) {
            twitterStream.sample();
        }
    }

    private synchronized void unregistrer(FluxSink<? super Status> fluxSink) {
        fluxSinks.remove(fluxSink);
        if (fluxSinks.isEmpty()) {
            twitterStream.shutdown();
        }
    }

}
