package android.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import com.android.internal.R;
import com.android.internal.view.menu.ShowableListMenu;
import java.lang.annotation.RCUnownedThisRef;

public class Spinner extends AbsSpinner implements DialogInterface.OnClickListener {
    private static final int MAX_ITEMS_MEASURED = 15;
    public static final int MODE_DIALOG = 0;
    public static final int MODE_DROPDOWN = 1;
    private static final int MODE_THEME = -1;
    private static final String TAG = "Spinner";
    private boolean mDisableChildrenWhenDisabled;
    int mDropDownWidth;
    private ForwardingListener mForwardingListener;
    private int mGravity;
    /* access modifiers changed from: private */
    public SpinnerPopup mPopup;
    private final Context mPopupContext;
    private SpinnerAdapter mTempAdapter;
    /* access modifiers changed from: private */
    public final Rect mTempRect;

    private class DialogPopup implements SpinnerPopup, DialogInterface.OnClickListener {
        private ListAdapter mListAdapter;
        private AlertDialog mPopup;
        private CharSequence mPrompt;

        private DialogPopup() {
        }

        public void dismiss() {
            if (this.mPopup != null) {
                this.mPopup.dismiss();
                this.mPopup = null;
            }
        }

        public boolean isShowing() {
            if (this.mPopup != null) {
                return this.mPopup.isShowing();
            }
            return false;
        }

        public void setAdapter(ListAdapter adapter) {
            this.mListAdapter = adapter;
        }

        public void setPromptText(CharSequence hintText) {
            this.mPrompt = hintText;
        }

        public CharSequence getHintText() {
            return this.mPrompt;
        }

        public void show(int textDirection, int textAlignment) {
            if (this.mListAdapter != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Spinner.this.getPopupContext());
                if (this.mPrompt != null) {
                    builder.setTitle(this.mPrompt);
                }
                this.mPopup = builder.setSingleChoiceItems(this.mListAdapter, Spinner.this.getSelectedItemPosition(), this).create();
                ListView listView = this.mPopup.getListView();
                listView.setTextDirection(textDirection);
                listView.setTextAlignment(textAlignment);
                this.mPopup.show();
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            Spinner.this.setSelection(which);
            if (Spinner.this.mOnItemClickListener != null) {
                Spinner.this.performItemClick(null, which, this.mListAdapter.getItemId(which));
            }
            dismiss();
        }

        public void setBackgroundDrawable(Drawable bg) {
            Log.e(Spinner.TAG, "Cannot set popup background for MODE_DIALOG, ignoring");
        }

        public void setVerticalOffset(int px) {
            Log.e(Spinner.TAG, "Cannot set vertical offset for MODE_DIALOG, ignoring");
        }

        public void setHorizontalOffset(int px) {
            Log.e(Spinner.TAG, "Cannot set horizontal offset for MODE_DIALOG, ignoring");
        }

        public Drawable getBackground() {
            return null;
        }

        public int getVerticalOffset() {
            return 0;
        }

        public int getHorizontalOffset() {
            return 0;
        }
    }

    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        public DropDownAdapter(SpinnerAdapter adapter, Resources.Theme dropDownTheme) {
            this.mAdapter = adapter;
            if (adapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter) adapter;
            }
            if (dropDownTheme != null && (adapter instanceof ThemedSpinnerAdapter)) {
                ThemedSpinnerAdapter themedAdapter = (ThemedSpinnerAdapter) adapter;
                if (themedAdapter.getDropDownViewTheme() == null) {
                    themedAdapter.setDropDownViewTheme(dropDownTheme);
                }
            }
        }

        public int getCount() {
            if (this.mAdapter == null) {
                return 0;
            }
            return this.mAdapter.getCount();
        }

        public Object getItem(int position) {
            if (this.mAdapter == null) {
                return null;
            }
            return this.mAdapter.getItem(position);
        }

        public long getItemId(int position) {
            if (this.mAdapter == null) {
                return -1;
            }
            return this.mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (this.mAdapter == null) {
                return null;
            }
            return this.mAdapter.getDropDownView(position, convertView, parent);
        }

        public boolean hasStableIds() {
            return this.mAdapter != null && this.mAdapter.hasStableIds();
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterDataSetObserver(observer);
            }
        }

        public boolean areAllItemsEnabled() {
            ListAdapter adapter = this.mListAdapter;
            if (adapter != null) {
                return adapter.areAllItemsEnabled();
            }
            return true;
        }

        public boolean isEnabled(int position) {
            ListAdapter adapter = this.mListAdapter;
            if (adapter != null) {
                return adapter.isEnabled(position);
            }
            return true;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    private class DropdownPopup extends ListPopupWindow implements SpinnerPopup {
        /* access modifiers changed from: private */
        public ListAdapter mAdapter;
        private CharSequence mHintText;

        public DropdownPopup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            setAnchorView(Spinner.this);
            setModal(true);
            setPromptPosition(0);
            setOnItemClickListener(new AdapterView.OnItemClickListener(Spinner.this) {
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    Spinner.this.setSelection(position);
                    if (Spinner.this.mOnItemClickListener != null) {
                        Spinner.this.performItemClick(v, position, DropdownPopup.this.mAdapter.getItemId(position));
                    }
                    DropdownPopup.this.dismiss();
                }
            });
        }

        public void setAdapter(ListAdapter adapter) {
            super.setAdapter(adapter);
            this.mAdapter = adapter;
        }

        public CharSequence getHintText() {
            return this.mHintText;
        }

        public void setPromptText(CharSequence hintText) {
            this.mHintText = hintText;
        }

        /* access modifiers changed from: package-private */
        public void computeContentWidth() {
            int hOffset;
            Drawable background = getBackground();
            int hOffset2 = 0;
            if (background != null) {
                background.getPadding(Spinner.this.mTempRect);
                hOffset2 = Spinner.this.isLayoutRtl() ? Spinner.this.mTempRect.right : -Spinner.this.mTempRect.left;
            } else {
                Rect access$400 = Spinner.this.mTempRect;
                Spinner.this.mTempRect.right = 0;
                access$400.left = 0;
            }
            int spinnerPaddingLeft = Spinner.this.getPaddingLeft();
            int spinnerPaddingRight = Spinner.this.getPaddingRight();
            int spinnerWidth = Spinner.this.getWidth();
            if (Spinner.this.mDropDownWidth == -2) {
                int contentWidth = Spinner.this.measureContentWidth((SpinnerAdapter) this.mAdapter, getBackground());
                int contentWidthLimit = (Spinner.this.mContext.getResources().getDisplayMetrics().widthPixels - Spinner.this.mTempRect.left) - Spinner.this.mTempRect.right;
                if (contentWidth > contentWidthLimit) {
                    contentWidth = contentWidthLimit;
                }
                setContentWidth(Math.max(contentWidth, (spinnerWidth - spinnerPaddingLeft) - spinnerPaddingRight));
            } else if (Spinner.this.mDropDownWidth == -1) {
                setContentWidth((spinnerWidth - spinnerPaddingLeft) - spinnerPaddingRight);
            } else {
                setContentWidth(Spinner.this.mDropDownWidth);
            }
            if (Spinner.this.isLayoutRtl()) {
                hOffset = hOffset2 + ((spinnerWidth - spinnerPaddingRight) - getWidth());
            } else {
                hOffset = hOffset2 + spinnerPaddingLeft;
            }
            setHorizontalOffset(hOffset);
        }

        public void show(int textDirection, int textAlignment) {
            boolean wasShowing = isShowing();
            computeContentWidth();
            setInputMethodMode(2);
            super.show();
            ListView listView = getListView();
            listView.setChoiceMode(1);
            listView.setTextDirection(textDirection);
            listView.setTextAlignment(textAlignment);
            setSelection(Spinner.this.getSelectedItemPosition());
            if (!wasShowing) {
                ViewTreeObserver vto = Spinner.this.getViewTreeObserver();
                if (vto != null) {
                    final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            if (!Spinner.this.isVisibleToUser()) {
                                DropdownPopup.this.dismiss();
                                return;
                            }
                            DropdownPopup.this.computeContentWidth();
                            DropdownPopup.super.show();
                        }
                    };
                    vto.addOnGlobalLayoutListener(layoutListener);
                    setOnDismissListener(new PopupWindow.OnDismissListener() {
                        public void onDismiss() {
                            ViewTreeObserver vto = Spinner.this.getViewTreeObserver();
                            if (vto != null) {
                                vto.removeOnGlobalLayoutListener(layoutListener);
                            }
                        }
                    });
                }
            }
        }
    }

    static class SavedState extends AbsSpinner.SavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean showDropdown;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.showDropdown = in.readByte() != 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte(this.showDropdown ? (byte) 1 : 0);
        }
    }

    private interface SpinnerPopup {
        void dismiss();

        Drawable getBackground();

        CharSequence getHintText();

        int getHorizontalOffset();

        int getVerticalOffset();

        boolean isShowing();

        void setAdapter(ListAdapter listAdapter);

        void setBackgroundDrawable(Drawable drawable);

        void setHorizontalOffset(int i);

        void setPromptText(CharSequence charSequence);

        void setVerticalOffset(int i);

        void show(int i, int i2);
    }

    public Spinner(Context context) {
        this(context, (AttributeSet) null);
    }

    public Spinner(Context context, int mode) {
        this(context, null, 16842881, mode);
    }

    public Spinner(Context context, AttributeSet attrs) {
        this(context, attrs, 16842881);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0, -1);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, 0, mode);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        this(context, attrs, defStyleAttr, defStyleRes, mode, null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes);
        int mode2;
        Context context2 = context;
        AttributeSet attributeSet = attrs;
        int i = defStyleAttr;
        int i2 = defStyleRes;
        Resources.Theme theme = popupTheme;
        this.mTempRect = new Rect();
        TypedArray a = context2.obtainStyledAttributes(attributeSet, R.styleable.Spinner, i, i2);
        if (theme != null) {
            this.mPopupContext = new ContextThemeWrapper(context2, theme);
        } else {
            int popupThemeResId = a.getResourceId(7, 0);
            if (popupThemeResId != 0) {
                this.mPopupContext = new ContextThemeWrapper(context2, popupThemeResId);
            } else {
                this.mPopupContext = context2;
            }
        }
        int i3 = mode;
        if (i3 == -1) {
            mode2 = a.getInt(5, 0);
        } else {
            mode2 = i3;
        }
        switch (mode2) {
            case 0:
                this.mPopup = new DialogPopup();
                this.mPopup.setPromptText(a.getString(3));
                break;
            case 1:
                DropdownPopup popup = new DropdownPopup(this.mPopupContext, attributeSet, i, i2);
                TypedArray pa = this.mPopupContext.obtainStyledAttributes(attributeSet, R.styleable.Spinner, i, i2);
                this.mDropDownWidth = pa.getLayoutDimension(4, -2);
                if (pa.hasValueOrEmpty(1)) {
                    popup.setListSelector(pa.getDrawable(1));
                }
                popup.setBackgroundDrawable(pa.getDrawable(2));
                popup.setPromptText(a.getString(3));
                pa.recycle();
                this.mPopup = popup;
                this.mForwardingListener = new ForwardingListener(this) {
                    @RCUnownedThisRef
                    public ShowableListMenu getPopup() {
                        return Spinner.this.mPopup;
                    }

                    public boolean onForwardingStarted() {
                        if (!Spinner.this.mPopup.isShowing()) {
                            Spinner.this.mPopup.show(Spinner.this.getTextDirection(), Spinner.this.getTextAlignment());
                        }
                        return true;
                    }
                };
                break;
        }
        this.mGravity = a.getInt(0, 17);
        this.mDisableChildrenWhenDisabled = a.getBoolean(8, false);
        a.recycle();
        if (this.mTempAdapter != null) {
            setAdapter(this.mTempAdapter);
            this.mTempAdapter = null;
        }
    }

    public Context getPopupContext() {
        return this.mPopupContext;
    }

    public void setPopupBackgroundDrawable(Drawable background) {
        if (!(this.mPopup instanceof DropdownPopup)) {
            Log.e(TAG, "setPopupBackgroundDrawable: incompatible spinner mode; ignoring...");
        } else {
            this.mPopup.setBackgroundDrawable(background);
        }
    }

    public void setPopupBackgroundResource(int resId) {
        setPopupBackgroundDrawable(getPopupContext().getDrawable(resId));
    }

    public Drawable getPopupBackground() {
        return this.mPopup.getBackground();
    }

    public boolean isPopupShowing() {
        return this.mPopup != null && this.mPopup.isShowing();
    }

    public void setDropDownVerticalOffset(int pixels) {
        this.mPopup.setVerticalOffset(pixels);
    }

    public int getDropDownVerticalOffset() {
        return this.mPopup.getVerticalOffset();
    }

    public void setDropDownHorizontalOffset(int pixels) {
        this.mPopup.setHorizontalOffset(pixels);
    }

    public int getDropDownHorizontalOffset() {
        return this.mPopup.getHorizontalOffset();
    }

    public void setDropDownWidth(int pixels) {
        if (!(this.mPopup instanceof DropdownPopup)) {
            Log.e(TAG, "Cannot set dropdown width for MODE_DIALOG, ignoring");
        } else {
            this.mDropDownWidth = pixels;
        }
    }

    public int getDropDownWidth() {
        return this.mDropDownWidth;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mDisableChildrenWhenDisabled) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).setEnabled(enabled);
            }
        }
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            if ((gravity & 7) == 0) {
                gravity |= Gravity.START;
            }
            this.mGravity = gravity;
            requestLayout();
        }
    }

    public int getGravity() {
        return this.mGravity;
    }

    public void setAdapter(SpinnerAdapter adapter) {
        if (this.mPopup == null) {
            this.mTempAdapter = adapter;
            return;
        }
        super.setAdapter(adapter);
        this.mRecycler.clear();
        if (this.mContext.getApplicationInfo().targetSdkVersion < 21 || adapter == null || adapter.getViewTypeCount() == 1) {
            this.mPopup.setAdapter(new DropDownAdapter(adapter, (this.mPopupContext == null ? this.mContext : this.mPopupContext).getTheme()));
            return;
        }
        throw new IllegalArgumentException("Spinner adapter view type count must be 1");
    }

    public int getBaseline() {
        View child = null;
        if (getChildCount() > 0) {
            child = getChildAt(0);
        } else if (this.mAdapter != null && this.mAdapter.getCount() > 0) {
            child = makeView(0, false);
            this.mRecycler.put(0, child);
        }
        int i = -1;
        if (child == null) {
            return -1;
        }
        int childBaseline = child.getBaseline();
        if (childBaseline >= 0) {
            i = child.getTop() + childBaseline;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        throw new RuntimeException("setOnItemClickListener cannot be used with a spinner.");
    }

    public void setOnItemClickListenerInt(AdapterView.OnItemClickListener l) {
        super.setOnItemClickListener(l);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mForwardingListener == null || !this.mForwardingListener.onTouch(this, event)) {
            return super.onTouchEvent(event);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mPopup != null && View.MeasureSpec.getMode(widthMeasureSpec) == Integer.MIN_VALUE) {
            setMeasuredDimension(Math.min(Math.max(getMeasuredWidth(), measureContentWidth(getAdapter(), getBackground())), View.MeasureSpec.getSize(widthMeasureSpec)), getMeasuredHeight());
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        layout(0, false);
        this.mInLayout = false;
    }

    /* access modifiers changed from: package-private */
    public void layout(int delta, boolean animate) {
        int childrenLeft = this.mSpinnerPadding.left;
        int childrenWidth = ((this.mRight - this.mLeft) - this.mSpinnerPadding.left) - this.mSpinnerPadding.right;
        if (this.mDataChanged) {
            handleDataChanged();
        }
        if (this.mItemCount == 0) {
            resetList();
            return;
        }
        if (this.mNextSelectedPosition >= 0) {
            setSelectedPositionInt(this.mNextSelectedPosition);
        }
        recycleAllViews();
        removeAllViewsInLayout();
        this.mFirstPosition = this.mSelectedPosition;
        if (this.mAdapter != null) {
            View sel = makeView(this.mSelectedPosition, true);
            int width = sel.getMeasuredWidth();
            int selectedOffset = childrenLeft;
            int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, getLayoutDirection()) & 7;
            if (absoluteGravity == 1) {
                selectedOffset = ((childrenWidth / 2) + childrenLeft) - (width / 2);
            } else if (absoluteGravity == 5) {
                selectedOffset = (childrenLeft + childrenWidth) - width;
            }
            sel.offsetLeftAndRight(selectedOffset);
        }
        this.mRecycler.clear();
        invalidate();
        checkSelectionChanged();
        this.mDataChanged = false;
        this.mNeedSync = false;
        setNextSelectedPositionInt(this.mSelectedPosition);
    }

    private View makeView(int position, boolean addChild) {
        if (!this.mDataChanged) {
            View child = this.mRecycler.get(position);
            if (child != null) {
                setUpChild(child, addChild);
                return child;
            }
        }
        View child2 = this.mAdapter.getView(position, null, this);
        setUpChild(child2, addChild);
        return child2;
    }

    private void setUpChild(View child, boolean addChild) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if (lp == null) {
            lp = generateDefaultLayoutParams();
        }
        addViewInLayout(child, 0, lp);
        child.setSelected(hasFocus());
        if (this.mDisableChildrenWhenDisabled) {
            child.setEnabled(isEnabled());
        }
        child.measure(ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mSpinnerPadding.left + this.mSpinnerPadding.right, lp.width), ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mSpinnerPadding.top + this.mSpinnerPadding.bottom, lp.height));
        int childTop = this.mSpinnerPadding.top + ((((getMeasuredHeight() - this.mSpinnerPadding.bottom) - this.mSpinnerPadding.top) - child.getMeasuredHeight()) / 2);
        child.layout(0, childTop, 0 + child.getMeasuredWidth(), child.getMeasuredHeight() + childTop);
        if (!addChild) {
            removeViewInLayout(child);
        }
    }

    public boolean performClick() {
        boolean handled = super.performClick();
        if (!handled) {
            handled = true;
            if (!this.mPopup.isShowing()) {
                this.mPopup.show(getTextDirection(), getTextAlignment());
            }
        }
        return handled;
    }

    public void onClick(DialogInterface dialog, int which) {
        setSelection(which);
        dialog.dismiss();
    }

    public CharSequence getAccessibilityClassName() {
        return Spinner.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (this.mAdapter != null) {
            info.setCanOpenPopup(true);
        }
    }

    public void setPrompt(CharSequence prompt) {
        this.mPopup.setPromptText(prompt);
    }

    public void setPromptId(int promptId) {
        setPrompt(getContext().getText(promptId));
    }

    public CharSequence getPrompt() {
        return this.mPopup.getHintText();
    }

    /* access modifiers changed from: package-private */
    public int measureContentWidth(SpinnerAdapter adapter, Drawable background) {
        if (adapter == null) {
            return 0;
        }
        View itemView = null;
        int itemType = 0;
        int widthMeasureSpec = View.MeasureSpec.makeSafeMeasureSpec(getMeasuredWidth(), 0);
        int heightMeasureSpec = View.MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
        int start = Math.max(0, getSelectedItemPosition());
        int end = Math.min(adapter.getCount(), start + 15);
        int width = 0;
        for (int i = Math.max(0, start - (15 - (end - start))); i < end; i++) {
            int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            itemView = adapter.getView(i, itemView, this);
            if (itemView.getLayoutParams() == null) {
                itemView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            }
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, itemView.getMeasuredWidth());
        }
        if (background != null) {
            background.getPadding(this.mTempRect);
            width += this.mTempRect.left + this.mTempRect.right;
        }
        return width;
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.showDropdown = this.mPopup != null && this.mPopup.isShowing();
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.showDropdown) {
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        if (!Spinner.this.mPopup.isShowing()) {
                            Spinner.this.mPopup.show(Spinner.this.getTextDirection(), Spinner.this.getTextAlignment());
                        }
                        ViewTreeObserver vto = Spinner.this.getViewTreeObserver();
                        if (vto != null) {
                            vto.removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        if (getPointerIcon() != null || !isClickable() || !isEnabled()) {
            return super.onResolvePointerIcon(event, pointerIndex);
        }
        return PointerIcon.getSystemIcon(getContext(), 1002);
    }
}
