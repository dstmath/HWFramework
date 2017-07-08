package android.widget;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;
import com.android.internal.R;
import com.android.internal.view.menu.ShowableListMenu;
import huawei.cust.HwCfgFilePolicy;

public class Spinner extends AbsSpinner implements OnClickListener {
    private static final int MAX_ITEMS_MEASURED = 15;
    public static final int MODE_DIALOG = 0;
    public static final int MODE_DROPDOWN = 1;
    private static final int MODE_THEME = -1;
    private static final String TAG = "Spinner";
    private boolean mDisableChildrenWhenDisabled;
    int mDropDownWidth;
    private ForwardingListener mForwardingListener;
    private int mGravity;
    private SpinnerPopup mPopup;
    private final Context mPopupContext;
    private SpinnerAdapter mTempAdapter;
    private final Rect mTempRect;

    /* renamed from: android.widget.Spinner.1 */
    class AnonymousClass1 extends ForwardingListener {
        final /* synthetic */ DropdownPopup val$popup;

        AnonymousClass1(View $anonymous0, DropdownPopup val$popup) {
            this.val$popup = val$popup;
            super($anonymous0);
        }

        public ShowableListMenu getPopup() {
            return this.val$popup;
        }

        public boolean onForwardingStarted() {
            if (!Spinner.this.mPopup.isShowing()) {
                Spinner.this.mPopup.show(Spinner.this.getTextDirection(), Spinner.this.getTextAlignment());
            }
            return true;
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

    private class DialogPopup implements SpinnerPopup, OnClickListener {
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
            return this.mPopup != null ? this.mPopup.isShowing() : false;
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
                Builder builder = new Builder(Spinner.this.getPopupContext());
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
            return Spinner.MODE_DIALOG;
        }

        public int getHorizontalOffset() {
            return Spinner.MODE_DIALOG;
        }
    }

    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        public DropDownAdapter(SpinnerAdapter adapter, Theme dropDownTheme) {
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
            return this.mAdapter == null ? Spinner.MODE_DIALOG : this.mAdapter.getCount();
        }

        public Object getItem(int position) {
            return this.mAdapter == null ? null : this.mAdapter.getItem(position);
        }

        public long getItemId(int position) {
            return this.mAdapter == null ? -1 : this.mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return this.mAdapter == null ? null : this.mAdapter.getDropDownView(position, convertView, parent);
        }

        public boolean hasStableIds() {
            return this.mAdapter != null ? this.mAdapter.hasStableIds() : false;
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
            return Spinner.MODE_DIALOG;
        }

        public int getViewTypeCount() {
            return Spinner.MODE_DROPDOWN;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    private class DropdownPopup extends ListPopupWindow implements SpinnerPopup {
        private ListAdapter mAdapter;
        private CharSequence mHintText;

        /* renamed from: android.widget.Spinner.DropdownPopup.3 */
        class AnonymousClass3 implements OnDismissListener {
            final /* synthetic */ OnGlobalLayoutListener val$layoutListener;

            AnonymousClass3(OnGlobalLayoutListener val$layoutListener) {
                this.val$layoutListener = val$layoutListener;
            }

            public void onDismiss() {
                ViewTreeObserver vto = Spinner.this.getViewTreeObserver();
                if (vto != null) {
                    vto.removeOnGlobalLayoutListener(this.val$layoutListener);
                }
            }
        }

        public DropdownPopup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            setAnchorView(Spinner.this);
            setModal(true);
            setPromptPosition(Spinner.MODE_DIALOG);
            setOnItemClickListener(new OnItemClickListener() {
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

        void computeContentWidth() {
            Drawable background = getBackground();
            int hOffset = Spinner.MODE_DIALOG;
            if (background != null) {
                background.getPadding(Spinner.this.mTempRect);
                hOffset = Spinner.this.isLayoutRtl() ? Spinner.this.mTempRect.right : -Spinner.this.mTempRect.left;
            } else {
                Rect -get2 = Spinner.this.mTempRect;
                Spinner.this.mTempRect.right = Spinner.MODE_DIALOG;
                -get2.left = Spinner.MODE_DIALOG;
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
            } else if (Spinner.this.mDropDownWidth == Spinner.MODE_THEME) {
                setContentWidth((spinnerWidth - spinnerPaddingLeft) - spinnerPaddingRight);
            } else {
                setContentWidth(Spinner.this.mDropDownWidth);
            }
            if (Spinner.this.isLayoutRtl()) {
                hOffset += (spinnerWidth - spinnerPaddingRight) - getWidth();
            } else {
                hOffset += spinnerPaddingLeft;
            }
            setHorizontalOffset(hOffset);
        }

        public void show(int textDirection, int textAlignment) {
            boolean wasShowing = isShowing();
            computeContentWidth();
            setInputMethodMode(2);
            super.show();
            ListView listView = getListView();
            listView.setChoiceMode(Spinner.MODE_DROPDOWN);
            listView.setTextDirection(textDirection);
            listView.setTextAlignment(textAlignment);
            setSelection(Spinner.this.getSelectedItemPosition());
            if (!wasShowing) {
                ViewTreeObserver vto = Spinner.this.getViewTreeObserver();
                if (vto != null) {
                    OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            if (Spinner.this.isVisibleToUser()) {
                                DropdownPopup.this.computeContentWidth();
                                super.show();
                                return;
                            }
                            DropdownPopup.this.dismiss();
                        }
                    };
                    vto.addOnGlobalLayoutListener(layoutListener);
                    setOnDismissListener(new AnonymousClass3(layoutListener));
                }
            }
        }
    }

    static class SavedState extends SavedState {
        public static final Creator<SavedState> CREATOR = null;
        boolean showDropdown;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.Spinner.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.Spinner.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.Spinner.SavedState.<clinit>():void");
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            boolean z = false;
            super(in);
            if (in.readByte() != null) {
                z = true;
            }
            this.showDropdown = z;
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (this.showDropdown ? Spinner.MODE_DROPDOWN : Spinner.MODE_DIALOG));
        }
    }

    public Spinner(Context context) {
        this(context, null);
    }

    public Spinner(Context context, int mode) {
        this(context, null, R.attr.spinnerStyle, mode);
    }

    public Spinner(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.spinnerStyle);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, MODE_DIALOG, MODE_THEME);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, MODE_DIALOG, mode);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        this(context, attrs, defStyleAttr, defStyleRes, mode, null);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode, Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTempRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Spinner, defStyleAttr, defStyleRes);
        if (popupTheme != null) {
            this.mPopupContext = new ContextThemeWrapper(context, popupTheme);
        } else {
            int popupThemeResId = a.getResourceId(7, MODE_DIALOG);
            if (popupThemeResId != 0) {
                this.mPopupContext = new ContextThemeWrapper(context, popupThemeResId);
            } else {
                this.mPopupContext = context;
            }
        }
        if (mode == MODE_THEME) {
            mode = a.getInt(5, MODE_DIALOG);
        }
        switch (mode) {
            case MODE_DIALOG /*0*/:
                this.mPopup = new DialogPopup();
                this.mPopup.setPromptText(a.getString(3));
                break;
            case MODE_DROPDOWN /*1*/:
                DropdownPopup popup = new DropdownPopup(this.mPopupContext, attrs, defStyleAttr, defStyleRes);
                TypedArray pa = this.mPopupContext.obtainStyledAttributes(attrs, R.styleable.Spinner, defStyleAttr, defStyleRes);
                this.mDropDownWidth = pa.getLayoutDimension(4, -2);
                if (pa.hasValueOrEmpty(MODE_DROPDOWN)) {
                    popup.setListSelector(pa.getDrawable(MODE_DROPDOWN));
                }
                popup.setBackgroundDrawable(pa.getDrawable(2));
                popup.setSpinner(true);
                popup.setPromptText(a.getString(3));
                pa.recycle();
                this.mPopup = popup;
                this.mForwardingListener = new AnonymousClass1(this, popup);
                break;
        }
        this.mGravity = a.getInt(MODE_DIALOG, 17);
        this.mDisableChildrenWhenDisabled = a.getBoolean(9, false);
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
        if (this.mPopup instanceof DropdownPopup) {
            this.mPopup.setBackgroundDrawable(background);
        } else {
            Log.e(TAG, "setPopupBackgroundDrawable: incompatible spinner mode; ignoring...");
        }
    }

    public void setPopupBackgroundResource(int resId) {
        setPopupBackgroundDrawable(getPopupContext().getDrawable(resId));
    }

    public Drawable getPopupBackground() {
        return this.mPopup.getBackground();
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
        if (this.mPopup instanceof DropdownPopup) {
            this.mDropDownWidth = pixels;
        } else {
            Log.e(TAG, "Cannot set dropdown width for MODE_DIALOG, ignoring");
        }
    }

    public int getDropDownWidth() {
        return this.mDropDownWidth;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mDisableChildrenWhenDisabled) {
            int count = getChildCount();
            for (int i = MODE_DIALOG; i < count; i += MODE_DROPDOWN) {
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
        if (this.mContext.getApplicationInfo().targetSdkVersion < 21 || adapter == null || adapter.getViewTypeCount() == MODE_DROPDOWN) {
            this.mPopup.setAdapter(new DropDownAdapter(adapter, (this.mPopupContext == null ? this.mContext : this.mPopupContext).getTheme()));
            return;
        }
        throw new IllegalArgumentException("Spinner adapter view type count must be 1");
    }

    public int getBaseline() {
        int i = MODE_THEME;
        View child = null;
        if (getChildCount() > 0) {
            child = getChildAt(MODE_DIALOG);
        } else if (this.mAdapter != null && this.mAdapter.getCount() > 0) {
            child = makeView(MODE_DIALOG, false);
            this.mRecycler.put(MODE_DIALOG, child);
        }
        if (child == null) {
            return MODE_THEME;
        }
        int childBaseline = child.getBaseline();
        if (childBaseline >= 0) {
            i = child.getTop() + childBaseline;
        }
        return i;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        throw new RuntimeException("setOnItemClickListener cannot be used with a spinner.");
    }

    public void setOnItemClickListenerInt(OnItemClickListener l) {
        super.setOnItemClickListener(l);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mForwardingListener == null || !this.mForwardingListener.onTouch(this, event)) {
            return super.onTouchEvent(event);
        }
        return true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mPopup != null && MeasureSpec.getMode(widthMeasureSpec) == RtlSpacingHelper.UNDEFINED) {
            setMeasuredDimension(Math.min(Math.max(getMeasuredWidth(), measureContentWidth(getAdapter(), getBackground())), MeasureSpec.getSize(widthMeasureSpec)), getMeasuredHeight());
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        layout(MODE_DIALOG, false);
        this.mInLayout = false;
    }

    void layout(int delta, boolean animate) {
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
            switch (Gravity.getAbsoluteGravity(this.mGravity, getLayoutDirection()) & 7) {
                case MODE_DROPDOWN /*1*/:
                    selectedOffset = ((childrenWidth / 2) + childrenLeft) - (width / 2);
                    break;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    selectedOffset = (childrenLeft + childrenWidth) - width;
                    break;
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
        View child;
        if (!this.mDataChanged) {
            child = this.mRecycler.get(position);
            if (child != null) {
                setUpChild(child, addChild);
                return child;
            }
        }
        child = this.mAdapter.getView(position, null, this);
        setUpChild(child, addChild);
        return child;
    }

    private void setUpChild(View child, boolean addChild) {
        LayoutParams lp = child.getLayoutParams();
        if (lp == null) {
            lp = generateDefaultLayoutParams();
        }
        addViewInLayout(child, MODE_DIALOG, lp);
        child.setSelected(hasFocus());
        if (this.mDisableChildrenWhenDisabled) {
            child.setEnabled(isEnabled());
        }
        child.measure(ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mSpinnerPadding.left + this.mSpinnerPadding.right, lp.width), ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mSpinnerPadding.top + this.mSpinnerPadding.bottom, lp.height));
        int childTop = this.mSpinnerPadding.top + ((((getMeasuredHeight() - this.mSpinnerPadding.bottom) - this.mSpinnerPadding.top) - child.getMeasuredHeight()) / 2);
        child.layout(MODE_DIALOG, childTop, child.getMeasuredWidth() + MODE_DIALOG, childTop + child.getMeasuredHeight());
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

    int measureContentWidth(SpinnerAdapter adapter, Drawable background) {
        if (adapter == null) {
            return MODE_DIALOG;
        }
        int width = MODE_DIALOG;
        View itemView = null;
        int itemType = MODE_DIALOG;
        int widthMeasureSpec = MeasureSpec.makeSafeMeasureSpec(getMeasuredWidth(), MODE_DIALOG);
        int heightMeasureSpec = MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), MODE_DIALOG);
        int start = Math.max(MODE_DIALOG, getSelectedItemPosition());
        int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
        for (int i = Math.max(MODE_DIALOG, start - (15 - (end - start))); i < end; i += MODE_DROPDOWN) {
            int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            itemView = adapter.getView(i, itemView, this);
            if (itemView.getLayoutParams() == null) {
                itemView.setLayoutParams(new LayoutParams(-2, -2));
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
        ss.showDropdown = this.mPopup != null ? this.mPopup.isShowing() : false;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.showDropdown) {
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
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
}
