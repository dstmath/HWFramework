package huawei.android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import androidhwext.R;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.RollbackRuleDetector;
import huawei.android.widget.plume.HwPlumeManager;
import java.util.HashMap;
import java.util.Map;

public class ExpandableListView extends android.widget.ExpandableListView implements ScrollCallback {
    private static final String CLICL_STATUS_BAR_ACTION = "com.huawei.intent.action.CLICK_STATUSBAR";
    private static final int DEFAULT_MAP_SIZE = 16;
    private static final int SCREEN_PAGE_NUMBER = 15;
    private static final int SCROLL_TO_TOP_DURATION = 600;
    private static final String SYSTEMUI_PERMITION = "huawei.permission.CLICK_STATUSBAR_BROADCAST";
    private static final String TAG = "ExpandableListView";
    private Class mAbsListParamsClass;
    private Context mContext;
    private IntentFilter mFilter;
    private HwWidgetSafeInsets mHwWidgetSafeInsets;
    private boolean mIsHasRegistReciver;
    private boolean mIsHasUsedRollback;
    private boolean mIsScrollTopEnable;
    private Map<Integer, Rect> mItemViewOriginalPadding;
    private Rect mListInsetsRect;
    private Rect mListRect;
    private Context mReceiverContext;
    private RollbackRuleDetector mRollbackRuleDetector;
    private BroadcastReceiver mScrollToTopReceiver;

    public ExpandableListView(Context context) {
        this(context, null);
    }

    public ExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842863);
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mItemViewOriginalPadding = new HashMap(16);
        this.mHwWidgetSafeInsets = new HwWidgetSafeInsets(this);
        this.mIsScrollTopEnable = false;
        this.mIsHasRegistReciver = false;
        this.mListInsetsRect = new Rect();
        this.mAbsListParamsClass = null;
        this.mScrollToTopReceiver = new BroadcastReceiver() {
            /* class huawei.android.widget.ExpandableListView.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && ExpandableListView.CLICL_STATUS_BAR_ACTION.equals(intent.getAction())) {
                    ExpandableListView.this.handleScrollToTop();
                }
            }
        };
        this.mContext = context;
        setIgnoreScrollMultiSelectStub();
        if (context.getApplicationContext() != null) {
            this.mReceiverContext = context.getApplicationContext();
        } else {
            this.mReceiverContext = context;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HwExpandableListView, defStyleAttr, defStyleRes);
        this.mIsScrollTopEnable = typedArray.getBoolean(0, true);
        typedArray.recycle();
        this.mHwWidgetSafeInsets.parseHwDisplayCutout(context, attrs);
        this.mRollbackRuleDetector = new RollbackRuleDetector(new RollbackRuleDetector.RollBackScrollListener() {
            /* class huawei.android.widget.ExpandableListView.AnonymousClass2 */

            @Override // huawei.android.widget.RollbackRuleDetector.RollBackScrollListener
            public int getScrollYDistance() {
                View view = ExpandableListView.this.getChildAt(0);
                if (view != null) {
                    return (ExpandableListView.this.getFirstVisiblePosition() * view.getHeight()) - view.getTop();
                }
                return 0;
            }
        });
        this.mListRect = new Rect(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        initFitCutout();
        setValueFromPlume();
    }

    private void setValueFromPlume() {
        if (!HwPlumeManager.isPlumeUsed(this.mContext)) {
            setExtendedMultiChoiceEnabled(false, false);
            setExtendedMultiChoiceEnabled(true, false);
            setExtendScrollEnabled(true);
            return;
        }
        setExtendedMultiChoiceEnabled(false, HwPlumeManager.getInstance(this.mContext).getDefault(this, "quickSelectEnabled", false));
        setExtendedMultiChoiceEnabled(true, HwPlumeManager.getInstance(this.mContext).getDefault(this, "consecutiveSelectEnabled", false));
        setExtendScrollEnabled(HwPlumeManager.getInstance(this.mContext).getDefault(this, "listScrollEnabled", true));
    }

    @Override // huawei.android.widget.ScrollCallback
    public void scrollToTop() {
        handleScrollToTop();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScrollToTop() {
        post(new Runnable() {
            /* class huawei.android.widget.ExpandableListView.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                int num = ExpandableListView.this.getChildCount() * 15;
                if (ExpandableListView.this.getFirstVisiblePosition() > num) {
                    ExpandableListView.this.setSelection(num);
                }
                ExpandableListView.this.smoothScrollToPositionFromTop(0, 0, ExpandableListView.SCROLL_TO_TOP_DURATION);
                if (!ExpandableListView.this.mIsHasUsedRollback) {
                    ExpandableListView.this.mRollbackRuleDetector.postScrollUsedEvent();
                    ExpandableListView.this.mIsHasUsedRollback = true;
                }
            }
        });
    }

    public boolean dispatchStatusBarTop() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void adjustIndicatorLocation(Rect indicatorRect, boolean isLayoutRtl, int indicatorStart, Drawable groupIndicator) {
        if (isLayoutRtl && indicatorRect != null && groupIndicator != null && indicatorStart != 0) {
            indicatorRect.left = indicatorStart;
            indicatorRect.right = groupIndicator.getIntrinsicWidth() + indicatorStart;
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mHwWidgetSafeInsets.updateWindowInsets(insets);
        return super.onApplyWindowInsets(insets);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View, android.widget.AdapterView
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        updateChildrenInsets();
    }

    private void updateChildrenInsets() {
        if (isCutoutEnabled()) {
            ListAdapter adapter = getAdapter();
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (!(child == null || adapter == null)) {
                    int firstVisibleItemPosition = getFirstVisiblePosition();
                    if (firstVisibleItemPosition + i < adapter.getCount()) {
                        doItemCutoutPadding(child, adapter.getItemViewType(firstVisibleItemPosition + i));
                    } else {
                        return;
                    }
                }
            }
            Rect rect = this.mHwWidgetSafeInsets.getDisplaySafeInsets(this);
            if (rect != null) {
                this.mListInsetsRect.set(rect);
            }
        }
    }

    private void updateListRect() {
        this.mListRect.set(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        this.mHwWidgetSafeInsets.updateOriginPadding(this.mListRect);
    }

    private boolean isCutoutEnabled() {
        return !this.mHwWidgetSafeInsets.isCutoutModeNever();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean isPreventRequestLayout) {
        Class cls;
        if ((isCutoutEnabled() && child != null && params != null && (params instanceof AbsListView.LayoutParams)) && (cls = this.mAbsListParamsClass) != null) {
            doItemCutoutPadding(child, ((Integer) ReflectUtil.getObject(params, "viewType", cls)).intValue());
        }
        return super.addViewInLayout(child, index, params, isPreventRequestLayout);
    }

    private void doItemCutoutPadding(View child, int viewType) {
        Rect childRect;
        Rect originRect = this.mItemViewOriginalPadding.get(Integer.valueOf(viewType));
        if (originRect == null) {
            child.setLayoutDirection(getLayoutDirection());
            originRect = new Rect(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), child.getPaddingBottom());
            this.mItemViewOriginalPadding.put(Integer.valueOf(viewType), originRect);
            childRect = originRect;
        } else {
            childRect = new Rect(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), child.getPaddingBottom());
        }
        Rect childSafeRect = originRect;
        if ((child instanceof NonSafeInsetsAvailable) || (childSafeRect = this.mHwWidgetSafeInsets.getDisplaySafeInsets(this, originRect)) != null) {
            Rect newRect = new Rect(childSafeRect.left, child.getPaddingTop(), childSafeRect.right, child.getPaddingBottom());
            if (!newRect.equals(childRect)) {
                child.setPadding(newRect.left, newRect.top, newRect.right, newRect.bottom);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ExpandableListView, android.widget.AbsListView, android.view.ViewGroup, android.view.View, android.widget.ListView
    public void dispatchDraw(Canvas canvas) {
        Rect listSafeInsetsRect = this.mListInsetsRect;
        if (isCutoutEnabled() && !(this.mListRect.left == listSafeInsetsRect.left && this.mListRect.right == listSafeInsetsRect.right)) {
            this.mPaddingLeft = listSafeInsetsRect.left;
            this.mPaddingRight = listSafeInsetsRect.right;
            super.dispatchDraw(canvas);
            this.mPaddingLeft = this.mListRect.left;
            this.mPaddingRight = this.mListRect.right;
            return;
        }
        super.dispatchDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public void adjustVerticalScrollBarBounds(Rect bounds) {
        int offsetX;
        if (bounds != null && !bounds.isEmpty() && isCutoutEnabled()) {
            int verticalScrollbarPosition = getVerticalScrollbarPosition();
            if (verticalScrollbarPosition == 0) {
                verticalScrollbarPosition = isRtlLocale() ? 1 : 2;
            }
            Rect listSafeInsetsRect = this.mListInsetsRect;
            if (this.mListRect.left != listSafeInsetsRect.left && verticalScrollbarPosition == 1) {
                offsetX = listSafeInsetsRect.left - this.mListRect.left;
            } else if (this.mListRect.right == listSafeInsetsRect.right || verticalScrollbarPosition != 2) {
                offsetX = 0;
            } else {
                offsetX = this.mListRect.right - listSafeInsetsRect.right;
            }
            bounds.offset(offsetX, 0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateListRect();
        registerReceiver();
        if (this.mIsHasRegistReciver) {
            this.mRollbackRuleDetector.start(this);
        }
    }

    public void setScrollTopEnable(boolean isScrollTopEnable) {
        if (isScrollTopEnable != this.mIsScrollTopEnable) {
            this.mIsScrollTopEnable = isScrollTopEnable;
            if (isScrollTopEnable) {
                registerReceiver();
                if (this.mIsHasRegistReciver) {
                    this.mRollbackRuleDetector.start(this);
                    return;
                }
                return;
            }
            unregisterReceiver();
            this.mRollbackRuleDetector.stop();
        }
    }

    private void registerReceiver() {
        if (this.mIsScrollTopEnable && !this.mIsHasRegistReciver && this.mReceiverContext != null) {
            if (this.mFilter == null) {
                this.mFilter = new IntentFilter(CLICL_STATUS_BAR_ACTION);
            }
            try {
                this.mReceiverContext.registerReceiver(this.mScrollToTopReceiver, this.mFilter, SYSTEMUI_PERMITION, null);
                this.mIsHasRegistReciver = true;
            } catch (IllegalStateException e) {
                Log.w(TAG, "registerReceiver IllegalStateException");
                this.mIsHasRegistReciver = false;
            } catch (ReceiverCallNotAllowedException e2) {
                Log.w(TAG, "There is a problem with the APP application scenario:BroadcastReceiver components are not allowed to register to receive intents");
                this.mIsHasRegistReciver = false;
            }
        }
    }

    private void unregisterReceiver() {
        Context context;
        if (this.mIsHasRegistReciver && (context = this.mReceiverContext) != null) {
            try {
                context.unregisterReceiver(this.mScrollToTopReceiver);
                this.mIsHasRegistReciver = false;
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View, android.widget.ListView, android.widget.AdapterView
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver();
        this.mRollbackRuleDetector.stop();
    }

    @Override // android.widget.AbsListView, android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        this.mRollbackRuleDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    private void initFitCutout() {
        this.mAbsListParamsClass = ReflectUtil.getPrivateClass("android.widget.AbsListView$LayoutParams");
    }
}
