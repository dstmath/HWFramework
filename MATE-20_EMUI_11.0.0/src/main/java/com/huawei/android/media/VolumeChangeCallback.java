package com.huawei.android.media;

import android.app.ActivityThread;
import android.media.IVolumeChangeDispatcher;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.huawei.android.audio.HwAudioServiceManager;
import com.huawei.android.hardware.usb.UsbManagerExt;
import java.util.ArrayList;
import java.util.List;

public abstract class VolumeChangeCallback {
    private static final String TAG = "VolumeChangeCallback";
    private static final Object VOLUME_CHANGE_CALLBACK_LOCK = new Object();
    private static final HwAudioServiceManager sHwAudioServiceManager = new HwAudioServiceManager();
    private static List<VolumeChangeCallback> sVolumeChangeCallbackList;
    private static final IVolumeChangeDispatcher sVolumeChangeCb = new IVolumeChangeDispatcher.Stub() {
        /* class com.huawei.android.media.VolumeChangeCallback.AnonymousClass1 */

        public void dispatchVolumeChange(int device, int stream, String caller, int volume) {
            synchronized (VolumeChangeCallback.VOLUME_CHANGE_CALLBACK_LOCK) {
                if (VolumeChangeCallback.sVolumeChangeCallbackList != null) {
                    int size = VolumeChangeCallback.sVolumeChangeCallbackList.size();
                    for (int i = 0; i < size; i++) {
                        VolumeChangeCallback.postRunnable((VolumeChangeCallback) VolumeChangeCallback.sVolumeChangeCallbackList.get(i), device, stream, caller, volume);
                    }
                } else {
                    Log.w(VolumeChangeCallback.TAG, "failed to dispatch volume change");
                }
            }
        }
    };
    private Handler mHandler;

    public abstract void onVolumeChange(int i, int i2, String str, int i3);

    public static boolean registerVolumeChangeCallback(VolumeChangeCallback cb, Handler handler) throws IllegalArgumentException {
        if (cb != null) {
            String pkgName = UsbManagerExt.USB_FUNCTION_NONE;
            if (ActivityThread.currentApplication() != null) {
                pkgName = ActivityThread.currentApplication().getPackageName();
            }
            cb.setHandle(handler);
            synchronized (VOLUME_CHANGE_CALLBACK_LOCK) {
                if (sVolumeChangeCallbackList == null) {
                    sVolumeChangeCallbackList = new ArrayList();
                }
                int oldCbCount = sVolumeChangeCallbackList.size();
                if (!hasVolumeChangeCallbackSync(cb)) {
                    sVolumeChangeCallbackList.add(cb);
                    int newCbCount = sVolumeChangeCallbackList.size();
                    if (oldCbCount == 0 && newCbCount > 0) {
                        Log.i(TAG, "start to registerVolumeChangeCallback");
                        return sHwAudioServiceManager.registerVolumeChangeCallback(sVolumeChangeCb, cb.toString(), pkgName);
                    }
                } else {
                    Log.w(TAG, "attempt to call registerVolumeChangeCallback() on a previouslyregistered callback");
                }
                return false;
            }
        }
        throw new IllegalArgumentException("Illegal null VolumeChangeCallback argument");
    }

    public static boolean unregisterVolumeChangeCallback(VolumeChangeCallback cb) throws IllegalArgumentException {
        if (cb != null) {
            String pkgName = UsbManagerExt.USB_FUNCTION_NONE;
            if (ActivityThread.currentApplication() != null) {
                pkgName = ActivityThread.currentApplication().getPackageName();
            }
            synchronized (VOLUME_CHANGE_CALLBACK_LOCK) {
                if (sVolumeChangeCallbackList == null) {
                    Log.w(TAG, "attempt to call unregisterVolumeChangeCallback() on a callback that was never registered");
                    return false;
                }
                int oldCbCount = sVolumeChangeCallbackList.size();
                if (removeVolumeChangeCallbackSync(cb)) {
                    int newCbCount = sVolumeChangeCallbackList.size();
                    if (oldCbCount > 0 && newCbCount == 0) {
                        Log.i(TAG, "start to unregisterVolumeChangeCallback");
                        return sHwAudioServiceManager.unregisterVolumeChangeCallback(sVolumeChangeCb, cb.toString(), pkgName);
                    }
                } else {
                    Log.w(TAG, "attempt to call unregisterVolumeChangeCallback() on a callback already unregistered or never registered");
                }
                return false;
            }
        }
        throw new IllegalArgumentException("Illegal null VolumeChangeCallback argument");
    }

    /* access modifiers changed from: private */
    public static void postRunnable(VolumeChangeCallback cb, final int device, final int stream, final String caller, final int volume) {
        Handler handler = cb.mHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class com.huawei.android.media.VolumeChangeCallback.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    VolumeChangeCallback.this.onVolumeChange(device, stream, caller, volume);
                }
            });
        }
    }

    private static boolean hasVolumeChangeCallbackSync(VolumeChangeCallback cb) {
        List<VolumeChangeCallback> list = sVolumeChangeCallbackList;
        if (list == null) {
            return false;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (cb.equals(sVolumeChangeCallbackList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean removeVolumeChangeCallbackSync(VolumeChangeCallback cb) {
        List<VolumeChangeCallback> list = sVolumeChangeCallbackList;
        if (list == null) {
            return false;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (cb.equals(sVolumeChangeCallbackList.get(i))) {
                sVolumeChangeCallbackList.remove(i);
                return true;
            }
        }
        return false;
    }

    private void setHandle(Handler handler) throws IllegalArgumentException {
        if (handler != null) {
            this.mHandler = handler;
            return;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mHandler = new Handler(looper);
            return;
        }
        Looper looper2 = Looper.getMainLooper();
        if (looper2 != null) {
            this.mHandler = new Handler(looper2);
            return;
        }
        throw new IllegalArgumentException("No Looper for VolumeChangeCallback");
    }
}
