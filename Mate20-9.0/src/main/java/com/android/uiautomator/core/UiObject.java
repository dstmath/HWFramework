package com.android.uiautomator.core;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
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

    /* access modifiers changed from: package-private */
    public QueryController getQueryController() {
        return UiDevice.getInstance().getAutomatorBridge().getQueryController();
    }

    /* access modifiers changed from: package-private */
    public InteractionController getInteractionController() {
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

    /* access modifiers changed from: protected */
    public AccessibilityNodeInfo findAccessibilityNodeInfo(long timeout) {
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
        if (scrollableParentNode != null && !nodeRect.intersect(AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(scrollableParentNode, w, h))) {
            return new Rect();
        }
        return nodeRect;
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
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().clickAndSync(rect.centerX(), rect.centerY(), this.mConfig.getActionAcknowledgmentTimeout());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean clickAndWaitForNewWindow() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        return clickAndWaitForNewWindow(WAIT_FOR_WINDOW_TMEOUT);
    }

    public boolean clickAndWaitForNewWindow(long timeout) throws UiObjectNotFoundException {
        Tracer.trace(Long.valueOf(timeout));
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().clickAndWaitForNewWindow(rect.centerX(), rect.centerY(), this.mConfig.getActionAcknowledgmentTimeout());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean clickTopLeft() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().clickNoSync(rect.left + SWIPE_MARGIN_LIMIT, rect.top + SWIPE_MARGIN_LIMIT);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean longClickBottomRight() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().longTapNoSync(rect.right - 5, rect.bottom - 5);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean clickBottomRight() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().clickNoSync(rect.right - 5, rect.bottom - 5);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean longClick() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().longTapNoSync(rect.centerX(), rect.centerY());
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean longClickTopLeft() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            return getInteractionController().longTapNoSync(rect.left + SWIPE_MARGIN_LIMIT, rect.top + SWIPE_MARGIN_LIMIT);
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public String getText() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            String retVal = safeStringReturn(node.getText());
            Log.d(LOG_TAG, String.format("getText() = %s", new Object[]{retVal}));
            return retVal;
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public String getClassName() throws UiObjectNotFoundException {
        Tracer.trace(new Object[0]);
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            String retVal = safeStringReturn(node.getClassName());
            Log.d(LOG_TAG, String.format("getClassName() = %s", new Object[]{retVal}));
            return retVal;
        }
        throw new UiObjectNotFoundException(getSelector().toString());
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
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            getInteractionController().longTapNoSync(rect.left + FINGER_TOUCH_HALF_WIDTH, rect.centerY());
            UiObject selectAll = new UiObject(new UiSelector().descriptionContains("Select all"));
            if (selectAll.waitForExists(50)) {
                selectAll.click();
            }
            SystemClock.sleep(250);
            getInteractionController().sendKey(67, 0);
            return;
        }
        throw new UiObjectNotFoundException(getSelector().toString());
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
        if (node != null) {
            Rect nodeRect = new Rect();
            node.getBoundsInScreen(nodeRect);
            return nodeRect;
        }
        throw new UiObjectNotFoundException(getSelector().toString());
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
        int percent2 = 100;
        if (percent < 0) {
            percent2 = 1;
        } else if (percent <= 100) {
            percent2 = percent;
        }
        float percentage = ((float) percent2) / 100.0f;
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            if (rect.width() > 40) {
                return performTwoPointerGesture(new Point(rect.centerX() - 20, rect.centerY()), new Point(rect.centerX() + FINGER_TOUCH_HALF_WIDTH, rect.centerY()), new Point(rect.centerX() - ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() + ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), steps);
            }
            throw new IllegalStateException("Object width is too small for operation");
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean pinchIn(int percent, int steps) throws UiObjectNotFoundException {
        int percent2 = 100;
        if (percent < 0) {
            percent2 = 0;
        } else if (percent <= 100) {
            percent2 = percent;
        }
        float percentage = ((float) percent2) / 100.0f;
        AccessibilityNodeInfo node = findAccessibilityNodeInfo(this.mConfig.getWaitForSelectorTimeout());
        if (node != null) {
            Rect rect = getVisibleBounds(node);
            if (rect.width() > 40) {
                return performTwoPointerGesture(new Point(rect.centerX() - ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() + ((int) (((float) (rect.width() / 2)) * percentage)), rect.centerY()), new Point(rect.centerX() - 20, rect.centerY()), new Point(rect.centerX() + FINGER_TOUCH_HALF_WIDTH, rect.centerY()), steps);
            }
            throw new IllegalStateException("Object width is too small for operation");
        }
        throw new UiObjectNotFoundException(getSelector().toString());
    }

    public boolean performTwoPointerGesture(Point startPoint1, Point startPoint2, Point endPoint1, Point endPoint2, int steps) {
        int steps2;
        Point point = startPoint1;
        Point point2 = startPoint2;
        Point point3 = endPoint1;
        Point point4 = endPoint2;
        if (steps == 0) {
            steps2 = 1;
        } else {
            steps2 = steps;
        }
        float stepX1 = (float) ((point3.x - point.x) / steps2);
        float stepY1 = (float) ((point3.y - point.y) / steps2);
        float stepX2 = (float) ((point4.x - point2.x) / steps2);
        float stepY2 = (float) ((point4.y - point2.y) / steps2);
        int eventX1 = point.x;
        int eventY1 = point.y;
        int eventX2 = point2.x;
        MotionEvent.PointerCoords[] points1 = new MotionEvent.PointerCoords[(steps2 + 2)];
        MotionEvent.PointerCoords[] points2 = new MotionEvent.PointerCoords[(steps2 + 2)];
        int eventY2 = point2.y;
        int eventX22 = eventX2;
        int eventY12 = eventY1;
        int eventX12 = eventX1;
        int i = 0;
        while (i < steps2 + 1) {
            MotionEvent.PointerCoords p1 = new MotionEvent.PointerCoords();
            p1.x = (float) eventX12;
            p1.y = (float) eventY12;
            p1.pressure = 1.0f;
            p1.size = 1.0f;
            points1[i] = p1;
            MotionEvent.PointerCoords p2 = new MotionEvent.PointerCoords();
            MotionEvent.PointerCoords pointerCoords = p1;
            p2.x = (float) eventX22;
            p2.y = (float) eventY2;
            p2.pressure = 1.0f;
            p2.size = 1.0f;
            points2[i] = p2;
            eventX12 = (int) (((float) eventX12) + stepX1);
            eventY12 = (int) (((float) eventY12) + stepY1);
            eventX22 = (int) (((float) eventX22) + stepX2);
            eventY2 = (int) (((float) eventY2) + stepY2);
            i++;
            Point point5 = startPoint1;
            Point point6 = startPoint2;
        }
        MotionEvent.PointerCoords p12 = new MotionEvent.PointerCoords();
        p12.x = (float) point3.x;
        p12.y = (float) point3.y;
        p12.pressure = 1.0f;
        p12.size = 1.0f;
        points1[steps2 + 1] = p12;
        MotionEvent.PointerCoords p22 = new MotionEvent.PointerCoords();
        p22.x = (float) point4.x;
        p22.y = (float) point4.y;
        p22.pressure = 1.0f;
        p22.size = 1.0f;
        points2[steps2 + 1] = p22;
        MotionEvent.PointerCoords pointerCoords2 = p12;
        return performMultiPointerGesture(points1, points2);
    }

    public boolean performMultiPointerGesture(MotionEvent.PointerCoords[]... touches) {
        return getInteractionController().performMultiPointerGesture(touches);
    }
}
