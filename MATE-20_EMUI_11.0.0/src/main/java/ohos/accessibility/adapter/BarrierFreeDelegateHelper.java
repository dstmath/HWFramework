package ohos.accessibility.adapter;

import android.os.Bundle;
import android.util.IntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import ohos.accessibility.adapter.AccessibilityViewInfo;
import ohos.accessibility.utils.LogUtil;
import ohos.agp.utils.Rect;

public abstract class BarrierFreeDelegateHelper extends View.AccessibilityDelegate {
    private static final String DEFAULT_CLASS_NAME = View.class.getName();
    private static final int HOST_VIEW_ID = -1;
    private static final int INVALID_VIEW_ID = Integer.MIN_VALUE;
    private static final int POSITION_SIZE = 2;
    private static final String TAG = "BarrierFreeDelegateHelper";
    private final AccessibilityManager accessibilityManager;
    private int focusedViewId = Integer.MIN_VALUE;
    private View hostView;
    private int hoveredVirtualId = Integer.MIN_VALUE;
    private BarrierFreeDelegateProvider nodeProvider;

    /* access modifiers changed from: protected */
    public abstract int getViewIdByCoordinates(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle);

    /* access modifiers changed from: protected */
    public abstract void onPopulateAllViewIds(IntArray intArray);

    /* access modifiers changed from: protected */
    public abstract void onPopulateEvent(AccessibilityViewInfo accessibilityViewInfo, AccessibilityEvent accessibilityEvent);

    /* access modifiers changed from: protected */
    public abstract void onPopulateNodeInfo(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo);

    /* access modifiers changed from: protected */
    public abstract AccessibilityViewInfo queryAccessibilityViewInfoById(int i);

    public BarrierFreeDelegateHelper(View view) {
        if (view == null || view.getContext() == null) {
            throw new IllegalArgumentException("host view can not be null.");
        }
        this.hostView = view;
        this.accessibilityManager = (AccessibilityManager) view.getContext().getSystemService(AccessibilityManager.class);
    }

    @Override // android.view.View.AccessibilityDelegate
    public AccessibilityNodeProvider getAccessibilityNodeProvider(View view) {
        if (this.nodeProvider == null) {
            this.nodeProvider = new BarrierFreeDelegateProvider();
        }
        return this.nodeProvider;
    }

    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            LogUtil.error(TAG, "event is null.");
            return false;
        } else if (!isAccessibilityManagerEnabled()) {
            LogUtil.error(TAG, "accessibility manager is not enabled.");
            return false;
        } else {
            int action = motionEvent.getAction();
            if (action == 7 || action == 9) {
                int viewIdByCoordinates = getViewIdByCoordinates(motionEvent.getRawX(), motionEvent.getRawY());
                updateHoveredEvent(viewIdByCoordinates);
                if (viewIdByCoordinates != Integer.MIN_VALUE) {
                    return true;
                }
                return false;
            } else if (action != 10 || this.focusedViewId == Integer.MIN_VALUE) {
                return false;
            } else {
                updateHoveredEvent(Integer.MIN_VALUE);
                return true;
            }
        }
    }

    public boolean sendAccessibilityEvent(int i, int i2) {
        LogUtil.info(TAG, "sendAccessibilityEvent virtualViewId:" + i + " eventType:" + i2);
        if (i == Integer.MIN_VALUE || !isAccessibilityEnabled()) {
            LogUtil.error(TAG, "sendAccessibilityEvent failed, accessibility is not enabled.");
            return false;
        }
        ViewParent parent = this.hostView.getParent();
        if (parent == null) {
            LogUtil.error(TAG, "sendAccessibilityEvent failed, parent is null.");
            return false;
        }
        return parent.requestSendAccessibilityEvent(this.hostView, createEvent(i, i2));
    }

    private boolean isAccessibilityManagerEnabled() {
        AccessibilityManager accessibilityManager2 = this.accessibilityManager;
        if (accessibilityManager2 == null) {
            return false;
        }
        if (accessibilityManager2.isEnabled() || this.accessibilityManager.isTouchExplorationEnabled()) {
            return true;
        }
        return false;
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager accessibilityManager2 = this.accessibilityManager;
        return accessibilityManager2 != null && accessibilityManager2.isEnabled();
    }

    private AccessibilityEvent createEvent(int i, int i2) {
        if (i != -1) {
            return createChildEvent(i, i2);
        }
        return createHostEvent(i2);
    }

    private AccessibilityEvent createHostEvent(int i) {
        AccessibilityEvent obtain = AccessibilityEvent.obtain(i);
        this.hostView.onInitializeAccessibilityEvent(obtain);
        return obtain;
    }

    private AccessibilityEvent createChildEvent(int i, int i2) {
        AccessibilityEvent obtain = AccessibilityEvent.obtain(i2);
        obtain.setEnabled(true);
        obtain.setClassName(DEFAULT_CLASS_NAME);
        AccessibilityViewInfo queryAccessibilityViewInfoById = queryAccessibilityViewInfoById(i);
        if (queryAccessibilityViewInfoById != null) {
            obtain.setContentDescription(queryAccessibilityViewInfoById.getDescription());
            AccessibilityViewInfo.ProgressInfo progressInfo = queryAccessibilityViewInfoById.getProgressInfo();
            if (progressInfo != null) {
                obtain.setItemCount(progressInfo.getMax() - progressInfo.getMin());
                obtain.setCurrentItemIndex(progressInfo.getValue());
            }
        }
        onPopulateEvent(queryAccessibilityViewInfoById, obtain);
        obtain.setPackageName(this.hostView.getContext().getPackageName());
        obtain.setSource(this.hostView, i);
        return obtain;
    }

    private void updateHoveredEvent(int i) {
        int i2 = this.hoveredVirtualId;
        if (i2 == i) {
            LogUtil.info(TAG, "View is already hovered.");
            return;
        }
        this.hoveredVirtualId = i;
        sendAccessibilityEvent(i, 128);
        sendAccessibilityEvent(i2, 256);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AccessibilityNodeInfo createNode(int i) {
        if (i != -1) {
            return createChildNodeInfo(i);
        }
        return createHostNodeInfo();
    }

    private AccessibilityNodeInfo createHostNodeInfo() {
        AccessibilityNodeInfo obtain = AccessibilityNodeInfo.obtain(this.hostView);
        this.hostView.onInitializeAccessibilityNodeInfo(obtain);
        obtain.setParent(null);
        IntArray intArray = new IntArray();
        onPopulateAllViewIds(intArray);
        int size = intArray.size();
        for (int i = 0; i < size; i++) {
            obtain.addChild(this.hostView, intArray.get(i));
        }
        return obtain;
    }

    private AccessibilityNodeInfo createChildNodeInfo(int i) {
        AccessibilityNodeInfo obtain = AccessibilityNodeInfo.obtain();
        obtain.setEnabled(true);
        obtain.setClassName(DEFAULT_CLASS_NAME);
        obtain.setPackageName(this.hostView.getContext().getPackageName());
        obtain.setSource(this.hostView, i);
        obtain.setImportantForAccessibility(true);
        AccessibilityViewInfo queryAccessibilityViewInfoById = queryAccessibilityViewInfoById(i);
        if (queryAccessibilityViewInfoById != null) {
            obtain.setText(queryAccessibilityViewInfoById.getText());
            obtain.setContentDescription(queryAccessibilityViewInfoById.getDescription());
            obtain.setViewIdResourceName(queryAccessibilityViewInfoById.getResourceName());
            obtain.setCheckable(queryAccessibilityViewInfoById.isCheckable());
            obtain.setClickable(queryAccessibilityViewInfoById.isClickable());
            obtain.setFocusable(queryAccessibilityViewInfoById.isFocusable());
            obtain.setLongClickable(queryAccessibilityViewInfoById.isLongClickable());
            obtain.setScrollable(queryAccessibilityViewInfoById.isScrollable());
            obtain.setChecked(queryAccessibilityViewInfoById.isChecked());
            obtain.setEnabled(queryAccessibilityViewInfoById.isEnabled());
            obtain.setEditable(queryAccessibilityViewInfoById.isEditable());
            obtain.setFocused(queryAccessibilityViewInfoById.isFocused());
            obtain.setSelected(queryAccessibilityViewInfoById.isSelected());
            obtain.setMultiLine(queryAccessibilityViewInfoById.isMultiLine());
            obtain.setPassword(queryAccessibilityViewInfoById.isPassword());
            obtain.setShowingHintText(queryAccessibilityViewInfoById.isShowingHintText());
            obtain.setTextSelection(queryAccessibilityViewInfoById.getTextSelectionStart(), queryAccessibilityViewInfoById.getTextSelectionEnd());
            obtain.setHintText(queryAccessibilityViewInfoById.getHintText());
            obtain.setError(queryAccessibilityViewInfoById.getError());
            obtain.setVisibleToUser(queryAccessibilityViewInfoById.isVisible());
            obtain.setParent(this.hostView, queryAccessibilityViewInfoById.getParentId());
            populateRangInfoForNode(queryAccessibilityViewInfoById, obtain);
            populateListInfoForNode(queryAccessibilityViewInfoById, obtain);
            populateListItemInfoForNode(queryAccessibilityViewInfoById, obtain);
            populateChildrenForNode(queryAccessibilityViewInfoById, obtain);
            populateRectForNode(obtain, queryAccessibilityViewInfoById);
        }
        if (this.hostView.getTouchDelegate() != null) {
            obtain.setTouchDelegateInfo(this.hostView.getTouchDelegate().getTouchDelegateInfo());
        }
        onPopulateNodeInfo(queryAccessibilityViewInfoById, obtain);
        if (this.focusedViewId == i) {
            obtain.setAccessibilityFocused(true);
            obtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        } else {
            obtain.setAccessibilityFocused(false);
            obtain.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
        }
        return obtain;
    }

    private void populateRangInfoForNode(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        AccessibilityViewInfo.ProgressInfo progressInfo = accessibilityViewInfo.getProgressInfo();
        if (progressInfo != null) {
            accessibilityNodeInfo.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(0, (float) progressInfo.getMin(), (float) progressInfo.getMax(), (float) progressInfo.getValue()));
        }
    }

    private void populateListInfoForNode(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        AccessibilityViewInfo.ListInfo listInfo = accessibilityViewInfo.getListInfo();
        if (listInfo != null && listInfo.getRowCount() > 1) {
            accessibilityNodeInfo.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(listInfo.getRowCount(), listInfo.getColumnCount(), false, 0));
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
            accessibilityNodeInfo.setScrollable(true);
        }
    }

    private void populateListItemInfoForNode(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        AccessibilityViewInfo.ListItemInfo listItemInfo = accessibilityViewInfo.getListItemInfo();
        if (listItemInfo != null && listItemInfo.getRowIndex() > 0) {
            accessibilityNodeInfo.setCollectionItemInfo(AccessibilityNodeInfo.CollectionItemInfo.obtain(listItemInfo.getRowIndex(), 1, listItemInfo.getColumnIndex(), 1, listItemInfo.isHeading(), listItemInfo.isSelected()));
        }
    }

    private void populateChildrenForNode(AccessibilityViewInfo accessibilityViewInfo, AccessibilityNodeInfo accessibilityNodeInfo) {
        int[] childIdList = accessibilityViewInfo.getChildIdList();
        if (childIdList.length > 0) {
            for (int i = 0; i < childIdList.length; i++) {
                if (childIdList[i] >= 0) {
                    accessibilityNodeInfo.addChild(this.hostView, childIdList[i]);
                }
            }
        }
    }

    private void populateRectForNode(AccessibilityNodeInfo accessibilityNodeInfo, AccessibilityViewInfo accessibilityViewInfo) {
        Rect rect = accessibilityViewInfo.getRect();
        android.graphics.Rect rect2 = new android.graphics.Rect(rect.left, rect.top, rect.right, rect.bottom);
        accessibilityNodeInfo.setBoundsInParent(rect2);
        int[] iArr = new int[2];
        View view = this.hostView;
        if (view != null) {
            view.getLocationOnScreen(iArr);
        }
        android.graphics.Rect rect3 = new android.graphics.Rect(rect2);
        rect3.offset(iArr[0], iArr[1]);
        accessibilityNodeInfo.setBoundsInScreen(rect3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean performAction(int i, int i2, Bundle bundle) {
        if (i != -1) {
            return performChildAction(i, i2, bundle);
        }
        return performHostAction(i2, bundle);
    }

    private boolean performHostAction(int i, Bundle bundle) {
        return this.hostView.performAccessibilityAction(i, bundle);
    }

    private boolean performChildAction(int i, int i2, Bundle bundle) {
        if (i2 == 64) {
            return requestAccessibilityFocus(i);
        }
        if (i2 != 128) {
            return onPerformActionForVirtualView(i, i2, bundle);
        }
        return clearAccessibilityFocus(i);
    }

    private boolean requestAccessibilityFocus(int i) {
        if (!isAccessibilityManagerEnabled()) {
            LogUtil.error(TAG, "requestAccessibilityFocus failed, accessibility manager is not enable.");
            return false;
        } else if (isAccessibilityFocused(i)) {
            return false;
        } else {
            int i2 = this.focusedViewId;
            if (i2 != Integer.MIN_VALUE) {
                sendAccessibilityEvent(i2, 65536);
            }
            this.focusedViewId = i;
            this.hostView.invalidate();
            sendAccessibilityEvent(i, 32768);
            return true;
        }
    }

    private boolean clearAccessibilityFocus(int i) {
        if (!isAccessibilityFocused(i)) {
            return false;
        }
        this.focusedViewId = Integer.MIN_VALUE;
        this.hostView.invalidate();
        sendAccessibilityEvent(i, 65536);
        return true;
    }

    private boolean isAccessibilityFocused(int i) {
        return this.focusedViewId == i;
    }

    private class BarrierFreeDelegateProvider extends AccessibilityNodeProvider {
        private BarrierFreeDelegateProvider() {
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int i) {
            LogUtil.info(BarrierFreeDelegateHelper.TAG, "createAccessibilityNodeInfo virtualViewId:" + i);
            return BarrierFreeDelegateHelper.this.createNode(i);
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public boolean performAction(int i, int i2, Bundle bundle) {
            LogUtil.info(BarrierFreeDelegateHelper.TAG, "performAction virtualViewId:" + i + " action:" + i2);
            return BarrierFreeDelegateHelper.this.performAction(i, i2, bundle);
        }
    }
}
