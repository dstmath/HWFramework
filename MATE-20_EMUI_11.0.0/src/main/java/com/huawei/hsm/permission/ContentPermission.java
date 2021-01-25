package com.huawei.hsm.permission;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartsecurity.BuildConfig;

public class ContentPermission {
    private static final boolean SUPPORT_ACCESS_BROWSER_HISTORY_MONITOR = SystemPropertiesEx.getBoolean("ro.config.browser_history_perm", false);
    private static final String TAG = "ContentPermission";
    private int mPermissionType = 0;
    private int mPid = Binder.getCallingPid();
    private int mUid = Binder.getCallingUid();

    public ContentPermission() {
    }

    public ContentPermission(Context context) {
    }

    public int getContentType(Uri uri, int action) {
        if (uri == null) {
            SlogEx.w(TAG, "getContentType, uri==null");
            return 0;
        }
        String auth = uri.getAuthority();
        if (auth == null) {
            SlogEx.w(TAG, "getContentType, auth is null.");
            return 0;
        }
        if (auth.equals("com.android.contacts") || auth.equals("contacts")) {
            if (action == 1) {
                this.mPermissionType = 1;
            } else if (action == 2) {
                this.mPermissionType = StubController.PERMISSION_CONTACTS_WRITE;
            } else if (action == 3) {
                this.mPermissionType = StubController.PERMISSION_CONTACTS_DELETE;
            } else {
                this.mPermissionType = 0;
            }
        } else if (auth.equals("call_log")) {
            if (action == 1) {
                this.mPermissionType = 2;
            } else if (action == 2) {
                this.mPermissionType = StubController.PERMISSION_CALLLOG_WRITE;
            } else if (action == 3) {
                this.mPermissionType = StubController.PERMISSION_CALLLOG_DELETE;
            } else {
                this.mPermissionType = 0;
            }
        } else if (auth.equals("sms") || auth.equals("mms-sms") || auth.equals("mms")) {
            if (action == 1) {
                this.mPermissionType = 4;
            } else {
                this.mPermissionType = 0;
            }
        } else if (auth.equals("com.android.calendar")) {
            if (action == 1) {
                this.mPermissionType = 2048;
            } else if (action == 2) {
                this.mPermissionType = 268435456;
            } else if (action == 3) {
                this.mPermissionType = 268435456;
            } else {
                this.mPermissionType = 0;
            }
        } else if (auth.equals("browser") || auth.equals("com.android.browser")) {
            if (action == 1) {
                this.mPermissionType = getAccessBrowserHistoryPermType();
            } else {
                this.mPermissionType = 0;
            }
            SlogEx.i(TAG, "browser mPermissionType = " + this.mPermissionType);
        }
        if (this.mPermissionType == 0 || StubController.checkPrecondition(this.mUid)) {
            return this.mPermissionType;
        }
        return 0;
    }

    private int getAccessBrowserHistoryPermType() {
        return SUPPORT_ACCESS_BROWSER_HISTORY_MONITOR ? 1073741824 : 0;
    }

    private boolean isGlobalSwitchOn(Cursor cursor) {
        return true;
    }

    private int remind() {
        return StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null);
    }

    public static boolean allowContentOpInner(Uri uri, int action) {
        ContentPermission contentPermission = new ContentPermission();
        if (contentPermission.getContentType(uri, action) == 0) {
            return true;
        }
        if (!contentPermission.isGlobalSwitchOn(null)) {
            SlogEx.i(TAG, "isGlobalSwitchOn false");
            return true;
        }
        int selectionResult = contentPermission.remind();
        if (selectionResult == 0) {
            SlogEx.e(TAG, "Get selection error");
            return true;
        } else if (1 == selectionResult) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isBeginWithRightBrace(String selection) {
        if (selection == null || selection.equals(BuildConfig.FLAVOR) || selection.indexOf(")") >= selection.indexOf("(")) {
            return false;
        }
        return true;
    }

    public static Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getDummyCursorInner(uri, projection, selection, selectionArgs, sortOrder);
    }

    private static Cursor getDummyCursorInner(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (projection == null) {
            return new MatrixCursor(new String[]{"_id"});
        }
        return new MatrixCursor(projection);
    }
}
