package android.hardware.camera2.legacy;

import android.hardware.Camera;
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00e4, code lost:
        if (r2.equals("auto") != false) goto L_0x00f2;
     */
    public void processRequestTriggers(CaptureRequest captureRequest, Camera.Parameters parameters) {
        final int currentAfRun;
        boolean z;
        int afStateAfterStart;
        final int currentAfRun2;
        Preconditions.checkNotNull(captureRequest, "captureRequest must not be null");
        boolean z2 = false;
        int afTrigger = ((Integer) ParamsUtils.getOrDefault(captureRequest, CaptureRequest.CONTROL_AF_TRIGGER, 0)).intValue();
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
        Camera.AutoFocusMoveCallback afMoveCallback = new Camera.AutoFocusMoveCallback() {
            /* class android.hardware.camera2.legacy.LegacyFocusStateMapper.AnonymousClass1 */

            @Override // android.hardware.Camera.AutoFocusMoveCallback
            public void onAutoFocusMoving(boolean start, Camera camera) {
                int newAfState;
                boolean z;
                synchronized (LegacyFocusStateMapper.this.mLock) {
                    if (currentAfRun != LegacyFocusStateMapper.this.mAfRun) {
                        Log.d(LegacyFocusStateMapper.TAG, "onAutoFocusMoving - ignoring move callbacks from old af run" + currentAfRun);
                        return;
                    }
                    if (start) {
                        newAfState = 1;
                    } else {
                        newAfState = 2;
                    }
                    String str = afMode;
                    int hashCode = str.hashCode();
                    if (hashCode != -194628547) {
                        if (hashCode == 910005312 && str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            z = false;
                            if (z && !z) {
                                Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                            }
                            LegacyFocusStateMapper.this.mAfState = newAfState;
                        }
                    } else if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        z = true;
                        Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                        LegacyFocusStateMapper.this.mAfState = newAfState;
                    }
                    z = true;
                    Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                    LegacyFocusStateMapper.this.mAfState = newAfState;
                }
            }
        };
        switch (afMode.hashCode()) {
            case -194628547:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 3005871:
                if (afMode.equals("auto")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 103652300:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 910005312:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z || z || z || z) {
            this.mCamera.setAutoFocusMoveCallback(afMoveCallback);
        }
        if (afTrigger == 0) {
            return;
        }
        if (afTrigger == 1) {
            switch (afMode.hashCode()) {
                case -194628547:
                    if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        z2 = true;
                        break;
                    }
                    z2 = true;
                    break;
                case 3005871:
                    break;
                case 103652300:
                    if (afMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                        z2 = true;
                        break;
                    }
                    z2 = true;
                    break;
                case 910005312:
                    if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        z2 = true;
                        break;
                    }
                    z2 = true;
                    break;
                default:
                    z2 = true;
                    break;
            }
            if (!z2 || z2) {
                afStateAfterStart = 3;
            } else if (z2 || z2) {
                afStateAfterStart = 1;
            } else {
                afStateAfterStart = 0;
            }
            synchronized (this.mLock) {
                currentAfRun2 = this.mAfRun + 1;
                this.mAfRun = currentAfRun2;
                this.mAfState = afStateAfterStart;
            }
            if (afStateAfterStart != 0) {
                this.mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    /* class android.hardware.camera2.legacy.LegacyFocusStateMapper.AnonymousClass2 */

                    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
                    @Override // android.hardware.Camera.AutoFocusCallback
                    public void onAutoFocus(boolean success, Camera camera) {
                        int newAfState;
                        synchronized (LegacyFocusStateMapper.this.mLock) {
                            int latestAfRun = LegacyFocusStateMapper.this.mAfRun;
                            boolean z = false;
                            if (latestAfRun != currentAfRun2) {
                                Log.d(LegacyFocusStateMapper.TAG, String.format("onAutoFocus - ignoring AF callback (old run %d, new run %d)", Integer.valueOf(currentAfRun2), Integer.valueOf(latestAfRun)));
                                return;
                            }
                            if (success) {
                                newAfState = 4;
                            } else {
                                newAfState = 5;
                            }
                            String str = afMode;
                            switch (str.hashCode()) {
                                case -194628547:
                                    if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                                        z = true;
                                        break;
                                    }
                                    z = true;
                                    break;
                                case 3005871:
                                    if (str.equals("auto")) {
                                        break;
                                    }
                                    z = true;
                                    break;
                                case 103652300:
                                    if (str.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                                        z = true;
                                        break;
                                    }
                                    z = true;
                                    break;
                                case 910005312:
                                    if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                        z = true;
                                        break;
                                    }
                                    z = true;
                                    break;
                                default:
                                    z = true;
                                    break;
                            }
                            if (!(!z || z || z || z)) {
                                Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                            }
                            LegacyFocusStateMapper.this.mAfState = newAfState;
                        }
                    }
                });
            }
        } else if (afTrigger != 2) {
            Log.w(TAG, "processRequestTriggers - ignoring unknown control.afTrigger = " + afTrigger);
        } else {
            synchronized (this.mLock) {
                synchronized (this.mLock) {
                    this.mAfRun++;
                    this.mAfState = 0;
                }
                this.mCamera.cancelAutoFocus();
            }
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
