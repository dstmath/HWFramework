package com.huawei.reliability.faultnotify;

import android.zrhung.IFaultEventCallback;
import java.util.List;

public abstract class FaultEventListener extends IFaultEventCallback.Stub {
    public abstract void onEvent(int i, List<String> list);

    public void actionPerforemed(int tag, List<String> faultInfo) {
        onEvent(tag, faultInfo);
    }
}
