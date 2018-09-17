package com.android.internal.policy;

import android.graphics.Rect;

public class DockedDividerUtils {
    public static void calculateBoundsForPosition(int position, int dockSide, Rect outRect, int displayWidth, int displayHeight, int dividerSize) {
        boolean z = true;
        outRect.set(0, 0, displayWidth, displayHeight);
        switch (dockSide) {
            case 1:
                outRect.right = position;
                break;
            case 2:
                outRect.bottom = position;
                break;
            case 3:
                outRect.left = position + dividerSize;
                break;
            case 4:
                outRect.top = position + dividerSize;
                break;
        }
        if (!(dockSide == 1 || dockSide == 2)) {
            z = false;
        }
        sanitizeStackBounds(outRect, z);
    }

    public static void sanitizeStackBounds(Rect bounds, boolean topLeft) {
        if (topLeft) {
            if (bounds.left >= bounds.right) {
                bounds.left = bounds.right - 1;
            }
            if (bounds.top >= bounds.bottom) {
                bounds.top = bounds.bottom - 1;
                return;
            }
            return;
        }
        if (bounds.right <= bounds.left) {
            bounds.right = bounds.left + 1;
        }
        if (bounds.bottom <= bounds.top) {
            bounds.bottom = bounds.top + 1;
        }
    }

    public static int calculatePositionForBounds(Rect bounds, int dockSide, int dividerSize) {
        switch (dockSide) {
            case 1:
                return bounds.right;
            case 2:
                return bounds.bottom;
            case 3:
                return bounds.left - dividerSize;
            case 4:
                return bounds.top - dividerSize;
            default:
                return 0;
        }
    }

    public static int calculateMiddlePosition(boolean isHorizontalDivision, Rect insets, int displayWidth, int displayHeight, int dividerSize) {
        int end;
        int start = isHorizontalDivision ? insets.top : insets.left;
        if (isHorizontalDivision) {
            end = displayHeight - insets.bottom;
        } else {
            end = displayWidth - insets.right;
        }
        return (((end - start) / 2) + start) - (dividerSize / 2);
    }

    public static int getDockSideFromCreatedMode(boolean dockOnTopOrLeft, boolean isHorizontalDivision) {
        if (dockOnTopOrLeft) {
            if (isHorizontalDivision) {
                return 2;
            }
            return 1;
        } else if (isHorizontalDivision) {
            return 4;
        } else {
            return 3;
        }
    }

    public static int invertDockSide(int dockSide) {
        switch (dockSide) {
            case 1:
                return 3;
            case 2:
                return 4;
            case 3:
                return 1;
            case 4:
                return 2;
            default:
                return -1;
        }
    }
}
