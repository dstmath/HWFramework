package android.provider;

import android.Manifest.permission;
import android.app.backup.FullBackup;
import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.hwtheme.HwThemeManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Root;
import android.provider.Downloads.Impl;
import android.provider.VoicemailContract.Voicemails;
import android.security.KeyChain;
import android.util.Log;
import java.io.FileNotFoundException;
import java.util.Objects;
import libcore.io.IoUtils;

public abstract class DocumentsProvider extends ContentProvider {
    private static final int MATCH_CHILDREN = 6;
    private static final int MATCH_CHILDREN_TREE = 8;
    private static final int MATCH_DOCUMENT = 5;
    private static final int MATCH_DOCUMENT_TREE = 7;
    private static final int MATCH_RECENT = 3;
    private static final int MATCH_ROOT = 2;
    private static final int MATCH_ROOTS = 1;
    private static final int MATCH_SEARCH = 4;
    private static final String TAG = "DocumentsProvider";
    private String mAuthority;
    private UriMatcher mMatcher;

    public abstract ParcelFileDescriptor openDocument(String str, String str2, CancellationSignal cancellationSignal) throws FileNotFoundException;

    public abstract Cursor queryChildDocuments(String str, String[] strArr, String str2) throws FileNotFoundException;

    public abstract Cursor queryDocument(String str, String[] strArr) throws FileNotFoundException;

    public abstract Cursor queryRoots(String[] strArr) throws FileNotFoundException;

    public void attachInfo(Context context, ProviderInfo info) {
        this.mAuthority = info.authority;
        this.mMatcher = new UriMatcher(-1);
        this.mMatcher.addURI(this.mAuthority, HwThemeManager.HWT_USER_ROOT, MATCH_ROOTS);
        this.mMatcher.addURI(this.mAuthority, "root/*", MATCH_ROOT);
        this.mMatcher.addURI(this.mAuthority, "root/*/recent", MATCH_RECENT);
        this.mMatcher.addURI(this.mAuthority, "root/*/search", MATCH_SEARCH);
        this.mMatcher.addURI(this.mAuthority, "document/*", MATCH_DOCUMENT);
        this.mMatcher.addURI(this.mAuthority, "document/*/children", MATCH_CHILDREN);
        this.mMatcher.addURI(this.mAuthority, "tree/*/document/*", MATCH_DOCUMENT_TREE);
        this.mMatcher.addURI(this.mAuthority, "tree/*/document/*/children", MATCH_CHILDREN_TREE);
        if (!info.exported) {
            throw new SecurityException("Provider must be exported");
        } else if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grantUriPermissions");
        } else if (permission.MANAGE_DOCUMENTS.equals(info.readPermission) && permission.MANAGE_DOCUMENTS.equals(info.writePermission)) {
            super.attachInfo(context, info);
        } else {
            throw new SecurityException("Provider must be protected by MANAGE_DOCUMENTS");
        }
    }

    public boolean isChildDocument(String parentDocumentId, String documentId) {
        return false;
    }

    private void enforceTree(Uri documentUri) {
        if (DocumentsContract.isTreeUri(documentUri)) {
            String parent = DocumentsContract.getTreeDocumentId(documentUri);
            String child = DocumentsContract.getDocumentId(documentUri);
            if (!(Objects.equals(parent, child) || isChildDocument(parent, child))) {
                throw new SecurityException("Document " + child + " is not a descendant of " + parent);
            }
        }
    }

    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        throw new UnsupportedOperationException("Create not supported");
    }

    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        throw new UnsupportedOperationException("Rename not supported");
    }

    public void deleteDocument(String documentId) throws FileNotFoundException {
        throw new UnsupportedOperationException("Delete not supported");
    }

    public String copyDocument(String sourceDocumentId, String targetParentDocumentId) throws FileNotFoundException {
        throw new UnsupportedOperationException("Copy not supported");
    }

    public String moveDocument(String sourceDocumentId, String sourceParentDocumentId, String targetParentDocumentId) throws FileNotFoundException {
        throw new UnsupportedOperationException("Move not supported");
    }

    public void removeDocument(String documentId, String parentDocumentId) throws FileNotFoundException {
        throw new UnsupportedOperationException("Remove not supported");
    }

    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {
        throw new UnsupportedOperationException("Recent not supported");
    }

    public Cursor queryChildDocumentsForManage(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        throw new UnsupportedOperationException("Manage not supported");
    }

    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        throw new UnsupportedOperationException("Search not supported");
    }

    public String getDocumentType(String documentId) throws FileNotFoundException {
        Cursor cursor = queryDocument(documentId, null);
        try {
            if (cursor.moveToFirst()) {
                String string = cursor.getString(cursor.getColumnIndexOrThrow(Voicemails.MIME_TYPE));
                return string;
            }
            IoUtils.closeQuietly(cursor);
            return null;
        } finally {
            IoUtils.closeQuietly(cursor);
        }
    }

    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        throw new UnsupportedOperationException("Thumbnails not supported");
    }

    public AssetFileDescriptor openTypedDocument(String documentId, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws FileNotFoundException {
        throw new FileNotFoundException("The requested MIME type is not supported.");
    }

    public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            switch (this.mMatcher.match(uri)) {
                case MATCH_ROOTS /*1*/:
                    return queryRoots(projection);
                case MATCH_RECENT /*3*/:
                    return queryRecentDocuments(DocumentsContract.getRootId(uri), projection);
                case MATCH_SEARCH /*4*/:
                    return querySearchDocuments(DocumentsContract.getRootId(uri), DocumentsContract.getSearchDocumentsQuery(uri), projection);
                case MATCH_DOCUMENT /*5*/:
                case MATCH_DOCUMENT_TREE /*7*/:
                    enforceTree(uri);
                    return queryDocument(DocumentsContract.getDocumentId(uri), projection);
                case MATCH_CHILDREN /*6*/:
                case MATCH_CHILDREN_TREE /*8*/:
                    enforceTree(uri);
                    if (DocumentsContract.isManageMode(uri)) {
                        return queryChildDocumentsForManage(DocumentsContract.getDocumentId(uri), projection, sortOrder);
                    }
                    return queryChildDocuments(DocumentsContract.getDocumentId(uri), projection, sortOrder);
                default:
                    throw new UnsupportedOperationException("Unsupported Uri " + uri);
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Failed during query", e);
            return null;
        }
        Log.w(TAG, "Failed during query", e);
        return null;
    }

    public final String getType(Uri uri) {
        try {
            switch (this.mMatcher.match(uri)) {
                case MATCH_ROOT /*2*/:
                    return Root.MIME_TYPE_ITEM;
                case MATCH_DOCUMENT /*5*/:
                case MATCH_DOCUMENT_TREE /*7*/:
                    enforceTree(uri);
                    return getDocumentType(DocumentsContract.getDocumentId(uri));
                default:
                    return null;
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Failed during getType", e);
            return null;
        }
    }

    public Uri canonicalize(Uri uri) {
        Context context = getContext();
        switch (this.mMatcher.match(uri)) {
            case MATCH_DOCUMENT_TREE /*7*/:
                enforceTree(uri);
                Uri narrowUri = DocumentsContract.buildDocumentUri(uri.getAuthority(), DocumentsContract.getDocumentId(uri));
                context.grantUriPermission(getCallingPackage(), narrowUri, getCallingOrSelfUriPermissionModeFlags(context, uri));
                return narrowUri;
            default:
                return null;
        }
    }

    private static int getCallingOrSelfUriPermissionModeFlags(Context context, Uri uri) {
        int modeFlags = 0;
        if (context.checkCallingOrSelfUriPermission(uri, MATCH_ROOTS) == 0) {
            modeFlags = MATCH_ROOTS;
        }
        if (context.checkCallingOrSelfUriPermission(uri, MATCH_ROOT) == 0) {
            modeFlags |= MATCH_ROOT;
        }
        if (context.checkCallingOrSelfUriPermission(uri, 65) == 0) {
            return modeFlags | 64;
        }
        return modeFlags;
    }

    public final Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported");
    }

    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (!method.startsWith("android:")) {
            return super.call(method, arg, extras);
        }
        try {
            return callUnchecked(method, arg, extras);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed call " + method, e);
        }
    }

    private Bundle callUnchecked(String method, String arg, Bundle extras) throws FileNotFoundException {
        Context context = getContext();
        Uri documentUri = (Uri) extras.getParcelable(KeyChain.EXTRA_URI);
        String authority = documentUri.getAuthority();
        String documentId = DocumentsContract.getDocumentId(documentUri);
        if (this.mAuthority.equals(authority)) {
            Bundle out = new Bundle();
            enforceTree(documentUri);
            if (DocumentsContract.METHOD_IS_CHILD_DOCUMENT.equals(method)) {
                boolean isChildDocument;
                enforceReadPermissionInner(documentUri, getCallingPackage(), null);
                Uri childUri = (Uri) extras.getParcelable(DocumentsContract.EXTRA_TARGET_URI);
                String childAuthority = childUri.getAuthority();
                String childId = DocumentsContract.getDocumentId(childUri);
                String str = DocumentsContract.EXTRA_RESULT;
                if (this.mAuthority.equals(childAuthority)) {
                    isChildDocument = isChildDocument(documentId, childId);
                } else {
                    isChildDocument = false;
                }
                out.putBoolean(str, isChildDocument);
            } else {
                Uri newDocumentUri;
                if (DocumentsContract.METHOD_CREATE_DOCUMENT.equals(method)) {
                    enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                    newDocumentUri = DocumentsContract.buildDocumentUriMaybeUsingTree(documentUri, createDocument(documentId, extras.getString(Voicemails.MIME_TYPE), extras.getString(OpenableColumns.DISPLAY_NAME)));
                    out.putParcelable(KeyChain.EXTRA_URI, newDocumentUri);
                } else {
                    String newDocumentId;
                    int modeFlags;
                    if (DocumentsContract.METHOD_RENAME_DOCUMENT.equals(method)) {
                        enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                        newDocumentId = renameDocument(documentId, extras.getString(OpenableColumns.DISPLAY_NAME));
                        if (newDocumentId != null) {
                            newDocumentUri = DocumentsContract.buildDocumentUriMaybeUsingTree(documentUri, newDocumentId);
                            if (!DocumentsContract.isTreeUri(newDocumentUri)) {
                                modeFlags = getCallingOrSelfUriPermissionModeFlags(context, documentUri);
                                context.grantUriPermission(getCallingPackage(), newDocumentUri, modeFlags);
                            }
                            out.putParcelable(KeyChain.EXTRA_URI, newDocumentUri);
                            revokeDocumentPermission(documentId);
                        }
                    } else {
                        if (DocumentsContract.METHOD_DELETE_DOCUMENT.equals(method)) {
                            enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                            deleteDocument(documentId);
                            revokeDocumentPermission(documentId);
                        } else {
                            Uri targetUri;
                            String targetId;
                            if (DocumentsContract.METHOD_COPY_DOCUMENT.equals(method)) {
                                targetUri = (Uri) extras.getParcelable(DocumentsContract.EXTRA_TARGET_URI);
                                targetId = DocumentsContract.getDocumentId(targetUri);
                                enforceReadPermissionInner(documentUri, getCallingPackage(), null);
                                enforceWritePermissionInner(targetUri, getCallingPackage(), null);
                                newDocumentId = copyDocument(documentId, targetId);
                                if (newDocumentId != null) {
                                    newDocumentUri = DocumentsContract.buildDocumentUriMaybeUsingTree(documentUri, newDocumentId);
                                    if (!DocumentsContract.isTreeUri(newDocumentUri)) {
                                        modeFlags = getCallingOrSelfUriPermissionModeFlags(context, documentUri);
                                        context.grantUriPermission(getCallingPackage(), newDocumentUri, modeFlags);
                                    }
                                    out.putParcelable(KeyChain.EXTRA_URI, newDocumentUri);
                                }
                            } else {
                                Uri parentSourceUri;
                                String parentSourceId;
                                if (DocumentsContract.METHOD_MOVE_DOCUMENT.equals(method)) {
                                    parentSourceUri = (Uri) extras.getParcelable(DocumentsContract.EXTRA_PARENT_URI);
                                    parentSourceId = DocumentsContract.getDocumentId(parentSourceUri);
                                    targetUri = (Uri) extras.getParcelable(DocumentsContract.EXTRA_TARGET_URI);
                                    targetId = DocumentsContract.getDocumentId(targetUri);
                                    enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                                    enforceReadPermissionInner(parentSourceUri, getCallingPackage(), null);
                                    enforceWritePermissionInner(targetUri, getCallingPackage(), null);
                                    newDocumentId = moveDocument(documentId, parentSourceId, targetId);
                                    if (newDocumentId != null) {
                                        newDocumentUri = DocumentsContract.buildDocumentUriMaybeUsingTree(documentUri, newDocumentId);
                                        if (!DocumentsContract.isTreeUri(newDocumentUri)) {
                                            modeFlags = getCallingOrSelfUriPermissionModeFlags(context, documentUri);
                                            context.grantUriPermission(getCallingPackage(), newDocumentUri, modeFlags);
                                        }
                                        out.putParcelable(KeyChain.EXTRA_URI, newDocumentUri);
                                    }
                                } else {
                                    if (DocumentsContract.METHOD_REMOVE_DOCUMENT.equals(method)) {
                                        parentSourceUri = (Uri) extras.getParcelable(DocumentsContract.EXTRA_PARENT_URI);
                                        parentSourceId = DocumentsContract.getDocumentId(parentSourceUri);
                                        enforceReadPermissionInner(parentSourceUri, getCallingPackage(), null);
                                        enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                                        removeDocument(documentId, parentSourceId);
                                    } else {
                                        throw new UnsupportedOperationException("Method not supported " + method);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return out;
        }
        StringBuilder append = new StringBuilder().append("Requested authority ");
        throw new SecurityException(r22.append(authority).append(" doesn't match provider ").append(this.mAuthority).toString());
    }

    public final void revokeDocumentPermission(String documentId) {
        Context context = getContext();
        context.revokeUriPermission(DocumentsContract.buildDocumentUri(this.mAuthority, documentId), -1);
        context.revokeUriPermission(DocumentsContract.buildTreeDocumentUri(this.mAuthority, documentId), -1);
    }

    public final ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        enforceTree(uri);
        return openDocument(DocumentsContract.getDocumentId(uri), mode, null);
    }

    public final ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        enforceTree(uri);
        return openDocument(DocumentsContract.getDocumentId(uri), mode, signal);
    }

    public final AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        enforceTree(uri);
        ParcelFileDescriptor fd = openDocument(DocumentsContract.getDocumentId(uri), mode, null);
        if (fd != null) {
            return new AssetFileDescriptor(fd, 0, -1);
        }
        return null;
    }

    public final AssetFileDescriptor openAssetFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        enforceTree(uri);
        ParcelFileDescriptor fd = openDocument(DocumentsContract.getDocumentId(uri), mode, signal);
        if (fd != null) {
            return new AssetFileDescriptor(fd, 0, -1);
        }
        return null;
    }

    public final AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts) throws FileNotFoundException {
        return openTypedAssetFileImpl(uri, mimeTypeFilter, opts, null);
    }

    public final AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws FileNotFoundException {
        return openTypedAssetFileImpl(uri, mimeTypeFilter, opts, signal);
    }

    public String[] getDocumentStreamTypes(String documentId, String mimeTypeFilter) {
        AutoCloseable autoCloseable = null;
        String[] strArr = null;
        try {
            autoCloseable = queryDocument(documentId, null);
            if (autoCloseable.moveToFirst()) {
                String mimeType = autoCloseable.getString(autoCloseable.getColumnIndexOrThrow(Voicemails.MIME_TYPE));
                if ((512 & autoCloseable.getLong(autoCloseable.getColumnIndexOrThrow(Impl.COLUMN_FLAGS))) == 0 && mimeType != null && mimeTypeMatches(mimeTypeFilter, mimeType)) {
                    strArr = new String[MATCH_ROOTS];
                    strArr[0] = mimeType;
                    return strArr;
                }
            }
            IoUtils.closeQuietly(autoCloseable);
            return null;
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
        enforceTree(uri);
        return getDocumentStreamTypes(DocumentsContract.getDocumentId(uri), mimeTypeFilter);
    }

    private final AssetFileDescriptor openTypedAssetFileImpl(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws FileNotFoundException {
        enforceTree(uri);
        String documentId = DocumentsContract.getDocumentId(uri);
        if (opts != null && opts.containsKey(ContentResolver.EXTRA_SIZE)) {
            return openDocumentThumbnail(documentId, (Point) opts.getParcelable(ContentResolver.EXTRA_SIZE), signal);
        }
        if ("*/*".equals(mimeTypeFilter)) {
            return openAssetFile(uri, FullBackup.ROOT_TREE_TOKEN);
        }
        String baseType = getType(uri);
        if (baseType == null || !ClipDescription.compareMimeTypes(baseType, mimeTypeFilter)) {
            return openTypedDocument(documentId, mimeTypeFilter, opts, signal);
        }
        return openAssetFile(uri, FullBackup.ROOT_TREE_TOKEN);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean mimeTypeMatches(String filter, String test) {
        if (test == null) {
            return false;
        }
        if (filter == null || "*/*".equals(filter) || filter.equals(test)) {
            return true;
        }
        if (filter.endsWith("/*")) {
            return filter.regionMatches(0, test, 0, filter.indexOf(47));
        }
        return false;
    }
}
