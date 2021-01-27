package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;

public class NearbySendBean implements Parcelable {
    public static final Parcelable.Creator<NearbySendBean> CREATOR = new Parcelable.Creator<NearbySendBean>() {
        /* class com.huawei.nearbysdk.NearbySendBean.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearbySendBean createFromParcel(Parcel source) {
            if (source == null) {
                HwLog.e(NearbySendBean.TAG, "Input param source is null.");
                return null;
            }
            int sendType = source.readInt();
            long totalSize = source.readLong();
            long totalNum = source.readLong();
            String selectedFileUri = source.readString();
            String selectedFileId = source.readString();
            String albumCountUri = source.readString();
            String albumItemsUri = source.readString();
            NearbySendBean sendBean = new NearbySendBean(sendType, totalSize, totalNum, selectedFileUri, selectedFileId);
            sendBean.setAlbumCountUri(albumCountUri);
            sendBean.setAlbumItemsUri(albumItemsUri);
            return sendBean;
        }

        @Override // android.os.Parcelable.Creator
        public NearbySendBean[] newArray(int size) {
            return new NearbySendBean[size];
        }
    };
    private static final int DEFAULT_SIZE = 16;
    private static final String TAG = "NearbySendBean";
    private String mAlbumCountUri;
    private String mAlbumItemsUri;
    private String mSelectedFileId;
    private String mSelectedFileUri;
    private int mSendType;
    private long mTotalNum;
    private long mTotalSize;

    public NearbySendBean(int sendType, long totalSize, long totalNum, String selectedFileUri, String selectedFileId) {
        this.mSendType = sendType;
        this.mTotalSize = totalSize;
        this.mTotalNum = totalNum;
        this.mSelectedFileUri = selectedFileUri;
        this.mSelectedFileId = selectedFileId;
    }

    public int getSendType() {
        return this.mSendType;
    }

    public void setSendType(int sendType) {
        this.mSendType = sendType;
    }

    public long getTotalSize() {
        return this.mTotalSize;
    }

    public void setTotalSize(long totalSize) {
        this.mTotalSize = totalSize;
    }

    public long getTotalNum() {
        return this.mTotalNum;
    }

    public void setTotalNum(long totalNum) {
        this.mTotalNum = totalNum;
    }

    public String getSelectedFileUri() {
        return this.mSelectedFileUri;
    }

    public void setSelectedFileUri(String selectedFileUri) {
        this.mSelectedFileUri = selectedFileUri;
    }

    public String getSelectedFileId() {
        return this.mSelectedFileId;
    }

    public void setSelectedFileId(String selectedFileId) {
        this.mSelectedFileId = selectedFileId;
    }

    public String getAlbumCountUri() {
        return this.mAlbumCountUri;
    }

    public void setAlbumCountUri(String albumCountUri) {
        this.mAlbumCountUri = albumCountUri;
    }

    public String getAlbumItemsUri() {
        return this.mAlbumItemsUri;
    }

    public void setAlbumItemsUri(String albumItemsUri) {
        this.mAlbumItemsUri = albumItemsUri;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null) {
            HwLog.e(TAG, "Input param dest is null.");
            return;
        }
        dest.writeInt(this.mSendType);
        dest.writeLong(this.mTotalSize);
        dest.writeLong(this.mTotalNum);
        dest.writeString(this.mSelectedFileUri == null ? BuildConfig.FLAVOR : this.mSelectedFileUri);
        dest.writeString(this.mSelectedFileId == null ? BuildConfig.FLAVOR : this.mSelectedFileId);
        dest.writeString(this.mAlbumCountUri == null ? BuildConfig.FLAVOR : this.mAlbumCountUri);
        dest.writeString(this.mAlbumItemsUri == null ? BuildConfig.FLAVOR : this.mAlbumItemsUri);
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder((int) DEFAULT_SIZE);
        stringBuilder.append(TAG);
        stringBuilder.append(" sendType :");
        stringBuilder.append(this.mSendType);
        stringBuilder.append(" totalSize :");
        stringBuilder.append(this.mTotalSize);
        stringBuilder.append(" totalNum: ");
        stringBuilder.append(this.mTotalNum);
        return stringBuilder.toString();
    }
}
