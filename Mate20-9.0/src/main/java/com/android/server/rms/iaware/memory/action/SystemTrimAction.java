package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DeviceInfo;
import android.util.ArrayMap;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.memory.policy.SystemTrimPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemTrimAction extends Action {
    private static final long DEFAULT_APP_INTERVAL = 1800000;
    private static final long DEFAULT_PROCESS_INTERVAL = 3000;
    private static final int MSG_SEND_BROADCAST = 101;
    private static final String TAG = "AwareMem_SystemTrimAction";
    private static final Map<String, Long> mPackageNames = new ArrayMap();
    private static final List<ProcessInfo> mProcessInfos = new ArrayList();
    private HwActivityManagerService mHwAMS;
    private Handler mSystemTrimHandler;

    private class SystemTrimHandler extends Handler {
        public SystemTrimHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 101) {
                SystemTrimAction.this.doTrim();
            }
        }
    }

    public SystemTrimAction(Context context) {
        super(context);
        this.mSystemTrimHandler = null;
        this.mHwAMS = HwActivityManagerService.self();
        this.mSystemTrimHandler = new SystemTrimHandler(BackgroundThread.get().getLooper());
    }

    public int execute(Bundle extras) {
        if (extras == null) {
            AwareLog.w(TAG, "system memory action for extras null");
            return -1;
        } else if (MemoryConstant.getConfigSystemTrimSwitch() == 0) {
            AwareLog.d(TAG, "system trim function is close");
            return -1;
        } else if (DeviceInfo.getDeviceLevel() <= 1) {
            AwareLog.d(TAG, "system trim function can be excuted on middle or low level, level=" + DeviceInfo.getDeviceLevel());
            return -1;
        } else {
            long start = SystemClock.elapsedRealtime();
            List<AwareProcessInfo> procsGroups = MemoryUtils.getAppMngSortPolicyForSystemTrim();
            if (procsGroups == null) {
                AwareLog.w(TAG, "getAppMngSortPolicyForSystemTrim is null!");
                return -1;
            }
            Set<ProcessInfo> procInfos = SystemTrimPolicy.getInstance().getProcNeedTrim(procsGroups);
            AwareLog.d(TAG, "generic system memory cost time:" + (SystemClock.elapsedRealtime() - start));
            if (procInfos == null || procInfos.isEmpty()) {
                AwareLog.d(TAG, "no proc need to trim");
                return -1;
            }
            synchronized (mProcessInfos) {
                if (mProcessInfos.isEmpty()) {
                    addProcToListForTrim(procInfos);
                    this.mSystemTrimHandler.sendEmptyMessage(101);
                    return 0;
                }
                AwareLog.w(TAG, "is on trimming now, ignore this");
                return -1;
            }
        }
    }

    public void reset() {
        synchronized (mProcessInfos) {
            mProcessInfos.clear();
        }
    }

    private void addProcToListForTrim(Set<ProcessInfo> procInfos) {
        for (ProcessInfo proc : procInfos) {
            if (!(proc == null || proc.mPackageName == null || proc.mPackageName.isEmpty())) {
                String packageName = (String) proc.mPackageName.get(0);
                if (mPackageNames.containsKey(packageName)) {
                    if (SystemClock.elapsedRealtime() - mPackageNames.get(packageName).longValue() > 1800000) {
                        mProcessInfos.add(proc);
                    }
                } else {
                    mProcessInfos.add(proc);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void doTrim() {
        synchronized (mProcessInfos) {
            if (!mProcessInfos.isEmpty()) {
                ProcessInfo processInfo = mProcessInfos.remove(0);
                MemoryUtils.trimMemory(this.mHwAMS, String.valueOf(processInfo.mPid), UserHandle.getUserId(processInfo.mUid), 15, false);
                mPackageNames.put((String) processInfo.mPackageName.get(0), Long.valueOf(SystemClock.elapsedRealtime()));
                if (!mProcessInfos.isEmpty()) {
                    this.mSystemTrimHandler.sendEmptyMessageDelayed(101, 3000);
                }
            }
        }
    }
}
