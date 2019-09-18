package com.android.server.wm;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.HwPCMultiWindowCompatibility;
import android.freeform.HwFreeFormUtils;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.hardware.fingerprint.FingerprintManager;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.am.ActivityRecord;
import com.android.server.am.HwActivityRecord;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.mplink.HwMpLinkWifiImpl;
import com.android.server.input.InputWindowHandle;
import com.android.server.notch.HwNotchScreenWhiteConfig;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.google.android.collect.Sets;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.view.HwTaskSnapshotWrapper;
import com.huawei.forcerotation.HwForceRotationManager;
import huawei.android.hwutil.HwFullScreenDisplay;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public final class HwWindowManagerServiceEx implements IHwWindowManagerServiceEx {
    private static final int APP_ASSOC_WINDOWADD = 8;
    private static final int APP_ASSOC_WINDOWDEL = 9;
    private static final int APP_ASSOC_WINDOWUPDATE = 27;
    private static final int APP_ASSOC_WINDOWUPOPS = 10;
    private static final String ASSOC_PKGNAME = "pkgname";
    private static final String ASSOC_RELATION_TYPE = "relationType";
    private static final String ASSOC_UID = "uid";
    private static final String ASSOC_WINDOW = "window";
    private static final String ASSOC_WINDOW_ALPHA = "alpha";
    private static final String ASSOC_WINDOW_HASHCODE = "hashcode";
    private static final String ASSOC_WINDOW_HEIGHT = "height";
    private static final String ASSOC_WINDOW_MODE = "windowmode";
    private static final String ASSOC_WINDOW_PHIDE = "permanentlyhidden";
    private static final String ASSOC_WINDOW_TYPE = "windowtype";
    private static final String ASSOC_WINDOW_WIDTH = "width";
    private static final int FORBIDDEN_ADDVIEW_BROADCAST = 1;
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    private static final boolean IS_SUPPORT_SINGLE_MODE = SystemProperties.getBoolean("ro.feature.wms.singlemode", true);
    private static final float LAZY_SCALE = 0.75f;
    private static final int LAZY_TYPE_DEFAULT = 0;
    private static final int LAZY_TYPE_LEFT = 1;
    private static final int LAZY_TYPE_RIGHT = 2;
    private static final long MSG_ROG_FREEZE_TIME_DELEAYED = 6000;
    public static final int NOTIFY_FINGER_WIN_COVERED = 101;
    private static final String RESOURCE_APPASSOC = "RESOURCE_APPASSOC";
    private static final String RESOURCE_SYSLOAD = "RESOURCE_SYSLOAD";
    private static final int ROG_FREEZE_TIMEOUT = 100;
    private static final String SYSLOAD_SINGLEHAND_TYPE = "LazyMode";
    static final String TAG = "HwWindowManagerServiceEx";
    private static final int UPDATE_WINDOW_STATE = 0;
    final Context mContext;
    Configuration mCurNaviConfiguration;
    private HandlerThread mHandlerThread = new HandlerThread("hw_ops_handler_thread");
    private boolean mHasRecord = false;
    private Handler mHwHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    Slog.d(HwWindowManagerServiceEx.TAG, "ROG_FREEZE_TIMEOUT");
                    SurfaceControl.unfreezeDisplay();
                    return;
                case 101:
                    if (HwWindowManagerServiceEx.this.mContext.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
                        boolean z = true;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        boolean covered = z;
                        Rect frame = (Rect) msg.obj;
                        if (((FingerprintManager) HwWindowManagerServiceEx.this.mContext.getSystemService("fingerprint")) != null) {
                            Slog.i(HwWindowManagerServiceEx.TAG, "handleMessage: NOTIFY_FINGER_WIN_COVERED covered=" + covered + " frame=" + frame);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHwSafeMode;
    IHwWindowManagerInner mIWmsInner = null;
    protected boolean mIgnoreFrozen = false;
    boolean mIsCoverOpen = true;
    private Rect mLastCoveredFrame = new Rect();
    private boolean mLastCoveredState;
    boolean mLayoutNaviBar = false;
    private int mLazyModeOnEx;
    private OpsUpdateHandler mOpsHandler;
    private int mPCScreenDisplayMode = 0;
    private float mPCScreenScale = 1.0f;
    private final Runnable mReevaluateStatusBarSize = new Runnable() {
        public void run() {
            synchronized (HwWindowManagerServiceEx.this.mIWmsInner.getWindowMap()) {
                HwWindowManagerServiceEx.this.mIgnoreFrozen = true;
                if (HwWindowManagerServiceEx.this.mLayoutNaviBar) {
                    HwWindowManagerServiceEx.this.mLayoutNaviBar = false;
                    HwWindowManagerServiceEx.this.mCurNaviConfiguration = HwWindowManagerServiceEx.this.mIWmsInner.computeNewConfiguration(HwWindowManagerServiceEx.this.mIWmsInner.getDefaultDisplayContentLocked().getDisplayId());
                    if (HwWindowManagerServiceEx.this.mIWmsInner.getRoot().mWallpaperController.getWallpaperTarget() != null) {
                        HwWindowManagerServiceEx.this.mIWmsInner.getRoot().mWallpaperController.updateWallpaperVisibility();
                    }
                    HwWindowManagerServiceEx.this.performhwLayoutAndPlaceSurfacesLocked();
                } else {
                    HwWindowManagerServiceEx.this.performhwLayoutAndPlaceSurfacesLocked();
                }
            }
        }
    };
    private ArrayList<WindowState> mSecureScreenRecords = new ArrayList<>();
    private ArrayList<WindowState> mSecureScreenShot = new ArrayList<>();
    private SingleHandAdapter mSingleHandAdapter;
    private boolean mSingleHandSwitch;
    private int mTempOrientation = -3;
    private AppWindowToken mTempToken = null;
    private final Handler mUiHandler;

    private class OpsUpdateHandler extends Handler {
        public OpsUpdateHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    HwWindowManagerServiceEx.this.mIWmsInner.updateAppOpsState();
                    return;
                case 1:
                    HwWindowManagerServiceEx.this.sendForbiddenBroadcast(msg.getData());
                    return;
                default:
                    return;
            }
        }
    }

    public HwWindowManagerServiceEx(IHwWindowManagerInner wms, Context context) {
        this.mIWmsInner = wms;
        this.mContext = context;
        this.mHandlerThread.start();
        this.mOpsHandler = new OpsUpdateHandler(this.mHandlerThread.getLooper());
        this.mUiHandler = UiThread.getHandler();
    }

    public void onChangeConfiguration(MergedConfiguration mergedConfiguration, WindowState ws) {
        if (HwPCUtils.enabled() && HwPCUtils.isPcCastModeInServer() && ws != null && HwPCUtils.isValidExtDisplayId(ws.getDisplayId()) && mergedConfiguration != null && ws.getTask() != null && ws.getTask().isFullscreen()) {
            Configuration cf = mergedConfiguration.getOverrideConfiguration();
            DisplayContent dc = ws.getDisplayContent();
            if (cf != null && dc != null) {
                DisplayInfo displayInfo = ws.getDisplayInfo();
                if (displayInfo != null) {
                    int displayWidth = displayInfo.logicalWidth;
                    int displayHeight = displayInfo.logicalHeight;
                    float scale = ((float) displayInfo.logicalDensityDpi) / 160.0f;
                    cf.screenWidthDp = (int) ((((float) displayWidth) / scale) + 0.5f);
                    cf.screenHeightDp = (int) ((((float) displayHeight) / scale) + 0.5f);
                    mergedConfiguration.setOverrideConfiguration(cf);
                    ws.onConfigurationChanged(mergedConfiguration.getMergedConfiguration());
                    HwPCUtils.log(TAG, "set pc fullscreen, width:" + displayWidth + " height:" + displayHeight + " scale:" + scale + " cf.screenWidthDp:" + cf.screenWidthDp + " cf.screenHeightDp:" + cf.screenHeightDp);
                }
            }
        }
    }

    private boolean isInputTargetWindow(WindowState windowState, WindowState inputTargetWin) {
        boolean z = false;
        if (inputTargetWin == null) {
            return false;
        }
        Task inputMethodTask = inputTargetWin.getTask();
        Task task = windowState.getTask();
        if (inputMethodTask == null || task == null) {
            return false;
        }
        if (inputMethodTask.mTaskId == task.mTaskId) {
            z = true;
        }
        return z;
    }

    public void adjustWindowPosForPadPC(Rect containingFrame, Rect contentFrame, WindowState imeWin, WindowState inputTargetWin, WindowState win) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad() && HwPCUtils.isValidExtDisplayId(win.getDisplayId()) && imeWin != null && imeWin.isVisibleNow() && isInputTargetWindow(win, inputTargetWin) && win.getTask() != null) {
            int windowState = -1;
            ActivityRecord r = ActivityRecord.forToken(win.getAttrs().token);
            if (r != null) {
                if (r instanceof HwActivityRecord) {
                    windowState = ((HwActivityRecord) r).getWindowState();
                }
                if (windowState != -1 && !HwPCMultiWindowCompatibility.isLayoutFullscreen(windowState) && !HwPCMultiWindowCompatibility.isLayoutMaximized(windowState) && !contentFrame.isEmpty()) {
                    int D1 = 0;
                    int D2 = 0;
                    if (win.getAttrs() == null || (win.getAttrs().softInputMode & 240) != 16) {
                        Rect imeBounds = new Rect();
                        imeWin.getBounds(imeBounds);
                        if (!imeBounds.isEmpty() && containingFrame.bottom > imeBounds.top) {
                            D1 = imeBounds.top - containingFrame.bottom;
                            D2 = contentFrame.top - containingFrame.top;
                        }
                    } else if (containingFrame.bottom > contentFrame.bottom) {
                        D1 = contentFrame.bottom - containingFrame.bottom;
                        D2 = contentFrame.top - containingFrame.top;
                    }
                    int offsetY = D1 > D2 ? D1 : D2;
                    Rect taskBounds = new Rect();
                    if (offsetY < 0) {
                        win.getTask().getBounds(taskBounds);
                        taskBounds.offset(0, offsetY);
                        win.getTask().setBounds(taskBounds);
                        containingFrame.offset(0, offsetY);
                    }
                }
            }
        }
    }

    public void layoutWindowForPadPCMode(WindowState win, WindowState inputTargetWin, WindowState imeWin, Rect pf, Rect df, Rect cf, Rect vf, int contentBottom) {
        if (isInputTargetWindow(win, inputTargetWin)) {
            int inputMethodTop = 0;
            if (imeWin != null && imeWin.isVisibleLw()) {
                int top = (imeWin.getDisplayFrameLw().top > imeWin.getContentFrameLw().top ? imeWin.getDisplayFrameLw() : imeWin.getContentFrameLw()).top + imeWin.getGivenContentInsetsLw().top;
                inputMethodTop = contentBottom < top ? contentBottom : top;
            }
            if (inputMethodTop > 0) {
                vf.bottom = inputMethodTop;
                cf.bottom = inputMethodTop;
                df.bottom = inputMethodTop;
                pf.bottom = inputMethodTop;
            }
        }
    }

    public void sendUpdateAppOpsState() {
        this.mOpsHandler.removeMessages(0);
        this.mOpsHandler.sendEmptyMessage(0);
    }

    public void setAppOpHideHook(WindowState win, boolean visible) {
        if (!visible) {
            setAppOpVisibilityChecked(win, visible);
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

    private void sendForbiddenMessage(WindowState win) {
        Message msg = this.mOpsHandler.obtainMessage(1);
        Bundle bundle = new Bundle();
        bundle.putInt("uid", win.getOwningUid());
        bundle.putString("package", win.getOwningPackage());
        msg.setData(bundle);
        this.mOpsHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void sendForbiddenBroadcast(Bundle data) {
        Intent preventIntent = new Intent("com.android.server.wm.addview.preventnotify");
        preventIntent.putExtras(data);
        this.mContext.sendBroadcastAsUser(preventIntent, UserHandle.ALL);
    }

    private boolean checkFullWindowWithoutTransparent(WindowManager.LayoutParams attrs) {
        return -1 == attrs.width && -1 == attrs.height && 0.0d != ((double) attrs.alpha);
    }

    public void setVisibleFromParent(WindowState win) {
        if (parentHiddenByAppOp(win)) {
            Slog.i(TAG, "parent is hidden by app ops, should also hide this win:" + win);
            setWinAndChildrenVisibility(win, false);
        }
    }

    private boolean parentHiddenByAppOp(WindowState win) {
        if (win == null || !win.isChildWindow()) {
            return false;
        }
        if (!win.getParentWindow().mAppOpVisibility) {
            return true;
        }
        return parentHiddenByAppOp(win.getParentWindow());
    }

    public void checkSingleHandMode(AppWindowToken oldFocus, AppWindowToken newFocus) {
        if ((oldFocus != newFocus) && newFocus != null) {
            int requestedOrientation = newFocus.mOrientation;
            if (requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11) {
                Slog.i(TAG, "requestedOrientation: " + requestedOrientation);
                Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
            }
        }
    }

    public void updateSurfacePositionForPCMode(WindowState win, Point outPoint) {
        WindowState windowState = win;
        Point point = outPoint;
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(win.getDisplayId()) && getPCScreenDisplayMode() != 0) {
            DisplayInfo di = win.getDisplayInfo();
            if (di == null) {
                DisplayInfo displayInfo = di;
            } else if (win.getDisplayContent() == null) {
                DisplayInfo displayInfo2 = di;
            } else {
                int width = di.logicalWidth;
                int height = di.logicalHeight;
                float pcScreenScale = this.mPCScreenScale;
                Rect surfaceInsets = windowState.mAttrs.surfaceInsets;
                point.x = (int) ((((float) win.getFrameLw().left) * pcScreenScale) + ((((float) width) * (1.0f - pcScreenScale)) / 2.0f));
                point.y = (int) ((((float) win.getFrameLw().top) * pcScreenScale) + ((((float) height) * (1.0f - pcScreenScale)) / 2.0f));
                WindowContainer parentWindowContainer = win.getParent();
                if (win.isChildWindow()) {
                    WindowState parent = win.getParentWindow();
                    Rect parentSurfaceInsets = parent.mAttrs.surfaceInsets;
                    Rect parentFrame = parent.getFrameLw();
                    DisplayInfo displayInfo3 = di;
                    point.offset((int) (((float) ((-parentFrame.left) + parentSurfaceInsets.left)) * pcScreenScale), (int) (((float) ((-parentFrame.top) + parentSurfaceInsets.top)) * pcScreenScale));
                } else {
                    if (parentWindowContainer != null) {
                        Rect parentBounds = parentWindowContainer.getBounds();
                        point.offset(-((int) (((float) parentBounds.left) * pcScreenScale)), -((int) (((float) parentBounds.top) * pcScreenScale)));
                    }
                }
                TaskStack stack = win.getStack();
                if (stack != null) {
                    int outset = stack.getStackOutset();
                    point.offset((int) (((float) outset) * pcScreenScale), (int) (((float) outset) * pcScreenScale));
                }
                point.offset(-((int) (((float) surfaceInsets.left) * pcScreenScale)), -((int) (((float) surfaceInsets.top) * pcScreenScale)));
                windowState.mOverscanPosition.set(point.x, point.y);
            }
            HwPCUtils.log(TAG, "fail to get display info");
            return;
        }
    }

    public void updateDimPositionForPCMode(WindowContainer host, Rect outBounds) {
        if (HwPCUtils.isPcCastModeInServer() && getPCScreenDisplayMode() != 0) {
            int displayId = -1;
            int screenWidth = 0;
            int screenHeight = 0;
            float pcScreenScale = getPCScreenScale();
            if (host instanceof Task) {
                Task task = (Task) host;
                if (task.getDisplayContent() != null) {
                    displayId = task.getDisplayContent().getDisplayId();
                    screenWidth = task.getDisplayContent().mInitialDisplayWidth;
                    screenHeight = task.getDisplayContent().mInitialDisplayHeight;
                }
            } else if (host instanceof TaskStack) {
                TaskStack taskStack = (TaskStack) host;
                if (taskStack.getDisplayContent() != null) {
                    displayId = taskStack.getDisplayContent().getDisplayId();
                    screenWidth = taskStack.getDisplayContent().mInitialDisplayWidth;
                    screenHeight = taskStack.getDisplayContent().mInitialDisplayHeight;
                }
            }
            if (!HwPCUtils.isValidExtDisplayId(displayId)) {
                return;
            }
            if (host.getParent() == null) {
                int left = (int) ((((float) outBounds.left) * pcScreenScale) + ((((float) screenWidth) * (1.0f - pcScreenScale)) / 2.0f));
                int top = (int) ((((float) outBounds.top) * pcScreenScale) + ((((float) screenHeight) * (1.0f - pcScreenScale)) / 2.0f));
                outBounds.set(left, top, (int) (((float) left) + (((float) outBounds.width()) * pcScreenScale)), (int) (((float) top) + (((float) outBounds.height()) * pcScreenScale)));
            } else if (outBounds.right >= 0 || outBounds.bottom >= 0) {
                int left2 = (int) (((((float) outBounds.left) * pcScreenScale) + ((((float) screenWidth) * (1.0f - pcScreenScale)) / 2.0f)) - 1.0f);
                int top2 = (int) (((((float) outBounds.top) * pcScreenScale) + ((((float) screenHeight) * (1.0f - pcScreenScale)) / 2.0f)) - 1.0f);
                outBounds.set(left2, top2, ((int) ((((float) outBounds.width()) * pcScreenScale) + 1.0f)) + left2, ((int) ((((float) outBounds.height()) * pcScreenScale) + 1.0f)) + top2);
            } else {
                outBounds.left = (int) (((float) outBounds.left) * pcScreenScale);
                outBounds.top = (int) (((float) outBounds.top) * pcScreenScale);
            }
        }
    }

    public int getPCScreenDisplayMode() {
        return this.mPCScreenDisplayMode;
    }

    public float getPCScreenScale() {
        return this.mPCScreenScale;
    }

    public void computeShownFrameLockedByPCScreenDpMode(int curMode) {
        this.mPCScreenDisplayMode = curMode;
        if (curMode == 0) {
            this.mPCScreenScale = 1.0f;
        } else if (curMode == 1) {
            this.mPCScreenScale = 0.95f;
        } else if (curMode == 2) {
            this.mPCScreenScale = 0.9f;
        }
    }

    public boolean isFullScreenDevice() {
        return HwFullScreenDisplay.isFullScreenDevice();
    }

    public float getDeviceMaxRatio() {
        return HwFullScreenDisplay.getDeviceMaxRatio();
    }

    public float getDefaultNonFullMaxRatio() {
        return HwFullScreenDisplay.getDefaultNonFullMaxRatio();
    }

    public float getExclusionNavBarMaxRatio() {
        return HwFullScreenDisplay.getExclusionNavBarMaxRatio();
    }

    public void setNotchHeight(int notchHeight) {
        HwFullScreenDisplay.setNotchHeight(notchHeight);
    }

    public void getAppDisplayRect(float appMaxRatio, Rect rect, int left, int rotation) {
        HwFullScreenDisplay.getAppDisplayRect(appMaxRatio, rect, left, rotation);
    }

    public Rect getTopAppDisplayBounds(float appMaxRatio, int rotation, int screenWidth) {
        return HwFullScreenDisplay.getTopAppDisplayBounds(appMaxRatio, rotation, screenWidth);
    }

    public List<String> getNotchSystemApps() {
        return HwNotchScreenWhiteConfig.getInstance().getNotchSystemApps();
    }

    public int getAppUseNotchMode(String packageName) {
        return HwNotchScreenWhiteConfig.getInstance().getAppUseNotchMode(packageName);
    }

    public boolean isInNotchAppWhitelist(WindowState win) {
        return HwNotchScreenWhiteConfig.getInstance().isNotchAppInfo(win);
    }

    private void getAlertWindows(ArrayMap<WindowState, Integer> windows) {
        synchronized (this.mIWmsInner.getWindowMap()) {
            for (WindowState win : this.mIWmsInner.getWindowMap().values()) {
                if (!(win == null || win.mAttrs == null || win.mSession == null)) {
                    if (!windows.containsKey(win)) {
                        if (win.mAppOp == 24) {
                            if (this.mIWmsInner.getAppOps() != null) {
                                windows.put(win, Integer.valueOf(this.mIWmsInner.getAppOps().startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage())));
                            } else {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public List<Bundle> getVisibleWindows(int ops) {
        ArrayMap<WindowState, Integer> windows = new ArrayMap<>();
        getVisibleWindows(windows, ops);
        List<Bundle> windowsList = new ArrayList<>();
        for (Map.Entry<WindowState, Integer> win : windows.entrySet()) {
            WindowState state = win.getKey();
            Bundle bundle = new Bundle();
            bundle.putInt("window_pid", state.mSession.mPid);
            bundle.putInt("window_value", win.getValue().intValue());
            bundle.putInt("window_state", System.identityHashCode(state));
            bundle.putInt("window_width", state.mRequestedWidth);
            bundle.putInt("window_height", state.mRequestedHeight);
            bundle.putFloat("window_alpha", state.mAttrs.alpha);
            bundle.putBoolean("window_hidden", state.mPermanentlyHidden);
            bundle.putString("window_package", state.getOwningPackage());
            bundle.putInt("window_uid", state.getOwningUid());
            windowsList.add(bundle);
        }
        return windowsList;
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
        synchronized (this.mIWmsInner.getWindowMap()) {
            for (WindowState win : this.mIWmsInner.getWindowMap().values()) {
                if (!(win == null || win.mAttrs == null || win.mSession == null)) {
                    if (!windows.containsKey(win)) {
                        if (win.mAttrs.type == 2005) {
                            windows.put(win, 3);
                        }
                    }
                }
            }
        }
    }

    private void updateVisibleWindows(int eventType, int mode, int type, WindowState win, int requestedWidth, int requestedHeight, boolean isupdate) {
        if (requestedWidth != win.mRequestedWidth || requestedHeight != win.mRequestedHeight || !isupdate) {
            Bundle args = new Bundle();
            args.putInt(ASSOC_WINDOW, win.mSession.mPid);
            args.putInt(ASSOC_WINDOW_MODE, mode);
            args.putInt(ASSOC_RELATION_TYPE, eventType);
            args.putInt(ASSOC_WINDOW_HASHCODE, System.identityHashCode(win));
            args.putInt(ASSOC_WINDOW_TYPE, type);
            args.putInt(ASSOC_WINDOW_WIDTH, eventType == 8 ? win.getAttrs().width : requestedWidth);
            args.putInt(ASSOC_WINDOW_HEIGHT, eventType == 8 ? win.getAttrs().height : requestedHeight);
            args.putFloat(ASSOC_WINDOW_ALPHA, win.getAttrs().alpha);
            args.putBoolean(ASSOC_WINDOW_PHIDE, win.mPermanentlyHidden);
            args.putString("pkgname", win.getOwningPackage());
            args.putInt("uid", win.getOwningUid());
            this.mIWmsInner.getWMMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), args);
        }
    }

    private void updateVisibleWindowsOps(int eventType, String pkgName) {
        Bundle args = new Bundle();
        args.putString("pkgname", pkgName);
        args.putInt(ASSOC_RELATION_TYPE, eventType);
        this.mIWmsInner.getWMMonitor().reportData(RESOURCE_APPASSOC, System.currentTimeMillis(), args);
    }

    private void reportWindowStatusToIAware(int eventType, WindowState win, int mode, int requestedWidth, int requestedHeight, boolean isupdate) {
        WindowState windowState = win;
        boolean isToast = windowState != null && windowState.getAttrs().type == 2005;
        if (windowState == null || ((windowState.mAppOp != 24 && !isToast) || windowState.mSession == null)) {
            return;
        }
        if (this.mIWmsInner.getWMMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            updateVisibleWindows(eventType, mode, isToast ? 45 : 24, windowState, requestedWidth, requestedHeight, isupdate);
        }
    }

    public void addWindowReport(WindowState win, int mode) {
        reportWindowStatusToIAware(8, win, mode, 0, 0, false);
    }

    public void removeWindowReport(WindowState win) {
        reportWindowStatusToIAware(9, win, 3, 0, 0, false);
    }

    public void updateWindowReport(WindowState win, int requestedWidth, int requestedHeight) {
        reportWindowStatusToIAware(27, win, 3, requestedWidth, requestedHeight, true);
    }

    public void updateAppOpsStateReport(int ops, String packageName) {
        if (ops == 24 && this.mIWmsInner.getWMMonitor().isResourceNeeded(RESOURCE_APPASSOC)) {
            updateVisibleWindowsOps(10, packageName);
        }
    }

    public void notifyFingerWinCovered(boolean covered, Rect frame) {
        if (this.mLastCoveredState != covered || !this.mLastCoveredFrame.equals(frame)) {
            this.mHwHandler.sendMessage(this.mHwHandler.obtainMessage(101, covered, 0, frame));
            this.mLastCoveredState = covered;
            this.mLastCoveredFrame.set(frame);
        }
    }

    public int getFocusWindowWidth(WindowState mCurrentFocus, WindowState mInputMethodTarget) {
        WindowState mFocusWindow;
        Rect rect;
        if (mInputMethodTarget == null) {
            mFocusWindow = mCurrentFocus;
        } else {
            mFocusWindow = mInputMethodTarget;
        }
        if (mFocusWindow == null) {
            Log.e(TAG, "WMS getFocusWindowWidth error");
            return 0;
        }
        if (mFocusWindow.getAttrs().type == 2) {
            rect = mFocusWindow.getDisplayFrameLw();
        } else {
            rect = mFocusWindow.getContentFrameLw();
        }
        return rect.width();
    }

    public void reportLazyModeToIAware(int lazyMode) {
        Bundle args = new Bundle();
        args.putInt(SYSLOAD_SINGLEHAND_TYPE, lazyMode);
        this.mIWmsInner.getWMMonitor().reportData(RESOURCE_SYSLOAD, System.currentTimeMillis(), args);
    }

    public void handleNewDisplayConfiguration(Configuration overrideConfig, int displayId) {
        HwPhoneWindowManager policy = this.mIWmsInner.getPolicy();
        if (policy instanceof HwPhoneWindowManager) {
            HwPhoneWindowManager policy2 = policy;
            if (policy2.getHwWindowCallback() != null && (this.mIWmsInner.getRoot().getDisplayContent(displayId).getConfiguration().diff(overrideConfig) & 128) != 0) {
                Slog.v(TAG, "handleNewDisplayConfiguration notify window callback");
                try {
                    policy2.getHwWindowCallback().handleConfigurationChanged();
                } catch (Exception ex) {
                    Slog.w(TAG, "mIHwWindowCallback handleNewDisplayConfiguration", ex);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006e, code lost:
        return;
     */
    public void getCurrFocusedWinInExtDisplay(Bundle outBundle) {
        if (outBundle != null) {
            synchronized (this.mIWmsInner.getWindowMap()) {
                if (this.mIWmsInner.getInputMonitor() != null) {
                    InputWindowHandle inputWindowHandle = this.mIWmsInner.getInputMonitor().getFousedWinExtDisplayInPCCastMode();
                    HwPCUtils.log(TAG, "getCurrFocusedWinInExtDisplay inputWindowHandle = " + inputWindowHandle);
                    if (inputWindowHandle != null && (inputWindowHandle.windowState instanceof WindowState)) {
                        WindowState ws = (WindowState) inputWindowHandle.windowState;
                        Rect rect = null;
                        outBundle.putString(AwareIntelligentRecg.CMP_PKGNAME, ws == null ? null : ws.getAttrs().packageName);
                        boolean isApp = false;
                        if (ws != null && ws.getAppToken() != null) {
                            isApp = true;
                        }
                        outBundle.putBoolean("isApp", isApp);
                        if (isApp) {
                            rect = ws.getBounds();
                        }
                        outBundle.putParcelable("bounds", rect);
                    }
                }
            }
        }
    }

    public boolean hasLighterViewInPCCastMode() {
        synchronized (this.mIWmsInner.getWindowMap()) {
            if (this.mIWmsInner.getInputMonitor() == null) {
                return false;
            }
            boolean hasLighterViewInPCCastMode = this.mIWmsInner.getInputMonitor().hasLighterViewInPCCastMode();
            return hasLighterViewInPCCastMode;
        }
    }

    public boolean shouldDropMotionEventForTouchPad(float x, float y) {
        DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(0);
        if (dc != null && (dc instanceof HwDisplayContent)) {
            return ((HwDisplayContent) dc).shouldDropMotionEventForTouchPad(x, y);
        }
        return false;
    }

    public void updateHwStartWindowRecord(int appUid) {
        HwStartWindowRecord.getInstance().resetStartWindowApp(Integer.valueOf(appUid));
    }

    public HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(TaskSnapshotController mTaskSnapshotController, WindowState focusedWindow, boolean refresh) {
        ActivityManager.TaskSnapshot taskSnapshot;
        if (refresh) {
            mTaskSnapshotController.clearForegroundTaskSnapshot();
        }
        if (focusedWindow == null || !refresh) {
            taskSnapshot = mTaskSnapshotController.getForegroundTaskSnapshot();
        } else {
            taskSnapshot = mTaskSnapshotController.createForegroundTaskSnapshot(focusedWindow.mAppToken);
        }
        HwTaskSnapshotWrapper hwTaskSnapshotWrapper = new HwTaskSnapshotWrapper();
        hwTaskSnapshotWrapper.setTaskSnapshot(taskSnapshot);
        return hwTaskSnapshotWrapper;
    }

    public boolean detectSafeMode() {
        WindowManagerPolicy wmPolicy = this.mIWmsInner.getPolicy();
        boolean sCheckSafeModeState = false;
        if (HwDeviceManager.disallowOp(10)) {
            Slog.i(TAG, "safemode is disabled by dpm");
            this.mHwSafeMode = false;
            wmPolicy.setSafeMode(this.mHwSafeMode);
            sCheckSafeModeState = true;
        }
        if (!"1".equals(SystemProperties.get("sys.bootfail.safemode"))) {
            return sCheckSafeModeState;
        }
        Slog.i(TAG, "safemode is enabled eRecovery");
        this.mHwSafeMode = true;
        wmPolicy.setSafeMode(this.mHwSafeMode);
        return true;
    }

    public boolean getSafeMode() {
        return this.mHwSafeMode;
    }

    public void setLazyModeEx(int lazyMode) {
        Slog.i(TAG, "cur: " + this.mLazyModeOnEx + " to: " + lazyMode);
        if (this.mLazyModeOnEx != lazyMode) {
            reportLazyModeToIAware(lazyMode);
            this.mLazyModeOnEx = lazyMode;
        }
    }

    public int getLazyModeEx() {
        return this.mLazyModeOnEx;
    }

    public void setNaviBarFlag() {
        this.mIWmsInner.getPolicy().setInputMethodWindowVisible(this.mIWmsInner.getInputMethodWindow() == null ? false : this.mIWmsInner.getInputMethodWindow().isVisibleLw());
        AppWindowToken appWindowToken = this.mIWmsInner.getFocusedAppWindowToken();
        if (appWindowToken != null) {
            this.mIWmsInner.getPolicy().setNaviBarFlag(appWindowToken.navigationBarHide);
        }
    }

    public final void performhwLayoutAndPlaceSurfacesLocked() {
        this.mIWmsInner.getWindowSurfacePlacer().performSurfacePlacement();
    }

    public Configuration getCurNaviConfiguration() {
        return this.mCurNaviConfiguration;
    }

    public boolean getIgnoreFrozen() {
        return this.mIgnoreFrozen;
    }

    public void setIgnoreFrozen(boolean flag) {
        this.mIgnoreFrozen = flag;
    }

    public void reevaluateStatusBarSize(boolean layoutNaviBar) {
        synchronized (this.mIWmsInner.getWindowMap()) {
            this.mLayoutNaviBar = layoutNaviBar;
            this.mIWmsInner.getWindowMangerServiceHandler().post(this.mReevaluateStatusBarSize);
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        this.mIWmsInner.getInputManager().setCurrentUser(newUserId, currentProfileIds);
        HwPhoneWindowManager policy = this.mIWmsInner.getPolicy();
        if (policy instanceof HwPhoneWindowManager) {
            policy.setCurrentUser(newUserId, currentProfileIds);
        }
    }

    public void hwSystemReady() {
        if (IS_SUPPORT_SINGLE_MODE) {
            this.mSingleHandSwitch = judgeSingleHandSwitchBySize();
            Slog.i(TAG, "WMS systemReady mSingleHandSwitch = " + this.mSingleHandSwitch);
            if (this.mSingleHandSwitch) {
                this.mSingleHandAdapter = new SingleHandAdapter(this.mContext, this.mHwHandler, this.mUiHandler, this.mIWmsInner.getService());
                this.mSingleHandAdapter.registerLocked();
            }
        }
        if (CoordinationModeUtils.isFoldable()) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "coordination_create_mode", 0);
        }
    }

    private boolean judgeSingleHandSwitchBySize() {
        return this.mContext.getResources().getBoolean(34537473);
    }

    public boolean isSupportSingleHand() {
        return this.mSingleHandSwitch;
    }

    public void setCoverManagerState(boolean isCoverOpen) {
        this.mIsCoverOpen = isCoverOpen;
        HwServiceFactory.setIfCoverClosed(!isCoverOpen);
    }

    public boolean isCoverOpen() {
        return this.mIsCoverOpen;
    }

    public void freezeOrThawRotation(int rotation) {
        if (rotation < -1 || rotation > 3) {
            throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
        }
        Slog.v(TAG, "freezeRotationTemporarily: rotation=" + rotation);
        HwPhoneWindowManager policy = this.mIWmsInner.getPolicy();
        if (policy instanceof HwPhoneWindowManager) {
            policy.freezeOrThawRotation(rotation);
        }
    }

    public void preAddWindow(WindowManager.LayoutParams attrs) {
        if (attrs.type == 2101) {
            attrs.token = null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01b4, code lost:
        android.os.Binder.restoreCallingIdentity(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x01b8, code lost:
        return;
     */
    public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
        int maxWidth;
        int i = displayId;
        int i2 = density;
        int i3 = width;
        int i4 = height;
        if (i2 >= 200 && i3 >= 600) {
            if (i4 >= 600) {
                Slog.d(TAG, "setForcedDisplayDensityAndSize size: " + i3 + "x" + i4 + " density: " + i2);
                if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                    throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
                } else if (i == 0) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        synchronized (this.mIWmsInner.getWindowMap()) {
                            try {
                                DisplayContent displayContent = this.mIWmsInner.getRoot().getDisplayContent(i);
                                if (displayContent != null) {
                                    int maxWidth2 = i3 > 200 ? i3 : 200;
                                    if (maxWidth2 < displayContent.mInitialDisplayWidth * 2) {
                                        maxWidth = maxWidth2;
                                    } else {
                                        try {
                                            maxWidth = displayContent.mInitialDisplayWidth * 2;
                                        } catch (Throwable th) {
                                            th = th;
                                            throw th;
                                        }
                                    }
                                    int width2 = maxWidth;
                                    int maxHeight = 200;
                                    if (i4 > 200) {
                                        maxHeight = i4;
                                    }
                                    int i5 = maxWidth2;
                                    int height2 = maxHeight < displayContent.mInitialDisplayHeight * 2 ? maxHeight : displayContent.mInitialDisplayHeight * 2;
                                    displayContent.mBaseDisplayWidth = width2;
                                    displayContent.mBaseDisplayHeight = height2;
                                    displayContent.mBaseDisplayDensity = i2;
                                    int i6 = maxHeight;
                                    this.mHwHandler.removeMessages(100);
                                    this.mHwHandler.sendEmptyMessageDelayed(100, MSG_ROG_FREEZE_TIME_DELEAYED);
                                    updateResourceConfiguration(i, i2, width2, height2);
                                    this.mIWmsInner.getService().reconfigureDisplayLocked(displayContent);
                                    ScreenRotationAnimation screenRotationAnimation = this.mIWmsInner.getWindowAnimator().getScreenRotationAnimationLocked(i);
                                    if (screenRotationAnimation != null) {
                                        screenRotationAnimation.kill();
                                    }
                                    ContentResolver contentResolver = this.mContext.getContentResolver();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(width2);
                                    ScreenRotationAnimation screenRotationAnimation2 = screenRotationAnimation;
                                    sb.append(",");
                                    sb.append(height2);
                                    Settings.Global.putString(contentResolver, "display_size_forced", sb.toString());
                                    List<UserInfo> userList = UserManager.get(this.mContext).getUsers();
                                    if (userList != null) {
                                        Iterator<UserInfo> it = userList.iterator();
                                        while (it.hasNext()) {
                                            Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density), it.next().id);
                                            userList = userList;
                                            it = it;
                                            int i7 = displayId;
                                        }
                                    }
                                    SystemProperties.set("persist.sys.realdpi", i2 + "");
                                    SystemProperties.set("persist.sys.rog.width", width2 + "");
                                    SystemProperties.set("persist.sys.rog.height", height2 + "");
                                    if (IS_NOTCH_PROP) {
                                        this.mIWmsInner.getDisplayManagerInternal().updateCutoutInfoForRog(0);
                                        Slog.d(TAG, "updateCutoutInfoForRog width: " + width2 + " height " + height2);
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("Can only set the default display");
                }
            }
        }
        Slog.d(TAG, "the para of setForcedDisplayDensityAndSize is illegal : size = " + i3 + "x" + i4 + "; density = " + i2);
    }

    public void updateResourceConfiguration(int displayId, int density, int width, int height) {
        if (density == 0) {
            Slog.e(TAG, "setForcedDisplayDensityAndSize density is 0");
            return;
        }
        Slog.d(TAG, "setForcedDisplay and updateResourceConfiguration, density = " + density + " width = " + width + " height = " + height);
        Configuration tempResourceConfiguration = new Configuration(this.mIWmsInner.getRoot().getDisplayContent(displayId).getConfiguration());
        DisplayMetrics tempMetrics = this.mContext.getResources().getDisplayMetrics();
        tempResourceConfiguration.densityDpi = density;
        tempResourceConfiguration.screenWidthDp = (width * HwMpLinkWifiImpl.BAND_WIDTH_160MHZ) / density;
        tempResourceConfiguration.smallestScreenWidthDp = (width * HwMpLinkWifiImpl.BAND_WIDTH_160MHZ) / density;
        tempMetrics.density = ((float) density) / 160.0f;
        tempMetrics.densityDpi = density;
        this.mContext.getResources().updateConfiguration(tempResourceConfiguration, tempMetrics);
        Slog.d(TAG, "setForcedDisplay and updateResourceConfiguration, tempResourceConfiguration is: " + tempResourceConfiguration + "tempMetrics is: " + tempMetrics);
    }

    public void setHwSecureScreenShot(WindowState win) {
        WindowStateAnimator winAnimator = win.mWinAnimator;
        if (winAnimator.mSurfaceController != null) {
            if ((win.mAttrs.hwFlags & 4096) != 0) {
                if (!this.mSecureScreenShot.contains(win)) {
                    winAnimator.mSurfaceController.setSecureScreenShot(true);
                    this.mSecureScreenShot.add(win);
                    Slog.i(TAG, "Set SecureScreenShot by: " + win);
                }
            } else if (this.mSecureScreenShot.contains(win)) {
                this.mSecureScreenShot.remove(win);
                winAnimator.mSurfaceController.setSecureScreenShot(false);
                Slog.i(TAG, "Remove SecureScreenShot by: " + win);
            }
            if ((win.mAttrs.hwFlags & 8192) != 0) {
                if (!this.mSecureScreenRecords.contains(win)) {
                    winAnimator.mSurfaceController.setSecureScreenRecord(true);
                    this.mSecureScreenRecords.add(win);
                    Slog.i(TAG, "Set SecureScreenRecord by: " + win);
                }
            } else if (this.mSecureScreenRecords.contains(win)) {
                this.mSecureScreenRecords.remove(win);
                winAnimator.mSurfaceController.setSecureScreenRecord(false);
                Slog.i(TAG, "Remove SecureScreenRecord by: " + win);
            }
        }
    }

    public Point updateLazyModePoint(int type, Point point) {
        int width;
        int height;
        if (type == 0) {
            return point;
        }
        DisplayInfo defaultDisplayInfo = new DisplayInfo();
        this.mIWmsInner.getDefaultDisplayContentLocked().getDisplay().getDisplayInfo(defaultDisplayInfo);
        boolean isPortrait = defaultDisplayInfo.logicalHeight > defaultDisplayInfo.logicalWidth;
        if (isPortrait) {
            width = defaultDisplayInfo.logicalWidth;
        } else {
            width = defaultDisplayInfo.logicalHeight;
        }
        if (isPortrait) {
            height = defaultDisplayInfo.logicalHeight;
        } else {
            height = defaultDisplayInfo.logicalWidth;
        }
        float pendingX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        float pendingY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (type == 1) {
            pendingY = ((float) height) * 0.25f;
        } else if (type == 2) {
            pendingX = ((float) width) * 0.25f;
            pendingY = ((float) height) * 0.25f;
        }
        return new Point((int) ((((float) point.x) * 0.75f) + pendingX), (int) ((((float) point.y) * 0.75f) + pendingY));
    }

    public float getLazyModeScale() {
        return 0.75f;
    }

    public void takeTaskSnapshot(IBinder binder) {
        AppWindowToken appWindowToken = this.mIWmsInner.getRoot().getAppWindowToken(binder);
        if (appWindowToken != null) {
            WindowContainer wc = appWindowToken.getParent();
            if (wc == null || !(wc instanceof Task)) {
                Slog.v(TAG, "takeTaskSnapshot has no tasks");
                return;
            }
            ArraySet<Task> tasks = Sets.newArraySet(new Task[]{(Task) wc});
            synchronized (this.mIWmsInner.getWindowMap()) {
                this.mIWmsInner.getTaskSnapshotController().snapshotTasks(tasks);
            }
            return;
        }
        Slog.v(TAG, "takeTaskSnapshot appWindowToken is null");
    }

    public Rect getFocuseWindowVisibleFrame(WindowManagerService wms) {
        WindowState currentWindowState = wms.getFocusedWindow();
        if (currentWindowState != null) {
            Rect currentRect = currentWindowState.getVisibleFrameLw();
            if (currentRect != null) {
                return currentRect;
            }
        }
        HwFreeFormUtils.log(TAG, "getFocuseWindowVisibleFrame is null");
        return null;
    }

    public String getTopAppPackageByWindowMode(int windowMode, RootWindowContainer mRoot) {
        TaskStack stack = mRoot.getStack(windowMode, 1);
        if (stack == null || stack.getTopChild() == null || stack.getTopChild().getTopFullscreenAppToken() == null) {
            return null;
        }
        return stack.getTopChild().getTopFullscreenAppToken().appPackageName;
    }

    public void showWallpaperIfNeed(WindowState w) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen(this.mContext) || w == null || w.isInMultiWindowMode()) {
            return;
        }
        if (w.mAppToken == null || !forceRotationManager.isAppForceLandRotatable(w.mAppToken.appPackageName, w.mAppToken.appToken.asBinder())) {
            Slog.v(TAG, "current window do not support force rotation mAppToken:" + w.mAppToken);
            return;
        }
        DisplayContent dc = this.mIWmsInner.getRoot().getDisplayContent(0);
        if (dc != null) {
            Display dp = dc.getDisplay();
            if (dp != null) {
                DisplayMetrics dm = new DisplayMetrics();
                dp.getMetrics(dm);
                if (dm.widthPixels < dm.heightPixels) {
                    w.mAttrs.flags &= -1048577;
                } else {
                    w.mAttrs.flags |= HighBitsCompModeID.MODE_COLOR_ENHANCE;
                }
            }
        }
    }

    public void prepareForForceRotation(IBinder token, String packageName, int pid, String processName) {
        if (HwForceRotationManager.getDefault().isForceRotationSupported()) {
            synchronized (this.mIWmsInner.getWindowMap()) {
                AppWindowToken aToken = this.mIWmsInner.getRoot().getAppWindowToken(token);
                if (aToken == null) {
                    Slog.w(TAG, "Attempted to set orientation of non-existing app token: " + token);
                    return;
                }
                aToken.appPackageName = packageName;
                aToken.appPid = pid;
                aToken.appProcessName = processName;
            }
        }
    }

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

    public boolean isDisplayOkForAnimation(int width, int height, int transit, AppWindowToken atoken) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (forceRotationManager.isForceRotationSupported() && forceRotationManager.isForceRotationSwitchOpen(this.mContext) && width > height && ((transit == 7 || transit == 6) && forceRotationManager.isAppForceLandRotatable(atoken.appPackageName, atoken.appToken.asBinder()))) {
            return false;
        }
        return true;
    }

    public ArrayList<WindowState> getSecureScreenWindow() {
        ArrayList<WindowState> secureScreenWindow = new ArrayList<>();
        secureScreenWindow.addAll(this.mSecureScreenRecords);
        secureScreenWindow.addAll(this.mSecureScreenShot);
        return secureScreenWindow;
    }

    public void removeSecureScreenWindow(WindowState win) {
        if (win != null) {
            WindowStateAnimator winAnimator = win.mWinAnimator;
            if (winAnimator != null && winAnimator.mSurfaceController != null) {
                if (this.mSecureScreenRecords.contains(win)) {
                    this.mSecureScreenRecords.remove(win);
                    winAnimator.mSurfaceController.setSecureScreenRecord(false);
                    Slog.i(TAG, "Remove SecureScreenRecord : " + win);
                }
                if (this.mSecureScreenShot.contains(win)) {
                    this.mSecureScreenShot.remove(win);
                    winAnimator.mSurfaceController.setSecureScreenShot(false);
                    Slog.i(TAG, "Remove SecureScreenShot : " + win);
                }
            }
        }
    }

    public boolean isInFoldFullDisplayMode() {
        if (!HwFoldScreenState.isFoldScreenDevice()) {
            return false;
        }
        HwFoldScreenManagerInternal foldScreenManagerInternal = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        if (foldScreenManagerInternal != null && foldScreenManagerInternal.getDisplayMode() == 1) {
            return true;
        }
        return false;
    }
}
