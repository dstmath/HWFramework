package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.NegativeFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class NegativeEffect extends SingleFilterEffect {
    public NegativeEffect(EffectContext context, String name) {
        super(context, name, NegativeFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
