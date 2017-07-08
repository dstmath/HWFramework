package android.media.effect.effects;

import android.filterpacks.imageproc.FillLightFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class FillLightEffect extends SingleFilterEffect {
    public FillLightEffect(EffectContext context, String name) {
        super(context, name, FillLightFilter.class, "image", "image", new Object[0]);
    }
}
