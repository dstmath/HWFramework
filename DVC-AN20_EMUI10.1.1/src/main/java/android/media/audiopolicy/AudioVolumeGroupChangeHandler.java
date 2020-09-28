package android.media.audiopolicy;

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.internal.util.Preconditions;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AudioVolumeGroupChangeHandler {
    private static final int AUDIOVOLUMEGROUP_EVENT_NEW_LISTENER = 4;
    private static final int AUDIOVOLUMEGROUP_EVENT_VOLUME_CHANGED = 1000;
    private static final String TAG = "AudioVolumeGroupChangeHandler";
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private long mJniCallback;
    private final ArrayList<AudioManager.VolumeGroupCallback> mListeners = new ArrayList<>();

    private native void native_finalize();

    private native void native_setup(Object obj);

    public void init() {
        synchronized (this) {
            if (this.mHandler == null) {
                this.mHandlerThread = new HandlerThread(TAG);
                this.mHandlerThread.start();
                if (this.mHandlerThread.getLooper() == null) {
                    this.mHandler = null;
                    return;
                }
                this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
                    /* class android.media.audiopolicy.AudioVolumeGroupChangeHandler.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        ArrayList<AudioManager.VolumeGroupCallback> listeners;
                        synchronized (this) {
                            if (msg.what == 4) {
                                listeners = new ArrayList<>();
                                if (AudioVolumeGroupChangeHandler.this.mListeners.contains(msg.obj)) {
                                    listeners.add((AudioManager.VolumeGroupCallback) msg.obj);
                                }
                            } else {
                                listeners = AudioVolumeGroupChangeHandler.this.mListeners;
                            }
                        }
                        if (!listeners.isEmpty() && msg.what == 1000) {
                            for (int i = 0; i < listeners.size(); i++) {
                                listeners.get(i).onAudioVolumeGroupChanged(msg.arg1, msg.arg2);
                            }
                        }
                    }
                };
                native_setup(new WeakReference(this));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        native_finalize();
        if (this.mHandlerThread.isAlive()) {
            this.mHandlerThread.quit();
        }
    }

    public void registerListener(AudioManager.VolumeGroupCallback cb) {
        Preconditions.checkNotNull(cb, "volume group callback shall not be null");
        synchronized (this) {
            this.mListeners.add(cb);
        }
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessage(handler.obtainMessage(4, 0, 0, cb));
        }
    }

    public void unregisterListener(AudioManager.VolumeGroupCallback cb) {
        Preconditions.checkNotNull(cb, "volume group callback shall not be null");
        synchronized (this) {
            this.mListeners.remove(cb);
        }
    }

    /* access modifiers changed from: package-private */
    public Handler handler() {
        return this.mHandler;
    }

    private static void postEventFromNative(Object moduleRef, int what, int arg1, int arg2, Object obj) {
        Handler handler;
        AudioVolumeGroupChangeHandler eventHandler = (AudioVolumeGroupChangeHandler) ((WeakReference) moduleRef).get();
        if (eventHandler != null && (handler = eventHandler.handler()) != null) {
            Message m = handler.obtainMessage(what, arg1, arg2, obj);
            if (what != 4) {
                handler.removeMessages(what);
            }
            handler.sendMessage(m);
        }
    }
}
