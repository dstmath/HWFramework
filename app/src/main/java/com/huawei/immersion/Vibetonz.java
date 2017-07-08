package com.huawei.immersion;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings.System;
import huawei.android.os.HwGeneralManager;

public class Vibetonz {
    public static final int HAPTIC_ALARM = 1;
    public static final int HAPTIC_EVENT_BULK_MOVE_ICONDROP = 900;
    public static final int HAPTIC_EVENT_BULK_MOVE_ICONPICKUP = 1100;
    public static final int HAPTIC_EVENT_BULK_MOVE_ICON_GATHER = 1000;
    public static final int HAPTIC_EVENT_CONTACT_ALPHA_SWITCH = 1600;
    public static final int HAPTIC_EVENT_COUNTDOWN_SWING = 100;
    public static final int HAPTIC_EVENT_FM_ADJUST = 500;
    public static final int HAPTIC_EVENT_FM_ADJUST_DONE = 700;
    public static final int HAPTIC_EVENT_FM_SPIN = 600;
    public static final int HAPTIC_EVENT_HOMESCREEN_ICON_FLY_WORKSPACE = 800;
    public static final int HAPTIC_EVENT_HOMESCREEN_SHAKE_ALIGN = 1200;
    public static final int HAPTIC_EVENT_LOCKSCREEN_UNLOCK = 2500;
    public static final int HAPTIC_EVENT_LONG_PRESS = 1300;
    public static final int HAPTIC_EVENT_LONG_PRESS_WORKSPACE = 1400;
    public static final int HAPTIC_EVENT_NUMBERPICKER_ITEMSCROLL = 300;
    public static final int HAPTIC_EVENT_NUMBERPICKER_TUNER = 400;
    public static final int HAPTIC_EVENT_SCROLL_INDICATOR_POP = 1500;
    public static final int HAPTIC_EVENT_TEXTVIEW_DOUBLE_TAP_SELECTWORD = 1800;
    public static final int HAPTIC_EVENT_TEXTVIEW_SELECTCHAR = 1700;
    public static final int HAPTIC_EVENT_TEXTVIEW_SETCURSOR = 2000;
    public static final int HAPTIC_EVENT_TEXTVIEW_TAPWORD = 1900;
    public static final int HAPTIC_EVENT_TIMING_ROTATE = 200;
    public static final int HAPTIC_EVENT_VIRTUAL_KEY = 2600;
    public static final int HAPTIC_EVENT_WEATHER_RAIN = 2100;
    public static final int HAPTIC_EVENT_WEATHER_SAND = 2200;
    public static final int HAPTIC_EVENT_WEATHER_THUNDERSTORM = 2300;
    public static final int HAPTIC_EVENT_WEATHER_WINDY = 2400;
    public static final int HAPTIC_NOTIFICATION = 3;
    public static final int HAPTIC_RINGTONE = 2;
    private static boolean mIsVibrateImplemented;
    private static Vibetonz mVibetonz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.immersion.Vibetonz.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.immersion.Vibetonz.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.immersion.Vibetonz.<clinit>():void");
    }

    private Vibetonz() {
    }

    public static boolean isVibrateOn(Context mContext) {
        boolean z = true;
        if (mContext == null) {
            return false;
        }
        if (!mIsVibrateImplemented) {
            z = false;
        } else if (HAPTIC_ALARM != System.getInt(mContext.getContentResolver(), "touch_vibrate_mode", HAPTIC_ALARM)) {
            z = false;
        }
        return z;
    }

    public static Vibetonz getInstance() {
        if (mIsVibrateImplemented && mVibetonz == null) {
            mVibetonz = new Vibetonz();
        }
        return mVibetonz;
    }

    public void playIvtEffect(int effectNo) {
        HwGeneralManager.getInstance().playIvtEffect(effectNo);
    }

    public void stopPlayEffect() {
        HwGeneralManager.getInstance().stopPlayEffect();
    }

    public void pausePlayEffect(int effectNo) {
        HwGeneralManager.getInstance().pausePlayEffect(effectNo);
    }

    public void resumePausedEffect(int effectNo) {
        HwGeneralManager.getInstance().resumePausedEffect(effectNo);
    }

    public boolean isPlaying(int effectNo) {
        return HwGeneralManager.getInstance().isPlaying(effectNo);
    }

    public boolean startHaptic(Context mContext, int callerID, int ringtoneType, Uri uri) {
        return HwGeneralManager.getInstance().startHaptic(mContext, callerID, ringtoneType, uri);
    }

    public boolean hasHaptic(Context mContext, Uri uri) {
        return HwGeneralManager.getInstance().hasHaptic(mContext, uri);
    }

    public void stopHaptic() {
        HwGeneralManager.getInstance().stopHaptic();
    }
}
