package com.huawei.hwanimation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import androidhwext.R;

public class HwClipRectAnimation extends Animation {
    private static final String TAG = HwClipRectAnimation.class.getSimpleName();
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
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HwClipRectAnimation);
        Animation.Description description = parseValue(typedArray.peekValue(0));
        this.mFromLeftType = description.type;
        this.mFromLeftValue = description.value;
        Animation.Description description2 = parseValue(typedArray.peekValue(1));
        this.mFromTopType = description2.type;
        this.mFromTopValue = description2.value;
        Animation.Description description3 = parseValue(typedArray.peekValue(2));
        this.mFromRightType = description3.type;
        this.mFromRightValue = description3.value;
        Animation.Description description4 = parseValue(typedArray.peekValue(3));
        this.mFromBottomType = description4.type;
        this.mFromBottomValue = description4.value;
        Animation.Description description5 = parseValue(typedArray.peekValue(4));
        this.mToLeftType = description5.type;
        this.mToLeftValue = description5.value;
        Animation.Description description6 = parseValue(typedArray.peekValue(5));
        this.mToTopType = description6.type;
        this.mToTopValue = description6.value;
        Animation.Description description7 = parseValue(typedArray.peekValue(6));
        this.mToRightType = description7.type;
        this.mToRightValue = description7.value;
        Animation.Description description8 = parseValue(typedArray.peekValue(7));
        this.mToBottomType = description8.type;
        this.mToBottomValue = description8.value;
        typedArray.recycle();
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
        Animation.Description description = new HwDescription();
        if (value == null) {
            description.type = 0;
            description.value = 0.0f;
        } else if (value.type == 6) {
            int i = 1;
            if ((value.data & 15) == 1) {
                i = 2;
            }
            description.type = i;
            description.value = TypedValue.complexToFloat(value.data);
            return description;
        } else if (value.type == 4) {
            description.type = 0;
            description.value = value.getFloat();
            return description;
        } else if (value.type < 16 || value.type > 31) {
            String str = TAG;
            Log.e(str, "Wrong value type:" + value.type);
        } else {
            description.type = 0;
            description.value = (float) value.data;
            return description;
        }
        description.type = 0;
        description.value = 0.0f;
        return description;
    }

    public void setCropStartPoint(Point offsets) {
        this.mOffsets = offsets;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.animation.Animation
    public void applyTransformation(float it, Transformation tr) {
        this.mCurrentRect.set(this.mFromRect.left + ((int) (((float) (this.mToRect.left - this.mFromRect.left)) * it)), this.mFromRect.top + ((int) (((float) (this.mToRect.top - this.mFromRect.top)) * it)), this.mFromRect.right + ((int) (((float) (this.mToRect.right - this.mFromRect.right)) * it)), this.mFromRect.bottom + ((int) (((float) (this.mToRect.bottom - this.mFromRect.bottom)) * it)));
        Point point = this.mOffsets;
        if (point != null) {
            this.mCurrentRect.offset(point.x, this.mOffsets.y);
        }
        tr.setClipRect(this.mCurrentRect);
    }

    @Override // android.view.animation.Animation
    public boolean willChangeTransformationMatrix() {
        return false;
    }

    @Override // android.view.animation.Animation
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
