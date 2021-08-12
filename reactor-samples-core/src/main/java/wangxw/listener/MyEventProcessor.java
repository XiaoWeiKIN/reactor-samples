package wangxw.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wangxw
 * @DateTime: 2021/8/8
 * @Description: TODO
 */
public class MyEventProcessor<T> {

    private List<MyEventListener<T>> listeners;

    public MyEventProcessor() {
        this.listeners = new ArrayList<>();
    }

    public void register(MyEventListener<T> listener) {    // 1
        listeners.add(listener);
    }

    public void newEvent(MyEvent<T> event) {
        for (MyEventListener listener :
                listeners) {
            listener.onDataChunk(event);     // 2
        }
    }

    public void processComplete() {
        for (MyEventListener listener :
                listeners) {
            listener.processComplete();      // 3
        }
    }
}
