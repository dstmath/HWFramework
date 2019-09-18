package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ChangeClipBounds extends Transition {
    private static final String PROPNAME_BOUNDS = "android:clipBounds:bounds";
    private static final String PROPNAME_CLIP = "android:clipBounds:clip";
    private static final String TAG = "ChangeTransform";
    private static final String[] sTransitionProperties = {PROPNAME_CLIP};

    public ChangeClipBounds() {
    }

    public ChangeClipBounds(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    private void captureValues(TransitionValues values) {
        View view = values.view;
        if (view.getVisibility() != 8) {
            Rect clip = view.getClipBounds();
            values.values.put(PROPNAME_CLIP, clip);
            if (clip == null) {
                values.values.put(PROPNAME_BOUNDS, new Rect(0, 0, view.getWidth(), view.getHeight()));
            }
        }
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: android.graphics.Rect} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: android.graphics.Rect} */
    /* JADX WARNING: Multi-variable type inference failed */
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null || !startValues.values.containsKey(PROPNAME_CLIP) || !endValues.values.containsKey(PROPNAME_CLIP)) {
            return null;
        }
        Rect start = (Rect) startValues.values.get(PROPNAME_CLIP);
        Rect end = (Rect) endValues.values.get(PROPNAME_CLIP);
        boolean endIsNull = end == null;
        if (start == null && end == null) {
            return null;
        }
        if (start == null) {
            start = startValues.values.get(PROPNAME_BOUNDS);
        } else if (end == null) {
            end = (Rect) endValues.values.get(PROPNAME_BOUNDS);
        }
        if (start.equals(end)) {
            return null;
        }
        endValues.view.setClipBounds(start);
        ObjectAnimator animator = ObjectAnimator.ofObject(endValues.view, "clipBounds", new RectEvaluator(new Rect()), new Object[]{start, end});
        if (endIsNull) {
            final View endView = endValues.view;
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    endView.setClipBounds(null);
                }
            });
        }
        return animator;
    }
}
