package android.media.effect.effects;

import android.filterpacks.imageproc.BrightnessFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class BrightnessEffect extends SingleFilterEffect {
    public BrightnessEffect(EffectContext context, String name) {
        super(context, name, BrightnessFilter.class, "image", "image", new Object[0]);
    }
}
