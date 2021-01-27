package android.os;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class MessageEx {
    public static Message getNext(Message message) {
        return message.next;
    }

    public static long getExpectedDispatchTime(Message message) {
        return message.expectedDispatchTime;
    }

    public static Handler getTarget(Message message) {
        return message.target;
    }

    public static Runnable getCallback(Message message) {
        return message.callback;
    }
}
