package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.widget.TextView;

public class ButtonWindowTextView extends TextView {
    private int mOrientation = 1;

    public ButtonWindowTextView(Context context, int orientation) {
        super(context);
        this.mOrientation = orientation;
    }

    public ButtonWindowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mOrientation == 2) {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec);
            setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected boolean setFrame(int l, int t, int r, int b) {
        if (this.mOrientation == 2) {
            return super.setFrame(l, t, (b - t) + l, (r - l) + t);
        }
        return super.setFrame(l, t, r, b);
    }

    public void draw(Canvas canvas) {
        if (this.mOrientation == 2) {
            canvas.translate(0.0f, (float) getWidth());
            canvas.rotate(-90.0f);
            canvas.clipRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), Op.REPLACE);
        }
        super.draw(canvas);
    }
}
