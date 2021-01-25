package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.TintFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class TintEffect extends SingleFilterEffect {
    public TintEffect(EffectContext context, String name) {
        super(context, name, TintFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
