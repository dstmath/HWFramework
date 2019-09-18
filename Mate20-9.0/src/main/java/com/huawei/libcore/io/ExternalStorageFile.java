package com.huawei.libcore.io;

import android.os.storage.ExternalStorageFileImpl;
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
        if (this.mImpl == null) {
            return this;
        }
        return this.mImpl.getInternalFile();
    }

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

    public String getParent() {
        if (this.mImpl == null) {
            return super.getParent();
        }
        return this.mImpl.getParent();
    }

    public File getAbsoluteFile() {
        if (this.mImpl == null) {
            return super.getAbsoluteFile();
        }
        return new ExternalStorageFile(getAbsolutePath());
    }

    public File getCanonicalFile() throws IOException {
        if (this.mImpl == null) {
            return super.getCanonicalFile();
        }
        return new ExternalStorageFile(this.mImpl.getCanonicalPath());
    }

    public boolean canRead() {
        if (this.mImpl == null) {
            return super.canRead();
        }
        return this.mImpl.canRead();
    }

    public boolean canWrite() {
        if (this.mImpl == null) {
            return super.canWrite();
        }
        return this.mImpl.canWrite();
    }

    public boolean exists() {
        if (this.mImpl == null) {
            return super.exists();
        }
        return this.mImpl.exists();
    }

    public boolean isDirectory() {
        if (this.mImpl == null) {
            return super.isDirectory();
        }
        return this.mImpl.isDirectory();
    }

    public boolean isFile() {
        if (this.mImpl == null) {
            return super.isFile();
        }
        return this.mImpl.isFile();
    }

    public boolean isHidden() {
        if (this.mImpl == null) {
            return super.isHidden();
        }
        return this.mImpl.isHidden();
    }

    public long lastModified() {
        if (this.mImpl == null) {
            return super.lastModified();
        }
        return this.mImpl.lastModified();
    }

    public long length() {
        if (this.mImpl == null) {
            return super.length();
        }
        return this.mImpl.length();
    }

    public boolean createNewFile() throws IOException {
        if (this.mImpl == null) {
            return super.createNewFile();
        }
        return this.mImpl.createNewFile();
    }

    public boolean delete() {
        if (this.mImpl == null) {
            return super.delete();
        }
        return this.mImpl.delete();
    }

    public void deleteOnExit() {
        if (this.mImpl == null) {
            super.deleteOnExit();
        } else {
            this.mImpl.deleteOnExit();
        }
    }

    public String[] list() {
        if (this.mImpl == null) {
            return super.list();
        }
        return this.mImpl.list();
    }

    public File[] listFiles() {
        File[] listedFiles;
        if (this.mImpl == null) {
            listedFiles = super.listFiles();
        } else {
            listedFiles = this.mImpl.listFiles();
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

    public File[] listFiles(FilenameFilter filter) {
        File[] listedFiles;
        if (this.mImpl == null) {
            listedFiles = super.listFiles(filter);
        } else {
            listedFiles = this.mImpl.listFiles(filter);
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

    public File[] listFiles(FileFilter filter) {
        File[] listedFiles;
        if (this.mImpl == null) {
            listedFiles = super.listFiles(filter);
        } else {
            listedFiles = this.mImpl.listFiles(filter);
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

    public boolean mkdir() {
        if (this.mImpl == null) {
            return super.mkdir();
        }
        return this.mImpl.mkdir();
    }

    public boolean renameTo(File dest) {
        if (this.mImpl == null) {
            return super.renameTo(dest);
        }
        return this.mImpl.renameTo(dest);
    }

    public boolean setLastModified(long time) {
        if (this.mImpl == null) {
            return super.setLastModified(time);
        }
        return this.mImpl.setLastModified(time);
    }

    public boolean setReadOnly() {
        if (this.mImpl == null) {
            return super.setReadOnly();
        }
        return this.mImpl.setReadOnly();
    }

    public boolean setWritable(boolean writable, boolean ownerOnly) {
        if (this.mImpl == null) {
            return super.setWritable(writable, ownerOnly);
        }
        return this.mImpl.setWritable(writable, ownerOnly);
    }

    public boolean setReadable(boolean readable, boolean ownerOnly) {
        if (this.mImpl == null) {
            return super.setReadable(readable, ownerOnly);
        }
        return this.mImpl.setReadable(readable, ownerOnly);
    }

    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        if (this.mImpl == null) {
            return super.setExecutable(executable, ownerOnly);
        }
        return this.mImpl.setExecutable(executable, ownerOnly);
    }

    public boolean canExecute() {
        if (this.mImpl == null) {
            return super.canExecute();
        }
        return this.mImpl.canExecute();
    }

    public long getTotalSpace() {
        if (this.mImpl == null) {
            return super.getTotalSpace();
        }
        return this.mImpl.getTotalSpace();
    }

    public long getFreeSpace() {
        if (this.mImpl == null) {
            return super.getFreeSpace();
        }
        return this.mImpl.getFreeSpace();
    }

    public long getUsableSpace() {
        if (this.mImpl == null) {
            return super.getUsableSpace();
        }
        return this.mImpl.getUsableSpace();
    }

    public boolean equals(Object obj) {
        if (this.mImpl == null) {
            return super.equals(obj);
        }
        return this.mImpl.equals(obj);
    }

    public int hashCode() {
        if (this.mImpl == null) {
            return super.hashCode();
        }
        return this.mImpl.hashCode();
    }
}
