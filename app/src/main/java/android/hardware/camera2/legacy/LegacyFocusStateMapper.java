package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.utils.ParamsUtils;
import android.provider.CalendarContract.CalendarCache;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public class LegacyFocusStateMapper {
    private static final boolean DEBUG = false;
    private static String TAG;
    private String mAfModePrevious;
    private int mAfRun;
    private int mAfState;
    private int mAfStatePrevious;
    private final Camera mCamera;
    private final Object mLock;

    /* renamed from: android.hardware.camera2.legacy.LegacyFocusStateMapper.1 */
    class AnonymousClass1 implements AutoFocusMoveCallback {
        final /* synthetic */ String val$afMode;
        final /* synthetic */ int val$currentAfRun;

        AnonymousClass1(int val$currentAfRun, String val$afMode) {
            this.val$currentAfRun = val$currentAfRun;
            this.val$afMode = val$afMode;
        }

        public void onAutoFocusMoving(boolean start, Camera camera) {
            synchronized (LegacyFocusStateMapper.this.mLock) {
                if (this.val$currentAfRun != LegacyFocusStateMapper.this.mAfRun) {
                    Log.d(LegacyFocusStateMapper.TAG, "onAutoFocusMoving - ignoring move callbacks from old af run" + this.val$currentAfRun);
                    return;
                }
                int newAfState;
                if (start) {
                    newAfState = 1;
                } else {
                    newAfState = 2;
                }
                String str = this.val$afMode;
                if (!str.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    if (!str.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + this.val$afMode);
                    }
                }
                LegacyFocusStateMapper.this.mAfState = newAfState;
            }
        }
    }

    /* renamed from: android.hardware.camera2.legacy.LegacyFocusStateMapper.2 */
    class AnonymousClass2 implements AutoFocusCallback {
        final /* synthetic */ String val$afMode;
        final /* synthetic */ int val$currentAfRun;

        AnonymousClass2(int val$currentAfRun, String val$afMode) {
            this.val$currentAfRun = val$currentAfRun;
            this.val$afMode = val$afMode;
        }

        public void onAutoFocus(boolean success, Camera camera) {
            synchronized (LegacyFocusStateMapper.this.mLock) {
                if (LegacyFocusStateMapper.this.mAfRun != this.val$currentAfRun) {
                    Log.d(LegacyFocusStateMapper.TAG, String.format("onAutoFocus - ignoring AF callback (old run %d, new run %d)", new Object[]{Integer.valueOf(this.val$currentAfRun), Integer.valueOf(latestAfRun)}));
                    return;
                }
                int newAfState;
                if (success) {
                    newAfState = 4;
                } else {
                    newAfState = 5;
                }
                String str = this.val$afMode;
                if (!str.equals(CalendarCache.TIMEZONE_TYPE_AUTO)) {
                    if (!(str.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) || str.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) || str.equals(Parameters.FOCUS_MODE_MACRO))) {
                        Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + this.val$afMode);
                    }
                }
                LegacyFocusStateMapper.this.mAfState = newAfState;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.legacy.LegacyFocusStateMapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.legacy.LegacyFocusStateMapper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFocusStateMapper.<clinit>():void");
    }

    public LegacyFocusStateMapper(Camera camera) {
        this.mAfStatePrevious = 0;
        this.mAfModePrevious = null;
        this.mLock = new Object();
        this.mAfRun = 0;
        this.mAfState = 0;
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
    }

    public void processRequestTriggers(CaptureRequest captureRequest, Parameters parameters) {
        int currentAfRun;
        int afStateAfterStart;
        Preconditions.checkNotNull(captureRequest, "captureRequest must not be null");
        int afTrigger = ((Integer) ParamsUtils.getOrDefault(captureRequest, CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0))).intValue();
        String afMode = parameters.getFocusMode();
        if (!Objects.equals(this.mAfModePrevious, afMode)) {
            synchronized (this.mLock) {
                this.mAfRun++;
                this.mAfState = 0;
            }
            this.mCamera.cancelAutoFocus();
        }
        this.mAfModePrevious = afMode;
        synchronized (this.mLock) {
            currentAfRun = this.mAfRun;
        }
        AutoFocusMoveCallback afMoveCallback = new AnonymousClass1(currentAfRun, afMode);
        if (!(afMode.equals(CalendarCache.TIMEZONE_TYPE_AUTO) || afMode.equals(Parameters.FOCUS_MODE_MACRO) || afMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))) {
            if (afMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            }
            switch (afTrigger) {
                case TextToSpeech.SUCCESS /*0*/:
                case AudioState.ROUTE_EARPIECE /*1*/:
                    if (afMode.equals(CalendarCache.TIMEZONE_TYPE_AUTO) || afMode.equals(Parameters.FOCUS_MODE_MACRO)) {
                        afStateAfterStart = 3;
                    } else if (afMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) || afMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        afStateAfterStart = 1;
                    } else {
                        afStateAfterStart = 0;
                    }
                    synchronized (this.mLock) {
                        currentAfRun = this.mAfRun + 1;
                        this.mAfRun = currentAfRun;
                        this.mAfState = afStateAfterStart;
                        break;
                    }
                    if (afStateAfterStart == 0) {
                        this.mCamera.autoFocus(new AnonymousClass2(currentAfRun, afMode));
                    }
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    synchronized (this.mLock) {
                        synchronized (this.mLock) {
                            this.mAfRun++;
                            this.mAfState = 0;
                            break;
                        }
                        this.mCamera.cancelAutoFocus();
                        break;
                    }
                default:
                    Log.w(TAG, "processRequestTriggers - ignoring unknown control.afTrigger = " + afTrigger);
            }
        }
        this.mCamera.setAutoFocusMoveCallback(afMoveCallback);
        switch (afTrigger) {
            case TextToSpeech.SUCCESS /*0*/:
            case AudioState.ROUTE_EARPIECE /*1*/:
                if (afMode.equals(CalendarCache.TIMEZONE_TYPE_AUTO)) {
                    if (afMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        afStateAfterStart = 0;
                        synchronized (this.mLock) {
                            currentAfRun = this.mAfRun + 1;
                            this.mAfRun = currentAfRun;
                            this.mAfState = afStateAfterStart;
                            break;
                        }
                        if (afStateAfterStart == 0) {
                            this.mCamera.autoFocus(new AnonymousClass2(currentAfRun, afMode));
                        }
                    }
                    afStateAfterStart = 1;
                    synchronized (this.mLock) {
                        currentAfRun = this.mAfRun + 1;
                        this.mAfRun = currentAfRun;
                        this.mAfState = afStateAfterStart;
                    }
                    if (afStateAfterStart == 0) {
                        this.mCamera.autoFocus(new AnonymousClass2(currentAfRun, afMode));
                    }
                }
                afStateAfterStart = 3;
                synchronized (this.mLock) {
                    currentAfRun = this.mAfRun + 1;
                    this.mAfRun = currentAfRun;
                    this.mAfState = afStateAfterStart;
                }
                if (afStateAfterStart == 0) {
                    this.mCamera.autoFocus(new AnonymousClass2(currentAfRun, afMode));
                }
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                synchronized (this.mLock) {
                    synchronized (this.mLock) {
                        this.mAfRun++;
                        this.mAfState = 0;
                        break;
                    }
                    this.mCamera.cancelAutoFocus();
                    break;
                }
            default:
                Log.w(TAG, "processRequestTriggers - ignoring unknown control.afTrigger = " + afTrigger);
        }
    }

    public void mapResultTriggers(CameraMetadataNative result) {
        int newAfState;
        Preconditions.checkNotNull(result, "result must not be null");
        synchronized (this.mLock) {
            newAfState = this.mAfState;
        }
        result.set(CaptureResult.CONTROL_AF_STATE, Integer.valueOf(newAfState));
        this.mAfStatePrevious = newAfState;
    }

    private static String afStateToString(int afState) {
        switch (afState) {
            case TextToSpeech.SUCCESS /*0*/:
                return "INACTIVE";
            case AudioState.ROUTE_EARPIECE /*1*/:
                return "PASSIVE_SCAN";
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return "PASSIVE_FOCUSED";
            case Engine.DEFAULT_STREAM /*3*/:
                return "ACTIVE_SCAN";
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return "FOCUSED_LOCKED";
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return "NOT_FOCUSED_LOCKED";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                return "PASSIVE_UNFOCUSED";
            default:
                return "UNKNOWN(" + afState + ")";
        }
    }
}
