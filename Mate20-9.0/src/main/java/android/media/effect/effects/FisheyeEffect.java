package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.FisheyeFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class FisheyeEffect extends SingleFilterEffect {
    public FisheyeEffect(EffectContext context, String name) {
        super(context, name, FisheyeFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
