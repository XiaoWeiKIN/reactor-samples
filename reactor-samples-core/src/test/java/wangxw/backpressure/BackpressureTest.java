package wangxw.backpressure;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import wangxw.listener.MyEvent;
import wangxw.listener.MyEventListener;
import wangxw.listener.MyEventProcessor;
import wangxw.utils.PrintUtil;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * @Author: wangxw
 * @Date: 2021/08/11
 * @Description:
 */
public class BackpressureTest {

    private final int eventDuration = 10;    // 生成的事件间隔时间，单位毫秒
    private final int eventCount = 20;    // 生成的事件个数
    private final int processDuration = 30;    // 订阅者处理每个元素的时间，单位毫秒

    private MyEventProcessor<String> eventProcessor;
    private Flux<MyEvent<String>> fastPublisher;
    private SlowSubscriber slowSubscriber;
    private CountDownLatch countDownLatch;

    @Before
    public void setup() {
        countDownLatch = new CountDownLatch(1);
        slowSubscriber = new SlowSubscriber();
        eventProcessor = new MyEventProcessor<>();
    }

    /**
     * 触发订阅，使用CountDownLatch等待订阅者处理完成。
     */
    @After
    public void subscribe() throws InterruptedException {
        fastPublisher.subscribe(slowSubscriber);
        generateEvent(eventCount, eventDuration); // 生产20个事件,每隔10ms生成一个
        countDownLatch.await(1, TimeUnit.MINUTES);
    }

    private Flux<MyEvent<String>> createFlux(FluxSink.OverflowStrategy strategy) {
        return Flux.create(emitter -> eventProcessor.register(new MyEventListener<String>() {
            @Override
            public void onDataChunk(MyEvent<String> event) {
                System.out.println("publish >>>> " + event);
                emitter.next(event);
            }

            @Override
            public void processComplete() {
                emitter.complete();
            }
        }), strategy);
    }

    private void generateEvent(int count, int millis) {
        for (int i = 0; i < count; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(millis);
            } catch (InterruptedException e) {
            }
            eventProcessor.newEvent(new MyEvent(new Date(), "Event-" + i));
        }
        eventProcessor.processComplete();
    }


    class SlowSubscriber extends BaseSubscriber<MyEvent<String>> {

        @Override
        protected void hookOnSubscribe(Subscription s) {
            request(1);
        }

        @Override
        protected void hookOnNext(MyEvent<String> event) {
            PrintUtil.println("                  receive <<< " + event);
            try {
                TimeUnit.MILLISECONDS.sleep(processDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            request(1);
        }

        @Override
        protected void hookOnComplete() {
            countDownLatch.countDown();
        }
    }

    @Test
    public void testBufferBackPressure() {
        fastPublisher = createFlux(FluxSink.OverflowStrategy.BUFFER)
                .doOnRequest(n -> PrintUtil.println("         ===  request: " + n + " ===")) // 使用FluxPeek进行增强操作
                .publishOn(Schedulers.newSingle("newSingle"), 1);

    }

    @Test
    public void testDropBackPressure() {
        fastPublisher = createFlux(FluxSink.OverflowStrategy.DROP)
                .onBackpressureDrop()
                .doOnRequest(n -> PrintUtil.println("         ===  request: " + n + " ===")) // 使用FluxPeek进行增强操作
                .publishOn(Schedulers.newSingle("newSingle"), 1);

    }

    @Test
    public void testLatestBackPressure() {
        fastPublisher = createFlux(FluxSink.OverflowStrategy.LATEST).onBackpressureLatest()
                .doOnRequest(n -> PrintUtil.println("         ===  request: " + n + " ===")) // 使用FluxPeek进行增强操作
                .publishOn(Schedulers.newSingle("newSingle"), 1);

    }

    @Test
    public void testErrorBackPressure() {
        fastPublisher = createFlux(FluxSink.OverflowStrategy.ERROR)
                .doOnRequest(n -> PrintUtil.println("         ===  request: " + n + " ===")) // 使用FluxPeek进行增强操作
                .publishOn(Schedulers.newSingle("newSingle"), 1);

    }

    @Test
    public void testIgnoreBackPressure() {
        fastPublisher = createFlux(FluxSink.OverflowStrategy.IGNORE)
                .doOnRequest(n -> PrintUtil.println("         ===  request: " + n + " ===")) // 使用FluxPeek进行增强操作
                .publishOn(Schedulers.newSingle("newSingle"), 1);

    }




}
