package android.media.effect.effects;

import android.filterpacks.imageproc.ToGrayFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class GrayscaleEffect extends SingleFilterEffect {
    public GrayscaleEffect(EffectContext context, String name) {
        super(context, name, ToGrayFilter.class, "image", "image", new Object[0]);
    }
}
