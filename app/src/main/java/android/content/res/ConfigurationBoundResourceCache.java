package android.content.res;

import android.content.res.Resources.Theme;

public class ConfigurationBoundResourceCache<T> extends ThemedResourceCache<ConstantState<T>> {
    public /* bridge */ /* synthetic */ Object get(long key, Theme theme) {
        return super.get(key, theme);
    }

    public /* bridge */ /* synthetic */ void onConfigurationChange(int configChanges) {
        super.onConfigurationChange(configChanges);
    }

    public /* bridge */ /* synthetic */ void put(long key, Theme theme, Object entry) {
        super.put(key, theme, entry);
    }

    public /* bridge */ /* synthetic */ void put(long key, Theme theme, Object entry, boolean usesTheme) {
        super.put(key, theme, entry, usesTheme);
    }

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
