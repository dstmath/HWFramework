package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

public class ZoomControls extends LinearLayout {
    private final ZoomButton mZoomIn;
    private final ZoomButton mZoomOut;

    public ZoomControls(Context context) {
        this(context, null);
    }

    public ZoomControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(17367342, (ViewGroup) this, true);
        this.mZoomIn = (ZoomButton) findViewById(16909550);
        this.mZoomOut = (ZoomButton) findViewById(16909552);
    }

    public void setOnZoomInClickListener(View.OnClickListener listener) {
        this.mZoomIn.setOnClickListener(listener);
    }

    public void setOnZoomOutClickListener(View.OnClickListener listener) {
        this.mZoomOut.setOnClickListener(listener);
    }

    public void setZoomSpeed(long speed) {
        this.mZoomIn.setZoomSpeed(speed);
        this.mZoomOut.setZoomSpeed(speed);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void show() {
        fade(0, 0.0f, 1.0f);
    }

    public void hide() {
        fade(8, 1.0f, 0.0f);
    }

    private void fade(int visibility, float startAlpha, float endAlpha) {
        AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
        anim.setDuration(500);
        startAnimation(anim);
        setVisibility(visibility);
    }

    public void setIsZoomInEnabled(boolean isEnabled) {
        this.mZoomIn.setEnabled(isEnabled);
    }

    public void setIsZoomOutEnabled(boolean isEnabled) {
        this.mZoomOut.setEnabled(isEnabled);
    }

    public boolean hasFocus() {
        return this.mZoomIn.hasFocus() || this.mZoomOut.hasFocus();
    }

    public CharSequence getAccessibilityClassName() {
        return ZoomControls.class.getName();
    }
}
