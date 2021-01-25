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

public class ScrollView extends android.widget.ScrollView implements ScrollCallback {
    private static final String CLICL_STATUS_BAR_ACTION = "com.huawei.intent.action.CLICK_STATUSBAR";
    private static final int DEFAULT_SENSITIVITY_MODE = 1;
    private static final String SYSTEMUI_PERMITION = "huawei.permission.CLICK_STATUSBAR_BROADCAST";
    private static final String TAG = "ScrollView";
    private Context mContext;
    private IntentFilter mFilter;
    private HwCutoutUtil mHwCutoutUtil;
    private boolean mIsEnforceableOverScroll;
    private boolean mIsHasRegistReciver;
    private boolean mIsHasUsedRollback;
    private boolean mIsScrollTopEnable;
    private Context mReceiverContext;
    private RollbackRuleDetector mRollbackRuleDetector;
    private BroadcastReceiver mScrollToTopReceiver;

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
        this.mIsEnforceableOverScroll = false;
        this.mIsScrollTopEnable = false;
        this.mIsHasRegistReciver = false;
        this.mScrollToTopReceiver = new BroadcastReceiver() {
            /* class huawei.android.widget.ScrollView.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && ScrollView.this.getScrollY() >= 0 && ScrollView.this.getScrollY() <= ScrollView.this.getScrollRange() && ScrollView.CLICL_STATUS_BAR_ACTION.equals(intent.getAction())) {
                    ScrollView.this.handleScrollToTop();
                }
            }
        };
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = context;
        if (context.getApplicationContext() != null) {
            this.mReceiverContext = context.getApplicationContext();
        } else {
            this.mReceiverContext = context;
        }
        this.mHwCutoutUtil = new HwCutoutUtil();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HwScrollView, defStyleAttr, defStyleRes);
        this.mIsScrollTopEnable = array.getBoolean(0, true);
        int sensitivityMode = array.getInt(1, 1);
        array.recycle();
        this.mRollbackRuleDetector = new RollbackRuleDetector(new RollbackRuleDetector.RollBackScrollListener() {
            /* class huawei.android.widget.ScrollView.AnonymousClass2 */

            @Override // huawei.android.widget.RollbackRuleDetector.RollBackScrollListener
            public int getScrollYDistance() {
                return ScrollView.this.getScrollY();
            }
        });
        setExtendScrollEnabled(true);
        setSensitivityMode(sensitivityMode);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getScrollRange() {
        if (getChildCount() > 0) {
            return Math.max(0, getChildAt(0).getHeight() - ((getHeight() - getPaddingBottom()) - getPaddingTop()));
        }
        return 0;
    }

    @Override // huawei.android.widget.ScrollCallback
    public void scrollToTop() {
        handleScrollToTop();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScrollToTop() {
        post(new Runnable() {
            /* class huawei.android.widget.ScrollView.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                ScrollView.this.smoothScrollTo(0, 0);
                if (!ScrollView.this.mIsHasUsedRollback) {
                    ScrollView.this.mRollbackRuleDetector.postScrollUsedEvent();
                    ScrollView.this.mIsHasUsedRollback = true;
                }
            }
        });
    }

    public boolean dispatchStatusBarTop() {
        return true;
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

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerReceiver();
        if (this.mIsHasRegistReciver) {
            this.mRollbackRuleDetector.start(this);
        }
    }

    @Override // android.widget.ScrollView, android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        this.mRollbackRuleDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
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
    @Override // android.widget.ScrollView, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver();
        this.mRollbackRuleDetector.stop();
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        HwCutoutUtil hwCutoutUtil = this.mHwCutoutUtil;
        if (hwCutoutUtil != null) {
            hwCutoutUtil.checkCutoutStatus(insets, this, this.mContext);
        }
        return super.onApplyWindowInsets(insets);
    }

    /* access modifiers changed from: protected */
    public void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int left, int top, int right, int bottom) {
        int tempR;
        int tempL;
        HwCutoutUtil hwCutoutUtil = this.mHwCutoutUtil;
        if (hwCutoutUtil != null) {
            hwCutoutUtil.checkViewInCutoutArea(this);
            int verticalScrollbarPosition = getVerticalScrollbarPosition();
            if (verticalScrollbarPosition == 0) {
                verticalScrollbarPosition = isRtlLocale() ? 1 : 2;
            }
            int scrollbarCutoutPadding = this.mHwCutoutUtil.caculateScrollBarPadding(verticalScrollbarPosition);
            tempL = left + scrollbarCutoutPadding;
            tempR = right + scrollbarCutoutPadding;
        } else {
            tempL = left;
            tempR = right;
        }
        super.onDrawVerticalScrollBar(canvas, scrollBar, tempL, top, tempR, bottom);
    }

    public boolean isEnforceableOverScrollEnabled() {
        return this.mIsEnforceableOverScroll;
    }

    public void setEnforceableOverScrollEnabled(boolean isEnabled) {
        this.mIsEnforceableOverScroll = isEnabled;
    }
}
