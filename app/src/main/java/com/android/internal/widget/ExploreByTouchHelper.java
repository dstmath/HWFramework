package com.android.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.IntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeProvider;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import javax.microedition.khronos.opengles.GL10;

public abstract class ExploreByTouchHelper extends AccessibilityDelegate {
    private static final String DEFAULT_CLASS_NAME = null;
    public static final int HOST_ID = -1;
    public static final int INVALID_ID = Integer.MIN_VALUE;
    private static final Rect INVALID_PARENT_BOUNDS = null;
    private final Context mContext;
    private int mFocusedVirtualViewId;
    private int mHoveredVirtualViewId;
    private final AccessibilityManager mManager;
    private ExploreByTouchNodeProvider mNodeProvider;
    private IntArray mTempArray;
    private int[] mTempGlobalRect;
    private Rect mTempParentRect;
    private Rect mTempScreenRect;
    private Rect mTempVisibleRect;
    private final View mView;

    private class ExploreByTouchNodeProvider extends AccessibilityNodeProvider {
        private ExploreByTouchNodeProvider() {
        }

        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            return ExploreByTouchHelper.this.createNode(virtualViewId);
        }

        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            return ExploreByTouchHelper.this.performAction(virtualViewId, action, arguments);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.ExploreByTouchHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.ExploreByTouchHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.ExploreByTouchHelper.<clinit>():void");
    }

    protected abstract int getVirtualViewAt(float f, float f2);

    protected abstract void getVisibleVirtualViews(IntArray intArray);

    protected abstract boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle);

    protected abstract void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent);

    protected abstract void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfo accessibilityNodeInfo);

    public ExploreByTouchHelper(View forView) {
        this.mFocusedVirtualViewId = INVALID_ID;
        this.mHoveredVirtualViewId = INVALID_ID;
        if (forView == null) {
            throw new IllegalArgumentException("View may not be null");
        }
        this.mView = forView;
        this.mContext = forView.getContext();
        this.mManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
        if (this.mNodeProvider == null) {
            this.mNodeProvider = new ExploreByTouchNodeProvider();
        }
        return this.mNodeProvider;
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        boolean z = true;
        if (!this.mManager.isEnabled() || !this.mManager.isTouchExplorationEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case HwCfgFilePolicy.CLOUD_APN /*7*/:
            case PGSdk.TYPE_SCRLOCK /*9*/:
                int virtualViewId = getVirtualViewAt(event.getX(), event.getY());
                updateHoveredVirtualView(virtualViewId);
                if (virtualViewId == INVALID_ID) {
                    z = false;
                }
                return z;
            case PGSdk.TYPE_CLOCK /*10*/:
                if (this.mFocusedVirtualViewId == INVALID_ID) {
                    return false;
                }
                updateHoveredVirtualView(INVALID_ID);
                return true;
            default:
                return false;
        }
    }

    public boolean sendEventForVirtualView(int virtualViewId, int eventType) {
        if (virtualViewId == INVALID_ID || !this.mManager.isEnabled()) {
            return false;
        }
        ViewParent parent = this.mView.getParent();
        if (parent == null) {
            return false;
        }
        return parent.requestSendAccessibilityEvent(this.mView, createEvent(virtualViewId, eventType));
    }

    public void invalidateRoot() {
        invalidateVirtualView(HOST_ID, 1);
    }

    public void invalidateVirtualView(int virtualViewId) {
        invalidateVirtualView(virtualViewId, 0);
    }

    public void invalidateVirtualView(int virtualViewId, int changeTypes) {
        if (virtualViewId != INVALID_ID && this.mManager.isEnabled()) {
            ViewParent parent = this.mView.getParent();
            if (parent != null) {
                AccessibilityEvent event = createEvent(virtualViewId, GL10.GL_EXP);
                event.setContentChangeTypes(changeTypes);
                parent.requestSendAccessibilityEvent(this.mView, event);
            }
        }
    }

    public int getFocusedVirtualView() {
        return this.mFocusedVirtualViewId;
    }

    private void updateHoveredVirtualView(int virtualViewId) {
        if (this.mHoveredVirtualViewId != virtualViewId) {
            int previousVirtualViewId = this.mHoveredVirtualViewId;
            this.mHoveredVirtualViewId = virtualViewId;
            sendEventForVirtualView(virtualViewId, LogPower.START_CHG_ROTATION);
            sendEventForVirtualView(previousVirtualViewId, GL10.GL_DEPTH_BUFFER_BIT);
        }
    }

    private AccessibilityEvent createEvent(int virtualViewId, int eventType) {
        switch (virtualViewId) {
            case HOST_ID /*-1*/:
                return createEventForHost(eventType);
            default:
                return createEventForChild(virtualViewId, eventType);
        }
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
        if (event.getText().isEmpty() && event.getContentDescription() == null) {
            throw new RuntimeException("Callbacks must add text or a content description in populateEventForVirtualViewId()");
        }
        event.setPackageName(this.mView.getContext().getPackageName());
        event.setSource(this.mView, virtualViewId);
        return event;
    }

    private AccessibilityNodeInfo createNode(int virtualViewId) {
        switch (virtualViewId) {
            case HOST_ID /*-1*/:
                return createNodeForHost();
            default:
                return createNodeForChild(virtualViewId);
        }
    }

    private AccessibilityNodeInfo createNodeForHost() {
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(this.mView);
        this.mView.onInitializeAccessibilityNodeInfo(node);
        int realNodeCount = node.getChildCount();
        onPopulateNodeForHost(node);
        if (this.mTempArray == null) {
            this.mTempArray = new IntArray();
        } else {
            this.mTempArray.clear();
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
        if (tempParentRect.equals(INVALID_PARENT_BOUNDS)) {
            throw new RuntimeException("Callbacks must set parent bounds in populateNodeForVirtualViewId()");
        }
        int actions = node.getActions();
        if ((actions & 64) != 0) {
            throw new RuntimeException("Callbacks must not add ACTION_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
        } else if ((actions & LogPower.START_CHG_ROTATION) != 0) {
            throw new RuntimeException("Callbacks must not add ACTION_CLEAR_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
        } else {
            node.setPackageName(this.mView.getContext().getPackageName());
            node.setSource(this.mView, virtualViewId);
            node.setParent(this.mView);
            if (this.mFocusedVirtualViewId == virtualViewId) {
                node.setAccessibilityFocused(true);
                node.addAction(AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            } else {
                node.setAccessibilityFocused(false);
                node.addAction(AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
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
        }
    }

    private void ensureTempRects() {
        this.mTempGlobalRect = new int[2];
        this.mTempParentRect = new Rect();
        this.mTempScreenRect = new Rect();
    }

    private boolean performAction(int virtualViewId, int action, Bundle arguments) {
        switch (virtualViewId) {
            case HOST_ID /*-1*/:
                return performActionForHost(action, arguments);
            default:
                return performActionForChild(virtualViewId, action, arguments);
        }
    }

    private boolean performActionForHost(int action, Bundle arguments) {
        return this.mView.performAccessibilityAction(action, arguments);
    }

    private boolean performActionForChild(int virtualViewId, int action, Bundle arguments) {
        switch (action) {
            case RILConstants.RIL_REQUEST_DELETE_SMS_ON_SIM /*64*/:
            case LogPower.START_CHG_ROTATION /*128*/:
                return manageFocusForChild(virtualViewId, action);
            default:
                return onPerformActionForVirtualView(virtualViewId, action, arguments);
        }
    }

    private boolean manageFocusForChild(int virtualViewId, int action) {
        switch (action) {
            case RILConstants.RIL_REQUEST_DELETE_SMS_ON_SIM /*64*/:
                return requestAccessibilityFocus(virtualViewId);
            case LogPower.START_CHG_ROTATION /*128*/:
                return clearAccessibilityFocus(virtualViewId);
            default:
                return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        if (this.mView.getLocalVisibleRect(tempVisibleRect)) {
            return localRect.intersect(tempVisibleRect);
        }
        return false;
    }

    private boolean isAccessibilityFocused(int virtualViewId) {
        return this.mFocusedVirtualViewId == virtualViewId;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean requestAccessibilityFocus(int virtualViewId) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        if (!this.mManager.isEnabled() || !accessibilityManager.isTouchExplorationEnabled() || isAccessibilityFocused(virtualViewId)) {
            return false;
        }
        if (this.mFocusedVirtualViewId != INVALID_ID) {
            sendEventForVirtualView(this.mFocusedVirtualViewId, Protocol.BASE_SYSTEM_RESERVED);
        }
        this.mFocusedVirtualViewId = virtualViewId;
        this.mView.invalidate();
        sendEventForVirtualView(virtualViewId, AccessibilityNodeInfo.ACTION_PASTE);
        return true;
    }

    private boolean clearAccessibilityFocus(int virtualViewId) {
        if (!isAccessibilityFocused(virtualViewId)) {
            return false;
        }
        this.mFocusedVirtualViewId = INVALID_ID;
        this.mView.invalidate();
        sendEventForVirtualView(virtualViewId, Protocol.BASE_SYSTEM_RESERVED);
        return true;
    }

    protected void onPopulateEventForHost(AccessibilityEvent event) {
    }

    protected void onPopulateNodeForHost(AccessibilityNodeInfo node) {
    }
}
