package huawei.android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowInsets;
import androidhwext.R;
import huawei.android.widget.RollbackRuleDetector;

public class ScrollView extends android.widget.ScrollView {
    private static final String CLICL_STATUS_BAR_ACTION = "com.huawei.intent.action.CLICK_STATUSBAR";
    private static final String SYSTEMUI_PERMITION = "huawei.permission.CLICK_STATUSBAR_BROADCAST";
    private static final String TAG = "ScrollView";
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

    public ScrollView(Context context) {
        this(context, null);
    }

    public ScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842880);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHwCutoutUtil = null;
        this.mScrollTopEnable = false;
        this.mHasRegistReciver = false;
        this.mScrollToTopReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ScrollView.CLICL_STATUS_BAR_ACTION.equals(intent.getAction())) {
                    ScrollView.this.post(new Runnable() {
                        public void run() {
                            ScrollView.this.smoothScrollTo(0, 0);
                            if (!ScrollView.this.mHasUsedRollback) {
                                ScrollView.this.mRollbackRuleDetector.postScrollUsedEvent();
                                boolean unused = ScrollView.this.mHasUsedRollback = true;
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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HwScrollView, defStyleAttr, defStyleRes);
        this.mScrollTopEnable = a.getBoolean(0, true);
        a.recycle();
        this.mRollbackRuleDetector = new RollbackRuleDetector(new RollbackRuleDetector.RollBackScrollListener() {
            public int getScrollYDistance() {
                return ScrollView.this.getScrollY();
            }
        });
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

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerReceiver();
        if (this.mHasRegistReciver) {
            this.mRollbackRuleDetector.start(this);
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        this.mRollbackRuleDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
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
                this.mHasRegistReciver = false;
            }
        }
    }

    private void unregisterReceiver() {
        if (this.mHasRegistReciver && this.mReceiverContext != null) {
            try {
                this.mReceiverContext.unregisterReceiver(this.mScrollToTopReceiver);
                this.mHasRegistReciver = false;
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

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (this.mHwCutoutUtil != null) {
            this.mHwCutoutUtil.checkCutoutStatus(insets, this, this.mContext);
        }
        return super.onApplyWindowInsets(insets);
    }

    /* access modifiers changed from: protected */
    public void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        if (this.mHwCutoutUtil != null) {
            this.mHwCutoutUtil.checkViewInCutoutArea(this);
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
}
