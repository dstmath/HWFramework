package android.mtp;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class MtpObjectEx {
    private int mFormat;
    private int mId;
    private boolean mIsDir;
    private long mModifiedTime;
    private String mPath;
    private long mSize;
    private int mStrorageId;
    private String name;
    private int parentGetId;

    public static class Params {
        public int format = -1;
        public int id = -1;
        public boolean isDir = false;
        public String name = "";
        public int parentGetId = -1;
        public String path = "";
        public long size = 0;
        public int storageId = -1;
        public long time = 0;
    }

    public MtpObjectEx(Params params) {
        this.mId = params.id;
        this.mStrorageId = params.storageId;
        this.parentGetId = params.parentGetId;
        this.mSize = params.size;
        this.mModifiedTime = params.time;
        this.name = params.name;
        this.mFormat = params.format;
        this.mPath = params.path;
        this.mIsDir = params.isDir;
    }

    public int getId() {
        return this.mId;
    }

    public int getStorageId() {
        return this.mStrorageId;
    }

    public int getParentId() {
        return this.parentGetId;
    }

    public long getSize() {
        return this.mSize;
    }

    public long getModifiedTime() {
        return this.mModifiedTime;
    }

    public String getName() {
        return this.name;
    }

    public int getFormat() {
        return this.mFormat;
    }

    public String getPath() {
        return this.mPath;
    }

    public boolean isDirc() {
        return this.mIsDir;
    }
}
