package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.Animation;
import com.android.internal.R;

public class ClipRectAnimation extends Animation {
    private int mFromBottomType;
    private float mFromBottomValue;
    private int mFromLeftType;
    private float mFromLeftValue;
    protected final Rect mFromRect;
    private int mFromRightType;
    private float mFromRightValue;
    private int mFromTopType;
    private float mFromTopValue;
    private int mToBottomType;
    private float mToBottomValue;
    private int mToLeftType;
    private float mToLeftValue;
    protected final Rect mToRect;
    private int mToRightType;
    private float mToRightValue;
    private int mToTopType;
    private float mToTopValue;

    public ClipRectAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFromRect = new Rect();
        this.mToRect = new Rect();
        this.mFromLeftType = 0;
        this.mFromTopType = 0;
        this.mFromRightType = 0;
        this.mFromBottomType = 0;
        this.mToLeftType = 0;
        this.mToTopType = 0;
        this.mToRightType = 0;
        this.mToBottomType = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClipRectAnimation);
        Animation.Description d = Animation.Description.parseValue(a.peekValue(1));
        this.mFromLeftType = d.type;
        this.mFromLeftValue = d.value;
        Animation.Description d2 = Animation.Description.parseValue(a.peekValue(3));
        this.mFromTopType = d2.type;
        this.mFromTopValue = d2.value;
        Animation.Description d3 = Animation.Description.parseValue(a.peekValue(2));
        this.mFromRightType = d3.type;
        this.mFromRightValue = d3.value;
        Animation.Description d4 = Animation.Description.parseValue(a.peekValue(0));
        this.mFromBottomType = d4.type;
        this.mFromBottomValue = d4.value;
        Animation.Description d5 = Animation.Description.parseValue(a.peekValue(5));
        this.mToLeftType = d5.type;
        this.mToLeftValue = d5.value;
        Animation.Description d6 = Animation.Description.parseValue(a.peekValue(7));
        this.mToTopType = d6.type;
        this.mToTopValue = d6.value;
        Animation.Description d7 = Animation.Description.parseValue(a.peekValue(6));
        this.mToRightType = d7.type;
        this.mToRightValue = d7.value;
        Animation.Description d8 = Animation.Description.parseValue(a.peekValue(4));
        this.mToBottomType = d8.type;
        this.mToBottomValue = d8.value;
        a.recycle();
    }

    public ClipRectAnimation(Rect fromClip, Rect toClip) {
        this.mFromRect = new Rect();
        this.mToRect = new Rect();
        this.mFromLeftType = 0;
        this.mFromTopType = 0;
        this.mFromRightType = 0;
        this.mFromBottomType = 0;
        this.mToLeftType = 0;
        this.mToTopType = 0;
        this.mToRightType = 0;
        this.mToBottomType = 0;
        if (fromClip == null || toClip == null) {
            throw new RuntimeException("Expected non-null animation clip rects");
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

    public ClipRectAnimation(int fromL, int fromT, int fromR, int fromB, int toL, int toT, int toR, int toB) {
        this(new Rect(fromL, fromT, fromR, fromB), new Rect(toL, toT, toR, toB));
    }

    /* access modifiers changed from: protected */
    public void applyTransformation(float it, Transformation tr) {
        tr.setClipRect(this.mFromRect.left + ((int) (((float) (this.mToRect.left - this.mFromRect.left)) * it)), this.mFromRect.top + ((int) (((float) (this.mToRect.top - this.mFromRect.top)) * it)), this.mFromRect.right + ((int) (((float) (this.mToRect.right - this.mFromRect.right)) * it)), this.mFromRect.bottom + ((int) (((float) (this.mToRect.bottom - this.mFromRect.bottom)) * it)));
    }

    public boolean willChangeTransformationMatrix() {
        return false;
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        this.mFromRect.set((int) resolveSize(this.mFromLeftType, this.mFromLeftValue, width, parentWidth), (int) resolveSize(this.mFromTopType, this.mFromTopValue, height, parentHeight), (int) resolveSize(this.mFromRightType, this.mFromRightValue, width, parentWidth), (int) resolveSize(this.mFromBottomType, this.mFromBottomValue, height, parentHeight));
        this.mToRect.set((int) resolveSize(this.mToLeftType, this.mToLeftValue, width, parentWidth), (int) resolveSize(this.mToTopType, this.mToTopValue, height, parentHeight), (int) resolveSize(this.mToRightType, this.mToRightValue, width, parentWidth), (int) resolveSize(this.mToBottomType, this.mToBottomValue, height, parentHeight));
    }
}
