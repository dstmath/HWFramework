package android.hwrme;

import android.os.SystemClock;
import android.util.Log;

public final class HwPrommEventManager {
    private static final String TAG = "HwPrommEvent";
    private static final int TIME_INTERVAL = 50;
    private static HwPrommEventManager hwPrommEventMng = null;
    private long appStartTime = 0;
    private int appUid = 0;
    private long inputEventTime = 0;
    private boolean needToNotify = false;

    public static synchronized HwPrommEventManager getInstance() {
        HwPrommEventManager hwPrommEventManager;
        synchronized (HwPrommEventManager.class) {
            if (hwPrommEventMng == null) {
                hwPrommEventMng = new HwPrommEventManager();
            }
            hwPrommEventManager = hwPrommEventMng;
        }
        return hwPrommEventManager;
    }

    public void getActivityDisplayed(int uid) {
        if (uid != this.appUid) {
            this.needToNotify = true;
        }
        this.appStartTime = SystemClock.elapsedRealtime();
        this.appUid = uid;
    }

    public void getInputEvent() {
        this.inputEventTime = SystemClock.elapsedRealtime();
        if (checkAppStartEnd() && this.needToNotify) {
            sendToPromm();
        }
    }

    private boolean checkAppStartEnd() {
        if (this.inputEventTime - this.appStartTime > 50) {
            return true;
        }
        return false;
    }

    private void sendToPromm() {
        HwResMngEngine resMng = HwResMngEngine.getInstance();
        if (resMng == null) {
            Log.e(TAG, "failed to get HwResMngEngine instance");
            return;
        }
        resMng.sendMmEvent(1, this.appUid);
        this.needToNotify = false;
    }
}
