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
    private static final int MAX_SCROLL_DISTANCE = 3;
    private static final int MAX_SCROLL_NUMBER = 3;
    private static final int MULT_SCROLL_TIMEOUT = 1000;
    private static final int MULT_SCROLL_TIMEOUT_ENVENT = 1;
    private static final int ROLLBACK_USED = 1;
    private DecisionHelper mDecisionHelper;
    private GestureDetector mGestureDetector;
    private Handler mHandler = new Handler() {
        /* class huawei.android.widget.RollbackRuleDetector.AnonymousClass1 */

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                RollbackRuleDetector.this.mScrollCount = 0;
            }
        }
    };
    private boolean mIsRunning;
    private int mMaxScrollHeight;
    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.OnGestureListener() {
        /* class huawei.android.widget.RollbackRuleDetector.AnonymousClass2 */

        public boolean onDown(MotionEvent e) {
            return false;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            RollbackRuleDetector.this.mHandler.removeMessages(1);
            if (e1 == null || e2 == null || e1.getY() >= e2.getY() || Math.abs(velocityY) <= Math.abs(velocityX)) {
                RollbackRuleDetector.this.mScrollCount = 0;
            } else {
                RollbackRuleDetector.access$008(RollbackRuleDetector.this);
                RollbackRuleDetector.this.calMaxScollHeight();
                if (RollbackRuleDetector.this.isExceedMaxCount() && RollbackRuleDetector.this.isExceedMaxHeight()) {
                    RollbackRuleDetector.this.mScrollCount = 0;
                    if (RollbackRuleDetector.this.mDecisionHelper != null) {
                        RollbackRuleDetector.this.mDecisionHelper.executeEvent(DecisionHelper.ROLLBACK_EVENT);
                    }
                }
                RollbackRuleDetector.this.mHandler.sendEmptyMessageDelayed(1, 1000);
            }
            return false;
        }
    };
    private RollBackScrollListener mRollBackScrollListener;
    private int mScrollCount;
    private Context mServiceContext;
    private View mView;

    public interface RollBackScrollListener {
        int getScrollYDistance();
    }

    static /* synthetic */ int access$008(RollbackRuleDetector x0) {
        int i = x0.mScrollCount;
        x0.mScrollCount = i + 1;
        return i;
    }

    public RollbackRuleDetector(RollBackScrollListener rollBackScrollListener) {
        this.mRollBackScrollListener = rollBackScrollListener;
    }

    public void start(View view) {
        if (this.mIsRunning) {
            Log.w(LOG, "RollbackRuleDetector already start");
        } else if (view != null && isRollbackUnused(view.getContext())) {
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
                this.mIsRunning = true;
            } catch (ReceiverCallNotAllowedException e) {
                Log.w(LOG, "There is a problem with the APP application scenario:BroadcastReceiver components are not allowed to register to receive intents");
                this.mDecisionHelper = null;
                this.mView = null;
                this.mGestureDetector = null;
                this.mIsRunning = false;
            }
        }
    }

    public void stop() {
        if (!this.mIsRunning) {
            Log.w(LOG, "RollbackRuleDetector already stop");
            return;
        }
        Context context = this.mServiceContext;
        if (context == null) {
            Log.w(LOG, "mServiceContext is null");
            return;
        }
        DecisionHelper decisionHelper = this.mDecisionHelper;
        if (decisionHelper != null) {
            decisionHelper.unbindService(context);
            this.mDecisionHelper = null;
            this.mView = null;
            this.mGestureDetector = null;
            this.mIsRunning = false;
        }
    }

    public void onTouchEvent(MotionEvent ev) {
        if (!this.mIsRunning) {
            Log.w(LOG, "RollbackRuleDetector already stop");
            return;
        }
        GestureDetector gestureDetector = this.mGestureDetector;
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
    }

    private boolean isRollbackUnused(Context context) {
        if (context == null) {
            Log.w(LOG, "isRollbackUnused context is null");
            return false;
        } else if ((Settings.Secure.getInt(context.getContentResolver(), "com.huawei.recsys.LMT_FeatureRecStatus", 0) & 1) != 1) {
            return true;
        } else {
            return false;
        }
    }

    public void postScrollUsedEvent() {
        DecisionHelper decisionHelper = this.mDecisionHelper;
        if (decisionHelper != null) {
            decisionHelper.executeEvent(DecisionHelper.ROLLBACK_USED_EVENT);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void calMaxScollHeight() {
        View view = this.mView;
        if (view != null) {
            this.mMaxScrollHeight = view.getHeight() * 3;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isExceedMaxHeight() {
        RollBackScrollListener rollBackScrollListener = this.mRollBackScrollListener;
        if (rollBackScrollListener == null || rollBackScrollListener.getScrollYDistance() <= this.mMaxScrollHeight) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isExceedMaxCount() {
        return this.mScrollCount > 3;
    }
}
