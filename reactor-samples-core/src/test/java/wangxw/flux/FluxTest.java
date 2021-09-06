package wangxw.flux;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import wangxw.listener.MyEvent;
import wangxw.listener.MyEventListener;
import wangxw.listener.MyEventProcessor;
import wangxw.utils.PrintUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

/**
 * @Author: wangxw
 * @Date: 2021/08/05
 * @Description:
 */
@Slf4j
public class FluxTest {
    /**
     * 同步创建
     * SynchronousSink
     * one-by-one
     * 必须request->sink.next-> request->sink.next
     */
    @Test
    public void testGenerate() {
        Flux<String> flux = Flux.generate(
                () -> 0, // 初始state值
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state); // 产生数据是同步的，每次产生一个数据
                    if (state == 10) {
                        sink.complete();
                    }
                    return state + 1; // 改变状态
                },
                (state) -> System.out.println("state: " + state)); // 最后状态值
        // 订阅时触发requset->sink.next顺序产生数据
        // 生产一个数据消费一个
        flux.subscribe(System.out::println);
    }

    @Test
    public void testCreate() throws InterruptedException {
        MyEventProcessor<String> myEventProcesser = new MyEventProcessor<>();
        Flux<MyEvent<String>> flux = Flux.<MyEvent<String>>create(emitter -> {
            PrintUtil.println("Create");
            myEventProcesser.register(new MyEventListener<String>() {
                @Override
                public void onDataChunk(MyEvent<String> event) {
                    emitter.next(event);
                }

                @Override
                public void processComplete() {
                    emitter.complete();
                }
            });

            emitter.onRequest(n -> { // n subscribe.requset时调用
                List<String> messages = getHistory(n);
                messages.forEach(PrintUtil::println);
            });
        }).doOnSubscribe(x -> PrintUtil.println("doOnSubscribe")).publish().refCount();

        flux.subscribe(x -> PrintUtil.println("S1 " + x.toString()), PrintUtil::println); // 这时候还没有任何事件产生；
        flux.subscribe(x -> PrintUtil.println("S2 " + x.toString()), PrintUtil::println); // 这时候还没有任何事件产生；

        for (int i = 0; i < 20; i++) {  // 6
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(1000));
            myEventProcesser.newEvent(new MyEvent<>(new Date(), "Event" + i));
        }
        myEventProcesser.processComplete();
    }

    @Test
    public void testPush() throws InterruptedException {
        MyEventProcessor<String> myEventProcesser = new MyEventProcessor<>();
        Flux.push(emitter -> {
            myEventProcesser.register(new MyEventListener<String>() {
                @Override
                public void onDataChunk(MyEvent<String> event) {
                    emitter.next(event);
                }

                @Override
                public void processComplete() {
                    emitter.complete();

                }
            });

            emitter.onRequest(n -> { // n
                List<String> messages = getHistory(n);
                messages.forEach(PrintUtil::println);
            });
        }).subscribe(PrintUtil::println);// 这时候还没有任何事件产生；

        for (int i = 0; i < 20; i++) {  // 6
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(1000));
            myEventProcesser.newEvent(new MyEvent<>(new Date(), "Event" + i));
        }
        myEventProcesser.processComplete();
    }

    @Test
    public void hot() {
        Sinks.Many<String> hotSource = Sinks.unsafe().many().multicast().directBestEffort();

        Flux<String> hotFlux = hotSource.asFlux().map(String::toUpperCase);

        hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: " + d));

        hotSource.emitNext("blue", FAIL_FAST);
        hotSource.tryEmitNext("green").orThrow();

        hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: " + d));

        hotSource.emitNext("orange", FAIL_FAST);
        hotSource.emitNext("purple", FAIL_FAST);
        hotSource.emitComplete(FAIL_FAST);
    }




    public List<String> getHistory(Long n) {
        return Arrays.asList("History1", "History2", "History3");
    }
}
