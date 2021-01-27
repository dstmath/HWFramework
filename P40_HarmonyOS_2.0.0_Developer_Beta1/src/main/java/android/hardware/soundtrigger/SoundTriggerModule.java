package android.hardware.soundtrigger;

import android.annotation.UnsupportedAppUsage;
import android.hardware.soundtrigger.SoundTrigger;
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
    @UnsupportedAppUsage
    private int mId;
    @UnsupportedAppUsage
    private long mNativeContext;

    private native void native_finalize();

    private native void native_setup(String str, Object obj);

    @UnsupportedAppUsage
    public native void detach();

    public native int getModelState(int i);

    @UnsupportedAppUsage
    public native int loadSoundModel(SoundTrigger.SoundModel soundModel, int[] iArr);

    @UnsupportedAppUsage
    public native int startRecognition(int i, SoundTrigger.RecognitionConfig recognitionConfig);

    @UnsupportedAppUsage
    public native int stopRecognition(int i);

    @UnsupportedAppUsage
    public native int unloadSoundModel(int i);

    SoundTriggerModule(int moduleId, SoundTrigger.StatusListener listener, Handler handler) {
        this.mId = moduleId;
        this.mEventHandlerDelegate = new NativeEventHandlerDelegate(listener, handler);
        native_setup(SoundTrigger.getCurrentOpPackageName(), new WeakReference(this));
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        native_finalize();
    }

    private class NativeEventHandlerDelegate {
        private final Handler mHandler;

        NativeEventHandlerDelegate(final SoundTrigger.StatusListener listener, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = Looper.getMainLooper();
            }
            if (looper != null) {
                this.mHandler = new Handler(looper, SoundTriggerModule.this) {
                    /* class android.hardware.soundtrigger.SoundTriggerModule.NativeEventHandlerDelegate.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        SoundTrigger.StatusListener statusListener;
                        int i = msg.what;
                        if (i == 1) {
                            SoundTrigger.StatusListener statusListener2 = listener;
                            if (statusListener2 != null) {
                                statusListener2.onRecognition((SoundTrigger.RecognitionEvent) msg.obj);
                            }
                        } else if (i == 2) {
                            SoundTrigger.StatusListener statusListener3 = listener;
                            if (statusListener3 != null) {
                                statusListener3.onServiceDied();
                            }
                        } else if (i == 3) {
                            SoundTrigger.StatusListener statusListener4 = listener;
                            if (statusListener4 != null) {
                                statusListener4.onSoundModelUpdate((SoundTrigger.SoundModelEvent) msg.obj);
                            }
                        } else if (i == 4 && (statusListener = listener) != null) {
                            statusListener.onServiceStateChange(msg.arg1);
                        }
                    }
                };
            } else {
                this.mHandler = null;
            }
        }

        /* access modifiers changed from: package-private */
        public Handler handler() {
            return this.mHandler;
        }
    }

    @UnsupportedAppUsage
    private static void postEventFromNative(Object module_ref, int what, int arg1, int arg2, Object obj) {
        NativeEventHandlerDelegate delegate;
        Handler handler;
        SoundTriggerModule module = (SoundTriggerModule) ((WeakReference) module_ref).get();
        if (module != null && (delegate = module.mEventHandlerDelegate) != null && (handler = delegate.handler()) != null) {
            handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
        }
    }
}
