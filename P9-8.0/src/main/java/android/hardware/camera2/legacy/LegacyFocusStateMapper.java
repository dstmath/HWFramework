package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.utils.ParamsUtils;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public class LegacyFocusStateMapper {
    private static final boolean DEBUG = false;
    private static String TAG = "LegacyFocusStateMapper";
    private String mAfModePrevious = null;
    private int mAfRun = 0;
    private int mAfState = 0;
    private int mAfStatePrevious = 0;
    private final Camera mCamera;
    private final Object mLock = new Object();

    public LegacyFocusStateMapper(Camera camera) {
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
    }

    public void processRequestTriggers(CaptureRequest captureRequest, Parameters parameters) {
        final int currentAfRun;
        Preconditions.checkNotNull(captureRequest, "captureRequest must not be null");
        int afTrigger = ((Integer) ParamsUtils.getOrDefault(captureRequest, CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0))).intValue();
        final String afMode = parameters.getFocusMode();
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
        AutoFocusMoveCallback afMoveCallback = new AutoFocusMoveCallback() {
            public void onAutoFocusMoving(boolean start, Camera camera) {
                synchronized (LegacyFocusStateMapper.this.mLock) {
                    if (currentAfRun != LegacyFocusStateMapper.this.mAfRun) {
                        Log.d(LegacyFocusStateMapper.TAG, "onAutoFocusMoving - ignoring move callbacks from old af run" + currentAfRun);
                        return;
                    }
                    int newAfState;
                    if (start) {
                        newAfState = 1;
                    } else {
                        newAfState = 2;
                    }
                    String str = afMode;
                    if (!str.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        if (!str.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                            Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                        }
                    }
                    LegacyFocusStateMapper.this.mAfState = newAfState;
                }
            }
        };
        if (afMode.equals("auto") || afMode.equals(Parameters.FOCUS_MODE_MACRO) || afMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) || afMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            this.mCamera.setAutoFocusMoveCallback(afMoveCallback);
        }
        switch (afTrigger) {
            case 0:
                return;
            case 1:
                int afStateAfterStart;
                if (afMode.equals("auto") || afMode.equals(Parameters.FOCUS_MODE_MACRO)) {
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
                }
                if (afStateAfterStart != 0) {
                    this.mCamera.autoFocus(new AutoFocusCallback() {
                        public void onAutoFocus(boolean success, Camera camera) {
                            synchronized (LegacyFocusStateMapper.this.mLock) {
                                if (LegacyFocusStateMapper.this.mAfRun != currentAfRun) {
                                    Log.d(LegacyFocusStateMapper.TAG, String.format("onAutoFocus - ignoring AF callback (old run %d, new run %d)", new Object[]{Integer.valueOf(currentAfRun), Integer.valueOf(latestAfRun)}));
                                    return;
                                }
                                int newAfState;
                                if (success) {
                                    newAfState = 4;
                                } else {
                                    newAfState = 5;
                                }
                                String str = afMode;
                                if (!str.equals("auto")) {
                                    if (!(str.equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) || str.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) || str.equals(Parameters.FOCUS_MODE_MACRO))) {
                                        Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                                    }
                                }
                                LegacyFocusStateMapper.this.mAfState = newAfState;
                            }
                        }
                    });
                    return;
                }
                return;
            case 2:
                synchronized (this.mLock) {
                    synchronized (this.mLock) {
                        this.mAfRun++;
                        this.mAfState = 0;
                    }
                    this.mCamera.cancelAutoFocus();
                }
                return;
            default:
                Log.w(TAG, "processRequestTriggers - ignoring unknown control.afTrigger = " + afTrigger);
                return;
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
            case 0:
                return "INACTIVE";
            case 1:
                return "PASSIVE_SCAN";
            case 2:
                return "PASSIVE_FOCUSED";
            case 3:
                return "ACTIVE_SCAN";
            case 4:
                return "FOCUSED_LOCKED";
            case 5:
                return "NOT_FOCUSED_LOCKED";
            case 6:
                return "PASSIVE_UNFOCUSED";
            default:
                return "UNKNOWN(" + afState + ")";
        }
    }
}
