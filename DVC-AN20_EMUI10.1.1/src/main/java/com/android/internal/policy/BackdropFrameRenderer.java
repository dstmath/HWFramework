package com.android.internal.policy;

import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.Choreographer;
import android.view.ThreadedRenderer;

public class BackdropFrameRenderer extends Thread implements Choreographer.FrameCallback {
    private Drawable mCaptionBackgroundDrawable;
    private Choreographer mChoreographer;
    private DecorView mDecorView;
    private RenderNode mFrameAndBackdropNode;
    private boolean mFullscreen;
    private int mLastCaptionHeight;
    private int mLastContentHeight;
    private int mLastContentWidth;
    private int mLastXOffset;
    private int mLastYOffset;
    private ColorDrawable mNavigationBarColor;
    private final Rect mNewTargetRect = new Rect();
    private boolean mOldFullscreen;
    private final Rect mOldStableInsets = new Rect();
    private final Rect mOldSystemInsets = new Rect();
    private final Rect mOldTargetRect = new Rect();
    private ThreadedRenderer mRenderer;
    private boolean mReportNextDraw;
    private Drawable mResizingBackgroundDrawable;
    private final Rect mStableInsets = new Rect();
    private ColorDrawable mStatusBarColor;
    private RenderNode mSystemBarBackgroundNode;
    private final Rect mSystemInsets = new Rect();
    private final Rect mTargetRect = new Rect();
    private final Rect mTmpRect = new Rect();
    private Drawable mUserCaptionBackgroundDrawable;

    public BackdropFrameRenderer(DecorView decorView, ThreadedRenderer renderer, Rect initialBounds, Drawable resizingBackgroundDrawable, Drawable captionBackgroundDrawable, Drawable userCaptionBackgroundDrawable, int statusBarColor, int navigationBarColor, boolean fullscreen, Rect systemInsets, Rect stableInsets) {
        setName("ResizeFrame");
        this.mRenderer = renderer;
        onResourcesLoaded(decorView, resizingBackgroundDrawable, captionBackgroundDrawable, userCaptionBackgroundDrawable, statusBarColor, navigationBarColor);
        this.mFrameAndBackdropNode = RenderNode.create("FrameAndBackdropNode", null);
        this.mRenderer.addRenderNode(this.mFrameAndBackdropNode, true);
        this.mTargetRect.set(initialBounds);
        this.mFullscreen = fullscreen;
        this.mOldFullscreen = fullscreen;
        this.mSystemInsets.set(systemInsets);
        this.mStableInsets.set(stableInsets);
        this.mOldSystemInsets.set(systemInsets);
        this.mOldStableInsets.set(stableInsets);
        start();
    }

    /* access modifiers changed from: package-private */
    public void onResourcesLoaded(DecorView decorView, Drawable resizingBackgroundDrawable, Drawable captionBackgroundDrawableDrawable, Drawable userCaptionBackgroundDrawable, int statusBarColor, int navigationBarColor) {
        Drawable drawable;
        Drawable drawable2;
        Drawable drawable3;
        synchronized (this) {
            this.mDecorView = decorView;
            if (resizingBackgroundDrawable == null || resizingBackgroundDrawable.getConstantState() == null) {
                drawable = null;
            } else {
                drawable = resizingBackgroundDrawable.getConstantState().newDrawable();
            }
            this.mResizingBackgroundDrawable = drawable;
            if (captionBackgroundDrawableDrawable == null || captionBackgroundDrawableDrawable.getConstantState() == null) {
                drawable2 = null;
            } else {
                drawable2 = captionBackgroundDrawableDrawable.getConstantState().newDrawable();
            }
            this.mCaptionBackgroundDrawable = drawable2;
            if (userCaptionBackgroundDrawable == null || userCaptionBackgroundDrawable.getConstantState() == null) {
                drawable3 = null;
            } else {
                drawable3 = userCaptionBackgroundDrawable.getConstantState().newDrawable();
            }
            this.mUserCaptionBackgroundDrawable = drawable3;
            if (this.mCaptionBackgroundDrawable == null) {
                this.mCaptionBackgroundDrawable = this.mResizingBackgroundDrawable;
            }
            if (statusBarColor != 0) {
                this.mStatusBarColor = new ColorDrawable(statusBarColor);
                addSystemBarNodeIfNeeded();
            } else {
                this.mStatusBarColor = null;
            }
            if (navigationBarColor != 0) {
                this.mNavigationBarColor = new ColorDrawable(navigationBarColor);
                addSystemBarNodeIfNeeded();
            } else {
                this.mNavigationBarColor = null;
            }
        }
    }

    private void addSystemBarNodeIfNeeded() {
        if (this.mSystemBarBackgroundNode == null) {
            this.mSystemBarBackgroundNode = RenderNode.create("SystemBarBackgroundNode", null);
            this.mRenderer.addRenderNode(this.mSystemBarBackgroundNode, false);
        }
    }

    public void setTargetRect(Rect newTargetBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets) {
        synchronized (this) {
            this.mFullscreen = fullscreen;
            this.mTargetRect.set(newTargetBounds);
            this.mSystemInsets.set(systemInsets);
            this.mStableInsets.set(stableInsets);
            pingRenderLocked(false);
        }
    }

    public void onConfigurationChange() {
        synchronized (this) {
            if (this.mRenderer != null) {
                this.mOldTargetRect.set(0, 0, 0, 0);
                pingRenderLocked(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void releaseRenderer() {
        synchronized (this) {
            if (this.mRenderer != null) {
                this.mRenderer.setContentDrawBounds(0, 0, 0, 0);
                this.mRenderer.removeRenderNode(this.mFrameAndBackdropNode);
                if (this.mSystemBarBackgroundNode != null) {
                    this.mRenderer.removeRenderNode(this.mSystemBarBackgroundNode);
                }
                this.mRenderer = null;
                pingRenderLocked(false);
            }
        }
    }

    public void run() {
        try {
            Looper.prepare();
            synchronized (this) {
                this.mChoreographer = Choreographer.getInstance();
            }
            Looper.loop();
            synchronized (this) {
                this.mChoreographer = null;
                Choreographer.releaseInstance();
            }
        } finally {
            releaseRenderer();
        }
    }

    @Override // android.view.Choreographer.FrameCallback
    public void doFrame(long frameTimeNanos) {
        synchronized (this) {
            if (this.mRenderer == null) {
                reportDrawIfNeeded();
                Looper.myLooper().quit();
                return;
            }
            doFrameUncheckedLocked();
        }
    }

    private void doFrameUncheckedLocked() {
        this.mNewTargetRect.set(this.mTargetRect);
        if (!this.mNewTargetRect.equals(this.mOldTargetRect) || this.mOldFullscreen != this.mFullscreen || !this.mStableInsets.equals(this.mOldStableInsets) || !this.mSystemInsets.equals(this.mOldSystemInsets) || this.mReportNextDraw) {
            this.mOldFullscreen = this.mFullscreen;
            this.mOldTargetRect.set(this.mNewTargetRect);
            this.mOldSystemInsets.set(this.mSystemInsets);
            this.mOldStableInsets.set(this.mStableInsets);
            redrawLocked(this.mNewTargetRect, this.mFullscreen, this.mSystemInsets, this.mStableInsets);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onContentDrawn(int xOffset, int yOffset, int xSize, int ySize) {
        boolean z;
        synchronized (this) {
            z = true;
            boolean firstCall = this.mLastContentWidth == 0;
            this.mLastContentWidth = xSize;
            this.mLastContentHeight = ySize - this.mLastCaptionHeight;
            this.mLastXOffset = xOffset;
            this.mLastYOffset = yOffset;
            this.mRenderer.setContentDrawBounds(this.mLastXOffset, this.mLastYOffset, this.mLastXOffset + this.mLastContentWidth, this.mLastYOffset + this.mLastCaptionHeight + this.mLastContentHeight);
            if (!firstCall || (this.mLastCaptionHeight == 0 && this.mDecorView.isShowingCaption())) {
                z = false;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void onRequestDraw(boolean reportNextDraw) {
        synchronized (this) {
            this.mReportNextDraw = reportNextDraw;
            this.mOldTargetRect.set(0, 0, 0, 0);
            pingRenderLocked(true);
        }
    }

    private void redrawLocked(Rect newBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets) {
        int captionHeight = this.mDecorView.getCaptionHeight();
        if (captionHeight != 0) {
            this.mLastCaptionHeight = captionHeight;
        }
        if ((this.mLastCaptionHeight != 0 || !this.mDecorView.isShowingCaption()) && this.mLastContentWidth != 0 && this.mLastContentHeight != 0) {
            int left = this.mLastXOffset + newBounds.left;
            int top = this.mLastYOffset + newBounds.top;
            int width = newBounds.width();
            int height = newBounds.height();
            this.mFrameAndBackdropNode.setLeftTopRightBottom(left, top, left + width, top + height);
            RecordingCanvas canvas = this.mFrameAndBackdropNode.beginRecording(width, height);
            Drawable drawable = this.mUserCaptionBackgroundDrawable;
            if (drawable == null) {
                drawable = this.mCaptionBackgroundDrawable;
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, left + width, this.mLastCaptionHeight + top);
                drawable.draw(canvas);
            }
            Drawable drawable2 = this.mResizingBackgroundDrawable;
            if (drawable2 != null) {
                drawable2.setBounds(0, this.mLastCaptionHeight, left + width, top + height);
                this.mResizingBackgroundDrawable.draw(canvas);
            }
            this.mFrameAndBackdropNode.endRecording();
            drawColorViews(left, top, width, height, fullscreen, systemInsets, stableInsets);
            this.mRenderer.drawRenderNode(this.mFrameAndBackdropNode);
            reportDrawIfNeeded();
        }
    }

    private void drawColorViews(int left, int top, int width, int height, boolean fullscreen, Rect systemInsets, Rect stableInsets) {
        RenderNode renderNode = this.mSystemBarBackgroundNode;
        if (renderNode != null) {
            RecordingCanvas canvas = renderNode.beginRecording(width, height);
            this.mSystemBarBackgroundNode.setLeftTopRightBottom(left, top, left + width, top + height);
            int topInset = DecorView.getColorViewTopInset(this.mStableInsets.top, this.mSystemInsets.top);
            ColorDrawable colorDrawable = this.mStatusBarColor;
            if (colorDrawable != null) {
                colorDrawable.setBounds(0, 0, left + width, topInset);
                this.mStatusBarColor.draw(canvas);
            }
            if (this.mNavigationBarColor != null && fullscreen) {
                DecorView.getNavigationBarRect(width, height, stableInsets, systemInsets, this.mTmpRect, 1.0f);
                this.mNavigationBarColor.setBounds(this.mTmpRect);
                this.mNavigationBarColor.draw(canvas);
            }
            this.mSystemBarBackgroundNode.endRecording();
            this.mRenderer.drawRenderNode(this.mSystemBarBackgroundNode);
        }
    }

    private void reportDrawIfNeeded() {
        if (this.mReportNextDraw) {
            if (this.mDecorView.isAttachedToWindow()) {
                this.mDecorView.getViewRootImpl().reportDrawFinish();
            }
            this.mReportNextDraw = false;
        }
    }

    private void pingRenderLocked(boolean drawImmediate) {
        Choreographer choreographer = this.mChoreographer;
        if (choreographer == null || drawImmediate) {
            doFrameUncheckedLocked();
        } else {
            choreographer.postFrameCallback(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setUserCaptionBackgroundDrawable(Drawable userCaptionBackgroundDrawable) {
        synchronized (this) {
            this.mUserCaptionBackgroundDrawable = userCaptionBackgroundDrawable;
        }
    }
}
