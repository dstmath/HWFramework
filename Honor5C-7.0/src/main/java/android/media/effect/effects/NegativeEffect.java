package android.media.effect.effects;

import android.filterpacks.imageproc.NegativeFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class NegativeEffect extends SingleFilterEffect {
    public NegativeEffect(EffectContext context, String name) {
        super(context, name, NegativeFilter.class, "image", "image", new Object[0]);
    }
}
