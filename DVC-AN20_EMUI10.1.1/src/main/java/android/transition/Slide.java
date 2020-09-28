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
        /* class android.transition.Slide.AnonymousClass6 */

        @Override // android.transition.Slide.CalculateSlide
        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY() + (((float) sceneRoot.getHeight()) * fraction);
        }
    };
    private static final CalculateSlide sCalculateEnd = new CalculateSlideHorizontal() {
        /* class android.transition.Slide.AnonymousClass5 */

        @Override // android.transition.Slide.CalculateSlide
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
        /* class android.transition.Slide.AnonymousClass1 */

        @Override // android.transition.Slide.CalculateSlide
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX() - (((float) sceneRoot.getWidth()) * fraction);
        }
    };
    private static final CalculateSlide sCalculateRight = new CalculateSlideHorizontal() {
        /* class android.transition.Slide.AnonymousClass4 */

        @Override // android.transition.Slide.CalculateSlide
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX() + (((float) sceneRoot.getWidth()) * fraction);
        }
    };
    private static final CalculateSlide sCalculateStart = new CalculateSlideHorizontal() {
        /* class android.transition.Slide.AnonymousClass2 */

        @Override // android.transition.Slide.CalculateSlide
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
        /* class android.transition.Slide.AnonymousClass3 */

        @Override // android.transition.Slide.CalculateSlide
        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY() - (((float) sceneRoot.getHeight()) * fraction);
        }
    };
    private static final TimeInterpolator sDecelerate = new DecelerateInterpolator();
    private CalculateSlide mSlideCalculator = sCalculateBottom;
    private int mSlideEdge = 80;
    private float mSlideFraction = 1.0f;

    /* access modifiers changed from: private */
    public interface CalculateSlide {
        float getGoneX(ViewGroup viewGroup, View view, float f);

        float getGoneY(ViewGroup viewGroup, View view, float f);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface GravityFlag {
    }

    private static abstract class CalculateSlideHorizontal implements CalculateSlide {
        private CalculateSlideHorizontal() {
        }

        @Override // android.transition.Slide.CalculateSlide
        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY();
        }
    }

    private static abstract class CalculateSlideVertical implements CalculateSlide {
        private CalculateSlideVertical() {
        }

        @Override // android.transition.Slide.CalculateSlide
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

    @Override // android.transition.Visibility, android.transition.Transition
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        captureValues(transitionValues);
    }

    @Override // android.transition.Visibility, android.transition.Transition
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

    @Override // android.transition.Visibility
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (endValues == null) {
            return null;
        }
        int[] position = (int[]) endValues.values.get(PROPNAME_SCREEN_POSITION);
        float endX = view.getTranslationX();
        float endY = view.getTranslationY();
        return TranslationAnimationCreator.createAnimation(view, endValues, position[0], position[1], this.mSlideCalculator.getGoneX(sceneRoot, view, this.mSlideFraction), this.mSlideCalculator.getGoneY(sceneRoot, view, this.mSlideFraction), endX, endY, sDecelerate, this);
    }

    @Override // android.transition.Visibility
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
