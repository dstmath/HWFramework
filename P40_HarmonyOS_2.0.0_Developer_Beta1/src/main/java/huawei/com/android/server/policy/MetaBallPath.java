package huawei.com.android.server.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import java.util.ArrayList;
import java.util.List;

public class MetaBallPath {
    public static final long ANIM_DELAY = 350;
    public static final long ANIM_DURATION = 900;
    private static final int BALL_MULTI = 2;
    private static final float CIRCLE_LINE_WIDTH_SCALE_MAX = 2.0f;
    private static final int CIRCLE_PAINT_STROKE_WIDTH = 4;
    private static final float CONTROL_X1 = 0.3f;
    private static final float CONTROL_X2 = 0.1f;
    private static final float CONTROL_Y1 = 0.15f;
    private static final float CONTROL_Y2 = 0.85f;
    private static final float DELTA_END_RATIO = 0.8f;
    private static final int INVALID_DEFAULT = -1;
    private static final int META_BALL_RANGE = 3;
    private static final int MSG_REPEAT_ANIMATION = 101;
    private static final int MSG_REPEAT_DELAY = 200;
    private static final float POINT_DST_GAP = 2.0f;
    public static final int POINT_NUM = 4;
    private static final int RADIUS_POSITION_0 = 0;
    private static final int RADIUS_POSITION_1 = 1;
    private static final int RADIUS_POSITION_2 = 2;
    private static final int RADIUS_POSITION_3 = 3;
    private static final int RADIUS_SIZE = 4;
    private static final float RATIO_X = 0.4f;
    private static final float RATIO_Y_DEF = 0.5f;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final String TAG = "MetaBallPath";
    AnimatorSet mAnimatorSet;
    Paint mBallPaint = new Paint();
    Ball[] mBalls = new Ball[4];
    Callback mCallback;
    PointF mCenterBall = new PointF();
    PointF mCenterCircle = new PointF();
    float mCircleLineWidthScale = 1.0f;
    Paint mCirclePaint = new Paint();
    Context mContext;
    private Handler mHandler = new BallHandler();
    Interpolator mInterpolatorMove = new PathInterpolator(CONTROL_X1, CONTROL_Y1, CONTROL_X2, 0.85f);
    private boolean mIsStopManual = false;
    float mMetaLimit;
    float mRadiusBall;
    float mRadiusCircle;
    private Runnable mUpdateRunnable = new UpdateRunnable();

    public interface Callback {
        void onCircleLineWidthChange(float f);

        void onUpdate();
    }

    public MetaBallPath(Context context, Callback callback) {
        if (context == null) {
            Log.w(TAG, "context is null");
            return;
        }
        this.mContext = context;
        this.mCallback = callback;
        int i = 0;
        while (true) {
            Ball[] ballArr = this.mBalls;
            if (i < ballArr.length) {
                ballArr[i] = new Ball(0.0f);
                i++;
            } else {
                this.mCirclePaint.setStyle(Paint.Style.STROKE);
                this.mCirclePaint.setColor(-1);
                this.mCirclePaint.setAntiAlias(true);
                this.mCirclePaint.setStrokeWidth(4.0f);
                this.mBallPaint.setStyle(Paint.Style.FILL);
                this.mBallPaint.setColor(-1);
                this.mBallPaint.setAntiAlias(true);
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPosition(float[] position, float radiusCircle, float radiusBall) {
        float sx = position[0];
        float sy = position[1];
        float ex = position[2];
        float ey = position[3];
        this.mCenterCircle.set(sx, sy);
        this.mCenterBall.set(ex, ey);
        this.mRadiusCircle = radiusCircle;
        this.mRadiusBall = radiusBall;
        this.mMetaLimit = this.mRadiusBall * 3.0f;
        for (Ball ball : this.mBalls) {
            ball.mCy = sy;
        }
        initAnimation();
    }

    public void start() {
        this.mIsStopManual = false;
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null && !animatorSet.isRunning()) {
            this.mAnimatorSet.start();
        }
    }

    public void stop() {
        this.mIsStopManual = true;
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onUpdate();
        }
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(101);
        }
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null) {
            animatorSet.end();
        }
    }

    public void draw(Canvas canvas, boolean isUpdate) {
        if (isUpdate) {
            for (Ball b : this.mBalls) {
                b.draw(canvas);
            }
        }
        drawCircle(canvas);
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(this.mCenterCircle.x, this.mCenterCircle.y, this.mRadiusCircle, this.mCirclePaint);
        canvas.drawCircle(this.mCenterCircle.x, this.mCenterCircle.y, 4.0f, this.mBallPaint);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void circle2Rect(float cx, float cy, float r, RectF rect) {
        rect.set(cx - r, cy - r, cx + r, cy + r);
    }

    private float dp2px(float dp) {
        Context context = this.mContext;
        if (context == null) {
            return -1.0f;
        }
        return context.getResources().getDisplayMetrics().density * dp;
    }

    private void initAnimation() {
        List<Animator> animators = new ArrayList<>(8);
        ValueAnimator[] moveAnimators = new ValueAnimator[4];
        ValueAnimator[] scaleAnimators = new ValueAnimator[4];
        float pointGapIndex = dp2px(2.0f);
        for (int i = 0; i < 4; i++) {
            this.mBalls[i].setMoveRange((this.mCenterCircle.x + this.mRadiusCircle) - this.mRadiusBall, this.mCenterBall.x - (((float) i) * pointGapIndex));
            Ball[] ballArr = this.mBalls;
            moveAnimators[i] = ObjectAnimator.ofFloat(ballArr[i], "Cx", ballArr[i].getFrom(), this.mBalls[i].getTo());
            moveAnimators[i].setDuration(900L);
            moveAnimators[i].setStartDelay(((long) i) * 350);
            moveAnimators[i].setInterpolator(this.mInterpolatorMove);
            moveAnimators[i].addListener(new MoveAnimatorListener(i));
            scaleAnimators[i] = ObjectAnimator.ofFloat(this.mBalls[i], "Radius", this.mRadiusBall, 0.0f);
            scaleAnimators[i].setDuration(900L);
            scaleAnimators[i].setStartDelay(((long) i) * 350);
            scaleAnimators[i].setInterpolator(new AccelerateInterpolator());
            animators.add(moveAnimators[i]);
            animators.add(scaleAnimators[i]);
        }
        this.mAnimatorSet = new AnimatorSet();
        this.mAnimatorSet.addListener(new AnimatorSetListener());
        this.mAnimatorSet.playTogether(animators);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doUpdate() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onUpdate();
        }
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mHandler.postDelayed(this.mUpdateRunnable, 60);
        }
    }

    public void onBallAnimationStart(int index) {
        if (index == 0) {
            this.mCircleLineWidthScale = 2.0f;
        }
        if (index == 3) {
            this.mCircleLineWidthScale = 2.0f - this.mBalls[3].getFraction();
        }
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onCircleLineWidthChange(this.mCircleLineWidthScale);
        }
    }

    private class BallHandler extends Handler {
        private BallHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 101) {
                MetaBallPath.this.start();
            }
        }
    }

    public class Ball {
        boolean isDrawBezier;
        private Path mBallPath = new Path();
        PointF mControl = new PointF();
        float mCx;
        float mCy;
        PointF mEnd = new PointF();
        float mFraction;
        float mFrom;
        float mRadius;
        RectF mRect = new RectF();
        PointF mStart = new PointF();
        float mTo;

        public Ball(float cy) {
            this.mCy = cy;
        }

        public void setCx(float cx) {
            float delta;
            float delta2;
            this.mCx = cx;
            float f = this.mCx;
            float f2 = this.mFrom;
            float dx = f - f2;
            this.mFraction = dx / (this.mTo - f2);
            float fractionMeta = (dx - (MetaBallPath.this.mRadiusBall * 2.0f)) / (MetaBallPath.this.mMetaLimit - (MetaBallPath.this.mRadiusBall * 2.0f));
            this.isDrawBezier = dx < MetaBallPath.this.mMetaLimit;
            if (fractionMeta < 0.0f) {
                fractionMeta = 0.0f;
            }
            if (fractionMeta > 1.0f) {
                fractionMeta = 1.0f;
            }
            float hyPotBall2Circle = (float) Math.hypot((double) (this.mCx - MetaBallPath.this.mCenterCircle.x), (double) (this.mCy - MetaBallPath.this.mCenterCircle.y));
            double deltaEnd = ((double) (dx / MetaBallPath.this.mMetaLimit)) * 2.5132741603225375d;
            this.mEnd.x = this.mCx + ((float) (((double) MetaBallPath.this.mRadiusBall) * Math.cos(deltaEnd)));
            this.mEnd.y = this.mCy - ((float) (((double) MetaBallPath.this.mRadiusBall) * Math.sin(deltaEnd)));
            float deltaStart = (float) Math.asin((double) ((MetaBallPath.this.mRadiusBall * 2.0f) / MetaBallPath.this.mRadiusCircle));
            if (dx < MetaBallPath.this.mRadiusBall * 2.0f) {
                delta = deltaStart;
            } else {
                delta = (1.0f - (fractionMeta * 0.5f)) * deltaStart;
            }
            this.mStart.x = MetaBallPath.this.mCenterCircle.x + ((float) (((double) MetaBallPath.this.mRadiusCircle) * Math.cos((double) delta)));
            this.mStart.y = MetaBallPath.this.mCenterCircle.y - ((float) (((double) MetaBallPath.this.mRadiusCircle) * Math.sin((double) delta)));
            float deltaControl = (float) (Math.asin((double) (MetaBallPath.this.mRadiusBall / (MetaBallPath.this.mRadiusCircle * 2.0f))) * 2.0d);
            float ex = 0.0f;
            float ey = 0.0f;
            if (dx < MetaBallPath.this.mRadiusBall) {
                delta2 = (float) Math.acos((double) ((((hyPotBall2Circle * hyPotBall2Circle) + (MetaBallPath.this.mRadiusCircle * MetaBallPath.this.mRadiusCircle)) - (MetaBallPath.this.mRadiusBall * MetaBallPath.this.mRadiusBall)) / ((hyPotBall2Circle * 2.0f) * MetaBallPath.this.mRadiusCircle)));
            } else if (dx < MetaBallPath.this.mRadiusBall * 2.0f) {
                delta2 = deltaControl;
            } else {
                delta2 = deltaControl * (1.0f - fractionMeta);
                ex = MetaBallPath.this.mRadiusBall * 0.4f * fractionMeta;
                ey = MetaBallPath.this.mRadiusBall * 0.5f * fractionMeta;
            }
            this.mControl.x = MetaBallPath.this.mCenterCircle.x + ((float) (((double) MetaBallPath.this.mRadiusCircle) * Math.cos((double) delta2))) + ex;
            this.mControl.y = (MetaBallPath.this.mCenterCircle.y - ((float) (((double) MetaBallPath.this.mRadiusCircle) * Math.sin((double) delta2)))) + ey;
            this.mBallPath.reset();
            this.mBallPath.moveTo(this.mStart.x, this.mStart.y);
            this.mBallPath.quadTo(this.mControl.x, this.mControl.y, this.mEnd.x, this.mEnd.y);
            MetaBallPath metaBallPath = MetaBallPath.this;
            metaBallPath.circle2Rect(this.mCx, this.mCy, metaBallPath.mRadiusBall, this.mRect);
            this.mBallPath.arcTo(this.mRect, -((float) Math.toDegrees(deltaEnd)), (float) Math.toDegrees(2.0d * deltaEnd), false);
            this.mBallPath.quadTo(this.mControl.x, (MetaBallPath.this.mCenterCircle.y * 2.0f) - this.mControl.y, this.mStart.x, (MetaBallPath.this.mCenterCircle.y * 2.0f) - this.mStart.y);
            MetaBallPath metaBallPath2 = MetaBallPath.this;
            metaBallPath2.circle2Rect(metaBallPath2.mCenterCircle.x, MetaBallPath.this.mCenterCircle.y, MetaBallPath.this.mRadiusCircle, this.mRect);
            this.mBallPath.arcTo(this.mRect, (float) Math.toDegrees((double) deltaStart), -((float) Math.toDegrees((double) (2.0f * deltaStart))), false);
        }

        public void setRadius(float radius) {
            this.mRadius = radius;
        }

        public void setMoveRange(float from, float to) {
            this.mFrom = from;
            this.mTo = to;
        }

        public float getFrom() {
            return this.mFrom;
        }

        public float getTo() {
            return this.mTo;
        }

        public void draw(Canvas canvas) {
            if (this.isDrawBezier) {
                drawBezier(canvas);
                canvas.save();
                canvas.translate(MetaBallPath.this.mCenterCircle.x * 2.0f, 0.0f);
                canvas.scale(-1.0f, 1.0f);
                drawBezier(canvas);
                canvas.restore();
                return;
            }
            canvas.drawCircle(this.mCx, this.mCy, this.mRadius, MetaBallPath.this.mBallPaint);
            canvas.save();
            canvas.translate(MetaBallPath.this.mCenterCircle.x * 2.0f, 0.0f);
            canvas.scale(-1.0f, 1.0f);
            canvas.drawCircle(this.mCx, this.mCy, this.mRadius, MetaBallPath.this.mBallPaint);
            canvas.restore();
        }

        public void drawBezier(Canvas canvas) {
            canvas.drawPath(this.mBallPath, MetaBallPath.this.mBallPaint);
        }

        public float getFraction() {
            return this.mFraction;
        }
    }

    /* access modifiers changed from: private */
    public class MoveAnimatorListener extends AnimatorListenerAdapter {
        private int mIndex;

        MoveAnimatorListener(int index) {
            this.mIndex = index;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            MetaBallPath.this.onBallAnimationStart(this.mIndex);
        }
    }

    /* access modifiers changed from: private */
    public class AnimatorSetListener extends AnimatorListenerAdapter {
        private AnimatorSetListener() {
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            MetaBallPath.this.doUpdate();
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (MetaBallPath.this.mHandler != null) {
                MetaBallPath.this.mHandler.removeMessages(101);
                if (!MetaBallPath.this.mIsStopManual) {
                    MetaBallPath.this.mHandler.sendEmptyMessageDelayed(101, 200);
                }
            }
        }
    }

    public class UpdateRunnable implements Runnable {
        public UpdateRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            MetaBallPath.this.doUpdate();
        }
    }
}
