package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.ToGrayFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class GrayscaleEffect extends SingleFilterEffect {
    public GrayscaleEffect(EffectContext context, String name) {
        super(context, name, ToGrayFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
