package com.huawei.android.os;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Registrant;
import com.huawei.annotation.HwSystemApi;

public class RegistrantEx {
    Registrant mRegistrant = null;

    @HwSystemApi
    public RegistrantEx() {
    }

    public RegistrantEx(Handler handler, int what, Object obj) {
        this.mRegistrant = new Registrant(handler, what, obj);
    }

    public void setRegistrant(Registrant registrant) {
        this.mRegistrant = registrant;
    }

    @HwSystemApi
    public static RegistrantEx from(Object result) {
        if (result == null || !(result instanceof Registrant)) {
            return null;
        }
        RegistrantEx resultEx = new RegistrantEx();
        resultEx.setRegistrant((Registrant) result);
        return resultEx;
    }

    public void notifyRegistrant(Object userObject, Object remoteObject, Throwable ex) {
        Registrant registrant = this.mRegistrant;
        if (registrant != null) {
            registrant.notifyRegistrant(new AsyncResult(userObject, remoteObject, ex));
        }
    }

    @HwSystemApi
    public void notifyRegistrant() {
        Registrant registrant = this.mRegistrant;
        if (registrant != null) {
            registrant.notifyRegistrant();
        }
    }

    @HwSystemApi
    public Handler getHandler() {
        Registrant registrant = this.mRegistrant;
        if (registrant != null) {
            return registrant.getHandler();
        }
        return null;
    }

    public Registrant getRegistrant() {
        return this.mRegistrant;
    }
}
