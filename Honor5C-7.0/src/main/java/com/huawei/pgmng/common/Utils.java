package com.huawei.pgmng.common;

import android.content.pm.ActivityInfo;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.util.Log;
import android.view.Window;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.AsyncService;
import com.huawei.pgmng.log.LogPower;
import com.vzw.nfc.dos.FilterEntryDo;
import javax.microedition.khronos.opengles.GL10;

public final class Utils {
    private static final String TAG = "PG Utils";
    private static int mAutoAdjustBrightnessLimitVal;
    private static int mRatioMaxBrightness;
    private static int mRatioMinBrightness;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.pgmng.common.Utils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.pgmng.common.Utils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.pgmng.common.Utils.<clinit>():void");
    }

    public static void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
        Log.i(TAG, "configBrightnessRange ratioMin = " + ratioMin + "  ratioMax = " + ratioMax + "  autoLimit = " + autoLimit);
        mRatioMinBrightness = ratioMin;
        mRatioMaxBrightness = ratioMax;
        mAutoAdjustBrightnessLimitVal = autoLimit;
    }

    public static int getRatioBright(int bright, double ratio) {
        if (bright <= mRatioMinBrightness || bright >= mRatioMaxBrightness) {
            return bright;
        }
        bright = (int) (((double) bright) * ratio);
        if (bright < mRatioMinBrightness) {
            return mRatioMinBrightness;
        }
        return bright;
    }

    public static int getAutoAdjustBright(int bright) {
        if (bright < mAutoAdjustBrightnessLimitVal && bright > mRatioMinBrightness) {
            return bright - (((bright - mRatioMinBrightness) * 3) / 10);
        }
        if (bright <= mAutoAdjustBrightnessLimitVal || bright >= mRatioMaxBrightness) {
            return bright;
        }
        return bright - (((mRatioMaxBrightness - bright) * 3) / 10);
    }

    public static int getAnimatedValue(int tarVal, int curVal, int amount) {
        int animatedValue = curVal;
        if (curVal < tarVal) {
            return Math.min(curVal + amount, tarVal);
        }
        if (curVal > tarVal) {
            return Math.max(curVal - amount, tarVal);
        }
        return animatedValue;
    }

    public static boolean isActivityHardwareAccelerated(ActivityInfo ai, Window w) {
        boolean prp = SystemProperties.getBoolean(Window.PROPERTY_HARDWARE_UI, false);
        boolean act = (ai.flags & GL10.GL_NEVER) != 0;
        boolean win = (w == null || (w.getAttributes().flags & AsyncService.CMD_ASYNC_SERVICE_DESTROY) == 0) ? false : true;
        return (prp || act) ? true : win;
    }

    public static void noteWakelock(int flags, String tag, int ownerUid, int ownerPid, WorkSource workSource, int eventTag) {
        if (workSource != null) {
            int N = workSource.size();
            for (int i = 0; i < N; i++) {
                ownerUid = workSource.get(i);
                if (!(ownerUid == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED || ownerUid == RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED)) {
                    LogPower.push(eventTag, Integer.toString(ownerUid), Integer.toString(flags), Integer.toString(-2), new String[]{tag});
                }
            }
        } else if (ownerUid != RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED && ownerUid != RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED) {
            LogPower.push(eventTag, Integer.toString(ownerUid), Integer.toString(flags), Integer.toString(ownerPid), new String[]{tag});
        }
    }

    private static void checkWorkSourceThenNote(int flags, String tag, int ownerUid, int ownerPid, WorkSource workSource1, WorkSource workSource2, int eventTag) {
        int iWs1Size = workSource1.size();
        int iWs2Size = workSource2.size();
        for (int i = 0; i < iWs2Size; i++) {
            ownerUid = workSource2.get(i);
            int j = 0;
            while (j < iWs1Size && workSource1.get(j) != ownerUid) {
                j++;
            }
            if (!(j != iWs1Size || ownerUid == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED || ownerUid == RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED)) {
                LogPower.push(eventTag, Integer.toString(ownerUid), Integer.toString(flags), Integer.toString(-2), new String[]{tag});
            }
        }
    }

    public static void noteWakelock(int flags, String tag, int ownerUid, int ownerPid, WorkSource oldWorkSource, WorkSource newWorkSource) {
        checkWorkSourceThenNote(flags, tag, ownerUid, ownerPid, newWorkSource, oldWorkSource, FilterEntryDo._TAG);
        checkWorkSourceThenNote(flags, tag, ownerUid, ownerPid, oldWorkSource, newWorkSource, LogPower.WAKELOCK_ACQUIRED);
    }

    public static void handleTimeOut(String reason, String pkg, String pid) {
        LogPower.push(LogPower.FREEZER_EXCEPTION, reason, pkg, pid);
    }
}
