package huawei.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AnimationView extends ImageView {
    private Drawable mBgDrawable;
    private Drawable mDrawable;
    private boolean mIsShowBg;
    private int mTranslateX;
    private int mTranslateY;

    public AnimationView(Context context) {
        this(context, null);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mIsShowBg = false;
        this.mTranslateY = 0;
        init();
    }

    public void showBg(boolean isShow) {
        if (isShow != this.mIsShowBg) {
            this.mIsShowBg = isShow;
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mBgDrawable.setBounds(0, 0, w, h);
    }

    private void init() {
        this.mDrawable = getResources().getDrawable(33751784, null);
        this.mBgDrawable = getResources().getDrawable(33751783, null);
    }

    public void update(int offsetX, int width) {
        this.mTranslateX = offsetX;
        this.mDrawable.setBounds(0, 0, width, getMeasuredHeight());
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mIsShowBg) {
            this.mBgDrawable.draw(canvas);
            canvas.save();
            canvas.translate((float) this.mTranslateX, (float) this.mTranslateY);
            this.mDrawable.draw(canvas);
            canvas.restore();
        }
    }
}
