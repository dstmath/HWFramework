package huawei.android.widget;

import android.content.Context;
import android.content.ReceiverCallNotAllowedException;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.huawei.decision.DecisionHelper;

public class RollbackRuleDetector {
    private static final boolean DEBUG = false;
    private static final String LOG = RollbackRuleDetector.class.getSimpleName();
    private static final int MAX_SCROLL_NUMBER = 3;
    private static final int MULT_SCROLL_TIMEOUT = 1000;
    private static final int MULT_SCROLL_TIMEOUT_ENVENT = 1;
    private static final int ROLLBACK_USED = 1;
    /* access modifiers changed from: private */
    public DecisionHelper mDecisionHelper;
    private GestureDetector mGestureDetector;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                int unused = RollbackRuleDetector.this.mScrollCount = 0;
            }
        }
    };
    private int mMaxScrollHeight;
    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.OnGestureListener() {
        public boolean onDown(MotionEvent e) {
            return RollbackRuleDetector.DEBUG;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return RollbackRuleDetector.DEBUG;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return RollbackRuleDetector.DEBUG;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            RollbackRuleDetector.this.mHandler.removeMessages(1);
            if (e1 == null || e2 == null || e1.getY() >= e2.getY() || Math.abs(velocityY) <= Math.abs(velocityX)) {
                int unused = RollbackRuleDetector.this.mScrollCount = 0;
            } else {
                int unused2 = RollbackRuleDetector.this.mScrollCount = RollbackRuleDetector.this.mScrollCount + 1;
                RollbackRuleDetector.this.calMaxScollHeight();
                if (RollbackRuleDetector.this.isExceedMaxCount() && RollbackRuleDetector.this.isExceedMaxHeight()) {
                    int unused3 = RollbackRuleDetector.this.mScrollCount = 0;
                    if (RollbackRuleDetector.this.mDecisionHelper != null) {
                        RollbackRuleDetector.this.mDecisionHelper.executeEvent(DecisionHelper.ROLLBACK_EVENT);
                    }
                }
                RollbackRuleDetector.this.mHandler.sendEmptyMessageDelayed(1, 1000);
            }
            return RollbackRuleDetector.DEBUG;
        }
    };
    private RollBackScrollListener mRollBackScrollListener;
    private boolean mRunning;
    /* access modifiers changed from: private */
    public int mScrollCount;
    private Context mServiceContext;
    private View mView;

    public interface RollBackScrollListener {
        int getScrollYDistance();
    }

    public RollbackRuleDetector(RollBackScrollListener rollBackScrollListener) {
        this.mRollBackScrollListener = rollBackScrollListener;
    }

    public void start(View view) {
        if (this.mRunning) {
            Log.w(LOG, "RollbackRuleDetector already start");
            return;
        }
        if (view != null && isRollbackUnused(view.getContext())) {
            this.mView = view;
            this.mDecisionHelper = new DecisionHelper();
            Context context = this.mView.getContext();
            if (context == null) {
                Log.w(LOG, "context is null");
                return;
            }
            this.mServiceContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
            try {
                this.mDecisionHelper.bindService(this.mServiceContext);
                this.mGestureDetector = new GestureDetector(this.mView.getContext(), this.mOnGestureListener);
                this.mRunning = true;
            } catch (ReceiverCallNotAllowedException e) {
                Log.w(LOG, "There is a problem with the APP application scenario:BroadcastReceiver components are not allowed to register to receive intents");
                this.mDecisionHelper = null;
                this.mView = null;
                this.mGestureDetector = null;
                this.mRunning = DEBUG;
            }
        }
    }

    public void stop() {
        if (!this.mRunning) {
            Log.w(LOG, "RollbackRuleDetector already stop");
        } else if (this.mServiceContext == null) {
            Log.w(LOG, "mServiceContext is null");
        } else {
            if (this.mDecisionHelper != null) {
                this.mDecisionHelper.unbindService(this.mServiceContext);
                this.mDecisionHelper = null;
                this.mView = null;
                this.mGestureDetector = null;
                this.mRunning = DEBUG;
            }
        }
    }

    public void onTouchEvent(MotionEvent ev) {
        if (!this.mRunning) {
            Log.w(LOG, "RollbackRuleDetector already stop");
            return;
        }
        if (this.mGestureDetector != null) {
            this.mGestureDetector.onTouchEvent(ev);
        }
    }

    private boolean isRollbackUnused(Context context) {
        boolean z = DEBUG;
        if (context == null) {
            Log.w(LOG, "isRollbackUnused context is null");
            return DEBUG;
        }
        if ((Settings.Secure.getInt(context.getContentResolver(), "com.huawei.recsys.LMT_FeatureRecStatus", 0) & 1) != 1) {
            z = true;
        }
        return z;
    }

    public void postScrollUsedEvent() {
        if (this.mDecisionHelper != null) {
            this.mDecisionHelper.executeEvent(DecisionHelper.ROLLBACK_USED_EVENT);
        }
    }

    /* access modifiers changed from: private */
    public void calMaxScollHeight() {
        if (this.mView != null) {
            this.mMaxScrollHeight = this.mView.getHeight() * 3;
        }
    }

    /* access modifiers changed from: private */
    public boolean isExceedMaxHeight() {
        RollBackScrollListener rollBackScrollListener = this.mRollBackScrollListener;
        boolean z = DEBUG;
        if (rollBackScrollListener == null) {
            return DEBUG;
        }
        if (this.mRollBackScrollListener.getScrollYDistance() > this.mMaxScrollHeight) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isExceedMaxCount() {
        if (this.mScrollCount > 3) {
            return true;
        }
        return DEBUG;
    }
}
