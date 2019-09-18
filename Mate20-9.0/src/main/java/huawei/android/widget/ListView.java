package huawei.android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FastScrollerEx;
import android.widget.ListAdapter;
import android.widget.RemoteViews;
import androidhwext.R;
import huawei.android.widget.RollbackRuleDetector;
import java.util.HashMap;
import java.util.Map;

@RemoteViews.RemoteView
public class ListView extends android.widget.ListView {
    private static final String CLICL_STATUS_BAR_ACTION = "com.huawei.intent.action.CLICK_STATUSBAR";
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", IS_EMUI_LITE);
    private static final int SCREEN_PAGE_NUMBER = 15;
    private static final int SCROLL_TO_TOP_DURATION = 600;
    private static final String SYSTEMUI_PERMITION = "huawei.permission.CLICK_STATUSBAR_BROADCAST";
    private static final String TAG = "ListView";
    private Map<Integer, String> allViewOriginalPadding;
    private Context mContext;
    private IntentFilter mFilter;
    private boolean mHasRegistReciver;
    /* access modifiers changed from: private */
    public boolean mHasUsedRollback;
    private HwCutoutUtil mHwCutoutUtil;
    private Context mReceiverContext;
    /* access modifiers changed from: private */
    public RollbackRuleDetector mRollbackRuleDetector;
    private BroadcastReceiver mScrollToTopReceiver;
    private boolean mScrollTopEnable;

    public ListView(Context context) {
        this(context, null);
    }

    public ListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.allViewOriginalPadding = new HashMap();
        this.mHwCutoutUtil = null;
        this.mScrollTopEnable = IS_EMUI_LITE;
        this.mHasRegistReciver = IS_EMUI_LITE;
        this.mScrollToTopReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ListView.CLICL_STATUS_BAR_ACTION.equals(intent.getAction())) {
                    ListView.this.post(new Runnable() {
                        public void run() {
                            int num = ListView.this.getChildCount() * 15;
                            if (ListView.this.getFirstVisiblePosition() > num) {
                                ListView.this.setSelection(num);
                            }
                            ListView.this.smoothScrollToPositionFromTop(0, 0, ListView.SCROLL_TO_TOP_DURATION);
                            if (!ListView.this.mHasUsedRollback) {
                                ListView.this.mRollbackRuleDetector.postScrollUsedEvent();
                                boolean unused = ListView.this.mHasUsedRollback = true;
                            }
                        }
                    });
                }
            }
        };
        this.mContext = context;
        if (context.getApplicationContext() != null) {
            this.mReceiverContext = context.getApplicationContext();
        } else {
            this.mReceiverContext = context;
        }
        this.mHwCutoutUtil = new HwCutoutUtil();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HwListView, defStyleAttr, defStyleRes);
        this.mScrollTopEnable = a.getBoolean(0, true);
        a.recycle();
        this.mRollbackRuleDetector = new RollbackRuleDetector(new RollbackRuleDetector.RollBackScrollListener() {
            public int getScrollYDistance() {
                View view = ListView.this.getChildAt(0);
                if (view != null) {
                    return (ListView.this.getFirstVisiblePosition() * view.getHeight()) - view.getTop();
                }
                return 0;
            }
        });
    }

    public void setItemDeleteAnimation(boolean enable) {
        if ("com.android.mms".equals(getContext().getPackageName()) && enable) {
            this.mIsSupportAnim = true;
        }
        if (IS_EMUI_LITE) {
            this.mIsSupportAnim = IS_EMUI_LITE;
        }
        wrapObserver();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        wrapObserver();
        registerReceiver();
        if (this.mHasRegistReciver) {
            this.mRollbackRuleDetector.start(this);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!this.mIsSupportAnim || this.mListDeleteAnimator == null) {
            return super.dispatchTouchEvent(ev);
        }
        this.mListDeleteAnimator.cancel();
        return true;
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (this.mHwCutoutUtil != null) {
            this.mHwCutoutUtil.checkCutoutStatus(insets, this, this.mContext);
        }
        return super.onApplyWindowInsets(insets);
    }

    public void onDraw(Canvas c) {
        int mRight;
        int mLeft;
        super.onDraw(c);
        ListAdapter adapter = getAdapter();
        int childCount = getChildCount();
        if (this.mHwCutoutUtil != null) {
            this.mHwCutoutUtil.checkViewInCutoutArea(this);
        }
        for (int i = 0; i < childCount; i++) {
            View item = getChildAt(i);
            if (!(item == null || adapter == null)) {
                int firstVisibleItemPosition = getFirstVisiblePosition();
                if (firstVisibleItemPosition + i < adapter.getCount()) {
                    int viewType = adapter.getItemViewType(firstVisibleItemPosition + i);
                    String originalPadding = this.allViewOriginalPadding.get(Integer.valueOf(viewType));
                    if (originalPadding == null) {
                        mLeft = item.getPaddingLeft();
                        mRight = item.getPaddingRight();
                        this.allViewOriginalPadding.put(Integer.valueOf(viewType), "" + mLeft + ":" + mRight);
                    } else {
                        String[] singlePadding = originalPadding.split(":");
                        mLeft = Integer.parseInt(singlePadding[0]);
                        mRight = Integer.parseInt(singlePadding[1]);
                    }
                    if (this.mHwCutoutUtil != null) {
                        this.mHwCutoutUtil.doCutoutPadding(item, mLeft, mRight);
                    }
                } else {
                    return;
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        this.mRollbackRuleDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        if (this.mHwCutoutUtil == null || !this.mHwCutoutUtil.getNeedFitCutout() || !this.mHwCutoutUtil.getViewInCutoutArea()) {
            super.dispatchDraw(canvas);
            return;
        }
        int rotate = this.mHwCutoutUtil.getDisplayRotate();
        int mCutoutPadding = this.mHwCutoutUtil.getCutoutPadding();
        HwCutoutUtil hwCutoutUtil = this.mHwCutoutUtil;
        boolean noNavigationBar = !HwCutoutUtil.isNavigationBarExist(this.mContext);
        if (1 == rotate) {
            this.mPaddingLeft += mCutoutPadding;
            super.dispatchDraw(canvas);
            this.mPaddingLeft -= mCutoutPadding;
        } else if (3 != rotate || !noNavigationBar) {
            super.dispatchDraw(canvas);
        } else {
            this.mPaddingRight += mCutoutPadding;
            super.dispatchDraw(canvas);
            this.mPaddingRight -= mCutoutPadding;
        }
    }

    /* access modifiers changed from: protected */
    public void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        if (this.mHwCutoutUtil != null) {
            int verticalScrollbarPosition = getVerticalScrollbarPosition();
            if (verticalScrollbarPosition == 0) {
                verticalScrollbarPosition = isRtlLocale() ? 1 : 2;
            }
            int scrollbarCutoutPadding = this.mHwCutoutUtil.caculateScrollBarPadding(verticalScrollbarPosition);
            l += scrollbarCutoutPadding;
            r += scrollbarCutoutPadding;
        }
        super.onDrawVerticalScrollBar(canvas, scrollBar, l, t, r, b);
    }

    public Object getScroller() {
        return super.getScrollerInner();
    }

    public void setScroller(FastScrollerEx scroller) {
        super.setScrollerInner(scroller);
    }

    public void setScrollerInner(Object fastScroller) {
        if (fastScroller == null || (fastScroller instanceof FastScrollerEx)) {
            super.setScrollerInner((FastScrollerEx) fastScroller);
        }
    }

    public void setScrollTopEnable(boolean scrollTopEnable) {
        if (scrollTopEnable != this.mScrollTopEnable) {
            this.mScrollTopEnable = scrollTopEnable;
            if (scrollTopEnable) {
                registerReceiver();
                if (this.mHasRegistReciver) {
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
        if (this.mScrollTopEnable && !this.mHasRegistReciver && this.mReceiverContext != null) {
            if (this.mFilter == null) {
                this.mFilter = new IntentFilter(CLICL_STATUS_BAR_ACTION);
            }
            try {
                this.mReceiverContext.registerReceiver(this.mScrollToTopReceiver, this.mFilter, SYSTEMUI_PERMITION, null);
                this.mHasRegistReciver = true;
            } catch (ReceiverCallNotAllowedException e) {
                Log.w(TAG, "There is a problem with the APP application scenario:BroadcastReceiver components are not allowed to register to receive intents");
                this.mHasRegistReciver = IS_EMUI_LITE;
            }
        }
    }

    private void unregisterReceiver() {
        if (this.mHasRegistReciver && this.mReceiverContext != null) {
            try {
                this.mReceiverContext.unregisterReceiver(this.mScrollToTopReceiver);
                this.mHasRegistReciver = IS_EMUI_LITE;
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver();
        this.mRollbackRuleDetector.stop();
    }
}
