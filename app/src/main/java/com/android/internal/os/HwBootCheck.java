package com.android.internal.os;

import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import com.huawei.pgmng.PGAction;

public final class HwBootCheck {
    public static final int MESSAGE_CHECK_AFTER_AMS_INIT = 100;
    public static final int MESSAGE_CHECK_AFTER_BOOT_DEXOPT = 102;
    public static final int MESSAGE_CHECK_AFTER_PMS_INIT = 101;
    public static final int MESSAGE_CHECK_PERFORM_SYSTEM_SERVER_DEXOPT = 103;
    private static final String TAG = "HwBootFail";
    private static Handler mBootCheckHandler;
    private static HandlerThread mBootCheckThread;
    private static StringBuilder mBootInfo;

    private static final class BootCheckHandler extends Handler {
        public BootCheckHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwBootCheck.MESSAGE_CHECK_PERFORM_SYSTEM_SERVER_DEXOPT /*103*/:
                    try {
                        Slog.w(HwBootCheck.TAG, "performSystemServerDexOpt: installer.waitForConnection");
                        HwBootCheck.addBootInfo("performSystemServerDexOpt: installer.waitForConnection");
                        HwBootFail.bootFailError(HwBootFail.SYSTEM_SERVICE_LOAD_FAIL, 0, HwBootFail.creatFrameworkBootFailLog(null, HwBootCheck.getBootInfo()));
                    } catch (Exception ex) {
                        Slog.e(HwBootCheck.TAG, "CHECK_PERFORM_SYSTEM_SERVER_DEXOPT has exception:" + ex);
                    }
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.HwBootCheck.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.HwBootCheck.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.HwBootCheck.<clinit>():void");
    }

    private static void ensureThreadLocked() {
        if (mBootCheckThread == null) {
            mBootCheckThread = new HandlerThread("bootCheck");
            mBootCheckThread.start();
            mBootCheckHandler = new BootCheckHandler(mBootCheckThread.getLooper());
        }
    }

    public static HandlerThread getHandlerThread() {
        HandlerThread handlerThread;
        synchronized (HwBootCheck.class) {
            ensureThreadLocked();
            handlerThread = mBootCheckThread;
        }
        return handlerThread;
    }

    public static Handler getHandler() {
        Handler handler;
        synchronized (HwBootCheck.class) {
            ensureThreadLocked();
            handler = mBootCheckHandler;
        }
        return handler;
    }

    public static void bootCheckThreadQuit() {
        mBootInfo.delete(0, mBootInfo.length());
        getHandlerThread().quit();
    }

    public static boolean bootSceneStart(int sceneId, long maxTime) {
        try {
            if (PGAction.PG_ID_DEFAULT_FRONT <= Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            } else if (mBootCheckThread.isAlive()) {
                Slog.i(TAG, "bootSceneStart :" + sceneId);
                if (!mBootCheckHandler.hasMessages(sceneId)) {
                    mBootCheckHandler.sendEmptyMessageDelayed(sceneId, maxTime);
                }
                return true;
            } else {
                Slog.w(TAG, "mBootCheckThread is not alive");
                return false;
            }
        } catch (Exception ex) {
            Slog.e(TAG, "get ex:" + ex);
            return false;
        }
    }

    public static boolean bootSceneEnd(int sceneId) {
        try {
            if (PGAction.PG_ID_DEFAULT_FRONT <= Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            }
            Slog.i(TAG, "bootSceneEnd :" + sceneId);
            if (mBootCheckHandler.hasMessages(sceneId)) {
                mBootCheckHandler.removeMessages(sceneId);
            }
            return true;
        } catch (Exception ex) {
            Slog.e(TAG, "has ex:" + ex);
            return false;
        }
    }

    public static String getBootInfo() {
        try {
            if (PGAction.PG_ID_DEFAULT_FRONT > Binder.getCallingUid()) {
                return mBootInfo.toString();
            }
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return null;
        } catch (Exception ex) {
            Slog.e(TAG, "get exception ex:" + ex);
            return null;
        }
    }

    public static boolean addBootInfo(String info) {
        try {
            if (PGAction.PG_ID_DEFAULT_FRONT <= Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            } else if (HwBootFail.isBootSuccess() || mBootInfo == null) {
                return false;
            } else {
                mBootInfo.append(info).append("\n");
                return true;
            }
        } catch (Exception ex) {
            Slog.e(TAG, "exception info ex:" + ex);
            return false;
        }
    }
}
