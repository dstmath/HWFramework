package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.SharpenFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class SharpenEffect extends SingleFilterEffect {
    public SharpenEffect(EffectContext context, String name) {
        super(context, name, SharpenFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
