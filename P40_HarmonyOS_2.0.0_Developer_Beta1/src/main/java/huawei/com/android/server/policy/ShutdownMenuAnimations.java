package huawei.com.android.server.policy;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.hwanimation.CubicBezierInterpolator;
import huawei.com.android.server.policy.HwGlobalActionsView;
import java.util.ArrayList;
import java.util.List;

public class ShutdownMenuAnimations {
    private static final int ANIMATION_DURATION_1 = 100;
    private static final int ANIMATION_DURATION_2 = 130;
    private static final int ANIMATION_DURATION_3 = 220;
    private static final int ANIMATION_DURATION_4 = 250;
    private static final int ANIMATION_DURATION_5 = 350;
    private static final int ANIMATION_DURATION_6 = 800;
    static final int CONFIRM_ANIMATION_DURATION = 200;
    private static final float CONFIRM_SCALE = 0.8f;
    private static final int COORDINATE_ARRAY_SIZE = 2;
    static final Interpolator CUBIC_BEZIER_20_20 = new CubicBezierInterpolator(0.2f, 0.5f, 0.8f, 0.5f);
    private static final Interpolator CUBIC_BEZIER_30_90 = new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
    static final Interpolator CUBIC_BEZIER_40_0 = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
    private static final Interpolator CUBIC_BEZIER_40_80 = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final Interpolator CUBIC_BEZIER_51_85 = new CubicBezierInterpolator(0.51f, 0.35f, 0.15f, 1.0f);
    private static final Interpolator CUBIC_BEZIER_53_31 = new CubicBezierInterpolator(0.53f, 0.51f, 0.69f, 0.99f);
    private static final int DEGREES_ROTATE = 360;
    private static final int DICHOTOMY_SIZE = 2;
    private static final int DURATION_ROTATE = 1280;
    private static final float END_SCALE = 0.95f;
    private static final int ID_AIRPLANEMODE_RESOURCE = 34603145;
    private static final int ID_REBOOT_RESOURCE = 34603151;
    private static final int ID_REBOOT_SCREENBLACK = 34603473;
    private static final int ID_SHUTDOWN_RESOURCE = 34603152;
    private static final int ID_SILENTMODE_RESOURCE = 34603146;
    private static final int ID_STR_LOCKDOWN = 33686054;
    private static final int ID_STR_REBOOT = 33685740;
    private static final int ID_STR_SHUTDOWN = 33685738;
    private static final boolean IS_DEBUG = Log.HWLog;
    private static final boolean IS_INFO_DEBUG = Log.HWINFO;
    private static final boolean IS_SUPER_LITE_MODE = "SuperLite".equals(SystemProperties.get("ro.build.hw_emui_feature_level", ""));
    private static final float MENU_SCALE = 1.6f;
    private static final int MESSAGE_REPEAT_ROTATE = 0;
    private static final float NEW_CONFIRM_SCALE = 1.2f;
    private static final int ROTATION = SystemProperties.getInt("ro.panel.hw_orientation", 0);
    private static final int ROTATION_DEFAULT = 0;
    private static final int ROTATION_NINETY = 90;
    private static final float SHUTDOWN_MENU_ACTION_SHORT_TRANSLATE_DP = 12.5f;
    private static final float START_SCALE = 1.05f;
    private static final String TAG = "ShutdownMenuAnimations";
    private static Context sContext;
    private static ShutdownMenuAnimations sInstance = new ShutdownMenuAnimations();
    private ArrayList<View> mActionViewList = new ArrayList<>();
    private AnimatorSet mAnimFocusEnterSet;
    private AnimatorSet mAnimFocusExitSet;
    private View mCircleView;
    private TextView mConfirmMessage;
    private AnimatorSet mConfirmSet;
    private View mConfirmView;
    private View mFocusView;
    private int[] mImageLocationEnd = new int[2];
    private int[] mImageLocationStart = new int[2];
    private boolean mIsAnimRunning = false;
    private View mKeyCombinationHintView;
    private View mLockdownView;
    private View mRebootProgress;
    private View mRebootView;
    private AnimatorSet mShutdownEnterSet;
    private View mShutdownScreenoff;
    private View mShutdownView;
    private View mTipLayout;
    private View mViewConfirmAction;
    private View mViewFourAction;
    private ArrayList<View> mViewList = new ArrayList<>();
    private View mViewTwoAction;
    private View mWarningListView;

    public static boolean isSuperLiteMode() {
        logDebug("is super lite mode " + IS_SUPER_LITE_MODE);
        return IS_SUPER_LITE_MODE;
    }

    public static synchronized ShutdownMenuAnimations getInstance(Context context) {
        ShutdownMenuAnimations shutdownMenuAnimations;
        synchronized (ShutdownMenuAnimations.class) {
            sContext = context.getApplicationContext();
            if (sInstance == null) {
                sInstance = new ShutdownMenuAnimations();
            }
            shutdownMenuAnimations = sInstance;
        }
        return shutdownMenuAnimations;
    }

    private static void logDebug(String log) {
        if (IS_DEBUG) {
            Log.d(TAG, log);
        }
    }

    public AnimatorSet setImageAnimation(boolean isEnter) {
        ArrayList<View> arrayList;
        ObjectAnimator translateX;
        ObjectAnimator translateY;
        ObjectAnimator alpha;
        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
        AnimatorSet set = new AnimatorSet();
        if (this.mIsAnimRunning || (arrayList = this.mViewList) == null || arrayList.size() == 0) {
            Log.w(TAG, "viewList is null or isAnimRunning");
            return set;
        }
        ArrayList<Animator> anims = new ArrayList<>();
        int fromX = 0;
        int fromY = 0;
        int size = this.mViewList.size();
        for (int i = 0; i < size; i++) {
            View view = this.mViewList.get(i);
            if (view != null) {
                switch (view.getId()) {
                    case ID_AIRPLANEMODE_RESOURCE /* 34603145 */:
                        fromX = (int) sContext.getResources().getDimension(34472072);
                        fromY = (int) sContext.getResources().getDimension(34472073);
                        logDebug("setImageAnimation viewid=ID_AIRPLANEMODE_RESOURCE fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_SILENTMODE_RESOURCE /* 34603146 */:
                        fromX = (int) sContext.getResources().getDimension(34472074);
                        fromY = (int) sContext.getResources().getDimension(34472075);
                        logDebug("setImageAnimation viewid=ID_SILENTMODE_RESOURCE fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_REBOOT_RESOURCE /* 34603151 */:
                        this.mRebootView = view;
                        fromX = (int) sContext.getResources().getDimension(34472076);
                        fromY = (int) sContext.getResources().getDimension(34472077);
                        logDebug("setImageAnimation viewid=ID_REBOOT_RESOURCE fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_SHUTDOWN_RESOURCE /* 34603152 */:
                        this.mShutdownView = view;
                        fromX = (int) sContext.getResources().getDimension(34472078);
                        fromY = (int) sContext.getResources().getDimension(34472079);
                        logDebug("setImageAnimation viewid=ID_SHUTDOWN_RESOURCE fromX=" + fromX + ", fromY= " + fromY);
                        break;
                }
                if (isEnter) {
                    translateX = ObjectAnimator.ofFloat(view, "translationX", (float) fromX, 0.0f);
                    translateY = ObjectAnimator.ofFloat(view, "translationY", (float) fromY, 0.0f);
                    alpha = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
                    scaleX = ObjectAnimator.ofFloat(view, "scaleX", MENU_SCALE, 1.0f);
                    scaleY = ObjectAnimator.ofFloat(view, "scaleY", MENU_SCALE, 1.0f);
                } else {
                    translateX = ObjectAnimator.ofFloat(view, "translationX", 0.0f, (float) fromX);
                    translateY = ObjectAnimator.ofFloat(view, "translationY", 0.0f, (float) fromY);
                    alpha = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
                    scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, MENU_SCALE);
                    scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, MENU_SCALE);
                }
                anims.add(translateX);
                anims.add(translateY);
                anims.add(alpha);
                anims.add(scaleX);
                anims.add(scaleY);
            }
        }
        set.setInterpolator(CUBIC_BEZIER_51_85);
        set.setDuration(800L);
        set.playTogether(anims);
        return set;
    }

    public void shutdownOrRebootEnterAnim(View view, boolean isReboot) {
        if (view == null) {
            Log.w(TAG, "shutdownOrRebootEnterAnim view is null");
            return;
        }
        view.getLocationInWindow(this.mImageLocationEnd);
        int[] iArr = this.mImageLocationEnd;
        iArr[0] = iArr[0] + (view.getWidth() / 2);
        int[] iArr2 = this.mImageLocationEnd;
        iArr2[1] = iArr2[1] + (view.getHeight() / 2);
        startConfirmEnterOrExitAnim(view, true, isReboot);
    }

    public void rebackShutdownMenu(boolean isReboot) {
        if (isReboot) {
            this.mConfirmMessage.setText(ID_STR_REBOOT);
            startConfirmEnterOrExitAnim(this.mRebootView, false, isReboot);
        } else {
            this.mConfirmMessage.setText(ID_STR_SHUTDOWN);
            startConfirmEnterOrExitAnim(this.mShutdownView, false, isReboot);
        }
        View view = this.mCircleView;
        if (view != null) {
            view.setVisibility(4);
            this.mCircleView.clearAnimation();
        }
    }

    private void startConfirmEnterOrExitAnim(View view, boolean isEnter, boolean isReboot) {
        if (this.mIsAnimRunning || view == null) {
            Log.w(TAG, "startConfirmEnterOrExitAnim isAnimRunning return! view = " + view);
            return;
        }
        if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
            View view2 = this.mViewFourAction;
            if (view2 != null) {
                view2.setVisibility(0);
            }
            this.mViewTwoAction = null;
        } else {
            View view3 = this.mViewTwoAction;
            if (view3 != null) {
                view3.setVisibility(0);
            }
            this.mViewFourAction = null;
        }
        int[] iArr = this.mImageLocationStart;
        if (iArr[0] == 0 && iArr[1] == 0 && ROTATION == 0) {
            this.mConfirmView.getLocationInWindow(iArr);
            int[] iArr2 = this.mImageLocationStart;
            iArr2[0] = iArr2[0] + (this.mConfirmView.getWidth() / 2);
            int[] iArr3 = this.mImageLocationStart;
            iArr3[1] = iArr3[1] + (this.mConfirmView.getHeight() / 2);
        }
        if (ROTATION == ROTATION_NINETY) {
            WindowManager wm = (WindowManager) sContext.getSystemService("window");
            if (Settings.System.getInt(sContext.getContentResolver(), "navigationbar_is_min", 0) != 0) {
                this.mImageLocationStart[0] = wm.getDefaultDisplay().getWidth() / 2;
                this.mImageLocationStart[1] = wm.getDefaultDisplay().getHeight() / 2;
            } else {
                int navigationBarHeight = sContext.getResources().getDimensionPixelSize(17105309);
                this.mImageLocationStart[0] = wm.getDefaultDisplay().getWidth() / 2;
                this.mImageLocationStart[1] = (wm.getDefaultDisplay().getHeight() + navigationBarHeight) >>> 1;
            }
        }
        logDebug("mImageLocationStart[] = [" + this.mImageLocationStart[0] + ", " + this.mImageLocationStart[1] + "],mImageLocationEnd[] = [" + this.mImageLocationEnd[0] + ", " + this.mImageLocationEnd[1] + "]");
        View view4 = this.mConfirmView;
        int[] iArr4 = this.mImageLocationEnd;
        int i = iArr4[0];
        int[] iArr5 = this.mImageLocationStart;
        this.mShutdownEnterSet = startConfirmTranslationAnimation(view4, i - iArr5[0], iArr4[1] - iArr5[1], isEnter);
        AnimatorSet animatorSet = this.mConfirmSet;
        if (animatorSet == null || !animatorSet.isRunning()) {
            createAndStartConfirmAnim(view, isEnter, isReboot);
        }
    }

    private void createAndStartConfirmAnim(final View view, final boolean isEnter, final boolean isReboot) {
        this.mConfirmSet = setImageAnimation(!isEnter);
        this.mConfirmSet.addListener(new Animator.AnimatorListener() {
            /* class huawei.com.android.server.policy.ShutdownMenuAnimations.AnonymousClass1 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                ShutdownMenuAnimations.this.onConfirmAnimationStart(view, isEnter, isReboot);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                View view = view;
                if (view != null) {
                    view.setVisibility(0);
                } else {
                    Log.w(ShutdownMenuAnimations.TAG, "mView is null!");
                }
                if (isEnter) {
                    if (ShutdownMenuAnimations.this.mViewFourAction != null) {
                        ShutdownMenuAnimations.this.mViewFourAction.setVisibility(4);
                    }
                    if (ShutdownMenuAnimations.this.mViewTwoAction != null && !HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                        ShutdownMenuAnimations.this.mViewTwoAction.setVisibility(4);
                    }
                } else if (ShutdownMenuAnimations.this.mViewConfirmAction != null) {
                    ShutdownMenuAnimations.this.mViewConfirmAction.setVisibility(4);
                }
                ShutdownMenuAnimations.this.mIsAnimRunning = false;
                ShutdownMenuAnimations.this.mViewConfirmAction.findViewById(34603108).performAccessibilityAction(64, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
                ShutdownMenuAnimations.this.mIsAnimRunning = false;
            }
        });
        this.mConfirmSet.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConfirmAnimationStart(View actionView, boolean isEnter, boolean isReboot) {
        this.mIsAnimRunning = true;
        setViewVisibility(actionView, 4);
        View view = this.mViewConfirmAction;
        if (view != null) {
            view.setVisibility(0);
        } else {
            Log.w(TAG, "mViewConfirmAction is null!");
        }
        if (isEnter) {
            setViewVisibility(this.mTipLayout, 4);
            if (!isReboot && this.mWarningListView != null && ActivityManager.getCurrentUser() == 0) {
                this.mWarningListView.setVisibility(0);
            }
        } else {
            if (!isReboot && this.mWarningListView != null && ActivityManager.getCurrentUser() == 0) {
                this.mWarningListView.setVisibility(4);
            }
            setViewVisibility(this.mTipLayout, 0);
        }
        AnimatorSet animatorSet = this.mShutdownEnterSet;
        if (animatorSet != null && !animatorSet.isRunning()) {
            Log.w(TAG, "mShutdownEnterSet start!");
            this.mShutdownEnterSet.start();
        }
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private AnimatorSet startConfirmTranslationAnimation(View view, int fromX, int fromY, boolean isEnter) {
        ObjectAnimator translateX;
        ObjectAnimator translateY;
        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
        if (isEnter) {
            translateX = ObjectAnimator.ofFloat(view, "translationX", (float) fromX, 0.0f);
            translateY = ObjectAnimator.ofFloat(view, "translationY", (float) fromY, 0.0f);
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.0f);
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.0f);
        } else {
            translateX = ObjectAnimator.ofFloat(view, "translationX", 0.0f, (float) fromX);
            translateY = ObjectAnimator.ofFloat(view, "translationY", 0.0f, (float) fromY);
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.8f);
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.8f);
        }
        ArrayList<Animator> anims = new ArrayList<>();
        anims.add(translateX);
        anims.add(translateY);
        anims.add(scaleX);
        anims.add(scaleY);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anims);
        set.setInterpolator(CUBIC_BEZIER_51_85);
        set.setDuration(800L);
        return set;
    }

    public Animation setSoundModeRotate() {
        Animation animRotation = AnimationUtils.loadAnimation(sContext, 34209799);
        animRotation.setInterpolator(CUBIC_BEZIER_53_31);
        return animRotation;
    }

    public void setFourActionView(View view) {
        this.mViewFourAction = view;
        this.mTipLayout = this.mViewFourAction.findViewById(34603149);
    }

    public void setTwoActionView(View view) {
        this.mViewTwoAction = view;
        this.mAnimFocusEnterSet = null;
        this.mAnimFocusExitSet = null;
        this.mTipLayout = this.mViewTwoAction.findViewById(34603150);
        this.mRebootView = this.mViewTwoAction.findViewById(ID_REBOOT_RESOURCE);
        this.mShutdownView = this.mViewTwoAction.findViewById(ID_SHUTDOWN_RESOURCE);
        this.mLockdownView = this.mViewTwoAction.findViewById(34603205);
        this.mActionViewList.clear();
        this.mActionViewList.add(this.mRebootView);
        this.mActionViewList.add(this.mShutdownView);
        this.mActionViewList.add(this.mLockdownView);
    }

    public void setConfirmActionView(View view) {
        if (view == null) {
            Log.w(TAG, "setConfirmActionView view is null");
            return;
        }
        this.mViewConfirmAction = view;
        this.mConfirmView = this.mViewConfirmAction.findViewById(34603153);
        this.mConfirmMessage = (TextView) this.mViewConfirmAction.findViewById(34603129);
        int[] iArr = this.mImageLocationStart;
        iArr[0] = 0;
        iArr[1] = 0;
        int[] iArr2 = this.mImageLocationEnd;
        iArr2[0] = 0;
        iArr2[1] = 0;
        this.mWarningListView = this.mViewConfirmAction.findViewById(34603154);
    }

    public void setKeyCombinationHintView(View view) {
        if (view == null) {
            Log.w(TAG, "setKeyCombinationHintView view is null");
        } else {
            this.mKeyCombinationHintView = view;
        }
    }

    public void setMenuViewList(ArrayList<View> views) {
        this.mViewList = views;
    }

    public void setIsAnimRunning(boolean isRunning) {
        this.mIsAnimRunning = isRunning;
    }

    public boolean getIsAnimRunning() {
        return this.mIsAnimRunning;
    }

    public AnimatorSet setNewShutdownViewAnimation(boolean isEnter) {
        ObjectAnimator alpha;
        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
        if (isEnter) {
            alpha = ObjectAnimator.ofFloat(this.mViewTwoAction, "alpha", 0.0f, 1.0f);
            scaleX = ObjectAnimator.ofFloat(this.mViewTwoAction, "scaleX", NEW_CONFIRM_SCALE, 1.0f);
            scaleY = ObjectAnimator.ofFloat(this.mViewTwoAction, "scaleY", NEW_CONFIRM_SCALE, 1.0f);
        } else {
            alpha = ObjectAnimator.ofFloat(this.mViewTwoAction, "alpha", 1.0f, 0.0f);
            scaleX = ObjectAnimator.ofFloat(this.mViewTwoAction, "scaleX", 1.0f, NEW_CONFIRM_SCALE);
            scaleY = ObjectAnimator.ofFloat(this.mViewTwoAction, "scaleY", 1.0f, NEW_CONFIRM_SCALE);
        }
        ArrayList<Animator> anims = new ArrayList<>();
        anims.add(alpha);
        anims.add(scaleX);
        anims.add(scaleY);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anims);
        set.setInterpolator(CUBIC_BEZIER_30_90);
        set.setDuration(350L);
        return set;
    }

    public AnimatorSet setShutdownMenuDismissAnim() {
        Animator confirmAlpha = ObjectAnimator.ofFloat(this.mConfirmView, "alpha", 1.0f, 0.0f);
        Animator confirmScaleX = ObjectAnimator.ofFloat(this.mConfirmView, "scaleX", NEW_CONFIRM_SCALE, 1.0f);
        Animator confirmScaleY = ObjectAnimator.ofFloat(this.mConfirmView, "scaleY", NEW_CONFIRM_SCALE, 1.0f);
        ArrayList<Animator> anims = new ArrayList<>();
        anims.add(confirmAlpha);
        anims.add(confirmScaleX);
        anims.add(confirmScaleY);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(anims);
        animSet.setInterpolator(CUBIC_BEZIER_40_0);
        animSet.setDuration(200L);
        return animSet;
    }

    public void newShutdownOrRebootEnterAnim(View view) {
        if (view == null) {
            Log.w(TAG, "newShutdownOrRebootEnterAnim view is null");
            return;
        }
        view.getLocationInWindow(this.mImageLocationEnd);
        int[] iArr = this.mImageLocationEnd;
        iArr[0] = iArr[0] + (view.getWidth() / 2);
        View view2 = this.mWarningListView;
        if (view2 != null) {
            view2.setAlpha(0.0f);
        }
        View view3 = this.mViewConfirmAction;
        if (view3 != null) {
            view3.setVisibility(0);
        }
        startNewConfirmEnterOrExitAnim(view, true);
    }

    public void rebackNewShutdownMenu(int flag) {
        if (!this.mIsAnimRunning) {
            View view = this.mViewTwoAction;
            if (view != null) {
                view.setVisibility(0);
            }
            if ((flag & 512) != 0) {
                startNewConfirmEnterOrExitAnim(this.mRebootView, false);
            } else if ((flag & 8192) != 0) {
                startNewConfirmEnterOrExitAnim(this.mShutdownView, false);
            } else if ((131072 & flag) != 0) {
                startNewConfirmEnterOrExitAnim(this.mLockdownView, false);
            } else {
                Log.w(TAG, "reback newShutDownMenu, unknow flag = " + flag);
            }
        }
    }

    private void startNewConfirmEnterOrExitAnim(View view, boolean isEnter) {
        if (this.mIsAnimRunning || view == null) {
            Log.w(TAG, "startNewConfirmEnterOrExitAnim isAnimRunning return! view = " + view);
        } else if (isSuperLiteMode()) {
            Log.w(TAG, "super lite mode, no animations.");
            endNewConfirmEnterOrExit(view, isEnter);
        } else {
            int[] iArr = this.mImageLocationStart;
            if (iArr[0] == 0) {
                this.mConfirmView.getLocationInWindow(iArr);
                int[] iArr2 = this.mImageLocationStart;
                iArr2[0] = iArr2[0] + (this.mConfirmView.getWidth() / 2);
            }
            this.mShutdownEnterSet = startNewConfirmTranslationAnimation(this.mConfirmView, view, this.mImageLocationEnd[0] - this.mImageLocationStart[0], isEnter);
            AnimatorSet animatorSet = this.mConfirmSet;
            if (animatorSet == null || !animatorSet.isRunning()) {
                View iconFrame = view.findViewById(34603108);
                String iconTag = "";
                if (iconFrame != null) {
                    Object obj = iconFrame.getTag();
                    if (obj instanceof String) {
                        iconTag = (String) obj;
                    }
                }
                boolean isReboot = iconTag.equals("reboot");
                this.mConfirmSet = startViewStubAnimation(this.mImageLocationStart[0] - this.mImageLocationEnd[0], isEnter, iconTag);
                configNewConfirmTranslationAnimation(view, isEnter, isReboot, iconTag);
                this.mShutdownEnterSet.start();
            }
        }
    }

    private void configNewConfirmTranslationAnimation(final View viewContainer, final boolean isEnter, final boolean isReboot, final String iconTag) {
        this.mShutdownEnterSet.addListener(new AnimatorListenerAdapter() {
            /* class huawei.com.android.server.policy.ShutdownMenuAnimations.AnonymousClass2 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                ShutdownMenuAnimations.this.mIsAnimRunning = true;
                View view = viewContainer;
                if (view != null) {
                    view.setVisibility(4);
                }
                if (ShutdownMenuAnimations.this.mConfirmSet != null && !ShutdownMenuAnimations.this.mConfirmSet.isRunning()) {
                    ShutdownMenuAnimations.this.mConfirmSet.start();
                }
                if (isReboot) {
                    return;
                }
                if (isEnter) {
                    ObjectAnimator tipLayoutAlpha = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.mTipLayout, "alpha", 1.0f, 0.0f);
                    ObjectAnimator warningListViewAlpha = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.mWarningListView, "alpha", 0.0f, 1.0f);
                    tipLayoutAlpha.setInterpolator(ShutdownMenuAnimations.CUBIC_BEZIER_20_20);
                    tipLayoutAlpha.setDuration(130L);
                    warningListViewAlpha.setInterpolator(ShutdownMenuAnimations.CUBIC_BEZIER_20_20);
                    warningListViewAlpha.setStartDelay(130);
                    warningListViewAlpha.setDuration(220L);
                    tipLayoutAlpha.start();
                    warningListViewAlpha.start();
                    return;
                }
                ObjectAnimator tipLayoutAlpha2 = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.mTipLayout, "alpha", 0.0f, 1.0f);
                ObjectAnimator warningListViewAlpha2 = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.mWarningListView, "alpha", 1.0f, 0.0f);
                tipLayoutAlpha2.setInterpolator(ShutdownMenuAnimations.CUBIC_BEZIER_20_20);
                tipLayoutAlpha2.setStartDelay(130);
                tipLayoutAlpha2.setDuration(220L);
                warningListViewAlpha2.setInterpolator(ShutdownMenuAnimations.CUBIC_BEZIER_20_20);
                warningListViewAlpha2.setDuration(130L);
                tipLayoutAlpha2.start();
                warningListViewAlpha2.start();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                ShutdownMenuAnimations.this.endNewConfirmEnterOrExitAnim(viewContainer, isEnter, iconTag);
                ShutdownMenuAnimations.this.showKeyCombinationWhenReback();
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
                ShutdownMenuAnimations.this.mIsAnimRunning = false;
                ShutdownMenuAnimations.this.showKeyCombinationWhenReback();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void endNewConfirmEnterOrExitAnim(View view, boolean isEnter, String iconTag) {
        if (isEnter) {
            View view2 = this.mViewTwoAction;
            if (view2 != null) {
                view2.setVisibility(8);
            }
        } else {
            if (view != null) {
                view.setVisibility(0);
            }
            View view3 = this.mViewConfirmAction;
            if (view3 != null) {
                view3.setVisibility(8);
            }
        }
        this.mIsAnimRunning = false;
        requestAccessibilityFocused(isEnter, iconTag);
    }

    private void requestAccessibilityFocused(boolean isEnter, String iconTag) {
        View container;
        View iconFrameLayout;
        if (iconTag != null && !HwGlobalActionsData.getSingletoneInstance().isKeyCombinationEnterAnimationNeeds()) {
            if (isEnter) {
                container = this.mViewConfirmAction;
            } else if (iconTag.equals("reboot")) {
                container = this.mRebootView;
            } else if (iconTag.equals("shutdown")) {
                container = this.mShutdownView;
            } else if (iconTag.equals("lockdown")) {
                container = this.mLockdownView;
            } else {
                container = null;
            }
            if (container != null && (iconFrameLayout = container.findViewById(34603108)) != null) {
                iconFrameLayout.performAccessibilityAction(64, null);
            }
        }
    }

    private void endNewConfirmEnterOrExit(View view, boolean isEnter) {
        View iconFrame = view.findViewById(34603108);
        String iconTag = "";
        if (iconFrame != null) {
            Object obj = iconFrame.getTag();
            if (obj instanceof String) {
                iconTag = (String) obj;
            }
        }
        endNewConfirmEnterOrExitAnim(view, isEnter, iconTag);
    }

    private AnimatorSet startNewConfirmTranslationAnimation(View view, final View menuView, int fromX, final boolean isEnter) {
        ObjectAnimator translateX = ObjectAnimator.ofFloat(view, "translationX", (float) fromX, 0.0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, NEW_CONFIRM_SCALE);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, NEW_CONFIRM_SCALE);
        final String menuText = ((TextView) menuView.findViewById(34603129)).getText().toString();
        final TextView confirmMessage = (TextView) view.findViewById(34603129);
        final String confirmText = confirmMessage.getText().toString();
        ObjectAnimator alphaFadeOut = ObjectAnimator.ofFloat(confirmMessage, "alpha", 1.0f, 0.0f);
        if (isEnter) {
            confirmMessage.setText(menuText);
            ViewGroup.LayoutParams textParams = confirmMessage.getLayoutParams();
            textParams.width = menuView.getLayoutParams().width;
            confirmMessage.setLayoutParams(textParams);
            alphaFadeOut.setDuration(200L);
        } else {
            translateX = ObjectAnimator.ofFloat(view, "translationX", 0.0f, (float) fromX);
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", NEW_CONFIRM_SCALE, 1.0f);
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", NEW_CONFIRM_SCALE, 1.0f);
            alphaFadeOut.setDuration(100L);
        }
        alphaFadeOut.addListener(new AnimatorListenerAdapter() {
            /* class huawei.com.android.server.policy.ShutdownMenuAnimations.AnonymousClass3 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                ShutdownMenuAnimations.this.newAlphaFadeIn(confirmMessage, menuView, confirmText, menuText, isEnter).start();
            }
        });
        translateX.setDuration(350L);
        translateX.setInterpolator(AnimationUtils.loadInterpolator(sContext, 17563661));
        scaleX.setDuration(350L);
        scaleX.setInterpolator(AnimationUtils.loadInterpolator(sContext, 17563661));
        scaleY.setDuration(350L);
        scaleY.setInterpolator(AnimationUtils.loadInterpolator(sContext, 17563661));
        alphaFadeOut.setInterpolator(CUBIC_BEZIER_20_20);
        List<Animator> anims = new ArrayList<>();
        anims.add(translateX);
        anims.add(scaleX);
        anims.add(scaleY);
        anims.add(alphaFadeOut);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anims);
        return set;
    }

    private List<View> findOtherViews(String tag) {
        List<View> res = new ArrayList<>();
        ArrayList<View> arrayList = this.mActionViewList;
        if (arrayList == null) {
            Log.w(TAG, "action views list is null!");
            return res;
        }
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            View view = this.mActionViewList.get(i);
            if (view != null) {
                String viewTag = "";
                Object obj = view.getTag();
                if (obj instanceof String) {
                    viewTag = (String) obj;
                }
                if (!tag.equals(viewTag)) {
                    res.add(view);
                }
            }
        }
        return res;
    }

    private int getPixelsByDp(float dp) {
        Context context = sContext;
        if (context == null) {
            Log.w(TAG, "sContext is null, return!");
            return 0;
        }
        Resources resources = context.getResources();
        if (resources == null) {
            Log.w(TAG, "resources is null, return!");
            return 0;
        }
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics != null) {
            return (int) (displayMetrics.density * dp);
        }
        Log.w(TAG, "displayMetrics is null, return!");
        return 0;
    }

    private AnimatorSet startViewStubAnimation(int toX, boolean isEnter, String tag) {
        AnimatorSet set = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList<>();
        if (this.mIsAnimRunning) {
            Log.w(TAG, "Animation is Running");
            return set;
        } else if (tag == null) {
            Log.w(TAG, "Tag is null");
            return set;
        } else {
            List<View> otherViews = findOtherViews(tag);
            int translateX = calculateTranslateXOfOtherView(toX);
            float endAlpha = 1.0f;
            float startAlpha = isEnter ? 1.0f : 0.0f;
            if (isEnter) {
                endAlpha = 0.0f;
            }
            for (View view : otherViews) {
                if (view != null) {
                    createAnimatorListOfOtherView(anims, view, translateX, isEnter);
                }
            }
            if (tag.equals("reboot")) {
                ObjectAnimator alphaHintAnim = ObjectAnimator.ofFloat(this.mTipLayout, "alpha", startAlpha, endAlpha);
                alphaHintAnim.setDuration(350L);
                alphaHintAnim.setInterpolator(CUBIC_BEZIER_30_90);
                anims.add(alphaHintAnim);
            }
            set.playTogether(anims);
            return set;
        }
    }

    public void startNewShutdownOrRebootAnim(boolean isReboot, final boolean isScreenOff) {
        if (this.mIsAnimRunning) {
            logDebug("startNewShutdownOrRebootAnim isAnimRunning return!");
        } else if (isSuperLiteMode()) {
            Log.w(TAG, "super lite mode, no animations.");
            endNewShutdownOrRebootAnim(isScreenOff);
        } else if (this.mViewConfirmAction != null) {
            Animator alpha = ObjectAnimator.ofFloat(this.mConfirmView, "alpha", 1.0f, 0.0f);
            Animator scaleX = ObjectAnimator.ofFloat(this.mConfirmView, "scaleX", NEW_CONFIRM_SCALE, 1.0f);
            Animator scaleY = ObjectAnimator.ofFloat(this.mConfirmView, "scaleY", NEW_CONFIRM_SCALE, 1.0f);
            ArrayList<Animator> anims = new ArrayList<>();
            anims.add(alpha);
            anims.add(scaleX);
            anims.add(scaleY);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(anims);
            animatorSet.setInterpolator(CUBIC_BEZIER_30_90);
            animatorSet.setDuration(200L);
            animatorSet.addListener(new Animator.AnimatorListener() {
                /* class huawei.com.android.server.policy.ShutdownMenuAnimations.AnonymousClass4 */

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    ShutdownMenuAnimations.this.mIsAnimRunning = true;
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animation) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    ShutdownMenuAnimations.this.endNewShutdownOrRebootAnim(isScreenOff);
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                    ShutdownMenuAnimations.this.mIsAnimRunning = false;
                }
            });
            animatorSet.start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void endNewShutdownOrRebootAnim(boolean isScreenOff) {
        this.mConfirmView.setVisibility(8);
        if (isScreenOff) {
            View view = this.mWarningListView;
            if (view != null) {
                view.setVisibility(8);
            }
            newEndScreenOff();
            return;
        }
        Object confirmAction = this.mViewConfirmAction.findViewById(34603155);
        if (confirmAction instanceof View) {
            this.mRebootProgress = (View) confirmAction;
            Log.w(TAG, "endNewShutdownOrRebootAnim, isHardwareAccelerated = " + this.mRebootProgress.isHardwareAccelerated());
            this.mRebootProgress.setVisibility(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ObjectAnimator newAlphaFadeIn(TextView confirmMessageView, View menuView, String confirmText, String menutext, boolean isEnter) {
        ObjectAnimator alphaFadeIn = ObjectAnimator.ofFloat(confirmMessageView, "alpha", 0.0f, 1.0f);
        alphaFadeIn.setInterpolator(CUBIC_BEZIER_20_20);
        if (isEnter) {
            confirmMessageView.setText(confirmText);
            ViewGroup.LayoutParams layoutParams = confirmMessageView.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams textParams = (ViewGroup.MarginLayoutParams) layoutParams;
                View view = this.mConfirmView;
                if (view != null) {
                    textParams.width = (int) Math.round(((double) ((view.getMeasuredWidth() - textParams.leftMargin) - textParams.rightMargin)) / 1.2000000476837158d);
                    confirmMessageView.setLayoutParams(textParams);
                }
            }
            alphaFadeIn.setDuration(150L);
        } else {
            confirmMessageView.setText(menutext);
            ViewGroup.LayoutParams textParams2 = confirmMessageView.getLayoutParams();
            textParams2.width = menuView.getLayoutParams().width;
            confirmMessageView.setLayoutParams(textParams2);
            alphaFadeIn.setDuration(250L);
        }
        return alphaFadeIn;
    }

    private int calculateTranslateXOfOtherView(int toX) {
        if (this.mLockdownView == null) {
            return toX;
        }
        if (toX == 0) {
            return 0;
        }
        if (toX > 0) {
            return getPixelsByDp(SHUTDOWN_MENU_ACTION_SHORT_TRANSLATE_DP);
        }
        return -getPixelsByDp(SHUTDOWN_MENU_ACTION_SHORT_TRANSLATE_DP);
    }

    private void createAnimatorListOfOtherView(List<Animator> anims, View view, int translateX, boolean isEnter) {
        float endAlpha = 0.0f;
        float startX = isEnter ? 0.0f : (float) translateX;
        float endX = isEnter ? (float) translateX : 0.0f;
        float startAlpha = isEnter ? 1.0f : 0.0f;
        if (!isEnter) {
            endAlpha = 1.0f;
        }
        ObjectAnimator translateXAnim = ObjectAnimator.ofFloat(view, "translationX", startX, endX);
        translateXAnim.setDuration(250L);
        translateXAnim.setInterpolator(AnimationUtils.loadInterpolator(sContext, 17563661));
        View message = view.findViewById(34603129);
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view.findViewById(34603108), "alpha", startAlpha, endAlpha);
        ObjectAnimator alphaMessage = ObjectAnimator.ofFloat(message, "alpha", startAlpha, endAlpha);
        alphaAnim.setDuration(250L);
        alphaAnim.setInterpolator(CUBIC_BEZIER_20_20);
        alphaMessage.setInterpolator(CUBIC_BEZIER_20_20);
        if (isEnter) {
            alphaMessage.setDuration(100L);
        } else {
            alphaMessage.setDuration(250L);
            translateXAnim.setStartDelay(100);
            alphaAnim.setStartDelay(100);
            alphaMessage.setStartDelay(100);
        }
        anims.add(translateXAnim);
        anims.add(alphaAnim);
        anims.add(alphaMessage);
    }

    private void newEndScreenOff() {
        Object screenOff = this.mViewConfirmAction.findViewById(ID_REBOOT_SCREENBLACK);
        if (screenOff instanceof View) {
            this.mShutdownScreenoff = (View) screenOff;
        }
        View view = this.mShutdownScreenoff;
        if (view != null) {
            view.setVisibility(0);
        }
        Object powerObject = sContext.getSystemService("power");
        PowerManager powerManager = null;
        if (powerObject instanceof PowerManager) {
            powerManager = (PowerManager) powerObject;
        }
        if (powerManager != null) {
            powerManager.goToSleep(SystemClock.uptimeMillis(), 2, 0);
        }
    }

    public void setFocusChanageAnim(Context context, View view, HwGlobalActionsView.InnerResCollection innerResCollection, boolean hasFocus) {
        if (getIsAnimRunning()) {
            return;
        }
        if (hasFocus) {
            onFoucusEnter(context, view, innerResCollection);
        } else {
            onFocusExit(context, view, innerResCollection);
        }
    }

    private void onFoucusEnter(final Context context, View view, HwGlobalActionsView.InnerResCollection innerResCollection) {
        int layoutId = innerResCollection.getResLayoutId();
        Object object = AnimatorInflater.loadAnimator(sContext, 34144256);
        if (this.mAnimFocusEnterSet != null) {
            Log.w(TAG, "onFoucusEnter: the anim conflict!");
            this.mAnimFocusEnterSet.cancel();
        }
        if (object instanceof AnimatorSet) {
            this.mAnimFocusEnterSet = (AnimatorSet) object;
            final View layoutView = view.findViewById(layoutId);
            if (layoutView == null) {
                Log.e(TAG, "onFoucusEnter: layoutView is null!");
                return;
            }
            final int resBackgroundId = innerResCollection.getResBackgroundId();
            final int resDrawableId = innerResCollection.getResDrawableId();
            this.mAnimFocusEnterSet.setTarget(layoutView);
            this.mAnimFocusEnterSet.addListener(new AnimatorListenerAdapter() {
                /* class huawei.com.android.server.policy.ShutdownMenuAnimations.AnonymousClass5 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    ShutdownMenuAnimations.this.onFocusEnterAnimStart(context, layoutView, resBackgroundId, resDrawableId);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ShutdownMenuAnimations.this.mAnimFocusEnterSet = null;
                }
            });
            this.mAnimFocusEnterSet.start();
            return;
        }
        Log.e(TAG, "onFoucusEnter: the loaded animator is not a AnimatorSet instance!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFocusEnterAnimStart(Context context, View layoutView, int resBackgroundId, int resDrawableId) {
        onFocusChangeStart(context, layoutView, resBackgroundId, resDrawableId);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(16842806, outValue, true);
        int textColor = context.getResources().getColor(outValue.resourceId);
        TextView messageView = (TextView) layoutView.findViewById(34603129);
        if (messageView != null) {
            messageView.setTextColor(textColor);
        } else {
            Log.e(TAG, "onFocusEnterAnimStart: messageView is null!");
        }
    }

    private void onFocusExit(final Context context, View view, HwGlobalActionsView.InnerResCollection innerResCollection) {
        int layoutId = innerResCollection.getResLayoutId();
        Object objectFirst = AnimatorInflater.loadAnimator(context, 34144257);
        Object objectSecond = AnimatorInflater.loadAnimator(context, 34144258);
        if (!(objectFirst instanceof AnimatorSet) || !(objectSecond instanceof AnimatorSet)) {
            Log.e(TAG, "onFocusExit: the loaded animators are not a AnimatorSet instance!");
            return;
        }
        AnimatorSet animationSet = (AnimatorSet) objectFirst;
        AnimatorSet alphaAnimator = (AnimatorSet) objectSecond;
        final View layoutView = view.findViewById(layoutId);
        if (layoutView == null) {
            Log.e(TAG, "onFocusExit: layoutView is null!");
            return;
        }
        final int resBackgroundId = innerResCollection.getResBackgroundId();
        final int resDrawableId = innerResCollection.getResDrawableId();
        animationSet.addListener(new AnimatorListenerAdapter() {
            /* class huawei.com.android.server.policy.ShutdownMenuAnimations.AnonymousClass6 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ShutdownMenuAnimations.this.onFocusExitAnimEnd(context, layoutView, resBackgroundId, resDrawableId);
            }
        });
        AnimatorSet animatorSet = this.mAnimFocusExitSet;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        this.mAnimFocusExitSet = new AnimatorSet();
        this.mAnimFocusExitSet.play(alphaAnimator).after(animationSet);
        this.mAnimFocusExitSet.setTarget(layoutView);
        this.mAnimFocusExitSet.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFocusExitAnimEnd(Context context, View layoutView, int resBackgroundId, int resDrawableId) {
        onFocusChangeStart(context, layoutView, resBackgroundId, resDrawableId);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(16842808, outValue, true);
        int textColor = context.getResources().getColor(outValue.resourceId);
        TextView messageView = (TextView) layoutView.findViewById(34603129);
        if (messageView != null) {
            messageView.setTextColor(textColor);
        } else {
            Log.e(TAG, "onFocusExitAnimEnd: messageView is null!");
        }
    }

    private void onFocusChangeStart(Context context, View layoutView, int resBackgroundId, int resDrawableId) {
        View frameView = layoutView.findViewById(34603108);
        if (frameView != null) {
            frameView.setBackgroundResource(resBackgroundId);
            ImageView iconImageView = (ImageView) layoutView.findViewById(34603172);
            if (iconImageView != null) {
                iconImageView.setImageResource(resDrawableId);
            } else {
                Log.e(TAG, "onFocusChangeStart: iconImageView is null!");
            }
        } else {
            Log.e(TAG, "onFocusChangeStart: frameView is null!");
        }
    }

    private AnimatorSet setConfirmEnterAnim(View layoutView, Context context) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(layoutView, "scaleX", START_SCALE, END_SCALE, START_SCALE);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(layoutView, "scaleY", START_SCALE, END_SCALE, START_SCALE);
        AnimatorSet animationFirstSet = new AnimatorSet();
        animationFirstSet.play(animatorX).with(animatorY);
        animationFirstSet.setInterpolator(CUBIC_BEZIER_30_90);
        animationFirstSet.setDuration(200L);
        Object object = AnimatorInflater.loadAnimator(context, 34144259);
        if (object instanceof AnimatorSet) {
            AnimatorSet alphaAnimatorConirm = (AnimatorSet) object;
            alphaAnimatorConirm.setTarget(this.mViewTwoAction);
            AnimatorSet animationSecSet = new AnimatorSet();
            animationSecSet.play(alphaAnimatorConirm).after(animationFirstSet);
            return animationSecSet;
        }
        Log.e(TAG, "setConfirmEnterAnim: the loaded animators are not a AnimatorSet instance!");
        return null;
    }

    public AnimatorSet getFocusConfirmEnterAnim(Context context, boolean isTelevisionMode, int layoutId) {
        if (isTelevisionMode) {
            this.mFocusView = this.mViewTwoAction.findViewById(layoutId);
            View view = this.mFocusView;
            if (view != null) {
                return setConfirmEnterAnim(view, context);
            }
            Log.e(TAG, "television mode, mFocusView is null！");
            return null;
        }
        Log.e(TAG, "Television mode, but can not get focus layout btn!");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showKeyCombinationWhenReback() {
        if (HwGlobalActionsData.getSingletoneInstance().isKeyCombinationEnterAnimationNeeds()) {
            if (IS_INFO_DEBUG) {
                Log.i(TAG, "showKeyCombinationWhenReback: needs show");
            }
            setKeyCombinationHintViewAnimation(true, HwGlobalActionsData.getSingletoneInstance().isNowConfirmView()).start();
        }
    }

    /* JADX INFO: Multiple debug info for r0v2 android.animation.AnimatorSet: [D('formerView' android.view.View), D('animatorSet' android.animation.AnimatorSet)] */
    public AnimatorSet setKeyCombinationHintViewAnimation(boolean isEnter, boolean isConfirmView) {
        View formerView;
        if (!isEnter) {
            return getKeyCombinationHintViewExitAnimatorSet();
        }
        if (isConfirmView) {
            formerView = this.mConfirmView;
        } else {
            formerView = this.mViewTwoAction;
        }
        return getKeyCombinationHintViewEnterAnimatorSet(formerView);
    }

    private AnimatorSet getKeyCombinationHintViewEnterAnimatorSet(View formerView) {
        this.mKeyCombinationHintView.setAlpha(0.0f);
        this.mKeyCombinationHintView.setVisibility(0);
        Animator alpha = ObjectAnimator.ofFloat(this.mKeyCombinationHintView, "alpha", 0.0f, 1.0f);
        Animator formerAlpha = ObjectAnimator.ofFloat(formerView, "alpha", 1.0f, 0.0f);
        List<Animator> anims = new ArrayList<>();
        anims.add(alpha);
        anims.add(formerAlpha);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(CUBIC_BEZIER_40_80);
        animatorSet.setDuration(350L);
        animatorSet.addListener(new Animator.AnimatorListener() {
            /* class huawei.com.android.server.policy.ShutdownMenuAnimations.AnonymousClass7 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                ShutdownMenuAnimations.getInstance(ShutdownMenuAnimations.sContext).setIsAnimRunning(true);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                ShutdownMenuAnimations.this.mViewTwoAction.setVisibility(8);
                ShutdownMenuAnimations.this.mConfirmView.setVisibility(8);
                HwGlobalActionsData.getSingletoneInstance().setKeyCombinationMode(2097152);
                ShutdownMenuAnimations.getInstance(ShutdownMenuAnimations.sContext).setIsAnimRunning(false);
                ShutdownMenuAnimations.this.sendAccessibilityTipEvent();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
                ShutdownMenuAnimations.getInstance(ShutdownMenuAnimations.sContext).setIsAnimRunning(false);
            }
        });
        animatorSet.playTogether(anims);
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAccessibilityTipEvent() {
        Context context = sContext;
        if (context != null && context.getResources() != null && this.mKeyCombinationHintView != null) {
            Resources tmpResources = sContext.getResources();
            this.mKeyCombinationHintView.announceForAccessibility(String.format(tmpResources.getString(33685680), Integer.valueOf(tmpResources.getInteger(34275331))));
        }
    }

    private AnimatorSet getKeyCombinationHintViewExitAnimatorSet() {
        Animator alpha = ObjectAnimator.ofFloat(this.mKeyCombinationHintView, "alpha", 1.0f, 0.0f);
        Animator scaleX = ObjectAnimator.ofFloat(this.mKeyCombinationHintView, "scaleX", 1.0f, NEW_CONFIRM_SCALE);
        Animator scaleY = ObjectAnimator.ofFloat(this.mKeyCombinationHintView, "scaleY", 1.0f, NEW_CONFIRM_SCALE);
        List<Animator> anims = new ArrayList<>();
        anims.add(alpha);
        anims.add(scaleX);
        anims.add(scaleY);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(CUBIC_BEZIER_30_90);
        animatorSet.setDuration(350L);
        animatorSet.playTogether(anims);
        return animatorSet;
    }
}
