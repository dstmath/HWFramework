package ohos.dcall;

import android.content.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AospCallAdapter {
    private static final int LOG_DOMAIN_DCALL = 218111744;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218111744, TAG);
    private static final String TAG = "AospCallAdapter";
    private static AospCallAdapter sInstance = new AospCallAdapter();
    private static boolean sNativeAvailable;
    private AospInCallService mService;

    private native void nativeInitDCALL();

    static {
        sNativeAvailable = false;
        try {
            HiLog.info(LOG_LABEL, "AospCallAdapter load libdcall_jni.z.so", new Object[0]);
            System.loadLibrary("dcall_jni.z");
            sNativeAvailable = true;
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LOG_LABEL, "AospCallAdapter Could not load libdcall_jni.z.so", new Object[0]);
        }
    }

    public static AospCallAdapter getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        if (!sNativeAvailable) {
            HiLog.error(LOG_LABEL, "init error! libdcall_jni.z.so is not available", new Object[0]);
        } else {
            nativeInitDCALL();
        }
    }

    public void setAospInCallService(AospInCallService aospInCallService) {
        this.mService = aospInCallService;
    }

    public AospInCallService getAospInCallService() {
        return this.mService;
    }

    static AospInCallService getInCallService() {
        return getInstance().getAospInCallService();
    }

    static void answerCall(int i, int i2) {
        HiLog.info(LOG_LABEL, "answerCall, invoke from SA.", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.answer(i, i2);
        } else {
            HiLog.error(LOG_LABEL, "answerCall fail, no InCallService.", new Object[0]);
        }
    }

    static void disconnectCall(int i) {
        HiLog.info(LOG_LABEL, "disconnectCall, invoke from SA.", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.disconnect(i);
        } else {
            HiLog.error(LOG_LABEL, "disconnectCall fail, no InCallService.", new Object[0]);
        }
    }

    static void playDtmfTone(int i, int i2) {
        HiLog.info(LOG_LABEL, "playDtmfTone, invoke from SA.", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.playDtmfTone(i, (char) i2);
        } else {
            HiLog.error(LOG_LABEL, "playDtmfTone fail, no InCallService.", new Object[0]);
        }
    }

    static void stopDtmfTone(int i) {
        HiLog.info(LOG_LABEL, "stopDtmfTone, invoke from SA.", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.stopDtmfTone(i);
        } else {
            HiLog.error(LOG_LABEL, "stopDtmfTone fail, no InCallService.", new Object[0]);
        }
    }

    static void postDialContinue(int i, boolean z) {
        HiLog.info(LOG_LABEL, "postDialContinue, invoke from SA.", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.postDialContinue(i, z);
        } else {
            HiLog.error(LOG_LABEL, "postDialContinue fail, no InCallService.", new Object[0]);
        }
    }

    static void rejectCall(int i, boolean z, String str) {
        HiLog.info(LOG_LABEL, "rejectCall, invoke from SA.", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.reject(i, z, str);
        } else {
            HiLog.error(LOG_LABEL, "rejectCall fail, no InCallService.", new Object[0]);
        }
    }

    static boolean canAddCall() {
        HiLog.info(LOG_LABEL, "canAddCall, invoke from SA.", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            boolean canAddCall = inCallService.canAddCall();
            HiLog.info(LOG_LABEL, "canAddCall, %{public}s", new Object[]{Boolean.valueOf(canAddCall)});
            return canAddCall;
        }
        HiLog.error(LOG_LABEL, "canAddCall fail, no InCallService.", new Object[0]);
        return false;
    }

    static void setMuted(boolean z) {
        HiLog.info(LOG_LABEL, "setMuted, invoke from SA", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.setMuted(z);
        } else {
            HiLog.error(LOG_LABEL, "setMuted fail,, no InCallService", new Object[0]);
        }
    }

    static void setAudioRoute(int i) {
        HiLog.info(LOG_LABEL, "setAudioRoute, invoke from SA", new Object[0]);
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.setAudioRoute(i);
        } else {
            HiLog.error(LOG_LABEL, "setAudioRoute fail,, no InCallService", new Object[0]);
        }
    }

    private AospCallAdapter() {
        HiLog.info(LOG_LABEL, "constructor", new Object[0]);
    }
}
