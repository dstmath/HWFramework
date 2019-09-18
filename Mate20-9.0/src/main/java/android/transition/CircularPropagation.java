package android.transition;

import android.graphics.Rect;
import android.view.ViewGroup;

public class CircularPropagation extends VisibilityPropagation {
    private static final String TAG = "CircularPropagation";
    private float mPropagationSpeed = 3.0f;

    public void setPropagationSpeed(float propagationSpeed) {
        if (propagationSpeed != 0.0f) {
            this.mPropagationSpeed = propagationSpeed;
            return;
        }
        throw new IllegalArgumentException("propagationSpeed may not be 0");
    }

    public long getStartDelay(ViewGroup sceneRoot, Transition transition, TransitionValues startValues, TransitionValues endValues) {
        TransitionValues positionValues;
        int epicenterY;
        int epicenterX;
        TransitionValues transitionValues = startValues;
        if (transitionValues == null && endValues == null) {
            return 0;
        }
        int directionMultiplier = 1;
        if (endValues == null || getViewVisibility(transitionValues) == 0) {
            positionValues = transitionValues;
            directionMultiplier = -1;
        } else {
            positionValues = endValues;
        }
        int viewCenterX = getViewX(positionValues);
        int viewCenterY = getViewY(positionValues);
        Rect epicenter = transition.getEpicenter();
        if (epicenter != null) {
            epicenterX = epicenter.centerX();
            epicenterY = epicenter.centerY();
            ViewGroup viewGroup = sceneRoot;
        } else {
            int[] loc = new int[2];
            sceneRoot.getLocationOnScreen(loc);
            int epicenterX2 = Math.round(((float) (loc[0] + (sceneRoot.getWidth() / 2))) + sceneRoot.getTranslationX());
            epicenterY = Math.round(((float) (loc[1] + (sceneRoot.getHeight() / 2))) + sceneRoot.getTranslationY());
            epicenterX = epicenterX2;
        }
        double distanceFraction = distance((float) viewCenterX, (float) viewCenterY, (float) epicenterX, (float) epicenterY) / distance(0.0f, 0.0f, (float) sceneRoot.getWidth(), (float) sceneRoot.getHeight());
        long duration = transition.getDuration();
        if (duration < 0) {
            duration = 300;
        }
        return Math.round(((double) (((float) (((long) directionMultiplier) * duration)) / this.mPropagationSpeed)) * distanceFraction);
    }

    private static double distance(float x1, float y1, float x2, float y2) {
        return Math.hypot((double) (x2 - x1), (double) (y2 - y1));
    }
}
