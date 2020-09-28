package huawei.android.widget.appbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SmoothScaleLinearLayout extends LinearLayout {
    private Bitmap mCacheBmp;
    private float mOriginBmpSx = 1.0f;
    private float mOriginBmpSy = 1.0f;
    private Paint mPaint = new Paint(3);

    public SmoothScaleLinearLayout(Context context) {
        super(context);
    }

    public SmoothScaleLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmoothScaleLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSmoothScale(boolean isEnabled) {
        if (!isEnabled) {
            this.mCacheBmp = null;
        } else if (this.mCacheBmp == null) {
            int width = getWidth();
            int height = getHeight();
            if (width > 0 && height > 0) {
                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);
                this.mOriginBmpSx = getScaleX();
                this.mOriginBmpSy = getScaleY();
                canvas.scale(this.mOriginBmpSx, this.mOriginBmpSy);
                draw(canvas);
                this.mCacheBmp = bmp;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        if (canvas != null) {
            if (this.mCacheBmp == null) {
                super.dispatchDraw(canvas);
                return;
            }
            canvas.scale(1.0f / this.mOriginBmpSx, 1.0f / this.mOriginBmpSy);
            canvas.drawBitmap(this.mCacheBmp, 0.0f, 0.0f, this.mPaint);
        }
    }
}
