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
    /* access modifiers changed from: private */
    public static String TAG = "LegacyFocusStateMapper";
    private String mAfModePrevious = null;
    /* access modifiers changed from: private */
    public int mAfRun = 0;
    /* access modifiers changed from: private */
    public int mAfState = 0;
    private int mAfStatePrevious = 0;
    private final Camera mCamera;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();

    public LegacyFocusStateMapper(Camera camera) {
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
    }

    /* JADX WARNING: Removed duplicated region for block: B:108:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b3  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00cf  */
    public void processRequestTriggers(CaptureRequest captureRequest, Camera.Parameters parameters) {
        final int currentAfRun;
        boolean z;
        final int currentAfRun2;
        CaptureRequest captureRequest2 = captureRequest;
        Preconditions.checkNotNull(captureRequest2, "captureRequest must not be null");
        int afStateAfterStart = 0;
        int afTrigger = ((Integer) ParamsUtils.getOrDefault(captureRequest2, CaptureRequest.CONTROL_AF_TRIGGER, 0)).intValue();
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
            /* JADX WARNING: Removed duplicated region for block: B:25:0x005e  */
            /* JADX WARNING: Removed duplicated region for block: B:26:0x0063  */
            public void onAutoFocusMoving(boolean start, Camera camera) {
                synchronized (LegacyFocusStateMapper.this.mLock) {
                    if (currentAfRun != LegacyFocusStateMapper.this.mAfRun) {
                        Log.d(LegacyFocusStateMapper.TAG, "onAutoFocusMoving - ignoring move callbacks from old af run" + currentAfRun);
                        return;
                    }
                    boolean z = true;
                    int newAfState = start ? 1 : 2;
                    String str = afMode;
                    int hashCode = str.hashCode();
                    if (hashCode != -194628547) {
                        if (hashCode == 910005312) {
                            if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                z = false;
                                switch (z) {
                                    case false:
                                    case true:
                                        break;
                                    default:
                                        Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                                        break;
                                }
                                int unused = LegacyFocusStateMapper.this.mAfState = newAfState;
                            }
                        }
                    } else if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        switch (z) {
                            case false:
                            case true:
                                break;
                        }
                        int unused2 = LegacyFocusStateMapper.this.mAfState = newAfState;
                    }
                    z = true;
                    switch (z) {
                        case false:
                        case true:
                            break;
                    }
                    int unused3 = LegacyFocusStateMapper.this.mAfState = newAfState;
                }
            }
        };
        int hashCode = afMode.hashCode();
        char c = 65535;
        if (hashCode == -194628547) {
            if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                z = true;
                switch (z) {
                    case false:
                    case true:
                    case true:
                    case true:
                        break;
                }
                switch (afTrigger) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 3005871) {
            if (afMode.equals("auto")) {
                z = false;
                switch (z) {
                    case false:
                    case true:
                    case true:
                    case true:
                        break;
                }
                switch (afTrigger) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 103652300) {
            if (afMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                z = true;
                switch (z) {
                    case false:
                    case true:
                    case true:
                    case true:
                        break;
                }
                switch (afTrigger) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 910005312 && afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            z = true;
            switch (z) {
                case false:
                case true:
                case true:
                case true:
                    this.mCamera.setAutoFocusMoveCallback(afMoveCallback);
                    break;
            }
            switch (afTrigger) {
                case 0:
                    return;
                case 1:
                    int hashCode2 = afMode.hashCode();
                    if (hashCode2 != -194628547) {
                        if (hashCode2 != 3005871) {
                            if (hashCode2 != 103652300) {
                                if (hashCode2 == 910005312 && afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                    c = 2;
                                }
                            } else if (afMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                                c = 1;
                            }
                        } else if (afMode.equals("auto")) {
                            c = 0;
                        }
                    } else if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        c = 3;
                    }
                    switch (c) {
                        case 0:
                        case 1:
                            afStateAfterStart = 3;
                            break;
                        case 2:
                        case 3:
                            afStateAfterStart = 1;
                            break;
                    }
                    synchronized (this.mLock) {
                        currentAfRun2 = this.mAfRun + 1;
                        this.mAfRun = currentAfRun2;
                        this.mAfState = afStateAfterStart;
                    }
                    if (afStateAfterStart != 0) {
                        this.mCamera.autoFocus(new Camera.AutoFocusCallback() {
                            /* JADX WARNING: Removed duplicated region for block: B:34:0x0081  */
                            /* JADX WARNING: Removed duplicated region for block: B:35:0x0086  */
                            public void onAutoFocus(boolean success, Camera camera) {
                                int newAfState;
                                synchronized (LegacyFocusStateMapper.this.mLock) {
                                    int latestAfRun = LegacyFocusStateMapper.this.mAfRun;
                                    char c = 1;
                                    if (latestAfRun != currentAfRun2) {
                                        Log.d(LegacyFocusStateMapper.TAG, String.format("onAutoFocus - ignoring AF callback (old run %d, new run %d)", new Object[]{Integer.valueOf(currentAfRun2), Integer.valueOf(latestAfRun)}));
                                        return;
                                    }
                                    if (success) {
                                        newAfState = 4;
                                    } else {
                                        newAfState = 5;
                                    }
                                    String str = afMode;
                                    int hashCode = str.hashCode();
                                    if (hashCode != -194628547) {
                                        if (hashCode != 3005871) {
                                            if (hashCode != 103652300) {
                                                if (hashCode == 910005312) {
                                                    if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                                        switch (c) {
                                                            case 0:
                                                            case 1:
                                                            case 2:
                                                            case 3:
                                                                break;
                                                            default:
                                                                Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                                                                break;
                                                        }
                                                        int unused = LegacyFocusStateMapper.this.mAfState = newAfState;
                                                    }
                                                }
                                            } else if (str.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                                                c = 3;
                                                switch (c) {
                                                    case 0:
                                                    case 1:
                                                    case 2:
                                                    case 3:
                                                        break;
                                                }
                                                int unused2 = LegacyFocusStateMapper.this.mAfState = newAfState;
                                            }
                                        } else if (str.equals("auto")) {
                                            c = 0;
                                            switch (c) {
                                                case 0:
                                                case 1:
                                                case 2:
                                                case 3:
                                                    break;
                                            }
                                            int unused3 = LegacyFocusStateMapper.this.mAfState = newAfState;
                                        }
                                    } else if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                                        c = 2;
                                        switch (c) {
                                            case 0:
                                            case 1:
                                            case 2:
                                            case 3:
                                                break;
                                        }
                                        int unused4 = LegacyFocusStateMapper.this.mAfState = newAfState;
                                    }
                                    c = 65535;
                                    switch (c) {
                                        case 0:
                                        case 1:
                                        case 2:
                                        case 3:
                                            break;
                                    }
                                    int unused5 = LegacyFocusStateMapper.this.mAfState = newAfState;
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
        z = true;
        switch (z) {
            case false:
            case true:
            case true:
            case true:
                break;
        }
        switch (afTrigger) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
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
