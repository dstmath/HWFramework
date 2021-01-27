package com.android.server.rms.iaware.qos;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.huawei.android.os.ProcessExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.server.AnimationThreadEx;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AwareBinderSchedManager {
    private static final int ANIMATION_VIP_DURATION = 500;
    public static final int ANR_TYPE_PROTECTED = 1;
    public static final int ANR_TYPE_VISIBLE_WINDOW = 0;
    private static final int APP_STATUS_BG = 0;
    private static final int APP_STATUS_FG = 1;
    private static final int BINDER_BGCTL_ANR_CHANGED = 3;
    private static final int BINDER_BGCTL_FG_CHANGED = 1;
    private static final int BINDER_BGCTL_SCHED = 4;
    private static final int BINDER_BGCTL_SET_SWITCH = 2;
    private static final int BINDER_LIMIT_OTHERS = 2;
    private static final int BINDER_LIMIT_SCENE_STOP = 0;
    private static final int BINDER_LIMIT_START = 1;
    private static final int BINDER_LIMIT_START_ACTIVITY = 1;
    private static final int BINDER_LIMIT_STOP = 0;
    private static final int BINDER_SCHED_DEBUG_DISABLE = 0;
    private static final int BINDER_SCHED_DEBUG_ENABLE = 1;
    private static final int BINDER_SCHED_SWITCH_BIT = 16;
    private static final String BUNDLE_STR_SCENE = "scene";
    private static final String BUNDLE_STR_STATUS = "status";
    private static final String BUNDLE_STR_UID = "uid";
    private static final boolean DEBUG_SWITCH = SystemPropertiesEx.getBoolean("persist.sys.iaware.bindersched.debug", false);
    private static final int DEFAULT_BINDER_LIMIT_ACTIVITY_DURATION = 10000;
    private static final int DEFAULT_BINDER_LIMIT_OTHERS_DURATION = 2000;
    private static final int DEFAULT_INVAILD_APP_STATUS = -1;
    private static final int DEFAULT_INVAILD_UID = -1;
    private static final Object INSTANCE_LOCK = new Object();
    private static final int INT_LENGTH = 4;
    private static final int LENGTH_OF_BUFFER = 4;
    private static final int MSG_ANIMATION_REMOVE_VIP = 1104;
    private static final int MSG_BASE_VALUE = 1100;
    private static final int MSG_BINDER_SCHED_REPORT_SCENE = 1101;
    private static final int MSG_BINDER_SCHED_SCENE_START = 1102;
    private static final int MSG_BINDER_SCHED_SCENE_STOP = 1103;
    private static final int MSG_UPDATE_PROTECTED_PORCESS = 1110;
    private static final int PRIORITY_FIFO = 1;
    private static final int PRIORITY_NORMAL = 0;
    private static final int RTG_ANIMATION_SWITCH_BIT = 32;
    private static final String TAG = "AwareBinderSchedManager";
    private static final int TIMEOUT_BINDER_OPEN = 2000;
    private static final int VALUE_QOS_HIGH_ADD = 100;
    private static final int VALUE_QOS_HIGH_MINUS = -100;
    private static final int VALUE_QOS_NORMAL_ADD = 1;
    private static final int VALUE_QOS_NORMAL_MINUS = -1;
    private static AwareBinderSchedManager sInstance = null;
    private AtomicBoolean mAnimationFeatureEnable = new AtomicBoolean(false);
    private AtomicInteger mAnimationThreadTid = new AtomicInteger(-1);
    private AtomicInteger mAppStatus = new AtomicInteger(-1);
    private AtomicInteger mBinderLimitScene = new AtomicInteger(0);
    private BinderSchedHandler mBinderSchedHandler = null;
    private AwareSceneRecognizeCallback mCallback = new AwareSceneRecognizeCallback();
    private AtomicInteger mCurUid = new AtomicInteger(-1);
    private AtomicInteger mDefaultInputMethodUid = new AtomicInteger(-1);
    private AtomicBoolean mFeatureEnable = new AtomicBoolean(false);
    private HandlerThread mHandlerThread = null;

    private AwareBinderSchedManager() {
        if (this.mHandlerThread == null) {
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mBinderSchedHandler = new BinderSchedHandler(this.mHandlerThread.getLooper());
        }
    }

    public static AwareBinderSchedManager getInstance() {
        AwareBinderSchedManager awareBinderSchedManager;
        synchronized (INSTANCE_LOCK) {
            if (sInstance == null) {
                sInstance = new AwareBinderSchedManager();
            }
            awareBinderSchedManager = sInstance;
        }
        return awareBinderSchedManager;
    }

    public void enable(int switchValue) {
        boolean featureSwitch = false;
        if ((switchValue & 16) != 0) {
            featureSwitch = true;
        }
        this.mFeatureEnable.set(featureSwitch);
        if ((switchValue & 32) != 0) {
            this.mAnimationFeatureEnable.set(true);
            AtomicInteger atomicInteger = this.mAnimationThreadTid;
            AnimationThreadEx.get();
            atomicInteger.set(AnimationThreadEx.getThreadId());
        } else {
            this.mAnimationFeatureEnable.set(false);
        }
        setDebugSwitch(DEBUG_SWITCH ? 1 : 0);
        initDefaultInputMethod();
        registerAwareSceneRecognize();
    }

    private void initDefaultInputMethod() {
        String defaultInputMethod = AwareIntelligentRecg.getInstance().getDefaultInputMethod();
        this.mDefaultInputMethodUid.set(AwareIntelligentRecg.getInstance().getDefaultInputMethodUid());
        if (this.mDefaultInputMethodUid.get() > 0) {
            reportDefaultInputMethod(this.mDefaultInputMethodUid.get(), defaultInputMethod);
        }
    }

    private void updateProtectedPorcessQos(int uid, int pid) {
        BinderSchedHandler binderSchedHandler = this.mBinderSchedHandler;
        if (binderSchedHandler != null) {
            Message msg = binderSchedHandler.obtainMessage();
            msg.what = MSG_UPDATE_PROTECTED_PORCESS;
            msg.arg1 = uid;
            msg.arg2 = pid;
            this.mBinderSchedHandler.sendMessageDelayed(msg, 2000);
        }
    }

    public void disable() {
        unregisterAwareSceneRecognize();
        setDebugSwitch(0);
        this.mFeatureEnable.set(false);
    }

    private void sendDelayMsg(int msgType, int duration) {
        BinderSchedHandler binderSchedHandler;
        if (this.mFeatureEnable.get() && (binderSchedHandler = this.mBinderSchedHandler) != null) {
            Message msg = binderSchedHandler.obtainMessage();
            msg.what = msgType;
            this.mBinderSchedHandler.sendMessageDelayed(msg, (long) duration);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBinderLimitSceneStart(int sceneType) {
        if (this.mFeatureEnable.get() && this.mBinderSchedHandler != null) {
            if (this.mBinderLimitScene.get() == 0) {
                if (DEBUG_SWITCH) {
                    AwareLog.i(TAG, "binder_sched limit Start sceneType:" + sceneType);
                }
                Message msg = this.mBinderSchedHandler.obtainMessage();
                msg.what = MSG_BINDER_SCHED_SCENE_START;
                Bundle bundle = new Bundle();
                bundle.putInt("scene", sceneType);
                msg.setData(bundle);
                if (sceneType == 4) {
                    this.mBinderLimitScene.set(1);
                } else {
                    this.mBinderLimitScene.set(2);
                }
                this.mBinderSchedHandler.sendMessage(msg);
            } else if (this.mBinderLimitScene.get() == 2) {
                checkPrevLimitScene(sceneType);
            } else if (DEBUG_SWITCH) {
                AwareLog.i(TAG, "binder_sched ignore sceneType:" + sceneType);
            }
        }
    }

    private void checkPrevLimitScene(int sceneType) {
        if (sceneType == 4 && this.mBinderSchedHandler != null) {
            if (DEBUG_SWITCH) {
                AwareLog.i(TAG, "binder_sched checkPrevLimitScene sceneType:" + sceneType);
            }
            this.mBinderSchedHandler.removeMessages(MSG_BINDER_SCHED_SCENE_STOP);
            this.mBinderLimitScene.set(1);
            sendDelayMsg(MSG_BINDER_SCHED_SCENE_STOP, 10000);
        }
    }

    public void setRtgThreadForAnimation(boolean flag) {
        if (this.mAnimationFeatureEnable.get() && this.mBinderSchedHandler != null) {
            if (this.mAnimationThreadTid.get() < 0) {
                AtomicInteger atomicInteger = this.mAnimationThreadTid;
                AnimationThreadEx.get();
                atomicInteger.set(AnimationThreadEx.getThreadId());
            }
            if (flag) {
                this.mBinderSchedHandler.removeMessages(MSG_ANIMATION_REMOVE_VIP);
                logForAnimationThread(flag);
                ProcessExt.setThreadScheduler(this.mAnimationThreadTid.get(), 1, 1);
                Message msg = this.mBinderSchedHandler.obtainMessage();
                msg.what = MSG_ANIMATION_REMOVE_VIP;
                this.mBinderSchedHandler.sendMessageDelayed(msg, 500);
            } else if (this.mBinderSchedHandler.hasMessages(MSG_ANIMATION_REMOVE_VIP)) {
                this.mBinderSchedHandler.removeMessages(MSG_ANIMATION_REMOVE_VIP);
                logForAnimationThread(flag);
                ProcessExt.setThreadScheduler(this.mAnimationThreadTid.get(), 0, 0);
            }
        }
    }

    private void logForAnimationThread(boolean flag) {
        if (DEBUG_SWITCH) {
            StringBuilder sb = new StringBuilder();
            sb.append("setRtgThreadForInflate: ");
            sb.append("rtgFlag:" + flag + CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + "AnimationTid:" + this.mAnimationThreadTid.get());
            AwareLog.d(TAG, sb.toString());
        }
    }

    public void reportProcessStarted(int pid, int uid, String processName) {
        if (this.mFeatureEnable.get() && this.mDefaultInputMethodUid.get() == uid) {
            if (DEBUG_SWITCH) {
                AwareLog.i(TAG, "reportProcessStarted uid:" + uid + " processName:" + processName);
            }
            setProcessQos(uid, pid, true, 1);
            reportFgChanged(pid, uid, true);
            updateProtectedPorcessQos(uid, pid);
        }
    }

    public void reportDefaultInputMethod(int uid, String pkgName) {
        if (this.mFeatureEnable.get()) {
            if (DEBUG_SWITCH) {
                AwareLog.i(TAG, "reportDefaultInputMethod uid:" + uid + " pkgName:" + pkgName);
            }
            this.mDefaultInputMethodUid.set(uid);
            ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
            if (!procList.isEmpty()) {
                int size = procList.size();
                for (int i = 0; i < size; i++) {
                    ProcessInfo info = procList.get(i);
                    if (info != null && uid == info.mUid) {
                        setProcessQos(uid, info.mPid, true, 1);
                        reportFgChanged(info.mPid, uid, true);
                        updateProtectedPorcessQos(uid, info.mPid);
                    }
                }
            }
        }
    }

    public void setProcessQos(int uid, int pid, boolean isSet, int type) {
        if (this.mFeatureEnable.get() && this.mBinderSchedHandler != null && UserHandleEx.getAppId(uid) > 10000) {
            if (pid <= 0) {
                AwareLog.w(TAG, "iawared setProcessQos, pid <= 0");
                return;
            }
            if (DEBUG_SWITCH) {
                AwareLog.i(TAG, "iawared binder_sched uid:" + uid + " type:" + type + " isSet:" + isSet);
            }
            Message msg = this.mBinderSchedHandler.obtainMessage();
            msg.what = MSG_BINDER_SCHED_REPORT_SCENE;
            Bundle bundle = new Bundle();
            bundle.putInt("scene", 3);
            bundle.putInt("uid", pid);
            int value = 1;
            if (type == 1) {
                bundle.putInt(BUNDLE_STR_STATUS, isSet ? 100 : -100);
            } else {
                if (!isSet) {
                    value = -1;
                }
                bundle.putInt(BUNDLE_STR_STATUS, value);
            }
            msg.setData(bundle);
            this.mBinderSchedHandler.sendMessage(msg);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r9v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public void reportFgChanged(int pid, int uid, boolean isCurFg) {
        if (this.mFeatureEnable.get() && this.mBinderSchedHandler != null) {
            if (pid <= 0) {
                AwareLog.w(TAG, "iawared reportSceneToBinder in reportFgChanged, pid <= 0");
            } else if (AwareAppAssociate.getInstance().isSystemUnRemoveApp(uid)) {
                AwareLog.d(TAG, "iawared reportSceneToBinder in reportFgChanged, uid < 10000");
                this.mCurUid.set(-1);
                this.mAppStatus.set(-1);
            } else if (this.mCurUid.get() != uid || this.mAppStatus.get() != isCurFg) {
                Message msg = this.mBinderSchedHandler.obtainMessage();
                msg.what = MSG_BINDER_SCHED_REPORT_SCENE;
                Bundle bundle = new Bundle();
                bundle.putInt("scene", 1);
                bundle.putInt("uid", uid);
                bundle.putInt(BUNDLE_STR_STATUS, isCurFg ? 1 : 0);
                msg.setData(bundle);
                this.mBinderSchedHandler.sendMessage(msg);
                this.mCurUid.set(uid);
                this.mAppStatus.set(isCurFg);
                AwareLog.d(TAG, "curUid: " + this.mCurUid + ", appStatus: " + ((int) isCurFg));
            }
        }
    }

    private void setDebugSwitch(int value) {
        BinderSchedHandler binderSchedHandler;
        if (this.mFeatureEnable.get() && (binderSchedHandler = this.mBinderSchedHandler) != null) {
            Message msg = binderSchedHandler.obtainMessage();
            msg.what = MSG_BINDER_SCHED_REPORT_SCENE;
            Bundle bundle = new Bundle();
            bundle.putInt("scene", 2);
            bundle.putInt("uid", -1);
            bundle.putInt(BUNDLE_STR_STATUS, value);
            msg.setData(bundle);
            this.mBinderSchedHandler.sendMessage(msg);
            AwareLog.i(TAG, "iawared binder_sched setBinderSchedSwitch:" + value);
        }
    }

    /* access modifiers changed from: private */
    public class BinderSchedHandler extends Handler {
        public BinderSchedHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i != AwareBinderSchedManager.MSG_UPDATE_PROTECTED_PORCESS) {
                switch (i) {
                    case AwareBinderSchedManager.MSG_BINDER_SCHED_REPORT_SCENE /* 1101 */:
                        AwareBinderSchedManager.this.doReportSceneToBinder(msg);
                        return;
                    case AwareBinderSchedManager.MSG_BINDER_SCHED_SCENE_START /* 1102 */:
                        AwareBinderSchedManager.this.doBinderSchedStart(msg);
                        return;
                    case AwareBinderSchedManager.MSG_BINDER_SCHED_SCENE_STOP /* 1103 */:
                        AwareBinderSchedManager.this.doBinderSchedStop(msg);
                        return;
                    case AwareBinderSchedManager.MSG_ANIMATION_REMOVE_VIP /* 1104 */:
                        AwareBinderSchedManager.this.doAnimRemoveVip();
                        return;
                    default:
                        return;
                }
            } else {
                int uid = msg.arg1;
                int pid = msg.arg2;
                AwareBinderSchedManager.this.setProcessQos(uid, pid, true, 1);
                AwareBinderSchedManager.this.reportFgChanged(pid, uid, true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doAnimRemoveVip() {
        if (this.mAnimationFeatureEnable.get()) {
            logForAnimationThread(false);
            ProcessExt.setThreadScheduler(this.mAnimationThreadTid.get(), 0, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doBinderSchedStart(Message msg) {
        if (this.mFeatureEnable.get()) {
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putInt(MSG_BINDER_SCHED_REPORT_SCENE);
            buffer.putInt(4);
            buffer.putInt(-1);
            buffer.putInt(1);
            if (!IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position())) {
                AwareLog.w(TAG, "iawared doBinderSchedStart sendPacket failed");
            }
            Bundle bundle = msg.getData();
            if (bundle != null) {
                int durationTime = 10000;
                if (bundle.getInt("scene") != 4) {
                    durationTime = 2000;
                }
                sendDelayMsg(MSG_BINDER_SCHED_SCENE_STOP, durationTime);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doBinderSchedStop(Message msg) {
        if (this.mFeatureEnable.get() && this.mBinderLimitScene.get() != 0) {
            if (DEBUG_SWITCH) {
                AwareLog.i(TAG, "iawared doBinderSchedStop");
            }
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putInt(MSG_BINDER_SCHED_REPORT_SCENE);
            buffer.putInt(4);
            buffer.putInt(-1);
            buffer.putInt(0);
            boolean ret = IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position());
            this.mBinderLimitScene.set(0);
            if (!ret) {
                AwareLog.w(TAG, "iawared doBinderSchedStop sendPacket failed");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doReportSceneToBinder(Message msg) {
        if (this.mFeatureEnable.get()) {
            int msgId = msg.what;
            Bundle bundle = msg.getData();
            if (bundle != null) {
                int scene = bundle.getInt("scene");
                int uid = bundle.getInt("uid");
                int status = bundle.getInt(BUNDLE_STR_STATUS);
                ByteBuffer buffer = ByteBuffer.allocate(16);
                buffer.putInt(msgId);
                buffer.putInt(scene);
                buffer.putInt(uid);
                buffer.putInt(status);
                if (!IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position())) {
                    AwareLog.w(TAG, "iawared doReportSceneToBinder sendPacket failed");
                }
            }
        }
    }

    private void registerAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.registerStateCallback(this.mCallback, 1);
        }
    }

    private void unregisterAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.unregisterStateCallback(this.mCallback);
        }
    }

    /* access modifiers changed from: private */
    public class AwareSceneRecognizeCallback implements AwareSceneRecognize.IAwareSceneRecCallback {
        private AwareSceneRecognizeCallback() {
        }

        @Override // com.android.server.rms.iaware.appmng.AwareSceneRecognize.IAwareSceneRecCallback
        public void onStateChanged(int sceneType, int eventType, String pkgName) {
            if (eventType != 1) {
                return;
            }
            if (sceneType == 2 || sceneType == 4 || sceneType == 8 || sceneType == 16 || sceneType == 32 || sceneType == 64) {
                AwareBinderSchedManager.this.handleBinderLimitSceneStart(sceneType);
            }
        }
    }
}
