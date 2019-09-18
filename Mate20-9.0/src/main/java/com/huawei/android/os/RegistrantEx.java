package com.huawei.android.os;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Registrant;

public class RegistrantEx {
    Registrant mRegistrant = null;

    public RegistrantEx(Handler h, int what, Object obj) {
        this.mRegistrant = new Registrant(h, what, obj);
    }

    public void notifyRegistrant(Object uo, Object r, Throwable ex) {
        this.mRegistrant.notifyRegistrant(new AsyncResult(uo, r, ex));
    }
}
