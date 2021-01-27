package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.BlackWhiteFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class BlackWhiteEffect extends SingleFilterEffect {
    public BlackWhiteEffect(EffectContext context, String name) {
        super(context, name, BlackWhiteFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
