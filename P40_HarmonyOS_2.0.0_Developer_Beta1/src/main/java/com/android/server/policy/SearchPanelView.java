package com.android.server.policy;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Flog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.app.AssistUtils;
import com.android.server.LocalServices;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.provider.FrontFingerPrintSettings;
import java.lang.reflect.Method;
import java.util.List;

public class SearchPanelView extends FrameLayout {
    private static final String ACTION_SINGLE_BUTTON_KEYEVENT = "com.android.huawei.SINGLE_BUTTON_ACTION_KEYEVENT";
    public static final Interpolator ALPHA_OUT = new PathInterpolator(0.0f, 0.0f, 0.8f, 1.0f);
    private static String ASSISTANT_ACTION = "com.huawei.action.VOICE_ASSISTANT";
    private static String ASSISTANT_ACTIVITY_NAME = "com.huawei.vassistant.ui.main.VAssistantActivity";
    private static final String ASSISTANT_ICON_METADATA_NAME = "com.android.systemui.action_assist_icon";
    private static final int AXIS_MOVE_FROM_INSIDE = 0;
    private static final int AXIS_MOVE_FROM_OUTSIDE = 1;
    private static final int AXIS_TOUCH_DATA_VALID = 1;
    private static final String EVENT_POINTER_COUNT = "pointer_count";
    private static final String EVENT_X = "event_x";
    private static final String EVENT_Y = "event_y";
    private static final String FINGER_PRINT_ACTION_KEYEVENT = "com.android.huawei.FINGER_PRINT_ACTION_KEYEVENT";
    protected static final String HW_ASSISTANT_PACKAGE_NAME = "com.huawei.vassistant";
    public static final String INTENT_KEY = "keycode";
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final boolean IS_HDL_DCM = "HDL-L0J".equals(SystemProperties.get("ro.product.name", ""));
    private static final int KEYCODE_START_VASSISTANT = 9999;
    private static final int MSG_HIDE_SEARCH_VIEW = 2;
    private static final int MSG_MOVE_EVENT = 5;
    private static final int MSG_SHOW_SEARCH_VIEW = 1;
    private static final int MSG_START_ABORT_ANIMATION = 3;
    private static final int MSG_START_EXIT_ANIMATION = 4;
    private static final String TAG = SearchPanelView.class.getSimpleName();
    private static ComponentName sVAssistantComponentName = null;
    private Boolean isLandScapeProduct;
    private boolean mAdded;
    private AssistUtils mAssistUtils;
    private SearchPanelCircleView mCircle;
    private Context mContext;
    private boolean mDraggedFarEnough;
    private boolean mDragging;
    private float mEffctiveStartTouch;
    private Handler mHandler;
    private Boolean mHasHwAssist;
    private boolean mIsCollapseStatusBar;
    private boolean mIsFromOutScreen;
    private boolean mIsTouchDataValid;
    private boolean mLaunchPending;
    private boolean mLaunching;
    private ImageView mLogo;
    private int mNavigationBarHeight;
    private boolean mNeedShowSearchPanel;
    private boolean mRotationReady;
    private View mScrim;
    private boolean mShowing;
    private float mStartDrag;
    private float mStartTouch;
    private float mStartX;
    private float mStartY;
    private int mThreshold;

    public SearchPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        boolean z = true;
        this.mIsFromOutScreen = true;
        this.mHasHwAssist = null;
        this.isLandScapeProduct = Boolean.valueOf(SystemProperties.getInt("ro.panel.hw_orientation", 0) != 90 ? false : z);
        this.mNeedShowSearchPanel = false;
        this.mRotationReady = false;
        this.mIsCollapseStatusBar = false;
        this.mContext = context;
        this.mThreshold = context.getResources().getDimensionPixelSize(34472148);
        this.mNavigationBarHeight = context.getResources().getDimensionPixelSize(17105309);
        this.mAssistUtils = new AssistUtils(context);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCircle = (SearchPanelCircleView) findViewById(34603139);
        this.mLogo = (ImageView) findViewById(34603140);
        this.mScrim = findViewById(34603141);
        maybeSwapSearchIcon();
    }

    private ComponentName getAssistInfo() {
        AssistUtils assistUtils = this.mAssistUtils;
        if (assistUtils != null) {
            return assistUtils.getAssistComponentForUser(-2);
        }
        return null;
    }

    private void maybeSwapSearchIcon() {
        if (hasHwAssist()) {
            replaceDrawable(this.mLogo, getVAssistantComponentName(), ASSISTANT_ICON_METADATA_NAME);
            return;
        }
        Intent intent = ((SearchManager) this.mContext.getSystemService("search")).getAssistIntent(false);
        if (intent != null) {
            replaceDrawable(this.mLogo, intent.getComponent(), ASSISTANT_ICON_METADATA_NAME);
            return;
        }
        this.mLogo.setImageDrawable(null);
    }

    public void replaceDrawable(ImageView imageView, ComponentName component, String name) {
        int iconResId;
        if (component != null) {
            try {
                PackageManager packageManager = this.mContext.getPackageManager();
                Bundle metaData = packageManager.getActivityInfo(component, 128).metaData;
                if (metaData != null && (iconResId = metaData.getInt(name)) != 0) {
                    imageView.setImageDrawable(packageManager.getResourcesForActivity(component).getDrawable(iconResId));
                }
            } catch (PackageManager.NameNotFoundException e) {
                String str = TAG;
                Log.w(str, "Failed to swap drawable; " + component.flattenToShortString() + " not found", e);
            } catch (Resources.NotFoundException nfe) {
                String str2 = TAG;
                Log.w(str2, "Failed to swap drawable from " + component.flattenToShortString(), nfe);
            }
        }
    }

    private ComponentName getVAssistantComponentName() {
        ComponentName componentName = sVAssistantComponentName;
        if (componentName != null) {
            return componentName;
        }
        sVAssistantComponentName = new ComponentName(HW_ASSISTANT_PACKAGE_NAME, ASSISTANT_ACTIVITY_NAME);
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            Log.w(TAG, "packageManager is null");
            return sVAssistantComponentName;
        }
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(new Intent(ASSISTANT_ACTION), 65536);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            Log.w(TAG, "resolveInfos is null");
            return sVAssistantComponentName;
        }
        ComponentInfo info = resolveInfos.get(0).activityInfo;
        if (info == null) {
            Log.w(TAG, "activityInfo is null");
            return sVAssistantComponentName;
        }
        sVAssistantComponentName = new ComponentName(info.packageName, info.name);
        return sVAssistantComponentName;
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchHoverEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0055, code lost:
        if (r0 != 3) goto L_0x0195;
     */
    public boolean handleGesture(MotionEvent event) {
        float lastTouch;
        if (isNotReady(event)) {
            return false;
        }
        if (!this.mIsTouchDataValid && 1 == ((int) event.getAxisValue(37))) {
            Log.d(TAG, " mIsTouchDataValid:true");
            this.mIsTouchDataValid = true;
        }
        if (this.mIsTouchDataValid) {
            this.mIsFromOutScreen = ((int) event.getAxisValue(36)) != 0;
            String str = TAG;
            Log.d(str, "mIsFromOutScreen:" + this.mIsFromOutScreen);
        }
        int action = event.getActionMasked();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    move(event);
                }
            }
            if (!this.mIsFromOutScreen) {
                return true;
            }
            if (this.isLandScapeProduct.booleanValue()) {
                lastTouch = event.getY();
            } else {
                lastTouch = this.mCircle.isLand() ? event.getX() : event.getY();
                if (IS_HDL_DCM && this.mCircle.isLand()) {
                    lastTouch = event.getY();
                }
            }
            String str2 = TAG;
            Log.d(str2, "lastTouch is:" + lastTouch + "mDraggedFarEnough=" + this.mDraggedFarEnough + ",mRotationReady=" + this.mRotationReady);
            if (Math.abs(this.mStartTouch - lastTouch) <= ((float) this.mThreshold)) {
                Log.d(TAG, "Not Far Enough 1");
            } else if (!IS_CHINA && hasHwAssist()) {
                if (this.mRotationReady) {
                    startVoiceAssist();
                }
                this.mIsFromOutScreen = true;
                return true;
            }
            if (!this.mDraggedFarEnough || IS_CHINA) {
                abortAnimation();
                Log.d(TAG, "Not Far Enough 2");
            } else {
                processDraggedEvent();
            }
            this.mIsCollapseStatusBar = false;
            this.mIsFromOutScreen = true;
        } else {
            if (this.isLandScapeProduct.booleanValue()) {
                this.mStartTouch = event.getY();
            } else {
                this.mStartTouch = this.mCircle.isLand() ? event.getX() : event.getY();
                if (IS_HDL_DCM && this.mCircle.isLand()) {
                    this.mStartTouch = event.getY();
                }
            }
            this.mEffctiveStartTouch = this.mCircle.isLand() ? event.getY() : event.getX();
            this.mDragging = false;
            this.mDraggedFarEnough = false;
            this.mCircle.reset();
            this.mStartX = this.mCircle.isLand() ? event.getY() : event.getX();
            this.mStartY = this.mCircle.isLand() ? event.getX() : event.getY();
            if (IS_HDL_DCM && this.mCircle.isLand()) {
                this.mStartX = event.getX();
                this.mStartY = event.getY();
            }
            this.mNeedShowSearchPanel = false;
            this.mRotationReady = false;
        }
        return true;
    }

    private int getRotationBetweenPoints(float eventX, float eventY) {
        double rotationDegree = 0.0d;
        try {
            float endX = this.mCircle.isLand() ? eventY : eventX;
            float endY = this.mCircle.isLand() ? eventX : eventY;
            if (IS_HDL_DCM && this.mCircle.isLand()) {
                endX = eventX;
                endY = eventY;
            }
            String str = TAG;
            Log.d(str, "endX = " + endX + " endY = " + endY);
            float f = this.mStartX;
            if (f != endX && this.mStartY > endY) {
                double distanceX = (double) Math.abs(f - endX);
                double distanceY = (double) Math.abs(this.mStartY - endY);
                String str2 = TAG;
                Log.d(str2, " distanceX = " + distanceX + " distanceY = " + distanceY);
                rotationDegree = (Math.atan(distanceY / distanceX) / 3.141592653589793d) * 180.0d;
            }
            String str3 = TAG;
            Log.d(str3, "getRotationBetweenPoints rotationDegree = " + rotationDegree);
            return (int) rotationDegree;
        } catch (IllegalArgumentException e) {
            String str4 = TAG;
            Log.w(str4, "getRotationBetweenPoints exception, " + e);
            return 0;
        }
    }

    private boolean isNotReady(MotionEvent event) {
        return this.mLaunching || this.mLaunchPending || this.mCircle == null || event.getPointerCount() > 1;
    }

    private void processDraggedEvent() {
        if (this.mCircle.isAnimationRunning(true)) {
            this.mLaunchPending = true;
            this.mCircle.setAnimatingOut(true);
            this.mCircle.performOnAnimationFinished(new Runnable() {
                /* class com.android.server.policy.SearchPanelView.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    SearchPanelView.this.exitAnimation();
                }
            });
            return;
        }
        exitAnimation();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onMove(Bundle bundle) {
        float currentTouch;
        if (bundle == null) {
            Log.w(TAG, "onMove return because the event is null");
        } else if (bundle.getInt(EVENT_POINTER_COUNT) == 0) {
            Log.w(TAG, "onMove return because the pointer count is 0, it will crash if go on.");
        } else {
            float currentEventX = bundle.getFloat(EVENT_X);
            float currentEventY = bundle.getFloat(EVENT_Y);
            try {
                if (this.isLandScapeProduct.booleanValue()) {
                    currentTouch = currentEventY;
                } else {
                    currentTouch = this.mCircle.isLand() ? currentEventX : currentEventY;
                    if (IS_HDL_DCM && this.mCircle.isLand()) {
                        currentTouch = currentEventY;
                    }
                }
                String str = TAG;
                Log.d(str, "onMove showSearchPanelView:" + this.mIsFromOutScreen);
                boolean z = true;
                if (Math.abs(this.mStartTouch - currentTouch) > ((float) this.mNavigationBarHeight) && !isShowing()) {
                    if (!this.mIsCollapseStatusBar) {
                        collapseStatusBar(this.mContext);
                        this.mIsCollapseStatusBar = true;
                    }
                    if (this.mIsFromOutScreen) {
                        if (IS_CHINA) {
                            if (this.isLandScapeProduct.booleanValue()) {
                                showSearchPanelView();
                            } else if (!this.mNeedShowSearchPanel) {
                                this.mNeedShowSearchPanel = true;
                                int rotationDegree = getRotationBetweenPoints(currentEventX, currentEventY);
                                if (rotationDegree >= 70 && rotationDegree <= 90) {
                                    showSearchPanelView();
                                }
                            }
                        } else if (!this.mNeedShowSearchPanel) {
                            this.mNeedShowSearchPanel = true;
                            int rotationDegree2 = getRotationBetweenPoints(currentEventX, currentEventY);
                            if (rotationDegree2 >= 70 && rotationDegree2 <= 90) {
                                this.mRotationReady = true;
                            }
                        }
                    }
                }
                if (getVisibility() == 0 && !this.mDragging && (!this.mCircle.isAnimationRunning(true) || Math.abs(this.mStartTouch - currentTouch) > ((float) this.mThreshold))) {
                    this.mStartDrag = currentTouch;
                    this.mDragging = true;
                }
                if (this.mDragging) {
                    float f = this.mStartDrag;
                    this.mCircle.setDragDistance(f > currentTouch ? f - currentTouch : 0.0f);
                    if (Math.abs(this.mStartTouch - currentTouch) <= ((float) this.mThreshold)) {
                        z = false;
                    }
                    this.mDraggedFarEnough = z;
                    this.mCircle.setDraggedFarEnough(this.mDraggedFarEnough);
                }
            } catch (IllegalArgumentException e) {
                String str2 = TAG;
                Log.w(str2, "onMove return because there is an IllegalArgumentException, " + e);
            }
        }
    }

    public void show(boolean show, boolean animate) {
        this.mShowing = show;
        if (show) {
            maybeSwapSearchIcon();
            if (getVisibility() != 0) {
                setVisibility(0);
                if (animate) {
                    startEnterAnimation();
                    notifyTrikeyEvent(KEYCODE_START_VASSISTANT);
                } else {
                    this.mScrim.setAlpha(1.0f);
                }
            }
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        } else if (animate) {
            startAbortAnimation();
        } else {
            setVisibility(4);
        }
    }

    private void notifyTrikeyEvent(int keyCode) {
        ContentResolver resolver = this.mContext.getContentResolver();
        int isShowNaviGuide = Settings.System.getIntForUser(resolver, "systemui_tips_already_shown", 0, ActivityManager.getCurrentUser());
        boolean aiEnable = FrontFingerPrintSettings.isSingleNavBarAIEnable(resolver);
        boolean aiNavEnable = FrontFingerPrintSettings.isSingleVirtualNavbarEnable(resolver);
        String str = TAG;
        Log.d(str, "isShowNaviGuide:" + isShowNaviGuide + ", aiEnable:" + aiEnable + ", aiNavEnable:" + aiNavEnable);
        if (aiNavEnable && !aiEnable && !FrontFingerPrintSettings.isChinaArea()) {
            Intent intent = new Intent(ACTION_SINGLE_BUTTON_KEYEVENT);
            intent.putExtra(INTENT_KEY, keyCode);
            intent.setPackage(HwCustPkgNameConstant.HW_SYSTEMUI_PACKAGE);
            intent.addFlags(268435456);
            this.mContext.sendBroadcast(intent, "android.permission.STATUS_BAR");
        } else if (isShowNaviGuide == 0 && !FrontFingerPrintSettings.isNaviBarEnabled(resolver)) {
            Intent intent2 = new Intent(FINGER_PRINT_ACTION_KEYEVENT);
            intent2.putExtra(INTENT_KEY, keyCode);
            intent2.setPackage(HwCustPkgNameConstant.HW_SYSTEMUI_PACKAGE);
            intent2.addFlags(268435456);
            this.mContext.sendBroadcast(intent2, "android.permission.STATUS_BAR");
        }
    }

    private void startEnterAnimation() {
        this.mCircle.startEnterAnimation();
        this.mScrim.setAlpha(1.0f);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAbortAnimation() {
        this.mCircle.startAbortAnimation(new Runnable() {
            /* class com.android.server.policy.SearchPanelView.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                SearchPanelView.this.mCircle.setAnimatingOut(false);
                SearchPanelView.this.setVisibility(4);
            }
        });
        this.mCircle.setAnimatingOut(true);
        this.mScrim.setAlpha(0.0f);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startExitAnimation() {
        this.mLaunchPending = false;
        if (!this.mLaunching && getVisibility() == 0) {
            this.mLaunching = true;
            startVoiceAssist();
            this.mCircle.setAnimatingOut(true);
            this.mCircle.startExitAnimation(new Runnable() {
                /* class com.android.server.policy.SearchPanelView.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    SearchPanelView.this.mLaunching = false;
                    SearchPanelView.this.mCircle.setAnimatingOut(false);
                    SearchPanelView.this.setVisibility(4);
                }
            });
            this.mScrim.animate().alpha(0.0f).setDuration(300).setStartDelay(0).setInterpolator(ALPHA_OUT);
        }
    }

    private void startVoiceAssist() {
        try {
            notifyTrikeyEvent(KEYCODE_START_VASSISTANT);
            if (FrontFingerPrintSettings.isSingleVirtualNavbarEnable(this.mContext.getContentResolver())) {
                Flog.bdReport(this.mContext, 11, "{type:singleVirturalBar}");
            } else {
                Flog.bdReport(this.mContext, 11, "{type:NoVirturalBar}");
            }
            ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).startAssist(new Bundle());
        } catch (Exception e) {
            Log.e(TAG, "startVoiceAssist error");
        }
    }

    public void hide(boolean animate) {
        if (animate) {
            startAbortAnimation();
        } else {
            setVisibility(4);
        }
    }

    public boolean isShowing() {
        return getVisibility() == 0 && !this.mCircle.isAnimatingOut();
    }

    public void initUI(Looper looper) {
        initHandler(looper);
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class com.android.server.policy.SearchPanelView.AnonymousClass4 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    SearchPanelView.this.show(true, true);
                } else if (i == 2) {
                    SearchPanelView.this.hide(true);
                } else if (i == 3) {
                    SearchPanelView.this.startAbortAnimation();
                } else if (i == 4) {
                    SearchPanelView.this.startExitAnimation();
                } else if (i == 5) {
                    SearchPanelView.this.onMove(msg.getData());
                }
            }
        };
    }

    public void showSearchPanelView() {
        Handler handler;
        if (hasHwAssist() && !IS_CHINA && (handler = this.mHandler) != null) {
            handler.sendEmptyMessage(1);
        }
    }

    public void hideSearchPanelView() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(2);
        }
    }

    public boolean isInContentArea(int x, int y) {
        return true;
    }

    public void abortAnimation() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(3);
        }
    }

    public void exitAnimation() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(4);
        }
    }

    public void move(MotionEvent event) {
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = 5;
            Bundle bundle = new Bundle();
            bundle.putFloat(EVENT_X, event.getX());
            bundle.putFloat(EVENT_Y, event.getY());
            bundle.putInt(EVENT_POINTER_COUNT, event.getPointerCount());
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    public static final void collapseStatusBar(Context context) {
        Method collapse;
        Object sbservice = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            if (Build.VERSION.SDK_INT >= 17) {
                collapse = statusBarManager.getMethod("collapsePanels", new Class[0]);
            } else {
                collapse = statusBarManager.getMethod("collapse", new Class[0]);
            }
            collapse.invoke(sbservice, new Object[0]);
        } catch (ClassNotFoundException ce) {
            String str = TAG;
            Log.w(str, "Class not found for StatusBarManager:" + ce.getMessage());
        } catch (NoSuchMethodException me) {
            String str2 = TAG;
            Log.w(str2, "Method not found for collapse:" + me.getMessage());
        } catch (RuntimeException re) {
            String str3 = TAG;
            Log.e(str3, "collapseStatusBar RuntimeException :" + re.getMessage());
        } catch (Exception e) {
            String str4 = TAG;
            Log.e(str4, "collapseStatusBar Exception :" + e.getClass());
        }
    }

    public boolean hasHwAssist() {
        if (IS_CHINA) {
            if (this.mHasHwAssist == null) {
                try {
                    this.mContext.getPackageManager().getPackageInfo(HW_ASSISTANT_PACKAGE_NAME, 128);
                    this.mHasHwAssist = true;
                } catch (PackageManager.NameNotFoundException e) {
                    this.mHasHwAssist = false;
                } catch (Exception exp) {
                    this.mHasHwAssist = false;
                    String str = TAG;
                    Log.e(str, "hasHwAssist Exception :" + exp.getClass());
                }
            }
            return this.mHasHwAssist.booleanValue();
        } else if (getAssistInfo() != null) {
            return true;
        } else {
            Log.w(TAG, "startAssist::assistComponent is null, return");
            return false;
        }
    }
}
