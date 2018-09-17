package android.content.res;

import android.content.res.Resources.Theme;

public class ConfigurationBoundResourceCache<T> extends ThemedResourceCache<ConstantState<T>> {
    public T getInstance(long key, Resources resources, Theme theme) {
        ConstantState<T> entry = (ConstantState) get(key, theme);
        if (entry != null) {
            return entry.newInstance(resources, theme);
        }
        return null;
    }

    public boolean shouldInvalidateEntry(ConstantState<T> entry, int configChanges) {
        return Configuration.needNewResources(configChanges, entry.getChangingConfigurations());
    }
}
