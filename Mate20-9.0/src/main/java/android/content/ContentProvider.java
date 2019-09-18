package android.content;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.backup.FullBackup;
import android.common.HwFrameworkFactory;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.Trace;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public abstract class ContentProvider implements ComponentCallbacks2 {
    private static final String TAG = "ContentProvider";
    private String[] mAuthorities;
    private String mAuthority;
    private final ThreadLocal<String> mCallingPackage = new ThreadLocal<>();
    private Context mContext = null;
    private boolean mExported;
    private int mMyUid;
    private boolean mNoPerms;
    private PathPermission[] mPathPermissions;
    private String mReadPermission;
    private boolean mSingleUser;
    private Transport mTransport = new Transport();
    private String mWritePermission;

    public interface PipeDataWriter<T> {
        void writeDataToPipe(ParcelFileDescriptor parcelFileDescriptor, Uri uri, String str, Bundle bundle, T t);
    }

    class Transport extends ContentProviderNative {
        AppOpsManager mAppOpsManager = null;
        int mReadOp = -1;
        int mWriteOp = -1;

        Transport() {
        }

        /* access modifiers changed from: package-private */
        public ContentProvider getContentProvider() {
            return ContentProvider.this;
        }

        public String getProviderName() {
            return getContentProvider().getClass().getName();
        }

        public Cursor query(String callingPkg, Uri uri, String[] projection, Bundle queryArgs, ICancellationSignal cancellationSignal) {
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONTENTPROVIDER_QUERY, new Object[]{uri});
            }
            Uri uri2 = ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
            if (enforceReadPermission(callingPkg, uri2, null) == 0) {
                String original = ContentProvider.this.setCallingPackage(callingPkg);
                try {
                    return ContentProvider.this.query(uri2, projection, queryArgs, CancellationSignal.fromTransport(cancellationSignal));
                } finally {
                    String unused = ContentProvider.this.setCallingPackage(original);
                }
            } else if (projection != null) {
                return new MatrixCursor(projection, 0);
            } else {
                Cursor cursor = ContentProvider.this.query(uri2, projection, queryArgs, CancellationSignal.fromTransport(cancellationSignal));
                if (cursor == null) {
                    return null;
                }
                return new MatrixCursor(cursor.getColumnNames(), 0);
            }
        }

        public String getType(Uri uri) {
            return ContentProvider.this.getType(ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri)));
        }

        public Uri insert(String callingPkg, Uri uri, ContentValues initialValues) {
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONTENTPROVIDER_INSERT, new Object[]{uri});
            }
            Uri uri2 = ContentProvider.this.validateIncomingUri(uri);
            int userId = ContentProvider.getUserIdFromUri(uri2);
            Uri uri3 = ContentProvider.this.maybeGetUriWithoutUserId(uri2);
            if (enforceWritePermission(callingPkg, uri3, null) != 0) {
                return ContentProvider.this.rejectInsert(uri3, initialValues);
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.maybeAddUserId(ContentProvider.this.insert(uri3, initialValues), userId);
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public int bulkInsert(String callingPkg, Uri uri, ContentValues[] initialValues) {
            Uri uri2 = ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
            if (enforceWritePermission(callingPkg, uri2, null) != 0) {
                return 0;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.bulkInsert(uri2, initialValues);
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public ContentProviderResult[] applyBatch(String callingPkg, ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
            int numOperations = operations.size();
            int[] userIds = new int[numOperations];
            int i = 0;
            while (i < numOperations) {
                ContentProviderOperation operation = operations.get(i);
                Uri uri = operation.getUri();
                userIds[i] = ContentProvider.getUserIdFromUri(uri);
                Uri uri2 = ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
                if (!Objects.equals(operation.getUri(), uri2)) {
                    operation = new ContentProviderOperation(operation, uri2);
                    operations.set(i, operation);
                }
                if (operation.isReadOperation() && enforceReadPermission(callingPkg, uri2, null) != 0) {
                    throw new OperationApplicationException("App op not allowed", 0);
                } else if (!operation.isWriteOperation() || enforceWritePermission(callingPkg, uri2, null) == 0) {
                    i++;
                } else {
                    throw new OperationApplicationException("App op not allowed", 0);
                }
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                ContentProviderResult[] results = ContentProvider.this.applyBatch(operations);
                if (results != null) {
                    for (int i2 = 0; i2 < results.length; i2++) {
                        if (userIds[i2] != -2) {
                            results[i2] = new ContentProviderResult(results[i2], userIds[i2]);
                        }
                    }
                }
                return results;
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public int delete(String callingPkg, Uri uri, String selection, String[] selectionArgs) {
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONTENTPROVIDER_DELETE, new Object[]{uri});
            }
            Uri uri2 = ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
            if (enforceWritePermission(callingPkg, uri2, null) != 0) {
                return 0;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.delete(uri2, selection, selectionArgs);
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public int update(String callingPkg, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.CONTENTPROVIDER_UPDATE, new Object[]{uri});
            }
            Uri uri2 = ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
            if (enforceWritePermission(callingPkg, uri2, null) != 0) {
                return 0;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.update(uri2, values, selection, selectionArgs);
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public ParcelFileDescriptor openFile(String callingPkg, Uri uri, String mode, ICancellationSignal cancellationSignal, IBinder callerToken) throws FileNotFoundException {
            Uri uri2 = ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
            enforceFilePermission(callingPkg, uri2, mode, callerToken);
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.openFile(uri2, mode, CancellationSignal.fromTransport(cancellationSignal));
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public AssetFileDescriptor openAssetFile(String callingPkg, Uri uri, String mode, ICancellationSignal cancellationSignal) throws FileNotFoundException {
            Uri uri2 = ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
            enforceFilePermission(callingPkg, uri2, mode, null);
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.openAssetFile(uri2, mode, CancellationSignal.fromTransport(cancellationSignal));
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public Bundle call(String callingPkg, String method, String arg, Bundle extras) {
            Bundle.setDefusable(extras, true);
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.call(method, arg, extras);
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
            return ContentProvider.this.getStreamTypes(ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri)), mimeTypeFilter);
        }

        public AssetFileDescriptor openTypedAssetFile(String callingPkg, Uri uri, String mimeType, Bundle opts, ICancellationSignal cancellationSignal) throws FileNotFoundException {
            Bundle.setDefusable(opts, true);
            Uri uri2 = ContentProvider.this.maybeGetUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
            enforceFilePermission(callingPkg, uri2, FullBackup.ROOT_TREE_TOKEN, null);
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.openTypedAssetFile(uri2, mimeType, opts, CancellationSignal.fromTransport(cancellationSignal));
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public ICancellationSignal createCancellationSignal() {
            return CancellationSignal.createTransport();
        }

        public Uri canonicalize(String callingPkg, Uri uri) {
            Uri uri2 = ContentProvider.this.validateIncomingUri(uri);
            int userId = ContentProvider.getUserIdFromUri(uri2);
            Uri uri3 = ContentProvider.getUriWithoutUserId(uri2);
            if (enforceReadPermission(callingPkg, uri3, null) != 0) {
                return null;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.maybeAddUserId(ContentProvider.this.canonicalize(uri3), userId);
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public Uri uncanonicalize(String callingPkg, Uri uri) {
            Uri uri2 = ContentProvider.this.validateIncomingUri(uri);
            int userId = ContentProvider.getUserIdFromUri(uri2);
            Uri uri3 = ContentProvider.getUriWithoutUserId(uri2);
            if (enforceReadPermission(callingPkg, uri3, null) != 0) {
                return null;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.maybeAddUserId(ContentProvider.this.uncanonicalize(uri3), userId);
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        public boolean refresh(String callingPkg, Uri uri, Bundle args, ICancellationSignal cancellationSignal) throws RemoteException {
            Uri uri2 = ContentProvider.getUriWithoutUserId(ContentProvider.this.validateIncomingUri(uri));
            if (enforceReadPermission(callingPkg, uri2, null) != 0) {
                return false;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.refresh(uri2, args, CancellationSignal.fromTransport(cancellationSignal));
            } finally {
                String unused = ContentProvider.this.setCallingPackage(original);
            }
        }

        private void enforceFilePermission(String callingPkg, Uri uri, String mode, IBinder callerToken) throws FileNotFoundException, SecurityException {
            if (mode == null || mode.indexOf(119) == -1) {
                if (enforceReadPermission(callingPkg, uri, callerToken) != 0) {
                    throw new FileNotFoundException("App op not allowed");
                }
            } else if (enforceWritePermission(callingPkg, uri, callerToken) != 0) {
                throw new FileNotFoundException("App op not allowed");
            }
        }

        private int enforceReadPermission(String callingPkg, Uri uri, IBinder callerToken) throws SecurityException {
            int mode = ContentProvider.this.enforceReadPermissionInner(uri, callingPkg, callerToken);
            if (mode != 0) {
                return mode;
            }
            if (this.mReadOp != -1) {
                return this.mAppOpsManager.noteProxyOp(this.mReadOp, callingPkg);
            }
            return 0;
        }

        private int enforceWritePermission(String callingPkg, Uri uri, IBinder callerToken) throws SecurityException {
            int mode = ContentProvider.this.enforceWritePermissionInner(uri, callingPkg, callerToken);
            if (mode != 0) {
                return mode;
            }
            if (this.mWriteOp != -1) {
                return this.mAppOpsManager.noteProxyOp(this.mWriteOp, callingPkg);
            }
            return 0;
        }
    }

    public abstract int delete(Uri uri, String str, String[] strArr);

    public abstract String getType(Uri uri);

    public abstract Uri insert(Uri uri, ContentValues contentValues);

    public abstract boolean onCreate();

    public abstract Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2);

    public abstract int update(Uri uri, ContentValues contentValues, String str, String[] strArr);

    public ContentProvider() {
    }

    public ContentProvider(Context context, String readPermission, String writePermission, PathPermission[] pathPermissions) {
        this.mContext = context;
        this.mReadPermission = readPermission;
        this.mWritePermission = writePermission;
        this.mPathPermissions = pathPermissions;
    }

    public static ContentProvider coerceToLocalContentProvider(IContentProvider abstractInterface) {
        if (abstractInterface instanceof Transport) {
            return ((Transport) abstractInterface).getContentProvider();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean checkUser(int pid, int uid, Context context) {
        return UserHandle.getUserId(uid) == context.getUserId() || this.mSingleUser || context.checkPermission(Manifest.permission.INTERACT_ACROSS_USERS, pid, uid) == 0;
    }

    private int checkPermissionAndAppOp(String permission, String callingPkg, IBinder callerToken) {
        if (getContext().checkPermission(permission, Binder.getCallingPid(), Binder.getCallingUid(), callerToken) != 0) {
            return 2;
        }
        int permOp = AppOpsManager.permissionToOpCode(permission);
        if (permOp != -1) {
            return this.mTransport.mAppOpsManager.noteProxyOp(permOp, callingPkg);
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int enforceReadPermissionInner(Uri uri, String callingPkg, IBinder callerToken) throws SecurityException {
        String suffix;
        boolean allowDefaultRead;
        Uri uri2 = uri;
        String str = callingPkg;
        IBinder iBinder = callerToken;
        Context context = getContext();
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        String missingPerm = null;
        int strongestMode = 0;
        if (UserHandle.isSameApp(uid, this.mMyUid)) {
            return 0;
        }
        if (this.mExported && checkUser(pid, uid, context)) {
            String componentPerm = getReadPermission();
            if (componentPerm != null) {
                int mode = checkPermissionAndAppOp(componentPerm, str, iBinder);
                if (mode == 0) {
                    return 0;
                }
                missingPerm = componentPerm;
                strongestMode = Math.max(0, mode);
            }
            boolean allowDefaultRead2 = componentPerm == null;
            PathPermission[] pps = getPathPermissions();
            if (pps != null) {
                String path = uri.getPath();
                allowDefaultRead = allowDefaultRead2;
                int strongestMode2 = strongestMode;
                String missingPerm2 = missingPerm;
                for (PathPermission pp : pps) {
                    String pathPerm = pp.getReadPermission();
                    if (pathPerm == null || !pp.match(path)) {
                        missingPerm2 = missingPerm2;
                    } else {
                        String str2 = missingPerm2;
                        int mode2 = checkPermissionAndAppOp(pathPerm, str, iBinder);
                        if (mode2 == 0) {
                            return 0;
                        }
                        allowDefaultRead = false;
                        strongestMode2 = Math.max(strongestMode2, mode2);
                        missingPerm2 = pathPerm;
                    }
                }
                String str3 = missingPerm2;
                strongestMode = strongestMode2;
                missingPerm = str3;
            } else {
                allowDefaultRead = allowDefaultRead2;
            }
            if (allowDefaultRead) {
                return 0;
            }
        }
        int strongestMode3 = strongestMode;
        if (context.checkUriPermission((!this.mSingleUser || UserHandle.isSameUser(this.mMyUid, uid)) ? uri2 : maybeAddUserId(uri2, UserHandle.getUserId(uid)), pid, uid, 1, iBinder) == 0) {
            return 0;
        }
        if (strongestMode3 == 1) {
            return 1;
        }
        if (Manifest.permission.MANAGE_DOCUMENTS.equals(this.mReadPermission)) {
            suffix = " requires that you obtain access using ACTION_OPEN_DOCUMENT or related APIs";
        } else if (this.mExported) {
            suffix = " requires " + missingPerm + ", or grantUriPermission()";
        } else {
            suffix = " requires the provider be exported, or grantUriPermission()";
        }
        throw new SecurityException("Permission Denial: reading " + getClass().getName() + " uri " + uri2 + " from pid=" + pid + ", uid=" + uid + suffix);
    }

    /* access modifiers changed from: protected */
    public int enforceWritePermissionInner(Uri uri, String callingPkg, IBinder callerToken) throws SecurityException {
        String failReason;
        boolean allowDefaultWrite;
        String str = callingPkg;
        IBinder iBinder = callerToken;
        Context context = getContext();
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        String missingPerm = null;
        int strongestMode = 0;
        if (UserHandle.isSameApp(uid, this.mMyUid)) {
            return 0;
        }
        if (this.mExported && checkUser(pid, uid, context)) {
            String componentPerm = getWritePermission();
            if (componentPerm != null) {
                int mode = checkPermissionAndAppOp(componentPerm, str, iBinder);
                if (mode == 0) {
                    return 0;
                }
                missingPerm = componentPerm;
                strongestMode = Math.max(0, mode);
            }
            boolean allowDefaultWrite2 = componentPerm == null;
            PathPermission[] pps = getPathPermissions();
            if (pps != null) {
                String path = uri.getPath();
                allowDefaultWrite = allowDefaultWrite2;
                int strongestMode2 = strongestMode;
                String missingPerm2 = missingPerm;
                for (PathPermission pp : pps) {
                    String pathPerm = pp.getWritePermission();
                    if (pathPerm == null || !pp.match(path)) {
                        missingPerm2 = missingPerm2;
                    } else {
                        String str2 = missingPerm2;
                        int mode2 = checkPermissionAndAppOp(pathPerm, str, iBinder);
                        if (mode2 == 0) {
                            return 0;
                        }
                        allowDefaultWrite = false;
                        strongestMode2 = Math.max(strongestMode2, mode2);
                        missingPerm2 = pathPerm;
                    }
                }
                String str3 = missingPerm2;
                strongestMode = strongestMode2;
                missingPerm = str3;
            } else {
                allowDefaultWrite = allowDefaultWrite2;
            }
            if (allowDefaultWrite) {
                return 0;
            }
        }
        int strongestMode3 = strongestMode;
        if (context.checkUriPermission(uri, pid, uid, 2, iBinder) == 0) {
            return 0;
        }
        if (strongestMode3 == 1) {
            return 1;
        }
        if (this.mExported) {
            failReason = " requires " + missingPerm + ", or grantUriPermission()";
        } else {
            failReason = " requires the provider be exported, or grantUriPermission()";
        }
        throw new SecurityException("Permission Denial: writing " + getClass().getName() + " uri " + uri + " from pid=" + pid + ", uid=" + uid + failReason);
    }

    public final Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: private */
    public String setCallingPackage(String callingPackage) {
        String original = this.mCallingPackage.get();
        this.mCallingPackage.set(callingPackage);
        return original;
    }

    public final String getCallingPackage() {
        String pkg = this.mCallingPackage.get();
        if (pkg != null) {
            this.mTransport.mAppOpsManager.checkPackage(Binder.getCallingUid(), pkg);
        }
        return pkg;
    }

    /* access modifiers changed from: protected */
    public final void setAuthorities(String authorities) {
        if (authorities == null) {
            return;
        }
        if (authorities.indexOf(59) == -1) {
            this.mAuthority = authorities;
            this.mAuthorities = null;
            return;
        }
        this.mAuthority = null;
        this.mAuthorities = authorities.split(";");
    }

    /* access modifiers changed from: protected */
    public final boolean matchesOurAuthorities(String authority) {
        if (this.mAuthority != null) {
            return this.mAuthority.equals(authority);
        }
        if (this.mAuthorities != null) {
            for (String equals : this.mAuthorities) {
                if (equals.equals(authority)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public final void setReadPermission(String permission) {
        this.mReadPermission = permission;
    }

    public final String getReadPermission() {
        return this.mReadPermission;
    }

    /* access modifiers changed from: protected */
    public final void setWritePermission(String permission) {
        this.mWritePermission = permission;
    }

    public final String getWritePermission() {
        return this.mWritePermission;
    }

    /* access modifiers changed from: protected */
    public final void setPathPermissions(PathPermission[] permissions) {
        this.mPathPermissions = permissions;
    }

    public final PathPermission[] getPathPermissions() {
        return this.mPathPermissions;
    }

    public final void setAppOps(int readOp, int writeOp) {
        if (!this.mNoPerms) {
            this.mTransport.mReadOp = readOp;
            this.mTransport.mWriteOp = writeOp;
        }
    }

    public AppOpsManager getAppOpsManager() {
        return this.mTransport.mAppOpsManager;
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(int level) {
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        return query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Bundle queryArgs2 = queryArgs != null ? queryArgs : Bundle.EMPTY;
        String sortClause = queryArgs2.getString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER);
        if (sortClause == null && queryArgs2.containsKey(ContentResolver.QUERY_ARG_SORT_COLUMNS)) {
            sortClause = ContentResolver.createSqlSortClause(queryArgs2);
        }
        return query(uri, projection, queryArgs2.getString(ContentResolver.QUERY_ARG_SQL_SELECTION), queryArgs2.getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS), sortClause, cancellationSignal);
    }

    public Uri canonicalize(Uri url) {
        return null;
    }

    public Uri uncanonicalize(Uri url) {
        return url;
    }

    public boolean refresh(Uri uri, Bundle args, CancellationSignal cancellationSignal) {
        return false;
    }

    public Uri rejectInsert(Uri uri, ContentValues values) {
        return uri.buildUpon().appendPath("0").build();
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        for (ContentValues insert : values) {
            insert(uri, insert);
        }
        return numValues;
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        throw new FileNotFoundException("No files supported by provider at " + uri);
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        return openFile(uri, mode);
    }

    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        ParcelFileDescriptor fd = openFile(uri, mode);
        if (fd == null) {
            return null;
        }
        AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(fd, 0, -1);
        return assetFileDescriptor;
    }

    public AssetFileDescriptor openAssetFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        return openAssetFile(uri, mode);
    }

    /* access modifiers changed from: protected */
    public final ParcelFileDescriptor openFileHelper(Uri uri, String mode) throws FileNotFoundException {
        Cursor c = query(uri, new String[]{"_data"}, null, null, null);
        int count = c != null ? c.getCount() : 0;
        if (count != 1) {
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }
        c.moveToFirst();
        int i = c.getColumnIndex("_data");
        String path = i >= 0 ? c.getString(i) : null;
        c.close();
        if (path != null) {
            return ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.parseMode(mode));
        }
        throw new FileNotFoundException("Column _data not found.");
    }

    public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
        return null;
    }

    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts) throws FileNotFoundException {
        if ("*/*".equals(mimeTypeFilter)) {
            return openAssetFile(uri, FullBackup.ROOT_TREE_TOKEN);
        }
        String baseType = getType(uri);
        if (baseType != null && ClipDescription.compareMimeTypes(baseType, mimeTypeFilter)) {
            return openAssetFile(uri, FullBackup.ROOT_TREE_TOKEN);
        }
        throw new FileNotFoundException("Can't open " + uri + " as type " + mimeTypeFilter);
    }

    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws FileNotFoundException {
        return openTypedAssetFile(uri, mimeTypeFilter, opts);
    }

    public <T> ParcelFileDescriptor openPipeHelper(Uri uri, String mimeType, Bundle opts, T args, PipeDataWriter<T> func) throws FileNotFoundException {
        try {
            ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
            final PipeDataWriter<T> pipeDataWriter = func;
            final ParcelFileDescriptor[] parcelFileDescriptorArr = fds;
            final Uri uri2 = uri;
            final String str = mimeType;
            final Bundle bundle = opts;
            final T t = args;
            AnonymousClass1 task = new AsyncTask<Object, Object, Object>() {
                /* access modifiers changed from: protected */
                public Object doInBackground(Object... params) {
                    pipeDataWriter.writeDataToPipe(parcelFileDescriptorArr[1], uri2, str, bundle, t);
                    try {
                        parcelFileDescriptorArr[1].close();
                    } catch (IOException e) {
                        Log.w(ContentProvider.TAG, "Failure closing pipe", e);
                    }
                    return null;
                }
            };
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            return fds[0];
        } catch (IOException e) {
            throw new FileNotFoundException("failure making pipe");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isTemporary() {
        return false;
    }

    public IContentProvider getIContentProvider() {
        return this.mTransport;
    }

    public void attachInfoForTesting(Context context, ProviderInfo info) {
        attachInfo(context, info, true);
    }

    public void attachInfo(Context context, ProviderInfo info) {
        attachInfo(context, info, false);
    }

    private void attachInfo(Context context, ProviderInfo info, boolean testing) {
        this.mNoPerms = testing;
        if (this.mContext == null) {
            this.mContext = context;
            if (!(context == null || this.mTransport == null)) {
                this.mTransport.mAppOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            }
            this.mMyUid = Process.myUid();
            if (info != null) {
                setReadPermission(info.readPermission);
                setWritePermission(info.writePermission);
                setPathPermissions(info.pathPermissions);
                this.mExported = info.exported;
                this.mSingleUser = (info.flags & 1073741824) != 0;
                setAuthorities(info.authority);
            }
            if (Log.HWINFO) {
                Trace.traceBegin(64, getClass().getSimpleName() + ".onCreate");
            }
            onCreate();
            if (Log.HWINFO) {
                Trace.traceEnd(64);
            }
        }
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        int numOperations = operations.size();
        ContentProviderResult[] results = new ContentProviderResult[numOperations];
        for (int i = 0; i < numOperations; i++) {
            results[i] = operations.get(i).apply(this, results, i);
        }
        return results;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        return null;
    }

    public void shutdown() {
        Log.w(TAG, "implement ContentProvider shutdown() to make sure all database connections are gracefully shutdown");
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("nothing to dump");
    }

    public Uri validateIncomingUri(Uri uri) throws SecurityException {
        Uri normalized;
        String message;
        String auth = uri.getAuthority();
        if (!this.mSingleUser) {
            int userId = getUserIdFromAuthority(auth, -2);
            if (!(userId == -2 || userId == this.mContext.getUserId())) {
                throw new SecurityException("trying to query a ContentProvider in user " + this.mContext.getUserId() + " with a uri belonging to user " + userId);
            }
        }
        if (!matchesOurAuthorities(getAuthorityWithoutUserId(auth))) {
            String message2 = "The authority of the uri " + uri + " does not match the one of the contentProvider: ";
            if (this.mAuthority != null) {
                message = message2 + this.mAuthority;
            } else {
                message = message2 + Arrays.toString(this.mAuthorities);
            }
            throw new SecurityException(message);
        }
        String encodedPath = uri.getEncodedPath();
        if (encodedPath == null || encodedPath.indexOf("//") == -1) {
            return uri;
        }
        Log.w(TAG, "Normalized " + uri + " to " + normalized + " to avoid possible security issues");
        return normalized;
    }

    /* access modifiers changed from: private */
    public Uri maybeGetUriWithoutUserId(Uri uri) {
        if (this.mSingleUser) {
            return uri;
        }
        return getUriWithoutUserId(uri);
    }

    public static int getUserIdFromAuthority(String auth, int defaultUserId) {
        if (auth == null) {
            return defaultUserId;
        }
        int end = auth.lastIndexOf(64);
        if (end == -1) {
            return defaultUserId;
        }
        try {
            return Integer.parseInt(auth.substring(0, end));
        } catch (NumberFormatException e) {
            Log.w(TAG, "Error parsing userId.", e);
            return UserInfo.NO_PROFILE_GROUP_ID;
        }
    }

    public static int getUserIdFromAuthority(String auth) {
        return getUserIdFromAuthority(auth, -2);
    }

    public static int getUserIdFromUri(Uri uri, int defaultUserId) {
        if (uri == null) {
            return defaultUserId;
        }
        return getUserIdFromAuthority(uri.getAuthority(), defaultUserId);
    }

    public static int getUserIdFromUri(Uri uri) {
        return getUserIdFromUri(uri, -2);
    }

    public static String getAuthorityWithoutUserId(String auth) {
        if (auth == null) {
            return null;
        }
        return auth.substring(auth.lastIndexOf(64) + 1);
    }

    public static Uri getUriWithoutUserId(Uri uri) {
        if (uri == null) {
            return null;
        }
        Uri.Builder builder = uri.buildUpon();
        builder.authority(getAuthorityWithoutUserId(uri.getAuthority()));
        return builder.build();
    }

    public static boolean uriHasUserId(Uri uri) {
        if (uri == null) {
            return false;
        }
        return !TextUtils.isEmpty(uri.getUserInfo());
    }

    public static Uri maybeAddUserId(Uri uri, int userId) {
        if (uri == null) {
            return null;
        }
        if (userId == -2 || !"content".equals(uri.getScheme()) || uriHasUserId(uri)) {
            return uri;
        }
        Uri.Builder builder = uri.buildUpon();
        builder.encodedAuthority("" + userId + "@" + uri.getEncodedAuthority());
        return builder.build();
    }
}
