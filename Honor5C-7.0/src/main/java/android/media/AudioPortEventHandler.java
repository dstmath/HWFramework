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
    private final ArrayList<OnAudioPortUpdateListener> mListeners;

    /* renamed from: android.media.AudioPortEventHandler.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            ArrayList<OnAudioPortUpdateListener> listeners;
            ArrayList<AudioPort> ports;
            ArrayList<AudioPatch> patches;
            AudioPort[] portList;
            int i;
            AudioPatch[] patchList;
            synchronized (this) {
                if (msg.what == AudioPortEventHandler.AUDIOPORT_EVENT_NEW_LISTENER) {
                    listeners = new ArrayList();
                    if (AudioPortEventHandler.this.mListeners.contains(msg.obj)) {
                        listeners.add((OnAudioPortUpdateListener) msg.obj);
                    }
                } else {
                    listeners = AudioPortEventHandler.this.mListeners;
                }
            }
            if (!(msg.what == AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED || msg.what == AudioPortEventHandler.AUDIOPORT_EVENT_PATCH_LIST_UPDATED)) {
                if (msg.what == AudioPortEventHandler.AUDIOPORT_EVENT_SERVICE_DIED) {
                }
                if (listeners.isEmpty()) {
                    ports = new ArrayList();
                    patches = new ArrayList();
                    if (msg.what != AudioPortEventHandler.AUDIOPORT_EVENT_SERVICE_DIED || AudioManager.updateAudioPortCache(ports, patches, null) == 0) {
                        switch (msg.what) {
                            case AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED /*1*/:
                            case AudioPortEventHandler.AUDIOPORT_EVENT_NEW_LISTENER /*4*/:
                                portList = (AudioPort[]) ports.toArray(new AudioPort[0]);
                                for (i = 0; i < listeners.size(); i += AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED) {
                                    ((OnAudioPortUpdateListener) listeners.get(i)).onAudioPortListUpdate(portList);
                                }
                                break;
                            case AudioPortEventHandler.AUDIOPORT_EVENT_PATCH_LIST_UPDATED /*2*/:
                                patchList = (AudioPatch[]) patches.toArray(new AudioPatch[0]);
                                for (i = 0; i < listeners.size(); i += AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED) {
                                    ((OnAudioPortUpdateListener) listeners.get(i)).onAudioPatchListUpdate(patchList);
                                }
                                break;
                            case AudioPortEventHandler.AUDIOPORT_EVENT_SERVICE_DIED /*3*/:
                                for (i = 0; i < listeners.size(); i += AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED) {
                                    ((OnAudioPortUpdateListener) listeners.get(i)).onServiceDied();
                                }
                                break;
                        }
                    }
                    return;
                }
            }
            AudioManager.resetAudioPortGeneration();
            if (listeners.isEmpty()) {
                ports = new ArrayList();
                patches = new ArrayList();
                if (msg.what != AudioPortEventHandler.AUDIOPORT_EVENT_SERVICE_DIED) {
                }
                switch (msg.what) {
                    case AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED /*1*/:
                    case AudioPortEventHandler.AUDIOPORT_EVENT_NEW_LISTENER /*4*/:
                        portList = (AudioPort[]) ports.toArray(new AudioPort[0]);
                        for (i = 0; i < listeners.size(); i += AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED) {
                            ((OnAudioPortUpdateListener) listeners.get(i)).onAudioPortListUpdate(portList);
                        }
                        break;
                    case AudioPortEventHandler.AUDIOPORT_EVENT_PATCH_LIST_UPDATED /*2*/:
                        patchList = (AudioPatch[]) patches.toArray(new AudioPatch[0]);
                        for (i = 0; i < listeners.size(); i += AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED) {
                            ((OnAudioPortUpdateListener) listeners.get(i)).onAudioPatchListUpdate(patchList);
                        }
                        break;
                    case AudioPortEventHandler.AUDIOPORT_EVENT_SERVICE_DIED /*3*/:
                        for (i = 0; i < listeners.size(); i += AudioPortEventHandler.AUDIOPORT_EVENT_PORT_LIST_UPDATED) {
                            ((OnAudioPortUpdateListener) listeners.get(i)).onServiceDied();
                        }
                        break;
                }
            }
        }
    }

    private native void native_finalize();

    private native void native_setup(Object obj);

    AudioPortEventHandler() {
        this.mListeners = new ArrayList();
    }

    void init() {
        synchronized (this) {
            if (this.mHandler != null) {
                return;
            }
            Looper looper = Looper.getMainLooper();
            if (looper != null) {
                this.mHandler = new AnonymousClass1(looper);
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(AUDIOPORT_EVENT_NEW_LISTENER, 0, 0, l));
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
