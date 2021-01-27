package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DeviceInfo;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.memory.policy.SystemTrimPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.android.os.UserHandleEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SystemTrimAction extends Action {
    private static final long DEFAULT_APP_INTERVAL = 1800000;
    private static final long DEFAULT_PROCESS_INTERVAL = 3000;
    private static final int MSG_SEND_BROADCAST = 101;
    private static final Map<String, Long> PACKAGE_NAMES = new ArrayMap();
    private static final List<ProcessInfo> PROCESS_INFOS = new ArrayList();
    private static final String TAG = "AwareMem_SystemTrimAction";
    private HwActivityManagerService mHwAms = HwActivityManagerService.self();
    private Handler mSystemTrimHandler = null;

    public SystemTrimAction(Context context) {
        super(context);
        initHandler();
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public int execute(Bundle extras) {
        if (extras == null) {
            AwareLog.w(TAG, "system memory action for extras null");
            return -1;
        } else if (MemoryConstant.getConfigSystemTrimSwitch() == 0) {
            AwareLog.w(TAG, "system trim function is close");
            return -1;
        } else if (DeviceInfo.getDeviceLevel() <= 1) {
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "system trim function can be excuted on middle or low level, level=" + DeviceInfo.getDeviceLevel());
            }
            return -1;
        } else {
            long start = SystemClock.elapsedRealtime();
            List<AwareProcessInfo> procsGroups = MemoryUtils.getAppMngSortPolicyForSystemTrim();
            if (procsGroups == null) {
                AwareLog.w(TAG, "getAppMngSortPolicyForSystemTrim is null!");
                return -1;
            }
            List<AwareProcessInfo> procInfos = SystemTrimPolicy.getInstance().getProcNeedTrim(procsGroups);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "generic system memory cost time:" + (SystemClock.elapsedRealtime() - start));
            }
            if (procInfos == null || procInfos.isEmpty()) {
                AwareLog.d(TAG, "no proc need to trim");
                return -1;
            }
            synchronized (PROCESS_INFOS) {
                if (PROCESS_INFOS.isEmpty()) {
                    addProcToListForTrim(procInfos);
                    this.mSystemTrimHandler.sendEmptyMessage(101);
                    return 0;
                }
                AwareLog.d(TAG, "is on trimming now, ignore this");
                return -1;
            }
        }
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public void reset() {
        synchronized (PROCESS_INFOS) {
            PROCESS_INFOS.clear();
        }
    }

    private void addProcToListForTrim(List<AwareProcessInfo> procInfos) {
        for (AwareProcessInfo proc : procInfos) {
            if (!(proc == null || proc.procProcInfo == null || proc.procProcInfo.mPackageName == null || proc.procProcInfo.mPackageName.isEmpty())) {
                String packageName = (String) proc.procProcInfo.mPackageName.get(0);
                if (PACKAGE_NAMES.containsKey(packageName)) {
                    if (SystemClock.elapsedRealtime() - PACKAGE_NAMES.get(packageName).longValue() > DEFAULT_APP_INTERVAL) {
                        PROCESS_INFOS.add(proc.procProcInfo);
                    }
                } else {
                    PROCESS_INFOS.add(proc.procProcInfo);
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v1, resolved type: java.util.Map<java.lang.String, java.lang.Long> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doTrim() {
        synchronized (PROCESS_INFOS) {
            if (!PROCESS_INFOS.isEmpty()) {
                ProcessInfo processInfo = PROCESS_INFOS.remove(0);
                MemoryUtils.trimMemory(this.mHwAms, String.valueOf(processInfo.mPid), UserHandleEx.getUserId(processInfo.mUid), 15, false);
                PACKAGE_NAMES.put(processInfo.mPackageName.get(0), Long.valueOf(SystemClock.elapsedRealtime()));
                if (!PROCESS_INFOS.isEmpty()) {
                    this.mSystemTrimHandler.sendEmptyMessageDelayed(101, DEFAULT_PROCESS_INTERVAL);
                }
            }
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mSystemTrimHandler = new SystemTrimHandler(looper);
        } else {
            this.mSystemTrimHandler = new SystemTrimHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public class SystemTrimHandler extends Handler {
        SystemTrimHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 101) {
                SystemTrimAction.this.doTrim();
            }
        }
    }
}
