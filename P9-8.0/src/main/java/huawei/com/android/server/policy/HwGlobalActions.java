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
import android.hdm.HwDeviceManager;
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
import android.provider.Settings.System;
import android.service.dreams.IDreamManager;
import android.service.dreams.IDreamManager.Stub;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.widget.Toast;
import com.android.server.HwAutoUpdate;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;
import huawei.com.android.server.policy.HwGlobalActionsView.ActionPressedCallback;
import huawei.com.android.server.policy.recsys.HwLog;
import huawei.com.android.server.policy.recsys.RecSysClient;

public class HwGlobalActions implements OnDismissListener, OnClickListener, ActionPressedCallback {
    private static final String ACTION_NOTIFY_STATUSBAR_HIDE = "com.android.server.pc.action.DOCKBAR_HIDE";
    private static final String ACTION_NOTIFY_STATUSBAR_SHOW = "com.android.server.pc.action.DOCKBAR_SHOW";
    private static final String ACTION_RECOMMEND_BROADCAST = "com.huawei.recsys.action.RECEIVE_EVENT";
    private static final String ACTION_TIMING_TASK_SETTINGS = "settings.huawei.intent.action.TIMING_TASK_SETTINGS";
    private static final int AUTOROTATION_OPEN = 1;
    private static final int BLUR_RADIUS = 18;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_BITMAP_FILL_COLOR = -872415232;
    private static final int DEFAULT_BITMAP_WIDTH_AND_HEIGHT = 100;
    private static final int DISMISSS_DELAY_0 = 0;
    private static final int DISMISSS_DELAY_200 = 200;
    private static final int DISMISS_RECOMMENDED = 1;
    private static final int DISMISS_REMIND = 0;
    private static final String EVENT_OPERATE_RECOMMEND = "Recommended";
    private static final String EVENT_OPERATE_REMIND = "Remind";
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
    private static final String PERMISSION_BROADCAST_GLOBALACTIONS_VIEW_STATE_CHANGED = "com.huawei.permission.GLOBALACTIONS_VIEW_STATE_CHANGED";
    private static final String RECOMMEND_BROADCAST_PERMISSION = "com.huawei.tips.permission.SHOW_TIPS";
    private static final String RECOMMEND_EVENT_ITEM_KEY = "eventItem";
    private static final String RECOMMEND_EVENT_ITEM_VALUE = "autoPowerOnOff";
    private static final String RECOMMEND_EVENT_OPERATE_KEY = "eventOperator";
    private static final int ROTATION_DEFAULT = 0;
    private static final int ROTATION_NINETY = 90;
    private static final float SCALE = 0.125f;
    private static final String TAG = "HwGlobalActions";
    private final int ROTATION = SystemProperties.getInt("ro.panel.hw_orientation", 0);
    private ToggleAction mAirplaneModeAction;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwGlobalActions.this.mHandler.sendEmptyMessage(2);
        }
    };
    private AudioManager mAudioManager;
    private BitmapDrawable mBlurDrawable;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                if (!"globalactions".equals(intent.getStringExtra("reason"))) {
                    HwGlobalActions.this.mHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action)) {
                if (!intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && HwGlobalActions.this.mIsWaitingForEcmExit) {
                    HwGlobalActions.this.mIsWaitingForEcmExit = false;
                    HwGlobalActions.this.changeAirplaneModeSystemSetting(true);
                }
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                if (TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra("state"))) {
                    HwGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 200);
                }
            }
        }
    };
    private final Context mContext;
    private final IDreamManager mDreamManager;
    private HwGlobalActionsView mGlobalActionsView;
    private HwGlobalActionsData mGlobalactionsData = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwGlobalActions.this.hideGlobalActionsView();
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
                    if (HwGlobalActions.this.mGlobalactionsData == null) {
                        return;
                    }
                    if ((HwGlobalActions.this.mGlobalactionsData.getState() & 131072) == 0) {
                        ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setImageAnimation(false).start();
                        HwGlobalActions.this.mGlobalactionsData.setRebootMode(131072);
                        return;
                    }
                    if (HwGlobalActions.this.isHwGlobalTwoActionsSupport()) {
                        ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).rebackNewShutdownMenu(true);
                    } else {
                        ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).rebackShutdownMenu(true);
                    }
                    HwGlobalActions.this.mGlobalactionsData.setRebootMode(65536);
                    return;
                case 5:
                    if (HwGlobalActions.this.mGlobalactionsData == null) {
                        return;
                    }
                    if ((HwGlobalActions.this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0) {
                        ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setImageAnimation(false).start();
                        HwGlobalActions.this.mGlobalactionsData.setShutdownMode(HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM);
                        return;
                    }
                    if (HwGlobalActions.this.isHwGlobalTwoActionsSupport()) {
                        ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).rebackNewShutdownMenu(false);
                    } else {
                        ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).rebackShutdownMenu(false);
                    }
                    HwGlobalActions.this.mGlobalactionsData.setShutdownMode(HwGlobalActionsData.FLAG_SHUTDOWN);
                    return;
                case 6:
                    HwGlobalActions.this.mThread = null;
                    Bitmap blurBitmap = msg.obj;
                    Bitmap currBitmap = HwGlobalActions.this.mBlurDrawable == null ? null : HwGlobalActions.this.mBlurDrawable.getBitmap();
                    if (!(currBitmap == null || currBitmap == blurBitmap)) {
                        currBitmap.recycle();
                    }
                    if (blurBitmap != null) {
                        HwGlobalActions.this.mBlurDrawable = new BitmapDrawable(HwGlobalActions.this.mContext.getResources(), blurBitmap);
                        HwGlobalActions.this.setDrawableBound();
                    }
                    HwGlobalActions.this.doHandleShowWork();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private boolean mHwGlobalActionsShowing;
    private boolean mIsDeviceProvisioned;
    private boolean mIsShowRecommend = false;
    private boolean mIsWaitingForEcmExit = false;
    private boolean mKeyguardSecure;
    private boolean mKeyguardShowing;
    final Object mLock = new Object[0];
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onServiceStateChanged(ServiceState serviceState) {
            if (HwGlobalActions.this.mHasTelephony) {
                HwGlobalActions.this.mHandler.sendEmptyMessage(2);
            }
        }
    };
    private final RecSysClient mRecSysClient;
    private BroadcastReceiver mRingerModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction()) && HwGlobalActions.this.mSilentModeAction != null) {
                HwGlobalActions.this.mHandler.sendEmptyMessage(3);
            }
        }
    };
    private ToggleAction mSilentModeAction;
    private int mSystemRotation = -1;
    private BitmapThread mThread;
    private final WindowManagerFuncs mWindowManagerFuncs;

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
            return false;
        }

        protected void changeStateFromPress() {
        }

        public void updateState() {
        }

        protected boolean isInTransition() {
            return false;
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
                    screenShot = BlurUtils.screenShotBitmap(HwGlobalActions.this.mContext, 0, HwGlobalActions.MAXLAYER, HwGlobalActions.SCALE, new Rect());
                } catch (Exception ex) {
                    ex.printStackTrace();
                } catch (Error err) {
                    Log.e(HwGlobalActions.TAG, "startBlurScreenshotThread  Error er = " + err.getMessage());
                }
                if (screenShot == null) {
                    Log.e(HwGlobalActions.TAG, "start screen shot fail,we fill it with a default color.");
                    Bitmap defaultBitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
                    defaultBitmap.eraseColor(HwGlobalActions.DEFAULT_BITMAP_FILL_COLOR);
                    HwGlobalActions.this.notifyBlurResult(defaultBitmap);
                    return;
                }
                Bitmap tmp;
                if (!(screenShot.isMutable() && screenShot.getConfig() == Config.ARGB_8888)) {
                    tmp = BlurUtils.covertToARGB888(screenShot);
                    screenShot.recycle();
                    screenShot = tmp;
                }
                BlurUtils.blurImage(HwGlobalActions.this.mContext, screenShot, screenShot, 18);
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

    public HwGlobalActions(Context context, WindowManagerFuncs windowManagerFuncs) {
        boolean z = false;
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mDreamManager = Stub.asInterface(ServiceManager.getService("dreams"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        filter.addAction("android.intent.action.PHONE_STATE");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 1);
        this.mHasTelephony = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (vibrator != null) {
            z = vibrator.hasVibrator();
        }
        this.mHasVibrator = z;
        this.mContext.registerReceiver(this.mRingerModeReceiver, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter3, "android.permission.INJECT_EVENTS", null);
        HwLog.d(TAG, "HwGlobalActions: bind recommend services");
        this.mRecSysClient = new RecSysClient(this.mContext);
        this.mRecSysClient.bindRecService();
    }

    public void showDialog(boolean keyguardShowing, boolean keyguardSecure, boolean isDeviceProvisioned) {
        if (!this.mHwGlobalActionsShowing) {
            if (!isDeviceProvisioned && ((UserManager) this.mContext.getSystemService("user")).getUserCount() > 1 && isOwnerUser()) {
                isDeviceProvisioned = true;
            }
            this.mKeyguardShowing = keyguardShowing;
            this.mKeyguardSecure = keyguardSecure;
            this.mIsDeviceProvisioned = isDeviceProvisioned;
            this.mHandler.sendEmptyMessage(1);
        }
    }

    private boolean isOwnerUser() {
        return ActivityManager.getCurrentUser() == 0;
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
        if ((this.mGlobalactionsData.getState() & 131072) != 0) {
            this.mHandler.sendEmptyMessage(4);
            stateName = "cancel_reboot";
        } else if ((this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) != 0) {
            this.mHandler.sendEmptyMessage(5);
            stateName = "cancel_shutdown";
        } else if (this.mGlobalActionsView == null || !this.mIsShowRecommend) {
            dismissShutdownMenu(0);
        } else {
            HwLog.d(TAG, "onOtherAreaPressed: The recommend view should be hidden when displayed");
            this.mGlobalActionsView.hideRecommendView(true);
            this.mIsShowRecommend = false;
            sendBroadcast(0);
        }
        StatisticalUtils.reporte(this.mContext, 22, String.format("{action:touch_black, state:%s}", new Object[]{stateName}));
    }

    public void dismissShutdownMenu(int delayTime) {
        AnimatorSet mExitSet;
        if (isHwGlobalTwoActionsSupport()) {
            mExitSet = ShutdownMenuAnimations.getInstance(this.mContext).setNewShutdownViewAnimation(false);
        } else {
            mExitSet = ShutdownMenuAnimations.getInstance(this.mContext).setImageAnimation(false);
        }
        mExitSet.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(true);
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
                HwGlobalActions.this.mHandler.sendEmptyMessage(0);
            }

            public void onAnimationCancel(Animator arg0) {
                ShutdownMenuAnimations.getInstance(HwGlobalActions.this.mContext).setIsAnimRunning(false);
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
        if (this.mGlobalActionsView != null && this.mIsShowRecommend) {
            this.mGlobalActionsView.hideRecommendView(false);
            this.mIsShowRecommend = false;
            sendBroadcast(0);
        }
        StatisticalUtils.reporte(this.mContext, 22, "{action:reboot, state:pressed}");
        if (this.mGlobalactionsData != null) {
            if ((this.mGlobalactionsData.getState() & 131072) == 0) {
                this.mHandler.sendEmptyMessage(4);
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
                        if (HwAutoUpdate.getInstance().isAutoSystemUpdate(this.mContext, true)) {
                            pm.reboot(false, "recovery", false);
                        } else {
                            pm.reboot(false, "huawei_reboot", false);
                        }
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
        if (this.mGlobalActionsView != null && this.mIsShowRecommend) {
            this.mGlobalActionsView.hideRecommendView(false);
            this.mIsShowRecommend = false;
            sendBroadcast(0);
        }
        if (HwDeviceManager.disallowOp(49)) {
            Toast toast = Toast.makeText(this.mContext, 33685904, 0);
            toast.getWindowParams().type = 2010;
            LayoutParams windowParams = toast.getWindowParams();
            windowParams.privateFlags |= 16;
            toast.show();
            return;
        }
        StatisticalUtils.reporte(this.mContext, 22, "{action:shutdown, state:pressed}");
        if (this.mGlobalactionsData != null) {
            if ((this.mGlobalactionsData.getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0) {
                this.mHandler.sendEmptyMessage(5);
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
                    ShutdownMenuAnimations.getInstance(this.mContext).startNewShutdownOrRebootAnim(false);
                } else {
                    if (this.mGlobalActionsView != null) {
                        this.mGlobalActionsView.showShutdongingHint();
                    }
                    ShutdownMenuAnimations.getInstance(this.mContext).startShutdownOrRebootAnim(false);
                }
                try {
                    IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                    if (pm != null) {
                        if (HwAutoUpdate.getInstance().isAutoSystemUpdate(this.mContext, false)) {
                            pm.reboot(false, "recovery", false);
                        } else {
                            this.mWindowManagerFuncs.shutdown(false);
                        }
                        StatisticalUtils.reporte(this.mContext, 22, "{action:shutdown, state:confrim}");
                        if (isHwGlobalTwoActionsSupport()) {
                            ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(true);
                        }
                    }
                } catch (RemoteException e) {
                    this.mWindowManagerFuncs.shutdown(false);
                    Log.e(TAG, "onShutdownActionPressed PowerManager service died!", e);
                }
            }
        }
    }

    public void onGoToSetActionPressed() {
        if (this.mGlobalActionsView == null) {
            HwLog.e(TAG, "GlobalActionsView is null when click go_to_set view");
            return;
        }
        HwLog.d(TAG, "onGoToSetActionPressed");
        this.mGlobalActionsView.hideRecommendView(false);
        this.mIsShowRecommend = false;
        sendBroadcast(1);
        Intent intent = new Intent();
        intent.setAction(ACTION_TIMING_TASK_SETTINGS);
        this.mContext.startActivity(intent);
        this.mHandler.sendEmptyMessage(0);
    }

    public void onDoNotNeedActionPressed() {
        if (this.mGlobalActionsView == null) {
            HwLog.e(TAG, "GlobalActionsView is null when click do_not_need view");
            return;
        }
        HwLog.d(TAG, "onDoNotNeedActionPressed");
        this.mGlobalActionsView.hideRecommendView(true);
        this.mIsShowRecommend = false;
        sendBroadcast(1);
    }

    public void onRecommendAreaPressed() {
    }

    private boolean isOtherAreaPressedInTalkback() {
        if (this.mGlobalActionsView == null || !this.mGlobalActionsView.isAccessibilityFocused()) {
            return false;
        }
        return true;
    }

    public void onDismiss(DialogInterface dialog) {
    }

    public void onClick(DialogInterface dialog, int which) {
    }

    private void initGlobalActions() {
        if (this.mAirplaneModeAction == null) {
            this.mAirplaneModeAction = new ToggleAction(this) {
                public void onToggle() {
                    String targetStateName = "none";
                    if (this.mHasTelephony && Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                        this.mIsWaitingForEcmExit = true;
                        Intent ecmDialogIntent = new Intent("com.android.internal.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                        ecmDialogIntent.addFlags(268435456);
                        this.mContext.startActivity(ecmDialogIntent);
                    } else {
                        boolean airplaneModeOn = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
                        this.changeAirplaneModeSystemSetting(airplaneModeOn ^ 1);
                        if (airplaneModeOn) {
                            targetStateName = "off";
                        } else {
                            targetStateName = PreciseIgnore.COMP_SCREEN_ON_VALUE_;
                        }
                    }
                    StatisticalUtils.reporte(this.mContext, 22, String.format("{action:airplane, state:%s}", new Object[]{targetStateName}));
                }

                protected boolean isInTransition() {
                    if ((this.mGlobalactionsData.getState() & 4) != 0) {
                        return true;
                    }
                    return false;
                }

                protected void changeStateFromPress() {
                    this.mGlobalactionsData.setAirplaneMode(this.mGlobalactionsData.getState() | 4);
                }

                public void updateState() {
                    this.updateGlobalActionsAirplanemodeState();
                }
            };
        }
        if (this.mSilentModeAction == null) {
            this.mSilentModeAction = new ToggleAction(this) {
                public void onToggle() {
                    this.mAudioManager = this.getAudioService(this.mContext);
                    if (this.mAudioManager == null) {
                        Log.e(HwGlobalActions.TAG, "AudioManager is null !!");
                        return;
                    }
                    String targetStateName = "none";
                    switch (this.mAudioManager.getRingerMode()) {
                        case 0:
                            this.mAudioManager.setRingerMode(2);
                            targetStateName = "normal";
                            break;
                        case 1:
                            if (this.mHasVibrator) {
                                this.mAudioManager.setRingerMode(0);
                                targetStateName = "silent";
                                break;
                            }
                            break;
                        case 2:
                            if (!this.mHasVibrator) {
                                this.mAudioManager.setRingerMode(0);
                                targetStateName = "silent";
                                break;
                            }
                            this.mAudioManager.setRingerMode(1);
                            targetStateName = "vibrate";
                            break;
                    }
                    StatisticalUtils.reporte(this.mContext, 22, String.format("{action:sound, state:%s}", new Object[]{targetStateName}));
                }

                protected boolean isInTransition() {
                    if ((this.mGlobalactionsData.getState() & 2048) != 0) {
                        return true;
                    }
                    return false;
                }

                protected void changeStateFromPress() {
                    this.mGlobalactionsData.setSilentMode(this.mGlobalactionsData.getState() | 2048);
                }

                public void updateState() {
                    this.updateGlobalActionsSilentmodeState();
                }
            };
        }
    }

    private void updateGlobalActionsSilentmodeState() {
        AudioManager mAudioManager = getAudioService(this.mContext);
        if (mAudioManager == null) {
            Log.e(TAG, "AudioManager is null !!");
            return;
        }
        switch (mAudioManager.getRingerMode()) {
            case 0:
                this.mGlobalactionsData.setSilentMode(256);
                break;
            case 1:
                this.mGlobalactionsData.setSilentMode(512);
                break;
            case 2:
                this.mGlobalactionsData.setSilentMode(1024);
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
        int i = 1;
        boolean airplaneModeOn = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        HwGlobalActionsData hwGlobalActionsData = this.mGlobalactionsData;
        if (!airplaneModeOn) {
            i = 2;
        }
        hwGlobalActionsData.setAirplaneMode(i);
    }

    private void initGlobalActionsData(boolean keyguardShowing, boolean keyguardSecure, boolean isDeviceProvisioned) {
        this.mGlobalactionsData = HwGlobalActionsData.getSingletoneInstance();
        this.mGlobalactionsData.init(keyguardShowing, keyguardSecure, isDeviceProvisioned);
        updateGlobalActionsAirplanemodeState();
        updateGlobalActionsSilentmodeState();
        this.mGlobalactionsData.setRebootMode(65536);
        this.mGlobalactionsData.setShutdownMode(HwGlobalActionsData.FLAG_SHUTDOWN);
    }

    private void createGlobalActionsView() {
        LayoutParams lp = new LayoutParams(-1, -1, 2009, 16909569, -2);
        lp.privateFlags |= Integer.MIN_VALUE;
        lp.windowAnimations = this.mContext.getResources().getIdentifier("androidhwext:style/HwAnimation.GlobalActionsView", null, null);
        if (this.ROTATION == 0) {
            lp.screenOrientation = 5;
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
            lp.screenOrientation = convertRotationToScreenOrientation(wm.getDefaultDisplay().getRotation());
        } else {
            this.mGlobalActionsView = (HwGlobalActionsView) inflater.inflate(34013224, null);
        }
        this.mGlobalActionsView.setContentDescription(this.mContext.getResources().getString(33685738));
        this.mGlobalActionsView.setVisibility(4);
        this.mGlobalActionsView.initUI(this.mHandler.getLooper());
        this.mGlobalActionsView.registerActionPressedCallback(this);
        this.mGlobalActionsView.requestFocus();
        wm.addView(this.mGlobalActionsView, lp);
        this.mGlobalActionsView.setSystemUiVisibility(16909573);
        if (ActivityManager.getCurrentUser() == 0) {
            HwLog.d(TAG, "createGlobalActionsView() current user is system");
            queryRecSysResult();
        }
    }

    private int convertRotationToScreenOrientation(int rotation) {
        switch (rotation) {
            case 0:
                return 1;
            case 1:
                return 0;
            case 2:
                return 9;
            case 3:
                return 8;
            default:
                Log.w(TAG, "convertRotationToScreenOrientation: not expected rotation = " + rotation);
                return 1;
        }
    }

    public void showGlobalActionsView() {
        if (this.mGlobalActionsView != null) {
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                sendStatusbarHideNotification();
                HwPCUtils.log(TAG, "showGlobalActionsView()--show sendStatusbarHideNotification");
            }
            this.mGlobalActionsView.setVisibility(0);
            this.mHwGlobalActionsShowing = true;
            if (this.mIsShowRecommend) {
                HwLog.d(TAG, "showGlobalActionsView()--show recommend view");
                this.mGlobalActionsView.showRecommendView();
            }
        }
    }

    public void hideGlobalActionsView() {
        if (this.mGlobalActionsView != null) {
            this.mIsShowRecommend = false;
            ObjectAnimator alpha_global_action = ObjectAnimator.ofFloat(this.mGlobalActionsView, "alpha", new float[]{1.0f, 0.0f});
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
                    if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                        HwGlobalActions.this.sendStatusbarShowNotification();
                        HwPCUtils.log(HwGlobalActions.TAG, "showGlobalActionsView()--hide sendStatusbarShowNotification");
                    }
                }

                public void onAnimationCancel(Animator arg0) {
                }
            });
            alpha_global_action.start();
            Bitmap currBitmap = this.mBlurDrawable == null ? null : this.mBlurDrawable.getBitmap();
            if (currBitmap != null) {
                currBitmap.recycle();
            }
        }
        this.mHwGlobalActionsShowing = false;
        ShutdownMenuAnimations.getInstance(this.mContext).setIsAnimRunning(false);
        if (this.ROTATION == ROTATION_NINETY && this.mSystemRotation >= 0) {
            System.putInt(this.mContext.getContentResolver(), "accelerometer_rotation", this.mSystemRotation);
            this.mSystemRotation = -1;
        }
    }

    public boolean isHwGlobalActionsShowing() {
        return this.mHwGlobalActionsShowing;
    }

    private void changeAirplaneModeSystemSetting(boolean on) {
        Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", on ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra("state", on);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        dismissShutdownMenu(200);
    }

    private final void notifyBlurResult(Bitmap bitmap) {
        Message msg = Message.obtain();
        msg.obj = bitmap;
        msg.what = 6;
        this.mHandler.sendMessage(msg);
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
    }

    public boolean isHwGlobalTwoActionsSupport() {
        return this.mContext.getResources().getBoolean(34537474);
    }

    private void queryRecSysResult() {
        HwLog.d(TAG, "queryRecSysResult");
        if (this.mRecSysClient == null) {
            HwLog.e(TAG, "RecSysClient is null ,can't query services");
            return;
        }
        this.mRecSysClient.requestRecRes(RecSysClient.BUSINESS_NAME);
        if (this.mRecSysClient.getRequestResult()) {
            this.mIsShowRecommend = true;
            this.mRecSysClient.resetRequestResult();
        } else {
            this.mIsShowRecommend = false;
        }
        HwLog.d(TAG, "queryRecSysResult(), query recommend result: " + this.mIsShowRecommend);
    }

    private void sendBroadcast(int dismissFlag) {
        HwLog.d(TAG, "sendBroadcast");
        Intent intent = new Intent(ACTION_RECOMMEND_BROADCAST);
        switch (dismissFlag) {
            case 0:
                intent.putExtra(RECOMMEND_EVENT_OPERATE_KEY, EVENT_OPERATE_REMIND);
                break;
            case 1:
                intent.putExtra(RECOMMEND_EVENT_OPERATE_KEY, EVENT_OPERATE_RECOMMEND);
                break;
        }
        intent.putExtra(RECOMMEND_EVENT_ITEM_KEY, RECOMMEND_EVENT_ITEM_VALUE);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, RECOMMEND_BROADCAST_PERMISSION);
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
}
