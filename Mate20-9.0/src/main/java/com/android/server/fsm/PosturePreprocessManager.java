package com.android.server.fsm;

import android.content.Context;
import android.util.Slog;
import huawei.android.hardware.tp.HwTpManager;
import java.io.PrintWriter;

public final class PosturePreprocessManager {
    private static final String TAG = "Fsm_PosturePreprocessManager";
    private static PosturePreprocessManager mInstance = null;
    /* access modifiers changed from: private */
    public Context mContext = null;
    private IntelligentPosturePreprocess mIntelligentPolicy = null;
    private final Object mLock = new Object();
    private NormalPosturePreprocess mNormalPolicy = null;
    private PosturePreprocessPolicy mPolicy = null;
    /* access modifiers changed from: private */
    public PostureStateMachine mPostureSM;
    private int mWakeUpType = 0;

    private class IntelligentPosturePreprocess implements PosturePreprocessPolicy {
        private static final int HANDHELD_MAIN = 1;
        private static final int HANDHELD_SUB = 2;
        private static final int HANDHELD_UNKNOWN = 0;
        private static final String TAG = "Fsm_IntelligentPosturePreprocess";
        private ISensorPostureCallback callback = new ISensorPostureCallback() {
            public void onPostureChange(int posture) {
                int unused = IntelligentPosturePreprocess.this.mSensorPosture = posture;
                IntelligentPosturePreprocess.this.processPosture();
            }
        };
        private int mHandheldState = 0;
        private int mPosture = 100;
        /* access modifiers changed from: private */
        public int mSensorPosture = 100;
        private SensorPostureManager mSensorPostureManager = null;
        private HwTpManager mTpManager = null;

        IntelligentPosturePreprocess(Context context) {
            this.mSensorPostureManager = new SensorPostureManager(PosturePreprocessManager.this.mContext);
            this.mTpManager = HwTpManager.getInstance();
        }

        /* access modifiers changed from: private */
        public void processPosture() {
            Slog.i("Fsm_IntelligentPosturePreprocess", "processPosture HandheldState:" + this.mHandheldState + ", sensorposture:" + this.mSensorPosture + ", mPosture:" + this.mPosture);
            if (PosturePreprocessManager.this.mPostureSM != null) {
                int posture = this.mSensorPosture;
                if (posture != 100) {
                    if (this.mSensorPosture == 102 || this.mSensorPosture == 101 || this.mSensorPosture == 103) {
                        if (this.mHandheldState == 1) {
                            posture = 104;
                        } else if (this.mHandheldState == 2) {
                            posture = 105;
                        }
                    }
                    if (this.mPosture != posture) {
                        this.mPosture = posture;
                        PosturePreprocessManager.this.mPostureSM.setPosture(this.mPosture);
                    }
                }
            }
        }

        public void turnOn(int wakeUpType) {
            Slog.d("Fsm_IntelligentPosturePreprocess", "turnon");
            if (this.mSensorPostureManager == null) {
                Slog.i("Fsm_IntelligentPosturePreprocess", "mSensorPostureManager IS NULL");
            } else if (this.mSensorPostureManager.turnOnPostureSensor(this.callback, wakeUpType)) {
                updateHandheldPosture();
            }
        }

        public void turnOff() {
            Slog.i("Fsm_IntelligentPosturePreprocess", "turnoff");
            if (this.mSensorPostureManager != null) {
                this.mSensorPostureManager.turnOffPostureSensor(this.callback);
                this.mHandheldState = 0;
                this.mSensorPosture = 100;
                this.mPosture = 100;
                return;
            }
            Slog.i("Fsm_IntelligentPosturePreprocess", "mSensorPostureManager IS NULL");
        }

        public void updateHandheldPosture() {
            if (this.mTpManager == null) {
                this.mTpManager = HwTpManager.getInstance();
            }
            this.mHandheldState = this.mTpManager.hwTsSetAftConfig("version:3+grab_gesture");
            processPosture();
        }

        public void dump(String prefix, PrintWriter pw) {
            String innerPrefix = prefix + "  ";
            pw.println(innerPrefix + "IntelligentPosturePreprocess");
        }
    }

    private class NormalPosturePreprocess implements PosturePreprocessPolicy {
        private static final String TAG = "Fsm_NormalPosturePreprocess";
        private ISensorPostureCallback callback = new ISensorPostureCallback() {
            public void onPostureChange(int posture) {
                Slog.i("Fsm_NormalPosturePreprocess", "onFoldStateChange posture:" + posture);
                if (PosturePreprocessManager.this.mPostureSM != null) {
                    PosturePreprocessManager.this.mPostureSM.setPosture(posture);
                }
            }
        };
        private SensorFoldStateManager mSensorFoldStateManager = null;

        NormalPosturePreprocess(Context context) {
            this.mSensorFoldStateManager = new SensorFoldStateManager(PosturePreprocessManager.this.mContext);
        }

        public void turnOn(int wakeUpType) {
            Slog.d("Fsm_NormalPosturePreprocess", "turnon");
            if (this.mSensorFoldStateManager != null) {
                this.mSensorFoldStateManager.turnOnFoldStateSensor(this.callback, wakeUpType);
            } else {
                Slog.i("Fsm_NormalPosturePreprocess", "mSensorFoldStateManager IS NULL");
            }
        }

        public void turnOff() {
            Slog.i("Fsm_NormalPosturePreprocess", "turnoff");
            if (this.mSensorFoldStateManager != null) {
                this.mSensorFoldStateManager.turnOffFoldStateSensor(this.callback);
            } else {
                Slog.i("Fsm_NormalPosturePreprocess", "mSensorFoldStateManager IS NULL");
            }
        }

        public void updateHandheldPosture() {
        }

        public void dump(String prefix, PrintWriter pw) {
            String innerPrefix = prefix + "  ";
            pw.println(innerPrefix + "NormalPosturePreprocess");
        }
    }

    private PosturePreprocessManager() {
    }

    public static synchronized PosturePreprocessManager getInstance() {
        PosturePreprocessManager posturePreprocessManager;
        synchronized (PosturePreprocessManager.class) {
            if (mInstance == null) {
                mInstance = new PosturePreprocessManager();
            }
            posturePreprocessManager = mInstance;
        }
        return posturePreprocessManager;
    }

    public void init(Context context, boolean isIntelligent) {
        if (context == null) {
            Slog.e("Fsm_PosturePreprocessManager", "parameters is null, init failed.");
            return;
        }
        this.mContext = context;
        this.mIntelligentPolicy = new IntelligentPosturePreprocess(this.mContext);
        this.mNormalPolicy = new NormalPosturePreprocess(this.mContext);
        updatePolicy(isIntelligent);
        this.mPostureSM = PostureStateMachine.getInstance();
    }

    public void start(int wakeUpType) {
        this.mWakeUpType = wakeUpType;
        Slog.d("Fsm_PosturePreprocessManager", "start wakeUpType:" + wakeUpType);
        synchronized (this.mLock) {
            this.mPolicy.turnOn(wakeUpType);
        }
    }

    public void stop() {
        Slog.i("Fsm_PosturePreprocessManager", "stop");
        synchronized (this.mLock) {
            this.mPolicy.turnOff();
        }
    }

    public void updatePolicy(boolean isIntelligent) {
        synchronized (this.mLock) {
            if (this.mPolicy != null) {
                this.mPolicy.turnOff();
            }
            if (isIntelligent) {
                this.mPolicy = this.mIntelligentPolicy;
            } else {
                this.mPolicy = this.mNormalPolicy;
            }
            this.mPolicy.turnOn(this.mWakeUpType);
        }
    }

    public void updateHandheldPosture() {
        synchronized (this.mLock) {
            if (this.mPolicy != null) {
                this.mPolicy.updateHandheldPosture();
            }
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
            this.mPolicy.dump(prefix, pw);
        }
    }
}
