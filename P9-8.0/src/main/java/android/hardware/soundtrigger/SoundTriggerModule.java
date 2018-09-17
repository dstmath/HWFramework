package android.hardware.soundtrigger;

import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import android.hardware.soundtrigger.SoundTrigger.RecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.SoundModel;
import android.hardware.soundtrigger.SoundTrigger.SoundModelEvent;
import android.hardware.soundtrigger.SoundTrigger.StatusListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;

public class SoundTriggerModule {
    private static final int EVENT_RECOGNITION = 1;
    private static final int EVENT_SERVICE_DIED = 2;
    private static final int EVENT_SERVICE_STATE_CHANGE = 4;
    private static final int EVENT_SOUNDMODEL = 3;
    private NativeEventHandlerDelegate mEventHandlerDelegate;
    private int mId;
    private long mNativeContext;

    private class NativeEventHandlerDelegate {
        private final Handler mHandler;

        NativeEventHandlerDelegate(final StatusListener listener, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = Looper.getMainLooper();
            }
            if (looper != null) {
                this.mHandler = new Handler(looper) {
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 1:
                                if (listener != null) {
                                    listener.onRecognition((RecognitionEvent) msg.obj);
                                    return;
                                }
                                return;
                            case 2:
                                if (listener != null) {
                                    listener.onServiceDied();
                                    return;
                                }
                                return;
                            case 3:
                                if (listener != null) {
                                    listener.onSoundModelUpdate((SoundModelEvent) msg.obj);
                                    return;
                                }
                                return;
                            case 4:
                                if (listener != null) {
                                    listener.onServiceStateChange(msg.arg1);
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                    }
                };
            } else {
                this.mHandler = null;
            }
        }

        Handler handler() {
            return this.mHandler;
        }
    }

    private native void native_finalize();

    private native void native_setup(Object obj);

    public native void detach();

    public native int loadSoundModel(SoundModel soundModel, int[] iArr);

    public native int startRecognition(int i, RecognitionConfig recognitionConfig);

    public native int stopRecognition(int i);

    public native int unloadSoundModel(int i);

    SoundTriggerModule(int moduleId, StatusListener listener, Handler handler) {
        this.mId = moduleId;
        this.mEventHandlerDelegate = new NativeEventHandlerDelegate(listener, handler);
        native_setup(new WeakReference(this));
    }

    protected void finalize() {
        native_finalize();
    }

    private static void postEventFromNative(Object module_ref, int what, int arg1, int arg2, Object obj) {
        SoundTriggerModule module = (SoundTriggerModule) ((WeakReference) module_ref).get();
        if (module != null) {
            NativeEventHandlerDelegate delegate = module.mEventHandlerDelegate;
            if (delegate != null) {
                Handler handler = delegate.handler();
                if (handler != null) {
                    handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
                }
            }
        }
    }
}
