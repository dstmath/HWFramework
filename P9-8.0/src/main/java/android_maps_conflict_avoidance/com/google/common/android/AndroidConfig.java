package android_maps_conflict_avoidance.com.google.common.android;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android_maps_conflict_avoidance.com.google.common.Clock;
import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.graphics.FontFactory;
import android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidAshmemImageFactory;
import android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidFontFactory;
import android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidImageFactory;
import android_maps_conflict_avoidance.com.google.common.io.Gunzipper;
import android_maps_conflict_avoidance.com.google.common.io.Gunzipper.GunzipInterface;
import android_maps_conflict_avoidance.com.google.common.io.InMemoryPersistentStore;
import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import android_maps_conflict_avoidance.com.google.common.io.android.AndroidFixedPersistentStore;
import android_maps_conflict_avoidance.com.google.common.io.android.AndroidHttpConnectionFactory;
import android_maps_conflict_avoidance.com.google.common.io.android.AndroidPersistentStore;
import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class AndroidConfig extends Config {
    private static Thread uiThread;
    private final Clock clock;
    protected AndroidHttpConnectionFactory connectionFactory;
    protected final Context context;
    protected FontFactory fontFactory;
    protected AndroidImageFactory imageFactory;
    protected PersistentStore persistentStore;
    private final int pixelsPerInch;

    public AndroidConfig(Context context) {
        this(context, null);
    }

    public AndroidConfig(Context context, String basePath) {
        this.clock = new AndroidClock();
        this.context = context;
        uiThread = Thread.currentThread();
        Config.setConfig(this);
        init();
        USE_NATIVE_COMMANDS = true;
        USE_NATIVE_MENUS = true;
        KEY_BACK = 4;
        if (context == null) {
            this.pixelsPerInch = 160;
        } else {
            this.pixelsPerInch = context.getResources().getDisplayMetrics().densityDpi;
        }
        initPortabilityFields(basePath);
        initLocale(Locale.getDefault());
    }

    protected void initPortabilityFields(String basePath) {
        if (this.context == null) {
            this.persistentStore = new InMemoryPersistentStore();
        } else if (basePath == null) {
            this.persistentStore = new AndroidPersistentStore(this.context);
        } else {
            this.persistentStore = new AndroidFixedPersistentStore(basePath);
        }
        this.connectionFactory = new AndroidHttpConnectionFactory(this.context);
        this.fontFactory = new AndroidFontFactory();
        this.imageFactory = new AndroidAshmemImageFactory(this.context);
    }

    private void initLocale(Locale locale) {
        String localeString = locale.toString();
        getI18n().setSystemLocale(localeString);
        getI18n().setUiLocale(localeString);
    }

    public String getAppProperty(String key) {
        return null;
    }

    protected String getDistributionChannelInternal() {
        String defaultValue = "Web";
        String donutValue = getSetting("maps_client_id");
        if (!TextUtil.isEmpty(donutValue)) {
            return donutValue;
        }
        String cupcakeValue = getSetting("client_id");
        return !TextUtil.isEmpty(cupcakeValue) ? "gmm-" + cupcakeValue : "Web";
    }

    private String getSetting(String key) {
        try {
            Cursor cursor = this.context.getContentResolver().query(Uri.parse("content://com.google.settings/partner"), new String[]{"value"}, "name='" + key + "'", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("value"));
            }
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    public AndroidHttpConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    public PersistentStore getPersistentStore() {
        return this.persistentStore;
    }

    public AndroidImageFactory getImageFactory() {
        return this.imageFactory;
    }

    protected void setupGzipper() {
        Gunzipper.setImplementation(new GunzipInterface() {
            public InputStream gunzip(InputStream source) throws IOException {
                return new GZIPInputStream(source);
            }
        });
    }

    public int getPixelsPerInch() {
        return this.pixelsPerInch;
    }

    public Clock getClock() {
        return this.clock;
    }

    public InputStream getInflaterInputStream(InputStream source) throws IOException {
        return new InflaterInputStream(source, new Inflater(true));
    }
}
