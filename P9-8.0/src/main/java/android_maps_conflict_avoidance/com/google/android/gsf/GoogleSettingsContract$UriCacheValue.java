package android_maps_conflict_avoidance.com.google.android.gsf;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class GoogleSettingsContract$UriCacheValue {
    AtomicBoolean invalidateCache = new AtomicBoolean(false);
    HashMap<String, String> valueCache = new HashMap();
    Object versionToken = new Object();

    GoogleSettingsContract$UriCacheValue() {
    }
}
