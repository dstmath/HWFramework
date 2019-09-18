package com.huawei.systemmanager.settingsearch;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.android.ManifestEx;

public abstract class HwSearchIndexablesProviderEx extends ContentProvider {
    private static final int MATCH_NON_INDEXABLE_KEYS_CODE = 3;
    private static final int MATCH_RAW_CODE = 2;
    private static final int MATCH_RES_CODE = 1;
    private UriMatcher mMatcher;

    public abstract Cursor queryNonIndexableKeys(String[] strArr);

    public abstract Cursor queryRawData(String[] strArr);

    public abstract Cursor queryXmlResources(String[] strArr);

    public boolean onCreate() {
        return true;
    }

    public void attachInfo(Context context, ProviderInfo info) {
        String mAuthority = info.authority;
        this.mMatcher = new UriMatcher(-1);
        this.mMatcher.addURI(mAuthority, "settings/indexables_xml_res", 1);
        this.mMatcher.addURI(mAuthority, "settings/indexables_raw", 2);
        this.mMatcher.addURI(mAuthority, "settings/non_indexables_key", 3);
        if (!info.exported) {
            throw new SecurityException("Provider must be exported");
        } else if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grantUriPermissions");
        } else if (ManifestEx.permission.READ_SEARCH_INDEXABLES.equals(info.readPermission)) {
            super.attachInfo(context, info);
        } else {
            throw new SecurityException("Provider must be protected by READ_SEARCH_INDEXABLES");
        }
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (this.mMatcher == null) {
            return null;
        }
        switch (this.mMatcher.match(uri)) {
            case 1:
                return queryXmlResources(null);
            case 2:
                return queryRawData(null);
            case 3:
                return queryNonIndexableKeys(null);
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
    }

    public String getType(Uri uri) {
        if (this.mMatcher == null) {
            return null;
        }
        switch (this.mMatcher.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/indexables_xml_res";
            case 2:
                return "vnd.android.cursor.dir/indexables_raw";
            case 3:
                return "vnd.android.cursor.dir/non_indexables_key";
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported");
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }
}
