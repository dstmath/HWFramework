package huawei.android.graphics.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.Locale;

public class HwEventBadge extends Drawable {
    private static final int BADGE_RECTANGLE_NUM = 10;
    private static final int DEFAULT_BADGE_MAX_NUM = 99;
    private Drawable mBackgroundDrawable;
    private int mBadgeCount = 0;
    private float mBaseLine;
    private Context mContext;
    private String mCountText;
    private TextPaint mPaint;
    private float mTextStartX;

    public HwEventBadge(Context context) {
        this.mContext = context;
        this.mPaint = new TextPaint();
        this.mPaint.setColor(ResLoaderUtil.getColor(context, "emui_white"));
        this.mPaint.setAntiAlias(true);
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setTextSize((float) ResLoaderUtil.getDimensionPixelSize(context, "badge_default_text_size"));
        refresh();
    }

    private void refresh() {
        this.mBackgroundDrawable = ResLoader.getInstance().getResources(this.mContext).getDrawable(ResLoaderUtil.getDrawableId(this.mContext, "badge_bg"), null);
        this.mBackgroundDrawable.setTint(ResLoaderUtil.getColor(this.mContext, "emui_functional_red"));
    }

    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
        if (this.mBackgroundDrawable != null) {
            Rect backRect = this.mBackgroundDrawable.getBounds();
            if (bounds.width() < backRect.width() || bounds.height() < backRect.height()) {
                super.setBounds(backRect);
            }
        }
    }

    public void setBadgeCount(int count) {
        setBadgeCount(count, DEFAULT_BADGE_MAX_NUM);
    }

    public void setBadgeCount(int count, int maxNumber) {
        if (this.mContext != null) {
            this.mBadgeCount = count;
            if (this.mBadgeCount <= maxNumber) {
                this.mCountText = String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(this.mBadgeCount)});
            } else {
                this.mCountText = String.format(Locale.getDefault(), "%d+", new Object[]{Integer.valueOf(maxNumber)});
            }
            calcBadgeLocation();
        }
    }

    private void calcBadgeLocation() {
        int backgroundWidth;
        int backgroundWidth2;
        float width = this.mPaint.measureText(this.mCountText);
        float height = this.mPaint.descent() - this.mPaint.ascent();
        if (this.mBadgeCount == 0) {
            backgroundWidth2 = ResLoaderUtil.getDimensionPixelSize(this.mContext, "badge_dot_size");
            backgroundWidth = backgroundWidth2;
        } else if (this.mBadgeCount < BADGE_RECTANGLE_NUM) {
            backgroundWidth2 = ResLoaderUtil.getDimensionPixelSize(this.mContext, "badge_height");
            backgroundWidth = backgroundWidth2;
        } else {
            int dimensionPixelSize = (int) ((((float) ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_s")) * 2.0f) + width);
            backgroundWidth = ResLoaderUtil.getDimensionPixelSize(this.mContext, "badge_height");
            backgroundWidth2 = dimensionPixelSize;
        }
        this.mTextStartX = (((float) backgroundWidth2) - width) / 2.0f;
        this.mBaseLine = ((((float) backgroundWidth) - height) / 2.0f) - this.mPaint.ascent();
        setBounds(0, 0, backgroundWidth2, backgroundWidth);
        this.mBackgroundDrawable.setBounds(0, 0, backgroundWidth2, backgroundWidth);
        invalidateSelf();
    }

    public void setBackgoundColor(int badgeColor) {
        this.mBackgroundDrawable.setTint(badgeColor);
        invalidateSelf();
    }

    public void setTextColor(int textColor) {
        if (this.mPaint.getColor() != textColor) {
            this.mPaint.setColor(textColor);
            invalidateSelf();
        }
    }

    public void draw(Canvas canvas) {
        if (this.mBackgroundDrawable != null) {
            this.mBackgroundDrawable.draw(canvas);
            if (this.mBadgeCount != 0) {
                canvas.drawText(this.mCountText, this.mTextStartX, this.mBaseLine, this.mPaint);
            }
        }
    }

    public int getOpacity() {
        return 0;
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }
}
