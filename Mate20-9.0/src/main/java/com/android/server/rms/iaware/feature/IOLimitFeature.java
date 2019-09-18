package com.android.server.rms.iaware.feature;

import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IOLimitFeature {
    private static final int MAX_TRANSFER_NUM = 30;
    static final int MSG_IO_BASE_VALUE = 200;
    static final int MSG_IO_LIMIT_REMOVE_ALL = 213;
    static final int MSG_IO_LIMIT_REMOVE_TASK = 212;
    static final int MSG_IO_LIMIT_SET_TASK = 211;
    static final int MSG_IO_LIMIT_SWITCHING = 210;
    private static final int NUMBER_OF_INT = 80;
    private static final int SIZE_OF_INT = 4;
    private static final String TAG = "IoLimit";
    private static IOLimitFeature mInstance = null;
    private final Object lock = new Object();

    public static IOLimitFeature getInstance() {
        synchronized (IOLimitFeature.class) {
            if (mInstance == null) {
                mInstance = new IOLimitFeature();
                IOLimitFeature iOLimitFeature = mInstance;
                return iOLimitFeature;
            }
            IOLimitFeature iOLimitFeature2 = mInstance;
            return iOLimitFeature2;
        }
    }

    public boolean enable(IOLimitGroup groupType) {
        return setIoLimitSwitch(groupType, 1);
    }

    public boolean disable(IOLimitGroup groupType) {
        return setIoLimitSwitch(groupType, 0);
    }

    public boolean setIoLimitTaskList(IOLimitGroup groupType, Map<Integer, Integer> pidList) {
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
                res &= set2Daemon(groupType, pidSend);
                index = 0;
                pidSend.clear();
            }
        }
        return res & set2Daemon(groupType, pidSend);
    }

    public boolean removeIoLimitTaskList(ArrayList<Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        boolean res = true;
        int index = 0;
        ArrayList<Integer> pidSend = new ArrayList<>();
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

    private boolean set2Daemon(IOLimitGroup groupType, Map<Integer, Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        int gtype = getGroupTypeCode(groupType);
        int size = pidList.size();
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(211);
        buffer.putInt(gtype);
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

    public boolean setIoLimitTaskList(IOLimitGroup groupType, Map<Integer, Integer> pidList, boolean removeExisted) {
        boolean ioLimitTaskList;
        synchronized (this.lock) {
            if (removeExisted) {
                try {
                    removeOneGroupPID(groupType);
                } catch (Throwable th) {
                    throw th;
                }
            }
            ioLimitTaskList = setIoLimitTaskList(groupType, pidList);
        }
        return ioLimitTaskList;
    }

    private boolean removeFromDaemon(ArrayList<Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        int size = pidList.size();
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(212);
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

    public boolean removeIoLimitTask(int pid) {
        ArrayList<Integer> pidList = new ArrayList<>();
        pidList.add(Integer.valueOf(pid));
        return removeIoLimitTaskList(pidList);
    }

    private boolean setIoLimitSwitch(IOLimitGroup groupType, int policy) {
        int gtype = getGroupTypeCode(groupType);
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(210);
        buffer.putInt(gtype);
        buffer.putInt(policy);
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.e(TAG, "Failed to set switch");
        }
        return res;
    }

    public boolean removeOneGroupPID(IOLimitGroup groupType) {
        int gtype = getGroupTypeCode(groupType);
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(213);
        buffer.putInt(gtype);
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.e(TAG, "Failed to set switch");
        }
        return res;
    }

    private int getGroupTypeCode(IOLimitGroup groupType) {
        switch (groupType) {
            case LIGHT:
                return 0;
            case HEAVY:
                return 1;
            default:
                return 0;
        }
    }
}
