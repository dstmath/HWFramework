package com.android.server.accessibility;

import android.util.MathUtils;
import android.view.MotionEvent;

/* access modifiers changed from: package-private */
public final class GestureUtils {
    private GestureUtils() {
    }

    public static boolean isMultiTap(MotionEvent firstUp, MotionEvent secondUp, int multiTapTimeSlop, int multiTapDistanceSlop) {
        if (firstUp == null || secondUp == null) {
            return false;
        }
        return eventsWithinTimeAndDistanceSlop(firstUp, secondUp, multiTapTimeSlop, multiTapDistanceSlop);
    }

    private static boolean eventsWithinTimeAndDistanceSlop(MotionEvent first, MotionEvent second, int timeout, int distance) {
        if (!isTimedOut(first, second, timeout) && distance(first, second) < ((double) distance)) {
            return true;
        }
        return false;
    }

    public static double distance(MotionEvent first, MotionEvent second) {
        return (double) MathUtils.dist(first.getX(), first.getY(), second.getX(), second.getY());
    }

    public static boolean isTimedOut(MotionEvent firstUp, MotionEvent secondUp, int timeout) {
        return secondUp.getEventTime() - firstUp.getEventTime() >= ((long) timeout);
    }

    public static boolean isDraggingGesture(float firstPtrDownX, float firstPtrDownY, float secondPtrDownX, float secondPtrDownY, float firstPtrX, float firstPtrY, float secondPtrX, float secondPtrY, float maxDraggingAngleCos) {
        float firstDeltaX = firstPtrX - firstPtrDownX;
        float firstDeltaY = firstPtrY - firstPtrDownY;
        if (firstDeltaX == 0.0f && firstDeltaY == 0.0f) {
            return true;
        }
        float firstMagnitude = (float) Math.hypot((double) firstDeltaX, (double) firstDeltaY);
        float firstXNormalized = firstMagnitude > 0.0f ? firstDeltaX / firstMagnitude : firstDeltaX;
        float firstYNormalized = firstMagnitude > 0.0f ? firstDeltaY / firstMagnitude : firstDeltaY;
        float secondDeltaX = secondPtrX - secondPtrDownX;
        float secondDeltaY = secondPtrY - secondPtrDownY;
        if (secondDeltaX == 0.0f && secondDeltaY == 0.0f) {
            return true;
        }
        float secondMagnitude = (float) Math.hypot((double) secondDeltaX, (double) secondDeltaY);
        if ((firstXNormalized * (secondMagnitude > 0.0f ? secondDeltaX / secondMagnitude : secondDeltaX)) + (firstYNormalized * (secondMagnitude > 0.0f ? secondDeltaY / secondMagnitude : secondDeltaY)) < maxDraggingAngleCos) {
            return false;
        }
        return true;
    }
}
