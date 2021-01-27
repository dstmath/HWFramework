package com.android.server.vr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.ArraySet;
import java.util.Objects;
import java.util.Set;

public class SettingsObserver {
    private final ContentObserver mContentObserver;
    private final String mSecureSettingName;
    private final BroadcastReceiver mSettingRestoreReceiver;
    private final Set<SettingChangeListener> mSettingsListeners = new ArraySet();

    public interface SettingChangeListener {
        void onSettingChanged();

        void onSettingRestored(String str, String str2, int i);
    }

    private SettingsObserver(Context context, Handler handler, final Uri settingUri, final String secureSettingName) {
        this.mSecureSettingName = secureSettingName;
        this.mSettingRestoreReceiver = new BroadcastReceiver() {
            /* class com.android.server.vr.SettingsObserver.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.os.action.SETTING_RESTORED".equals(intent.getAction()) && Objects.equals(intent.getStringExtra("setting_name"), secureSettingName)) {
                    SettingsObserver.this.sendSettingRestored(intent.getStringExtra("previous_value"), intent.getStringExtra("new_value"), getSendingUserId());
                }
            }
        };
        this.mContentObserver = new ContentObserver(handler) {
            /* class com.android.server.vr.SettingsObserver.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange, Uri uri) {
                if (uri == null || settingUri.equals(uri)) {
                    SettingsObserver.this.sendSettingChanged();
                }
            }
        };
        context.getContentResolver().registerContentObserver(settingUri, false, this.mContentObserver, -1);
    }

    public static SettingsObserver build(Context context, Handler handler, String settingName) {
        return new SettingsObserver(context, handler, Settings.Secure.getUriFor(settingName), settingName);
    }

    public void addListener(SettingChangeListener listener) {
        this.mSettingsListeners.add(listener);
    }

    public void removeListener(SettingChangeListener listener) {
        this.mSettingsListeners.remove(listener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSettingChanged() {
        for (SettingChangeListener l : this.mSettingsListeners) {
            l.onSettingChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSettingRestored(String prevValue, String newValue, int userId) {
        for (SettingChangeListener l : this.mSettingsListeners) {
            l.onSettingRestored(prevValue, newValue, userId);
        }
    }
}
