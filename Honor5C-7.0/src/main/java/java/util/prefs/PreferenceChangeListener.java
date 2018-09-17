package java.util.prefs;

import java.util.EventListener;

public interface PreferenceChangeListener extends EventListener {
    void preferenceChange(PreferenceChangeEvent preferenceChangeEvent);
}
