package huawei.com.android.server.policy;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.server.LocalServices;
import com.android.server.policy.DefaultHwScreenOnProximityLock;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;

public class BallFrameView extends FrameLayout {
    private static final float EVA_WIDTH_SCALE = 0.2f;
    private static final int INITIAL_ANIMATION_WIDTH = 1080;
    private static final float LAND_WIDTH_SCALE = 0.125f;
    private static final float MT_WIDTH_SCALE = 0.3f;
    private static final int SWIPE_EXIT_TIMES = 2;
    private static final String TAG = "BallFrameView";
    private static final int TIME_DELAY = 200;
    private static final float WIDTH_SCALE = 0.25f;
    private int mAnimationWidth;
    private BollView mBooView;
    private int mDownEventId;
    private boolean mIsFirstRightSwipe;
    private Handler mMainHandler;
    private int mOffsetX;
    private HwPhoneWindowManager mPolicy;
    private DefaultHwScreenOnProximityLock mScreenOnProximity;
    private float mStartX;
    private int mSwipeCount;

    public BallFrameView(Context context) {
        this(context, null);
    }

    public BallFrameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BallFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMainHandler = null;
        this.mAnimationWidth = INITIAL_ANIMATION_WIDTH;
        this.mStartX = 0.0f;
        this.mOffsetX = 0;
        this.mSwipeCount = 0;
        this.mBooView = (BollView) LayoutInflater.from(context).inflate(34013232, (ViewGroup) this, true).findViewById(34603031);
        this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * WIDTH_SCALE);
        this.mPolicy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        HwPhoneWindowManager hwPhoneWindowManager = this.mPolicy;
        if (hwPhoneWindowManager != null) {
            this.mScreenOnProximity = hwPhoneWindowManager.getScreenOnProximity();
        }
    }

    public void startTextViewAnimal() {
        BollView bollView = this.mBooView;
        if (bollView != null) {
            bollView.startAnim();
        }
    }

    public void stopTextViewAnimal() {
        BollView bollView = this.mBooView;
        if (bollView != null) {
            bollView.endAnim();
        }
    }

    public void setBallViewVisibal(int visibal) {
        BollView bollView = this.mBooView;
        if (bollView != null) {
            bollView.setVisibility(visibal);
        }
    }

    private boolean isVerticalInArea(int eventY) {
        Rect bound = new Rect();
        getBoundsOnScreen(bound);
        return eventY >= bound.top && eventY <= bound.bottom;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        if (r2 == 6) goto L_0x0045;
     */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getRawX();
        float eventY = event.getRawY();
        BollView bollView = this.mBooView;
        if (bollView != null) {
            bollView.handleTouchEvent(event);
        }
        int action = event.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action != 2) {
                    if (action != 3) {
                    }
                } else if (event.getPointerId(event.getActionIndex()) != this.mDownEventId || !isVerticalInArea((int) eventY)) {
                    return true;
                } else {
                    this.mOffsetX = (int) (eventX - this.mStartX);
                    updateStart(this.mOffsetX);
                }
            }
            if (event.getPointerId(event.getActionIndex()) != this.mDownEventId || !isVerticalInArea((int) eventY)) {
                updateStart(0);
                restoreAnimal();
                return true;
            }
            handleUpEvent(eventX);
        } else {
            this.mDownEventId = event.getPointerId(event.getActionIndex());
            this.mStartX = eventX;
            stopTextViewAnimal();
        }
        return true;
    }

    private void handleUpEvent(float eventX) {
        if (this.mScreenOnProximity != null) {
            this.mOffsetX = (int) (eventX - this.mStartX);
            Math.abs(this.mOffsetX);
            if (Math.abs(this.mOffsetX) >= this.mAnimationWidth) {
                boolean z = true;
                this.mSwipeCount++;
                if (this.mSwipeCount == 2) {
                    if ((this.mOffsetX > 0) != this.mIsFirstRightSwipe) {
                        Log.i(TAG, "swipe on different direction");
                        this.mSwipeCount = 1;
                        updateStart(0);
                        restoreAnimal();
                        return;
                    }
                    this.mScreenOnProximity.swipeExitHintView();
                    this.mBooView.setVisibility(4);
                    this.mSwipeCount = 0;
                    return;
                }
                this.mScreenOnProximity.refreshHintTextView();
                updateStart(0);
                restoreAnimal();
                if (this.mOffsetX <= 0) {
                    z = false;
                }
                this.mIsFirstRightSwipe = z;
                return;
            }
            Log.i(TAG, "the distance of moving is too short");
            updateStart(0);
            restoreAnimal();
        }
    }

    public void restoreAnimal() {
        postDelayed(new Runnable() {
            /* class huawei.com.android.server.policy.$$Lambda$gc7a005ItyQwwp8psU7BMiKVLSo */

            @Override // java.lang.Runnable
            public final void run() {
                BallFrameView.this.startTextViewAnimal();
            }
        }, 200);
    }

    public void updateStart(int x) {
        BollView bollView = this.mBooView;
        if (bollView != null) {
            bollView.setTranslationX((float) x);
        }
    }

    public void updateAnimCount(int count) {
        this.mSwipeCount = count;
    }

    public void setCoverViewWidth(int animationWidth) {
        this.mAnimationWidth = animationWidth;
        this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * WIDTH_SCALE);
    }
}
