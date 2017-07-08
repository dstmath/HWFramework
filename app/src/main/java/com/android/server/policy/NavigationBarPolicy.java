package com.android.server.policy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.HwSlog;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.vkey.SettingsHelper;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.os.HwGeneralManager;

public class NavigationBarPolicy implements OnGestureListener {
    static final boolean DEBUG = false;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = false;
    static final int HIT_REGION_SCALE = 4;
    static int HIT_REGION_TO_MAX = 0;
    static int HIT_REGION_TO_TOP_BOTTOM = 0;
    private static final boolean IS_CHINA_AREA = false;
    private static final String TAG = "NavigationBarPolicy";
    private boolean IS_SUPPORT_PRESSURE;
    private Context mContext;
    private GestureDetector mDetector;
    boolean mForceMinNavigationBar;
    private boolean mImmersiveMode;
    private boolean mIsValidGesture;
    boolean mMinNavigationBar;
    private PhoneWindowManager mPolicy;
    private Point realSize;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.NavigationBarPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.NavigationBarPolicy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.NavigationBarPolicy.<clinit>():void");
    }

    public NavigationBarPolicy(Context context, PhoneWindowManager policy) {
        boolean z = IS_CHINA_AREA;
        this.mContext = null;
        this.mPolicy = null;
        this.mDetector = null;
        this.mMinNavigationBar = IS_CHINA_AREA;
        this.mForceMinNavigationBar = IS_CHINA_AREA;
        this.mIsValidGesture = IS_CHINA_AREA;
        this.IS_SUPPORT_PRESSURE = IS_CHINA_AREA;
        this.mImmersiveMode = IS_CHINA_AREA;
        this.realSize = new Point();
        this.mContext = context;
        this.mPolicy = policy;
        this.mDetector = new GestureDetector(context, this);
        if (Global.getInt(this.mContext.getContentResolver(), "navigationbar_is_min", 0) != 0) {
            z = true;
        }
        this.mMinNavigationBar = z;
        updateRealSize();
        this.IS_SUPPORT_PRESSURE = HwGeneralManager.getInstance().isSupportForce();
    }

    public void addPointerEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
    }

    private void reset() {
        this.mIsValidGesture = IS_CHINA_AREA;
    }

    public void setImmersiveMode(boolean mode) {
        this.mImmersiveMode = mode;
    }

    private boolean touchDownIsValid(float pointX, float pointY) {
        if (this.mPolicy.mDisplay == null || this.mForceMinNavigationBar || (this.mPolicy.mKeyguardDelegate.isShowing() && !this.mPolicy.mKeyguardDelegate.isOccluded())) {
            return IS_CHINA_AREA;
        }
        if (this.IS_SUPPORT_PRESSURE && !IS_CHINA_AREA && !this.mImmersiveMode) {
            return IS_CHINA_AREA;
        }
        boolean ret = IS_CHINA_AREA;
        HIT_REGION_TO_MAX = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17104920)) / 3.5d);
        if (this.mMinNavigationBar) {
            updateRealSize();
            ret = this.mPolicy.mNavigationBarOnBottom ? pointY > ((float) (this.realSize.y - HIT_REGION_TO_MAX)) ? true : IS_CHINA_AREA : pointX > ((float) (this.realSize.x - HIT_REGION_TO_MAX)) ? true : IS_CHINA_AREA;
        }
        return ret;
    }

    private void updateRealSize() {
        if (this.mPolicy.mDisplay != null) {
            this.mPolicy.mDisplay.getRealSize(this.realSize);
        }
    }

    public void updateNavigationBar(boolean minNaviBar) {
        this.mMinNavigationBar = minNaviBar;
        Global.putInt(this.mContext.getContentResolver(), "navigationbar_is_min", minNaviBar ? 1 : 0);
        this.mPolicy.mWindowManagerFuncs.reevaluateStatusBarSize(true);
    }

    private void sendBroadcast(boolean minNaviBar) {
        HwSlog.d(TAG, "sendBroadcast minNaviBar = " + minNaviBar);
        Intent intent = new Intent("com.huawei.navigationbar.statuschange");
        intent.putExtra("minNavigationBar", minNaviBar);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        StatisticalUtils.reportc(this.mContext, 61);
    }

    public boolean onDown(MotionEvent event) {
        this.mIsValidGesture = touchDownIsValid(event.getRawX(), event.getRawY());
        return IS_CHINA_AREA;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean z = true;
        if ("normal".equals(SystemProperties.get("ro.runmode", "normal")) && !IS_CHINA_AREA) {
            HwSlog.d(TAG, "onFling::runmode is normal and not China area, return! ");
            return IS_CHINA_AREA;
        } else if ("normal".equals(SystemProperties.get("ro.runmode", "normal")) && FRONT_FINGERPRINT_NAVIGATION) {
            HwSlog.d(TAG, "onFling::FRONT_FINGERPRINT_NAVIGATION, return! ");
            return IS_CHINA_AREA;
        } else {
            if (this.IS_SUPPORT_PRESSURE && IS_CHINA_AREA) {
                boolean ret = IS_CHINA_AREA;
                if (this.mMinNavigationBar) {
                    ret = this.mPolicy.mNavigationBarOnBottom ? e2.getY() < ((float) (this.realSize.y - HIT_REGION_TO_TOP_BOTTOM)) ? true : IS_CHINA_AREA : e2.getX() < ((float) (this.realSize.x - HIT_REGION_TO_TOP_BOTTOM)) ? true : IS_CHINA_AREA;
                }
                if (!ret) {
                    HwSlog.d(TAG, "onFling::move distance is not enough, return! ");
                    return IS_CHINA_AREA;
                }
            }
            if (!this.mIsValidGesture || SettingsHelper.isTouchPlusOn(this.mContext)) {
                HwSlog.d(TAG, "onFling::not valid gesture or touch plus on, " + this.mIsValidGesture + ", return!");
                return IS_CHINA_AREA;
            }
            if (this.mPolicy.mNavigationBarOnBottom) {
                if (e1.getRawY() >= e2.getRawY()) {
                    z = IS_CHINA_AREA;
                }
                sendBroadcast(z);
            } else {
                if (e1.getRawX() >= e2.getRawX()) {
                    z = IS_CHINA_AREA;
                }
                sendBroadcast(z);
            }
            reset();
            return IS_CHINA_AREA;
        }
    }

    public void onLongPress(MotionEvent arg0) {
    }

    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return IS_CHINA_AREA;
    }

    public void onShowPress(MotionEvent arg0) {
    }

    public boolean onSingleTapUp(MotionEvent arg0) {
        reset();
        return IS_CHINA_AREA;
    }
}
