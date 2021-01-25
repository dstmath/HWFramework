package android.transition;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.util.AttributeSet;
import com.android.internal.R;

public class ArcMotion extends PathMotion {
    private static final float DEFAULT_MAX_ANGLE_DEGREES = 70.0f;
    private static final float DEFAULT_MAX_TANGENT = ((float) Math.tan(Math.toRadians(35.0d)));
    private static final float DEFAULT_MIN_ANGLE_DEGREES = 0.0f;
    private float mMaximumAngle = DEFAULT_MAX_ANGLE_DEGREES;
    private float mMaximumTangent = DEFAULT_MAX_TANGENT;
    private float mMinimumHorizontalAngle = 0.0f;
    private float mMinimumHorizontalTangent = 0.0f;
    private float mMinimumVerticalAngle = 0.0f;
    private float mMinimumVerticalTangent = 0.0f;

    public ArcMotion() {
    }

    public ArcMotion(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcMotion);
        setMinimumVerticalAngle(a.getFloat(1, 0.0f));
        setMinimumHorizontalAngle(a.getFloat(0, 0.0f));
        setMaximumAngle(a.getFloat(2, DEFAULT_MAX_ANGLE_DEGREES));
        a.recycle();
    }

    public void setMinimumHorizontalAngle(float angleInDegrees) {
        this.mMinimumHorizontalAngle = angleInDegrees;
        this.mMinimumHorizontalTangent = toTangent(angleInDegrees);
    }

    public float getMinimumHorizontalAngle() {
        return this.mMinimumHorizontalAngle;
    }

    public void setMinimumVerticalAngle(float angleInDegrees) {
        this.mMinimumVerticalAngle = angleInDegrees;
        this.mMinimumVerticalTangent = toTangent(angleInDegrees);
    }

    public float getMinimumVerticalAngle() {
        return this.mMinimumVerticalAngle;
    }

    public void setMaximumAngle(float angleInDegrees) {
        this.mMaximumAngle = angleInDegrees;
        this.mMaximumTangent = toTangent(angleInDegrees);
    }

    public float getMaximumAngle() {
        return this.mMaximumAngle;
    }

    private static float toTangent(float arcInDegrees) {
        if (arcInDegrees >= 0.0f && arcInDegrees <= 90.0f) {
            return (float) Math.tan(Math.toRadians((double) (arcInDegrees / 2.0f)));
        }
        throw new IllegalArgumentException("Arc must be between 0 and 90 degrees");
    }

    @Override // android.transition.PathMotion
    public Path getPath(float startX, float startY, float endX, float endY) {
        float minimumArcDist2;
        float ey;
        float eDistX;
        float newArcDistance2;
        float ey2;
        float ex;
        float ex2;
        float ex3;
        float ey3;
        Path path = new Path();
        path.moveTo(startX, startY);
        float deltaX = endX - startX;
        float deltaY = endY - startY;
        float h2 = (deltaX * deltaX) + (deltaY * deltaY);
        float dx = (startX + endX) / 2.0f;
        float dy = (startY + endY) / 2.0f;
        float midDist2 = h2 * 0.25f;
        boolean isMovingUpwards = startY > endY;
        if (deltaY == 0.0f) {
            eDistX = dx;
            ey = (Math.abs(deltaX) * 0.5f * this.mMinimumHorizontalTangent) + dy;
            minimumArcDist2 = 0.0f;
        } else if (deltaX == 0.0f) {
            eDistX = (Math.abs(deltaY) * 0.5f * this.mMinimumVerticalTangent) + dx;
            ey = dy;
            minimumArcDist2 = 0.0f;
        } else if (Math.abs(deltaX) < Math.abs(deltaY)) {
            float eDistY = Math.abs(h2 / (deltaY * 2.0f));
            if (isMovingUpwards) {
                ey3 = endY + eDistY;
                ex3 = endX;
            } else {
                ey3 = startY + eDistY;
                ex3 = startX;
            }
            float f = this.mMinimumVerticalTangent;
            minimumArcDist2 = midDist2 * f * f;
            eDistX = ex3;
            ey = ey3;
        } else {
            float eDistX2 = h2 / (deltaX * 2.0f);
            if (isMovingUpwards) {
                ex2 = startX + eDistX2;
                ey = startY;
            } else {
                ex2 = endX - eDistX2;
                ey = endY;
            }
            float f2 = this.mMinimumHorizontalTangent;
            minimumArcDist2 = midDist2 * f2 * f2;
            eDistX = ex2;
        }
        float arcDistX = dx - eDistX;
        float arcDistY = dy - ey;
        float arcDist2 = (arcDistX * arcDistX) + (arcDistY * arcDistY);
        float f3 = this.mMaximumTangent;
        float maximumArcDist2 = midDist2 * f3 * f3;
        if (arcDist2 != 0.0f && arcDist2 < minimumArcDist2) {
            newArcDistance2 = minimumArcDist2;
        } else if (arcDist2 > maximumArcDist2) {
            newArcDistance2 = maximumArcDist2;
        } else {
            newArcDistance2 = 0.0f;
        }
        if (newArcDistance2 != 0.0f) {
            float ratio = (float) Math.sqrt((double) (newArcDistance2 / arcDist2));
            ex = dx + ((eDistX - dx) * ratio);
            ey2 = dy + ((ey - dy) * ratio);
        } else {
            ex = eDistX;
            ey2 = ey;
        }
        path.cubicTo((startX + ex) / 2.0f, (startY + ey2) / 2.0f, (ex + endX) / 2.0f, (ey2 + endY) / 2.0f, endX, endY);
        return path;
    }
}
