package android.app.mtm.iaware.appmng;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class AppCleanParam implements Parcelable {
    public static final Parcelable.Creator<AppCleanParam> CREATOR = new Parcelable.Creator<AppCleanParam>() {
        /* class android.app.mtm.iaware.appmng.AppCleanParam.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppCleanParam createFromParcel(Parcel in) {
            return new AppCleanParam(in);
        }

        @Override // android.os.Parcelable.Creator
        public AppCleanParam[] newArray(int size) {
            return new AppCleanParam[size];
        }
    };
    private int mAction;
    private List<AppCleanInfo> mAppCleanInfoList;
    private Bundle mBundle;
    private List<Integer> mIntList;
    private List<Integer> mIntListEx;
    private int mKilledCount;
    private int mLevel;
    private int mSource;
    private List<String> mStringList;
    private long mTimeStamp;

    public static class AppCleanInfo implements Parcelable {
        public static final Parcelable.Creator<AppCleanInfo> CREATOR = new Parcelable.Creator<AppCleanInfo>() {
            /* class android.app.mtm.iaware.appmng.AppCleanParam.AppCleanInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public AppCleanInfo createFromParcel(Parcel source) {
                return new AppCleanInfo(source);
            }

            @Override // android.os.Parcelable.Creator
            public AppCleanInfo[] newArray(int size) {
                return new AppCleanInfo[size];
            }
        };
        Integer mCleanType;
        String mPkgName;
        Integer mTaskId = -1;
        Integer mUserId;

        public AppCleanInfo(Parcel source) {
            this.mPkgName = source.readString();
            this.mUserId = Integer.valueOf(source.readInt());
            this.mCleanType = Integer.valueOf(source.readInt());
            this.mTaskId = Integer.valueOf(source.readInt());
        }

        public AppCleanInfo(String pkgName, Integer userId, Integer cleanType) {
            this.mPkgName = pkgName;
            this.mUserId = userId;
            this.mCleanType = cleanType;
        }

        public String getPkgName() {
            return this.mPkgName;
        }

        public Integer getUserId() {
            return this.mUserId;
        }

        public Integer getCleanType() {
            return this.mCleanType;
        }

        public void setTaskId(Integer taskId) {
            this.mTaskId = taskId;
        }

        public Integer getTaskId() {
            return this.mTaskId;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mPkgName);
            dest.writeInt(this.mUserId.intValue());
            dest.writeInt(this.mCleanType.intValue());
            dest.writeInt(this.mTaskId.intValue());
        }
    }

    public static class Builder {
        private int mAction;
        private List<AppCleanInfo> mAppCleanInfoList;
        private Bundle mBundle;
        private List<Integer> mIntList;
        private List<Integer> mIntListEx;
        private int mKilledCount;
        private int mLevel;
        private int mSource;
        private List<String> mStringList;
        private long mTimeStamp;

        public Builder(int source) {
            this.mSource = source;
        }

        public Builder level(int level) {
            this.mLevel = level;
            return this;
        }

        public Builder timeStamp(long timeStamp) {
            this.mTimeStamp = timeStamp;
            return this;
        }

        public Builder killedCount(int killedCount) {
            this.mKilledCount = killedCount;
            return this;
        }

        public Builder action(int action) {
            this.mAction = action;
            return this;
        }

        public Builder stringList(List<String> stringList) {
            this.mStringList = stringList;
            return this;
        }

        public Builder intList(List<Integer> intList) {
            this.mIntList = intList;
            return this;
        }

        public Builder intList2(List<Integer> intList) {
            this.mIntListEx = intList;
            return this;
        }

        public Builder appCleanInfoList(List<AppCleanInfo> appCleanInfoList) {
            this.mAppCleanInfoList = appCleanInfoList;
            return this;
        }

        public Builder bundle(Bundle bundle) {
            this.mBundle = bundle;
            return this;
        }

        public AppCleanParam build() {
            return new AppCleanParam(this);
        }
    }

    private AppCleanParam(Builder builder) {
        this.mSource = builder.mSource;
        this.mAction = builder.mAction;
        this.mStringList = builder.mStringList;
        this.mIntList = builder.mIntList;
        this.mIntListEx = builder.mIntListEx;
        this.mAppCleanInfoList = builder.mAppCleanInfoList;
        this.mLevel = builder.mLevel;
        this.mTimeStamp = builder.mTimeStamp;
        this.mKilledCount = builder.mKilledCount;
        this.mBundle = builder.mBundle;
    }

    public int getSource() {
        return this.mSource;
    }

    public int getAction() {
        return this.mAction;
    }

    public List<String> getStringList() {
        if (this.mStringList == null) {
            this.mStringList = new ArrayList();
        }
        return this.mStringList;
    }

    public List<Integer> getIntList() {
        if (this.mIntList == null) {
            this.mIntList = new ArrayList();
        }
        return this.mIntList;
    }

    public List<Integer> getIntListEx() {
        if (this.mIntListEx == null) {
            this.mIntListEx = new ArrayList();
        }
        return this.mIntListEx;
    }

    public List<AppCleanInfo> getAppCleanInfoList() {
        if (this.mAppCleanInfoList == null) {
            this.mAppCleanInfoList = new ArrayList();
        }
        return this.mAppCleanInfoList;
    }

    public int getLevel() {
        return this.mLevel;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public int getKilledCount() {
        return this.mKilledCount;
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    private AppCleanParam(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSource);
        dest.writeInt(this.mAction);
        dest.writeList(this.mStringList);
        dest.writeList(this.mIntList);
        dest.writeList(this.mIntListEx);
        dest.writeList(this.mAppCleanInfoList);
        dest.writeInt(this.mLevel);
        dest.writeLong(this.mTimeStamp);
        dest.writeInt(this.mKilledCount);
        dest.writeBundle(this.mBundle);
    }

    public final void readFromParcel(Parcel in) {
        this.mSource = in.readInt();
        this.mAction = in.readInt();
        if (this.mStringList == null) {
            this.mStringList = new ArrayList();
        }
        in.readList(this.mStringList, List.class.getClassLoader());
        if (this.mIntList == null) {
            this.mIntList = new ArrayList();
        }
        in.readList(this.mIntList, List.class.getClassLoader());
        if (this.mIntListEx == null) {
            this.mIntListEx = new ArrayList();
        }
        in.readList(this.mIntListEx, List.class.getClassLoader());
        if (this.mAppCleanInfoList == null) {
            this.mAppCleanInfoList = new ArrayList();
        }
        in.readList(this.mAppCleanInfoList, List.class.getClassLoader());
        this.mLevel = in.readInt();
        this.mTimeStamp = in.readLong();
        this.mKilledCount = in.readInt();
        this.mBundle = in.readBundle();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Source = " + this.mSource + ", Action = " + this.mAction + ", Level = " + this.mLevel + ", TimeStamp = " + this.mTimeStamp + ", KilledCount = " + this.mKilledCount + ", Bundle = " + this.mBundle;
    }
}
