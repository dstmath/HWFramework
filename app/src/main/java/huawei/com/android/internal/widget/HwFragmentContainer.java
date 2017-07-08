package huawei.com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class HwFragmentContainer {
    protected static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_TYPE_LEFT_IN = 2;
    private static final int ANIMATION_TYPE_LEFT_OUT = 1;
    private static final int ANIMATION_TYPE_RIGHT_IN = 0;
    private static final int ANIMATION_TYPE_RIGHT_OUT = 3;
    protected static final float BLUR_LAYER_FORTY_PERCENT = 0.4f;
    protected static final float BLUR_LAYER_TRANSPARENT = 0.0f;
    public static final int COLUMN_NUMBER_ONE = 1;
    public static final int COLUMN_NUMBER_TWO = 2;
    public static final int CONTAINER_BOTH = 2;
    public static final int CONTAINER_LEFT = 0;
    public static final int CONTAINER_RIGHT = 1;
    private static final boolean DEBUG = false;
    public static final float DISPLAY_RATE_FIFTY_PERCENT = 0.5f;
    public static final float DISPLAY_RATE_FORTY_PERCENT = 0.4f;
    public static final float DISPLAY_RATE_SIXTY_PERCENT = 0.6f;
    protected static final int FRAGMENT_BACKGROUND_COLORCOLOR = -197380;
    private static final int FRAGMENT_LAYOUT_ID = 655360;
    public static final int FRAGMENT_LEVEL_1 = 1;
    public static final int FRAGMENT_LEVEL_2 = 2;
    public static final int FRAGMENT_LEVEL_3 = 3;
    public static final int FRAGMENT_LEVEL_NONE = 0;
    protected static final PathInterpolator INTERPOLATOR_20_90 = null;
    protected static final PathInterpolator INTERPOLATOR_33_33 = null;
    public static final int SPLITE_MODE_ALL_SEPARATE = 3;
    public static final int SPLITE_MODE_DEFAULT_SEPARATE = 0;
    public static final int SPLITE_MODE_LAND_SEPARATE = 2;
    public static final int SPLITE_MODE_NONE_SEPARATE = 1;
    private static final String TAG = "FragmentContainer";
    private static final String TAG_LEFT_CONTAINER = "left_container";
    private static final String TAG_RIGHT_CONTAINER = "right_container";
    public static final int TRANSITION_FADE = 1;
    public static final int TRANSITION_SLIDE_HORIZONTAL = 2;
    private int mAnimatorWidth;
    private ObjectAnimator mBlurLayerAnimator;
    private HwFragmentLayout mFragmentLayout;
    private FragmentManager mFragmentManager;
    private boolean mNoFragmentAnim;
    private int mPopCount;
    private boolean mPopMultiple;

    /* renamed from: huawei.com.android.internal.widget.HwFragmentContainer.1 */
    class AnonymousClass1 implements AnimatorUpdateListener {
        final /* synthetic */ float val$endAlpha;

        AnonymousClass1(float val$endAlpha) {
            this.val$endAlpha = val$endAlpha;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            if (animation != null && animation.getAnimatedValue() != null) {
                float alphaValue = ((Float) animation.getAnimatedValue()).floatValue();
                animation.getCurrentPlayTime();
                HwFragmentContainer.this.mFragmentLayout.getRightBlurLayer().setAlpha(alphaValue);
                if (((double) Math.abs(this.val$endAlpha - alphaValue)) < 0.001d) {
                    HwFragmentContainer.this.mFragmentLayout.getRightBlurLayer().setVisibility(8);
                    HwFragmentContainer.this.mFragmentLayout.getRightBlurLayer().setLayerType(HwFragmentContainer.SPLITE_MODE_DEFAULT_SEPARATE, null);
                }
            }
        }
    }

    private static class TranslateAnimatorListener implements AnimatorListener {
        boolean hasSetBackground;
        private View mView;

        private TranslateAnimatorListener() {
            this.hasSetBackground = HwFragmentContainer.DEBUG;
            this.mView = null;
        }

        protected void setView(View view) {
            this.mView = view;
        }

        public void onAnimationStart(Animator animation) {
            if (this.mView != null && this.mView.getBackground() == null) {
                this.hasSetBackground = true;
                this.mView.setBackgroundColor(HwFragmentContainer.FRAGMENT_BACKGROUND_COLORCOLOR);
            }
        }

        public void onAnimationEnd(Animator animation) {
            if (this.mView != null && this.hasSetBackground) {
                this.mView.setBackground(null);
                this.mView = null;
            }
        }

        public void onAnimationCancel(Animator animation) {
            if (this.mView != null && this.hasSetBackground) {
                this.mView.setBackground(null);
                this.mView = null;
            }
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.internal.widget.HwFragmentContainer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.internal.widget.HwFragmentContainer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.internal.widget.HwFragmentContainer.<clinit>():void");
    }

    public HwFragmentContainer(Context context, FragmentManager fm) {
        this(context, (float) DISPLAY_RATE_FORTY_PERCENT, fm);
    }

    public HwFragmentContainer(Context context, float displayRate, FragmentManager fm) {
        this(context, displayRate, DEBUG, fm);
    }

    public HwFragmentContainer(Context context, float displayRate, boolean canMove, FragmentManager fm) {
        this.mAnimatorWidth = SPLITE_MODE_DEFAULT_SEPARATE;
        this.mPopMultiple = DEBUG;
        this.mPopCount = SPLITE_MODE_DEFAULT_SEPARATE;
        this.mNoFragmentAnim = DEBUG;
        this.mFragmentLayout = new HwFragmentLayout(context, displayRate, canMove);
        if (this.mFragmentLayout.getId() == -1) {
            this.mFragmentLayout.setId(FRAGMENT_LAYOUT_ID);
        }
        this.mFragmentManager = fm;
    }

    public HwFragmentContainer(Context context, HwFragmentLayout fragmentLayout, FragmentManager fm) {
        this.mAnimatorWidth = SPLITE_MODE_DEFAULT_SEPARATE;
        this.mPopMultiple = DEBUG;
        this.mPopCount = SPLITE_MODE_DEFAULT_SEPARATE;
        this.mNoFragmentAnim = DEBUG;
        this.mFragmentLayout = fragmentLayout;
        this.mFragmentManager = fm;
    }

    public View getFragmentLayout() {
        return this.mFragmentLayout.getFragmentLayout();
    }

    public FrameLayout getLeftLayout() {
        return this.mFragmentLayout.getLeftLayout();
    }

    public FrameLayout getRightLayout() {
        return this.mFragmentLayout.getRightLayout();
    }

    public ImageView getSplitLine() {
        return this.mFragmentLayout.getSplitLine();
    }

    public ImageView getSplitBtn() {
        return this.mFragmentLayout.getSplitBtn();
    }

    private int getLeftContentID() {
        return this.mFragmentLayout.getLeftContentID();
    }

    private int getRightContentID() {
        return this.mFragmentLayout.getRightContentID();
    }

    public ImageView getLeftBlurLayer() {
        return this.mFragmentLayout.getLeftBlurLayer();
    }

    public ImageView getRightBlurLayer() {
        return this.mFragmentLayout.getRightBlurLayer();
    }

    public void setDisplayRate(float displayRate) {
        this.mFragmentLayout.setDisplayRate(displayRate);
    }

    public void setCanMove(boolean canMove) {
        this.mFragmentLayout.setCanMove(canMove);
    }

    public void setSplitMode(int splitMode) {
        this.mFragmentLayout.setSplitMode(splitMode);
    }

    public int getColumnsNumber() {
        return this.mFragmentLayout.calculateColumnsNumber();
    }

    public int getColumnsNumber(int orientation, int appWidth) {
        return this.mFragmentLayout.calculateColumnsNumber(orientation, appWidth);
    }

    public void setSeparateDeviceSize(double landSeparateSize, double portSeparteSize) {
        this.mFragmentLayout.setSeparateDeviceSize(landSeparateSize, portSeparteSize);
    }

    public void openLeftClearStack(Fragment fragment) {
        if (fragment != null) {
            setSelectedAndAnimation(SPLITE_MODE_DEFAULT_SEPARATE);
            popPrepare();
            this.mFragmentManager.popBackStackImmediate(TAG_LEFT_CONTAINER, TRANSITION_FADE);
            popFinish();
            FragmentTransaction transaction = this.mFragmentManager.beginTransaction();
            transaction.setTransition(4097);
            transaction.replace(getLeftContentID(), fragment, fragment.getClass().getName());
            transaction.addToBackStack(TAG_LEFT_CONTAINER);
            transaction.commitAllowingStateLoss();
            refreshFragmentLayout();
        }
    }

    public void openRightClearStack(Fragment fragment) {
        openRightClearStack(fragment, DEBUG);
    }

    public void initRightContainer(Fragment fragment) {
        openRightClearStack(fragment, true);
    }

    private void openRightClearStack(Fragment fragment, boolean isInitial) {
        if (fragment != null) {
            setSelectedAndAnimation(isInitial ? SPLITE_MODE_DEFAULT_SEPARATE : TRANSITION_FADE);
            popPrepare();
            this.mFragmentManager.popBackStackImmediate(TAG_LEFT_CONTAINER, SPLITE_MODE_DEFAULT_SEPARATE);
            popFinish();
            FragmentTransaction transaction = this.mFragmentManager.beginTransaction();
            transaction.setTransition(4097);
            transaction.replace(getRightContentID(), fragment, fragment.getClass().getName());
            transaction.addToBackStack(TAG_RIGHT_CONTAINER);
            transaction.commitAllowingStateLoss();
            this.mFragmentManager.executePendingTransactions();
            if (!isInitial) {
                refreshFragmentLayout();
            }
        }
    }

    public void changeRightAddToStack(Fragment nextFragment, Fragment currentFragment) {
        if (nextFragment != null) {
            setSelectedAndAnimation(TRANSITION_FADE);
            FragmentTransaction transaction = this.mFragmentManager.beginTransaction();
            transaction.setTransition(4097);
            if (currentFragment == null) {
                transaction.replace(getRightContentID(), nextFragment, nextFragment.getClass().getName());
            } else if (nextFragment.isAdded()) {
                transaction.hide(currentFragment);
                transaction.show(nextFragment);
            } else {
                transaction.hide(currentFragment);
                transaction.add(getRightContentID(), nextFragment, nextFragment.getClass().getName());
            }
            transaction.addToBackStack(TAG_RIGHT_CONTAINER);
            transaction.commitAllowingStateLoss();
            this.mFragmentManager.executePendingTransactions();
            refreshFragmentLayout();
        }
    }

    public void setSelectedContainer(int selectedContainer) {
        this.mFragmentLayout.setSelectedContainer(selectedContainer);
    }

    private void setSelectedAndAnimation(int selectedContainer) {
        if (TRANSITION_FADE != getColumnsNumber() || selectedContainer == getSelectedContainer()) {
            this.mNoFragmentAnim = DEBUG;
        } else {
            this.mNoFragmentAnim = true;
        }
        setSelectedContainer(selectedContainer);
    }

    public int getSelectedContainer() {
        return this.mFragmentLayout.getSelectedContainer();
    }

    public void refreshFragmentLayout() {
        this.mFragmentLayout.refreshFragmentLayout();
    }

    public int[] getLeftRightBackStackCount() {
        int[] stackCounts = new int[TRANSITION_SLIDE_HORIZONTAL];
        int leftStackCount = SPLITE_MODE_DEFAULT_SEPARATE;
        int rightStackCount = SPLITE_MODE_DEFAULT_SEPARATE;
        int backStackCount = this.mFragmentManager.getBackStackEntryCount();
        for (int i = SPLITE_MODE_DEFAULT_SEPARATE; i < backStackCount; i += TRANSITION_FADE) {
            BackStackEntry backstatck = this.mFragmentManager.getBackStackEntryAt(i);
            if (backstatck.getName() != null && backstatck.getName().indexOf(TAG_LEFT_CONTAINER) >= 0) {
                leftStackCount += TRANSITION_FADE;
            } else if (backstatck.getName() != null && backstatck.getName().indexOf(TAG_RIGHT_CONTAINER) >= 0) {
                rightStackCount += TRANSITION_FADE;
            }
        }
        stackCounts[SPLITE_MODE_DEFAULT_SEPARATE] = leftStackCount;
        stackCounts[TRANSITION_FADE] = rightStackCount;
        return stackCounts;
    }

    public boolean isBackPressed() {
        int rightStackCount = getLeftRightBackStackCount()[TRANSITION_FADE];
        int columnNumber = getColumnsNumber();
        if (rightStackCount > TRANSITION_FADE) {
            this.mFragmentManager.popBackStackImmediate();
            this.mFragmentLayout.setSelectedContainer(TRANSITION_FADE);
            return DEBUG;
        }
        if (columnNumber != TRANSITION_FADE) {
            return (columnNumber == TRANSITION_SLIDE_HORIZONTAL && rightStackCount == TRANSITION_FADE) ? true : true;
        } else {
            if (rightStackCount != TRANSITION_FADE || getRightLayout().getVisibility() != 0) {
                return true;
            }
            this.mFragmentLayout.setSelectedContainer(SPLITE_MODE_DEFAULT_SEPARATE);
            refreshFragmentLayout();
            return DEBUG;
        }
    }

    public void setSelectContainerByTouch(boolean enabled) {
        this.mFragmentLayout.setSelectContainerByTouch(enabled);
    }

    public void logBackStacksInfo() {
        int leftStackCount = SPLITE_MODE_DEFAULT_SEPARATE;
        int rightStackCount = SPLITE_MODE_DEFAULT_SEPARATE;
        int rightOpenStackCount = SPLITE_MODE_DEFAULT_SEPARATE;
        int backStackCount = this.mFragmentManager.getBackStackEntryCount();
        for (int i = SPLITE_MODE_DEFAULT_SEPARATE; i < backStackCount; i += TRANSITION_FADE) {
            BackStackEntry backstatck = this.mFragmentManager.getBackStackEntryAt(i);
            if (backstatck.getName() != null && backstatck.getName().indexOf(TAG_LEFT_CONTAINER) >= 0) {
                leftStackCount += TRANSITION_FADE;
            } else if (backstatck.getName() != null && backstatck.getName().indexOf(TAG_RIGHT_CONTAINER) >= 0) {
                rightStackCount += TRANSITION_FADE;
            }
            if (TAG_RIGHT_CONTAINER.equals(backstatck.getName())) {
                rightOpenStackCount += TRANSITION_FADE;
            }
            Log.d(TAG, "ID = " + backstatck.getId() + ", name = " + backstatck.getName());
        }
        Log.d(TAG, "BackStackCount = " + backStackCount + ", leftStackCount = " + leftStackCount + ", rightStackCount = " + rightStackCount + ", rightOpenStackCount = " + rightOpenStackCount);
    }

    private void popPrepare() {
        this.mPopMultiple = true;
        this.mPopCount = SPLITE_MODE_DEFAULT_SEPARATE;
    }

    private void popFinish() {
        this.mPopMultiple = DEBUG;
        this.mPopCount = SPLITE_MODE_DEFAULT_SEPARATE;
    }

    public Animator getAnimator(View view, int transit, boolean enter) {
        this.mFragmentLayout.displayAnimation();
        if (view == null || this.mNoFragmentAnim || getSelectedContainer() != TRANSITION_FADE) {
            return null;
        }
        int width = view.getWidth();
        if (width == 0) {
            width = this.mAnimatorWidth;
        }
        this.mAnimatorWidth = width;
        if (this.mAnimatorWidth == 0) {
            return null;
        }
        AnimatorSet animatorSet = null;
        if (transit == 4097) {
            if (enter) {
                animatorSet = createAnimator(view, SPLITE_MODE_DEFAULT_SEPARATE);
            } else {
                animatorSet = createAnimator(view, TRANSITION_FADE);
            }
        } else if (transit == 8194) {
            if (this.mPopMultiple) {
                if (view.getVisibility() == 0 && this.mPopCount == 0) {
                    animatorSet = createAnimator(view, TRANSITION_FADE);
                }
                this.mPopCount += TRANSITION_FADE;
            } else if (enter) {
                animatorSet = createAnimator(view, TRANSITION_SLIDE_HORIZONTAL);
            } else {
                animatorSet = createAnimator(view, SPLITE_MODE_ALL_SEPARATE);
            }
        }
        return animatorSet;
    }

    private AnimatorSet createAnimator(View view, int type) {
        float[] fArr;
        ObjectAnimator translateAnimator;
        TranslateAnimatorListener animatorListener;
        AnimatorSet animatorSet;
        if (type == 0) {
            view.setTranslationZ(HwFragmentMenuItemView.ALPHA_NORMAL);
            fArr = new float[TRANSITION_SLIDE_HORIZONTAL];
            fArr[SPLITE_MODE_DEFAULT_SEPARATE] = (float) this.mAnimatorWidth;
            fArr[TRANSITION_FADE] = BLUR_LAYER_TRANSPARENT;
            translateAnimator = ObjectAnimator.ofFloat(view, "translationX", fArr);
            translateAnimator.setInterpolator(INTERPOLATOR_20_90);
            translateAnimator.setDuration(300);
            animatorListener = new TranslateAnimatorListener();
            animatorListener.setView(view);
            translateAnimator.addListener(animatorListener);
            animatorSet = new AnimatorSet();
            animatorSet.play(translateAnimator);
            return animatorSet;
        } else if (type == TRANSITION_FADE) {
            view.setTranslationZ(-1.0f);
            fArr = new float[TRANSITION_SLIDE_HORIZONTAL];
            fArr[SPLITE_MODE_DEFAULT_SEPARATE] = BLUR_LAYER_TRANSPARENT;
            fArr[TRANSITION_FADE] = ((float) (-this.mAnimatorWidth)) / 2.0f;
            translateAnimator = ObjectAnimator.ofFloat(view, "translationX", fArr);
            translateAnimator.setInterpolator(INTERPOLATOR_33_33);
            translateAnimator.setDuration(300);
            createBlurLayerAnimator(view, BLUR_LAYER_TRANSPARENT, DISPLAY_RATE_FORTY_PERCENT);
            animatorSet = new AnimatorSet();
            r3 = new Animator[TRANSITION_SLIDE_HORIZONTAL];
            r3[SPLITE_MODE_DEFAULT_SEPARATE] = translateAnimator;
            r3[TRANSITION_FADE] = this.mBlurLayerAnimator;
            animatorSet.playTogether(r3);
            return animatorSet;
        } else if (type == TRANSITION_SLIDE_HORIZONTAL) {
            view.setTranslationZ(-1.0f);
            fArr = new float[TRANSITION_SLIDE_HORIZONTAL];
            fArr[SPLITE_MODE_DEFAULT_SEPARATE] = ((float) (-this.mAnimatorWidth)) / 2.0f;
            fArr[TRANSITION_FADE] = BLUR_LAYER_TRANSPARENT;
            translateAnimator = ObjectAnimator.ofFloat(view, "translationX", fArr);
            translateAnimator.setInterpolator(INTERPOLATOR_20_90);
            translateAnimator.setDuration(300);
            createBlurLayerAnimator(view, DISPLAY_RATE_FORTY_PERCENT, BLUR_LAYER_TRANSPARENT);
            animatorSet = new AnimatorSet();
            r3 = new Animator[TRANSITION_SLIDE_HORIZONTAL];
            r3[SPLITE_MODE_DEFAULT_SEPARATE] = translateAnimator;
            r3[TRANSITION_FADE] = this.mBlurLayerAnimator;
            animatorSet.playTogether(r3);
            return animatorSet;
        } else if (type != SPLITE_MODE_ALL_SEPARATE) {
            return null;
        } else {
            view.setTranslationZ(HwFragmentMenuItemView.ALPHA_NORMAL);
            fArr = new float[TRANSITION_SLIDE_HORIZONTAL];
            fArr[SPLITE_MODE_DEFAULT_SEPARATE] = BLUR_LAYER_TRANSPARENT;
            fArr[TRANSITION_FADE] = (float) this.mAnimatorWidth;
            translateAnimator = ObjectAnimator.ofFloat(view, "translationX", fArr);
            translateAnimator.setInterpolator(INTERPOLATOR_20_90);
            translateAnimator.setDuration(300);
            animatorListener = new TranslateAnimatorListener();
            animatorListener.setView(view);
            translateAnimator.addListener(animatorListener);
            animatorSet = new AnimatorSet();
            animatorSet.play(translateAnimator);
            return animatorSet;
        }
    }

    private void createBlurLayerAnimator(View view, float startAlpha, float endAlpha) {
        if (this.mBlurLayerAnimator != null) {
            this.mBlurLayerAnimator.removeAllUpdateListeners();
        }
        float[] fArr = new float[TRANSITION_SLIDE_HORIZONTAL];
        fArr[SPLITE_MODE_DEFAULT_SEPARATE] = startAlpha;
        fArr[TRANSITION_FADE] = endAlpha;
        this.mBlurLayerAnimator = ObjectAnimator.ofFloat(view, "blurlayer", fArr);
        this.mBlurLayerAnimator.setInterpolator(INTERPOLATOR_20_90);
        this.mBlurLayerAnimator.setDuration(300);
        this.mFragmentLayout.getRightBlurLayer().setTranslationZ(BLUR_LAYER_TRANSPARENT);
        this.mFragmentLayout.getRightBlurLayer().setVisibility(SPLITE_MODE_DEFAULT_SEPARATE);
        this.mFragmentLayout.getRightBlurLayer().setLayerType(TRANSITION_SLIDE_HORIZONTAL, null);
        this.mBlurLayerAnimator.addUpdateListener(new AnonymousClass1(endAlpha));
    }
}
