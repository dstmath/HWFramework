package huawei.android.widget;

public class ClickEffectEntry {
    private static final float CLICK_EFFECT_MAX_RECSCALE = 1.0f;
    private static final float CLICK_EFFECT_MIN_RECSCALE = 0.9f;
    private static final float DEFAULT_CLICK_EFFECT_ALPHA = 1.0f;
    private static final float DEFAULT_CLICK_EFFECT_CORNER_RADIUS = 12.0f;
    private static final int DEFAULT_COLOR = 201326592;
    public float mClickEffectAlpha = 1.0f;
    public int mClickEffectColor = DEFAULT_COLOR;
    public float mClickEffectCornerRadius = DEFAULT_CLICK_EFFECT_CORNER_RADIUS;
    public boolean mClickEffectForceDoScaleAnim = true;
    public float mClickEffectMaxRecScale = 1.0f;
    public float mClickEffectMinRecScale = CLICK_EFFECT_MIN_RECSCALE;
}
