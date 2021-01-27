package com.msic.qarth;

import java.io.File;

public class PatchFile {
    private static final String TAG = PatchFile.class.getSimpleName();
    private final String fileName;
    private final String path;
    private final String versionCode;
    private final String versionName;

    public static class Builder {
        private String fileName;
        private String path;
        private String versionCode;
        private String versionName;

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
        return "PatchFile:Path:" + this.path + ", FileName:" + this.fileName + ", VersionCode:" + this.versionCode + ", VersionName:" + this.versionName;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PatchFile)) {
            return false;
        }
        PatchFile other = (PatchFile) obj;
        String str = this.path;
        if (str == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!str.equals(other.path)) {
            return false;
        }
        String str2 = this.fileName;
        if (str2 == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!str2.equals(other.fileName)) {
            return false;
        }
        String str3 = this.versionCode;
        if (str3 == null) {
            if (other.versionCode != null) {
                return false;
            }
        } else if (!str3.equals(other.versionCode)) {
            return false;
        }
        String str4 = this.versionName;
        if (str4 == null) {
            if (other.versionName != null) {
                return false;
            }
        } else if (!str4.equals(other.versionName)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = 0;
        String str = this.path;
        if (str != null) {
            hashCode = str.hashCode();
        }
        String str2 = this.fileName;
        if (str2 != null) {
            hashCode = (hashCode * 31) + str2.hashCode();
        }
        String str3 = this.versionCode;
        if (str3 != null) {
            hashCode = (hashCode * 31) + str3.hashCode();
        }
        String str4 = this.versionName;
        if (str4 != null) {
            return (hashCode * 31) + str4.hashCode();
        }
        return hashCode;
    }
}
