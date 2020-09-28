package com.huawei.android.audio;

import android.app.ActivityThread;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusChangeDispatcher;
import android.media.IAudioModeDispatcher;
import android.media.IAudioService;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.audio.IHwAudioServiceManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HwAudioServiceManager {
    private static final Singleton<IHwAudioServiceManager> IAudioServiceManagerSingleton = new Singleton<IHwAudioServiceManager>() {
        /* class com.huawei.android.audio.HwAudioServiceManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
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
    private List<AudioModeCallbackInfo> mAudioModeCallbackList;
    private final Object mAudioModeCallbackLock = new Object();
    private final IAudioModeDispatcher mModeCb = new IAudioModeDispatcher.Stub() {
        /* class com.huawei.android.audio.HwAudioServiceManager.AnonymousClass2 */

        @Override // android.media.IAudioModeDispatcher
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

    /* access modifiers changed from: private */
    public static IAudioService getAudioService() {
        IAudioService iAudioService = sService;
        if (iAudioService != null) {
            return iAudioService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return sService;
    }

    private static IHwAudioServiceManager getService() {
        return IAudioServiceManagerSingleton.get();
    }

    public static int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        try {
            return getService().setSoundEffectState(restore, packageName, isOnTop, reserved);
        } catch (RemoteException e) {
            Log.e(TAG, "setSoundEffectState failed: catch RemoteException!");
            return -1;
        }
    }

    public static int startVirtualAudio(String deviceId, String serviceId, int serviceType, Map<String, Object> dataMap) {
        try {
            return getService().startVirtualAudio(deviceId, serviceId, serviceType, dataMap);
        } catch (RemoteException e) {
            Log.e(TAG, "mAudioService is unavailable ");
            return -1;
        }
    }

    public static int removeVirtualAudio(String deviceId, String serviceId, int serviceType, Map<String, Object> dataMap) {
        try {
            return getService().removeVirtualAudio(deviceId, serviceId, serviceType, dataMap);
        } catch (RemoteException e) {
            Log.e(TAG, "mAudioService is unavailable ");
            return -1;
        }
    }

    public static int checkRecordActive(int audioSource) {
        if (audioSource < 0 || audioSource > MediaRecorder.getAudioSourceMax()) {
            return -1;
        }
        try {
            if (getService().checkRecordActive()) {
                return 0;
            }
            getService().checkMicMute();
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

    public static boolean checkMuteZenMode() {
        try {
            return getService().checkMuteZenMode();
        } catch (RemoteException e) {
            Log.e(TAG, "checkMuteZenMode failed: catch RemoteException!");
            return false;
        }
    }

    public void registerAudioModeCallback(AudioModeCallback cb, Handler handler) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("registerAudioModeCallback handler is null ? ");
        sb.append(handler == null);
        Log.i(TAG, sb.toString());
        if (cb != null) {
            synchronized (this.mAudioModeCallbackLock) {
                if (this.mAudioModeCallbackList == null) {
                    this.mAudioModeCallbackList = new ArrayList();
                }
                int oldCbCount = this.mAudioModeCallbackList.size();
                if (!hasAudioModeCallback_sync(cb)) {
                    this.mAudioModeCallbackList.add(new AudioModeCallbackInfo(cb, new ServiceEventHandlerDelegate(handler).getHandler()));
                    Log.i(TAG, "registerAudioModeCallback add callback");
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

    public void unregisterAudioModeCallback(AudioModeCallback cb) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("unregisterAudioModeCallback cb is null ? ");
        sb.append(cb == null);
        Log.i(TAG, sb.toString());
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
                return;
            }
        }
        throw new IllegalArgumentException("Illegal null AudioModeCallback argument");
    }

    public boolean registerAudioDeviceSelectCallback(IBinder cb) throws IllegalArgumentException {
        if (cb != null) {
            try {
                return getService().registerAudioDeviceSelectCallback(cb);
            } catch (RemoteException e) {
                Log.e(TAG, "registerAudioDeviceSelectCallback failed: catch RemoteException!");
                return false;
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioDeviceSelectCallback argument");
        }
    }

    public boolean unregisterAudioDeviceSelectCallback(IBinder cb) throws IllegalArgumentException {
        if (cb != null) {
            try {
                return getService().unregisterAudioDeviceSelectCallback(cb);
            } catch (RemoteException e) {
                Log.e(TAG, "unregisterAudioDeviceSelectCallback failed: catch RemoteException!");
                return false;
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioDeviceSelectCallback argument");
        }
    }

    public static abstract class AudioModeCallback {
        public void onAudioModeChanged(int audioMode) {
        }
    }

    /* access modifiers changed from: private */
    public static class AudioModeCallbackInfo {
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

    private boolean hasAudioModeCallback_sync(AudioModeCallback cb) {
        List<AudioModeCallbackInfo> list = this.mAudioModeCallbackList;
        if (list == null) {
            return false;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (cb.equals(this.mAudioModeCallbackList.get(i).mCb)) {
                return true;
            }
        }
        return false;
    }

    private boolean removeAudioModeCallback_sync(AudioModeCallback cb) {
        List<AudioModeCallbackInfo> list = this.mAudioModeCallbackList;
        if (list == null) {
            return false;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (cb.equals(this.mAudioModeCallbackList.get(i).mCb)) {
                this.mAudioModeCallbackList.remove(i);
                return true;
            }
        }
        return false;
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
            if (looper != null) {
                this.mHandler = new Handler(looper, HwAudioServiceManager.this) {
                    /* class com.huawei.android.audio.HwAudioServiceManager.ServiceEventHandlerDelegate.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        if (msg.what != 3) {
                            Log.e(HwAudioServiceManager.TAG, "Unknown event " + msg.what);
                            return;
                        }
                        AudioModeConfigChangeCallbackData cbData = (AudioModeConfigChangeCallbackData) msg.obj;
                        if (cbData.mCb != null) {
                            Log.i(HwAudioServiceManager.TAG, "dispatching onAudioModeChanged()");
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

    public boolean registerAudioFocusChangeCallback(IAudioFocusChangeDispatcher cb, String callback, String pkgName) throws IllegalArgumentException {
        if (cb != null) {
            try {
                return getService().registerAudioFocusChangeCallback(cb, callback, pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "registerAudioFocusChangeCallback failed");
                return false;
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioFocusChangeCallback argument");
        }
    }

    public boolean unregisterAudioFocusChangeCallback(IAudioFocusChangeDispatcher cb, String callback, String pkgName) throws IllegalArgumentException {
        if (cb != null) {
            try {
                return getService().unregisterAudioFocusChangeCallback(cb, callback, pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "unregisterAudioFocusChangeCallback failed");
                return false;
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioFocusChangeCallback argument");
        }
    }

    public AudioFocusInfo getAudioFocusInfo(String pkgName) {
        try {
            return getService().getAudioFocusInfo(pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "could not get audio focus info");
            return null;
        }
    }

    public boolean setFmDeviceAvailable(int state) {
        try {
            return getService().setFmDeviceAvailable(state);
        } catch (RemoteException e) {
            Log.e(TAG, "setFmDeviceAvailable failed");
            return false;
        }
    }

    public void setBtScoForRecord(boolean on) {
        try {
            getService().setBtScoForRecord(on);
        } catch (RemoteException e) {
            Log.e(TAG, "setBtScoForRecord failed.");
        }
    }

    public static void setBluetoothScoState(int state, int sessionId) {
        try {
            getService().setBluetoothScoState(state, sessionId);
        } catch (RemoteException e) {
            Log.e(TAG, "setBluetoothScoState failed.");
        }
    }
}
