package wangxw.core;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.Function;

/**
 * @Author: wangxw
 * @Date: 2021/08/03
 * @Description:
 */
public class SimpleFluxMap<T, R> extends SimpleFlux<R> {
    private final SimpleFlux<? extends T> source;
    private final Function<? super T, ? extends R> mapper;

    public SimpleFluxMap(SimpleFlux<? extends T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Subscriber<? super R> actual) {
        source.subscribe(new MapSubscriber<>(actual, mapper));

    }

    /**
     * 操作符 ：只对数据做搬运和加工，对下游是作为发布者，传递上游的数据到下游；对上游是作为订阅者，传递下游的请求到上游。
     * @param <T>
     * @param <R>
     */
    static final class MapSubscriber<T, R> implements Subscriber<T>, Subscription {
        private final Subscriber<? super R> actual;
        private final Function<? super T, ? extends R> mapper;

        boolean done;

        Subscription subscriptionOfUpstream;

        MapSubscriber(Subscriber<? super R> actual, Function<? super T, ? extends R> mapper) {
            this.actual = actual;
            this.mapper = mapper;
        }

        @Override
        public void onSubscribe(Subscription s) {
            this.subscriptionOfUpstream = s;    // 1
            actual.onSubscribe(this);           // 2
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            actual.onNext(mapper.apply(t));     // 3
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                return;
            }
            done = true;
            actual.onError(t);                  // 4
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            actual.onComplete();                // 5
        }

        @Override
        public void request(long n) {
            this.subscriptionOfUpstream.request(n);     // 6
        }

        @Override
        public void cancel() {
            this.subscriptionOfUpstream.cancel();       // 7
        }
    }

}
