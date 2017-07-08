package android.media.effect.effects;

import android.filterpacks.imageproc.SaturateFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class SaturateEffect extends SingleFilterEffect {
    public SaturateEffect(EffectContext context, String name) {
        super(context, name, SaturateFilter.class, "image", "image", new Object[0]);
    }
}
