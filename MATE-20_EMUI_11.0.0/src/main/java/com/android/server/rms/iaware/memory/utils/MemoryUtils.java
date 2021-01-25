package com.android.server.rms.iaware.memory.utils;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.CommonUtils;
import com.android.server.rms.iaware.feature.MemoryFeatureEx;
import com.android.server.rms.iaware.memory.policy.DmeServer;
import com.huawei.android.internal.util.MemInfoReaderExt;
import com.huawei.android.os.ProcessEx;
import com.huawei.android.os.ProcessExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.system.ErrnoExceptionEx;
import com.huawei.android.system.Int32RefEx;
import com.huawei.android.system.OsConstantsEx;
import com.huawei.android.system.OsEx;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MemoryUtils {
    private static final long FLUSH_TIMEOUT = 2000;
    private static final int INTEGER_BYTE_LENTH = 4;
    private static final Object LOCK = new Object();
    private static final int MAX_RECV_BYTE_BUFFER_LENTH = 8;
    private static final int MAX_SEND_BYTE_BUFFER_LENTH = 256;
    private static final String MEMORY_SOCKET = "iawared";
    private static final int[] PROCESS_FULL_STATS_FORMAT = {32, 4640, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224, 32, 32, 32, 32, 32, 32, 32, 8224};
    private static final int PROCESS_PROC_OUT_LONG = 8192;
    private static final int PROCESS_PROC_OUT_STRING = 4096;
    private static final int PROCESS_PROC_PARENS = 512;
    private static final String PROCESS_PROC_PATH = "/proc";
    private static final int PROCESS_PROC_SPACE_TERM = 32;
    private static final int PROCESS_PROC_STATS_STRING_LENGTH = 6;
    private static final String PROCESS_STAT_PATH = "stat";
    private static final String TAG = "AwareMem_MemoryUtils";
    private static InputStream sInputStream;
    private static long sLastCpuCompressTime = 0;
    private static long sLastRamSize = 0;
    private static int sLastSwappiness = 60;
    private static int sLastTargetRccAvail = 0;
    private static LocalSocket sMemorySocket;
    private static boolean sOpenProtectAlways = false;
    private static boolean sOpenProtectAlwaysConfig = false;
    private static OutputStream sOutputStream;
    private static int sRestoredSwappiness = 0;

    public static AwareAppMngSortPolicy getAppMngSortPolicy(int resourceType, int groupId) {
        return getAppMngSortPolicy(resourceType, groupId, 0);
    }

    public static AwareAppMngSortPolicy getAppMngSortPolicy(int resourceType, int groupId, int subType) {
        AwareAppMngSort sorted;
        if (AwareAppMngSort.checkAppMngEnable() && groupId >= 0 && groupId <= 3 && (sorted = AwareAppMngSort.getInstance()) != null) {
            return sorted.getAppMngSortPolicy(resourceType, subType, groupId);
        }
        return null;
    }

    public static AwareAppMngSortPolicy getAppMngSortPolicyForMemRepair(int sceneType) {
        AwareAppMngSort sorted;
        if (AwareAppMngSort.checkAppMngEnable() && (sorted = AwareAppMngSort.getInstance()) != null) {
            return sorted.getAppMngSortPolicyForMemRepair(sceneType);
        }
        return null;
    }

    public static AwareAppMngSortPolicy getAppSortPolicyForMemRepairVss(int sceneType, AwareAppMngSortPolicy pssPolicy) {
        AwareAppMngSort sorted;
        if (AwareAppMngSort.checkAppMngEnable() && (sorted = AwareAppMngSort.getInstance()) != null) {
            return sorted.getAppMngSortPolicyForMemRepairVss(sceneType, pssPolicy);
        }
        return null;
    }

    public static List<AwareProcessInfo> getAppMngSortPolicyForSystemTrim() {
        AwareAppMngSort sorted;
        if (AwareAppMngSort.checkAppMngEnable() && (sorted = AwareAppMngSort.getInstance()) != null) {
            return sorted.getAppMngSortPolicyForSystemTrim();
        }
        return null;
    }

    public static List<AwareProcessBlockInfo> getAppMngProcGroup(AwareAppMngSortPolicy policy, int groupId) {
        if (policy == null) {
            AwareLog.w(TAG, "getAppMngProcGroup sort policy null!");
            return null;
        } else if (groupId == 0) {
            return policy.getForbidStopProcBlockList();
        } else {
            if (groupId == 1) {
                return policy.getShortageStopProcBlockList();
            }
            if (groupId == 2) {
                return policy.getAllowStopProcBlockList();
            }
            AwareLog.w(TAG, "getAppMngProcGroup unknown group id!");
            return null;
        }
    }

    public static int killProcessGroupForQuickKill(int uid, int pid) {
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            return ResourceCollector.killProcessGroupForQuickKill(uid, pid);
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    public static int getLastSwappiness() {
        return sLastSwappiness;
    }

    public static void writeSwappiness(int swappiness) {
        if (swappiness > 200 || swappiness < 0 || swappiness == sLastSwappiness) {
            AwareLog.w(TAG, "invalid swappiness value");
            return;
        }
        AwareLog.d(TAG, "setSwappiness = " + swappiness);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_SWAPPINESS);
        buffer.putInt(swappiness);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
        sLastSwappiness = swappiness;
    }

    public static void setBalanceSwappiness() {
        int configBalanceSwappiness = MemoryConstant.getConfigBalanceSwappiness();
        int i = sLastSwappiness;
        if (configBalanceSwappiness < i) {
            sRestoredSwappiness = i;
            writeSwappiness(MemoryConstant.getConfigBalanceSwappiness());
        }
    }

    public static void restoreSwappiness() {
        int i = sRestoredSwappiness;
        if (i != 0) {
            writeSwappiness(i);
            sRestoredSwappiness = 0;
        }
    }

    public static void writeDirectSwappiness(int directSwappiness) {
        if (directSwappiness > 200 || directSwappiness < 0) {
            AwareLog.w(TAG, "invalid directSwappiness value");
            return;
        }
        AwareLog.d(TAG, "setDirectSwappiness = " + directSwappiness);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_DIRECT_SWAPPINESS);
        buffer.putInt(directSwappiness);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void configQosMemoryWatermark() {
        String configVal = MemoryConstant.getConfigQosMemoryWatermark();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        try {
            byte[] stringBytes = configVal.getBytes("UTF-8");
            if (stringBytes.length >= 1) {
                if (stringBytes.length <= 7) {
                    buffer.clear();
                    buffer.putInt(MemoryConstant.MSG_CONFIG_QOS_MEMORY_WATERMARK);
                    buffer.putInt(stringBytes.length);
                    buffer.put(stringBytes);
                    buffer.putChar(0);
                    IAwaredConnection.getInstance().sendPacket(buffer.array());
                    MemoryConstant.setQosMemoryConfigState(true);
                    return;
                }
            }
            AwareLog.w(TAG, "configQosMemory incorrect val");
        } catch (UnsupportedEncodingException e) {
            AwareLog.e(TAG, "setPackageProtectLru UTF-8 not supported");
        }
    }

    public static void setQosMemorySwitch(int on) {
        if (on == 1) {
            if (MemoryConstant.getConfigQosMemorySwitch() == 1) {
                if (!MemoryConstant.getQosMemoryConfigState()) {
                    configQosMemoryWatermark();
                }
            } else {
                return;
            }
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_CONFIG_QOS_MEMORY_SWITCH);
        buffer.putInt(on);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void setEglInitOptSwitch(int on) {
        if (MemoryConstant.getEglInitOptConfigSwitch() == 1) {
            String val = on == 1 ? "true" : "false";
            try {
                SystemPropertiesEx.set("sys.eglinit_opt", val);
            } catch (RuntimeException e) {
                AwareLog.e(TAG, "fail to set sys.eglinit_opt to " + val);
            }
        }
    }

    public static void writeExtraFreeKbytes(int extraFreekbytes) {
        if (extraFreekbytes <= 0 || extraFreekbytes >= 200000) {
            AwareLog.w(TAG, "invalid extraFreekbytes value");
            return;
        }
        int lastExtraFreeKbytes = SystemPropertiesEx.getInt("sys.sysctl.extra_free_kbytes", (int) MemoryConstant.PROCESS_LIST_EXTRA_FREE_KBYTES);
        if (lastExtraFreeKbytes != extraFreekbytes) {
            SystemPropertiesEx.set("sys.sysctl.extra_free_kbytes", Integer.toString(extraFreekbytes));
        } else if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "extraFreekbytes is already " + lastExtraFreeKbytes + ", no need to set");
        }
    }

    private static void configProtectLru() {
        setProtectLruLimit(MemoryConstant.getConfigProtectLruLimit());
        setProtectLruRatio(MemoryConstant.getConfigProtectLruRatio());
        AwareLog.d(TAG, "onProtectLruConfigUpdate");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE);
        buffer.putInt(0);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
        setFileProtectLru(MemoryConstant.MSG_PROTECTLRU_SET_FILENODE);
        dynamicSetProtectLru(DmeServer.getInstance().getProtectLruState());
        sOpenProtectAlwaysConfig = MemoryConstant.getConfigProtectLruSwitch();
    }

    public static void enableProtectLru() {
        if (!sOpenProtectAlways) {
            setProtectLruSwitch(true);
            if (sOpenProtectAlwaysConfig) {
                sOpenProtectAlways = true;
            }
        }
    }

    public static void disableProtectLru() {
        if (!sOpenProtectAlways) {
            setProtectLruSwitch(false);
        }
    }

    public static void onProtectLruConfigUpdate() {
        configProtectLru();
        if (sOpenProtectAlwaysConfig) {
            sOpenProtectAlways = false;
            enableProtectLru();
            return;
        }
        disableProtectLru();
    }

    public static void dynamicSetProtectLru(int state) {
        if (state == 1) {
            enableProtectLru();
        } else if (state == 0) {
            disableProtectLru();
        }
    }

    private static void setProtectLruRatio(int ratio) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "set ProtectLru ratio = " + ratio);
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SET_PROTECTRATIO);
        buffer.putInt(ratio);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void reclaimProcessAll(int pid, boolean suspend) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        if (suspend) {
            buffer.putInt(MemoryConstant.MSG_PROCRECLAIM_ALL_SUSPEND);
        } else {
            buffer.putInt(MemoryConstant.MSG_PROCRECLAIM_ALL);
        }
        buffer.putInt(pid);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private static void setFileProtectLru(int commandType) {
        ArrayMap<Integer, ArraySet<String>> filterMap = MemoryConstant.getFileCacheMap();
        if (filterMap != null) {
            AwareLog.i(TAG, "set ProtectLru filterMap size:" + filterMap.size());
            ByteBuffer buffer = ByteBuffer.allocate(272);
            for (int i = 0; i < filterMap.size(); i++) {
                try {
                    int index = filterMap.keyAt(i).intValue();
                    int isDir = 0;
                    if (index > 50) {
                        index -= 50;
                        isDir = 1;
                    }
                    ArraySet<String> filterSet = filterMap.valueAt(i);
                    if (filterSet != null) {
                        Iterator<String> it = filterSet.iterator();
                        while (it.hasNext()) {
                            String filterStr = it.next();
                            if (!TextUtils.isEmpty(filterStr)) {
                                byte[] stringBytes = filterStr.getBytes("UTF-8");
                                if (stringBytes.length >= 1) {
                                    if (stringBytes.length <= 255) {
                                        if (AwareLog.getDebugLogSwitch()) {
                                            AwareLog.d(TAG, "setPackageProtectLru filterStr = " + filterStr);
                                        }
                                        buffer.clear();
                                        buffer.putInt(commandType);
                                        buffer.putInt(isDir);
                                        buffer.putInt(index);
                                        buffer.putInt(stringBytes.length);
                                        buffer.put(stringBytes);
                                        buffer.putChar(0);
                                        if (sendPacket(buffer) != 0) {
                                            AwareLog.w(TAG, "setPackageProtectLru sendPacket failed");
                                        }
                                    }
                                }
                                AwareLog.w(TAG, "setPackageProtectLru incorrect filterStr = " + filterStr);
                            }
                        }
                    } else {
                        return;
                    }
                } catch (UnsupportedEncodingException e) {
                    AwareLog.e(TAG, "setPackageProtectLru UTF-8 not supported");
                    return;
                }
            }
        }
    }

    private static void setProtectLruLimit(String lruConfigStr) {
        if (checkLimitConfigStr(lruConfigStr)) {
            ByteBuffer buffer = ByteBuffer.allocate(268);
            try {
                byte[] stringBytes = lruConfigStr.getBytes("UTF-8");
                if (stringBytes.length >= 1) {
                    if (stringBytes.length <= 255) {
                        if (AwareLog.getDebugLogSwitch()) {
                            AwareLog.d(TAG, "setProtectLruLimit configstr=" + lruConfigStr);
                        }
                        buffer.clear();
                        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SET_PROTECTZONE);
                        buffer.putInt(stringBytes.length);
                        buffer.put(stringBytes);
                        buffer.putChar(0);
                        if (sendPacket(buffer) != 0) {
                            AwareLog.w(TAG, "setProtectLruLimit sendPacket failed");
                            return;
                        }
                        return;
                    }
                }
                AwareLog.w(TAG, "setProtectLruLimit incorrect config = " + lruConfigStr);
            } catch (UnsupportedEncodingException e) {
                AwareLog.e(TAG, "setProtectLruLimit UTF-8 not supported?!?");
            }
        }
    }

    private static boolean checkLimitConfigStr(String lruConfigStr) {
        if (lruConfigStr == null) {
            return false;
        }
        String[] lruConfigStrArray = lruConfigStr.split(" ");
        if (lruConfigStrArray.length != 3) {
            return false;
        }
        for (String str : lruConfigStrArray) {
            try {
                int levelValue = Integer.parseInt(str);
                if (levelValue >= 0) {
                    if (levelValue <= 100) {
                    }
                }
                AwareLog.w(TAG, "protect lru level value is invalid: " + levelValue);
                return false;
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "protectlru limit parse error");
                return false;
            }
        }
        return true;
    }

    private static void setProtectLruSwitch(boolean isEnable) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "set ProtectLru switch = " + isEnable);
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SWITCH);
        buffer.putInt(isEnable ? 1 : 0);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void enablePsi() {
        setPsiThreshold(0, MemoryConstant.getConfigPsiThreshold(0));
        setPsiThreshold(1, MemoryConstant.getConfigPsiThreshold(1));
        setPsiThreshold(2, MemoryConstant.getConfigPsiThreshold(2));
        if (MemoryConstant.getPressureReclaimSwitch() == 1) {
            AwareLog.d(TAG, "enable psi detector.");
            PressureDetector.getInstance().startPressureDetect();
        }
    }

    private static void setPsiThreshold(int psiRes, String thresholdStr) {
        if (psiRes >= 0 && psiRes < 3 && thresholdStr != null && MemoryConstant.getPressureReclaimSwitch() != 0) {
            AwareLog.d(TAG, "setPsiThreshold, resType:" + psiRes + ", thresholdStr:" + thresholdStr);
            try {
                String[] thresholdStrArray = thresholdStr.split(" ");
                if (thresholdStrArray.length == 6) {
                    for (int i = 0; i < 3; i++) {
                        int typeValue = Integer.parseInt(thresholdStrArray[i * 2]);
                        int thresholdValue = Integer.parseInt(thresholdStrArray[(i * 2) + 1]);
                        if ((typeValue == 0 || typeValue == 1) && thresholdValue >= 0) {
                            if (thresholdValue <= 1000000) {
                                int result = PressureDetector.getInstance().registPressure(psiRes, typeValue, thresholdValue, i + 1);
                                if (result != 0) {
                                    MemoryConstant.setPressureReclaimSwitch(0);
                                    AwareLog.w(TAG, "registPressure error: " + result);
                                    return;
                                }
                            }
                        }
                        AwareLog.w(TAG, "psi threshold value is invalid");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "setPsiThreshold error");
            }
        }
    }

    public static boolean checkRamSize(String ramSize) {
        if (ramSize == null) {
            return false;
        }
        try {
            long ramSizeL = Long.parseLong(ramSize.trim());
            long ramSizeRecorded = MemoryConstant.getRamSizeMB();
            if (ramSizeRecorded == -1) {
                MemInfoReaderExt memInfoReader = new MemInfoReaderExt();
                memInfoReader.readMemInfo();
                long totalMemMb = memInfoReader.getTotalSize() / MemoryConstant.MB_SIZE;
                if (totalMemMb > ramSizeL || totalMemMb <= sLastRamSize) {
                    sLastRamSize = ramSizeL;
                    return false;
                }
                sLastRamSize = ramSizeL;
                MemoryConstant.setRamSizeMB(ramSizeL);
                return true;
            } else if (ramSizeL == ramSizeRecorded) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse ramsze error: " + e);
            return false;
        }
    }

    public static void initialRamSizeLowerBound() {
        sLastRamSize = 0;
    }

    public static void rccCompress(long reqKb) {
        int compressMb = (int) (((long) (((double) reqKb) / 0.65d)) / 1024);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_RCC_COMPRESS);
        buffer.putInt(compressMb);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "rcc compress " + compressMb + "mb");
        }
    }

    public static void rccIdleCompressEnable(boolean enable) {
        AwareLog.i(TAG, "rcc rccIdleCompressEnable");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_RCC_IDLE_COMPRESS_SWITCH);
        buffer.putInt(enable ? 1 : 0);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void rccSetIdleThreshold(int percent) {
        AwareLog.i(TAG, "rcc rccSetIdleThreshold");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_RCC_IDLE_THRESHOLD);
        buffer.putInt(percent);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void rccSetAvailTarget(int target) {
        if (target != sLastTargetRccAvail) {
            AwareLog.i(TAG, "rcc rccSetAvailTarget, target:" + target);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putInt(MemoryConstant.MSG_RCC_AVAIL_TARGET);
            buffer.putInt(target);
            IAwaredConnection.getInstance().sendPacket(buffer.array());
            sLastTargetRccAvail = target;
        }
    }

    public static void rccSetAnonTarget(int target) {
        if (target >= 0) {
            AwareLog.i(TAG, "rcc rccSetAnonTarget");
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putInt(MemoryConstant.MSG_RCC_ANON_TARGET);
            buffer.putInt(target);
            IAwaredConnection.getInstance().sendPacket(buffer.array());
        }
    }

    public static void rccSetSwapPercent(int target) {
        if (target >= 0) {
            AwareLog.i(TAG, "rcc rccSetSwapPercent");
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putInt(MemoryConstant.MSG_RCC_ZRAM_PERCENT_LOW);
            buffer.putInt(target);
            IAwaredConnection.getInstance().sendPacket(buffer.array());
        }
    }

    public static void rccPause() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(MemoryConstant.MSG_RCC_PAUSE);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void setReclaimGpuMemory(boolean isCompress, int pid) {
        if (AwareLog.getDebugLogSwitch()) {
            StringBuilder sb = new StringBuilder();
            sb.append(isCompress ? "compress" : "decompress");
            sb.append(" GPU memory, pid:");
            sb.append(pid);
            AwareLog.d(TAG, sb.toString());
        }
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(MemoryConstant.MSG_COMPRESS_GPU);
        buffer.putInt(isCompress ? 1 : 0);
        buffer.putInt(pid);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
        if (isCompress) {
            sLastCpuCompressTime = SystemClock.uptimeMillis();
        }
    }

    public static boolean shouldPauseGpuCompress() {
        return sLastCpuCompressTime + MemoryConstant.MIN_INTERVAL_OP_TIMEOUT > SystemClock.uptimeMillis();
    }

    public static boolean trimMemory(HwActivityManagerService hwAms, String proc, int level) {
        return trimMemory(hwAms, proc, -2, level, true);
    }

    public static boolean trimMemory(HwActivityManagerService hwAms, String proc, int userId, int level, boolean fromAware) {
        if (hwAms == null) {
            return false;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "trim Memory, proc:" + proc + " userId:" + userId + " level:" + level + " fromAware:" + fromAware);
        }
        try {
            return hwAms.setProcessMemoryTrimLevel(proc, userId, level, fromAware);
        } catch (RemoteException e) {
            AwareLog.e(TAG, "trim Memory remote exception, proc:" + proc + " userId:" + userId + " level:" + level + " fromAware:" + fromAware);
            return false;
        } catch (IllegalArgumentException e2) {
            AwareLog.e(TAG, "trim Memory illegal arg exception, proc:" + proc + " userId:" + userId + " level:" + level + " fromAware:" + fromAware);
            return false;
        }
    }

    public static void sendActivityDisplayedTime(String activityName, int pid, int time) {
        ByteBuffer buffer = ByteBuffer.allocate(276);
        try {
            byte[] stringBytes = activityName.getBytes("UTF-8");
            if (stringBytes.length >= 1) {
                if (stringBytes.length <= 255) {
                    if (AwareLog.getDebugLogSwitch()) {
                        AwareLog.d(TAG, "sendActivityDisplayedTime: " + activityName + " " + pid + " " + time);
                    }
                    buffer.clear();
                    buffer.putInt(MemoryConstant.MSG_ACTIVITY_DISPLAY_STATISTICS);
                    buffer.putInt(pid);
                    buffer.putInt(time);
                    buffer.putInt(stringBytes.length);
                    buffer.put(stringBytes);
                    buffer.putChar(0);
                    if (sendPacket(buffer) != 0) {
                        AwareLog.w(TAG, "sendActivityDisplayedTime sendPacket failed");
                    }
                    return;
                }
            }
            AwareLog.w(TAG, "sendActivityDisplayedTime incorrect activityName = " + activityName);
        } catch (UnsupportedEncodingException e) {
            AwareLog.e(TAG, "sendActivityDisplayedTime UTF-8 not supported?!?");
        }
    }

    public static void writeMemMonitorSwitch(int switchValue) {
        if (switchValue > 1 || switchValue < 0) {
            AwareLog.w(TAG, "invalid mmonitor switch value");
            return;
        }
        AwareLog.i(TAG, "writeMemMonitorSwitch = " + switchValue);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_MMONITOR_SWITCH);
        buffer.putInt(switchValue);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private static void createSocket() {
        if (sMemorySocket == null) {
            try {
                sMemorySocket = new LocalSocket(3);
                sMemorySocket.connect(new LocalSocketAddress("iawared", LocalSocketAddress.Namespace.RESERVED));
                sOutputStream = sMemorySocket.getOutputStream();
                sInputStream = sMemorySocket.getInputStream();
                sMemorySocket.setReceiveBufferSize(8);
            } catch (IOException e) {
                AwareLog.e(TAG, "createSocket happend IOException");
                destroySocket();
            }
        }
    }

    public static void destroySocket() {
        synchronized (LOCK) {
            CommonUtils.closeStream(sOutputStream, TAG, null);
            sOutputStream = null;
            CommonUtils.closeStream(sInputStream, TAG, null);
            sInputStream = null;
            CommonUtils.closeStream(sMemorySocket, TAG, null);
            sMemorySocket = null;
        }
    }

    public static int sendPacket(ByteBuffer buffer) {
        synchronized (LOCK) {
            if (buffer == null) {
                try {
                    AwareLog.w(TAG, "sendPacket ByteBuffer is null!");
                    return -1;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                int retry = 2;
                do {
                    if (sMemorySocket == null) {
                        createSocket();
                    }
                    if (sOutputStream != null) {
                        try {
                            sOutputStream.write(buffer.array(), 0, buffer.position());
                            flush(FLUSH_TIMEOUT);
                            return 0;
                        } catch (IOException e) {
                            AwareLog.e(TAG, "sOutputStream write failed! happend IOException");
                            destroySocket();
                            retry--;
                            continue;
                        }
                    }
                } while (retry > 0);
                return -1;
            }
        }
    }

    private static void flush(long millis) throws IOException {
        FileDescriptor myFd = sMemorySocket.getFileDescriptor();
        if (myFd != null) {
            long start = SystemClock.uptimeMillis();
            Int32RefEx pending = new Int32RefEx(0);
            while (true) {
                try {
                    OsEx.ioctlInt(myFd, OsConstantsEx.TIOCOUTQ, pending);
                    if (pending.getValue() > 0) {
                        if (SystemClock.uptimeMillis() - start < millis) {
                            int left = pending.getValue();
                            if (left <= 1000) {
                                try {
                                    Thread.sleep(0, 10);
                                } catch (InterruptedException e) {
                                    return;
                                }
                            } else if (left <= 5000) {
                                Thread.sleep(0, 500);
                            } else {
                                Thread.sleep(1);
                            }
                        } else {
                            AwareLog.e(TAG, "Socket flush timed out !!!");
                            throw new IOException("flush timeout");
                        }
                    } else {
                        return;
                    }
                } catch (ErrnoException e2) {
                    throw ErrnoExceptionEx.rethrowAsIOException(e2);
                }
            }
        } else {
            throw new IOException("socket closed");
        }
    }

    public static byte[] recvPacket(int byteSize) {
        byte[] emptyByte = new byte[0];
        if (byteSize <= 0 || sInputStream == null) {
            return emptyByte;
        }
        byte[] recvByte = new byte[byteSize];
        try {
            Arrays.fill(recvByte, (byte) 0);
            if (sInputStream.read(recvByte) == 0) {
                return emptyByte;
            }
            return recvByte;
        } catch (IOException e) {
            AwareLog.e(TAG, " sInputStream write failed happend IOException!");
            destroySocket();
            return emptyByte;
        }
    }

    public static void enterSpecialSceneNotify(int totalWaterMark, int workerMask, int autoStopTimeout) {
        if (MemoryConstant.getConfigIonSpeedupSwitch() == 0) {
            AwareLog.w(TAG, "iaware2.0 mem feature camera ion memory speedup switch off");
        } else {
            doEnterSpecialSceneNotify(totalWaterMark, workerMask, autoStopTimeout);
        }
    }

    public static void doEnterSpecialSceneNotify(int totalWaterMark, int workerMask, int autoStopTimeout) {
        if (!MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get()) {
            AwareLog.w(TAG, "iaware2.0 mem feature  " + MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get());
            return;
        }
        AwareLog.i(TAG, "Enter special scene, totalWaterMark: " + totalWaterMark + ", workerMask: " + workerMask + ", autoStopTimeout: " + autoStopTimeout);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putInt(340);
        buffer.putInt(totalWaterMark);
        buffer.putInt(workerMask);
        buffer.putInt(autoStopTimeout);
        sendPacket(buffer);
    }

    public static void exitSpecialSceneNotify() {
        if (MemoryConstant.getConfigIonSpeedupSwitch() == 0) {
            AwareLog.w(TAG, "iaware2.0 mem feature camera ion memory speedup switch off...");
        } else {
            doExitSpecialSceneNotify();
        }
    }

    public static void doExitSpecialSceneNotify() {
        if (!MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get()) {
            AwareLog.w(TAG, "iaware2.0 mem feature " + MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get());
            return;
        }
        AwareLog.i(TAG, "Exit special scene");
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(MemoryConstant.MSG_SPECIAL_SCENE_POOL_EXIT);
        sendPacket(buffer);
    }

    public static AwareConfig.Item getCurrentMemItem(AwareConfig configList, boolean isCust) {
        AwareConfig.Item lastItem;
        String ramSize;
        if (configList == null || configList.getConfigList() == null) {
            AwareLog.w(TAG, "loadMemConfig fail, null configList");
            return null;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        initialRamSizeLowerBound();
        for (AwareConfig.Item item : itemList) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadMemConstantConfig continue cause null item");
            } else {
                String ramSize2 = item.getProperties().get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
                if (checkRamSize(ramSize2)) {
                    AwareLog.i(TAG, "loadMemConfig success. ramSize: " + ramSize2);
                    return item;
                }
            }
        }
        if (isCust || itemList.size() <= 0 || (lastItem = itemList.get(itemList.size() - 1)) == null || lastItem.getProperties() == null || (ramSize = lastItem.getProperties().get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME)) == null) {
            return null;
        }
        try {
            MemInfoReaderExt minfo = new MemInfoReaderExt();
            minfo.readMemInfo();
            Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
            if (totalMemMb.longValue() > Long.parseLong(ramSize.trim())) {
                AwareLog.i(TAG, "inheriteMemConfig success. ramSize: " + ramSize + " totalMemMb: " + totalMemMb);
                return lastItem;
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse ramsize error");
        }
        return null;
    }

    public static int getNativeRelatedPid(String processName) {
        int[] pids = ProcessExt.getPids(PROCESS_PROC_PATH, (int[]) null);
        if (pids == null) {
            return -1;
        }
        for (int pid : pids) {
            if (pid < 0) {
                break;
            }
            String[] procStatsString = new String[6];
            if (ProcessEx.readProcFile(new File(new File(PROCESS_PROC_PATH, Integer.toString(pid)), PROCESS_STAT_PATH).toString(), PROCESS_FULL_STATS_FORMAT, procStatsString, new long[6], (float[]) null) && procStatsString[0].equals(processName)) {
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "getNativeRelated pid=" + pid);
                }
                return pid;
            }
        }
        return -1;
    }

    public static void setTopnProcMem(int topN) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_TOPN_PROCESS_MEM);
        buffer.putInt(topN);
        AwareLog.i(TAG, "set process memory topn:" + topN);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void sortByTimeIfNeed(List<AwareProcessBlockInfo> procs, int level) {
        AwareAppMngSort sorted;
        if (AwareAppMngSort.checkAppMngEnable() && (sorted = AwareAppMngSort.getInstance()) != null) {
            sorted.sortByTimeIfNeed(procs, level);
        }
    }

    public static AwareAppMngSortPolicy getCachedCleanPolicy() {
        if (!AwareAppMngSort.checkAppMngEnable()) {
            return new AwareAppMngSortPolicy();
        }
        AwareAppMngSort sorted = AwareAppMngSort.getInstance();
        if (sorted == null) {
            return new AwareAppMngSortPolicy();
        }
        return sorted.getCachedCleanPolicy();
    }
}
