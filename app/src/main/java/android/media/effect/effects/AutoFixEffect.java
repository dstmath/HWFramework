package android.media.effect.effects;

import android.filterpacks.imageproc.AutoFixFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class AutoFixEffect extends SingleFilterEffect {
    public AutoFixEffect(EffectContext context, String name) {
        super(context, name, AutoFixFilter.class, "image", "image", new Object[0]);
    }
}
