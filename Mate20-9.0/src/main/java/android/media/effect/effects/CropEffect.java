package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.CropRectFilter;
import android.media.effect.EffectContext;
import android.media.effect.SizeChangeEffect;

public class CropEffect extends SizeChangeEffect {
    public CropEffect(EffectContext context, String name) {
        super(context, name, CropRectFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
