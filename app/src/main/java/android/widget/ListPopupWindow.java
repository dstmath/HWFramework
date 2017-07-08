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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import com.android.internal.R;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.android.internal.view.menu.ShowableListMenu;
import com.hisi.perfhub.PerfHub;
import com.huawei.hwperformance.HwPerformance;

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
    private OnItemClickListener mItemClickListener;
    private OnItemSelectedListener mItemSelectedListener;
    int mListItemExpandMaximum;
    private boolean mModal;
    private DataSetObserver mObserver;
    PopupWindow mPopup;
    private int mPromptPosition;
    private View mPromptView;
    private final ResizePopupRunnable mResizePopupRunnable;
    private final PopupScrollListener mScrollListener;
    private Runnable mShowDropDownRunnable;
    private final Rect mTempRect;
    private final PopupTouchInterceptor mTouchInterceptor;

    /* renamed from: android.widget.ListPopupWindow.1 */
    class AnonymousClass1 extends ForwardingListener {
        AnonymousClass1(View $anonymous0) {
            super($anonymous0);
        }

        public ShowableListMenu getPopup() {
            return ListPopupWindow.this;
        }
    }

    private class ListSelectorHider implements Runnable {
        private ListSelectorHider() {
        }

        public void run() {
            ListPopupWindow.this.clearListSelection();
        }
    }

    private class PopupDataSetObserver extends DataSetObserver {
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
        private PopupScrollListener() {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (!(scrollState != ListPopupWindow.POSITION_PROMPT_BELOW || ListPopupWindow.this.isInputMethodNotNeeded() || ListPopupWindow.this.mPopup.getContentView() == null)) {
                ListPopupWindow.this.mHandler.removeCallbacks(ListPopupWindow.this.mResizePopupRunnable);
                ListPopupWindow.this.mResizePopupRunnable.run();
            }
            view.setVerticalScrollBarEnabled(true);
        }
    }

    private class PopupTouchInterceptor implements OnTouchListener {
        private PopupTouchInterceptor() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (action == 0 && ListPopupWindow.this.mPopup != null && ListPopupWindow.this.mPopup.isShowing() && x >= 0 && x < ListPopupWindow.this.mPopup.getWidth() && y >= 0 && y < ListPopupWindow.this.mPopup.getHeight()) {
                ListPopupWindow.this.mHandler.postDelayed(ListPopupWindow.this.mResizePopupRunnable, 250);
            } else if (action == ListPopupWindow.POSITION_PROMPT_BELOW) {
                ListPopupWindow.this.mHandler.removeCallbacks(ListPopupWindow.this.mResizePopupRunnable);
            }
            return ListPopupWindow.DEBUG;
        }
    }

    private class ResizePopupRunnable implements Runnable {
        private ResizePopupRunnable() {
        }

        public void run() {
            if (ListPopupWindow.this.mDropDownList != null && ListPopupWindow.this.mDropDownList.isAttachedToWindow() && ListPopupWindow.this.mDropDownList.getCount() > ListPopupWindow.this.mDropDownList.getChildCount() && ListPopupWindow.this.mDropDownList.getChildCount() <= ListPopupWindow.this.mListItemExpandMaximum) {
                ListPopupWindow.this.mPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
                ListPopupWindow.this.show();
            }
        }
    }

    public ListPopupWindow(Context context) {
        this(context, null, R.attr.listPopupWindowStyle, POSITION_PROMPT_ABOVE);
    }

    public ListPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listPopupWindowStyle, POSITION_PROMPT_ABOVE);
    }

    public ListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, POSITION_PROMPT_ABOVE);
    }

    public ListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mDropDownHeight = WRAP_CONTENT;
        this.mDropDownWidth = WRAP_CONTENT;
        this.mDropDownWindowLayoutType = RILConstants.RIL_UNSOL_RESPONSE_VOICE_NETWORK_STATE_CHANGED;
        this.mIsAnimatedFromAnchor = true;
        this.mDropDownGravity = POSITION_PROMPT_ABOVE;
        this.mDropDownAlwaysVisible = DEBUG;
        this.mForceIgnoreOutsideTouch = DEBUG;
        this.mListItemExpandMaximum = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mPromptPosition = POSITION_PROMPT_ABOVE;
        this.mResizePopupRunnable = new ResizePopupRunnable();
        this.mTouchInterceptor = new PopupTouchInterceptor();
        this.mScrollListener = new PopupScrollListener();
        this.mHideSelector = new ListSelectorHider();
        this.mTempRect = new Rect();
        this.mContext = context;
        this.mHandler = new Handler(context.getMainLooper());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPopupWindow, defStyleAttr, defStyleRes);
        this.mDropDownHorizontalOffset = a.getDimensionPixelOffset(POSITION_PROMPT_ABOVE, POSITION_PROMPT_ABOVE);
        this.mDropDownVerticalOffset = a.getDimensionPixelOffset(POSITION_PROMPT_BELOW, POSITION_PROMPT_ABOVE);
        if (this.mDropDownVerticalOffset != 0) {
            this.mDropDownVerticalOffsetSet = true;
        }
        a.recycle();
        this.mPopup = new PopupWindow(context, attrs, defStyleAttr, defStyleRes);
        this.mPopup.setInputMethodMode(POSITION_PROMPT_BELOW);
    }

    public void setAdapter(ListAdapter adapter) {
        if (this.mObserver == null) {
            this.mObserver = new PopupDataSetObserver();
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

    public void setSpinner(boolean IsForSpinner) {
        this.mPopup.setSpinner(IsForSpinner);
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
        return POSITION_PROMPT_ABOVE;
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
        boolean z;
        boolean z2 = true;
        boolean z3 = DEBUG;
        int i = MATCH_PARENT;
        int height = buildDropDown();
        boolean noInputMethod = isInputMethodNotNeeded();
        PopupWindow popupWindow = this.mPopup;
        if (noInputMethod) {
            z = DEBUG;
        } else {
            z = true;
        }
        popupWindow.setAllowScrollingAnchorParent(z);
        this.mPopup.setWindowLayoutType(this.mDropDownWindowLayoutType);
        int widthSpec;
        int heightSpec;
        if (this.mPopup.isShowing()) {
            int i2;
            if (this.mDropDownWidth == MATCH_PARENT) {
                widthSpec = MATCH_PARENT;
            } else if (this.mDropDownWidth == WRAP_CONTENT) {
                widthSpec = getAnchorView().getWidth();
            } else {
                widthSpec = this.mDropDownWidth;
            }
            if (this.mDropDownHeight == MATCH_PARENT) {
                heightSpec = noInputMethod ? height : MATCH_PARENT;
                int i3;
                if (noInputMethod) {
                    popupWindow = this.mPopup;
                    if (this.mDropDownWidth == MATCH_PARENT) {
                        i3 = MATCH_PARENT;
                    } else {
                        i3 = POSITION_PROMPT_ABOVE;
                    }
                    popupWindow.setWidth(i3);
                    this.mPopup.setHeight(POSITION_PROMPT_ABOVE);
                } else {
                    popupWindow = this.mPopup;
                    if (this.mDropDownWidth == MATCH_PARENT) {
                        i3 = MATCH_PARENT;
                    } else {
                        i3 = POSITION_PROMPT_ABOVE;
                    }
                    popupWindow.setWidth(i3);
                    this.mPopup.setHeight(MATCH_PARENT);
                }
            } else {
                heightSpec = this.mDropDownHeight == WRAP_CONTENT ? height : this.mDropDownHeight;
            }
            PopupWindow popupWindow2 = this.mPopup;
            if (!(this.mForceIgnoreOutsideTouch || this.mDropDownAlwaysVisible)) {
                z3 = true;
            }
            popupWindow2.setOutsideTouchable(z3);
            popupWindow2 = this.mPopup;
            View anchorView = getAnchorView();
            int i4 = this.mDropDownHorizontalOffset;
            int i5 = this.mDropDownVerticalOffset;
            if (widthSpec < 0) {
                i2 = MATCH_PARENT;
            } else {
                i2 = widthSpec;
            }
            if (heightSpec >= 0) {
                i = heightSpec;
            }
            popupWindow2.update(anchorView, i4, i5, i2, i);
            return;
        }
        if (this.mDropDownWidth == MATCH_PARENT) {
            widthSpec = MATCH_PARENT;
        } else if (this.mDropDownWidth == WRAP_CONTENT) {
            widthSpec = getAnchorView().getWidth();
        } else {
            widthSpec = this.mDropDownWidth;
        }
        if (this.mDropDownHeight == MATCH_PARENT) {
            heightSpec = MATCH_PARENT;
        } else if (this.mDropDownHeight == WRAP_CONTENT) {
            heightSpec = height;
        } else {
            heightSpec = this.mDropDownHeight;
        }
        this.mPopup.setWidth(widthSpec);
        this.mPopup.setHeight(heightSpec);
        this.mPopup.setClipToScreenEnabled(true);
        popupWindow2 = this.mPopup;
        if (this.mForceIgnoreOutsideTouch || this.mDropDownAlwaysVisible) {
            z2 = DEBUG;
        }
        popupWindow2.setOutsideTouchable(z2);
        this.mPopup.setTouchInterceptor(this.mTouchInterceptor);
        this.mPopup.setEpicenterBounds(this.mEpicenterBounds);
        this.mPopup.showAsDropDown(getAnchorView(), this.mDropDownHorizontalOffset, this.mDropDownVerticalOffset, this.mDropDownGravity);
        this.mDropDownList.setSelection(MATCH_PARENT);
        this.mDropDownList.setVerticalScrollBarEnabled(DEBUG);
        if (!this.mModal || this.mDropDownList.isInTouchMode()) {
            clearListSelection();
        }
        if (!this.mModal) {
            this.mHandler.post(this.mHideSelector);
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
            list.setListSelectionHidden(DEBUG);
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
        return this.mPopup.getInputMethodMode() == INPUT_METHOD_NOT_NEEDED ? true : DEBUG;
    }

    public boolean performItemClick(int position) {
        if (!isShowing()) {
            return DEBUG;
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
        return MATCH_PARENT;
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
        if (isShowing() && keyCode != 62 && (this.mDropDownList.getSelectedItemPosition() >= 0 || !KeyEvent.isConfirmKey(keyCode))) {
            int curIndex = this.mDropDownList.getSelectedItemPosition();
            boolean below = this.mPopup.isAboveAnchor() ? DEBUG : true;
            ListAdapter adapter = this.mAdapter;
            int firstItem = HwBootFail.STAGE_BOOT_SUCCESS;
            int lastItem = RtlSpacingHelper.UNDEFINED;
            if (adapter != null) {
                boolean allEnabled = adapter.areAllItemsEnabled();
                if (allEnabled) {
                    firstItem = POSITION_PROMPT_ABOVE;
                } else {
                    firstItem = this.mDropDownList.lookForSelectablePosition(POSITION_PROMPT_ABOVE, true);
                }
                if (allEnabled) {
                    lastItem = adapter.getCount() + MATCH_PARENT;
                } else {
                    lastItem = this.mDropDownList.lookForSelectablePosition(adapter.getCount() + MATCH_PARENT, DEBUG);
                }
            }
            if (!(below && keyCode == 19 && curIndex <= firstItem) && (below || keyCode != 20 || curIndex < lastItem)) {
                this.mDropDownList.setListSelectionHidden(DEBUG);
                if (this.mDropDownList.onKeyDown(keyCode, event)) {
                    this.mPopup.setInputMethodMode(INPUT_METHOD_NOT_NEEDED);
                    this.mDropDownList.requestFocusFromTouch();
                    show();
                    switch (keyCode) {
                        case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                        case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                        case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
                        case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                            return true;
                        default:
                            break;
                    }
                } else if (!below || keyCode != 20) {
                    return (!below && keyCode == 19 && curIndex == firstItem) ? true : DEBUG;
                } else {
                    if (curIndex == lastItem) {
                        return true;
                    }
                }
            }
            clearListSelection();
            this.mPopup.setInputMethodMode(POSITION_PROMPT_BELOW);
            show();
            return true;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isShowing() || this.mDropDownList.getSelectedItemPosition() < 0) {
            return DEBUG;
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
            } else if (event.getAction() == POSITION_PROMPT_BELOW) {
                state = anchorView.getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    dismiss();
                    return true;
                }
            }
        }
        return DEBUG;
    }

    public OnTouchListener createDragToOpenListener(View src) {
        return new AnonymousClass1(src);
    }

    private int buildDropDown() {
        int padding;
        int otherHeights = POSITION_PROMPT_ABOVE;
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
            this.mDropDownList = createDropDownListView(context, this.mModal ? DEBUG : true);
            if (HwWidgetFactory.isHwTheme(context)) {
                this.mDropDownList.setNeedAdjustSelector(true);
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
                    if (position != ListPopupWindow.MATCH_PARENT) {
                        DropDownListView dropDownList = ListPopupWindow.this.mDropDownList;
                        if (dropDownList != null) {
                            dropDownList.setListSelectionHidden(ListPopupWindow.DEBUG);
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
                hintContainer.setOrientation(POSITION_PROMPT_BELOW);
                hintParams = new LayoutParams(MATCH_PARENT, POSITION_PROMPT_ABOVE, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                switch (this.mPromptPosition) {
                    case POSITION_PROMPT_ABOVE /*0*/:
                        hintContainer.addView(hintView);
                        hintContainer.addView((View) dropDownView, (ViewGroup.LayoutParams) hintParams);
                        break;
                    case POSITION_PROMPT_BELOW /*1*/:
                        hintContainer.addView((View) dropDownView, (ViewGroup.LayoutParams) hintParams);
                        hintContainer.addView(hintView);
                        break;
                    default:
                        Log.e(TAG, "Invalid hint position " + this.mPromptPosition);
                        break;
                }
                if (this.mDropDownWidth >= 0) {
                    widthMode = RtlSpacingHelper.UNDEFINED;
                    widthSize = this.mDropDownWidth;
                } else {
                    widthMode = POSITION_PROMPT_ABOVE;
                    widthSize = POSITION_PROMPT_ABOVE;
                }
                hintView.measure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), POSITION_PROMPT_ABOVE);
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
            padding = POSITION_PROMPT_ABOVE;
        }
        int maxHeight = this.mPopup.getMaxAvailableHeight(getAnchorView(), this.mDropDownVerticalOffset, this.mPopup.getInputMethodMode() == INPUT_METHOD_NOT_NEEDED ? true : DEBUG);
        if (this.mDropDownAlwaysVisible || this.mDropDownHeight == MATCH_PARENT) {
            return maxHeight + padding;
        }
        int childWidthSpec;
        switch (this.mDropDownWidth) {
            case WRAP_CONTENT /*-2*/:
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), RtlSpacingHelper.UNDEFINED);
                break;
            case MATCH_PARENT /*-1*/:
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                break;
            default:
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mDropDownWidth, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                break;
        }
        int listContent = this.mDropDownList.measureHeightOfChildren(childWidthSpec, POSITION_PROMPT_ABOVE, MATCH_PARENT, maxHeight - otherHeights, MATCH_PARENT);
        if (listContent > 0) {
            otherHeights += padding + (this.mDropDownList.getPaddingTop() + this.mDropDownList.getPaddingBottom());
        }
        return listContent + otherHeights;
    }
}
