package com.android.server.input;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import huawei.com.android.server.policy.HwGlobalActionsData;

public class HwGuiShou {
    private static final String FINGERPRINT_SLIDE_SWITCH = "fingerprint_slide_switch";
    private static boolean GUISHOU_ENABLED = false;
    private static final int SIM_SUB1 = 0;
    private static final int SIM_SUB2 = 1;
    private static final String TAG = "HwGuiShou";
    private Context mContext;
    private FingerprintNavigation mFingerprintNavigationFilter;
    private FpSlideSwitchSettingsObserver mFpSlideSwitchSettingsObserver;
    private Handler mHandler;
    private HwInputManagerService mHwInputManagerService;
    private int mInCallCount;
    private boolean mIsPhoneStateListenerNotRegister;
    private Looper mLooper;
    private TelephonyManager mPhoneManager;
    private ContentResolver mResolver;
    private int mTempSlideSwitch;

    /* renamed from: com.android.server.input.HwGuiShou.1 */
    class AnonymousClass1 extends PhoneStateListener {
        AnonymousClass1(int $anonymous0, Looper $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            int i = HwGuiShou.SIM_SUB1;
            HwGuiShou hwGuiShou;
            switch (state) {
                case HwGuiShou.SIM_SUB1 /*0*/:
                    Slog.i(HwGuiShou.TAG, "call state: idle");
                    HwGuiShou hwGuiShou2 = HwGuiShou.this;
                    hwGuiShou2.mInCallCount = hwGuiShou2.mInCallCount - 1;
                    if (HwGuiShou.this.mInCallCount == 0) {
                        Secure.putIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, HwGuiShou.this.mTempSlideSwitch, ActivityManager.getCurrentUser());
                    } else if (HwGuiShou.this.mInCallCount < 0) {
                        HwGuiShou.this.mInCallCount = HwGuiShou.SIM_SUB1;
                    }
                case HwGuiShou.SIM_SUB2 /*1*/:
                    Slog.i(HwGuiShou.TAG, "call state: ringing");
                    if (HwGuiShou.this.mInCallCount == 0) {
                        if (HwGuiShou.this.mFingerprintNavigationFilter.isAlarm()) {
                            hwGuiShou = HwGuiShou.this;
                            if (HwGuiShou.this.isDefaultSlideSwitchOn()) {
                                i = HwGuiShou.SIM_SUB2;
                            }
                            hwGuiShou.mTempSlideSwitch = i;
                        } else {
                            HwGuiShou.this.mTempSlideSwitch = Secure.getIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, HwGuiShou.SIM_SUB1, ActivityManager.getCurrentUser());
                        }
                    }
                    HwGuiShou hwGuiShou3 = HwGuiShou.this;
                    hwGuiShou3.mInCallCount = hwGuiShou3.mInCallCount + HwGuiShou.SIM_SUB2;
                    Secure.putIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, HwGuiShou.SIM_SUB2, ActivityManager.getCurrentUser());
                case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                    Slog.i(HwGuiShou.TAG, "call state: offhook");
                    hwGuiShou = HwGuiShou.this;
                    if (HwGuiShou.this.isDefaultSlideSwitchOn()) {
                        i = HwGuiShou.SIM_SUB2;
                    }
                    hwGuiShou.mTempSlideSwitch = i;
                    HwGuiShou.this.mFpSlideSwitchSettingsObserver.mmForceUpdateChange = true;
                    Secure.putIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, HwGuiShou.this.mTempSlideSwitch, ActivityManager.getCurrentUser());
                default:
            }
        }
    }

    class FpSlideSwitchSettingsObserver extends ContentObserver {
        public boolean mmForceUpdateChange;

        FpSlideSwitchSettingsObserver(Handler handler) {
            super(handler);
            this.mmForceUpdateChange = false;
        }

        public void registerContentObserver(int userId) {
            HwGuiShou.this.mResolver.registerContentObserver(Secure.getUriFor(HwGuiShou.FINGERPRINT_SLIDE_SWITCH), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            int i = HwGuiShou.SIM_SUB2;
            boolean injectSlide = Secure.getIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, HwGuiShou.SIM_SUB1, ActivityManager.getCurrentUser()) != 0;
            if (this.mmForceUpdateChange || HwGuiShou.this.mInCallCount <= 0 || injectSlide) {
                this.mmForceUpdateChange = false;
                Log.d(HwGuiShou.TAG, "open fingerprint nav=" + injectSlide);
                ContentResolver -get3 = HwGuiShou.this.mResolver;
                String str = HwGuiShou.FINGERPRINT_SLIDE_SWITCH;
                if (!injectSlide) {
                    i = HwGuiShou.SIM_SUB1;
                }
                System.putInt(-get3, str, i);
                return;
            }
            Secure.putIntForUser(HwGuiShou.this.mResolver, HwGuiShou.FINGERPRINT_SLIDE_SWITCH, HwGuiShou.SIM_SUB2, ActivityManager.getCurrentUser());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.input.HwGuiShou.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.input.HwGuiShou.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.input.HwGuiShou.<clinit>():void");
    }

    public HwGuiShou(HwInputManagerService inputService, Context context, Handler handler, FingerprintNavigation fn) {
        this.mInCallCount = SIM_SUB1;
        this.mTempSlideSwitch = SIM_SUB1;
        this.mIsPhoneStateListenerNotRegister = true;
        this.mContext = context;
        this.mHandler = handler;
        this.mHwInputManagerService = inputService;
        this.mFingerprintNavigationFilter = fn;
        if (context != null) {
            this.mResolver = context.getContentResolver();
        }
    }

    public static boolean isGuiShouEnabled() {
        return GUISHOU_ENABLED;
    }

    public static boolean isFPNavigationInThreeAppsEnabled(boolean injectCamera, boolean answerCall, boolean stopAlarm) {
        boolean z = false;
        if (isGuiShouEnabled()) {
            return true;
        }
        if (!(injectCamera || answerCall || stopAlarm)) {
            z = true;
        }
        return z;
    }

    private boolean isDefaultSlideSwitchOn() {
        if (this.mHwInputManagerService != null) {
            return this.mHwInputManagerService.isDefaultSlideSwitchOn();
        }
        return true;
    }

    private void registerPhoneStateListenerBySub(int sub) {
        this.mTempSlideSwitch = isDefaultSlideSwitchOn() ? SIM_SUB2 : SIM_SUB1;
        PhoneStateListener listener = new AnonymousClass1(sub, this.mLooper);
        if (this.mPhoneManager != null) {
            this.mPhoneManager.listen(listener, 481);
        }
    }

    public void registerPhoneStateListener() {
        if (isGuiShouEnabled()) {
            this.mLooper = Looper.myLooper();
            this.mPhoneManager = TelephonyManager.from(this.mContext);
            if (this.mPhoneManager != null && this.mIsPhoneStateListenerNotRegister) {
                registerPhoneStateListenerBySub(SIM_SUB1);
                registerPhoneStateListenerBySub(SIM_SUB2);
                this.mIsPhoneStateListenerNotRegister = false;
            }
        }
    }

    public void registerFpSlideSwitchSettingsObserver() {
        if (isGuiShouEnabled()) {
            this.mFpSlideSwitchSettingsObserver = new FpSlideSwitchSettingsObserver(this.mHandler);
            this.mFpSlideSwitchSettingsObserver.registerContentObserver(UserHandle.myUserId());
        }
    }

    public void registerFpSlideSwitchSettingsObserver(int newUserId) {
        if (isGuiShouEnabled()) {
            this.mFpSlideSwitchSettingsObserver = new FpSlideSwitchSettingsObserver(this.mHandler);
            this.mFpSlideSwitchSettingsObserver.registerContentObserver(newUserId);
        }
    }

    public void setFingerprintSlideSwitchValue(boolean value) {
        if (isGuiShouEnabled()) {
            Secure.putIntForUser(this.mResolver, FINGERPRINT_SLIDE_SWITCH, value ? SIM_SUB2 : SIM_SUB1, ActivityManager.getCurrentUser());
        }
    }
}
