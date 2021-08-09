package wangxw.utils;

/**
 * @Author: wangxw
 * @DateTime: 2021/8/8
 * @Description: TODO
 */
public class PrintUtil {

    public static void println(Object x) {
        System.out.println(String.format("Thread-Name-[%s], %s",
                Thread.currentThread().getName(), x));
    }
}
