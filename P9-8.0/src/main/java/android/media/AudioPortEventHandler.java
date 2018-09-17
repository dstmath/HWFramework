package android.media;

import android.media.AudioManager.OnAudioPortUpdateListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

class AudioPortEventHandler {
    private static final int AUDIOPORT_EVENT_NEW_LISTENER = 4;
    private static final int AUDIOPORT_EVENT_PATCH_LIST_UPDATED = 2;
    private static final int AUDIOPORT_EVENT_PORT_LIST_UPDATED = 1;
    private static final int AUDIOPORT_EVENT_SERVICE_DIED = 3;
    private static final String TAG = "AudioPortEventHandler";
    private Handler mHandler;
    private long mJniCallback;
    private final ArrayList<OnAudioPortUpdateListener> mListeners = new ArrayList();

    private native void native_finalize();

    private native void native_setup(Object obj);

    AudioPortEventHandler() {
    }

    /* JADX WARNING: Missing block: B:11:0x001d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void init() {
        synchronized (this) {
            if (this.mHandler != null) {
                return;
            }
            Looper looper = Looper.getMainLooper();
            if (looper != null) {
                this.mHandler = new Handler(looper) {
                    /* JADX WARNING: Missing block: B:37:0x007f, code:
            if (r13.what != 1) goto L_0x0081;
     */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void handleMessage(Message msg) {
                        ArrayList<OnAudioPortUpdateListener> listeners;
                        synchronized (this) {
                            if (msg.what == 4) {
                                listeners = new ArrayList();
                                if (AudioPortEventHandler.this.mListeners.contains(msg.obj)) {
                                    listeners.add((OnAudioPortUpdateListener) msg.obj);
                                }
                            } else {
                                listeners = AudioPortEventHandler.this.mListeners;
                            }
                        }
                        if (msg.what == 1 || msg.what == 2 || msg.what == 3) {
                            AudioManager.resetAudioPortGeneration();
                        }
                        if (!listeners.isEmpty()) {
                            ArrayList<AudioPort> ports = new ArrayList();
                            ArrayList<AudioPatch> patches = new ArrayList();
                            if (msg.what == 3 || AudioManager.updateAudioPortCache(ports, patches, null) == 0) {
                                int i;
                                switch (msg.what) {
                                    case 1:
                                    case 4:
                                        AudioPort[] portList = (AudioPort[]) ports.toArray(new AudioPort[0]);
                                        for (i = 0; i < listeners.size(); i++) {
                                            ((OnAudioPortUpdateListener) listeners.get(i)).onAudioPortListUpdate(portList);
                                        }
                                        break;
                                    case 2:
                                        AudioPatch[] patchList = (AudioPatch[]) patches.toArray(new AudioPatch[0]);
                                        for (i = 0; i < listeners.size(); i++) {
                                            ((OnAudioPortUpdateListener) listeners.get(i)).onAudioPatchListUpdate(patchList);
                                        }
                                        break;
                                    case 3:
                                        for (i = 0; i < listeners.size(); i++) {
                                            ((OnAudioPortUpdateListener) listeners.get(i)).onServiceDied();
                                        }
                                        break;
                                }
                            }
                        }
                    }
                };
                native_setup(new WeakReference(this));
            } else {
                this.mHandler = null;
            }
        }
    }

    protected void finalize() {
        native_finalize();
    }

    void registerListener(OnAudioPortUpdateListener l) {
        synchronized (this) {
            this.mListeners.add(l);
        }
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(4, 0, 0, l));
        }
    }

    void unregisterListener(OnAudioPortUpdateListener l) {
        synchronized (this) {
            this.mListeners.remove(l);
        }
    }

    Handler handler() {
        return this.mHandler;
    }

    private static void postEventFromNative(Object module_ref, int what, int arg1, int arg2, Object obj) {
        AudioPortEventHandler eventHandler = (AudioPortEventHandler) ((WeakReference) module_ref).get();
        if (!(eventHandler == null || eventHandler == null)) {
            Handler handler = eventHandler.handler();
            if (handler != null) {
                handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
            }
        }
    }
}
