package com.android.internal.app;

import android.animation.TimeAnimator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.android.internal.colorextraction.types.Tonal;

public class PlatLogoActivity extends Activity {
    TimeAnimator anim;
    PBackground bg;
    FrameLayout layout;

    private class PBackground extends Drawable {
        private int darkest;
        private float dp;
        private float maxRadius;
        private float offset;
        private int[] palette;
        private float radius;
        private float x;
        private float y;

        public PBackground() {
            randomizePalette();
        }

        public void setRadius(float r) {
            this.radius = Math.max(48.0f * this.dp, r);
        }

        public void setPosition(float x2, float y2) {
            this.x = x2;
            this.y = y2;
        }

        public void setOffset(float o) {
            this.offset = o;
        }

        public float lum(int rgb) {
            return (((((float) Color.red(rgb)) * 299.0f) + (((float) Color.green(rgb)) * 587.0f)) + (((float) Color.blue(rgb)) * 114.0f)) / 1000.0f;
        }

        public void randomizePalette() {
            int slots = ((int) (Math.random() * 2.0d)) + 2;
            float[] color = {((float) Math.random()) * 360.0f, 1.0f, 1.0f};
            this.palette = new int[slots];
            this.darkest = 0;
            for (int i = 0; i < slots; i++) {
                this.palette[i] = Color.HSVToColor(color);
                color[0] = color[0] + (360.0f / ((float) slots));
                if (lum(this.palette[i]) < lum(this.palette[this.darkest])) {
                    this.darkest = i;
                }
            }
            StringBuilder str = new StringBuilder();
            for (int c : this.palette) {
                str.append(String.format("#%08x ", new Object[]{Integer.valueOf(c)}));
            }
            Log.v("PlatLogoActivity", "color palette: " + str);
        }

        public void draw(Canvas canvas) {
            Canvas canvas2 = canvas;
            if (this.dp == 0.0f) {
                this.dp = PlatLogoActivity.this.getResources().getDisplayMetrics().density;
            }
            float width = (float) canvas.getWidth();
            float height = (float) canvas.getHeight();
            float f = 2.0f;
            if (this.radius == 0.0f) {
                setPosition(width / 2.0f, height / 2.0f);
                setRadius(width / 6.0f);
            }
            float inner_w = this.radius * 0.667f;
            Paint paint = new Paint();
            paint.setStrokeCap(Paint.Cap.BUTT);
            canvas2.translate(this.x, this.y);
            Path p = new Path();
            p.moveTo(-this.radius, height);
            p.lineTo(-this.radius, 0.0f);
            Path path = p;
            path.arcTo(-this.radius, -this.radius, this.radius, this.radius, -180.0f, 270.0f, false);
            p.lineTo(-this.radius, this.radius);
            paint.setStyle(Paint.Style.FILL);
            int i = 0;
            float w = ((float) Math.max(canvas.getWidth(), canvas.getHeight())) * 1.414f;
            while (true) {
                int i2 = i;
                if (w > (this.radius * f) + (inner_w * f)) {
                    paint.setColor(this.palette[i2 % this.palette.length] | Tonal.MAIN_COLOR_DARK);
                    float f2 = (-w) / f;
                    float f3 = (-w) / f;
                    float f4 = w / f;
                    float f5 = w / f;
                    canvas2.drawOval(f2, f3, f4, f5, paint);
                    w = (float) (((double) w) - (((double) inner_w) * (1.100000023841858d + Math.sin((double) (((((float) i2) / 20.0f) + this.offset) * 3.14159f)))));
                    i = i2 + 1;
                    p = p;
                    f = 2.0f;
                } else {
                    Path p2 = p;
                    paint.setColor(this.palette[(this.darkest + 1) % this.palette.length] | Tonal.MAIN_COLOR_DARK);
                    canvas2.drawOval(-this.radius, -this.radius, this.radius, this.radius, paint);
                    p2.reset();
                    p2.moveTo(-this.radius, height);
                    p2.lineTo(-this.radius, 0.0f);
                    int i3 = i2;
                    float f6 = w;
                    p2.arcTo(-this.radius, -this.radius, this.radius, this.radius, -180.0f, 270.0f, false);
                    p2.lineTo((-this.radius) + inner_w, this.radius);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2.0f * inner_w);
                    paint.setColor(this.palette[this.darkest]);
                    canvas2.drawPath(p2, paint);
                    paint.setStrokeWidth(inner_w);
                    paint.setColor(-1);
                    canvas2.drawPath(p2, paint);
                    return;
                }
            }
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public int getOpacity() {
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.layout = new FrameLayout(this);
        setContentView(this.layout);
        this.bg = new PBackground();
        this.layout.setBackground(this.bg);
        this.layout.setOnTouchListener(new View.OnTouchListener() {
            final MotionEvent.PointerCoords pc0 = new MotionEvent.PointerCoords();
            final MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();

            public boolean onTouch(View v, MotionEvent event) {
                int actionMasked = event.getActionMasked();
                if ((actionMasked == 0 || actionMasked == 2) && event.getPointerCount() > 1) {
                    event.getPointerCoords(0, this.pc0);
                    event.getPointerCoords(1, this.pc1);
                    PlatLogoActivity.this.bg.setRadius(((float) Math.hypot((double) (this.pc0.x - this.pc1.x), (double) (this.pc0.y - this.pc1.y))) / 2.0f);
                }
                return true;
            }
        });
    }

    public void onStart() {
        super.onStart();
        this.bg.randomizePalette();
        this.anim = new TimeAnimator();
        this.anim.setTimeListener(new TimeAnimator.TimeListener() {
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
                PlatLogoActivity.this.bg.setOffset(((float) totalTime) / 60000.0f);
                PlatLogoActivity.this.bg.invalidateSelf();
            }
        });
        this.anim.start();
    }

    public void onStop() {
        if (this.anim != null) {
            this.anim.cancel();
            this.anim = null;
        }
        super.onStop();
    }
}
