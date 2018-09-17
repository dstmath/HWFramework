package tmsdk.fg.module.spacemanager;

import java.util.ArrayList;

/* compiled from: Unknown */
public class PhotoSimilarResult {
    public ArrayList<PhotoSimilarBucketItem> mItemList;
    public long mTime;
    public String mTimeString;

    /* compiled from: Unknown */
    public static class PhotoSimilarBucketItem {
        public long mFileSize;
        public long mId;
        public String mPath;
        public boolean mSelected;
    }
}
