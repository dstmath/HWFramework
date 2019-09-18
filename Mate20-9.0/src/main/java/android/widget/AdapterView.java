package android.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.database.StaleDataException;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.autofill.AutofillManager;
import android.widget.Adapter;

public abstract class AdapterView<T extends Adapter> extends ViewGroup {
    public static final int INVALID_POSITION = -1;
    public static final long INVALID_ROW_ID = Long.MIN_VALUE;
    public static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;
    public static final int ITEM_VIEW_TYPE_IGNORE = -1;
    static final int SYNC_FIRST_POSITION = 1;
    static final int SYNC_MAX_DURATION_MILLIS = 100;
    static final int SYNC_SELECTED_POSITION = 0;
    boolean mBlockLayoutRequests;
    boolean mDataChanged;
    private boolean mDesiredFocusableInTouchModeState;
    private int mDesiredFocusableState;
    private View mEmptyView;
    @ViewDebug.ExportedProperty(category = "scrolling")
    int mFirstPosition;
    boolean mInLayout;
    @ViewDebug.ExportedProperty(category = "list")
    int mItemCount;
    private int mLayoutHeight;
    boolean mNeedSync;
    @ViewDebug.ExportedProperty(category = "list")
    int mNextSelectedPosition;
    long mNextSelectedRowId;
    int mOldItemCount;
    int mOldSelectedPosition;
    long mOldSelectedRowId;
    OnItemClickListener mOnItemClickListener;
    OnItemLongClickListener mOnItemLongClickListener;
    OnItemSelectedListener mOnItemSelectedListener;
    /* access modifiers changed from: private */
    public AdapterView<T>.SelectionNotifier mPendingSelectionNotifier;
    @ViewDebug.ExportedProperty(category = "list")
    int mSelectedPosition;
    long mSelectedRowId;
    private AdapterView<T>.SelectionNotifier mSelectionNotifier;
    int mSpecificTop;
    long mSyncHeight;
    int mSyncMode;
    int mSyncPosition;
    long mSyncRowId;

    public static class AdapterContextMenuInfo implements ContextMenu.ContextMenuInfo {
        public long id;
        public int position;
        public View targetView;

        public AdapterContextMenuInfo(View targetView2, int position2, long id2) {
            this.targetView = targetView2;
            this.position = position2;
            this.id = id2;
        }
    }

    class AdapterDataSetObserver extends DataSetObserver {
        private Parcelable mInstanceState = null;

        AdapterDataSetObserver() {
        }

        public void onChanged() {
            AdapterView.this.mDataChanged = true;
            AdapterView.this.mOldItemCount = AdapterView.this.mItemCount;
            AdapterView.this.mItemCount = AdapterView.this.getAdapter().getCount();
            if (!AdapterView.this.getAdapter().hasStableIds() || this.mInstanceState == null || AdapterView.this.mOldItemCount != 0 || AdapterView.this.mItemCount <= 0) {
                AdapterView.this.rememberSyncState();
            } else {
                AdapterView.this.onRestoreInstanceState(this.mInstanceState);
                this.mInstanceState = null;
            }
            AdapterView.this.checkFocus();
            AdapterView.this.requestLayout();
        }

        public void onInvalidated() {
            AdapterView.this.mDataChanged = true;
            if (AdapterView.this.getAdapter().hasStableIds()) {
                this.mInstanceState = AdapterView.this.onSaveInstanceState();
            }
            AdapterView.this.mOldItemCount = AdapterView.this.mItemCount;
            AdapterView.this.mItemCount = 0;
            AdapterView.this.mSelectedPosition = -1;
            AdapterView.this.mSelectedRowId = Long.MIN_VALUE;
            AdapterView.this.mNextSelectedPosition = -1;
            AdapterView.this.mNextSelectedRowId = Long.MIN_VALUE;
            AdapterView.this.mNeedSync = false;
            AdapterView.this.checkFocus();
            AdapterView.this.requestLayout();
        }

        public void clearSavedState() {
            this.mInstanceState = null;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(AdapterView<?> adapterView, View view, int i, long j);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long j);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(AdapterView<?> adapterView, View view, int i, long j);

        void onNothingSelected(AdapterView<?> adapterView);
    }

    private class SelectionNotifier implements Runnable {
        private SelectionNotifier() {
        }

        public void run() {
            SelectionNotifier unused = AdapterView.this.mPendingSelectionNotifier = null;
            if (!AdapterView.this.mDataChanged || AdapterView.this.getViewRootImpl() == null || !AdapterView.this.getViewRootImpl().isLayoutRequested()) {
                AdapterView.this.dispatchOnItemSelected();
            } else if (AdapterView.this.getAdapter() != null) {
                SelectionNotifier unused2 = AdapterView.this.mPendingSelectionNotifier = this;
            }
        }
    }

    public abstract T getAdapter();

    public abstract View getSelectedView();

    public abstract void setAdapter(T t);

    public abstract void setSelection(int i);

    public AdapterView(Context context) {
        this(context, null);
    }

    public AdapterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdapterView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AdapterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mFirstPosition = 0;
        this.mSyncRowId = Long.MIN_VALUE;
        this.mNeedSync = false;
        this.mInLayout = false;
        this.mNextSelectedPosition = -1;
        this.mNextSelectedRowId = Long.MIN_VALUE;
        this.mSelectedPosition = -1;
        this.mSelectedRowId = Long.MIN_VALUE;
        this.mOldSelectedPosition = -1;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        this.mDesiredFocusableState = 16;
        this.mBlockLayoutRequests = false;
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        this.mDesiredFocusableState = getFocusable();
        if (this.mDesiredFocusableState == 16) {
            super.setFocusable(0);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public final OnItemClickListener getOnItemClickListener() {
        return this.mOnItemClickListener;
    }

    public boolean performItemClick(View view, int position, long id) {
        boolean result = false;
        if (this.mOnItemClickListener != null) {
            playSoundEffect(0);
            this.mOnItemClickListener.onItemClick(this, view, position, id);
            result = true;
        }
        boolean result2 = result;
        if (view != null) {
            view.sendAccessibilityEvent(1);
        }
        return result2;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        this.mOnItemLongClickListener = listener;
    }

    public final OnItemLongClickListener getOnItemLongClickListener() {
        return this.mOnItemLongClickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mOnItemSelectedListener = listener;
    }

    public final OnItemSelectedListener getOnItemSelectedListener() {
        return this.mOnItemSelectedListener;
    }

    public void addView(View child) {
        throw new UnsupportedOperationException("addView(View) is not supported in AdapterView");
    }

    public void addView(View child, int index) {
        throw new UnsupportedOperationException("addView(View, int) is not supported in AdapterView");
    }

    public void addView(View child, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, LayoutParams) is not supported in AdapterView");
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, int, LayoutParams) is not supported in AdapterView");
    }

    public void removeView(View child) {
        throw new UnsupportedOperationException("removeView(View) is not supported in AdapterView");
    }

    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported in AdapterView");
    }

    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported in AdapterView");
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.mLayoutHeight = getHeight();
    }

    @ViewDebug.CapturedViewProperty
    public int getSelectedItemPosition() {
        return this.mNextSelectedPosition;
    }

    @ViewDebug.CapturedViewProperty
    public long getSelectedItemId() {
        return this.mNextSelectedRowId;
    }

    public Object getSelectedItem() {
        T adapter = getAdapter();
        int selection = getSelectedItemPosition();
        if (adapter == null || adapter.getCount() <= 0 || selection < 0) {
            return null;
        }
        return adapter.getItem(selection);
    }

    @ViewDebug.CapturedViewProperty
    public int getCount() {
        return this.mItemCount;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0016  */
    public int getPositionForView(View view) {
        View listItem = view;
        while (true) {
            try {
                View view2 = (View) listItem.getParent();
                View v = view2;
                if (view2 != null && !v.equals(this)) {
                    listItem = v;
                } else if (listItem != null) {
                    int childCount = getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        if (getChildAt(i).equals(listItem)) {
                            return this.mFirstPosition + i;
                        }
                    }
                }
            } catch (ClassCastException e) {
                return -1;
            }
        }
        if (listItem != null) {
        }
        return -1;
    }

    public int getFirstVisiblePosition() {
        return this.mFirstPosition;
    }

    public int getLastVisiblePosition() {
        return (this.mFirstPosition + getChildCount()) - 1;
    }

    @RemotableViewMethod
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        boolean empty = true;
        if (emptyView != null && emptyView.getImportantForAccessibility() == 0) {
            emptyView.setImportantForAccessibility(1);
        }
        T adapter = getAdapter();
        if (adapter != null && !adapter.isEmpty()) {
            empty = false;
        }
        updateEmptyStatus(empty);
    }

    public View getEmptyView() {
        return this.mEmptyView;
    }

    /* access modifiers changed from: package-private */
    public boolean isInFilterMode() {
        return false;
    }

    public void setFocusable(int focusable) {
        T adapter = getAdapter();
        int i = 0;
        boolean empty = adapter == null || adapter.getCount() == 0;
        this.mDesiredFocusableState = focusable;
        if ((focusable & 17) == 0) {
            this.mDesiredFocusableInTouchModeState = false;
        }
        if (!empty || isInFilterMode()) {
            i = focusable;
        }
        super.setFocusable(i);
    }

    public void setFocusableInTouchMode(boolean focusable) {
        T adapter = getAdapter();
        boolean z = false;
        boolean empty = adapter == null || adapter.getCount() == 0;
        this.mDesiredFocusableInTouchModeState = focusable;
        if (focusable) {
            this.mDesiredFocusableState = 1;
        }
        if (focusable && (!empty || isInFilterMode())) {
            z = true;
        }
        super.setFocusableInTouchMode(z);
    }

    /* access modifiers changed from: package-private */
    public void checkFocus() {
        T adapter = getAdapter();
        boolean z = true;
        boolean focusable = !(adapter == null || adapter.getCount() == 0) || isInFilterMode();
        super.setFocusableInTouchMode(focusable && this.mDesiredFocusableInTouchModeState);
        super.setFocusable(focusable ? this.mDesiredFocusableState : 0);
        if (this.mEmptyView != null) {
            if (adapter != null && !adapter.isEmpty()) {
                z = false;
            }
            updateEmptyStatus(z);
        }
    }

    private void updateEmptyStatus(boolean empty) {
        if (isInFilterMode()) {
            empty = false;
        }
        if (empty) {
            if (this.mEmptyView != null) {
                this.mEmptyView.setVisibility(0);
                setVisibility(8);
            } else {
                setVisibility(0);
            }
            if (this.mDataChanged) {
                onLayout(false, this.mLeft, this.mTop, this.mRight, this.mBottom);
                return;
            }
            return;
        }
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(8);
        }
        setVisibility(0);
    }

    public Object getItemAtPosition(int position) {
        T adapter = getAdapter();
        if (adapter == null || position < 0) {
            return null;
        }
        return adapter.getItem(position);
    }

    public long getItemIdAtPosition(int position) {
        T adapter = getAdapter();
        if (adapter == null || position < 0) {
            return Long.MIN_VALUE;
        }
        return adapter.getItemId(position);
    }

    public void setOnClickListener(View.OnClickListener l) {
        throw new RuntimeException("Don't call setOnClickListener for an AdapterView. You probably want setOnItemClickListener instead");
    }

    /* access modifiers changed from: protected */
    public void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    /* access modifiers changed from: protected */
    public void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mSelectionNotifier);
    }

    /* access modifiers changed from: package-private */
    public void selectionChanged() {
        this.mPendingSelectionNotifier = null;
        if (this.mOnItemSelectedListener != null || AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if (this.mInLayout || this.mBlockLayoutRequests) {
                if (this.mSelectionNotifier == null) {
                    this.mSelectionNotifier = new SelectionNotifier();
                } else {
                    removeCallbacks(this.mSelectionNotifier);
                }
                post(this.mSelectionNotifier);
            } else {
                dispatchOnItemSelected();
            }
        }
        AutofillManager afm = (AutofillManager) this.mContext.getSystemService(AutofillManager.class);
        if (afm != null) {
            afm.notifyValueChanged(this);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnItemSelected() {
        fireOnSelected();
        performAccessibilityActionsOnSelected();
    }

    private void fireOnSelected() {
        if (this.mOnItemSelectedListener != null) {
            int selection = getSelectedItemPosition();
            if (selection >= 0) {
                View v = getSelectedView();
                this.mOnItemSelectedListener.onItemSelected(this, v, selection, getAdapter().getItemId(selection));
            } else {
                this.mOnItemSelectedListener.onNothingSelected(this);
            }
        }
    }

    private void performAccessibilityActionsOnSelected() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && getSelectedItemPosition() >= 0) {
            sendAccessibilityEvent(4);
        }
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        View selectedView = getSelectedView();
        if (selectedView == null || selectedView.getVisibility() != 0 || !selectedView.dispatchPopulateAccessibilityEvent(event)) {
            return false;
        }
        return true;
    }

    public boolean onRequestSendAccessibilityEventInternal(View child, AccessibilityEvent event) {
        if (!super.onRequestSendAccessibilityEventInternal(child, event)) {
            return false;
        }
        AccessibilityEvent record = AccessibilityEvent.obtain();
        onInitializeAccessibilityEvent(record);
        child.dispatchPopulateAccessibilityEvent(record);
        event.appendRecord(record);
        return true;
    }

    public CharSequence getAccessibilityClassName() {
        return AdapterView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setScrollable(isScrollableForAccessibility());
        View selectedView = getSelectedView();
        if (selectedView != null) {
            info.setEnabled(selectedView.isEnabled());
        }
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setScrollable(isScrollableForAccessibility());
        View selectedView = getSelectedView();
        if (selectedView != null) {
            event.setEnabled(selectedView.isEnabled());
        }
        event.setCurrentItemIndex(getSelectedItemPosition());
        event.setFromIndex(getFirstVisiblePosition());
        event.setToIndex(getLastVisiblePosition());
        event.setItemCount(getCount());
    }

    private boolean isScrollableForAccessibility() {
        T adapter = getAdapter();
        boolean z = false;
        if (adapter == null) {
            return false;
        }
        int itemCount = adapter.getCount();
        if (itemCount > 0 && (getFirstVisiblePosition() > 0 || getLastVisiblePosition() < itemCount - 1)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean canAnimate() {
        return super.canAnimate() && this.mItemCount > 0;
    }

    /* access modifiers changed from: package-private */
    public void handleDataChanged() {
        int count = this.mItemCount;
        boolean found = false;
        if (count > 0) {
            if (this.mNeedSync) {
                this.mNeedSync = false;
                int newPos = findSyncPosition();
                if (newPos >= 0 && lookForSelectablePosition(newPos, true) == newPos) {
                    setNextSelectedPositionInt(newPos);
                    found = true;
                }
            }
            if (!found) {
                int newPos2 = getSelectedItemPosition();
                if (newPos2 >= count) {
                    newPos2 = count - 1;
                }
                if (newPos2 < 0) {
                    newPos2 = 0;
                }
                int selectablePos = lookForSelectablePosition(newPos2, true);
                if (selectablePos < 0) {
                    selectablePos = lookForSelectablePosition(newPos2, false);
                }
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    checkSelectionChanged();
                    found = true;
                }
            }
        }
        if (!found) {
            this.mSelectedPosition = -1;
            this.mSelectedRowId = Long.MIN_VALUE;
            this.mNextSelectedPosition = -1;
            this.mNextSelectedRowId = Long.MIN_VALUE;
            this.mNeedSync = false;
            checkSelectionChanged();
        }
        notifySubtreeAccessibilityStateChangedIfNeeded();
    }

    /* access modifiers changed from: package-private */
    public void checkSelectionChanged() {
        if (!(this.mSelectedPosition == this.mOldSelectedPosition && this.mSelectedRowId == this.mOldSelectedRowId)) {
            selectionChanged();
            this.mOldSelectedPosition = this.mSelectedPosition;
            this.mOldSelectedRowId = this.mSelectedRowId;
        }
        if (this.mPendingSelectionNotifier != null) {
            this.mPendingSelectionNotifier.run();
        }
    }

    /* access modifiers changed from: package-private */
    public int findSyncPosition() {
        int count = this.mItemCount;
        if (count == 0) {
            return -1;
        }
        long idToMatch = this.mSyncRowId;
        int seed = this.mSyncPosition;
        if (idToMatch == Long.MIN_VALUE) {
            return -1;
        }
        int seed2 = Math.min(count - 1, Math.max(0, seed));
        long endTime = SystemClock.uptimeMillis() + 100;
        int first = seed2;
        int last = seed2;
        boolean next = false;
        T adapter = getAdapter();
        if (adapter == null) {
            return -1;
        }
        while (SystemClock.uptimeMillis() <= endTime) {
            if (adapter.getItemId(seed2) != idToMatch) {
                boolean hitFirst = true;
                boolean hitLast = last == count + -1;
                if (first != 0) {
                    hitFirst = false;
                }
                if (hitLast && hitFirst) {
                    break;
                } else if (hitFirst || (next && !hitLast)) {
                    last++;
                    seed2 = last;
                    next = false;
                } else if (hitLast || (!next && !hitFirst)) {
                    first--;
                    seed2 = first;
                    next = true;
                }
            } else {
                return seed2;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int lookForSelectablePosition(int position, boolean lookDown) {
        return position;
    }

    /* access modifiers changed from: package-private */
    public void setSelectedPositionInt(int position) {
        this.mSelectedPosition = position;
        this.mSelectedRowId = getItemIdAtPosition(position);
    }

    /* access modifiers changed from: package-private */
    public void setNextSelectedPositionInt(int position) {
        this.mNextSelectedPosition = position;
        this.mNextSelectedRowId = getItemIdAtPosition(position);
        if (this.mNeedSync && this.mSyncMode == 0 && position >= 0) {
            this.mSyncPosition = position;
            this.mSyncRowId = this.mNextSelectedRowId;
        }
    }

    /* access modifiers changed from: package-private */
    public void rememberSyncState() {
        if (getChildCount() > 0) {
            this.mNeedSync = true;
            this.mSyncHeight = (long) this.mLayoutHeight;
            if (this.mSelectedPosition >= 0) {
                View v = getChildAt(this.mSelectedPosition - this.mFirstPosition);
                this.mSyncRowId = this.mNextSelectedRowId;
                this.mSyncPosition = this.mNextSelectedPosition;
                if (v != null) {
                    this.mSpecificTop = v.getTop();
                }
                this.mSyncMode = 0;
                return;
            }
            View v2 = getChildAt(0);
            T adapter = getAdapter();
            if (this.mFirstPosition < 0 || this.mFirstPosition >= adapter.getCount()) {
                this.mSyncRowId = -1;
            } else {
                try {
                    this.mSyncRowId = adapter.getItemId(this.mFirstPosition);
                } catch (StaleDataException e) {
                    this.mSyncRowId = -1;
                }
            }
            this.mSyncPosition = this.mFirstPosition;
            if (v2 != null) {
                this.mSpecificTop = v2.getTop();
            }
            this.mSyncMode = 1;
        }
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("scrolling:firstPosition", this.mFirstPosition);
        encoder.addProperty("list:nextSelectedPosition", this.mNextSelectedPosition);
        encoder.addProperty("list:nextSelectedRowId", (float) this.mNextSelectedRowId);
        encoder.addProperty("list:selectedPosition", this.mSelectedPosition);
        encoder.addProperty("list:itemCount", this.mItemCount);
    }

    public void onProvideAutofillStructure(ViewStructure structure, int flags) {
        super.onProvideAutofillStructure(structure, flags);
        Adapter adapter = getAdapter();
        if (adapter != null) {
            CharSequence[] options = adapter.getAutofillOptions();
            if (options != null) {
                structure.setAutofillOptions(options);
            }
        }
    }
}
