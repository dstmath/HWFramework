package com.android.server.security.trustcircle.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.os.UserHandle;
import android.provider.Settings;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String getProperty(Context context, String key) {
        if (context == null) {
            return "";
        }
        return Settings.Global.getString(context.getContentResolver(), key);
    }

    public static void setProperty(Context context, String key, String value) {
        if (context != null) {
            Settings.Global.putString(context.getContentResolver(), key, value);
        }
    }

    public static int getCurrentUserId() {
        int userHandle = -10000;
        long origin = Binder.clearCallingIdentity();
        try {
            userHandle = ActivityManager.getCurrentUser();
        } catch (Exception e) {
            LogHelper.e(TAG, "error: exception in getCurrentUserId");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origin);
            throw th;
        }
        Binder.restoreCallingIdentity(origin);
        return userHandle;
    }

    public static boolean hasLoginAccount(Context context) {
        long origin = Binder.clearCallingIdentity();
        try {
            Account[] accs = AccountManager.get(context).getAccountsByTypeAsUser("com.huawei.hwid", UserHandle.of(getCurrentUserId()));
            if (accs != null && accs.length > 0) {
                Binder.restoreCallingIdentity(origin);
                return true;
            }
        } catch (Exception e) {
            LogHelper.e(TAG, "error: exception in hasLoginAccount");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origin);
            throw th;
        }
        Binder.restoreCallingIdentity(origin);
        return false;
    }
}
