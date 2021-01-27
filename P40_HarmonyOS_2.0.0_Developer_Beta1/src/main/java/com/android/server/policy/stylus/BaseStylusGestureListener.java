package com.android.server.policy.stylus;

import android.gesture.Gesture;

public abstract class BaseStylusGestureListener {

    public interface OnGesturePerformedListener {
        void onLetterGesture(String str, Gesture gesture, double d);

        void onLineGesture(String str, Gesture gesture, double d);

        void onRegionGesture(String str, Gesture gesture, double d);
    }

    public void cancelStylusGesture() {
    }

    public static class OrientationFix {
        public String multipleOrientationsGestureName;
        public String replaceGestureWith;
        public String singleOrientationGestureName;

        public OrientationFix(String gestureName, String singleOrientationGestureName2, String replaceGestureWith2) {
            this.multipleOrientationsGestureName = gestureName;
            this.singleOrientationGestureName = singleOrientationGestureName2;
            this.replaceGestureWith = replaceGestureWith2;
        }
    }
}
