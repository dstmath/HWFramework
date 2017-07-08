package android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Process;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;

@RemoteView
public class ViewFlipper extends ViewAnimator {
    private static final int DEFAULT_INTERVAL = 3000;
    private static final boolean LOGD = false;
    private static final String TAG = "ViewFlipper";
    private boolean mAutoStart;
    private int mFlipInterval;
    private final Runnable mFlipRunnable;
    private final BroadcastReceiver mReceiver;
    private boolean mRunning;
    private boolean mStarted;
    private boolean mUserPresent;
    private boolean mVisible;

    public ViewFlipper(Context context) {
        super(context);
        this.mFlipInterval = DEFAULT_INTERVAL;
        this.mAutoStart = LOGD;
        this.mRunning = LOGD;
        this.mStarted = LOGD;
        this.mVisible = LOGD;
        this.mUserPresent = true;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    ViewFlipper.this.mUserPresent = ViewFlipper.LOGD;
                    ViewFlipper.this.updateRunning();
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    ViewFlipper.this.mUserPresent = true;
                    ViewFlipper.this.updateRunning(ViewFlipper.LOGD);
                }
            }
        };
        this.mFlipRunnable = new Runnable() {
            public void run() {
                if (ViewFlipper.this.mRunning) {
                    ViewFlipper.this.showNext();
                    ViewFlipper.this.postDelayed(ViewFlipper.this.mFlipRunnable, (long) ViewFlipper.this.mFlipInterval);
                }
            }
        };
    }

    public ViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFlipInterval = DEFAULT_INTERVAL;
        this.mAutoStart = LOGD;
        this.mRunning = LOGD;
        this.mStarted = LOGD;
        this.mVisible = LOGD;
        this.mUserPresent = true;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    ViewFlipper.this.mUserPresent = ViewFlipper.LOGD;
                    ViewFlipper.this.updateRunning();
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    ViewFlipper.this.mUserPresent = true;
                    ViewFlipper.this.updateRunning(ViewFlipper.LOGD);
                }
            }
        };
        this.mFlipRunnable = new Runnable() {
            public void run() {
                if (ViewFlipper.this.mRunning) {
                    ViewFlipper.this.showNext();
                    ViewFlipper.this.postDelayed(ViewFlipper.this.mFlipRunnable, (long) ViewFlipper.this.mFlipInterval);
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewFlipper);
        this.mFlipInterval = a.getInt(0, DEFAULT_INTERVAL);
        this.mAutoStart = a.getBoolean(1, LOGD);
        a.recycle();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.USER_PRESENT");
        getContext().registerReceiverAsUser(this.mReceiver, Process.myUserHandle(), filter, null, getHandler());
        if (this.mAutoStart) {
            startFlipping();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mVisible = LOGD;
        getContext().unregisterReceiver(this.mReceiver);
        updateRunning();
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        } else {
            z = LOGD;
        }
        this.mVisible = z;
        updateRunning(LOGD);
    }

    @RemotableViewMethod
    public void setFlipInterval(int milliseconds) {
        this.mFlipInterval = milliseconds;
    }

    public void startFlipping() {
        this.mStarted = true;
        updateRunning();
    }

    public void stopFlipping() {
        this.mStarted = LOGD;
        updateRunning();
    }

    public CharSequence getAccessibilityClassName() {
        return ViewFlipper.class.getName();
    }

    private void updateRunning() {
        updateRunning(true);
    }

    private void updateRunning(boolean flipNow) {
        boolean z = (this.mVisible && this.mStarted) ? this.mUserPresent : LOGD;
        if (z != this.mRunning) {
            if (z) {
                showOnly(this.mWhichChild, flipNow);
                postDelayed(this.mFlipRunnable, (long) this.mFlipInterval);
            } else {
                removeCallbacks(this.mFlipRunnable);
            }
            this.mRunning = z;
        }
    }

    public boolean isFlipping() {
        return this.mStarted;
    }

    public void setAutoStart(boolean autoStart) {
        this.mAutoStart = autoStart;
    }

    public boolean isAutoStart() {
        return this.mAutoStart;
    }
}
