package com.huawei.android.hwdfu;

import android.os.SystemProperties;
import android.util.Log;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceInfo {
    static final boolean DEBUG;
    private static final String TAG = "ServiceInfo";
    private volatile boolean mIsEnabled = false;
    private volatile boolean mIsLazy;
    private AtomicInteger mReferenceCount = new AtomicInteger(0);
    private String mServiceName;

    static {
        boolean z = false;
        if (SystemProperties.getInt("persist.sys.dfu.debug", 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }

    ServiceInfo(String name) {
        this.mServiceName = name.intern();
    }

    /* access modifiers changed from: package-private */
    public void setServiceStart() {
        this.mIsEnabled = true;
        if (DEBUG) {
            Log.i(TAG, "setServiceStart " + this.mServiceName);
        }
    }

    /* access modifiers changed from: package-private */
    public void setServiceStop(boolean isLazy) {
        this.mIsEnabled = false;
        this.mIsLazy = isLazy;
        if (DEBUG) {
            Log.i(TAG, "setServiceStop " + this.mServiceName);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isNeedStop() {
        Log.i(TAG, "isNeedStop " + this.mServiceName + ", isEnabled:" + this.mIsEnabled + ", isLazy:" + this.mIsLazy + ", mReferenceCount:" + this.mReferenceCount);
        if (this.mIsEnabled) {
            return false;
        }
        if (!this.mIsLazy || this.mReferenceCount.get() == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabled() {
        Log.i(TAG, "isNeedStop " + this.mServiceName + ", isEnabled:" + this.mIsEnabled);
        return this.mIsEnabled;
    }

    /* access modifiers changed from: package-private */
    public void acquire() {
        this.mReferenceCount.incrementAndGet();
        if (DEBUG) {
            Log.i(TAG, "acquire " + this.mServiceName);
            Log.e(TAG, this.mServiceName + " acquire", new Exception());
        }
    }

    public void release() {
        this.mReferenceCount.decrementAndGet();
        if (this.mReferenceCount.get() < 0) {
            throw new IndexOutOfBoundsException();
        } else if (DEBUG) {
            Log.i(TAG, "release " + this.mServiceName);
        }
    }
}
