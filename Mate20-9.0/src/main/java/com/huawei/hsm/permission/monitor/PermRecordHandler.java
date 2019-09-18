package com.huawei.hsm.permission.monitor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.huawei.hsm.permission.StubController;

public class PermRecordHandler extends Handler {
    private static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final int CN_COMMERCIAL_USER = 1;
    private static final int MSG_UPLOAD = 1;
    private static final int MSG_UPLOAD_DELAY = 1000;
    private static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private static final String TAG = "PermRecordHandler";
    private static int mUserType = SystemProperties.getInt("ro.logsystem.usertype", -1);
    private static PermRecordHandler sPermHandler;
    private volatile PermCounter mPermCounter;

    private PermRecordHandler(Looper looper) {
        super(looper);
    }

    public static synchronized PermRecordHandler getHandleInstance() {
        PermRecordHandler permRecordHandler;
        synchronized (PermRecordHandler.class) {
            if (sPermHandler == null) {
                HandlerThread handlerThread = new HandlerThread(TAG);
                handlerThread.start();
                sPermHandler = new PermRecordHandler(handlerThread.getLooper());
            }
            permRecordHandler = sPermHandler;
        }
        return permRecordHandler;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
        return;
     */
    public synchronized void accessPermission(String pkg, String permission, boolean isAllow, int uid, String desStr) {
        if (!checkRejectCondition(permission, uid)) {
            if (this.mPermCounter != null && this.mPermCounter.isSamePerm(pkg, permission, isAllow, desStr)) {
                if (hasMessages(1)) {
                    this.mPermCounter.count();
                }
            }
            this.mPermCounter = new PermCounter(pkg, permission, isAllow, desStr);
            sendPermMessageDelayed(1, 1000);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
        return;
     */
    public synchronized void accessPermission(int uid, int pid, int permType, String desStr) {
        if (!checkRejectCondition(null, uid)) {
            int validUid = StubController.handleIncomingUser(uid);
            int validPid = StubController.handleIncomingPid(uid, pid);
            if (this.mPermCounter != null && this.mPermCounter.isSamePerm(validUid, validPid, permType)) {
                if (hasMessages(1)) {
                    this.mPermCounter.count();
                }
            }
            this.mPermCounter = new PermCounter(permType, desStr, validUid, validPid);
            sendPermMessageDelayed(1, 1000);
        }
    }

    private static boolean checkRejectCondition(String permName, int uid) {
        boolean z = true;
        if (!CHINA_RELEASE_VERSION) {
            return true;
        }
        if (mUserType == 1 && SYSTEM_ALERT_WINDOW.equals(permName)) {
            return !StubController.checkPrecondition(uid);
        }
        if (mUserType != 1) {
            z = false;
        }
        return z;
    }

    private void sendPermMessageDelayed(int what, int delay) {
        Message msg = Message.obtain(this, what);
        msg.setData(this.mPermCounter.getData());
        sendMessageDelayed(msg, (long) delay);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == 1) {
            StubController.recordPermissionUsage(msg.getData());
        }
    }
}
