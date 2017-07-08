package huawei.com.android.server.policy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.service.dreams.IDreamManager;
import android.service.dreams.IDreamManager.Stub;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import com.android.internal.view.RotationPolicy;
import com.android.server.input.HwCircleAnimation;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.policy.EnableAccessibilityController;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;
import huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback;

public class HwGlobalActions implements OnDismissListener, OnClickListener, ActionPressedCallback {
    private static final int AUTOROTATION_OPEN = 1;
    private static final int BLUR_RADIUS = 18;
    private static final boolean DEBUG = false;
    private static final int DISMISSS_DELAY_0 = 0;
    private static final int DISMISSS_DELAY_200 = 200;
    private static final int INCOMINGCALL_DISMISS_VIEW_DELAY = 200;
    private static final int MAXLAYER = 159999;
    private static final int MESSAGE_DISMISS = 0;
    private static final int MESSAGE_SHOW = 1;
    private static final int MESSAGE_UPDATE_AIRPLANE_MODE = 2;
    private static final int MESSAGE_UPDATE_REBOOT_MODE = 4;
    private static final int MESSAGE_UPDATE_SHUTDOWN_MODE = 5;
    private static final int MESSAGE_UPDATE_SILENT_MODE = 3;
    private static final int MINLAYER = 0;
    private static final int MSG_SET_BLUR_BITMAP = 6;
    private static final int ROTATION_DEFAULT = 0;
    private static final int ROTATION_NINETY = 90;
    private static final float SCALE = 0.125f;
    private static final String TAG = "HwGlobalActions";
    private final int ROTATION;
    private ToggleAction mAirplaneModeAction;
    private ContentObserver mAirplaneModeObserver;
    private AudioManager mAudioManager;
    private BitmapDrawable mBlurDrawable;
    private BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private final IDreamManager mDreamManager;
    private EnableAccessibilityController mEnableAccessibilityController;
    private HwGlobalActionsView mGlobalActionsView;
    private HwGlobalActionsData mGlobalactionsData;
    private Handler mHandler;
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private boolean mHwGlobalActionsShowing;
    private boolean mIsDeviceProvisioned;
    private boolean mIsWaitingForEcmExit;
    private boolean mKeyguardSecure;
    private boolean mKeyguardShowing;
    final Object mLock;
    PhoneStateListener mPhoneStateListener;
    private BroadcastReceiver mRingerModeReceiver;
    private ToggleAction mSilentModeAction;
    private int mSystemRotation;
    private BitmapThread mThread;
    private final WindowManagerFuncs mWindowManagerFuncs;

    /* renamed from: huawei.com.android.server.policy.HwGlobalActions.4 */
    class AnonymousClass4 extends ContentObserver {
        AnonymousClass4(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            HwGlobalActions.this.mHandler.sendEmptyMessage(HwGlobalActions.MESSAGE_UPDATE_AIRPLANE_MODE);
        }
    }

    public interface Action {
        boolean onLongPress();

        void onPress();
    }

    public abstract class ToggleAction implements Action {
        public abstract void onToggle();

        public final void onPress() {
            if (isInTransition()) {
                Log.w(HwGlobalActions.TAG, "shouldn't be able to toggle when in transition");
                return;
            }
            onToggle();
            changeStateFromPress();
        }

        public boolean onLongPress() {
            return HwGlobalActions.DEBUG;
        }

        protected void changeStateFromPress() {
        }

        public void updateState() {
        }

        protected boolean isInTransition() {
            return HwGlobalActions.DEBUG;
        }
    }

    /* renamed from: huawei.com.android.server.policy.HwGlobalActions.7 */
    class AnonymousClass7 extends ToggleAction {
        final /* synthetic */ HwGlobalActions this$0;

        AnonymousClass7(HwGlobalActions this$0, HwGlobalActions this$0_1) {
            this.this$0 = this$0_1;
            super();
        }

        public void onToggle() {
            String targetStateName = "none";
            if (this.this$0.mHasTelephony && Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                this.this$0.mIsWaitingForEcmExit = true;
                Intent ecmDialogIntent = new Intent("android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                ecmDialogIntent.addFlags(268435456);
                this.this$0.mContext.startActivity(ecmDialogIntent);
            } else {
                boolean z;
                boolean airplaneModeOn = Global.getInt(this.this$0.mContext.getContentResolver(), "airplane_mode_on", HwGlobalActions.ROTATION_DEFAULT) == HwGlobalActions.MESSAGE_SHOW ? true : HwGlobalActions.DEBUG;
                HwGlobalActions hwGlobalActions = this.this$0;
                if (airplaneModeOn) {
                    z = HwGlobalActions.DEBUG;
                } else {
                    z = true;
                }
                hwGlobalActions.changeAirplaneModeSystemSetting(z);
                if (airplaneModeOn) {
                    targetStateName = "off";
                } else {
                    targetStateName = PreciseIgnore.COMP_SCREEN_ON_VALUE_;
                }
            }
            Context -get3 = this.this$0.mContext;
            Object[] objArr = new Object[HwGlobalActions.MESSAGE_SHOW];
            objArr[HwGlobalActions.ROTATION_DEFAULT] = targetStateName;
            StatisticalUtils.reporte(-get3, 22, String.format("{action:airplane, state:%s}", objArr));
        }

        protected boolean isInTransition() {
            if ((this.this$0.mGlobalactionsData.getState() & HwGlobalActions.MESSAGE_UPDATE_REBOOT_MODE) != 0) {
                return true;
            }
            return HwGlobalActions.DEBUG;
        }

        protected void changeStateFromPress() {
            this.this$0.mGlobalactionsData.setAirplaneMode(this.this$0.mGlobalactionsData.getState() | HwGlobalActions.MESSAGE_UPDATE_REBOOT_MODE);
        }

        public void updateState() {
            this.this$0.updateGlobalActionsAirplanemodeState();
        }
    }

    /* renamed from: huawei.com.android.server.policy.HwGlobalActions.8 */
    class AnonymousClass8 extends ToggleAction {
        final /* synthetic */ HwGlobalActions this$0;

        AnonymousClass8(HwGlobalActions this$0, HwGlobalActions this$0_1) {
            this.this$0 = this$0_1;
            super();
        }

        public void onToggle() {
            this.this$0.mAudioManager = this.this$0.getAudioService(this.this$0.mContext);
            if (this.this$0.mAudioManager == null) {
                Log.e(HwGlobalActions.TAG, "AudioManager is null !!");
                return;
            }
            String targetStateName = "none";
            switch (this.this$0.mAudioManager.getRingerMode()) {
                case HwGlobalActions.ROTATION_DEFAULT /*0*/:
                    this.this$0.mAudioManager.setRingerMode(HwGlobalActions.MESSAGE_UPDATE_AIRPLANE_MODE);
                    targetStateName = "normal";
                    break;
                case HwGlobalActions.MESSAGE_SHOW /*1*/:
                    if (this.this$0.mHasVibrator) {
                        this.this$0.mAudioManager.setRingerMode(HwGlobalActions.ROTATION_DEFAULT);
                        targetStateName = "silent";
                        break;
                    }
                    break;
                case HwGlobalActions.MESSAGE_UPDATE_AIRPLANE_MODE /*2*/:
                    if (!this.this$0.mHasVibrator) {
                        this.this$0.mAudioManager.setRingerMode(HwGlobalActions.ROTATION_DEFAULT);
                        targetStateName = "silent";
                        break;
                    }
                    this.this$0.mAudioManager.setRingerMode(HwGlobalActions.MESSAGE_SHOW);
                    targetStateName = "vibrate";
                    break;
            }
            Context -get3 = this.this$0.mContext;
            Object[] objArr = new Object[HwGlobalActions.MESSAGE_SHOW];
            objArr[HwGlobalActions.ROTATION_DEFAULT] = targetStateName;
            StatisticalUtils.reporte(-get3, 22, String.format("{action:sound, state:%s}", objArr));
        }

        protected boolean isInTransition() {
            if ((this.this$0.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_SILENTMODE_TRANSITING) != 0) {
                return true;
            }
            return HwGlobalActions.DEBUG;
        }

        protected void changeStateFromPress() {
            this.this$0.mGlobalactionsData.setSilentMode(this.this$0.mGlobalactionsData.getState() | HwGlobalActionsData.FLAG_SILENTMODE_TRANSITING);
        }

        public void updateState() {
            this.this$0.updateGlobalActionsSilentmodeState();
        }
    }

    private class BitmapThread extends Thread {
        public void run() {
            super.run();
            synchronized (HwGlobalActions.this.mLock) {
                if (isInterrupted()) {
                    return;
                }
                Bitmap screenShot = null;
                try {
                    screenShot = BlurUtils.screenShotBitmap(HwGlobalActions.this.mContext, HwGlobalActions.ROTATION_DEFAULT, HwGlobalActions.MAXLAYER, HwGlobalActions.SCALE, new Rect());
                } catch (Exception ex) {
                    ex.printStackTrace();
                } catch (Error err) {
                    Log.e(HwGlobalActions.TAG, "startBlurScreenshotThread  Error er = " + err.getMessage());
                }
                if (screenShot == null) {
                    Log.e(HwGlobalActions.TAG, "start screen shot fail,notify caller");
                    HwGlobalActions.this.notifyBlurResult(null);
                    return;
                } else if (screenShot == null) {
                    return;
                } else {
                    Bitmap tmp;
                    if (!(screenShot.isMutable() && screenShot.getConfig() == Config.ARGB_8888)) {
                        tmp = BlurUtils.covertToARGB888(screenShot);
                        screenShot.recycle();
                        screenShot = tmp;
                    }
                    BlurUtils.blurImage(HwGlobalActions.this.mContext, screenShot, screenShot, HwGlobalActions.BLUR_RADIUS);
                    if (screenShot != null) {
                        if (HwPicAverageNoises.isAverageNoiseSupported()) {
                            tmp = new HwPicAverageNoises().addNoiseWithBlackBoard(screenShot, -1728053248);
                        } else {
                            tmp = BlurUtils.addBlackBoard(screenShot, -1728053248);
                        }
                        screenShot.recycle();
                        screenShot = tmp;
                    }
                    if (isInterrupted()) {
                        return;
                    }
                    HwGlobalActions.this.notifyBlurResult(screenShot);
                    return;
                }
            }
        }
    }

    public HwGlobalActions(Context context, WindowManagerFuncs windowManagerFuncs) {
        boolean z = DEBUG;
        this.mIsWaitingForEcmExit = DEBUG;
        this.mGlobalactionsData = null;
        this.ROTATION = SystemProperties.getInt("ro.panel.hw_orientation", ROTATION_DEFAULT);
        this.mSystemRotation = -1;
        this.mLock = new Object[ROTATION_DEFAULT];
        this.mRingerModeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction()) && HwGlobalActions.this.mSilentModeAction != null) {
                    HwGlobalActions.this.mHandler.sendEmptyMessage(HwGlobalActions.MESSAGE_UPDATE_SILENT_MODE);
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                    if (!"globalactions".equals(intent.getStringExtra("reason"))) {
                        HwGlobalActions.this.mHandler.sendEmptyMessage(HwGlobalActions.ROTATION_DEFAULT);
                    }
                } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action)) {
                    if (!intent.getBooleanExtra("PHONE_IN_ECM_STATE", HwGlobalActions.DEBUG) && HwGlobalActions.this.mIsWaitingForEcmExit) {
                        HwGlobalActions.this.mIsWaitingForEcmExit = HwGlobalActions.DEBUG;
                        HwGlobalActions.this.changeAirplaneModeSystemSetting(true);
                    }
                } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                    if (TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra("state"))) {
                        HwGlobalActions.this.mHandler.sendEmptyMessageDelayed(HwGlobalActions.ROTATION_DEFAULT, 200);
                    }
                }
            }
        };
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState serviceState) {
                if (HwGlobalActions.this.mHasTelephony) {
                    HwGlobalActions.this.mHandler.sendEmptyMessage(HwGlobalActions.MESSAGE_UPDATE_AIRPLANE_MODE);
                }
            }
        };
        this.mAirplaneModeObserver = new AnonymousClass4(new Handler());
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Bitmap currBitmap = null;
                switch (msg.what) {
                    case HwGlobalActions.ROTATION_DEFAULT /*0*/:
                        HwGlobalActions.this.hideGlobalActionsView();
                    case HwGlobalActions.MESSAGE_SHOW /*1*/:
                        HwGlobalActions.this.handleShow();
                    case HwGlobalActions.MESSAGE_UPDATE_AIRPLANE_MODE /*2*/:
                        if (HwGlobalActions.this.mAirplaneModeAction != null) {
                            HwGlobalActions.this.mAirplaneModeAction.updateState();
                        }
                    case HwGlobalActions.MESSAGE_UPDATE_SILENT_MODE /*3*/:
                        if (HwGlobalActions.this.mSilentModeAction != null) {
                            HwGlobalActions.this.mSilentModeAction.updateState();
                        }
                    case HwGlobalActions.MESSAGE_UPDATE_REBOOT_MODE /*4*/:
                        if (HwGlobalActions.this.mGlobalactionsData == null) {
                            return;
                        }
                        if ((HwGlobalActions.this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_REBOOT_CONFIRM) == 0) {
                            ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setImageAnimation(HwGlobalActions.DEBUG).start();
                            HwGlobalActions.this.mGlobalactionsData.setRebootMode(HwGlobalActionsData.FLAG_REBOOT_CONFIRM);
                            return;
                        }
                        if (HwGlobalActions.this.isHwGlobalTwoActionsSupport()) {
                            ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).rebackNewShutdownMenu(true);
                        } else {
                            ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).rebackShutdownMenu(true);
                        }
                        HwGlobalActions.this.mGlobalactionsData.setRebootMode(HwGlobalActionsData.FLAG_REBOOT);
                    case HwGlobalActions.MESSAGE_UPDATE_SHUTDOWN_MODE /*5*/:
                        if (HwGlobalActions.this.mGlobalactionsData == null) {
                            return;
                        }
                        if ((HwGlobalActions.this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0) {
                            ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setImageAnimation(HwGlobalActions.DEBUG).start();
                            HwGlobalActions.this.mGlobalactionsData.setShutdownMode(HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM);
                            return;
                        }
                        if (HwGlobalActions.this.isHwGlobalTwoActionsSupport()) {
                            ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).rebackNewShutdownMenu(HwGlobalActions.DEBUG);
                        } else {
                            ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).rebackShutdownMenu(HwGlobalActions.DEBUG);
                        }
                        HwGlobalActions.this.mGlobalactionsData.setShutdownMode(HwGlobalActionsData.FLAG_SHUTDOWN);
                    case HwGlobalActions.MSG_SET_BLUR_BITMAP /*6*/:
                        HwGlobalActions.this.mThread = null;
                        Bitmap blurBitmap = msg.obj;
                        if (HwGlobalActions.this.mBlurDrawable != null) {
                            currBitmap = HwGlobalActions.this.mBlurDrawable.getBitmap();
                        }
                        if (!(currBitmap == null || currBitmap == blurBitmap)) {
                            currBitmap.recycle();
                        }
                        if (blurBitmap != null) {
                            HwGlobalActions.this.mBlurDrawable = new BitmapDrawable(HwGlobalActions.this.mContext.getResources(), blurBitmap);
                            HwGlobalActions.this.setDrawableBound();
                        }
                        HwGlobalActions.this.doHandleShowWork();
                    default:
                }
            }
        };
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mDreamManager = Stub.asInterface(ServiceManager.getService("dreams"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        filter.addAction("android.intent.action.PHONE_STATE");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, MESSAGE_SHOW);
        this.mHasTelephony = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(ROTATION_DEFAULT);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (vibrator != null) {
            z = vibrator.hasVibrator();
        }
        this.mHasVibrator = z;
        this.mContext.registerReceiver(this.mRingerModeReceiver, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
    }

    public void showDialog(boolean keyguardShowing, boolean keyguardSecure, boolean isDeviceProvisioned) {
        if (!this.mHwGlobalActionsShowing) {
            if (!isDeviceProvisioned && ((UserManager) this.mContext.getSystemService("user")).getUserCount() > MESSAGE_SHOW && isOwnerUser()) {
                isDeviceProvisioned = true;
            }
            this.mKeyguardShowing = keyguardShowing;
            this.mKeyguardSecure = keyguardSecure;
            this.mIsDeviceProvisioned = isDeviceProvisioned;
            this.mHandler.sendEmptyMessage(MESSAGE_SHOW);
        }
    }

    private boolean isOwnerUser() {
        return ActivityManager.getCurrentUser() == 0 ? true : DEBUG;
    }

    private void awakenIfNecessary() {
        if (this.mDreamManager != null) {
            try {
                if (this.mDreamManager.isDreaming()) {
                    this.mDreamManager.awaken();
                }
            } catch (RemoteException e) {
            }
        }
    }

    private void doHandleShowWork() {
        initGlobalActionsData(this.mKeyguardShowing, this.mKeyguardSecure, this.mIsDeviceProvisioned);
        initGlobalActions();
        createGlobalActionsView();
        if (this.mGlobalActionsView != null) {
            this.mGlobalActionsView.setBackgroundDrawable(this.mBlurDrawable);
        }
        showGlobalActionsView();
        StatisticalUtils.reportc(this.mContext, 21);
    }

    private void handleShow() {
        awakenIfNecessary();
        startBlurScreenshotThread();
    }

    public void onOtherAreaPressed() {
        String stateName = "dismiss";
        if ((this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_REBOOT_CONFIRM) != 0) {
            this.mHandler.sendEmptyMessage(MESSAGE_UPDATE_REBOOT_MODE);
            stateName = "cancel_reboot";
        } else if ((this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) != 0) {
            this.mHandler.sendEmptyMessage(MESSAGE_UPDATE_SHUTDOWN_MODE);
            stateName = "cancel_shutdown";
        } else {
            dismissShutdownMenu(ROTATION_DEFAULT);
        }
        Context context = this.mContext;
        Object[] objArr = new Object[MESSAGE_SHOW];
        objArr[ROTATION_DEFAULT] = stateName;
        StatisticalUtils.reporte(context, 22, String.format("{action:touch_black, state:%s}", objArr));
    }

    public void dismissShutdownMenu(int delayTime) {
        AnimatorSet mExitSet;
        if (isHwGlobalTwoActionsSupport()) {
            mExitSet = ShutdownMenuAnimations.getInstance(this.mContext).setNewShutdownViewAnimation(DEBUG);
        } else {
            mExitSet = ShutdownMenuAnimations.getInstance(this.mContext).setImageAnimation(DEBUG);
        }
        mExitSet.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(true);
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(HwGlobalActions.DEBUG);
                HwGlobalActions.this.mHandler.sendEmptyMessage(HwGlobalActions.ROTATION_DEFAULT);
            }

            public void onAnimationCancel(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(HwGlobalActions.DEBUG);
            }
        });
        if (delayTime > 0) {
            mExitSet.setStartDelay((long) delayTime);
        }
        mExitSet.start();
        if (this.ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
            System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
            this.mSystemRotation = -1;
        }
    }

    public void onAirplaneModeActionPressed() {
        if (this.mAirplaneModeAction != null) {
            this.mAirplaneModeAction.onPress();
        }
    }

    public void onSilentModeActionPressed() {
        if (this.mSilentModeAction != null) {
            this.mSilentModeAction.onPress();
        }
    }

    public void onRebootActionPressed() {
        StatisticalUtils.reporte(this.mContext, 22, "{action:reboot, state:pressed}");
        if (this.mGlobalactionsData != null) {
            if ((this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_REBOOT_CONFIRM) == 0) {
                this.mHandler.sendEmptyMessage(MESSAGE_UPDATE_REBOOT_MODE);
            } else if (isOtherAreaPressedInTalkback()) {
                onOtherAreaPressed();
            } else {
                if (this.ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
                    System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
                    this.mSystemRotation = -1;
                }
                if (isHwGlobalTwoActionsSupport()) {
                    ShutdownMenuAnimations.getInstance(this.mContext).startNewShutdownOrRebootAnim(true);
                } else {
                    if (this.mGlobalActionsView != null) {
                        this.mGlobalActionsView.showRestartingHint();
                    }
                    ShutdownMenuAnimations.getInstance(this.mContext).startShutdownOrRebootAnim(true);
                }
                try {
                    IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                    if (pm != null) {
                        pm.reboot(DEBUG, "huawei_reboot", DEBUG);
                        StatisticalUtils.reporte(this.mContext, 22, "{action:reboot, state:confrim}");
                        if (isHwGlobalTwoActionsSupport()) {
                            ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(true);
                        }
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "PowerManager service died!", e);
                }
            }
        }
    }

    public void onShutdownActionPressed(boolean isDeskClockClose, boolean isBootOnTimeClose, int listviewState) {
        StatisticalUtils.reporte(this.mContext, 22, "{action:shutdown, state:pressed}");
        if (this.mGlobalactionsData != null) {
            if ((this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0) {
                this.mHandler.sendEmptyMessage(MESSAGE_UPDATE_SHUTDOWN_MODE);
            } else if (isOtherAreaPressedInTalkback()) {
                onOtherAreaPressed();
            } else {
                if (this.ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
                    System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
                    this.mSystemRotation = -1;
                }
                if ((HwGlobalActionsData.getSingletoneInstance().getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) != 0) {
                    AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
                    if (alarmManager != null) {
                        alarmManager.adjustHwRTCAlarm(isDeskClockClose, isBootOnTimeClose, listviewState);
                    }
                }
                if (isHwGlobalTwoActionsSupport()) {
                    ShutdownMenuAnimations.getInstance(this.mContext).startNewShutdownOrRebootAnim(DEBUG);
                } else {
                    if (this.mGlobalActionsView != null) {
                        this.mGlobalActionsView.showShutdongingHint();
                    }
                    ShutdownMenuAnimations.getInstance(this.mContext).startShutdownOrRebootAnim(DEBUG);
                }
                this.mWindowManagerFuncs.shutdown(DEBUG);
                StatisticalUtils.reporte(this.mContext, 22, "{action:shutdown, state:confrim}");
                if (isHwGlobalTwoActionsSupport()) {
                    ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(true);
                }
            }
        }
    }

    private boolean isOtherAreaPressedInTalkback() {
        if (this.mGlobalActionsView == null || !this.mGlobalActionsView.isAccessibilityFocused()) {
            return DEBUG;
        }
        return true;
    }

    public void onDismiss(DialogInterface dialog) {
    }

    public void onClick(DialogInterface dialog, int which) {
    }

    private void initGlobalActions() {
        if (this.mAirplaneModeAction == null) {
            this.mAirplaneModeAction = new AnonymousClass7(this, this);
        }
        if (this.mSilentModeAction == null) {
            this.mSilentModeAction = new AnonymousClass8(this, this);
        }
    }

    private void updateGlobalActionsSilentmodeState() {
        AudioManager mAudioManager = getAudioService(this.mContext);
        if (mAudioManager == null) {
            Log.e(TAG, "AudioManager is null !!");
            return;
        }
        switch (mAudioManager.getRingerMode()) {
            case ROTATION_DEFAULT /*0*/:
                this.mGlobalactionsData.setSilentMode(HwGlobalActionsData.FLAG_SILENTMODE_SILENT);
                break;
            case MESSAGE_SHOW /*1*/:
                this.mGlobalactionsData.setSilentMode(HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE);
                break;
            case MESSAGE_UPDATE_AIRPLANE_MODE /*2*/:
                this.mGlobalactionsData.setSilentMode(HwGlobalActionsData.FLAG_SILENTMODE_NORMAL);
                break;
        }
    }

    private AudioManager getAudioService(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (audioManager != null) {
            return audioManager;
        }
        return null;
    }

    private void updateGlobalActionsAirplanemodeState() {
        int i = MESSAGE_SHOW;
        boolean airplaneModeOn = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", ROTATION_DEFAULT) == MESSAGE_SHOW ? true : DEBUG;
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (!airplaneModeOn) {
            i = MESSAGE_UPDATE_AIRPLANE_MODE;
        }
        hwGlobalActionsData.setAirplaneMode(i);
    }

    private void initGlobalActionsData(boolean keyguardShowing, boolean keyguardSecure, boolean isDeviceProvisioned) {
        this.mGlobalactionsData = HwGlobalActionsData.getSingletoneInstance();
        this.mGlobalactionsData.init(keyguardShowing, keyguardSecure, isDeviceProvisioned);
        updateGlobalActionsAirplanemodeState();
        updateGlobalActionsSilentmodeState();
        this.mGlobalactionsData.setRebootMode(HwGlobalActionsData.FLAG_REBOOT);
        this.mGlobalactionsData.setShutdownMode(HwGlobalActionsData.FLAG_SHUTDOWN);
    }

    private void createGlobalActionsView() {
        LayoutParams lp = new LayoutParams(-1, -1, 2009, 16909569, -2);
        lp.privateFlags |= Integer.MIN_VALUE;
        lp.windowAnimations = this.mContext.getResources().getIdentifier("androidhwext:style/HwAnimation.GlobalActionsView", null, null);
        if (this.ROTATION == 0) {
            lp.screenOrientation = MESSAGE_UPDATE_SHUTDOWN_MODE;
        } else if (this.ROTATION == ROTATION_NINETY) {
            try {
                this.mSystemRotation = System.getInt(this.mContext.getContentResolver(), "accelerometer_rotation");
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (this.mSystemRotation == MESSAGE_SHOW) {
                RotationPolicy.setRotationLock(this.mContext, true);
            }
        }
        Log.d(TAG, " lp.screenOrientation = " + lp.screenOrientation);
        lp.setTitle(TAG);
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        if (this.mGlobalActionsView != null) {
            wm.removeView(this.mGlobalActionsView);
            this.mGlobalActionsView = null;
        }
        LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null)));
        if (isHwGlobalTwoActionsSupport()) {
            this.mGlobalActionsView = (HwGlobalActionsView) inflater.inflate(34013228, null);
            if (this.mContext.getResources().getConfiguration().orientation == MESSAGE_UPDATE_AIRPLANE_MODE) {
                lp.screenOrientation = ROTATION_DEFAULT;
            } else {
                lp.screenOrientation = MESSAGE_SHOW;
            }
        } else {
            this.mGlobalActionsView = (HwGlobalActionsView) inflater.inflate(34013224, null);
        }
        this.mGlobalActionsView.setContentDescription(this.mContext.getResources().getString(33685731));
        this.mGlobalActionsView.setVisibility(MESSAGE_UPDATE_REBOOT_MODE);
        this.mGlobalActionsView.initUI(this.mHandler.getLooper());
        this.mGlobalActionsView.registerActionPressedCallback(this);
        this.mGlobalActionsView.requestFocus();
        wm.addView(this.mGlobalActionsView, lp);
        this.mGlobalActionsView.setSystemUiVisibility(16909573);
    }

    public void showGlobalActionsView() {
        if (this.mGlobalActionsView != null) {
            this.mGlobalActionsView.setVisibility(ROTATION_DEFAULT);
            this.mHwGlobalActionsShowing = true;
        }
        enableAccessibilityController();
    }

    public void hideGlobalActionsView() {
        Bitmap currBitmap = null;
        if (this.mGlobalActionsView != null) {
            float[] fArr = new float[MESSAGE_UPDATE_AIRPLANE_MODE];
            fArr[ROTATION_DEFAULT] = HwCircleAnimation.SMALL_ALPHA;
            fArr[MESSAGE_SHOW] = 0.0f;
            ObjectAnimator alpha_global_action = ObjectAnimator.ofFloat(this.mGlobalActionsView, "alpha", fArr);
            alpha_global_action.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
            alpha_global_action.setDuration(350);
            alpha_global_action.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator arg0) {
                }

                public void onAnimationRepeat(Animator arg0) {
                }

                public void onAnimationEnd(Animator arg0) {
                    if (HwGlobalActions.this.mGlobalActionsView != null) {
                        HwGlobalActions.this.mGlobalActionsView.setBackgroundDrawable(null);
                        HwGlobalActions.this.mGlobalActionsView.deinitUI();
                        HwGlobalActions.this.mGlobalActionsView.unregisterActionPressedCallback();
                        HwGlobalActions.this.mGlobalActionsView.setVisibility(8);
                        ((WindowManager) HwGlobalActions.this.mContext.getSystemService("window")).removeView(HwGlobalActions.this.mGlobalActionsView);
                        HwGlobalActions.this.mGlobalActionsView = null;
                    }
                }

                public void onAnimationCancel(Animator arg0) {
                }
            });
            alpha_global_action.start();
            if (this.mBlurDrawable != null) {
                currBitmap = this.mBlurDrawable.getBitmap();
            }
            if (currBitmap != null) {
                currBitmap.recycle();
            }
            if (this.mEnableAccessibilityController != null) {
                this.mEnableAccessibilityController.onDestroy();
            }
        }
        this.mHwGlobalActionsShowing = DEBUG;
        ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(DEBUG);
        if (this.ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
            System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
            this.mSystemRotation = -1;
        }
    }

    public boolean isHwGlobalActionsShowing() {
        return this.mHwGlobalActionsShowing;
    }

    private void changeAirplaneModeSystemSetting(boolean on) {
        Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", on ? MESSAGE_SHOW : ROTATION_DEFAULT);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra("state", on);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        dismissShutdownMenu(INCOMINGCALL_DISMISS_VIEW_DELAY);
    }

    private final void notifyBlurResult(Bitmap bitmap) {
        Message msg = Message.obtain();
        msg.obj = bitmap;
        msg.what = MSG_SET_BLUR_BITMAP;
        this.mHandler.sendMessage(msg);
    }

    private void setDrawableBound() {
        if (this.mBlurDrawable != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
            this.mBlurDrawable.setBounds(ROTATION_DEFAULT, ROTATION_DEFAULT, displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
    }

    private void startBlurScreenshotThread() {
        this.mThread = new BitmapThread();
        this.mThread.start();
    }

    private void enableAccessibilityController() {
        if (EnableAccessibilityController.canEnableAccessibilityViaGesture(this.mContext)) {
            this.mEnableAccessibilityController = new EnableAccessibilityController(this.mContext, new Runnable() {
                public void run() {
                    HwGlobalActions.this.hideGlobalActionsView();
                }
            });
            this.mGlobalActionsView.setEnableAccessibilityController(this.mEnableAccessibilityController);
        }
    }

    public boolean isHwGlobalTwoActionsSupport() {
        return this.mContext.getResources().getBoolean(34406402);
    }
}
