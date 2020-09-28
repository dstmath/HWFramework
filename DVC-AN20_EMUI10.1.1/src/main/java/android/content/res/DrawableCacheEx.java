package android.content.res;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DrawableCacheEx {
    private DrawableCache mDrawableCache = new DrawableCache();

    public Drawable getInstance(long key, Resources resources, Resources.Theme theme) {
        return this.mDrawableCache.getInstance(key, resources, theme);
    }

    public void put(long key, Resources.Theme theme, Drawable.ConstantState entry) {
        this.mDrawableCache.put(key, theme, entry);
    }

    public void put(long key, Resources.Theme theme, Drawable.ConstantState entry, boolean isUsesTheme) {
        this.mDrawableCache.put(key, theme, entry);
    }

    public void onConfigurationChange(int configChanges) {
        this.mDrawableCache.onConfigurationChange(configChanges);
    }
}
