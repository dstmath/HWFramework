package com.android.server.rms.iaware.feature;

import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class CpuLimitFeature {
    private static final Object INSTANCE_LOCK = new Object();
    private static final int MAX_TRANSFER_NUM = 30;
    private static final int MSG_CPU_BASE_VALUE = 170;
    private static final int MSG_CPU_LIMIT_REMOVE_ALL = 172;
    private static final int MSG_CPU_LIMIT_REMOVE_TASK = 171;
    private static final int MSG_CPU_LIMIT_SET_TASK = 170;
    private static final int NUMBER_OF_INT = 80;
    private static final int SIZE_OF_INT = 4;
    private static final String TAG = "CpuLimit";
    private static CpuLimitFeature sInstance = null;
    private final Object lock = new Object();

    public static CpuLimitFeature getInstance() {
        synchronized (INSTANCE_LOCK) {
            if (sInstance == null) {
                sInstance = new CpuLimitFeature();
                return sInstance;
            }
            return sInstance;
        }
    }

    public boolean setCpuLimitTaskList(Map<Integer, Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        boolean res = true;
        int index = 0;
        Map<Integer, Integer> pidSend = new HashMap<>(pidList.size());
        for (Map.Entry<Integer, Integer> entry : pidList.entrySet()) {
            pidSend.put(entry.getKey(), entry.getValue());
            index++;
            if (index == 30) {
                res &= setToDaemon(pidSend);
                index = 0;
                pidSend.clear();
            }
        }
        return res & setToDaemon(pidSend);
    }

    public boolean removeCpuLimitTaskList(SparseSet pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        boolean res = true;
        int index = 0;
        SparseSet pidSend = new SparseSet();
        int size = pidList.size();
        for (int i = 0; i < size; i++) {
            index++;
            pidSend.add(pidList.keyAt(i));
            if (index == 30) {
                res &= removeFromDaemon(pidSend);
                index = 0;
                pidSend.clear();
            }
        }
        return res & removeFromDaemon(pidSend);
    }

    private boolean setToDaemon(Map<Integer, Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        int size = pidList.size();
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(170);
        buffer.putInt(size);
        for (Map.Entry<Integer, Integer> entry : pidList.entrySet()) {
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
                removeAllPids();
            }
            cpuLimitTaskList = setCpuLimitTaskList(pidList);
        }
        return cpuLimitTaskList;
    }

    private boolean removeFromDaemon(SparseSet pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        int size = pidList.size();
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(MSG_CPU_LIMIT_REMOVE_TASK);
        buffer.putInt(size);
        for (int i = 0; i < size; i++) {
            buffer.putInt(pidList.keyAt(i));
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
        SparseSet pidList = new SparseSet();
        pidList.add(pid);
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
