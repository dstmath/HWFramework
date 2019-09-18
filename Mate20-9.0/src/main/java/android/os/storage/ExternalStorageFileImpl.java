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
    private transient StoragePathStatus pathStatus = null;

    private enum StoragePathStatus {
        EXTERNAL,
        EMULATED
    }

    private int prefixLength(String pathname) {
        int i = 0;
        if (pathname.length() == 0) {
            return 0;
        }
        if (pathname.charAt(0) == '/') {
            i = 1;
        }
        return i;
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
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return this;
        }
        return this.internalFile;
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

    public File getCanonicalFile() throws IOException {
        return createInstance(getCanonicalPath());
    }

    public boolean canRead() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.canRead();
        }
        return this.internalFile.canRead();
    }

    public boolean canWrite() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.canWrite();
        }
        return this.internalFile.canWrite();
    }

    public boolean exists() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.exists();
        }
        return this.internalFile.exists();
    }

    public boolean isDirectory() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.isDirectory();
        }
        return this.internalFile.isDirectory();
    }

    public boolean isFile() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.isFile();
        }
        return this.internalFile.isFile();
    }

    public boolean isHidden() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.isHidden();
        }
        return this.internalFile.isHidden();
    }

    public long lastModified() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.lastModified();
        }
        return this.internalFile.lastModified();
    }

    public long length() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.length();
        }
        return this.internalFile.length();
    }

    public boolean createNewFile() throws IOException {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.createNewFile();
        }
        boolean ret = this.internalFile.createNewFile();
        try {
            Os.access(super.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
        return ret;
    }

    public boolean delete() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.delete();
        }
        boolean ret = this.internalFile.delete();
        try {
            Os.access(super.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
        return ret;
    }

    public void deleteOnExit() {
        super.deleteOnExit();
    }

    public String[] list() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.list();
        }
        return this.internalFile.list();
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
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.mkdir();
        }
        return this.internalFile.mkdir();
    }

    public boolean renameTo(File dest) {
        if (!enforceExternalStoragePath() || this.internalFile == null || dest == null) {
            return super.renameTo(dest);
        }
        boolean ret = this.internalFile.renameTo(new ExternalStorageFileImpl(dest.getPath()).getInternalFile());
        try {
            Os.access(super.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
        try {
            Os.access(dest.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e2) {
        }
        return ret;
    }

    public boolean setLastModified(long time) {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.setLastModified(time);
        }
        return this.internalFile.setLastModified(time);
    }

    public boolean setReadOnly() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.setReadOnly();
        }
        return this.internalFile.setReadOnly();
    }

    public boolean setWritable(boolean writable, boolean ownerOnly) {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.setWritable(writable, ownerOnly);
        }
        return this.internalFile.setWritable(writable, ownerOnly);
    }

    public boolean setReadable(boolean readable, boolean ownerOnly) {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.setReadable(readable, ownerOnly);
        }
        return this.internalFile.setReadable(readable, ownerOnly);
    }

    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.setExecutable(executable, ownerOnly);
        }
        return this.internalFile.setExecutable(executable, ownerOnly);
    }

    public boolean canExecute() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.canExecute();
        }
        return this.internalFile.canExecute();
    }

    public long getTotalSpace() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.getTotalSpace();
        }
        return this.internalFile.getTotalSpace();
    }

    public long getFreeSpace() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.getFreeSpace();
        }
        return this.internalFile.getFreeSpace();
    }

    public long getUsableSpace() {
        if (!enforceExternalStoragePath() || this.internalFile == null) {
            return super.getUsableSpace();
        }
        return this.internalFile.getUsableSpace();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }
}
