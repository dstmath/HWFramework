package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CmpTypeInfo implements Parcelable {
    public static final int CMP_TYPE_FAKEACTIVITY = 1;
    public static final int CMP_TYPE_INVALIDALARM = 4;
    public static final int CMP_TYPE_PUSHSDK = 0;
    public static final int CMP_TYPE_REALFGACTIVITY = 2;
    public static final int CMP_TYPE_UNKNOWN = -1;
    public static final int CMP_TYPE_VALIDALARM = 3;
    public static final Creator<CmpTypeInfo> CREATOR = new Creator<CmpTypeInfo>() {
        public CmpTypeInfo createFromParcel(Parcel source) {
            return new CmpTypeInfo(source);
        }

        public CmpTypeInfo[] newArray(int size) {
            return new CmpTypeInfo[size];
        }
    };
    private String cls;
    private long createTime;
    private int perception_count = 0;
    private String pkgName = "";
    private int type;
    private int unperception_count = 0;

    public CmpTypeInfo(Parcel source) {
        this.type = source.readInt();
        this.pkgName = source.readString();
        this.cls = source.readString();
        this.createTime = source.readLong();
        this.perception_count = source.readInt();
        this.unperception_count = source.readInt();
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getCls() {
        return this.cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public long getTime() {
        return this.createTime;
    }

    public void setTime(long time) {
        this.createTime = time;
    }

    public int getPerceptionCount() {
        return this.perception_count;
    }

    public void setPerceptionCount(int count) {
        this.perception_count = count;
    }

    public int getUnPerceptionCount() {
        return this.unperception_count;
    }

    public void setUnPerceptionCount(int count) {
        this.unperception_count = count;
    }

    public String getCmp() {
        return this.pkgName + "/" + this.cls;
    }

    public String toString() {
        return "CmpTypeInfo [type=" + this.type + ", pkg=" + this.pkgName + ", cls=" + this.cls + ", time=" + this.createTime + ", perception_count=" + this.perception_count + ", unperception_count=" + this.unperception_count + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.pkgName);
        dest.writeString(this.cls);
        dest.writeLong(this.createTime);
        dest.writeInt(this.perception_count);
        dest.writeInt(this.unperception_count);
    }
}
