package android.media.effect.effects;

import android.filterpacks.imageproc.CrossProcessFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class CrossProcessEffect extends SingleFilterEffect {
    public CrossProcessEffect(EffectContext context, String name) {
        super(context, name, CrossProcessFilter.class, "image", "image", new Object[0]);
    }
}
