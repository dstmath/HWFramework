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

        /* renamed from: android.hardware.soundtrigger.SoundTriggerModule.NativeEventHandlerDelegate.1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ StatusListener val$listener;

            AnonymousClass1(Looper $anonymous0, StatusListener val$listener) {
                this.val$listener = val$listener;
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SoundTriggerModule.EVENT_RECOGNITION /*1*/:
                        if (this.val$listener != null) {
                            this.val$listener.onRecognition((RecognitionEvent) msg.obj);
                        }
                    case SoundTriggerModule.EVENT_SERVICE_DIED /*2*/:
                        if (this.val$listener != null) {
                            this.val$listener.onServiceDied();
                        }
                    case SoundTriggerModule.EVENT_SOUNDMODEL /*3*/:
                        if (this.val$listener != null) {
                            this.val$listener.onSoundModelUpdate((SoundModelEvent) msg.obj);
                        }
                    case SoundTriggerModule.EVENT_SERVICE_STATE_CHANGE /*4*/:
                        if (this.val$listener != null) {
                            this.val$listener.onServiceStateChange(msg.arg1);
                        }
                    default:
                }
            }
        }

        NativeEventHandlerDelegate(StatusListener listener, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = Looper.getMainLooper();
            }
            if (looper != null) {
                this.mHandler = new AnonymousClass1(looper, listener);
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
