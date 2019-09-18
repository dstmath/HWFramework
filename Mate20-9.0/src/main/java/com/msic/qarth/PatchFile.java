package com.msic.qarth;

import java.io.File;

public class PatchFile {
    private static final String TAG = PatchFile.class.getSimpleName();
    private final String fileName;
    private final String path;
    private final String versionCode;
    private final String versionName;

    public static class Builder {
        /* access modifiers changed from: private */
        public String fileName;
        /* access modifiers changed from: private */
        public String path;
        /* access modifiers changed from: private */
        public String versionCode;
        /* access modifiers changed from: private */
        public String versionName;

        public Builder setPath(String path2) {
            this.path = path2;
            return this;
        }

        public Builder setFileName(String name) {
            this.fileName = name;
            return this;
        }

        public Builder setVersionCode(String versionCode2) {
            this.versionCode = versionCode2;
            return this;
        }

        public Builder setVersionName(String versionName2) {
            this.versionName = versionName2;
            return this;
        }

        public PatchFile build() {
            return new PatchFile(this);
        }
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getVersionCode() {
        return this.versionCode;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public String getPath() {
        return this.path;
    }

    public File getFile() {
        return new File(this.path);
    }

    private PatchFile(Builder builder) {
        this.path = builder.path;
        this.fileName = builder.fileName;
        this.versionCode = builder.versionCode;
        this.versionName = builder.versionName;
    }

    public String toString() {
        return "PatchFile:" + "Path:" + this.path + ", FileName:" + this.fileName + ", VersionCode:" + this.versionCode + ", VersionName:" + this.versionName;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof PatchFile)) {
            return false;
        }
        PatchFile other = (PatchFile) obj;
        if (this.path != null ? this.path.equals(other.path) : other.path == null) {
            if (this.fileName != null ? this.fileName.equals(other.fileName) : other.fileName == null) {
                if (this.versionCode != null ? this.versionCode.equals(other.versionCode) : other.versionCode == null) {
                    if (this.versionName != null ? this.versionName.equals(other.versionName) : other.versionName == null) {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public int hashCode() {
        int hashCode = 0;
        if (this.path != null) {
            hashCode = this.path.hashCode();
        }
        if (this.fileName != null) {
            hashCode = (hashCode * 31) + this.fileName.hashCode();
        }
        if (this.versionCode != null) {
            hashCode = (hashCode * 31) + this.versionCode.hashCode();
        }
        if (this.versionName != null) {
            return (hashCode * 31) + this.versionName.hashCode();
        }
        return hashCode;
    }
}
