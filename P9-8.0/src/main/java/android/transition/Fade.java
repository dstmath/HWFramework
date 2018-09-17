package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;

public class Fade extends Visibility {
    private static boolean DBG = false;
    public static final int IN = 1;
    private static final String LOG_TAG = "Fade";
    public static final int OUT = 2;
    static final String PROPNAME_TRANSITION_ALPHA = "android:fade:transitionAlpha";

    private static class FadeAnimatorListener extends AnimatorListenerAdapter {
        private boolean mLayerTypeChanged = false;
        private final View mView;

        public FadeAnimatorListener(View view) {
            this.mView = view;
        }

        public void onAnimationStart(Animator animator) {
            if (this.mView.hasOverlappingRendering() && this.mView.getLayerType() == 0) {
                this.mLayerTypeChanged = true;
                this.mView.setLayerType(2, null);
            }
        }

        public void onAnimationEnd(Animator animator) {
            this.mView.setTransitionAlpha(1.0f);
            if (this.mLayerTypeChanged) {
                this.mView.setLayerType(0, null);
            }
        }
    }

    public Fade(int fadingMode) {
        setMode(fadingMode);
    }

    public Fade(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Fade);
        setMode(a.getInt(0, getMode()));
        a.recycle();
    }

    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        transitionValues.values.put(PROPNAME_TRANSITION_ALPHA, Float.valueOf(transitionValues.view.getTransitionAlpha()));
    }

    private Animator createAnimation(final View view, float startAlpha, float endAlpha) {
        if (startAlpha == endAlpha) {
            return null;
        }
        view.setTransitionAlpha(startAlpha);
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "transitionAlpha", new float[]{endAlpha});
        if (DBG) {
            Log.d(LOG_TAG, "Created animator " + anim);
        }
        anim.addListener(new FadeAnimatorListener(view));
        addListener(new TransitionListenerAdapter() {
            public void onTransitionEnd(Transition transition) {
                view.setTransitionAlpha(1.0f);
                transition.removeListener(this);
            }
        });
        return anim;
    }

    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (DBG) {
            Log.d(LOG_TAG, "Fade.onAppear: startView, startVis, endView, endVis = " + (startValues != null ? startValues.view : null) + ", " + view);
        }
        float startAlpha = getStartAlpha(startValues, 0.0f);
        if (startAlpha == 1.0f) {
            startAlpha = 0.0f;
        }
        return createAnimation(view, startAlpha, 1.0f);
    }

    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return createAnimation(view, getStartAlpha(startValues, 1.0f), 0.0f);
    }

    private static float getStartAlpha(TransitionValues startValues, float fallbackValue) {
        float startAlpha = fallbackValue;
        if (startValues == null) {
            return startAlpha;
        }
        Float startAlphaFloat = (Float) startValues.values.get(PROPNAME_TRANSITION_ALPHA);
        if (startAlphaFloat != null) {
            return startAlphaFloat.floatValue();
        }
        return startAlpha;
    }
}
