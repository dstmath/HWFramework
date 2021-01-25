package com.android.internal.app;

import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.slice.Slice;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.internal.R;
import org.json.JSONObject;

public class PlatLogoActivity extends Activity {
    static final String TOUCH_STATS = "touch.stats";
    static final Paint sPaint = new Paint();
    BackslashDrawable mBackslash;
    int mClicks;
    ImageView mOneView;
    double mPressureMax = -1.0d;
    double mPressureMin = 0.0d;
    ImageView mZeroView;

    static {
        sPaint.setStyle(Paint.Style.STROKE);
        sPaint.setStrokeWidth(4.0f);
        sPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onPause() {
        BackslashDrawable backslashDrawable = this.mBackslash;
        if (backslashDrawable != null) {
            backslashDrawable.stopAnimating();
        }
        this.mClicks = 0;
        super.onPause();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float dp = getResources().getDisplayMetrics().density;
        getWindow().getDecorView().setSystemUiVisibility(768);
        getWindow().setNavigationBarColor(0);
        getWindow().setStatusBarColor(0);
        getActionBar().hide();
        setContentView(R.layout.platlogo_layout);
        this.mBackslash = new BackslashDrawable((int) (50.0f * dp));
        this.mOneView = (ImageView) findViewById(R.id.one);
        this.mOneView.setImageDrawable(new OneDrawable());
        this.mZeroView = (ImageView) findViewById(R.id.zero);
        this.mZeroView.setImageDrawable(new ZeroDrawable());
        ViewGroup root = (ViewGroup) this.mOneView.getParent();
        root.setClipChildren(false);
        root.setBackground(this.mBackslash);
        root.getBackground().setAlpha(32);
        View.OnTouchListener tl = new View.OnTouchListener() {
            /* class com.android.internal.app.PlatLogoActivity.AnonymousClass1 */
            long mClickTime;
            float mOffsetX;
            float mOffsetY;
            ObjectAnimator mRotAnim;

            /* JADX WARNING: Code restructure failed: missing block: B:5:0x0012, code lost:
                if (r0 != 3) goto L_0x00b5;
             */
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                PlatLogoActivity.this.measureTouchPressure(event);
                int actionMasked = event.getActionMasked();
                if (actionMasked != 0) {
                    if (actionMasked == 1) {
                        v.performClick();
                    } else if (actionMasked == 2) {
                        v.setX(event.getRawX() - this.mOffsetX);
                        v.setY(event.getRawY() - this.mOffsetY);
                        v.performHapticFeedback(9);
                    }
                    v.animate().scaleX(1.0f).scaleY(1.0f);
                    ObjectAnimator objectAnimator = this.mRotAnim;
                    if (objectAnimator != null) {
                        objectAnimator.cancel();
                    }
                    PlatLogoActivity.this.testOverlap();
                } else {
                    v.animate().scaleX(1.1f).scaleY(1.1f);
                    v.getParent().bringChildToFront(v);
                    this.mOffsetX = event.getRawX() - v.getX();
                    this.mOffsetY = event.getRawY() - v.getY();
                    long now = System.currentTimeMillis();
                    if (now - this.mClickTime < 350) {
                        this.mRotAnim = ObjectAnimator.ofFloat(v, View.ROTATION, v.getRotation(), v.getRotation() + 3600.0f);
                        this.mRotAnim.setDuration(JobInfo.MIN_BACKOFF_MILLIS);
                        this.mRotAnim.start();
                        this.mClickTime = 0;
                    } else {
                        this.mClickTime = now;
                    }
                }
                return true;
            }
        };
        findViewById(R.id.one).setOnTouchListener(tl);
        findViewById(R.id.zero).setOnTouchListener(tl);
        findViewById(R.id.text).setOnTouchListener(tl);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void testOverlap() {
        float width = (float) this.mZeroView.getWidth();
        if (Math.hypot((double) ((this.mZeroView.getX() + (width * 0.2f)) - this.mOneView.getX()), (double) ((this.mZeroView.getY() + (width * 0.3f)) - this.mOneView.getY())) >= ((double) (width * 0.2f)) || Math.abs((this.mOneView.getRotation() % 360.0f) - 315.0f) >= 15.0f) {
            this.mBackslash.stopAnimating();
            return;
        }
        this.mOneView.animate().x(this.mZeroView.getX() + (0.2f * width));
        this.mOneView.animate().y(this.mZeroView.getY() + (0.3f * width));
        ImageView imageView = this.mOneView;
        imageView.setRotation(imageView.getRotation() % 360.0f);
        this.mOneView.animate().rotation(315.0f);
        this.mOneView.performHapticFeedback(16);
        this.mBackslash.startAnimating();
        this.mClicks++;
        if (this.mClicks >= 7) {
            launchNextStage();
        }
    }

    private void launchNextStage() {
        ContentResolver cr = getContentResolver();
        if (Settings.System.getLong(cr, Settings.System.EGG_MODE, 0) == 0) {
            try {
                Settings.System.putLong(cr, Settings.System.EGG_MODE, System.currentTimeMillis());
            } catch (RuntimeException e) {
                Log.e("com.android.internal.app.PlatLogoActivity", "Can't write settings", e);
            }
        }
        try {
            startActivity(new Intent(Intent.ACTION_MAIN).setFlags(268468224).addCategory("com.android.internal.category.PLATLOGO"));
        } catch (ActivityNotFoundException e2) {
            Log.e("com.android.internal.app.PlatLogoActivity", "No more eggs.");
        }
        finish();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void measureTouchPressure(MotionEvent event) {
        float pressure = event.getPressure();
        int actionMasked = event.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked == 2) {
                if (((double) pressure) < this.mPressureMin) {
                    this.mPressureMin = (double) pressure;
                }
                if (((double) pressure) > this.mPressureMax) {
                    this.mPressureMax = (double) pressure;
                }
            }
        } else if (this.mPressureMax < 0.0d) {
            double d = (double) pressure;
            this.mPressureMax = d;
            this.mPressureMin = d;
        }
    }

    private void syncTouchPressure() {
        try {
            String touchDataJson = Settings.System.getString(getContentResolver(), TOUCH_STATS);
            JSONObject touchData = new JSONObject(touchDataJson != null ? touchDataJson : "{}");
            if (touchData.has("min")) {
                this.mPressureMin = Math.min(this.mPressureMin, touchData.getDouble("min"));
            }
            if (touchData.has(Slice.SUBTYPE_MAX)) {
                this.mPressureMax = Math.max(this.mPressureMax, touchData.getDouble(Slice.SUBTYPE_MAX));
            }
            if (this.mPressureMax >= 0.0d) {
                touchData.put("min", this.mPressureMin);
                touchData.put(Slice.SUBTYPE_MAX, this.mPressureMax);
                Settings.System.putString(getContentResolver(), TOUCH_STATS, touchData.toString());
            }
        } catch (Exception e) {
            Log.e("com.android.internal.app.PlatLogoActivity", "Can't write touch settings", e);
        }
    }

    @Override // android.app.Activity
    public void onStart() {
        super.onStart();
        syncTouchPressure();
    }

    @Override // android.app.Activity
    public void onStop() {
        syncTouchPressure();
        super.onStop();
    }

    static class ZeroDrawable extends Drawable {
        int mTintColor;

        ZeroDrawable() {
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            PlatLogoActivity.sPaint.setColor(this.mTintColor | -16777216);
            canvas.save();
            canvas.scale(((float) canvas.getWidth()) / 24.0f, ((float) canvas.getHeight()) / 24.0f);
            canvas.drawCircle(12.0f, 12.0f, 10.0f, PlatLogoActivity.sPaint);
            canvas.restore();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList tint) {
            this.mTintColor = tint.getDefaultColor();
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }
    }

    static class OneDrawable extends Drawable {
        int mTintColor;

        OneDrawable() {
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            PlatLogoActivity.sPaint.setColor(this.mTintColor | -16777216);
            canvas.save();
            canvas.scale(((float) canvas.getWidth()) / 24.0f, ((float) canvas.getHeight()) / 24.0f);
            Path p = new Path();
            p.moveTo(12.0f, 21.83f);
            p.rLineTo(0.0f, -19.67f);
            p.rLineTo(-5.0f, 0.0f);
            canvas.drawPath(p, PlatLogoActivity.sPaint);
            canvas.restore();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList tint) {
            this.mTintColor = tint.getDefaultColor();
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }
    }

    /* access modifiers changed from: private */
    public static class BackslashDrawable extends Drawable implements TimeAnimator.TimeListener {
        TimeAnimator mAnimator = new TimeAnimator();
        Matrix mMatrix = new Matrix();
        Paint mPaint = new Paint();
        BitmapShader mShader;
        Bitmap mTile;

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawPaint(this.mPaint);
        }

        BackslashDrawable(int width) {
            this.mTile = Bitmap.createBitmap(width, width, Bitmap.Config.ALPHA_8);
            this.mAnimator.setTimeListener(this);
            Canvas tileCanvas = new Canvas(this.mTile);
            float w = (float) tileCanvas.getWidth();
            float h = (float) tileCanvas.getHeight();
            Path path = new Path();
            path.moveTo(0.0f, 0.0f);
            path.lineTo(w / 2.0f, 0.0f);
            path.lineTo(w, h / 2.0f);
            path.lineTo(w, h);
            path.close();
            path.moveTo(0.0f, h / 2.0f);
            path.lineTo(w / 2.0f, h);
            path.lineTo(0.0f, h);
            path.close();
            Paint slashPaint = new Paint();
            slashPaint.setAntiAlias(true);
            slashPaint.setStyle(Paint.Style.FILL);
            slashPaint.setColor(-16777216);
            tileCanvas.drawPath(path, slashPaint);
            this.mShader = new BitmapShader(this.mTile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            this.mPaint.setShader(this.mShader);
        }

        public void startAnimating() {
            if (!this.mAnimator.isStarted()) {
                this.mAnimator.start();
            }
        }

        public void stopAnimating() {
            if (this.mAnimator.isStarted()) {
                this.mAnimator.cancel();
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
            this.mPaint.setAlpha(alpha);
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.mPaint.setColorFilter(colorFilter);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.animation.TimeAnimator.TimeListener
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            if (this.mShader != null) {
                this.mMatrix.postTranslate(((float) deltaTime) / 4.0f, 0.0f);
                this.mShader.setLocalMatrix(this.mMatrix);
                invalidateSelf();
            }
        }
    }
}
