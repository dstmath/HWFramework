package tmsdk.fg.module.spacemanager;

import java.util.ArrayList;

public class PhotoSimilarResult {
    public ArrayList<PhotoSimilarBucketItem> mItemList;
    public long mTime;
    public String mTimeString;

    public static class PhotoSimilarBucketItem {
        public double mBlurValue;
        public long mFileSize;
        public long mId;
        public String mPath;
        public boolean mSelected;
    }
}
