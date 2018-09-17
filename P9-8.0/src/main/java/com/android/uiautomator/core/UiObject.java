package com.android.uiautomator.core;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent.PointerCoords;
import android.view.accessibility.AccessibilityNodeInfo;

@Deprecated
public class UiObject {
    protected static final int FINGER_TOUCH_HALF_WIDTH = 20;
    private static final String LOG_TAG = UiObject.class.getSimpleName();
    protected static final int SWIPE_MARGIN_LIMIT = 5;
    @Deprecated
    protected static final long WAIT_FOR_EVENT_TMEOUT = 3000;
    protected static final long WAIT_FOR_SELECTOR_POLL = 1000;
    @Deprecated
    protected static final long WAIT_FOR_SELECTOR_TIMEOUT = 10000;
    protected static final long WAIT_FOR_WINDOW_TMEOUT = 5500;
    private final Configurator mConfig = Configurator.getInstance();
    private final UiSelector mSelector;

    public UiObject(UiSelector selector) {
        this.mSelector = selector;
    }

    public final UiSelector getSelector() {
        Tracer.trace(new Object[0]);
        return new UiSelector(this.mSelector);
    }

    QueryController getQueryController() {
        return UiDevice.getInstance().getAutomatorBridge().getQueryController();
    }

    InteractionController getInteractionController() {
        return UiDevice.getInstance().getAutomatorBridge().getInteractionController();
    }

    public UiObject getChild(UiSelector selector) throws UiObjectNotFoundException {
        Tracer.trace(selector);
        return new UiObject(getSelector().childSelector(selector));
    }

    public UiObject getFromParent(UiSelector selector) throws UiObjectNotFoundException {
        Tracer.trace(selector);
        return new UiObject(getSelector().fromParent(selector));
    }

    public int getChildCount() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.getChildCount();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    protected AccessibilityNodeInfo findAccessibilityNodeInfo(long timeout) {
        AccessibilityNodeInfo node = null;
        long startMills = SystemClock.uptimeMillis();
        long currentMills = 0;
        while (currentMills <= timeout) {
            node = getQueryController().findAccessibilityNodeInfo(getSelector());
            if (node != null) {
                break;
            }
            UiDevice.getInstance().runWatchers();
            currentMills = SystemClock.uptimeMillis() - startMills;
            if (timeout > 0) {
                SystemClock.sleep(WAIT_FOR_SELECTOR_POLL);
            }
        }
        return node;
    }

    public boolean dragTo(UiObject destObj, int steps) throws UiObjectNotFoundException {
        Rect srcRect = getVisibleBounds();
        Rect dstRect = destObj.getVisibleBounds();
        return getInteractionController().swipe(srcRect.centerX(), srcRect.centerY(), dstRect.centerX(), dstRect.centerY(), steps, true);
    }

    public boolean dragTo(int destX, int destY, int steps) throws UiObjectNotFoundException {
        Rect srcRect = getVisibleBounds();
        return getInteractionController().swipe(srcRect.centerX(), srcRect.centerY(), destX, destY, steps, true);
    }

    public boolean swipeUp(int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(steps));
        Rect rect = getVisibleBounds();
        if (rect.height() <= 10) {
            return false;
        }
        return getInteractionController().swipe(rect.centerX(), rect.bottom - 5, rect.centerX(), rect.top + SWIPE_MARGIN_LIMIT, steps);
    }

    public boolean swipeDown(int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(steps));
        Rect rect = getVisibleBounds();
        if (rect.height() <= 10) {
            return false;
        }
        return getInteractionController().swipe(rect.centerX(), rect.top + SWIPE_MARGIN_LIMIT, rect.centerX(), rect.bottom - 5, steps);
    }

    public boolean swipeLeft(int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(steps));
        Rect rect = getVisibleBounds();
        if (rect.width() <= 10) {
            return false;
        }
        return getInteractionController().swipe(rect.right - 5, rect.centerY(), rect.left + SWIPE_MARGIN_LIMIT, rect.centerY(), steps);
    }

    public boolean swipeRight(int steps) throws UiObjectNotFoundException {
        Tracer.trace(Integer.valueOf(steps));
        Rect rect = getVisibleBounds();
        if (rect.width() <= 10) {
            return false;
        }
        return getInteractionController().swipe(rect.left + SWIPE_MARGIN_LIMIT, rect.centerY(), rect.right - 5, rect.centerY(), steps);
    }

    private Rect getVisibleBounds(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }
        int w = UiDevice.getInstance().getDisplayWidth();
        int h = UiDevice.getInstance().getDisplayHeight();
        Rect nodeRect = AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(node, w, h);
        AccessibilityNodeInfo scrollableParentNode = getScrollableParent(node);
        if (scrollableParentNode == null || nodeRect.intersect(AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(scrollableParentNode, w, h))) {
            return nodeRect;
        }
        return new Rect();
    }

    private AccessibilityNodeInfo getScrollableParent(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node;
        while (parent != null) {
            parent = parent.getParent();
            if (parent != null && parent.isScrollable()) {
                return parent;
            }
        }
        return null;
    }

    public boolean click() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        return getInteractionController().clickAndSync(rect.centerX(), rect.centerY(), this.mConfig.getActionAcknowledgmentTimeout());
    }

    public boolean clickAndWaitForNewWindow() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        return clickAndWaitForNewWindow(WAIT_FOR_WINDOW_TMEOUT);
    }

    public boolean clickAndWaitForNewWindow(long timeout) throws UiObjectNotFoundException {
        Tracer.trace(Long.valueOf(timeout));
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        return getInteractionController().clickAndWaitForNewWindow(rect.centerX(), rect.centerY(), this.mConfig.getActionAcknowledgmentTimeout());
    }

    public boolean clickTopLeft() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        return getInteractionController().clickNoSync(rect.left + SWIPE_MARGIN_LIMIT, rect.top + SWIPE_MARGIN_LIMIT);
    }

    public boolean longClickBottomRight() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        return getInteractionController().longTapNoSync(rect.right - 5, rect.bottom - 5);
    }

    public boolean clickBottomRight() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        return getInteractionController().clickNoSync(rect.right - 5, rect.bottom - 5);
    }

    public boolean longClick() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        return getInteractionController().longTapNoSync(rect.centerX(), rect.centerY());
    }

    public boolean longClickTopLeft() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        return getInteractionController().longTapNoSync(rect.left + SWIPE_MARGIN_LIMIT, rect.top + SWIPE_MARGIN_LIMIT);
    }

    public String getText() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        String retVal = safeStringReturn(node.getText());
        Log.d(LOG_TAG, String.format("getText() = %s", new Object[]{retVal}));
        return retVal;
    }

    public String getClassName() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        String retVal = safeStringReturn(node.getClassName());
        Log.d(LOG_TAG, String.format("getClassName() = %s", new Object[]{retVal}));
        return retVal;
    }

    public String getContentDescription() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return safeStringReturn(node.getContentDescription());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean setText(String text) throws UiObjectNotFoundException {
        Tracer.trace(text);
        clearTextField();
        return getInteractionController().sendText(text);
    }

    public void clearTextField() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        getInteractionController().longTapNoSync(rect.left + FINGER_TOUCH_HALF_WIDTH, rect.centerY());
        UiObject selectAll = new UiObject(new UiSelector().descriptionContains("Select all"));
        if (selectAll.waitForExists(50)) {
            selectAll.click();
        }
        SystemClock.sleep(250);
        getInteractionController().sendKey(67, 0);
    }

    public boolean isChecked() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isChecked();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isSelected() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isSelected();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isCheckable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isCheckable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isEnabled() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isEnabled();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isClickable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isClickable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isFocused() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isFocused();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isFocusable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isFocusable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isScrollable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isScrollable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean isLongClickable() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return node.isLongClickable();
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public String getPackageName() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return safeStringReturn(node.getPackageName());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public Rect getVisibleBounds() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            return getVisibleBounds(node);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public Rect getBounds() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect nodeRect = new Rect();
        node.getBoundsInScreen(nodeRect);
        return nodeRect;
    }

    public boolean waitForExists(long timeout) {
        Tracer.trace(Long.valueOf(timeout));
        return findAccessibilityNodeInfo(timeout) != null;
    }

    public boolean waitUntilGone(long timeout) {
        Tracer.trace(Long.valueOf(timeout));
        long startMills = SystemClock.uptimeMillis();
        long currentMills = 0;
        while (currentMills <= timeout) {
            if (findAccessibilityNodeInfo(0) == null) {
                return true;
            }
            currentMills = SystemClock.uptimeMillis() - startMills;
            if (timeout > 0) {
                SystemClock.sleep(WAIT_FOR_SELECTOR_POLL);
            }
        }
        return false;
    }

    public boolean exists() {
        Tracer.trace(new Object[0]);
        return waitForExists(0);
    }

    private String safeStringReturn(CharSequence cs) {
        if (cs == null) {
            return "";
        }
        return cs.toString();
    }

    public boolean pinchOut(int percent, int steps) throws UiObjectNotFoundException {
        if (percent < 0) {
            percent = 1;
        } else if (percent > 100) {
            percent = 100;
        }
        float percentage = ((float) percent) / 100.0f;
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        if (rect.width() <= 40) {
            throw new IllegalStateException("Object width is too small for operation");
        }
        return performTwoPointerGesture(new Point(rect.centerX() - 20, rect.centerY()), new Point(rect.centerX() + FINGER_TOUCH_HALF_WIDTH, rect.centerY()), new Point(rect.centerX() - ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() + ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), steps);
    }

    public boolean pinchIn(int percent, int steps) throws UiObjectNotFoundException {
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        float percentage = ((float) percent) / 100.0f;
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node == null) {
            throw new UiObjectNotFoundException(getSelector().toString());
        }
        Rect rect = getVisibleBounds(node);
        if (rect.width() <= 40) {
            throw new IllegalStateException("Object width is too small for operation");
        }
        return performTwoPointerGesture(new Point(rect.centerX() - ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() + ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() - 20, rect.centerY()), new Point(rect.centerX() + FINGER_TOUCH_HALF_WIDTH, rect.centerY()), steps);
    }

    public boolean performTwoPointerGesture(Point startPoint1, Point startPoint2, Point endPoint1, Point endPoint2, int steps) {
        PointerCoords p1;
        PointerCoords p2;
        if (steps == 0) {
            steps = 1;
        }
        float stepX1 = (float) ((endPoint1.x - startPoint1.x) / steps);
        float stepY1 = (float) ((endPoint1.y - startPoint1.y) / steps);
        float stepX2 = (float) ((endPoint2.x - startPoint2.x) / steps);
        float stepY2 = (float) ((endPoint2.y - startPoint2.y) / steps);
        int eventX1 = startPoint1.x;
        int eventY1 = startPoint1.y;
        int eventX2 = startPoint2.x;
        int eventY2 = startPoint2.y;
        PointerCoords[] points1 = new PointerCoords[(steps + 2)];
        PointerCoords[] points2 = new PointerCoords[(steps + 2)];
        for (int i = 0; i < steps + 1; i++) {
            p1 = new PointerCoords();
            p1.x = (float) eventX1;
            p1.y = (float) eventY1;
            p1.pressure = 1.0f;
            p1.size = 1.0f;
            points1[i] = p1;
            p2 = new PointerCoords();
            p2.x = (float) eventX2;
            p2.y = (float) eventY2;
            p2.pressure = 1.0f;
            p2.size = 1.0f;
            points2[i] = p2;
            eventX1 = (int) (((float) eventX1) + stepX1);
            eventY1 = (int) (((float) eventY1) + stepY1);
            eventX2 = (int) (((float) eventX2) + stepX2);
            eventY2 = (int) (((float) eventY2) + stepY2);
        }
        p1 = new PointerCoords();
        p1.x = (float) endPoint1.x;
        p1.y = (float) endPoint1.y;
        p1.pressure = 1.0f;
        p1.size = 1.0f;
        points1[steps + 1] = p1;
        p2 = new PointerCoords();
        p2.x = (float) endPoint2.x;
        p2.y = (float) endPoint2.y;
        p2.pressure = 1.0f;
        p2.size = 1.0f;
        points2[steps + 1] = p2;
        return performMultiPointerGesture(points1, points2);
    }

    public boolean performMultiPointerGesture(PointerCoords[]... touches) {
        return getInteractionController().performMultiPointerGesture(touches);
    }
}
