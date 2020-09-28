package com.android.internal.policy;

import android.graphics.Rect;

public class DockedDividerUtils {
    public static void calculateBoundsForPosition(int position, int dockSide, Rect outRect, int displayWidth, int displayHeight, int dividerSize) {
        boolean z = false;
        outRect.set(0, 0, displayWidth, displayHeight);
        if (dockSide == 1) {
            outRect.right = position;
        } else if (dockSide == 2) {
            outRect.bottom = position;
        } else if (dockSide == 3) {
            outRect.left = position + dividerSize;
        } else if (dockSide == 4) {
            outRect.top = position + dividerSize;
        }
        if (dockSide == 1 || dockSide == 2) {
            z = true;
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
        if (dockSide == 1) {
            return bounds.right;
        }
        if (dockSide == 2) {
            return bounds.bottom;
        }
        if (dockSide == 3) {
            return bounds.left - dividerSize;
        }
        if (dockSide != 4) {
            return 0;
        }
        return bounds.top - dividerSize;
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
        if (dockSide == 1) {
            return 3;
        }
        if (dockSide == 2) {
            return 4;
        }
        if (dockSide == 3) {
            return 1;
        }
        if (dockSide != 4) {
            return -1;
        }
        return 2;
    }
}
