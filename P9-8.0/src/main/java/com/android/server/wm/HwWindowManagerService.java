package com.android.server.wm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.display.DisplayManager;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.HwSlog;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IRotationWatcher;
import android.view.IWindow;
import android.view.IWindowLayoutObserver;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InputEventReceiver.Factory;
import android.view.SurfaceControl;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.InputConsumer;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.HwServiceFactory;
import com.android.server.UiThread;
import com.android.server.am.ActivityRecord;
import com.android.server.input.HwInputManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.IntelliServiceManager.FaceRotationCallback;
import com.android.server.wm.WindowManagerService.H;
import com.huawei.forcerotation.HwForceRotationManager;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import huawei.android.app.IHwWindowCallback;
import huawei.android.app.IHwWindowCallback.Stub;
import huawei.android.os.HwGeneralManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class HwWindowManagerService extends WindowManagerService {
    static final boolean DEBUG = false;
    private static final int FORBIDDEN_ADDVIEW_BROADCAST = 1;
    private static final int IBINDER_CODE_FREEZETHAWROTATION = 208;
    private static final int IBINDER_CODE_IS_KEYGUARD_DISABLE = 1000;
    private static final long MSG_ROG_FREEZE_TIME_DELEAYED = 6000;
    public static final int ROG_FREEZE_TIMEOUT = 100;
    private static final int SET_NAVIBAR_SHOWLEFT_TRANSACTION = 2201;
    private static final int SINGLE_HAND_STATE = 1989;
    private static final int SINGLE_HAND_SWITCH = 1990;
    static final String TAG = HwWindowManagerService.class.getSimpleName();
    public static final int UPDATE_NAVIGATIONBAR = 99;
    private static final int UPDATE_WINDOW_STATE = 0;
    private boolean IS_SUPPORT_PRESSURE = false;
    final int TRANSACTION_GETTOUCHCOUNTINFO = HwPackageManagerService.transaction_sendLimitedPackageBroadcast;
    final int TRANSACTION_getVisibleWindows = 1005;
    final int TRANSACTION_isDimLayerVisible = HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST;
    final int TRANSACTION_isIMEVisble = 1004;
    final int TRANSACTION_registerWindowCallback = 1002;
    final int TRANSACTION_registerWindowObserver = HwPackageManagerService.TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP;
    final int TRANSACTION_setCoverState = HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED;
    final int TRANSACTION_unRegisterWindowCallback = 1003;
    final int TRANSACTION_unregisterWindowObserver = HwPackageManagerService.TRANSACTION_CODE_SET_HDB_KEY;
    IWindow mCurrentWindow = null;
    FaceRotationCallback mFaceRotationCallback = new FaceRotationCallback() {
        public void onEvent(int faceRotation) {
            HwWindowManagerService.this.updateRotationUnchecked(false, false);
        }
    };
    AppWindowToken mFocusedAppForNavi = null;
    protected int mFocusedDisplayId = -1;
    final Handler mHandler = new H(this);
    private HandlerThread mHandlerThread = new HandlerThread("hw_ops_handler_thread");
    private boolean mHasRecord = false;
    private Handler mHwHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 99:
                    HwWindowManagerService hwWindowManagerService = HwWindowManagerService.this;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    hwWindowManagerService.updateNavigationBar(z);
                    return;
                case 100:
                    Slog.d(HwWindowManagerService.TAG, "ROG_FREEZE_TIMEOUT");
                    SurfaceControl.unfreezeDisplay();
                    return;
                default:
                    return;
            }
        }
    };
    private RectF mImeDockShownFrame = new RectF();
    boolean mIsCoverOpen = true;
    long mLastRelayoutNotifyTime;
    private int mLayerIndex = -1;
    boolean mLayoutNaviBar = false;
    private LockPatternUtils mLockPatternUtils;
    private OpsUpdateHandler mOpsHandler;
    private int mPcRotationDirection = 0;
    private int mPcRotationMode = 0;
    private final Runnable mReevaluateStatusBarSize = new Runnable() {
        public void run() {
            synchronized (HwWindowManagerService.this.mWindowMap) {
                HwWindowManagerService.this.mIgnoreFrozen = true;
                if (HwWindowManagerService.this.mLayoutNaviBar) {
                    HwWindowManagerService.this.mLayoutNaviBar = false;
                    HwWindowManagerService.this.mCurNaviConfiguration = HwWindowManagerService.this.computeNewConfigurationLocked(HwWindowManagerService.this.getDefaultDisplayContentLocked().getDisplayId());
                    if (HwWindowManagerService.this.mRoot.mWallpaperController.getWallpaperTarget() != null) {
                        HwWindowManagerService.this.mRoot.mWallpaperController.updateWallpaperVisibility();
                    }
                    HwWindowManagerService.this.performhwLayoutAndPlaceSurfacesLocked();
                } else {
                    HwWindowManagerService.this.performhwLayoutAndPlaceSurfacesLocked();
                }
            }
        }
    };
    long mRelayoutNotifyPeriod;
    private ArrayList<WindowState> mSecureScreenRecords = new ArrayList();
    private ArrayList<WindowState> mSecureScreenShot = new ArrayList();
    private volatile long mSetTime = 0;
    private SingleHandAdapter mSingleHandAdapter;
    private int mSingleHandSwitch;
    private boolean mSplitMode = false;
    private boolean mTaskChanged = false;
    private int mTempOrientation = -3;
    private AppWindowToken mTempToken = null;
    private final Handler mUiHandler;
    IWindowLayoutObserver mWindowLayoutObserver = null;

    private class OpsUpdateHandler extends Handler {
        public OpsUpdateHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    HwWindowManagerService.this.updateAppOpsState();
                    return;
                case 1:
                    HwWindowManagerService.this.sendForbiddenBroadcast(msg.getData());
                    return;
                default:
                    return;
            }
        }
    }

    private void getAlertWindows(ArrayMap<WindowState, Integer> windows) {
        synchronized (this.mWindowMap) {
            for (WindowState win : this.mWindowMap.values()) {
                if (!(win == null || win.mAttrs == null || win.mSession == null || windows.containsKey(win) || win.mAppOp != 24)) {
                    if (this.mAppOps == null) {
                        return;
                    }
                    windows.put(win, Integer.valueOf(this.mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage())));
                }
            }
        }
    }

    private void getVisibleWindows(ArrayMap<WindowState, Integer> windows, int ops) {
        if (windows != null) {
            if (ops == 45) {
                getToastWindows(windows);
            } else {
                getAlertWindows(windows);
            }
        }
    }

    private void getToastWindows(ArrayMap<WindowState, Integer> windows) {
        synchronized (this.mWindowMap) {
            for (WindowState win : this.mWindowMap.values()) {
                if (!(win == null || win.mAttrs == null || win.mSession == null || windows.containsKey(win) || win.mAttrs.type != 2005)) {
                    windows.put(win, Integer.valueOf(3));
                }
            }
        }
    }

    private void updateVisibleWindows(int eventType, int mode, int type, WindowState win) {
        Bundle args = new Bundle();
        args.putInt("window", win.mSession.mPid);
        args.putInt("windowmode", mode);
        args.putInt("relationType", eventType);
        args.putInt("hashcode", win.hashCode());
        args.putInt("windowtype", type);
        args.putInt("width", win.getAttrs().width);
        args.putInt("height", win.getAttrs().height);
        CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
        long id = Binder.clearCallingIdentity();
        HwSysResManager.getInstance().reportData(data);
        Binder.restoreCallingIdentity(id);
    }

    private void updateVisibleWindowsOps(int eventType, String pkgName) {
        Bundle args = new Bundle();
        args.putString(MemoryConstant.MEM_PREREAD_ITEM_NAME, pkgName);
        args.putInt("relationType", eventType);
        CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
        long id = Binder.clearCallingIdentity();
        HwSysResManager.getInstance().reportData(data);
        Binder.restoreCallingIdentity(id);
    }

    protected void addWindowReport(WindowState win, int mode) {
        boolean isToast = win != null && win.getAttrs().type == 2005;
        if (win != null && ((win.mAppOp == 24 || (isToast ^ 1) == 0) && win.mSession != null)) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                updateVisibleWindows(8, mode, isToast ? 45 : 24, win);
            }
        }
    }

    protected void removeWindowReport(WindowState win) {
        boolean isToast = win != null && win.getAttrs().type == 2005;
        if (win != null && ((win.mAppOp == 24 || (isToast ^ 1) == 0) && win.mSession != null)) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                updateVisibleWindows(9, 3, isToast ? 45 : 24, win);
            }
        }
    }

    protected void updateAppOpsStateReport(int ops, String packageName) {
        if (ops == 24) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                updateVisibleWindowsOps(10, packageName);
            }
        }
    }

    public HwWindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        super(context, inputManager, haveInputMethods, showBootMsgs, onlyCore, policy);
        this.mHandlerThread.start();
        this.mOpsHandler = new OpsUpdateHandler(this.mHandlerThread.getLooper());
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mUiHandler = UiThread.getHandler();
    }

    private boolean judgeSingleHandSwitchBySize() {
        return this.mContext.getResources().getBoolean(34537473);
    }

    protected void setCropOnSingleHandMode(int singleHandleMode, boolean isMultiWindowApp, int dw, int dh, Rect crop) {
        float verticalBlank = ((float) dh) * 0.25f;
        float horizontalBlank = ((float) dw) * 0.25f;
        if (singleHandleMode == 1) {
            crop.right -= (int) horizontalBlank;
        } else {
            crop.left += (int) horizontalBlank;
        }
        if (isMultiWindowApp) {
            if (crop.top == 0) {
                crop.top += (int) (((float) dh) * 0.25f);
            } else {
                crop.top = (int) ((((float) crop.top) * 0.75f) + (((float) dh) * 0.25f));
            }
            crop.bottom = (int) ((((float) crop.bottom) * 0.75f) + (((float) dh) * 0.25f));
            return;
        }
        if (crop.top > 0) {
            crop.top = (int) ((((float) crop.top) * 0.75f) + verticalBlank);
        } else {
            crop.top = (int) verticalBlank;
        }
        if (crop.bottom < dh) {
            crop.bottom = (int) (((float) crop.bottom) + (((float) (dh - crop.bottom)) * 0.25f));
        }
    }

    protected void hwProcessOnMatrix(int rotation, int width, int height, Rect frame, Matrix outMatrix) {
        switch (rotation) {
            case 1:
            case 3:
                outMatrix.postRotate(90.0f);
                outMatrix.postTranslate((float) width, 0.0f);
                return;
            default:
                return;
        }
    }

    public int addWindow(Session session, IWindow client, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) {
        if (attrs.type == 2101) {
            attrs.token = null;
        }
        int newDisplayId = HwPCUtils.getPCDisplayID();
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(newDisplayId) && newDisplayId != displayId) {
            if ("HwGlobalActions".equals(attrs.getTitle()) || "VolumeDialog".equals(attrs.getTitle()) || "com.ss.android.article.news".equals(attrs.packageName) || 2010 == attrs.type || 2011 == attrs.type || 2012 == attrs.type || DeviceStatusConstant.MSDP_DEVICE_STATUS_MOVEMENT == attrs.type || ((2009 == attrs.type && "com.android.systemui".equals(attrs.packageName)) || ((2008 == attrs.type && "com.android.systemui".equals(attrs.packageName)) || "com.google.android.marvin.talkback".equals(attrs.packageName)))) {
                HwPCUtils.log(TAG, "addWindow Title = " + attrs.getTitle() + "packageName = " + attrs.packageName + ",setdisplayId = " + newDisplayId + " oldDisplayID=" + displayId);
                return super.addWindow(session, client, seq, attrs, viewVisibility, newDisplayId, outContentInsets, outStableInsets, outOutsets, outInputChannel);
            } else if (attrs.type >= 1000 && attrs.type <= 1999) {
                WindowState parentWindow = windowForClientLocked(null, attrs.token, false);
                if (parentWindow != null && parentWindow.mAttrs.type == 2011) {
                    HwPCUtils.log(TAG, "addSubWindow Title = " + attrs.getTitle() + "packageName = " + attrs.packageName + ",setdisplayId = " + newDisplayId + " oldDisplayID=" + displayId);
                    return super.addWindow(session, client, seq, attrs, viewVisibility, newDisplayId, outContentInsets, outStableInsets, outOutsets, outInputChannel);
                }
            }
        }
        return super.addWindow(session, client, seq, attrs, viewVisibility, displayId, outContentInsets, outStableInsets, outOutsets, outInputChannel);
    }

    public void setCoverManagerState(boolean isCoverOpen) {
        this.mIsCoverOpen = isCoverOpen;
        HwServiceFactory.setIfCoverClosed(isCoverOpen ^ 1);
    }

    public boolean isCoverOpen() {
        return this.mIsCoverOpen;
    }

    public void freezeOrThawRotation(int rotation) {
        if (!checkCallingPermission("android.permission.SET_ORIENTATION", "freezeRotation()")) {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        } else if (rotation < -1 || rotation > 3) {
            throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
        } else {
            Slog.v(TAG, "freezeRotationTemporarily: rotation=" + rotation);
            if (this.mPolicy instanceof HwPhoneWindowManager) {
                ((HwPhoneWindowManager) this.mPolicy).freezeOrThawRotation(rotation);
            }
            super.updateRotationUnchecked(false, false);
        }
    }

    public void saveRotationInPcMode() {
        this.mPcRotationMode = this.mPolicy.getUserRotationMode();
        this.mPcRotationDirection = this.mContext.getDisplay().getRotation();
        HwPCUtils.log(TAG, "saveRotationInPcMode Mode=" + this.mPcRotationMode + " Direction=" + this.mPcRotationDirection);
    }

    public void restoreRotationInPcMode() {
        HwPCUtils.log(TAG, "restoreRotationInPcMode Mode=" + this.mPcRotationMode + " Direction=" + this.mPcRotationDirection);
        this.mPolicy.setUserRotationMode(this.mPcRotationMode, this.mPcRotationDirection);
    }

    public boolean isKeyguardOccluded() {
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            return ((HwPhoneWindowManager) this.mPolicy).isKeyguardOccluded();
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:91:0x0390  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int result;
        switch (code) {
            case 201:
                data.enforceInterface("android.view.IWindowManager");
                result = getCurrentFloatWindowTotal(data.readString());
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case 202:
                data.enforceInterface("android.view.IWindowManager");
                result = getInitFloatPosition(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case 203:
                data.enforceInterface("android.view.IWindowManager");
                rotateWithHoldDialog();
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.HTTP_REACHALBE_GOOLE /*204*/:
                data.enforceInterface("android.view.IWindowManager");
                this.mHwHandler.sendMessage(this.mHwHandler.obtainMessage(99, data.readInt(), 0));
                reply.writeNoException();
                return true;
            case 205:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).swipeFromTop();
                }
                reply.writeNoException();
                return true;
            case 206:
                data.enforceInterface("android.view.IWindowManager");
                boolean isTopIsFullscreen = false;
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    isTopIsFullscreen = ((HwPhoneWindowManager) this.mPolicy).isTopIsFullscreen();
                }
                reply.writeInt(isTopIsFullscreen ? 1 : 0);
                return true;
            case 207:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).showHwTransientBars();
                }
                return true;
            case 209:
                data.enforceInterface("android.view.IWindowManager");
                freezeOrThawRotation(data.readInt());
                reply.writeNoException();
                return true;
            case 1001:
                data.enforceInterface("android.view.IWindowManager");
                boolean result2 = this.mLockPatternUtils.isLockScreenDisabled(0);
                reply.writeNoException();
                reply.writeInt(result2 ? 1 : 0);
                return true;
            case 1002:
                data.enforceInterface("android.view.IWindowManager");
                IHwWindowCallback hwWindowCallback = Stub.asInterface(data.readStrongBinder());
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).setHwWindowCallback(hwWindowCallback);
                }
                reply.writeNoException();
                return true;
            case 1003:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).setHwWindowCallback(null);
                }
                reply.writeNoException();
                return true;
            case 1004:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mContext.checkPermission("com.huawei.permission.HUAWEI_IME_STATE_ACCESS", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    reply.writeInt(-1);
                    return true;
                }
                boolean isIMEVisible = this.mInputMethodWindow != null ? this.mInputMethodWindow.isVisibleLw() : false;
                HwSlog.d(TAG, "imeVis=" + isIMEVisible);
                reply.writeNoException();
                reply.writeInt(isIMEVisible ? 1 : 0);
                return true;
            case 1005:
                data.enforceInterface("android.view.IWindowManager");
                int ops = data.readInt();
                ArrayMap<WindowState, Integer> windows = new ArrayMap();
                getVisibleWindows(windows, ops);
                reply.writeInt(windows.size());
                for (Entry<WindowState, Integer> win : windows.entrySet()) {
                    WindowState state = (WindowState) win.getKey();
                    reply.writeInt(state.mSession.mPid);
                    reply.writeInt(((Integer) win.getValue()).intValue());
                    reply.writeInt(state.hashCode());
                    reply.writeInt(state.getAttrs().width);
                    reply.writeInt(state.getAttrs().height);
                }
                reply.writeNoException();
                return true;
            case HwPackageManagerService.transaction_sendLimitedPackageBroadcast /*1006*/:
                data.enforceInterface("android.view.IWindowManager");
                if (!(this.mPolicy instanceof HwPhoneWindowManager)) {
                    Slog.w(TAG, "onTransct->current is not hw pwm");
                    return true;
                } else if (this.mContext.checkPermission("com.huawei.permission.GET_TOUCH_COUNT_INFO", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    reply.writeIntArray(((HwPhoneWindowManager) this.mPolicy).getDefaultTouchCountInfo());
                    return true;
                } else {
                    reply.writeIntArray(((HwPhoneWindowManager) this.mPolicy).getTouchCountInfo());
                    reply.writeNoException();
                    return true;
                }
            case HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST /*1007*/:
                data.enforceInterface("android.view.IWindowManager");
                result = isDLayerVisible();
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED /*1008*/:
                data.enforceInterface("android.view.IWindowManager");
                setCoverManagerState(data.readInt() != 0);
                reply.writeNoException();
                return true;
            case HwPackageManagerService.TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP /*1009*/:
                data.enforceInterface("android.view.IWindowManager");
                registerWindowObserver(IWindowLayoutObserver.Stub.asInterface(data.readStrongBinder()), data.readLong());
                reply.writeNoException();
                return true;
            case HwPackageManagerService.TRANSACTION_CODE_SET_HDB_KEY /*1010*/:
                data.enforceInterface("android.view.IWindowManager");
                unRegisterWindowObserver(IWindowLayoutObserver.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case SINGLE_HAND_SWITCH /*1990*/:
                data.enforceInterface("android.view.IWindowManager");
                reply.writeNoException();
                reply.writeInt(this.mLazyModeOn);
                return true;
            case 1991:
                Slog.i(TAG, "mSingleHandSwitch =" + this.mSingleHandSwitch);
                this.mSingleHandSwitch = judgeSingleHandSwitchBySize() ? 1 : 0;
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
                    ((HwPhoneWindowManager) this.mPolicy).setNavibarAlignLeftWhenLand(data.readInt() == 1);
                }
                return true;
            default:
                try {
                    return super.onTransact(code, data, reply, flags);
                } catch (RuntimeException e) {
                    if (!(e instanceof SecurityException)) {
                    }
                    throw e;
                }
        }
        if (e instanceof SecurityException) {
            Slog.w(TAG, "Window Manager Crash");
        }
        throw e;
    }

    public int isDLayerVisible() {
        return getDefaultDisplayContentLocked().getDockedDividerController().mDimLayer.mShowing ? 1 : 0;
    }

    public Bitmap getTaskSnapshotForPc(int displayId, IBinder binder) {
        synchronized (this.mWindowMap) {
            DisplayContent dc = this.mRoot.getDisplayContentOrCreate(displayId);
            if (dc == null) {
                return null;
            }
            AppWindowToken appWindowToken = dc.getAppWindowToken(binder);
            if (appWindowToken == null) {
                return null;
            }
            IBinder displayToken = this.mDisplayManagerInternal.getDisplayToken(displayId);
            DisplayContent displayContent = appWindowToken.mDisplayContent;
            if (displayContent instanceof HwDisplayContent) {
                GraphicBuffer buffer = ((HwDisplayContent) displayContent).screenshotApplicationsToBufferForExternalDisplay(displayToken, appWindowToken.token, -1, -1, false, 1.0f, false, true);
                WindowState windowState = appWindowToken.findMainWindow();
                if (buffer == null || windowState == null) {
                    return null;
                }
                if (new Rect(0, 0, dc.getDisplayInfo().logicalWidth, dc.getDisplayInfo().logicalHeight).contains(windowState.getFrameLw())) {
                    Bitmap createHardwareBitmap = Bitmap.createHardwareBitmap(buffer);
                    return createHardwareBitmap;
                }
                return null;
            }
            return null;
        }
    }

    private void updateNavigationBar(boolean minNaviBar) {
        this.mPolicy.updateNavigationBar(minNaviBar);
    }

    public void setFocusedAppForNavi(IBinder token) {
        if (token == null) {
            this.mFocusedAppForNavi = null;
        } else {
            AppWindowToken newFocus;
            synchronized (this.mWindowMap) {
                newFocus = this.mRoot.getAppWindowToken(token);
            }
            if (newFocus == null) {
                Slog.w(TAG, "Attempted to set focus to non-existing app token: " + token);
                return;
            }
            if (this.mPolicy instanceof HwPhoneWindowManager) {
                HwPhoneWindowManager policy = this.mPolicy;
                if (policy.getHwWindowCallback() != null) {
                    ActivityRecord r = ActivityRecord.forToken(newFocus.token);
                    if (r != null) {
                        Slog.d(TAG, "setFocuedApp r: " + r + ",pkgName=" + r.info.applicationInfo.packageName);
                    } else {
                        Slog.d(TAG, "setFocuedApp r: " + r);
                    }
                    if (!(r == null || (r.info.applicationInfo.packageName.equals("com.android.gallery3d") ^ 1) == 0)) {
                        try {
                            Slog.d(TAG, "setFocuedApp focusedAppChanged");
                            policy.getHwWindowCallback().focusedAppChanged();
                        } catch (Exception ex) {
                            Slog.w(TAG, "mIHwWindowCallback focusedAppChanged", ex);
                        }
                    }
                }
            }
            this.mFocusedAppForNavi = newFocus;
        }
    }

    public int[] setNewDisplayOverrideConfiguration(Configuration overrideConfig, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setNewDisplayOverrideConfiguration()")) {
            synchronized (this.mWindowMap) {
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    HwPhoneWindowManager policy = this.mPolicy;
                    if (!(policy.getHwWindowCallback() == null || (this.mRoot.getDisplayContent(displayId).getConfiguration().diff(overrideConfig) & 128) == 0)) {
                        Slog.v(TAG, "setNewConfiguration notify window callback");
                        try {
                            policy.getHwWindowCallback().handleConfigurationChanged();
                        } catch (Exception ex) {
                            Slog.w(TAG, "mIHwWindowCallback handleConfigurationChanged", ex);
                        }
                    }
                }
            }
            return super.setNewDisplayOverrideConfiguration(overrideConfig, displayId);
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void setNaviBarFlag() {
        this.mPolicy.setInputMethodWindowVisible(this.mInputMethodWindow == null ? false : this.mInputMethodWindow.isVisibleLw());
        if (this.mFocusedAppForNavi != null) {
            this.mPolicy.setNaviBarFlag(this.mFocusedAppForNavi.navigationBarHide);
        }
    }

    public void reevaluateStatusBarSize(boolean layoutNaviBar) {
        synchronized (this.mWindowMap) {
            this.mLayoutNaviBar = layoutNaviBar;
            this.mH.post(this.mReevaluateStatusBarSize);
        }
    }

    public Configuration getCurNaviConfiguration() {
        return this.mCurNaviConfiguration;
    }

    private void rotateWithHoldDialog() {
        this.mHandler.removeMessages(17);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(17));
        this.mHandler.removeMessages(11);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(11));
    }

    protected void sendUpdateAppOpsState() {
        this.mOpsHandler.removeMessages(0);
        this.mOpsHandler.sendEmptyMessage(0);
    }

    protected void setAppOpHideHook(WindowState win, boolean visible) {
        if (!visible) {
            setAppOpVisibilityChecked(win, visible);
        }
    }

    protected void setAppOpVisibilityLwHook(WindowState win, int mode) {
        LayoutParams attrs = win.mAttrs;
        if (mode == 0 || mode == 3) {
            setAppOpVisibilityChecked(win, true);
        } else {
            setAppOpVisibilityChecked(win, false);
        }
    }

    private boolean setAppOpVisibilityChecked(WindowState win, boolean visible) {
        if (visible) {
            setWinAndChildrenVisibility(win, true);
            return true;
        } else if (allowAnyway(win)) {
            setWinAndChildrenVisibility(win, true);
            return true;
        } else {
            setWinAndChildrenVisibility(win, false);
            sendForbiddenMessage(win);
            return false;
        }
    }

    private void setWinAndChildrenVisibility(WindowState win, boolean visible) {
        if (win != null) {
            win.setAppOpVisibilityLw(visible);
            int N = win.mChildren.size();
            Slog.i(TAG, "this win:" + win + " hase children size:" + N);
            for (int i = 0; i < N; i++) {
                setWinAndChildrenVisibility((WindowState) win.mChildren.get(i), visible);
            }
        }
    }

    private boolean allowAnyway(WindowState win) {
        if (win == null) {
            return true;
        }
        if (!checkFullWindowWithoutTransparent(win.mAttrs)) {
            return false;
        }
        Slog.i(TAG, "checkFullWindowWithoutTransparent = true , don't allow anyway," + win);
        return false;
    }

    private boolean isWinInTopTask(WindowState win) {
        if (this.mFocusedApp == null) {
            return false;
        }
        ActivityRecord r = ActivityRecord.forToken(this.mFocusedApp.token);
        if (r == null || r.info == null || r.info.applicationInfo == null) {
            return false;
        }
        boolean isTop = win.getOwningUid() == r.info.applicationInfo.uid;
        if (isTop) {
            Slog.i(TAG, "there is a top app's flow view:" + win);
        }
        return isTop;
    }

    private void sendForbiddenMessage(WindowState win) {
        Message msg = this.mOpsHandler.obtainMessage(1);
        Bundle bundle = new Bundle();
        bundle.putInt("uid", win.getOwningUid());
        bundle.putString("package", win.getOwningPackage());
        msg.setData(bundle);
        this.mOpsHandler.sendMessage(msg);
    }

    private void sendForbiddenBroadcast(Bundle data) {
        Intent preventIntent = new Intent("com.android.server.wm.addview.preventnotify");
        preventIntent.putExtras(data);
        this.mContext.sendBroadcastAsUser(preventIntent, UserHandle.ALL);
    }

    private boolean checkFullWindowWithoutTransparent(LayoutParams attrs) {
        if (-1 == attrs.width && -1 == attrs.height && 0.0d != ((double) attrs.alpha)) {
            return true;
        }
        return false;
    }

    public void setFocusedApp(IBinder token, boolean moveFocusNow) {
        synchronized (this.mWindowMap) {
            AppWindowToken newFocus;
            if (token == null) {
                newFocus = null;
            } else {
                newFocus = this.mRoot.getAppWindowToken(token);
                if (newFocus == null) {
                    Slog.w(TAG, "Attempted to set focus to non-existing app token: " + token);
                }
            }
            if ((this.mFocusedApp != newFocus) && newFocus != null) {
                int requestedOrientation = newFocus.mOrientation;
                if (requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11) {
                    Slog.i(TAG, "setFocusedApp token: " + token + " requestedOrientation: " + requestedOrientation);
                    Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
                }
            }
        }
        super.setFocusedApp(token, moveFocusNow);
        if (this.mTaskChanged) {
            this.mTaskChanged = false;
            Slog.v(TAG, "setFocusedApp update app ops, mTaskChanged set to:" + this.mTaskChanged);
            sendUpdateAppOpsState();
        }
    }

    protected void setVisibleFromParent(WindowState win) {
        if (parentHiddenByAppOp(win)) {
            Slog.i(TAG, "parent is hidden by app ops, should also hide this win:" + win);
            setWinAndChildrenVisibility(win, false);
        }
    }

    private boolean parentHiddenByAppOp(WindowState win) {
        if (win == null || !win.isChildWindow()) {
            return false;
        }
        if (win.getParentWindow().mAppOpVisibility) {
            return parentHiddenByAppOp(win.getParentWindow());
        }
        return true;
    }

    public int getCurrentFloatWindowTotal(String titlePrefix) {
        ArrayList<WindowState> hwFloatWindows = getFloatWindowList();
        int count = hwFloatWindows.size();
        if (titlePrefix == null) {
            return count;
        }
        for (int i = 0; i < count; i++) {
            if (titlePrefix.equals(((WindowState) hwFloatWindows.get(i)).mAttrs.getTitle().toString())) {
                return i;
            }
        }
        return count;
    }

    private ArrayList<WindowState> getFloatWindowList() {
        String prefix = "com.huawei.FloatWindow";
        ArrayList<WindowState> hwFloatWindows = new ArrayList();
        getDefaultDisplayContentLocked().forAllWindows(new -$Lambda$2llf7xFHC_eOUHiFBaZbS1vcCvs(hwFloatWindows), false);
        return hwFloatWindows;
    }

    static /* synthetic */ void lambda$-com_android_server_wm_HwWindowManagerService_60641(ArrayList hwFloatWindows, WindowState win) {
        if (win.mAttrs.getTitle().toString().startsWith("com.huawei.FloatWindow")) {
            hwFloatWindows.add(win);
        }
    }

    public int getInitFloatPosition(int initX, int initY, int offsetX, int offsetY) {
        ArrayList<WindowState> hwFloatWindows = getFloatWindowList();
        int posX = initX;
        int posY = initY;
        int count = hwFloatWindows.size();
        if (count == 0) {
            return (initX << 16) | initY;
        }
        DisplayInfo displayInfo = getDefaultDisplayContentLocked().getDisplayInfo();
        int dw = displayInfo.appWidth;
        int dh = displayInfo.appHeight;
        for (int i = count - 1; i >= 0; i--) {
            WindowState win = (WindowState) hwFloatWindows.get(i);
            int left = win.mFrame.left;
            int top = win.mFrame.top;
            int right = win.mFrame.right;
            int bottom = win.mFrame.bottom;
            posX = initX;
            posY = initY;
            if ((offsetX >> 2) + left > 0 && right - offsetX < dw) {
                posX = left + offsetX;
                if (top > 0 && top < (dw >> 1) && bottom < dh) {
                    posY = top + offsetY;
                    break;
                }
                posX = initX;
                posY = initY;
            }
        }
        return (posX << 16) | posY;
    }

    public void systemReady() {
        int i = 0;
        super.systemReady();
        if (judgeSingleHandSwitchBySize()) {
            i = 1;
        }
        this.mSingleHandSwitch = i;
        Slog.i(TAG, "WMS systemReady mSingleHandSwitch = " + this.mSingleHandSwitch);
        if (this.mSingleHandSwitch > 0) {
            this.mSingleHandAdapter = new SingleHandAdapter(this.mContext, this.mHandler, this.mUiHandler, this);
            this.mSingleHandAdapter.registerLocked();
        }
        this.IS_SUPPORT_PRESSURE = HwGeneralManager.getInstance().isSupportForce();
    }

    public int getLazyMode() {
        return this.mLazyModeOn;
    }

    public void setLazyMode(int lazyMode) {
        Slog.i(TAG, "cur: " + this.mLazyModeOn + " to: " + lazyMode);
        if (this.mLazyModeOn != lazyMode) {
            this.mLazyModeOn = lazyMode;
        }
    }

    protected void checkKeyguardDismissDoneLocked() {
        if (this.mKeyguardDismissDoneCallback != null) {
            if (this.mKeyguardWin != null) {
                int delay = 0;
                int wallpaperLayerUpdated = this.mKeyguardAttachWallpaper ^ 1;
                if (wallpaperLayerUpdated == 0 && this.mTopWallpaperWin != null) {
                    wallpaperLayerUpdated = this.mTopWallpaperWin.mWinAnimator.mSurfaceController.mSurfaceShown ? this.mTopWallpaperAnimLayer != this.mTopWallpaperWin.mWinAnimator.mSurfaceController.mSurfaceLayer ? 1 : 0 : 1;
                    delay = !this.mTopWallpaperWin.mWinAnimator.mSurfaceController.mSurfaceShown ? 60 : 0;
                }
                if (!(this.mKeyguardWin.mHasSurface || wallpaperLayerUpdated == 0)) {
                    this.mKeyguardWin = null;
                    this.mTopWallpaperWin = null;
                    this.mH.removeMessages(100);
                    this.mH.sendEmptyMessageDelayed(101, (long) delay);
                }
            } else if (!this.mPolicy.isStatusBarKeyguardShowing()) {
                this.mH.removeMessages(100);
                this.mH.sendEmptyMessageDelayed(101, 0);
            }
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        super.setCurrentUser(newUserId, currentProfileIds);
        synchronized (this.mWindowMap) {
            ((HwInputManagerService) this.mInputManager).setCurrentUser(newUserId, currentProfileIds);
            if (this.mPolicy instanceof HwPhoneWindowManager) {
                ((HwPhoneWindowManager) this.mPolicy).setCurrentUser(newUserId, currentProfileIds);
            }
        }
    }

    public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
        super.setForcedDisplayDensityAndSize(displayId, density, width, height);
        Slog.d(TAG, "setForcedDisplayDensityAndSize size: " + width + "x" + height);
        Slog.d(TAG, "setForcedDisplayDensityAndSize density: " + density);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                    if (displayContent != null) {
                        width = Math.min(Math.max(width, 200), displayContent.mInitialDisplayWidth * 2);
                        height = Math.min(Math.max(height, 200), displayContent.mInitialDisplayHeight * 2);
                        displayContent.mBaseDisplayWidth = width;
                        displayContent.mBaseDisplayHeight = height;
                        displayContent.mBaseDisplayDensity = density;
                        this.mHwHandler.removeMessages(100);
                        this.mHwHandler.sendEmptyMessageDelayed(100, MSG_ROG_FREEZE_TIME_DELEAYED);
                        updateResourceConfiguration(displayId, density, width, height);
                        reconfigureDisplayLocked(displayContent);
                        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                        if (screenRotationAnimation != null) {
                            screenRotationAnimation.kill();
                        }
                        Global.putString(this.mContext.getContentResolver(), "display_size_forced", width + "," + height);
                        List<UserInfo> userList = UserManager.get(this.mContext).getUsers();
                        if (userList != null) {
                            for (int i = 0; i < userList.size(); i++) {
                                Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density), ((UserInfo) userList.get(i)).id);
                            }
                        }
                        SystemProperties.set("persist.sys.realdpi", density + "");
                        SystemProperties.set("persist.sys.rog.width", width + "");
                        SystemProperties.set("persist.sys.rog.height", height + "");
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void updateResourceConfiguration(int displayId, int density, int width, int height) {
        if (density == 0) {
            Slog.e(TAG, "setForcedDisplayDensityAndSize density is 0");
            return;
        }
        Slog.d(TAG, "setForcedDisplay and updateResourceConfiguration, density = " + density + " width = " + width + " height = " + height);
        Configuration mTempResourceConfiguration = new Configuration(this.mRoot.getDisplayContent(displayId).getConfiguration());
        DisplayMetrics mTempMetrics = this.mContext.getResources().getDisplayMetrics();
        mTempResourceConfiguration.densityDpi = density;
        mTempResourceConfiguration.screenWidthDp = (width * 160) / density;
        mTempResourceConfiguration.smallestScreenWidthDp = (width * 160) / density;
        mTempMetrics.density = ((float) density) / 160.0f;
        mTempMetrics.densityDpi = density;
        this.mContext.getResources().updateConfiguration(mTempResourceConfiguration, mTempMetrics);
        Slog.d(TAG, "setForcedDisplay and updateResourceConfiguration, mTempResourceConfiguration is: " + mTempResourceConfiguration);
        Slog.d(TAG, "setForcedDisplay and updateResourceConfiguration, mTempMetrics is: " + mTempMetrics);
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

    public boolean detectSafeMode() {
        if (HwDeviceManager.disallowOp(10)) {
            Slog.i(TAG, "safemode is disabled by dpm");
            this.mSafeMode = false;
            this.mPolicy.setSafeMode(this.mSafeMode);
            return this.mSafeMode;
        } else if (!"1".equals(SystemProperties.get("sys.bootfail.safemode"))) {
            return super.detectSafeMode();
        } else {
            Slog.i(TAG, "safemode is enabled eRecovery");
            this.mSafeMode = true;
            this.mPolicy.setSafeMode(this.mSafeMode);
            return this.mSafeMode;
        }
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
            displayContent.forAllWindows(new com.android.server.wm.-$Lambda$2llf7xFHC_eOUHiFBaZbS1vcCvs.AnonymousClass1(this, appName), false);
        } catch (Exception e) {
            Slog.w(TAG, "getLayerIndex exception!");
        }
        return this.mLayerIndex;
    }

    /* synthetic */ void lambda$-com_android_server_wm_HwWindowManagerService_75919(String appName, WindowState ws) {
        if (ws.getWindowTag().toString().indexOf(appName) > -1) {
            this.mLayerIndex = ws.mLayer;
        }
    }

    public void setKeyguardGoingAway(boolean keyGuardGoingAway) {
        this.mKeyguardGoingAway = keyGuardGoingAway;
        this.mSetTime = SystemClock.elapsedRealtime();
        super.setKeyguardGoingAway(keyGuardGoingAway);
    }

    protected boolean shouldHideIMExitAnim(WindowState win) {
        boolean z = this.mKeyguardGoingAway && SystemClock.elapsedRealtime() - this.mSetTime > 100;
        return !z && (win.mAttrs.type == 2012 || win.mAttrs.type == 2011);
    }

    public void registerWindowObserver(IWindowLayoutObserver observer, long period) throws RemoteException {
        if (!checkCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "registerWindowObserver()")) {
            return;
        }
        if (period <= 0) {
            Slog.e(TAG, "registerWindowObserver with wrong period " + period);
            return;
        }
        this.mRelayoutNotifyPeriod = period;
        if (this.mRelayoutNotifyPeriod < 500) {
            this.mRelayoutNotifyPeriod = 500;
        }
        this.mLastRelayoutNotifyTime = 0;
        this.mWindowLayoutObserver = observer;
        WindowState ws = super.getFocusedWindow();
        synchronized (this.mWindowMap) {
            for (Entry<IBinder, WindowState> entry : this.mWindowMap.entrySet()) {
                if (ws == entry.getValue()) {
                    this.mCurrentWindow = IWindow.Stub.asInterface((IBinder) entry.getKey());
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
            Slog.d(TAG, "unRegisterWindowObserver OK, observer = " + observer);
        }
    }

    /* JADX WARNING: Missing block: B:37:0x006a, code:
            if (r5.size() <= 0) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:38:0x006c, code:
            r1 = r5.size() - 1;
     */
    /* JADX WARNING: Missing block: B:39:0x0072, code:
            if (r1 < 0) goto L_0x00a5;
     */
    /* JADX WARNING: Missing block: B:40:0x0074, code:
            r4 = ((java.lang.Integer) r5.get(r1)).intValue();
     */
    /* JADX WARNING: Missing block: B:41:0x007e, code:
            if (r4 < 0) goto L_0x008d;
     */
    /* JADX WARNING: Missing block: B:42:0x0080, code:
            r11.mHandler.sendMessage(r11.mHandler.obtainMessage(104, r4, 0));
     */
    /* JADX WARNING: Missing block: B:43:0x008d, code:
            r1 = r1 - 1;
     */
    /* JADX WARNING: Missing block: B:48:0x0097, code:
            if (android.util.HwPCUtils.isValidExtDisplayId(r12) == false) goto L_0x00a5;
     */
    /* JADX WARNING: Missing block: B:50:0x00a0, code:
            if (r14.equals("handleTapOutsideTask-1-1") == false) goto L_0x00a5;
     */
    /* JADX WARNING: Missing block: B:51:0x00a2, code:
            setPCLauncherFocused(true);
     */
    /* JADX WARNING: Missing block: B:53:0x00a9, code:
            if (r3 == getPCLauncherFocused()) goto L_0x00c5;
     */
    /* JADX WARNING: Missing block: B:55:0x00b4, code:
            if ((r14.equals("handleTapOutsideTaskXY") ^ 1) == 0) goto L_0x00c5;
     */
    /* JADX WARNING: Missing block: B:56:0x00b6, code:
            r7 = r11.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:57:0x00b8, code:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:59:?, code:
            r0 = r11.mRoot.getDisplayContent(r12);
     */
    /* JADX WARNING: Missing block: B:60:0x00bf, code:
            if (r0 == null) goto L_0x00c4;
     */
    /* JADX WARNING: Missing block: B:61:0x00c1, code:
            r0.layoutAndAssignWindowLayersIfNeeded();
     */
    /* JADX WARNING: Missing block: B:62:0x00c4, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:63:0x00c5, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFocusedDisplay(int displayId, boolean findTopTask, String reason) {
        boolean oldPCLauncherFocused = getPCLauncherFocused();
        List<Integer> tasks = new ArrayList();
        synchronized (this.mWindowMap) {
            DisplayContent dc = this.mRoot.getDisplayContent(displayId);
            if (dc == null) {
            } else if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad() && "lockScreen".equals(reason)) {
            } else if (dc.getDisplayId() == this.mFocusedDisplayId) {
            } else {
                this.mFocusedDisplayId = dc.getDisplayId();
                if (this.mFocusedDisplayId == 0) {
                    setPCLauncherFocused(false);
                }
                if (findTopTask && (dc instanceof HwDisplayContent)) {
                    tasks = ((HwDisplayContent) dc).taskIdFromTop();
                }
                if (!HwPCUtils.enabledInPad() && this.mHardKeyboardAvailable) {
                    relaunchIMEProcess();
                }
                updateFocusedWindowLocked(0, true);
            }
        }
    }

    void notifyHardKeyboardStatusChange() {
        super.notifyHardKeyboardStatusChange();
        if (!HwPCUtils.enabledInPad()) {
            relaunchIMEProcess();
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0019, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            if (focus == this.mPCLauncherFocused) {
                return;
            }
            this.mPCLauncherFocused = focus;
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
        return HwPCUtils.isPcCastModeInServer() ? this.mFocusedDisplayId : 0;
    }

    public void togglePCMode(boolean pcmode, int displayId) {
        if (pcmode) {
            if (this.mPolicy instanceof HwPhoneWindowManager) {
                HwPCUtils.log(TAG, "registerExternalPointerEventListener for screenlock");
                ((HwPhoneWindowManager) this.mPolicy).registerExternalPointerEventListener();
            }
            return;
        }
        if (this.mPolicy instanceof HwPhoneWindowManager) {
            HwPCUtils.log(TAG, "unRegisterExternalPointerEventListener for screenlock");
            ((HwPhoneWindowManager) this.mPolicy).unRegisterExternalPointerEventListener();
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
        return SurfaceControl.screenshot(this.mDisplayManagerInternal.getDisplayToken(displayId), width, height);
    }

    void getStableInsetsLocked(int displayId, Rect outInsets) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            outInsets.setEmpty();
            DisplayContent dc = this.mRoot.getDisplayContent(displayId);
            if (dc != null) {
                DisplayInfo di = dc.getDisplayInfo();
                this.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets, displayId);
            }
            return;
        }
        super.getStableInsetsLocked(displayId, outInsets);
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
        super.onDisplayAdded(displayId);
        if (isDisplayIdInVrMode(displayId)) {
            Log.i(TAG, "onDisplayAdded, displayId = " + displayId + " is VR mode");
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
        super.onDisplayChanged(displayId);
        if (isDisplayIdInVrMode(displayId)) {
            Log.i(TAG, "onDisplayChanged, displayId = " + displayId + " is VR mode");
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
        super.onDisplayRemoved(displayId);
        if (isDisplayIdInVrMode(displayId)) {
            Log.i(TAG, "onDisplayRemoved, displayId = " + displayId + " is VR mode");
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

    /* JADX WARNING: Missing block: B:25:0x0083, code:
            super.addWindowToken(r6, r7, r8);
     */
    /* JADX WARNING: Missing block: B:26:0x0086, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addWindowToken(IBinder binder, int type, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
            synchronized (this.mWindowMap) {
                if (HwPCUtils.isPcCastModeInServer() && type == 2011) {
                    if (this.mHardKeyboardAvailable) {
                        displayId = getFocusedDisplayId();
                    }
                    if (HwPCUtils.enabledInPad()) {
                        displayId = HwPCUtils.getPCDisplayID();
                        Slog.v(TAG, "addWindowToken: displayId = " + displayId);
                    }
                }
                if (HwPCUtils.isValidExtDisplayId(displayId) && this.mDisplayManager.getDisplay(displayId) == null) {
                    Slog.w("WindowManager", "addWindowToken: Attempted to add binder token: " + binder + " for non-exiting displayId=" + displayId);
                    return;
                }
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    protected boolean isTokenFound(IBinder binder, DisplayContent dc) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return false;
        }
        for (int i = 0; i < this.mRoot.mChildren.size(); i++) {
            DisplayContent displayContent = (DisplayContent) this.mRoot.mChildren.get(i);
            if (displayContent.getDisplayId() != dc.getDisplayId()) {
                WindowToken windowToken = displayContent.getWindowToken(binder);
                if (windowToken != null && windowToken.windowType == 2011) {
                    displayContent.removeWindowToken(binder);
                    HwPCUtils.log(TAG, "removeWindowToken isTokenFound in display:" + displayContent.getDisplayId());
                    return true;
                }
            }
        }
        return false;
    }

    boolean isSecureLocked(WindowState w) {
        if (HwPCUtils.isPcDynamicStack(w.getStackId()) && (w.getDisplayInfo().flags & 2) == 0) {
            return false;
        }
        return super.isSecureLocked(w);
    }

    protected boolean isDisplayOkForAnimation(int width, int height, int transit, AppWindowToken atoken) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported()) {
            return okToDisplay();
        }
        if (!forceRotationManager.isForceRotationSwitchOpen(this.mContext)) {
            return okToDisplay();
        }
        if (width <= height || ((transit != 7 && transit != 6) || !forceRotationManager.isAppForceLandRotatable(atoken.appPackageName, atoken.appToken.asBinder()))) {
            return okToDisplay();
        }
        return false;
    }

    protected boolean checkAppOrientationForForceRotation(AppWindowToken aToken) {
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
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen(this.mContext) || w == null || w.isInMultiWindowMode()) {
            return;
        }
        if (w.mAppToken == null || (forceRotationManager.isAppForceLandRotatable(w.mAppToken.appPackageName, w.mAppToken.appToken.asBinder()) ^ 1) != 0) {
            Slog.v(TAG, "current window do not support force rotation mAppToken:" + w.mAppToken);
            return;
        }
        DisplayContent dc = getDefaultDisplayContentLocked();
        if (dc != null) {
            Display dp = dc.getDisplay();
            if (dp != null) {
                DisplayMetrics dm = new DisplayMetrics();
                dp.getMetrics(dm);
                LayoutParams layoutParams;
                if (dm.widthPixels < dm.heightPixels) {
                    layoutParams = w.mAttrs;
                    layoutParams.flags &= -1048577;
                } else {
                    layoutParams = w.mAttrs;
                    layoutParams.flags |= HighBitsCompModeID.MODE_COLOR_ENHANCE;
                }
            }
        }
    }

    public void prepareForForceRotation(IBinder token, String packageName, int pid, String processName) {
        synchronized (this.mWindowMap) {
            AppWindowToken aToken = this.mRoot.getAppWindowToken(token);
            if (aToken == null) {
                Slog.w(TAG, "Attempted to set orientation of non-existing app token: " + token);
                return;
            }
            aToken.appPackageName = packageName;
            aToken.appPid = pid;
            aToken.appProcessName = processName;
        }
    }

    public void prepareForForceRotation(IBinder appToken, String packageName, String componentName) {
        synchronized (this.mWindowMap) {
            AppWindowToken aToken = this.mRoot.getAppWindowToken(appToken);
            if (aToken == null) {
                Slog.w(TAG, "Attempted to set orientation of non-existing app token: " + appToken);
                return;
            }
            aToken.appPackageName = packageName;
            aToken.appComponentName = componentName;
        }
    }

    protected void setHwSecureScreen(WindowState win) {
        WindowStateAnimator winAnimator = win.mWinAnimator;
        if (winAnimator.mSurfaceController != null) {
            if ((win.mAttrs.hwFlags & 4096) != 0) {
                if (!this.mSecureScreenShot.contains(win)) {
                    winAnimator.mSurfaceController.setSecureScreenShot(true);
                    this.mSecureScreenShot.add(win);
                }
            } else if (this.mSecureScreenShot.contains(win)) {
                winAnimator.mSurfaceController.setSecureScreenShot(false);
                this.mSecureScreenShot.remove(win);
            }
            if ((win.mAttrs.hwFlags & 8192) != 0) {
                if (!this.mSecureScreenRecords.contains(win)) {
                    winAnimator.mSurfaceController.setSecureScreenRecord(true);
                    this.mSecureScreenRecords.add(win);
                }
            } else if (this.mSecureScreenRecords.contains(win)) {
                winAnimator.mSurfaceController.setSecureScreenRecord(false);
                this.mSecureScreenRecords.remove(win);
            }
        }
    }

    private void sendDisplayStateBroadcast(boolean isAdded, int displayId) {
        if (!(!SystemProperties.getBoolean("ro.config.vrbroad", false) || displayId == 0 || this.mContext == null)) {
            Intent intent = new Intent();
            Log.i(TAG, "send broadcast displayState, displayId:" + displayId + " isAdded:" + isAdded);
            intent.setAction("com.huawei.display.vr.added");
            intent.setPackage("com.huawei.vrservice");
            intent.putExtra("displayId", displayId);
            if (isAdded) {
                intent.putExtra("displayState", PreciseIgnore.COMP_SCREEN_ON_VALUE_);
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

    public void startIntelliServiceFR(int orientation) {
        if (IntelliServiceManager.isIntelliServiceEnabled(this.mContext, orientation, this.mCurrentUserId)) {
            IntelliServiceManager.getInstance(this.mContext).startIntelliService(this.mFaceRotationCallback);
        } else {
            IntelliServiceManager.getInstance(this.mContext).setKeepPortrait(false);
        }
    }

    public InputConsumer createInputConsumer(Looper looper, String name, Factory inputEventReceiverFactory) {
        if (name != null) {
            return super.createInputConsumer(looper, name, inputEventReceiverFactory);
        }
        Slog.e(TAG, "createInputConsumer name is null");
        return null;
    }

    public void createInputConsumer(String name, InputChannel inputChannel) {
        if (name == null) {
            Slog.e(TAG, "createInputConsumer name is null");
        } else {
            super.createInputConsumer(name, inputChannel);
        }
    }

    public boolean inputMethodClientHasFocus(IInputMethodClient client) {
        if (client != null) {
            return super.inputMethodClientHasFocus(client);
        }
        Slog.e(TAG, "inputMethodClientHasFocus name is null");
        return false;
    }

    public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) {
        if (client != null) {
            return super.openSession(callback, client, inputContext);
        }
        Slog.e(TAG, "openSession client is null");
        return null;
    }

    public void setDockedStackDividerTouchRegion(Rect touchRegion) {
        if (touchRegion == null) {
            Slog.e(TAG, "setDockedStackDividerTouchRegion touchRegion is null");
        } else {
            super.setDockedStackDividerTouchRegion(touchRegion);
        }
    }

    public void removeRotationWatcher(IRotationWatcher watcher) {
        if (watcher == null) {
            Slog.e(TAG, "removeRotationWatcher watcher is null");
        } else {
            super.removeRotationWatcher(watcher);
        }
    }

    public int watchRotation(IRotationWatcher watcher, int displayId) {
        if (watcher != null) {
            return super.watchRotation(watcher, displayId);
        }
        Slog.e(TAG, "watchRotation watcher is null");
        return 0;
    }
}
