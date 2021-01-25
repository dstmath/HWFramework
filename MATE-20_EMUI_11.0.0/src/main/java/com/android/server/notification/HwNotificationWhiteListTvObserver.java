package com.android.server.notification;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Slog;
import java.util.HashSet;
import java.util.Set;

public final class HwNotificationWhiteListTvObserver extends ContentObserver {
    private static final String NOTIFICATION_PACKAGE_NAME = "notification_pkg_name";
    private static final String NOTIFICATION_TABLE_NAME = "notification_manager";
    private static final Uri NOTIFICATION_WHITELIST_TV_URI = Uri.parse("content://com.huawei.tvsystemmanager.notification.provider/notification_manager");
    private static final String TAG = "HwNotificationWhiteListTvObserver";
    private static final String WHITE_LIST_AUTHORITY = "com.huawei.tvsystemmanager.notification.provider";
    private static final int WHITE_LIST_DEFAULT_SIZE = 16;
    private Context context;
    private boolean isObserverInitialized = false;
    private Set<String> whiteAppSetForTv = new HashSet(16);

    public HwNotificationWhiteListTvObserver(Context notificationContext, Handler handler) {
        super(handler);
        this.context = notificationContext;
    }

    public void observe() {
        if (!this.isObserverInitialized) {
            this.isObserverInitialized = true;
            this.context.getContentResolver().registerContentObserver(NOTIFICATION_WHITELIST_TV_URI, false, this, -1);
            Slog.i(TAG, "HwNotificationWhiteListTvObserver observe success");
        }
    }

    public void unObserve() {
        this.isObserverInitialized = false;
        this.context.getContentResolver().unregisterContentObserver(this);
    }

    public boolean isTvNoitficationWhiteApp(String packageName) {
        return this.whiteAppSetForTv.contains(packageName);
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange, Uri uri) {
        if (uri != null && NOTIFICATION_WHITELIST_TV_URI.equals(uri)) {
            updateNotificationWhiteListTv();
        }
    }

    private void updateNotificationWhiteListTv() {
        Cursor cursor = null;
        try {
            cursor = this.context.getContentResolver().query(NOTIFICATION_WHITELIST_TV_URI, new String[]{NOTIFICATION_PACKAGE_NAME}, null, null, null);
            this.whiteAppSetForTv.clear();
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String packageName = cursor.getString(cursor.getColumnIndex(NOTIFICATION_PACKAGE_NAME));
                    Slog.i(TAG, "updateNotificationWhiteListTv get packageName = " + packageName);
                    if (!TextUtils.isEmpty(packageName)) {
                        this.whiteAppSetForTv.add(packageName);
                    }
                }
            }
            if (cursor == null) {
                return;
            }
        } catch (SQLiteException e) {
            Slog.e(TAG, "updateNotificationWhiteListTv open database failed");
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
    }
}
