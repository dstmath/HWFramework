package android.media;

import android.media.AudioManager;
import android.media.IAudioService;
import android.media.IRecordingConfigDispatcher;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

public class AudioRecordingMonitorImpl implements AudioRecordingMonitor {
    private static final int MSG_RECORDING_CONFIG_CHANGE = 1;
    private static final String TAG = "android.media.AudioRecordingMonitor";
    private static IAudioService sService;
    private final AudioRecordingMonitorClient mClient;
    @GuardedBy({"mRecordCallbackLock"})
    private LinkedList<AudioRecordingCallbackInfo> mRecordCallbackList = new LinkedList<>();
    private final Object mRecordCallbackLock = new Object();
    @GuardedBy({"mRecordCallbackLock"})
    private final IRecordingConfigDispatcher mRecordingCallback = new IRecordingConfigDispatcher.Stub() {
        /* class android.media.AudioRecordingMonitorImpl.AnonymousClass1 */

        @Override // android.media.IRecordingConfigDispatcher
        public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> configs) {
            AudioRecordingConfiguration config = AudioRecordingMonitorImpl.this.getMyConfig(configs);
            if (config != null) {
                synchronized (AudioRecordingMonitorImpl.this.mRecordCallbackLock) {
                    if (AudioRecordingMonitorImpl.this.mRecordingCallbackHandler != null) {
                        AudioRecordingMonitorImpl.this.mRecordingCallbackHandler.sendMessage(AudioRecordingMonitorImpl.this.mRecordingCallbackHandler.obtainMessage(1, config));
                    }
                }
            }
        }
    };
    @GuardedBy({"mRecordCallbackLock"})
    private volatile Handler mRecordingCallbackHandler;
    @GuardedBy({"mRecordCallbackLock"})
    private HandlerThread mRecordingCallbackHandlerThread;

    AudioRecordingMonitorImpl(AudioRecordingMonitorClient client) {
        this.mClient = client;
    }

    @Override // android.media.AudioRecordingMonitor
    public void registerAudioRecordingCallback(Executor executor, AudioManager.AudioRecordingCallback cb) {
        if (cb == null) {
            throw new IllegalArgumentException("Illegal null AudioRecordingCallback");
        } else if (executor != null) {
            synchronized (this.mRecordCallbackLock) {
                Iterator<AudioRecordingCallbackInfo> it = this.mRecordCallbackList.iterator();
                while (it.hasNext()) {
                    if (it.next().mCb == cb) {
                        throw new IllegalArgumentException("AudioRecordingCallback already registered");
                    }
                }
                beginRecordingCallbackHandling();
                this.mRecordCallbackList.add(new AudioRecordingCallbackInfo(executor, cb));
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor");
        }
    }

    @Override // android.media.AudioRecordingMonitor
    public void unregisterAudioRecordingCallback(AudioManager.AudioRecordingCallback cb) {
        if (cb != null) {
            synchronized (this.mRecordCallbackLock) {
                Iterator<AudioRecordingCallbackInfo> it = this.mRecordCallbackList.iterator();
                while (it.hasNext()) {
                    AudioRecordingCallbackInfo arci = it.next();
                    if (arci.mCb == cb) {
                        this.mRecordCallbackList.remove(arci);
                        if (this.mRecordCallbackList.size() == 0) {
                            endRecordingCallbackHandling();
                        }
                    }
                }
                throw new IllegalArgumentException("AudioRecordingCallback was not registered");
            }
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioRecordingCallback argument");
    }

    @Override // android.media.AudioRecordingMonitor
    public AudioRecordingConfiguration getActiveRecordingConfiguration() {
        try {
            return getMyConfig(getService().getActiveRecordingConfigurations());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    public static class AudioRecordingCallbackInfo {
        final AudioManager.AudioRecordingCallback mCb;
        final Executor mExecutor;

        AudioRecordingCallbackInfo(Executor e, AudioManager.AudioRecordingCallback cb) {
            this.mExecutor = e;
            this.mCb = cb;
        }
    }

    @GuardedBy({"mRecordCallbackLock"})
    private void beginRecordingCallbackHandling() {
        if (this.mRecordingCallbackHandlerThread == null) {
            this.mRecordingCallbackHandlerThread = new HandlerThread("android.media.AudioRecordingMonitor.RecordingCallback");
            this.mRecordingCallbackHandlerThread.start();
            Looper looper = this.mRecordingCallbackHandlerThread.getLooper();
            if (looper != null) {
                this.mRecordingCallbackHandler = new Handler(looper) {
                    /* class android.media.AudioRecordingMonitorImpl.AnonymousClass2 */

                    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0050, code lost:
                        r3 = android.os.Binder.clearCallingIdentity();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
                        r1 = r2.iterator();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005c, code lost:
                        if (r1.hasNext() == false) goto L_0x006f;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005e, code lost:
                        r5 = r1.next();
                        r5.mExecutor.execute(new android.media.$$Lambda$AudioRecordingMonitorImpl$2$cn04v8rie0OYr_fiLO_SMYka7I(r5, r0));
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0075, code lost:
                        r1 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0076, code lost:
                        android.os.Binder.restoreCallingIdentity(r3);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0079, code lost:
                        throw r1;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
                        return;
                     */
                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        if (msg.what != 1) {
                            Log.e(AudioRecordingMonitorImpl.TAG, "Unknown event " + msg.what);
                        } else if (msg.obj != null) {
                            ArrayList<AudioRecordingConfiguration> configs = new ArrayList<>();
                            configs.add((AudioRecordingConfiguration) msg.obj);
                            synchronized (AudioRecordingMonitorImpl.this.mRecordCallbackLock) {
                                if (AudioRecordingMonitorImpl.this.mRecordCallbackList.size() != 0) {
                                    LinkedList<AudioRecordingCallbackInfo> cbInfoList = new LinkedList<>(AudioRecordingMonitorImpl.this.mRecordCallbackList);
                                }
                            }
                        }
                    }
                };
                try {
                    getService().registerRecordingCallback(this.mRecordingCallback);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    @GuardedBy({"mRecordCallbackLock"})
    private void endRecordingCallbackHandling() {
        if (this.mRecordingCallbackHandlerThread != null) {
            try {
                getService().unregisterRecordingCallback(this.mRecordingCallback);
                this.mRecordingCallbackHandlerThread.quit();
                this.mRecordingCallbackHandlerThread = null;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public AudioRecordingConfiguration getMyConfig(List<AudioRecordingConfiguration> configs) {
        int portId = this.mClient.getPortId();
        for (AudioRecordingConfiguration config : configs) {
            if (config.getClientPortId() == portId) {
                return config;
            }
        }
        return null;
    }

    private static IAudioService getService() {
        IAudioService iAudioService = sService;
        if (iAudioService != null) {
            return iAudioService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return sService;
    }
}
