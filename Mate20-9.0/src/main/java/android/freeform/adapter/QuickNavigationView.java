package android.freeform.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;

public class QuickNavigationView extends View {
    private static final int BLUE_CIRCLE_RADIUS = 3;
    private static final int GRAY_CIRCLE_RADIUS = 2;
    private static final String GRAY_COLRO = "#26000000";
    private static final String HONOR_BLUE_COLOR = "#00B5E2";
    private static final String HW_BLUE_COLOR = "#007dff";
    private static final boolean IS_HONOR_PRODUCT = "HONOR".equals(SystemProperties.get("ro.product.brand"));
    private static final boolean IS_NOVA_PRODUCT = SystemProperties.getBoolean("ro.config.hw_novaThemeSupport", false);
    private static final String NOVA_BLUE_COLOR = "#596FE1";
    private static final int POINT_SPACE = 8;
    private Context mContext;
    private int mCurrentPage = 0;
    private int mPageSize = 0;

    public QuickNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float radius;
        super.onDraw(canvas);
        float startPointX = calcStartPointX();
        if (startPointX != 0.0f) {
            Paint paint = new Paint();
            for (int i = 0; i < this.mPageSize; i++) {
                if (i == this.mCurrentPage) {
                    if (IS_HONOR_PRODUCT) {
                        paint.setColor(Color.parseColor(HONOR_BLUE_COLOR));
                    } else if (IS_NOVA_PRODUCT) {
                        paint.setColor(Color.parseColor(NOVA_BLUE_COLOR));
                    } else {
                        paint.setColor(Color.parseColor(HW_BLUE_COLOR));
                    }
                    radius = 3.0f * this.mContext.getResources().getDisplayMetrics().density;
                } else {
                    paint.setColor(Color.parseColor(GRAY_COLRO));
                    radius = this.mContext.getResources().getDisplayMetrics().density * 2.0f;
                }
                canvas.drawCircle((((float) (i * 8)) * this.mContext.getResources().getDisplayMetrics().density) + startPointX, ((float) getHeight()) / 2.0f, radius, paint);
            }
        }
    }

    private float calcStartPointX() {
        if (this.mPageSize == 0) {
            return 0.0f;
        }
        return (((float) getWidth()) - (((float) ((this.mPageSize - 1) * 8)) * this.mContext.getResources().getDisplayMetrics().density)) / 2.0f;
    }

    public void setPageSize(int pagesize) {
        this.mPageSize = pagesize;
        invalidate();
    }

    public void scrollToPage(int page) {
        this.mCurrentPage = page;
        invalidate();
    }
}
