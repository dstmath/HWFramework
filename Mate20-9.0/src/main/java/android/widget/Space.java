package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public final class Space extends View {
    public Space(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (getVisibility() == 0) {
            setVisibility(4);
        }
    }

    public Space(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Space(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Space(Context context) {
        this(context, null);
    }

    public void draw(Canvas canvas) {
    }

    private static int getDefaultSize2(int size, int measureSpec) {
        int result = size;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(size, specSize);
        }
        if (specMode == 0) {
            return size;
        }
        if (specMode != 1073741824) {
            return result;
        }
        return specSize;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize2(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize2(getSuggestedMinimumHeight(), heightMeasureSpec));
    }
}
