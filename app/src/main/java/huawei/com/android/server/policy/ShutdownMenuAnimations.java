package huawei.com.android.server.policy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.TextView;
import com.android.internal.view.animation.FallbackLUTInterpolator;
import com.android.server.input.HwCircleAnimation;
import com.huawei.hwanimation.CubicBezierInterpolator;
import java.util.ArrayList;

public class ShutdownMenuAnimations {
    private static final float CONFIRM_SCALE = 0.8f;
    public static final Interpolator CubicBezier_20_90 = null;
    public static final Interpolator CubicBezier_33_33 = null;
    private static final int DEGREES_ROTATE = 360;
    private static final int DURATION_130MS = 130;
    private static final int DURATION_220MS = 220;
    private static final int DURATION_350MS = 350;
    private static final int DURATION_800MS = 800;
    private static final int DURATION_ROTATE = 1280;
    private static final int ID_AIRPLANEMODE_RESOURCE = 34603209;
    private static final int ID_REBOOT_RESOURCE = 34603211;
    private static final int ID_SHUTDOWN_RESOURCE = 34603212;
    private static final int ID_SILENTMODE_RESOURCE = 34603210;
    private static final int ID_STR_REBOOT = 33685733;
    private static final int ID_STR_SHUTDOWN = 33685731;
    private static final float MENU_SCALE = 1.6f;
    private static final int MESSAGE_REPEAT_ROTATE = 0;
    private static final float NEW_CONFIRM_SCALE = 1.3f;
    private static final int ROTATION_DEFAULT = 0;
    private static final int ROTATION_NINETY = 90;
    private static final String TAG = "ShutdownMenuAnimations";
    private static ShutdownMenuAnimations instance;
    private static Context mContext;
    private final int ROTATION;
    private View circle_view;
    private TextView confirm_message;
    private View confirm_view;
    private int[] imageLocationEnd;
    private int[] imageLocationStart;
    private boolean isAnimRunning;
    private AnimatorSet mConfirmSet;
    private Handler mHandler;
    private AnimatorSet mShutdownEnterSet;
    private View poweroff_hint_view;
    private View reboot_progress;
    private View reboot_view;
    private View shutdown_view;
    private final Interpolator typeA;
    private final Interpolator typeB;
    private ArrayList<View> viewList;
    private View view_confirm_action;
    private View view_four_action;
    private View view_two_action;
    private View warning_list_view;

    /* renamed from: huawei.com.android.server.policy.ShutdownMenuAnimations.2 */
    class AnonymousClass2 implements AnimatorListener {
        final /* synthetic */ boolean val$isEnter;
        final /* synthetic */ boolean val$isReboot;
        final /* synthetic */ View val$mView;

        AnonymousClass2(View val$mView, boolean val$isEnter, boolean val$isReboot) {
            this.val$mView = val$mView;
            this.val$isEnter = val$isEnter;
            this.val$isReboot = val$isReboot;
        }

        public void onAnimationStart(Animator animation) {
            ShutdownMenuAnimations.this.isAnimRunning = true;
            if (this.val$mView != null) {
                this.val$mView.setVisibility(4);
            }
            if (ShutdownMenuAnimations.this.view_confirm_action != null) {
                ShutdownMenuAnimations.this.view_confirm_action.setVisibility(ShutdownMenuAnimations.ROTATION_DEFAULT);
            } else {
                Log.w(ShutdownMenuAnimations.TAG, "view_confirm_action is null!");
            }
            if (this.val$isEnter) {
                if (ShutdownMenuAnimations.this.poweroff_hint_view != null) {
                    ShutdownMenuAnimations.this.poweroff_hint_view.setVisibility(4);
                }
                if (!(this.val$isReboot || ShutdownMenuAnimations.this.warning_list_view == null || ActivityManager.getCurrentUser() != 0)) {
                    ShutdownMenuAnimations.this.warning_list_view.setVisibility(ShutdownMenuAnimations.ROTATION_DEFAULT);
                }
            } else {
                if (!(this.val$isReboot || ShutdownMenuAnimations.this.warning_list_view == null || ActivityManager.getCurrentUser() != 0)) {
                    ShutdownMenuAnimations.this.warning_list_view.setVisibility(4);
                }
                if (ShutdownMenuAnimations.this.poweroff_hint_view != null) {
                    ShutdownMenuAnimations.this.poweroff_hint_view.setVisibility(ShutdownMenuAnimations.ROTATION_DEFAULT);
                }
            }
            if (ShutdownMenuAnimations.this.mShutdownEnterSet != null && !ShutdownMenuAnimations.this.mShutdownEnterSet.isRunning()) {
                Log.d(ShutdownMenuAnimations.TAG, "mShutdownEnterSet start!");
                ShutdownMenuAnimations.this.mShutdownEnterSet.start();
            }
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (this.val$mView != null) {
                this.val$mView.setVisibility(ShutdownMenuAnimations.ROTATION_DEFAULT);
            } else {
                Log.w(ShutdownMenuAnimations.TAG, "mView is null!");
            }
            if (this.val$isEnter) {
                if (ShutdownMenuAnimations.this.view_four_action != null) {
                    ShutdownMenuAnimations.this.view_four_action.setVisibility(4);
                }
                if (!(ShutdownMenuAnimations.this.view_two_action == null || HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned())) {
                    ShutdownMenuAnimations.this.view_two_action.setVisibility(4);
                }
            } else if (ShutdownMenuAnimations.this.view_confirm_action != null) {
                ShutdownMenuAnimations.this.view_confirm_action.setVisibility(4);
            }
            ShutdownMenuAnimations.this.isAnimRunning = false;
            ShutdownMenuAnimations.this.view_confirm_action.findViewById(34603122).performAccessibilityAction(64, null);
        }

        public void onAnimationCancel(Animator animation) {
            ShutdownMenuAnimations.this.isAnimRunning = false;
        }
    }

    /* renamed from: huawei.com.android.server.policy.ShutdownMenuAnimations.4 */
    class AnonymousClass4 implements AnimatorListener {
        final /* synthetic */ boolean val$isEnter;
        final /* synthetic */ boolean val$isReboot;
        final /* synthetic */ View val$mView;

        AnonymousClass4(View val$mView, boolean val$isReboot, boolean val$isEnter) {
            this.val$mView = val$mView;
            this.val$isReboot = val$isReboot;
            this.val$isEnter = val$isEnter;
        }

        public void onAnimationStart(Animator animation) {
            ShutdownMenuAnimations.this.isAnimRunning = true;
            if (this.val$mView != null) {
                this.val$mView.setVisibility(4);
            }
            if (!(ShutdownMenuAnimations.this.mShutdownEnterSet == null || ShutdownMenuAnimations.this.mShutdownEnterSet.isRunning())) {
                ShutdownMenuAnimations.this.mShutdownEnterSet.start();
            }
            if (!this.val$isReboot) {
                ObjectAnimator alpha_hint;
                ObjectAnimator alpha_warning;
                if (this.val$isEnter) {
                    alpha_hint = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.poweroff_hint_view, "alpha", new float[]{HwCircleAnimation.SMALL_ALPHA, 0.0f});
                    alpha_warning = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.warning_list_view, "alpha", new float[]{0.0f, HwCircleAnimation.SMALL_ALPHA});
                    alpha_hint.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
                    alpha_hint.setDuration(130);
                    alpha_warning.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
                    alpha_warning.setStartDelay(130);
                    alpha_warning.setDuration(220);
                    alpha_hint.start();
                    alpha_warning.start();
                    return;
                }
                alpha_hint = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.poweroff_hint_view, "alpha", new float[]{0.0f, HwCircleAnimation.SMALL_ALPHA});
                alpha_warning = ObjectAnimator.ofFloat(ShutdownMenuAnimations.this.warning_list_view, "alpha", new float[]{HwCircleAnimation.SMALL_ALPHA, 0.0f});
                alpha_hint.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
                alpha_hint.setStartDelay(130);
                alpha_hint.setDuration(220);
                alpha_warning.setInterpolator(ShutdownMenuAnimations.CubicBezier_33_33);
                alpha_warning.setDuration(130);
                alpha_hint.start();
                alpha_warning.start();
            }
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.val$isEnter) {
                if (this.val$mView != null) {
                    this.val$mView.setVisibility(ShutdownMenuAnimations.ROTATION_DEFAULT);
                }
                if (ShutdownMenuAnimations.this.view_confirm_action != null) {
                    ShutdownMenuAnimations.this.view_confirm_action.setVisibility(8);
                }
            } else if (ShutdownMenuAnimations.this.view_two_action != null) {
                ShutdownMenuAnimations.this.view_two_action.setVisibility(8);
            }
            ShutdownMenuAnimations.this.isAnimRunning = false;
            ShutdownMenuAnimations.this.view_confirm_action.findViewById(34603122).performAccessibilityAction(64, null);
        }

        public void onAnimationCancel(Animator animation) {
            ShutdownMenuAnimations.this.isAnimRunning = false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.server.policy.ShutdownMenuAnimations.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.server.policy.ShutdownMenuAnimations.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.server.policy.ShutdownMenuAnimations.<clinit>():void");
    }

    public ShutdownMenuAnimations() {
        this.typeA = new CubicBezierInterpolator(0.51f, 0.35f, 0.15f, HwCircleAnimation.SMALL_ALPHA);
        this.typeB = new CubicBezierInterpolator(0.53f, 0.51f, 0.69f, 0.99f);
        this.imageLocationStart = new int[2];
        this.imageLocationEnd = new int[2];
        this.isAnimRunning = false;
        this.viewList = new ArrayList();
        this.ROTATION = SystemProperties.getInt("ro.panel.hw_orientation", ROTATION_DEFAULT);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ShutdownMenuAnimations.ROTATION_DEFAULT /*0*/:
                        ShutdownMenuAnimations.this.circle_view.setRotation(0.0f);
                        ShutdownMenuAnimations.this.rotateCircle();
                    default:
                }
            }
        };
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
        Log.d(TAG, "setImageAnimation Enter!isEnter=" + isEnter);
        AnimatorSet mSet = new AnimatorSet();
        if (this.isAnimRunning || this.viewList == null || this.viewList.size() == 0) {
            Log.w(TAG, "viewList is null or isAnimRunning");
            return mSet;
        }
        ArrayList<Animator> anims = new ArrayList();
        int fromX = ROTATION_DEFAULT;
        int fromY = ROTATION_DEFAULT;
        for (View view : this.viewList) {
            if (view != null) {
                ObjectAnimator translateX;
                ObjectAnimator translateY;
                ObjectAnimator alpha;
                ObjectAnimator scaleX;
                ObjectAnimator scaleY;
                switch (view.getId()) {
                    case ID_AIRPLANEMODE_RESOURCE /*34603209*/:
                        fromX = (int) mContext.getResources().getDimension(34472081);
                        fromY = (int) mContext.getResources().getDimension(34472082);
                        Log.d(TAG, "setImageAnimation viewid=ID_AIRPLANEMODE_RESOURCE  fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_SILENTMODE_RESOURCE /*34603210*/:
                        fromX = (int) mContext.getResources().getDimension(34472083);
                        fromY = (int) mContext.getResources().getDimension(34472084);
                        Log.d(TAG, "setImageAnimation viewid=ID_SILENTMODE_RESOURCE  fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_REBOOT_RESOURCE /*34603211*/:
                        this.reboot_view = view;
                        fromX = (int) mContext.getResources().getDimension(34472085);
                        fromY = (int) mContext.getResources().getDimension(34472086);
                        Log.d(TAG, "setImageAnimation viewid=ID_REBOOT_RESOURCE  fromX=" + fromX + ", fromY= " + fromY);
                        break;
                    case ID_SHUTDOWN_RESOURCE /*34603212*/:
                        this.shutdown_view = view;
                        fromX = (int) mContext.getResources().getDimension(34472087);
                        fromY = (int) mContext.getResources().getDimension(34472088);
                        Log.d(TAG, "setImageAnimation viewid=ID_SHUTDOWN_RESOURCE  fromX=" + fromX + ", fromY= " + fromY);
                        break;
                }
                if (isEnter) {
                    translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{(float) fromX, 0.0f});
                    translateY = ObjectAnimator.ofFloat(view, "translationY", new float[]{(float) fromY, 0.0f});
                    alpha = ObjectAnimator.ofFloat(view, "alpha", new float[]{0.0f, HwCircleAnimation.SMALL_ALPHA});
                    scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{MENU_SCALE, HwCircleAnimation.SMALL_ALPHA});
                    scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{MENU_SCALE, HwCircleAnimation.SMALL_ALPHA});
                } else {
                    translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, (float) fromX});
                    translateY = ObjectAnimator.ofFloat(view, "translationY", new float[]{0.0f, (float) fromY});
                    alpha = ObjectAnimator.ofFloat(view, "alpha", new float[]{HwCircleAnimation.SMALL_ALPHA, 0.0f});
                    scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{HwCircleAnimation.SMALL_ALPHA, MENU_SCALE});
                    scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{HwCircleAnimation.SMALL_ALPHA, MENU_SCALE});
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
        iArr[ROTATION_DEFAULT] = iArr[ROTATION_DEFAULT] + (view.getWidth() / 2);
        iArr = this.imageLocationEnd;
        iArr[1] = iArr[1] + (view.getHeight() / 2);
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

    private void startConfirmEnterOrExitAnim(View view, boolean isEnter, boolean isReboot) {
        boolean z = false;
        if (this.isAnimRunning || view == null) {
            Log.w(TAG, "startConfirmEnterOrExitAnim isAnimRunning return! view = " + view);
            return;
        }
        if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
            if (this.view_four_action != null) {
                this.view_four_action.setVisibility(ROTATION_DEFAULT);
            }
            this.view_two_action = null;
        } else {
            if (this.view_two_action != null) {
                this.view_two_action.setVisibility(ROTATION_DEFAULT);
            }
            this.view_four_action = null;
        }
        if (this.imageLocationStart[ROTATION_DEFAULT] == 0 && this.imageLocationStart[1] == 0 && this.ROTATION == 0) {
            this.confirm_view.getLocationInWindow(this.imageLocationStart);
            int[] iArr = this.imageLocationStart;
            iArr[ROTATION_DEFAULT] = iArr[ROTATION_DEFAULT] + (this.confirm_view.getWidth() / 2);
            iArr = this.imageLocationStart;
            iArr[1] = iArr[1] + (this.confirm_view.getHeight() / 2);
        }
        if (this.ROTATION == ROTATION_NINETY) {
            boolean navigationBarStatus;
            WindowManager wm = (WindowManager) mContext.getSystemService("window");
            if (System.getInt(mContext.getContentResolver(), "navigationbar_is_min", ROTATION_DEFAULT) != 0) {
                navigationBarStatus = true;
            } else {
                navigationBarStatus = false;
            }
            if (navigationBarStatus) {
                this.imageLocationStart[ROTATION_DEFAULT] = wm.getDefaultDisplay().getWidth() / 2;
                this.imageLocationStart[1] = wm.getDefaultDisplay().getHeight() / 2;
            } else {
                int navigationBarHeight = mContext.getResources().getDimensionPixelSize(17104920);
                this.imageLocationStart[ROTATION_DEFAULT] = wm.getDefaultDisplay().getWidth() / 2;
                this.imageLocationStart[1] = (wm.getDefaultDisplay().getHeight() + navigationBarHeight) >>> 1;
            }
        }
        Log.d(TAG, "imageLocationStart[] = [" + this.imageLocationStart[ROTATION_DEFAULT] + ", " + this.imageLocationStart[1] + "]," + "imageLocationEnd[] = [" + this.imageLocationEnd[ROTATION_DEFAULT] + ", " + this.imageLocationEnd[1] + "]");
        this.mShutdownEnterSet = startConfirmTranslationAnimation(this.confirm_view, this.imageLocationEnd[ROTATION_DEFAULT] - this.imageLocationStart[ROTATION_DEFAULT], this.imageLocationEnd[1] - this.imageLocationStart[1], isEnter);
        if (this.mConfirmSet == null || !this.mConfirmSet.isRunning()) {
            View mView = view;
            if (!isEnter) {
                z = true;
            }
            this.mConfirmSet = setImageAnimation(z);
            this.mConfirmSet.addListener(new AnonymousClass2(view, isEnter, isReboot));
            this.mConfirmSet.start();
        }
    }

    private AnimatorSet startConfirmTranslationAnimation(View view, int fromX, int fromY, boolean isEnter) {
        ObjectAnimator translateX;
        ObjectAnimator translateY;
        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
        AnimatorSet mSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList();
        Log.d(TAG, "fromX = " + fromX + ",fromY = " + fromY + ",isEnter = " + isEnter + ",view = " + view);
        if (isEnter) {
            translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{(float) fromX, 0.0f});
            translateY = ObjectAnimator.ofFloat(view, "translationY", new float[]{(float) fromY, 0.0f});
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{CONFIRM_SCALE, HwCircleAnimation.SMALL_ALPHA});
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{CONFIRM_SCALE, HwCircleAnimation.SMALL_ALPHA});
        } else {
            translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, (float) fromX});
            translateY = ObjectAnimator.ofFloat(view, "translationY", new float[]{0.0f, (float) fromY});
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{HwCircleAnimation.SMALL_ALPHA, CONFIRM_SCALE});
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{HwCircleAnimation.SMALL_ALPHA, CONFIRM_SCALE});
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
            this.circle_view = this.view_confirm_action.findViewById(34603206);
        }
        if (this.circle_view != null) {
            this.circle_view.setVisibility(ROTATION_DEFAULT);
            rotateCircle();
        }
    }

    private void rotateCircle() {
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
                ShutdownMenuAnimations.this.isAnimRunning = true;
            }

            public void onAnimationCancel(Animator animation) {
                ShutdownMenuAnimations.this.isAnimRunning = false;
            }

            public void onAnimationEnd(Animator animation) {
                ShutdownMenuAnimations.this.isAnimRunning = false;
                ShutdownMenuAnimations.this.mHandler.sendEmptyMessage(ShutdownMenuAnimations.ROTATION_DEFAULT);
            }
        });
        circleRotate.start();
    }

    public Animation setSoundModeRotate() {
        Animation animRotation = AnimationUtils.loadAnimation(mContext, 34078763);
        animRotation.setInterpolator(this.typeB);
        return animRotation;
    }

    public void setFourActionView(View view) {
        this.view_four_action = view;
        this.poweroff_hint_view = this.view_four_action.findViewById(34603213);
    }

    public void setTwoActionView(View view) {
        this.view_two_action = view;
        this.poweroff_hint_view = this.view_two_action.findViewById(34603214);
        this.reboot_view = this.view_two_action.findViewById(ID_REBOOT_RESOURCE);
        this.shutdown_view = this.view_two_action.findViewById(ID_SHUTDOWN_RESOURCE);
    }

    public void setConfirmActionView(View view) {
        if (view == null) {
            Log.w(TAG, "setConfirmActionView view is null");
            return;
        }
        this.view_confirm_action = view;
        this.confirm_view = this.view_confirm_action.findViewById(34603205);
        this.confirm_message = (TextView) this.view_confirm_action.findViewById(34603117);
        this.imageLocationStart[ROTATION_DEFAULT] = ROTATION_DEFAULT;
        this.imageLocationStart[1] = ROTATION_DEFAULT;
        this.imageLocationEnd[ROTATION_DEFAULT] = ROTATION_DEFAULT;
        this.imageLocationEnd[1] = ROTATION_DEFAULT;
        this.warning_list_view = this.view_confirm_action.findViewById(34603208);
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
        ObjectAnimator alpha_two_action;
        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
        AnimatorSet mSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList();
        if (isEnter) {
            alpha_two_action = ObjectAnimator.ofFloat(this.view_two_action, "alpha", new float[]{0.0f, HwCircleAnimation.SMALL_ALPHA});
            scaleX = ObjectAnimator.ofFloat(this.view_two_action, "scaleX", new float[]{NEW_CONFIRM_SCALE, HwCircleAnimation.SMALL_ALPHA});
            scaleY = ObjectAnimator.ofFloat(this.view_two_action, "scaleY", new float[]{NEW_CONFIRM_SCALE, HwCircleAnimation.SMALL_ALPHA});
        } else {
            alpha_two_action = ObjectAnimator.ofFloat(this.view_two_action, "alpha", new float[]{HwCircleAnimation.SMALL_ALPHA, 0.0f});
            scaleX = ObjectAnimator.ofFloat(this.view_two_action, "scaleX", new float[]{HwCircleAnimation.SMALL_ALPHA, NEW_CONFIRM_SCALE});
            scaleY = ObjectAnimator.ofFloat(this.view_two_action, "scaleY", new float[]{HwCircleAnimation.SMALL_ALPHA, NEW_CONFIRM_SCALE});
        }
        anims.add(alpha_two_action);
        anims.add(scaleX);
        anims.add(scaleY);
        mSet.playTogether(anims);
        mSet.setInterpolator(CubicBezier_20_90);
        mSet.setDuration(350);
        return mSet;
    }

    public void newShutdownOrRebootEnterAnim(View view, boolean isReboot) {
        if (view == null) {
            Log.w(TAG, "newShutdownOrRebootEnterAnim view is null");
            return;
        }
        view.getLocationInWindow(this.imageLocationEnd);
        int[] iArr = this.imageLocationEnd;
        iArr[ROTATION_DEFAULT] = iArr[ROTATION_DEFAULT] + (view.getWidth() / 2);
        if (!(this.view_confirm_action == null || this.warning_list_view == null)) {
            this.warning_list_view.setAlpha(0.0f);
            this.view_confirm_action.setVisibility(ROTATION_DEFAULT);
        }
        startNewConfirmEnterOrExitAnim(view, true, isReboot);
    }

    public void rebackNewShutdownMenu(boolean isReboot) {
        if (!this.isAnimRunning) {
            if (this.view_two_action != null) {
                this.view_two_action.setVisibility(ROTATION_DEFAULT);
            }
            if (isReboot) {
                this.confirm_message.setText(ID_STR_REBOOT);
                startNewConfirmEnterOrExitAnim(this.reboot_view, false, isReboot);
            } else {
                this.confirm_message.setText(ID_STR_SHUTDOWN);
                startNewConfirmEnterOrExitAnim(this.shutdown_view, false, isReboot);
            }
        }
    }

    private void startNewConfirmEnterOrExitAnim(View view, boolean isEnter, boolean isReboot) {
        if (this.isAnimRunning || view == null) {
            Log.w(TAG, "startNewConfirmEnterOrExitAnim isAnimRunning return! view = " + view);
            return;
        }
        if (this.imageLocationStart[ROTATION_DEFAULT] == 0 && this.ROTATION == 0) {
            this.confirm_view.getLocationInWindow(this.imageLocationStart);
            int[] iArr = this.imageLocationStart;
            iArr[ROTATION_DEFAULT] = iArr[ROTATION_DEFAULT] + (this.confirm_view.getWidth() / 2);
        }
        if (this.ROTATION == ROTATION_NINETY) {
            this.imageLocationStart[ROTATION_DEFAULT] = ((WindowManager) mContext.getSystemService("window")).getDefaultDisplay().getWidth() / 2;
        }
        this.mShutdownEnterSet = startNewConfirmTranslationAnimation(this.confirm_view, this.imageLocationEnd[ROTATION_DEFAULT] - this.imageLocationStart[ROTATION_DEFAULT], isEnter);
        if (this.mConfirmSet == null || !this.mConfirmSet.isRunning()) {
            View mView = view;
            this.mConfirmSet = startViewStubAnimation(this.imageLocationStart[ROTATION_DEFAULT] - this.imageLocationEnd[ROTATION_DEFAULT], isEnter, isReboot);
            this.mConfirmSet.addListener(new AnonymousClass4(view, isReboot, isEnter));
            this.mConfirmSet.start();
        }
    }

    private AnimatorSet startNewConfirmTranslationAnimation(View view, int fromX, boolean isEnter) {
        ObjectAnimator translateX;
        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
        AnimatorSet mSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList();
        if (isEnter) {
            translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{(float) fromX, 0.0f});
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{HwCircleAnimation.SMALL_ALPHA, NEW_CONFIRM_SCALE});
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{HwCircleAnimation.SMALL_ALPHA, NEW_CONFIRM_SCALE});
        } else {
            translateX = ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, (float) fromX});
            scaleX = ObjectAnimator.ofFloat(view, "scaleX", new float[]{NEW_CONFIRM_SCALE, HwCircleAnimation.SMALL_ALPHA});
            scaleY = ObjectAnimator.ofFloat(view, "scaleY", new float[]{NEW_CONFIRM_SCALE, HwCircleAnimation.SMALL_ALPHA});
        }
        anims.add(translateX);
        anims.add(scaleX);
        anims.add(scaleY);
        mSet.playTogether(anims);
        mSet.setInterpolator(CubicBezier_20_90);
        mSet.setDuration(350);
        return mSet;
    }

    private AnimatorSet startViewStubAnimation(int toX, boolean isEnter, boolean isReboot) {
        AnimatorSet mSet = new AnimatorSet();
        ArrayList<Animator> anims = new ArrayList();
        if (this.isAnimRunning) {
            Log.w(TAG, "Animation is Running");
            return mSet;
        }
        ObjectAnimator translateX;
        ObjectAnimator alpha_view;
        if (isEnter) {
            if (isReboot) {
                translateX = ObjectAnimator.ofFloat(this.shutdown_view, "translationX", new float[]{0.0f, (float) toX});
                alpha_view = ObjectAnimator.ofFloat(this.shutdown_view, "alpha", new float[]{HwCircleAnimation.SMALL_ALPHA, 0.0f});
                anims.add(ObjectAnimator.ofFloat(this.poweroff_hint_view, "alpha", new float[]{HwCircleAnimation.SMALL_ALPHA, 0.0f}));
            } else {
                translateX = ObjectAnimator.ofFloat(this.reboot_view, "translationX", new float[]{0.0f, (float) toX});
                alpha_view = ObjectAnimator.ofFloat(this.reboot_view, "alpha", new float[]{HwCircleAnimation.SMALL_ALPHA, 0.0f});
            }
        } else if (isReboot) {
            translateX = ObjectAnimator.ofFloat(this.shutdown_view, "translationX", new float[]{(float) toX, 0.0f});
            alpha_view = ObjectAnimator.ofFloat(this.shutdown_view, "alpha", new float[]{0.0f, HwCircleAnimation.SMALL_ALPHA});
            anims.add(ObjectAnimator.ofFloat(this.poweroff_hint_view, "alpha", new float[]{0.0f, HwCircleAnimation.SMALL_ALPHA}));
        } else {
            translateX = ObjectAnimator.ofFloat(this.reboot_view, "translationX", new float[]{(float) toX, 0.0f});
            alpha_view = ObjectAnimator.ofFloat(this.reboot_view, "alpha", new float[]{0.0f, HwCircleAnimation.SMALL_ALPHA});
        }
        anims.add(translateX);
        anims.add(alpha_view);
        mSet.playTogether(anims);
        mSet.setInterpolator(CubicBezier_20_90);
        mSet.setDuration(350);
        return mSet;
    }

    public void startNewShutdownOrRebootAnim(boolean isReboot) {
        if (this.isAnimRunning) {
            Log.d(TAG, "startNewShutdownOrRebootAnim isAnimRunning return!");
            return;
        }
        if (this.view_confirm_action != null) {
            AnimatorSet mSet = new AnimatorSet();
            ArrayList<Animator> anims = new ArrayList();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(this.confirm_view, "alpha", new float[]{HwCircleAnimation.SMALL_ALPHA, 0.0f});
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this.confirm_view, "scaleX", new float[]{NEW_CONFIRM_SCALE, HwCircleAnimation.SMALL_ALPHA});
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this.confirm_view, "scaleY", new float[]{NEW_CONFIRM_SCALE, HwCircleAnimation.SMALL_ALPHA});
            anims.add(alpha);
            anims.add(scaleX);
            anims.add(scaleY);
            mSet.playTogether(anims);
            mSet.setInterpolator(CubicBezier_20_90);
            mSet.setDuration(350);
            mSet.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    ShutdownMenuAnimations.this.isAnimRunning = true;
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    ShutdownMenuAnimations.this.confirm_view.setVisibility(8);
                    ShutdownMenuAnimations.this.reboot_progress = ShutdownMenuAnimations.this.view_confirm_action.findViewById(34603216);
                    ShutdownMenuAnimations.this.reboot_progress.setVisibility(ShutdownMenuAnimations.ROTATION_DEFAULT);
                }

                public void onAnimationCancel(Animator animation) {
                    ShutdownMenuAnimations.this.isAnimRunning = false;
                }
            });
            mSet.start();
        }
    }
}
