package com.huawei.android.os;

import android.os.UEventObserver;
import com.huawei.android.os.UEventObserverExt;

public class UEventObserverBridge extends UEventObserver {
    private UEventObserverExt uEventObserverExt;

    public void setUEventObserverExt(UEventObserverExt uEventObserverExt2) {
        this.uEventObserverExt = uEventObserverExt2;
    }

    public void onUEvent(UEventObserver.UEvent event) {
        if (this.uEventObserverExt != null) {
            UEventObserverExt.UEvent eventExt = new UEventObserverExt.UEvent();
            eventExt.setUEvent(event);
            this.uEventObserverExt.onUEvent(eventExt);
        }
    }
}
