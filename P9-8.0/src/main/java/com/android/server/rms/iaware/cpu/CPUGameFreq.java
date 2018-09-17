package com.android.server.rms.iaware.cpu;

import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.SparseArray;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class CPUGameFreq {
    public static final int AWARE_GAME_LEVEL_0 = 0;
    public static final int AWARE_GAME_LEVEL_1 = 1;
    public static final int AWARE_GAME_LEVEL_2 = 2;
    public static final int AWARE_GAME_LEVEL_3 = 3;
    public static final int AWARE_GAME_LEVEL_4 = 4;
    private static final int CLEAN_DELAY_TIME = 5000;
    private static final int HANDLER_CANCEL_MSG = 3;
    private static final int HANDLER_CLEAN_MSG = 1;
    private static final int HANDLER_SET_MSG = 2;
    private static final int MAX_READ_BUFFER = 1024;
    private static final int MAX_THREAD_COUNT = 10;
    private static final int MSG_CANCEL_TASK_BOOST = 148;
    private static final int MSG_CLEAN_TASK_BOOST = 149;
    private static final int MSG_SET_GAME_ENTER = 151;
    private static final int MSG_SET_GAME_LEVEL = 150;
    private static final int MSG_SET_TASK_BOOST = 147;
    private static final String STR_CPUSET_VIP = "/vip";
    private static final String TAG = "AwareGameFreq";
    private TimerCleanHandler mTimerCleanHandler = new TimerCleanHandler(this, null);
    private SparseArray<List<GameBoostInfo>> mVipPids = new SparseArray();

    private static class GameBoostInfo {
        public int level;
        public int pid;
        public int tid;

        public GameBoostInfo(int level, int pid, int tid) {
            this.level = level;
            this.pid = pid;
            this.tid = tid;
        }
    }

    private class TimerCleanHandler extends Handler {
        /* synthetic */ TimerCleanHandler(CPUGameFreq this$0, TimerCleanHandler -this1) {
            this();
        }

        private TimerCleanHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    CPUGameFreq.this.cleanTaskBoost();
                    break;
                case 2:
                case 3:
                    GameBoostInfo obj = msg.obj;
                    if (obj != null && ((obj instanceof GameBoostInfo) ^ 1) == 0) {
                        GameBoostInfo info = obj;
                        int pid = info.pid;
                        int tid = info.tid;
                        int level = info.level;
                        if (msg.what != 2) {
                            if (msg.what == 3) {
                                CPUGameFreq.this.cancelTaskBoost(level, pid, tid);
                                break;
                            }
                        } else if (CPUGameFreq.this.isThreadInProcess(pid, tid)) {
                            CPUGameFreq.this.setTaskBoost(level, pid, tid);
                            break;
                        } else {
                            AwareLog.d(CPUGameFreq.TAG, "setTaskBoost pid = " + pid + " have not tid = " + tid);
                            return;
                        }
                    }
                    return;
                    break;
                case 150:
                    if (msg.obj != null && ((msg.obj instanceof Bundle) ^ 1) == 0) {
                        IAwareMode.getInstance().gameLevel((Bundle) msg.obj);
                        break;
                    }
                    return;
                case 151:
                    if (msg.obj != null && ((msg.obj instanceof Bundle) ^ 1) == 0) {
                        IAwareMode.getInstance().gameEnter((Bundle) msg.obj);
                        break;
                    }
                    return;
            }
        }
    }

    private List<GameBoostInfo> getAllTidList() {
        List<GameBoostInfo> allInfos = new ArrayList();
        int count = this.mVipPids.size();
        for (int i = 0; i < count; i++) {
            List<GameBoostInfo> listInfo = (List) this.mVipPids.valueAt(i);
            if (!(listInfo == null || listInfo.size() == 0)) {
                allInfos.addAll(listInfo);
            }
        }
        return allInfos;
    }

    private void buildByteBuffer(ByteBuffer buffer, List<GameBoostInfo> listInfo) {
        if (buffer != null && listInfo != null) {
            int count = listInfo.size();
            for (int i = 0; i < count; i++) {
                GameBoostInfo info = (GameBoostInfo) listInfo.get(i);
                if (info != null) {
                    buffer.putInt(info.level);
                    buffer.putInt(info.pid);
                    buffer.putInt(info.tid);
                }
            }
        }
    }

    private void sendGameBoostMsg(int msg) {
        sendGameBoostMsg(msg, getAllTidList());
    }

    private void sendGameBoostMsg(int msg, List<GameBoostInfo> listInfo) {
        if (listInfo != null) {
            int count = listInfo.size();
            AwareLog.d(TAG, "sendGameBoostMsg msg = " + msg + " count = " + count);
            if (count > 0 && count <= 10) {
                ByteBuffer buffer = ByteBuffer.allocate(((count * 3) + 2) * 4);
                buffer.putInt(msg);
                buffer.putInt(count);
                buildByteBuffer(buffer, listInfo);
                IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position());
            }
        }
    }

    private String getProcCpuset(File fileCpuset) {
        String strCpuset = null;
        try {
            return FileUtils.readTextFile(fileCpuset, 1024, null);
        } catch (IOException e) {
            AwareLog.w(TAG, "getProcCpuset read cpuset failed!");
            return strCpuset;
        }
    }

    private GameBoostInfo getInfo(List<GameBoostInfo> listInfo, int tid) {
        if (listInfo == null) {
            return null;
        }
        int count = listInfo.size();
        for (int i = 0; i < count; i++) {
            GameBoostInfo info = (GameBoostInfo) listInfo.get(i);
            if (info != null && info.tid == tid) {
                return info;
            }
        }
        return null;
    }

    private void updateTaskLevel(int level, int pid, int tid) {
        List<GameBoostInfo> listInfo = (List) this.mVipPids.get(pid);
        if (listInfo != null) {
            GameBoostInfo info = getInfo(listInfo, tid);
            if (!(info == null || info.level == level)) {
                info.level = level;
            }
        }
    }

    private boolean isThreadInProcess(int pid, int tid) {
        try {
            if (Os.access("/proc/" + pid + "/task/" + tid, OsConstants.F_OK)) {
                return true;
            }
            return false;
        } catch (ErrnoException e) {
            return false;
        }
    }

    private void setTaskBoost(int level, int pid, int tid) {
        List<GameBoostInfo> listInfo = (List) this.mVipPids.get(pid);
        if (listInfo == null) {
            listInfo = new ArrayList();
            this.mVipPids.put(pid, listInfo);
        }
        GameBoostInfo info = getInfo(listInfo, tid);
        if (info == null) {
            listInfo.add(new GameBoostInfo(level, pid, tid));
        } else if (info.level != level) {
            info.level = level;
        } else {
            return;
        }
        sendGameBoostMsg(147);
        this.mTimerCleanHandler.removeMessages(1);
        this.mTimerCleanHandler.sendEmptyMessageDelayed(1, 5000);
    }

    private void removeTaskThread(int pid, int tid) {
        List<GameBoostInfo> listInfo = (List) this.mVipPids.get(pid);
        if (listInfo != null) {
            int count = listInfo.size();
            for (int i = 0; i < count; i++) {
                GameBoostInfo info = (GameBoostInfo) listInfo.get(i);
                if (info != null && info.tid == tid) {
                    listInfo.remove(i);
                    break;
                }
            }
            if (listInfo.size() == 0) {
                this.mVipPids.remove(pid);
            }
            if (this.mVipPids.size() == 0) {
                this.mTimerCleanHandler.removeMessages(1);
            }
        }
    }

    private void cancelTaskBoost(int level, int pid, int tid) {
        AwareLog.d(TAG, "cancelTaskBoost pid = " + pid + " tid = " + tid);
        updateTaskLevel(level, pid, tid);
        sendGameBoostMsg(148);
        removeTaskThread(pid, tid);
    }

    private void cleanTaskBoost() {
        boolean restult = false;
        int num = this.mVipPids.size();
        List<GameBoostInfo> moveList = new ArrayList();
        for (int i = 0; i < num; i++) {
            restult |= cleanTaskBoost((List) this.mVipPids.valueAt(i), moveList);
        }
        if (restult) {
            sendGameBoostMsg(MSG_CLEAN_TASK_BOOST, moveList);
            this.mTimerCleanHandler.removeMessages(1);
            this.mTimerCleanHandler.sendEmptyMessageDelayed(1, 5000);
        }
    }

    private boolean cleanTaskBoost(List<GameBoostInfo> infoList, List<GameBoostInfo> moveList) {
        if (infoList == null || moveList == null) {
            return false;
        }
        boolean res = false;
        for (int i = infoList.size() - 1; i >= 0; i--) {
            GameBoostInfo info = (GameBoostInfo) infoList.get(i);
            if (info != null) {
                boolean ret = checkThreadGroup(info.level, info.pid, info.tid);
                if (ret) {
                    moveList.add(info);
                }
                res |= ret;
            }
        }
        return res;
    }

    private boolean checkThreadGroup(int level, int pid, int tid) {
        File fileCpuset = new File("/proc/" + tid + "/cpuset");
        if (fileCpuset.exists()) {
            String strCpuset = getProcCpuset(fileCpuset);
            if (strCpuset == null) {
                AwareLog.d(TAG, "getProcCpuset cpuset null file content!");
                return false;
            } else if (strCpuset.contains(STR_CPUSET_VIP)) {
                return false;
            } else {
                return true;
            }
        }
        removeTaskThread(pid, tid);
        AwareLog.d(TAG, "getProcCpuset cpuset file not exists!");
        return false;
    }

    public int gameFreq(Bundle bundle) {
        if (bundle == null) {
            AwareLog.e(TAG, "empty bundle!");
            return -1;
        }
        int type = bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, -1);
        int ret = 0;
        Message msg;
        if (type == 0) {
            ret = gameFreqVersion0(bundle);
        } else if (type == 1) {
            msg = Message.obtain();
            msg.obj = bundle;
            msg.what = 151;
            this.mTimerCleanHandler.sendMessage(msg);
        } else if (type == 3) {
            msg = Message.obtain();
            msg.obj = bundle;
            msg.what = 150;
            this.mTimerCleanHandler.sendMessage(msg);
        }
        return ret;
    }

    private int gameFreqVersion0(Bundle bundle) {
        int pid = bundle.getInt("pid", 0);
        int tid = bundle.getInt("tid", 0);
        int level = bundle.getInt(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, 0);
        if (pid <= 0 || tid <= 0) {
            AwareLog.e(TAG, "invalid pid or renderTid, pid = " + pid + " renderTid = " + tid);
            return -1;
        }
        Message msg = this.mTimerCleanHandler.obtainMessage();
        msg.obj = new GameBoostInfo(level, pid, tid);
        AwareLog.i(TAG, "gameFreq level = " + level + " pid = " + pid + " tid = " + tid);
        int ret = 0;
        switch (level) {
            case 0:
                msg.what = 3;
                this.mTimerCleanHandler.sendMessage(msg);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                msg.what = 2;
                this.mTimerCleanHandler.sendMessage(msg);
                break;
            default:
                ret = -1;
                AwareLog.w(TAG, "GameFreq unknown level = " + level);
                break;
        }
        return ret;
    }
}
