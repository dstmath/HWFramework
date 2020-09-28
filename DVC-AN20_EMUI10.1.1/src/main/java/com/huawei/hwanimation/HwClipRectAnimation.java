package com.huawei.hwanimation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import androidhwext.R;

public class HwClipRectAnimation extends Animation {
    protected final Rect mCurrentRect = new Rect();
    private int mFromBottomType = 0;
    private float mFromBottomValue;
    private int mFromLeftType = 0;
    private float mFromLeftValue;
    protected final Rect mFromRect = new Rect();
    private int mFromRightType = 0;
    private float mFromRightValue;
    private int mFromTopType = 0;
    private float mFromTopValue;
    private Point mOffsets;
    private int mToBottomType = 0;
    private float mToBottomValue;
    private int mToLeftType = 0;
    private float mToLeftValue;
    protected final Rect mToRect = new Rect();
    private int mToRightType = 0;
    private float mToRightValue;
    private int mToTopType = 0;
    private float mToTopValue;

    public HwClipRectAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HwClipRectAnimation);
        Animation.Description d = parseValue(a.peekValue(0));
        this.mFromLeftType = d.type;
        this.mFromLeftValue = d.value;
        Animation.Description d2 = parseValue(a.peekValue(1));
        this.mFromTopType = d2.type;
        this.mFromTopValue = d2.value;
        Animation.Description d3 = parseValue(a.peekValue(2));
        this.mFromRightType = d3.type;
        this.mFromRightValue = d3.value;
        Animation.Description d4 = parseValue(a.peekValue(3));
        this.mFromBottomType = d4.type;
        this.mFromBottomValue = d4.value;
        Animation.Description d5 = parseValue(a.peekValue(4));
        this.mToLeftType = d5.type;
        this.mToLeftValue = d5.value;
        Animation.Description d6 = parseValue(a.peekValue(5));
        this.mToTopType = d6.type;
        this.mToTopValue = d6.value;
        Animation.Description d7 = parseValue(a.peekValue(6));
        this.mToRightType = d7.type;
        this.mToRightValue = d7.value;
        Animation.Description d8 = parseValue(a.peekValue(7));
        this.mToBottomType = d8.type;
        this.mToBottomValue = d8.value;
        a.recycle();
    }

    public HwClipRectAnimation(Rect fromClip, Rect toClip) {
        if (fromClip == null || toClip == null) {
            throw new NonClipException("Expected non-null animation clip rects");
        }
        this.mFromLeftValue = (float) fromClip.left;
        this.mFromTopValue = (float) fromClip.top;
        this.mFromRightValue = (float) fromClip.right;
        this.mFromBottomValue = (float) fromClip.bottom;
        this.mToLeftValue = (float) toClip.left;
        this.mToTopValue = (float) toClip.top;
        this.mToRightValue = (float) toClip.right;
        this.mToBottomValue = (float) toClip.bottom;
    }

    private Animation.Description parseValue(TypedValue value) {
        Animation.Description d = new HwDescription();
        if (value == null) {
            d.type = 0;
            d.value = 0.0f;
        } else if (value.type == 6) {
            int i = 1;
            if ((value.data & 15) == 1) {
                i = 2;
            }
            d.type = i;
            d.value = TypedValue.complexToFloat(value.data);
            return d;
        } else if (value.type == 4) {
            d.type = 0;
            d.value = value.getFloat();
            return d;
        } else if (value.type >= 16 && value.type <= 31) {
            d.type = 0;
            d.value = (float) value.data;
            return d;
        }
        d.type = 0;
        d.value = 0.0f;
        return d;
    }

    public void setCropStartPoint(Point offsets) {
        this.mOffsets = offsets;
    }

    /* access modifiers changed from: protected */
    public void applyTransformation(float it, Transformation tr) {
        this.mCurrentRect.set(this.mFromRect.left + ((int) (((float) (this.mToRect.left - this.mFromRect.left)) * it)), this.mFromRect.top + ((int) (((float) (this.mToRect.top - this.mFromRect.top)) * it)), this.mFromRect.right + ((int) (((float) (this.mToRect.right - this.mFromRect.right)) * it)), this.mFromRect.bottom + ((int) (((float) (this.mToRect.bottom - this.mFromRect.bottom)) * it)));
        Point point = this.mOffsets;
        if (point != null) {
            this.mCurrentRect.offset(point.x, this.mOffsets.y);
        }
        tr.setClipRect(this.mCurrentRect);
    }

    public boolean willChangeTransformationMatrix() {
        return false;
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        this.mFromRect.set((int) resolveSize(this.mFromLeftType, this.mFromLeftValue, width, parentWidth), (int) resolveSize(this.mFromTopType, this.mFromTopValue, height, parentHeight), (int) resolveSize(this.mFromRightType, this.mFromRightValue, width, parentWidth), (int) resolveSize(this.mFromBottomType, this.mFromBottomValue, height, parentHeight));
        this.mToRect.set((int) resolveSize(this.mToLeftType, this.mToLeftValue, width, parentWidth), (int) resolveSize(this.mToTopType, this.mToTopValue, height, parentHeight), (int) resolveSize(this.mToRightType, this.mToRightValue, width, parentWidth), (int) resolveSize(this.mToBottomType, this.mToBottomValue, height, parentHeight));
    }

    /* access modifiers changed from: private */
    public static class HwDescription extends Animation.Description {
        private HwDescription() {
        }
    }

    public static class NonClipException extends RuntimeException {
        public NonClipException(String msg) {
            super(msg);
        }
    }
}
