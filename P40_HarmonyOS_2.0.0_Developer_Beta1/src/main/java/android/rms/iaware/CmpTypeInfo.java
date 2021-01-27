package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.hwpartiaware.BuildConfig;

public class CmpTypeInfo implements Parcelable {
    public static final int CMP_TYPE_FAKE_ACTIVITY = 1;
    public static final int CMP_TYPE_GOOD_SERVICE = 5;
    public static final int CMP_TYPE_INVALID_ALARM = 4;
    public static final int CMP_TYPE_PUSH_SDK = 0;
    public static final int CMP_TYPE_REAL_FG_ACTIVITY = 2;
    public static final int CMP_TYPE_UNKNOWN = -1;
    public static final int CMP_TYPE_VALID_ALARM = 3;
    public static final Parcelable.Creator<CmpTypeInfo> CREATOR = new Parcelable.Creator<CmpTypeInfo>() {
        /* class android.rms.iaware.CmpTypeInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CmpTypeInfo createFromParcel(Parcel source) {
            return new CmpTypeInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public CmpTypeInfo[] newArray(int size) {
            return new CmpTypeInfo[size];
        }
    };
    private String cls;
    private long createTime;
    private int perceptionCount = 0;
    private String pkgName = BuildConfig.FLAVOR;
    private int type;
    private int unperceptionCount = 0;
    private int userId = 0;

    public CmpTypeInfo() {
    }

    public CmpTypeInfo(Parcel source) {
        this.type = source.readInt();
        this.pkgName = source.readString();
        this.cls = source.readString();
        this.createTime = source.readLong();
        this.perceptionCount = source.readInt();
        this.unperceptionCount = source.readInt();
        this.userId = source.readInt();
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
    }

    public String getCls() {
        return this.cls;
    }

    public void setCls(String cls2) {
        this.cls = cls2;
    }

    public long getTime() {
        return this.createTime;
    }

    public void setTime(long time) {
        this.createTime = time;
    }

    public int getPerceptionCount() {
        return this.perceptionCount;
    }

    public void setPerceptionCount(int count) {
        this.perceptionCount = count;
    }

    public int getUnPerceptionCount() {
        return this.unperceptionCount;
    }

    public void setUnPerceptionCount(int count) {
        this.unperceptionCount = count;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId2) {
        this.userId = userId2;
    }

    public String getCmp() {
        return this.pkgName + "/" + this.cls;
    }

    @Override // java.lang.Object
    public String toString() {
        return "CmpTypeInfo [type=" + this.type + ", userId=" + this.userId + ", pkg=" + this.pkgName + ", cls=" + this.cls + ", time=" + this.createTime + ", perception_count=" + this.perceptionCount + ", unperception_count=" + this.unperceptionCount + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.pkgName);
        dest.writeString(this.cls);
        dest.writeLong(this.createTime);
        dest.writeInt(this.perceptionCount);
        dest.writeInt(this.unperceptionCount);
        dest.writeInt(this.userId);
    }
}
