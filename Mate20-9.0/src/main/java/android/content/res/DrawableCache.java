package android.content.res;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

class DrawableCache extends ThemedResourceCache<Drawable.ConstantState> {
    DrawableCache() {
    }

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
