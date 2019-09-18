package android.transition;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
            boolean isRtl = true;
            if (sceneRoot.getLayoutDirection() != 1) {
                isRtl = false;
            }
            if (isRtl) {
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
            boolean isRtl = true;
            if (sceneRoot.getLayoutDirection() != 1) {
                isRtl = false;
            }
            if (isRtl) {
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
        private CalculateSlideHorizontal() {
        }

        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY();
        }
    }

    private static abstract class CalculateSlideVertical implements CalculateSlide {
        private CalculateSlideVertical() {
        }

        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface GravityFlag {
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
        if (slideEdge == 3) {
            this.mSlideCalculator = sCalculateLeft;
        } else if (slideEdge == 5) {
            this.mSlideCalculator = sCalculateRight;
        } else if (slideEdge == 48) {
            this.mSlideCalculator = sCalculateTop;
        } else if (slideEdge == 80) {
            this.mSlideCalculator = sCalculateBottom;
        } else if (slideEdge == 8388611) {
            this.mSlideCalculator = sCalculateStart;
        } else if (slideEdge == 8388613) {
            this.mSlideCalculator = sCalculateEnd;
        } else {
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
        ViewGroup viewGroup = sceneRoot;
        View view2 = view;
        TransitionValues transitionValues = endValues;
        if (transitionValues == null) {
            return null;
        }
        int[] position = (int[]) transitionValues.values.get(PROPNAME_SCREEN_POSITION);
        float endX = view.getTranslationX();
        float endY = view.getTranslationY();
        float startX = this.mSlideCalculator.getGoneX(viewGroup, view2, this.mSlideFraction);
        return TranslationAnimationCreator.createAnimation(view2, transitionValues, position[0], position[1], startX, this.mSlideCalculator.getGoneY(viewGroup, view2, this.mSlideFraction), endX, endY, sDecelerate, this);
    }

    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        ViewGroup viewGroup = sceneRoot;
        View view2 = view;
        TransitionValues transitionValues = startValues;
        if (transitionValues == null) {
            return null;
        }
        int[] position = (int[]) transitionValues.values.get(PROPNAME_SCREEN_POSITION);
        float startX = view.getTranslationX();
        float startY = view.getTranslationY();
        float endX = this.mSlideCalculator.getGoneX(viewGroup, view2, this.mSlideFraction);
        return TranslationAnimationCreator.createAnimation(view2, transitionValues, position[0], position[1], startX, startY, endX, this.mSlideCalculator.getGoneY(viewGroup, view2, this.mSlideFraction), sAccelerate, this);
    }

    public void setSlideFraction(float slideFraction) {
        this.mSlideFraction = slideFraction;
    }
}
