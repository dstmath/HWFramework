package com.android.server.gesture.anim;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GLGestureBackView extends GLSurfaceView {
    public static final int ANIMATION_TYPE_FAST_SLIDING = 1;
    public static final int ANIMATION_TYPE_SLOW_SLIDING_DISAPPEAR = 2;
    private GLGestureBackRender mRender;

    public interface GestureBackAnimListener {
        void onAnimationEnd(int i);
    }

    public interface ViewSizeChangeListener {
        void onViewSizeChanged(int i, int i2);
    }

    public GLGestureBackView(Context context) {
        super(context);
        initSurfaceView(context);
    }

    public GLGestureBackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSurfaceView(context);
    }

    private void initSurfaceView(Context context) {
        setEGLContextClientVersion(3);
        setZOrderOnTop(true);
        getHolder().setFormat(1);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.mRender = new GLGestureBackRender(context);
        setRenderer(this.mRender);
        setRenderMode(1);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void setAnimProcess(float process) {
        this.mRender.setAnimProcess(process);
    }

    public void setSide(boolean isLeft) {
        this.mRender.setSide(isLeft);
    }

    public void setAnimPosition(float y) {
        this.mRender.setAnimPosition(y);
    }

    public void setDraw(boolean draw) {
        this.mRender.setDraw(draw);
    }

    public void addAnimationListener(GestureBackAnimListener listener) {
        this.mRender.addAnimationListener(listener);
    }

    public void addViewChangedListener(ViewSizeChangeListener listener) {
        this.mRender.addViewSizeChangeListener(listener);
    }

    public void playFastSlidingAnim() {
        this.mRender.playFastSlidingAnim();
    }

    public void playDisappearAnim() {
        this.mRender.playDisappearAnim();
    }
}
