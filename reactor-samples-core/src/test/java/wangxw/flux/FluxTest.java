package wangxw.flux;

import org.junit.Test;
import reactor.core.publisher.Flux;
import wangxw.listener.MyEvent;
import wangxw.listener.MyEventListener;
import wangxw.listener.MyEventProcesser;
import wangxw.utils.PrintUtil;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wangxw
 * @Date: 2021/08/05
 * @Description:
 */
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
        MyEventProcesser<String> myEventProcesser = new MyEventProcesser<>();
        Flux.create(emitter -> myEventProcesser.register(new MyEventListener<String>() {
            @Override
            public void onDataChunk(MyEvent<String> event) {
                emitter.next(event);
            }

            @Override
            public void processComplete() {
                emitter.complete();
            }
        })).subscribe(PrintUtil::println);// 这时候还没有任何事件产生；

        for (int i = 0; i < 20; i++) {  // 6
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(1000));
            myEventProcesser.newEvent(new MyEvent<>(new Date(), "Event" + i));
        }
        myEventProcesser.processComplete();

    }
}
