package com.android.server.multiwin.view;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.android.server.multiwin.HwMultiWinUtils;
import com.android.server.multiwin.animation.HwMultiWinSplitBarController;
import com.android.server.multiwin.animation.SplitScaleAnimation;
import com.android.server.multiwin.animation.interpolator.SharpCurveInterpolator;
import com.android.server.multiwin.listener.DragAnimationListener;
import com.android.server.multiwin.listener.HwMultiWinHotAreaConfigListener;

public class HwMultiWinHotAreaView extends HwMultiWinClipImageView {
    public static final int BOTTOM_SPLIT = 4;
    private static final String DARK_COLOR = "#FF1A1A1A";
    public static final int FREE_FORM = 5;
    public static final int LEFT_SPLIT = 1;
    protected static final int LOC_NUM = 2;
    public static final int NONE_SPLIT = 0;
    public static final int RIGHT_SPLIT = 2;
    private static final String TAG = "HwMultiWinHotAreaView";
    public static final int TOP_SPLIT = 3;
    protected static final String WHITE_COLOR = "#FFFFFFFF";
    private static final long WHITE_COVER_SHOW_DURATION = 250;
    DragAnimationListener mDragAnimationListener;
    private int mDragSplitMode = 0;
    protected ViewGroup mHotAreaLayout;
    protected HwMultiWinHotAreaConfigListener mHwMultiWinHotAreaConfigListener;
    private int mInitialDragSplitMode = 0;
    private boolean mIsAsHotArea;
    protected boolean mIsDropOnThisView;
    private boolean mIsLandScape = false;
    protected Rect mNavBarBound;
    protected int mNavBarPos = -1;
    protected int mNotchPos = -1;
    protected int mOriginalNotchStatus = 0;
    protected HwMultiWinSplitBarController mSplitBarController;
    protected int mSplitMode = 0;
    SplitScaleAnimation mSplitScaleAnimation;
    private Drawable mWhiteCover;
    private float mWhiteCoverAlpha;
    private ValueAnimator mWhiteCoverAnimator;
    private Path mWhiteCoverPath;

    public HwMultiWinHotAreaView(Context context) {
        super(context);
    }

    public HwMultiWinHotAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwMultiWinHotAreaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDragAnimationListener(DragAnimationListener listener) {
        this.mDragAnimationListener = listener;
    }

    public void setHotAreaLayout(ViewGroup hotAreaLayout) {
        this.mHotAreaLayout = hotAreaLayout;
    }

    public void setSplitBarController(HwMultiWinSplitBarController splitBarController) {
        this.mSplitBarController = splitBarController;
    }

    public void asHotArea(boolean isAsHotArea) {
        this.mIsAsHotArea = isAsHotArea;
        if (isAsHotArea) {
            this.mWhiteCover = new ColorDrawable(Color.parseColor(HwMultiWinUtils.isInNightMode(getContext()) ? DARK_COLOR : WHITE_COLOR));
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                /* class com.android.server.multiwin.view.HwMultiWinHotAreaView.AnonymousClass1 */

                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    Rect whiteCoverBound = new Rect(0, 0, HwMultiWinHotAreaView.this.getWidth(), HwMultiWinHotAreaView.this.getHeight());
                    HwMultiWinHotAreaView.this.resizeWhiteCoverBound(whiteCoverBound);
                    Log.d(HwMultiWinHotAreaView.TAG, "resizeWhiteCoverBound: whiteCoverBound = " + whiteCoverBound);
                    HwMultiWinHotAreaView.this.mWhiteCover.setBounds(whiteCoverBound);
                    HwMultiWinHotAreaView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    HwMultiWinHotAreaView.this.mWhiteCoverPath = new Path();
                    HwMultiWinHotAreaView.this.mWhiteCoverPath.addRoundRect((float) whiteCoverBound.left, (float) whiteCoverBound.top, (float) whiteCoverBound.right, (float) whiteCoverBound.bottom, HwMultiWinHotAreaView.this.mRoundCornerRadius, HwMultiWinHotAreaView.this.mRoundCornerRadius, Path.Direction.CW);
                }
            });
        }
    }

    @Override // android.view.View
    public boolean onDragEvent(DragEvent dragEvent) {
        int animType;
        int animType2;
        switch (dragEvent.getAction()) {
            case 1:
                handleDragStarted(dragEvent);
                return true;
            case 2:
                handleDragLocation(dragEvent);
                return true;
            case 3:
                if (this.mSplitMode == 5) {
                    animType = 4;
                } else {
                    animType = 6;
                }
                handleDrop(dragEvent, animType);
                return true;
            case 4:
                boolean isHandle = dragEvent.getResult();
                handleDragEnded(dragEvent);
                return isHandle;
            case 5:
                if (this.mSplitMode == 5) {
                    animType2 = 3;
                } else {
                    animType2 = 5;
                }
                handleDragEntered(dragEvent, animType2);
                return true;
            case 6:
                handleDragExited(dragEvent);
                return true;
            default:
                return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resizeWhiteCoverBound(Rect bound) {
        int i = this.mNavBarPos;
        if (i == 4) {
            if (isLandScape() || this.mSplitMode == 4) {
                bound.set(0, 0, bound.width(), bound.height() - this.mNavBarBound.height());
            }
        } else if (i == 1) {
            if (!isLandScape() || this.mSplitMode == 1) {
                bound.set(this.mNavBarBound.width(), 0, bound.width(), bound.height());
            }
        } else if (i != 2) {
        } else {
            if (!isLandScape() || this.mSplitMode == 2) {
                bound.set(0, 0, bound.width() - this.mNavBarBound.width(), bound.height());
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.multiwin.view.HwMultiWinClipImageView, android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mWhiteCover != null) {
            canvas.save();
            canvas.clipPath(this.mWhiteCoverPath);
            this.mWhiteCover.setAlpha((int) (this.mWhiteCoverAlpha * 255.0f));
            this.mWhiteCover.draw(canvas);
            canvas.restore();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleDragStarted(DragEvent dragEvent) {
        DragAnimationListener dragAnimationListener = this.mDragAnimationListener;
        if (dragAnimationListener != null) {
            dragAnimationListener.onDragStarted();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleDragEntered(DragEvent dragEvent, int dragSurfaceAnimType) {
        SplitScaleAnimation splitScaleAnimation = this.mSplitScaleAnimation;
        if (splitScaleAnimation != null) {
            splitScaleAnimation.playScaleDownAnmation();
        }
        DragAnimationListener dragAnimationListener = this.mDragAnimationListener;
        if (dragAnimationListener != null) {
            dragAnimationListener.onDragEntered(this, dragEvent, this.mSplitMode, dragSurfaceAnimType);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleDragLocation(DragEvent dragEvent) {
        DragAnimationListener dragAnimationListener = this.mDragAnimationListener;
        if (dragAnimationListener != null) {
            dragAnimationListener.onDragLocation();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleDragExited(DragEvent dragEvent) {
        SplitScaleAnimation splitScaleAnimation = this.mSplitScaleAnimation;
        if (splitScaleAnimation != null) {
            splitScaleAnimation.playScaleUpAnimation();
        }
        DragAnimationListener dragAnimationListener = this.mDragAnimationListener;
        if (dragAnimationListener != null) {
            dragAnimationListener.onDragExited(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleDrop(DragEvent dragEvent, int dragSurfaceAnimType) {
        Log.d(TAG, "handleDrop dragSurfaceAnimTypev = " + dragSurfaceAnimType);
        this.mIsDropOnThisView = true;
        SplitScaleAnimation splitScaleAnimation = this.mSplitScaleAnimation;
        if (splitScaleAnimation != null) {
            splitScaleAnimation.playScaleUpAnimation();
        }
        if (this.mDragAnimationListener != null) {
            Log.d(TAG, "mDragAnimationListener mSplitMode = " + this.mSplitMode);
            this.mDragAnimationListener.onDrop(this, dragEvent, this.mSplitMode, getDropBound(), dragSurfaceAnimType);
        }
        if (this.mIsAsHotArea) {
            playWhiteCoverAnimation();
        }
    }

    public void setNavBarInfo(Rect navBarBound, int navBarPos) {
        this.mNavBarBound = navBarBound;
        this.mNavBarPos = navBarPos;
    }

    @SuppressLint({"NewApi"})
    private void playWhiteCoverAnimation() {
        ValueAnimator valueAnimator = this.mWhiteCoverAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mWhiteCoverAnimator.cancel();
        }
        this.mWhiteCoverAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("whiteCoverAlpha", 0.0f, 1.0f));
        this.mWhiteCoverAnimator.setDuration(WHITE_COVER_SHOW_DURATION);
        this.mWhiteCoverAnimator.setInterpolator(new SharpCurveInterpolator());
        this.mWhiteCoverAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.view.HwMultiWinHotAreaView.AnonymousClass2 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                Object whiteCoverAlphaObj = animation.getAnimatedValue("whiteCoverAlpha");
                if (whiteCoverAlphaObj instanceof Float) {
                    HwMultiWinHotAreaView.this.mWhiteCoverAlpha = ((Float) whiteCoverAlphaObj).floatValue();
                }
                HwMultiWinHotAreaView.this.invalidate();
            }
        });
        this.mWhiteCoverAnimator.start();
    }

    /* access modifiers changed from: package-private */
    public Rect getDropBound() {
        return new Rect(getLeft(), getTop(), getRight(), getBottom());
    }

    /* access modifiers changed from: package-private */
    public void handleDragEnded(DragEvent dragEvent) {
        DragAnimationListener dragAnimationListener = this.mDragAnimationListener;
        if (dragAnimationListener != null) {
            dragAnimationListener.onDragEnded(this.mIsDropOnThisView);
        }
    }

    public int getSplitMode() {
        return this.mSplitMode;
    }

    public void setSplitMode(int splitMode) {
        this.mSplitMode = splitMode;
    }

    public void initSplitScaleAnimation() {
        int i = this.mSplitMode;
        if (i == 1 || i == 2 || i == 3 || i == 4) {
            this.mSplitScaleAnimation = new SplitScaleAnimation(this, this.mSplitMode);
        }
    }

    @Override // com.android.server.multiwin.view.HwMultiWinClipImageView
    public void setIsLandScape(boolean isLandScape) {
        this.mIsLandScape = isLandScape;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.multiwin.view.HwMultiWinClipImageView
    public boolean isLandScape() {
        return this.mIsLandScape;
    }

    public void setDragSplitMode(int dragSplitMode) {
        this.mDragSplitMode = dragSplitMode;
    }

    public int getDragSplitMode() {
        return this.mDragSplitMode;
    }

    public int getInitialDragSplitMode() {
        return this.mInitialDragSplitMode;
    }

    public void setInitialDragSplitMode(int initialDragSplitMode) {
        this.mInitialDragSplitMode = initialDragSplitMode;
    }

    public void setHwMultiWinHotAreaConfigListener(HwMultiWinHotAreaConfigListener listener) {
        this.mHwMultiWinHotAreaConfigListener = listener;
    }

    public void setOriginalNotchStatus(int originalNotchStatus) {
        this.mOriginalNotchStatus = originalNotchStatus;
    }

    public void setNotchPos(int notchPos) {
        this.mNotchPos = notchPos;
    }
}
