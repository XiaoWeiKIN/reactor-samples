package wangxw.func;

import org.junit.Test;

import java.util.function.Consumer;

/**
 * @Author: wangxw
 * @DateTime: 2021/8/8
 * @Description: TODO
 */
public class FuncTest {
    @Test
    public void testConsumer(){
        executeConsumer(s->System.out.println(s));

        executeConsumer(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println(s);
            }
        });
    }


    public void executeConsumer(Consumer<String> consumer) {
        consumer.accept("testConsumer");
    }
}
