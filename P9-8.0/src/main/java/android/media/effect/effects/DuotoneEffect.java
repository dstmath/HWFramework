package android.media.effect.effects;

import android.filterpacks.imageproc.DuotoneFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class DuotoneEffect extends SingleFilterEffect {
    public DuotoneEffect(EffectContext context, String name) {
        super(context, name, DuotoneFilter.class, "image", "image", new Object[0]);
    }
}
