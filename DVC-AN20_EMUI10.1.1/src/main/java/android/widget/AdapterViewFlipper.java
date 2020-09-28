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
public class AdapterViewFlipper extends AdapterViewAnimator {
    private static final int DEFAULT_INTERVAL = 10000;
    private static final boolean LOGD = false;
    private static final String TAG = "ViewFlipper";
    private boolean mAdvancedByHost;
    private boolean mAutoStart;
    private int mFlipInterval;
    private final Runnable mFlipRunnable;
    private final BroadcastReceiver mReceiver;
    private boolean mRunning;
    private boolean mStarted;
    private boolean mUserPresent;
    private boolean mVisible;

    public AdapterViewFlipper(Context context) {
        super(context);
        this.mFlipInterval = 10000;
        this.mAutoStart = false;
        this.mRunning = false;
        this.mStarted = false;
        this.mVisible = false;
        this.mUserPresent = true;
        this.mAdvancedByHost = false;
        this.mReceiver = new BroadcastReceiver() {
            /* class android.widget.AdapterViewFlipper.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    AdapterViewFlipper.this.mUserPresent = false;
                    AdapterViewFlipper.this.updateRunning();
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    AdapterViewFlipper.this.mUserPresent = true;
                    AdapterViewFlipper.this.updateRunning(false);
                }
            }
        };
        this.mFlipRunnable = new Runnable() {
            /* class android.widget.AdapterViewFlipper.AnonymousClass2 */

            public void run() {
                if (AdapterViewFlipper.this.mRunning) {
                    AdapterViewFlipper.this.showNext();
                }
            }
        };
    }

    public AdapterViewFlipper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdapterViewFlipper(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AdapterViewFlipper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mFlipInterval = 10000;
        this.mAutoStart = false;
        this.mRunning = false;
        this.mStarted = false;
        this.mVisible = false;
        this.mUserPresent = true;
        this.mAdvancedByHost = false;
        this.mReceiver = new BroadcastReceiver() {
            /* class android.widget.AdapterViewFlipper.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    AdapterViewFlipper.this.mUserPresent = false;
                    AdapterViewFlipper.this.updateRunning();
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    AdapterViewFlipper.this.mUserPresent = true;
                    AdapterViewFlipper.this.updateRunning(false);
                }
            }
        };
        this.mFlipRunnable = new Runnable() {
            /* class android.widget.AdapterViewFlipper.AnonymousClass2 */

            public void run() {
                if (AdapterViewFlipper.this.mRunning) {
                    AdapterViewFlipper.this.showNext();
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AdapterViewFlipper, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.AdapterViewFlipper, attrs, a, defStyleAttr, defStyleRes);
        this.mFlipInterval = a.getInt(0, 10000);
        this.mAutoStart = a.getBoolean(1, false);
        this.mLoopViews = true;
        a.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        getContext().registerReceiverAsUser(this.mReceiver, Process.myUserHandle(), filter, null, getHandler());
        if (this.mAutoStart) {
            startFlipping();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.widget.AdapterView, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mVisible = false;
        getContext().unregisterReceiver(this.mReceiver);
        updateRunning();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mVisible = visibility == 0;
        updateRunning(false);
    }

    @Override // android.widget.AdapterViewAnimator, android.widget.AdapterView
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        updateRunning();
    }

    public int getFlipInterval() {
        return this.mFlipInterval;
    }

    public void setFlipInterval(int flipInterval) {
        this.mFlipInterval = flipInterval;
    }

    public void startFlipping() {
        this.mStarted = true;
        updateRunning();
    }

    public void stopFlipping() {
        this.mStarted = false;
        updateRunning();
    }

    @Override // android.widget.AdapterViewAnimator
    @RemotableViewMethod
    public void showNext() {
        if (this.mRunning) {
            removeCallbacks(this.mFlipRunnable);
            postDelayed(this.mFlipRunnable, (long) this.mFlipInterval);
        }
        super.showNext();
    }

    @Override // android.widget.AdapterViewAnimator
    @RemotableViewMethod
    public void showPrevious() {
        if (this.mRunning) {
            removeCallbacks(this.mFlipRunnable);
            postDelayed(this.mFlipRunnable, (long) this.mFlipInterval);
        }
        super.showPrevious();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRunning() {
        updateRunning(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRunning(boolean flipNow) {
        boolean running = !this.mAdvancedByHost && this.mVisible && this.mStarted && this.mUserPresent && this.mAdapter != null;
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

    @Override // android.widget.AdapterViewAnimator, android.widget.Advanceable
    public void fyiWillBeAdvancedByHostKThx() {
        this.mAdvancedByHost = true;
        updateRunning(false);
    }

    @Override // android.widget.AdapterViewAnimator, android.view.View, android.widget.AdapterView, android.view.ViewGroup
    public CharSequence getAccessibilityClassName() {
        return AdapterViewFlipper.class.getName();
    }
}
