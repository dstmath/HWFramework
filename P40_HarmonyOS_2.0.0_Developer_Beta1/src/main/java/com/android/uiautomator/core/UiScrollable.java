package com.android.uiautomator.core;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

@Deprecated
public class UiScrollable extends UiCollection {
    private static final double DEFAULT_SWIPE_DEADZONE_PCT = 0.1d;
    private static final int FLING_STEPS = 5;
    private static final String LOG_TAG = UiScrollable.class.getSimpleName();
    private static final int SCROLL_STEPS = 55;
    private static int mMaxSearchSwipes = 30;
    private boolean mIsVerticalList = true;
    private double mSwipeDeadZonePercentage = DEFAULT_SWIPE_DEADZONE_PCT;

    public UiScrollable(UiSelector container) {
        super(container);
    }

    public UiScrollable setAsVerticalList() {
        Tracer.trace(new Object[0]);
        this.mIsVerticalList = true;
        return this;
    }

    public UiScrollable setAsHorizontalList() {
        Tracer.trace(new Object[0]);
        this.mIsVerticalList = false;
        return this;
    }

    /* access modifiers changed from: protected */
    public boolean exists(UiSelector selector) {
        if (getQueryController().findAccessibilityNodeInfo(selector) != null) {
            return true;
        }
        return false;
    }

    @Override // com.android.uiautomator.core.UiCollection
    public UiObject getChildByDescription(UiSelector childPattern, String text) throws UiObjectNotFoundException {
        Tracer.trace(childPattern, text);
        return getChildByDescription(childPattern, text, true);
    }

    public UiObject getChildByDescription(UiSelector childPattern, String text, boolean allowScrollSearch) throws UiObjectNotFoundException {
        Tracer.trace(childPattern, text, Boolean.valueOf(allowScrollSearch));
        if (text != null) {
            if (allowScrollSearch) {
                scrollIntoView(new UiSelector().descriptionContains(text));
            }
            return super.getChildByDescription(childPattern, text);
        }
        throw new UiObjectNotFoundException("for description= \"" + text + "\"");
    }

    @Override // com.android.uiautomator.core.UiCollection
    public UiObject getChildByInstance(UiSelector childPattern, int instance) throws UiObjectNotFoundException {
        Tracer.trace(childPattern, Integer.valueOf(instance));
        return new UiObject(UiSelector.patternBuilder(getSelector(), UiSelector.patternBuilder(childPattern).instance(instance)));
    }

    @Override // com.android.uiautomator.core.UiCollection
    public UiObject getChildByText(UiSelector childPattern, String text) throws UiObjectNotFoundException {
        Tracer.trace(childPattern, text);
        return getChildByText(childPattern, text, true);
    }

    public UiObject getChildByText(UiSelector childPattern, String text, boolean allowScrollSearch) throws UiObjectNotFoundException {
        Tracer.trace(childPattern, text, Boolean.valueOf(allowScrollSearch));
        if (text != null) {
            if (allowScrollSearch) {
                scrollIntoView(new UiSelector().text(text));
            }
            return super.getChildByText(childPattern, text);
        }
        throw new UiObjectNotFoundException("for text= \"" + text + "\"");
    }

    public boolean scrollDescriptionIntoView(String text) throws UiObjectNotFoundException {
        Tracer.trace(text);
        return scrollIntoView(new UiSelector().description(text));
    }

    public boolean scrollIntoView(UiObject obj) throws UiObjectNotFoundException {
        Tracer.trace(obj.getSelector());
        return scrollIntoView(obj.getSelector());
    }

    public boolean scrollIntoView(UiSelector selector) throws UiObjectNotFoundException {
        Tracer.trace(selector);
        UiSelector childSelector = getSelector().childSelector(selector);
        if (exists(childSelector)) {
            return true;
        }
        scrollToBeginning(mMaxSearchSwipes);
        if (exists(childSelector)) {
            return true;
        }
        for (int x = 0; x < mMaxSearchSwipes; x++) {
            boolean scrolled = scrollForward();
            if (exists(childSelector)) {
                return true;
            }
            if (!scrolled) {
                return false;
            }
        }
        return false;
    }

    public boolean ensureFullyVisible(UiObject childObject) throws UiObjectNotFoundException {
        boolean shouldSwipeForward;
        Rect actual = childObject.getBounds();
        Rect visible = childObject.getVisibleBounds();
        boolean z = true;
        if (visible.width() * visible.height() == actual.width() * actual.height()) {
            return true;
        }
        if (this.mIsVerticalList) {
            if (actual.top != visible.top) {
                z = false;
            }
            shouldSwipeForward = z;
        } else {
            if (actual.left != visible.left) {
                z = false;
            }
            shouldSwipeForward = z;
        }
        if (this.mIsVerticalList) {
            if (shouldSwipeForward) {
                return swipeUp(10);
            }
            return swipeDown(10);
        } else if (shouldSwipeForward) {
            return swipeLeft(10);
        } else {
            return swipeRight(10);
        }
    }

    public boolean scrollTextIntoView(String text) throws UiObjectNotFoundException {
        Tracer.trace(text);
        return scrollIntoView(new UiSelector().text(text));
    }

    public UiScrollable setMaxSearchSwipes(int swipes) {
        Tracer.trace(Integer.valueOf(swipes));
        mMaxSearchSwipes = swipes;
        return this;
    }

    public int getMaxSearchSwipes() {
        Tracer.trace(new Object[0]);
        return mMaxSearchSwipes;
    }

    public boolean flingForward() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        return scrollForward(FLING_STEPS);
    }

    public boolean scrollForward() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        return scrollForward(SCROLL_STEPS);
    }

    public boolean scrollForward(int steps) throws UiObjectNotFoundException {
        int downY;
        int upY;
        int downY2;
        int downX;
        Tracer.trace(Integer.valueOf(steps));
        String str = LOG_TAG;
        Log.d(str, "scrollForward() on selector = " + getSelector());
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(10000);
        if (node != null) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            if (this.mIsVerticalList) {
                int swipeAreaAdjust = (int) (((double) rect.height()) * getSwipeDeadZonePercentage());
                downX = rect.centerX();
                downY = rect.bottom - swipeAreaAdjust;
                downY2 = rect.centerX();
                upY = rect.top + swipeAreaAdjust;
            } else {
                int swipeAreaAdjust2 = (int) (((double) rect.width()) * getSwipeDeadZonePercentage());
                downX = rect.right - swipeAreaAdjust2;
                downY = rect.centerY();
                downY2 = rect.left + swipeAreaAdjust2;
                upY = rect.centerY();
            }
            return getInteractionController().scrollSwipe(downX, downY, downY2, upY, steps);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean flingBackward() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        return scrollBackward(FLING_STEPS);
    }

    public boolean scrollBackward() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        return scrollBackward(SCROLL_STEPS);
    }

    public boolean scrollBackward(int steps) throws UiObjectNotFoundException {
        int downY;
        int upY;
        int downY2;
        int downX;
        Tracer.trace(Integer.valueOf(steps));
        String str = LOG_TAG;
        Log.d(str, "scrollBackward() on selector = " + getSelector());
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(10000);
        if (node != null) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            if (this.mIsVerticalList) {
                int swipeAreaAdjust = (int) (((double) rect.height()) * getSwipeDeadZonePercentage());
                Log.d(LOG_TAG, "scrollToBegining() using vertical scroll");
                downX = rect.centerX();
                downY = rect.top + swipeAreaAdjust;
                downY2 = rect.centerX();
                upY = rect.bottom - swipeAreaAdjust;
            } else {
                int swipeAreaAdjust2 = (int) (((double) rect.width()) * getSwipeDeadZonePercentage());
                Log.d(LOG_TAG, "scrollToBegining() using hotizontal scroll");
                downX = rect.left + swipeAreaAdjust2;
                downY = rect.centerY();
                downY2 = rect.right - swipeAreaAdjust2;
                upY = rect.centerY();
            }
            return getInteractionController().scrollSwipe(downX, downY, downY2, upY, steps);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean scrollToBeginning(int maxSwipes, int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(maxSwipes), Integer.valueOf(steps));
        String str = LOG_TAG;
        Log.d(str, "scrollToBeginning() on selector = " + getSelector());
        for (int x = 0; x < maxSwipes && scrollBackward(steps); x++) {
        }
        return true;
    }

    public boolean scrollToBeginning(int maxSwipes) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(maxSwipes));
        return scrollToBeginning(maxSwipes, SCROLL_STEPS);
    }

    public boolean flingToBeginning(int maxSwipes) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(maxSwipes));
        return scrollToBeginning(maxSwipes, FLING_STEPS);
    }

    public boolean scrollToEnd(int maxSwipes, int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(maxSwipes), Integer.valueOf(steps));
        for (int x = 0; x < maxSwipes && scrollForward(steps); x++) {
        }
        return true;
    }

    public boolean scrollToEnd(int maxSwipes) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(maxSwipes));
        return scrollToEnd(maxSwipes, SCROLL_STEPS);
    }

    public boolean flingToEnd(int maxSwipes) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(maxSwipes));
        return scrollToEnd(maxSwipes, FLING_STEPS);
    }

    public double getSwipeDeadZonePercentage() {
        Tracer.trace(new Object[0]);
        return this.mSwipeDeadZonePercentage;
    }

    public UiScrollable setSwipeDeadZonePercentage(double swipeDeadZonePercentage) {
        Tracer.trace(Double.valueOf(swipeDeadZonePercentage));
        this.mSwipeDeadZonePercentage = swipeDeadZonePercentage;
        return this;
    }
}
