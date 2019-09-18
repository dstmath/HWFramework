package java.util.prefs;

import java.util.EventListener;

@FunctionalInterface
public interface PreferenceChangeListener extends EventListener {
    void preferenceChange(PreferenceChangeEvent preferenceChangeEvent);
}
