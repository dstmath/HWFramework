package com.android.server.policy;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManagerGlobal;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.LocalServices;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.RootActivityContainerEx;
import com.android.server.wm.TaskRecordEx;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.controlcenter.ui.service.IControlCenterGesture;
import com.huawei.hwdockbar.IDockAidlInterface;
import java.util.List;
import java.util.NoSuchElementException;

public class HwHandleKeyManager {
    private static final String AUTHORITY = "com.huawei.controlcenter.SwitchProvider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.huawei.controlcenter.SwitchProvider");
    private static final int BINDER_FLAG = 0;
    private static final String CHARACTERISTICS = SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT);
    private static final String CTRLCENTER_ACTION = "com.huawei.controlcenter.action.CONTROL_CENTER_GESTURE";
    private static final String CTRLCENTER_PKG = "com.huawei.controlcenter";
    private static final int CTRL_CENTER_CONNECT = 4;
    private static final int CTRL_CENTER_DISMISS = 3;
    private static final String DEFAULT_DOCK_AIDL_INTERFACE = "com.huawei.hwdockbar.IDockAidlInterface";
    private static final String DEFAULT_DOCK_MAIN_CLASS = "com.huawei.hwdockbar.DockMainService";
    private static final String DEFAULT_DOCK_PACKAGE = "com.huawei.hwdockbar";
    private static final int DOCK_CONNECT = 2;
    private static final int DOCK_DISMISS = 1;
    private static final String DOCK_SHOW_CLOSE_ACTION = "com.huawei.hwdockbar.dock.operation.action";
    private static final String DOCK_SHOW_CLOSE_PER = "com.huawei.hwdockbar.permission.START_DOCK";
    private static final int EMUI_11_0 = 25;
    private static final int EMUI_VERSION = SystemProperties.getInt("ro.build.hw_emui_api_level", 0);
    private static final int ID_SHOW_DOCK_LAST_POSITION = 0;
    private static final long INIT_DELAY_TIME = 10000;
    private static final String ISCONTROLCENTERENABLE = "isControlCenterEnable";
    private static final String ISSWITCHON = "isSwitchOn";
    private static final boolean IS_TABLET = "tablet".equals(CHARACTERISTICS);
    private static final String METHOD_CHECK_CONTROL_CENTER_ENABLE = "getControlCenterState";
    private static final String METHOD_CHECK_SWITCH = "checkCtrlPanelSwitch";
    private static final int MSG_CHECK_START_CTRLCENTER = 115;
    private static final int MSG_MENU_SCREEN_DELAY_TME = 15000;
    private static final int MSG_MENU_SCREEN_REMIND = 1;
    private static final String PCK_SYSTEMUI = "com.android.systemui";
    private static final String SCREEN_SHOT_EVENT_NAME = "com.huawei.screenshot.intent.action.KeyScreenshot";
    private static final String TAG = "HwHandleKeyManager";
    private static HwHandleKeyManager sInstance;
    private static boolean sIsDockShow = false;
    private Context mContext;
    private final ServiceConnection mCtrlCenterConn = new ServiceConnection() {
        /* class com.android.server.policy.HwHandleKeyManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwHandleKeyManager.this.mCtrlCenterIntf = IControlCenterGesture.Stub.asInterface(service);
            Slog.i(HwHandleKeyManager.TAG, "ControlCenterGesture Service connected, " + HwHandleKeyManager.this.mCtrlCenterIntf);
            HwHandleKeyManager.this.mHandler.sendEmptyMessage(115);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Slog.i(HwHandleKeyManager.TAG, "ControlCenterGesture Service disconnected");
            HwHandleKeyManager.this.mCtrlCenterIntf = null;
        }
    };
    private IControlCenterGesture mCtrlCenterIntf = null;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.server.policy.HwHandleKeyManager.AnonymousClass4 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            HwHandleKeyManager.this.rmvDockDeathRecipient();
        }
    };
    private ServiceConnection mDockConn = new ServiceConnection() {
        /* class com.android.server.policy.HwHandleKeyManager.AnonymousClass5 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Slog.i(HwHandleKeyManager.TAG, "onServiceConnected");
            synchronized (HwHandleKeyManager.this.mTimerLock) {
                HwHandleKeyManager.this.mDockService = IDockAidlInterface.Stub.asInterface(binder);
                if (HwHandleKeyManager.this.mDockService != null) {
                    try {
                        HwHandleKeyManager.this.mDockService.asBinder().linkToDeath(HwHandleKeyManager.this.mDeathRecipient, 0);
                        HwHandleKeyManager.this.mDockService.connect(0);
                    } catch (RemoteException e) {
                        Slog.e(HwHandleKeyManager.TAG, "onServiceConnected failed");
                    }
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    private BroadcastReceiver mDockReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.HwHandleKeyManager.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                try {
                    boolean unused = HwHandleKeyManager.sIsDockShow = intent.getBooleanExtra("isShow", false);
                } catch (BadParcelableException e) {
                    Slog.e(HwHandleKeyManager.TAG, "get dock state error.");
                }
            }
        }
    };
    private IDockAidlInterface mDockService;
    private Handler mHandler;
    private HwPhoneWindowManager mHwPhoneWindowManager;
    private boolean mIsCanBeSearched = false;
    private final Runnable mScreenshotRunnable = new Runnable() {
        /* class com.android.server.policy.HwHandleKeyManager.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            Slog.i(HwHandleKeyManager.TAG, "hardware_keys ScreenShot enter.");
            HwHandleKeyManager.this.sendScreenshotNotification();
            HwHandleKeyManager.this.mHandler.sendEmptyMessageDelayed(1, 15000);
            HwHandleKeyManager.this.mHwPhoneWindowManager.getDefaultDisplayPolicy().takeScreenshot(1);
        }
    };
    private final Object mTimerLock = new Object();
    private WindowManagerInternal mWindowManagerInternal;

    private HwHandleKeyManager(Context context) {
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new CombHandler(handlerThread.getLooper());
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mHwPhoneWindowManager = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        registerReceivers();
    }

    public static synchronized HwHandleKeyManager getInstance(Context context) {
        HwHandleKeyManager hwHandleKeyManager;
        synchronized (HwHandleKeyManager.class) {
            if (sInstance == null) {
                sInstance = new HwHandleKeyManager(context);
            }
            hwHandleKeyManager = sInstance;
        }
        return hwHandleKeyManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendScreenshotNotification() {
        Intent screenshotIntent = new Intent("com.huawei.recsys.action.RECEIVE_EVENT");
        screenshotIntent.putExtra("eventOperator", "sysScreenShot");
        screenshotIntent.putExtra("eventItem", "hardware_keys");
        screenshotIntent.setPackage("com.huawei.recsys");
        this.mContext.sendBroadcastAsUser(screenshotIntent, UserHandle.CURRENT, "com.huawei.tips.permission.SHOW_TIPS");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void rmvDockDeathRecipient() {
        synchronized (this.mTimerLock) {
            StringBuilder sb = new StringBuilder();
            sb.append("rmvDockDeathRecipient mDockService = ");
            sb.append(this.mDockService == null);
            Slog.d(TAG, sb.toString());
            if (this.mDockService != null) {
                try {
                    this.mDockService.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
                } catch (NoSuchElementException e) {
                    Slog.e(TAG, "rmvDockDeathRecipient: no such element., this=" + this);
                }
            }
        }
    }

    private void registerReceivers() {
        IntentFilter dockFilter = new IntentFilter();
        dockFilter.addAction(DOCK_SHOW_CLOSE_ACTION);
        this.mContext.registerReceiverAsUser(this.mDockReceiver, UserHandle.ALL, dockFilter, DOCK_SHOW_CLOSE_PER, null);
    }

    public boolean handleKeyEvent(KeyEvent event) {
        if (HwPCUtils.isPcCastModeInServer() || this.mHwPhoneWindowManager.isKeyguardLocked()) {
            return false;
        }
        int keyCode = event.getKeyCode();
        boolean isActionDown = event.getAction() == 0;
        int repeatCount = event.getRepeatCount();
        if (!KeyEvent.isMetaKey(keyCode)) {
            this.mIsCanBeSearched = false;
            if (!isActionDown || repeatCount != 0) {
                return false;
            }
            boolean isMetaPressed = event.isMetaPressed();
            boolean isAltPressed = event.isAltPressed();
            if (isMetaPressed) {
                boolean result = handlePadKeyWithMeta(keyCode, event);
                if (!result) {
                    return handleCommonKeyWithMeta(keyCode, event);
                }
                return result;
            } else if (!isAltPressed) {
                return false;
            } else {
                boolean result2 = handlePadKeyWithAlt(keyCode, event);
                if (!result2) {
                    return handleCommonKeyWithAlt(keyCode, event);
                }
                return result2;
            }
        } else if (isActionDown) {
            this.mIsCanBeSearched = true;
            return false;
        } else if (!this.mIsCanBeSearched) {
            return true;
        } else {
            this.mIsCanBeSearched = false;
            return false;
        }
    }

    private boolean handleCommonKeyWithAlt(int keyCode, KeyEvent event) {
        if (keyCode != 134) {
            return false;
        }
        goHomeOrRecoveryLast(event);
        return true;
    }

    private boolean handlePadKeyWithAlt(int keyCode, KeyEvent event) {
        if (IS_TABLET && keyCode == 61) {
            try {
                this.mHwPhoneWindowManager.mWindowManager.setInTouchMode(false);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to set touch mode!");
            }
        }
        return false;
    }

    private boolean handlePhoneKeyWithAlt(int keycode, KeyEvent event) {
        return false;
    }

    private boolean handleCommonKeyWithMeta(int keyCode, KeyEvent event) {
        if (keyCode == 32) {
            goHomeOrRecoveryLast(event);
            Flog.bdReport(this.mContext, 990203005, "{keycode:meta_d}");
            return true;
        } else if (keyCode == 36) {
            goHomeOrRecoveryLast(event);
            return true;
        } else if (event.isShiftPressed() && keyCode == 47) {
            this.mHandler.post(this.mScreenshotRunnable);
            return true;
        } else if (keyCode == 52) {
            showOrHideDockBar();
            return true;
        } else if (keyCode != 37 || !showOrHideCtrlCenter()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean handlePadKeyWithMeta(int keyCode, KeyEvent event) {
        PowerManager powerManager;
        if (!IS_TABLET) {
            return false;
        }
        if (keyCode == 40 && (powerManager = (PowerManager) this.mContext.getSystemService("power")) != null) {
            powerManager.goToSleep(SystemClock.uptimeMillis());
            return true;
        } else if (keyCode != 33) {
            return false;
        } else {
            openFileManagerByShortcutKey(this.mWindowManagerInternal);
            Flog.bdReport(this.mContext, 990203005, "{keycode:meta_e}");
            return true;
        }
    }

    private void showOrHideDockBar() {
        Intent intent = new Intent(DEFAULT_DOCK_AIDL_INTERFACE);
        intent.setComponent(new ComponentName("com.huawei.hwdockbar", DEFAULT_DOCK_MAIN_CLASS));
        synchronized (this.mTimerLock) {
            if (this.mDockService != null && this.mDockService.asBinder().isBinderAlive()) {
                if (this.mDockService.asBinder().pingBinder()) {
                    if (sIsDockShow) {
                        dockServiceFunction(1);
                    } else {
                        dockServiceFunction(2);
                    }
                }
            }
            ContextEx.bindServiceAsUser(this.mContext, intent, this.mDockConn, 1, UserHandleEx.CURRENT);
        }
    }

    private Bundle ctrlCenterServiceFunction(int type) {
        Slog.i(TAG, "ctrlCenterServiceFunction " + type);
        Bundle out = new Bundle();
        IControlCenterGesture iControlCenterGesture = this.mCtrlCenterIntf;
        if (iControlCenterGesture == null) {
            return out;
        }
        if (type != 3) {
            if (type == 4) {
                try {
                    iControlCenterGesture.startControlCenterSide(false);
                } catch (RemoteException e) {
                    Slog.e(TAG, "ctrlCenter connect failed");
                }
            }
        } else if (iControlCenterGesture.asBinder().isBinderAlive() && this.mCtrlCenterIntf.asBinder().pingBinder()) {
            try {
                this.mCtrlCenterIntf.dismissControlCenter();
            } catch (RemoteException e2) {
                Slog.e(TAG, "ctrlCenter dismiss failed");
            }
        }
        return out;
    }

    private Bundle dockServiceFunction(int type) {
        Slog.i(TAG, "dockServiceFunction " + type);
        Bundle out = new Bundle();
        IDockAidlInterface iDockAidlInterface = this.mDockService;
        if (iDockAidlInterface == null) {
            return out;
        }
        if (type != 1) {
            if (type == 2) {
                try {
                    iDockAidlInterface.connect(0);
                } catch (RemoteException e) {
                    Slog.e(TAG, "mDockService connect failed");
                }
            }
        } else if (iDockAidlInterface.asBinder().isBinderAlive() && this.mDockService.asBinder().pingBinder()) {
            try {
                this.mDockService.dismissWithAnimation();
            } catch (RemoteException e2) {
                Slog.e(TAG, "Dock dismiss failed");
            }
        }
        return out;
    }

    private boolean showOrHideCtrlCenter() {
        if (!checkPackageInstalled(CTRLCENTER_PKG) || !isControlCenterSwitchOn()) {
            return false;
        }
        Slog.i(TAG, "bindCtrlCenter");
        Intent intent = new Intent(CTRLCENTER_ACTION);
        intent.setPackage(CTRLCENTER_PKG);
        IControlCenterGesture iControlCenterGesture = this.mCtrlCenterIntf;
        if (iControlCenterGesture == null || !iControlCenterGesture.asBinder().isBinderAlive() || !this.mCtrlCenterIntf.asBinder().pingBinder()) {
            ContextEx.bindServiceAsUser(this.mContext, intent, this.mCtrlCenterConn, 1, UserHandleEx.CURRENT);
        } else if (isCtrlCenterShowing()) {
            ctrlCenterServiceFunction(3);
        } else {
            ctrlCenterServiceFunction(4);
        }
        return true;
    }

    private boolean isCtrlCenterShowing() {
        try {
            return this.mCtrlCenterIntf != null && this.mCtrlCenterIntf.isControlCenterShowing();
        } catch (RemoteException e) {
            Slog.e(TAG, "isControlCenterShowing RemoteException");
            return false;
        } catch (Exception e2) {
            Slog.e(TAG, "isControlCenterShowing Exception");
            return false;
        }
    }

    private boolean isControlCenterSwitchOn() {
        ContentResolver resolver;
        Bundle res;
        try {
            if (!(AUTHORITY_URI == null || (res = (resolver = this.mContext.getContentResolver()).call(AUTHORITY_URI, METHOD_CHECK_CONTROL_CENTER_ENABLE, (String) null, (Bundle) null)) == null)) {
                if (res.getBoolean(ISCONTROLCENTERENABLE, false)) {
                    Bundle res2 = resolver.call(AUTHORITY_URI, METHOD_CHECK_SWITCH, (String) null, (Bundle) null);
                    if (res2 == null || !res2.getBoolean(ISSWITCHON, false)) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException | IllegalStateException e) {
            Slog.e(TAG, "control center may not ready");
            return false;
        } catch (Exception e2) {
            Slog.e(TAG, "get control center content exception");
            return false;
        }
    }

    private boolean checkPackageInstalled(String packageName) {
        try {
            this.mContext.getPackageManager().getPackageInfo(packageName, 128);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void goHomeOrRecoveryLast(KeyEvent event) {
        if (!isHome() || isStatusBarShown()) {
            this.mHwPhoneWindowManager.launchHomeFromHotKey(event.getDisplayId());
            return;
        }
        List<ActivityManager.RecentTaskInfo> recentTaskInfos = ((ActivityManager) this.mContext.getSystemService("activity")).getRecentTasks(1, 2);
        if (recentTaskInfos != null && recentTaskInfos.size() > 0) {
            ActivityManager.RecentTaskInfo info = recentTaskInfos.get(0);
            Intent intent = new Intent(info.baseIntent);
            intent.setFlags(270532608);
            if (info.origActivity != null) {
                intent.setComponent(info.origActivity);
            }
            try {
                this.mContext.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Slog.e(TAG, "cloud not find activity!" + ex);
            }
        }
    }

    private boolean isStatusBarShown() {
        WindowManagerPolicy.WindowState windowState = this.mHwPhoneWindowManager.getDefaultDisplayPolicy().getFocusedWindow();
        if (windowState != null && windowState.getAttrs().type == 2000 && windowState.toString().contains("StatusBar")) {
            return true;
        }
        return false;
    }

    private boolean isHome() {
        TaskRecordEx taskRecord;
        ActivityRecordEx activityRecord;
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (runningTaskInfos == null || runningTaskInfos.isEmpty()) {
            return false;
        }
        int taskId = runningTaskInfos.get(0).taskId;
        ActivityTaskManagerService atms = this.mWindowManagerInternal.getActivityTaskManagerService();
        if (atms == null) {
            return false;
        }
        ActivityTaskManagerServiceEx atmsEx = new ActivityTaskManagerServiceEx();
        atmsEx.setActivityTaskManagerService(atms);
        RootActivityContainerEx rootActivityContainerEx = atmsEx.getRootActivityContainer();
        if (rootActivityContainerEx == null || (taskRecord = rootActivityContainerEx.anyTaskForId(taskId)) == null || (activityRecord = taskRecord.getTopActivity()) == null) {
            return false;
        }
        return activityRecord.isActivityTypeHome();
    }

    private void openFileManagerByShortcutKey(WindowManagerInternal windowManagerInternal) {
        ComponentName componentName;
        int displayId = windowManagerInternal.getFocusedDisplayId();
        if (25 > EMUI_VERSION) {
            componentName = new ComponentName("com.huawei.hidisk", "com.huawei.hidisk.filemanager.FileManager");
        } else {
            componentName = new ComponentName("com.huawei.filemanager", "com.huawei.hidisk.filemanager.FileManager");
        }
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.setFlags(268435456);
        intent.setComponent(componentName);
        Context context = this.mContext.createDisplayContext(DisplayManagerGlobal.getInstance().getRealDisplay(displayId));
        if (context != null) {
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Slog.e(TAG, "cloud not find activity!" + ex);
            }
        }
    }

    private class CombHandler extends Handler {
        CombHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                removeMessages(1);
                if (!DecisionUtil.bindServiceToAidsEngine(HwHandleKeyManager.this.mContext, HwHandleKeyManager.SCREEN_SHOT_EVENT_NAME)) {
                    Slog.i(HwHandleKeyManager.TAG, "bindServiceToAidsEngine error");
                }
            } else if (i == 115) {
                if (HwHandleKeyManager.this.mCtrlCenterIntf != null) {
                    Slog.i(HwHandleKeyManager.TAG, "start control center directly:");
                    try {
                        HwHandleKeyManager.this.mCtrlCenterIntf.startControlCenterSide(false);
                    } catch (RemoteException e) {
                        Slog.e(HwHandleKeyManager.TAG, "start control center fail");
                    } catch (Exception e2) {
                        Slog.e(HwHandleKeyManager.TAG, "start control center exception");
                    }
                } else {
                    Slog.i(HwHandleKeyManager.TAG, "center is null!!");
                }
            }
        }
    }
}
