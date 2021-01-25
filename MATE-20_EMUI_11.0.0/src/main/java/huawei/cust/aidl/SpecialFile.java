package huawei.cust.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class SpecialFile implements Parcelable {
    public static final Parcelable.Creator<SpecialFile> CREATOR = new Parcelable.Creator<SpecialFile>() {
        /* class huawei.cust.aidl.SpecialFile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SpecialFile createFromParcel(Parcel in) {
            return new SpecialFile(in);
        }

        @Override // android.os.Parcelable.Creator
        public SpecialFile[] newArray(int size) {
            return new SpecialFile[size];
        }
    };
    private String fileId;
    private String filePath;
    private String value;

    public SpecialFile() {
    }

    public SpecialFile(String filePath2, String fileId2, String value2) {
        this.filePath = filePath2;
        this.fileId = fileId2;
        this.value = value2;
    }

    protected SpecialFile(Parcel in) {
        this(in.readString(), in.readString(), in.readString());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.filePath);
        dest.writeString(this.fileId);
        dest.writeString(this.value);
    }

    public String getFileId() {
        return this.fileId;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getValue() {
        return this.value;
    }

    public void setFilePath(String filePath2) {
        this.filePath = filePath2;
    }

    public void setFileId(String fileId2) {
        this.fileId = fileId2;
    }

    public void setValue(String value2) {
        this.value = value2;
    }

    @Override // java.lang.Object
    public String toString() {
        return "SpecialFile{filePath='" + this.filePath + "', fileId='" + this.fileId + "', value='" + givePrintableMsg(this.value) + "'}";
    }

    private static String givePrintableMsg(String value2) {
        if (value2 == null) {
            return null;
        }
        if (value2.length() <= 6) {
            return value2;
        }
        return value2.substring(0, 6) + "XXXXXXXXXXX";
    }
}
