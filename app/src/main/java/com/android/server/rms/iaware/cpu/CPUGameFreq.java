package com.android.server.rms.iaware.cpu;

import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.SparseArray;
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
    private static final int MSG_CANCEL_TASK_BOOST = 146;
    private static final int MSG_CLEAN_TASK_BOOST = 147;
    private static final int MSG_SET_TASK_BOOST = 145;
    private static final String STR_CPUSET_VIP = "/vip";
    private static final String TAG = "AwareGameFreq";
    private TimerCleanHandler mTimerCleanHandler;
    private SparseArray<List<GameBoostInfo>> mVipPids;

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
        private TimerCleanHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CPUGameFreq.HANDLER_CLEAN_MSG /*1*/:
                    CPUGameFreq.this.cleanTaskBoost();
                    break;
                case CPUGameFreq.HANDLER_SET_MSG /*2*/:
                case CPUGameFreq.HANDLER_CANCEL_MSG /*3*/:
                    GameBoostInfo obj = msg.obj;
                    if (obj != null && (obj instanceof GameBoostInfo)) {
                        GameBoostInfo info = obj;
                        int pid = info.pid;
                        int tid = info.tid;
                        int level = info.level;
                        if (msg.what != CPUGameFreq.HANDLER_SET_MSG) {
                            if (msg.what == CPUGameFreq.HANDLER_CANCEL_MSG) {
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
            }
        }
    }

    public CPUGameFreq() {
        this.mVipPids = new SparseArray();
        this.mTimerCleanHandler = new TimerCleanHandler();
    }

    private void sendGameBoostMsg(int msg, int level, int pid, int tid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putInt(msg);
        buffer.putInt(level);
        buffer.putInt(pid);
        buffer.putInt(tid);
        IAwaredConnection.getInstance().sendPacket(buffer.array(), AWARE_GAME_LEVEL_0, buffer.position());
    }

    private int getAllTidCount() {
        int tidCounts = AWARE_GAME_LEVEL_0;
        int count = this.mVipPids.size();
        for (int i = AWARE_GAME_LEVEL_0; i < count; i += HANDLER_CLEAN_MSG) {
            List<GameBoostInfo> listInfo = (List) this.mVipPids.valueAt(i);
            if (listInfo != null) {
                tidCounts += listInfo.size();
            }
        }
        return tidCounts;
    }

    private void buildByteBuffer(ByteBuffer buffer, List<GameBoostInfo> listInfo) {
        if (buffer != null && listInfo != null) {
            int count = listInfo.size();
            for (int i = AWARE_GAME_LEVEL_0; i < count; i += HANDLER_CLEAN_MSG) {
                GameBoostInfo info = (GameBoostInfo) listInfo.get(i);
                if (info != null) {
                    buffer.putInt(info.level);
                    buffer.putInt(info.pid);
                    buffer.putInt(info.tid);
                }
            }
        }
    }

    private void sendGameCleanMsg(int msg) {
        int count = getAllTidCount();
        if (count != 0 && count <= MAX_THREAD_COUNT) {
            ByteBuffer buffer = ByteBuffer.allocate(((count * HANDLER_CANCEL_MSG) + HANDLER_SET_MSG) * AWARE_GAME_LEVEL_4);
            buffer.putInt(msg);
            buffer.putInt(count);
            int pidCount = this.mVipPids.size();
            for (int i = AWARE_GAME_LEVEL_0; i < pidCount; i += HANDLER_CLEAN_MSG) {
                buildByteBuffer(buffer, (List) this.mVipPids.valueAt(i));
            }
            IAwaredConnection.getInstance().sendPacket(buffer.array(), AWARE_GAME_LEVEL_0, buffer.position());
        }
    }

    private String getProcCpuset(File fileCpuset) {
        String strCpuset = null;
        try {
            strCpuset = FileUtils.readTextFile(fileCpuset, MAX_READ_BUFFER, null);
        } catch (IOException e) {
            AwareLog.w(TAG, "getProcCpuset read cpuset failed!");
        }
        return strCpuset;
    }

    private GameBoostInfo getInfo(List<GameBoostInfo> listInfo, int tid) {
        if (listInfo == null) {
            return null;
        }
        int count = listInfo.size();
        for (int i = AWARE_GAME_LEVEL_0; i < count; i += HANDLER_CLEAN_MSG) {
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
        sendGameBoostMsg(MSG_SET_TASK_BOOST, level, pid, tid);
        this.mTimerCleanHandler.removeMessages(HANDLER_CLEAN_MSG);
        this.mTimerCleanHandler.sendEmptyMessageDelayed(HANDLER_CLEAN_MSG, 5000);
    }

    private void removeTaskThread(int pid, int tid) {
        List<GameBoostInfo> listInfo = (List) this.mVipPids.get(pid);
        if (listInfo != null) {
            int count = listInfo.size();
            for (int i = AWARE_GAME_LEVEL_0; i < count; i += HANDLER_CLEAN_MSG) {
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
                this.mTimerCleanHandler.removeMessages(HANDLER_CLEAN_MSG);
            }
        }
    }

    private void cancelTaskBoost(int level, int pid, int tid) {
        updateTaskLevel(level, pid, tid);
        sendGameCleanMsg(MSG_CANCEL_TASK_BOOST);
        removeTaskThread(pid, tid);
    }

    private void cleanTaskBoost() {
        boolean restult = false;
        int num = this.mVipPids.size();
        for (int i = AWARE_GAME_LEVEL_0; i < num; i += HANDLER_CLEAN_MSG) {
            restult |= cleanTaskBoost((List) this.mVipPids.valueAt(i));
        }
        if (restult) {
            sendGameCleanMsg(MSG_CLEAN_TASK_BOOST);
            this.mTimerCleanHandler.removeMessages(HANDLER_CLEAN_MSG);
            this.mTimerCleanHandler.sendEmptyMessageDelayed(HANDLER_CLEAN_MSG, 5000);
        }
    }

    private boolean cleanTaskBoost(List<GameBoostInfo> infoList) {
        if (infoList == null) {
            return false;
        }
        boolean res = false;
        for (int i = AWARE_GAME_LEVEL_0; i < infoList.size(); i += HANDLER_CLEAN_MSG) {
            GameBoostInfo info = (GameBoostInfo) infoList.get(i);
            if (info != null) {
                res |= cleanTaskBoost(info.level, info.pid, info.tid);
            }
        }
        return res;
    }

    private boolean cleanTaskBoost(int level, int pid, int tid) {
        File fileCpuset = new File("/proc/" + tid + "/cpuset");
        if (fileCpuset.exists()) {
            String strCpuset = getProcCpuset(fileCpuset);
            if (strCpuset == null) {
                AwareLog.d(TAG, "getProcCpuset cpuset null file content!");
                return false;
            }
            if (!strCpuset.contains(STR_CPUSET_VIP)) {
                sendGameBoostMsg(MSG_SET_TASK_BOOST, level, pid, tid);
            }
            return true;
        }
        removeTaskThread(pid, tid);
        AwareLog.d(TAG, "getProcCpuset cpuset file not exists!");
        return false;
    }

    public int gameFreq(int level, int pid, int tid) {
        if (pid <= 0 || tid <= 0) {
            AwareLog.e(TAG, "invalid pid or renderTid, pid = " + pid + " renderTid = " + tid);
            return -1;
        }
        Message msg = this.mTimerCleanHandler.obtainMessage();
        msg.obj = new GameBoostInfo(level, pid, tid);
        int ret = AWARE_GAME_LEVEL_0;
        switch (level) {
            case AWARE_GAME_LEVEL_0 /*0*/:
                msg.what = HANDLER_CANCEL_MSG;
                this.mTimerCleanHandler.sendMessage(msg);
                break;
            case HANDLER_CLEAN_MSG /*1*/:
            case HANDLER_SET_MSG /*2*/:
            case HANDLER_CANCEL_MSG /*3*/:
            case AWARE_GAME_LEVEL_4 /*4*/:
                msg.what = HANDLER_SET_MSG;
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
