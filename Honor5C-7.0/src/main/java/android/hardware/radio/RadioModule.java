package android.hardware.radio;

import android.hardware.radio.RadioManager.BandConfig;
import android.hardware.radio.RadioManager.ProgramInfo;
import android.hardware.radio.RadioTuner.Callback;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;

public class RadioModule extends RadioTuner {
    static final int EVENT_AF_SWITCH = 6;
    static final int EVENT_ANTENNA = 2;
    static final int EVENT_CONFIG = 1;
    static final int EVENT_CONTROL = 100;
    static final int EVENT_EA = 7;
    static final int EVENT_HW_FAILURE = 0;
    static final int EVENT_METADATA = 4;
    static final int EVENT_SERVER_DIED = 101;
    static final int EVENT_TA = 5;
    static final int EVENT_TUNED = 3;
    private NativeEventHandlerDelegate mEventHandlerDelegate;
    private int mId;
    private long mNativeContext;

    private class NativeEventHandlerDelegate {
        private final Handler mHandler;

        /* renamed from: android.hardware.radio.RadioModule.NativeEventHandlerDelegate.1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ Callback val$callback;

            AnonymousClass1(Looper $anonymous0, Callback val$callback) {
                this.val$callback = val$callback;
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                Callback callback;
                boolean z = true;
                switch (msg.what) {
                    case RadioModule.EVENT_HW_FAILURE /*0*/:
                        if (this.val$callback != null) {
                            this.val$callback.onError(RadioModule.EVENT_HW_FAILURE);
                            return;
                        }
                        return;
                    case RadioModule.EVENT_CONFIG /*1*/:
                        BandConfig config = msg.obj;
                        switch (msg.arg1) {
                            case RadioModule.EVENT_HW_FAILURE /*0*/:
                                if (this.val$callback != null) {
                                    this.val$callback.onConfigurationChanged(config);
                                    return;
                                }
                                return;
                            default:
                                if (this.val$callback != null) {
                                    this.val$callback.onError(RadioModule.EVENT_METADATA);
                                    return;
                                }
                                return;
                        }
                    case RadioModule.EVENT_ANTENNA /*2*/:
                        if (this.val$callback != null) {
                            callback = this.val$callback;
                            if (msg.arg2 != RadioModule.EVENT_CONFIG) {
                                z = false;
                            }
                            callback.onAntennaState(z);
                            return;
                        }
                        return;
                    case RadioModule.EVENT_TUNED /*3*/:
                    case RadioModule.EVENT_AF_SWITCH /*6*/:
                        ProgramInfo info = msg.obj;
                        switch (msg.arg1) {
                            case MediaPlayer.MEDIA_ERROR_TIMED_OUT /*-110*/:
                                if (this.val$callback != null) {
                                    this.val$callback.onError(RadioModule.EVENT_TUNED);
                                    return;
                                }
                                return;
                            case RadioModule.EVENT_HW_FAILURE /*0*/:
                                if (this.val$callback != null) {
                                    this.val$callback.onProgramInfoChanged(info);
                                    return;
                                }
                                return;
                            default:
                                if (this.val$callback != null) {
                                    this.val$callback.onError(RadioModule.EVENT_ANTENNA);
                                    return;
                                }
                                return;
                        }
                    case RadioModule.EVENT_METADATA /*4*/:
                        RadioMetadata metadata = msg.obj;
                        if (this.val$callback != null) {
                            this.val$callback.onMetadataChanged(metadata);
                            return;
                        }
                        return;
                    case RadioModule.EVENT_TA /*5*/:
                        if (this.val$callback != null) {
                            callback = this.val$callback;
                            if (msg.arg2 != RadioModule.EVENT_CONFIG) {
                                z = false;
                            }
                            callback.onTrafficAnnouncement(z);
                            return;
                        }
                        return;
                    case RadioModule.EVENT_EA /*7*/:
                        if (this.val$callback != null) {
                            boolean z2;
                            Callback callback2 = this.val$callback;
                            if (msg.arg2 == RadioModule.EVENT_CONFIG) {
                                z2 = true;
                            } else {
                                z2 = false;
                            }
                            callback2.onEmergencyAnnouncement(z2);
                            break;
                        }
                        break;
                    case RadioModule.EVENT_CONTROL /*100*/:
                        break;
                    case RadioModule.EVENT_SERVER_DIED /*101*/:
                        if (this.val$callback != null) {
                            this.val$callback.onError(RadioModule.EVENT_CONFIG);
                            return;
                        }
                        return;
                    default:
                        return;
                }
                if (this.val$callback != null) {
                    callback = this.val$callback;
                    if (msg.arg2 != RadioModule.EVENT_CONFIG) {
                        z = false;
                    }
                    callback.onControlChanged(z);
                }
            }
        }

        NativeEventHandlerDelegate(Callback callback, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = Looper.getMainLooper();
            }
            if (looper != null) {
                this.mHandler = new AnonymousClass1(looper, callback);
            } else {
                this.mHandler = null;
            }
        }

        Handler handler() {
            return this.mHandler;
        }
    }

    private native void native_finalize();

    private native void native_setup(Object obj, BandConfig bandConfig, boolean z);

    public native int cancel();

    public native void close();

    public native int getConfiguration(BandConfig[] bandConfigArr);

    public native boolean getMute();

    public native int getProgramInformation(ProgramInfo[] programInfoArr);

    public native boolean hasControl();

    public native boolean isAntennaConnected();

    public native int scan(int i, boolean z);

    public native int setConfiguration(BandConfig bandConfig);

    public native int setMute(boolean z);

    public native int step(int i, boolean z);

    public native int tune(int i, int i2);

    RadioModule(int moduleId, BandConfig config, boolean withAudio, Callback callback, Handler handler) {
        this.mNativeContext = 0;
        this.mId = moduleId;
        this.mEventHandlerDelegate = new NativeEventHandlerDelegate(callback, handler);
        native_setup(new WeakReference(this), config, withAudio);
    }

    protected void finalize() {
        native_finalize();
    }

    boolean initCheck() {
        return this.mNativeContext != 0;
    }

    private static void postEventFromNative(Object module_ref, int what, int arg1, int arg2, Object obj) {
        RadioModule module = (RadioModule) ((WeakReference) module_ref).get();
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
