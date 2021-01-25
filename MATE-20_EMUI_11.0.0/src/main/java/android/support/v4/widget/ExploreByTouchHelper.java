package android.support.v4.widget;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.subtitle.Cea708CCParser;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewParentCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeProviderCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v4.widget.FocusStrategy;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;
import java.util.List;

public abstract class ExploreByTouchHelper extends AccessibilityDelegateCompat {
    private static final String DEFAULT_CLASS_NAME = "android.view.View";
    public static final int HOST_ID = -1;
    public static final int INVALID_ID = Integer.MIN_VALUE;
    private static final Rect INVALID_PARENT_BOUNDS = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    private static final FocusStrategy.BoundsAdapter<AccessibilityNodeInfoCompat> NODE_ADAPTER = new FocusStrategy.BoundsAdapter<AccessibilityNodeInfoCompat>() {
        /* class android.support.v4.widget.ExploreByTouchHelper.AnonymousClass1 */

        public void obtainBounds(AccessibilityNodeInfoCompat node, Rect outBounds) {
            node.getBoundsInParent(outBounds);
        }
    };
    private static final FocusStrategy.CollectionAdapter<SparseArrayCompat<AccessibilityNodeInfoCompat>, AccessibilityNodeInfoCompat> SPARSE_VALUES_ADAPTER = new FocusStrategy.CollectionAdapter<SparseArrayCompat<AccessibilityNodeInfoCompat>, AccessibilityNodeInfoCompat>() {
        /* class android.support.v4.widget.ExploreByTouchHelper.AnonymousClass2 */

        public AccessibilityNodeInfoCompat get(SparseArrayCompat<AccessibilityNodeInfoCompat> collection, int index) {
            return collection.valueAt(index);
        }

        public int size(SparseArrayCompat<AccessibilityNodeInfoCompat> collection) {
            return collection.size();
        }
    };
    private int mAccessibilityFocusedVirtualViewId = Integer.MIN_VALUE;
    private final View mHost;
    private int mHoveredVirtualViewId = Integer.MIN_VALUE;
    private int mKeyboardFocusedVirtualViewId = Integer.MIN_VALUE;
    private final AccessibilityManager mManager;
    private MyNodeProvider mNodeProvider;
    private final int[] mTempGlobalRect = new int[2];
    private final Rect mTempParentRect = new Rect();
    private final Rect mTempScreenRect = new Rect();
    private final Rect mTempVisibleRect = new Rect();

    /* access modifiers changed from: protected */
    public abstract int getVirtualViewAt(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract void getVisibleVirtualViews(List<Integer> list);

    /* access modifiers changed from: protected */
    public abstract boolean onPerformActionForVirtualView(int i, int i2, @Nullable Bundle bundle);

    /* access modifiers changed from: protected */
    public abstract void onPopulateNodeForVirtualView(int i, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat);

    public ExploreByTouchHelper(@NonNull View host) {
        if (host != null) {
            this.mHost = host;
            this.mManager = (AccessibilityManager) host.getContext().getSystemService("accessibility");
            host.setFocusable(true);
            if (ViewCompat.getImportantForAccessibility(host) == 0) {
                ViewCompat.setImportantForAccessibility(host, 1);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("View may not be null");
    }

    @Override // android.support.v4.view.AccessibilityDelegateCompat
    public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(View host) {
        if (this.mNodeProvider == null) {
            this.mNodeProvider = new MyNodeProvider();
        }
        return this.mNodeProvider;
    }

    public final boolean dispatchHoverEvent(@NonNull MotionEvent event) {
        if (!this.mManager.isEnabled() || !this.mManager.isTouchExplorationEnabled()) {
            return false;
        }
        int action = event.getAction();
        if (action != 7) {
            switch (action) {
                case 9:
                    break;
                case 10:
                    if (this.mHoveredVirtualViewId == Integer.MIN_VALUE) {
                        return false;
                    }
                    updateHoveredVirtualView(Integer.MIN_VALUE);
                    return true;
                default:
                    return false;
            }
        }
        int virtualViewId = getVirtualViewAt(event.getX(), event.getY());
        updateHoveredVirtualView(virtualViewId);
        if (virtualViewId != Integer.MIN_VALUE) {
            return true;
        }
        return false;
    }

    public final boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == 1) {
            return false;
        }
        int keyCode = event.getKeyCode();
        if (keyCode != 61) {
            if (keyCode != 66) {
                switch (keyCode) {
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                        if (!event.hasNoModifiers()) {
                            return false;
                        }
                        int direction = keyToDirection(keyCode);
                        int count = 1 + event.getRepeatCount();
                        for (int i = 0; i < count && moveFocus(direction, null); i++) {
                            handled = true;
                        }
                        return handled;
                    case 23:
                        break;
                    default:
                        return false;
                }
            }
            if (!event.hasNoModifiers() || event.getRepeatCount() != 0) {
                return false;
            }
            clickKeyboardFocusedVirtualView();
            return true;
        } else if (event.hasNoModifiers()) {
            return moveFocus(2, null);
        } else {
            if (event.hasModifiers(1)) {
                return moveFocus(1, null);
            }
            return false;
        }
    }

    public final void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        if (this.mKeyboardFocusedVirtualViewId != Integer.MIN_VALUE) {
            clearKeyboardFocusForVirtualView(this.mKeyboardFocusedVirtualViewId);
        }
        if (gainFocus) {
            moveFocus(direction, previouslyFocusedRect);
        }
    }

    public final int getAccessibilityFocusedVirtualViewId() {
        return this.mAccessibilityFocusedVirtualViewId;
    }

    public final int getKeyboardFocusedVirtualViewId() {
        return this.mKeyboardFocusedVirtualViewId;
    }

    private static int keyToDirection(int keyCode) {
        switch (keyCode) {
            case 19:
                return 33;
            case 20:
            default:
                return Cea708CCParser.Const.CODE_C1_CW2;
            case 21:
                return 17;
            case 22:
                return 66;
        }
    }

    private void getBoundsInParent(int virtualViewId, Rect outBounds) {
        obtainAccessibilityNodeInfo(virtualViewId).getBoundsInParent(outBounds);
    }

    private boolean moveFocus(int direction, @Nullable Rect previouslyFocusedRect) {
        AccessibilityNodeInfoCompat focusedNode;
        AccessibilityNodeInfoCompat nextFocusedNode;
        int index;
        SparseArrayCompat<AccessibilityNodeInfoCompat> allNodes = getAllNodes();
        int focusedNodeId = this.mKeyboardFocusedVirtualViewId;
        if (focusedNodeId == Integer.MIN_VALUE) {
            focusedNode = null;
        } else {
            focusedNode = allNodes.get(focusedNodeId);
        }
        if (direction != 17 && direction != 33 && direction != 66 && direction != 130) {
            switch (direction) {
                default:
                    throw new IllegalArgumentException("direction must be one of {FOCUS_FORWARD, FOCUS_BACKWARD, FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
                case 1:
                case 2:
                    nextFocusedNode = (AccessibilityNodeInfoCompat) FocusStrategy.findNextFocusInRelativeDirection(allNodes, SPARSE_VALUES_ADAPTER, NODE_ADAPTER, focusedNode, direction, ViewCompat.getLayoutDirection(this.mHost) == 1, false);
                    break;
            }
        } else {
            Rect selectedRect = new Rect();
            if (this.mKeyboardFocusedVirtualViewId != Integer.MIN_VALUE) {
                getBoundsInParent(this.mKeyboardFocusedVirtualViewId, selectedRect);
            } else if (previouslyFocusedRect != null) {
                selectedRect.set(previouslyFocusedRect);
            } else {
                guessPreviouslyFocusedRect(this.mHost, direction, selectedRect);
            }
            nextFocusedNode = (AccessibilityNodeInfoCompat) FocusStrategy.findNextFocusInAbsoluteDirection(allNodes, SPARSE_VALUES_ADAPTER, NODE_ADAPTER, focusedNode, selectedRect, direction);
        }
        if (nextFocusedNode == null) {
            index = Integer.MIN_VALUE;
        } else {
            index = allNodes.keyAt(allNodes.indexOfValue(nextFocusedNode));
        }
        return requestKeyboardFocusForVirtualView(index);
    }

    private SparseArrayCompat<AccessibilityNodeInfoCompat> getAllNodes() {
        List<Integer> virtualViewIds = new ArrayList<>();
        getVisibleVirtualViews(virtualViewIds);
        SparseArrayCompat<AccessibilityNodeInfoCompat> allNodes = new SparseArrayCompat<>();
        for (int virtualViewId = 0; virtualViewId < virtualViewIds.size(); virtualViewId++) {
            allNodes.put(virtualViewId, createNodeForChild(virtualViewId));
        }
        return allNodes;
    }

    private static Rect guessPreviouslyFocusedRect(@NonNull View host, int direction, @NonNull Rect outBounds) {
        int w = host.getWidth();
        int h = host.getHeight();
        if (direction == 17) {
            outBounds.set(w, 0, w, h);
        } else if (direction == 33) {
            outBounds.set(0, h, w, h);
        } else if (direction == 66) {
            outBounds.set(-1, 0, -1, h);
        } else if (direction == 130) {
            outBounds.set(0, -1, w, -1);
        } else {
            throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
        }
        return outBounds;
    }

    private boolean clickKeyboardFocusedVirtualView() {
        return this.mKeyboardFocusedVirtualViewId != Integer.MIN_VALUE && onPerformActionForVirtualView(this.mKeyboardFocusedVirtualViewId, 16, null);
    }

    public final boolean sendEventForVirtualView(int virtualViewId, int eventType) {
        ViewParent parent;
        if (virtualViewId == Integer.MIN_VALUE || !this.mManager.isEnabled() || (parent = this.mHost.getParent()) == null) {
            return false;
        }
        return ViewParentCompat.requestSendAccessibilityEvent(parent, this.mHost, createEvent(virtualViewId, eventType));
    }

    public final void invalidateRoot() {
        invalidateVirtualView(-1, 1);
    }

    public final void invalidateVirtualView(int virtualViewId) {
        invalidateVirtualView(virtualViewId, 0);
    }

    public final void invalidateVirtualView(int virtualViewId, int changeTypes) {
        ViewParent parent;
        if (virtualViewId != Integer.MIN_VALUE && this.mManager.isEnabled() && (parent = this.mHost.getParent()) != null) {
            AccessibilityEvent event = createEvent(virtualViewId, 2048);
            AccessibilityEventCompat.setContentChangeTypes(event, changeTypes);
            ViewParentCompat.requestSendAccessibilityEvent(parent, this.mHost, event);
        }
    }

    @Deprecated
    public int getFocusedVirtualView() {
        return getAccessibilityFocusedVirtualViewId();
    }

    /* access modifiers changed from: protected */
    public void onVirtualViewKeyboardFocusChanged(int virtualViewId, boolean hasFocus) {
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
        this.mHost.onInitializeAccessibilityEvent(event);
        return event;
    }

    @Override // android.support.v4.view.AccessibilityDelegateCompat
    public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(host, event);
        onPopulateEventForHost(event);
    }

    private AccessibilityEvent createEventForChild(int virtualViewId, int eventType) {
        AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        AccessibilityNodeInfoCompat node = obtainAccessibilityNodeInfo(virtualViewId);
        event.getText().add(node.getText());
        event.setContentDescription(node.getContentDescription());
        event.setScrollable(node.isScrollable());
        event.setPassword(node.isPassword());
        event.setEnabled(node.isEnabled());
        event.setChecked(node.isChecked());
        onPopulateEventForVirtualView(virtualViewId, event);
        if (!event.getText().isEmpty() || event.getContentDescription() != null) {
            event.setClassName(node.getClassName());
            AccessibilityRecordCompat.setSource(event, this.mHost, virtualViewId);
            event.setPackageName(this.mHost.getContext().getPackageName());
            return event;
        }
        throw new RuntimeException("Callbacks must add text or a content description in populateEventForVirtualViewId()");
    }

    /* access modifiers changed from: package-private */
    @NonNull
    public AccessibilityNodeInfoCompat obtainAccessibilityNodeInfo(int virtualViewId) {
        if (virtualViewId == -1) {
            return createNodeForHost();
        }
        return createNodeForChild(virtualViewId);
    }

    @NonNull
    private AccessibilityNodeInfoCompat createNodeForHost() {
        AccessibilityNodeInfoCompat info = AccessibilityNodeInfoCompat.obtain(this.mHost);
        ViewCompat.onInitializeAccessibilityNodeInfo(this.mHost, info);
        ArrayList<Integer> virtualViewIds = new ArrayList<>();
        getVisibleVirtualViews(virtualViewIds);
        if (info.getChildCount() <= 0 || virtualViewIds.size() <= 0) {
            int count = virtualViewIds.size();
            for (int i = 0; i < count; i++) {
                info.addChild(this.mHost, virtualViewIds.get(i).intValue());
            }
            return info;
        }
        throw new RuntimeException("Views cannot have both real and virtual children");
    }

    @Override // android.support.v4.view.AccessibilityDelegateCompat
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        onPopulateNodeForHost(info);
    }

    @NonNull
    private AccessibilityNodeInfoCompat createNodeForChild(int virtualViewId) {
        AccessibilityNodeInfoCompat node = AccessibilityNodeInfoCompat.obtain();
        node.setEnabled(true);
        node.setFocusable(true);
        node.setClassName(DEFAULT_CLASS_NAME);
        node.setBoundsInParent(INVALID_PARENT_BOUNDS);
        node.setBoundsInScreen(INVALID_PARENT_BOUNDS);
        node.setParent(this.mHost);
        onPopulateNodeForVirtualView(virtualViewId, node);
        if (node.getText() == null && node.getContentDescription() == null) {
            throw new RuntimeException("Callbacks must add text or a content description in populateNodeForVirtualViewId()");
        }
        node.getBoundsInParent(this.mTempParentRect);
        if (!this.mTempParentRect.equals(INVALID_PARENT_BOUNDS)) {
            int actions = node.getActions();
            if ((actions & 64) != 0) {
                throw new RuntimeException("Callbacks must not add ACTION_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
            } else if ((actions & 128) == 0) {
                node.setPackageName(this.mHost.getContext().getPackageName());
                node.setSource(this.mHost, virtualViewId);
                if (this.mAccessibilityFocusedVirtualViewId == virtualViewId) {
                    node.setAccessibilityFocused(true);
                    node.addAction(128);
                } else {
                    node.setAccessibilityFocused(false);
                    node.addAction(64);
                }
                boolean isFocused = this.mKeyboardFocusedVirtualViewId == virtualViewId;
                if (isFocused) {
                    node.addAction(2);
                } else if (node.isFocusable()) {
                    node.addAction(1);
                }
                node.setFocused(isFocused);
                this.mHost.getLocationOnScreen(this.mTempGlobalRect);
                node.getBoundsInScreen(this.mTempScreenRect);
                if (this.mTempScreenRect.equals(INVALID_PARENT_BOUNDS)) {
                    node.getBoundsInParent(this.mTempScreenRect);
                    if (node.mParentVirtualDescendantId != -1) {
                        AccessibilityNodeInfoCompat parentNode = AccessibilityNodeInfoCompat.obtain();
                        for (int virtualDescendantId = node.mParentVirtualDescendantId; virtualDescendantId != -1; virtualDescendantId = parentNode.mParentVirtualDescendantId) {
                            parentNode.setParent(this.mHost, -1);
                            parentNode.setBoundsInParent(INVALID_PARENT_BOUNDS);
                            onPopulateNodeForVirtualView(virtualDescendantId, parentNode);
                            parentNode.getBoundsInParent(this.mTempParentRect);
                            this.mTempScreenRect.offset(this.mTempParentRect.left, this.mTempParentRect.top);
                        }
                        parentNode.recycle();
                    }
                    this.mTempScreenRect.offset(this.mTempGlobalRect[0] - this.mHost.getScrollX(), this.mTempGlobalRect[1] - this.mHost.getScrollY());
                }
                if (this.mHost.getLocalVisibleRect(this.mTempVisibleRect)) {
                    this.mTempVisibleRect.offset(this.mTempGlobalRect[0] - this.mHost.getScrollX(), this.mTempGlobalRect[1] - this.mHost.getScrollY());
                    if (this.mTempScreenRect.intersect(this.mTempVisibleRect)) {
                        node.setBoundsInScreen(this.mTempScreenRect);
                        if (isVisibleToUser(this.mTempScreenRect)) {
                            node.setVisibleToUser(true);
                        }
                    }
                }
                return node;
            } else {
                throw new RuntimeException("Callbacks must not add ACTION_CLEAR_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
            }
        } else {
            throw new RuntimeException("Callbacks must set parent bounds in populateNodeForVirtualViewId()");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean performAction(int virtualViewId, int action, Bundle arguments) {
        if (virtualViewId != -1) {
            return performActionForChild(virtualViewId, action, arguments);
        }
        return performActionForHost(action, arguments);
    }

    private boolean performActionForHost(int action, Bundle arguments) {
        return ViewCompat.performAccessibilityAction(this.mHost, action, arguments);
    }

    private boolean performActionForChild(int virtualViewId, int action, Bundle arguments) {
        if (action == 64) {
            return requestAccessibilityFocus(virtualViewId);
        }
        if (action == 128) {
            return clearAccessibilityFocus(virtualViewId);
        }
        switch (action) {
            case 1:
                return requestKeyboardFocusForVirtualView(virtualViewId);
            case 2:
                return clearKeyboardFocusForVirtualView(virtualViewId);
            default:
                return onPerformActionForVirtualView(virtualViewId, action, arguments);
        }
    }

    private boolean isVisibleToUser(Rect localRect) {
        if (localRect == null || localRect.isEmpty() || this.mHost.getWindowVisibility() != 0) {
            return false;
        }
        ViewParent viewParent = this.mHost.getParent();
        while (viewParent instanceof View) {
            View view = (View) viewParent;
            if (view.getAlpha() <= 0.0f || view.getVisibility() != 0) {
                return false;
            }
            viewParent = view.getParent();
        }
        if (viewParent != null) {
            return true;
        }
        return false;
    }

    private boolean requestAccessibilityFocus(int virtualViewId) {
        if (!this.mManager.isEnabled() || !this.mManager.isTouchExplorationEnabled() || this.mAccessibilityFocusedVirtualViewId == virtualViewId) {
            return false;
        }
        if (this.mAccessibilityFocusedVirtualViewId != Integer.MIN_VALUE) {
            clearAccessibilityFocus(this.mAccessibilityFocusedVirtualViewId);
        }
        this.mAccessibilityFocusedVirtualViewId = virtualViewId;
        this.mHost.invalidate();
        sendEventForVirtualView(virtualViewId, 32768);
        return true;
    }

    private boolean clearAccessibilityFocus(int virtualViewId) {
        if (this.mAccessibilityFocusedVirtualViewId != virtualViewId) {
            return false;
        }
        this.mAccessibilityFocusedVirtualViewId = Integer.MIN_VALUE;
        this.mHost.invalidate();
        sendEventForVirtualView(virtualViewId, 65536);
        return true;
    }

    public final boolean requestKeyboardFocusForVirtualView(int virtualViewId) {
        if ((!this.mHost.isFocused() && !this.mHost.requestFocus()) || this.mKeyboardFocusedVirtualViewId == virtualViewId) {
            return false;
        }
        if (this.mKeyboardFocusedVirtualViewId != Integer.MIN_VALUE) {
            clearKeyboardFocusForVirtualView(this.mKeyboardFocusedVirtualViewId);
        }
        this.mKeyboardFocusedVirtualViewId = virtualViewId;
        onVirtualViewKeyboardFocusChanged(virtualViewId, true);
        sendEventForVirtualView(virtualViewId, 8);
        return true;
    }

    public final boolean clearKeyboardFocusForVirtualView(int virtualViewId) {
        if (this.mKeyboardFocusedVirtualViewId != virtualViewId) {
            return false;
        }
        this.mKeyboardFocusedVirtualViewId = Integer.MIN_VALUE;
        onVirtualViewKeyboardFocusChanged(virtualViewId, false);
        sendEventForVirtualView(virtualViewId, 8);
        return true;
    }

    /* access modifiers changed from: protected */
    public void onPopulateEventForVirtualView(int virtualViewId, @NonNull AccessibilityEvent event) {
    }

    /* access modifiers changed from: protected */
    public void onPopulateEventForHost(@NonNull AccessibilityEvent event) {
    }

    /* access modifiers changed from: protected */
    public void onPopulateNodeForHost(@NonNull AccessibilityNodeInfoCompat node) {
    }

    private class MyNodeProvider extends AccessibilityNodeProviderCompat {
        MyNodeProvider() {
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeProviderCompat
        public AccessibilityNodeInfoCompat createAccessibilityNodeInfo(int virtualViewId) {
            return AccessibilityNodeInfoCompat.obtain(ExploreByTouchHelper.this.obtainAccessibilityNodeInfo(virtualViewId));
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeProviderCompat
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            return ExploreByTouchHelper.this.performAction(virtualViewId, action, arguments);
        }

        @Override // android.support.v4.view.accessibility.AccessibilityNodeProviderCompat
        public AccessibilityNodeInfoCompat findFocus(int focusType) {
            int focusedId = focusType == 2 ? ExploreByTouchHelper.this.mAccessibilityFocusedVirtualViewId : ExploreByTouchHelper.this.mKeyboardFocusedVirtualViewId;
            if (focusedId == Integer.MIN_VALUE) {
                return null;
            }
            return createAccessibilityNodeInfo(focusedId);
        }
    }
}
