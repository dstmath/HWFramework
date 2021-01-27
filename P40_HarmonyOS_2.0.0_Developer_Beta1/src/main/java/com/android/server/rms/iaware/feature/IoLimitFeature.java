package com.android.server.rms.iaware.feature;

import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IoLimitFeature {
    private static final Object INSTANCE_LOCK = new Object();
    private static final Object LOCK = new Object();
    private static final int MAX_TRANSFER_NUM = 30;
    private static final int MSG_IO_BASE_VALUE = 200;
    private static final int MSG_IO_LIMIT_REMOVE_ALL = 213;
    private static final int MSG_IO_LIMIT_REMOVE_TASK = 212;
    private static final int MSG_IO_LIMIT_SET_TASK = 211;
    private static final int MSG_IO_LIMIT_SWITCHING = 210;
    private static final int NUMBER_OF_INT = 80;
    private static final int SIZE_OF_INT = 4;
    private static final String TAG = "IoLimit";
    private static IoLimitFeature sInstance = null;

    public static IoLimitFeature getInstance() {
        IoLimitFeature ioLimitFeature;
        synchronized (INSTANCE_LOCK) {
            if (sInstance == null) {
                sInstance = new IoLimitFeature();
            }
            ioLimitFeature = sInstance;
        }
        return ioLimitFeature;
    }

    public boolean enable(IoLimitGroup groupType) {
        return setIoLimitSwitch(groupType, 1);
    }

    public boolean disable(IoLimitGroup groupType) {
        return setIoLimitSwitch(groupType, 0);
    }

    public boolean setIoLimitTaskList(IoLimitGroup groupType, Map<Integer, Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        boolean res = true;
        int index = 0;
        Map<Integer, Integer> pidSend = new HashMap<>(30);
        for (Map.Entry<Integer, Integer> entry : pidList.entrySet()) {
            pidSend.put(entry.getKey(), entry.getValue());
            index++;
            if (index == 30) {
                res &= setToDaemon(groupType, pidSend);
                index = 0;
                pidSend.clear();
            }
        }
        return res & setToDaemon(groupType, pidSend);
    }

    public boolean removeIoLimitTaskList(SparseSet pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        boolean res = true;
        int index = 0;
        ArrayList<Integer> pidSend = new ArrayList<>(30);
        int size = pidList.size();
        for (int i = 0; i < size; i++) {
            index++;
            pidSend.add(Integer.valueOf(pidList.keyAt(i)));
            if (index == 30) {
                res &= removeFromDaemon(pidSend);
                index = 0;
                pidSend.clear();
            }
        }
        return res & removeFromDaemon(pidSend);
    }

    private boolean setToDaemon(IoLimitGroup groupType, Map<Integer, Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return true;
        }
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(MSG_IO_LIMIT_SET_TASK);
        buffer.putInt(getGroupTypeCode(groupType));
        buffer.putInt(pidList.size());
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

    public boolean setIoLimitTaskList(IoLimitGroup groupType, Map<Integer, Integer> pidList, boolean removeExisted) {
        boolean ioLimitTaskList;
        synchronized (LOCK) {
            if (removeExisted) {
                removeOneGroupPid(groupType);
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
        buffer.putInt(MSG_IO_LIMIT_REMOVE_TASK);
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
        SparseSet pidList = new SparseSet();
        pidList.add(pid);
        return removeIoLimitTaskList(pidList);
    }

    private boolean setIoLimitSwitch(IoLimitGroup groupType, int policy) {
        int type = getGroupTypeCode(groupType);
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(MSG_IO_LIMIT_SWITCHING);
        buffer.putInt(type);
        buffer.putInt(policy);
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.e(TAG, "Failed to set switch");
        }
        return res;
    }

    public boolean removeOneGroupPid(IoLimitGroup groupType) {
        ByteBuffer buffer = ByteBuffer.allocate(MemoryConstant.MSG_PROCRECLAIM_ALL);
        buffer.putInt(MSG_IO_LIMIT_REMOVE_ALL);
        buffer.putInt(getGroupTypeCode(groupType));
        boolean res = sendPacket(buffer.array());
        if (!res) {
            AwareLog.e(TAG, "Failed to set switch");
        }
        return res;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.rms.iaware.feature.IoLimitFeature$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$rms$iaware$feature$IoLimitGroup = new int[IoLimitGroup.values().length];

        static {
            try {
                $SwitchMap$com$android$server$rms$iaware$feature$IoLimitGroup[IoLimitGroup.LIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$rms$iaware$feature$IoLimitGroup[IoLimitGroup.HEAVY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private int getGroupTypeCode(IoLimitGroup groupType) {
        int i = AnonymousClass1.$SwitchMap$com$android$server$rms$iaware$feature$IoLimitGroup[groupType.ordinal()];
        if (i == 1 || i != 2) {
            return 0;
        }
        return 1;
    }
}
