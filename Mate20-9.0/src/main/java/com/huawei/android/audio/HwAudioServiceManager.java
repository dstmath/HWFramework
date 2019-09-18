package com.huawei.android.audio;

import android.app.ActivityThread;
import android.media.IAudioModeDispatcher;
import android.media.IAudioService;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.android.internal.os.PowerProfile;
import com.huawei.android.audio.IHwAudioServiceManager;
import java.util.ArrayList;
import java.util.List;

public class HwAudioServiceManager {
    private static final Singleton<IHwAudioServiceManager> IAudioServiceManagerSingleton = new Singleton<IHwAudioServiceManager>() {
        /* access modifiers changed from: protected */
        public IHwAudioServiceManager create() {
            try {
                return IHwAudioServiceManager.Stub.asInterface(HwAudioServiceManager.getAudioService().getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final int MSSG_AUDIOMODE_CONFIG_CHANGE = 3;
    private static final String TAG = "HwAudioServiceManager";
    private static IAudioService sService;
    /* access modifiers changed from: private */
    public List<AudioModeCallbackInfo> mAudioModeCallbackList;
    /* access modifiers changed from: private */
    public final Object mAudioModeCallbackLock = new Object();
    private final IAudioModeDispatcher mModeCb = new IAudioModeDispatcher.Stub() {
        public void dispatchAudioModeChange(int audioMode) {
            synchronized (HwAudioServiceManager.this.mAudioModeCallbackLock) {
                if (HwAudioServiceManager.this.mAudioModeCallbackList != null) {
                    int size = HwAudioServiceManager.this.mAudioModeCallbackList.size();
                    for (int i = 0; i < size; i++) {
                        AudioModeCallbackInfo arci = (AudioModeCallbackInfo) HwAudioServiceManager.this.mAudioModeCallbackList.get(i);
                        if (arci.mHandler != null) {
                            arci.mHandler.sendMessage(arci.mHandler.obtainMessage(3, new AudioModeConfigChangeCallbackData(arci.mCb, audioMode)));
                        }
                    }
                }
            }
        }
    };

    public static abstract class AudioModeCallback {
        public void onAudioModeChanged(int audioMode) {
        }
    }

    private static class AudioModeCallbackInfo {
        final AudioModeCallback mCb;
        final Handler mHandler;

        AudioModeCallbackInfo(AudioModeCallback cb, Handler handler) {
            this.mCb = cb;
            this.mHandler = handler;
        }
    }

    private static final class AudioModeConfigChangeCallbackData {
        final int mAudioMode;
        final AudioModeCallback mCb;

        AudioModeConfigChangeCallbackData(AudioModeCallback cb, int audioMode) {
            this.mCb = cb;
            this.mAudioMode = audioMode;
        }
    }

    private class ServiceEventHandlerDelegate {
        private final Handler mHandler;

        ServiceEventHandlerDelegate(Handler handler) {
            Looper looper;
            if (handler == null) {
                Looper myLooper = Looper.myLooper();
                looper = myLooper;
                if (myLooper == null) {
                    looper = Looper.getMainLooper();
                }
            } else {
                looper = handler.getLooper();
            }
            Looper looper2 = looper;
            if (looper2 != null) {
                this.mHandler = new Handler(looper2, HwAudioServiceManager.this) {
                    public void handleMessage(Message msg) {
                        if (msg.what != 3) {
                            Log.e(HwAudioServiceManager.TAG, "Unknown event " + msg.what);
                            return;
                        }
                        AudioModeConfigChangeCallbackData cbData = (AudioModeConfigChangeCallbackData) msg.obj;
                        if (cbData.mCb != null) {
                            Log.d(HwAudioServiceManager.TAG, "dispatching onAudioModeChanged()");
                            cbData.mCb.onAudioModeChanged(cbData.mAudioMode);
                        }
                    }
                };
            } else {
                this.mHandler = null;
            }
        }

        /* access modifiers changed from: package-private */
        public Handler getHandler() {
            return this.mHandler;
        }
    }

    /* access modifiers changed from: private */
    public static IAudioService getAudioService() {
        if (sService != null) {
            return sService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService(PowerProfile.POWER_AUDIO));
        return sService;
    }

    private static IHwAudioServiceManager getService() {
        return (IHwAudioServiceManager) IAudioServiceManagerSingleton.get();
    }

    public static int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        try {
            return getService().setSoundEffectState(restore, packageName, isOnTop, reserved);
        } catch (RemoteException e) {
            Log.e(TAG, "setSoundEffectState failed: catch RemoteException!");
            return -1;
        }
    }

    public static int checkRecordActive(int audioSource) {
        if (audioSource < 0 || audioSource > MediaRecorder.getAudioSourceMax()) {
            return -1;
        }
        try {
            if (!getService().checkRecordActive()) {
                getService().checkMicMute();
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "checkRecordActive or checkMicMute failed: catch RemoteException!");
            return -1;
        }
    }

    public static int sendRecordStateChangedIntent(int state) {
        try {
            getService().sendRecordStateChangedIntent(MediaRecorder.class.getSimpleName(), state, Process.myPid(), ActivityThread.currentPackageName());
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "sendRecordStateChangedIntent failed: catch RemoteException!");
            return -1;
        }
    }

    public static int getRecordConcurrentType() {
        try {
            return getService().getRecordConcurrentType(ActivityThread.currentPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "getRecordConcurrentType failed: catch RemoteException!");
            return 0;
        }
    }

    public void registerAudioModeCallback(AudioModeCallback cb, Handler handler) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("registerAudioModeCallback handler is null ? ");
        sb.append(handler == null);
        Log.v(TAG, sb.toString());
        if (cb != null) {
            synchronized (this.mAudioModeCallbackLock) {
                if (this.mAudioModeCallbackList == null) {
                    this.mAudioModeCallbackList = new ArrayList();
                }
                int oldCbCount = this.mAudioModeCallbackList.size();
                if (!hasAudioModeCallback_sync(cb)) {
                    this.mAudioModeCallbackList.add(new AudioModeCallbackInfo(cb, new ServiceEventHandlerDelegate(handler).getHandler()));
                    Log.v(TAG, "registerAudioModeCallback add callback");
                    int newCbCount = this.mAudioModeCallbackList.size();
                    if (oldCbCount == 0 && newCbCount > 0) {
                        try {
                            getService().registerAudioModeCallback(this.mModeCb);
                        } catch (RemoteException e) {
                            Log.e(TAG, "sendRecordStateChangedIntent failed: catch RemoteException!");
                        }
                    }
                } else {
                    Log.w(TAG, "attempt to call registerAudioModeCallback() on a previouslyregistered callback");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioModeCallback argument");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005e, code lost:
        return;
     */
    public void unregisterAudioModeCallback(AudioModeCallback cb) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("unregisterAudioModeCallback cb is null ? ");
        sb.append(cb == null);
        Log.v(TAG, sb.toString());
        if (cb != null) {
            synchronized (this.mAudioModeCallbackLock) {
                if (this.mAudioModeCallbackList == null) {
                    Log.w(TAG, "attempt to call unregisterAudioModeCallback() on a callback that was never registered");
                    return;
                }
                int oldCbCount = this.mAudioModeCallbackList.size();
                if (removeAudioModeCallback_sync(cb)) {
                    int newCbCount = this.mAudioModeCallbackList.size();
                    if (oldCbCount > 0 && newCbCount == 0) {
                        try {
                            getService().unregisterAudioModeCallback(this.mModeCb);
                        } catch (RemoteException e) {
                            Log.e(TAG, "sendRecordStateChangedIntent failed: catch RemoteException!");
                        }
                    }
                } else {
                    Log.w(TAG, "attempt to call unregisterAudioModeCallback() on a callback already unregistered or never registered");
                }
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioModeCallback argument");
        }
    }

    private boolean hasAudioModeCallback_sync(AudioModeCallback cb) {
        if (this.mAudioModeCallbackList != null) {
            int size = this.mAudioModeCallbackList.size();
            for (int i = 0; i < size; i++) {
                if (cb.equals(this.mAudioModeCallbackList.get(i).mCb)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean removeAudioModeCallback_sync(AudioModeCallback cb) {
        if (this.mAudioModeCallbackList != null) {
            int size = this.mAudioModeCallbackList.size();
            for (int i = 0; i < size; i++) {
                if (cb.equals(this.mAudioModeCallbackList.get(i).mCb)) {
                    this.mAudioModeCallbackList.remove(i);
                    return true;
                }
            }
        }
        return false;
    }
}
