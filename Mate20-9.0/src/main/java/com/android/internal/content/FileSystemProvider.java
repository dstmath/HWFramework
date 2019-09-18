package com.android.internal.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.provider.MetadataReader;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Protocol;
import com.android.internal.widget.MessagingMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import libcore.io.IoUtils;

public abstract class FileSystemProvider extends DocumentsProvider {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final boolean LOG_INOTIFY = false;
    private static final String MIMETYPE_JPEG = "image/jpeg";
    private static final String MIMETYPE_JPG = "image/jpg";
    private static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
    private static final String TAG = "FileSystemProvider";
    private String[] mDefaultProjection;
    private Handler mHandler;
    @GuardedBy("mObservers")
    private final ArrayMap<File, DirectoryObserver> mObservers = new ArrayMap<>();

    private class DirectoryCursor extends MatrixCursor {
        private final File mFile;

        public DirectoryCursor(String[] columnNames, String docId, File file) {
            super(columnNames);
            Uri notifyUri = FileSystemProvider.this.buildNotificationUri(docId);
            setNotificationUri(FileSystemProvider.this.getContext().getContentResolver(), notifyUri);
            this.mFile = file;
            FileSystemProvider.this.startObserving(this.mFile, notifyUri);
        }

        public void close() {
            super.close();
            FileSystemProvider.this.stopObserving(this.mFile);
        }
    }

    private static class DirectoryObserver extends FileObserver {
        private static final int NOTIFY_EVENTS = 4044;
        private final File mFile;
        private final Uri mNotifyUri;
        /* access modifiers changed from: private */
        public int mRefCount = 0;
        private final ContentResolver mResolver;

        static /* synthetic */ int access$010(DirectoryObserver x0) {
            int i = x0.mRefCount;
            x0.mRefCount = i - 1;
            return i;
        }

        public DirectoryObserver(File file, ContentResolver resolver, Uri notifyUri) {
            super(file.getAbsolutePath(), NOTIFY_EVENTS);
            this.mFile = file;
            this.mResolver = resolver;
            this.mNotifyUri = notifyUri;
        }

        public void onEvent(int event, String path) {
            if ((event & NOTIFY_EVENTS) != 0) {
                this.mResolver.notifyChange(this.mNotifyUri, null, false);
            }
        }

        public String toString() {
            return "DirectoryObserver{file=" + this.mFile.getAbsolutePath() + ", ref=" + this.mRefCount + "}";
        }
    }

    /* access modifiers changed from: protected */
    public abstract Uri buildNotificationUri(String str);

    /* access modifiers changed from: protected */
    public abstract String getDocIdForFile(File file) throws FileNotFoundException;

    /* access modifiers changed from: protected */
    public abstract File getFileForDocId(String str, boolean z) throws FileNotFoundException;

    /* access modifiers changed from: protected */
    public void onDocIdChanged(String docId) {
    }

    public boolean onCreate() {
        throw new UnsupportedOperationException("Subclass should override this and call onCreate(defaultDocumentProjection)");
    }

    /* access modifiers changed from: protected */
    public void onCreate(String[] defaultProjection) {
        this.mHandler = new Handler();
        this.mDefaultProjection = defaultProjection;
    }

    public boolean isChildDocument(String parentDocId, String docId) {
        try {
            return FileUtils.contains(getFileForDocId(parentDocId).getCanonicalFile(), getFileForDocId(docId).getCanonicalFile());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to determine if " + docId + " is child of " + parentDocId + ": " + e);
        }
    }

    public Bundle getDocumentMetadata(String documentId) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        if (!file.exists()) {
            throw new FileNotFoundException("Can't find the file for documentId: " + documentId);
        } else if (!file.isFile()) {
            Log.w(TAG, "Can't stream non-regular file. Returning empty metadata.");
            return null;
        } else if (!file.canRead()) {
            Log.w(TAG, "Can't stream non-readable file. Returning empty metadata.");
            return null;
        } else {
            String mimeType = getTypeForFile(file);
            if (!MetadataReader.isSupportedMimeType(mimeType)) {
                return null;
            }
            InputStream stream = null;
            try {
                Bundle metadata = new Bundle();
                stream = new FileInputStream(file.getAbsolutePath());
                MetadataReader.getMetadata(metadata, stream, mimeType, null);
                return metadata;
            } catch (IOException e) {
                Log.e(TAG, "An error occurred retrieving the metadata", e);
                return null;
            } finally {
                IoUtils.closeQuietly(stream);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final List<String> findDocumentPath(File parent, File doc) throws FileNotFoundException {
        if (doc != null && !doc.exists()) {
            throw new FileNotFoundException(doc + " is not found.");
        } else if (FileUtils.contains(parent, doc)) {
            LinkedList<String> path = new LinkedList<>();
            while (doc != null && FileUtils.contains(parent, doc)) {
                path.addFirst(getDocIdForFile(doc));
                doc = doc.getParentFile();
            }
            return path;
        } else {
            throw new FileNotFoundException(doc + " is not found under " + parent);
        }
    }

    public String createDocument(String docId, String mimeType, String displayName) throws FileNotFoundException {
        String displayName2 = FileUtils.buildValidFatFilename(displayName);
        File parent = getFileForDocId(docId);
        if (parent.isDirectory()) {
            File file = FileUtils.buildUniqueFile(parent, mimeType, displayName2);
            if (!"vnd.android.document/directory".equals(mimeType)) {
                try {
                    if (file.createNewFile()) {
                        String childId = getDocIdForFile(file);
                        onDocIdChanged(childId);
                        return childId;
                    }
                    throw new IllegalStateException("Failed to touch " + file);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to touch " + file + ": " + e);
                }
            } else if (file.mkdir()) {
                String childId2 = getDocIdForFile(file);
                onDocIdChanged(childId2);
                addFolderToMediaStore(getFileForDocId(childId2, true));
                return childId2;
            } else {
                throw new IllegalStateException("Failed to mkdir " + file);
            }
        } else {
            throw new IllegalArgumentException("Parent document isn't a directory");
        }
    }

    private void addFolderToMediaStore(File visibleFolder) {
        if (visibleFolder != null) {
            long token = Binder.clearCallingIdentity();
            try {
                ContentResolver resolver = getContext().getContentResolver();
                Uri uri = MediaStore.Files.getDirectoryUri("external");
                ContentValues values = new ContentValues();
                values.put("_data", visibleFolder.getAbsolutePath());
                resolver.insert(uri, values);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public String renameDocument(String docId, String displayName) throws FileNotFoundException {
        String displayName2 = FileUtils.buildValidFatFilename(displayName);
        File before = getFileForDocId(docId);
        File beforeVisibleFile = getFileForDocId(docId, true);
        File after = FileUtils.buildUniqueFile(before.getParentFile(), displayName2);
        if (before.renameTo(after)) {
            String afterDocId = getDocIdForFile(after);
            onDocIdChanged(docId);
            onDocIdChanged(afterDocId);
            File afterVisibleFile = getFileForDocId(afterDocId, true);
            moveInMediaStore(beforeVisibleFile, afterVisibleFile);
            if (TextUtils.equals(docId, afterDocId)) {
                return null;
            }
            scanFile(afterVisibleFile);
            return afterDocId;
        }
        throw new IllegalStateException("Failed to rename to " + after);
    }

    public String moveDocument(String sourceDocumentId, String sourceParentDocumentId, String targetParentDocumentId) throws FileNotFoundException {
        File before = getFileForDocId(sourceDocumentId);
        File after = new File(getFileForDocId(targetParentDocumentId), before.getName());
        File visibleFileBefore = getFileForDocId(sourceDocumentId, true);
        if (after.exists()) {
            throw new IllegalStateException("Already exists " + after);
        } else if (before.renameTo(after)) {
            String docId = getDocIdForFile(after);
            onDocIdChanged(sourceDocumentId);
            onDocIdChanged(docId);
            moveInMediaStore(visibleFileBefore, getFileForDocId(docId, true));
            return docId;
        } else {
            throw new IllegalStateException("Failed to move to " + after);
        }
    }

    private void moveInMediaStore(File oldVisibleFile, File newVisibleFile) {
        Uri externalUri;
        if (oldVisibleFile != null && newVisibleFile != null) {
            long token = Binder.clearCallingIdentity();
            try {
                ContentResolver resolver = getContext().getContentResolver();
                if (newVisibleFile.isDirectory()) {
                    externalUri = MediaStore.Files.getDirectoryUri("external");
                } else {
                    externalUri = MediaStore.Files.getContentUri("external");
                }
                ContentValues values = new ContentValues();
                values.put("_data", newVisibleFile.getAbsolutePath());
                String path = oldVisibleFile.getAbsolutePath();
                resolver.update(externalUri, values, "_data LIKE ? AND lower(_data)=lower(?)", new String[]{path, path});
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void deleteDocument(String docId) throws FileNotFoundException {
        File file = getFileForDocId(docId);
        File visibleFile = getFileForDocId(docId, true);
        boolean isDirectory = file.isDirectory();
        if (isDirectory) {
            FileUtils.deleteContents(file);
        }
        if (file.delete()) {
            onDocIdChanged(docId);
            removeFromMediaStore(visibleFile, isDirectory);
            return;
        }
        throw new IllegalStateException("Failed to delete " + file);
    }

    private void removeFromMediaStore(File visibleFile, boolean isFolder) throws FileNotFoundException {
        if (visibleFile != null) {
            long token = Binder.clearCallingIdentity();
            try {
                ContentResolver resolver = getContext().getContentResolver();
                Uri externalUri = MediaStore.Files.getContentUri("external");
                if (isFolder) {
                    String path = visibleFile.getAbsolutePath() + "/";
                    resolver.delete(externalUri, "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)", new String[]{path + "%", Integer.toString(path.length()), path});
                }
                String path2 = visibleFile.getAbsolutePath();
                resolver.delete(externalUri, "_data LIKE ?1 AND lower(_data)=lower(?2)", new String[]{path2, path2});
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        MatrixCursor result = new MatrixCursor(resolveProjection(projection));
        includeFile(result, documentId, null);
        return result;
    }

    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        File parent = getFileForDocId(parentDocumentId);
        MatrixCursor result = new DirectoryCursor(resolveProjection(projection), parentDocumentId, parent);
        File[] fsList = parent.listFiles();
        if (fsList != null) {
            for (File file : fsList) {
                includeFile(result, null, file);
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public final Cursor querySearchDocuments(File folder, String query, String[] projection, Set<String> exclusion) throws FileNotFoundException {
        String query2 = query.toLowerCase();
        MatrixCursor result = new MatrixCursor(resolveProjection(projection));
        LinkedList<File> pending = new LinkedList<>();
        pending.add(folder);
        while (!pending.isEmpty() && result.getCount() < 24) {
            File file = pending.removeFirst();
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    pending.add(child);
                }
            }
            if (file.getName().toLowerCase().contains(query2) && !exclusion.contains(file.getAbsolutePath())) {
                includeFile(result, null, file);
            }
        }
        return result;
    }

    public String getDocumentType(String documentId) throws FileNotFoundException {
        return getTypeForFile(getFileForDocId(documentId));
    }

    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        File visibleFile = getFileForDocId(documentId, true);
        int pfdMode = ParcelFileDescriptor.parseMode(mode);
        if (pfdMode == 268435456 || visibleFile == null) {
            return ParcelFileDescriptor.open(file, pfdMode);
        }
        try {
            return ParcelFileDescriptor.open(file, pfdMode, this.mHandler, new ParcelFileDescriptor.OnCloseListener(documentId, visibleFile) {
                private final /* synthetic */ String f$1;
                private final /* synthetic */ File f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void onClose(IOException iOException) {
                    FileSystemProvider.lambda$openDocument$0(FileSystemProvider.this, this.f$1, this.f$2, iOException);
                }
            });
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to open for writing: " + e);
        }
    }

    public static /* synthetic */ void lambda$openDocument$0(FileSystemProvider fileSystemProvider, String documentId, File visibleFile, IOException e) {
        fileSystemProvider.onDocIdChanged(documentId);
        fileSystemProvider.scanFile(visibleFile);
    }

    private void scanFile(File visibleFile) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(Uri.fromFile(visibleFile));
        getContext().sendBroadcast(intent);
    }

    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        return DocumentsContract.openImageThumbnail(getFileForDocId(documentId));
    }

    /* access modifiers changed from: protected */
    public MatrixCursor.RowBuilder includeFile(MatrixCursor result, String docId, File file) throws FileNotFoundException {
        if (docId == null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }
        int flags = 0;
        if (file.canWrite()) {
            if (file.isDirectory()) {
                flags = 0 | 8 | 4 | 64 | 256;
            } else {
                flags = 0 | 2 | 4 | 64 | 256;
            }
        }
        String mimeType = getTypeForFile(file);
        String displayName = file.getName();
        if (mimeType.startsWith(MessagingMessage.IMAGE_MIME_TYPE_PREFIX)) {
            flags |= 1;
        }
        if (typeSupportsMetadata(mimeType)) {
            flags |= Protocol.BASE_WIFI;
        }
        MatrixCursor.RowBuilder row = result.newRow();
        row.add("document_id", docId);
        row.add("_display_name", displayName);
        row.add("_size", Long.valueOf(file.length()));
        row.add("mime_type", mimeType);
        row.add("flags", Integer.valueOf(flags));
        long lastModified = file.lastModified();
        if (lastModified > 31536000000L) {
            row.add("last_modified", Long.valueOf(lastModified));
        }
        return row;
    }

    private static String getTypeForFile(File file) {
        if (file.isDirectory()) {
            return "vnd.android.document/directory";
        }
        return getTypeForName(file.getName());
    }

    /* access modifiers changed from: protected */
    public boolean typeSupportsMetadata(String mimeType) {
        return MetadataReader.isSupportedMimeType(mimeType);
    }

    private static String getTypeForName(String name) {
        int lastDot = name.lastIndexOf(46);
        if (lastDot >= 0) {
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(lastDot + 1).toLowerCase());
            if (mime != null) {
                return mime;
            }
        }
        return MIMETYPE_OCTET_STREAM;
    }

    /* access modifiers changed from: protected */
    public final File getFileForDocId(String docId) throws FileNotFoundException {
        return getFileForDocId(docId, false);
    }

    private String[] resolveProjection(String[] projection) {
        return projection == null ? this.mDefaultProjection : projection;
    }

    /* access modifiers changed from: private */
    public void startObserving(File file, Uri notifyUri) {
        synchronized (this.mObservers) {
            DirectoryObserver observer = this.mObservers.get(file);
            if (observer == null) {
                observer = new DirectoryObserver(file, getContext().getContentResolver(), notifyUri);
                observer.startWatching();
                this.mObservers.put(file, observer);
            }
            int unused = observer.mRefCount = observer.mRefCount + 1;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        return;
     */
    public void stopObserving(File file) {
        synchronized (this.mObservers) {
            DirectoryObserver observer = this.mObservers.get(file);
            if (observer != null) {
                DirectoryObserver.access$010(observer);
                if (observer.mRefCount == 0) {
                    this.mObservers.remove(file);
                    observer.stopWatching();
                }
            }
        }
    }
}
