package wangxw.listener;


/**
 * @Author: wangxw
 * @DateTime: 2021/8/8
 * @Description: TODO
 */
public interface MyEventListener<T> {
    void onDataChunk(MyEvent<T> event);

    void processComplete();
}
