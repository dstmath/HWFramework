package android.text.style;

import android.graphics.Rasterizer;
import android.text.TextPaint;

public class RasterizerSpan extends CharacterStyle implements UpdateAppearance {
    private Rasterizer mRasterizer;

    public RasterizerSpan(Rasterizer r) {
        this.mRasterizer = r;
    }

    public Rasterizer getRasterizer() {
        return this.mRasterizer;
    }

    public void updateDrawState(TextPaint ds) {
        ds.setRasterizer(this.mRasterizer);
    }
}
