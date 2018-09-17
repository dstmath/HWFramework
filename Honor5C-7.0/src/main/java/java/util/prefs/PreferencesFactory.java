package java.util.prefs;

public interface PreferencesFactory {
    Preferences systemRoot();

    Preferences userRoot();
}
