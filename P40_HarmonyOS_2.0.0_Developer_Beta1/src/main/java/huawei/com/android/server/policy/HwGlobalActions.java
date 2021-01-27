package huawei.com.android.server.policy;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hdm.HwDeviceManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.BatteryManagerInternal;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Toast;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.HwAutoUpdate;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.policy.GlobalActionsProvider;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.IHwShutdownThread;
import com.huawei.android.app.HiEventEx;
import com.huawei.android.app.HiViewEx;
import com.huawei.android.app.HwAlarmManager;
import com.huawei.android.view.ViewEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.aod.AodThemeConst;
import com.huawei.iimagekit.blur.BlurAlgorithm;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;
import huawei.com.android.server.policy.HwGlobalActionsView;
import java.util.Locale;

public class HwGlobalActions implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener, HwGlobalActionsView.ActionPressedCallback {
    private static final String ACTION_NOTIFY_HIVOICE_HIDE_NEW = "com.huawei.hiassistantoversea.action.DEVICE_SHUTDOWN_SIGNAL";
    private static final String ACTION_NOTIFY_HIVOICE_HIDE_OLD = "com.huawei.vassistant.action.DEVICE_SHUTDOWN_SIGNAL";
    private static final String ACTION_NOTIFY_STATUSBAR_HIDE = "com.android.server.pc.action.DOCKBAR_HIDE";
    private static final String ACTION_NOTIFY_STATUSBAR_SHOW = "com.android.server.pc.action.DOCKBAR_SHOW";
    private static final String ACTION_UPDATE = "com.huawei.upgrade.action.SYSTEM_UI_UPDATE_BROADCAST";
    private static final String ALPHA_ANIMATOR = "alpha";
    private static final int AUTOROTATION_OPEN = 1;
    private static final int BASE_EVENT_ID = 991310000;
    private static final int DEFAULT_BITMAP_FILL_COLOR = -452984832;
    private static final int DEFAULT_BITMAP_WIDTH_AND_HEIGHT = 100;
    private static final int DEVICE_TYPE_TELEVISION = 2;
    private static final int DEVICE_TYPE_WATCH = 8;
    private static final int DISMISSS_DELAY_0 = 0;
    private static final int DISMISSS_DELAY_200 = 200;
    private static final String HIVOICE_PACAKGE_NEW = "com.huawei.hiassistantoversea";
    private static final String HIVOICE_PACAKGE_OLD = "com.huawei.vassistant";
    private static final int HWUE_POWERKEY_LONGPUSH = 21;
    private static final int HWUE_TOUCH_STATE = 22;
    private static final int INCOMINGCALL_DISMISS_VIEW_DELAY = 200;
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_DEBUG = Log.HWLog;
    private static final boolean IS_FASTSHUTDOWN_CONSIDER_SDCARD = true;
    private static final boolean IS_FASTSHUTDOWN_PROP = SystemProperties.getBoolean("ro.config.hw_emui_fastshutdown_mode", true);
    private static final boolean IS_INFO_DEBUG = Log.HWINFO;
    private static final String IS_LOCK_DOWN = "is_lock_down";
    private static final String IS_REBOOT = "is_reboot";
    private static final String IS_SCREEN_OFF = "is_screen_off";
    private static final String IS_SHUT_DOWN = "is_shut_down";
    private static final int MASK_PLANE_COLOR = -2146957304;
    private static final int MASK_PLANE_COLOR_TELEVISION = -452984832;
    private static final int MASK_PLANE_COLOR_WATCH = -1728053248;
    private static final int MAXLAYER = 159999;
    private static final int MAX_BLUR_RADIUS = 25;
    private static final int MESSAGE_DISMISS = 0;
    private static final int MESSAGE_SHOW = 1;
    private static final int MESSAGE_UPDATE_AIRPLANE_MODE = 2;
    private static final int MESSAGE_UPDATE_KEYKEYCOMBINATION_MODE = 8;
    private static final int MESSAGE_UPDATE_LOCKDOWN_MODE = 6;
    private static final int MESSAGE_UPDATE_REBOOT_MODE = 4;
    private static final int MESSAGE_UPDATE_SHUTDOWN_MODE = 5;
    private static final int MESSAGE_UPDATE_SILENT_MODE = 3;
    private static final int MINIMUM_SIZE = 50;
    private static final int MINLAYER = 0;
    private static final int MSG_SET_BLUR_BITMAP = 7;
    private static final int NO_REBOOT_CHARGE_FLAG = 1;
    private static final String PERMISSION_BROADCAST_GLOBALACTIONS_VIEW_STATE_CHANGED = "com.huawei.permission.GLOBALACTIONS_VIEW_STATE_CHANGED";
    private static final String PERMISSION_BROADCAST_NOTIFY_HIVOICE_HIDE_NEW = "com.huawei.hiassistantoversea.permission.SHUTDOWN_SIGNAL_SEND";
    private static final String PERMISSION_BROADCAST_NOTIFY_HIVOICE_HIDE_OLD = "com.huawei.vassistant.permission.SHUTDOWN_SIGNAL_SEND";
    private static final int REBOOT_ALPHA_ANIMATION_DURATION = 30000;
    private static final String REBOOT_TAG = "reboot";
    private static final int ROTATION = SystemProperties.getInt("ro.panel.hw_orientation", 0);
    private static final int ROTATION_DEFAULT = 0;
    private static final int ROTATION_NINETY = 90;
    private static final float SCALE = 0.1f;
    private static final String SCREENOFF_TAG = "screenoff";
    private static final int SHUTDOWN_ALPHA_ANIMATION_DURATION = 350;
    private static final String SHUTDOWN_TAG = "shutdown";
    private static final String STR_MODEL = "model";
    private static final String STR_MODEL_SCF = "screenOff";
    private static final String STR_MODEL_STY = "standBy";
    private static final String TAG = "HwGlobalActions";
    private static final int UPDATE_FLAG_ERROR = -1;
    private static final int UPDATE_FLAG_HAVE = 1;
    private static final String UPDATE_PERMISSION = "com.huawei.upgrade.permission.ACCESS_SYSTEM_UI_BROADCAST";
    private static IHwShutdownThread sHwShutdownThread = HwServiceFactory.getHwShutdownThread();
    private static boolean sIsCharging;
    private String mActionNotifyHivoiceHide = ACTION_NOTIFY_HIVOICE_HIDE_OLD;
    private ToggleAction mAirplaneModeAction;
    private ContentObserver mAirplaneModeObserver;
    private AudioManager mAudioManager;
    private BitmapDrawable mBlurDrawable;
    private int mBlurRadius;
    private BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private final IDreamManager mDreamManager;
    private final GlobalActionsProvider mGlobalActionsProvider;
    private HwGlobalActionsView mGlobalActionsView;
    private HwGlobalActionsData mGlobalactionsData;
    private Handler mHandler;
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private String mHivoicePackage = HIVOICE_PACAKGE_OLD;
    private HwPhoneWindowManager mHwPhoneWindowManager;
    private boolean mIsDeviceProvisioned;
    private boolean mIsHwFastShutdownEnable;
    private boolean mIsHwGlobalActionsShowing;
    private boolean mIsKeyguardSecure;
    private boolean mIsKeyguardShowing;
    private boolean mIsTelevisionMode;
    private boolean mIsWaitingForEcmExit;
    private boolean mIsWatchMode;
    private WindowManagerEx.LayoutParamsEx mLayoutParamsEx;
    private final Object mLock;
    private String mPermissionBroadcastNotifyHivoiceHide = PERMISSION_BROADCAST_NOTIFY_HIVOICE_HIDE_OLD;
    private PhoneStateListener mPhoneStateListener;
    private int mRadius;
    private BroadcastReceiver mRingerModeReceiver;
    private int mScale;
    private ToggleAction mSilentModeAction;
    private int mSystemRotation = -1;
    private BitmapThread mThread;
    private final WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;

    public interface Action {
        boolean onLongPress();

        void onPress();
    }

    public HwGlobalActions(Context context, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        boolean z = false;
        this.mLock = new Object[0];
        this.mPhoneStateListener = new PhoneStateListener() {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass1 */

            @Override // android.telephony.PhoneStateListener
            public void onServiceStateChanged(ServiceState serviceState) {
                if (HwGlobalActions.this.mHasTelephony) {
                    HwGlobalActions.this.mHandler.sendEmptyMessage(2);
                }
            }
        };
        this.mAirplaneModeObserver = new ContentObserver(new Handler()) {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean isSelfChange) {
                HwGlobalActions.this.mHandler.sendEmptyMessage(2);
            }
        };
        this.mHandler = new Handler() {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        HwGlobalActions.this.doHandlerMessageDismiss(msg);
                        return;
                    case 1:
                        HwGlobalActions.this.handleShow();
                        return;
                    case 2:
                        if (HwGlobalActions.this.mAirplaneModeAction != null) {
                            HwGlobalActions.this.mAirplaneModeAction.updateState();
                            return;
                        }
                        return;
                    case 3:
                        if (HwGlobalActions.this.mSilentModeAction != null) {
                            HwGlobalActions.this.mSilentModeAction.updateState();
                            return;
                        }
                        return;
                    case 4:
                        HwGlobalActions.this.doHandlerMessageUpdateRebootMode();
                        return;
                    case 5:
                        HwGlobalActions.this.doHandlerMessageUpdateShutdownMode();
                        return;
                    case 6:
                        HwGlobalActions.this.doHandlerMessageUpdateLockdownMode();
                        return;
                    case 7:
                        HwGlobalActions.this.doHandlerMessageBlurBitmap(msg);
                        return;
                    case 8:
                        HwGlobalActions.this.doHandlerMessageUpdateKeyCombinationMode();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                        String reason = "";
                        try {
                            reason = intent.getStringExtra("reason");
                        } catch (BadParcelableException e) {
                            Log.w(HwGlobalActions.TAG, "Bad target parcelable when calling onComplete.");
                        }
                        if (!"globalactions".equals(reason)) {
                            Log.w(HwGlobalActions.TAG, "onReceive, broad home key input!");
                            if (HwGlobalActions.this.mIsTelevisionMode) {
                                HwGlobalActions.this.mHandler.removeMessages(0);
                            }
                            HwGlobalActions.this.mHandler.sendEmptyMessage(0);
                        }
                    } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action)) {
                        try {
                            if (!intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && HwGlobalActions.this.mIsWaitingForEcmExit) {
                                HwGlobalActions.this.mIsWaitingForEcmExit = false;
                                HwGlobalActions.this.changeAirplaneModeSystemSetting(true);
                            }
                        } catch (BadParcelableException e2) {
                            Log.w(HwGlobalActions.TAG, "Bad target parcelable when calling onComplete.");
                        }
                    } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                        String state = "";
                        try {
                            state = intent.getStringExtra("state");
                        } catch (BadParcelableException e3) {
                            Log.w(HwGlobalActions.TAG, "Bad target parcelable when calling onComplete.");
                        }
                        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                            Log.w(HwGlobalActions.TAG, "onReceive, broad dismiss input!");
                            if (HwGlobalActions.this.mIsTelevisionMode) {
                                HwGlobalActions.this.mHandler.removeMessages(0);
                            }
                            HwGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 200);
                        }
                    }
                }
            }
        };
        this.mRingerModeReceiver = new BroadcastReceiver() {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass5 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwGlobalActions.logDebug("mRingerModeReceiver--intent.getAction = " + intent.getAction());
                if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction()) && HwGlobalActions.this.mSilentModeAction != null) {
                    HwGlobalActions.logDebug("mRingerModeReceiver--send silent_mode");
                    HwGlobalActions.this.mHandler.sendEmptyMessage(3);
                }
            }
        };
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mScale = 1;
        this.mRadius = 1;
        this.mIsTelevisionMode = false;
        this.mIsWatchMode = false;
        this.mBlurRadius = this.mContext.getResources().getInteger(34275420);
        if (this.mBlurRadius == 0) {
            this.mBlurRadius = 25;
        }
        initPkgName();
        this.mHwPhoneWindowManager = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        this.mGlobalActionsProvider = (GlobalActionsProvider) LocalServices.getService(GlobalActionsProvider.class);
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        filter.addAction("android.intent.action.PHONE_STATE");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 1);
        this.mHasTelephony = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        try {
            Uri uri = Settings.Global.getUriFor("airplane_mode_on");
            if (uri != null) {
                if (this.mAirplaneModeObserver != null) {
                    this.mContext.getContentResolver().registerContentObserver(uri, true, this.mAirplaneModeObserver);
                    Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
                    if (vibrator != null && vibrator.hasVibrator()) {
                        z = true;
                    }
                    this.mHasVibrator = z;
                    this.mContext.registerReceiver(this.mRingerModeReceiver, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
                    IntentFilter closeDialogFilter = new IntentFilter();
                    closeDialogFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                    this.mContext.registerReceiver(this.mBroadcastReceiver, closeDialogFilter, "android.permission.INJECT_EVENTS", null);
                }
            }
            Log.w(TAG, "HwGlobalActions: uri is null or airplaneModeObserver is null");
        } catch (NullPointerException e) {
            Log.e(TAG, "HwGlobalActions: getUriFor has exception");
        }
        Vibrator vibrator2 = (Vibrator) this.mContext.getSystemService("vibrator");
        z = true;
        this.mHasVibrator = z;
        this.mContext.registerReceiver(this.mRingerModeReceiver, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
        IntentFilter closeDialogFilter2 = new IntentFilter();
        closeDialogFilter2.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mContext.registerReceiver(this.mBroadcastReceiver, closeDialogFilter2, "android.permission.INJECT_EVENTS", null);
    }

    /* access modifiers changed from: private */
    public static void logDebug(String log) {
        if (IS_DEBUG) {
            Log.d(TAG, log);
        }
    }

    private static void logInfo(String log) {
        if (IS_INFO_DEBUG) {
            Log.i(TAG, log);
        }
    }

    private void sendUpdateBroadCast(String model) {
        Intent intent = new Intent(ACTION_UPDATE);
        try {
            intent.putExtra(STR_MODEL, model);
        } catch (BadParcelableException e) {
            Log.w(TAG, "Bad target parcelable when calling onComplete.");
        }
        Log.w(TAG, "sendUpdateBroadCast model=" + model);
        this.mContext.sendBroadcast(intent, UPDATE_PERMISSION);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x006d, code lost:
        if (r9 != null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x006f, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x007c, code lost:
        if (0 == 0) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0080, code lost:
        if (r10 != 1) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0089, code lost:
        if (huawei.com.android.server.policy.HwGlobalActions.STR_MODEL_STY.equals(r14) == false) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x008b, code lost:
        sendUpdateBroadCast(huawei.com.android.server.policy.HwGlobalActions.STR_MODEL_STY);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008f, code lost:
        sendUpdateBroadCast(huawei.com.android.server.policy.HwGlobalActions.STR_MODEL_SCF);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0095, code lost:
        return r10;
     */
    private int getAutoUpdateFlag(String reason) {
        Uri locationContentUri = Uri.parse("content://com.huawei.upgrade.provider" + AodThemeConst.SPLASH + "updateConfigTable");
        Cursor cursor = null;
        int autoUpdateFlag = -1;
        ContentResolver resolver = this.mContext.getContentResolver();
        if (resolver == null) {
            Log.e(TAG, "resolver is null !!!");
            return -1;
        }
        try {
            cursor = resolver.query(locationContentUri, null, null, null, null);
            Log.w(TAG, "Enter the method.");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    autoUpdateFlag = cursor.getInt(cursor.getColumnIndex("autoUpdateFlag"));
                }
            }
            Log.w(TAG, "The auto update flag value is: " + autoUpdateFlag);
        } catch (SQLiteException e) {
            Log.e(TAG, "Get the auto update flag value failure.");
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    public void showDialog(boolean isKeyguardShowing, boolean isKeyguardSecure, boolean isDeviceProvisioned) {
        GlobalActionsProvider globalActionsProvider = this.mGlobalActionsProvider;
        if (globalActionsProvider != null && globalActionsProvider.isGlobalActionsDisabled()) {
            logInfo("showDialog(): global actions disabled(device in Lock Task Mode)");
        } else if (this.mIsHwGlobalActionsShowing) {
            Log.w(TAG, "showDialog(): mIsHwGlobalActionsShowing = " + this.mIsHwGlobalActionsShowing);
        } else {
            if (!isDeviceProvisioned && ((UserManager) this.mContext.getSystemService("user")).getUserCount() > 1 && isOwnerUser()) {
                this.mIsDeviceProvisioned = true;
            }
            this.mIsKeyguardShowing = isKeyguardShowing;
            this.mIsKeyguardSecure = isKeyguardSecure;
            this.mIsDeviceProvisioned = isDeviceProvisioned;
            if (this.mContext.getResources().getInteger(34275393) == 2) {
                this.mIsTelevisionMode = true;
            }
            if (this.mContext.getResources().getInteger(34275393) == 8) {
                this.mIsWatchMode = true;
            }
            Log.w(TAG, "showDialog(): mIsTelevisionMode = " + this.mIsTelevisionMode);
            this.mHandler.sendEmptyMessage(1);
            sendGlobalActionsIntent(true);
        }
    }

    public void showKeyCombinationHint() {
        if (!this.mIsHwGlobalActionsShowing) {
            Log.w(TAG, "showKeyCombinationHint(): HwGlobalActionsView is not showing");
            return;
        }
        HwGlobalActionsView hwGlobalActionsView = this.mGlobalActionsView;
        if (hwGlobalActionsView == null) {
            Log.w(TAG, "showKeyCombinationHint(): GlobalActionsView is null");
        } else if (this.mGlobalactionsData == null) {
            Log.w(TAG, "showKeyCombinationHint: GlobalActionsData is null");
        } else if (hwGlobalActionsView.getPowerKeyRestartType() == 0) {
            Log.w(TAG, "showKeyCombinationHint(): poweroff with only power key");
        } else if (this.mGlobalactionsData.isKeyCombinationAlreadyShows()) {
            Log.w(TAG, "showKeyCombinationHint: key combination hint view already shows");
        } else {
            logInfo("showKeyCombinationHint: will show");
            this.mHandler.sendEmptyMessage(8);
        }
    }

    private void sendGlobalActionsIntent(boolean isShown) {
        if (this.mContext != null) {
            Intent globalActionsIntent = new Intent("com.android.systemui.action.GLOBAL_ACTION");
            try {
                globalActionsIntent.putExtra("isShown", isShown);
                globalActionsIntent.setPackage(HwCustPkgNameConstant.HW_SYSTEMUI_PACKAGE);
            } catch (BadParcelableException | IllegalArgumentException e) {
                Log.w(TAG, "Bad target parcelable or illegal argument exception = " + e.toString());
            }
            this.mContext.sendBroadcastAsUser(globalActionsIntent, UserHandle.CURRENT, "com.android.keyguard.FINGERPRINT_UNLOCK");
        }
    }

    private boolean isOwnerUser() {
        return ActivityManager.getCurrentUser() == 0;
    }

    private void awakenIfNecessary() {
        IDreamManager iDreamManager = this.mDreamManager;
        if (iDreamManager != null) {
            try {
                if (iDreamManager.isDreaming()) {
                    this.mDreamManager.awaken();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "awakenIfNecessary remoteException");
            }
        }
    }

    private void doHandleShowWork() {
        initGlobalActionsData(this.mIsKeyguardShowing, this.mIsKeyguardSecure, this.mIsDeviceProvisioned);
        initGlobalActions();
        createGlobalActionsView();
        if (this.mGlobalActionsView != null) {
            if (WindowManagerEx.getBlurFeatureEnabled()) {
                this.mGlobalActionsView.setBackgroundColor(MASK_PLANE_COLOR);
            } else {
                this.mGlobalActionsView.setBackground(this.mBlurDrawable);
            }
        }
        showGlobalActionsView();
        HiEventEx event = new HiEventEx(991310021);
        event.putAppInfo(this.mContext);
        HiViewEx.report(event);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShow() {
        awakenIfNecessary();
        if (WindowManagerEx.getBlurFeatureEnabled()) {
            doHandleShowWork();
        } else {
            startBlurScreenshotThread();
        }
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void onOtherAreaPressed() {
        String stateName = "dismiss";
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            Log.w(TAG, "onOtherAreaPressed(): mGlobalactionsData is null");
            return;
        }
        if (hwGlobalActionsData.isKeyCombinationAlreadyShows()) {
            dismissShutdownMenu(0);
            stateName = "dismiss_keycombination_hint";
        } else if ((this.mGlobalactionsData.getState() & 512) != 0) {
            this.mHandler.sendEmptyMessage(4);
            stateName = "cancel_reboot";
        } else if ((this.mGlobalactionsData.getState() & 8192) != 0) {
            this.mHandler.sendEmptyMessage(5);
            stateName = "cancel_shutdown";
        } else if ((this.mGlobalactionsData.getState() & 131072) != 0) {
            this.mHandler.sendEmptyMessage(6);
            stateName = "cancel_lockdown";
        } else {
            dismissShutdownMenu(0);
        }
        HiViewEx.report(HiViewEx.byContent(991310022, this.mContext, String.format(Locale.ROOT, "{action:touch_black, state:%s}", stateName)));
    }

    private void dismissShutdownMenuAfterLockdown() {
        if (ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
            Settings.System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
            this.mSystemRotation = -1;
        }
        if (ShutdownMenuAnimations.isSuperLiteMode()) {
            sendLockdownBundleMsg();
            return;
        }
        AnimatorSet dismissAnimSet = ShutdownMenuAnimations.getInstance(this.mContext).setShutdownMenuDismissAnim();
        dismissAnimSet.addListener(new Animator.AnimatorListener() {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass6 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(true);
                HwGlobalActions.this.sendLockdownBundleMsg();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
            }
        });
        dismissAnimSet.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLockdownBundleMsg() {
        Log.w(TAG, "sendLockdownBundleMsg");
        Bundle bundle = new Bundle();
        try {
            bundle.putBoolean(IS_LOCK_DOWN, true);
        } catch (BadParcelableException e) {
            Log.w(TAG, "Bad target parcelable when calling onComplete.");
        }
        Message msg = this.mHandler.obtainMessage(0);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void dismissShutdownMenu(int delayTime) {
        AnimatorSet exitAnimSet;
        if (ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
            Settings.System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
            this.mSystemRotation = -1;
        }
        if (ShutdownMenuAnimations.isSuperLiteMode()) {
            logDebug("dismissShutdownMenu super lite mode don't need animations.");
            this.mHandler.sendEmptyMessage(0);
        } else if (ShutdownMenuAnimations.getInstance(this.mContext).getIsAnimRunning()) {
            Log.w(TAG, "dismissShutdownMenu: animation is running and conflict");
        } else {
            HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
            if (hwGlobalActionsData == null) {
                Log.w(TAG, "dismissShutdownMenu(): mGlobalactionsData is null");
                return;
            }
            if (hwGlobalActionsData.isKeyCombinationAlreadyShows()) {
                exitAnimSet = ShutdownMenuAnimations.getInstance(this.mContext).setKeyCombinationHintViewAnimation(false, true);
            } else {
                exitAnimSet = ShutdownMenuAnimations.getInstance(this.mContext).setNewShutdownViewAnimation(false);
            }
            exitAnimSet.addListener(new Animator.AnimatorListener() {
                /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass7 */

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(true);
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animation) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    HwGlobalActions.this.onDismissAnimationEnd();
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                    ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
                }
            });
            if (delayTime > 0) {
                exitAnimSet.setStartDelay((long) delayTime);
            }
            exitAnimSet.start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDismissAnimationEnd() {
        ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(false);
        HwGlobalActionsView hwGlobalActionsView = this.mGlobalActionsView;
        if (hwGlobalActionsView != null) {
            hwGlobalActionsView.unregisterActionPressedCallback();
        } else {
            Log.w(TAG, "onDismissAnimationEnd, mGlobalActionsView is null");
        }
        if (this.mIsTelevisionMode) {
            this.mHandler.removeMessages(0);
        }
        this.mHandler.sendEmptyMessage(0);
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void onAirplaneModeActionPressed() {
        ToggleAction toggleAction = this.mAirplaneModeAction;
        if (toggleAction != null) {
            toggleAction.onPress();
        }
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void onSilentModeActionPressed() {
        ToggleAction toggleAction = this.mSilentModeAction;
        if (toggleAction != null) {
            toggleAction.onPress();
        }
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void onRebootActionPressed() {
        HiViewEx.report(HiViewEx.byContent(991310022, this.mContext, "{action:reboot, state:pressed}"));
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            return;
        }
        if ((hwGlobalActionsData.getState() & 512) == 0 && !this.mIsTelevisionMode) {
            this.mHandler.sendEmptyMessage(4);
        } else if (isOtherAreaPressedInTalkback()) {
            onOtherAreaPressed();
        } else {
            if (ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
                Settings.System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
                this.mSystemRotation = -1;
            }
            if (!this.mIsTelevisionMode) {
                ShutdownMenuAnimations.getInstance(this.mContext).startNewShutdownOrRebootAnim(true, false);
            }
            tryToRebootDevice();
        }
    }

    private void tryToRebootDevice() {
        if (!this.mIsTelevisionMode) {
            callRebootInterface();
        } else {
            dismissShutdownMenuWithAction(REBOOT_TAG, 34603151);
        }
        HiViewEx.report(HiViewEx.byContent(991310022, this.mContext, "{action:reboot, state:confrim}"));
        ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(true);
    }

    private void callRebootInterface() {
        try {
            IPowerManager powerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            if (powerManager != null) {
                if (HwAutoUpdate.getInstance().isAutoSystemUpdate(this.mContext, true)) {
                    Log.w(TAG, "callRebootInterface, PowerManager.REBOOT_RECOVERY");
                    powerManager.reboot(false, "recovery", false);
                    return;
                }
                Log.w(TAG, "callRebootInterface, huawei_reboot");
                powerManager.reboot(false, "huawei_reboot", false);
            }
        } catch (RemoteException exception) {
            Log.e(TAG, "PowerManager service died : " + exception.getMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToRebootTelevison(boolean isReboot) {
        if (!this.mIsTelevisionMode || !isReboot) {
            Log.w(TAG, "tryToRebootTelevison, not television mode, isReboot = " + isReboot);
            return;
        }
        Log.w(TAG, "tryToRebootTelevison, callRebootInterface!");
        HwGlobalActionsView hwGlobalActionsView = this.mGlobalActionsView;
        if (hwGlobalActionsView != null) {
            hwGlobalActionsView.setBackgroundColor(-16777216);
        }
        callRebootInterface();
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void onLockdownActionPressed() {
        logDebug("lock screen pressed!");
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            return;
        }
        if ((hwGlobalActionsData.getState() & 131072) == 0) {
            this.mHandler.sendEmptyMessage(6);
        } else if (isOtherAreaPressedInTalkback()) {
            onOtherAreaPressed();
        } else {
            if (ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
                Settings.System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
                this.mSystemRotation = -1;
            }
            logDebug("lockdown enter success!");
            dismissShutdownMenuAfterLockdown();
        }
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void onScreenoffActionPressed(boolean isPower) {
        logDebug("screenoff pressed!");
        if (isOtherAreaPressedInTalkback()) {
            onOtherAreaPressed();
            return;
        }
        logDebug("screenoff enter success!");
        if (ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
            Settings.System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
            this.mSystemRotation = -1;
        }
        if (isPower) {
            onPowerKeyDoubleClick();
        } else {
            dismissShutdownMenuWithAction(SCREENOFF_TAG, 34603483);
        }
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void onKeyCombinationActionStateChanged() {
        logDebug("onKeyCombinationActionStateChanged:");
        if (this.mGlobalactionsData == null) {
            Log.w(TAG, "onKeyCombinationActionStateChanged: mGlobalactionsData is null");
        } else {
            this.mHandler.sendEmptyMessage(8);
        }
    }

    private void onPowerKeyDoubleClick() {
        if (ShutdownMenuAnimations.getInstance(this.mContext).getIsAnimRunning()) {
            Log.w(TAG, "onPowerKeyDoubleClick: animation is running and conflict");
            return;
        }
        AnimatorSet exitAnimSet = ShutdownMenuAnimations.getInstance(this.mContext).setNewShutdownViewAnimation(false);
        exitAnimSet.addListener(new Animator.AnimatorListener() {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass8 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(true);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                Log.w(HwGlobalActions.TAG, "onPowerKeyDoubleClick: screnn off execute");
                HwGlobalActions.this.executeRelatedAction(HwGlobalActions.SCREENOFF_TAG);
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
                if (HwGlobalActions.this.mGlobalActionsView != null) {
                    HwGlobalActions.this.mGlobalActionsView.unregisterActionPressedCallback();
                } else {
                    Log.w(HwGlobalActions.TAG, "onPowerKeyDoubleClick, mGlobalActionsView is null");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
            }
        });
        exitAnimSet.start();
    }

    public static void SetShutdownFlag(int flag) {
        try {
            logDebug("shutdownThread: writeBootAnimShutFlag =" + flag);
            if (HwBootAnimationOeminfo.setBootChargeShutFlag(flag) != 0) {
                Log.e(TAG, "shutdownThread: writeBootAnimShutFlag error");
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "SetShutdownFlag RuntimeException");
        } catch (Exception e2) {
            Log.e(TAG, "SetShutdownFlag error");
        }
    }

    @Override // huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback
    public void onShutdownActionPressed(boolean isDeskClockClose, boolean isBootOnTimeClose, int listviewState) {
        if (HwDeviceManager.disallowOp(49)) {
            Toast toast = Toast.makeText(this.mContext, 33685904, 0);
            toast.getWindowParams().type = 2101;
            toast.getWindowParams().privateFlags |= 16;
            toast.show();
            return;
        }
        HiViewEx.report(HiViewEx.byContent(991310022, this.mContext, "{action:shutdown, state:pressed}"));
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            return;
        }
        if ((hwGlobalActionsData.getState() & 8192) == 0 && !this.mIsTelevisionMode) {
            this.mHandler.sendEmptyMessage(5);
        } else if (isOtherAreaPressedInTalkback()) {
            onOtherAreaPressed();
        } else {
            if (ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
                Settings.System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
                this.mSystemRotation = -1;
            }
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & 8192) != 0) {
                HwAlarmManager.adjustHwRTCAlarm(isDeskClockClose, isBootOnTimeClose, listviewState);
            }
            addAutomaticPowerOnAfterCharging();
            if (!this.mIsTelevisionMode) {
                checkFastShutdownCondition();
                ShutdownMenuAnimations.getInstance(this.mContext).startNewShutdownOrRebootAnim(false, this.mIsHwFastShutdownEnable);
            }
            tryToDoShutdown();
        }
    }

    private void addAutomaticPowerOnAfterCharging() {
        BatteryManagerInternal batteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
        if (batteryManagerInternal != null) {
            sIsCharging = batteryManagerInternal.isPowered(7);
            logDebug("the current sIsCharging = " + sIsCharging);
            if (sIsCharging) {
                SetShutdownFlag(1);
            }
        }
    }

    private void dismissShutdownMenuWithAction(final String layoutTags, int layoutId) {
        if (ShutdownMenuAnimations.getInstance(this.mContext).getIsAnimRunning()) {
            Log.w(TAG, "dismissShutdownMenuWithAction: animation is running and conflict");
            return;
        }
        AnimatorSet dismissAnimSet = ShutdownMenuAnimations.getInstance(this.mContext).getFocusConfirmEnterAnim(this.mContext, this.mIsTelevisionMode, layoutId);
        if (dismissAnimSet != null) {
            dismissAnimSet.addListener(new Animator.AnimatorListener() {
                /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass9 */

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(true);
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animation) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    HwGlobalActions.this.executeRelatedAction(layoutTags);
                    ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
                    if (HwGlobalActions.this.mGlobalActionsView != null) {
                        HwGlobalActions.this.mGlobalActionsView.unregisterActionPressedCallback();
                    } else {
                        Log.w(HwGlobalActions.TAG, "dismissShutdownMenuWithAction, mGlobalActionsView is null");
                    }
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                    ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
                }
            });
            dismissAnimSet.start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void executeRelatedAction(String layoutTags) {
        if (REBOOT_TAG.equals(layoutTags)) {
            Log.w(TAG, "executeRelatedAction reboot tags");
            sendDissmissMessageWithAction(IS_REBOOT);
        } else if (SHUTDOWN_TAG.equals(layoutTags)) {
            Log.w(TAG, "executeRelatedAction shutdown tags");
            sendDissmissMessageWithAction(IS_SHUT_DOWN);
        } else if (SCREENOFF_TAG.equals(layoutTags)) {
            Log.w(TAG, "executeRelatedAction screenoff tags");
            sendDissmissMessageWithAction(IS_SCREEN_OFF);
        } else {
            Log.w(TAG, "executeRelatedAction other tags = " + layoutTags);
        }
    }

    private void sendDissmissMessageWithAction(String tags) {
        Log.w(TAG, "sendDissmissMessageWithAction tags = " + tags);
        Bundle bundle = new Bundle();
        try {
            bundle.putBoolean(tags, true);
        } catch (BadParcelableException e) {
            Log.w(TAG, "Bad target parcelable when calling onComplete.");
        }
        Message msg = this.mHandler.obtainMessage(0);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToShutdownTeleVision(boolean isShutdown) {
        if (!this.mIsTelevisionMode || !isShutdown) {
            Log.w(TAG, "tryToShutdownTeleVision, not television mode, isShutdown = " + isShutdown);
        } else if (getAutoUpdateFlag(STR_MODEL_STY) == 1) {
            Log.w(TAG, "update have new version in shutdown.");
        } else {
            Log.w(TAG, "tryToShutdownTeleVision shutdown tags");
            callPowerManagerMethod(65536);
        }
    }

    private void tryToDoShutdown() {
        if (!this.mIsTelevisionMode) {
            try {
                IPowerManager powerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                if (powerManager != null) {
                    if (HwAutoUpdate.getInstance().isAutoSystemUpdate(this.mContext, false)) {
                        powerManager.reboot(false, "recovery", false);
                    } else {
                        this.mWindowManagerFuncs.shutdown(false);
                    }
                } else {
                    return;
                }
            } catch (RemoteException e) {
                this.mWindowManagerFuncs.shutdown(false);
                Log.e(TAG, "tryToDoShutdown PowerManager or isAutoSystemUpdate service died!");
            }
        } else {
            Log.w(TAG, "tryToDoShutdown with PowerManager goToSleep!");
            dismissShutdownMenuWithAction(SHUTDOWN_TAG, 34603152);
        }
        HiViewEx.report(HiViewEx.byContent(991310022, this.mContext, "{action:shutdown, state:confrim}"));
        ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(true);
    }

    private void callPowerManagerMethod(int flags) {
        Object powerObject = this.mContext.getSystemService("power");
        if (powerObject instanceof PowerManager) {
            ((PowerManager) powerObject).goToSleep(SystemClock.uptimeMillis(), 4, flags);
        } else {
            Log.e(TAG, "callPowerManagerMethod, PowerManager service died!");
        }
    }

    public boolean isHwFastShutdownEnable() {
        return this.mIsHwFastShutdownEnable;
    }

    private void checkFastShutdownCondition() {
        IHwShutdownThread iHwShutdownThread = sHwShutdownThread;
        boolean z = false;
        if (iHwShutdownThread == null || !iHwShutdownThread.isShutDownAnimationAvailable()) {
            if (IS_FASTSHUTDOWN_PROP && !isSdCardMounted()) {
                z = true;
            }
            this.mIsHwFastShutdownEnable = z;
        } else {
            this.mIsHwFastShutdownEnable = false;
        }
        logDebug("checkFastShutdownCondition: fastshutdown=" + this.mIsHwFastShutdownEnable + " ,sdcardmounted=" + isSdCardMounted());
    }

    private boolean isSdCardMounted() {
        StorageManager storageManager = (StorageManager) this.mContext.getSystemService("storage");
        if (storageManager == null) {
            return false;
        }
        StorageVolume[] volumeList = storageManager.getVolumeList();
        for (StorageVolume storageVolume : volumeList) {
            if (storageVolume.isRemovable() && !storageVolume.getPath().contains("usb") && "mounted".equals(storageManager.getVolumeState(storageVolume.getPath()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isOtherAreaPressedInTalkback() {
        HwGlobalActionsView hwGlobalActionsView = this.mGlobalActionsView;
        return hwGlobalActionsView != null && hwGlobalActionsView.isAccessibilityFocused();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialog) {
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialog, int which) {
    }

    public abstract class ToggleAction implements Action {
        public abstract void onToggle();

        public ToggleAction() {
        }

        @Override // huawei.com.android.server.policy.HwGlobalActions.Action
        public final void onPress() {
            if (isInTransition()) {
                Log.w(HwGlobalActions.TAG, "shouldn't be able to toggle when in transition");
                return;
            }
            onToggle();
            changeStateFromPress();
        }

        @Override // huawei.com.android.server.policy.HwGlobalActions.Action
        public boolean onLongPress() {
            return false;
        }

        /* access modifiers changed from: protected */
        public void changeStateFromPress() {
        }

        public void updateState() {
        }

        /* access modifiers changed from: protected */
        public boolean isInTransition() {
            return false;
        }
    }

    private void initGlobalActions() {
        if (this.mSilentModeAction == null) {
            this.mSilentModeAction = new ToggleAction() {
                /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass10 */

                @Override // huawei.com.android.server.policy.HwGlobalActions.ToggleAction
                public void onToggle() {
                    HwGlobalActions hwGlobalActions = HwGlobalActions.this;
                    hwGlobalActions.mAudioManager = hwGlobalActions.getAudioService(hwGlobalActions.mContext);
                    if (HwGlobalActions.this.mAudioManager == null) {
                        Log.e(HwGlobalActions.TAG, "AudioManager is null !!");
                    } else {
                        HwGlobalActions.this.tryToImplementToggle();
                    }
                }

                /* access modifiers changed from: protected */
                @Override // huawei.com.android.server.policy.HwGlobalActions.ToggleAction
                public boolean isInTransition() {
                    if ((HwGlobalActions.this.mGlobalactionsData.getState() & 128) != 0) {
                        return true;
                    }
                    return false;
                }

                /* access modifiers changed from: protected */
                @Override // huawei.com.android.server.policy.HwGlobalActions.ToggleAction
                public void changeStateFromPress() {
                    int state = HwGlobalActions.this.mGlobalactionsData.getState();
                    HwGlobalActions.logDebug("mSilentModeAction state = 0x" + Integer.toHexString(state));
                    int state2 = state | 128;
                    HwGlobalActions.logDebug("mSilentModeAction after state = 0x" + Integer.toHexString(state2));
                    HwGlobalActions.this.mGlobalactionsData.setSilentMode(state2);
                }

                @Override // huawei.com.android.server.policy.HwGlobalActions.ToggleAction
                public void updateState() {
                    HwGlobalActions.this.updateGlobalActionsSilentmodeState();
                }
            };
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToImplementToggle() {
        String targetStateName = "none";
        int ringerMode = this.mAudioManager.getRingerMode();
        if (ringerMode == 0) {
            logDebug("RINGER_MODE_SILENT ->RINGER_MODE_NORMAL");
            this.mAudioManager.setRingerMode(2);
            targetStateName = "normal";
        } else if (ringerMode != 1) {
            if (ringerMode == 2) {
                if (this.mHasVibrator) {
                    logDebug("RINGER_MODE_NORMAL ->RINGER_MODE_VIBRATE");
                    this.mAudioManager.setRingerMode(1);
                    targetStateName = "vibrate";
                } else {
                    logDebug("RINGER_MODE_NORMAL ->RINGER_MODE_SILENT");
                    this.mAudioManager.setRingerMode(0);
                    targetStateName = "silent";
                }
            }
        } else if (this.mHasVibrator) {
            logDebug("RINGER_MODE_VIBRATE ->RINGER_MODE_SILENT");
            this.mAudioManager.setRingerMode(0);
            targetStateName = "silent";
        }
        HiViewEx.report(HiViewEx.byContent(991310022, this.mContext, String.format(Locale.ROOT, "{action:sound, state:%s}", targetStateName)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateGlobalActionsSilentmodeState() {
        AudioManager audioManager = getAudioService(this.mContext);
        if (audioManager == null) {
            Log.e(TAG, "AudioManager is null !!");
            return;
        }
        int ringerMode = audioManager.getRingerMode();
        if (ringerMode == 0) {
            logDebug("updateGlobalActionsSilentmodeState -RINGER_MODE_SILENT");
            this.mGlobalactionsData.setSilentMode(16);
        } else if (ringerMode == 1) {
            logDebug("updateGlobalActionsSilentmodeState -RINGER_MODE_VIBRATE");
            this.mGlobalactionsData.setSilentMode(32);
        } else if (ringerMode == 2) {
            logDebug("updateGlobalActionsSilentmodeState -RINGER_MODE_NORMAL");
            this.mGlobalactionsData.setSilentMode(64);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AudioManager getAudioService(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (audioManager != null) {
            return audioManager;
        }
        return null;
    }

    private void updateGlobalActionsAirplanemodeState() {
        boolean isAirplaneModeOn = false;
        int i = 1;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
            isAirplaneModeOn = true;
        }
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (!isAirplaneModeOn) {
            i = 2;
        }
        hwGlobalActionsData.setAirplaneMode(i);
    }

    private void initGlobalActionsData(boolean isKeyguardShowing, boolean isKeyguardSecure, boolean isDeviceProvisioned) {
        this.mGlobalactionsData = HwGlobalActionsData.getSingletoneInstance();
        this.mGlobalactionsData.init(isKeyguardShowing, isKeyguardSecure, isDeviceProvisioned);
        updateGlobalActionsAirplanemodeState();
        updateGlobalActionsSilentmodeState();
        this.mGlobalactionsData.setRebootMode(256);
        this.mGlobalactionsData.setShutdownMode(4096);
        this.mGlobalactionsData.setLockdownMode(65536);
        this.mGlobalactionsData.setKeyCombinationMode(HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE);
    }

    private void adjustRingDisplaySideMode(WindowManager.LayoutParams params) {
        this.mLayoutParamsEx = new WindowManagerEx.LayoutParamsEx(params);
        if (this.mIsTelevisionMode) {
            this.mLayoutParamsEx.addHwFlags(Integer.MIN_VALUE);
        }
        this.mLayoutParamsEx.setDisplaySideMode(1);
    }

    private int createGlobalActionsViewFlag() {
        return 16909569;
    }

    private void setGlobalActionsViewLiteMode(WindowManager.LayoutParams params) {
        if (!ShutdownMenuAnimations.isSuperLiteMode()) {
            params.windowAnimations = this.mContext.getResources().getIdentifier("androidhwext:style/HwAnimation.GlobalActionsView", null, null);
        }
    }

    private void createGlobalActionsView() {
        int flags = createGlobalActionsViewFlag();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(-1, -1, 2009, flags, -2);
        params.privateFlags |= Integer.MIN_VALUE;
        params.layoutInDisplayCutoutMode = 1;
        adjustRingDisplaySideMode(params);
        setGlobalActionsViewLiteMode(params);
        if (ROTATION == 0) {
            params.screenOrientation = 5;
        }
        logDebug(" lp.screenOrientation = " + params.screenOrientation);
        params.setTitle(TAG);
        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
        HwGlobalActionsView hwGlobalActionsView = this.mGlobalActionsView;
        if (hwGlobalActionsView != null) {
            windowManager.removeView(hwGlobalActionsView);
        }
        View view = LayoutInflater.from(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null))).inflate(34013228, (ViewGroup) null);
        if (view instanceof HwGlobalActionsView) {
            this.mGlobalActionsView = (HwGlobalActionsView) view;
            params.screenOrientation = convertRotationToScreenOrientation(windowManager.getDefaultDisplay().getRotation());
            initGlobalActionsView();
            initBlurParameters();
            windowManager.addView(this.mGlobalActionsView, params);
            this.mGlobalActionsView.setSystemUiVisibility(flags | 4);
        }
    }

    private void initGlobalActionsView() {
        this.mGlobalActionsView.setContentDescription(this.mContext.getResources().getString(33686210));
        this.mGlobalActionsView.setVisibility(4);
        this.mGlobalActionsView.initUI(this.mHandler.getLooper());
        this.mGlobalActionsView.registerActionPressedCallback(this);
        if (!this.mIsTelevisionMode) {
            this.mGlobalActionsView.requestFocus();
        }
    }

    private void initBlurParameters() {
        if (WindowManagerEx.getBlurFeatureEnabled()) {
            this.mLayoutParamsEx.addHwFlags(33554432);
            this.mLayoutParamsEx.setBlurStyle(103);
            WindowManagerEx.setBlurMode(this.mGlobalActionsView, 1);
            ViewEx.setBlurEnabled(this.mGlobalActionsView, true);
        }
    }

    private int convertRotationToScreenOrientation(int rotation) {
        if (rotation == 0) {
            return 1;
        }
        if (rotation == 1) {
            return 0;
        }
        if (rotation == 2) {
            return 9;
        }
        if (rotation == 3) {
            return 8;
        }
        Log.w(TAG, "convertRotationToScreenOrientation: not expected rotation = " + rotation);
        return 1;
    }

    public void showGlobalActionsView() {
        if (this.mGlobalActionsView != null) {
            this.mHwPhoneWindowManager.handleCloseMobileViewChanged(true);
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                sendStatusbarHideNotification();
                HwPCUtils.log(TAG, "showGlobalActionsView()--show sendStatusbarHideNotification");
            }
            sendHiVoiceHideNotification();
            this.mGlobalActionsView.setVisibility(0);
            this.mIsHwGlobalActionsShowing = true;
            logInfo("show GlobalActionsView finish");
        }
    }

    public void hideGlobalActionsView(boolean isLockdown, boolean isScreenoff, boolean isShutdown, boolean isReboot) {
        if (this.mGlobalActionsView != null) {
            if (ShutdownMenuAnimations.isSuperLiteMode()) {
                logDebug("Super lite mode don't need animations. isLockdown " + isLockdown);
                tryToDoLockdown(isLockdown);
                tryToDoScrennoff(isScreenoff);
                tryToShutdownTeleVision(isShutdown);
                tryToRebootTelevison(isReboot);
                hideGlobalActionViewEnd();
            } else {
                ObjectAnimator alphaGlobalAction = getGlobalActionAnimator(isLockdown, isScreenoff, isShutdown, isReboot);
                if (alphaGlobalAction != null) {
                    alphaGlobalAction.start();
                } else {
                    Log.e(TAG, "hideGlobalActionsView Error!");
                    return;
                }
            }
        }
        ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(false);
        setSystemRotation();
        this.mHwPhoneWindowManager.handleCloseMobileViewChanged(false);
    }

    private void setSystemRotation() {
        if (ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
            Settings.System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
            this.mSystemRotation = -1;
        }
    }

    private ObjectAnimator getGlobalActionAnimator(final boolean isLockdown, final boolean isScreenoff, final boolean isShutdown, final boolean isReboot) {
        ObjectAnimator alphaGlobalActionAnimator = ObjectAnimator.ofFloat(this.mGlobalActionsView, ALPHA_ANIMATOR, 1.0f, 0.0f);
        boolean isDelayAnim = isScreenoff || isShutdown || isReboot;
        if (isLockdown || isDelayAnim) {
            alphaGlobalActionAnimator = ObjectAnimator.ofFloat(this.mGlobalActionsView, ALPHA_ANIMATOR, 1.0f, 1.0f);
            alphaGlobalActionAnimator.setInterpolator(ShutdownMenuAnimations.CUBIC_BEZIER_40_0);
            int duration = 200;
            if (isScreenoff || isShutdown) {
                duration = SHUTDOWN_ALPHA_ANIMATION_DURATION;
            }
            if (isReboot) {
                duration = 30000;
            }
            alphaGlobalActionAnimator.setDuration((long) duration);
        } else {
            alphaGlobalActionAnimator.setInterpolator(ShutdownMenuAnimations.CUBIC_BEZIER_20_20);
            alphaGlobalActionAnimator.setDuration(350L);
        }
        alphaGlobalActionAnimator.addListener(new Animator.AnimatorListener() {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass11 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                HwGlobalActions.this.tryToDoLockdown(isLockdown);
                HwGlobalActions.this.tryToDoScrennoff(isScreenoff);
                HwGlobalActions.this.tryToShutdownTeleVision(isShutdown);
                HwGlobalActions.this.tryToRebootTelevison(isReboot);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                HwGlobalActions.this.hideGlobalActionViewEnd();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
            }
        });
        return alphaGlobalActionAnimator;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToDoScrennoff(boolean isScreenoff) {
        if (!this.mIsTelevisionMode || !isScreenoff) {
            Log.w(TAG, "tryToDoScrennoff, not television mode, isScreenoff = " + isScreenoff);
        } else if (getAutoUpdateFlag(STR_MODEL_SCF) == 1) {
            Log.w(TAG, "update have new version in Screenoff.");
        } else {
            Log.w(TAG, "tryToDoScrennoff, start");
            callPowerManagerMethod(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToDoLockdown(boolean isLockDown) {
        if (isLockDown) {
            new LockPatternUtils(this.mContext).requireStrongAuth(32, -1);
            try {
                WindowManagerGlobal.getWindowManagerService().lockNow((Bundle) null);
            } catch (RemoteException e) {
                Log.e(TAG, "Error while trying to lock device.", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideGlobalActionViewEnd() {
        HwGlobalActionsView hwGlobalActionsView = this.mGlobalActionsView;
        if (hwGlobalActionsView != null) {
            hwGlobalActionsView.setBackgroundDrawable(null);
            this.mBlurDrawable = null;
            this.mGlobalActionsView.deinitUI();
            this.mGlobalActionsView.unregisterActionPressedCallback();
            if (this.mIsTelevisionMode) {
                Log.w(TAG, "hideGlobalActionViewEnd, execute cancelCountDownTimer");
                this.mGlobalActionsView.cancelCountDownTimer();
            }
            this.mGlobalActionsView.setVisibility(8);
            ((WindowManager) this.mContext.getSystemService("window")).removeView(this.mGlobalActionsView);
            this.mGlobalActionsView = null;
            sendGlobalActionsIntent(false);
            this.mIsHwGlobalActionsShowing = false;
        }
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            sendStatusbarShowNotification();
            HwPCUtils.log(TAG, "showGlobalActionsView()--hide sendStatusbarShowNotification");
        }
    }

    public boolean isHwGlobalActionsShowing() {
        return this.mIsHwGlobalActionsShowing;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeAirplaneModeSystemSetting(boolean isOn) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", isOn ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        try {
            intent.putExtra("state", isOn);
        } catch (BadParcelableException e) {
            Log.w(TAG, "Bad target parcelable when calling onComplete.");
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        dismissShutdownMenu(200);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHandlerMessageDismiss(Message msg) {
        boolean isLockdown = false;
        boolean isScreenoff = false;
        boolean isShutdown = false;
        boolean isReboot = false;
        Bundle bundle = msg.getData();
        if (bundle != null) {
            try {
                isLockdown = bundle.getBoolean(IS_LOCK_DOWN, false);
                isScreenoff = bundle.getBoolean(IS_SCREEN_OFF, false);
                isShutdown = bundle.getBoolean(IS_SHUT_DOWN, false);
                isReboot = bundle.getBoolean(IS_REBOOT, false);
            } catch (BadParcelableException e) {
                Log.w(TAG, "Bad target parcelable when calling onComplete.");
            }
        }
        logDebug("dismiss info: isLockdown = " + isLockdown + " isScreenoff = " + isScreenoff + " isShutdown = " + isShutdown + " isReboot = " + isReboot);
        hideGlobalActionsView(isLockdown, isScreenoff, isShutdown, isReboot);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHandlerMessageUpdateRebootMode() {
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            return;
        }
        if ((hwGlobalActionsData.getState() & 512) == 0) {
            enterSetImageAnimation();
            this.mGlobalactionsData.setRebootMode(512);
            return;
        }
        ShutdownMenuAnimations.getInstance(this.mContext).rebackNewShutdownMenu(this.mGlobalactionsData.getState());
        this.mGlobalactionsData.setRebootMode(256);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHandlerMessageUpdateShutdownMode() {
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            return;
        }
        if ((hwGlobalActionsData.getState() & 8192) == 0) {
            enterSetImageAnimation();
            this.mGlobalactionsData.setShutdownMode(8192);
            return;
        }
        ShutdownMenuAnimations.getInstance(this.mContext).rebackNewShutdownMenu(this.mGlobalactionsData.getState());
        this.mGlobalactionsData.setShutdownMode(4096);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHandlerMessageUpdateLockdownMode() {
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            return;
        }
        if ((hwGlobalActionsData.getState() & 131072) == 0) {
            enterSetImageAnimation();
            this.mGlobalactionsData.setLockdownMode(131072);
            return;
        }
        ShutdownMenuAnimations.getInstance(this.mContext).rebackNewShutdownMenu(this.mGlobalactionsData.getState());
        this.mGlobalactionsData.setLockdownMode(65536);
    }

    private void enterSetImageAnimation() {
        AnimatorSet enterAnimationSet = ShutdownMenuAnimations.getInstance(this.mContext).setImageAnimation(false);
        enterAnimationSet.addListener(new Animator.AnimatorListener() {
            /* class huawei.com.android.server.policy.HwGlobalActions.AnonymousClass12 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(true);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
                HwGlobalActions.this.showKeyCombinationWhenNeeds();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
                HwGlobalActions.this.showKeyCombinationWhenNeeds();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }
        });
        enterAnimationSet.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showKeyCombinationWhenNeeds() {
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            Log.w(TAG, "showKeyCombinationWhenNeeds: mGlobalactionsData is null");
        } else if (hwGlobalActionsData.isKeyCombinationEnterAnimationNeeds()) {
            logInfo("showKeyCombinationWhenNeeds: needs show, send message");
            this.mHandler.sendEmptyMessage(8);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHandlerMessageBlurBitmap(Message msg) {
        Bitmap currBitmap = null;
        this.mThread = null;
        if (msg.obj instanceof Bitmap) {
            Bitmap blurBitmap = (Bitmap) msg.obj;
            BitmapDrawable bitmapDrawable = this.mBlurDrawable;
            if (bitmapDrawable != null) {
                currBitmap = bitmapDrawable.getBitmap();
            }
            if (!(currBitmap == null || currBitmap == blurBitmap)) {
                currBitmap.recycle();
            }
            this.mBlurDrawable = new BitmapDrawable(this.mContext.getResources(), blurBitmap);
        }
        setDrawableBound();
        doHandleShowWork();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHandlerMessageUpdateKeyCombinationMode() {
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (hwGlobalActionsData == null) {
            Log.w(TAG, "doHandlerMessageUpdateKeyCombinationMode: mGlobalactionsData is null");
        } else {
            hwGlobalActionsData.setKeyCombinationMode(1048576);
        }
    }

    /* access modifiers changed from: private */
    public class BitmapThread extends Thread {
        BitmapThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            super.run();
            synchronized (HwGlobalActions.this.mLock) {
                if (!isInterrupted()) {
                    Bitmap screenShot = null;
                    if (ShutdownMenuAnimations.isSuperLiteMode() || HwGlobalActions.this.mIsTelevisionMode) {
                        Log.w(HwGlobalActions.TAG, "Phone is in super lite mode, use default color instead of screenshot,mIsTelevisionMode = " + HwGlobalActions.this.mIsTelevisionMode);
                    } else {
                        try {
                            screenShot = BlurUtils.screenShotBitmap(HwGlobalActions.this.mContext, 0, HwGlobalActions.MAXLAYER, HwGlobalActions.SCALE, new Rect());
                        } catch (ClassCastException e) {
                            Log.e(HwGlobalActions.TAG, "BlurUtils.screenShotBitmap() ClassCastException.");
                        } catch (Exception e2) {
                            Log.e(HwGlobalActions.TAG, "Screenshot Exception.");
                        } catch (Error err) {
                            Log.e(HwGlobalActions.TAG, "startBlurScreenshotThread  Error er = " + err.getMessage());
                        }
                    }
                    if (screenShot != null) {
                        int originHeight = (int) (((float) screenShot.getHeight()) / HwGlobalActions.SCALE);
                        screenShot = HwGlobalActions.this.scaleBlurBitmap(screenShot, (int) (((float) screenShot.getWidth()) / HwGlobalActions.SCALE), originHeight, true);
                    }
                    Bitmap screenShot2 = HwGlobalActions.this.screenShotPostProcessing(screenShot);
                    if (!isInterrupted()) {
                        HwGlobalActions.this.notifyBlurResult(screenShot2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyBlurResult(Bitmap bitmap) {
        Message msg = Message.obtain();
        msg.obj = bitmap;
        msg.what = 7;
        this.mHandler.sendMessage(msg);
        logInfo("end background blur");
    }

    private Bitmap getDefaultBitmap() {
        Bitmap defaultBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        int eraseColor = -452984832;
        if (this.mIsTelevisionMode) {
            eraseColor = -452984832;
        }
        defaultBitmap.eraseColor(eraseColor);
        return defaultBitmap;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap screenShotPostProcessing(Bitmap screenShot) {
        if (screenShot == null) {
            Log.e(TAG, "start screen shot fail,we fill it with a default color.");
            return getDefaultBitmap();
        }
        if (!screenShot.isMutable() || screenShot.getConfig() != Bitmap.Config.ARGB_8888) {
            Bitmap tmp = screenShot.copy(Bitmap.Config.ARGB_8888, true);
            screenShot.recycle();
            if (tmp == null) {
                return getDefaultBitmap();
            }
            screenShot = tmp;
        }
        Bitmap blurredBitmap = screenShot.copy(Bitmap.Config.ARGB_8888, true);
        if (this.mIsWatchMode) {
            blurredBitmap = watchBlur(screenShot);
        } else if (blurredBitmap == null) {
            return getDefaultBitmap();
        } else {
            BlurAlgorithm blurAlgorithm = new BlurAlgorithm();
            if (!isScreenBitmapValid(screenShot)) {
                return getDefaultBitmap();
            }
            blurAlgorithm.styleBlur(this.mContext, screenShot, blurredBitmap, 4);
        }
        Bitmap blurredBitmap2 = addBlackBoardToBlurBitmap(blurredBitmap);
        if (blurredBitmap2 != null) {
            return blurredBitmap2;
        }
        Log.e(TAG, "addBlackBoardToBlurBitmap return null, fill it with a default color.");
        return getDefaultBitmap();
    }

    private Bitmap watchBlur(Bitmap screenShot) {
        int originWidth = screenShot.getWidth();
        int originHeight = screenShot.getHeight();
        findRadius(this.mBlurRadius);
        int i = this.mScale;
        if (i == 0) {
            Log.e(TAG, "scaleBlurBitmap mScale is 0, fill it with a default color.");
            return getDefaultBitmap();
        }
        Bitmap screenShot2 = scaleBlurBitmap(screenShot, originWidth / i, originHeight / i, true);
        if (screenShot2 == null) {
            Log.e(TAG, "scaleBlurBitmap return null firstly, fill it with a default color.");
            return getDefaultBitmap();
        }
        BlurUtils.blurImage(this.mContext, screenShot2, screenShot2, this.mRadius);
        Bitmap screenShot3 = scaleBlurBitmap(screenShot2, originWidth, originHeight, true);
        if (screenShot3 != null) {
            return screenShot3;
        }
        Log.e(TAG, "scaleBlurBitmap return null secondly, fill it with a default color.");
        return getDefaultBitmap();
    }

    private boolean isScreenBitmapValid(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= 50 || height <= 50) {
            return false;
        }
        return true;
    }

    private void setDrawableBound() {
        if (this.mBlurDrawable != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
            this.mBlurDrawable.setBounds(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
    }

    private void startBlurScreenshotThread() {
        this.mThread = new BitmapThread();
        this.mThread.start();
        logInfo("start background blur");
    }

    private void sendStatusbarHideNotification() {
        Intent intent = new Intent();
        intent.setAction(ACTION_NOTIFY_STATUSBAR_HIDE);
        this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_GLOBALACTIONS_VIEW_STATE_CHANGED);
    }

    private void sendStatusbarShowNotification() {
        Intent intent = new Intent();
        intent.setAction(ACTION_NOTIFY_STATUSBAR_SHOW);
        this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_GLOBALACTIONS_VIEW_STATE_CHANGED);
    }

    private void sendHiVoiceHideNotification() {
        Intent intent = new Intent();
        intent.setAction(this.mActionNotifyHivoiceHide);
        intent.setPackage(this.mHivoicePackage);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, this.mPermissionBroadcastNotifyHivoiceHide);
    }

    private void findRadius(int blurRadius) {
        int i = this.mScale;
        int radius = blurRadius / i;
        if (radius < 25) {
            this.mRadius = radius;
            return;
        }
        this.mScale = i + 1;
        findRadius(blurRadius);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap scaleBlurBitmap(Bitmap src, int dstWidth, int dstHeight, boolean isFilter) {
        if (this.mBlurRadius < 25) {
            return src;
        }
        Bitmap screenSampling = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, isFilter);
        if (!src.isRecycled()) {
            src.recycle();
            return screenSampling;
        }
        Log.w(TAG, "Bitmap is isRecycled");
        return screenSampling;
    }

    private Bitmap addBlackBoardToBlurBitmap(Bitmap screenShot) {
        int blackBoard;
        Bitmap screenBitmap = null;
        if (this.mIsWatchMode) {
            blackBoard = MASK_PLANE_COLOR_WATCH;
        } else {
            blackBoard = MASK_PLANE_COLOR;
        }
        if (screenShot != null) {
            if (HwPicAverageNoises.isAverageNoiseSupported()) {
                screenBitmap = new HwPicAverageNoises().addNoiseWithBlackBoard(screenShot, blackBoard);
            } else {
                screenBitmap = BlurUtils.addBlackBoard(screenShot, blackBoard);
            }
            screenShot.recycle();
        }
        return screenBitmap;
    }

    private boolean isAppExist(Context context, String pkgName) {
        if (context == null || pkgName == null) {
            Log.e(TAG, "Parameter context or pkgName is invalid.");
            return false;
        }
        try {
            if (context.getPackageManager().getPackageInfo(pkgName, 0) != null) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager doesn't exist: " + pkgName);
        }
    }

    private void initPkgName() {
        if (isAppExist(this.mContext, HIVOICE_PACAKGE_NEW)) {
            this.mHivoicePackage = HIVOICE_PACAKGE_NEW;
            this.mActionNotifyHivoiceHide = ACTION_NOTIFY_HIVOICE_HIDE_NEW;
            this.mPermissionBroadcastNotifyHivoiceHide = PERMISSION_BROADCAST_NOTIFY_HIVOICE_HIDE_NEW;
            return;
        }
        this.mHivoicePackage = HIVOICE_PACAKGE_OLD;
        this.mActionNotifyHivoiceHide = ACTION_NOTIFY_HIVOICE_HIDE_OLD;
        this.mPermissionBroadcastNotifyHivoiceHide = PERMISSION_BROADCAST_NOTIFY_HIVOICE_HIDE_OLD;
    }
}
