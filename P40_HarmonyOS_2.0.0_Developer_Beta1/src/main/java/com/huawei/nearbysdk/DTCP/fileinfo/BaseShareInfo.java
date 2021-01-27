package com.huawei.nearbysdk.DTCP.fileinfo;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.HwLog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class BaseShareInfo implements Parcelable, Serializable, IDTCPSerialize {
    public static final Parcelable.Creator<BaseShareInfo> CREATOR = new BaseShareInfoCreate();
    public static final String TAG = "BaseShareInfoFilesInfo";
    protected int mInfoType;
    BasePreviewInfo mPreviewInfo;

    protected BaseShareInfo() {
    }

    protected BaseShareInfo(BasePreviewInfo basePreviewInfo) {
        this.mPreviewInfo = basePreviewInfo;
    }

    public BasePreviewInfo getPreviewInfo() {
        return this.mPreviewInfo;
    }

    public void setPreviewInfo(BasePreviewInfo previewInfo) {
        this.mPreviewInfo = previewInfo;
    }

    public int getInfoType() {
        return this.mInfoType;
    }

    /* access modifiers changed from: protected */
    public void setInfoType(int type) {
        this.mInfoType = type;
    }

    @Override // java.lang.Object
    public String toString() {
        return this.mPreviewInfo.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        this.mPreviewInfo.writeToParcel(dest, flags);
    }

    protected BaseShareInfo(Parcel in) {
        int type = in.readInt();
        switch (type) {
            case 1:
                this.mPreviewInfo = new MediaPreviewInfo(in);
                return;
            case 2:
                this.mPreviewInfo = new FilePreviewInfo(in);
                return;
            case 3:
                this.mPreviewInfo = new TextPreviewInfo(in);
                return;
            default:
                throw new ShareInfoException("Preview info type " + type + " not found!");
        }
    }

    static class BaseShareInfoCreate implements Parcelable.Creator<BaseShareInfo> {
        BaseShareInfoCreate() {
        }

        @Override // android.os.Parcelable.Creator
        public BaseShareInfo createFromParcel(Parcel source) {
            int type = source.readInt();
            switch (type) {
                case 5:
                    return new TextShareInfo(source);
                case 6:
                    return new FileShareInfo(source);
                case 7:
                    return new FileShareInfo(source);
                default:
                    throw new ShareInfoException("Share info type " + type + " not found!");
            }
        }

        @Override // android.os.Parcelable.Creator
        public BaseShareInfo[] newArray(int size) {
            return new BaseShareInfo[size];
        }
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void writeToDTCPStream(DataOutputStream dos, int dtcpVersion) throws IOException {
        HwLog.e(TAG, "dtcpVersion is " + dtcpVersion);
        if (this.mPreviewInfo != null) {
            HwLog.d(TAG, "PreviewInfoType is " + this.mPreviewInfo.getInfoType());
            dos.writeInt(this.mPreviewInfo.getInfoType());
            this.mPreviewInfo.writeToDTCPStream(dos, dtcpVersion);
            return;
        }
        dos.writeInt(0);
    }

    @Override // com.huawei.nearbysdk.DTCP.fileinfo.IDTCPSerialize
    public void readFromDTCPStream(DataInputStream dis, int dtcpVersion) throws IOException {
        int previewType = dis.readInt();
        HwLog.d(TAG, "previewType is " + previewType);
        switch (previewType) {
            case 1:
                this.mPreviewInfo = new MediaPreviewInfo(dtcpVersion);
                break;
            case 2:
                this.mPreviewInfo = new FilePreviewInfo(dtcpVersion);
                break;
            case 3:
                this.mPreviewInfo = new TextPreviewInfo();
                break;
            default:
                HwLog.e(TAG, "Unsupport preview info type!");
                this.mPreviewInfo = null;
                return;
        }
        this.mPreviewInfo.readFromDTCPStream(dis, dtcpVersion);
    }

    public static class ShareInfoException extends RuntimeException {
        public ShareInfoException(Exception ex) {
            super(ex);
        }

        public ShareInfoException(String message) {
            super(message);
        }
    }
}
