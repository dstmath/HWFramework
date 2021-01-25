package com.android.internal.os;

import android.os.FileUtils;
import android.util.ArrayMap;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public final class AtomicDirectory {
    private static final int DEFAULT_FD = -1;
    private final File mBackupDirectory;
    private int mBackupDirectoryFd = -1;
    private final File mBaseDirectory;
    private int mBaseDirectoryFd = -1;
    private final ArrayMap<File, FileOutputStream> mOpenFiles = new ArrayMap<>();

    private static native void fsyncDirectoryFd(int i);

    private static native int getDirectoryFd(String str);

    public AtomicDirectory(File baseDirectory) {
        Preconditions.checkNotNull(baseDirectory, "baseDirectory cannot be null");
        this.mBaseDirectory = baseDirectory;
        this.mBackupDirectory = new File(baseDirectory.getPath() + "_bak");
    }

    public File getBackupDirectory() {
        return this.mBackupDirectory;
    }

    public File startRead() throws IOException {
        restore();
        return getOrCreateBaseDirectory();
    }

    public void finishRead() {
        if (this.mBaseDirectory.exists()) {
            closeNativeFd(this.mBaseDirectoryFd);
        }
        if (this.mBackupDirectory.exists()) {
            closeNativeFd(this.mBackupDirectoryFd);
        }
        this.mBaseDirectoryFd = -1;
        this.mBackupDirectoryFd = -1;
    }

    private void closeNativeFd(int fd) {
        FileDescriptor fileDescriptor = new FileDescriptor();
        fileDescriptor.setInt$(fd);
        FileUtils.closeQuietly(fileDescriptor);
    }

    public File startWrite() throws IOException {
        backup();
        return getOrCreateBaseDirectory();
    }

    public FileOutputStream openWrite(File file) throws IOException {
        if (file.isDirectory() || !file.getParentFile().equals(getOrCreateBaseDirectory())) {
            throw new IllegalArgumentException("Must be a file in " + getOrCreateBaseDirectory());
        }
        FileOutputStream destination = new FileOutputStream(file);
        if (this.mOpenFiles.put(file, destination) == null) {
            return destination;
        }
        throw new IllegalArgumentException("Already open file" + file.getCanonicalPath());
    }

    public void closeWrite(FileOutputStream destination) {
        if (this.mOpenFiles.removeAt(this.mOpenFiles.indexOfValue(destination)) != null) {
            FileUtils.sync(destination);
            try {
                destination.close();
            } catch (IOException e) {
            }
        } else {
            throw new IllegalArgumentException("Unknown file stream " + destination);
        }
    }

    public void failWrite(FileOutputStream destination) {
        int indexOfValue = this.mOpenFiles.indexOfValue(destination);
        if (indexOfValue >= 0) {
            this.mOpenFiles.removeAt(indexOfValue);
        }
    }

    public void finishWrite() {
        throwIfSomeFilesOpen();
        fsyncDirectoryFd(this.mBaseDirectoryFd);
        closeNativeFd(this.mBaseDirectoryFd);
        deleteDirectory(this.mBackupDirectory);
        fsyncDirectoryFd(this.mBackupDirectoryFd);
        closeNativeFd(this.mBackupDirectoryFd);
        this.mBaseDirectoryFd = -1;
        this.mBackupDirectoryFd = -1;
    }

    public void failWrite() {
        throwIfSomeFilesOpen();
        try {
            restore();
        } catch (IOException e) {
        }
        this.mBaseDirectoryFd = -1;
        this.mBackupDirectoryFd = -1;
    }

    public boolean exists() {
        return this.mBaseDirectory.exists() || this.mBackupDirectory.exists();
    }

    public void delete() {
        if (this.mBaseDirectory.exists()) {
            deleteDirectory(this.mBaseDirectory);
            fsyncDirectoryFd(this.mBaseDirectoryFd);
            closeNativeFd(this.mBaseDirectoryFd);
            this.mBaseDirectoryFd = -1;
        }
        if (this.mBackupDirectory.exists()) {
            deleteDirectory(this.mBackupDirectory);
            fsyncDirectoryFd(this.mBackupDirectoryFd);
            closeNativeFd(this.mBackupDirectoryFd);
            this.mBackupDirectoryFd = -1;
        }
    }

    private File getOrCreateBaseDirectory() throws IOException {
        if (!this.mBaseDirectory.exists()) {
            if (this.mBaseDirectory.mkdirs()) {
                FileUtils.setPermissions(this.mBaseDirectory.getPath(), 505, -1, -1);
            } else {
                throw new IOException("Couldn't create directory " + this.mBaseDirectory);
            }
        }
        if (this.mBaseDirectoryFd < 0) {
            this.mBaseDirectoryFd = getDirectoryFd(this.mBaseDirectory.getCanonicalPath());
        }
        return this.mBaseDirectory;
    }

    private void throwIfSomeFilesOpen() {
        if (!this.mOpenFiles.isEmpty()) {
            throw new IllegalStateException("Unclosed files: " + Arrays.toString(this.mOpenFiles.keySet().toArray()));
        }
    }

    private void backup() throws IOException {
        if (this.mBaseDirectory.exists()) {
            if (this.mBaseDirectoryFd < 0) {
                this.mBaseDirectoryFd = getDirectoryFd(this.mBaseDirectory.getCanonicalPath());
            }
            if (this.mBackupDirectory.exists()) {
                deleteDirectory(this.mBackupDirectory);
            }
            if (this.mBaseDirectory.renameTo(this.mBackupDirectory)) {
                this.mBackupDirectoryFd = this.mBaseDirectoryFd;
                this.mBaseDirectoryFd = -1;
                fsyncDirectoryFd(this.mBackupDirectoryFd);
                return;
            }
            throw new IOException("Couldn't backup " + this.mBaseDirectory + " to " + this.mBackupDirectory);
        }
    }

    private void restore() throws IOException {
        if (this.mBackupDirectory.exists()) {
            if (this.mBackupDirectoryFd == -1) {
                this.mBackupDirectoryFd = getDirectoryFd(this.mBackupDirectory.getCanonicalPath());
            }
            if (this.mBaseDirectory.exists()) {
                deleteDirectory(this.mBaseDirectory);
            }
            if (this.mBackupDirectory.renameTo(this.mBaseDirectory)) {
                this.mBaseDirectoryFd = this.mBackupDirectoryFd;
                this.mBackupDirectoryFd = -1;
                fsyncDirectoryFd(this.mBaseDirectoryFd);
                return;
            }
            throw new IOException("Couldn't restore " + this.mBackupDirectory + " to " + this.mBaseDirectory);
        }
    }

    private static void deleteDirectory(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteDirectory(child);
            }
        }
        file.delete();
    }
}
