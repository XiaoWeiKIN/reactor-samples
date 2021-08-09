package wangxw.listener;

import java.util.Date;

/**
 * @Author: wangxw
 * @DateTime: 2021/8/8
 * @Description: TODO
 */
public class MyEvent<T> {
    private Date timestamp;
    private T message;

    public MyEvent(Date timestamp, T message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MyEvent{" +
                "timestamp=" + timestamp +
                ", message=" + message +
                '}';
    }
}
