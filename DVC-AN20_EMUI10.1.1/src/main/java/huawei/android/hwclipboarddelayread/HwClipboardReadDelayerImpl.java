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
    private static final Object clipLockObj = new Object();
    private static int getWaitTime = 4;
    private RemoteCallbackList<IOnPrimaryClipGetedListener> primaryClipGetedListeners;

    HwClipboardReadDelayerImpl() {
        this.primaryClipGetedListeners = null;
        this.primaryClipGetedListeners = new RemoteCallbackList<>();
    }

    public boolean addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) {
        synchronized (clipLockObj) {
            if (!(this.primaryClipGetedListeners == null || listener == null)) {
                if (listener.asBinder() != null) {
                    if (this.primaryClipGetedListeners.getRegisteredCallbackCount() == MAX_CLIPBOARDGET_LISTENER) {
                        this.primaryClipGetedListeners.unregister(this.primaryClipGetedListeners.getRegisteredCallbackItem(0));
                    }
                    return this.primaryClipGetedListeners.register(listener, callingPackage);
                }
            }
            return false;
        }
    }

    public boolean removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) {
        synchronized (clipLockObj) {
            if (!(this.primaryClipGetedListeners == null || listener == null)) {
                if (listener.asBinder() != null) {
                    return this.primaryClipGetedListeners.unregister(listener);
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
        synchronized (clipLockObj) {
            clipLockObj.notifyAll();
        }
    }

    public void getPrimaryClipNotify() {
        synchronized (clipLockObj) {
            if (this.primaryClipGetedListeners.getRegisteredCallbackCount() > 0) {
                long ident = Binder.clearCallingIdentity();
                int n = this.primaryClipGetedListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        this.primaryClipGetedListeners.getBroadcastItem(i).dispatchPrimaryClipGet();
                    } catch (RemoteException e) {
                        return;
                    }
                }
                this.primaryClipGetedListeners.finishBroadcast();
                Binder.restoreCallingIdentity(ident);
                try {
                    clipLockObj.notifyAll();
                    long startTime = System.currentTimeMillis();
                    clipLockObj.wait((long) (getWaitTime * 1000));
                    if (System.currentTimeMillis() - startTime >= ((long) getWaitTime) * 1000 && this.primaryClipGetedListeners.getRegisteredCallbackCount() == MAX_CLIPBOARDGET_LISTENER) {
                        this.primaryClipGetedListeners.unregister(this.primaryClipGetedListeners.getRegisteredCallbackItem(0));
                    }
                } catch (InterruptedException e2) {
                }
            }
        }
    }
}
