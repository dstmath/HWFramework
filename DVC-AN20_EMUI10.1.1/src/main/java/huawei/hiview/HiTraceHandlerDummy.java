package huawei.hiview;

import android.os.Message;

public class HiTraceHandlerDummy implements HiTraceHandler {
    @Override // huawei.hiview.HiTraceHandler
    public void csTraceInHandler(Message msg) {
    }

    @Override // huawei.hiview.HiTraceHandler
    public void srTraceInLooper(Message msg) {
    }

    @Override // huawei.hiview.HiTraceHandler
    public void ssTraceInLooper(Message msg) {
    }
}
