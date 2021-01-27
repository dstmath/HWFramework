package com.android.server.rms.iaware.appmng;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.io.PrintWriter;
import java.util.Map;

public class AwareAudioFocusManager {
    private static final long DECAY_CLEAR_AUDIO_CACHE = 5000;
    private static final long DECAY_STATE_CHANGE_TIME = 3000;
    private static final Object LOCK = new Object();
    private static final int MSG_CLEAR_AUDIO_CACHE = 2;
    private static final int MSG_FOCUS_REQUEST_CHANGE = 1;
    private static final int MSG_FOREGROUND_CHANGE = 0;
    private static final String TAG = "AwareAudioFocusManager";
    private static boolean sDebug = false;
    private static AwareAudioFocusManager sInstance;
    private final ArrayMap<Integer, AudioFocusInfo> mAudioFocusInfoStack = new ArrayMap<>();
    private final ArraySet<Integer> mForegroundUids = new ArraySet<>();
    private AudioFocusObserver mObserver;
    private StateChangeHandler mStateChangeHandler;

    public interface AudioFocusObserver {
        void onFocusPermanentLoss(int i);

        void onFocusRelease(int i);
    }

    private AwareAudioFocusManager() {
        init();
    }

    public static AwareAudioFocusManager getInstance() {
        AwareAudioFocusManager awareAudioFocusManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareAudioFocusManager();
            }
            awareAudioFocusManager = sInstance;
        }
        return awareAudioFocusManager;
    }

    public static void enableDebug() {
        sDebug = true;
    }

    private void init() {
        initHandler();
        synchronized (this.mAudioFocusInfoStack) {
            this.mAudioFocusInfoStack.clear();
        }
        synchronized (this.mForegroundUids) {
            this.mForegroundUids.clear();
        }
    }

    public void registerFocusChangeObserver(AudioFocusObserver observer) {
        this.mObserver = observer;
    }

    public void unregisterFocusChangeObserver() {
        this.mObserver = null;
    }

    public void reportAudioFocusLoss(int stateType, int uid, String focusName) {
        if (sDebug) {
            AwareLog.i(TAG, "Aware report audio focus loss event:" + stateType + ", loss focus app uid:" + uid + ", loss focus name:" + focusName);
        }
        if (stateType > 0 || focusName == null) {
            AwareLog.i(TAG, "Aware report audio focus loss error");
            return;
        }
        synchronized (this.mAudioFocusInfoStack) {
            AudioFocusInfo info = this.mAudioFocusInfoStack.get(Integer.valueOf(uid));
            if (info == null) {
                AwareLog.i(TAG, "Aware uid:" + uid + " audio focus not exit");
                return;
            }
            info.setFocusLossReceived(stateType, focusName);
            if (this.mObserver != null) {
                if (info.isPermanentLoss()) {
                    this.mObserver.onFocusPermanentLoss(uid);
                }
                if (!info.isTransientLoss()) {
                    AwareLog.i(TAG, "Aware uid:" + uid + " again audio focus or release audio focus");
                    Message message = this.mStateChangeHandler.obtainMessage(2);
                    message.arg2 = uid;
                    this.mStateChangeHandler.sendMessageDelayed(message, DECAY_CLEAR_AUDIO_CACHE);
                }
            }
        }
    }

    public void reportAudioFocusRequest(int stateType, int uid, String focusName) {
        if (sDebug) {
            AwareLog.i(TAG, "Aware report request audio focus event:" + stateType + ", request focus app uid:" + uid + ", request focus name:" + focusName);
        }
        if (stateType < 0 || focusName == null) {
            AwareLog.i(TAG, "Aware report audio focus request error");
            return;
        }
        synchronized (this.mAudioFocusInfoStack) {
            AudioFocusInfo info = this.mAudioFocusInfoStack.get(Integer.valueOf(uid));
            if (info == null) {
                if (stateType != 0) {
                    this.mAudioFocusInfoStack.put(Integer.valueOf(uid), new AudioFocusInfo(uid, stateType, focusName));
                }
                return;
            }
            if (stateType != 0) {
                this.mStateChangeHandler.removeMessages(1, focusName);
                info.setFocusGainRequest(stateType, focusName);
            } else {
                updateAudioCache(info, focusName);
                if (info.isExistFocusRequest(stateType, focusName)) {
                    Message message = this.mStateChangeHandler.obtainMessage(1, focusName);
                    message.arg2 = uid;
                    this.mStateChangeHandler.sendMessageDelayed(message, DECAY_STATE_CHANGE_TIME);
                }
            }
        }
    }

    public void reportForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        if (sDebug) {
            AwareLog.i(TAG, "Aware report activity foreground event:" + foregroundActivities + ", pid:" + pid + ", uid:" + uid);
        }
        synchronized (this.mForegroundUids) {
            int index = this.mForegroundUids.indexOf(Integer.valueOf(uid));
            if (index >= 0 && index < this.mForegroundUids.size()) {
                Integer uidInteger = this.mForegroundUids.valueAt(index);
                if (foregroundActivities) {
                    this.mStateChangeHandler.removeMessages(0, uidInteger);
                    AwareLog.i(TAG, "Aware uid:" + uid + " again enter foreground");
                } else {
                    Message message = this.mStateChangeHandler.obtainMessage(0, uidInteger);
                    message.arg2 = uid;
                    this.mStateChangeHandler.sendMessageDelayed(message, DECAY_STATE_CHANGE_TIME);
                }
            }
            if (foregroundActivities) {
                this.mForegroundUids.add(Integer.valueOf(uid));
            }
        }
    }

    public boolean isAppForeground(int uid) {
        boolean contains;
        synchronized (this.mForegroundUids) {
            contains = this.mForegroundUids.contains(Integer.valueOf(uid));
        }
        return contains;
    }

    public boolean isAppExistFocus(int uid) {
        boolean containsKey;
        synchronized (this.mAudioFocusInfoStack) {
            containsKey = this.mAudioFocusInfoStack.containsKey(Integer.valueOf(uid));
        }
        return containsKey;
    }

    public boolean isTransientPlay(int uid) {
        synchronized (this.mAudioFocusInfoStack) {
            AudioFocusInfo info = this.mAudioFocusInfoStack.get(Integer.valueOf(uid));
            if (info == null) {
                return false;
            }
            return info.isTransientPlay();
        }
    }

    public boolean isTransientLoss(int uid) {
        synchronized (this.mAudioFocusInfoStack) {
            AudioFocusInfo info = this.mAudioFocusInfoStack.get(Integer.valueOf(uid));
            if (info == null) {
                return false;
            }
            return info.isTransientLoss();
        }
    }

    public boolean isPermanentLoss(int uid) {
        synchronized (this.mAudioFocusInfoStack) {
            AudioFocusInfo info = this.mAudioFocusInfoStack.get(Integer.valueOf(uid));
            if (info == null) {
                return false;
            }
            return info.isPermanentLoss();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateForeground(int uid) {
        synchronized (this.mForegroundUids) {
            this.mForegroundUids.remove(Integer.valueOf(uid));
        }
    }

    private void updateAudioCache(AudioFocusInfo info, String focusName) {
        if (this.mObserver != null && info.isExistTransientLoss(focusName)) {
            this.mObserver.onFocusRelease(info.getUid());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAudioCacheDecay(int uid) {
        if (this.mObserver != null) {
            synchronized (this.mAudioFocusInfoStack) {
                AudioFocusInfo info = this.mAudioFocusInfoStack.get(Integer.valueOf(uid));
                if (info != null && !info.isTransientLoss()) {
                    this.mObserver.onFocusRelease(uid);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAudioFocusStack(int uid, String focusName) {
        synchronized (this.mAudioFocusInfoStack) {
            AudioFocusInfo info = this.mAudioFocusInfoStack.get(Integer.valueOf(uid));
            if (info == null) {
                AwareLog.d(TAG, "Aware app uid(" + uid + ") audio focus is already remove");
                return;
            }
            if (info.removeFocusGainRequest(focusName)) {
                this.mAudioFocusInfoStack.remove(Integer.valueOf(uid));
            }
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mStateChangeHandler = new StateChangeHandler(looper);
        } else {
            this.mStateChangeHandler = new StateChangeHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public static class AudioFocusInfo {
        private ArrayMap<String, Integer> mFocusGainRequests = new ArrayMap<>();
        private ArrayMap<String, Integer> mFocusLossReceiveds = new ArrayMap<>();
        private int mUid;

        public AudioFocusInfo(int uid, int gain, String focusName) {
            this.mUid = uid;
            this.mFocusGainRequests.put(focusName, Integer.valueOf(gain));
        }

        public int getUid() {
            return this.mUid;
        }

        public void setFocusLossReceived(int lossReceived, String focusName) {
            if (lossReceived == 0) {
                this.mFocusLossReceiveds.remove(focusName);
            } else {
                this.mFocusLossReceiveds.put(focusName, Integer.valueOf(lossReceived));
            }
        }

        public boolean isExistTransientLoss(String focusName) {
            if (!isTransientLoss()) {
                return true;
            }
            for (Map.Entry<String, Integer> entry : this.mFocusLossReceiveds.entrySet()) {
                if (!(entry.getKey() == null || entry.getKey().equals(focusName) || entry.getValue().intValue() != -2)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isExistFocusRequest(int gain, String focusName) {
            if (gain != 0 || !this.mFocusGainRequests.containsKey(focusName)) {
                return false;
            }
            return true;
        }

        public boolean removeFocusGainRequest(String focusName) {
            if (focusName != null) {
                this.mFocusLossReceiveds.remove(focusName);
                this.mFocusGainRequests.remove(focusName);
            }
            if (this.mFocusGainRequests.size() == 0) {
                return true;
            }
            return false;
        }

        public void setFocusGainRequest(int gain, String focusName) {
            this.mFocusGainRequests.put(focusName, Integer.valueOf(gain));
        }

        public boolean isTransientPlay() {
            if (this.mFocusGainRequests.values() == null || this.mFocusGainRequests.values().isEmpty()) {
                return false;
            }
            for (Integer num : this.mFocusGainRequests.values()) {
                if (num.intValue() != 3) {
                    return false;
                }
            }
            return true;
        }

        public boolean isTransientLoss() {
            return this.mFocusLossReceiveds.containsValue(-2);
        }

        public boolean isPermanentLoss() {
            if (this.mFocusLossReceiveds.values() == null || this.mFocusLossReceiveds.values().isEmpty()) {
                return false;
            }
            for (Integer num : this.mFocusLossReceiveds.values()) {
                if (num.intValue() != -1) {
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class StateChangeHandler extends Handler {
        public StateChangeHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int uid = msg.arg2;
            int i = msg.what;
            if (i == 0) {
                AwareAudioFocusManager.this.updateForeground(uid);
            } else if (i != 1) {
                if (i == 2) {
                    AwareAudioFocusManager.this.updateAudioCacheDecay(uid);
                }
            } else if (msg.obj instanceof String) {
                AwareAudioFocusManager.this.updateAudioFocusStack(uid, (String) msg.obj);
            }
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            synchronized (this.mForegroundUids) {
                pw.println("State[FOREGROUND] Uids:" + this.mForegroundUids);
            }
            synchronized (this.mAudioFocusInfoStack) {
                if (this.mAudioFocusInfoStack.values() != null) {
                    pw.println("AUDIO FOCUS INFO:");
                    for (AudioFocusInfo info : this.mAudioFocusInfoStack.values()) {
                        if (info != null) {
                            pw.println(" uid:" + info.getUid() + " --transientLoss:" + info.isTransientLoss() + " --permanentLoss:" + info.isPermanentLoss() + " --transientPlay:" + info.isTransientPlay());
                        }
                    }
                }
            }
        }
    }
}
