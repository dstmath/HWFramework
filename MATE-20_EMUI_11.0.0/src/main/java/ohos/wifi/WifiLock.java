package ohos.wifi;

import java.io.IOException;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

@SystemApi
public class WifiLock {
    private static final String DEFAULT_CALLER = "ohos";
    public static final int FAST_RESPONSE_MODE = 4;
    public static final int FULL_MODE = 1;
    public static final int HIGH_PERF_MODE = 3;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiLock");
    private static final int MAX_WIFI_LOCKS = 50;
    public static final int MULTICAST_MODE = 10;
    private String mCaller;
    private boolean mIsHeld;
    private boolean mIsRefCounted;
    private final WifiLockProxy mLockProxy = WifiLockProxy.getInstance();
    private int mLockType;
    private RemoteObject mRemote;
    private String mTag;
    private int mWifiLockCount;

    static {
        System.loadLibrary("ipc_core.z");
    }

    private WifiLock(String str, int i, String str2) {
        this.mCaller = str;
        this.mTag = str2;
        this.mLockType = i;
        this.mRemote = new RemoteObject(str2);
        this.mIsRefCounted = true;
        this.mIsHeld = false;
    }

    public static WifiLock create(Context context, int i, String str) {
        return new WifiLock((context == null || context.getAbilityInfo() == null) ? DEFAULT_CALLER : context.getAbilityInfo().getBundleName(), i, str);
    }

    public synchronized void acquire() throws IOException, RemoteException {
        HiLog.info(LABEL, "acquire wifi lock start!", new Object[0]);
        if (this.mIsRefCounted || !this.mIsHeld) {
            try {
                this.mLockProxy.acquire(this.mRemote, this.mCaller, this.mLockType, this.mTag);
                if (this.mWifiLockCount < MAX_WIFI_LOCKS) {
                    this.mWifiLockCount++;
                    this.mIsHeld = true;
                } else {
                    release();
                    throw new IOException();
                }
            } catch (RemoteException unused) {
                throw new RemoteException();
            }
        }
    }

    public synchronized void release() throws IOException, RemoteException {
        HiLog.info(LABEL, "release wifi lock start!", new Object[0]);
        if (this.mIsRefCounted || this.mIsHeld) {
            try {
                this.mLockProxy.release(this.mRemote, this.mLockType, this.mTag);
                this.mWifiLockCount--;
                this.mIsHeld = false;
            } catch (RemoteException unused) {
                throw new RemoteException();
            }
        }
        if (this.mWifiLockCount < 0) {
            throw new IOException();
        }
    }

    public synchronized boolean isHeld() {
        return this.mIsHeld;
    }

    public synchronized void setReferenceCounted(boolean z) {
        this.mIsRefCounted = z;
    }

    public String toString() {
        return "WifiLock{mTag='" + this.mTag + "', mCaller='" + this.mCaller + "', mLockType=" + this.mLockType + ", mIsRefCounted=" + this.mIsRefCounted + ", mIsHeld=" + this.mIsHeld + '}';
    }
}
