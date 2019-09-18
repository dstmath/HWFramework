package huawei.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AnimationView extends ImageView {
    private Drawable mBgDrawable;
    private Drawable mDrawable;
    private boolean mShowBg;
    private int mTranslateX;
    private int mTranslateY;

    public void showBg(boolean show) {
        if (show != this.mShowBg) {
            this.mShowBg = show;
            invalidate();
        }
    }

    public AnimationView(Context context) {
        this(context, null);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mShowBg = false;
        this.mTranslateY = 0;
        init();
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
        if (this.mShowBg) {
            this.mBgDrawable.draw(canvas);
            canvas.save();
            canvas.translate((float) this.mTranslateX, (float) this.mTranslateY);
            this.mDrawable.draw(canvas);
            canvas.restore();
        }
    }
}
