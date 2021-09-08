package wangxw.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: wangxw
 * @DateTime: 2021/8/8
 * @Description: TODO
 */
public class PrintUtil {

    private static final SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");


    public static void println(Object x) {
        System.out.println(String.format(format0.format(new Date()) + " [Thread-Name-%s], %s",
                Thread.currentThread().getName(), x));
    }

    public static String println(Date date) {
        return format0.format(new Date());
    }
}
