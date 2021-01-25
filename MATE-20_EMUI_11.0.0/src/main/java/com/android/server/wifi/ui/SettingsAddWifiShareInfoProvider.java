package com.android.server.wifi.ui;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;

public class SettingsAddWifiShareInfoProvider extends ContentProvider {
    private static final String CHECK_ON_SWITCH_CHANGE_METHOD = "onSwitchChange";
    private static final String CHECK_SEARCH_SUPPORT_METHOD = "checkResIsSupportToSearch";
    private static final String CHECK_SUPPORT_METHOD = "checkMenuIsSupportToShow";
    private static final String IS_SUPPORT = "IS_SUPPORT";
    private static final String KEY_WIFI_CONFIGURATION_SYNC = "wifi_configuration_sync";
    private static final String SWITCH_CHANGE_RESULT = "SWITCH_CHANGE_RESULT";
    private static final String SWITCH_IS_CHECKED = "SWITCH_IS_CHECKED";
    private static final String TAG = "SettingsAddWifiShareInfoProvider";

    @Override // android.content.ContentProvider
    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public final String getType(Uri uri) {
        return null;
    }

    @Override // android.content.ContentProvider
    public final Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override // android.content.ContentProvider
    public final boolean onCreate() {
        return false;
    }

    @Override // android.content.ContentProvider
    public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override // android.content.ContentProvider
    public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public Bundle call(String method, String arg, Bundle extras) {
        HwHiLog.d(TAG, false, "call method = " + method, new Object[0]);
        if (TextUtils.isEmpty(arg)) {
            return null;
        }
        if (CHECK_SEARCH_SUPPORT_METHOD.equals(method)) {
            Bundle result = new Bundle();
            result.putBoolean(IS_SUPPORT, isResSupportToSearch(arg));
            return result;
        } else if (CHECK_SUPPORT_METHOD.equals(method)) {
            Bundle result2 = new Bundle();
            result2.putBoolean(IS_SUPPORT, isMenuSupportToShow(arg));
            result2.putBoolean(SWITCH_IS_CHECKED, isSwitchChecked(arg));
            return result2;
        } else if (CHECK_ON_SWITCH_CHANGE_METHOD.equals(method)) {
            Bundle result3 = new Bundle();
            result3.putBoolean(SWITCH_CHANGE_RESULT, isSwitchStatusChanged(arg, extras));
            return result3;
        } else {
            HwHiLog.d(TAG, false, "other type,no action is required.", new Object[0]);
            return null;
        }
    }

    private boolean isSwitchStatusChanged(String title, Bundle extras) {
        if (extras == null || !extras.containsKey(SWITCH_IS_CHECKED)) {
            return false;
        }
        try {
            Settings.System.putInt(getContext().getContentResolver(), KEY_WIFI_CONFIGURATION_SYNC, extras.getBoolean(SWITCH_IS_CHECKED) ? 1 : 0);
            return true;
        } catch (BadParcelableException e) {
            HwHiLog.e(TAG, false, "put value error", new Object[0]);
            return false;
        }
    }

    private boolean isSwitchChecked(String title) {
        if (Settings.System.getInt(getContext().getContentResolver(), KEY_WIFI_CONFIGURATION_SYNC, 0) == 1) {
            return true;
        }
        return false;
    }

    private boolean isResSupportToSearch(String title) {
        return isMenuSupportToShow(title);
    }

    private boolean isMenuSupportToShow(String title) {
        int value = SystemProperties.getInt("hw_mc.multiscreen.padcollaboration.value", 0);
        if (!"tablet".equals(SystemProperties.get("ro.build.characteristics", "")) || (value & 3) == 0) {
            return false;
        }
        return true;
    }
}
