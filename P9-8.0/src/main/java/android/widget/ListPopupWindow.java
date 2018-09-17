package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import com.android.internal.R;
import com.android.internal.view.menu.ShowableListMenu;

public class ListPopupWindow implements ShowableListMenu {
    private static final boolean DEBUG = false;
    private static final int EXPAND_LIST_TIMEOUT = 250;
    public static final int INPUT_METHOD_FROM_FOCUSABLE = 0;
    public static final int INPUT_METHOD_NEEDED = 1;
    public static final int INPUT_METHOD_NOT_NEEDED = 2;
    public static final int MATCH_PARENT = -1;
    public static final int POSITION_PROMPT_ABOVE = 0;
    public static final int POSITION_PROMPT_BELOW = 1;
    private static final String TAG = "ListPopupWindow";
    public static final int WRAP_CONTENT = -2;
    private ListAdapter mAdapter;
    private Context mContext;
    private boolean mDropDownAlwaysVisible;
    private View mDropDownAnchorView;
    private int mDropDownGravity;
    private int mDropDownHeight;
    private int mDropDownHorizontalOffset;
    private DropDownListView mDropDownList;
    private Drawable mDropDownListHighlight;
    private int mDropDownVerticalOffset;
    private boolean mDropDownVerticalOffsetSet;
    private int mDropDownWidth;
    private int mDropDownWindowLayoutType;
    private Rect mEpicenterBounds;
    private boolean mForceIgnoreOutsideTouch;
    private final Handler mHandler;
    private final ListSelectorHider mHideSelector;
    private boolean mIsAnimatedFromAnchor;
    private boolean mIsForSpinner;
    private OnItemClickListener mItemClickListener;
    private OnItemSelectedListener mItemSelectedListener;
    int mListItemExpandMaximum;
    private boolean mModal;
    private DataSetObserver mObserver;
    private boolean mOverlapAnchor;
    private boolean mOverlapAnchorSet;
    PopupWindow mPopup;
    private int mPromptPosition;
    private View mPromptView;
    private final ResizePopupRunnable mResizePopupRunnable;
    private final PopupScrollListener mScrollListener;
    private Runnable mShowDropDownRunnable;
    private final Rect mTempRect;
    private final PopupTouchInterceptor mTouchInterceptor;

    private class ListSelectorHider implements Runnable {
        /* synthetic */ ListSelectorHider(ListPopupWindow this$0, ListSelectorHider -this1) {
            this();
        }

        private ListSelectorHider() {
        }

        public void run() {
            ListPopupWindow.this.clearListSelection();
        }
    }

    private class PopupDataSetObserver extends DataSetObserver {
        /* synthetic */ PopupDataSetObserver(ListPopupWindow this$0, PopupDataSetObserver -this1) {
            this();
        }

        private PopupDataSetObserver() {
        }

        public void onChanged() {
            if (ListPopupWindow.this.isShowing()) {
                ListPopupWindow.this.show();
            }
        }

        public void onInvalidated() {
            ListPopupWindow.this.dismiss();
        }
    }

    private class PopupScrollListener implements OnScrollListener {
        /* synthetic */ PopupScrollListener(ListPopupWindow this$0, PopupScrollListener -this1) {
            this();
        }

        private PopupScrollListener() {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (!(scrollState != 1 || (ListPopupWindow.this.isInputMethodNotNeeded() ^ 1) == 0 || ListPopupWindow.this.mPopup.getContentView() == null)) {
                ListPopupWindow.this.mHandler.removeCallbacks(ListPopupWindow.this.mResizePopupRunnable);
                ListPopupWindow.this.mResizePopupRunnable.run();
            }
            view.setVerticalScrollBarEnabled(true);
        }
    }

    private class PopupTouchInterceptor implements OnTouchListener {
        /* synthetic */ PopupTouchInterceptor(ListPopupWindow this$0, PopupTouchInterceptor -this1) {
            this();
        }

        private PopupTouchInterceptor() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (action == 0 && ListPopupWindow.this.mPopup != null && ListPopupWindow.this.mPopup.isShowing() && x >= 0 && x < ListPopupWindow.this.mPopup.getWidth() && y >= 0 && y < ListPopupWindow.this.mPopup.getHeight()) {
                ListPopupWindow.this.mHandler.postDelayed(ListPopupWindow.this.mResizePopupRunnable, 250);
            } else if (action == 1) {
                ListPopupWindow.this.mHandler.removeCallbacks(ListPopupWindow.this.mResizePopupRunnable);
            }
            return false;
        }
    }

    private class ResizePopupRunnable implements Runnable {
        /* synthetic */ ResizePopupRunnable(ListPopupWindow this$0, ResizePopupRunnable -this1) {
            this();
        }

        private ResizePopupRunnable() {
        }

        public void run() {
            if (ListPopupWindow.this.mDropDownList != null && ListPopupWindow.this.mDropDownList.isAttachedToWindow() && ListPopupWindow.this.mDropDownList.getCount() > ListPopupWindow.this.mDropDownList.getChildCount() && ListPopupWindow.this.mDropDownList.getChildCount() <= ListPopupWindow.this.mListItemExpandMaximum) {
                ListPopupWindow.this.mPopup.setInputMethodMode(2);
                ListPopupWindow.this.show();
            }
        }
    }

    public ListPopupWindow(Context context) {
        this(context, null, R.attr.listPopupWindowStyle, 0);
    }

    public ListPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listPopupWindowStyle, 0);
    }

    public ListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mDropDownHeight = -2;
        this.mDropDownWidth = -2;
        this.mDropDownWindowLayoutType = 1002;
        this.mIsAnimatedFromAnchor = true;
        this.mDropDownGravity = 0;
        this.mDropDownAlwaysVisible = false;
        this.mForceIgnoreOutsideTouch = false;
        this.mListItemExpandMaximum = Integer.MAX_VALUE;
        this.mPromptPosition = 0;
        this.mResizePopupRunnable = new ResizePopupRunnable(this, null);
        this.mTouchInterceptor = new PopupTouchInterceptor(this, null);
        this.mScrollListener = new PopupScrollListener(this, null);
        this.mHideSelector = new ListSelectorHider(this, null);
        this.mTempRect = new Rect();
        this.mIsForSpinner = false;
        this.mContext = context;
        this.mHandler = new Handler(context.getMainLooper());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPopupWindow, defStyleAttr, defStyleRes);
        this.mDropDownHorizontalOffset = a.getDimensionPixelOffset(0, 0);
        this.mDropDownVerticalOffset = a.getDimensionPixelOffset(1, 0);
        if (this.mDropDownVerticalOffset != 0) {
            this.mDropDownVerticalOffsetSet = true;
        }
        a.recycle();
        this.mPopup = new PopupWindow(context, attrs, defStyleAttr, defStyleRes);
        this.mPopup.setInputMethodMode(1);
    }

    public void setAdapter(ListAdapter adapter) {
        if (this.mObserver == null) {
            this.mObserver = new PopupDataSetObserver(this, null);
        } else if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mObserver);
        }
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            adapter.registerDataSetObserver(this.mObserver);
        }
        if (this.mDropDownList != null) {
            this.mDropDownList.setAdapter(this.mAdapter);
        }
    }

    public void setSpinner(boolean isForSpinner) {
        this.mIsForSpinner = isForSpinner;
        this.mPopup.setSpinner(isForSpinner);
    }

    public void setPromptPosition(int position) {
        this.mPromptPosition = position;
    }

    public int getPromptPosition() {
        return this.mPromptPosition;
    }

    public void setModal(boolean modal) {
        this.mModal = modal;
        this.mPopup.setFocusable(modal);
    }

    public boolean isModal() {
        return this.mModal;
    }

    public void setForceIgnoreOutsideTouch(boolean forceIgnoreOutsideTouch) {
        this.mForceIgnoreOutsideTouch = forceIgnoreOutsideTouch;
    }

    public void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible) {
        this.mDropDownAlwaysVisible = dropDownAlwaysVisible;
    }

    public boolean isDropDownAlwaysVisible() {
        return this.mDropDownAlwaysVisible;
    }

    public void setSoftInputMode(int mode) {
        this.mPopup.setSoftInputMode(mode);
    }

    public int getSoftInputMode() {
        return this.mPopup.getSoftInputMode();
    }

    public void setListSelector(Drawable selector) {
        this.mDropDownListHighlight = selector;
    }

    public Drawable getBackground() {
        return this.mPopup.getBackground();
    }

    public void setBackgroundDrawable(Drawable d) {
        this.mPopup.setBackgroundDrawable(d);
    }

    public void setAnimationStyle(int animationStyle) {
        this.mPopup.setAnimationStyle(animationStyle);
    }

    public int getAnimationStyle() {
        return this.mPopup.getAnimationStyle();
    }

    public View getAnchorView() {
        return this.mDropDownAnchorView;
    }

    public void setAnchorView(View anchor) {
        this.mDropDownAnchorView = anchor;
    }

    public int getHorizontalOffset() {
        return this.mDropDownHorizontalOffset;
    }

    public void setHorizontalOffset(int offset) {
        this.mDropDownHorizontalOffset = offset;
    }

    public int getVerticalOffset() {
        if (this.mDropDownVerticalOffsetSet) {
            return this.mDropDownVerticalOffset;
        }
        return 0;
    }

    public void setVerticalOffset(int offset) {
        this.mDropDownVerticalOffset = offset;
        this.mDropDownVerticalOffsetSet = true;
    }

    public void setEpicenterBounds(Rect bounds) {
        this.mEpicenterBounds = bounds;
    }

    public void setDropDownGravity(int gravity) {
        this.mDropDownGravity = gravity;
    }

    public int getWidth() {
        return this.mDropDownWidth;
    }

    public void setWidth(int width) {
        this.mDropDownWidth = width;
    }

    public void setContentWidth(int width) {
        Drawable popupBackground = this.mPopup.getBackground();
        if (popupBackground != null) {
            popupBackground.getPadding(this.mTempRect);
            this.mDropDownWidth = (this.mTempRect.left + this.mTempRect.right) + width;
            return;
        }
        setWidth(width);
    }

    public int getHeight() {
        return this.mDropDownHeight;
    }

    public void setHeight(int height) {
        if (!(height >= 0 || -2 == height || -1 == height)) {
            if (this.mContext.getApplicationInfo().targetSdkVersion < 26) {
                Log.e(TAG, "Negative value " + height + " passed to ListPopupWindow#setHeight" + " produces undefined results");
            } else {
                throw new IllegalArgumentException("Invalid height. Must be a positive value, MATCH_PARENT, or WRAP_CONTENT.");
            }
        }
        this.mDropDownHeight = height;
    }

    public void setWindowLayoutType(int layoutType) {
        this.mDropDownWindowLayoutType = layoutType;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.mItemClickListener = clickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener selectedListener) {
        this.mItemSelectedListener = selectedListener;
    }

    public void setPromptView(View prompt) {
        boolean showing = isShowing();
        if (showing) {
            removePromptView();
        }
        this.mPromptView = prompt;
        if (showing) {
            show();
        }
    }

    public void postShow() {
        this.mHandler.post(this.mShowDropDownRunnable);
    }

    public void show() {
        boolean z = false;
        int i = -1;
        int height = buildDropDown();
        boolean noInputMethod = isInputMethodNotNeeded();
        this.mPopup.setAllowScrollingAnchorParent(noInputMethod ^ 1);
        this.mPopup.setWindowLayoutType(this.mDropDownWindowLayoutType);
        int widthSpec;
        int heightSpec;
        PopupWindow popupWindow;
        if (!this.mPopup.isShowing()) {
            boolean z2;
            if (this.mDropDownWidth == -1) {
                widthSpec = -1;
            } else if (this.mDropDownWidth == -2) {
                widthSpec = getAnchorView().getWidth();
            } else {
                widthSpec = this.mDropDownWidth;
            }
            if (this.mDropDownHeight == -1) {
                heightSpec = -1;
            } else if (this.mDropDownHeight == -2) {
                heightSpec = height;
            } else {
                heightSpec = this.mDropDownHeight;
            }
            this.mPopup.setWidth(widthSpec);
            this.mPopup.setHeight(heightSpec);
            this.mPopup.setClipToScreenEnabled(true);
            popupWindow = this.mPopup;
            if (this.mForceIgnoreOutsideTouch) {
                z2 = false;
            } else {
                z2 = this.mDropDownAlwaysVisible ^ 1;
            }
            popupWindow.setOutsideTouchable(z2);
            this.mPopup.setTouchInterceptor(this.mTouchInterceptor);
            this.mPopup.setEpicenterBounds(this.mEpicenterBounds);
            if (this.mOverlapAnchorSet) {
                this.mPopup.setOverlapAnchor(this.mOverlapAnchor);
            }
            this.mPopup.showAsDropDown(getAnchorView(), this.mDropDownHorizontalOffset, this.mDropDownVerticalOffset, this.mDropDownGravity);
            this.mDropDownList.setSelection(-1);
            this.mDropDownList.setVerticalScrollBarEnabled(false);
            if (!this.mModal || this.mDropDownList.isInTouchMode()) {
                clearListSelection();
            }
            if (!this.mModal) {
                this.mHandler.post(this.mHideSelector);
            }
        } else if (getAnchorView().isAttachedToWindow()) {
            int i2;
            if (this.mDropDownWidth == -1) {
                widthSpec = -1;
            } else if (this.mDropDownWidth == -2) {
                widthSpec = getAnchorView().getWidth();
            } else {
                widthSpec = this.mDropDownWidth;
            }
            if (this.mDropDownHeight == -1) {
                heightSpec = noInputMethod ? height : -1;
                int i3;
                if (noInputMethod) {
                    popupWindow = this.mPopup;
                    if (this.mDropDownWidth == -1) {
                        i3 = -1;
                    } else {
                        i3 = 0;
                    }
                    popupWindow.setWidth(i3);
                    this.mPopup.setHeight(0);
                } else {
                    popupWindow = this.mPopup;
                    if (this.mDropDownWidth == -1) {
                        i3 = -1;
                    } else {
                        i3 = 0;
                    }
                    popupWindow.setWidth(i3);
                    this.mPopup.setHeight(-1);
                }
            } else {
                heightSpec = this.mDropDownHeight == -2 ? height : this.mDropDownHeight;
            }
            PopupWindow popupWindow2 = this.mPopup;
            if (!this.mForceIgnoreOutsideTouch) {
                z = this.mDropDownAlwaysVisible ^ 1;
            }
            popupWindow2.setOutsideTouchable(z);
            popupWindow2 = this.mPopup;
            View anchorView = getAnchorView();
            int i4 = this.mDropDownHorizontalOffset;
            int i5 = this.mDropDownVerticalOffset;
            if (widthSpec < 0) {
                i2 = -1;
            } else {
                i2 = widthSpec;
            }
            if (heightSpec >= 0) {
                i = heightSpec;
            }
            popupWindow2.update(anchorView, i4, i5, i2, i);
        }
    }

    public void dismiss() {
        this.mPopup.dismiss();
        removePromptView();
        this.mPopup.setContentView(null);
        this.mDropDownList = null;
        this.mHandler.removeCallbacks(this.mResizePopupRunnable);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mPopup.setOnDismissListener(listener);
    }

    private void removePromptView() {
        if (this.mPromptView != null) {
            ViewParent parent = this.mPromptView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.mPromptView);
            }
        }
    }

    public void setInputMethodMode(int mode) {
        this.mPopup.setInputMethodMode(mode);
    }

    public int getInputMethodMode() {
        return this.mPopup.getInputMethodMode();
    }

    public void setSelection(int position) {
        DropDownListView list = this.mDropDownList;
        if (isShowing() && list != null) {
            list.setListSelectionHidden(false);
            list.setSelection(position);
            if (list.getChoiceMode() != 0) {
                list.setItemChecked(position, true);
            }
        }
    }

    public void clearListSelection() {
        DropDownListView list = this.mDropDownList;
        if (list != null) {
            list.setListSelectionHidden(true);
            list.hideSelector();
            list.requestLayout();
        }
    }

    public boolean isShowing() {
        return this.mPopup.isShowing();
    }

    public boolean isInputMethodNotNeeded() {
        return this.mPopup.getInputMethodMode() == 2;
    }

    public boolean performItemClick(int position) {
        if (!isShowing()) {
            return false;
        }
        if (this.mItemClickListener != null) {
            DropDownListView list = this.mDropDownList;
            int i = position;
            this.mItemClickListener.onItemClick(list, list.getChildAt(position - list.getFirstVisiblePosition()), i, list.getAdapter().getItemId(position));
        }
        return true;
    }

    public Object getSelectedItem() {
        if (isShowing()) {
            return this.mDropDownList.getSelectedItem();
        }
        return null;
    }

    public int getSelectedItemPosition() {
        if (isShowing()) {
            return this.mDropDownList.getSelectedItemPosition();
        }
        return -1;
    }

    public long getSelectedItemId() {
        if (isShowing()) {
            return this.mDropDownList.getSelectedItemId();
        }
        return Long.MIN_VALUE;
    }

    public View getSelectedView() {
        if (isShowing()) {
            return this.mDropDownList.getSelectedView();
        }
        return null;
    }

    public ListView getListView() {
        return this.mDropDownList;
    }

    DropDownListView createDropDownListView(Context context, boolean hijackFocus) {
        return new DropDownListView(context, hijackFocus);
    }

    void setListItemExpandMax(int max) {
        this.mListItemExpandMaximum = max;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isShowing() && keyCode != 62 && (this.mDropDownList.getSelectedItemPosition() >= 0 || (KeyEvent.isConfirmKey(keyCode) ^ 1) != 0)) {
            int curIndex = this.mDropDownList.getSelectedItemPosition();
            boolean below = this.mPopup.isAboveAnchor() ^ 1;
            ListAdapter adapter = this.mAdapter;
            int firstItem = Integer.MAX_VALUE;
            int lastItem = Integer.MIN_VALUE;
            if (adapter != null) {
                boolean allEnabled = adapter.areAllItemsEnabled();
                if (allEnabled) {
                    firstItem = 0;
                } else {
                    firstItem = this.mDropDownList.lookForSelectablePosition(0, true);
                }
                if (allEnabled) {
                    lastItem = adapter.getCount() - 1;
                } else {
                    lastItem = this.mDropDownList.lookForSelectablePosition(adapter.getCount() - 1, false);
                }
            }
            if (!(below && keyCode == 19 && curIndex <= firstItem) && (below || keyCode != 20 || curIndex < lastItem)) {
                this.mDropDownList.setListSelectionHidden(false);
                if (this.mDropDownList.onKeyDown(keyCode, event)) {
                    this.mPopup.setInputMethodMode(2);
                    this.mDropDownList.requestFocusFromTouch();
                    show();
                    switch (keyCode) {
                        case 19:
                        case 20:
                        case 23:
                        case 66:
                            return true;
                    }
                } else if (!below || keyCode != 20) {
                    return !below && keyCode == 19 && curIndex == firstItem;
                } else {
                    if (curIndex == lastItem) {
                        return true;
                    }
                }
            }
            clearListSelection();
            this.mPopup.setInputMethodMode(1);
            show();
            return true;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isShowing() || this.mDropDownList.getSelectedItemPosition() < 0) {
            return false;
        }
        boolean consumed = this.mDropDownList.onKeyUp(keyCode, event);
        if (consumed && KeyEvent.isConfirmKey(keyCode)) {
            dismiss();
        }
        return consumed;
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == 4 && isShowing()) {
            View anchorView = this.mDropDownAnchorView;
            DispatcherState state;
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                state = anchorView.getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == 1) {
                state = anchorView.getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && (event.isCanceled() ^ 1) != 0) {
                    dismiss();
                    return true;
                }
            }
        }
        return false;
    }

    public OnTouchListener createDragToOpenListener(View src) {
        return new ForwardingListener(src) {
            public ShowableListMenu getPopup() {
                return ListPopupWindow.this;
            }
        };
    }

    private int buildDropDown() {
        int padding;
        int otherHeights = 0;
        LayoutParams hintParams;
        if (this.mDropDownList == null) {
            Context context = this.mContext;
            this.mShowDropDownRunnable = new Runnable() {
                public void run() {
                    View view = ListPopupWindow.this.getAnchorView();
                    if (view != null && view.getWindowToken() != null) {
                        ListPopupWindow.this.show();
                    }
                }
            };
            this.mDropDownList = createDropDownListView(context, this.mModal ^ 1);
            if (HwWidgetFactory.isHwTheme(context)) {
                this.mDropDownList.setNeedAdjustSelector(true);
                this.mDropDownList.setSpinner(this.mIsForSpinner);
            }
            if (this.mDropDownListHighlight != null) {
                this.mDropDownList.setSelector(this.mDropDownListHighlight);
            }
            this.mDropDownList.setAdapter(this.mAdapter);
            this.mDropDownList.setOnItemClickListener(this.mItemClickListener);
            this.mDropDownList.setFocusable(true);
            this.mDropDownList.setFocusableInTouchMode(true);
            this.mDropDownList.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (position != -1) {
                        DropDownListView dropDownList = ListPopupWindow.this.mDropDownList;
                        if (dropDownList != null) {
                            dropDownList.setListSelectionHidden(false);
                        }
                    }
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            this.mDropDownList.setOnScrollListener(this.mScrollListener);
            if (this.mItemSelectedListener != null) {
                this.mDropDownList.setOnItemSelectedListener(this.mItemSelectedListener);
            }
            ViewGroup dropDownView = this.mDropDownList;
            View hintView = this.mPromptView;
            if (hintView != null) {
                int widthMode;
                int widthSize;
                ViewGroup hintContainer = new LinearLayout(context);
                hintContainer.setOrientation(1);
                hintParams = new LayoutParams(-1, 0, 1.0f);
                switch (this.mPromptPosition) {
                    case 0:
                        hintContainer.addView(hintView);
                        hintContainer.addView((View) dropDownView, (ViewGroup.LayoutParams) hintParams);
                        break;
                    case 1:
                        hintContainer.addView((View) dropDownView, (ViewGroup.LayoutParams) hintParams);
                        hintContainer.addView(hintView);
                        break;
                    default:
                        Log.e(TAG, "Invalid hint position " + this.mPromptPosition);
                        break;
                }
                if (this.mDropDownWidth >= 0) {
                    widthMode = Integer.MIN_VALUE;
                    widthSize = this.mDropDownWidth;
                } else {
                    widthMode = 0;
                    widthSize = 0;
                }
                hintView.measure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), 0);
                hintParams = (LayoutParams) hintView.getLayoutParams();
                otherHeights = (hintView.getMeasuredHeight() + hintParams.topMargin) + hintParams.bottomMargin;
                dropDownView = hintContainer;
            }
            this.mPopup.setContentView(dropDownView);
        } else {
            View view = this.mPromptView;
            if (view != null) {
                hintParams = (LayoutParams) view.getLayoutParams();
                otherHeights = (view.getMeasuredHeight() + hintParams.topMargin) + hintParams.bottomMargin;
            }
        }
        Drawable background = this.mPopup.getBackground();
        if (background != null) {
            background.getPadding(this.mTempRect);
            padding = this.mTempRect.top + this.mTempRect.bottom;
            if (!this.mDropDownVerticalOffsetSet) {
                this.mDropDownVerticalOffset = -this.mTempRect.top;
            }
        } else {
            this.mTempRect.setEmpty();
            padding = 0;
        }
        int maxHeight = this.mPopup.getMaxAvailableHeight(getAnchorView(), this.mDropDownVerticalOffset, this.mPopup.getInputMethodMode() == 2);
        if (this.mDropDownAlwaysVisible || this.mDropDownHeight == -1) {
            return maxHeight + padding;
        }
        int childWidthSpec;
        switch (this.mDropDownWidth) {
            case -2:
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), Integer.MIN_VALUE);
                break;
            case -1:
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), 1073741824);
                break;
            default:
                int drawablePadding = this.mTempRect.left + this.mTempRect.right;
                if (!HwWidgetFactory.isHwTheme(this.mContext)) {
                    childWidthSpec = MeasureSpec.makeMeasureSpec(this.mDropDownWidth, 1073741824);
                    break;
                }
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mDropDownWidth - drawablePadding, 1073741824);
                break;
        }
        int listContent = this.mDropDownList.measureHeightOfChildren(childWidthSpec, 0, -1, maxHeight - otherHeights, -1);
        if (listContent > 0) {
            otherHeights += padding + (this.mDropDownList.getPaddingTop() + this.mDropDownList.getPaddingBottom());
        }
        return listContent + otherHeights;
    }

    public void setOverlapAnchor(boolean overlap) {
        this.mOverlapAnchorSet = true;
        this.mOverlapAnchor = overlap;
    }
}
