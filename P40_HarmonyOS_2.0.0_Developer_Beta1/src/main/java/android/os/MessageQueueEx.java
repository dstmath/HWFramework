package android.os;

import android.util.Printer;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class MessageQueueEx {
    public static boolean isPolling(MessageQueue queue) {
        return queue.isPolling();
    }

    public static long getLastVsyncEnd(MessageQueue queue) {
        return queue.mLastVsyncEnd;
    }

    public static void dump(MessageQueue queue, Printer pw, String prefix, Handler hander) {
        queue.dump(pw, prefix, hander);
    }

    public static Message getMessage(MessageQueue queue) {
        return queue.mMessages;
    }
}
