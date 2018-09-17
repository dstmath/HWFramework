package com.android.internal.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.graphics.Point;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsProvider;
import android.provider.MediaStore.Files;
import android.rms.iaware.AwareConstant.Database;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.webkit.MimeTypeMap;
import com.android.internal.annotations.GuardedBy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class FileSystemProvider extends DocumentsProvider {
    static final /* synthetic */ boolean -assertionsDisabled = (FileSystemProvider.class.desiredAssertionStatus() ^ 1);
    private static final boolean LOG_INOTIFY = false;
    private static final String TAG = "FileSystemProvider";
    private String[] mDefaultProjection;
    private Handler mHandler;
    @GuardedBy("mObservers")
    private final ArrayMap<File, DirectoryObserver> mObservers = new ArrayMap();

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
        private int mRefCount = 0;
        private final ContentResolver mResolver;

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

    protected abstract Uri buildNotificationUri(String str);

    protected abstract String getDocIdForFile(File file) throws FileNotFoundException;

    protected abstract File getFileForDocId(String str, boolean z) throws FileNotFoundException;

    public boolean onCreate() {
        throw new UnsupportedOperationException("Subclass should override this and call onCreate(defaultDocumentProjection)");
    }

    protected void onCreate(String[] defaultProjection) {
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

    protected final List<String> findDocumentPath(File parent, File doc) throws FileNotFoundException {
        if (doc != null && (doc.exists() ^ 1) != 0) {
            throw new FileNotFoundException(doc + " is not found.");
        } else if (FileUtils.contains(parent, doc)) {
            LinkedList<String> path = new LinkedList();
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
        displayName = FileUtils.buildValidFatFilename(displayName);
        File parent = getFileForDocId(docId);
        if (parent.isDirectory()) {
            File file = FileUtils.buildUniqueFile(parent, mimeType, displayName);
            if (!Document.MIME_TYPE_DIR.equals(mimeType)) {
                try {
                    if (file.createNewFile()) {
                        return getDocIdForFile(file);
                    }
                    throw new IllegalStateException("Failed to touch " + file);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to touch " + file + ": " + e);
                }
            } else if (file.mkdir()) {
                String childId = getDocIdForFile(file);
                addFolderToMediaStore(getFileForDocId(childId, true));
                return childId;
            } else {
                throw new IllegalStateException("Failed to mkdir " + file);
            }
        }
        throw new IllegalArgumentException("Parent document isn't a directory");
    }

    private void addFolderToMediaStore(File visibleFolder) {
        if (visibleFolder == null) {
            return;
        }
        if (-assertionsDisabled || visibleFolder.isDirectory()) {
            ContentResolver resolver = getContext().getContentResolver();
            Uri uri = Files.getDirectoryUri("external");
            ContentValues values = new ContentValues();
            values.put("_data", visibleFolder.getAbsolutePath());
            resolver.insert(uri, values);
            return;
        }
        throw new AssertionError();
    }

    public String renameDocument(String docId, String displayName) throws FileNotFoundException {
        displayName = FileUtils.buildValidFatFilename(displayName);
        File before = getFileForDocId(docId);
        File after = FileUtils.buildUniqueFile(before.getParentFile(), displayName);
        File visibleFileBefore = getFileForDocId(docId, true);
        if (before.renameTo(after)) {
            String afterDocId = getDocIdForFile(after);
            moveInMediaStore(visibleFileBefore, getFileForDocId(afterDocId, true));
            if (TextUtils.equals(docId, afterDocId)) {
                return null;
            }
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
            moveInMediaStore(visibleFileBefore, getFileForDocId(docId, true));
            return docId;
        } else {
            throw new IllegalStateException("Failed to move to " + after);
        }
    }

    private void moveInMediaStore(File oldVisibleFile, File newVisibleFile) {
        if (oldVisibleFile != null && newVisibleFile != null) {
            Uri externalUri;
            ContentResolver resolver = getContext().getContentResolver();
            if (newVisibleFile.isDirectory()) {
                externalUri = Files.getDirectoryUri("external");
            } else {
                externalUri = Files.getContentUri("external");
            }
            ContentValues values = new ContentValues();
            values.put("_data", newVisibleFile.getAbsolutePath());
            String path = oldVisibleFile.getAbsolutePath();
            resolver.update(externalUri, values, "_data LIKE ? AND lower(_data)=lower(?)", new String[]{path, path});
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
            removeFromMediaStore(visibleFile, isDirectory);
            return;
        }
        throw new IllegalStateException("Failed to delete " + file);
    }

    private void removeFromMediaStore(File visibleFile, boolean isFolder) throws FileNotFoundException {
        if (visibleFile != null) {
            String path;
            ContentResolver resolver = getContext().getContentResolver();
            Uri externalUri = Files.getContentUri("external");
            if (isFolder) {
                path = visibleFile.getAbsolutePath() + "/";
                resolver.delete(externalUri, "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)", new String[]{path + "%", Integer.toString(path.length()), path});
            }
            path = visibleFile.getAbsolutePath();
            resolver.delete(externalUri, "_data LIKE ?1 AND lower(_data)=lower(?2)", new String[]{path, path});
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

    protected final Cursor querySearchDocuments(File folder, String query, String[] projection, Set<String> exclusion) throws FileNotFoundException {
        query = query.toLowerCase();
        MatrixCursor result = new MatrixCursor(resolveProjection(projection));
        LinkedList<File> pending = new LinkedList();
        pending.add(folder);
        while (!pending.isEmpty() && result.getCount() < 24) {
            File file = (File) pending.removeFirst();
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    pending.add(child);
                }
            }
            if (file.getName().toLowerCase().contains(query) && (exclusion.contains(file.getAbsolutePath()) ^ 1) != 0) {
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
            return ParcelFileDescriptor.open(file, pfdMode, this.mHandler, new -$Lambda$qCDQZ4U5of2rgsneNEo3bc5KTII(this, visibleFile));
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to open for writing: " + e);
        }
    }

    private void scanFile(File visibleFile) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(Uri.fromFile(visibleFile));
        getContext().sendBroadcast(intent);
    }

    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        return DocumentsContract.openImageThumbnail(getFileForDocId(documentId));
    }

    protected RowBuilder includeFile(MatrixCursor result, String docId, File file) throws FileNotFoundException {
        if (docId == null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }
        int flags = 0;
        if (file.canWrite()) {
            if (file.isDirectory()) {
                flags = ((8 | 4) | 64) | 256;
            } else {
                flags = ((2 | 4) | 64) | 256;
            }
        }
        String mimeType = getTypeForFile(file);
        String displayName = file.getName();
        if (mimeType.startsWith("image/")) {
            flags |= 1;
        }
        RowBuilder row = result.newRow();
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
            return Document.MIME_TYPE_DIR;
        }
        return getTypeForName(file.getName());
    }

    private static String getTypeForName(String name) {
        int lastDot = name.lastIndexOf(46);
        if (lastDot >= 0) {
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(lastDot + 1).toLowerCase());
            if (mime != null) {
                return mime;
            }
        }
        return Database.UNKNOWN_MIME_TYPE;
    }

    protected final File getFileForDocId(String docId) throws FileNotFoundException {
        return getFileForDocId(docId, false);
    }

    private String[] resolveProjection(String[] projection) {
        return projection == null ? this.mDefaultProjection : projection;
    }

    private void startObserving(File file, Uri notifyUri) {
        synchronized (this.mObservers) {
            DirectoryObserver observer = (DirectoryObserver) this.mObservers.get(file);
            if (observer == null) {
                observer = new DirectoryObserver(file, getContext().getContentResolver(), notifyUri);
                observer.startWatching();
                this.mObservers.put(file, observer);
            }
            observer.mRefCount = observer.mRefCount + 1;
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0027, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void stopObserving(File file) {
        synchronized (this.mObservers) {
            DirectoryObserver observer = (DirectoryObserver) this.mObservers.get(file);
            if (observer == null) {
                return;
            }
            observer.mRefCount = observer.mRefCount - 1;
            if (observer.mRefCount == 0) {
                this.mObservers.remove(file);
                observer.stopWatching();
            }
        }
    }
}
