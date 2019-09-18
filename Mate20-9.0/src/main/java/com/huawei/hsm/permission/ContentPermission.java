package com.huawei.hsm.permission;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.huawei.hsm.permission.monitor.PermRecordHandler;

public class ContentPermission {
    private static final boolean SUPPORT_ACCESS_BROWSER_HISTORY_MONITOR = SystemProperties.getBoolean("ro.config.browser_history_perm", false);
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
            Slog.w(TAG, "getContentType, uri==null");
            return 0;
        }
        String auth = uri.getAuthority();
        if (auth == null) {
            Slog.w(TAG, "getContentType, auth is null.");
            return 0;
        }
        if (auth.equals("com.android.contacts") || auth.equals("contacts")) {
            if (action == 1) {
                this.mPermissionType = 1;
            } else if (action == 2) {
                this.mPermissionType = 16384;
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
                this.mPermissionType = StubController.PERMISSION_CALENDAR;
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
            Slog.i(TAG, "browser mPermissionType = " + this.mPermissionType);
        }
        if (this.mPermissionType == 0 || StubController.checkPrecondition(this.mUid)) {
            return this.mPermissionType;
        }
        recordPermissionUsed();
        return 0;
    }

    private int getAccessBrowserHistoryPermType() {
        if (SUPPORT_ACCESS_BROWSER_HISTORY_MONITOR) {
            return StubController.PERMISSION_ACCESS_BROWSER_RECORDS;
        }
        return 0;
    }

    private boolean isGlobalSwitchOn(Cursor cursor) {
        return true;
    }

    private int remind() {
        return StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null);
    }

    private void recordPermissionUsed() {
        if (this.mPermissionType != 0) {
            PermRecordHandler mPermRecHandler = PermRecordHandler.getHandleInstance();
            if (mPermRecHandler != null) {
                mPermRecHandler.accessPermission(this.mUid, this.mPid, this.mPermissionType, null);
            }
        }
    }

    public static boolean allowContentOpInner(Uri uri, int action) {
        ContentPermission contentPermission = new ContentPermission();
        if (contentPermission.getContentType(uri, action) == 0) {
            return true;
        }
        if (!contentPermission.isGlobalSwitchOn(null)) {
            Slog.i(TAG, "isGlobalSwitchOn false");
            return true;
        }
        int selectionResult = contentPermission.remind();
        if (selectionResult == 0) {
            Slog.e(TAG, "Get selection error");
            return true;
        } else if (1 == selectionResult) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isBeginWithRightBrace(String selection) {
        if (selection == null || selection.equals("") || selection.indexOf(")") >= selection.indexOf("(")) {
            return false;
        }
        return true;
    }

    public static Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        IContentProvider unstableProvider = resolver.acquireUnstableProvider(uri);
        if (unstableProvider == null) {
            return null;
        }
        try {
            return getDummyCursorInner(resolver.getPackageName(), uri, projection, selection, selectionArgs, sortOrder, unstableProvider);
        } catch (RemoteException e) {
            return null;
        } finally {
            resolver.releaseProvider(unstableProvider);
        }
    }

    private static Cursor getDummyCursorInner(String pkgName, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, IContentProvider provider) throws RemoteException {
        if (projection == null) {
            return new MatrixCursor(new String[]{"_id"});
        }
        return new MatrixCursor(projection);
    }
}
