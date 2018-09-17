package android_maps_conflict_avoidance.com.google.common.io;

public abstract class BasePersistentStore implements PersistentStore {
    private final PreferenceStore preferenceStore = new PreferenceStore(this);

    public boolean setPreference(String name, byte[] data) {
        return this.preferenceStore.setPreference(name, data);
    }

    public byte[] readPreference(String name) {
        return this.preferenceStore.readPreference(name);
    }

    public void savePreferences() {
        this.preferenceStore.savePreferences();
    }
}
