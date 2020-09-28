package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.CrossProcessFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class CrossProcessEffect extends SingleFilterEffect {
    public CrossProcessEffect(EffectContext context, String name) {
        super(context, name, CrossProcessFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
