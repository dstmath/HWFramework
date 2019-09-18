package com.android.server.rms.iaware.feature;

import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CpuLimitFeature {
    private static final int MAX_TRANSFER_NUM = 30;
    private static final int MSG_CPU_BASE_VALUE = 170;
    private static final int MSG_CPU_LIMIT_REMOVE_ALL = 172;
    private static final int MSG_CPU_LIMIT_REMOVE_TASK = 171;
    private static final int MSG_CPU_LIMIT_SET_TASK = 170;
    private static final int NUMBER_OF_INT = 80;
    private static final int SIZE_OF_INT = 4;
    private static final String TAG = "CpuLimit";
    private static CpuLimitFeature mInstance = null;
    private final Object lock = new Object();

    public static CpuLimitFeature getInstance() {
        CpuLimitFeature cpuLimitFeature;
        synchronized (CpuLimitFeature.class) {
            if (mInstance == null) {
                mInstance = new CpuLimitFeature();
            }
            cpuLimitFeature = mInstance;
        }
        return cpuLimitFeature;
    }

    public boolean setCpuLimitTaskList(Map<Integer, Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        boolean res = true;
        int index = 0;
        int mapsize = pidList.size();
        Map<Integer, Integer> pidSend = new HashMap<>();
        Iterator keyValuePairs = pidList.entrySet().iterator();
        for (int i = 0; i < mapsize; i++) {
            Map.Entry<Integer, Integer> entry = keyValuePairs.next();
            pidSend.put(entry.getKey(), entry.getValue());
            index++;
            if (30 == index) {
                res &= set2Daemon(pidSend);
                index = 0;
                pidSend.clear();
            }
        }
        return res & set2Daemon(pidSend);
    }

    public boolean removeCpuLimitTaskList(List<Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        boolean res = true;
        int index = 0;
        List<Integer> pidSend = new ArrayList<>();
        int size = pidList.size();
        for (int i = 0; i < size; i++) {
            index++;
            pidSend.add(pidList.get(i));
            if (30 == index) {
                res &= removeFromDaemon(pidSend);
                index = 0;
                pidSend.clear();
            }
        }
        return res & removeFromDaemon(pidSend);
    }

    private boolean set2Daemon(Map<Integer, Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        int size = pidList.size();
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(HwSecDiagnoseConstant.OEMINFO_ID_DEVICE_RENEW);
        buffer.putInt(size);
        Iterator keyValuePairs = pidList.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            Map.Entry<Integer, Integer> entry = keyValuePairs.next();
            buffer.putInt(entry.getKey().intValue());
            buffer.putInt(entry.getValue().intValue());
        }
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.e(TAG, "Failed to set task");
        }
        return res;
    }

    public boolean setCpuLimitTaskList(Map<Integer, Integer> pidList, boolean removeExisted) {
        boolean cpuLimitTaskList;
        synchronized (this.lock) {
            if (removeExisted) {
                try {
                    removeAllPids();
                } catch (Throwable th) {
                    throw th;
                }
            }
            cpuLimitTaskList = setCpuLimitTaskList(pidList);
        }
        return cpuLimitTaskList;
    }

    private boolean removeFromDaemon(List<Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        int size = pidList.size();
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(171);
        buffer.putInt(size);
        for (int i = 0; i < size; i++) {
            buffer.putInt(pidList.get(i).intValue());
        }
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.e(TAG, "Failed to remove task");
        }
        return res;
    }

    private boolean sendPacket(byte[] msg) {
        return IAwaredConnection.getInstance().sendPacket(msg);
    }

    public boolean removeCpuLimitTask(int pid) {
        List<Integer> pidList = new ArrayList<>();
        pidList.add(Integer.valueOf(pid));
        return removeCpuLimitTaskList(pidList);
    }

    public boolean removeAllPids() {
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(MSG_CPU_LIMIT_REMOVE_ALL);
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.e(TAG, "Failed to set switch");
        }
        return res;
    }
}
