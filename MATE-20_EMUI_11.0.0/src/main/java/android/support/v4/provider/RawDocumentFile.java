package android.support.v4.provider;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class RawDocumentFile extends DocumentFile {
    private File mFile;

    RawDocumentFile(@Nullable DocumentFile parent, File file) {
        super(parent);
        this.mFile = file;
    }

    @Override // android.support.v4.provider.DocumentFile
    @Nullable
    public DocumentFile createFile(String mimeType, String displayName) {
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (extension != null) {
            displayName = displayName + "." + extension;
        }
        File target = new File(this.mFile, displayName);
        try {
            target.createNewFile();
            return new RawDocumentFile(this, target);
        } catch (IOException e) {
            Log.w("DocumentFile", "Failed to createFile: " + e);
            return null;
        }
    }

    @Override // android.support.v4.provider.DocumentFile
    @Nullable
    public DocumentFile createDirectory(String displayName) {
        File target = new File(this.mFile, displayName);
        if (target.isDirectory() || target.mkdir()) {
            return new RawDocumentFile(this, target);
        }
        return null;
    }

    @Override // android.support.v4.provider.DocumentFile
    public Uri getUri() {
        return Uri.fromFile(this.mFile);
    }

    @Override // android.support.v4.provider.DocumentFile
    public String getName() {
        return this.mFile.getName();
    }

    @Override // android.support.v4.provider.DocumentFile
    @Nullable
    public String getType() {
        if (this.mFile.isDirectory()) {
            return null;
        }
        return getTypeForName(this.mFile.getName());
    }

    @Override // android.support.v4.provider.DocumentFile
    public boolean isDirectory() {
        return this.mFile.isDirectory();
    }

    @Override // android.support.v4.provider.DocumentFile
    public boolean isFile() {
        return this.mFile.isFile();
    }

    @Override // android.support.v4.provider.DocumentFile
    public boolean isVirtual() {
        return false;
    }

    @Override // android.support.v4.provider.DocumentFile
    public long lastModified() {
        return this.mFile.lastModified();
    }

    @Override // android.support.v4.provider.DocumentFile
    public long length() {
        return this.mFile.length();
    }

    @Override // android.support.v4.provider.DocumentFile
    public boolean canRead() {
        return this.mFile.canRead();
    }

    @Override // android.support.v4.provider.DocumentFile
    public boolean canWrite() {
        return this.mFile.canWrite();
    }

    @Override // android.support.v4.provider.DocumentFile
    public boolean delete() {
        deleteContents(this.mFile);
        return this.mFile.delete();
    }

    @Override // android.support.v4.provider.DocumentFile
    public boolean exists() {
        return this.mFile.exists();
    }

    @Override // android.support.v4.provider.DocumentFile
    public DocumentFile[] listFiles() {
        ArrayList<DocumentFile> results = new ArrayList<>();
        File[] files = this.mFile.listFiles();
        if (files != null) {
            for (File file : files) {
                results.add(new RawDocumentFile(this, file));
            }
        }
        return (DocumentFile[]) results.toArray(new DocumentFile[results.size()]);
    }

    @Override // android.support.v4.provider.DocumentFile
    public boolean renameTo(String displayName) {
        File target = new File(this.mFile.getParentFile(), displayName);
        if (!this.mFile.renameTo(target)) {
            return false;
        }
        this.mFile = target;
        return true;
    }

    private static String getTypeForName(String name) {
        String mime;
        int lastDot = name.lastIndexOf(46);
        if (lastDot < 0 || (mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(lastDot + 1).toLowerCase())) == null) {
            return "application/octet-stream";
        }
        return mime;
    }

    private static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Log.w("DocumentFile", "Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }
}
