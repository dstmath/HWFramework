package huawei.com.android.server.policy;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import huawei.com.android.server.policy.MetaBallPath;

public class CallPanelView extends View implements MetaBallPath.Callback {
    private static final int BALL_DST = 39;
    private static final float BALL_LEFT_OFFSET = 0.3f;
    private static final int BALL_RADIUS = 4;
    private static final int CIRCLE_RADIUS = 60;
    private static final float RATIO = 0.5f;
    private static final int STROKE_WIDTH = 3;
    private float mBallDst;
    private float mBallRadiusDp;
    private float mCircleRadiusDp;
    private boolean mIsUpdate;
    MetaBallPath mMetaBall;

    public CallPanelView(Context context) {
        this(context, null);
    }

    public CallPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMetaBall = new MetaBallPath(getContext(), this);
        this.mBallRadiusDp = 4.0f;
        this.mCircleRadiusDp = 60.0f;
        this.mBallDst = 39.0f;
        this.mIsUpdate = false;
        float density = getResources().getDisplayMetrics().density;
        this.mBallRadiusDp *= density;
        this.mCircleRadiusDp *= density;
        this.mBallDst *= density;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        this.mCircleRadiusDp = (((float) h) * 0.5f) - 3.0f;
        MetaBallPath metaBallPath = this.mMetaBall;
        float f = this.mCircleRadiusDp;
        metaBallPath.setPosition(new float[]{((float) w) * 0.5f, ((float) h) * 0.5f, (((float) w) * 0.5f) + f + this.mBallDst, ((float) h) * 0.5f}, f, this.mBallRadiusDp);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.mMetaBall.draw(canvas, this.mIsUpdate);
    }

    public void startAnim() {
        this.mIsUpdate = true;
        this.mMetaBall.start();
    }

    public void endAnim() {
        this.mIsUpdate = false;
        this.mMetaBall.stop();
    }

    @Override // huawei.com.android.server.policy.MetaBallPath.Callback
    public void onUpdate() {
        invalidate((int) (((float) getRight()) * BALL_LEFT_OFFSET), 0, (int) (((float) getRight()) * 0.7f), getBottom());
    }

    @Override // huawei.com.android.server.policy.MetaBallPath.Callback
    public void onCircleLineWidthChange(float scale) {
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean isHasWindowFocus) {
        super.onWindowFocusChanged(isHasWindowFocus);
        if (isHasWindowFocus) {
            startAnim();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        endAnim();
    }
}
