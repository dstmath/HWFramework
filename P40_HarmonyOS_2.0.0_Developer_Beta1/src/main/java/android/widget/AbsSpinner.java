package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillValue;
import android.widget.AdapterView;
import com.android.internal.R;
import java.lang.annotation.RCUnownedThisRef;

public abstract class AbsSpinner extends AdapterView<SpinnerAdapter> {
    private static final String LOG_TAG = AbsSpinner.class.getSimpleName();
    SpinnerAdapter mAdapter;
    private DataSetObserver mDataSetObserver;
    int mHeightMeasureSpec;
    final RecycleBin mRecycler;
    int mSelectionBottomPadding;
    int mSelectionLeftPadding;
    int mSelectionRightPadding;
    int mSelectionTopPadding;
    final Rect mSpinnerPadding;
    private Rect mTouchFrame;
    int mWidthMeasureSpec;

    /* access modifiers changed from: package-private */
    public abstract void layout(int i, boolean z);

    public AbsSpinner(Context context) {
        super(context);
        this.mSelectionLeftPadding = 0;
        this.mSelectionTopPadding = 0;
        this.mSelectionRightPadding = 0;
        this.mSelectionBottomPadding = 0;
        this.mSpinnerPadding = new Rect();
        this.mRecycler = new RecycleBin();
        initAbsSpinner();
    }

    public AbsSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AbsSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSelectionLeftPadding = 0;
        this.mSelectionTopPadding = 0;
        this.mSelectionRightPadding = 0;
        this.mSelectionBottomPadding = 0;
        this.mSpinnerPadding = new Rect();
        this.mRecycler = new RecycleBin();
        if (getImportantForAutofill() == 0) {
            setImportantForAutofill(1);
        }
        initAbsSpinner();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AbsSpinner, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.AbsSpinner, attrs, a, defStyleAttr, defStyleRes);
        CharSequence[] entries = a.getTextArray(0);
        if (entries != null) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, 17367048, entries);
            adapter.setDropDownViewResource(17367049);
            setAdapter((SpinnerAdapter) adapter);
        }
        a.recycle();
    }

    private void initAbsSpinner() {
        setFocusable(true);
        setWillNotDraw(false);
    }

    public void setAdapter(SpinnerAdapter adapter) {
        SpinnerAdapter spinnerAdapter = this.mAdapter;
        if (spinnerAdapter != null) {
            spinnerAdapter.unregisterDataSetObserver(this.mDataSetObserver);
            resetList();
        }
        this.mAdapter = adapter;
        int position = -1;
        this.mOldSelectedPosition = -1;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        if (this.mAdapter != null) {
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
            checkFocus();
            this.mDataSetObserver = new AdapterView.AdapterDataSetObserver();
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            if (this.mItemCount > 0) {
                position = 0;
            }
            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);
            if (this.mItemCount == 0) {
                checkSelectionChanged();
            }
        } else {
            checkFocus();
            resetList();
            checkSelectionChanged();
        }
        requestLayout();
    }

    /* access modifiers changed from: package-private */
    public void resetList() {
        this.mDataChanged = false;
        this.mNeedSync = false;
        removeAllViewsInLayout();
        this.mOldSelectedPosition = -1;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        setSelectedPositionInt(-1);
        setNextSelectedPositionInt(-1);
        invalidate();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        SpinnerAdapter spinnerAdapter;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        Rect rect = this.mSpinnerPadding;
        int i = this.mPaddingLeft;
        int i2 = this.mSelectionLeftPadding;
        if (i > i2) {
            i2 = this.mPaddingLeft;
        }
        rect.left = i2;
        Rect rect2 = this.mSpinnerPadding;
        int i3 = this.mPaddingTop;
        int i4 = this.mSelectionTopPadding;
        if (i3 > i4) {
            i4 = this.mPaddingTop;
        }
        rect2.top = i4;
        Rect rect3 = this.mSpinnerPadding;
        int i5 = this.mPaddingRight;
        int i6 = this.mSelectionRightPadding;
        if (i5 > i6) {
            i6 = this.mPaddingRight;
        }
        rect3.right = i6;
        Rect rect4 = this.mSpinnerPadding;
        int i7 = this.mPaddingBottom;
        int i8 = this.mSelectionBottomPadding;
        if (i7 > i8) {
            i8 = this.mPaddingBottom;
        }
        rect4.bottom = i8;
        if (this.mDataChanged) {
            handleDataChanged();
        }
        int preferredHeight = 0;
        int preferredWidth = 0;
        boolean needsMeasuring = true;
        int selectedPosition = getSelectedItemPosition();
        if (selectedPosition >= 0 && (spinnerAdapter = this.mAdapter) != null && selectedPosition < spinnerAdapter.getCount()) {
            View view = this.mRecycler.get(selectedPosition);
            if (view == null) {
                view = this.mAdapter.getView(selectedPosition, null, this);
                if (view.getImportantForAccessibility() == 0) {
                    view.setImportantForAccessibility(1);
                }
            }
            this.mRecycler.put(selectedPosition, view);
            if (view.getLayoutParams() == null) {
                this.mBlockLayoutRequests = true;
                view.setLayoutParams(generateDefaultLayoutParams());
                this.mBlockLayoutRequests = false;
            }
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            preferredHeight = getChildHeight(view) + this.mSpinnerPadding.top + this.mSpinnerPadding.bottom;
            preferredWidth = getChildWidth(view) + this.mSpinnerPadding.left + this.mSpinnerPadding.right;
            needsMeasuring = false;
        }
        if (needsMeasuring) {
            preferredHeight = this.mSpinnerPadding.top + this.mSpinnerPadding.bottom;
            if (widthMode == 0) {
                preferredWidth = this.mSpinnerPadding.left + this.mSpinnerPadding.right;
            }
        }
        int preferredHeight2 = Math.max(preferredHeight, getSuggestedMinimumHeight());
        setMeasuredDimension(resolveSizeAndState(Math.max(preferredWidth, getSuggestedMinimumWidth()), widthMeasureSpec, 0), resolveSizeAndState(preferredHeight2, heightMeasureSpec, 0));
        this.mHeightMeasureSpec = heightMeasureSpec;
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    /* access modifiers changed from: package-private */
    public int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    /* access modifiers changed from: package-private */
    public int getChildWidth(View child) {
        return child.getMeasuredWidth();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.LayoutParams(-1, -2);
    }

    /* access modifiers changed from: package-private */
    public void recycleAllViews() {
        int childCount = getChildCount();
        RecycleBin recycleBin = this.mRecycler;
        int position = this.mFirstPosition;
        for (int i = 0; i < childCount; i++) {
            recycleBin.put(position + i, getChildAt(i));
        }
    }

    public void setSelection(int position, boolean animate) {
        boolean shouldAnimate = true;
        if (!animate || this.mFirstPosition > position || position > (this.mFirstPosition + getChildCount()) - 1) {
            shouldAnimate = false;
        }
        setSelectionInt(position, shouldAnimate);
    }

    @Override // android.widget.AdapterView
    public void setSelection(int position) {
        setNextSelectedPositionInt(position);
        requestLayout();
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setSelectionInt(int position, boolean animate) {
        if (position != this.mOldSelectedPosition) {
            this.mBlockLayoutRequests = true;
            setNextSelectedPositionInt(position);
            layout(position - this.mSelectedPosition, animate);
            this.mBlockLayoutRequests = false;
        }
    }

    @Override // android.widget.AdapterView
    public View getSelectedView() {
        if (this.mItemCount <= 0 || this.mSelectedPosition < 0) {
            return null;
        }
        return getChildAt(this.mSelectedPosition - this.mFirstPosition);
    }

    @Override // android.view.View, android.view.ViewParent
    public void requestLayout() {
        if (!this.mBlockLayoutRequests) {
            super.requestLayout();
        }
    }

    @Override // android.widget.AdapterView
    public SpinnerAdapter getAdapter() {
        return this.mAdapter;
    }

    @Override // android.widget.AdapterView
    public int getCount() {
        return this.mItemCount;
    }

    public int pointToPosition(int x, int y) {
        Rect frame = this.mTouchFrame;
        if (frame == null) {
            this.mTouchFrame = new Rect();
            frame = this.mTouchFrame;
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return this.mFirstPosition + i;
                }
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchRestoreInstanceState(container);
        handleDataChanged();
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.widget.AbsSpinner.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int position;
        long selectedId;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel in) {
            super(in);
            this.selectedId = in.readLong();
            this.position = in.readInt();
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(this.selectedId);
            out.writeInt(this.position);
        }

        public String toString() {
            return "AbsSpinner.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId=" + this.selectedId + " position=" + this.position + "}";
        }
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.selectedId = getSelectedItemId();
        if (ss.selectedId >= 0) {
            ss.position = getSelectedItemPosition();
        } else {
            ss.position = -1;
        }
        return ss;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.selectedId >= 0) {
            this.mDataChanged = true;
            this.mNeedSync = true;
            this.mSyncRowId = ss.selectedId;
            this.mSyncPosition = ss.position;
            this.mSyncMode = 0;
            requestLayout();
        }
    }

    @RCUnownedThisRef
    class RecycleBin {
        private final SparseArray<View> mScrapHeap = new SparseArray<>();

        RecycleBin() {
        }

        public void put(int position, View v) {
            this.mScrapHeap.put(position, v);
        }

        /* access modifiers changed from: package-private */
        public View get(int position) {
            View result = this.mScrapHeap.get(position);
            if (result != null) {
                this.mScrapHeap.delete(position);
            }
            return result;
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            SparseArray<View> scrapHeap = this.mScrapHeap;
            int count = scrapHeap.size();
            for (int i = 0; i < count; i++) {
                View view = scrapHeap.valueAt(i);
                if (view != null) {
                    AbsSpinner.this.removeDetachedView(view, true);
                }
            }
            scrapHeap.clear();
        }
    }

    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return AbsSpinner.class.getName();
    }

    @Override // android.view.View
    public void autofill(AutofillValue value) {
        if (isEnabled()) {
            if (!value.isList()) {
                String str = LOG_TAG;
                Log.w(str, value + " could not be autofilled into " + this);
                return;
            }
            setSelection(value.getListValue());
        }
    }

    @Override // android.view.View
    public int getAutofillType() {
        return isEnabled() ? 3 : 0;
    }

    @Override // android.view.View
    public AutofillValue getAutofillValue() {
        if (isEnabled()) {
            return AutofillValue.forList(getSelectedItemPosition());
        }
        return null;
    }
}
