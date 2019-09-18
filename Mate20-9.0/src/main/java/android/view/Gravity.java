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
        int i = gravity & 6;
        if (i == 0) {
            outRect.left = container.left + (((container.right - container.left) - w) / 2) + xAdj;
            outRect.right = outRect.left + w;
            if ((gravity & 8) == 8) {
                if (outRect.left < container.left) {
                    outRect.left = container.left;
                }
                if (outRect.right > container.right) {
                    outRect.right = container.right;
                }
            }
        } else if (i == 2) {
            outRect.left = container.left + xAdj;
            outRect.right = outRect.left + w;
            if ((gravity & 8) == 8 && outRect.right > container.right) {
                outRect.right = container.right;
            }
        } else if (i != 4) {
            outRect.left = container.left + xAdj;
            outRect.right = container.right + xAdj;
        } else {
            outRect.right = container.right - xAdj;
            outRect.left = outRect.right - w;
            if ((gravity & 8) == 8 && outRect.left < container.left) {
                outRect.left = container.left;
            }
        }
        int i2 = gravity & 96;
        if (i2 == 0) {
            outRect.top = container.top + (((container.bottom - container.top) - h) / 2) + yAdj;
            outRect.bottom = outRect.top + h;
            if ((gravity & 128) == 128) {
                if (outRect.top < container.top) {
                    outRect.top = container.top;
                }
                if (outRect.bottom > container.bottom) {
                    outRect.bottom = container.bottom;
                }
            }
        } else if (i2 == 32) {
            outRect.top = container.top + yAdj;
            outRect.bottom = outRect.top + h;
            if ((gravity & 128) == 128 && outRect.bottom > container.bottom) {
                outRect.bottom = container.bottom;
            }
        } else if (i2 != 64) {
            outRect.top = container.top + yAdj;
            outRect.bottom = container.bottom + yAdj;
        } else {
            outRect.bottom = container.bottom - yAdj;
            outRect.top = outRect.bottom - h;
            if ((gravity & 128) == 128 && outRect.top < container.top) {
                outRect.top = container.top;
            }
        }
    }

    public static void apply(int gravity, int w, int h, Rect container, int xAdj, int yAdj, Rect outRect, int layoutDirection) {
        apply(getAbsoluteGravity(gravity, layoutDirection), w, h, container, xAdj, yAdj, outRect);
    }

    public static void applyDisplay(int gravity, Rect display, Rect inoutObj) {
        if ((268435456 & gravity) != 0) {
            if (inoutObj.top < display.top) {
                inoutObj.top = display.top;
            }
            if (inoutObj.bottom > display.bottom) {
                inoutObj.bottom = display.bottom;
            }
        } else {
            int off = 0;
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
        int off2 = 0;
        if (inoutObj.left < display.left) {
            off2 = display.left - inoutObj.left;
        } else if (inoutObj.right > display.right) {
            off2 = display.right - inoutObj.right;
        }
        if (off2 == 0) {
            return;
        }
        if (inoutObj.width() > display.right - display.left) {
            inoutObj.left = display.left;
            inoutObj.right = display.right;
            return;
        }
        inoutObj.left += off2;
        inoutObj.right += off2;
    }

    public static void applyDisplay(int gravity, Rect display, Rect inoutObj, int layoutDirection) {
        applyDisplay(getAbsoluteGravity(gravity, layoutDirection), display, inoutObj);
    }

    public static boolean isVertical(int gravity) {
        return gravity > 0 && (gravity & 112) != 0;
    }

    public static boolean isHorizontal(int gravity) {
        return gravity > 0 && (8388615 & gravity) != 0;
    }

    public static int getAbsoluteGravity(int gravity, int layoutDirection) {
        int result = gravity;
        if ((8388608 & result) <= 0) {
            return result;
        }
        if ((result & START) == 8388611) {
            int result2 = result & -8388612;
            if (layoutDirection == 1) {
                result = result2 | 5;
            } else {
                result = result2 | 3;
            }
        } else if ((result & END) == 8388613) {
            int result3 = result & -8388614;
            if (layoutDirection == 1) {
                result = result3 | 3;
            } else {
                result = result3 | 5;
            }
        }
        return result & -8388609;
    }

    public static String toString(int gravity) {
        StringBuilder result = new StringBuilder();
        if ((gravity & 119) == 119) {
            result.append("FILL");
            result.append(' ');
        } else {
            if ((gravity & 112) == 112) {
                result.append("FILL_VERTICAL");
                result.append(' ');
            } else {
                if ((gravity & 48) == 48) {
                    result.append("TOP");
                    result.append(' ');
                }
                if ((gravity & 80) == 80) {
                    result.append("BOTTOM");
                    result.append(' ');
                }
            }
            if ((gravity & 7) == 7) {
                result.append("FILL_HORIZONTAL");
                result.append(' ');
            } else {
                if ((gravity & START) == 8388611) {
                    result.append("START");
                    result.append(' ');
                } else if ((gravity & 3) == 3) {
                    result.append("LEFT");
                    result.append(' ');
                }
                if ((gravity & END) == 8388613) {
                    result.append("END");
                    result.append(' ');
                } else if ((gravity & 5) == 5) {
                    result.append("RIGHT");
                    result.append(' ');
                }
            }
        }
        if ((gravity & 17) == 17) {
            result.append("CENTER");
            result.append(' ');
        } else {
            if ((gravity & 16) == 16) {
                result.append("CENTER_VERTICAL");
                result.append(' ');
            }
            if ((gravity & 1) == 1) {
                result.append("CENTER_HORIZONTAL");
                result.append(' ');
            }
        }
        if (result.length() == 0) {
            result.append("NO GRAVITY");
            result.append(' ');
        }
        if ((gravity & 268435456) == 268435456) {
            result.append("DISPLAY_CLIP_VERTICAL");
            result.append(' ');
        }
        if ((gravity & 16777216) == 16777216) {
            result.append("DISPLAY_CLIP_HORIZONTAL");
            result.append(' ');
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}
