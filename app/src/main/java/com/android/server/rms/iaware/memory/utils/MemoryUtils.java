package com.android.server.rms.iaware.memory.utils;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.nio.ByteBuffer;
import java.util.List;

public class MemoryUtils {
    private static final String TAG = "AwareMem_MemoryUtils";

    public static AwareAppMngSortPolicy getAppMngSortPolicy(int groupId) {
        return getAppMngSortPolicy(groupId, 0);
    }

    public static AwareAppMngSortPolicy getAppMngSortPolicy(int groupId, int subType) {
        if (!AwareAppMngSort.checkAppMngEnable() || groupId < 0 || groupId > 3) {
            return null;
        }
        AwareAppMngSort sorted = AwareAppMngSort.getInstance();
        if (sorted == null) {
            return null;
        }
        return sorted.getAppMngSortPolicy(0, subType, groupId);
    }

    public static List<AwareProcessBlockInfo> getAppMngProcGroup(AwareAppMngSortPolicy policy, int groupId) {
        if (policy == null) {
            AwareLog.e(TAG, "getAppMngProcGroup sort policy null!");
            return null;
        }
        List<AwareProcessBlockInfo> processGroups = null;
        switch (groupId) {
            case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                processGroups = policy.getForbidStopProcBlockList();
                break;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                processGroups = policy.getShortageStopProcBlockList();
                break;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                processGroups = policy.getAllowStopProcBlockList();
                break;
            default:
                AwareLog.w(TAG, "getAppMngProcGroup unknown group id!");
                break;
        }
        return processGroups;
    }

    public static int killProcessGroupForQuickKill(int uid, int pid) {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            int killProcessGroupForQuickKill = ResourceCollector.killProcessGroupForQuickKill(uid, pid);
            return killProcessGroupForQuickKill;
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    public static void writeSwappiness(int swappiness) {
        if (swappiness > WifiProCommonUtils.HTTP_REACHALBE_HOME || swappiness < 0) {
            AwareLog.w(TAG, "invalid swappiness value");
            return;
        }
        AwareLog.i(TAG, "setSwappiness = " + swappiness);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(WifiProCommonUtils.HTTP_REDIRECTED);
        buffer.putInt(swappiness);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void writeDirectSwappiness(int directswappiness) {
        if (directswappiness > WifiProCommonUtils.HTTP_REACHALBE_HOME || directswappiness < 0) {
            AwareLog.w(TAG, "invalid directswappiness value");
            return;
        }
        AwareLog.i(TAG, "setDirectSwappiness = " + directswappiness);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_DIRECT_SWAPPINESS);
        buffer.putInt(directswappiness);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void writeExtraFreeKbytes(int extrafreekbytes) {
        if (extrafreekbytes <= 0 || extrafreekbytes >= MemoryConstant.MAX_EXTRA_FREE_KBYTES) {
            AwareLog.w(TAG, "invalid extrafreekbytes value");
        } else {
            SystemProperties.set("sys.sysctl.extra_free_kbytes", Integer.toString(extrafreekbytes));
        }
    }
}
