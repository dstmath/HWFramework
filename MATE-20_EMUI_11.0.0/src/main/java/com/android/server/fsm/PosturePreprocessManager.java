package com.android.server.fsm;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import huawei.android.hardware.tp.HwTpManager;
import java.io.PrintWriter;

public final class PosturePreprocessManager {
    private static final String TAG = "Fsm_PosturePreprocessManager";
    private static final String TAG_FSM_TURNOFF = "turnoff";
    private static final String TAG_FSM_TURNON = "turnon";
    private static PosturePreprocessManager sInstance = null;
    private Context mContext = null;
    private IntelligentPosturePreprocess mIntelligentPolicy = null;
    private final Object mLock = new Object();
    private NormalPosturePreprocess mNormalPolicy = null;
    private PosturePreprocessPolicy mPolicy = null;
    private PostureStateMachine mPostureSm;
    private SensorFoldStateManager mSensorFoldStateManager = null;
    private TentPosturePreprocess mTentPolicy = null;
    private TestPosturePreprocess mTestPolicy = null;
    private int mWakeUpType = 0;

    private PosturePreprocessManager() {
    }

    protected static synchronized PosturePreprocessManager getInstance() {
        PosturePreprocessManager posturePreprocessManager;
        synchronized (PosturePreprocessManager.class) {
            if (sInstance == null) {
                sInstance = new PosturePreprocessManager();
            }
            posturePreprocessManager = sInstance;
        }
        return posturePreprocessManager;
    }

    /* access modifiers changed from: protected */
    public void init(Context context, int policy) {
        if (context == null) {
            Slog.e("Fsm_PosturePreprocessManager", "parameters is null, init failed.");
            return;
        }
        this.mContext = context;
        this.mSensorFoldStateManager = new SensorFoldStateManager(this.mContext);
        this.mIntelligentPolicy = new IntelligentPosturePreprocess();
        this.mNormalPolicy = new NormalPosturePreprocess();
        this.mTentPolicy = new TentPosturePreprocess();
        this.mTestPolicy = new TestPosturePreprocess();
        this.mPostureSm = PostureStateMachine.getInstance();
        updatePolicy(policy);
    }

    /* access modifiers changed from: protected */
    public void start(int wakeUpType) {
        this.mWakeUpType = wakeUpType;
        Slog.d("Fsm_PosturePreprocessManager", "start wakeUpType:" + wakeUpType);
        synchronized (this.mLock) {
            this.mPolicy.turnOn(wakeUpType);
        }
    }

    /* access modifiers changed from: protected */
    public void stop() {
        Slog.i("Fsm_PosturePreprocessManager", "stop");
        synchronized (this.mLock) {
            this.mPolicy.turnOff();
        }
    }

    /* access modifiers changed from: protected */
    public int getWakeUpType() {
        return this.mWakeUpType;
    }

    /* access modifiers changed from: protected */
    public void updatePolicy(int type) {
        PosturePreprocessPolicy policy;
        synchronized (this.mLock) {
            if (type == 0) {
                policy = this.mNormalPolicy;
            } else if (type == 1) {
                policy = this.mIntelligentPolicy;
            } else if (type == 2) {
                policy = this.mTentPolicy;
            } else if (type != 3) {
                try {
                    Slog.w("Fsm_PosturePreprocessManager", "invalid type" + type);
                    return;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                policy = this.mTestPolicy;
            }
            if (this.mPolicy != null) {
                this.mPolicy.turnOff();
            }
            this.mPolicy = policy;
            this.mPolicy.turnOn(this.mWakeUpType);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
            this.mPolicy.dump(prefix, pw);
        }
    }

    /* access modifiers changed from: private */
    public class IntelligentPosturePreprocess implements PosturePreprocessPolicy {
        private static final int HANDHELD_MAIN = 1;
        private static final int HANDHELD_SUB = 2;
        private static final int HANDHELD_UNKNOWN = 0;
        private static final String TAG = "Fsm_IntelligentPosturePreprocess";
        private ISensorPostureCallback mCallback = new ISensorPostureCallback() {
            /* class com.android.server.fsm.PosturePreprocessManager.IntelligentPosturePreprocess.AnonymousClass1 */

            @Override // com.android.server.fsm.ISensorPostureCallback
            public void onPostureChange(int posture) {
                if (IntelligentPosturePreprocess.this.isFolded(posture)) {
                    IntelligentPosturePreprocess.this.updateHandheldPosture();
                } else {
                    IntelligentPosturePreprocess.this.mHandheldState = 0;
                }
                IntelligentPosturePreprocess.this.mSensorPosture = posture;
                IntelligentPosturePreprocess.this.processPosture();
            }
        };
        private int mHandheldState = 0;
        private int mPosture = 100;
        private int mSensorPosture = 100;
        private SensorPostureManager mSensorPostureManager = null;
        private HwTpManager mTpManager = null;

        IntelligentPosturePreprocess() {
            this.mSensorPostureManager = new SensorPostureManager(PosturePreprocessManager.this.mContext);
            this.mTpManager = HwTpManager.getInstance();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void processPosture() {
            Slog.i("Fsm_IntelligentPosturePreprocess", "processPosture HandheldState:" + this.mHandheldState + ", sensorposture:" + this.mSensorPosture + ", mPosture:" + this.mPosture);
            if (this.mSensorPosture != 100 && PosturePreprocessManager.this.mPostureSm != null) {
                int posture = this.mSensorPosture;
                int i = this.mHandheldState;
                if (i == 1) {
                    if (isFolded(this.mSensorPosture)) {
                        posture = 104;
                    }
                } else if (i == 2 && isFolded(this.mSensorPosture)) {
                    posture = 105;
                }
                if (this.mPosture != posture) {
                    this.mPosture = posture;
                    PosturePreprocessManager.this.mPostureSm.setPosture(this.mPosture);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isFolded(int posture) {
            if (posture == 102 || posture == 101 || posture == 103) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateHandheldPosture() {
            if (this.mTpManager == null) {
                this.mTpManager = HwTpManager.getInstance();
            }
            this.mHandheldState = this.mTpManager.hwTsSetAftConfig("version:3+grab_gesture");
            Slog.i("Fsm_IntelligentPosturePreprocess", "updateHandheldPosture mHandheldState: " + this.mHandheldState);
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void turnOn(int wakeUpType) {
            Slog.d("Fsm_IntelligentPosturePreprocess", PosturePreprocessManager.TAG_FSM_TURNON);
            SensorPostureManager sensorPostureManager = this.mSensorPostureManager;
            if (sensorPostureManager == null) {
                Slog.i("Fsm_IntelligentPosturePreprocess", "mSensorPostureManager IS NULL");
            } else if (sensorPostureManager.turnOnPostureSensor(this.mCallback, wakeUpType)) {
                updateHandheldPosture();
            }
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void turnOff() {
            Slog.i("Fsm_IntelligentPosturePreprocess", PosturePreprocessManager.TAG_FSM_TURNOFF);
            SensorPostureManager sensorPostureManager = this.mSensorPostureManager;
            if (sensorPostureManager != null) {
                sensorPostureManager.turnOffPostureSensor(this.mCallback);
                this.mHandheldState = 0;
                this.mSensorPosture = 100;
                this.mPosture = 100;
                return;
            }
            Slog.i("Fsm_IntelligentPosturePreprocess", "mSensorPostureManager IS NULL");
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void dump(String prefix, PrintWriter pw) {
            pw.println((prefix + "  ") + "IntelligentPosturePreprocess");
        }
    }

    /* access modifiers changed from: private */
    public class NormalPosturePreprocess implements PosturePreprocessPolicy {
        private static final String TAG = "Fsm_NormalPosturePreprocess";
        private ISensorPostureCallback mCallback = new ISensorPostureCallback() {
            /* class com.android.server.fsm.PosturePreprocessManager.NormalPosturePreprocess.AnonymousClass1 */

            @Override // com.android.server.fsm.ISensorPostureCallback
            public void onPostureChange(int posture) {
                Slog.i("Fsm_NormalPosturePreprocess", "onFoldStateChange posture:" + posture);
                if (PosturePreprocessManager.this.mPostureSm == null) {
                    return;
                }
                if (posture == 103) {
                    PosturePreprocessManager.this.mPostureSm.setPosture(posture, true);
                } else {
                    PosturePreprocessManager.this.mPostureSm.setPosture(posture, false);
                }
            }
        };

        NormalPosturePreprocess() {
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void turnOn(int wakeUpType) {
            Slog.d("Fsm_NormalPosturePreprocess", PosturePreprocessManager.TAG_FSM_TURNON);
            if (PosturePreprocessManager.this.mSensorFoldStateManager != null) {
                PosturePreprocessManager.this.mSensorFoldStateManager.turnOnFoldStateSensor(this.mCallback, wakeUpType);
            } else {
                Slog.i("Fsm_NormalPosturePreprocess", "mSensorFoldStateManager IS NULL");
            }
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void turnOff() {
            Slog.i("Fsm_NormalPosturePreprocess", PosturePreprocessManager.TAG_FSM_TURNOFF);
            if (PosturePreprocessManager.this.mSensorFoldStateManager != null) {
                PosturePreprocessManager.this.mSensorFoldStateManager.turnOffFoldStateSensor(this.mCallback);
            } else {
                Slog.i("Fsm_NormalPosturePreprocess", "mSensorFoldStateManager IS NULL");
            }
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void dump(String prefix, PrintWriter pw) {
            pw.println((prefix + "  ") + "NormalPosturePreprocess");
        }
    }

    /* access modifiers changed from: private */
    public class TestPosturePreprocess implements PosturePreprocessPolicy {
        private static final String TAG = "Fsm_TestPosturePreprocess";

        private TestPosturePreprocess() {
        }

        private void processTestPosture() {
            int posture = 109;
            int mode = SystemProperties.getInt("persist.sys.foldDispMode", 0);
            if (mode == 1) {
                posture = 109;
            } else if (mode == 2) {
                posture = 101;
            } else if (mode == 3) {
                posture = 102;
            } else if (mode == 5) {
                posture = 110;
            } else if (mode != 6) {
                Slog.w("Fsm_TestPosturePreprocess", "getInitState mode = " + mode);
            } else {
                posture = 111;
            }
            Slog.i("Fsm_TestPosturePreprocess", "TestPosture posture = " + posture + ", mode = " + mode);
            PosturePreprocessManager.this.mPostureSm.setPosture(posture);
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void turnOn(int wakeUpType) {
            Slog.d("Fsm_TestPosturePreprocess", PosturePreprocessManager.TAG_FSM_TURNON);
            processTestPosture();
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void turnOff() {
            Slog.i("Fsm_TestPosturePreprocess", PosturePreprocessManager.TAG_FSM_TURNOFF);
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void dump(String prefix, PrintWriter pw) {
            pw.println((prefix + "  ") + "TestPosturePreprocess");
        }
    }

    /* access modifiers changed from: private */
    public class TentPosturePreprocess implements PosturePreprocessPolicy {
        private static final String TAG = "Fsm_TestPosturePreprocess";
        private ISensorPostureCallback mCallback;

        private TentPosturePreprocess() {
            this.mCallback = new ISensorPostureCallback() {
                /* class com.android.server.fsm.PosturePreprocessManager.TentPosturePreprocess.AnonymousClass1 */

                @Override // com.android.server.fsm.ISensorPostureCallback
                public void onPostureChange(int posture) {
                    Slog.i("Fsm_TestPosturePreprocess", "TentPosturePreprocess posture:" + posture);
                }
            };
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void turnOn(int wakeUpType) {
            Slog.d("Fsm_TestPosturePreprocess", PosturePreprocessManager.TAG_FSM_TURNON);
            if (PosturePreprocessManager.this.mSensorFoldStateManager != null) {
                PosturePreprocessManager.this.mSensorFoldStateManager.turnOnFoldStateSensor(this.mCallback, wakeUpType);
            } else {
                Slog.i("Fsm_TestPosturePreprocess", "mSensorFoldStateManager IS NULL");
            }
            PosturePreprocessManager.this.mPostureSm.setPosture(103);
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void turnOff() {
            Slog.i("Fsm_TestPosturePreprocess", PosturePreprocessManager.TAG_FSM_TURNOFF);
            if (PosturePreprocessManager.this.mSensorFoldStateManager != null) {
                PosturePreprocessManager.this.mSensorFoldStateManager.turnOffFoldStateSensor(this.mCallback);
            } else {
                Slog.i("Fsm_TestPosturePreprocess", "mSensorFoldStateManager IS NULL");
            }
        }

        @Override // com.android.server.fsm.PosturePreprocessPolicy
        public void dump(String prefix, PrintWriter pw) {
            pw.println((prefix + "  ") + "TentPosturePreprocess");
        }
    }
}
