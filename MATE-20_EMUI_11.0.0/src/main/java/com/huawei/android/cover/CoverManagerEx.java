package com.huawei.android.cover;

import android.cover.CoverManager;
import android.cover.HallState;
import android.cover.IHallCallback;
import android.util.Log;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CoverManagerEx {
    public static final String HALL_STATE_RECEIVER_ASSOCIATED = "associated";
    public static final String HALL_STATE_RECEIVER_AUDIO = "audioserver";
    public static final String HALL_STATE_RECEIVER_CAMERA = "cameraserver";
    public static final String HALL_STATE_RECEIVER_DEFINE = "android";
    public static final String HALL_STATE_RECEIVER_FACE = "facerecognize";
    public static final String HALL_STATE_RECEIVER_GETSTATE = "getstate";
    public static final String HALL_STATE_RECEIVER_PHONE = "com.android.phone";
    private static final int KNOWN_HALL_CALLBACK_COUNT = 7;
    private static final Object S_INSTANCE_SYNC = new Object();
    private static final String TAG = "CoverMangerEx";
    private static final int TOKEN_MIN_LENGTH = 2;
    private static volatile CoverManagerEx sSelf = null;
    private IHallCallback.Stub mCallback = new IHallCallback.Stub() {
        /* class com.huawei.android.cover.CoverManagerEx.AnonymousClass1 */

        public void onStateChange(HallState hallState) {
            IHallCallbackEx callback;
            HallStateEx stateEx = new HallStateEx(hallState);
            for (Map.Entry<String, IHallCallbackEx> entry : CoverManagerEx.this.mCallbackExList.entrySet()) {
                String[] tokens = entry.getKey().split("#");
                if (tokens.length < 2) {
                    Log.w(CoverManagerEx.TAG, "invalid name =" + entry.getKey());
                } else if (Integer.parseInt(tokens[tokens.length - 1]) == stateEx.type && (callback = entry.getValue()) != null) {
                    callback.onStateChange(stateEx);
                }
            }
        }
    };
    private Map<String, IHallCallbackEx> mCallbackExList = new ConcurrentHashMap(7);

    public static CoverManagerEx getDefault() {
        CoverManagerEx coverManagerEx;
        synchronized (S_INSTANCE_SYNC) {
            if (sSelf == null) {
                sSelf = new CoverManagerEx();
            }
            coverManagerEx = sSelf;
        }
        return coverManagerEx;
    }

    public static boolean isCoverOpen() {
        CoverManager coverManager = CoverManager.getDefault();
        if (coverManager != null) {
            return coverManager.isCoverOpen();
        }
        return false;
    }

    public static int getHallState(int hallType) {
        CoverManager coverManager = CoverManager.getDefault();
        if (coverManager != null) {
            return coverManager.getHallState(hallType);
        }
        return -1;
    }

    public boolean registerHallCallback(String receiverName, int hallType, IHallCallbackEx callback) {
        if (receiverName == null || callback == null) {
            return false;
        }
        CoverManager coverManager = CoverManager.getDefault();
        boolean ret = false;
        if (coverManager != null) {
            ret = coverManager.registerHallCallback(receiverName, hallType, this.mCallback);
        }
        if (ret) {
            Map<String, IHallCallbackEx> map = this.mCallbackExList;
            map.put(receiverName + "#" + hallType, callback);
        }
        return ret;
    }

    public boolean unRegisterHallCallback(String receiverName, int hallType) {
        if (receiverName == null) {
            return false;
        }
        CoverManager coverManager = CoverManager.getDefault();
        boolean ret = false;
        if (coverManager != null) {
            ret = coverManager.unRegisterHallCallback(receiverName, hallType);
        }
        String name = receiverName + "#" + hallType;
        if (this.mCallbackExList.containsKey(name)) {
            this.mCallbackExList.remove(name);
        }
        return ret;
    }

    public boolean unRegisterHallCallback(int hallType, IHallCallbackEx callback) {
        if (callback == null) {
            return false;
        }
        CoverManager coverManager = CoverManager.getDefault();
        boolean ret = false;
        if (coverManager != null) {
            ret = coverManager.unRegisterHallCallbackEx(hallType, this.mCallback);
        }
        Iterator<Map.Entry<String, IHallCallbackEx>> it = this.mCallbackExList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, IHallCallbackEx> entry = it.next();
            String[] tokens = entry.getKey().split("#");
            if (tokens.length < 2) {
                Log.w(TAG, "invalid name =" + entry.getKey());
            } else if (Integer.parseInt(tokens[tokens.length - 1]) == hallType && callback.equals(entry.getValue())) {
                it.remove();
                Log.d(TAG, "callback removed!");
            }
        }
        return ret;
    }
}
