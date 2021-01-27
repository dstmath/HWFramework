package android.media;

import android.annotation.UnsupportedAppUsage;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class AudioPortEventHandler {
    private static final int AUDIOPORT_EVENT_NEW_LISTENER = 4;
    private static final int AUDIOPORT_EVENT_PATCH_LIST_UPDATED = 2;
    private static final int AUDIOPORT_EVENT_PORT_LIST_UPDATED = 1;
    private static final int AUDIOPORT_EVENT_SERVICE_DIED = 3;
    private static final long RESCHEDULE_MESSAGE_DELAY_MS = 100;
    private static final String TAG = "AudioPortEventHandler";
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    @UnsupportedAppUsage
    private long mJniCallback;
    private final ArrayList<AudioManager.OnAudioPortUpdateListener> mListeners = new ArrayList<>();

    private native void native_finalize();

    private native void native_setup(Object obj);

    AudioPortEventHandler() {
    }

    /* access modifiers changed from: package-private */
    public void init() {
        synchronized (this) {
            if (this.mHandler == null) {
                this.mHandlerThread = new HandlerThread(TAG);
                this.mHandlerThread.start();
                if (this.mHandlerThread.getLooper() != null) {
                    this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
                        /* class android.media.AudioPortEventHandler.AnonymousClass1 */

                        /* JADX WARNING: Removed duplicated region for block: B:43:0x00b3 A[LOOP:2: B:41:0x00ad->B:43:0x00b3, LOOP_END] */
                        @Override // android.os.Handler
                        public void handleMessage(Message msg) {
                            ArrayList<AudioManager.OnAudioPortUpdateListener> listeners;
                            int i;
                            synchronized (this) {
                                if (msg.what == 4) {
                                    listeners = new ArrayList<>();
                                    if (AudioPortEventHandler.this.mListeners.contains(msg.obj)) {
                                        listeners.add((AudioManager.OnAudioPortUpdateListener) msg.obj);
                                    }
                                } else {
                                    listeners = AudioPortEventHandler.this.mListeners;
                                }
                            }
                            if (msg.what == 1 || msg.what == 2 || msg.what == 3) {
                                AudioManager.resetAudioPortGeneration();
                            }
                            if (!listeners.isEmpty()) {
                                ArrayList<AudioPort> ports = new ArrayList<>();
                                ArrayList<AudioPatch> patches = new ArrayList<>();
                                if (msg.what == 3 || AudioManager.updateAudioPortCache(ports, patches, null) == 0) {
                                    int status = msg.what;
                                    if (status != 1) {
                                        if (status != 2) {
                                            if (status == 3) {
                                                for (int i2 = 0; i2 < listeners.size(); i2++) {
                                                    listeners.get(i2).onServiceDied();
                                                }
                                                return;
                                            } else if (status != 4) {
                                                return;
                                            }
                                        }
                                        AudioPatch[] patchList = (AudioPatch[]) patches.toArray(new AudioPatch[0]);
                                        for (i = 0; i < listeners.size(); i++) {
                                            listeners.get(i).onAudioPatchListUpdate(patchList);
                                        }
                                        return;
                                    }
                                    AudioPort[] portList = (AudioPort[]) ports.toArray(new AudioPort[0]);
                                    for (int i3 = 0; i3 < listeners.size(); i3++) {
                                        listeners.get(i3).onAudioPortListUpdate(portList);
                                    }
                                    if (msg.what == 1) {
                                        return;
                                    }
                                    AudioPatch[] patchList2 = (AudioPatch[]) patches.toArray(new AudioPatch[0]);
                                    while (i < listeners.size()) {
                                    }
                                    return;
                                }
                                sendMessageDelayed(obtainMessage(msg.what, msg.obj), AudioPortEventHandler.RESCHEDULE_MESSAGE_DELAY_MS);
                            }
                        }
                    };
                    native_setup(new WeakReference(this));
                } else {
                    this.mHandler = null;
                }
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

    /* access modifiers changed from: package-private */
    public void registerListener(AudioManager.OnAudioPortUpdateListener l) {
        synchronized (this) {
            this.mListeners.add(l);
        }
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessage(handler.obtainMessage(4, 0, 0, l));
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(AudioManager.OnAudioPortUpdateListener l) {
        synchronized (this) {
            this.mListeners.remove(l);
        }
    }

    /* access modifiers changed from: package-private */
    public Handler handler() {
        return this.mHandler;
    }

    @UnsupportedAppUsage
    private static void postEventFromNative(Object module_ref, int what, int arg1, int arg2, Object obj) {
        Handler handler;
        AudioPortEventHandler eventHandler = (AudioPortEventHandler) ((WeakReference) module_ref).get();
        if (eventHandler != null && (handler = eventHandler.handler()) != null) {
            Message m = handler.obtainMessage(what, arg1, arg2, obj);
            if (what != 4) {
                handler.removeMessages(what);
            }
            handler.sendMessage(m);
        }
    }
}
