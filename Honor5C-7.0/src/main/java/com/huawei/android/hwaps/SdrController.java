package com.huawei.android.hwaps;

import android.app.HwApsInterface;
import android.os.SystemClock;
import android.os.SystemProperties;

public class SdrController {
    public static final int KEYCODE_BACK = 4;
    public static final int KEYCODE_HOME = 3;
    public static final int KEYCODE_MENU = 82;
    public static final int KEYCODE_POWER = 26;
    private static final String TAG = "SdrController";
    private static boolean mIsTurnOnSSR;
    private static SdrController sInstance;
    public float mRatio;

    /* renamed from: com.huawei.android.hwaps.SdrController.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ String val$msg;

        AnonymousClass1(String val$msg) {
            this.val$msg = val$msg;
        }

        public void run() {
            SystemProperties.set("sys.aps.keycode", this.val$msg);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.SdrController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.SdrController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.SdrController.<clinit>():void");
    }

    public SdrController() {
        this.mRatio = 2.0f;
    }

    public static synchronized SdrController getInstance() {
        SdrController sdrController;
        synchronized (SdrController.class) {
            if (sInstance == null) {
                sInstance = new SdrController();
            }
            sdrController = sInstance;
        }
        return sdrController;
    }

    public static boolean isSupportApsSdr() {
        if (2048 == (SystemProperties.getInt("sys.aps.support", 0) & 2048)) {
            return true;
        }
        ApsCommon.logI(TAG, "SDR: control: Dcr module is not supported");
        return false;
    }

    public void startSdr() {
        HwApsInterface.nativeStartSdr(this.mRatio);
        ApsCommon.logD(TAG, "SDR: control: start zoom");
    }

    public void stopSdr() {
        HwApsInterface.nativeStopSdr();
        ApsCommon.logD(TAG, "SDR: control: stop zoom");
    }

    public void stopSdrImmediately() {
        HwApsInterface.nativeStopSdrImmediately();
        ApsCommon.logD(TAG, "SDR: control: stop zoom immediately");
    }

    public void setSdrRatio(float ratio) {
        this.mRatio = ratio;
        HwApsInterface.nativeSetSdrRatio(ratio);
        ApsCommon.logD(TAG, "SDR: control: setSdrRatio  : " + this.mRatio);
    }

    public float getCurrentSdrRatio() {
        return HwApsInterface.nativeGetCurrentSdrRatio();
    }

    public boolean IsSdrCase() {
        boolean isSdrCase = HwApsInterface.nativeIsSdrCase();
        ApsCommon.logD(TAG, "SDR: control: check if sdr can be run. [result:" + isSdrCase + "]");
        return isSdrCase;
    }

    public static boolean StopSdrForSpecial(String info, int keyCode) {
        if (-1 != keyCode && mIsTurnOnSSR) {
            setPropertyForKeyCode(keyCode);
        }
        return true;
    }

    private static void setPropertyForKeyCode(int keyCode) {
        String msg = Long.toString(SystemClock.uptimeMillis()) + "|" + Integer.toString(keyCode);
        new Thread(new AnonymousClass1(msg)).start();
        ApsCommon.logD(TAG, "SDR: Controller, setPropertyForKeyCode, keycode: " + keyCode + ", msg: " + msg);
    }
}
