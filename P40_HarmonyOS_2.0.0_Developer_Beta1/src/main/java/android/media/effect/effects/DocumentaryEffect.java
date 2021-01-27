package android.media.effect.effects;

import android.app.slice.SliceItem;
import android.filterpacks.imageproc.DocumentaryFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class DocumentaryEffect extends SingleFilterEffect {
    public DocumentaryEffect(EffectContext context, String name) {
        super(context, name, DocumentaryFilter.class, SliceItem.FORMAT_IMAGE, SliceItem.FORMAT_IMAGE, new Object[0]);
    }
}
