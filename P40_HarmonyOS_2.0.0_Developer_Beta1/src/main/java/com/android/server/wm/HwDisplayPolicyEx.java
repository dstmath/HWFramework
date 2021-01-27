package com.android.server.wm;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.app.ActivityManager;
import android.app.WindowConfiguration;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.swing.HwSwingManager;
import android.swing.IHwSwingService;
import android.util.HwPCUtils;
import android.util.HwSlog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import com.android.server.LocalServices;
import com.android.server.gesture.DefaultGestureNavConst;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.input.InputManagerServiceBridge;
import com.android.server.inputmethod.HwInputMethodManagerService;
import com.android.server.policy.HwGameDockGesture;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.IHwPhoneWindowManagerEx;
import com.android.server.policy.NavigationBarPolicy;
import com.android.server.policy.NavigationCallOut;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.view.HwExtDisplaySizeUtil;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.HwPCFactory;
import com.huawei.server.sidetouch.DefaultHwDisplaySideRegionConfig;
import com.huawei.server.wm.IHwDisplayPolicyEx;
import com.huawei.server.wm.IHwDisplayPolicyInner;
import java.io.PrintWriter;
import java.util.List;

public class HwDisplayPolicyEx implements IHwDisplayPolicyEx {
    private static final int DEF_DIRECTION_CNT = 4;
    private static final int EVENT_DURING_MIN_TIME = 500;
    private static final boolean IS_DEBUG = false;
    private static final boolean IS_FACTORY_MODE = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
    private static final boolean IS_HW_NAVIBAR = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private static final boolean IS_NOTCH_PROP = (!"".equals(SystemProperties.get("ro.config.hw_notch_size", "")));
    private static final boolean IS_SUPPORT_GAME_ASSIST;
    private static final String LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    private static final int MSG_DELAY_TIME = 200;
    private static final int NAVIGATION_BAR_HEIGHT_MAX = 1;
    private static final int NAVIGATION_BAR_HEIGHT_MIN = 0;
    private static final int NAVIGATION_BAR_WIDTH_MAX = 2;
    private static final int NAVIGATION_BAR_WIDTH_MIN = 3;
    private static final int ROTATION_LANDACAPE_DEF = 0;
    private static final int ROTATION_LANDACAPE_OTHER = 1;
    private static final int ROTATION_PORTRAIT_DEF = 2;
    private static final int ROTATION_PORTRAIT_OTHER = 3;
    private static final String TAG = "HwDisplayPolicyEx";
    private Context mContext;
    private DisplayContent mDisplayContent;
    private IHwDisplayPolicyInner mDisplayPolicy = null;
    private HwGameDockGesture mGameDockGesture;
    private DefaultGestureNavManager mGestureNavPolicy;
    private DefaultHwDisplaySideRegionConfig mHwDisplaySideConfig;
    private boolean mIsAppWindow = false;
    private boolean mIsDefaultDisplay;
    private boolean mIsFocusWindowUsingNotch = true;
    private boolean mIsImmersiveMode = false;
    private boolean mIsInputMethodWindowVisible;
    private boolean mIsNavibarHide;
    private String mLastFgPackageName;
    private long mLastKeyPointerTime = 0;
    private WindowManagerPolicyConstants.PointerEventListener mLockScreenBuildInDisplayListener;
    private WindowManagerPolicyConstants.PointerEventListener mLockScreenListener;
    private final BarController mNavigationBarControllerExternal = new BarController("NavigationBarExternal", 0, 134217728, 536870912, Integer.MIN_VALUE, 2, 134217728, 32768);
    private WindowState mNavigationBarExternal = null;
    int mNavigationBarHeightExternal = 0;
    private int[] mNavigationBarHeightForRotationMaxs = new int[4];
    private int[] mNavigationBarHeightForRotationMins = new int[4];
    private NavigationBarPolicy mNavigationBarPolicy = null;
    private int mNavigationBarWidthExternal = 0;
    private int[] mNavigationBarWidthForRotationMaxs = new int[4];
    private int[] mNavigationBarWidthForRotationMins = new int[4];
    private NavigationCallOut mNavigationCallOut = null;
    private final WindowManagerService mService;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.config.gameassist", 0) != 1) {
            z = false;
        }
        IS_SUPPORT_GAME_ASSIST = z;
    }

    public HwDisplayPolicyEx(WindowManagerService service, IHwDisplayPolicyInner displayPolicy, DisplayContent displayContent, Context context, boolean isDefaultDisplay) {
        this.mService = service;
        this.mContext = context;
        this.mDisplayContent = displayContent;
        this.mDisplayPolicy = displayPolicy;
        this.mIsDefaultDisplay = isDefaultDisplay;
        this.mGestureNavPolicy = (DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class);
        this.mGameDockGesture = (HwGameDockGesture) LocalServices.getService(HwGameDockGesture.class);
        if (isDefaultDisplay && HwExtDisplaySizeUtil.getInstance().hasSideInScreen()) {
            this.mHwDisplaySideConfig = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_SIDE_TOUCH_PART_FACTORY_IMPL).getHwDisplaySideRegionConfigInstance();
        }
    }

    public void systemReadyEx() {
        if (IS_HW_NAVIBAR && (this.mService.getPolicy() instanceof PhoneWindowManager)) {
            PhoneWindowManager phoneWindowManager = this.mService.getPolicy();
            if (this.mNavigationCallOut == null && this.mIsDefaultDisplay) {
                Context context = this.mContext;
                DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
                this.mNavigationCallOut = new NavigationCallOut(context, phoneWindowManager, defaultGestureNavManager != null ? defaultGestureNavManager.getGestureLoooper() : null);
            }
            if (this.mIsDefaultDisplay) {
                this.mNavigationBarPolicy = new NavigationBarPolicy(this.mContext, phoneWindowManager, this.mNavigationCallOut, this.mService);
                this.mService.getPolicy().getWindowManagerFuncs().registerPointerEventListener(new WindowManagerPolicyConstants.PointerEventListener() {
                    /* class com.android.server.wm.HwDisplayPolicyEx.AnonymousClass1 */

                    public void onPointerEvent(MotionEvent motionEvent) {
                        if (HwDisplayPolicyEx.this.mNavigationBarPolicy != null) {
                            HwDisplayPolicyEx.this.mNavigationBarPolicy.addPointerEvent(motionEvent);
                        }
                    }
                }, 0);
            }
        }
        if (IS_SUPPORT_GAME_ASSIST) {
            ActivityManagerEx.registerGameObserver(new IGameObserver.Stub() {
                /* class com.android.server.wm.HwDisplayPolicyEx.AnonymousClass2 */

                public void onGameStatusChanged(String packageName, int event) {
                    Log.i(HwDisplayPolicyEx.TAG, "currentFgApp=" + packageName + ", mLastFgPackageName=" + HwDisplayPolicyEx.this.mLastFgPackageName);
                    if (!(packageName == null || packageName.equals(HwDisplayPolicyEx.this.mLastFgPackageName) || HwDisplayPolicyEx.this.mNavigationBarPolicy == null)) {
                        HwDisplayPolicyEx.this.mNavigationBarPolicy.setEnableSwipeInCurrentGameApp(false);
                        Log.i(HwDisplayPolicyEx.TAG, "setEnableSwipeInCurrentGameApp false");
                    }
                    HwDisplayPolicyEx.this.mLastFgPackageName = packageName;
                }

                public void onGameListChanged() {
                }
            });
        }
    }

    public void registerExternalPointerEventListener() {
        if (HwPCUtils.enabled() && HwPCUtils.isPcCastModeInServer()) {
            unRegisterExternalPointerEventListener();
            this.mLockScreenListener = new WindowManagerPolicyConstants.PointerEventListener() {
                /* class com.android.server.wm.HwDisplayPolicyEx.AnonymousClass3 */

                public void onPointerEvent(MotionEvent motionEvent) {
                    if (motionEvent.getEventTime() - HwDisplayPolicyEx.this.mLastKeyPointerTime > 500) {
                        HwDisplayPolicyEx.this.mLastKeyPointerTime = motionEvent.getEventTime();
                        HwDisplayPolicyEx.this.userActivityOnDesktop();
                    }
                }
            };
            this.mService.registerPointerEventListener(this.mLockScreenListener, HwPCUtils.getPCDisplayID());
            this.mLockScreenBuildInDisplayListener = new WindowManagerPolicyConstants.PointerEventListener() {
                /* class com.android.server.wm.HwDisplayPolicyEx.AnonymousClass4 */

                public void onPointerEvent(MotionEvent motionEvent) {
                    if (motionEvent.getEventTime() - HwDisplayPolicyEx.this.mLastKeyPointerTime > 500) {
                        HwDisplayPolicyEx.this.mLastKeyPointerTime = motionEvent.getEventTime();
                        HwDisplayPolicyEx.this.userActivityOnDesktop();
                    }
                }
            };
            this.mService.registerPointerEventListener(this.mLockScreenBuildInDisplayListener, 0);
        }
    }

    public void unRegisterExternalPointerEventListener() {
        if (HwPCUtils.enabled() && HwPCUtils.isPcCastModeInServer()) {
            WindowManagerPolicyConstants.PointerEventListener pointerEventListener = this.mLockScreenListener;
            if (pointerEventListener != null) {
                this.mService.unregisterPointerEventListener(pointerEventListener, HwPCUtils.getPCDisplayID());
                this.mLockScreenListener = null;
            }
            WindowManagerPolicyConstants.PointerEventListener pointerEventListener2 = this.mLockScreenBuildInDisplayListener;
            if (pointerEventListener2 != null) {
                this.mService.unregisterPointerEventListener(pointerEventListener2, 0);
                this.mLockScreenBuildInDisplayListener = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void userActivityOnDesktop() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.userActivityOnDesktop();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException userActivityOnDesktop");
            }
        }
    }

    public void onConfigurationChanged() {
        DefaultGestureNavManager defaultGestureNavManager;
        NavigationBarPolicy navigationBarPolicy = this.mNavigationBarPolicy;
        if (navigationBarPolicy != null) {
            if (navigationBarPolicy.getMinNavigationBar()) {
                this.mDisplayPolicy.setNavigationBarHeightDef((int[]) this.mNavigationBarHeightForRotationMins.clone());
                this.mDisplayPolicy.setNavigationBarWidthDef((int[]) this.mNavigationBarWidthForRotationMins.clone());
            } else {
                this.mDisplayPolicy.setNavigationBarHeightDef((int[]) this.mNavigationBarHeightForRotationMaxs.clone());
                this.mDisplayPolicy.setNavigationBarWidthDef((int[]) this.mNavigationBarWidthForRotationMaxs.clone());
            }
        }
        NavigationCallOut navigationCallOut = this.mNavigationCallOut;
        if (navigationCallOut != null) {
            navigationCallOut.updateHotArea();
        }
        if (this.mIsDefaultDisplay && (this.mService.getInputManager() instanceof InputManagerServiceBridge)) {
            this.mService.getInputManager().onConfigurationChanged();
        }
        if (this.mIsDefaultDisplay && (defaultGestureNavManager = this.mGestureNavPolicy) != null) {
            defaultGestureNavManager.onConfigurationChanged();
        }
        if (this.mIsDefaultDisplay && this.mGameDockGesture != null && HwGameDockGesture.isGameDockGestureFeatureOn()) {
            this.mGameDockGesture.updateOnConfigurationChange();
        }
        if (this.mService.getPolicy() instanceof HwPhoneWindowManager) {
            this.mService.getPolicy().onConfigurationChanged();
        }
    }

    private void notifyFocusChange(WindowState newFocus) {
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            int pid = 0;
            String title = null;
            if (!(newFocus == null || newFocus.getAttrs() == null)) {
                pid = newFocus.mSession.mPid;
                title = newFocus.getAttrs().getTitle().toString();
            }
            try {
                hwAft.notifyFocusChange(pid, title);
            } catch (RemoteException e) {
                Log.e(TAG, "binder call hwAft throw");
            }
        }
        NavigationCallOut navigationCallOut = this.mNavigationCallOut;
        if (navigationCallOut != null) {
            navigationCallOut.notifyFocusChange(newFocus);
        }
    }

    public int focusChangedLw(WindowState lastFocus, WindowState newFocus) {
        if (this.mIsDefaultDisplay && this.mGestureNavPolicy != null) {
            WindowManagerPolicyEx.WindowStateEx lastFocusEx = null;
            if (lastFocus != null) {
                lastFocusEx = new WindowManagerPolicyEx.WindowStateEx();
                lastFocusEx.setWindowState(lastFocus);
            }
            WindowManagerPolicyEx.WindowStateEx newFocusEx = null;
            if (newFocus != null) {
                newFocusEx = new WindowManagerPolicyEx.WindowStateEx();
                newFocusEx.setWindowState(newFocus);
            }
            this.mIsFocusWindowUsingNotch = this.mGestureNavPolicy.onFocusWindowChanged(lastFocusEx, newFocusEx);
        }
        if (this.mIsDefaultDisplay && this.mGameDockGesture != null && HwGameDockGesture.isGameDockGestureFeatureOn()) {
            this.mGameDockGesture.updateOnFocusChange(newFocus);
        }
        notifyFocusChange(newFocus);
        IHwSwingService hwSwing = HwSwingManager.getService();
        if (hwSwing != null) {
            String title = null;
            String pkgName = null;
            if (!(newFocus == null || newFocus.getAttrs() == null)) {
                title = newFocus.getAttrs().getTitle().toString();
                pkgName = newFocus.getOwningPackage();
            }
            try {
                hwSwing.notifyFocusChange(title, pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "binder call hwSwing throw error");
            }
        }
        if (HwDisplaySizeUtil.hasSideInScreen() && newFocus != null && (this.mService.getPolicy() instanceof HwPhoneWindowManager)) {
            this.mService.getPolicy().notchControlFilletForSideScreen(newFocus, false);
        }
        WindowManagerPolicyEx.WindowStateEx lastFocusEx2 = null;
        if (lastFocus != null) {
            lastFocusEx2 = new WindowManagerPolicyEx.WindowStateEx();
            lastFocusEx2.setWindowState(lastFocus);
        }
        WindowManagerPolicyEx.WindowStateEx newFocusEx2 = null;
        if (newFocus != null) {
            newFocusEx2 = new WindowManagerPolicyEx.WindowStateEx();
            newFocusEx2.setWindowState(newFocus);
        }
        HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwFalseTouchMonitor().handleFocusChanged(lastFocusEx2, newFocusEx2);
        return 0;
    }

    public void layoutWindowLw(WindowState win, WindowState attached, WindowState focusedWindow, boolean isLayoutBeyondDisplayCutout) {
        DefaultGestureNavManager defaultGestureNavManager;
        if (IS_NOTCH_PROP && this.mIsDefaultDisplay && (defaultGestureNavManager = this.mGestureNavPolicy) != null && defaultGestureNavManager.isGestureNavStartedNotLocked() && isFocusedWindow(win, focusedWindow)) {
            boolean isOldUsingNotch = this.mIsFocusWindowUsingNotch;
            this.mIsFocusWindowUsingNotch = !isLayoutBeyondDisplayCutout;
            if (isOldUsingNotch != this.mIsFocusWindowUsingNotch) {
                WindowManagerPolicyEx.WindowStateEx winEx = null;
                if (win != null) {
                    winEx = new WindowManagerPolicyEx.WindowStateEx();
                    winEx.setWindowState(win);
                }
                this.mGestureNavPolicy.onLayoutInDisplayCutoutModeChanged(winEx, isOldUsingNotch, this.mIsFocusWindowUsingNotch);
            }
        }
    }

    private boolean isFocusedWindow(WindowState win, WindowState focusedWindow) {
        if (win == null || focusedWindow == null || win.getAttrs() == null || focusedWindow.getAttrs() == null) {
            return false;
        }
        return focusedWindow.getAttrs().getTitle().toString().equals(win.getAttrs().getTitle().toString());
    }

    public int getNaviBarHeightForRotationMin(int index) {
        return this.mNavigationBarHeightForRotationMins[index];
    }

    public int getNaviBarWidthForRotationMin(int index) {
        int[] iArr = this.mNavigationBarWidthForRotationMins;
        if (index >= iArr.length) {
            return 0;
        }
        return iArr[index];
    }

    public int getNaviBarHeightForRotationMax(int index) {
        int[] iArr = this.mNavigationBarHeightForRotationMaxs;
        if (index >= iArr.length) {
            return 0;
        }
        return iArr[index];
    }

    public int getNaviBarWidthForRotationMax(int index) {
        int[] iArr = this.mNavigationBarWidthForRotationMaxs;
        if (index >= iArr.length) {
            return 0;
        }
        return iArr[index];
    }

    public boolean isNaviBarMini() {
        NavigationBarPolicy navigationBarPolicy = this.mNavigationBarPolicy;
        if (navigationBarPolicy != null) {
            return navigationBarPolicy.getMinNavigationBar();
        }
        return true;
    }

    public void setNaviBarFlag(boolean isHide) {
        if (isHide != this.mIsNavibarHide) {
            this.mIsNavibarHide = isHide;
            HwSlog.d(TAG, "setNeedHideWindow setFlag isNavibarHide is " + this.mIsNavibarHide);
        }
    }

    public boolean getNaviBarFlag() {
        return this.mIsNavibarHide;
    }

    public void setNavigationBarExternal(WindowState state) {
        this.mNavigationBarExternal = state;
    }

    public WindowState getNavigationBarExternal() {
        return this.mNavigationBarExternal;
    }

    public void setNaviImmersiveMode(boolean isImmersiveMode) {
        NavigationBarPolicy navigationBarPolicy = this.mNavigationBarPolicy;
        if (navigationBarPolicy != null) {
            navigationBarPolicy.setImmersiveMode(isImmersiveMode);
        }
        this.mIsImmersiveMode = isImmersiveMode;
    }

    public boolean getImmersiveMode() {
        return this.mIsImmersiveMode;
    }

    public void setInputMethodWindowVisible(boolean isVisible) {
        this.mIsInputMethodWindowVisible = isVisible;
    }

    public NavigationBarPolicy getNavigationBarPolicy() {
        return this.mNavigationBarPolicy;
    }

    public int getRotationValueByType(int type) {
        if (type == 0) {
            return this.mDisplayContent.getDisplayRotation().getLandscapeRotation();
        }
        if (type == 1) {
            return this.mDisplayContent.getDisplayRotation().getSeascapeRotation();
        }
        if (type == 2) {
            return this.mDisplayContent.getDisplayRotation().getPortraitRotation();
        }
        if (type != 3) {
            return 0;
        }
        return this.mDisplayContent.getDisplayRotation().getUpsideDownRotation();
    }

    public int[] getNavigationBarValueForRotation(int index) {
        if (index == 0) {
            return (int[]) this.mNavigationBarHeightForRotationMins.clone();
        }
        if (index == 1) {
            return (int[]) this.mNavigationBarHeightForRotationMaxs.clone();
        }
        if (index == 2) {
            return (int[]) this.mNavigationBarWidthForRotationMaxs.clone();
        }
        if (index != 3) {
            return new int[4];
        }
        return (int[]) this.mNavigationBarWidthForRotationMins.clone();
    }

    private void setNavigationBarValueForRotation(int index, int type, int value) {
        if (index == 0) {
            this.mNavigationBarHeightForRotationMins[type] = value;
        } else if (index == 1) {
            this.mNavigationBarHeightForRotationMaxs[type] = value;
        } else if (index == 2) {
            this.mNavigationBarWidthForRotationMaxs[type] = value;
        } else if (index == 3) {
            this.mNavigationBarWidthForRotationMins[type] = value;
        }
    }

    public boolean computeNaviBarFlag() {
        boolean isForceNavibar;
        WindowManager.LayoutParams focusAttrs = this.mDisplayPolicy.getFocusedWindow() != null ? this.mDisplayPolicy.getFocusedWindow().getAttrs() : null;
        int type = focusAttrs != null ? focusAttrs.type : 0;
        if (focusAttrs != null) {
            isForceNavibar = (focusAttrs.hwFlags & 1) == 1;
        } else {
            isForceNavibar = false;
        }
        boolean isKeyguardOn = type == 2101 || type == 2100;
        boolean isKeyguardDialog = type == 2009 && this.mService.getPolicy().isKeyGuardOn();
        boolean isDreamOn = focusAttrs != null && focusAttrs.type == 2023;
        boolean isNeedHideNaviBarWin = (focusAttrs == null || (focusAttrs.privateFlags & Integer.MIN_VALUE) == 0) ? false : true;
        IHwPhoneWindowManagerEx phoneWindow = null;
        if (this.mService.getPolicy() instanceof PhoneWindowManager) {
            phoneWindow = this.mService.getPolicy().getPhoneWindowManagerEx();
        }
        if (phoneWindow != null && phoneWindow.getFPAuthState()) {
            Log.i(TAG, "in fingerprint authentication,hide nav bar");
            return true;
        } else if (this.mDisplayPolicy.getStatusBar() == this.mDisplayPolicy.getFocusedWindow()) {
            return false;
        } else {
            if (isKeyguardDialog && !isForceNavibar) {
                return true;
            }
            if (isDreamOn) {
                return false;
            }
            if (isKeyguardOn || isNeedHideNaviBarWin) {
                return true;
            }
            if (!getNaviBarFlag() || this.mIsInputMethodWindowVisible) {
                return false;
            }
            return true;
        }
    }

    public void updateNavigationBar(boolean isMinNaviBar) {
        if (isMinNaviBar) {
            this.mDisplayPolicy.setNavigationBarHeightDef(getNavigationBarValueForRotation(0));
            this.mDisplayPolicy.setNavigationBarWidthDef(getNavigationBarValueForRotation(3));
        } else {
            HwSlog.d(TAG, "updateNavigationBar navigationbar mode: " + SystemProperties.getInt("persist.sys.navigationbar.mode", 0));
            Resources res = this.mDisplayContent.getDisplayPolicy().getCurrentUserResources();
            if (res == null) {
                HwSlog.d(TAG, "updateNavigationBar currentUserResources is null");
                return;
            }
            setNavigationBarValueForRotation(1, getRotationValueByType(2), res.getDimensionPixelSize(17105309));
            setNavigationBarValueForRotation(1, getRotationValueByType(3), res.getDimensionPixelSize(17105309));
            setNavigationBarValueForRotation(1, getRotationValueByType(0), res.getDimensionPixelSize(17105311));
            setNavigationBarValueForRotation(1, getRotationValueByType(1), res.getDimensionPixelSize(17105311));
            setNavigationBarValueForRotation(2, getRotationValueByType(2), res.getDimensionPixelSize(17105314));
            setNavigationBarValueForRotation(2, getRotationValueByType(3), res.getDimensionPixelSize(17105314));
            setNavigationBarValueForRotation(2, getRotationValueByType(0), res.getDimensionPixelSize(17105314));
            setNavigationBarValueForRotation(2, getRotationValueByType(1), res.getDimensionPixelSize(17105314));
            this.mDisplayPolicy.setNavigationBarHeightDef(getNavigationBarValueForRotation(1));
            this.mDisplayPolicy.setNavigationBarWidthDef(getNavigationBarValueForRotation(2));
        }
        NavigationBarPolicy navigationBarPolicy = this.mNavigationBarPolicy;
        if (navigationBarPolicy != null) {
            navigationBarPolicy.updateNavigationBar(isMinNaviBar);
        }
    }

    public void initialNavigationSize(Display display, int width, int height, int density) {
        if (density == 0) {
            Log.e(TAG, "density is 0");
        } else if (this.mContext != null) {
            initNavigationBarHightExternal(display, width, height);
            Resources res = this.mDisplayContent.getDisplayPolicy().getCurrentUserResources();
            if (res == null) {
                HwSlog.d(TAG, "initialNavigationSize currentUserResources is null");
                return;
            }
            DisplayRotation displayRotation = this.mDisplayContent.getDisplayRotation();
            int navBarHeight = res.getDimensionPixelSize(17105309);
            this.mNavigationBarHeightForRotationMaxs[displayRotation.getPortraitRotation()] = navBarHeight;
            this.mNavigationBarHeightForRotationMaxs[displayRotation.getUpsideDownRotation()] = navBarHeight;
            int navBarHeightLand = res.getDimensionPixelSize(17105311);
            this.mNavigationBarHeightForRotationMaxs[displayRotation.getLandscapeRotation()] = navBarHeightLand;
            this.mNavigationBarHeightForRotationMaxs[displayRotation.getSeascapeRotation()] = navBarHeightLand;
            ContentResolver resolver = this.mContext.getContentResolver();
            int navBarHeightMin = Settings.System.getInt(resolver, "navigationbar_height_min", 0);
            this.mNavigationBarHeightForRotationMins[displayRotation.getPortraitRotation()] = navBarHeightMin;
            this.mNavigationBarHeightForRotationMins[displayRotation.getUpsideDownRotation()] = navBarHeightMin;
            this.mNavigationBarHeightForRotationMins[displayRotation.getLandscapeRotation()] = navBarHeightMin;
            this.mNavigationBarHeightForRotationMins[displayRotation.getSeascapeRotation()] = navBarHeightMin;
            int navBarWidth = res.getDimensionPixelSize(17105314);
            this.mNavigationBarWidthForRotationMaxs[displayRotation.getPortraitRotation()] = navBarWidth;
            this.mNavigationBarWidthForRotationMaxs[displayRotation.getUpsideDownRotation()] = navBarWidth;
            this.mNavigationBarWidthForRotationMaxs[displayRotation.getLandscapeRotation()] = navBarWidth;
            this.mNavigationBarWidthForRotationMaxs[displayRotation.getSeascapeRotation()] = navBarWidth;
            int navBarMinWidth = Settings.System.getInt(resolver, "navigationbar_width_min", 0);
            this.mNavigationBarWidthForRotationMins[displayRotation.getPortraitRotation()] = navBarMinWidth;
            this.mNavigationBarWidthForRotationMins[displayRotation.getUpsideDownRotation()] = navBarMinWidth;
            this.mNavigationBarWidthForRotationMins[displayRotation.getLandscapeRotation()] = navBarMinWidth;
            this.mNavigationBarWidthForRotationMins[displayRotation.getSeascapeRotation()] = navBarMinWidth;
        }
    }

    public void showTopBar(Handler handler, int displayId) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            handler.postDelayed(new Runnable() {
                /* class com.android.server.wm.HwDisplayPolicyEx.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!HwDisplayPolicyEx.this.isCloudOnPcTop()) {
                        try {
                            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                            if (pcManager != null) {
                                pcManager.showTopBar();
                            }
                        } catch (RemoteException e) {
                            Log.e(HwDisplayPolicyEx.TAG, "error in remote communication");
                        } catch (Exception e2) {
                            Log.e(HwDisplayPolicyEx.TAG, "other error");
                        }
                    }
                }
            }, 200);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCloudOnPcTop() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    for (ActivityManager.RunningTaskInfo info : tasks) {
                        if (info.topActivity != null) {
                            if (info.baseActivity != null) {
                                if ("com.huawei.cloud".equals(info.topActivity.getPackageName()) && "com.huawei.cloud".equals(info.baseActivity.getPackageName()) && HwPCUtils.isPcDynamicStack(info.stackId) && "com.huawei.ahdp.session.VmActivity".equals(info.topActivity.getClassName())) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                    return false;
                }
            }
            return false;
        } catch (RuntimeException e) {
            HwPCUtils.log(TAG, "isCloudOnPCTOP->remote error happened");
        } catch (Exception e2) {
            HwPCUtils.log(TAG, "isCloudOnPCTOP->other error happened");
        }
    }

    private int getNavigationBarWidthExternal() {
        return this.mNavigationBarWidthExternal;
    }

    /* access modifiers changed from: protected */
    public int getNavigationBarHeightExternal() {
        return this.mNavigationBarHeightExternal;
    }

    public void resetCurrentNaviBarHeightExternal() {
        HwPCUtils.log(TAG, "resetCurrentNaviBarHeightExternal");
        if (HwPCUtils.enabled() && this.mNavigationBarHeightExternal != 0) {
            this.mNavigationBarHeightExternal = 0;
            this.mNavigationBarWidthExternal = 0;
        }
    }

    private void initNavigationBarHightExternal(Display display, int width, int height) {
        if (display == null || this.mContext == null) {
            Log.e(TAG, "fail to ini nav, display or context is null");
        } else if (HwPCUtils.enabled() && HwPCUtils.isValidExtDisplayId(display.getDisplayId())) {
            Context externalContext = this.mContext.createDisplayContext(display);
            if (HwPCUtils.isHiCarCastMode()) {
                this.mNavigationBarWidthExternal = getHwHiCarMultiWindowManager().getAppDockWidth();
                this.mNavigationBarHeightExternal = getHwHiCarMultiWindowManager().getAppDockHeight();
            } else {
                this.mNavigationBarHeightExternal = externalContext.getResources().getDimensionPixelSize(34472195);
            }
            HwPCUtils.log(TAG, "mNavigationBarWidthExternal = " + this.mNavigationBarWidthExternal + " mNavigationBarHeightExternal = " + this.mNavigationBarHeightExternal);
        }
    }

    public void removeWindowForPC(WindowState win) {
        if (!HwPCUtils.enabled()) {
            return;
        }
        if (getNavigationBarExternal() == win) {
            setNavigationBarExternal(null);
            this.mNavigationBarControllerExternal.setWindow((WindowState) null);
        } else if (this.mService.getPolicy() instanceof HwPhoneWindowManager) {
            HwPhoneWindowManager policy = this.mService.getPolicy();
            if (policy.mLighterDrawView == win) {
                policy.mLighterDrawView = null;
            }
        }
    }

    public void dumpPC(String prefix, PrintWriter pw) {
        if (HwPCUtils.isPcCastModeInServer()) {
            if (getNavigationBarExternal() != null) {
                pw.print(prefix);
                pw.print("mNavigationBarExternal=");
                pw.println(getNavigationBarExternal());
            }
            BarController barController = this.mNavigationBarControllerExternal;
            if (barController != null) {
                barController.dump(pw, prefix);
            }
        }
    }

    public int prepareAddWindowForPC(WindowState win, WindowManager.LayoutParams attrs) {
        if (!HwPCUtils.isPcCastModeInServer() || win == null || attrs == null) {
            return -10;
        }
        if ((attrs.type == 2019 || (HwPCUtils.isHiCarCastMode() && getHwHiCarMultiWindowManager().isHiCarNavigationBar(attrs))) && HwPCUtils.isValidExtDisplayId(win.getDisplayId())) {
            if (getNavigationBarExternal() != null && getNavigationBarExternal().isAlive()) {
                return -7;
            }
            setNavigationBarExternal(win);
            this.mNavigationBarControllerExternal.setWindow(win);
            return 0;
        } else if (attrs.type != 2104 || !HwPCUtils.isValidExtDisplayId(win.getDisplayId())) {
            return -10;
        } else {
            if (this.mService.getPolicy() instanceof HwPhoneWindowManager) {
                HwPhoneWindowManager policy = this.mService.getPolicy();
                if (policy.mLighterDrawView != null) {
                    return -7;
                }
                policy.mLighterDrawView = win;
            }
            return 0;
        }
    }

    public boolean getStableInsetsForPC(Rect outInsets, int displayId) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId) || outInsets == null) {
            return false;
        }
        outInsets.setEmpty();
        getNonDecorInsetsForPC(outInsets, displayId);
        outInsets.top = 0;
        return true;
    }

    public boolean getNonDecorInsetsForPC(Rect outInsets, int displayId) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId) || outInsets == null) {
            return false;
        }
        outInsets.setEmpty();
        if (!this.mDisplayContent.getDisplayPolicy().hasNavigationBar()) {
            return true;
        }
        if (getNavigationBarExternal() == null || !getNavigationBarExternal().isVisibleLw()) {
            outInsets.bottom = 0;
        } else {
            if (HwPCUtils.isHiCarCastMode()) {
                outInsets.left = getNavigationBarWidthExternal();
            }
            outInsets.bottom = getNavigationBarHeightExternal();
        }
        return true;
    }

    public void beginLayoutForPC(DisplayFrames displayFrames) {
        Context context;
        if (HwPCUtils.isPcCastModeInServer() && getNavigationBarExternal() != null && HwPCUtils.isValidExtDisplayId(displayFrames.mDisplayId)) {
            if (this.mNavigationBarHeightExternal == 0 && (context = this.mContext) != null) {
                initNavigationBarHightExternal(((DisplayManager) context.getSystemService("display")).getDisplay(displayFrames.mDisplayId), displayFrames.mDisplayWidth, displayFrames.mDisplayHeight);
            }
            layoutNavigationBarExternal(displayFrames.mDisplayHeight, displayFrames.mDisplayWidth, 0, displayFrames);
            if (HwPCUtils.enabledInPad() && this.mDisplayContent.getDisplayPolicy().getStatusBar() != null) {
                if (this.mService.isKeyguardLocked()) {
                    this.mDisplayContent.getDisplayPolicy().getStatusBar().computeFrameLw();
                } else {
                    this.mDisplayContent.getDisplayPolicy().getStatusBar().hideLw(false);
                }
            }
        }
    }

    private boolean layoutNavigationBarExternal(int displayHeight, int displayWidth, int overscanBottom, DisplayFrames displayFrames) {
        Rect navigationFrame = new Rect();
        if (!HwPCUtils.isHiCarCastMode() || !getHwHiCarMultiWindowManager().isRotationLandscape()) {
            int top = getNavigationBarExternal().isVisibleLw() ? (displayHeight - overscanBottom) - getNavigationBarHeightExternal() : displayHeight - overscanBottom;
            navigationFrame.set(0, top, displayWidth, displayHeight - overscanBottom);
            displayFrames.mStable.bottom = top;
            displayFrames.mStableFullscreen.bottom = top;
            this.mNavigationBarControllerExternal.setBarShowingLw(true);
            displayFrames.mDock.bottom = displayFrames.mStable.bottom;
            displayFrames.mRestricted.bottom = displayFrames.mStable.bottom;
            displayFrames.mRestrictedOverscan.bottom = displayFrames.mDock.bottom;
            displayFrames.mSystem.bottom = displayFrames.mStable.bottom;
        } else {
            navigationFrame.set(0, 0, getNavigationBarWidthExternal(), displayHeight);
            displayFrames.mStable.left = navigationFrame.right;
            displayFrames.mStableFullscreen.left = navigationFrame.right;
            this.mNavigationBarControllerExternal.setBarShowingLw(true);
            displayFrames.mDock.left = displayFrames.mStable.left;
            displayFrames.mRestricted.left = displayFrames.mStable.left;
            displayFrames.mRestrictedOverscan.left = displayFrames.mDock.left;
            displayFrames.mSystem.left = displayFrames.mStable.left;
        }
        displayFrames.mContent.set(displayFrames.mDock);
        displayFrames.mVoiceContent.set(displayFrames.mDock);
        displayFrames.mCurrent.set(displayFrames.mDock);
        getNavigationBarExternal().getVisibleFrameLw().set(navigationFrame);
        getNavigationBarExternal().getContentFrameLw().set(navigationFrame);
        getNavigationBarExternal().getStableFrameLw().set(navigationFrame);
        getNavigationBarExternal().getParentFrame().set(navigationFrame);
        getNavigationBarExternal().getDisplayFrameLw().set(navigationFrame);
        getNavigationBarExternal().computeFrameLw();
        return false;
    }

    public boolean layoutWindowForPCNavigationBar(WindowState win) {
        return HwPCUtils.isPcCastModeInServer() && getNavigationBarExternal() == win;
    }

    public boolean focusChangedLwForPC(WindowState newFocus) {
        return HwPCUtils.isPcCastModeInServer() && newFocus != null && HwPCUtils.isValidExtDisplayId(newFocus.getDisplayId());
    }

    public void updateWindowFramesForPC(WindowFrames windowFrames, Rect pf, Rect df, Rect cf, Rect vf, boolean isPcDisplay) {
        if (HwPCUtils.isPcCastModeInServer() && isPcDisplay) {
            windowFrames.mParentFrame.set(pf);
            windowFrames.mDisplayFrame.set(df);
            windowFrames.mContentFrame.set(cf);
            windowFrames.mVisibleFrame.set(vf);
        }
    }

    public boolean isGestureIsolated(WindowState focusedWindow, WindowState topFullscreenOpaqueWindowState) {
        WindowState win = focusedWindow != null ? focusedWindow : topFullscreenOpaqueWindowState;
        return win != null && (win.getAttrs().hwFlags & 512) == 512;
    }

    public boolean swipeFromBottom() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
            return true;
        }
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager != null && defaultGestureNavManager.isGestureNavStartedNotLocked() && !this.mGestureNavPolicy.isKeyNavEnabled()) {
            return true;
        }
        DisplayPolicy displayPolicy = this.mDisplayContent.getDisplayPolicy();
        if (!IS_HW_NAVIBAR || !displayPolicy.isLastImmersiveMode() || displayPolicy.getNavigationBar() == null || displayPolicy.getNavBarPosition() != 4) {
            return false;
        }
        NavigationBarPolicy navigationBarPolicy = this.mNavigationBarPolicy;
        if (navigationBarPolicy == null || !navigationBarPolicy.getGameControlReslut(2)) {
            displayPolicy.requestHwTransientBars(displayPolicy.getNavigationBar());
        }
        return true;
    }

    public boolean swipeFromRight() {
        DisplayPolicy displayPolicy = this.mDisplayContent.getDisplayPolicy();
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager != null && defaultGestureNavManager.isGestureNavStartedNotLocked() && !this.mGestureNavPolicy.isKeyNavEnabled()) {
            if (DefaultGestureNavConst.IS_SUPPORT_FULL_BACK) {
                if (this.mGestureNavPolicy.isPointInExcludedRegion(displayPolicy.getGestureStartedPoint()) && displayPolicy.getStatusBar() != null) {
                    displayPolicy.requestHwTransientBars(displayPolicy.getStatusBar());
                    showGameAssist();
                }
            }
            return true;
        } else if (!IS_HW_NAVIBAR || !displayPolicy.isLastImmersiveMode() || displayPolicy.getNavigationBar() == null || displayPolicy.getNavBarPosition() == 4) {
            return false;
        } else {
            NavigationBarPolicy navigationBarPolicy = this.mNavigationBarPolicy;
            if (navigationBarPolicy == null || !navigationBarPolicy.getGameControlReslut(3)) {
                displayPolicy.requestHwTransientBars(displayPolicy.getNavigationBar());
            }
            return true;
        }
    }

    public boolean swipeFromLeft() {
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager == null || !defaultGestureNavManager.isGestureNavStartedNotLocked() || this.mGestureNavPolicy.isKeyNavEnabled() || !DefaultGestureNavConst.IS_SUPPORT_FULL_BACK) {
            return false;
        }
        DisplayPolicy displayPolicy = this.mDisplayContent.getDisplayPolicy();
        if (!this.mGestureNavPolicy.isPointInExcludedRegion(displayPolicy.getGestureStartedPoint()) || displayPolicy.getStatusBar() == null) {
            return false;
        }
        displayPolicy.requestHwTransientBars(displayPolicy.getStatusBar());
        showGameAssist();
        return false;
    }

    public void onPointDown() {
        HwPhoneWindowManager policy;
        if ((this.mService.getPolicy() instanceof HwPhoneWindowManager) && (policy = this.mService.getPolicy()) != null) {
            policy.onPointDown();
        }
    }

    public int getNonDecorDisplayHeight(int fullHeight, int displayId) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId) || getNavigationBarExternal() == null || !getNavigationBarExternal().isVisibleLw()) {
            return fullHeight;
        }
        return fullHeight - getNavigationBarHeightExternal();
    }

    public void onLockTaskStateChangedLw(int lockTaskState) {
        NavigationCallOut navigationCallOut;
        DefaultGestureNavManager defaultGestureNavManager;
        if (this.mIsDefaultDisplay && (defaultGestureNavManager = this.mGestureNavPolicy) != null) {
            defaultGestureNavManager.onLockTaskStateChanged(lockTaskState);
        }
        if (this.mIsDefaultDisplay && (navigationCallOut = this.mNavigationCallOut) != null) {
            navigationCallOut.updateLockTaskState(lockTaskState);
        }
    }

    public int getNonDecorDisplayWidthForExtraDisplay(int fullWidth, int displayId) {
        if (!HwPCUtils.isHiCarCastMode() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            return -1;
        }
        return fullWidth - getHwHiCarMultiWindowManager().getAppDockWidth();
    }

    public int getInputMethodRightForHwMultiDisplay(int dockLeft, int dockRight) {
        return HwPCUtils.isHiCarCastMode() ? getHwHiCarMultiWindowManager().getInputMethodWidth() + dockLeft : dockRight;
    }

    public void addPointerEvent(MotionEvent motionEvent) {
        NavigationBarPolicy navigationBarPolicy = this.mNavigationBarPolicy;
        if (navigationBarPolicy != null) {
            navigationBarPolicy.addPointerEvent(motionEvent);
        }
    }

    public boolean isAppNeedExpand(String packageName) {
        DefaultHwDisplaySideRegionConfig defaultHwDisplaySideRegionConfig = this.mHwDisplaySideConfig;
        return defaultHwDisplaySideRegionConfig != null && defaultHwDisplaySideRegionConfig.isExtendApp(packageName);
    }

    public boolean isSystemApp(String packageName) {
        List<String> systemApps;
        if (packageName == null || (systemApps = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig().getNotchSystemApps()) == null || systemApps.size() == 0) {
            return false;
        }
        for (String name : systemApps) {
            if (packageName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void updateDisplayFrames(WindowState win, DisplayFrames displayFrames, int systemUiFlags, Rect cf, int navHeight) {
        if (win != null && displayFrames != null && cf != null && canUpdateDisplayFrames(win, win.getAttrs(), systemUiFlags) && displayFrames.mRotation == 1) {
            cf.left = 0;
        }
    }

    public boolean canUpdateDisplayFrames(WindowState win, WindowManager.LayoutParams attrs, int systemUiFlags) {
        if (win == null || attrs == null) {
            return false;
        }
        boolean isFullScreen = ((attrs.flags & 1024) != 0) || ((systemUiFlags & 4) != 0 && !win.toString().contains("StatusBar"));
        if (((win.isWindowUsingNotch() && win.getAttrs().layoutInDisplayCutoutMode != 1) || isFullScreen) && !win.inHwMagicWindowingMode()) {
            return false;
        }
        return true;
    }

    private boolean isNeedExceptDisplaySideForExactWindow(WindowState win) {
        WindowManager.LayoutParams attrs = win.getAttrs();
        if (this.mDisplayContent.getRotation() == 1 || this.mDisplayContent.getRotation() == 3) {
            return true;
        }
        if ("com.huawei.android.launcher/com.huawei.android.launcher.splitscreen.SplitScreenAppActivity".equals(attrs.getTitle()) || HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(attrs.packageName)) {
            return false;
        }
        if (!isSystemApp(attrs.packageName)) {
            return !isAppNeedExpand(attrs.packageName);
        }
        if (attrs.layoutInDisplaySideMode != 1) {
            return true;
        }
        return false;
    }

    private boolean isNeedExceptDisplaySideForSplitScreen(int splitWindowMode, WindowState win) {
        int anotherSplitWindowMode;
        Task topTask;
        WindowState topWindow;
        if (isNeedExceptDisplaySideForExactWindow(win)) {
            return true;
        }
        if (WindowConfiguration.isSplitScreenWindowingMode(splitWindowMode)) {
            int i = 3;
            if (splitWindowMode == 3) {
                i = 4;
            }
            anotherSplitWindowMode = i;
        } else {
            int i2 = 100;
            if (splitWindowMode == 100) {
                i2 = 101;
            }
            anotherSplitWindowMode = i2;
        }
        TaskStack anotherSplitStack = this.mDisplayContent.getTopStackInWindowingMode(anotherSplitWindowMode);
        if (anotherSplitStack == null || (topTask = anotherSplitStack.getTopChild()) == null || (topWindow = topTask.getTopVisibleNonPermissionAppMainWindow()) == null) {
            return false;
        }
        return isNeedExceptDisplaySideForExactWindow(topWindow);
    }

    private boolean isNeedExceptDisplaySideInternal(WindowManager.LayoutParams attrs, WindowState win) {
        if (!HwDisplaySizeUtil.hasSideInScreen() || win.getDisplayId() > 0 || win.mIsWallpaper || IS_FACTORY_MODE || "com.huawei.mmitest".equals(attrs.packageName) || win.getAttrs().type == 2034 || this.mService.mPolicy.isKeyguardLockedAndOccluded() || "VolumeIndex".equals(win.getAttrs().getTitle())) {
            return false;
        }
        if (win.inMultiWindowMode() && win.getAttrs().type == 3 && win.getAttrs().getTitle() != null && win.getAttrs().getTitle().toString().startsWith("SnapshotStartingWindow")) {
            return false;
        }
        if ("com.huawei.android.launcher".equals(attrs.packageName) && !win.inSplitScreenWindowingMode()) {
            return false;
        }
        if (this.mService.getLazyMode() != 0 && !win.toString().contains("hwSingleMode_window")) {
            return false;
        }
        int windowingMode = win.getWindowingMode();
        if (windowingMode == 3 || windowingMode == 4 || windowingMode == 100 || windowingMode == 101) {
            return isNeedExceptDisplaySideForSplitScreen(windowingMode, win);
        }
        return isNeedExceptDisplaySideForExactWindow(win);
    }

    public boolean isNeedExceptDisplaySide(WindowManager.LayoutParams attrs, WindowState win, int displayRotation) {
        return isNeedExceptDisplaySideInternal(attrs, win);
    }

    public void updateWindowDisplayFrame(WindowState win, int type, Rect df) {
        if (type == 2038 && win != null && df != null && HwPCUtils.isHiCarCastMode() && !"com.huawei.hicar".equals(win.getOwningPackage())) {
            DefaultHwHiCarMultiWindowManager hicarMultiWindowManager = getHwHiCarMultiWindowManager();
            if (hicarMultiWindowManager.isRotationLandscape()) {
                df.left += hicarMultiWindowManager.getAppDockWidth();
            } else {
                df.bottom -= hicarMultiWindowManager.getAppDockHeight();
            }
        }
    }

    private DefaultHwHiCarMultiWindowManager getHwHiCarMultiWindowManager() {
        return HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwHiCarMultiWindowManager();
    }

    private void showGameAssist() {
        if (this.mGameDockGesture != null && this.mDisplayContent.getDisplayPolicy().getStatusBarController().checkShowTransientBarLw()) {
            this.mGameDockGesture.notifyFingerServiceFromTop();
        }
    }

    public void swipeFromTop() {
        showGameAssist();
    }
}
