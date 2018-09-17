package android.transition;

import android.view.View;

public abstract class VisibilityPropagation extends TransitionPropagation {
    private static final String PROPNAME_VIEW_CENTER = "android:visibilityPropagation:center";
    private static final String PROPNAME_VISIBILITY = "android:visibilityPropagation:visibility";
    private static final String[] VISIBILITY_PROPAGATION_VALUES = new String[]{PROPNAME_VISIBILITY, PROPNAME_VIEW_CENTER};

    public void captureValues(TransitionValues values) {
        View view = values.view;
        Integer visibility = (Integer) values.values.get("android:visibility:visibility");
        if (visibility == null) {
            visibility = Integer.valueOf(view.getVisibility());
        }
        values.values.put(PROPNAME_VISIBILITY, visibility);
        loc = new int[2];
        view.getLocationOnScreen(loc);
        loc[0] = loc[0] + Math.round(view.getTranslationX());
        loc[0] = loc[0] + (view.getWidth() / 2);
        loc[1] = loc[1] + Math.round(view.getTranslationY());
        loc[1] = loc[1] + (view.getHeight() / 2);
        values.values.put(PROPNAME_VIEW_CENTER, loc);
    }

    public String[] getPropagationProperties() {
        return VISIBILITY_PROPAGATION_VALUES;
    }

    public int getViewVisibility(TransitionValues values) {
        if (values == null) {
            return 8;
        }
        Integer visibility = (Integer) values.values.get(PROPNAME_VISIBILITY);
        if (visibility == null) {
            return 8;
        }
        return visibility.intValue();
    }

    public int getViewX(TransitionValues values) {
        return getViewCoordinate(values, 0);
    }

    public int getViewY(TransitionValues values) {
        return getViewCoordinate(values, 1);
    }

    private static int getViewCoordinate(TransitionValues values, int coordinateIndex) {
        if (values == null) {
            return -1;
        }
        int[] coordinates = (int[]) values.values.get(PROPNAME_VIEW_CENTER);
        if (coordinates == null) {
            return -1;
        }
        return coordinates[coordinateIndex];
    }
}
