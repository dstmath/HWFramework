package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import com.android.internal.R;
import org.xmlpull.v1.XmlPullParser;

public class PaintDrawable extends ShapeDrawable {
    public PaintDrawable(int color) {
        getPaint().setColor(color);
    }

    public void setCornerRadius(float radius) {
        float[] radii = null;
        if (radius > 0.0f) {
            radii = new float[8];
            for (int i = 0; i < 8; i++) {
                radii[i] = radius;
            }
        }
        setCornerRadii(radii);
    }

    public void setCornerRadii(float[] radii) {
        if (radii != null) {
            setShape(new RoundRectShape(radii, null, null));
        } else if (getShape() != null) {
            setShape(null);
        }
        invalidateSelf();
    }

    protected boolean inflateTag(String name, Resources r, XmlPullParser parser, AttributeSet attrs) {
        if (!name.equals("corners")) {
            return super.inflateTag(name, r, parser, attrs);
        }
        TypedArray a = r.obtainAttributes(attrs, R.styleable.DrawableCorners);
        int radius = a.getDimensionPixelSize(0, 0);
        setCornerRadius((float) radius);
        int topLeftRadius = a.getDimensionPixelSize(1, radius);
        int topRightRadius = a.getDimensionPixelSize(2, radius);
        int bottomLeftRadius = a.getDimensionPixelSize(3, radius);
        int bottomRightRadius = a.getDimensionPixelSize(4, radius);
        if (topLeftRadius == radius && topRightRadius == radius && bottomLeftRadius == radius) {
            if (bottomRightRadius != radius) {
            }
            a.recycle();
            return true;
        }
        setCornerRadii(new float[]{(float) topLeftRadius, (float) topLeftRadius, (float) topRightRadius, (float) topRightRadius, (float) bottomLeftRadius, (float) bottomLeftRadius, (float) bottomRightRadius, (float) bottomRightRadius});
        a.recycle();
        return true;
    }
}
