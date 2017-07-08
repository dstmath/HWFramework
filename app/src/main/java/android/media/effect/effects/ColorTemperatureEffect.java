package android.media.effect.effects;

import android.filterpacks.imageproc.ColorTemperatureFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class ColorTemperatureEffect extends SingleFilterEffect {
    public ColorTemperatureEffect(EffectContext context, String name) {
        super(context, name, ColorTemperatureFilter.class, "image", "image", new Object[0]);
    }
}
