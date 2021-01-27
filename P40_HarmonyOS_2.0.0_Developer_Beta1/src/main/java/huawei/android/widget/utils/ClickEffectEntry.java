package huawei.android.widget.utils;

public class ClickEffectEntry {
    private static final float DEFAULT_ALPHA = 1.0f;
    private static final int DEFAULT_COLOR = 201326592;
    private static final float DEFAULT_CORNER_RADIUS = 12.0f;
    private static final float DEFAULT_MAX_REC_SCALE = 1.0f;
    private static final float DEFAULT_MIN_REC_SCALE = 0.9f;
    private float mAlpha = 1.0f;
    private int mColor = DEFAULT_COLOR;
    private float mCornerRadius = DEFAULT_CORNER_RADIUS;
    private boolean mIsForceDoScaleAnim = true;
    private float mMaxRecScale = 1.0f;
    private float mMinRecScale = DEFAULT_MIN_REC_SCALE;

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
    }

    public float getMinRecScale() {
        return this.mMinRecScale;
    }

    public void setMinRecScale(float minRecScale) {
        this.mMinRecScale = minRecScale;
    }

    public float getMaxRecScale() {
        return this.mMaxRecScale;
    }

    public void setMaxRecScale(float maxRecScale) {
        this.mMaxRecScale = maxRecScale;
    }

    public float getCornerRadius() {
        return this.mCornerRadius;
    }

    public void setCornerRadius(float cornerRadius) {
        this.mCornerRadius = cornerRadius;
    }

    public boolean isForceDoScaleAnim() {
        return this.mIsForceDoScaleAnim;
    }

    public void setIsForceDoScaleAnim(boolean isForceDoScaleAnim) {
        this.mIsForceDoScaleAnim = isForceDoScaleAnim;
    }
}
