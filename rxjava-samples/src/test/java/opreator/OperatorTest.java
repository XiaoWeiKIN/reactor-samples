package opreator;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @Author: wangxw
 * @Date: 2021/08/31
 * @Description:
 */
public class OperatorTest {

    @Test
    public void createTest(){
        Flowable.fromArray(1,2)
                .subscribe(s -> System.out.println("Hello " + s + "!"));

        Flowable.interval(1_000, TimeUnit.SECONDS).subscribe(System.out::println);



    }


}
