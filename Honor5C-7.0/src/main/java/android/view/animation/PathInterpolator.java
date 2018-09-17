package android.view.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.PathParser;
import android.view.InflateException;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;

public class PathInterpolator extends BaseInterpolator {
    private static final float PRECISION = 0.002f;
    private float[] mX;
    private float[] mY;

    public PathInterpolator(Path path) {
        initPath(path);
    }

    public PathInterpolator(float controlX, float controlY) {
        initQuad(controlX, controlY);
    }

    public PathInterpolator(float controlX1, float controlY1, float controlX2, float controlY2) {
        initCubic(controlX1, controlY1, controlX2, controlY2);
    }

    public PathInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    public PathInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.PathInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.PathInterpolator);
        }
        parseInterpolatorFromTypeArray(a);
        setChangingConfiguration(a.getChangingConfigurations());
        a.recycle();
    }

    private void parseInterpolatorFromTypeArray(TypedArray a) {
        if (a.hasValue(4)) {
            String pathData = a.getString(4);
            Path path = PathParser.createPathFromPathData(pathData);
            if (path == null) {
                throw new InflateException("The path is null, which is created from " + pathData);
            }
            initPath(path);
        } else if (!a.hasValue(0)) {
            throw new InflateException("pathInterpolator requires the controlX1 attribute");
        } else if (a.hasValue(1)) {
            float x1 = a.getFloat(0, 0.0f);
            float y1 = a.getFloat(1, 0.0f);
            boolean hasX2 = a.hasValue(2);
            if (hasX2 != a.hasValue(3)) {
                throw new InflateException("pathInterpolator requires both controlX2 and controlY2 for cubic Beziers.");
            } else if (hasX2) {
                initCubic(x1, y1, a.getFloat(2, 0.0f), a.getFloat(3, 0.0f));
            } else {
                initQuad(x1, y1);
            }
        } else {
            throw new InflateException("pathInterpolator requires the controlY1 attribute");
        }
    }

    private void initQuad(float controlX, float controlY) {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.quadTo(controlX, controlY, LayoutParams.BRIGHTNESS_OVERRIDE_FULL, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        initPath(path);
    }

    private void initCubic(float x1, float y1, float x2, float y2) {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(x1, y1, x2, y2, LayoutParams.BRIGHTNESS_OVERRIDE_FULL, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        initPath(path);
    }

    private void initPath(Path path) {
        float[] pointComponents = path.approximate(PRECISION);
        int numPoints = pointComponents.length / 3;
        if (pointComponents[1] == 0.0f && pointComponents[2] == 0.0f && pointComponents[pointComponents.length - 2] == LayoutParams.BRIGHTNESS_OVERRIDE_FULL && pointComponents[pointComponents.length - 1] == LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            this.mX = new float[numPoints];
            this.mY = new float[numPoints];
            float prevX = 0.0f;
            float prevFraction = 0.0f;
            int i = 0;
            int componentIndex = 0;
            while (i < numPoints) {
                int componentIndex2 = componentIndex + 1;
                float fraction = pointComponents[componentIndex];
                componentIndex = componentIndex2 + 1;
                float x = pointComponents[componentIndex2];
                componentIndex2 = componentIndex + 1;
                float y = pointComponents[componentIndex];
                if (fraction == prevFraction && x != prevX) {
                    throw new IllegalArgumentException("The Path cannot have discontinuity in the X axis.");
                } else if (x < prevX) {
                    throw new IllegalArgumentException("The Path cannot loop back on itself.");
                } else {
                    this.mX[i] = x;
                    this.mY[i] = y;
                    prevX = x;
                    prevFraction = fraction;
                    i++;
                    componentIndex = componentIndex2;
                }
            }
            return;
        }
        throw new IllegalArgumentException("The Path must start at (0,0) and end at (1,1)");
    }

    public float getInterpolation(float t) {
        if (t <= 0.0f) {
            return 0.0f;
        }
        if (t >= LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            return LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        }
        int startIndex = 0;
        int endIndex = this.mX.length - 1;
        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (t < this.mX[midIndex]) {
                endIndex = midIndex;
            } else {
                startIndex = midIndex;
            }
        }
        float xRange = this.mX[endIndex] - this.mX[startIndex];
        if (xRange == 0.0f) {
            return this.mY[startIndex];
        }
        float fraction = (t - this.mX[startIndex]) / xRange;
        float startY = this.mY[startIndex];
        return ((this.mY[endIndex] - startY) * fraction) + startY;
    }
}
