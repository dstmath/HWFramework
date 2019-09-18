package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.SaturateFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class SaturateEffect extends SingleFilterEffect {
    public SaturateEffect(EffectContext context, String name) {
        super(context, name, SaturateFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
