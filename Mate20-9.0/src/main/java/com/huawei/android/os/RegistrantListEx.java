package com.huawei.android.os;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.RegistrantList;

public class RegistrantListEx {
    RegistrantList mRegistrantList;

    public RegistrantListEx() {
        this.mRegistrantList = null;
        this.mRegistrantList = new RegistrantList();
    }

    public synchronized void addUnique(Handler h, int what, Object obj) {
        this.mRegistrantList.addUnique(h, what, obj);
    }

    public void notifyRegistrants(Object uo, Object r, Throwable ex) {
        this.mRegistrantList.notifyRegistrants(new AsyncResult(uo, r, ex));
    }

    public synchronized void remove(Handler h) {
        this.mRegistrantList.remove(h);
    }
}
