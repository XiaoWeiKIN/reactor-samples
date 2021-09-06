package wangxw.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RunWith(JUnit4.class)
public class SimpleFluxArrayTest {

    @Test
    public void justTest() {


        SimpleFlux<Integer> flux = SimpleFlux.just(1, 2, 3, 4, 5);

        flux.subscribe(new Subscriber<Integer>() { // 1
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe");
                s.request(6);   // 2
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("onNext:" + integer);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        });


    }

    @Test
    public void mapTest() {
        SimpleFlux.just(1, 2, 3, 4, 5)
                .map(e -> e + 1)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        System.out.println("onSubscribe");
                        subscription.request(6);   // 2
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("onNext:" + integer);
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });
    }
}