package com.android.server.mtm.iaware.srms;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.ServiceManager;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.jankshield.TableJankEvent;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.pm.PackageManagerService;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize.IAwareSceneRecCallback;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class AwareBroadcastPolicy {
    private static final int BROADCAST_PROXY_SPEED_INDEX = 2;
    private static final int BROADCAST_PROXY_SPEED_INTERVAL = 100;
    private static final long BROADCAST_PROXY_SPEED_INTERVAL_LONG = 100;
    private static final int BROADCAST_PROXY_SPEED_NUMBER = 10;
    private static final int BROADCAST_PROXY_SPEED_NUMBER_INDEX_MAX = 9;
    private static final int IAWARE_DWONLOAD_DELAY_TIME = 3000;
    private static final int IAWARE_START_ACTIVITY_DELAY_TIME = 3000;
    private static final int LOOPER_CHECK_TIME = 1000;
    private static final int LOOPER_CHECK_TIME_RECOUNT = 2000;
    private static final int MSG_POLICY_DL_END = 202;
    private static final int MSG_POLICY_DL_START = 201;
    private static final int MSG_POLICY_END_CHECK = 206;
    private static final int MSG_POLICY_SCENE_ACTIVITY = 205;
    private static final int MSG_POLICY_SCENE_APP = 204;
    private static final int MSG_POLICY_SCENE_SLIP = 203;
    private static final int MSG_START_ACTIVITY_TIMEOUT = 207;
    private static final String TAG = "AwareBroadcastPolicy";
    private AwareBroadcastConfig mAwareBroadcastConfig;
    private AwareSceneStateCallback mAwareSceneStateCallback;
    private AwareStateCallback mAwareStateCallback;
    private AwareBroadcastProcess mBgIawareBr;
    private long mCountCheck;
    private AwareBroadcastProcess mFgIawareBr;
    private int mForegroundAppLevel;
    private final IawareBroadcastPolicyHandler mHandler;
    private HwActivityManagerService mHwAMS;
    private ArraySet<Integer> mIawareDownloadingUid;
    private ArraySet<String> mIawareNoProxyActions;
    private ArraySet<String> mIawareNoProxyPkgs;
    private boolean mIawareProxyActivitStart;
    private boolean mIawareProxyAppStart;
    private boolean mIawareProxySlip;
    private ArraySet<String> mIawareProxySysPkgs;
    private boolean mIawareScreenOn;
    private long mLastParallelBrTime;
    private Object mLockNoProxyActions;
    private Object mLockNoProxyPkgs;
    private Object mLockProxySysPkgs;
    private int mNoTouchCheckCount;
    private final long[][] mProxyCount;
    private boolean mSpeedParallelStartProxy;
    private long mStartParallelBrTime;
    private int mTouchCheckCount;

    private class AwareSceneStateCallback implements IAwareSceneRecCallback {
        private AwareSceneStateCallback() {
        }

        public void onStateChanged(int sceneType, int eventType, String pkg) {
            if (BroadcastFeature.isFeatureEnabled(AwareBroadcastPolicy.BROADCAST_PROXY_SPEED_NUMBER)) {
                Message msg;
                if (sceneType == AwareBroadcastPolicy.BROADCAST_PROXY_SPEED_INDEX) {
                    msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = AwareBroadcastPolicy.MSG_POLICY_SCENE_SLIP;
                    msg.arg1 = eventType;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
                } else if (sceneType == 4) {
                    msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = AwareBroadcastPolicy.MSG_POLICY_SCENE_APP;
                    msg.arg1 = eventType;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
                } else if (sceneType == 8) {
                    msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = AwareBroadcastPolicy.MSG_POLICY_SCENE_ACTIVITY;
                    msg.arg1 = eventType;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
                    if (AwareBroadcastPolicy.this.mHandler.hasMessages(AwareBroadcastPolicy.MSG_START_ACTIVITY_TIMEOUT)) {
                        AwareBroadcastPolicy.this.mHandler.removeMessages(AwareBroadcastPolicy.MSG_START_ACTIVITY_TIMEOUT);
                    }
                    if (eventType == 1) {
                        AwareBroadcastPolicy.this.mHandler.sendEmptyMessageDelayed(AwareBroadcastPolicy.MSG_START_ACTIVITY_TIMEOUT, 3000);
                    }
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(AwareBroadcastPolicy.TAG, "don't process scene type " + sceneType);
                }
            }
        }
    }

    private class AwareStateCallback implements IAwareStateCallback {
        private AwareStateCallback() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            if (BroadcastFeature.isFeatureEnabled(AwareBroadcastPolicy.BROADCAST_PROXY_SPEED_NUMBER) && stateType == 5 && uid >= 0) {
                Message msg;
                if (eventType == 1) {
                    msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = AwareBroadcastPolicy.MSG_POLICY_DL_START;
                    msg.arg1 = uid;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
                } else if (eventType == AwareBroadcastPolicy.BROADCAST_PROXY_SPEED_INDEX) {
                    msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = AwareBroadcastPolicy.MSG_POLICY_DL_END;
                    msg.arg1 = uid;
                    AwareBroadcastPolicy.this.mHandler.sendMessageDelayed(msg, 3000);
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(AwareBroadcastPolicy.TAG, "don't process type " + eventType);
                }
            }
        }
    }

    private final class IawareBroadcastPolicyHandler extends Handler {
        public IawareBroadcastPolicyHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            ArraySet -get1;
            switch (msg.what) {
                case AwareBroadcastPolicy.MSG_POLICY_DL_START /*201*/:
                    -get1 = AwareBroadcastPolicy.this.mIawareDownloadingUid;
                    synchronized (-get1) {
                        break;
                    }
                    AwareBroadcastPolicy.this.mIawareDownloadingUid.add(Integer.valueOf(msg.arg1));
                    break;
                case AwareBroadcastPolicy.MSG_POLICY_DL_END /*202*/:
                    -get1 = AwareBroadcastPolicy.this.mIawareDownloadingUid;
                    synchronized (-get1) {
                        break;
                    }
                    AwareBroadcastPolicy.this.mIawareDownloadingUid.remove(Integer.valueOf(msg.arg1));
                    break;
                case AwareBroadcastPolicy.MSG_POLICY_SCENE_SLIP /*203*/:
                    AwareBroadcastPolicy.this.setIawarePolicy(AwareBroadcastPolicy.BROADCAST_PROXY_SPEED_INDEX, msg.arg1);
                    return;
                case AwareBroadcastPolicy.MSG_POLICY_SCENE_APP /*204*/:
                    AwareBroadcastPolicy.this.setIawarePolicy(4, msg.arg1);
                    return;
                case AwareBroadcastPolicy.MSG_POLICY_SCENE_ACTIVITY /*205*/:
                    AwareBroadcastPolicy.this.setIawarePolicy(8, msg.arg1);
                    return;
                case AwareBroadcastPolicy.MSG_POLICY_END_CHECK /*206*/:
                    AwareBroadcastPolicy.this.startUnproxyBroadcast();
                    return;
                case AwareBroadcastPolicy.MSG_START_ACTIVITY_TIMEOUT /*207*/:
                    AwareBroadcastPolicy.this.mIawareProxyActivitStart = false;
                    return;
                default:
                    return;
            }
        }
    }

    public AwareBroadcastPolicy(Handler handler) {
        this.mIawareDownloadingUid = new ArraySet();
        this.mIawareNoProxyActions = new ArraySet();
        this.mIawareNoProxyPkgs = new ArraySet();
        this.mIawareProxySysPkgs = new ArraySet();
        this.mLockNoProxyActions = new Object();
        this.mLockNoProxyPkgs = new Object();
        this.mLockProxySysPkgs = new Object();
        this.mHwAMS = HwActivityManagerService.self();
        this.mIawareProxySlip = false;
        this.mIawareProxyAppStart = false;
        this.mIawareProxyActivitStart = false;
        this.mIawareScreenOn = true;
        this.mStartParallelBrTime = 0;
        this.mLastParallelBrTime = 0;
        this.mCountCheck = 0;
        this.mSpeedParallelStartProxy = false;
        this.mForegroundAppLevel = 3;
        this.mProxyCount = (long[][]) Array.newInstance(Long.TYPE, new int[]{BROADCAST_PROXY_SPEED_NUMBER, BROADCAST_PROXY_SPEED_INDEX});
        this.mTouchCheckCount = 60;
        this.mNoTouchCheckCount = WifiProCommonUtils.HTTP_REACHALBE_HOME;
        this.mBgIawareBr = null;
        this.mFgIawareBr = null;
        this.mBgIawareBr = new AwareBroadcastProcess(this, handler, "iawarebackground");
        this.mFgIawareBr = new AwareBroadcastProcess(this, handler, "iawareforeground");
        this.mHandler = new IawareBroadcastPolicyHandler(handler.getLooper());
        this.mAwareBroadcastConfig = AwareBroadcastConfig.getInstance();
    }

    public void init() {
        this.mAwareStateCallback = new AwareStateCallback();
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 5);
        this.mAwareSceneStateCallback = new AwareSceneStateCallback();
        AwareSceneRecognize.getInstance().registerStateCallback(this.mAwareSceneStateCallback, 1);
        this.mAwareBroadcastConfig.doinit();
    }

    public boolean enqueueIawareProxyBroacast(boolean isParallel, HwBroadcastRecord r) {
        if (r == null) {
            return false;
        }
        if (r.isBg()) {
            this.mBgIawareBr.enqueueIawareProxyBroacast(isParallel, r);
        } else {
            this.mFgIawareBr.enqueueIawareProxyBroacast(isParallel, r);
        }
        return true;
    }

    public boolean shouldIawareProxyBroadcast(String brAction, int callingPid, int receiverUid, int receiverPid, String recevierPkg) {
        if (!isAppForeground(receiverUid) || isProxyPkg(recevierPkg, receiverPid)) {
            synchronized (this.mIawareDownloadingUid) {
                if (this.mIawareDownloadingUid.contains(Integer.valueOf(receiverUid))) {
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.d(TAG, "iaware_br : downloading, don't proxy : " + recevierPkg + ": action : " + brAction);
                    }
                    return false;
                }
                if (isSystemServerBroadcast(callingPid) && !isForbidProxy(brAction, recevierPkg) && isIawarePrepared()) {
                    return true;
                }
                return false;
            }
        }
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.d(TAG, "iaware_br foreground : " + recevierPkg);
        }
        return false;
    }

    private boolean isIawarePrepared() {
        if (this.mFgIawareBr == null || this.mBgIawareBr == null) {
            return false;
        }
        return true;
    }

    private boolean isAppForeground(int uid) {
        if (this.mHwAMS == null || this.mHwAMS.iawareGetUidState(uid) <= this.mForegroundAppLevel) {
            return true;
        }
        return false;
    }

    private boolean isForbidProxy(String action, String pkg) {
        synchronized (this.mLockNoProxyActions) {
            boolean noProxyAction = this.mIawareNoProxyActions.contains(action);
        }
        if (noProxyAction) {
            return true;
        }
        boolean noProxyPkg;
        synchronized (this.mLockNoProxyPkgs) {
            noProxyPkg = this.mIawareNoProxyPkgs.contains(pkg);
        }
        return noProxyPkg;
    }

    public boolean isProxyedAllowedCondition() {
        return this.mIawareScreenOn ? this.mSpeedParallelStartProxy : false;
    }

    private AwareProcessBaseInfo getAwareProcessBaseInfo(int pid) {
        return this.mHwAMS != null ? this.mHwAMS.getProcessBaseInfo(pid) : null;
    }

    private boolean isProxyPkg(String pkg, int pid) {
        boolean proxySysPkg = isProxySysPkg(pkg);
        AwareProcessBaseInfo processInfo = getAwareProcessBaseInfo(pid);
        if (processInfo == null || processInfo.mForegroundActivities) {
            return false;
        }
        return proxySysPkg || processInfo.mCurAdj >= HwActivityManagerService.SERVICE_ADJ;
    }

    public boolean isProxySysPkg(String pkg) {
        boolean contains;
        synchronized (this.mLockProxySysPkgs) {
            contains = this.mIawareProxySysPkgs.contains(pkg);
        }
        return contains;
    }

    public void updateXmlConfig() {
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "updateXmlConfig begin");
        }
        if (isIawarePrepared()) {
            synchronized (this.mLockNoProxyActions) {
                this.mIawareNoProxyActions = this.mAwareBroadcastConfig.getUnProxyActionList();
            }
            synchronized (this.mLockNoProxyPkgs) {
                this.mIawareNoProxyPkgs = this.mAwareBroadcastConfig.getUnProxyPkgList();
            }
            synchronized (this.mLockProxySysPkgs) {
                this.mIawareProxySysPkgs = this.mAwareBroadcastConfig.getProxySysPkgList();
            }
            this.mForegroundAppLevel = this.mAwareBroadcastConfig.getFGAppLevel();
            this.mNoTouchCheckCount = this.mAwareBroadcastConfig.getNoTouchCheckCount();
            this.mTouchCheckCount = this.mAwareBroadcastConfig.getTouchCheckCount();
            this.mBgIawareBr.setUnProxyMaxDuration(this.mAwareBroadcastConfig.getUnProxyMaxDuration());
            this.mBgIawareBr.setUnProxyMaxSpeed(this.mAwareBroadcastConfig.getUnProxyMaxSpeed());
            this.mBgIawareBr.setUnProxyMinSpeed(this.mAwareBroadcastConfig.getUnProxyMinSpeed());
            this.mFgIawareBr.setUnProxyMaxDuration(this.mAwareBroadcastConfig.getUnProxyMaxDuration());
            this.mFgIawareBr.setUnProxyMaxSpeed(this.mAwareBroadcastConfig.getUnProxyMaxSpeed());
            this.mFgIawareBr.setUnProxyMinSpeed(this.mAwareBroadcastConfig.getUnProxyMinSpeed());
            return;
        }
        AwareLog.e(TAG, "iaware process broacast don't prepared.");
    }

    public void iawareStartCountBroadcastSpeed(boolean isParallel, long dispatchClockTime, int size) {
        if (this.mIawareScreenOn && !this.mSpeedParallelStartProxy && isIawarePrepared() && isParallel) {
            checkParallCount(dispatchClockTime, size);
        }
    }

    public void endCheckCount() {
        if (isIawarePrepared()) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = MSG_POLICY_END_CHECK;
            this.mHandler.sendMessage(msg);
        }
    }

    private void startUnproxyBroadcast() {
        if (isEmptyIawareBrList()) {
            this.mSpeedParallelStartProxy = false;
            return;
        }
        this.mBgIawareBr.starUnproxyBroadcast();
        this.mFgIawareBr.starUnproxyBroadcast();
    }

    private void checkParallCount(long dispatchClockTime, int size) {
        this.mCountCheck = 0;
        int index;
        if (this.mStartParallelBrTime == 0) {
            long[] jArr = this.mProxyCount[0];
            this.mStartParallelBrTime = dispatchClockTime;
            jArr[0] = dispatchClockTime;
            jArr = this.mProxyCount[0];
            this.mCountCheck = (long) size;
            jArr[1] = (long) size;
            for (index = 1; index < BROADCAST_PROXY_SPEED_NUMBER; index++) {
                this.mProxyCount[index][0] = this.mStartParallelBrTime + (((long) index) * BROADCAST_PROXY_SPEED_INTERVAL_LONG);
                this.mProxyCount[index][1] = 0;
            }
            setProxyCount();
            return;
        }
        this.mLastParallelBrTime = dispatchClockTime;
        long tempPeriod = this.mLastParallelBrTime - this.mStartParallelBrTime;
        if (tempPeriod < 0) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br checkcount <0");
            }
            this.mStartParallelBrTime = 0;
            checkParallCount(dispatchClockTime, size);
        }
        if (tempPeriod >= TableJankEvent.recMAXCOUNT) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br checkcount >2000");
            }
            this.mStartParallelBrTime = 0;
            checkParallCount(dispatchClockTime, size);
        } else if (tempPeriod >= 0 && tempPeriod < 1000) {
            index = (int) (tempPeriod / BROADCAST_PROXY_SPEED_INTERVAL_LONG);
            this.mProxyCount[index][1] = this.mProxyCount[index][1] + ((long) size);
            for (tempIndex = 0; tempIndex <= index; tempIndex++) {
                this.mCountCheck += this.mProxyCount[tempIndex][1];
            }
            setProxyCount();
        } else if (tempPeriod >= 1000) {
            index = (int) ((tempPeriod - 1000) / BROADCAST_PROXY_SPEED_INTERVAL_LONG);
            if (index == BROADCAST_PROXY_SPEED_NUMBER_INDEX_MAX) {
                this.mStartParallelBrTime = 0;
                checkParallCount(dispatchClockTime, size);
            } else if (index < BROADCAST_PROXY_SPEED_NUMBER_INDEX_MAX) {
                this.mStartParallelBrTime = this.mProxyCount[index + 1][0];
                int rIndex = index;
                for (tempIndex = 0; tempIndex < BROADCAST_PROXY_SPEED_NUMBER; tempIndex++) {
                    rIndex++;
                    if (rIndex < BROADCAST_PROXY_SPEED_NUMBER) {
                        this.mProxyCount[tempIndex][0] = this.mProxyCount[rIndex][0];
                        this.mProxyCount[tempIndex][1] = this.mProxyCount[rIndex][1];
                    } else if (tempIndex < BROADCAST_PROXY_SPEED_NUMBER_INDEX_MAX) {
                        this.mProxyCount[tempIndex][0] = this.mProxyCount[tempIndex - 1][0] + BROADCAST_PROXY_SPEED_INTERVAL_LONG;
                        this.mProxyCount[tempIndex][1] = 0;
                    } else {
                        this.mProxyCount[tempIndex][0] = this.mProxyCount[tempIndex - 1][0] + BROADCAST_PROXY_SPEED_INTERVAL_LONG;
                        this.mProxyCount[tempIndex][1] = (long) size;
                    }
                }
                for (int countIndex = 0; countIndex < BROADCAST_PROXY_SPEED_NUMBER; countIndex++) {
                    this.mCountCheck += this.mProxyCount[countIndex][1];
                }
                setProxyCount();
            }
        }
    }

    private void setProxyCount() {
        if (isStrictCondition()) {
            if (this.mCountCheck > ((long) this.mTouchCheckCount)) {
                if (AwareBroadcastDebug.getDebug()) {
                    AwareLog.i(TAG, "iaware_br checkcount touch and receiver > " + this.mTouchCheckCount);
                }
                this.mSpeedParallelStartProxy = true;
            }
        } else if (this.mCountCheck > ((long) this.mNoTouchCheckCount)) {
            if (AwareBroadcastDebug.getDebug()) {
                AwareLog.i(TAG, "iaware_br checkcount no touch and receiver > " + this.mNoTouchCheckCount);
            }
            this.mSpeedParallelStartProxy = true;
        }
    }

    public void reportSysEvent(int event) {
        if (BroadcastFeature.isFeatureEnabled(BROADCAST_PROXY_SPEED_NUMBER)) {
            switch (event) {
                case 20011:
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.d(TAG, "iaware_br dev status event screen on");
                    }
                    this.mIawareScreenOn = true;
                    break;
                case 90011:
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.d(TAG, "iaware_br dev status event screen off");
                    }
                    this.mIawareScreenOn = false;
                    resetUnproxySpeedScreenOff();
                    break;
            }
        }
    }

    private void resetUnproxySpeedScreenOff() {
        if (isIawarePrepared()) {
            this.mBgIawareBr.setUnProxySpeedScreenOff();
            this.mFgIawareBr.setUnProxySpeedScreenOff();
        }
    }

    private void setIawarePolicy(int type, int event) {
        switch (type) {
            case BROADCAST_PROXY_SPEED_INDEX /*2*/:
                if (event == 1) {
                    this.mIawareProxySlip = true;
                } else if (event == 0) {
                    this.mIawareProxySlip = false;
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "don't process event " + event);
                }
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                if (event == 1) {
                    this.mIawareProxyAppStart = true;
                } else if (event == 0) {
                    this.mIawareProxyAppStart = false;
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "don't process event " + event);
                }
            case ByteUtil.LONG_SIZE /*8*/:
                if (event == 1) {
                    this.mIawareProxyActivitStart = true;
                } else if (event == 0) {
                    this.mIawareProxyActivitStart = false;
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "don't process event " + event);
                }
            default:
        }
    }

    private boolean isSystemServerBroadcast(int callingPid) {
        if (callingPid != Process.myPid()) {
            return false;
        }
        return true;
    }

    public boolean isSpeedNoCtrol() {
        return !isStrictCondition();
    }

    public boolean isScreenOff() {
        return !this.mIawareScreenOn;
    }

    private boolean isStrictCondition() {
        if (this.mIawareProxySlip || this.mIawareProxyAppStart || this.mIawareProxyActivitStart) {
            return true;
        }
        return false;
    }

    public boolean isEmptyIawareBrList() {
        return this.mBgIawareBr.getIawareBrSize() == 0 && this.mFgIawareBr.getIawareBrSize() == 0;
    }

    public void setStartProxy(boolean startProxy) {
        this.mSpeedParallelStartProxy = startProxy;
    }

    public void dumpIawareBr(PrintWriter pw) {
        pw.println("    feature enable :" + BroadcastFeature.isFeatureEnabled(BROADCAST_PROXY_SPEED_NUMBER));
        synchronized (this.mLockNoProxyActions) {
            pw.println("    Default no proxy actions :" + this.mIawareNoProxyActions);
        }
        synchronized (this.mLockNoProxyPkgs) {
            pw.println("    Default no proxy pkgs :" + this.mIawareNoProxyPkgs);
        }
        synchronized (this.mLockProxySysPkgs) {
            pw.println("    Default proxy sys pkgs :" + this.mIawareProxySysPkgs);
        }
        pw.println("    fg app level :" + this.mForegroundAppLevel);
        pw.println("    The receiver speed :" + this.mCountCheck);
        ArraySet<Integer> iawareDownloadingUid = new ArraySet();
        ArrayList<String> iawareDownloadingPkgs = new ArrayList();
        synchronized (this.mIawareDownloadingUid) {
            iawareDownloadingUid.addAll(this.mIawareDownloadingUid);
        }
        if (iawareDownloadingUid.size() > 0) {
            PackageManagerService pms = (PackageManagerService) ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY);
            for (Integer uid : iawareDownloadingUid) {
                String name = pms.getNameForUid(uid.intValue());
                if (name != null) {
                    iawareDownloadingPkgs.add(name);
                }
            }
        }
        pw.println("    App Downloading:" + iawareDownloadingPkgs);
        pw.println("    Screen:" + (this.mIawareScreenOn ? PreciseIgnore.COMP_SCREEN_ON_VALUE_ : "off"));
        pw.println("    Operation: [" + (this.mIawareProxySlip ? "slip" : AppHibernateCst.INVALID_PKG) + " " + (this.mIawareProxyAppStart ? "appstart" : AppHibernateCst.INVALID_PKG) + " " + (this.mIawareProxyActivitStart ? "activityStart" : AppHibernateCst.INVALID_PKG) + "]");
        pw.println("    Proxy info:");
        this.mBgIawareBr.dump(pw);
        this.mFgIawareBr.dump(pw);
    }

    public void notifyIawareUnproxyBr(int pid, int uid) {
        if (isIawarePrepared()) {
            this.mBgIawareBr.startUnproxyFgAppBroadcast(pid, uid);
            this.mFgIawareBr.startUnproxyFgAppBroadcast(pid, uid);
        }
    }
}
