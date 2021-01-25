package huawei.android.hwclipboarddelayread;

import android.hwclipboarddelayread.HwClipboardReadDelayer;
import android.os.Binder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.content.IOnPrimaryClipGetedListener;

public class HwClipboardReadDelayerImpl implements HwClipboardReadDelayer.IHwClipboardReadDelayer {
    private static final Object CLIP_LOCL_OBJ_CLIP = new Object();
    private static final int DEFAULT_WAIT_TIME = 4;
    private static final int MAX_CLIPBOARDGET_LISTENER = 1;
    private static final int MAX_GET_WAIT_TIME = 10;
    private static final String TAG = "HwClipboardReadDelayerImpl";
    private static final int WAIT_TIME_MULT = 1000;
    private static int getWaitTime = 4;
    private RemoteCallbackList<IOnPrimaryClipGetedListener> primaryClipGetedListeners;

    HwClipboardReadDelayerImpl() {
        this.primaryClipGetedListeners = null;
        this.primaryClipGetedListeners = new RemoteCallbackList<>();
    }

    public boolean addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) {
        synchronized (CLIP_LOCL_OBJ_CLIP) {
            if (!(this.primaryClipGetedListeners == null || listener == null)) {
                if (listener.asBinder() != null) {
                    if (this.primaryClipGetedListeners.getRegisteredCallbackCount() == 1) {
                        this.primaryClipGetedListeners.unregister(this.primaryClipGetedListeners.getRegisteredCallbackItem(0));
                    }
                    return this.primaryClipGetedListeners.register(listener, callingPackage);
                }
            }
            return false;
        }
    }

    public boolean removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) {
        synchronized (CLIP_LOCL_OBJ_CLIP) {
            if (!(this.primaryClipGetedListeners == null || listener == null)) {
                if (listener.asBinder() != null) {
                    return this.primaryClipGetedListeners.unregister(listener);
                }
            }
            return false;
        }
    }

    public int setGetWaitTime(int waitTime) {
        if (waitTime < 0 || waitTime > 10) {
            return -1;
        }
        getWaitTime = waitTime;
        return getWaitTime;
    }

    public void setPrimaryClipNotify() {
        synchronized (CLIP_LOCL_OBJ_CLIP) {
            CLIP_LOCL_OBJ_CLIP.notifyAll();
        }
    }

    public void getPrimaryClipNotify() {
        synchronized (CLIP_LOCL_OBJ_CLIP) {
            if (this.primaryClipGetedListeners.getRegisteredCallbackCount() > 0) {
                long ident = Binder.clearCallingIdentity();
                int number = this.primaryClipGetedListeners.beginBroadcast();
                for (int i = 0; i < number; i++) {
                    try {
                        this.primaryClipGetedListeners.getBroadcastItem(i).dispatchPrimaryClipGet();
                    } catch (RemoteException e) {
                        return;
                    }
                }
                this.primaryClipGetedListeners.finishBroadcast();
                Binder.restoreCallingIdentity(ident);
                try {
                    CLIP_LOCL_OBJ_CLIP.notifyAll();
                    long startTime = System.currentTimeMillis();
                    CLIP_LOCL_OBJ_CLIP.wait((long) (getWaitTime * 1000));
                    if (System.currentTimeMillis() - startTime >= ((long) getWaitTime) * 1000 && this.primaryClipGetedListeners.getRegisteredCallbackCount() == 1) {
                        this.primaryClipGetedListeners.unregister(this.primaryClipGetedListeners.getRegisteredCallbackItem(0));
                    }
                } catch (InterruptedException e2) {
                    Log.e(TAG, " getPrimaryClipNotify InterruptedException");
                }
            }
        }
    }
}
