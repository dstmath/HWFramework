package android.transition;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.R;

public class Slide extends Visibility {
    private static final String PROPNAME_SCREEN_POSITION = "android:slide:screenPosition";
    private static final String TAG = "Slide";
    private static final TimeInterpolator sAccelerate = new AccelerateInterpolator();
    private static final CalculateSlide sCalculateBottom = new CalculateSlideVertical() {
        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY() + (((float) sceneRoot.getHeight()) * fraction);
        }
    };
    private static final CalculateSlide sCalculateEnd = new CalculateSlideHorizontal() {
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            if (sceneRoot.getLayoutDirection() == 1) {
                return view.getTranslationX() - (((float) sceneRoot.getWidth()) * fraction);
            }
            return view.getTranslationX() + (((float) sceneRoot.getWidth()) * fraction);
        }
    };
    private static final CalculateSlide sCalculateLeft = new CalculateSlideHorizontal() {
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX() - (((float) sceneRoot.getWidth()) * fraction);
        }
    };
    private static final CalculateSlide sCalculateRight = new CalculateSlideHorizontal() {
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX() + (((float) sceneRoot.getWidth()) * fraction);
        }
    };
    private static final CalculateSlide sCalculateStart = new CalculateSlideHorizontal() {
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            if (sceneRoot.getLayoutDirection() == 1) {
                return view.getTranslationX() + (((float) sceneRoot.getWidth()) * fraction);
            }
            return view.getTranslationX() - (((float) sceneRoot.getWidth()) * fraction);
        }
    };
    private static final CalculateSlide sCalculateTop = new CalculateSlideVertical() {
        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY() - (((float) sceneRoot.getHeight()) * fraction);
        }
    };
    private static final TimeInterpolator sDecelerate = new DecelerateInterpolator();
    private CalculateSlide mSlideCalculator = sCalculateBottom;
    private int mSlideEdge = 80;
    private float mSlideFraction = 1.0f;

    private interface CalculateSlide {
        float getGoneX(ViewGroup viewGroup, View view, float f);

        float getGoneY(ViewGroup viewGroup, View view, float f);
    }

    private static abstract class CalculateSlideHorizontal implements CalculateSlide {
        /* synthetic */ CalculateSlideHorizontal(CalculateSlideHorizontal -this0) {
            this();
        }

        private CalculateSlideHorizontal() {
        }

        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY();
        }
    }

    private static abstract class CalculateSlideVertical implements CalculateSlide {
        /* synthetic */ CalculateSlideVertical(CalculateSlideVertical -this0) {
            this();
        }

        private CalculateSlideVertical() {
        }

        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX();
        }
    }

    public Slide() {
        setSlideEdge(80);
    }

    public Slide(int slideEdge) {
        setSlideEdge(slideEdge);
    }

    public Slide(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Slide);
        int edge = a.getInt(0, 80);
        a.recycle();
        setSlideEdge(edge);
    }

    private void captureValues(TransitionValues transitionValues) {
        int[] position = new int[2];
        transitionValues.view.getLocationOnScreen(position);
        transitionValues.values.put(PROPNAME_SCREEN_POSITION, position);
    }

    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        captureValues(transitionValues);
    }

    public void setSlideEdge(int slideEdge) {
        switch (slideEdge) {
            case 3:
                this.mSlideCalculator = sCalculateLeft;
                break;
            case 5:
                this.mSlideCalculator = sCalculateRight;
                break;
            case 48:
                this.mSlideCalculator = sCalculateTop;
                break;
            case 80:
                this.mSlideCalculator = sCalculateBottom;
                break;
            case Gravity.START /*8388611*/:
                this.mSlideCalculator = sCalculateStart;
                break;
            case Gravity.END /*8388613*/:
                this.mSlideCalculator = sCalculateEnd;
                break;
            default:
                throw new IllegalArgumentException("Invalid slide direction");
        }
        this.mSlideEdge = slideEdge;
        SidePropagation propagation = new SidePropagation();
        propagation.setSide(slideEdge);
        setPropagation(propagation);
    }

    public int getSlideEdge() {
        return this.mSlideEdge;
    }

    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (endValues == null) {
            return null;
        }
        int[] position = (int[]) endValues.values.get(PROPNAME_SCREEN_POSITION);
        float endX = view.getTranslationX();
        float endY = view.getTranslationY();
        return TranslationAnimationCreator.createAnimation(view, endValues, position[0], position[1], this.mSlideCalculator.getGoneX(sceneRoot, view, this.mSlideFraction), this.mSlideCalculator.getGoneY(sceneRoot, view, this.mSlideFraction), endX, endY, sDecelerate, this);
    }

    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null) {
            return null;
        }
        int[] position = (int[]) startValues.values.get(PROPNAME_SCREEN_POSITION);
        return TranslationAnimationCreator.createAnimation(view, startValues, position[0], position[1], view.getTranslationX(), view.getTranslationY(), this.mSlideCalculator.getGoneX(sceneRoot, view, this.mSlideFraction), this.mSlideCalculator.getGoneY(sceneRoot, view, this.mSlideFraction), sAccelerate, this);
    }

    public void setSlideFraction(float slideFraction) {
        this.mSlideFraction = slideFraction;
    }
}
