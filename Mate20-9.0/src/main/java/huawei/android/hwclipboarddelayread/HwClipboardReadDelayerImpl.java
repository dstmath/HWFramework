package huawei.android.hwclipboarddelayread;

import android.hwclipboarddelayread.HwClipboardReadDelayer;
import android.os.Binder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.huawei.android.content.IOnPrimaryClipGetedListener;

public class HwClipboardReadDelayerImpl implements HwClipboardReadDelayer.IHwClipboardReadDelayer {
    private static int MAX_CLIPBOARDGET_LISTENER = 1;
    private static int MAX_GET_WAIT_TIME = 10;
    private static String TAG = "HwClipboardReadDelayerImpl";
    private static int getWaitTime = 4;
    private Object clipLockObj;
    private RemoteCallbackList<IOnPrimaryClipGetedListener> primaryClipGetedListeners;

    HwClipboardReadDelayerImpl() {
        this.primaryClipGetedListeners = null;
        this.clipLockObj = null;
        this.clipLockObj = new Object();
        this.primaryClipGetedListeners = new RemoteCallbackList<>();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0031, code lost:
        return false;
     */
    public boolean addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) {
        synchronized (this.clipLockObj) {
            if (!(this.primaryClipGetedListeners == null || listener == null)) {
                if (listener.asBinder() != null) {
                    if (this.primaryClipGetedListeners.getRegisteredCallbackCount() == MAX_CLIPBOARDGET_LISTENER) {
                        this.primaryClipGetedListeners.unregister(this.primaryClipGetedListeners.getRegisteredCallbackItem(0));
                    }
                    boolean register = this.primaryClipGetedListeners.register(listener, callingPackage);
                    return register;
                }
            }
        }
    }

    public boolean removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) {
        synchronized (this.clipLockObj) {
            if (!(this.primaryClipGetedListeners == null || listener == null)) {
                if (listener.asBinder() != null) {
                    boolean unregister = this.primaryClipGetedListeners.unregister(listener);
                    return unregister;
                }
            }
            return false;
        }
    }

    public int setGetWaitTime(int waitTime) {
        if (waitTime < 0 || waitTime > MAX_GET_WAIT_TIME) {
            return -1;
        }
        getWaitTime = waitTime;
        return getWaitTime;
    }

    public void setPrimaryClipNotify() {
        synchronized (this.clipLockObj) {
            this.clipLockObj.notifyAll();
        }
    }

    public void getPrimaryClipNotify() {
        synchronized (this.clipLockObj) {
            if (this.primaryClipGetedListeners.getRegisteredCallbackCount() > 0) {
                long ident = Binder.clearCallingIdentity();
                int n = this.primaryClipGetedListeners.beginBroadcast();
                int i = 0;
                while (i < n) {
                    try {
                        this.primaryClipGetedListeners.getBroadcastItem(i).dispatchPrimaryClipGet();
                        i++;
                    } catch (RemoteException e) {
                        return;
                    }
                }
                this.primaryClipGetedListeners.finishBroadcast();
                Binder.restoreCallingIdentity(ident);
                try {
                    this.clipLockObj.notifyAll();
                    long startTime = System.currentTimeMillis();
                    this.clipLockObj.wait((long) (getWaitTime * 1000));
                    if (System.currentTimeMillis() - startTime >= ((long) getWaitTime) * 1000 && this.primaryClipGetedListeners.getRegisteredCallbackCount() == MAX_CLIPBOARDGET_LISTENER) {
                        this.primaryClipGetedListeners.unregister(this.primaryClipGetedListeners.getRegisteredCallbackItem(0));
                    }
                } catch (InterruptedException e2) {
                }
            }
        }
    }
}
