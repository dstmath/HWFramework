package android.graphics.drawable;

import android.R;
import android.content.res.Resources;
import android.hwcontrol.HwWidgetFactory;

public class HwColorfulDrawable extends LayerDrawable {
    private static int mColor;

    static class HwColorfulLayerState extends LayerState {
        HwColorfulLayerState(LayerState orig, LayerDrawable owner, Resources res) {
            super(orig, owner, res);
        }

        public Drawable newDrawable() {
            return new HwColorfulDrawable((LayerState) this, null);
        }

        public Drawable newDrawable(Resources res) {
            return new HwColorfulDrawable((LayerState) this, res);
        }

        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }
    }

    public HwColorfulDrawable(Drawable[] layers) {
        super(layers);
    }

    HwColorfulDrawable(Drawable[] layers, LayerState state) {
        super(layers, state);
    }

    HwColorfulDrawable() {
    }

    HwColorfulDrawable(LayerState state, Resources res) {
        super(state, res);
    }

    HwColorfulLayerState createConstantState(LayerState state, Resources res) {
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
            ChildDrawable[] array = this.mLayerState.mChildren;
            int N = this.mLayerState.mNumChildren;
            for (int i = 0; i < N; i++) {
                Drawable dr = array[i].mDrawable;
                if (array[i].mId == R.id.mask) {
                    dr.setTint(mColor);
                }
            }
        }
    }

    protected void initColorfulLayer(ChildDrawable layer) {
        if (layer.mId == R.id.mask) {
            layer.mDrawable.setTint(mColor);
        }
    }
}
