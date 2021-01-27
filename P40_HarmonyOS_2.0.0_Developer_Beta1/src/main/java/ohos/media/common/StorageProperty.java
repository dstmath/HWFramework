package ohos.media.common;

import java.io.File;
import java.io.FileDescriptor;

public final class StorageProperty {
    public static final FileDescriptor FD_INIT = null;
    public static final File FILE_INIT = null;
    public static final int MAX_DURATION_MS_INIT = -1;
    public static final long MAX_FILE_SIZE_BYTES_INIT = -1;
    public static final String PATH_INIT = null;
    private FileDescriptor recorderFd;
    private File recorderFile;
    private int recorderMaxDurationMs;
    private long recorderMaxFileSizeBytes;
    private String recorderPath;

    private StorageProperty() {
        this.recorderMaxDurationMs = -1;
        this.recorderMaxFileSizeBytes = -1;
        this.recorderFd = FD_INIT;
        this.recorderFile = FILE_INIT;
        this.recorderPath = PATH_INIT;
    }

    public int getRecorderMaxDurationMs() {
        return this.recorderMaxDurationMs;
    }

    public long getRecorderMaxFileSizeBytes() {
        return this.recorderMaxFileSizeBytes;
    }

    public FileDescriptor getRecorderFd() {
        return this.recorderFd;
    }

    public File getRecorderFile() {
        return this.recorderFile;
    }

    public String getRecorderPath() {
        return this.recorderPath;
    }

    public static class Builder {
        private FileDescriptor recorderFd = StorageProperty.FD_INIT;
        private File recorderFile = StorageProperty.FILE_INIT;
        private int recorderMaxDurationMs = -1;
        private long recorderMaxFileSizeBytes = -1;
        private String recorderPath = StorageProperty.PATH_INIT;

        public Builder() {
        }

        public Builder(StorageProperty storageProperty) {
            this.recorderMaxDurationMs = storageProperty.recorderMaxDurationMs;
            this.recorderMaxFileSizeBytes = storageProperty.recorderMaxFileSizeBytes;
            this.recorderFd = storageProperty.recorderFd;
            this.recorderFile = storageProperty.recorderFile;
            this.recorderPath = storageProperty.recorderPath;
        }

        public StorageProperty build() {
            StorageProperty storageProperty = new StorageProperty();
            storageProperty.recorderMaxDurationMs = this.recorderMaxDurationMs;
            storageProperty.recorderMaxFileSizeBytes = this.recorderMaxFileSizeBytes;
            storageProperty.recorderFd = this.recorderFd;
            storageProperty.recorderFile = this.recorderFile;
            storageProperty.recorderPath = this.recorderPath;
            return storageProperty;
        }

        public Builder setRecorderMaxDurationMs(int i) {
            this.recorderMaxDurationMs = i;
            return this;
        }

        public Builder setRecorderMaxFileSizeBytes(long j) {
            this.recorderMaxFileSizeBytes = j;
            return this;
        }

        public Builder setRecorderFd(FileDescriptor fileDescriptor) {
            this.recorderFd = fileDescriptor;
            return this;
        }

        public Builder setRecorderFile(File file) {
            this.recorderFile = file;
            return this;
        }

        public Builder setRecorderPath(String str) {
            this.recorderPath = str;
            return this;
        }
    }
}
