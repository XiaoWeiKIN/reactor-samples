package wangxw.flux;

import org.junit.Test;
import reactor.core.publisher.Flux;

/**
 * @Author: wangxw
 * @Date: 2021/08/05
 * @Description:
 */
public class FluxTest {

    @Test
    public void generateTest(){
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3*state);
                    if (state == 10) sink.complete();
                    return state + 1;
                });
    }
}
