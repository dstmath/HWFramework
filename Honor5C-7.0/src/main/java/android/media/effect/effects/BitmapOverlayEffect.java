package android.media.effect.effects;

import android.filterpacks.imageproc.BitmapOverlayFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class BitmapOverlayEffect extends SingleFilterEffect {
    public BitmapOverlayEffect(EffectContext context, String name) {
        super(context, name, BitmapOverlayFilter.class, "image", "image", new Object[0]);
    }
}
