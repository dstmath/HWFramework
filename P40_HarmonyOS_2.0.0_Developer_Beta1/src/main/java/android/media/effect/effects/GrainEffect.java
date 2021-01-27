package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.GrainFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class GrainEffect extends SingleFilterEffect {
    public GrainEffect(EffectContext context, String name) {
        super(context, name, GrainFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
