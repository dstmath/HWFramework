package com.android.server.wm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.HwSlog;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.IRotationWatcher;
import android.view.IWindow;
import android.view.IWindowLayoutObserver;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import android.view.InsetsState;
import android.view.WindowManager;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.input.InputManagerService;
import com.android.server.input.InputManagerServiceBridge;
import com.android.server.policy.HwGameDockGesture;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerService;
import com.huawei.forcerotation.HwForceRotationManager;
import com.huawei.server.HwBasicPlatformFactory;
import huawei.android.app.IHwWindowCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HwWindowManagerService extends WindowManagerService {
    private static final String CHARGING_VIEW_NAME = "ChargingAnimView";
    private static final String HW_LAUNCHER_PKGNAME = "com.huawei.android.launcher";
    private static final int IBINDER_CODE_IS_KEYGUARD_DISABLE = 1000;
    private static final int SET_NAVIBAR_SHOWLEFT_TRANSACTION = 2201;
    private static final int SINGLE_HAND_STATE = 1989;
    private static final int SINGLE_HAND_SWITCH = 1990;
    static final String TAG = HwWindowManagerService.class.getSimpleName();
    private static final int UPDATE_NAVIGATIONBAR = 99;
    private final int TRANSACTION_GETTOUCHCOUNTINFO = 1006;
    private final int TRANSACTION_GET_PENDING_APP_TRANSITION = 1040;
    private final int TRANSACTION_isDimLayerVisible = HwArbitrationDEFS.MSG_CELL_STATE_ENABLE;
    private final int TRANSACTION_isIMEVisble = 1004;
    private final int TRANSACTION_registerWindowCallback = 1002;
    private final int TRANSACTION_registerWindowObserver = HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED;
    private final int TRANSACTION_unRegisterWindowCallback = 1003;
    private final int TRANSACTION_unregisterWindowObserver = HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT;
    IWindow mCurrentWindow = null;
    private List<String> mDisallowedAppForPc = Arrays.asList("com.xunmeng.pinduoduo");
    protected int mFocusedDisplayId = -1;
    private HwGameDockGesture mGameDockGesture = null;
    final Handler mHandler = new WindowManagerService.H(this);
    private boolean mHasRecord = false;
    private WindowState mHoldWindow;
    Handler mHwHandler = new Handler() {
        /* class com.android.server.wm.HwWindowManagerService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 99) {
                HwWindowManagerService hwWindowManagerService = HwWindowManagerService.this;
                boolean z = true;
                if (msg.arg1 != 1) {
                    z = false;
                }
                hwWindowManagerService.updateNavigationBar(z);
            }
        }
    };
    long mLastRelayoutNotifyTime;
    private int mLayerIndex = -1;
    private LockPatternUtils mLockPatternUtils;
    private WindowState mPCHoldWindow;
    long mRelayoutNotifyPeriod;
    private volatile long mSetTime = 0;
    private int mSingleHandSwitch;
    private boolean mSplitMode = false;
    private int mTempOrientation = -3;
    private AppWindowToken mTempToken = null;
    IWindowLayoutObserver mWindowLayoutObserver = null;

    public HwWindowManagerService(Context context, InputManagerService inputManager, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy, ActivityTaskManagerService atm, TransactionFactory transactionFactory) {
        super(context, inputManager, showBootMsgs, onlyCore, policy, atm, transactionFactory);
        LocalServices.addService(DefaultGestureNavManager.class, HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getGestureNavManager(context));
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mGameDockGesture = new HwGameDockGesture(context);
        LocalServices.addService(HwGameDockGesture.class, this.mGameDockGesture);
    }

    public int addWindow(Session session, IWindow client, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout, InputChannel outInputChannel, InsetsState outInsetsState) {
        if (attrs.type == 2101) {
            attrs.token = null;
        }
        if (HwPCUtils.isPcCastModeInServer() && this.mHardKeyboardAvailable && attrs.type == 2011) {
            Slog.i(TAG, "addInputMethodWindow: displayId = " + displayId);
        }
        if (HwPCUtils.isHiCarCastMode() && HwPCUtils.isValidExtDisplayId(displayId)) {
            attrs.flags |= 524288;
            if (this.mHwWMSEx != null) {
                List<String> winList = this.mHwWMSEx.getCarFocusList();
                winList.add("CarNavigationBar_port");
                winList.add("CarNavigationBar_horizontal");
            }
        }
        int newDisplayId = HwPCUtils.getPCDisplayID();
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(newDisplayId) && newDisplayId != displayId) {
            if (!"com.huawei.android.launcher".equals(attrs.packageName)) {
                if (displayId != -1 && displayId != 0 && displayId != newDisplayId && this.mDisallowedAppForPc.contains(attrs.packageName)) {
                    HwPCUtils.log(TAG, "Not change displayId for " + attrs.packageName + ", displayId:" + displayId + ", newDisplayId:" + newDisplayId);
                } else if (!"com.android.systemui".equals(attrs.packageName) || "VolumeDialogImpl".equals(attrs.getTitle()) || attrs.type == 2009 || attrs.type == 2008 || CHARGING_VIEW_NAME.equals(attrs.getTitle())) {
                    HwPCUtils.log(TAG, "add window:" + ((Object) attrs.getTitle()) + " ,displayId:" + displayId + ", newDisplayId:" + newDisplayId);
                    return HwWindowManagerService.super.addWindow(session, client, seq, attrs, viewVisibility, newDisplayId, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout, outInputChannel, outInsetsState);
                }
            }
        }
        return HwWindowManagerService.super.addWindow(session, client, seq, attrs, viewVisibility, displayId, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout, outInputChannel, outInsetsState);
    }

    public boolean isKeyguardOccluded() {
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            return this.mPolicy.isKeyguardOccluded();
        }
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1006) {
            data.enforceInterface("android.view.IWindowManager");
            if (!(this.mPolicy instanceof HwPhoneWindowManager)) {
                Slog.w(TAG, "onTransct->current is not hw pwm");
                return true;
            } else if (this.mContext.checkPermission("com.huawei.permission.GET_TOUCH_COUNT_INFO", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                reply.writeIntArray(this.mPolicy.getDefaultTouchCountInfo());
                return true;
            } else {
                reply.writeIntArray(this.mPolicy.getTouchCountInfo());
                reply.writeNoException();
                return true;
            }
        } else if (code == 1007) {
            data.enforceInterface("android.view.IWindowManager");
            int result = isDLayerVisible();
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        } else if (code == 1009) {
            data.enforceInterface("android.view.IWindowManager");
            registerWindowObserver(IWindowLayoutObserver.Stub.asInterface(data.readStrongBinder()), data.readLong());
            reply.writeNoException();
            return true;
        } else if (code == 1010) {
            data.enforceInterface("android.view.IWindowManager");
            unRegisterWindowObserver(IWindowLayoutObserver.Stub.asInterface(data.readStrongBinder()));
            reply.writeNoException();
            return true;
        } else if (code != 1040) {
            boolean isNavibarLeft = false;
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            if (code == SET_NAVIBAR_SHOWLEFT_TRANSACTION) {
                data.enforceInterface("android.view.IWindowManager");
                if (this.mContext.checkPermission("com.huawei.permission.NAVIBAR_LEFT_WHENLAND", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    reply.writeInt(-1);
                    return true;
                }
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    if (data.readInt() == 1) {
                        isNavibarLeft = true;
                    }
                    this.mPolicy.setNavibarAlignLeftWhenLand(isNavibarLeft);
                }
                return true;
            } else if (code == SINGLE_HAND_SWITCH) {
                data.enforceInterface("android.view.IWindowManager");
                reply.writeNoException();
                reply.writeInt(getLazyMode());
                return true;
            } else if (code != 1991) {
                switch (code) {
                    case HwAPPQoEUtils.MSG_INTERNAL_CHR_EXCP_TRIGGER /* 203 */:
                        data.enforceInterface("android.view.IWindowManager");
                        rotateWithHoldDialog();
                        reply.writeNoException();
                        return true;
                    case 204:
                        data.enforceInterface("android.view.IWindowManager");
                        this.mHwHandler.sendMessage(this.mHwHandler.obtainMessage(99, data.readInt(), 0));
                        reply.writeNoException();
                        return true;
                    case HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK /* 205 */:
                        data.enforceInterface("android.view.IWindowManager");
                        if (this.mPolicy instanceof HwPhoneWindowManager) {
                            this.mPolicy.getDefaultDisplayPolicy().swipeFromTop();
                        }
                        reply.writeNoException();
                        return true;
                    case HwAPPQoEUtils.MSG_CHR_WIFI_KQI /* 206 */:
                        data.enforceInterface("android.view.IWindowManager");
                        boolean isTopIsFullscreen = false;
                        if (this.mPolicy instanceof HwPhoneWindowManager) {
                            isTopIsFullscreen = this.mPolicy.isTopIsFullscreen();
                        }
                        if (isTopIsFullscreen) {
                            i3 = 1;
                        }
                        reply.writeInt(i3);
                        return true;
                    case HwAPPQoEUtils.MSG_REGISTER_HICOM /* 207 */:
                        data.enforceInterface("android.view.IWindowManager");
                        if (this.mPolicy instanceof HwPhoneWindowManager) {
                            this.mPolicy.showHwTransientBars();
                        }
                        return true;
                    default:
                        WindowState inputMethodWindow = null;
                        switch (code) {
                            case 1001:
                                data.enforceInterface("android.view.IWindowManager");
                                boolean result2 = this.mLockPatternUtils.isLockScreenDisabled(0);
                                reply.writeNoException();
                                if (result2) {
                                    i2 = 1;
                                }
                                reply.writeInt(i2);
                                return true;
                            case 1002:
                                data.enforceInterface("android.view.IWindowManager");
                                IHwWindowCallback hwWindowCallback = IHwWindowCallback.Stub.asInterface(data.readStrongBinder());
                                if (this.mPolicy instanceof HwPhoneWindowManager) {
                                    this.mPolicy.setHwWindowCallback(hwWindowCallback);
                                }
                                reply.writeNoException();
                                return true;
                            case 1003:
                                data.enforceInterface("android.view.IWindowManager");
                                if (this.mPolicy instanceof HwPhoneWindowManager) {
                                    this.mPolicy.setHwWindowCallback(null);
                                }
                                reply.writeNoException();
                                return true;
                            case 1004:
                                data.enforceInterface("android.view.IWindowManager");
                                if (this.mContext.checkPermission("com.huawei.permission.HUAWEI_IME_STATE_ACCESS", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                                    reply.writeInt(-1);
                                    return true;
                                }
                                if (getInputMethodWindowLw() instanceof WindowState) {
                                    inputMethodWindow = (WindowState) getInputMethodWindowLw();
                                }
                                boolean isIMEVisible = inputMethodWindow != null && inputMethodWindow.isVisibleLw();
                                HwSlog.d(TAG, "imeVis=" + isIMEVisible);
                                reply.writeNoException();
                                if (isIMEVisible) {
                                    i = 1;
                                }
                                reply.writeInt(i);
                                return true;
                            default:
                                try {
                                    return HwWindowManagerService.super.onTransact(code, data, reply, flags);
                                } catch (RuntimeException e) {
                                    if (!(e instanceof SecurityException)) {
                                        Slog.w(TAG, "Window Manager Crash");
                                    }
                                    throw e;
                                }
                        }
                }
            } else {
                if (this.mHwWMSEx != null) {
                    if (this.mHwWMSEx.isSupportSingleHand()) {
                        i4 = 1;
                    }
                    this.mSingleHandSwitch = i4;
                }
                Slog.i(TAG, "mSingleHandSwitch =" + this.mSingleHandSwitch);
                data.enforceInterface("android.view.IWindowManager");
                reply.writeNoException();
                reply.writeInt(this.mSingleHandSwitch);
                return true;
            }
        } else {
            data.enforceInterface("android.view.IWindowManager");
            int result3 = getDefaultDisplayContentLocked().getAppTransition();
            reply.writeNoException();
            reply.writeInt(result3);
            return true;
        }
    }

    private int isDLayerVisible() {
        return 0;
    }

    public Bitmap getTaskSnapshotForPc(int displayId, IBinder binder, int taskId, int userId) {
        synchronized (this.mGlobalLock) {
            DisplayContent dc = this.mRoot.getDisplayContent(displayId);
            if (dc == null) {
                String str = TAG;
                HwPCUtils.log(str, "[getTaskSnapshotForPc]fail to get displaycontent, displayId:" + displayId + " taskId:" + taskId);
                return null;
            }
            AppWindowToken appWindowToken = dc.getAppWindowToken(binder);
            if (appWindowToken == null) {
                String str2 = TAG;
                HwPCUtils.log(str2, "[getTaskSnapshotForPc]fail to get app window token, taskId:" + taskId);
                return null;
            }
            Task task = appWindowToken.getTask();
            if (task == null) {
                String str3 = TAG;
                HwPCUtils.log(str3, "[getTaskSnapshotForPc]fail to get task, taskId:" + taskId);
                return null;
            }
            ArraySet<Task> taskSet = new ArraySet<>();
            taskSet.add(task);
            this.mTaskSnapshotController.snapshotTasks(taskSet);
            ActivityManager.TaskSnapshot taskSnapshot = getTaskSnapshot(taskId, userId, false, true);
            if (taskSnapshot == null) {
                String str4 = TAG;
                HwPCUtils.log(str4, "[getTaskSnapshotForPc] to get snapshot, taskId:" + taskId);
                return null;
            }
            return Bitmap.wrapHardwareBuffer(HardwareBuffer.createFromGraphicBuffer(taskSnapshot.getSnapshot()), taskSnapshot.getColorSpace());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNavigationBar(boolean minNaviBar) {
        getDefaultDisplayContentLocked().getDisplayPolicy().updateNavigationBar(minNaviBar);
    }

    private void rotateWithHoldDialog() {
        this.mHandler.removeMessages(17);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(17));
        this.mHandler.removeMessages(11);
        Handler handler2 = this.mHandler;
        handler2.sendMessage(handler2.obtainMessage(11, getDefaultDisplayContentLocked()));
    }

    public void setPCScreenDisplayMode(int mode) {
        String propVal = "normal";
        if (mode == 1) {
            propVal = "minor";
        } else if (mode == 2) {
            propVal = "smaller";
        }
        SystemProperties.set("hw.pc.display.mode", propVal);
        synchronized (this.mGlobalLock) {
            this.mHwWMSEx.performDisplayTraversalLocked();
        }
    }

    public int getPCScreenDisplayMode() {
        String strMode = SystemProperties.get("hw.pc.display.mode");
        if ("minor".equals(strMode)) {
            return 1;
        }
        if ("smaller".equals(strMode)) {
            return 2;
        }
        return 0;
    }

    public boolean isSplitMode() {
        return this.mSplitMode;
    }

    public void setSplittable(boolean splittable) {
        this.mSplitMode = splittable;
    }

    public int getLayerIndex(String appName, int windowType) {
        DisplayContent displayContent = getDefaultDisplayContentLocked();
        if (displayContent == null) {
            return -1;
        }
        try {
            displayContent.forAllWindows(new Consumer(appName) {
                /* class com.android.server.wm.$$Lambda$HwWindowManagerService$QUcD2orMmsft5oQxGQy29TJsU */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    HwWindowManagerService.this.lambda$getLayerIndex$0$HwWindowManagerService(this.f$1, (WindowState) obj);
                }
            }, false);
        } catch (Exception e) {
            Slog.w(TAG, "getLayerIndex exception!");
        }
        return this.mLayerIndex;
    }

    public /* synthetic */ void lambda$getLayerIndex$0$HwWindowManagerService(String appName, WindowState ws) {
        if (ws.getWindowTag().toString().indexOf(appName) > -1) {
            this.mLayerIndex = ws.mLayer;
        }
    }

    public void setKeyguardGoingAway(boolean keyGuardGoingAway) {
        if (keyGuardGoingAway) {
            this.mSetTime = SystemClock.elapsedRealtime();
        }
        HwWindowManagerService.super.setKeyguardGoingAway(keyGuardGoingAway);
    }

    /* access modifiers changed from: protected */
    public boolean shouldHideIMExitAnim(WindowState win) {
        boolean ret = SystemClock.elapsedRealtime() - this.mSetTime <= 500 && (win.mAttrs.type == 2012 || win.mAttrs.type == 2011);
        if (SystemClock.elapsedRealtime() - this.mSetTime < HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM && (win.mAttrs.type == 2012 || win.mAttrs.type == 2011)) {
            String str = TAG;
            Slog.d(str, "KeyguardGoingAway:" + this.mKeyguardGoingAway + ";Realtime():" + SystemClock.elapsedRealtime() + ";mSetTime:" + this.mSetTime + ";type:" + win.mAttrs.type + " reval = " + ret + " win:" + win);
        }
        return ret;
    }

    public void registerWindowObserver(IWindowLayoutObserver observer, long period) {
        if (checkCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "registerWindowObserver()")) {
            if (period <= 0) {
                String str = TAG;
                Slog.e(str, "registerWindowObserver with wrong period " + period);
                return;
            }
            this.mRelayoutNotifyPeriod = period;
            if (this.mRelayoutNotifyPeriod < 500) {
                this.mRelayoutNotifyPeriod = 500;
            }
            this.mLastRelayoutNotifyTime = 0;
            WindowState ws = HwWindowManagerService.super.getFocusedWindow();
            synchronized (this.mGlobalLock) {
                Iterator<Map.Entry<IBinder, WindowState>> it = this.mWindowMap.entrySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Map.Entry<IBinder, WindowState> entry = it.next();
                    if (ws == entry.getValue()) {
                        this.mCurrentWindow = IWindow.Stub.asInterface(entry.getKey());
                        break;
                    }
                }
            }
            IWindow iWindow = this.mCurrentWindow;
            if (iWindow != null) {
                try {
                    iWindow.registerWindowObserver(observer, period);
                } catch (RemoteException e) {
                    Slog.w(TAG, "registerWindowObserver get RemoteException");
                }
            }
        }
    }

    public void unRegisterWindowObserver(IWindowLayoutObserver observer) {
        if (checkCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "unRegisterWindowObserver()")) {
            IWindow iWindow = this.mCurrentWindow;
            if (iWindow != null) {
                try {
                    iWindow.unRegisterWindowObserver(observer);
                } catch (RemoteException e) {
                    Slog.w(TAG, "unRegisterWindowObserver get RemoteException");
                }
            }
            this.mCurrentWindow = null;
            String str = TAG;
            Slog.d(str, "unRegisterWindowObserver OK, observer = " + observer);
        }
    }

    public void setFocusedDisplay(int displayId, boolean findTopTask, String reason) {
        boolean oldPCLauncherFocused = getPCLauncherFocused();
        List<Integer> tasks = new ArrayList<>();
        synchronized (this.mGlobalLock) {
            DisplayContent dc = this.mRoot.getDisplayContent(displayId);
            if (dc != null) {
                if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad() && "lockScreen".equals(reason)) {
                    return;
                }
                if (dc.getDisplayId() != this.mFocusedDisplayId || this.mRoot.mTopFocusedDisplayId != dc.getDisplayId() || (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad() && "unlockScreen".equals(reason))) {
                    this.mFocusedDisplayId = dc.getDisplayId();
                    if (this.mRoot.mTopFocusedDisplayId != this.mFocusedDisplayId && this.mFocusedDisplayId == 0 && HwPCUtils.isPcCastModeInServer() && findTopTask) {
                        this.mInputManager.setFocusedDisplay(this.mFocusedDisplayId);
                    }
                    if (HwPCUtils.isPcCastModeInServer() && !HwPCUtils.enabledInPad() && (this.mHardKeyboardAvailable || HwPCUtils.mTouchDeviceID != -1)) {
                        this.mHwWMSEx.relaunchIMEProcess();
                    }
                    if (this.mFocusedDisplayId == 0) {
                        setPCLauncherFocused(false);
                    }
                    if (findTopTask && (dc instanceof DisplayContentBridge)) {
                        tasks = ((DisplayContentBridge) dc).taskIdFromTop();
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        if (tasks.size() > 0) {
            for (int i = tasks.size() - 1; i >= 0; i--) {
                int taskId = tasks.get(i).intValue();
                if (taskId >= 0) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(104, taskId, 0));
                }
            }
        } else if (HwPCUtils.isValidExtDisplayId(displayId) && "handleTapOutsideTask-1-1".equals(reason)) {
            setPCLauncherFocused(false);
        }
        if (!(oldPCLauncherFocused == getPCLauncherFocused() || "handleTapOutsideTaskXY".equals(reason))) {
            synchronized (this.mGlobalLock) {
                DisplayContent dc2 = this.mRoot.getDisplayContent(displayId);
                if (dc2 != null) {
                    dc2.layoutAndAssignWindowLayersIfNeeded();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyHardKeyboardStatusChange() {
        HwWindowManagerService.super.notifyHardKeyboardStatusChange();
    }

    public int getWindowSystemUiVisibility(IBinder token) {
        WindowState win;
        synchronized (this.mGlobalLock) {
            AppWindowToken appToken = this.mRoot.getAppWindowToken(token);
            if (appToken == null || (win = appToken.findMainWindow()) == null) {
                return 0;
            }
            return win.getSystemUiVisibility();
        }
    }

    public void setPCLauncherFocused(boolean focus) {
        synchronized (this.mGlobalLock) {
            if (focus != this.mPCLauncherFocused) {
                this.mPCLauncherFocused = focus;
            }
        }
    }

    public int getFocusedDisplayId() {
        if (HwPCUtils.isPcCastModeInServer() || HwPCUtils.isInWindowsCastMode()) {
            return this.mFocusedDisplayId;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void getStableInsetsLocked(int displayId, Rect outInsets) {
        HwWindowManagerService.super.getStableInsetsLocked(displayId, outInsets);
    }

    public DisplayManager getDisplayManager() {
        return this.mDisplayManager;
    }

    public WindowManagerPolicy getPolicy() {
        return this.mPolicy;
    }

    private boolean isDisplayIdInVrMode(int displayId) {
        Display display;
        if (!(this.mDisplayManager == null || (display = this.mDisplayManager.getDisplay(displayId)) == null)) {
            DisplayInfo displayInfo = new DisplayInfo();
            if (display.getDisplayInfo(displayInfo)) {
                int width = displayInfo.getNaturalWidth();
                int height = displayInfo.getNaturalHeight();
                if (width == 2880 && height == 1600) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public void onDisplayAdded(int displayId) {
        sendDisplayStateBroadcast(true, displayId);
    }

    public void onDisplayChanged(int displayId) {
        HwWindowManagerService.super.onDisplayChanged(displayId);
    }

    public void onDisplayRemoved(int displayId) {
        sendDisplayStateBroadcast(false, displayId);
    }

    public void addWindowToken(IBinder binder, int type, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
            synchronized (this.mGlobalLock) {
                if (!HwPCUtils.isValidExtDisplayId(displayId) || this.mDisplayManager.getDisplay(displayId) != null) {
                    HwWindowManagerService.super.addWindowToken(binder, type, displayId);
                    return;
                }
                Slog.w("WindowManager", "addWindowToken: Attempted to add binder token: " + binder + " for non-exiting displayId=" + displayId);
                return;
            }
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    /* access modifiers changed from: protected */
    public boolean isTokenFound(IBinder binder, DisplayContent dc) {
        WindowToken windowToken;
        if (!HwPCUtils.isPcCastModeInServer()) {
            return false;
        }
        for (int i = 0; i < this.mRoot.mChildren.size(); i++) {
            DisplayContent displayContent = (DisplayContent) this.mRoot.mChildren.get(i);
            if (displayContent.getDisplayId() != dc.getDisplayId() && (windowToken = displayContent.getWindowToken(binder)) != null && windowToken.windowType == 2011) {
                displayContent.removeWindowToken(binder);
                String str = TAG;
                HwPCUtils.log(str, "removeWindowToken isTokenFound in display:" + displayContent.getDisplayId());
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isSecureLocked(WindowState w) {
        return HwWindowManagerService.super.isSecureLocked(w);
    }

    /* access modifiers changed from: protected */
    public boolean isDisplayOkForAnimation(int width, int height, int transit, AppWindowToken atoken) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (forceRotationManager.isForceRotationSupported() && forceRotationManager.isForceRotationSwitchOpen(this.mContext) && width > height && ((transit == 7 || transit == 6) && forceRotationManager.isAppForceLandRotatable(atoken.appPackageName, atoken.appToken.asBinder()))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkAppOrientationForForceRotation(AppWindowToken aToken) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen(this.mContext) || aToken == null) {
            return false;
        }
        int or = aToken.mOrientation;
        if (!(aToken == this.mTempToken && or == this.mTempOrientation)) {
            this.mHasRecord = forceRotationManager.saveOrUpdateForceRotationAppInfo(aToken.appPackageName, aToken.appComponentName, aToken.appToken.asBinder(), or);
            this.mTempToken = aToken;
            this.mTempOrientation = or;
        }
        if (!this.mHasRecord) {
            return false;
        }
        if (or != 1 && or != 7 && or != 9 && or != 12) {
            return false;
        }
        forceRotationManager.showToastIfNeeded(aToken.appPackageName, aToken.appPid, aToken.appProcessName, aToken.appToken.asBinder());
        return true;
    }

    public void showWallpaperIfNeed(WindowState w) {
        Display dp;
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen(this.mContext) || w == null || w.inMultiWindowMode()) {
            return;
        }
        if (w.mAppToken == null || !forceRotationManager.isAppForceLandRotatable(w.mAppToken.appPackageName, w.mAppToken.appToken.asBinder())) {
            Slog.v(TAG, "current window do not support force rotation mAppToken:" + w.mAppToken);
            return;
        }
        DisplayContent dc = getDefaultDisplayContentLocked();
        if (dc != null && (dp = dc.getDisplay()) != null) {
            DisplayMetrics dm = new DisplayMetrics();
            dp.getMetrics(dm);
            if (dm.widthPixels < dm.heightPixels) {
                w.mAttrs.flags &= -1048577;
                return;
            }
            w.mAttrs.flags |= 1048576;
        }
    }

    public void prepareForForceRotation(IBinder token, String packageName, int pid, String processName) {
        if (HwForceRotationManager.getDefault().isForceRotationSupported()) {
            synchronized (this.mGlobalLock) {
                AppWindowToken aToken = this.mRoot.getAppWindowToken(token);
                if (aToken == null) {
                    String str = TAG;
                    Slog.w(str, "Attempted to set orientation of non-existing app token: " + token);
                    return;
                }
                aToken.appPackageName = packageName;
                aToken.appPid = pid;
                aToken.appProcessName = processName;
            }
        }
    }

    private void sendDisplayStateBroadcast(boolean isAdded, int displayId) {
        if (SystemProperties.getBoolean("ro.config.vrbroad", false) && displayId != 0 && this.mContext != null) {
            Intent intent = new Intent();
            String str = TAG;
            Log.i(str, "send broadcast displayState, displayId:" + displayId + " isAdded:" + isAdded);
            intent.setAction("com.huawei.display.vr.added");
            intent.setPackage("com.huawei.vrservice");
            intent.putExtra("displayId", displayId);
            if (isAdded) {
                intent.putExtra("displayState", "on");
            } else {
                intent.putExtra("displayState", "off");
            }
            this.mContext.sendBroadcast(intent, "com.huawei.display.vr.permission");
        }
    }

    public void updateFingerprintSlideSwitch() {
        if (HwPCUtils.enabled() && (this.mInputManager instanceof InputManagerServiceBridge)) {
            this.mInputManager.updateFingerprintSlideSwitchValue();
        }
    }

    public WindowManagerPolicy.InputConsumer createInputConsumer(Looper looper, String name, InputEventReceiver.Factory inputEventReceiverFactory, int displayId) {
        if (name != null) {
            return HwWindowManagerService.super.createInputConsumer(looper, name, inputEventReceiverFactory, displayId);
        }
        Slog.e(TAG, "createInputConsumer name is null");
        return null;
    }

    public boolean inputMethodClientHasFocus(IInputMethodClient client) {
        if (client != null) {
            return false;
        }
        Slog.e(TAG, "inputMethodClientHasFocus name is null");
        return false;
    }

    public void setDockedStackDividerTouchRegion(Rect touchRegion) {
        if (touchRegion == null) {
            Slog.e(TAG, "setDockedStackDividerTouchRegion touchRegion is null");
        } else {
            HwWindowManagerService.super.setDockedStackDividerTouchRegion(touchRegion);
        }
    }

    public void removeRotationWatcher(IRotationWatcher watcher) {
        if (watcher == null) {
            Slog.e(TAG, "removeRotationWatcher watcher is null");
        } else {
            HwWindowManagerService.super.removeRotationWatcher(watcher);
        }
    }

    public int watchRotation(IRotationWatcher watcher, int displayId) {
        if (watcher != null) {
            return HwWindowManagerService.super.watchRotation(watcher, displayId);
        }
        Slog.e(TAG, "watchRotation watcher is null");
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void setHoldScreenLocked(Session newHoldScreen) {
        HwWindowManagerService.super.setHoldScreenLocked(newHoldScreen);
        if (!HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            boolean hold = newHoldScreen != null;
            boolean pcState = this.mPCHoldingScreenWakeLock.isHeld();
            if (hold) {
                WindowState holdWindow = this.mRoot.mHoldScreenWindow;
                if (HwPCUtils.isValidExtDisplayId(holdWindow.getDisplayId())) {
                    this.mPCHoldWindow = holdWindow;
                } else {
                    this.mHoldWindow = holdWindow;
                }
            }
            if (hold != pcState) {
                if (!hold) {
                    this.mPCHoldingScreenWakeLock.release();
                } else if (HwPCUtils.isValidExtDisplayId(this.mRoot.mHoldScreenWindow.getDisplayId())) {
                    this.mPCHoldingScreenWakeLock.acquire();
                }
            }
            WindowState windowState = this.mHoldWindow;
            if (windowState == null || !windowState.isVisibleLw() || (this.mHoldWindow.mAttrs.flags & 128) == 0) {
                this.mHoldWindow = null;
                this.mHoldingScreenWakeLock.release();
            }
            WindowState windowState2 = this.mPCHoldWindow;
            if (windowState2 == null || !windowState2.isVisibleLw() || (this.mPCHoldWindow.mAttrs.flags & 128) == 0) {
                this.mPCHoldWindow = null;
                this.mPCHoldingScreenWakeLock.release();
            }
        }
    }
}
