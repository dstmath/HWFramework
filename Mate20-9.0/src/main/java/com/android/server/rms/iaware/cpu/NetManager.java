package com.android.server.rms.iaware.cpu;

import android.app.ActivityManager;
import android.net.wifi.HwInnerWifiManagerImpl;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NetManager {
    private static final int BASE_VISIBLE_EVENT_ID = 0;
    public static final int DELAY_TIME = 1000;
    private static final int INT_SIZE = 4;
    public static final int INVALID_MODE = -1;
    public static final int INVALID_PID = -1;
    private static final int INVALID_UID = -1;
    public static final int INVALID_WINDOWTYPE = -1;
    private static final String MODE_STR = "mode";
    public static final int MSG_APP_AUDIO = 4;
    public static final int MSG_APP_BACKGROUND = 1;
    public static final int MSG_APP_DIED = 2;
    public static final int MSG_APP_DOWNLOAD = 3;
    public static final int MSG_APP_FOREGROUND = 0;
    private static final int MSG_APP_TOAST = 11;
    private static final int MSG_APP_VISIBLE = 5;
    private static final int MSG_BGMNG_BASE_VALUE = 900;
    private static final int MSG_NET_BASE = 0;
    public static final int MSG_NET_GAME_ENABLE = 12;
    private static final int MSG_SET_LIMIT_MODE = 901;
    private static final String PATH_UID_INFO = "/acct/uid_";
    private static final String PID_STR = "pid";
    private static final String TAG = "NetManager";
    public static final int TOAST_WINDOW_ADD = 5;
    public static final int TOAST_WINDOW_CLR = 3;
    public static final int TOAST_WINDOW_DEL = 4;
    private static final int TOAST_WIN_TYPE = 2;
    private static final String TYPE_STR = "type";
    public static final int VISIBLE_WINDOW_CLR = 0;
    public static final int VISIBLE_WINDOW_DEL = 1;
    public static final int VISIBLE_WINDOW_UPDATE = 2;
    private static final int VISIBLE_WIN_TYPE = 1;
    private static NetManager sInstance;
    private static final Object sLock = new Object();
    private ArraySet<Integer> mAudioUidSet = new ArraySet<>();
    private AwareToastCallback mAwareToastCallback;
    private AwareVisibleCallback mAwareVisibleCallback;
    private SparseArray<List<Integer>> mBgUidPidArray = new SparseArray<>();
    private ArraySet<Integer> mBgUidSet = new ArraySet<>();
    private CPUFeature mCPUFeatureInstance = null;
    private ArraySet<Integer> mDownloadUidSet = new ArraySet<>();
    /* access modifiers changed from: private */
    public boolean mEnable = false;
    private SparseArray<List<Integer>> mFgUidPidArray = new SparseArray<>();
    private ArraySet<Integer> mFgUidSet = new ArraySet<>();
    private HwInnerWifiManagerImpl mHwInnerWifiManagerImpl;
    public NetManagerHandler mNetManagerHandler = new NetManagerHandler();
    private boolean mUidCanset = false;
    private SparseIntArray mVisiblePidArray = new SparseIntArray();

    private class AwareToastCallback implements AwareIntelligentRecg.IAwareToastCallback {
        private AwareToastCallback() {
        }

        public void onToastWindowsChanged(int type, int pid) {
            if (NetManager.this.mEnable) {
                Message msg = NetManager.this.mNetManagerHandler.obtainMessage();
                msg.what = 11;
                msg.arg1 = type;
                msg.arg2 = pid;
                NetManager.this.mNetManagerHandler.sendMessage(msg);
            }
        }
    }

    private class AwareVisibleCallback implements AwareAppAssociate.IAwareVisibleCallback {
        private AwareVisibleCallback() {
        }

        public void onVisibleWindowsChanged(int type, int pid, int mode) {
            if (NetManager.this.mEnable) {
                Message msg = NetManager.this.mNetManagerHandler.obtainMessage();
                msg.what = 5;
                Bundle data = new Bundle();
                data.putInt("type", type);
                data.putInt("pid", pid);
                data.putInt("mode", mode);
                msg.setData(data);
                NetManager.this.mNetManagerHandler.sendMessage(msg);
            }
        }
    }

    public class NetManagerHandler extends Handler {
        public NetManagerHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            switch (i) {
                case 0:
                    NetManager.this.onProcessAppForeground(msg.arg1, msg.arg2, true);
                    break;
                case 1:
                    NetManager.this.onProcessAppForeground(msg.arg1, msg.arg2, false);
                    break;
                case 2:
                    NetManager.this.onProcessDied(msg.arg1, msg.arg2);
                    break;
                case 3:
                    NetManager.this.appendDownloadAppUid(msg.arg1, msg.arg2);
                    break;
                case 4:
                    NetManager.this.appendAudioAppUid(msg.arg1, msg.arg2);
                    break;
                case 5:
                    Bundle visData = msg.getData();
                    NetManager.this.appendVisiblePid(visData.getInt("type"), visData.getInt("pid"), visData.getInt("mode"), 1);
                    break;
                default:
                    switch (i) {
                        case 11:
                            NetManager.this.appendVisiblePid(msg.arg1, msg.arg2, -1, 2);
                            break;
                        case 12:
                            if (NetManager.this.mEnable) {
                                int enable = msg.arg1;
                                int mode = msg.arg2;
                                List<ActivityManager.RunningAppProcessInfo> appProcesses = (List) msg.obj;
                                if (enable != 0) {
                                    NetManager.this.start(enable, mode, appProcesses);
                                    break;
                                } else {
                                    NetManager.this.stop();
                                    break;
                                }
                            } else {
                                return;
                            }
                        default:
                            AwareLog.e(NetManager.TAG, "error msg what = " + msg.what);
                            break;
                    }
            }
        }
    }

    private NetManager() {
    }

    public static NetManager getInstance() {
        NetManager netManager;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new NetManager();
            }
            netManager = sInstance;
        }
        return netManager;
    }

    public void enable(CPUFeature feature) {
        this.mEnable = true;
        this.mCPUFeatureInstance = feature;
        registerVisibleCallback();
        registerToastCallback();
    }

    public void disable() {
        unregisterVisibleCallback();
        unregisterToastCallback();
        this.mEnable = false;
        removeAllMsg();
        this.mBgUidSet.clear();
        this.mBgUidPidArray.clear();
        this.mFgUidSet.clear();
        this.mFgUidPidArray.clear();
        this.mDownloadUidSet.clear();
        this.mVisiblePidArray.clear();
        this.mAudioUidSet.clear();
    }

    /* access modifiers changed from: private */
    public void start(int enable, int mode, List<ActivityManager.RunningAppProcessInfo> appProcesses) {
        this.mUidCanset = true;
        AwareLog.d(TAG, "NetManager start!");
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (!(appProcess.processName == null || appProcess.importance == 100)) {
                AwareLog.d(TAG, "NetManager bg uid = " + appProcess.uid + ", bg app = " + appProcess.processName);
                onProcessAppForeground(appProcess.pid, appProcess.uid, false);
            }
        }
        sendLimitMode(mode, 901);
        sendUids(this.mFgUidSet, 141);
        sendUids(this.mBgUidSet, CPUFeature.MSG_SET_BG_UIDS);
    }

    /* access modifiers changed from: private */
    public void stop() {
        AwareLog.d(TAG, "NetManager stop!");
        ArraySet<Integer> resetUids = new ArraySet<>();
        resetUids.add(-1);
        sendLimitMode(0, 901);
        sendUids(resetUids, 141);
        sendUids(resetUids, CPUFeature.MSG_SET_BG_UIDS);
        this.mUidCanset = false;
    }

    private void registerVisibleCallback() {
        if (this.mAwareVisibleCallback == null) {
            this.mAwareVisibleCallback = new AwareVisibleCallback();
        }
        AwareAppAssociate.getInstance().registerVisibleCallback(this.mAwareVisibleCallback);
    }

    private void unregisterVisibleCallback() {
        if (this.mAwareVisibleCallback != null) {
            AwareAppAssociate.getInstance().unregisterVisibleCallback(this.mAwareVisibleCallback);
            this.mAwareVisibleCallback = null;
        }
    }

    private void registerToastCallback() {
        if (this.mAwareToastCallback == null) {
            this.mAwareToastCallback = new AwareToastCallback();
        }
        AwareIntelligentRecg.getInstance().registerToastCallback(this.mAwareToastCallback);
    }

    private void unregisterToastCallback() {
        if (this.mAwareToastCallback != null) {
            AwareIntelligentRecg.getInstance().unregisterToastCallback(this.mAwareToastCallback);
            this.mAwareToastCallback = null;
        }
    }

    private boolean isAppUid(int userUid) {
        if (UserHandle.getAppId(userUid) >= 10000) {
            return true;
        }
        return false;
    }

    private boolean addFgPidUidMap(int pid, int uid) {
        List<Integer> listPid = this.mFgUidPidArray.get(uid);
        if (listPid == null) {
            List<Integer> pidList = new ArrayList<>();
            pidList.add(Integer.valueOf(pid));
            this.mFgUidPidArray.put(uid, pidList);
            this.mFgUidSet.add(Integer.valueOf(uid));
            return true;
        }
        if (!this.mFgUidSet.contains(Integer.valueOf(uid))) {
            AwareLog.e(TAG, "addFgPidUidMap and uidset dont have same uid key");
        }
        if (listPid.contains(Integer.valueOf(pid))) {
            return false;
        }
        listPid.add(Integer.valueOf(pid));
        this.mFgUidPidArray.put(uid, listPid);
        return false;
    }

    private boolean addBgPidUidMap(int pid, int uid) {
        List<Integer> listPid = this.mBgUidPidArray.get(uid);
        if (listPid == null) {
            List<Integer> pidList = new ArrayList<>();
            pidList.add(Integer.valueOf(pid));
            this.mBgUidPidArray.put(uid, pidList);
            return true;
        } else if (listPid.contains(Integer.valueOf(pid))) {
            return false;
        } else {
            listPid.add(Integer.valueOf(pid));
            this.mBgUidPidArray.put(uid, listPid);
            return false;
        }
    }

    private boolean removeFgPidUidMap(int pid, int uid) {
        boolean resultUidSet = false;
        List<Integer> listPid = this.mFgUidPidArray.get(uid);
        if ((pid == 0 || pid == -1) && listPid != null) {
            this.mFgUidPidArray.remove(uid);
            this.mFgUidSet.remove(Integer.valueOf(uid));
            resultUidSet = true;
        }
        if (listPid == null) {
            return resultUidSet;
        }
        if (listPid.contains(Integer.valueOf(pid))) {
            listPid.remove(Integer.valueOf(pid));
        }
        if (listPid.size() != 0) {
            return resultUidSet;
        }
        if (!this.mFgUidSet.contains(Integer.valueOf(uid))) {
            AwareLog.e(TAG, "removeFgPidUidMap and uidset dont have same uid key");
        }
        this.mFgUidSet.remove(Integer.valueOf(uid));
        this.mFgUidPidArray.remove(uid);
        return true;
    }

    private boolean removeBgPidUidMap(int pid, int uid) {
        boolean resultUidSet = false;
        List<Integer> listPid = this.mBgUidPidArray.get(uid);
        if ((pid == 0 || pid == -1) && listPid != null) {
            this.mBgUidPidArray.remove(uid);
            resultUidSet = true;
        }
        if (listPid == null) {
            return resultUidSet;
        }
        if (listPid.contains(Integer.valueOf(pid))) {
            listPid.remove(Integer.valueOf(pid));
        }
        if (listPid.size() != 0) {
            return resultUidSet;
        }
        this.mBgUidPidArray.remove(uid);
        return true;
    }

    /* access modifiers changed from: private */
    public void onProcessAppForeground(int pid, int uid, boolean isFg) {
        if (isAppUid(uid)) {
            if (isFg) {
                removeBgUid(uid);
                removeBgPidUidMap(pid, uid);
                if (addFgPidUidMap(pid, uid)) {
                    sendUids(this.mFgUidSet, 141);
                }
            } else {
                if (this.mHwInnerWifiManagerImpl == null) {
                    this.mHwInnerWifiManagerImpl = HwInnerWifiManagerImpl.getDefault();
                }
                if (this.mHwInnerWifiManagerImpl.isBgLimitAllowed(uid)) {
                    if (removeFgPidUidMap(pid, uid)) {
                        sendUids(this.mFgUidSet, 141);
                    }
                    addBgUid(uid);
                    addBgPidUidMap(pid, uid);
                } else {
                    removeBgUid(uid);
                    removeBgPidUidMap(pid, uid);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onProcessDied(int pid, int uid) {
        if (isAppUid(uid)) {
            if (removeFgPidUidMap(pid, uid)) {
                sendUids(this.mFgUidSet, 141);
            }
            removeBgPidUidMap(pid, uid);
            if (this.mBgUidPidArray.get(uid) == null && this.mBgUidSet.contains(Integer.valueOf(uid))) {
                this.mBgUidSet.remove(Integer.valueOf(uid));
                sendUids(this.mBgUidSet, CPUFeature.MSG_SET_BG_UIDS);
            }
            if (deleteVisiblePid(pid, -1)) {
                onVisiblePidsDeleted();
            }
            ArraySet<Integer> pidSet = new ArraySet<>();
            getPidsByUid(uid, pidSet);
            if (pidSet.isEmpty()) {
                this.mAudioUidSet.remove(Integer.valueOf(uid));
                this.mDownloadUidSet.remove(Integer.valueOf(uid));
            }
        }
    }

    /* access modifiers changed from: private */
    public void appendDownloadAppUid(int uid, int eventType) {
        if (isAppUid(uid)) {
            if (eventType == 1) {
                AwareLog.d(TAG, "uid:" + uid + " enters download");
                this.mDownloadUidSet.add(Integer.valueOf(uid));
                addBgUid(uid);
            } else if (eventType == 2) {
                AwareLog.d(TAG, "uid:" + uid + " exits download");
                this.mDownloadUidSet.remove(Integer.valueOf(uid));
                removeBgUid(uid);
            }
        }
    }

    /* access modifiers changed from: private */
    public void appendAudioAppUid(int uid, int eventType) {
        if (isAppUid(uid)) {
            if (eventType == 1) {
                AwareLog.d(TAG, "uid:" + uid + " enters audio out");
                this.mAudioUidSet.add(Integer.valueOf(uid));
                removeBgUid(uid);
            } else if (eventType == 2 && this.mAudioUidSet.contains(Integer.valueOf(uid))) {
                AwareLog.d(TAG, "uid:" + uid + " exits audio out");
                this.mAudioUidSet.remove(Integer.valueOf(uid));
                addBgUid(uid);
            }
        }
    }

    /* access modifiers changed from: private */
    public void appendVisiblePid(int msg, int pid, int mode, int winType) {
        if (msg == 0 || msg == 1 || msg == 3 || msg == 4) {
            if (deleteVisiblePid(pid, winType)) {
                AwareLog.d(TAG, "appendVisiblePid delete pid:" + pid + " from visible window");
                onVisiblePidsDeleted();
            }
        } else if ((msg == 2 || msg == 5) && addVisiblePid(pid, mode, winType)) {
            AwareLog.d(TAG, "appendVisiblePid add pid:" + pid + " to visible window");
            onVisiblePidsAdded();
        }
    }

    private boolean addVisiblePid(int pid, int mode, int winType) {
        int value;
        int value2 = this.mVisiblePidArray.get(pid, -1);
        if (value2 != -1) {
            if (value2 != 1 || isVisibleAllowed(mode)) {
                value = addType(value2, winType);
            } else {
                value = clearType(value2, winType);
            }
            if (value == 0) {
                AwareLog.d(TAG, "appendVisiblePid delete pid:" + pid + " from visible window");
                this.mVisiblePidArray.delete(pid);
                onVisiblePidsDeleted();
                return false;
            }
            this.mVisiblePidArray.put(pid, value);
            return false;
        } else if (winType != 1) {
            this.mVisiblePidArray.put(pid, winType);
            return true;
        } else if (!isVisibleAllowed(mode)) {
            return false;
        } else {
            this.mVisiblePidArray.put(pid, winType);
            return true;
        }
    }

    private boolean isVisibleAllowed(int mode) {
        return mode == 0 || mode == 3;
    }

    private int clearType(int value, int winType) {
        return (~winType) & value;
    }

    private int addType(int value, int winType) {
        return value | winType;
    }

    private boolean deleteVisiblePid(int pid, int winType) {
        boolean isDeleted = false;
        if (winType == -1 && this.mVisiblePidArray.get(pid, -1) != -1) {
            this.mVisiblePidArray.delete(pid);
            isDeleted = true;
        }
        SparseIntArray tempArray = new SparseIntArray();
        int size = this.mVisiblePidArray.size();
        for (int i = 0; i < size; i++) {
            int visPid = this.mVisiblePidArray.keyAt(i);
            int value = this.mVisiblePidArray.valueAt(i);
            if (pid == -1 || pid == visPid) {
                int value2 = clearType(value, winType);
                if (value2 == 0) {
                    isDeleted = true;
                } else {
                    tempArray.put(this.mVisiblePidArray.keyAt(i), value2);
                }
            } else {
                tempArray.put(this.mVisiblePidArray.keyAt(i), value);
            }
        }
        this.mVisiblePidArray = tempArray;
        return isDeleted;
    }

    private void onVisiblePidsDeleted() {
        int dlNum = this.mDownloadUidSet.size();
        for (int i = 0; i < dlNum; i++) {
            addBgUid(this.mDownloadUidSet.valueAt(i).intValue());
        }
    }

    private void onVisiblePidsAdded() {
        int bgNum = this.mBgUidSet.size();
        for (int i = 0; i < bgNum; i++) {
            int uid = this.mBgUidSet.valueAt(i).intValue();
            if (isVisible(uid)) {
                removeBgUid(uid);
                return;
            }
        }
    }

    private void addBgUid(int uid) {
        if (!this.mBgUidSet.contains(Integer.valueOf(uid)) && !this.mFgUidSet.contains(Integer.valueOf(uid)) && !this.mAudioUidSet.contains(Integer.valueOf(uid)) && this.mDownloadUidSet.contains(Integer.valueOf(uid)) && !isVisible(uid)) {
            this.mBgUidSet.add(Integer.valueOf(uid));
            sendUids(this.mBgUidSet, CPUFeature.MSG_SET_BG_UIDS);
        }
    }

    private void removeBgUid(int uid) {
        if (this.mBgUidSet.contains(Integer.valueOf(uid))) {
            this.mBgUidSet.remove(Integer.valueOf(uid));
            sendUids(this.mBgUidSet, CPUFeature.MSG_SET_BG_UIDS);
        }
    }

    private boolean isVisible(int uid) {
        ArraySet<Integer> pidSet = new ArraySet<>();
        getPidsByUid(uid, pidSet);
        if (pidSet.size() < 1) {
            AwareLog.e(TAG, "getPidsByUid is null!");
            return true;
        }
        int size = this.mVisiblePidArray.size();
        for (int i = 0; i < size; i++) {
            if (pidSet.contains(Integer.valueOf(this.mVisiblePidArray.keyAt(i)))) {
                return true;
            }
        }
        return false;
    }

    private void getPidsByUid(int uid, Set<Integer> pidSet) {
        File[] files = new File(PATH_UID_INFO + uid).listFiles();
        if (files == null) {
            AwareLog.e(TAG, "files null ");
            return;
        }
        for (File dir : files) {
            if (dir.isDirectory() && -1 != dir.getName().indexOf("pid_")) {
                String[] a = dir.getName().split("_");
                if (a.length > 1) {
                    try {
                        pidSet.add(Integer.valueOf(Integer.parseInt(a[1])));
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                    }
                }
            }
        }
    }

    private void sendUids(ArraySet<Integer> uids, int msg) {
        ByteBuffer buffer;
        if (this.mUidCanset) {
            int size = uids.size();
            if (size == 0) {
                buffer = ByteBuffer.allocate(12);
                buffer.putInt(msg);
                buffer.putInt(1);
                buffer.putInt(-1);
            } else {
                buffer = ByteBuffer.allocate((size + 2) * 4);
                buffer.putInt(msg);
                buffer.putInt(size);
                for (int i = 0; i < size; i++) {
                    buffer.putInt(uids.valueAt(i).intValue());
                }
            }
            if (this.mCPUFeatureInstance != null) {
                this.mCPUFeatureInstance.sendPacket(buffer);
            }
        }
    }

    private void sendLimitMode(int mode, int msg) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(msg);
        buffer.putInt(mode);
        if (this.mCPUFeatureInstance != null) {
            int ret = this.mCPUFeatureInstance.sendPacket(buffer);
        }
        AwareLog.e(TAG, "sendLimitMode mode=  " + mode);
    }

    /* access modifiers changed from: package-private */
    public void sendMsgToNetMng(int arg1, int arg2, int msgCode) {
        if (this.mEnable) {
            Message msg = this.mNetManagerHandler.obtainMessage();
            msg.what = msgCode;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            this.mNetManagerHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendMsgToNetMng(int arg1, int arg2, int msgCode, int delay) {
        if (this.mEnable) {
            Message msg = this.mNetManagerHandler.obtainMessage();
            msg.what = msgCode;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            this.mNetManagerHandler.sendMessageDelayed(msg, (long) delay);
        }
    }

    private void removeAllMsg() {
        this.mNetManagerHandler.removeMessages(4);
        this.mNetManagerHandler.removeMessages(1);
        this.mNetManagerHandler.removeMessages(2);
        this.mNetManagerHandler.removeMessages(3);
        this.mNetManagerHandler.removeMessages(0);
        this.mNetManagerHandler.removeMessages(5);
        this.mNetManagerHandler.removeMessages(11);
    }
}
