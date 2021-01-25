package com.android.server.wm;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.policy.HwGameDockGesture;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.IntelliServiceManager;
import java.util.Calendar;

public class HwDisplayRotationEx extends DefaultDisplayRotationExImpl {
    private static final int DIRECTION_INIT = 100;
    private static final int MAX_REPORT_TIMES = 50;
    private static final int MSG_REPORT_LOG = 2;
    private static final String TAG = "HwDisplayRotationEx";
    private int mDate = Calendar.getInstance().get(5);
    DisplayContentEx mDisplayContent;
    IntelliServiceManager.FaceRotationCallback mFaceRotationCallback = new IntelliServiceManager.FaceRotationCallback() {
        /* class com.android.server.wm.HwDisplayRotationEx.AnonymousClass1 */

        public void onEvent(int faceRotation) {
            HwDisplayRotationEx.this.mService.updateRotationUnchecked(false, false);
        }
    };
    private HwGameDockGesture mGameDockGesture;
    private DefaultGestureNavManager mGestureNavPolicy;
    private Handler mHandler;
    private HwPhoneWindowManager mHwPhoneWindowManager;
    private boolean mIsDefaultDisplay;
    private int mReportCount = 0;
    private int mRotationType = 0;
    private WindowManagerServiceEx mService;
    private int mSwingRotation = 100;

    public HwDisplayRotationEx(WindowManagerServiceEx service, DisplayContentEx displayContent, boolean isDefaultDisplay) {
        super(service, displayContent, isDefaultDisplay);
        this.mHandler = service.getHwHandler();
        this.mIsDefaultDisplay = isDefaultDisplay;
        this.mGestureNavPolicy = (DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class);
        this.mGameDockGesture = (HwGameDockGesture) LocalServices.getService(HwGameDockGesture.class);
        this.mService = service;
        this.mDisplayContent = displayContent;
        WindowManagerPolicyEx hwPolicyEx = this.mService.getPolicyEx();
        if (hwPolicyEx != null) {
            this.mHwPhoneWindowManager = hwPolicyEx.getHwPhoneWindowManager();
        }
    }

    public void setRotation(int rotation) {
        DefaultGestureNavManager defaultGestureNavManager;
        if (this.mIsDefaultDisplay && (defaultGestureNavManager = this.mGestureNavPolicy) != null) {
            defaultGestureNavManager.onRotationChanged(rotation);
        }
        if (this.mIsDefaultDisplay && this.mGameDockGesture != null && HwGameDockGesture.isGameDockGestureFeatureOn()) {
            this.mGameDockGesture.updateOnRotationChange(rotation);
        }
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            try {
                hwAft.notifyOrientationChange(rotation);
            } catch (RemoteException e) {
                Log.e(TAG, "setRotationLw throw RemoteException");
            }
        }
        RemoteException e2 = this.mHwPhoneWindowManager;
        if (e2 != null) {
            e2.setRotationLw(rotation);
        }
    }

    public void setSwingRotation(int rotation) {
        Log.v(TAG, "setSwingRotation " + rotation);
        if (rotation < -2) {
            return;
        }
        if (rotation > 3 && rotation != 100) {
            return;
        }
        if (this.mSwingRotation == -2 && rotation == -1) {
            Slog.i(TAG, "old swing rotation is -2 current is -1 ignore it");
            return;
        }
        this.mSwingRotation = rotation;
        this.mRotationType = 2;
        this.mService.updateRotation(false, false);
        this.mRotationType = 0;
    }

    /* access modifiers changed from: package-private */
    public int swingRotationForSensorRotation(int lastRotation, int sensorRotation) {
        int desireRotation;
        int i = this.mSwingRotation;
        if (i != -2) {
            if (i == -1) {
                desireRotation = lastRotation;
            } else if (i != 100) {
                desireRotation = this.mSwingRotation;
            }
            Slog.i(TAG, "swingRotationForSensor desireRotation " + desireRotation + " swingRotation " + this.mSwingRotation + " sensorRotation " + sensorRotation + " lastRotation " + lastRotation);
            return desireRotation;
        }
        desireRotation = sensorRotation < 0 ? lastRotation : sensorRotation;
        Slog.i(TAG, "swingRotationForSensor desireRotation " + desireRotation + " swingRotation " + this.mSwingRotation + " sensorRotation " + sensorRotation + " lastRotation " + lastRotation);
        return desireRotation;
    }

    public int getSwingRotation(int lastRotation, int sensorRotation) {
        return swingRotationForSensorRotation(lastRotation, sensorRotation);
    }

    public int getRotationType() {
        return this.mRotationType;
    }

    public void setRotationType(int rotationType) {
        this.mRotationType = rotationType;
    }

    public void reportRotation(int rotationType, int oldRotation, int newRotation, String packageName) {
        if (shouldReportLog()) {
            Message msg = Message.obtain();
            msg.what = 2;
            Bundle data = msg.getData();
            data.putInt("rotationType", rotationType);
            data.putInt("oldRotation", oldRotation);
            data.putInt("newRotation", newRotation);
            data.putString("packageName", packageName);
            msg.setData(data);
            this.mHandler.sendMessage(msg);
        }
    }

    private boolean shouldReportLog() {
        if (this.mDisplayContent.getDisplayRotationEx().getUserRotationMode() == WindowManagerPolicyEx.USER_ROTATION_FREE) {
            int now = Calendar.getInstance().get(5);
            if (this.mDate != now) {
                this.mReportCount = 0;
                this.mDate = now;
                return true;
            } else if (this.mReportCount < MAX_REPORT_TIMES) {
                return true;
            }
        }
        return false;
    }

    public void handleReportLog(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            int rotationType = bundle.getInt("rotationType", 0);
            int oldRotation = bundle.getInt("oldRotation", 0);
            int newRotation = bundle.getInt("newRotation", 0);
            String packageName = bundle.getString("packageName", "");
            StringBuilder sb = new StringBuilder();
            if (rotationType == 1) {
                sb.append("{Source:");
                sb.append("sensor");
            } else {
                sb.append("{Source:");
                sb.append("swingFace");
            }
            sb.append(", oldRotation:");
            sb.append(oldRotation);
            sb.append(", newRotation:");
            sb.append(newRotation);
            sb.append(", packageName:");
            sb.append(packageName);
            sb.append("}");
            String content = sb.toString();
            this.mReportCount++;
            Flog.bdReport(991311001, content);
        }
    }

    public void setSensorRotation(int rotation) {
        IntelliServiceManager.setSensorRotation(rotation);
        if (HwMwUtils.ENABLED) {
            HwMwUtils.performPolicy(23, new Object[]{Integer.valueOf(rotation)});
        }
    }

    public boolean isIntelliServiceEnabled(int orientatin) {
        return IntelliServiceManager.isIntelliServiceEnabled(this.mService.getContext(), orientatin, this.mService.getCurrentUserId());
    }

    public void startIntelliService() {
        IntelliServiceManager.getInstance(this.mService.getContext()).startIntelliService(this.mFaceRotationCallback);
    }

    public void startIntelliService(final int orientation) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.wm.HwDisplayRotationEx.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                if (HwDisplayRotationEx.this.isIntelliServiceEnabled(orientation)) {
                    IntelliServiceManager.getInstance(HwDisplayRotationEx.this.mService.getContext()).startIntelliService(HwDisplayRotationEx.this.mFaceRotationCallback);
                } else {
                    IntelliServiceManager.getInstance(HwDisplayRotationEx.this.mService.getContext()).setKeepPortrait(false);
                }
            }
        });
    }

    public int getRotationFromSensorOrFace(int sensor) {
        if (IntelliServiceManager.getInstance(this.mService.getContext()).isKeepPortrait()) {
            Slog.d("IntelliServiceManager", "portraitRotaion:0");
            return 0;
        } else if (IntelliServiceManager.getInstance(this.mService.getContext()).getFaceRotaion() != -2) {
            int sensorRotation = IntelliServiceManager.getInstance(this.mService.getContext()).getFaceRotaion();
            Slog.d("IntelliServiceManager", "faceRotation:" + sensorRotation);
            return sensorRotation;
        } else {
            Slog.d("IntelliServiceManager", "sensor:" + sensor);
            return sensor;
        }
    }
}
