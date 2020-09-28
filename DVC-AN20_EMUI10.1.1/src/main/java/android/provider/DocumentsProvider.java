package android.provider;

import android.Manifest;
import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.content.MimeTypeFilter;
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
import android.os.ParcelableException;
import android.provider.DocumentsContract;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.io.FileNotFoundException;
import java.util.LinkedList;
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

    @Override // android.content.ContentProvider
    public void attachInfo(Context context, ProviderInfo info) {
        registerAuthority(info.authority);
        if (!info.exported) {
            throw new SecurityException("Provider must be exported");
        } else if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grantUriPermissions");
        } else if (!Manifest.permission.MANAGE_DOCUMENTS.equals(info.readPermission) || !Manifest.permission.MANAGE_DOCUMENTS.equals(info.writePermission)) {
            throw new SecurityException("Provider must be protected by MANAGE_DOCUMENTS");
        } else {
            super.attachInfo(context, info);
        }
    }

    @Override // android.content.ContentProvider
    public void attachInfoForTesting(Context context, ProviderInfo info) {
        registerAuthority(info.authority);
        super.attachInfoForTesting(context, info);
    }

    private void registerAuthority(String authority) {
        this.mAuthority = authority;
        this.mMatcher = new UriMatcher(-1);
        this.mMatcher.addURI(this.mAuthority, HwThemeManager.HWT_USER_ROOT, 1);
        this.mMatcher.addURI(this.mAuthority, "root/*", 2);
        this.mMatcher.addURI(this.mAuthority, "root/*/recent", 3);
        this.mMatcher.addURI(this.mAuthority, "root/*/search", 4);
        this.mMatcher.addURI(this.mAuthority, "document/*", 5);
        this.mMatcher.addURI(this.mAuthority, "document/*/children", 6);
        this.mMatcher.addURI(this.mAuthority, "tree/*/document/*", 7);
        this.mMatcher.addURI(this.mAuthority, "tree/*/document/*/children", 8);
    }

    public boolean isChildDocument(String parentDocumentId, String documentId) {
        return false;
    }

    private void enforceTree(Uri documentUri) {
        if (DocumentsContract.isTreeUri(documentUri)) {
            String parent = DocumentsContract.getTreeDocumentId(documentUri);
            String child = DocumentsContract.getDocumentId(documentUri);
            if (!Objects.equals(parent, child) && !isChildDocument(parent, child)) {
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

    public DocumentsContract.Path findDocumentPath(String parentDocumentId, String childDocumentId) throws FileNotFoundException {
        throw new UnsupportedOperationException("findDocumentPath not supported.");
    }

    public IntentSender createWebLinkIntent(String documentId, Bundle options) throws FileNotFoundException {
        throw new UnsupportedOperationException("createWebLink is not supported.");
    }

    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {
        throw new UnsupportedOperationException("Recent not supported");
    }

    public Cursor queryRecentDocuments(String rootId, String[] projection, Bundle queryArgs, CancellationSignal signal) throws FileNotFoundException {
        Preconditions.checkNotNull(rootId, "rootId can not be null");
        Cursor c = queryRecentDocuments(rootId, projection);
        Bundle extras = new Bundle();
        c.setExtras(extras);
        extras.putStringArray(ContentResolver.EXTRA_HONORED_ARGS, new String[0]);
        return c;
    }

    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, Bundle queryArgs) throws FileNotFoundException {
        return queryChildDocuments(parentDocumentId, projection, getSortClause(queryArgs));
    }

    public Cursor queryChildDocumentsForManage(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        throw new UnsupportedOperationException("Manage not supported");
    }

    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        throw new UnsupportedOperationException("Search not supported");
    }

    public Cursor querySearchDocuments(String rootId, String[] projection, Bundle queryArgs) throws FileNotFoundException {
        Preconditions.checkNotNull(rootId, "rootId can not be null");
        Preconditions.checkNotNull(queryArgs, "queryArgs can not be null");
        return querySearchDocuments(rootId, DocumentsContract.getSearchDocumentsQuery(queryArgs), projection);
    }

    public void ejectRoot(String rootId) {
        throw new UnsupportedOperationException("Eject not supported");
    }

    public Bundle getDocumentMetadata(String documentId) throws FileNotFoundException {
        throw new UnsupportedOperationException("Metadata not supported");
    }

    public String getDocumentType(String documentId) throws FileNotFoundException {
        Cursor cursor = queryDocument(documentId, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("mime_type"));
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

    @Override // android.content.ContentProvider
    public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("Pre-Android-O query format not supported.");
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        throw new UnsupportedOperationException("Pre-Android-O query format not supported.");
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public final Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        try {
            switch (this.mMatcher.match(uri)) {
                case 1:
                    return queryRoots(projection);
                case 2:
                default:
                    throw new UnsupportedOperationException("Unsupported Uri " + uri);
                case 3:
                    return queryRecentDocuments(DocumentsContract.getRootId(uri), projection, queryArgs, cancellationSignal);
                case 4:
                    return querySearchDocuments(DocumentsContract.getRootId(uri), projection, queryArgs);
                case 5:
                case 7:
                    enforceTree(uri);
                    return queryDocument(DocumentsContract.getDocumentId(uri), projection);
                case 6:
                case 8:
                    enforceTree(uri);
                    if (DocumentsContract.isManageMode(uri)) {
                        return queryChildDocumentsForManage(DocumentsContract.getDocumentId(uri), projection, getSortClause(queryArgs));
                    }
                    return queryChildDocuments(DocumentsContract.getDocumentId(uri), projection, queryArgs);
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Failed during query", e);
            return null;
        }
    }

    private static String getSortClause(Bundle queryArgs) {
        Bundle queryArgs2 = queryArgs != null ? queryArgs : Bundle.EMPTY;
        String sortClause = queryArgs2.getString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER);
        if (sortClause != null || !queryArgs2.containsKey(ContentResolver.QUERY_ARG_SORT_COLUMNS)) {
            return sortClause;
        }
        return ContentResolver.createSqlSortClause(queryArgs2);
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public final String getType(Uri uri) {
        try {
            int match = this.mMatcher.match(uri);
            if (match == 2) {
                return DocumentsContract.Root.MIME_TYPE_ITEM;
            }
            if (match != 5 && match != 7) {
                return null;
            }
            enforceTree(uri);
            return getDocumentType(DocumentsContract.getDocumentId(uri));
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Failed during getType", e);
            return null;
        }
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public Uri canonicalize(Uri uri) {
        Context context = getContext();
        if (this.mMatcher.match(uri) != 7) {
            return null;
        }
        enforceTree(uri);
        Uri narrowUri = DocumentsContract.buildDocumentUri(uri.getAuthority(), DocumentsContract.getDocumentId(uri));
        context.grantUriPermission(getCallingPackage(), narrowUri, getCallingOrSelfUriPermissionModeFlags(context, uri));
        return narrowUri;
    }

    private static int getCallingOrSelfUriPermissionModeFlags(Context context, Uri uri) {
        int modeFlags = 0;
        if (context.checkCallingOrSelfUriPermission(uri, 1) == 0) {
            modeFlags = 0 | 1;
        }
        if (context.checkCallingOrSelfUriPermission(uri, 2) == 0) {
            modeFlags |= 2;
        }
        if (context.checkCallingOrSelfUriPermission(uri, 65) == 0) {
            return modeFlags | 64;
        }
        return modeFlags;
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public final Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported");
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }

    @Override // android.content.ContentProvider
    public Bundle call(String method, String arg, Bundle extras) {
        if (!method.startsWith("android:")) {
            return super.call(method, arg, extras);
        }
        try {
            return callUnchecked(method, arg, extras);
        } catch (FileNotFoundException e) {
            throw new ParcelableException(e);
        }
    }

    private Bundle callUnchecked(String method, String arg, Bundle extras) throws FileNotFoundException {
        String parentDocumentId;
        Context context = getContext();
        Bundle out = new Bundle();
        if (DocumentsContract.METHOD_EJECT_ROOT.equals(method)) {
            Uri rootUri = (Uri) extras.getParcelable("uri");
            enforceWritePermissionInner(rootUri, getCallingPackage(), null);
            ejectRoot(DocumentsContract.getRootId(rootUri));
            return out;
        }
        Uri documentUri = (Uri) extras.getParcelable("uri");
        String authority = documentUri.getAuthority();
        String documentId = DocumentsContract.getDocumentId(documentUri);
        if (this.mAuthority.equals(authority)) {
            enforceTree(documentUri);
            boolean z = false;
            if (DocumentsContract.METHOD_IS_CHILD_DOCUMENT.equals(method)) {
                enforceReadPermissionInner(documentUri, getCallingPackage(), null);
                Uri childUri = (Uri) extras.getParcelable(DocumentsContract.EXTRA_TARGET_URI);
                String childAuthority = childUri.getAuthority();
                String childId = DocumentsContract.getDocumentId(childUri);
                if (this.mAuthority.equals(childAuthority) && isChildDocument(documentId, childId)) {
                    z = true;
                }
                out.putBoolean("result", z);
            } else if (DocumentsContract.METHOD_CREATE_DOCUMENT.equals(method)) {
                enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                out.putParcelable("uri", DocumentsContract.buildDocumentUriMaybeUsingTree(documentUri, createDocument(documentId, extras.getString("mime_type"), extras.getString("_display_name"))));
            } else if (DocumentsContract.METHOD_CREATE_WEB_LINK_INTENT.equals(method)) {
                enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                out.putParcelable("result", createWebLinkIntent(documentId, extras.getBundle(DocumentsContract.EXTRA_OPTIONS)));
            } else if (DocumentsContract.METHOD_RENAME_DOCUMENT.equals(method)) {
                enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                String newDocumentId = renameDocument(documentId, extras.getString("_display_name"));
                if (newDocumentId != null) {
                    Uri newDocumentUri = DocumentsContract.buildDocumentUriMaybeUsingTree(documentUri, newDocumentId);
                    if (!DocumentsContract.isTreeUri(newDocumentUri)) {
                        context.grantUriPermission(getCallingPackage(), newDocumentUri, getCallingOrSelfUriPermissionModeFlags(context, documentUri));
                    }
                    out.putParcelable("uri", newDocumentUri);
                    revokeDocumentPermission(documentId);
                }
            } else if (DocumentsContract.METHOD_DELETE_DOCUMENT.equals(method)) {
                enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                deleteDocument(documentId);
                revokeDocumentPermission(documentId);
            } else if (DocumentsContract.METHOD_COPY_DOCUMENT.equals(method)) {
                Uri targetUri = (Uri) extras.getParcelable(DocumentsContract.EXTRA_TARGET_URI);
                String targetId = DocumentsContract.getDocumentId(targetUri);
                enforceReadPermissionInner(documentUri, getCallingPackage(), null);
                enforceWritePermissionInner(targetUri, getCallingPackage(), null);
                String newDocumentId2 = copyDocument(documentId, targetId);
                if (newDocumentId2 != null) {
                    Uri newDocumentUri2 = DocumentsContract.buildDocumentUriMaybeUsingTree(documentUri, newDocumentId2);
                    if (!DocumentsContract.isTreeUri(newDocumentUri2)) {
                        context.grantUriPermission(getCallingPackage(), newDocumentUri2, getCallingOrSelfUriPermissionModeFlags(context, documentUri));
                    }
                    out.putParcelable("uri", newDocumentUri2);
                }
            } else if (DocumentsContract.METHOD_MOVE_DOCUMENT.equals(method)) {
                Uri parentSourceUri = (Uri) extras.getParcelable(DocumentsContract.EXTRA_PARENT_URI);
                String parentSourceId = DocumentsContract.getDocumentId(parentSourceUri);
                Uri targetUri2 = (Uri) extras.getParcelable(DocumentsContract.EXTRA_TARGET_URI);
                String targetId2 = DocumentsContract.getDocumentId(targetUri2);
                enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                enforceReadPermissionInner(parentSourceUri, getCallingPackage(), null);
                enforceWritePermissionInner(targetUri2, getCallingPackage(), null);
                String newDocumentId3 = moveDocument(documentId, parentSourceId, targetId2);
                if (newDocumentId3 != null) {
                    Uri newDocumentUri3 = DocumentsContract.buildDocumentUriMaybeUsingTree(documentUri, newDocumentId3);
                    if (!DocumentsContract.isTreeUri(newDocumentUri3)) {
                        context.grantUriPermission(getCallingPackage(), newDocumentUri3, getCallingOrSelfUriPermissionModeFlags(context, documentUri));
                    }
                    out.putParcelable("uri", newDocumentUri3);
                }
            } else if (DocumentsContract.METHOD_REMOVE_DOCUMENT.equals(method)) {
                Uri parentSourceUri2 = (Uri) extras.getParcelable(DocumentsContract.EXTRA_PARENT_URI);
                String parentSourceId2 = DocumentsContract.getDocumentId(parentSourceUri2);
                enforceReadPermissionInner(parentSourceUri2, getCallingPackage(), null);
                enforceWritePermissionInner(documentUri, getCallingPackage(), null);
                removeDocument(documentId, parentSourceId2);
            } else if (DocumentsContract.METHOD_FIND_DOCUMENT_PATH.equals(method)) {
                boolean isTreeUri = DocumentsContract.isTreeUri(documentUri);
                if (isTreeUri) {
                    enforceReadPermissionInner(documentUri, getCallingPackage(), null);
                } else {
                    getContext().enforceCallingPermission(Manifest.permission.MANAGE_DOCUMENTS, null);
                }
                if (isTreeUri) {
                    parentDocumentId = DocumentsContract.getTreeDocumentId(documentUri);
                } else {
                    parentDocumentId = null;
                }
                DocumentsContract.Path path = findDocumentPath(parentDocumentId, documentId);
                if (isTreeUri) {
                    if (!Objects.equals(path.getPath().get(0), parentDocumentId)) {
                        Log.wtf(TAG, "Provider doesn't return path from the tree root. Expected: " + parentDocumentId + " found: " + path.getPath().get(0));
                        LinkedList<String> docs = new LinkedList<>(path.getPath());
                        while (docs.size() > 1 && !Objects.equals(docs.getFirst(), parentDocumentId)) {
                            docs.removeFirst();
                        }
                        path = new DocumentsContract.Path(null, docs);
                    }
                    if (path.getRootId() != null) {
                        Log.wtf(TAG, "Provider returns root id :" + path.getRootId() + " unexpectedly. Erase root id.");
                        path = new DocumentsContract.Path(null, path.getPath());
                    }
                }
                out.putParcelable("result", path);
            } else if (DocumentsContract.METHOD_GET_DOCUMENT_METADATA.equals(method)) {
                enforceReadPermissionInner(documentUri, getCallingPackage(), null);
                return getDocumentMetadata(documentId);
            } else {
                throw new UnsupportedOperationException("Method not supported " + method);
            }
            return out;
        }
        throw new SecurityException("Requested authority " + authority + " doesn't match provider " + this.mAuthority);
    }

    public final void revokeDocumentPermission(String documentId) {
        Context context = getContext();
        context.revokeUriPermission(DocumentsContract.buildDocumentUri(this.mAuthority, documentId), -1);
        context.revokeUriPermission(DocumentsContract.buildTreeDocumentUri(this.mAuthority, documentId), -1);
    }

    @Override // android.content.ContentProvider
    public final ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        enforceTree(uri);
        return openDocument(DocumentsContract.getDocumentId(uri), mode, null);
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public final ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        enforceTree(uri);
        return openDocument(DocumentsContract.getDocumentId(uri), mode, signal);
    }

    @Override // android.content.ContentProvider
    public final AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        enforceTree(uri);
        ParcelFileDescriptor fd = openDocument(DocumentsContract.getDocumentId(uri), mode, null);
        if (fd != null) {
            return new AssetFileDescriptor(fd, 0, -1);
        }
        return null;
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public final AssetFileDescriptor openAssetFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        enforceTree(uri);
        ParcelFileDescriptor fd = openDocument(DocumentsContract.getDocumentId(uri), mode, signal);
        if (fd != null) {
            return new AssetFileDescriptor(fd, 0, -1);
        }
        return null;
    }

    @Override // android.content.ContentProvider
    public final AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts) throws FileNotFoundException {
        return openTypedAssetFileImpl(uri, mimeTypeFilter, opts, null);
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
    public final AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws FileNotFoundException {
        return openTypedAssetFileImpl(uri, mimeTypeFilter, opts, signal);
    }

    public String[] getDocumentStreamTypes(String documentId, String mimeTypeFilter) {
        Cursor cursor = null;
        try {
            cursor = queryDocument(documentId, null);
            if (cursor.moveToFirst()) {
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow("mime_type"));
                if ((512 & cursor.getLong(cursor.getColumnIndexOrThrow("flags"))) == 0 && mimeType != null && MimeTypeFilter.matches(mimeType, mimeTypeFilter)) {
                    return new String[]{mimeType};
                }
            }
            IoUtils.closeQuietly(cursor);
            return null;
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            IoUtils.closeQuietly(cursor);
        }
    }

    @Override // android.content.ContentInterface, android.content.ContentProvider
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
            return openAssetFile(uri, "r");
        }
        String baseType = getType(uri);
        if (baseType == null || !ClipDescription.compareMimeTypes(baseType, mimeTypeFilter)) {
            return openTypedDocument(documentId, mimeTypeFilter, opts, signal);
        }
        return openAssetFile(uri, "r");
    }
}
