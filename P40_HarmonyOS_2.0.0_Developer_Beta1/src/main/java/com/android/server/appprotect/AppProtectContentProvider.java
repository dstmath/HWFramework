package com.android.server.appprotect;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.android.server.appactcontrol.AppActConstant;
import huawei.cust.HwCfgFilePolicy;

public class AppProtectContentProvider extends ContentProvider {
    private static final String APP_CONTROL = "appProtect";
    private static final int APP_CONTROL_ID = 1;
    private static final String APP_PROTECT_COMPATIBLE_VERSION_VALUE = "1";
    private static final String APP_PROTECT_SUBTYPE_COLUMN_VALUE = "generic";
    private static final String APP_PROTECT_TYPE = "APPPROTECT";
    private static final String AUTHORITY = "com.android.server.appprotect.appprotectprovider";
    private static final String[] CURSOR_COLUMN_KEY = {"type", AppActConstant.VERSION, "subtype", "compatibleVersion"};
    private static final String DEFAULT_VERSION = "1.0.0.0";
    private static final String PKG_HWOUC = "com.huawei.android.hwouc";
    private static final String TAG = "AppProtectContentProvider";
    private static final UriMatcher URL_MATCHER = new UriMatcher(-1);

    static {
        URL_MATCHER.addURI(AUTHORITY, APP_CONTROL, 1);
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String callingPackage = getCallingPackage();
        if (!"com.huawei.android.hwouc".equals(callingPackage)) {
            Log.e(TAG, "invalid caller:" + callingPackage);
            return null;
        } else if (URL_MATCHER.match(uri) != 1) {
            return null;
        } else {
            String[] xmlDir = HwCfgFilePolicy.getDownloadCfgFile(AppProtectActionConstant.CFG_FILE_DIR, AppProtectActionConstant.CFG_FILE_PATH);
            String versionStr = DEFAULT_VERSION;
            if (xmlDir != null) {
                versionStr = xmlDir[1];
            }
            MatrixCursor dataCursor = new MatrixCursor(CURSOR_COLUMN_KEY);
            dataCursor.addRow(new Object[]{APP_PROTECT_TYPE, versionStr, APP_PROTECT_SUBTYPE_COLUMN_VALUE, "1"});
            return dataCursor;
        }
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return null;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
