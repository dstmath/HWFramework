package com.huawei.server.security.fileprotect;

public class PathPolicy {
    public static final int DIRECTORY_AND_FILES = 17;
    private static final String FILE_PROTECT_TYPE_ECE = "ece";
    private static final String FILE_PROTECT_TYPE_SECE = "sece";
    public static final int STORAGE_ECE_TYPE = 2;
    public static final int STORAGE_INVALID_TYPE = -1;
    public static final int STORAGE_SECE_TYPE = 3;
    public static final int TYPE_DIRECTORY = 16;
    public static final int TYPE_FILE = 0;
    private int mEncryptionType;
    private int mFileType;
    private String mPath;

    PathPolicy(String path, int encryptionType, int fileType) {
        this.mPath = path;
        this.mEncryptionType = encryptionType;
        this.mFileType = fileType;
    }

    PathPolicy(String path, String encryptionType, int fileType) {
        this(path, getStorageType(encryptionType), fileType);
    }

    public String getPath() {
        return this.mPath;
    }

    public int getEncryptionType() {
        return this.mEncryptionType;
    }

    public int getFileType() {
        return this.mFileType;
    }

    private static int getStorageType(String type) {
        if (FILE_PROTECT_TYPE_ECE.equals(type)) {
            return 2;
        }
        if (FILE_PROTECT_TYPE_SECE.equals(type)) {
            return 3;
        }
        return -1;
    }
}
