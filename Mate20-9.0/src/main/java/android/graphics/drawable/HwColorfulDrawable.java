package android.graphics.drawable;

import android.content.res.Resources;
import android.graphics.drawable.LayerDrawable;
import android.hwcontrol.HwWidgetFactory;

public class HwColorfulDrawable extends LayerDrawable {
    private static int mColor;

    static class HwColorfulLayerState extends LayerDrawable.LayerState {
        HwColorfulLayerState(LayerDrawable.LayerState orig, LayerDrawable owner, Resources res) {
            super(orig, owner, res);
        }

        public Drawable newDrawable() {
            return new HwColorfulDrawable((LayerDrawable.LayerState) this, (Resources) null);
        }

        public Drawable newDrawable(Resources res) {
            return new HwColorfulDrawable((LayerDrawable.LayerState) this, res);
        }

        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }
    }

    public HwColorfulDrawable(Drawable[] layers) {
        super(layers);
    }

    HwColorfulDrawable(Drawable[] layers, LayerDrawable.LayerState state) {
        super(layers, state);
    }

    HwColorfulDrawable() {
    }

    HwColorfulDrawable(LayerDrawable.LayerState state, Resources res) {
        super(state, res);
    }

    /* access modifiers changed from: package-private */
    public HwColorfulLayerState createConstantState(LayerDrawable.LayerState state, Resources res) {
        if (res != null) {
            int color = HwWidgetFactory.getControlColor(res);
            updateColorfulTint(color);
            updateColor(color);
        }
        return new HwColorfulLayerState(state, this, res);
    }

    public void setColor(int color) {
        if (mColor != color) {
            updateColorfulTint(color);
            updateColor(color);
            invalidateSelf();
        }
    }

    private static void updateColor(int color) {
        mColor = color;
    }

    private void updateColorfulTint(int color) {
        if (mColor != color) {
            LayerDrawable.ChildDrawable[] array = this.mLayerState.mChildren;
            int N = this.mLayerState.mNumChildren;
            for (int i = 0; i < N; i++) {
                Drawable dr = array[i].mDrawable;
                if (array[i].mId == 16908334) {
                    dr.setTint(mColor);
                }
            }
        }
    }
}
