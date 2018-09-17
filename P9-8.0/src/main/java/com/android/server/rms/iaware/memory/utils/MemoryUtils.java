package com.android.server.rms.iaware.memory.utils;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.MutableInt;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.feature.MemoryFeature2;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class MemoryUtils {
    private static final long FLUSH_TIMEOUT = 2000;
    private static final int MAX_RECV_BYTE_BUFFER_LENTH = 8;
    private static final String MEMORY_SOCKET = "iawared";
    private static final String TAG = "AwareMem_MemoryUtils";
    private static InputStream mInputStream;
    private static final Object mLock = new Object();
    private static LocalSocket mMemorySocket;
    private static OutputStream mOutputStream;

    public static AwareAppMngSortPolicy getAppMngSortPolicy(int resourceType, int groupId) {
        return getAppMngSortPolicy(resourceType, groupId, 0);
    }

    public static AwareAppMngSortPolicy getAppMngSortPolicy(int resourceType, int groupId, int subType) {
        if (!AwareAppMngSort.checkAppMngEnable() || groupId < 0 || groupId > 3) {
            return null;
        }
        AwareAppMngSort sorted = AwareAppMngSort.getInstance();
        if (sorted == null) {
            return null;
        }
        return sorted.getAppMngSortPolicy(resourceType, subType, groupId);
    }

    public static AwareAppMngSortPolicy getAppMngSortPolicyForMemRepair(int sceneType) {
        if (!AwareAppMngSort.checkAppMngEnable()) {
            return null;
        }
        AwareAppMngSort sorted = AwareAppMngSort.getInstance();
        if (sorted == null) {
            return null;
        }
        return sorted.getAppMngSortPolicyForMemRepair(sceneType);
    }

    public static List<AwareProcessBlockInfo> getAppMngProcGroup(AwareAppMngSortPolicy policy, int groupId) {
        if (policy == null) {
            AwareLog.e(TAG, "getAppMngProcGroup sort policy null!");
            return null;
        }
        List<AwareProcessBlockInfo> processGroups = null;
        switch (groupId) {
            case 0:
                processGroups = policy.getForbidStopProcBlockList();
                break;
            case 1:
                processGroups = policy.getShortageStopProcBlockList();
                break;
            case 2:
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
        if (swappiness > 200 || swappiness < 0) {
            AwareLog.w(TAG, "invalid swappiness value");
            return;
        }
        AwareLog.i(TAG, "setSwappiness = " + swappiness);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(302);
        buffer.putInt(swappiness);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void writeDirectSwappiness(int directswappiness) {
        if (directswappiness > 200 || directswappiness < 0) {
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
            return;
        }
        int lastExtraFreeKbytes = SystemProperties.getInt("sys.sysctl.extra_free_kbytes", MemoryConstant.PROCESSLIST_EXTRA_FREE_KBYTES);
        if (lastExtraFreeKbytes == extrafreekbytes) {
            AwareLog.d(TAG, "extrafreekbytes is already " + lastExtraFreeKbytes + ", no need to set");
        } else {
            SystemProperties.set("sys.sysctl.extra_free_kbytes", Integer.toString(extrafreekbytes));
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
    }

    public static void enableProtectLru() {
        setProtectLruSwitch(true);
    }

    public static void disableProtectLru() {
        setProtectLruSwitch(false);
    }

    public static void onProtectLruConfigUpdate() {
        configProtectLru();
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
        AwareLog.d(TAG, "set ProtectLru ratio = " + ratio);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SET_PROTECTRATIO);
        buffer.putInt(ratio);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private static void setFileProtectLru(int commandType) {
        ArrayMap<Integer, ArraySet<String>> filterMap = MemoryConstant.getFileCacheMap();
        if (filterMap != null) {
            AwareLog.i(TAG, "set ProtectLru filterMap size:" + filterMap.size());
            ByteBuffer buffer = ByteBuffer.allocate(272);
            int i = 0;
            try {
                int mapSize = filterMap.size();
                while (i < mapSize) {
                    int index = ((Integer) filterMap.keyAt(i)).intValue();
                    int isDir = 0;
                    if (index > 50) {
                        index -= 50;
                        isDir = 1;
                    }
                    ArraySet<String> filterSet = (ArraySet) filterMap.valueAt(i);
                    if (filterSet != null) {
                        for (String filterStr : filterSet) {
                            if (!TextUtils.isEmpty(filterStr)) {
                                byte[] stringBytes = filterStr.getBytes("UTF-8");
                                if (stringBytes.length < 1 || stringBytes.length > 255) {
                                    AwareLog.w(TAG, "setPackageProtectLru incorrect filterStr = " + filterStr);
                                } else {
                                    AwareLog.d(TAG, "setPackageProtectLru filterStr = " + filterStr);
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
                        }
                        i++;
                    } else {
                        return;
                    }
                }
            } catch (UnsupportedEncodingException e) {
                AwareLog.w(TAG, "setPackageProtectLru UTF-8 not supported");
            }
        }
    }

    private static void setProtectLruLimit(String lruConfigStr) {
        if (checkLimitConfigStr(lruConfigStr)) {
            ByteBuffer buffer = ByteBuffer.allocate(268);
            try {
                byte[] stringBytes = lruConfigStr.getBytes("UTF-8");
                if (stringBytes.length < 1 || stringBytes.length > 255) {
                    AwareLog.w(TAG, "setProtectLruLimit incorrect config = " + lruConfigStr);
                    return;
                }
                AwareLog.d(TAG, "setProtectLruLimit configstr=" + lruConfigStr);
                buffer.clear();
                buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SET_PROTECTZONE);
                buffer.putInt(stringBytes.length);
                buffer.put(stringBytes);
                buffer.putChar(0);
                if (sendPacket(buffer) != 0) {
                    AwareLog.w(TAG, "setProtectLruLimit sendPacket failed");
                }
            } catch (UnsupportedEncodingException e) {
                AwareLog.w(TAG, "setProtectLruLimit UTF-8 not supported?!?");
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
        for (String parseInt : lruConfigStrArray) {
            int levelValue = Integer.parseInt(parseInt);
            if (levelValue < 0 || levelValue > 100) {
                AwareLog.w(TAG, "protect lru level value is invalid: " + levelValue);
                return false;
            }
        }
        return true;
    }

    private static void setProtectLruSwitch(boolean isEnable) {
        AwareLog.d(TAG, "set ProtectLru switch = " + isEnable);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SWITCH);
        buffer.putInt(isEnable ? 1 : 0);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static boolean checkRamSize(String ramSize, Long totalMemMb) {
        if (ramSize == null) {
            return false;
        }
        try {
            long ramSizeL = Long.parseLong(ramSize.trim());
            if (totalMemMb.longValue() > ramSizeL || totalMemMb.longValue() <= ramSizeL - 1024) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse ramsze error: " + e);
            return false;
        }
    }

    public static void setReclaimGPUMemory(boolean isCompress, int pid) {
        AwareLog.d(TAG, (isCompress ? "compress" : "decompress") + " GPU memory, pid:" + pid);
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(MemoryConstant.MSG_COMPRESS_GPU);
        buffer.putInt(isCompress ? 1 : 0);
        buffer.putInt(pid);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static boolean trimMemory(HwActivityManagerService hwAms, String proc, int level) {
        boolean ret = false;
        if (hwAms == null) {
            return ret;
        }
        AwareLog.d(TAG, "trim Memory, proc:" + proc + " level:" + level);
        try {
            ret = hwAms.setProcessMemoryTrimLevel(proc, -2, level, true);
        } catch (RemoteException e) {
            AwareLog.w(TAG, "trim Memory remote exception, proc:" + proc + " level:" + level);
        } catch (IllegalArgumentException e2) {
            AwareLog.w(TAG, "trim Memory illegal arg exception, proc:" + proc + " level:" + level);
        }
        return ret;
    }

    public static void sendActivityDisplayedTime(String activityName, int pid, int time) {
        ByteBuffer buffer = ByteBuffer.allocate(276);
        try {
            byte[] stringBytes = activityName.getBytes("UTF-8");
            if (stringBytes.length < 1 || stringBytes.length > 255) {
                AwareLog.w(TAG, "sendActivityDisplayedTime incorrect activityName = " + activityName);
                return;
            }
            AwareLog.d(TAG, "sendActivityDisplayedTime: " + activityName + " " + pid + " " + time);
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
        } catch (UnsupportedEncodingException e) {
            AwareLog.w(TAG, "sendActivityDisplayedTime UTF-8 not supported?!?");
        }
    }

    public static void writeMMonitorSwitch(int switchValue) {
        if (switchValue > 1 || switchValue < 0) {
            AwareLog.w(TAG, "invalid mmonitor switch value");
            return;
        }
        AwareLog.i(TAG, "writeMMonitorSwitch = " + switchValue);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_MMONITOR_SWITCH);
        buffer.putInt(switchValue);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private static void createSocket() {
        if (mMemorySocket == null) {
            try {
                mMemorySocket = new LocalSocket(3);
                mMemorySocket.connect(new LocalSocketAddress("iawared", Namespace.RESERVED));
                mOutputStream = mMemorySocket.getOutputStream();
                mInputStream = mMemorySocket.getInputStream();
                mMemorySocket.setReceiveBufferSize(8);
                AwareLog.d(TAG, "createSocket Success!");
            } catch (IOException e) {
                AwareLog.e(TAG, "createSocket happend IOException");
                destroySocket();
            }
        }
    }

    public static void destroySocket() {
        synchronized (mLock) {
            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    AwareLog.e(TAG, "mOutputStream close failed! happend IOException");
                }
                mOutputStream = null;
            }
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e2) {
                    AwareLog.e(TAG, "mInputStream close failed! happend IOException");
                }
                mInputStream = null;
            }
            if (mMemorySocket != null) {
                try {
                    mMemorySocket.close();
                } catch (IOException e3) {
                    AwareLog.e(TAG, "closeSocket failed! happend IOException");
                }
                mMemorySocket = null;
            }
        }
        return;
    }

    public static int sendPacket(ByteBuffer buffer) {
        synchronized (mLock) {
            if (buffer == null) {
                AwareLog.w(TAG, "sendPacket ByteBuffer is null!");
                return -1;
            }
            int retry = 2;
            do {
                if (mMemorySocket == null) {
                    createSocket();
                }
                if (mOutputStream != null) {
                    try {
                        mOutputStream.write(buffer.array(), 0, buffer.position());
                        flush(FLUSH_TIMEOUT);
                        return 0;
                    } catch (IOException e) {
                        AwareLog.e(TAG, "mOutputStream write failed! happend IOException");
                        destroySocket();
                        retry--;
                        continue;
                    }
                }
            } while (retry > 0);
            return -1;
        }
    }

    private static void flush(long millis) throws IOException {
        FileDescriptor myFd = mMemorySocket.getFileDescriptor();
        if (myFd == null) {
            throw new IOException("socket closed");
        }
        long start = SystemClock.uptimeMillis();
        MutableInt pending = new MutableInt(0);
        while (true) {
            try {
                Os.ioctlInt(myFd, OsConstants.TIOCOUTQ, pending);
                if (pending.value > 0) {
                    if (SystemClock.uptimeMillis() - start >= millis) {
                        AwareLog.e(TAG, "Socket flush timed out !!!");
                        throw new IOException("flush timeout");
                    }
                    int left = pending.value;
                    if (left <= 1000) {
                        try {
                            Thread.sleep(0, 10);
                        } catch (InterruptedException e) {
                            return;
                        }
                    } else if (left <= 5000) {
                        Thread.sleep(0, HwActivityManagerService.SERVICE_ADJ);
                    } else {
                        Thread.sleep(1);
                    }
                } else {
                    return;
                }
            } catch (ErrnoException e2) {
                throw e2.rethrowAsIOException();
            }
        }
    }

    public static byte[] recvPacket(int byteSize) {
        byte[] emptyByte = new byte[0];
        if (byteSize <= 0 || mInputStream == null) {
            return emptyByte;
        }
        byte[] recvByte = new byte[byteSize];
        try {
            Arrays.fill(recvByte, (byte) 0);
            if (mInputStream.read(recvByte) == 0) {
                return emptyByte;
            }
            return recvByte;
        } catch (IOException e) {
            AwareLog.e(TAG, " mInputStream write failed happend IOException!");
            destroySocket();
            return emptyByte;
        }
    }

    public static void enterSpecialSceneNotify(int total_watermark, int worker_mask, int autostop_timeout) {
        if (!MemoryFeature2.isUpMemoryFeature.get() || MemoryConstant.getConfigIonSpeedupSwitch() == 0) {
            AwareLog.w(TAG, "iaware2.0 mem feature  " + MemoryFeature2.isUpMemoryFeature.get() + ",  camera ion memory speedup switch " + MemoryConstant.getConfigIonSpeedupSwitch());
            return;
        }
        AwareLog.i(TAG, "Enter special scene, total_watermark: " + total_watermark + ", worker_mask: " + worker_mask + ", autostop_timeout: " + autostop_timeout);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putInt(340);
        buffer.putInt(total_watermark);
        buffer.putInt(worker_mask);
        buffer.putInt(autostop_timeout);
        sendPacket(buffer);
    }

    public static void exitSpecialSceneNotify() {
        if (!MemoryFeature2.isUpMemoryFeature.get() || MemoryConstant.getConfigIonSpeedupSwitch() == 0) {
            AwareLog.w(TAG, "iaware2.0 mem feature " + MemoryFeature2.isUpMemoryFeature.get() + ",  camera ion memory speedup switch " + MemoryConstant.getConfigIonSpeedupSwitch());
            return;
        }
        AwareLog.i(TAG, "Exit special scene");
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(MemoryConstant.MSG_SPECIAL_SCENE_POOL_EXIT);
        sendPacket(buffer);
    }
}
