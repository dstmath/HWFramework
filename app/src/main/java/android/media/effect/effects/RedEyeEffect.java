package android.media.effect.effects;

import android.filterpacks.imageproc.RedEyeFilter;
import android.media.effect.EffectContext;
import android.media.effect.SingleFilterEffect;

public class RedEyeEffect extends SingleFilterEffect {
    public RedEyeEffect(EffectContext context, String name) {
        super(context, name, RedEyeFilter.class, "image", "image", new Object[0]);
    }
}
