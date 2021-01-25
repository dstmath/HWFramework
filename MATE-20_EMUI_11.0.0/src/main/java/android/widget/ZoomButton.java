package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

@Deprecated
public class ZoomButton extends ImageButton implements View.OnLongClickListener {
    private boolean mIsInLongpress;
    private final Runnable mRunnable;
    private long mZoomSpeed;

    public ZoomButton(Context context) {
        this(context, null);
    }

    public ZoomButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ZoomButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mRunnable = new Runnable() {
            /* class android.widget.ZoomButton.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                if (ZoomButton.this.hasOnClickListeners() && ZoomButton.this.mIsInLongpress && ZoomButton.this.isEnabled()) {
                    ZoomButton.this.callOnClick();
                    ZoomButton zoomButton = ZoomButton.this;
                    zoomButton.postDelayed(this, zoomButton.mZoomSpeed);
                }
            }
        };
        this.mZoomSpeed = 1000;
        setOnLongClickListener(this);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 3 || event.getAction() == 1) {
            this.mIsInLongpress = false;
        }
        return super.onTouchEvent(event);
    }

    public void setZoomSpeed(long speed) {
        this.mZoomSpeed = speed;
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View v) {
        this.mIsInLongpress = true;
        post(this.mRunnable);
        return true;
    }

    @Override // android.widget.ImageView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        this.mIsInLongpress = false;
        return super.onKeyUp(keyCode, event);
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            setPressed(false);
        }
        super.setEnabled(enabled);
    }

    @Override // android.view.View
    public boolean dispatchUnhandledMove(View focused, int direction) {
        clearFocus();
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override // android.widget.ImageButton, android.widget.ImageView, android.view.View
    public CharSequence getAccessibilityClassName() {
        return ZoomButton.class.getName();
    }
}
