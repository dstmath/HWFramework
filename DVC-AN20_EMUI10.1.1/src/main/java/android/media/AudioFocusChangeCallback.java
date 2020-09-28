package android.media;

import android.app.ActivityThread;
import android.media.IAudioFocusChangeDispatcher;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.huawei.android.audio.HwAudioServiceManager;
import java.util.ArrayList;
import java.util.List;

public abstract class AudioFocusChangeCallback {
    private static final String TAG = "AudioFocusChangeCallback";
    private static List<AudioFocusChangeCallback> sAudioFocusChangeCallbackList;
    private static final Object sAudioFocusChangeCallbackLock = new Object();
    private static final IAudioFocusChangeDispatcher sFocusChangeCb = new IAudioFocusChangeDispatcher.Stub() {
        /* class android.media.AudioFocusChangeCallback.AnonymousClass1 */

        public void dispatchAudioFocusChange(AudioAttributes mAttributes, String mClientId, int mFocusType, boolean mAction) {
            synchronized (AudioFocusChangeCallback.sAudioFocusChangeCallbackLock) {
                if (AudioFocusChangeCallback.sAudioFocusChangeCallbackList != null) {
                    int size = AudioFocusChangeCallback.sAudioFocusChangeCallbackList.size();
                    for (int i = 0; i < size; i++) {
                        AudioFocusChangeCallback.postRunnable((AudioFocusChangeCallback) AudioFocusChangeCallback.sAudioFocusChangeCallbackList.get(i), mAttributes, mClientId, mFocusType, mAction);
                    }
                } else {
                    Log.w(AudioFocusChangeCallback.TAG, "failed to dispatch audio focus change");
                }
            }
        }
    };
    private static final HwAudioServiceManager sHwAudioServiceManager = new HwAudioServiceManager();
    private Handler mHandler;

    public abstract void onAudioFocusChange(AudioAttributes audioAttributes, String str, int i, boolean z);

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
        throw new IllegalArgumentException("No Looper for AudioFocusChangeCallback");
    }

    /* access modifiers changed from: private */
    public static void postRunnable(AudioFocusChangeCallback arci, final AudioAttributes mAttributes, final String mClientId, final int mFocusType, final boolean mAction) {
        Handler handler = arci.mHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class android.media.AudioFocusChangeCallback.AnonymousClass2 */

                public void run() {
                    AudioFocusChangeCallback.this.onAudioFocusChange(mAttributes, mClientId, mFocusType, mAction);
                }
            });
        }
    }

    private static boolean hasAudioFocusChangeCallback_sync(AudioFocusChangeCallback cb) {
        List<AudioFocusChangeCallback> list = sAudioFocusChangeCallbackList;
        if (list == null) {
            return false;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (cb.equals(sAudioFocusChangeCallbackList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean removeAudioFocusChangeCallback_sync(AudioFocusChangeCallback cb) {
        List<AudioFocusChangeCallback> list = sAudioFocusChangeCallbackList;
        if (list == null) {
            return false;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (cb.equals(sAudioFocusChangeCallbackList.get(i))) {
                sAudioFocusChangeCallbackList.remove(i);
                return true;
            }
        }
        return false;
    }

    public static boolean registerAudioFocusChangeCallback(AudioFocusChangeCallback cb, Handler handler) throws IllegalArgumentException {
        if (cb != null) {
            String pkgName = "none";
            if (ActivityThread.currentApplication() != null) {
                pkgName = ActivityThread.currentApplication().getPackageName();
            }
            cb.setHandle(handler);
            synchronized (sAudioFocusChangeCallbackLock) {
                if (sAudioFocusChangeCallbackList == null) {
                    sAudioFocusChangeCallbackList = new ArrayList();
                }
                int oldCbCount = sAudioFocusChangeCallbackList.size();
                if (!hasAudioFocusChangeCallback_sync(cb)) {
                    sAudioFocusChangeCallbackList.add(cb);
                    int newCbCount = sAudioFocusChangeCallbackList.size();
                    if (oldCbCount == 0 && newCbCount > 0) {
                        Log.i(TAG, "start to registerAudioFocusChangeCallback");
                        return sHwAudioServiceManager.registerAudioFocusChangeCallback(sFocusChangeCb, cb.toString(), pkgName);
                    }
                } else {
                    Log.w(TAG, "attempt to call registerAudioFocusChangeCallback() on a previouslyregistered callback");
                }
                return false;
            }
        }
        throw new IllegalArgumentException("Illegal null AudioFocusChangeCallback argument");
    }

    public static boolean unregisterAudioFocusChangeCallback(AudioFocusChangeCallback cb) throws IllegalArgumentException {
        if (cb != null) {
            String pkgName = "none";
            if (ActivityThread.currentApplication() != null) {
                pkgName = ActivityThread.currentApplication().getPackageName();
            }
            synchronized (sAudioFocusChangeCallbackLock) {
                if (sAudioFocusChangeCallbackList == null) {
                    Log.w(TAG, "attempt to call unregisterAudioFocusChangeCallback() on a callback that was never registered");
                    return false;
                }
                int oldCbCount = sAudioFocusChangeCallbackList.size();
                if (removeAudioFocusChangeCallback_sync(cb)) {
                    int newCbCount = sAudioFocusChangeCallbackList.size();
                    if (oldCbCount > 0 && newCbCount == 0) {
                        Log.i(TAG, "start to unregisterAudioFocusChangeCallback");
                        return sHwAudioServiceManager.unregisterAudioFocusChangeCallback(sFocusChangeCb, cb.toString(), pkgName);
                    }
                } else {
                    Log.w(TAG, "attempt to call unregisterAudioFocusChangeCallback() on a callback already unregistered or never registered");
                }
                return false;
            }
        }
        throw new IllegalArgumentException("Illegal null AudioFocusChangeCallback argument");
    }
}
