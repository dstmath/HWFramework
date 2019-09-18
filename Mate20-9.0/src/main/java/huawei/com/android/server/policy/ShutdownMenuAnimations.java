package huawei.com.android.server.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.TextView;
import com.android.internal.view.animation.FallbackLUTInterpolator;
import com.android.server.gesture.GestureNavConst;
import com.huawei.hwanimation.CubicBezierInterpolator;
import java.util.ArrayList;
import java.util.List;

public class ShutdownMenuAnimations {
    private static final float CONFIRM_SCALE = 0.8f;
    public static final Interpolator CubicBezier_20_90 = new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
    public static final Interpolator CubicBezier_33_33 = new CubicBezierInterpolator(0.2f, 0.5f, 0.8f, 0.5f);
    static final Interpolator CubicBezier_40_0 = new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f, 1.0f);
    private static final int DEGREES_ROTATE = 360;
    private static final int DURATION_130MS = 130;
    static final int DURATION_200MS = 200;
    private static final int DURATION_220MS = 220;
    static final int DURATION_260MS = 260;
    private static final int DURATION_350MS = 350;
    private static final int DURATION_800MS = 800;
    private static final int DURATION_ROTATE = 1280;
    private static final int ID_AIRPLANEMODE_RESOURCE = 34603145;
    private static final int ID_REBOOT_RESOURCE = 34603151;
    private static final int ID_REBOOT_SCREENBLACK = 34603376;
    private static final int ID_SHUTDOWN_RESOURCE = 34603152;
    private static final int ID_SILENTMODE_RESOURCE = 34603146;
    private static final int ID_STR_LOCKDOWN = 33686054;
    private static final int ID_STR_REBOOT = 33685740;
    private static final int ID_STR_SHUTDOWN = 33685738;
    private static final boolean IS_SUPER_LITE_MODE = "SuperLite".equals(SystemProperties.get("ro.build.hw_emui_feature_level", ""));
    private static final float MENU_SCALE = 1.6f;
    private static final int MESSAGE_REPEAT_ROTATE = 0;
    private static final float NEW_CONFIRM_SCALE = 1.2f;
    private static final int ROTATION_DEFAULT = 0;
    private static final int ROTATION_NINETY = 90;
    private static final float SHUTDOWN_MENU_ACTION_SHORT_TRANSLATE_DP = 12.5f;
    private static final String TAG = "ShutdownMenuAnimations";
    private static ShutdownMenuAnimations instance = new ShutdownMenuAnimations();
    private static Context mContext;
    private final int ROTATION = SystemProperties.getInt("ro.panel.hw_orientation", 0);
    /* access modifiers changed from: private */
    public View circle_view;
    private TextView confirm_message;
    private View confirm_view;
    private int[] imageLocationEnd = new int[2];
    private int[] imageLocationStart = new int[2];
    /* access modifiers changed from: private */
    public boolean isAnimRunning = false;
    private View lockdown_view;
    private ArrayList<View> mActionViewList = new ArrayList<>();
    private AnimatorSet mConfirmSet;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                ShutdownMenuAnimations.this.circle_view.setRotation(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
                ShutdownMenuAnimations.this.rotateCircle();
            }
        }
    };
    /* access modifiers changed from: private */
    public AnimatorSet mShutdownEnterSet;
    /* access modifiers changed from: private */
    public View poweroff_hint_view;
    private View reboot_progress;
    private View reboot_view;
    private View shutdown_screenoff;
    private View shutdown_view;
    private final Interpolator typeA = new CubicBezierInterpolator(0.51f, 0.35f, 0.15f, 1.0f);
    private final Interpolator typeB = new CubicBezierInterpolator(0.53f, 0.51f, 0.69f, 0.99f);
    private ArrayList<View> viewList = new ArrayList<>();
    /* access modifiers changed from: private */
    public View view_confirm_action;
    /* access modifiers changed from: private */
    public View view_four_action;
    /* access modifiers changed from: private */
    public View view_two_action;
    /* access modifiers changed from: private */
    public View warning_list_view;

    public static boolean isSuperLiteMode() {
        Log.d(TAG, "is super lite mode " + IS_SUPER_LITE_MODE);
        return IS_SUPER_LITE_MODE;
    }

    public static synchronized ShutdownMenuAnimations getInstance(Context context) {
        ShutdownMenuAnimations shutdownMenuAnimations;
        synchronized (ShutdownMenuAnimations.class) {
            mContext = context.getApplicationContext();
            if (instance == null) {
                instance = new ShutdownMenuAnimations();
            }
            shutdownMenuAnimations = instance;
        }
        return shutdownMenuAnimations;
    }

    public AnimatorSet setImageAnimation(boolean isEnter) {
        ObjectAnimator scaleX;
        ObjectAnimator translateX;
        ObjectAnimator scaleY;
        ObjectAnimator alpha;
        ObjectAnimator translateY;
        AnimatorSet mSet = new AnimatorSet();
        if (this.isAnimRunning || this.viewList == null || this.viewList.size() == 0) {
            Log.w(TAG, "viewList is null or isAnimRunning");
            return mSet;
        }
        ArrayList<Animator> anims = new ArrayList<>();
        int size = this.viewList.size();
        int fromY = 0;
        int fromX = 0;
        for (int i = 0; i < size; i++) {
            View view = this.viewList.get(i);
            if (view != null) {
                switch (view.getId()) {
                    case ID_AIRPLANEMODE_RESOURCE /*34603145*/:
                        fromX = (int) mContext.getResources().getDimension(34472072);
                        fromY = (int) mContext.getResources().getDimension(34472073);
                        Log.d(TAG, "setImageAnimation viewid=ID_AIRPLANEMODE_RESOURCE  fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_SILENTMODE_RESOURCE /*34603146*/:
                        fromX = (int) mContext.getResources().getDimension(34472074);
                        fromY = (int) mContext.getResources().getDimension(34472075);
                        Log.d(TAG, "setImageAnimation viewid=ID_SILENTMODE_RESOURCE  fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_REBOOT_RESOURCE /*34603151*/:
                        this.reboot_view = view;
                        fromX = (int) mContext.getResources().getDimension(34472076);
                        fromY = (int) mContext.getResources().getDimension(34472077);
                        Log.d(TAG, "setImageAnimation viewid=ID_REBOOT_RESOURCE  fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_SHUTDOWN_RESOURCE /*34603152*/:
                        this.shutdown_view = view;
                        fromX = (int) mContext.getResources().getDimension(34472078);
                        fromY = (int) mContext.getResources().getDimension(34472079);
                        Log.d(TAG, "setImageAnimation viewid=ID_SHUTDOWN_RESOURCE  fromX=" + fromX + ", fromY= " + fromY);
                        break;
                }
                if (isEnter) {
                    translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{(float) fromX, 0.0f});
                    translateY = ObjectAnimator.ofFloat(view, "translationY", new float[]{(float) fromY, 0.0f});
                    alpha = ObjectAnimator.ofFloat(view, "alpha", new float[]{GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f});
                    scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{MENU_SCALE, 1.0f});
                    scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{MENU_SCALE, 1.0f});
                } else {
                    translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, (float) fromX});
                    translateY = ObjectAnimator.ofFloat(view, "translationY", new float[]{0.0f, (float) fromY});
                    alpha = ObjectAnimator.ofFloat(view, "alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
                    scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{1.0f, MENU_SCALE});
                    scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{1.0f, MENU_SCALE});
                }
                anims.add(translateX);
                anims.add(translateY);
                anims.add(alpha);
                anims.add(scaleX);
                anims.add(scaleY);
            }
        }
        mSet.setInterpolator(this.typeA);
        mSet.setDuration(800);
        mSet.playTogether(anims);
        return mSet;
    }

    public void shutdownOrRebootEnterAnim(View view, boolean isReboot) {
        if (view == null) {
            Log.w(TAG, "shutdownOrRebootEnterAnim view is null");
            return;
        }
        view.getLocationInWindow(this.imageLocationEnd);
        int[] iArr = this.imageLocationEnd;
        iArr[0] = iArr[0] + (view.getWidth() / 2);
        int[] iArr2 = this.imageLocationEnd;
        iArr2[1] = iArr2[1] + (view.getHeight() / 2);
        startConfirmEnterOrExitAnim(view, true, isReboot);
    }

    public void rebackShutdownMenu(boolean isReboot) {
        if (isReboot) {
            this.confirm_message.setText(ID_STR_REBOOT);
            startConfirmEnterOrExitAnim(this.reboot_view, false, isReboot);
        } else {
            this.confirm_message.setText(ID_STR_SHUTDOWN);
            startConfirmEnterOrExitAnim(this.shutdown_view, false, isReboot);
        }
        if (this.circle_view != null) {
            this.circle_view.setVisibility(4);
            this.circle_view.clearAnimation();
        }
    }

    private void startConfirmEnterOrExitAnim(View view, final boolean isEnter, final boolean isReboot) {
        if (this.isAnimRunning || view == null) {
            Log.w(TAG, "startConfirmEnterOrExitAnim isAnimRunning return! view = " + view);
            return;
        }
        if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
            if (this.view_four_action != null) {
                this.view_four_action.setVisibility(0);
            }
            this.view_two_action = null;
        } else {
            if (this.view_two_action != null) {
                this.view_two_action.setVisibility(0);
            }
            this.view_four_action = null;
        }
        if (this.imageLocationStart[0] == 0 && this.imageLocationStart[1] == 0 && this.ROTATION == 0) {
            this.confirm_view.getLocationInWindow(this.imageLocationStart);
            int[] iArr = this.imageLocationStart;
            iArr[0] = iArr[0] + (this.confirm_view.getWidth() / 2);
            int[] iArr2 = this.imageLocationStart;
            iArr2[1] = iArr2[1] + (this.confirm_view.getHeight() / 2);
        }
        if (this.ROTATION == ROTATION_NINETY) {
            WindowManager wm = (WindowManager) mContext.getSystemService("window");
            if (!(Settings.System.getInt(mContext.getContentResolver(), "navigationbar_is_min", 0) != 0)) {
                int navigationBarHeight = mContext.getResources().getDimensionPixelSize(17105186);
                this.imageLocationStart[0] = wm.getDefaultDisplay().getWidth() / 2;
                this.imageLocationStart[1] = (wm.getDefaultDisplay().getHeight() + navigationBarHeight) >>> 1;
            } else {
                this.imageLocationStart[0] = wm.getDefaultDisplay().getWidth() / 2;
                this.imageLocationStart[1] = wm.getDefaultDisplay().getHeight() / 2;
            }
        }
        Log.d(TAG, "imageLocationStart[] = [" + this.imageLocationStart[0] + ", " + this.imageLocationStart[1] + "],imageLocationEnd[] = [" + this.imageLocationEnd[0] + ", " + this.imageLocationEnd[1] + "]");
        this.mShutdownEnterSet = startConfirmTranslationAnimation(this.confirm_view, this.imageLocationEnd[0] - this.imageLocationStart[0], this.imageLocationEnd[1] - this.imageLocationStart[1], isEnter);
        if (this.mConfirmSet == null || !this.mConfirmSet.isRunning()) {
            final View mView = view;
            this.mConfirmSet = setImageAnimation(!isEnter);
            this.mConfirmSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    boolean unused = ShutdownMenuAnimations.this.isAnimRunning = true;
                    ShutdownMenuAnimations.this.setViewVisibility(mView, 4);
                    if (ShutdownMenuAnimations.this.view_confirm_action != null) {
                        ShutdownMenuAnimations.this.view_confirm_action.setVisibility(0);
                    } else {
                        Log.w(ShutdownMenuAnimations.TAG, "view_confirm_action is null!");
                    }
                    if (isEnter) {
                        ShutdownMenuAnimations.this.setViewVisibility(ShutdownMenuAnimations.this.poweroff_hint_view, 4);
                        if (!isReboot && ShutdownMenuAnimations.this.warning_list_view != null && ActivityManager.getCurrentUser() == 0) {
                            ShutdownMenuAnimations.this.warning_list_view.setVisibility(0);
                        }
                    } else {
                        if (!isReboot && ShutdownMenuAnimations.this.warning_list_view != null && ActivityManager.getCurrentUser() == 0) {
                            ShutdownMenuAnimations.this.warning_list_view.setVisibility(4);
                        }
                        ShutdownMenuAnimations.this.setViewVisibility(ShutdownMenuAnimations.this.poweroff_hint_view, 0);
                    }
                    if (ShutdownMenuAnimations.this.mShutdownEnterSet != null && !ShutdownMenuAnimations.this.mShutdownEnterSet.isRunning()) {
                        Log.d(ShutdownMenuAnimations.TAG, "mShutdownEnterSet start!");
                        ShutdownMenuAnimations.this.mShutdownEnterSet.start();
                    }
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    if (mView != null) {
                        mView.setVisibility(0);
                    } else {
                        Log.w(ShutdownMenuAnimations.TAG, "mView is null!");
                    }
                    if (isEnter) {
                        if (ShutdownMenuAnimations.this.view_four_action != null) {
                            ShutdownMenuAnimations.this.view_four_action.setVisibility(4);
                        }
                        if (ShutdownMenuAnimations.this.view_two_action != null && !HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                            ShutdownMenuAnimations.this.view_two_action.setVisibility(4);
                        }
                    } else if (ShutdownMenuAnimations.this.view_confirm_action != null) {
                        ShutdownMenuAnimations.this.view_confirm_action.setVisibility(4);
                    }
                    boolean unused = ShutdownMenuAnimations.this.isAnimRunning = false;
                    ShutdownMenuAnimations.this.view_confirm_action.findViewById(34603108).performAccessibilityAction(64, null);
                }

                public void onAnimationCancel(Animator animation) {
                    boolean unused = ShutdownMenuAnimations.this.isAnimRunning = false;
                }
            });
            this.mConfirmSet.start();
        }
    }

    /* access modifiers changed from: private */
    public void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private AnimatorSet startConfirmTranslationAnimation(View view, int fromX, int fromY, boolean isEnter) {
        ObjectAnimator translateX;
        ObjectAnimator scaleY;
        ObjectAnimator scaleX;
        ObjectAnimator translateY;
        AnimatorSet mSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList<>();
        Log.d(TAG, "fromX = " + fromX + ",fromY = " + fromY + ",isEnter = " + isEnter + ",view = " + view);
        if (isEnter) {
            translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{(float) fromX, 0.0f});
            translateY = ObjectAnimator.ofFloat(view, "translationY", new float[]{(float) fromY, 0.0f});
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{0.8f, 1.0f});
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{0.8f, 1.0f});
        } else {
            translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, (float) fromX});
            translateY = ObjectAnimator.ofFloat(view, "translationY", new float[]{0.0f, (float) fromY});
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{1.0f, 0.8f});
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{1.0f, 0.8f});
        }
        anims.add(translateX);
        anims.add(translateY);
        anims.add(scaleX);
        anims.add(scaleY);
        mSet.playTogether(anims);
        mSet.setInterpolator(this.typeA);
        mSet.setDuration(800);
        return mSet;
    }

    public void startShutdownOrRebootAnim(boolean isReboot) {
        if (this.isAnimRunning) {
            Log.d(TAG, "startShutdownOrRebootAnim isAnimRunning return!");
            return;
        }
        Log.d(TAG, "startShutdownOrRebootAnim!");
        if (this.view_confirm_action != null) {
            this.circle_view = this.view_confirm_action.findViewById(34603147);
        }
        if (this.circle_view != null) {
            this.circle_view.setVisibility(0);
            rotateCircle();
        }
    }

    /* access modifiers changed from: private */
    public void rotateCircle() {
        if (this.circle_view == null || !this.circle_view.isAttachedToWindow()) {
            Log.w(TAG, "rotateCircle circle_view is null or !circle_view.isAttachedToWindow()");
            return;
        }
        TimeInterpolator interpolator = this.typeA;
        if (!RenderNodeAnimator.isNativeInterpolator(interpolator)) {
            interpolator = new FallbackLUTInterpolator(this.typeA, 1280);
        }
        RenderNodeAnimator circleRotate = new RenderNodeAnimator(5, 360.0f);
        circleRotate.setDuration(1280);
        circleRotate.setInterpolator(interpolator);
        circleRotate.setTarget(this.circle_view);
        circleRotate.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                boolean unused = ShutdownMenuAnimations.this.isAnimRunning = true;
            }

            public void onAnimationCancel(Animator animation) {
                boolean unused = ShutdownMenuAnimations.this.isAnimRunning = false;
            }

            public void onAnimationEnd(Animator animation) {
                boolean unused = ShutdownMenuAnimations.this.isAnimRunning = false;
                ShutdownMenuAnimations.this.mHandler.sendEmptyMessage(0);
            }
        });
        circleRotate.start();
    }

    public Animation setSoundModeRotate() {
        Animation animRotation = AnimationUtils.loadAnimation(mContext, 34209799);
        animRotation.setInterpolator(this.typeB);
        return animRotation;
    }

    public void setFourActionView(View view) {
        this.view_four_action = view;
        this.poweroff_hint_view = this.view_four_action.findViewById(34603149);
    }

    public void setTwoActionView(View view) {
        this.view_two_action = view;
        this.poweroff_hint_view = this.view_two_action.findViewById(34603150);
        this.reboot_view = this.view_two_action.findViewById(ID_REBOOT_RESOURCE);
        this.shutdown_view = this.view_two_action.findViewById(ID_SHUTDOWN_RESOURCE);
        this.lockdown_view = this.view_two_action.findViewById(34603205);
        this.mActionViewList.clear();
        this.mActionViewList.add(this.reboot_view);
        this.mActionViewList.add(this.shutdown_view);
        this.mActionViewList.add(this.lockdown_view);
    }

    public void setConfirmActionView(View view) {
        if (view == null) {
            Log.w(TAG, "setConfirmActionView view is null");
            return;
        }
        this.view_confirm_action = view;
        this.confirm_view = this.view_confirm_action.findViewById(34603153);
        this.confirm_message = (TextView) this.view_confirm_action.findViewById(34603129);
        this.imageLocationStart[0] = 0;
        this.imageLocationStart[1] = 0;
        this.imageLocationEnd[0] = 0;
        this.imageLocationEnd[1] = 0;
        this.warning_list_view = this.view_confirm_action.findViewById(34603154);
    }

    public void setMenuViewList(ArrayList<View> views) {
        this.viewList = views;
    }

    public void setIsAnimRunning(boolean isRuning) {
        this.isAnimRunning = isRuning;
    }

    public boolean getIsAnimRunning() {
        return this.isAnimRunning;
    }

    public AnimatorSet setNewShutdownViewAnimation(boolean isEnter) {
        ObjectAnimator scaleX;
        ObjectAnimator alpha_two_action;
        ObjectAnimator scaleY;
        AnimatorSet mSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList<>();
        if (isEnter) {
            alpha_two_action = ObjectAnimator.ofFloat(this.view_two_action, "alpha", new float[]{GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f});
            scaleX = ObjectAnimator.ofFloat(this.view_two_action, "scaleX", new float[]{NEW_CONFIRM_SCALE, 1.0f});
            scaleY = ObjectAnimator.ofFloat(this.view_two_action, "scaleY", new float[]{NEW_CONFIRM_SCALE, 1.0f});
        } else {
            alpha_two_action = ObjectAnimator.ofFloat(this.view_two_action, "alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
            scaleX = ObjectAnimator.ofFloat(this.view_two_action, "scaleX", new float[]{1.0f, NEW_CONFIRM_SCALE});
            scaleY = ObjectAnimator.ofFloat(this.view_two_action, "scaleY", new float[]{1.0f, NEW_CONFIRM_SCALE});
        }
        anims.add(alpha_two_action);
        anims.add(scaleX);
        anims.add(scaleY);
        mSet.playTogether(anims);
        mSet.setInterpolator(CubicBezier_20_90);
        mSet.setDuration(350);
        return mSet;
    }

    public AnimatorSet setShutdownMenuDismissAnim() {
        AnimatorSet animSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList<>();
        ObjectAnimator confirmAlpha = ObjectAnimator.ofFloat(this.confirm_view, "alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
        ObjectAnimator confirmScaleX = ObjectAnimator.ofFloat(this.confirm_view, "scaleX", new float[]{NEW_CONFIRM_SCALE, 1.0f});
        ObjectAnimator confirmScaleY = ObjectAnimator.ofFloat(this.confirm_view, "scaleY", new float[]{NEW_CONFIRM_SCALE, 1.0f});
        anims.add(confirmAlpha);
        anims.add(confirmScaleX);
        anims.add(confirmScaleY);
        animSet.playTogether(anims);
        animSet.setInterpolator(CubicBezier_40_0);
        animSet.setDuration(200);
        return animSet;
    }

    public void newShutdownOrRebootEnterAnim(View view) {
        if (view == null) {
            Log.w(TAG, "newShutdownOrRebootEnterAnim view is null");
            return;
        }
        view.getLocationInWindow(this.imageLocationEnd);
        int[] iArr = this.imageLocationEnd;
        iArr[0] = iArr[0] + (view.getWidth() / 2);
        if (!(this.view_confirm_action == null || this.warning_list_view == null)) {
            this.warning_list_view.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
            this.view_confirm_action.setVisibility(0);
        }
        startNewConfirmEnterOrExitAnim(view, true);
    }

    public void rebackNewShutdownMenu(int flag) {
        if (!this.isAnimRunning) {
            if (this.view_two_action != null) {
                this.view_two_action.setVisibility(0);
            }
            if ((flag & 512) != 0) {
                this.confirm_message.setText(ID_STR_REBOOT);
                startNewConfirmEnterOrExitAnim(this.reboot_view, false);
            } else if ((flag & 8192) != 0) {
                this.confirm_message.setText(ID_STR_SHUTDOWN);
                startNewConfirmEnterOrExitAnim(this.shutdown_view, false);
            } else if ((131072 & flag) != 0) {
                this.confirm_message.setText(ID_STR_LOCKDOWN);
                startNewConfirmEnterOrExitAnim(this.lockdown_view, false);
            } else {
                Log.w(TAG, "reback newShutDownMenu, unknow flag = " + flag);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void startNewConfirmEnterOrExitAnim(View view, final boolean isEnter) {
        if (this.isAnimRunning || view == null) {
            Log.w(TAG, "startNewConfirmEnterOrExitAnim isAnimRunning return! view = " + view);
        } else if (isSuperLiteMode()) {
            Log.w(TAG, "super lite mode, no animations.");
            endNewConfirmEnterOrExitAnim(view, isEnter);
        } else {
            if (this.imageLocationStart[0] == 0 && this.ROTATION == 0) {
                this.confirm_view.getLocationInWindow(this.imageLocationStart);
                int[] iArr = this.imageLocationStart;
                iArr[0] = iArr[0] + (this.confirm_view.getWidth() / 2);
            }
            if (this.ROTATION == ROTATION_NINETY) {
                this.imageLocationStart[0] = ((WindowManager) mContext.getSystemService("window")).getDefaultDisplay().getWidth() / 2;
            }
            this.mShutdownEnterSet = startNewConfirmTranslationAnimation(this.confirm_view, this.imageLocationEnd[0] - this.imageLocationStart[0], isEnter);
            if (this.mConfirmSet == null || !this.mConfirmSet.isRunning()) {
                final View mView = view;
                View iconFrame = mView.findViewById(34603108);
                String iconTag = "";
                if (iconFrame != null) {
                    Object obj = iconFrame.getTag();
                    if (obj instanceof String) {
                        iconTag = obj;
                    }
                }
                final boolean isReboot = iconTag.equals("reboot");
                this.mConfirmSet = startViewStubAnimation(this.imageLocationStart[0] - this.imageLocationEnd[0], isEnter, iconTag);
                this.mConfirmSet.addListener(new Animator.AnimatorListener() {
                    public void onAnimationStart(Animator animation) {
                        boolean unused = ShutdownMenuAnimations.this.isAnimRunning = true;
                        if (mView != null) {
                            mView.setVisibility(4);
                        }
                        if (ShutdownMenuAnimations.this.mShutdownEnterSet != null && !ShutdownMenuAnimations.this.mShutdownEnterSet.isRunning()) {
                            ShutdownMenuAnimations.this.mShutdownEnterSet.start();
                        }
                        if (isReboot) {
                            return;
                        }
                        if (isEnter) {
                            ObjectAnimator alpha_hint = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.poweroff_hint_view, "alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
                            ObjectAnimator alpha_warning = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.warning_list_view, "alpha", new float[]{GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f});
                            alpha_hint.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
                            alpha_hint.setDuration(130);
                            alpha_warning.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
                            alpha_warning.setStartDelay(130);
                            alpha_warning.setDuration(220);
                            alpha_hint.start();
                            alpha_warning.start();
                            return;
                        }
                        ObjectAnimator alpha_hint2 = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.poweroff_hint_view, "alpha", new float[]{GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f});
                        ObjectAnimator alpha_warning2 = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.warning_list_view, "alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
                        alpha_hint2.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
                        alpha_hint2.setStartDelay(130);
                        alpha_hint2.setDuration(220);
                        alpha_warning2.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
                        alpha_warning2.setDuration(130);
                        alpha_hint2.start();
                        alpha_warning2.start();
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }

                    public void onAnimationEnd(Animator animation) {
                        ShutdownMenuAnimations.this.endNewConfirmEnterOrExitAnim(mView, isEnter);
                    }

                    public void onAnimationCancel(Animator animation) {
                        boolean unused = ShutdownMenuAnimations.this.isAnimRunning = false;
                    }
                });
                this.mConfirmSet.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void endNewConfirmEnterOrExitAnim(View view, boolean isEnter) {
        if (!isEnter) {
            if (view != null) {
                view.setVisibility(0);
            }
            if (this.view_confirm_action != null) {
                this.view_confirm_action.setVisibility(8);
            }
        } else if (this.view_two_action != null) {
            this.view_two_action.setVisibility(8);
        }
        this.isAnimRunning = false;
        if (this.view_confirm_action != null) {
            this.view_confirm_action.findViewById(34603108).performAccessibilityAction(64, null);
        }
    }

    private AnimatorSet startNewConfirmTranslationAnimation(View view, int fromX, boolean isEnter) {
        ObjectAnimator scaleY;
        ObjectAnimator scaleX;
        ObjectAnimator translateX;
        AnimatorSet mSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList<>();
        if (isEnter) {
            translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{(float) fromX, 0.0f});
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{1.0f, NEW_CONFIRM_SCALE});
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{1.0f, NEW_CONFIRM_SCALE});
        } else {
            translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, (float) fromX});
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{NEW_CONFIRM_SCALE, 1.0f});
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{NEW_CONFIRM_SCALE, 1.0f});
        }
        anims.add(translateX);
        anims.add(scaleX);
        anims.add(scaleY);
        mSet.playTogether(anims);
        mSet.setInterpolator(CubicBezier_20_90);
        mSet.setDuration(350);
        return mSet;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private List<View> findOtherViews(String tag) {
        List<View> res = new ArrayList<>();
        if (this.mActionViewList == null) {
            Log.w(TAG, "action views list is null!");
            return res;
        }
        int size = this.mActionViewList.size();
        for (int i = 0; i < size; i++) {
            View view = this.mActionViewList.get(i);
            if (view != null) {
                String viewTag = "";
                Object obj = view.getTag();
                if (obj instanceof String) {
                    viewTag = obj;
                }
                if (!tag.equals(viewTag)) {
                    res.add(view);
                }
            }
        }
        return res;
    }

    private int getPixelsByDp(float dp) {
        if (mContext == null) {
            Log.w(TAG, "mContext is null, return!");
            return 0;
        }
        Resources resources = mContext.getResources();
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
        int translateX;
        int translateX2;
        List<View> otherViews;
        String str = tag;
        AnimatorSet mSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList<>();
        if (this.isAnimRunning) {
            Log.w(TAG, "Animation is Running");
            return mSet;
        } else if (str == null) {
            Log.w(TAG, "Tag is null");
            return mSet;
        } else {
            List<View> otherViews2 = findOtherViews(str);
            if (this.lockdown_view == null) {
                translateX = toX;
            } else if (toX == 0) {
                translateX = 0;
            } else if (toX > 0) {
                translateX = getPixelsByDp(SHUTDOWN_MENU_ACTION_SHORT_TRANSLATE_DP);
            } else {
                translateX = -getPixelsByDp(SHUTDOWN_MENU_ACTION_SHORT_TRANSLATE_DP);
            }
            float startX = isEnter ? 0.0f : (float) translateX;
            float endX = isEnter ? (float) translateX : 0.0f;
            float f = 1.0f;
            float startAlpha = isEnter ? 1.0f : 0.0f;
            if (isEnter) {
                f = 0.0f;
            }
            float endAlpha = f;
            for (View view : otherViews2) {
                if (view != null) {
                    ObjectAnimator translateXAnim = ObjectAnimator.ofFloat(view, "translationX", new float[]{startX, endX});
                    translateXAnim.setDuration(350);
                    translateXAnim.setInterpolator(CubicBezier_20_90);
                    ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", new float[]{startAlpha, endAlpha});
                    if (this.lockdown_view != null) {
                        otherViews = otherViews2;
                        translateX2 = translateX;
                        alphaAnim.setDuration(260);
                    } else {
                        otherViews = otherViews2;
                        translateX2 = translateX;
                        alphaAnim.setDuration(350);
                    }
                    alphaAnim.setInterpolator(CubicBezier_20_90);
                    anims.add(translateXAnim);
                    anims.add(alphaAnim);
                    otherViews2 = otherViews;
                    translateX = translateX2;
                }
            }
            int i = translateX;
            if (str.equals("reboot")) {
                ObjectAnimator alphaHintAnim = ObjectAnimator.ofFloat(this.poweroff_hint_view, "alpha", new float[]{startAlpha, endAlpha});
                alphaHintAnim.setDuration(350);
                alphaHintAnim.setInterpolator(CubicBezier_20_90);
                anims.add(alphaHintAnim);
            }
            mSet.playTogether(anims);
            return mSet;
        }
    }

    public void startNewShutdownOrRebootAnim(boolean isReboot, final boolean isScreenOff) {
        if (this.isAnimRunning) {
            Log.d(TAG, "startNewShutdownOrRebootAnim isAnimRunning return!");
        } else if (isSuperLiteMode()) {
            Log.w(TAG, "super lite mode, no animations.");
            endNewShutdownOrRebootAnim(isScreenOff);
        } else {
            if (this.view_confirm_action != null) {
                AnimatorSet mSet = new AnimatorSet();
                ArrayList<Animator> anims = new ArrayList<>();
                ObjectAnimator alpha = ObjectAnimator.ofFloat(this.confirm_view, "alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(this.confirm_view, "scaleX", new float[]{NEW_CONFIRM_SCALE, 1.0f});
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(this.confirm_view, "scaleY", new float[]{NEW_CONFIRM_SCALE, 1.0f});
                anims.add(alpha);
                anims.add(scaleX);
                anims.add(scaleY);
                mSet.playTogether(anims);
                mSet.setInterpolator(CubicBezier_20_90);
                mSet.setDuration(200);
                mSet.addListener(new Animator.AnimatorListener() {
                    public void onAnimationStart(Animator animation) {
                        boolean unused = ShutdownMenuAnimations.this.isAnimRunning = true;
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }

                    public void onAnimationEnd(Animator animation) {
                        ShutdownMenuAnimations.this.endNewShutdownOrRebootAnim(isScreenOff);
                    }

                    public void onAnimationCancel(Animator animation) {
                        boolean unused = ShutdownMenuAnimations.this.isAnimRunning = false;
                    }
                });
                mSet.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void endNewShutdownOrRebootAnim(boolean isScreenOff) {
        this.confirm_view.setVisibility(8);
        if (isScreenOff) {
            this.warning_list_view.setVisibility(8);
            this.shutdown_screenoff = this.view_confirm_action.findViewById(ID_REBOOT_SCREENBLACK);
            this.shutdown_screenoff.setVisibility(0);
            PowerManager pm = (PowerManager) mContext.getSystemService("power");
            if (pm != null) {
                pm.goToSleep(SystemClock.uptimeMillis(), 2, 0);
                return;
            }
            return;
        }
        this.reboot_progress = this.view_confirm_action.findViewById(34603155);
        this.reboot_progress.setVisibility(0);
    }
}
