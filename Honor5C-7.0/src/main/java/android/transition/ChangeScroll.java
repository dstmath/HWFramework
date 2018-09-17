package android.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ChangeScroll extends Transition {
    private static final String PROPNAME_SCROLL_X = "android:changeScroll:x";
    private static final String PROPNAME_SCROLL_Y = "android:changeScroll:y";

    public ChangeScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_SCROLL_X, Integer.valueOf(transitionValues.view.getScrollX()));
        transitionValues.values.put(PROPNAME_SCROLL_Y, Integer.valueOf(transitionValues.view.getScrollY()));
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        View view = endValues.view;
        int startX = ((Integer) startValues.values.get(PROPNAME_SCROLL_X)).intValue();
        int endX = ((Integer) endValues.values.get(PROPNAME_SCROLL_X)).intValue();
        int startY = ((Integer) startValues.values.get(PROPNAME_SCROLL_Y)).intValue();
        int endY = ((Integer) endValues.values.get(PROPNAME_SCROLL_Y)).intValue();
        Animator scrollXAnimator = null;
        Animator scrollYAnimator = null;
        if (startX != endX) {
            view.setScrollX(startX);
            scrollXAnimator = ObjectAnimator.ofInt(view, "scrollX", new int[]{startX, endX});
        }
        if (startY != endY) {
            view.setScrollY(startY);
            scrollYAnimator = ObjectAnimator.ofInt(view, "scrollY", new int[]{startY, endY});
        }
        return TransitionUtils.mergeAnimators(scrollXAnimator, scrollYAnimator);
    }
}
