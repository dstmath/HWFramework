package com.huawei.server.rme.hyperhold;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Slog;
import com.android.server.appactcontrol.AppActConstant;
import huawei.cust.HwCfgFilePolicy;

public class HyperHoldCfgUpdateProvider extends ContentProvider {
    private static final String AUTHORITY = "com.huawei.server.rme.hyperhold.hyperholdcfgupdateprovider";
    private static final String COMPATIBLE_VERSION = "1";
    private static final String[] CURSOR_COLUMN_KEY = {"type", AppActConstant.VERSION, "subtype", "compatibleVersion"};
    private static final String DEFAULT_VERSION = "1.0.0.0";
    private static final String HWOUC_DIR = "/HYPERHOLD";
    private static final String HWOUC_PATH = "/HYPERHOLD/hyperhold_config.xml";
    private static final int HYPERHOLD_ID = 1;
    private static final String PKG_HWOUC = "com.huawei.android.hwouc";
    private static final String SUBTYPE = "generic";
    private static final String TAG = "HyperHold_HWOUC_Provider";
    private static final String TYPE = "HYPERHOLD";
    private static final String TYPE_URI = "hyperhold";
    private static final UriMatcher URL_MATCHER = new UriMatcher(-1);

    static {
        URL_MATCHER.addURI(AUTHORITY, TYPE_URI, 1);
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String callingPackage = getCallingPackage();
        if (!"com.huawei.android.hwouc".equals(callingPackage)) {
            Slog.e(TAG, "invalid caller:" + callingPackage);
            return null;
        } else if (URL_MATCHER.match(uri) != 1) {
            return null;
        } else {
            String[] configInfos = HwCfgFilePolicy.getDownloadCfgFile(HWOUC_DIR, HWOUC_PATH);
            String version = DEFAULT_VERSION;
            if (configInfos != null && configInfos.length > 1) {
                version = configInfos[1];
            }
            Slog.i(TAG, "the preset version is " + version);
            MatrixCursor dataCursor = new MatrixCursor(CURSOR_COLUMN_KEY);
            dataCursor.addRow(new Object[]{TYPE, version, SUBTYPE, "1"});
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
