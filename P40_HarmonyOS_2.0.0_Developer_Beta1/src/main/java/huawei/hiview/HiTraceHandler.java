package huawei.hiview;

import android.os.Message;

public interface HiTraceHandler {
    void csTraceInHandler(Message message);

    void srTraceInLooper(Message message);

    void ssTraceInLooper(Message message);
}
