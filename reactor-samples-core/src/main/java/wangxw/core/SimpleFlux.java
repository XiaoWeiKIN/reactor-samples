package wangxw.core;

import org.reactivestreams.Publisher;

import java.util.function.Function;

/**
 * @Author: wangxw
 * @Date: 2021/08/02
 * @Description:
 * @see: <a>href=https://github.com/reactive-streams/reactive-streams-jvm#specification</a>
 * @see: <a>href=https://lotabout.me/2019/Reactive-Streams-JVM-Specification-CN/</a>
 */
public abstract class SimpleFlux<T> implements Publisher<T> {

    public static <T> SimpleFlux<T> just(T... t) {
        return new SimpleFluxArray<>(t);
    }

    public <V> SimpleFlux<V> map(Function<? super T, ? extends V> mapper) {   // 1
        return new SimpleFluxMap<>(this, mapper); // 2
    }
}
