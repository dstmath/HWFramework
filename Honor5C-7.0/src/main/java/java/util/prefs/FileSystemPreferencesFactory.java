package java.util.prefs;

class FileSystemPreferencesFactory implements PreferencesFactory {
    FileSystemPreferencesFactory() {
    }

    public Preferences userRoot() {
        return FileSystemPreferences.getUserRoot();
    }

    public Preferences systemRoot() {
        return FileSystemPreferences.getSystemRoot();
    }
}
