package com.huawei.android.os;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.RegistrantList;
import com.huawei.annotation.HwSystemApi;
import java.util.stream.Stream;

public class RegistrantListEx {
    private static final int INVALID_SIZE = -1;
    RegistrantList mRegistrantList;

    public RegistrantListEx() {
        this.mRegistrantList = null;
        this.mRegistrantList = new RegistrantList();
    }

    public synchronized void addUnique(Handler handler, int what, Object obj) {
        if (this.mRegistrantList != null) {
            this.mRegistrantList.addUnique(handler, what, obj);
        }
    }

    @HwSystemApi
    public void notifyRegistrants() {
        RegistrantList registrantList = this.mRegistrantList;
        if (registrantList != null) {
            registrantList.notifyRegistrants();
        }
    }

    public void notifyRegistrants(Object userObject, Object remoteObject, Throwable ex) {
        RegistrantList registrantList = this.mRegistrantList;
        if (registrantList != null) {
            registrantList.notifyRegistrants(new AsyncResult(userObject, remoteObject, ex));
        }
    }

    @HwSystemApi
    public void notifyRegistrants(AsyncResultEx ar) {
        if (!Stream.of(this.mRegistrantList, ar).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            this.mRegistrantList.notifyRegistrants(ar.getAsyncResult());
        }
    }

    public synchronized void remove(Handler handler) {
        if (this.mRegistrantList != null) {
            this.mRegistrantList.remove(handler);
        }
    }

    @HwSystemApi
    public void add(RegistrantEx registrant) {
        RegistrantList registrantList;
        if (registrant != null && (registrantList = this.mRegistrantList) != null) {
            registrantList.add(registrant.getRegistrant());
        }
    }

    @HwSystemApi
    public int size() {
        RegistrantList registrantList = this.mRegistrantList;
        if (registrantList != null) {
            return registrantList.size();
        }
        return -1;
    }

    @HwSystemApi
    public RegistrantEx get(int index) {
        RegistrantList registrantList = this.mRegistrantList;
        if (registrantList != null) {
            return RegistrantEx.from(registrantList.get(index));
        }
        return null;
    }
}
