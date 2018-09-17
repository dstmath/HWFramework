package android_maps_conflict_avoidance.com.google.android.gsf;

import android.content.ContentResolver;
import android.net.Uri;

public final class GoogleSettingsContract$Partner extends GoogleSettingsContract$NameValueTable {
    public static final Uri CONTENT_URI = Uri.parse("content://com.google.settings/partner");

    public static String getString(ContentResolver resolver, String name) {
        return GoogleSettingsContract$NameValueTable.getString(resolver, CONTENT_URI, name);
    }

    public static String getString(ContentResolver resolver, String name, String defaultValue) {
        String value = getString(resolver, name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
