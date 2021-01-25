package android.util;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public abstract class KeyValueSettingObserver {
    private static final String TAG = "KeyValueSettingObserver";
    private final ContentObserver mObserver;
    private final KeyValueListParser mParser = new KeyValueListParser(',');
    private final ContentResolver mResolver;
    private final Uri mSettingUri;

    public abstract String getSettingValue(ContentResolver contentResolver);

    public abstract void update(KeyValueListParser keyValueListParser);

    public KeyValueSettingObserver(Handler handler, ContentResolver resolver, Uri uri) {
        this.mObserver = new SettingObserver(handler);
        this.mResolver = resolver;
        this.mSettingUri = uri;
    }

    public void start() {
        this.mResolver.registerContentObserver(this.mSettingUri, false, this.mObserver);
        setParserValue();
        update(this.mParser);
    }

    public void stop() {
        this.mResolver.unregisterContentObserver(this.mObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setParserValue() {
        String setting = getSettingValue(this.mResolver);
        try {
            this.mParser.setString(setting);
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Malformed setting: " + setting);
        }
    }

    private class SettingObserver extends ContentObserver {
        private SettingObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            KeyValueSettingObserver.this.setParserValue();
            KeyValueSettingObserver keyValueSettingObserver = KeyValueSettingObserver.this;
            keyValueSettingObserver.update(keyValueSettingObserver.mParser);
        }
    }
}
