package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.VignetteFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class VignetteEffect extends SingleFilterEffect {
    public VignetteEffect(EffectContext context, String name) {
        super(context, name, VignetteFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
