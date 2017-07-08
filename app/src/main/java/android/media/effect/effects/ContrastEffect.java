package android.media.effect.effects;

import android.filterpacks.imageproc.ContrastFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class ContrastEffect extends SingleFilterEffect {
    public ContrastEffect(EffectContext context, String name) {
        super(context, name, ContrastFilter.class, "image", "image", new Object[0]);
    }
}
