package com.huawei.libcore.io;

import android.os.storage.ExternalStorageFileImpl;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

public class ExternalStorageFile extends File {
    private ExternalStorageFileImpl mImpl;

    public ExternalStorageFile(String pathname) {
        super(pathname);
        this.mImpl = new ExternalStorageFileImpl(pathname);
    }

    public ExternalStorageFile(String parent, String child) {
        super(parent, child);
        this.mImpl = new ExternalStorageFileImpl(parent, child);
    }

    public ExternalStorageFile(File parent, String child) {
        super(parent, child);
        this.mImpl = new ExternalStorageFileImpl(parent, child);
    }

    public File getInternalFile() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return this;
        }
        return externalStorageFileImpl.getInternalFile();
    }

    @Override // java.io.File
    public File getParentFile() {
        if (this.mImpl == null) {
            return super.getParentFile();
        }
        String parentPath = getParent();
        if (parentPath == null) {
            return null;
        }
        return new ExternalStorageFile(parentPath);
    }

    @Override // java.io.File
    public String getParent() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.getParent();
        }
        return externalStorageFileImpl.getParent();
    }

    @Override // java.io.File
    public File getAbsoluteFile() {
        if (this.mImpl == null) {
            return super.getAbsoluteFile();
        }
        return new ExternalStorageFile(getAbsolutePath());
    }

    @Override // java.io.File
    public File getCanonicalFile() throws IOException {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.getCanonicalFile();
        }
        return new ExternalStorageFile(externalStorageFileImpl.getCanonicalPath());
    }

    @Override // java.io.File
    public boolean canRead() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.canRead();
        }
        return externalStorageFileImpl.canRead();
    }

    @Override // java.io.File
    public boolean canWrite() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.canWrite();
        }
        return externalStorageFileImpl.canWrite();
    }

    @Override // java.io.File
    public boolean exists() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.exists();
        }
        return externalStorageFileImpl.exists();
    }

    @Override // java.io.File
    public boolean isDirectory() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.isDirectory();
        }
        return externalStorageFileImpl.isDirectory();
    }

    @Override // java.io.File
    public boolean isFile() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.isFile();
        }
        return externalStorageFileImpl.isFile();
    }

    @Override // java.io.File
    public boolean isHidden() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.isHidden();
        }
        return externalStorageFileImpl.isHidden();
    }

    @Override // java.io.File
    public long lastModified() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.lastModified();
        }
        return externalStorageFileImpl.lastModified();
    }

    @Override // java.io.File
    public long length() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.length();
        }
        return externalStorageFileImpl.length();
    }

    @Override // java.io.File
    public boolean createNewFile() throws IOException {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.createNewFile();
        }
        return externalStorageFileImpl.createNewFile();
    }

    @Override // java.io.File
    public boolean delete() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.delete();
        }
        return externalStorageFileImpl.delete();
    }

    @Override // java.io.File
    public void deleteOnExit() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            super.deleteOnExit();
        } else {
            externalStorageFileImpl.deleteOnExit();
        }
    }

    @Override // java.io.File
    public String[] list() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.list();
        }
        return externalStorageFileImpl.list();
    }

    @Override // java.io.File
    public File[] listFiles() {
        File[] listedFiles;
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            listedFiles = super.listFiles();
        } else {
            listedFiles = externalStorageFileImpl.listFiles();
        }
        if (listedFiles == null) {
            return null;
        }
        int n = listedFiles.length;
        File[] fs = new ExternalStorageFile[n];
        for (int i = 0; i < n; i++) {
            fs[i] = new ExternalStorageFile(listedFiles[i].getPath());
        }
        return fs;
    }

    @Override // java.io.File
    public File[] listFiles(FilenameFilter filter) {
        File[] listedFiles;
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            listedFiles = super.listFiles(filter);
        } else {
            listedFiles = externalStorageFileImpl.listFiles(filter);
        }
        if (listedFiles == null) {
            return null;
        }
        int n = listedFiles.length;
        File[] fs = new ExternalStorageFile[n];
        for (int i = 0; i < n; i++) {
            fs[i] = new ExternalStorageFile(listedFiles[i].getPath());
        }
        return fs;
    }

    @Override // java.io.File
    public File[] listFiles(FileFilter filter) {
        File[] listedFiles;
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            listedFiles = super.listFiles(filter);
        } else {
            listedFiles = externalStorageFileImpl.listFiles(filter);
        }
        if (listedFiles == null) {
            return null;
        }
        int n = listedFiles.length;
        File[] fs = new ExternalStorageFile[n];
        for (int i = 0; i < n; i++) {
            fs[i] = new ExternalStorageFile(listedFiles[i].getPath());
        }
        return fs;
    }

    @Override // java.io.File
    public boolean mkdir() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.mkdir();
        }
        return externalStorageFileImpl.mkdir();
    }

    @Override // java.io.File
    public boolean renameTo(File dest) {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.renameTo(dest);
        }
        return externalStorageFileImpl.renameTo(dest);
    }

    @Override // java.io.File
    public boolean setLastModified(long time) {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.setLastModified(time);
        }
        return externalStorageFileImpl.setLastModified(time);
    }

    @Override // java.io.File
    public boolean setReadOnly() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.setReadOnly();
        }
        return externalStorageFileImpl.setReadOnly();
    }

    @Override // java.io.File
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.setWritable(writable, ownerOnly);
        }
        return externalStorageFileImpl.setWritable(writable, ownerOnly);
    }

    @Override // java.io.File
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.setReadable(readable, ownerOnly);
        }
        return externalStorageFileImpl.setReadable(readable, ownerOnly);
    }

    @Override // java.io.File
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.setExecutable(executable, ownerOnly);
        }
        return externalStorageFileImpl.setExecutable(executable, ownerOnly);
    }

    @Override // java.io.File
    public boolean canExecute() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.canExecute();
        }
        return externalStorageFileImpl.canExecute();
    }

    @Override // java.io.File
    public long getTotalSpace() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.getTotalSpace();
        }
        return externalStorageFileImpl.getTotalSpace();
    }

    @Override // java.io.File
    public long getFreeSpace() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.getFreeSpace();
        }
        return externalStorageFileImpl.getFreeSpace();
    }

    @Override // java.io.File
    public long getUsableSpace() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.getUsableSpace();
        }
        return externalStorageFileImpl.getUsableSpace();
    }

    @Override // java.io.File, java.lang.Object
    public boolean equals(Object obj) {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.equals(obj);
        }
        return externalStorageFileImpl.equals(obj);
    }

    @Override // java.io.File, java.lang.Object
    public int hashCode() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl == null) {
            return super.hashCode();
        }
        return externalStorageFileImpl.hashCode();
    }

    public static void refreshSDCardFSCache(String path) {
        try {
            Os.access(path, OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
    }

    public void acquireSelfRefreshSDCardFSCache() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl != null) {
            externalStorageFileImpl.acquireSelfRefreshSDCardFSCache();
        }
    }

    public void releaseSelfRefreshSDCardFSCache() {
        ExternalStorageFileImpl externalStorageFileImpl = this.mImpl;
        if (externalStorageFileImpl != null) {
            externalStorageFileImpl.releaseSelfRefreshSDCardFSCache();
        }
    }
}
