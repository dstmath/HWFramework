package com.android.server.location;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Secure;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public final class LocationBlacklist extends ContentObserver {
    private static final String BLACKLIST_CONFIG_NAME = "locationPackagePrefixBlacklist";
    private static final boolean D = false;
    private static final String TAG = "LocationBlacklist";
    private static final String WHITELIST_CONFIG_NAME = "locationPackagePrefixWhitelist";
    private String[] mBlacklist;
    private final Context mContext;
    private int mCurrentUserId;
    private final Object mLock;
    private String[] mWhitelist;

    public LocationBlacklist(Context context, Handler handler) {
        super(handler);
        this.mLock = new Object();
        this.mWhitelist = new String[0];
        this.mBlacklist = new String[0];
        this.mCurrentUserId = 0;
        this.mContext = context;
    }

    public void init() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(BLACKLIST_CONFIG_NAME), D, this, -1);
        reloadBlacklist();
    }

    private void reloadBlacklistLocked() {
        this.mWhitelist = getStringArrayLocked(WHITELIST_CONFIG_NAME);
        this.mBlacklist = getStringArrayLocked(BLACKLIST_CONFIG_NAME);
    }

    private void reloadBlacklist() {
        synchronized (this.mLock) {
            reloadBlacklistLocked();
        }
    }

    public boolean isBlacklisted(String packageName) {
        synchronized (this.mLock) {
            String[] strArr = this.mBlacklist;
            int length = strArr.length;
            int i = 0;
            while (i < length) {
                if (!packageName.startsWith(strArr[i]) || inWhitelist(packageName)) {
                    i++;
                } else {
                    return true;
                }
            }
            return D;
        }
    }

    private boolean inWhitelist(String pkg) {
        synchronized (this.mLock) {
            for (String white : this.mWhitelist) {
                if (pkg.startsWith(white)) {
                    return true;
                }
            }
            return D;
        }
    }

    public void onChange(boolean selfChange) {
        reloadBlacklist();
    }

    public void switchUser(int userId) {
        synchronized (this.mLock) {
            this.mCurrentUserId = userId;
            reloadBlacklistLocked();
        }
    }

    private String[] getStringArrayLocked(String key) {
        int i = 0;
        synchronized (this.mLock) {
            String flatString = Secure.getStringForUser(this.mContext.getContentResolver(), key, this.mCurrentUserId);
        }
        if (flatString == null) {
            return new String[0];
        }
        String[] splitStrings = flatString.split(",");
        ArrayList<String> result = new ArrayList();
        int length = splitStrings.length;
        while (i < length) {
            String pkg = splitStrings[i].trim();
            if (!pkg.isEmpty()) {
                result.add(pkg);
            }
            i++;
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public void dump(PrintWriter pw) {
        pw.println("mWhitelist=" + Arrays.toString(this.mWhitelist) + " mBlacklist=" + Arrays.toString(this.mBlacklist));
    }
}
