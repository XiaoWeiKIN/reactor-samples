package wangxw.scheduler;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @Author: wangxw
 * @Date: 2021/08/12
 * @Description:
 */
public class SchedulerTest {

    @Test
    public void testScheduling() {
        Flux.range(0, 10)
//                .log()
                .publishOn(Schedulers.newParallel("myParallel"))
                .log()
                .subscribeOn(Schedulers.boundedElastic())
//                .log()
                .blockLast();
    }
}
