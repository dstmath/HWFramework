package android.hardware.radio;

import android.hardware.radio.RadioManager.BandConfig;
import android.hardware.radio.RadioManager.ProgramInfo;
import android.hardware.radio.RadioTuner.Callback;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.List;

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
    private long mNativeContext = 0;

    private class NativeEventHandlerDelegate {
        private final Handler mHandler;

        NativeEventHandlerDelegate(final Callback callback, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = Looper.getMainLooper();
            }
            if (looper != null) {
                this.mHandler = new Handler(looper) {
                    public void handleMessage(Message msg) {
                        Callback callback;
                        boolean z = true;
                        switch (msg.what) {
                            case 0:
                                if (callback != null) {
                                    callback.onError(0);
                                    return;
                                }
                                return;
                            case 1:
                                BandConfig config = msg.obj;
                                switch (msg.arg1) {
                                    case 0:
                                        if (callback != null) {
                                            callback.onConfigurationChanged(config);
                                            return;
                                        }
                                        return;
                                    default:
                                        if (callback != null) {
                                            callback.onError(4);
                                            return;
                                        }
                                        return;
                                }
                            case 2:
                                if (callback != null) {
                                    callback = callback;
                                    if (msg.arg2 != 1) {
                                        z = false;
                                    }
                                    callback.onAntennaState(z);
                                    return;
                                }
                                return;
                            case 3:
                            case 6:
                                ProgramInfo info = msg.obj;
                                switch (msg.arg1) {
                                    case -110:
                                        if (callback != null) {
                                            callback.onError(3);
                                            return;
                                        }
                                        return;
                                    case 0:
                                        if (callback != null) {
                                            callback.onProgramInfoChanged(info);
                                            return;
                                        }
                                        return;
                                    default:
                                        if (callback != null) {
                                            callback.onError(2);
                                            return;
                                        }
                                        return;
                                }
                            case 4:
                                RadioMetadata metadata = msg.obj;
                                if (callback != null) {
                                    callback.onMetadataChanged(metadata);
                                    return;
                                }
                                return;
                            case 5:
                                if (callback != null) {
                                    callback = callback;
                                    if (msg.arg2 != 1) {
                                        z = false;
                                    }
                                    callback.onTrafficAnnouncement(z);
                                    return;
                                }
                                return;
                            case 7:
                                if (callback != null) {
                                    boolean z2;
                                    Callback callback2 = callback;
                                    if (msg.arg2 == 1) {
                                        z2 = true;
                                    } else {
                                        z2 = false;
                                    }
                                    callback2.onEmergencyAnnouncement(z2);
                                    break;
                                }
                                break;
                            case 100:
                                break;
                            case 101:
                                if (callback != null) {
                                    callback.onError(1);
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                        if (callback != null) {
                            callback = callback;
                            if (msg.arg2 != 1) {
                                z = false;
                            }
                            callback.onControlChanged(z);
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

    private native void native_setup(Object obj, BandConfig bandConfig, boolean z);

    public native int cancel();

    public native void close();

    public native int getConfiguration(BandConfig[] bandConfigArr);

    public native boolean getMute();

    public native int getProgramInformation(ProgramInfo[] programInfoArr);

    public native List<ProgramInfo> getProgramList(String str);

    public native boolean hasControl();

    public native boolean isAntennaConnected();

    public native int scan(int i, boolean z);

    public native int setConfiguration(BandConfig bandConfig);

    public native int setMute(boolean z);

    public native int step(int i, boolean z);

    public native int tune(int i, int i2);

    RadioModule(int moduleId, BandConfig config, boolean withAudio, Callback callback, Handler handler) {
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
