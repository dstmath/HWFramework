package android.view;

import android.graphics.Rect;

public class Gravity {
    public static final int AXIS_CLIP = 8;
    public static final int AXIS_PULL_AFTER = 4;
    public static final int AXIS_PULL_BEFORE = 2;
    public static final int AXIS_SPECIFIED = 1;
    public static final int AXIS_X_SHIFT = 0;
    public static final int AXIS_Y_SHIFT = 4;
    public static final int BOTTOM = 80;
    public static final int CENTER = 17;
    public static final int CENTER_HORIZONTAL = 1;
    public static final int CENTER_VERTICAL = 16;
    public static final int CLIP_HORIZONTAL = 8;
    public static final int CLIP_VERTICAL = 128;
    public static final int DISPLAY_CLIP_HORIZONTAL = 16777216;
    public static final int DISPLAY_CLIP_VERTICAL = 268435456;
    public static final int END = 8388613;
    public static final int FILL = 119;
    public static final int FILL_HORIZONTAL = 7;
    public static final int FILL_VERTICAL = 112;
    public static final int HORIZONTAL_GRAVITY_MASK = 7;
    public static final int LEFT = 3;
    public static final int NO_GRAVITY = 0;
    public static final int RELATIVE_HORIZONTAL_GRAVITY_MASK = 8388615;
    public static final int RELATIVE_LAYOUT_DIRECTION = 8388608;
    public static final int RIGHT = 5;
    public static final int START = 8388611;
    public static final int TOP = 48;
    public static final int VERTICAL_GRAVITY_MASK = 112;

    public static void apply(int gravity, int w, int h, Rect container, Rect outRect) {
        apply(gravity, w, h, container, 0, 0, outRect);
    }

    public static void apply(int gravity, int w, int h, Rect container, Rect outRect, int layoutDirection) {
        apply(getAbsoluteGravity(gravity, layoutDirection), w, h, container, 0, 0, outRect);
    }

    public static void apply(int gravity, int w, int h, Rect container, int xAdj, int yAdj, Rect outRect) {
        switch (gravity & 6) {
            case 0:
                outRect.left = (container.left + (((container.right - container.left) - w) / 2)) + xAdj;
                outRect.right = outRect.left + w;
                if ((gravity & 8) == 8) {
                    if (outRect.left < container.left) {
                        outRect.left = container.left;
                    }
                    if (outRect.right > container.right) {
                        outRect.right = container.right;
                        break;
                    }
                }
                break;
            case 2:
                outRect.left = container.left + xAdj;
                outRect.right = outRect.left + w;
                if ((gravity & 8) == 8 && outRect.right > container.right) {
                    outRect.right = container.right;
                    break;
                }
            case 4:
                outRect.right = container.right - xAdj;
                outRect.left = outRect.right - w;
                if ((gravity & 8) == 8 && outRect.left < container.left) {
                    outRect.left = container.left;
                    break;
                }
            default:
                outRect.left = container.left + xAdj;
                outRect.right = container.right + xAdj;
                break;
        }
        switch (gravity & 96) {
            case 0:
                outRect.top = (container.top + (((container.bottom - container.top) - h) / 2)) + yAdj;
                outRect.bottom = outRect.top + h;
                if ((gravity & 128) == 128) {
                    if (outRect.top < container.top) {
                        outRect.top = container.top;
                    }
                    if (outRect.bottom > container.bottom) {
                        outRect.bottom = container.bottom;
                        return;
                    }
                    return;
                }
                return;
            case 32:
                outRect.top = container.top + yAdj;
                outRect.bottom = outRect.top + h;
                if ((gravity & 128) == 128 && outRect.bottom > container.bottom) {
                    outRect.bottom = container.bottom;
                    return;
                }
                return;
            case 64:
                outRect.bottom = container.bottom - yAdj;
                outRect.top = outRect.bottom - h;
                if ((gravity & 128) == 128 && outRect.top < container.top) {
                    outRect.top = container.top;
                    return;
                }
                return;
            default:
                outRect.top = container.top + yAdj;
                outRect.bottom = container.bottom + yAdj;
                return;
        }
    }

    public static void apply(int gravity, int w, int h, Rect container, int xAdj, int yAdj, Rect outRect, int layoutDirection) {
        apply(getAbsoluteGravity(gravity, layoutDirection), w, h, container, xAdj, yAdj, outRect);
    }

    public static void applyDisplay(int gravity, Rect display, Rect inoutObj) {
        int off;
        if ((268435456 & gravity) != 0) {
            if (inoutObj.top < display.top) {
                inoutObj.top = display.top;
            }
            if (inoutObj.bottom > display.bottom) {
                inoutObj.bottom = display.bottom;
            }
        } else {
            off = 0;
            if (inoutObj.top < display.top) {
                off = display.top - inoutObj.top;
            } else if (inoutObj.bottom > display.bottom) {
                off = display.bottom - inoutObj.bottom;
            }
            if (off != 0) {
                if (inoutObj.height() > display.bottom - display.top) {
                    inoutObj.top = display.top;
                    inoutObj.bottom = display.bottom;
                } else {
                    inoutObj.top += off;
                    inoutObj.bottom += off;
                }
            }
        }
        if ((16777216 & gravity) != 0) {
            if (inoutObj.left < display.left) {
                inoutObj.left = display.left;
            }
            if (inoutObj.right > display.right) {
                inoutObj.right = display.right;
                return;
            }
            return;
        }
        off = 0;
        if (inoutObj.left < display.left) {
            off = display.left - inoutObj.left;
        } else if (inoutObj.right > display.right) {
            off = display.right - inoutObj.right;
        }
        if (off == 0) {
            return;
        }
        if (inoutObj.width() > display.right - display.left) {
            inoutObj.left = display.left;
            inoutObj.right = display.right;
            return;
        }
        inoutObj.left += off;
        inoutObj.right += off;
    }

    public static void applyDisplay(int gravity, Rect display, Rect inoutObj, int layoutDirection) {
        applyDisplay(getAbsoluteGravity(gravity, layoutDirection), display, inoutObj);
    }

    public static boolean isVertical(int gravity) {
        return gravity > 0 && (gravity & 112) != 0;
    }

    public static boolean isHorizontal(int gravity) {
        return gravity > 0 && (RELATIVE_HORIZONTAL_GRAVITY_MASK & gravity) != 0;
    }

    public static int getAbsoluteGravity(int gravity, int layoutDirection) {
        int result = gravity;
        if ((8388608 & gravity) <= 0) {
            return result;
        }
        if ((gravity & START) == START) {
            result = gravity & -8388612;
            if (layoutDirection == 1) {
                result |= 5;
            } else {
                result |= 3;
            }
        } else if ((gravity & END) == END) {
            result = gravity & -8388614;
            if (layoutDirection == 1) {
                result |= 3;
            } else {
                result |= 5;
            }
        }
        return result & -8388609;
    }
}
