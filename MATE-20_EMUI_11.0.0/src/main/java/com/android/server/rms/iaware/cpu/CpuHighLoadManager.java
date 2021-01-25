package com.android.server.rms.iaware.cpu;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwaredConnection;
import com.android.server.mtm.iaware.appmng.CloudPushManager;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.libcore.io.IoUtilsEx;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuHighLoadManager {
    private static final int BUFFER_UPDATE_UID = 3;
    private static final int BYTE_INT_SIZE = 4;
    private static final String CPU_FG_CTRL = "CPUFGCTRL";
    private static final String DATA_COMP = "comp";
    private static final String DATA_STATE = "state";
    private static final String DATA_UID = "uid";
    private static final int DEFAULT_LENGTH = 2;
    private static final int DEFAULT_LIMIT_DELAY = 2000;
    private static final String EVENT_RESUME = "onResume";
    private static final int FATHER_PID_OFFSET = 4;
    private static final String[] HL_CTL_PARAM_NAMES = {"times", "duration", "hl_limit_td", "hl_cancel_td"};
    private static final int INVALID_UID = -2;
    private static final int KERNEL_PID = 2;
    private static final Object LOCAL_LOCK = new Object();
    private static final int MSG_BG_HIGH = 3;
    private static final int MSG_BG_LOW = 4;
    private static final int MSG_CPU_BASE_VALUE = 170;
    private static final int MSG_CPU_BG_REMOVE_TASK = 179;
    private static final int MSG_CPU_BG_SET_TASK = 178;
    private static final int MSG_CPU_NATIVE_REMOVE_TASK = 177;
    private static final int MSG_CPU_NATIVE_SET_TASK = 176;
    private static final int MSG_FG_HIGH = 5;
    private static final int MSG_LOW_PRIOVITY_LIMIT_DELAY = 2;
    private static final int MSG_NATIVE_HIGH = 1;
    private static final int MSG_NATIVE_LOW = 2;
    private static final int MSG_NOTIFY_FG_UID = 3;
    private static final int MSG_POWER_MODE_CHANGED = 5;
    private static final int MSG_REPORT_DATA = 1;
    private static final int MSG_SEND_FG_HL_CTL_PARAMS = 4;
    private static final int NUMBER_OF_INT = 20;
    private static final int PIDS_MAX_LENGTH = 16;
    private static final String REASON_INFO = "activityLifeState";
    private static final int SEND_PARAMS_DELAYED = 5000;
    private static final int SEND_PARAMS_MAX_TIMES = 3;
    private static final String SMART_POWER = "persist.sys.smart_power";
    private static final int SWITCH_BG_CPUCTL = 33554432;
    private static final int SWITCH_NATIVE_SCHED = 16777216;
    private static final int SWITCH_ON = 1;
    private static final String TAG = "CPUHighLoadManager";
    private static CpuHighLoadManager sInstance = null;
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.android.server.rms.iaware.cpu.CpuHighLoadManager.AnonymousClass1 */

        public void call(Bundle extras) {
            if (CpuHighLoadManager.this.mSwitchOfFgHighLoadCtl.get() && extras != null && "onResume".equals(extras.getString("state")) && (extras.getParcelable("comp") instanceof ComponentName)) {
                CpuHighLoadManager.this.notifyFgUidChanged(extras.getInt("uid"));
            }
        }
    };
    private boolean mBgCpuCtlEnable = false;
    private boolean mBgCpuctlSend = false;
    private boolean mBgHighLoad = false;
    private final ArrayList<Integer> mBgPids = new ArrayList<>();
    private final ArrayList<Integer> mBgSendPids = new ArrayList<>();
    private int mClickLimitDelay = DEFAULT_LIMIT_DELAY;
    private CpuHighLoadHandler mCpuHighLoadHandler = null;
    private int mFeatureFlag = 0;
    private boolean mIsDelayMessageNotHandled = false;
    private boolean mIsPerfMode = false;
    private int mLastFgUid = -1;
    private boolean mLastIsGame = false;
    private final Object mLock = new Object();
    private final Object mModeLock = new Object();
    private AtomicBoolean mNativeBgCtl = new AtomicBoolean(false);
    private boolean mNativeCpusetEnable = false;
    private boolean mNativeCpusetSend = false;
    private boolean mNativeHighLoad = false;
    private final ArrayList<Integer> mNativePids = new ArrayList<>();
    private final ArrayList<Integer> mNativeSendPids = new ArrayList<>();
    private int mNotifyFgCtlParamsNum = 0;
    private AtomicBoolean mSwitchOfFgHighLoadCtl = new AtomicBoolean(false);

    private CpuHighLoadManager() {
    }

    public static CpuHighLoadManager getInstance() {
        CpuHighLoadManager cpuHighLoadManager;
        synchronized (LOCAL_LOCK) {
            if (sInstance == null) {
                sInstance = new CpuHighLoadManager();
            }
            cpuHighLoadManager = sInstance;
        }
        return cpuHighLoadManager;
    }

    public void enable(boolean isNativeBgCtl, boolean isFgHlCtl) {
        boolean isCloudEnable = CloudPushManager.getInstance().getFeatureSwitchByFeatureName(CPU_FG_CTRL);
        AwareLog.d(TAG, "isCloudEnable : " + isCloudEnable);
        boolean isFgHlCtlEnable = isFgHlCtl && isCloudEnable;
        if (isNativeBgCtl || isFgHlCtlEnable) {
            this.mFeatureFlag = parseIntValue(SystemPropertiesEx.get("persist.sys.cpuset.subswitch", "0"));
            if (this.mFeatureFlag != -1) {
                this.mCpuHighLoadHandler = new CpuHighLoadHandler();
                this.mNativeBgCtl.set(isNativeBgCtl);
                this.mNativeCpusetEnable = isSubFeatureEnable(SWITCH_NATIVE_SCHED);
                this.mBgCpuCtlEnable = isSubFeatureEnable(SWITCH_BG_CPUCTL);
                this.mSwitchOfFgHighLoadCtl.set(isFgHlCtlEnable);
                if (this.mSwitchOfFgHighLoadCtl.get()) {
                    new FgHighLoadCtrlConfig().init();
                    notifyPowerStateChanged(CpuPowerMode.isPowerModePerformance(SystemPropertiesEx.getInt(SMART_POWER, 0)));
                    AwareCallback.getInstance().registerActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
                }
                AwareLog.i(TAG, " mFeatureFlag : " + this.mFeatureFlag + " mNativeCpusetEnable : " + this.mNativeCpusetEnable + " mBgCpuCtlEnable : " + this.mBgCpuCtlEnable + " mSwitchOfFgHighLoadCtl : " + this.mSwitchOfFgHighLoadCtl);
            }
        }
    }

    public void disable() {
        this.mNativeBgCtl.set(false);
        if (this.mSwitchOfFgHighLoadCtl.get()) {
            this.mSwitchOfFgHighLoadCtl.set(false);
            AwareCallback.getInstance().unregisterActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
        }
        this.mCpuHighLoadHandler = null;
    }

    public void reportData(CollectData data) {
        if (!this.mNativeBgCtl.get()) {
            AwareLog.d(TAG, "reportData : mNativeBgCtl disable");
            return;
        }
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = data;
        this.mCpuHighLoadHandler.sendMessage(msg);
    }

    private boolean isSubFeatureEnable(int feature) {
        return (this.mFeatureFlag & feature) != 0;
    }

    private int parseIntValue(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            AwareLog.w(TAG, "parseInt failed!");
            return -1;
        }
    }

    private boolean isNativeProcess(int pid) {
        if (pid <= 0) {
            AwareLog.w(TAG, "pid less than 0, pid is " + pid);
            return false;
        }
        int fatherPid = getFatherPid(pid);
        if (fatherPid == 2 || (fatherPid > 0 && isZygoteProcess(fatherPid))) {
            return false;
        }
        return true;
    }

    private int getFatherPid(int pid) {
        try {
            String lineString = IoUtilsEx.readFileAsString("/proc/" + pid + "/stat");
            if (lineString == null) {
                return -1;
            }
            int beginNumber = lineString.indexOf(")") + 4;
            return parseIntValue(lineString.substring(beginNumber, lineString.indexOf(" ", beginNumber)));
        } catch (StringIndexOutOfBoundsException e) {
            AwareLog.w(TAG, "exception StringIndexOutOfBoundsException!");
            return -1;
        } catch (IOException e2) {
            AwareLog.w(TAG, "exception IOException!");
            return -1;
        }
    }

    private boolean isZygoteProcess(int pid) {
        try {
            String lineString = IoUtilsEx.readFileAsString("/proc/" + pid + "/stat");
            if (lineString == null) {
                return false;
            }
            int beginNumber = lineString.indexOf("(") + 1;
            if (lineString.substring(beginNumber, lineString.indexOf(")", beginNumber)).equals("main")) {
                return true;
            }
            return false;
        } catch (StringIndexOutOfBoundsException e) {
            AwareLog.w(TAG, "exception StringIndexOutOfBoundsException!");
            return false;
        } catch (IOException e2) {
            AwareLog.w(TAG, "exception IOException!");
            return false;
        }
    }

    private boolean isDefaultGroup(int pid) {
        try {
            String lineString = IoUtilsEx.readFileAsString("/proc/" + pid + "/cpuset");
            if (lineString == null || lineString.length() != 2) {
                return false;
            }
            return true;
        } catch (IOException e) {
            AwareLog.w(TAG, "exception IOException!");
            return false;
        }
    }

    private boolean isBgGroup(int pid) {
        try {
            String lineString = IoUtilsEx.readFileAsString("/proc/" + pid + "/cpuset");
            if (lineString == null || !lineString.contains(MemoryConstant.MEM_REPAIR_CONSTANT_BG)) {
                return false;
            }
            return true;
        } catch (IOException e) {
            AwareLog.w(TAG, "exception IOException!");
            return false;
        }
    }

    private void handleScrollBegin() {
        if (this.mIsDelayMessageNotHandled) {
            this.mCpuHighLoadHandler.removeMessages(2);
            AwareLog.d(TAG, "scroll begin, delay " + this.mClickLimitDelay + " ms stop limit");
        } else if (this.mNativeHighLoad || this.mBgHighLoad) {
            doCpuSetBg();
        }
    }

    private void handleScrollFinish() {
        if (this.mIsDelayMessageNotHandled) {
            this.mCpuHighLoadHandler.removeMessages(2);
            this.mCpuHighLoadHandler.sendEmptyMessageDelayed(2, (long) this.mClickLimitDelay);
            AwareLog.d(TAG, "scroll finish, delay " + this.mClickLimitDelay + " ms stop limit");
        }
    }

    private void handleFlingBegin(int duration) {
        if (this.mIsDelayMessageNotHandled) {
            this.mCpuHighLoadHandler.removeMessages(2);
            int delayTime = this.mClickLimitDelay + duration;
            this.mCpuHighLoadHandler.sendEmptyMessageDelayed(2, (long) delayTime);
            AwareLog.d(TAG, "flinging begin duration " + duration + ", delay " + delayTime + " ms stop limit");
        }
    }

    private void handleTouchEvent() {
        if (this.mNativeHighLoad || this.mBgHighLoad) {
            doCpuSetBg();
            this.mCpuHighLoadHandler.removeMessages(2);
            this.mCpuHighLoadHandler.sendEmptyMessageDelayed(2, (long) this.mClickLimitDelay);
            this.mIsDelayMessageNotHandled = true;
            AwareLog.d(TAG, "touch event, delay " + this.mClickLimitDelay + " ms stop limit");
        }
    }

    private void sceneRecDataHandle(Bundle bundle) {
        switch (bundle.getInt("relationType")) {
            case 13:
                handleScrollBegin();
                return;
            case 14:
                handleScrollFinish();
                return;
            case 15:
                int duration = bundle.getInt("scroll_duration");
                if (duration > 0) {
                    handleFlingBegin(duration);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void inputDataHandle(int event) {
        if (event == 10001) {
            handleTouchEvent();
        }
    }

    private AttrSegments parseCollectData(CollectData data) {
        String eventData = data.getData();
        AttrSegments.Builder builder = new AttrSegments.Builder();
        builder.addCollectData(eventData);
        return builder.build();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerReportData(CollectData data) {
        if (data != null) {
            AwareConstant.ResourceType type = AwareConstant.ResourceType.getResourceType(data.getResId());
            if (type == AwareConstant.ResourceType.RESOURCE_SCENE_REC) {
                Bundle bundle = data.getBundle();
                if (bundle != null) {
                    sceneRecDataHandle(bundle);
                }
            } else if (type == AwareConstant.ResourceType.RES_INPUT) {
                AttrSegments attrSegments = parseCollectData(data);
                if (attrSegments.isValid()) {
                    inputDataHandle(attrSegments.getEvent().intValue());
                }
            } else {
                AwareLog.d(TAG, "unknown type " + type);
            }
        }
    }

    /* access modifiers changed from: private */
    public class CpuHighLoadHandler extends Handler {
        private CpuHighLoadHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(CpuHighLoadManager.TAG, "CpuHighLoadHandler, msg is null, error!");
                return;
            }
            int i = msg.what;
            if (i != 1) {
                if (i == 2) {
                    CpuHighLoadManager.this.removeCpuSetBg();
                    CpuHighLoadManager.this.mIsDelayMessageNotHandled = false;
                } else if (i == 3) {
                    CpuHighLoadManager.this.updateFgUid(msg);
                } else if (i == 4) {
                    CpuHighLoadManager.this.notifyFgHighLoadCtlParams(msg);
                } else if (i != 5) {
                    AwareLog.d(CpuHighLoadManager.TAG, "CpuHighLoadHandler, default branch, msg.what is " + msg.what);
                } else {
                    CpuHighLoadManager.this.sendPowerMode(msg);
                }
            } else if (msg.obj instanceof CollectData) {
                CpuHighLoadManager.this.handlerReportData((CollectData) msg.obj);
            }
        }
    }

    private void doCpuSetBg() {
        if (this.mNativeCpusetEnable) {
            synchronized (this.mLock) {
                if (!this.mNativeCpusetSend) {
                    this.mNativeCpusetSend = sendNativePidsToDaemon(this.mNativePids);
                }
            }
        }
        if (this.mBgCpuCtlEnable) {
            synchronized (this.mLock) {
                if (!this.mBgCpuctlSend) {
                    this.mBgCpuctlSend = sendBgPidsToDaemon(this.mBgPids);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeCpuSetBg() {
        if (this.mNativeCpusetEnable) {
            synchronized (this.mLock) {
                if (this.mNativeCpusetSend) {
                    sendRemovePidsToDaemon(this.mNativeSendPids, true);
                    this.mNativeCpusetSend = false;
                }
            }
        }
        if (this.mBgCpuCtlEnable) {
            synchronized (this.mLock) {
                if (this.mBgCpuctlSend) {
                    sendRemovePidsToDaemon(this.mBgSendPids, false);
                    this.mBgCpuctlSend = false;
                }
            }
        }
    }

    private void sendRemovePidsToDaemon(ArrayList<Integer> pids, boolean isNative) {
        int size;
        if (!(pids == null || pids.isEmpty() || (size = pids.size()) > 16)) {
            AwareLog.i(TAG, "sendRemovePidsToDaemon size " + size);
            ByteBuffer buffer = ByteBuffer.allocate(80);
            if (isNative) {
                buffer.putInt(MSG_CPU_NATIVE_REMOVE_TASK);
            } else {
                buffer.putInt(MSG_CPU_BG_REMOVE_TASK);
            }
            buffer.putInt(size);
            for (int i = 0; i < size; i++) {
                int pid = pids.get(i).intValue();
                AwareLog.d(TAG, "sendRemovePidsToDaemon pid: " + pid);
                buffer.putInt(pid);
            }
            if (!sendPacket(buffer.array())) {
                AwareLog.w(TAG, "sendRemovePidsToDaemon failed ");
            }
        }
    }

    private boolean sendNativePidsToDaemon(ArrayList<Integer> pids) {
        int size;
        if (pids == null || pids.isEmpty() || (size = pids.size()) > 16) {
            return false;
        }
        AwareLog.i(TAG, "sendNativePidsToDaemon size " + size);
        ByteBuffer buffer = ByteBuffer.allocate(80);
        buffer.putInt(MSG_CPU_NATIVE_SET_TASK);
        buffer.putInt(size);
        this.mNativeSendPids.clear();
        for (int i = 0; i < size; i++) {
            int pid = pids.get(i).intValue();
            boolean isDefault = isDefaultGroup(pid);
            AwareLog.d(TAG, "sendNativePidsToDaemon pid: " + pid + " isDefault " + isDefault);
            if (isDefault) {
                buffer.putInt(pid);
                this.mNativeSendPids.add(Integer.valueOf(pid));
            }
        }
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.w(TAG, "sendNativePidsToDaemon failed ");
        }
        return res;
    }

    private boolean sendBgPidsToDaemon(ArrayList<Integer> pids) {
        int size;
        if (pids == null || pids.isEmpty() || (size = pids.size()) > 16) {
            return false;
        }
        AwareLog.i(TAG, "sendBgPidsToDaemon size " + size);
        ByteBuffer buffer = ByteBuffer.allocate(80);
        buffer.putInt(MSG_CPU_BG_SET_TASK);
        buffer.putInt(size);
        this.mBgSendPids.clear();
        for (int i = 0; i < size; i++) {
            int pid = pids.get(i).intValue();
            boolean isBg = isBgGroup(pid);
            AwareLog.d(TAG, "sendBgPidsToDaemon pid: " + pid + " isBg " + isBg);
            if (isBg) {
                buffer.putInt(pid);
                this.mBgSendPids.add(Integer.valueOf(pid));
            }
        }
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.w(TAG, "sendBgPidsToDaemon failed");
        }
        return res;
    }

    private void setNativeHighLoadTaskList(ArrayList<Integer> nativePids) {
        if (!(!this.mNativeCpusetEnable || nativePids == null || nativePids.isEmpty())) {
            synchronized (this.mLock) {
                this.mNativePids.clear();
                int size = nativePids.size();
                for (int i = 0; i < size; i++) {
                    int pid = nativePids.get(i).intValue();
                    boolean isNative = isNativeProcess(pid);
                    AwareLog.d(TAG, "setNativeHighLoadTaskList pid " + pid + " isNative " + isNative);
                    if (isNative) {
                        this.mNativePids.add(Integer.valueOf(pid));
                    }
                }
            }
            this.mNativeHighLoad = true;
        }
    }

    private void removeNativeHighLoadTaskList() {
        if (this.mNativeCpusetEnable) {
            synchronized (this.mLock) {
                this.mNativePids.clear();
            }
            this.mNativeHighLoad = false;
        }
    }

    private void setBgHighLoadTaskList(ArrayList<Integer> bgPids) {
        if (!(!this.mBgCpuCtlEnable || bgPids == null || bgPids.isEmpty())) {
            synchronized (this.mLock) {
                this.mBgPids.clear();
                int size = bgPids.size();
                for (int i = 0; i < size; i++) {
                    int pid = bgPids.get(i).intValue();
                    AwareLog.d(TAG, "setBgHighLoadTaskList pid " + pid);
                    this.mBgPids.add(Integer.valueOf(pid));
                }
            }
            this.mBgHighLoad = true;
        }
    }

    private void removeBgHighLoadTaskList() {
        if (this.mBgCpuCtlEnable) {
            synchronized (this.mLock) {
                this.mBgPids.clear();
            }
            this.mBgHighLoad = false;
        }
    }

    public void setCpuHighLoadTaskList(int msg, ArrayList<Integer> pids) {
        AwareLog.i(TAG, "setCpuHighLoadTaskList msg " + msg);
        if (msg == 1) {
            setNativeHighLoadTaskList(pids);
        } else if (msg == 2) {
            removeNativeHighLoadTaskList();
        } else if (msg == 3) {
            setBgHighLoadTaskList(pids);
        } else if (msg == 4) {
            removeBgHighLoadTaskList();
        } else if (msg == 5) {
            notifyFgHighLoadThread(pids);
        } else {
            AwareLog.d(TAG, "setCpuHighLoadTaskList default msg ");
        }
    }

    private boolean sendPacket(byte[] msg) {
        return IAwaredConnection.getInstance().sendPacket(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyFgHighLoadCtlParams(Message msg) {
        CpuHighLoadHandler cpuHighLoadHandler;
        if (msg.obj instanceof ArrayList) {
            ArrayList<Integer> params = (ArrayList) msg.obj;
            ByteBuffer buffer = ByteBuffer.allocate((params.size() + 1) * 4);
            buffer.putInt(CpuFeature.MSG_SET_FG_HL_CTL_PARAMS);
            Iterator<Integer> it = params.iterator();
            while (it.hasNext()) {
                buffer.putInt(it.next().intValue());
            }
            this.mNotifyFgCtlParamsNum++;
            if (!sendPacket(buffer.array())) {
                AwareLog.w(TAG, "notifyFgHighLoadCtlParams failed! Send times:" + this.mNotifyFgCtlParamsNum);
                if (this.mNotifyFgCtlParamsNum >= 3 || (cpuHighLoadHandler = this.mCpuHighLoadHandler) == null) {
                    this.mSwitchOfFgHighLoadCtl.set(false);
                    AwareLog.w(TAG, "Send fg high load ctl params failed. Close switch.");
                    return;
                }
                cpuHighLoadHandler.removeMessages(4);
                this.mCpuHighLoadHandler.sendMessageDelayed(Message.obtain(msg), 5000);
            }
        }
    }

    private boolean isGameApp(int uid) {
        String pkgName = InnerUtils.getPackageNameByUid(uid);
        if (pkgName != null && AppTypeRecoManager.getInstance().getAppType(pkgName) == 9) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFgUid(Message msg) {
        int uid = msg.arg1;
        if (this.mLastFgUid != uid) {
            this.mLastFgUid = uid;
            this.mLastIsGame = isGameApp(uid);
            ByteBuffer buffer = ByteBuffer.allocate(12);
            buffer.putInt(CpuFeature.MSG_UPDATE_FG_UIDS);
            buffer.putInt(uid);
            if (this.mLastIsGame) {
                buffer.putInt(1);
            } else {
                buffer.putInt(0);
            }
            boolean isResCode = sendPacket(buffer.array());
            if (!isResCode) {
                AwareLog.w(TAG, "sendFgUid: sendPacket failed, send error code: " + isResCode);
            }
        }
    }

    public void sendFgHighLoadCtlParams(Map<String, String> params) {
        if (!(!this.mSwitchOfFgHighLoadCtl.get() || this.mCpuHighLoadHandler == null || params == null || params.isEmpty())) {
            ArrayList<Integer> values = new ArrayList<>();
            String[] strArr = HL_CTL_PARAM_NAMES;
            for (String type : strArr) {
                int res = parseIntValue(params.get(type));
                if (res == -1) {
                    AwareLog.w(TAG, "Foreground HighLoad Ctl params error:" + type);
                    return;
                }
                values.add(Integer.valueOf(res));
            }
            values.add(1);
            Message msg = this.mCpuHighLoadHandler.obtainMessage();
            msg.what = 4;
            msg.obj = values;
            this.mCpuHighLoadHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyFgUidChanged(int uid) {
        if (this.mCpuHighLoadHandler != null) {
            int fgUid = INVALID_UID;
            if (uid > 10000) {
                fgUid = uid;
            }
            Message msg = this.mCpuHighLoadHandler.obtainMessage();
            msg.what = 3;
            msg.arg1 = fgUid;
            this.mCpuHighLoadHandler.sendMessage(msg);
        }
    }

    public void notifyFgHighLoadThread(ArrayList<Integer> tids) {
        if (this.mSwitchOfFgHighLoadCtl.get() && tids != null && !tids.isEmpty()) {
            sendFgHighLoadThread(tids);
        }
    }

    private void sendFgHighLoadThread(ArrayList<Integer> tids) {
        ByteBuffer buffer = ByteBuffer.allocate((tids.size() + 1) * 4);
        buffer.putInt(CpuFeature.MSG_HIGH_LOAD_THREAD);
        Iterator<Integer> it = tids.iterator();
        while (it.hasNext()) {
            buffer.putInt(it.next().intValue());
        }
        boolean isResCode = sendPacket(buffer.array());
        if (!isResCode) {
            AwareLog.w(TAG, "sendFgHighLoadThread: sendPacket failed, send error code: " + isResCode);
        }
    }

    public void notifyPowerStateChanged(boolean isPerfMode) {
        if (this.mSwitchOfFgHighLoadCtl.get() && this.mCpuHighLoadHandler != null) {
            synchronized (this.mModeLock) {
                if (this.mIsPerfMode != isPerfMode) {
                    this.mIsPerfMode = isPerfMode;
                    int cmd = isPerfMode ? 1 : 0;
                    Message msg = this.mCpuHighLoadHandler.obtainMessage();
                    msg.what = 5;
                    msg.arg1 = cmd;
                    this.mCpuHighLoadHandler.sendMessage(msg);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPowerMode(Message msg) {
        int isPerfMode = msg.arg1;
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(CpuFeature.MSG_SEND_POWER_MODE);
        buffer.putInt(isPerfMode);
        boolean isResCode = sendPacket(buffer.array());
        if (!isResCode) {
            AwareLog.w(TAG, "sendPowerMode: sendPacket failed, send error code: " + isResCode);
        }
    }
}
