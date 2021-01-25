package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Handler;
import android.os.ITrustedUIService;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;

public class TrustedUIService extends ITrustedUIService.Stub {
    private static final String HW_FOLD_DISPLAY_MODE = "hw_fold_display_mode";
    private static final String HW_FOLD_SCRREN_STATE = "hw_fold_screen_state";
    private static final int IDX_DISPLAY = 5;
    private static final int IDX_FOLD = 4;
    private static final int IDX_HEIGHT = 1;
    private static final int IDX_NOTCH = 6;
    private static final int IDX_WIDTH = 0;
    private static final int IDX_XRES = 2;
    private static final int IDX_YRES = 3;
    private static final double INCH_CM_FACTOR = 2.54d;
    private static final int NOTCHPROP_NUM = 4;
    private static final String PHONE_OUTGOING_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
    private static final String PHONE_STATE_ACTION = "android.intent.action.PHONE_STATE";
    private static final int PRODUCT_STR_SIZE = 3;
    private static final String TAG = "TrustedUIService";
    private static final int TUI_FOLD_FULL = 1;
    private static final int TUI_FOLD_SUB = 3;
    private static final int TUI_NEED_ROTATE = 256;
    public static final int TUI_POLL_FOLD = 26;
    private final Context mContext;
    private int mDisplayMode = 0;
    private int mFoldState = 0;
    private HwFoldScreenManagerInternal mFsmInternal;
    private boolean mIsFoldableScreen = false;
    private TUIEventListener mListener;
    private final String mNotchProp = SystemProperties.get("ro.config.hw_notch_size", "");
    private final String mProductModel = SystemProperties.get("ro.product.model", "");
    private final BroadcastReceiver mReceiver;
    private boolean mTUIStatus = false;
    private TelephonyManager mTelephonyManager;
    public int[] screenInfo = new int[7];

    private native int nativeSendTUICmd(int i, int i2, int[] iArr);

    private native void nativeSendTUIExitCmd();

    private native void nativeTUILibraryDeInit();

    private native boolean nativeTUILibraryInit();

    public TrustedUIService(Context context) {
        this.mContext = context;
        this.mListener = new TUIEventListener(this, context);
        new Thread(this.mListener, TUIEventListener.class.getName()).start();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PHONE_STATE_ACTION);
        filter.addAction(PHONE_OUTGOING_ACTION);
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.TrustedUIService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TrustedUIService.TAG, " Broadcast Receiver: " + action);
                if (action != null && !action.equals(TrustedUIService.PHONE_OUTGOING_ACTION)) {
                    TrustedUIService.this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
                    if (TrustedUIService.this.mTelephonyManager.getCallState() == 1) {
                        Log.d(TrustedUIService.TAG, "Phone incoming status action received, mTUIStatus: " + TrustedUIService.this.mTUIStatus);
                        if (TrustedUIService.this.mTUIStatus) {
                            TrustedUIService.this.mTUIStatus = false;
                            TrustedUIService.this.sendTUIExitCmd();
                        }
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(HW_FOLD_DISPLAY_MODE), true, new ContentObserver(new Handler()) {
            /* class com.android.server.TrustedUIService.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                Settings.Secure.getIntForUser(TrustedUIService.this.mContext.getContentResolver(), TrustedUIService.HW_FOLD_DISPLAY_MODE, 1, -2);
                TrustedUIService.this.getScreenSize();
                TrustedUIService trustedUIService = TrustedUIService.this;
                trustedUIService.sendTUICmd(26, 0, trustedUIService.screenInfo);
                if (TrustedUIService.this.mTUIStatus) {
                    TrustedUIService.this.sendTUIExitCmd();
                }
            }
        }, -1);
    }

    public void setTrustedUIStatus(boolean status) {
        Log.d(TAG, " set tui status: " + status);
        this.mTUIStatus = status;
    }

    public boolean getTrustedUIStatus() {
        Log.d(TAG, " getTrustedUIStatus: " + this.mTUIStatus);
        if (Binder.getCallingUid() == 1000) {
            return this.mTUIStatus;
        }
        throw new SecurityException("getTrustedUIStatus should only be called by TrustedUIService");
    }

    public void sendTUIExitCmd() {
        if (Binder.getCallingUid() == 1000) {
            nativeSendTUIExitCmd();
            return;
        }
        throw new SecurityException("send exit tui cmd should only be called by TrustedUIService");
    }

    public int sendTUICmd(int eventType, int value, int[] screenInfo2) {
        if (Binder.getCallingUid() == 1000) {
            if ((eventType == 26 && value == 0 && screenInfo2 != null) ? false : true) {
                Log.d(TAG, "invalid parmaters for jni");
                return -1;
            }
            int ret = nativeSendTUICmd(eventType, value, screenInfo2);
            Log.d(TAG, " sendTUICmd: eventType=" + eventType + " value=" + value + " ret=" + ret);
            return ret;
        }
        throw new SecurityException("sendTUICmd should only be called by TrustedUIService");
    }

    public boolean TUIServiceLibraryInit() {
        return nativeTUILibraryInit();
    }

    public void getScreenSize() {
        int i;
        Point displaySize = new Point();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (this.mContext.getSystemService("window") instanceof WindowManager) {
            WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            windowManager.getDefaultDisplay().getRealSize(displaySize);
        } else {
            Log.e(TAG, "window manager handle get failed");
        }
        this.mFoldState = getFoldState();
        this.mDisplayMode = getScreenState();
        correctDispValue();
        int screenWidth = displaySize.x;
        int screenHeight = displaySize.y;
        if (HwFoldScreenState.isFoldScreenDevice() && (i = this.mDisplayMode) != -1) {
            Rect subDisplayRect = HwFoldScreenState.getScreenPhysicalRect(i);
            screenWidth = subDisplayRect.width();
            screenHeight = subDisplayRect.height();
        }
        this.screenInfo[0] = (int) ((((double) screenWidth) * INCH_CM_FACTOR) / ((double) displayMetrics.xdpi));
        this.screenInfo[1] = (int) ((((double) screenHeight) * INCH_CM_FACTOR) / ((double) displayMetrics.ydpi));
        int[] iArr = this.screenInfo;
        iArr[2] = screenWidth;
        iArr[3] = screenHeight;
        iArr[4] = this.mFoldState;
        if (isDisplayNeedRotate()) {
            this.screenInfo[4] = this.mFoldState + 256;
        }
        correctDispValue();
        correctFoldValue();
        int[] iArr2 = this.screenInfo;
        iArr2[5] = this.mDisplayMode;
        iArr2[6] = getDisplayNotch();
        Log.e(TAG, "width: " + this.screenInfo[0] + " cm height: " + this.screenInfo[1] + " cm xres " + this.screenInfo[2] + " yres " + this.screenInfo[3] + " fold " + this.screenInfo[4] + " dis " + this.screenInfo[5] + " notch " + this.screenInfo[6]);
    }

    private int getDisplayNotch() {
        if ((HwFoldScreenState.isFoldScreenDevice() && HwFoldScreenState.isInwardFoldDevice() && getScreenState() != 2) || TextUtils.isEmpty(this.mNotchProp)) {
            return 0;
        }
        String[] params = this.mNotchProp.split(",");
        int length = params.length;
        if (length < 4) {
            Log.e(TAG, "hw_notch_size conifg error");
            return 0;
        }
        int[] notchArray = new int[length];
        for (int i = 0; i < length; i++) {
            notchArray[i] = Integer.parseInt(params[i]);
        }
        return notchArray[1];
    }

    private boolean isDisplayNeedRotate() {
        if (this.mProductModel.length() < 3) {
            return false;
        }
        if (this.mProductModel.substring(0, 3).compareTo("TAH") == 0) {
            return true;
        }
        if (this.mProductModel.substring(0, 3).compareTo("TET") == 0 && getScreenState() == 1) {
            return true;
        }
        return false;
    }

    private int getFoldState() {
        this.mIsFoldableScreen = HwFoldScreenState.isFoldScreenDevice();
        if (!this.mIsFoldableScreen) {
            return 0;
        }
        if (this.mFsmInternal == null) {
            this.mFsmInternal = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
        HwFoldScreenManagerInternal hwFoldScreenManagerInternal = this.mFsmInternal;
        if (hwFoldScreenManagerInternal != null) {
            this.mFoldState = hwFoldScreenManagerInternal.getFoldableState();
        } else {
            Log.e(TAG, "get HwFoldScreenManagerInternal failed !\n");
            this.mFoldState = -1;
        }
        return this.mFoldState;
    }

    private int getScreenState() {
        this.mIsFoldableScreen = HwFoldScreenState.isFoldScreenDevice();
        if (!this.mIsFoldableScreen) {
            return 0;
        }
        if (this.mFsmInternal == null) {
            if (LocalServices.getService(HwFoldScreenManagerInternal.class) instanceof HwFoldScreenManagerInternal) {
                this.mFsmInternal = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
            } else {
                Log.e(TAG, "get screen manager internal failed\n");
            }
        }
        HwFoldScreenManagerInternal hwFoldScreenManagerInternal = this.mFsmInternal;
        if (hwFoldScreenManagerInternal != null) {
            this.mDisplayMode = hwFoldScreenManagerInternal.getDisplayMode();
            if (this.mDisplayMode <= 0) {
                Log.e(TAG, "getDisplayMode return error " + this.mDisplayMode);
                this.mDisplayMode = -1;
            }
        } else {
            Log.e(TAG, "get service HwFoldScreenManagerInternal failed !\n");
            this.mDisplayMode = -1;
        }
        return this.mDisplayMode;
    }

    private void correctDispValue() {
        if (this.mProductModel.length() >= 3 && this.mProductModel.substring(0, 3).compareTo("TET") == 0 && this.mFoldState == 1 && this.mDisplayMode != 1) {
            this.mDisplayMode = 1;
        }
    }

    private void correctFoldValue() {
        int i;
        if (this.mProductModel.length() >= 3 && this.mProductModel.substring(0, 3).compareTo("TET") == 0 && (i = this.mFoldState) == 1) {
            this.screenInfo[4] = i + 256;
        }
    }
}
