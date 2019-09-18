package android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Process;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.widget.RemoteViews;
import com.android.internal.R;

@RemoteViews.RemoteView
public class ViewFlipper extends ViewAnimator {
    private static final int DEFAULT_INTERVAL = 3000;
    private static final boolean LOGD = false;
    private static final String TAG = "ViewFlipper";
    private boolean mAutoStart = false;
    /* access modifiers changed from: private */
    public int mFlipInterval = 3000;
    /* access modifiers changed from: private */
    public final Runnable mFlipRunnable = new Runnable() {
        public void run() {
            if (ViewFlipper.this.mRunning) {
                ViewFlipper.this.showNext();
                ViewFlipper.this.postDelayed(ViewFlipper.this.mFlipRunnable, (long) ViewFlipper.this.mFlipInterval);
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                boolean unused = ViewFlipper.this.mUserPresent = false;
                ViewFlipper.this.updateRunning();
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                boolean unused2 = ViewFlipper.this.mUserPresent = true;
                ViewFlipper.this.updateRunning(false);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mRunning = false;
    private boolean mStarted = false;
    /* access modifiers changed from: private */
    public boolean mUserPresent = true;
    private boolean mVisible = false;

    public ViewFlipper(Context context) {
        super(context);
    }

    public ViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewFlipper);
        this.mFlipInterval = a.getInt(0, 3000);
        this.mAutoStart = a.getBoolean(1, false);
        a.recycle();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.USER_PRESENT");
        getContext().registerReceiverAsUser(this.mReceiver, Process.myUserHandle(), filter, null, getHandler());
        if (this.mAutoStart) {
            startFlipping();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mVisible = false;
        getContext().unregisterReceiver(this.mReceiver);
        updateRunning();
    }

    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mVisible = visibility == 0;
        updateRunning(false);
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
        this.mStarted = false;
        updateRunning();
    }

    public CharSequence getAccessibilityClassName() {
        return ViewFlipper.class.getName();
    }

    /* access modifiers changed from: private */
    public void updateRunning() {
        updateRunning(true);
    }

    /* access modifiers changed from: private */
    public void updateRunning(boolean flipNow) {
        boolean running = this.mVisible && this.mStarted && this.mUserPresent;
        if (running != this.mRunning) {
            if (running) {
                showOnly(this.mWhichChild, flipNow);
                postDelayed(this.mFlipRunnable, (long) this.mFlipInterval);
            } else {
                removeCallbacks(this.mFlipRunnable);
            }
            this.mRunning = running;
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
