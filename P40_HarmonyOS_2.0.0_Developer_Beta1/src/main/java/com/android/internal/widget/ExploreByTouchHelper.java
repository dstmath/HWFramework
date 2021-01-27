package com.android.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.IntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

public abstract class ExploreByTouchHelper extends View.AccessibilityDelegate {
    private static final String DEFAULT_CLASS_NAME = View.class.getName();
    public static final int HOST_ID = -1;
    public static final int INVALID_ID = Integer.MIN_VALUE;
    private static final Rect INVALID_PARENT_BOUNDS = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    private final Context mContext;
    private int mFocusedVirtualViewId = Integer.MIN_VALUE;
    private int mHoveredVirtualViewId = Integer.MIN_VALUE;
    private final AccessibilityManager mManager;
    private ExploreByTouchNodeProvider mNodeProvider;
    private IntArray mTempArray;
    private int[] mTempGlobalRect;
    private Rect mTempParentRect;
    private Rect mTempScreenRect;
    private Rect mTempVisibleRect;
    private final View mView;

    /* access modifiers changed from: protected */
    public abstract int getVirtualViewAt(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract void getVisibleVirtualViews(IntArray intArray);

    /* access modifiers changed from: protected */
    public abstract boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle);

    /* access modifiers changed from: protected */
    public abstract void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent);

    /* access modifiers changed from: protected */
    public abstract void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfo accessibilityNodeInfo);

    public ExploreByTouchHelper(View forView) {
        if (forView != null) {
            this.mView = forView;
            this.mContext = forView.getContext();
            this.mManager = (AccessibilityManager) this.mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
            return;
        }
        throw new IllegalArgumentException("View may not be null");
    }

    @Override // android.view.View.AccessibilityDelegate
    public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
        if (this.mNodeProvider == null) {
            this.mNodeProvider = new ExploreByTouchNodeProvider();
        }
        return this.mNodeProvider;
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        if (!this.mManager.isEnabled() || !this.mManager.isTouchExplorationEnabled()) {
            return false;
        }
        int action = event.getAction();
        if (action == 7 || action == 9) {
            int virtualViewId = getVirtualViewAt(event.getX(), event.getY());
            updateHoveredVirtualView(virtualViewId);
            if (virtualViewId != Integer.MIN_VALUE) {
                return true;
            }
            return false;
        } else if (action != 10 || this.mHoveredVirtualViewId == Integer.MIN_VALUE) {
            return false;
        } else {
            updateHoveredVirtualView(Integer.MIN_VALUE);
            return true;
        }
    }

    public boolean sendEventForVirtualView(int virtualViewId, int eventType) {
        ViewParent parent;
        if (virtualViewId == Integer.MIN_VALUE || !this.mManager.isEnabled() || (parent = this.mView.getParent()) == null) {
            return false;
        }
        return parent.requestSendAccessibilityEvent(this.mView, createEvent(virtualViewId, eventType));
    }

    public void invalidateRoot() {
        invalidateVirtualView(-1, 1);
    }

    public void invalidateVirtualView(int virtualViewId) {
        invalidateVirtualView(virtualViewId, 0);
    }

    public void invalidateVirtualView(int virtualViewId, int changeTypes) {
        ViewParent parent;
        if (virtualViewId != Integer.MIN_VALUE && this.mManager.isEnabled() && (parent = this.mView.getParent()) != null) {
            AccessibilityEvent event = createEvent(virtualViewId, 2048);
            event.setContentChangeTypes(changeTypes);
            parent.requestSendAccessibilityEvent(this.mView, event);
        }
    }

    public int getFocusedVirtualView() {
        return this.mFocusedVirtualViewId;
    }

    private void updateHoveredVirtualView(int virtualViewId) {
        if (this.mHoveredVirtualViewId != virtualViewId) {
            int previousVirtualViewId = this.mHoveredVirtualViewId;
            this.mHoveredVirtualViewId = virtualViewId;
            sendEventForVirtualView(virtualViewId, 128);
            sendEventForVirtualView(previousVirtualViewId, 256);
        }
    }

    private AccessibilityEvent createEvent(int virtualViewId, int eventType) {
        if (virtualViewId != -1) {
            return createEventForChild(virtualViewId, eventType);
        }
        return createEventForHost(eventType);
    }

    private AccessibilityEvent createEventForHost(int eventType) {
        AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        this.mView.onInitializeAccessibilityEvent(event);
        onPopulateEventForHost(event);
        return event;
    }

    private AccessibilityEvent createEventForChild(int virtualViewId, int eventType) {
        AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        event.setEnabled(true);
        event.setClassName(DEFAULT_CLASS_NAME);
        onPopulateEventForVirtualView(virtualViewId, event);
        if (!event.getText().isEmpty() || event.getContentDescription() != null) {
            event.setPackageName(this.mView.getContext().getPackageName());
            event.setSource(this.mView, virtualViewId);
            return event;
        }
        throw new RuntimeException("Callbacks must add text or a content description in populateEventForVirtualViewId()");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AccessibilityNodeInfo createNode(int virtualViewId) {
        if (virtualViewId != -1) {
            return createNodeForChild(virtualViewId);
        }
        return createNodeForHost();
    }

    private AccessibilityNodeInfo createNodeForHost() {
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(this.mView);
        this.mView.onInitializeAccessibilityNodeInfo(node);
        int realNodeCount = node.getChildCount();
        onPopulateNodeForHost(node);
        IntArray intArray = this.mTempArray;
        if (intArray == null) {
            this.mTempArray = new IntArray();
        } else {
            intArray.clear();
        }
        IntArray virtualViewIds = this.mTempArray;
        getVisibleVirtualViews(virtualViewIds);
        if (realNodeCount <= 0 || virtualViewIds.size() <= 0) {
            int N = virtualViewIds.size();
            for (int i = 0; i < N; i++) {
                node.addChild(this.mView, virtualViewIds.get(i));
            }
            return node;
        }
        throw new RuntimeException("Views cannot have both real and virtual children");
    }

    private AccessibilityNodeInfo createNodeForChild(int virtualViewId) {
        ensureTempRects();
        Rect tempParentRect = this.mTempParentRect;
        int[] tempGlobalRect = this.mTempGlobalRect;
        Rect tempScreenRect = this.mTempScreenRect;
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
        node.setEnabled(true);
        node.setClassName(DEFAULT_CLASS_NAME);
        node.setBoundsInParent(INVALID_PARENT_BOUNDS);
        onPopulateNodeForVirtualView(virtualViewId, node);
        if (node.getText() == null && node.getContentDescription() == null) {
            throw new RuntimeException("Callbacks must add text or a content description in populateNodeForVirtualViewId()");
        }
        node.getBoundsInParent(tempParentRect);
        if (!tempParentRect.equals(INVALID_PARENT_BOUNDS)) {
            int actions = node.getActions();
            if ((actions & 64) != 0) {
                throw new RuntimeException("Callbacks must not add ACTION_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
            } else if ((actions & 128) == 0) {
                node.setPackageName(this.mView.getContext().getPackageName());
                node.setSource(this.mView, virtualViewId);
                node.setParent(this.mView);
                if (this.mFocusedVirtualViewId == virtualViewId) {
                    node.setAccessibilityFocused(true);
                    node.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                } else {
                    node.setAccessibilityFocused(false);
                    node.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
                }
                if (intersectVisibleToUser(tempParentRect)) {
                    node.setVisibleToUser(true);
                    node.setBoundsInParent(tempParentRect);
                }
                this.mView.getLocationOnScreen(tempGlobalRect);
                int offsetX = tempGlobalRect[0];
                int offsetY = tempGlobalRect[1];
                tempScreenRect.set(tempParentRect);
                tempScreenRect.offset(offsetX, offsetY);
                node.setBoundsInScreen(tempScreenRect);
                return node;
            } else {
                throw new RuntimeException("Callbacks must not add ACTION_CLEAR_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
            }
        } else {
            throw new RuntimeException("Callbacks must set parent bounds in populateNodeForVirtualViewId()");
        }
    }

    private void ensureTempRects() {
        this.mTempGlobalRect = new int[2];
        this.mTempParentRect = new Rect();
        this.mTempScreenRect = new Rect();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean performAction(int virtualViewId, int action, Bundle arguments) {
        if (virtualViewId != -1) {
            return performActionForChild(virtualViewId, action, arguments);
        }
        return performActionForHost(action, arguments);
    }

    private boolean performActionForHost(int action, Bundle arguments) {
        return this.mView.performAccessibilityAction(action, arguments);
    }

    private boolean performActionForChild(int virtualViewId, int action, Bundle arguments) {
        if (action == 64 || action == 128) {
            return manageFocusForChild(virtualViewId, action);
        }
        return onPerformActionForVirtualView(virtualViewId, action, arguments);
    }

    private boolean manageFocusForChild(int virtualViewId, int action) {
        if (action == 64) {
            return requestAccessibilityFocus(virtualViewId);
        }
        if (action != 128) {
            return false;
        }
        return clearAccessibilityFocus(virtualViewId);
    }

    private boolean intersectVisibleToUser(Rect localRect) {
        if (localRect == null || localRect.isEmpty() || this.mView.getWindowVisibility() != 0) {
            return false;
        }
        ViewParent viewParent = this.mView.getParent();
        while (viewParent instanceof View) {
            View view = (View) viewParent;
            if (view.getAlpha() <= 0.0f || view.getVisibility() != 0) {
                return false;
            }
            viewParent = view.getParent();
        }
        if (viewParent == null) {
            return false;
        }
        if (this.mTempVisibleRect == null) {
            this.mTempVisibleRect = new Rect();
        }
        Rect tempVisibleRect = this.mTempVisibleRect;
        if (!this.mView.getLocalVisibleRect(tempVisibleRect)) {
            return false;
        }
        return localRect.intersect(tempVisibleRect);
    }

    private boolean isAccessibilityFocused(int virtualViewId) {
        return this.mFocusedVirtualViewId == virtualViewId;
    }

    private boolean requestAccessibilityFocus(int virtualViewId) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) this.mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!this.mManager.isEnabled() || !accessibilityManager.isTouchExplorationEnabled() || isAccessibilityFocused(virtualViewId)) {
            return false;
        }
        int i = this.mFocusedVirtualViewId;
        if (i != Integer.MIN_VALUE) {
            sendEventForVirtualView(i, 65536);
        }
        this.mFocusedVirtualViewId = virtualViewId;
        this.mView.invalidate();
        sendEventForVirtualView(virtualViewId, 32768);
        return true;
    }

    private boolean clearAccessibilityFocus(int virtualViewId) {
        if (!isAccessibilityFocused(virtualViewId)) {
            return false;
        }
        this.mFocusedVirtualViewId = Integer.MIN_VALUE;
        this.mView.invalidate();
        sendEventForVirtualView(virtualViewId, 65536);
        return true;
    }

    /* access modifiers changed from: protected */
    public void onPopulateEventForHost(AccessibilityEvent event) {
    }

    /* access modifiers changed from: protected */
    public void onPopulateNodeForHost(AccessibilityNodeInfo node) {
    }

    private class ExploreByTouchNodeProvider extends AccessibilityNodeProvider {
        private ExploreByTouchNodeProvider() {
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            return ExploreByTouchHelper.this.createNode(virtualViewId);
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            return ExploreByTouchHelper.this.performAction(virtualViewId, action, arguments);
        }
    }
}
