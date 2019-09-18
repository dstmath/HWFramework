package com.android.server.wm;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
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
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.util.ToBooleanFunction;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.mplink.HwMpLinkServiceImpl;
import com.android.server.input.HwInputManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wm.IntelliServiceManager;
import com.android.server.wm.WindowManagerService;
import huawei.android.app.IHwWindowCallback;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HwWindowManagerService extends WindowManagerService {
    static final boolean DEBUG = false;
    private static final int IBINDER_CODE_IS_KEYGUARD_DISABLE = 1000;
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    private static final int SET_NAVIBAR_SHOWLEFT_TRANSACTION = 2201;
    private static final int SINGLE_HAND_STATE = 1989;
    private static final int SINGLE_HAND_SWITCH = 1990;
    static final String TAG = HwWindowManagerService.class.getSimpleName();
    public static final int UPDATE_NAVIGATIONBAR = 99;
    private boolean IS_SUPPORT_PRESSURE = false;
    final int TRANSACTION_GETTOUCHCOUNTINFO = HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT;
    final int TRANSACTION_isDimLayerVisible = HwArbitrationDEFS.MSG_CELL_STATE_ENABLE;
    final int TRANSACTION_isIMEVisble = 1004;
    final int TRANSACTION_registerWindowCallback = 1002;
    final int TRANSACTION_registerWindowObserver = HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED;
    final int TRANSACTION_unRegisterWindowCallback = 1003;
    final int TRANSACTION_unregisterWindowObserver = HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT;
    IWindow mCurrentWindow = null;
    private WindowState mCurrentWindowState = null;
    IntelliServiceManager.FaceRotationCallback mFaceRotationCallback = new IntelliServiceManager.FaceRotationCallback() {
        public void onEvent(int faceRotation) {
            HwWindowManagerService.this.updateRotationUnchecked(false, false);
        }
    };
    protected int mFocusedDisplayId = -1;
    final Handler mHandler = new WindowManagerService.H(this);
    private WindowState mHoldWindow;
    private Handler mHwHandler = new Handler() {
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
    private IVRSystemServiceManager mVrMananger;
    IWindowLayoutObserver mWindowLayoutObserver = null;

    public HwWindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        super(context, inputManager, haveInputMethods, showBootMsgs, onlyCore, policy);
        this.mLockPatternUtils = new LockPatternUtils(context);
        HwGestureNavWhiteConfig.getInstance().initWmsServer(this, context);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    /* access modifiers changed from: package-private */
    public boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
        boolean ret = HwWindowManagerService.super.updateFocusedWindowLocked(mode, updateInputWindows);
        HwGestureNavWhiteConfig.getInstance().updatewindow(this.mCurrentFocus);
        return ret;
    }

    public boolean isGestureNavMisTouch() {
        return HwGestureNavWhiteConfig.getInstance().isEnable();
    }

    public int addWindow(Session session, IWindow client, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout, InputChannel outInputChannel) {
        int newDisplayId;
        WindowManager.LayoutParams layoutParams = attrs;
        int i = displayId;
        if (layoutParams.type == 2101) {
            layoutParams.token = null;
        }
        if (HwPCUtils.isPcCastModeInServer() && this.mHardKeyboardAvailable && 2011 == layoutParams.type) {
            String str = TAG;
            Slog.i(str, "addInputMethodWindow: displayId = " + i);
        }
        int newDisplayId2 = HwPCUtils.getPCDisplayID();
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(newDisplayId2) && newDisplayId2 != i) {
            if ("HwGlobalActions".equals(attrs.getTitle()) || "VolumeDialogImpl".equals(attrs.getTitle()) || "com.ss.android.article.news".equals(layoutParams.packageName) || 2010 == layoutParams.type || 2011 == layoutParams.type || 2012 == layoutParams.type || 2003 == layoutParams.type) {
                newDisplayId = newDisplayId2;
            } else if ((2009 == layoutParams.type && FingerViewController.PKGNAME_OF_KEYGUARD.equals(layoutParams.packageName)) || ((2008 == layoutParams.type && FingerViewController.PKGNAME_OF_KEYGUARD.equals(layoutParams.packageName)) || "com.google.android.marvin.talkback".equals(layoutParams.packageName))) {
                newDisplayId = newDisplayId2;
            } else if (layoutParams.type >= 1000 && layoutParams.type <= 1999) {
                WindowState parentWindow = windowForClientLocked(null, layoutParams.token, false);
                if (parentWindow != null && parentWindow.mAttrs.type == 2011) {
                    String str2 = TAG;
                    HwPCUtils.log(str2, "addSubWindow Title = " + attrs.getTitle() + "packageName = " + layoutParams.packageName + ",setdisplayId = " + newDisplayId2 + " oldDisplayID=" + i);
                    WindowState windowState = parentWindow;
                    int i2 = newDisplayId2;
                    return HwWindowManagerService.super.addWindow(session, client, seq, layoutParams, viewVisibility, newDisplayId2, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout, outInputChannel);
                }
            }
            String str3 = TAG;
            HwPCUtils.log(str3, "addWindow Title = " + attrs.getTitle() + "packageName = " + layoutParams.packageName + ",setdisplayId = " + newDisplayId + " oldDisplayID=" + i);
            return HwWindowManagerService.super.addWindow(session, client, seq, layoutParams, viewVisibility, newDisplayId, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout, outInputChannel);
        }
        return HwWindowManagerService.super.addWindow(session, client, seq, attrs, viewVisibility, displayId, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout, outInputChannel);
    }

    public boolean isKeyguardOccluded() {
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            return this.mPolicy.isKeyguardOccluded();
        }
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        boolean isNavibarLeft = false;
        switch (code) {
            case 203:
                data.enforceInterface("android.view.IWindowManager");
                rotateWithHoldDialog();
                reply.writeNoException();
                return true;
            case 204:
                data.enforceInterface("android.view.IWindowManager");
                this.mHwHandler.sendMessage(this.mHwHandler.obtainMessage(99, data.readInt(), 0));
                reply.writeNoException();
                return true;
            case 205:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    this.mPolicy.swipeFromTop();
                }
                reply.writeNoException();
                return true;
            case 206:
                data.enforceInterface("android.view.IWindowManager");
                boolean isTopIsFullscreen = false;
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    isTopIsFullscreen = this.mPolicy.isTopIsFullscreen();
                }
                if (isTopIsFullscreen) {
                    isNavibarLeft = true;
                }
                reply.writeInt(isNavibarLeft ? 1 : 0);
                return true;
            case HwMpLinkServiceImpl.MPLINK_MSG_WIFI_VPN_DISCONNETED:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    this.mPolicy.showHwTransientBars();
                }
                return true;
            case 1001:
                data.enforceInterface("android.view.IWindowManager");
                boolean result = this.mLockPatternUtils.isLockScreenDisabled(0);
                reply.writeNoException();
                if (result) {
                    isNavibarLeft = true;
                }
                reply.writeInt(isNavibarLeft ? 1 : 0);
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
                boolean isIMEVisible = this.mInputMethodWindow != null && this.mInputMethodWindow.isVisibleLw();
                HwSlog.d(TAG, "imeVis=" + isIMEVisible);
                reply.writeNoException();
                if (isIMEVisible) {
                    isNavibarLeft = true;
                }
                reply.writeInt(isNavibarLeft ? 1 : 0);
                return true;
            case HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT:
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
            case HwArbitrationDEFS.MSG_CELL_STATE_ENABLE:
                data.enforceInterface("android.view.IWindowManager");
                int result2 = isDLayerVisible();
                reply.writeNoException();
                reply.writeInt(result2);
                return true;
            case HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED:
                data.enforceInterface("android.view.IWindowManager");
                registerWindowObserver(IWindowLayoutObserver.Stub.asInterface(data.readStrongBinder()), data.readLong());
                reply.writeNoException();
                return true;
            case HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT:
                data.enforceInterface("android.view.IWindowManager");
                unRegisterWindowObserver(IWindowLayoutObserver.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case SINGLE_HAND_SWITCH /*1990*/:
                data.enforceInterface("android.view.IWindowManager");
                reply.writeNoException();
                reply.writeInt(getLazyMode());
                return true;
            case 1991:
                Slog.i(TAG, "mSingleHandSwitch =" + this.mSingleHandSwitch);
                if (this.mHwWMSEx != null) {
                    this.mSingleHandSwitch = this.mHwWMSEx.isSupportSingleHand() ? 1 : 0;
                }
                data.enforceInterface("android.view.IWindowManager");
                reply.writeNoException();
                reply.writeInt(this.mSingleHandSwitch);
                return true;
            case SET_NAVIBAR_SHOWLEFT_TRANSACTION /*2201*/:
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

    public int isDLayerVisible() {
        return 0;
    }

    public Bitmap getTaskSnapshotForPc(int displayId, IBinder binder, int taskId, int userId) {
        synchronized (this.mWindowMap) {
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
            ActivityManager.TaskSnapshot taskSnapshot = getTaskSnapshot(taskId, userId, false);
            if (taskSnapshot == null) {
                String str4 = TAG;
                HwPCUtils.log(str4, "[getTaskSnapshotForPc] to get snapshot, taskId:" + taskId);
                return null;
            }
            Bitmap createHardwareBitmap = Bitmap.createHardwareBitmap(taskSnapshot.getSnapshot());
            return createHardwareBitmap;
        }
    }

    /* access modifiers changed from: private */
    public void updateNavigationBar(boolean minNaviBar) {
        this.mPolicy.updateNavigationBar(minNaviBar);
        synchronized (this.mWindowMap) {
            if (mSupporInputMethodFilletAdaptation && getDefaultDisplayContentLocked().getRotation() == 0 && this.mInputMethodWindow != null && this.mInputMethodWindow.isImeWithHwFlag() && this.mInputMethodWindow.isVisible() && this.mInputMethodWindow.mWinAnimator.mInsetSurfaceOverlay != null) {
                if (minNaviBar) {
                    this.mInputMethodWindow.showInsetSurfaceOverlayImmediately();
                } else {
                    this.mHwHandler.postDelayed(new Runnable() {
                        public void run() {
                            synchronized (HwWindowManagerService.this.mWindowMap) {
                                if (HwWindowManagerService.this.mInputMethodWindow != null) {
                                    HwWindowManagerService.this.mInputMethodWindow.hideInsetSurfaceOverlayImmediately();
                                }
                            }
                        }
                    }, 300);
                }
            }
        }
    }

    private void rotateWithHoldDialog() {
        this.mHandler.removeMessages(17);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(17));
        this.mHandler.removeMessages(11);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(11));
    }

    public void setPCScreenDisplayMode(int mode) {
        String propVal = "normal";
        switch (mode) {
            case 1:
                propVal = "minor";
                break;
            case 2:
                propVal = "smaller";
                break;
        }
        SystemProperties.set("hw.pc.display.mode", propVal);
        synchronized (this.mWindowMap) {
            this.mHwWMSEx.performhwLayoutAndPlaceSurfacesLocked();
        }
    }

    public int getPCScreenDisplayMode() {
        String strMode = SystemProperties.get("hw.pc.display.mode");
        if (strMode.equals("minor")) {
            return 1;
        }
        if (strMode.equals("smaller")) {
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
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    HwWindowManagerService.lambda$getLayerIndex$0(HwWindowManagerService.this, this.f$1, (WindowState) obj);
                }
            }, false);
        } catch (Exception e) {
            Slog.w(TAG, "getLayerIndex exception!");
        }
        return this.mLayerIndex;
    }

    public static /* synthetic */ void lambda$getLayerIndex$0(HwWindowManagerService hwWindowManagerService, String appName, WindowState ws) {
        if (ws.getWindowTag().toString().indexOf(appName) > -1) {
            hwWindowManagerService.mLayerIndex = ws.mLayer;
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
        if (SystemClock.elapsedRealtime() - this.mSetTime < MemoryConstant.MIN_INTERVAL_OP_TIMEOUT && (win.mAttrs.type == 2012 || win.mAttrs.type == 2011)) {
            String str = TAG;
            Slog.d(str, "KeyguardGoingAway:" + this.mKeyguardGoingAway + ";Realtime():" + SystemClock.elapsedRealtime() + ";mSetTime:" + this.mSetTime + ";type:" + win.mAttrs.type + " reval = " + ret + " win:" + win);
        }
        return ret;
    }

    public void registerWindowObserver(IWindowLayoutObserver observer, long period) throws RemoteException {
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
            this.mWindowLayoutObserver = observer;
            WindowState ws = HwWindowManagerService.super.getFocusedWindow();
            synchronized (this.mWindowMap) {
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
            if (this.mCurrentWindow != null) {
                try {
                    this.mCurrentWindow.registerWindowObserver(observer, period);
                } catch (RemoteException e) {
                    Slog.w(TAG, "registerWindowObserver get RemoteException");
                }
            }
        }
    }

    public void unRegisterWindowObserver(IWindowLayoutObserver observer) throws RemoteException {
        if (checkCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "unRegisterWindowObserver()")) {
            if (this.mCurrentWindow != null) {
                try {
                    this.mCurrentWindow.unRegisterWindowObserver(observer);
                } catch (RemoteException e) {
                    Slog.w(TAG, "unRegisterWindowObserver get RemoteException");
                }
            }
            this.mWindowLayoutObserver = null;
            this.mCurrentWindow = null;
            String str = TAG;
            Slog.d(str, "unRegisterWindowObserver OK, observer = " + observer);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0082, code lost:
        if (r1.size() <= 0) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0084, code lost:
        r2 = r1.size() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0089, code lost:
        if (r2 < 0) goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x008b, code lost:
        r3 = r1.get(r2).intValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0095, code lost:
        if (r3 < 0) goto L_0x00a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0097, code lost:
        r7.mHandler.sendMessage(r7.mHandler.obtainMessage(104, r3, 0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00a4, code lost:
        r2 = r2 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00ab, code lost:
        if (android.util.HwPCUtils.isValidExtDisplayId(r8) == 0) goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00b3, code lost:
        if (r10.equals("handleTapOutsideTask-1-1") == false) goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00b5, code lost:
        setPCLauncherFocused(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00bc, code lost:
        if (r0 == getPCLauncherFocused()) goto L_0x00d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00c4, code lost:
        if (r10.equals("handleTapOutsideTaskXY") != false) goto L_0x00d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c6, code lost:
        r2 = r7.mWindowMap;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00c8, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        r3 = r7.mRoot.getDisplayContent(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00cf, code lost:
        if (r3 == null) goto L_0x00d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00d1, code lost:
        r3.layoutAndAssignWindowLayersIfNeeded();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00d4, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00d9, code lost:
        return;
     */
    public void setFocusedDisplay(int displayId, boolean findTopTask, String reason) {
        boolean oldPCLauncherFocused = getPCLauncherFocused();
        List<Integer> tasks = new ArrayList<>();
        synchronized (this.mWindowMap) {
            DisplayContent dc = this.mRoot.getDisplayContent(displayId);
            if (dc != null) {
                if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad() && "lockScreen".equals(reason)) {
                    return;
                }
                if (dc.getDisplayId() != this.mFocusedDisplayId || (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad() && "unlockScreen".equals(reason))) {
                    this.mFocusedDisplayId = dc.getDisplayId();
                    if (this.mFocusedDisplayId == 0) {
                        setPCLauncherFocused(false);
                    }
                    if (findTopTask && (dc instanceof HwDisplayContent)) {
                        tasks = ((HwDisplayContent) dc).taskIdFromTop();
                    }
                    if (!HwPCUtils.enabledInPad() && (this.mHardKeyboardAvailable || HwPCUtils.mTouchDeviceID != -1)) {
                        relaunchIMEProcess();
                    }
                    updateFocusedWindowLocked(0, true);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyHardKeyboardStatusChange() {
        HwWindowManagerService.super.notifyHardKeyboardStatusChange();
        if (!HwPCUtils.enabledInPad()) {
            relaunchIMEProcess();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        return 0;
     */
    public int getWindowSystemUiVisibility(IBinder token) {
        synchronized (this.mWindowMap) {
            AppWindowToken appToken = this.mRoot.getAppWindowToken(token);
            if (appToken != null) {
                WindowState win = appToken.findMainWindow();
                if (win != null) {
                    int systemUiVisibility = win.getSystemUiVisibility();
                    return systemUiVisibility;
                }
            }
        }
    }

    public void setPCLauncherFocused(boolean focus) {
        synchronized (this.mWindowMap) {
            if (focus != this.mPCLauncherFocused) {
                this.mPCLauncherFocused = focus;
            }
        }
    }

    private void relaunchIMEProcess() {
        if (this.mPCManager == null) {
            this.mPCManager = HwPCUtils.getHwPCManager();
        }
        if (this.mPCManager != null) {
            try {
                this.mPCManager.relaunchIMEIfNecessary();
            } catch (RemoteException e) {
                Log.e(TAG, "relaunchIMEProcess()");
            }
        }
    }

    public int getFocusedDisplayId() {
        if (HwPCUtils.isPcCastModeInServer()) {
            return this.mFocusedDisplayId;
        }
        return 0;
    }

    public void togglePCMode(boolean pcmode, int displayId) {
        if (pcmode) {
            if (this.mPolicy instanceof HwPhoneWindowManager) {
                HwPCUtils.log(TAG, "registerExternalPointerEventListener for screenlock");
                this.mPolicy.registerExternalPointerEventListener();
            }
            return;
        }
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            HwPCUtils.log(TAG, "unRegisterExternalPointerEventListener for screenlock");
            this.mPolicy.unRegisterExternalPointerEventListener();
        }
        synchronized (this.mWindowMap) {
            DisplayContent dc = this.mRoot.getDisplayContent(displayId);
            if (dc != null && (dc instanceof HwDisplayContent)) {
                ((HwDisplayContent) dc).togglePCMode(pcmode);
            }
        }
        setFocusedDisplay(0, true, "resetToDefault");
    }

    public Bitmap getDisplayBitmap(int displayId, int width, int height) {
        ArrayList<WindowState> windows = new ArrayList<>();
        synchronized (this.mWindowMap) {
            this.mRoot.forAllWindows(new ToBooleanFunction(windows) {
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean apply(Object obj) {
                    return HwWindowManagerService.lambda$getDisplayBitmap$1(this.f$0, (WindowState) obj);
                }
            }, false);
        }
        if (windows.size() > 0) {
            return null;
        }
        return SurfaceControl.screenshot(this.mDisplayManagerInternal.getDisplayToken(displayId), width, height);
    }

    static /* synthetic */ boolean lambda$getDisplayBitmap$1(ArrayList windows, WindowState w) {
        if (w == null || !HwPCUtils.isValidExtDisplayId(w.getDisplayId()) || w.mAttrs == null || (w.mAttrs.flags & 8192) == 0 || !w.isVisible()) {
            return false;
        }
        windows.add(w);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void getStableInsetsLocked(int displayId, Rect outInsets) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            HwWindowManagerService.super.getStableInsetsLocked(displayId, outInsets);
            return;
        }
        outInsets.setEmpty();
        DisplayContent dc = this.mRoot.getDisplayContent(displayId);
        if (dc != null) {
            DisplayInfo di = dc.getDisplayInfo();
            this.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets, displayId, di.displayCutout);
        }
    }

    public DisplayManager getDisplayManager() {
        return this.mDisplayManager;
    }

    public WindowManagerPolicy getPolicy() {
        return this.mPolicy;
    }

    private boolean isDisplayIdInVrMode(int displayId) {
        if (this.mDisplayManager != null) {
            Display display = this.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                DisplayInfo displayInfo = new DisplayInfo();
                if (display.getDisplayInfo(displayInfo)) {
                    int width = displayInfo.getNaturalWidth();
                    int height = displayInfo.getNaturalHeight();
                    if (width == 2880 && height == 1600) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void onDisplayAdded(int displayId) {
        HwWindowManagerService.super.onDisplayAdded(displayId);
        if (isDisplayIdInVrMode(displayId)) {
            String str = TAG;
            Log.i(str, "onDisplayAdded, displayId = " + displayId + " is VR mode");
            this.mVrMananger.setVRDisplayID(displayId, true);
            return;
        }
        if (this.mPCManager == null) {
            this.mPCManager = HwPCUtils.getHwPCManager();
        }
        if (this.mPCManager != null) {
            try {
                this.mPCManager.scheduleDisplayAdded(displayId);
            } catch (RemoteException e) {
                Log.e(TAG, "onDisplayAdded()");
            }
        }
        sendDisplayStateBroadcast(true, displayId);
    }

    public void onDisplayChanged(int displayId) {
        HwWindowManagerService.super.onDisplayChanged(displayId);
        if (isDisplayIdInVrMode(displayId)) {
            String str = TAG;
            Log.i(str, "onDisplayChanged, displayId = " + displayId + " is VR mode");
            this.mVrMananger.setVRDisplayID(displayId, true);
            return;
        }
        if (this.mPCManager == null) {
            this.mPCManager = HwPCUtils.getHwPCManager();
        }
        if (this.mPCManager != null) {
            try {
                this.mPCManager.scheduleDisplayChanged(displayId);
            } catch (RemoteException e) {
                Log.e(TAG, "onDisplayChanged()");
            }
        }
    }

    public void onDisplayRemoved(int displayId) {
        HwWindowManagerService.super.onDisplayRemoved(displayId);
        if (this.mVrMananger.isValidVRDisplayId(displayId)) {
            String str = TAG;
            Log.i(str, "onDisplayRemoved, displayId = " + displayId + " is VR mode");
            this.mVrMananger.setVRDisplayID(-1, false);
            this.mVrMananger.setVirtualScreenMode(false);
            return;
        }
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            setFocusedDisplay(0, true, "resetToDefault");
        }
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            this.mPolicy.resetCurrentNaviBarHeightExternal();
        }
        if (this.mPCManager == null) {
            this.mPCManager = HwPCUtils.getHwPCManager();
        }
        if (this.mPCManager != null) {
            try {
                this.mPCManager.scheduleDisplayRemoved(displayId);
            } catch (RemoteException e) {
                Log.e(TAG, "onDisplayRemoved()");
            }
        }
        sendDisplayStateBroadcast(false, displayId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008b, code lost:
        com.android.server.wm.HwWindowManagerService.super.addWindowToken(r6, r7, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008e, code lost:
        return;
     */
    public void addWindowToken(IBinder binder, int type, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
            synchronized (this.mWindowMap) {
                if (HwPCUtils.isPcCastModeInServer() && type == 2011) {
                    if (this.mHardKeyboardAvailable || HwPCUtils.mTouchDeviceID != -1) {
                        displayId = getFocusedDisplayId();
                        String str = TAG;
                        Slog.i(str, "addInputMethodWindowToken: displayId = " + displayId);
                    }
                    if (HwPCUtils.enabledInPad()) {
                        displayId = HwPCUtils.getPCDisplayID();
                        String str2 = TAG;
                        Slog.v(str2, "addWindowToken: displayId = " + displayId);
                    }
                }
                if (HwPCUtils.isValidExtDisplayId(displayId) && this.mDisplayManager.getDisplay(displayId) == null) {
                    Slog.w("WindowManager", "addWindowToken: Attempted to add binder token: " + binder + " for non-exiting displayId=" + displayId);
                }
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isTokenFound(IBinder binder, DisplayContent dc) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return false;
        }
        for (int i = 0; i < this.mRoot.mChildren.size(); i++) {
            DisplayContent displayContent = (DisplayContent) this.mRoot.mChildren.get(i);
            if (displayContent.getDisplayId() != dc.getDisplayId()) {
                WindowToken windowToken = displayContent.getWindowToken(binder);
                if (windowToken != null && windowToken.windowType == 2011) {
                    displayContent.removeWindowToken(binder);
                    String str = TAG;
                    HwPCUtils.log(str, "removeWindowToken isTokenFound in display:" + displayContent.getDisplayId());
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isSecureLocked(WindowState w) {
        if (!HwPCUtils.isExtDynamicStack(w.getStackId()) || (w.getDisplayInfo().flags & 2) != 0) {
            return HwWindowManagerService.super.isSecureLocked(w);
        }
        return false;
    }

    private void sendDisplayStateBroadcast(boolean isAdded, int displayId) {
        if (!(!SystemProperties.getBoolean("ro.config.vrbroad", false) || displayId == 0 || this.mContext == null)) {
            Intent intent = new Intent();
            String str = TAG;
            Log.i(str, "send broadcast displayState, displayId:" + displayId + " isAdded:" + isAdded);
            intent.setAction("com.huawei.display.vr.added");
            intent.setPackage("com.huawei.vrservice");
            intent.putExtra("displayId", displayId);
            if (isAdded) {
                intent.putExtra("displayState", XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
            } else {
                intent.putExtra("displayState", "off");
            }
            this.mContext.sendBroadcast(intent, "com.huawei.display.vr.permission");
        }
    }

    public void updateFingerprintSlideSwitch() {
        if (HwPCUtils.enabled() && (this.mInputManager instanceof HwInputManagerService)) {
            this.mInputManager.updateFingerprintSlideSwitchValue();
        }
    }

    public void startIntelliServiceFR(final int orientation) {
        this.mHwHandler.post(new Runnable() {
            public void run() {
                if (IntelliServiceManager.isIntelliServiceEnabled(HwWindowManagerService.this.mContext, orientation, HwWindowManagerService.this.mCurrentUserId)) {
                    IntelliServiceManager.getInstance(HwWindowManagerService.this.mContext).startIntelliService(HwWindowManagerService.this.mFaceRotationCallback);
                } else {
                    IntelliServiceManager.getInstance(HwWindowManagerService.this.mContext).setKeepPortrait(false);
                }
            }
        });
    }

    public WindowManagerPolicy.InputConsumer createInputConsumer(Looper looper, String name, InputEventReceiver.Factory inputEventReceiverFactory) {
        if (name != null) {
            return HwWindowManagerService.super.createInputConsumer(looper, name, inputEventReceiverFactory);
        }
        Slog.e(TAG, "createInputConsumer name is null");
        return null;
    }

    public boolean inputMethodClientHasFocus(IInputMethodClient client) {
        if (client != null) {
            return HwWindowManagerService.super.inputMethodClientHasFocus(client);
        }
        Slog.e(TAG, "inputMethodClientHasFocus name is null");
        return false;
    }

    public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) {
        if (client != null) {
            return HwWindowManagerService.super.openSession(callback, client, inputContext);
        }
        Slog.e(TAG, "openSession client is null");
        return null;
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
            if (this.mHoldWindow == null || !this.mHoldWindow.isVisibleLw() || (this.mHoldWindow.mAttrs.flags & 128) == 0) {
                this.mHoldWindow = null;
                this.mHoldingScreenWakeLock.release();
            }
            if (this.mPCHoldWindow == null || !this.mPCHoldWindow.isVisibleLw() || (this.mPCHoldWindow.mAttrs.flags & 128) == 0) {
                this.mPCHoldWindow = null;
                this.mPCHoldingScreenWakeLock.release();
            }
        }
    }
}
