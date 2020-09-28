package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.DuotoneFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class DuotoneEffect extends SingleFilterEffect {
    public DuotoneEffect(EffectContext context, String name) {
        super(context, name, DuotoneFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
