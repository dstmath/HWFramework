package android.media.effect.effects;

import android.filterpacks.imageproc.SharpenFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class SharpenEffect extends SingleFilterEffect {
    public SharpenEffect(EffectContext context, String name) {
        super(context, name, SharpenFilter.class, "image", "image", new Object[0]);
    }
}
