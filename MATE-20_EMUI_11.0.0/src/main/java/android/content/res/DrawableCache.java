package android.content.res;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/* access modifiers changed from: package-private */
public class DrawableCache extends ThemedResourceCache<Drawable.ConstantState> {
    DrawableCache() {
    }

    @UnsupportedAppUsage
    public Drawable getInstance(long key, Resources resources, Resources.Theme theme) {
        Drawable.ConstantState entry = (Drawable.ConstantState) get(key, theme);
        if (entry != null) {
            return entry.newDrawable(resources, theme);
        }
        return null;
    }

    public boolean shouldInvalidateEntry(Drawable.ConstantState entry, int configChanges) {
        return Configuration.needNewResources(configChanges, entry.getChangingConfigurations());
    }
}
