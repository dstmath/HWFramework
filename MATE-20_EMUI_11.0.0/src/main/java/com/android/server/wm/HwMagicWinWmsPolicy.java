package com.android.server.wm;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.HwMwUtils;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.HwMagicWinModulePolicy;
import com.android.server.wm.HwMagicWinSplitAnimation;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.screenrecorder.activities.SurfaceControlEx;
import com.huawei.server.magicwin.HwMagicWinAnimation;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.utils.SharedParameters;
import com.huawei.server.utils.Utils;
import com.huawei.utils.HwPartResourceUtils;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HwMagicWinWmsPolicy extends HwMagicWinModulePolicy.ModulePolicy {
    private static final String BYTEDANCE_PUBLISHER_WINDOW_NAME = "com.ss.android.publisher.PublisherActivity";
    private static final Rect CLIP_RECT_FOR_CLEAR = new Rect(0, 0, -1, -1);
    private static final float DELAY_MOVE = 0.375f;
    private static final int MSG_SHOW_DIALOG = 1;
    private static final int NUM_BOUNDS_LIST = 3;
    private static final String PACKAGE_INSTALLER_NAME = "com.android.permissioncontroller";
    private static final int PARAM_INDEX_FIVE = 5;
    private static final int PARAM_INDEX_FOUR = 4;
    private static final int PARAM_INDEX_ONE = 1;
    private static final int PARAM_INDEX_THREE = 3;
    private static final int PARAM_INDEX_TWO = 2;
    private static final int PARAM_INDEX_ZERO = 0;
    private static final String TAG = "HWMW_HwMagicWinWmsPolicy";
    private HwMagicWinModulePolicy.IPolicyOperation computePivotForMagic = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$8Fb9_wT0Ih0LKjoIZUwXs0FHA4 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$10$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation getRotationAnima = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$W6fncVpYlPo2ENb209nq9o_2Vo */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$8$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation isNeedAnimation = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$QUCkbLja238PezJjnrf_580eK8 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$6$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation isNeedScale = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$b2TVD2zoo3Lc0mnv70trZLxbwI */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$5$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation isNeedSync = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$gC60Di1j05YkpBKQ9MXAfTMbqTA */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$13$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation isRightInMagicWindow = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$RU9PcIo_reL8YTImmM6OpGJEsVs */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$11$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private Context mContext = null;
    private String mMoveAnimFromHwFreeformPkg = null;
    private HwMagicWinManager mMwManager = null;
    private SettingsObserver mSettingsObserver;
    private int mUserRotation = 0;
    private int mUserRotationMode = WindowManagerPolicyEx.USER_ROTATION_FREE;
    private WindowManagerServiceEx mWmsEx = null;
    private HwMagicWinModulePolicy.IPolicyOperation setMwRoundCorner = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$GTmOzF62jVWgeihXUnNJnM7yoCU */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$4$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation showDialogIfNeeded = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$TvPOyEX9nyLCpVU3W6TkjSjRow */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$14$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updatePageTypeByKeyEvent = $$Lambda$HwMagicWinWmsPolicy$jG4sv6RU0tHmt0f_XHUdKwGYUo.INSTANCE;
    private HwMagicWinModulePolicy.IPolicyOperation updateStatusBar = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$lxEschfklfeZ9MTMGeWuvuAENe0 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$12$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateSystemUiVisibility = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$WhkAl96xdT4w6AgFHJ5F2Sd64FQ */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$3$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateWindowAttrs = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$zSUti75NV554inkXCKkPiBVsqYw */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$9$HwMagicWinWmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateWindowFrame = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinWmsPolicy$vOQicUac2fLlXWPpdmlGPQZ11c */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinWmsPolicy.this.lambda$new$15$HwMagicWinWmsPolicy(list, bundle);
        }
    };

    public HwMagicWinWmsPolicy(SharedParameters parameters) {
        this.mMwManager = parameters.getMwWinManager();
        this.mContext = parameters.getContext();
        this.mWmsEx = parameters.getWms();
        this.mSettingsObserver = new SettingsObserver(new Handler());
        this.mSettingsObserver.observe();
        addPolicy(HwMagicWinAnimationScene.SCENE_START_APP, this.updateWindowFrame, Object.class, Rect[].class, Object.class, Boolean.class);
        addPolicy(HwMagicWinAnimationScene.SCENE_MIDDLE, this.updateStatusBar, Integer.class, String.class);
        addPolicy(51, this.showDialogIfNeeded, IBinder.class, String.class);
        addPolicy(HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, this.updateWindowAttrs, Object.class);
        addPolicy(71, this.isNeedSync, Integer.class, Integer.class, Object.class);
        addPolicy(70, this.getRotationAnima, Object.class, Integer.class, Integer.class);
        addPolicy(HwMagicWinAnimationScene.SCENE_EXIT_MASTER_TO_SLAVE, this.computePivotForMagic, Object.class, float[].class, Integer.class, Integer.class);
        addPolicy(HwMagicWinAnimationScene.SCENE_ANAN_MASTER_TO_SLAVE, this.isRightInMagicWindow, Object.class);
        addPolicy(21, this.isNeedScale, Integer.class, Integer.class, Point.class, Point.class, Object.class);
        addPolicy(HwMagicWinAnimationScene.SCENE_EXIT_BY_MAGIC_WINDOW, this.setMwRoundCorner, Object.class, SurfaceControl.class, Rect.class);
        addPolicy(107, this.updateSystemUiVisibility, Object.class, Integer.class, Boolean.class);
        addPolicy(208, this.isNeedAnimation, Object.class);
        addPolicy(128, this.updatePageTypeByKeyEvent, Object.class);
    }

    static /* synthetic */ void lambda$new$2(List params, Bundle result) {
        Object curWindow = params.get(0);
        if (curWindow != null) {
            WindowStateEx curWindowEx = new WindowStateEx();
            curWindowEx.resetWindowState(curWindow);
            ActivityRecordEx activity = (ActivityRecordEx) Optional.ofNullable(curWindowEx).map($$Lambda$HwMagicWinWmsPolicy$P2Lmov7G9GCOwVXZX31aW8xmw0I.INSTANCE).map($$Lambda$HwMagicWinWmsPolicy$AiPCD6_0bdjCshe3ecTartTrl2E.INSTANCE).orElse(null);
            if (activity != null) {
                activity.setMagicWindowPageType(1);
            }
        }
    }

    public /* synthetic */ void lambda$new$3$HwMagicWinWmsPolicy(List params, Bundle result) {
        int curVis;
        ActivityRecordEx winAr;
        WindowStateEx curWindow = new WindowStateEx();
        curWindow.resetWindowState(params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(curWindow);
        if (container != null && !container.isVirtualContainer()) {
            int curVis2 = ((Integer) params.get(1)).intValue();
            if (((Boolean) params.get(2)).booleanValue()) {
                curVis = (curVis2 | 32768) & -17;
                if (container.getConfig().isNotchModeEnabled(curWindow.getAttrs().packageName) && container.getBoundsPosition(curWindow.getBounds()) != 5) {
                    curVis &= -1073741833;
                }
                if (curWindow.getAppWindowTokenEx() == null) {
                    winAr = null;
                } else {
                    winAr = curWindow.getAppWindowTokenEx().getActivityRecordEx();
                }
                if (this.mMwManager.getAmsPolicy().isInAppSplitWinMode(winAr)) {
                    curVis &= 2147450879;
                }
            } else {
                curVis = curVis2 & 2147450879;
            }
            result.putInt("RESULT_UPDATE_SYSUIVISIBILITY", curVis);
        }
    }

    public /* synthetic */ void lambda$new$4$HwMagicWinWmsPolicy(List params, Bundle result) {
        WindowStateEx targetWindow = new WindowStateEx();
        targetWindow.resetWindowState(params.get(0));
        SurfaceControl targetSurfaceControl = (SurfaceControl) params.get(1);
        Rect cropSize = (Rect) params.get(2);
        HwMagicContainer container = this.mMwManager.getContainer(targetWindow);
        if (targetWindow.getTaskEx() == null || container == null) {
            if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
                SlogEx.d(TAG, "MW round corner get task is null");
            }
        } else if (!container.getConfig().isSystemSupport(0) || container.getConfig().isSupportAppTaskSplitScreen(targetWindow.getAttrs().packageName)) {
        } else {
            if (targetWindow.inHwMagicWindowingMode()) {
                if (targetWindow.getBounds().equals(targetWindow.getTaskEx().getBounds())) {
                    SurfaceControlEx.setWindowCrop(targetSurfaceControl, CLIP_RECT_FOR_CLEAR);
                    SurfaceControlEx.setCornerRadius(targetSurfaceControl, (float) HwMagicWinAnimation.INVALID_THRESHOLD);
                    return;
                }
                SurfaceControlEx.setWindowCrop(targetSurfaceControl, cropSize);
                SurfaceControlEx.setCornerRadius(targetSurfaceControl, container.getConfig().getCornerRadius());
                targetWindow.setMwIsCornerCropSet(true);
                targetWindow.setWinAnimatorOpaqueLocked(false);
            } else if (targetWindow.isMwIsCornerCropSet()) {
                SurfaceControlEx.setWindowCrop(targetSurfaceControl, CLIP_RECT_FOR_CLEAR);
                SurfaceControlEx.setCornerRadius(targetSurfaceControl, (float) HwMagicWinAnimation.INVALID_THRESHOLD);
                targetWindow.setMwIsCornerCropSet(false);
            }
        }
    }

    public /* synthetic */ void lambda$new$5$HwMagicWinWmsPolicy(List params, Bundle result) {
        WindowStateEx ws = new WindowStateEx();
        ws.resetWindowState(params.get(4));
        HwMagicContainer container = this.mMwManager.getContainer(ws);
        int left = ((Integer) params.get(0)).intValue();
        int top = ((Integer) params.get(1)).intValue();
        Point outPoint = (Point) params.get(2);
        Point tmpPoint = (Point) params.get(3);
        if (container == null || ws.getAppWindowTokenEx() == null) {
            outPoint.offset(-tmpPoint.x, -tmpPoint.y);
            return;
        }
        String pkgName = Utils.getRealPkgName(ws.getAppWindowTokenEx().getActivityRecordEx());
        Rect middlePosition = container.getBounds(3, pkgName);
        Rect slavePosition = container.getBounds(2, pkgName);
        if (pkgName != null && pkgName.equals(this.mMoveAnimFromHwFreeformPkg)) {
            if (left == middlePosition.left) {
                this.mMoveAnimFromHwFreeformPkg = null;
                tmpPoint.set(0, 0);
            }
            if (this.mMoveAnimFromHwFreeformPkg != null && tmpPoint.x == 0) {
                this.mMoveAnimFromHwFreeformPkg = null;
            }
        }
        if (((middlePosition.left == left && middlePosition.top == top) || (slavePosition.left == left && slavePosition.top == top)) ? false : true) {
            boolean isReadyMoveWindow = false;
            if (slavePosition.equals(ws.getAppWindowTokenEx().getBounds())) {
                Rect masterPosition = container.getBounds(1, pkgName);
                isReadyMoveWindow = left == masterPosition.left && top == 0 && (container.getConfig().isRtl() ? masterPosition.left : -slavePosition.left) == outPoint.x && outPoint.x != 0;
            }
            if (!isReadyMoveWindow) {
                outPoint.x = (int) (((float) outPoint.x) * ws.getMwUsedScaleFactor());
                outPoint.y = (int) (((float) outPoint.y) * ws.getMwUsedScaleFactor());
            }
        }
        outPoint.offset((int) (((float) (-tmpPoint.x)) * ws.getMwUsedScaleFactor()), (int) (((float) (-tmpPoint.y)) * ws.getMwUsedScaleFactor()));
    }

    private boolean isClearTransitionAnimation(AppWindowTokenExt appWindowTokenExt, int transit, boolean enter) {
        AppWindowTokenExt appWindowToken = new AppWindowTokenExt();
        appWindowToken.resetAppWindowToken(appWindowTokenExt);
        HwMagicContainer container = this.mMwManager.getContainer(appWindowToken.getActivityRecordEx());
        boolean isClearAnimation = false;
        if (!(container != null && container.getBoundsPosition(appWindowToken.getRequestedOverrideBounds()) == 1 && transit == 6)) {
            return false;
        }
        TaskEx task = appWindowToken.getTaskEx();
        boolean isClearAnimation2 = !enter;
        if (!enter || task == null) {
            return isClearAnimation2;
        }
        for (int index = task.getChildCount() - 1; index >= 0; index--) {
            AppWindowTokenExt token = task.getAppWindowTokenExOfIndex(index);
            if (!token.isExiting() && !token.isClientHidden() && !token.isHiddenRequested() && container.getBoundsPosition(token.getRequestedOverrideBounds()) == 1) {
                if (appWindowToken.getLayer() < token.getLayer()) {
                    isClearAnimation = true;
                }
                return isClearAnimation;
            }
        }
        return isClearAnimation2;
    }

    public /* synthetic */ void lambda$new$6$HwMagicWinWmsPolicy(List params, Bundle result) {
        AppWindowTokenExt appWindowToken;
        Object object = params.get(0);
        if (object instanceof AppWindowTokenExt) {
            appWindowToken = (AppWindowTokenExt) object;
        } else {
            appWindowToken = new AppWindowTokenExt();
            appWindowToken.resetAppWindowToken(object);
        }
        result.putBoolean("RESULT_NEED_SYSTEM_ANIMATION", isUseSystemAnimation(appWindowToken));
    }

    private boolean isUseSystemAnimation(AppWindowTokenExt appWindowToken) {
        HwMagicContainer container = this.mMwManager.getContainer(appWindowToken.getActivityRecordEx());
        if (container == null) {
            return true;
        }
        if (isActivityFullMode(appWindowToken)) {
            return false;
        }
        return container.getConfig().isUsingSystemActivityAnimation(Utils.getPackageName(appWindowToken.getActivityRecordEx()));
    }

    private boolean isActivityFullMode(AppWindowTokenExt appWindowTokenExt) {
        TaskRecordEx taskRecordEx = (TaskRecordEx) Optional.ofNullable(appWindowTokenExt.getActivityRecordEx()).map($$Lambda$HwMagicWinWmsPolicy$7mHEREAM7H8wG6xSR9YsZyc5_8.INSTANCE).orElse(null);
        if (taskRecordEx == null) {
            return false;
        }
        return this.mMwManager.isDragFullMode(taskRecordEx.getTopActivity());
    }

    public void getInputMethodTouchableRegion(Region region) {
        WindowStateEx inputWindow = this.mWmsEx.getRootWindowContainerEx().getCurrentInputMethodWindow();
        if (inputWindow != null) {
            inputWindow.getTouchableRegion(region);
        }
    }

    public boolean isInputMethodWindowVisible() {
        WindowStateEx ime = this.mWmsEx.getRootWindowContainerEx().getCurrentInputMethodWindow();
        if (ime != null) {
            return ime.isVisible();
        }
        return false;
    }

    public void getRatio(List params, Bundle result) {
        AppWindowTokenExt appWindowTokenExt = new AppWindowTokenExt();
        appWindowTokenExt.resetAppWindowToken(params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(appWindowTokenExt.getActivityRecordEx());
        if (container != null) {
            String pkgName = Utils.getRealPkgName(appWindowTokenExt.getActivityRecordEx());
            float ratio = container.getRatio(pkgName);
            boolean isScaled = container.isScaled(pkgName);
            result.putFloat("RESULT_GET_RATIO", ratio);
            result.putBoolean("RESULT_SCALE_ENABLE", isScaled);
        }
    }

    public /* synthetic */ void lambda$new$8$HwMagicWinWmsPolicy(List params, Bundle result) {
        boolean isRotation = false;
        ScreenRotationAnimationEx animation = new ScreenRotationAnimationEx();
        animation.resetScreenRotationAnimation(params.get(0));
        Animation[] results = new Animation[2];
        Integer[] param = {Integer.valueOf(animation.getOriginalRotation()), Integer.valueOf(((Integer) params.get(1)).intValue()), Integer.valueOf(animation.getOriginalHeight()), Integer.valueOf(((Integer) params.get(2)).intValue())};
        HwMagicContainer container = this.mMwManager.getContainer(0);
        if (container != null) {
            if (container.getAnimation() != null && !this.mWmsEx.isKeyguardLocked()) {
                isRotation = container.getAnimation().getRotationAnim(param, results);
                container.getAnimation().resetParamsForRotation();
            }
            if (isRotation) {
                animation.setHwMagicWindow(true);
                animation.setRotateExitAnimation(results[0]);
                animation.setRotateEnterAnimation(results[1]);
            }
        }
    }

    public /* synthetic */ void lambda$new$9$HwMagicWinWmsPolicy(List params, Bundle result) {
        WindowStateEx mFocusedWindow = new WindowStateEx();
        mFocusedWindow.resetWindowState(params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(mFocusedWindow);
        if (container != null && !container.isVirtualContainer()) {
            if (mFocusedWindow.getWindowingMode() == 103 && mFocusedWindow.getAppToken() != null && !PACKAGE_INSTALLER_NAME.equals(mFocusedWindow.getAttrs().packageName)) {
                if ((mFocusedWindow.getHwFlags() & 1073741824) == 0) {
                    mFocusedWindow.getOriginAttrs().copyFrom(mFocusedWindow.getAttrs());
                }
                mFocusedWindow.setHwFlags(mFocusedWindow.getHwFlags() | 1073741824);
                if (container.getConfig().isShowStatusBar(mFocusedWindow.getAttrs().packageName)) {
                    if (mFocusedWindow.getAttrs().type != 1) {
                        return;
                    }
                    if (!this.mMwManager.getAmsPolicy().isFullScreenActivity(mFocusedWindow.getAppWindowTokenEx().getActivityRecordEx()) || (mFocusedWindow.getAttrs().flags & 1024) == 0) {
                        mFocusedWindow.getAttrs().flags |= 2048;
                    }
                } else if (!container.getConfig().isNotchModeEnabled(mFocusedWindow.getAttrs().packageName) || container.getBoundsPosition(mFocusedWindow.getBounds()) == 5) {
                    mFocusedWindow.getAttrs().flags |= mFocusedWindow.getAttrs().flags | 1024 | Integer.MIN_VALUE;
                    mFocusedWindow.getAttrs().flags &= -2049;
                } else {
                    mFocusedWindow.getAttrs().flags |= 2048;
                    mFocusedWindow.getAttrs().flags &= -67108865;
                }
            } else if ((mFocusedWindow.getHwFlags() & 1073741824) != 0) {
                mFocusedWindow.getAttrs().flags = mFocusedWindow.getOriginAttrs().flags;
            }
        }
    }

    public /* synthetic */ void lambda$new$10$HwMagicWinWmsPolicy(List params, Bundle result) {
        WindowStateEx ws = new WindowStateEx();
        ws.resetWindowState(params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(ws);
        if (ws.getAppWindowTokenEx() == null || container == null) {
            SlogEx.w(TAG, "Window state's AppWindowToken is null. w:" + ws);
            return;
        }
        String pkgName = Utils.getRealPkgName(ws.getAppWindowTokenEx().getActivityRecordEx());
        Rect vRect = new Rect(ws.getFrameLw());
        vRect.right = vRect.left + ((int) (((float) vRect.width()) * container.getRatio(pkgName)));
        vRect.bottom = vRect.top + ((int) (((float) vRect.height()) * container.getRatio(pkgName)));
        int iconWidth = ((Integer) params.get(2)).intValue();
        int iconHeight = ((Integer) params.get(3)).intValue();
        float[] scaleTo = HwMagicWinAnimation.computeScaleToForAppExit(vRect, iconWidth, iconHeight);
        result.putFloat("BUNDLE_EXITANIM_SCALETOX", scaleTo[0]);
        result.putFloat("BUNDLE_EXITANIM_SCALETOY", scaleTo[1]);
        float[] pivotTo = HwMagicWinAnimation.computePivotForAppExit(vRect, iconWidth, iconHeight, (float[]) params.get(1));
        result.putFloat("BUNDLE_EXITANIM_PIVOTX", pivotTo[0]);
        result.putFloat("BUNDLE_EXITANIM_PIVOTY", pivotTo[1]);
    }

    public /* synthetic */ void lambda$new$11$HwMagicWinWmsPolicy(List params, Bundle result) {
        WindowStateEx windowState = new WindowStateEx();
        windowState.resetWindowState(params.get(0));
        AppWindowTokenExt token = windowState.getAppWindowTokenEx();
        if (token != null) {
            if (this.mMwManager.isSlave(token.getActivityRecordEx())) {
                result.putBoolean("BUNDLE_ISRIGHT_INMW", true);
            } else if (this.mMwManager.isMaster(token.getActivityRecordEx())) {
                result.putBoolean("RESULT_ISLEFT_INMW", true);
            }
        }
    }

    public /* synthetic */ void lambda$new$12$HwMagicWinWmsPolicy(List params, Bundle result) {
        int flags = ((Integer) params.get(0)).intValue();
        String pkg = (String) params.get(1);
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null && container.isAppSupportMagicWin(pkg) && !container.getConfig().isShowStatusBar(pkg)) {
            result.putInt("enableStatusBar", (flags | -2147482624) & -2049);
        }
    }

    public /* synthetic */ void lambda$new$13$HwMagicWinWmsPolicy(List params, Bundle result) {
        WindowStateEx windowState = new WindowStateEx();
        windowState.resetWindowState(params.get(2));
        HwMagicContainer container = this.mMwManager.getContainer(windowState);
        if (container != null) {
            int fromX = ((Integer) params.get(0)).intValue();
            boolean needSync = true;
            int toX = ((Integer) params.get(1)).intValue();
            ActivityRecordEx ar = this.mMwManager.getAmsPolicy().getTopActivity(container);
            String pkgName = Utils.getRealPkgName(ar);
            int middleLeft = container.getBounds(3, pkgName).left;
            int leftLeft = container.getBounds(1, pkgName).left;
            int rightLeft = container.getBounds(2, pkgName).left;
            boolean isFinishedRightinAnAn = true;
            if (!(windowState.getAppWindowTokenEx() == null || ar == null || ar.getAppWindowTokenEx() == null)) {
                isFinishedRightinAnAn = windowState.isAppTokenNotEqual(ar);
            }
            if (!(middleLeft - leftLeft == fromX - toX || rightLeft - leftLeft == fromX - toX || (leftLeft - rightLeft == fromX - toX && isFinishedRightinAnAn))) {
                needSync = false;
            }
            if (container.getBounds(5, false).equals(windowState.getBounds())) {
                needSync = false;
            }
            if (ar != null && ar.isHwActivityRecord() && ar.isFullScreenVideoInLandscape()) {
                needSync = false;
            }
            result.putBoolean("IS_NEED_SYNC", needSync);
        }
    }

    public /* synthetic */ void lambda$new$14$HwMagicWinWmsPolicy(List params, Bundle result) {
        synchronized (this) {
            ActivityRecordEx resumeActivity = ActivityRecordEx.forToken((IBinder) params.get(0));
            HwMagicContainer container = this.mMwManager.getContainer(resumeActivity);
            if (container != null) {
                if (!container.isVirtualContainer()) {
                    if (!resumeActivity.inHwMagicWindowingMode()) {
                        this.mMwManager.getUIController().dismissDialog();
                        return;
                    }
                    String packageName = (String) params.get(1);
                    if (!TextUtils.isEmpty(packageName)) {
                        if (!packageName.equals(PACKAGE_INSTALLER_NAME)) {
                            this.mMwManager.getUIController().whetherShowDialog(packageName);
                        }
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$new$15$HwMagicWinWmsPolicy(List params, Bundle result) {
        Rect[] windowFrame = (Rect[]) params.get(1);
        if (windowFrame.length == 5) {
            WindowStateEx win = new WindowStateEx();
            win.resetWindowState(params.get(0));
            WindowStateEx naviBar = new WindowStateEx();
            naviBar.resetWindowState(params.get(2));
            adjustWindowFrame(win, windowFrame, naviBar, ((Boolean) params.get(3)).booleanValue());
        }
    }

    private boolean isAdjustCfVfTop(WindowStateEx win, int sysui, HwMagicContainer container) {
        boolean isFullScreen = ((win.getAttrs().flags & 1024) == 0 && (sysui & 4) == 0) ? false : true;
        boolean isForceNonFullscreen = (win.getAttrs().flags & 2048) != 0;
        boolean isShowStatusBar = container.getConfig().isShowStatusBar(win.getAttrs().packageName);
        boolean isNotchModeEnabled = container.getConfig().isNotchModeEnabled(win.getAttrs().packageName);
        if (isShowStatusBar || isNotchModeEnabled) {
            return isForceNonFullscreen || !isFullScreen;
        }
        return false;
    }

    /* JADX INFO: Multiple debug info for r7v4 'cf'  android.graphics.Rect: [D('cf' android.graphics.Rect), D('container' com.android.server.wm.HwMagicContainer)] */
    private void adjustWindowFrame(WindowStateEx win, Rect[] windowFrame, WindowStateEx naviBar, boolean isNaviBarMini) {
        int cfVfTop;
        Rect cf;
        Rect sf;
        Rect vf;
        Rect cf2;
        HwMagicContainer container = this.mMwManager.getContainer(win);
        if (container != null) {
            Rect df = windowFrame[0];
            Rect pf = windowFrame[1];
            Rect cf3 = windowFrame[2];
            Rect vf2 = windowFrame[3];
            Rect sf2 = windowFrame[4];
            Rect bounds = new Rect(win.getBounds());
            float ratio = win.isMwScaleEnabled() ? win.getMwScaleRatioConfig() : 1.0f;
            int position = container.getBoundsPosition(bounds);
            int sysui = win.getAttrs().systemUiVisibility | win.getSubtreeSystemUiVisibility();
            if (container.isVirtualContainer()) {
                pf.set(bounds);
                df.set(bounds);
                cf3.set(bounds);
                vf2.set(bounds);
                sf2.set(bounds);
                adjustImeTarget(win, ratio, sysui, position, bounds, df, pf, cf3, vf2);
            } else if (!win.getAttrs().getTitle().equals("MagicWindowGuideDialog")) {
                int bottom = (int) (((float) bounds.top) + (((float) (naviBar.getFrameLw().top - bounds.top)) / ratio) + 0.5f);
                boolean isLayoutInStable = (sysui & 256) != 0;
                boolean isHideNaviBar = (sysui & 2) != 0;
                boolean isOnBottom = isNaviBarOnBottom(naviBar);
                boolean isNaviVisible = naviBar.isVisibleLw();
                boolean isPositionFullscreen = position == 5;
                int cfVfTop2 = bounds.top;
                if (isAdjustCfVfTop(win, sysui, container)) {
                    cfVfTop = (int) (((float) bounds.top) + (((float) (this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("status_bar_height")) - bounds.top)) / (isPositionFullscreen ? 1.0f : ratio)));
                } else {
                    cfVfTop = cfVfTop2;
                }
                if (isPositionFullscreen) {
                    boolean isInstallerWindow = PACKAGE_INSTALLER_NAME.equals(win.getAttrs().packageName);
                    if (isOnBottom) {
                        int stableBottom = bounds.bottom - naviBar.getFrameLw().height();
                        if (((sysui & 512) != 0 || isHideNaviBar) && win.getAttrs().type >= 1 && win.getAttrs().type <= 1999) {
                            int i = bounds.bottom;
                            df.bottom = i;
                            pf.bottom = i;
                        } else {
                            int i2 = isNaviVisible ? stableBottom : bounds.bottom;
                            df.bottom = i2;
                            pf.bottom = i2;
                        }
                        int i3 = bounds.top;
                        df.top = i3;
                        pf.top = i3;
                        sf = sf2;
                        sf.top = cfVfTop;
                        vf = vf2;
                        vf.top = cfVfTop;
                        cf2 = cf3;
                        cf2.top = cfVfTop;
                        int i4 = (isInstallerWindow || !isNaviVisible) ? bounds.bottom : stableBottom;
                        vf.bottom = i4;
                        cf2.bottom = i4;
                        if (!isNaviVisible && !isNaviBarMini && isLayoutInStable) {
                            cf2.bottom = stableBottom;
                        }
                        if ((win.getAttrs().flags & 512) != 0) {
                            int i5 = bounds.bottom;
                            vf.bottom = i5;
                            cf2.bottom = i5;
                            df.bottom = i5;
                        }
                    } else {
                        sf = sf2;
                        vf = vf2;
                        cf2 = cf3;
                        int stableRight = bounds.right - naviBar.getFrameLw().width();
                        if (win.getAttrs().type == 1000) {
                            int i6 = isNaviVisible ? stableRight : bounds.right;
                            df.right = i6;
                            pf.right = i6;
                        } else {
                            int i7 = bounds.right;
                            df.right = i7;
                            pf.right = i7;
                        }
                        int i8 = (isInstallerWindow || !isNaviVisible) ? bounds.right : stableRight;
                        vf.right = i8;
                        cf2.right = i8;
                        if ((win.getAttrs().flags & 512) != 0) {
                            int i9 = bounds.right;
                            vf.right = i9;
                            cf2.right = i9;
                            df.right = i9;
                            pf.right = i9;
                        }
                    }
                    cf = cf2;
                } else {
                    sf = sf2;
                    vf = vf2;
                    cf = cf3;
                    int right = bounds.left + ((int) (((float) (naviBar.getFrameLw().left - bounds.left)) / ratio));
                    if (!isNaviVisible) {
                        df.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
                        pf.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
                        cf.set(bounds.left, cfVfTop, bounds.right, (isNaviBarMini || !isLayoutInStable || !isHideNaviBar || !isOnBottom) ? bounds.bottom : bottom);
                        vf.set(bounds.left, cfVfTop, bounds.right, bounds.bottom);
                        sf.set(bounds.left, bounds.top, (isOnBottom || isNaviBarMini) ? bounds.right : right, (!isOnBottom || isNaviBarMini) ? bounds.bottom : bottom);
                    } else if (isOnBottom) {
                        df.set(bounds.left, bounds.top, bounds.right, (win.isChildWindow() ? win.getTopParentWindow() : win).getAttrs().getTitle().toString().contains(BYTEDANCE_PUBLISHER_WINDOW_NAME) ? bounds.bottom : bottom);
                        pf.set(bounds.left, bounds.top, bounds.right, bottom);
                        cf.set(bounds.left, cfVfTop, bounds.right, bottom);
                        vf.set(bounds.left, cfVfTop, bounds.right, bottom);
                        sf.set(bounds.left, bounds.top, bounds.right, bottom);
                    } else {
                        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
                        if ((position != 2 || isRtl) && (position != 1 || !isRtl)) {
                            df.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
                            pf.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
                            cf.set(bounds.left, cfVfTop, bounds.right, bounds.bottom);
                            vf.set(bounds.left, cfVfTop, bounds.right, bounds.bottom);
                            sf.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
                        } else {
                            df.set(bounds.left, bounds.top, right, bounds.bottom);
                            pf.set(bounds.left, bounds.top, right, bounds.bottom);
                            cf.set(bounds.left, cfVfTop, right, bounds.bottom);
                            vf.set(bounds.left, cfVfTop, right, bounds.bottom);
                            sf.set(bounds.left, bounds.top, right, bounds.bottom);
                        }
                    }
                }
                if (win.getParentWindow() != null && (win.getAttrs().flags & 256) == 0) {
                    pf.set(win.getParentWindow().getFrameLw());
                }
                adjustImeTarget(win, ratio, sysui, position, bounds, df, pf, cf, vf);
            } else if (!isNaviBarOnBottom(naviBar)) {
                int i10 = bounds.right;
                df.right = i10;
                pf.right = i10;
            }
        }
    }

    private void adjustImeTarget(WindowStateEx win, float ratio, int sysui, int position, Rect bounds, Rect df, Rect pf, Rect cf, Rect vf) {
        int top;
        WindowStateEx ime = this.mWmsEx.getRootWindowContainerEx().getCurrentInputMethodWindow();
        if (ime != null && ime.isVisibleLw() && isInputMethodTarget(win)) {
            if (ime.getDisplayFrameLw().top > ime.getContentFrameLw().top) {
                top = ime.getDisplayFrameLw().top;
            } else {
                top = ime.getContentFrameLw().top;
            }
            int top2 = top + ime.getGivenContentInsetsLw().top;
            int adjustBottom = position == 5 ? top2 : (int) ((((float) (top2 - bounds.top)) / ratio) + ((float) bounds.top) + 0.5f);
            if ((win.getAttrs().flags & 65792) == 0) {
                vf.bottom = adjustBottom;
                cf.bottom = adjustBottom;
                df.bottom = adjustBottom;
                pf.bottom = adjustBottom;
            } else if ((win.getOriginAttrs() == null || (win.getOriginAttrs().flags & 1024) == 0) && (win.getAttrs().softInputMode & 240) == 16) {
                vf.bottom = adjustBottom;
                cf.bottom = adjustBottom;
            } else {
                vf.bottom = adjustBottom;
            }
            if (win.getParentWindow() != null && (win.getParentWindow().getAttrs().softInputMode & 240) != 48) {
                win.getParentWindow().getVisibleFrameLw().bottom = adjustBottom;
                win.getParentWindow().getVisibleInsets().bottom = win.getParentWindow().getFrameLw().bottom - adjustBottom;
            }
        }
    }

    private boolean isNaviBarOnBottom(WindowStateEx naviBar) {
        return naviBar.getFrameLw().width() >= naviBar.getFrameLw().height();
    }

    private boolean isInputMethodTarget(WindowStateEx win) {
        TaskStackEx imeTargetStack = this.mWmsEx.getImeFocusStackLocked();
        DisplayContentEx dcEx = this.mWmsEx.getRootWindowContainerEx().getDisplayContentEx(win.getDisplayId());
        WindowStateEx target = dcEx != null ? dcEx.getInputMethodTarget() : null;
        return (target != null && win.equalsWindowState(target)) || (imeTargetStack != null && imeTargetStack.hasChild(win.getTaskEx()) && target != null && !target.inHwMagicWindowingMode());
    }

    public int getOrientation() {
        if (this.mUserRotationMode != WindowManagerPolicyEx.USER_ROTATION_LOCKED) {
            return 4;
        }
        int i = this.mUserRotation;
        if (i == 0 || i == 2) {
            return -3;
        }
        return -1;
    }

    public boolean isUserRotationLocked() {
        return this.mUserRotationMode == WindowManagerPolicyEx.USER_ROTATION_LOCKED;
    }

    public void updateSettings() {
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mUserRotation = SettingsEx.System.getIntForUser(resolver, "user_rotation", 0, -2);
        this.mUserRotationMode = SettingsEx.System.getIntForUser(resolver, "accelerometer_rotation", 0, -2) != 0 ? WindowManagerPolicyEx.USER_ROTATION_FREE : WindowManagerPolicyEx.USER_ROTATION_LOCKED;
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = HwMagicWinWmsPolicy.this.mContext.getContentResolver();
            ContentResolverExt.registerContentObserver(resolver, Settings.System.getUriFor("accelerometer_rotation"), false, this, -1);
            ContentResolverExt.registerContentObserver(resolver, Settings.System.getUriFor("user_rotation"), false, this, -1);
            HwMagicWinWmsPolicy.this.updateSettings();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            HwMagicWinWmsPolicy.this.updateSettings();
        }
    }

    public boolean getAllDrawnByActivity(IBinder binder) {
        AppWindowTokenExt wtoken = this.mWmsEx.getRootWindowContainerEx().getAppWindowTokenEx(binder);
        if (wtoken != null) {
            if (!(wtoken.isAllDrawn() && !wtoken.isRelaunching()) && !wtoken.isStartingDisplayed() && !wtoken.isStartingMoved()) {
                return false;
            }
        }
        return true;
    }

    private void setWindowFrame(AppWindowTokenExt appWindowToken, Rect frame) {
        WindowStateEx windowState = appWindowToken.findMainWindow();
        if (appWindowToken.inHwMagicWindowingMode() && windowState != null) {
            frame.set(0, 0, (int) (((float) appWindowToken.getBounds().width()) * windowState.getMwUsedScaleFactor()), (int) (((float) appWindowToken.getBounds().height()) * windowState.getMwUsedScaleFactor()));
        }
    }

    public Animation getMagicAnimation(Animation animation, AppWindowTokenExt appWindowToken, int transit, boolean enter, Rect frame, boolean isAppLauncher) {
        ActivityStackEx focusedTopStack;
        HwMagicContainer container = this.mMwManager.getContainer(appWindowToken.getActivityRecordEx());
        if (container == null) {
            return animation;
        }
        if (isActivityFullMode(appWindowToken)) {
            if (transit == 25) {
                animation.setZAdjustment(0);
            }
            return animation;
        } else if (!isUseSystemAnimation(appWindowToken)) {
            return animation;
        } else {
            if (!appWindowToken.inHwMagicWindowingMode()) {
                if (transit != 12 || enter || !isAppLauncher || (focusedTopStack = this.mMwManager.getAmsPolicy().getFocusedTopStack(container)) == null || !focusedTopStack.inHwMagicWindowingMode()) {
                    return animation;
                }
                return container.getAnimation().getMwWallpaperCloseAnimation();
            } else if (isClearTransitionAnimation(appWindowToken, transit, enter)) {
                return null;
            } else {
                setWindowFrame(appWindowToken, frame);
                return container.getAnimation().getMagicAppAnimation(animation, enter, transit);
            }
        }
    }

    public void startSplitAnimation(IBinder token, String packageName) {
        AppWindowTokenExt appToken;
        HwMagicContainer container;
        if (token != null && packageName != null && (appToken = this.mWmsEx.getRootWindowContainerEx().getAppWindowTokenEx(token)) != null && (container = this.mMwManager.getContainer(appToken.getActivityRecordEx())) != null) {
            Rect startBounds = appToken.getTaskEx().getBounds();
            Rect endBounds = new Rect(container.getBounds(2, packageName));
            container.getConfig().adjustSplitBound(2, endBounds);
            new HwMagicWinSplitAnimation.SplitScreenAnimation().startSplitScreenAnimation(appToken, container.getAnimation().getSplitAnimation(startBounds, endBounds, appToken.getDisplayContentEx().getDisplayInfoEx()), false, HwMagicWinAnimation.INVALID_THRESHOLD, container.getDisplayId());
        }
    }

    public void startExitSplitAnimation(IBinder token, float cornerRadius) {
        AppWindowTokenExt appToken;
        HwMagicContainer container;
        if (token != null && (appToken = this.mWmsEx.getRootWindowContainerEx().getAppWindowTokenEx(token)) != null && (container = this.mMwManager.getContainer(appToken.getActivityRecordEx())) != null) {
            Rect startBounds = appToken.getTaskEx().getBounds();
            DisplayInfoEx displayInfo = appToken.getDisplayContentEx().getDisplayInfoEx();
            new HwMagicWinSplitAnimation.SplitScreenAnimation().startSplitScreenAnimation(appToken, container.getAnimation().getSplitAnimation(startBounds, new Rect(0, 0, displayInfo.getLogicalWidth(), displayInfo.getLogicalHeight()), displayInfo), true, cornerRadius, container.getDisplayId());
        }
    }

    public void startMoveAnimation(IBinder enterToken, IBinder exitToken, String packageName, boolean isAdjust) {
        HwMagicContainer container;
        if (enterToken != null && exitToken != null && packageName != null) {
            AppWindowTokenExt enterAppToken = this.mWmsEx.getRootWindowContainerEx().getAppWindowTokenEx(enterToken);
            AppWindowTokenExt exitAppToken = this.mWmsEx.getRootWindowContainerEx().getAppWindowTokenEx(exitToken);
            if (enterAppToken != null && exitAppToken != null && (container = this.mMwManager.getContainer(exitAppToken.getActivityRecordEx())) != null) {
                Rect startBounds = enterAppToken.getTaskEx().getBounds();
                Rect endBounds = new Rect(container.getBounds(2, packageName));
                if (isAdjust) {
                    container.getConfig().adjustSplitBound(2, endBounds);
                }
                DisplayInfoEx displayInfo = enterAppToken.getDisplayContentEx().getDisplayInfoEx();
                HwMagicWinAnimation animation = container.getAnimation();
                new HwMagicWinSplitAnimation.SplitScreenAnimation().startMultiTaskAnimation(enterAppToken.getTaskEx(), exitAppToken.getTaskEx(), container.getDisplayId(), animation.getSplitAnimation(startBounds, endBounds, displayInfo), animation.getExitTaskAnimation(endBounds, displayInfo), !HwMwUtils.IS_FOLD_SCREEN_DEVICE);
            }
        }
    }

    public void startMoveAnimationFullScreen(IBinder enterToken, IBinder exitToken) {
        HwMagicContainer container;
        if (enterToken != null && exitToken != null) {
            AppWindowTokenExt enterAppToken = this.mWmsEx.getRootWindowContainerEx().getAppWindowTokenEx(enterToken);
            AppWindowTokenExt exitAppToken = this.mWmsEx.getRootWindowContainerEx().getAppWindowTokenEx(exitToken);
            if (enterAppToken != null && exitAppToken != null && (container = this.mMwManager.getContainer(exitAppToken.getActivityRecordEx())) != null) {
                Rect startBounds = enterAppToken.getTaskEx().getBounds();
                DisplayInfoEx displayInfo = enterAppToken.getDisplayContentEx().getDisplayInfoEx();
                Rect endBounds = new Rect(0, 0, displayInfo.getLogicalWidth(), displayInfo.getLogicalHeight());
                HwMagicWinAnimation animation = container.getAnimation();
                new HwMagicWinSplitAnimation.SplitScreenAnimation().startMultiTaskAnimation(enterAppToken.getTaskEx(), exitAppToken.getTaskEx(), container.getDisplayId(), animation.getSplitAnimation(startBounds, endBounds, displayInfo), animation.getExitTaskAnimation(endBounds, displayInfo), true);
            }
        }
    }

    public void setMoveAnimFromHwFreeform(String pkgName) {
        this.mMoveAnimFromHwFreeformPkg = pkgName;
    }
}
