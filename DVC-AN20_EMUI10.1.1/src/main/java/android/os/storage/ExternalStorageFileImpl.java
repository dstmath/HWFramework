package android.os.storage;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class ExternalStorageFileImpl extends File {
    protected File internalFile;
    protected String internalPath;
    protected int internalPrefixLength;
    private boolean mDoAccessDefalut = true;
    private transient StoragePathStatus pathStatus = null;

    /* access modifiers changed from: private */
    public enum StoragePathStatus {
        EXTERNAL,
        EMULATED
    }

    private int prefixLength(String pathname) {
        if (pathname.length() != 0 && pathname.charAt(0) == '/') {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public final boolean enforceExternalStoragePath() {
        if (this.pathStatus == null) {
            if (!isExternalStoragePath(super.getPath())) {
                this.pathStatus = StoragePathStatus.EMULATED;
            } else {
                this.pathStatus = StoragePathStatus.EXTERNAL;
            }
        }
        return this.pathStatus == StoragePathStatus.EXTERNAL;
    }

    private boolean isExternalStoragePath(String path) {
        return path.startsWith("/storage/") && !path.startsWith("/storage/emulated/");
    }

    /* access modifiers changed from: protected */
    public File[] convertFiles(File parent, String[] fileNames) {
        int n = fileNames.length;
        File[] fs = new ExternalStorageFileImpl[n];
        for (int i = 0; i < n; i++) {
            fs[i] = new ExternalStorageFileImpl(parent, fileNames[i]);
        }
        return fs;
    }

    /* access modifiers changed from: protected */
    public File createInstance(String path) {
        return new ExternalStorageFileImpl(path);
    }

    public ExternalStorageFileImpl(String pathname) {
        super(pathname);
        if (enforceExternalStoragePath()) {
            this.internalPath = pathname.replaceFirst("/storage/", "/mnt/media_rw/");
            this.internalPrefixLength = prefixLength(this.internalPath);
            this.internalFile = new File(this.internalPath);
        }
    }

    public ExternalStorageFileImpl(String parent, String child) {
        super(parent, child);
        if (enforceExternalStoragePath()) {
            this.internalPath = super.getPath().replaceFirst("/storage/", "/mnt/media_rw/");
            this.internalPrefixLength = prefixLength(this.internalPath);
            this.internalFile = new File(this.internalPath);
        }
    }

    public ExternalStorageFileImpl(File parent, String child) {
        super(parent, child);
        if (enforceExternalStoragePath()) {
            this.internalPath = super.getPath().replaceFirst("/storage/", "/mnt/media_rw/");
            this.internalPrefixLength = prefixLength(this.internalPath);
            this.internalFile = new File(this.internalPath);
        }
    }

    public File getInternalFile() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return this;
        }
        return file;
    }

    public File getParentFile() {
        String p = getParent();
        if (p == null) {
            return null;
        }
        return createInstance(p);
    }

    public String getParent() {
        return super.getParent();
    }

    public File getAbsoluteFile() {
        return createInstance(getAbsolutePath());
    }

    @Override // java.io.File
    public File getCanonicalFile() throws IOException {
        return createInstance(getCanonicalPath());
    }

    public boolean canRead() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.canRead();
        }
        return file.canRead();
    }

    public boolean canWrite() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.canWrite();
        }
        return file.canWrite();
    }

    public boolean exists() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.exists();
        }
        return file.exists();
    }

    public boolean isDirectory() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.isDirectory();
        }
        return file.isDirectory();
    }

    public boolean isFile() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.isFile();
        }
        return file.isFile();
    }

    public boolean isHidden() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.isHidden();
        }
        return file.isHidden();
    }

    public long lastModified() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.lastModified();
        }
        return file.lastModified();
    }

    public long length() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.length();
        }
        return file.length();
    }

    @Override // java.io.File
    public boolean createNewFile() throws IOException {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.createNewFile();
        }
        boolean ret = file.createNewFile();
        if (this.mDoAccessDefalut) {
            refreshSDCardFSCache(super.getAbsolutePath());
        }
        return ret;
    }

    public boolean delete() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.delete();
        }
        boolean ret = file.delete();
        if (this.mDoAccessDefalut) {
            refreshSDCardFSCache(super.getAbsolutePath());
        }
        return ret;
    }

    public void deleteOnExit() {
        super.deleteOnExit();
    }

    public String[] list() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.list();
        }
        return file.list();
    }

    public File[] listFiles() {
        if (!enforceExternalStoragePath()) {
            return super.listFiles();
        }
        String[] ss = list();
        if (ss == null) {
            return null;
        }
        return convertFiles(this, ss);
    }

    @Override // java.io.File
    public File[] listFiles(FilenameFilter filter) {
        if (!enforceExternalStoragePath()) {
            return super.listFiles(filter);
        }
        String[] ss = list();
        if (ss == null) {
            return null;
        }
        ArrayList<String> fileNames = new ArrayList<>();
        for (String s : ss) {
            if (filter == null || filter.accept(this, s)) {
                fileNames.add(s);
            }
        }
        return convertFiles(this, (String[]) fileNames.toArray(new String[fileNames.size()]));
    }

    @Override // java.io.File
    public File[] listFiles(FileFilter filter) {
        if (!enforceExternalStoragePath()) {
            return super.listFiles(filter);
        }
        String[] ss = list();
        if (ss == null) {
            return null;
        }
        ArrayList<String> fileNames = new ArrayList<>();
        for (String s : ss) {
            File f = new File(this, s);
            if (filter == null || filter.accept(f)) {
                fileNames.add(s);
            }
        }
        return convertFiles(this, (String[]) fileNames.toArray(new String[fileNames.size()]));
    }

    public boolean mkdir() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.mkdir();
        }
        return file.mkdir();
    }

    public boolean renameTo(File dest) {
        if (!enforceExternalStoragePath() || this.internalFile == null || dest == null) {
            return super.renameTo(dest);
        }
        boolean ret = this.internalFile.renameTo(new ExternalStorageFileImpl(dest.getPath()).getInternalFile());
        if (this.mDoAccessDefalut) {
            refreshSDCardFSCache(super.getAbsolutePath());
            refreshSDCardFSCache(dest.getAbsolutePath());
        }
        return ret;
    }

    public boolean setLastModified(long time) {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.setLastModified(time);
        }
        return file.setLastModified(time);
    }

    public boolean setReadOnly() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.setReadOnly();
        }
        return file.setReadOnly();
    }

    public boolean setWritable(boolean writable, boolean ownerOnly) {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.setWritable(writable, ownerOnly);
        }
        return file.setWritable(writable, ownerOnly);
    }

    public boolean setReadable(boolean readable, boolean ownerOnly) {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.setReadable(readable, ownerOnly);
        }
        return file.setReadable(readable, ownerOnly);
    }

    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.setExecutable(executable, ownerOnly);
        }
        return file.setExecutable(executable, ownerOnly);
    }

    public boolean canExecute() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.canExecute();
        }
        return file.canExecute();
    }

    public long getTotalSpace() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.getTotalSpace();
        }
        return file.getTotalSpace();
    }

    public long getFreeSpace() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.getFreeSpace();
        }
        return file.getFreeSpace();
    }

    public long getUsableSpace() {
        File file;
        if (!enforceExternalStoragePath() || (file = this.internalFile) == null) {
            return super.getUsableSpace();
        }
        return file.getUsableSpace();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public void acquireSelfRefreshSDCardFSCache() {
        this.mDoAccessDefalut = false;
    }

    public void releaseSelfRefreshSDCardFSCache() {
        this.mDoAccessDefalut = true;
    }

    private void refreshSDCardFSCache(String path) {
        try {
            Os.access(path, OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
    }
}
