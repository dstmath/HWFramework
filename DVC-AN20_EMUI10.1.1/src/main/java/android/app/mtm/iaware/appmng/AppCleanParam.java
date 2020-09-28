package android.app.mtm.iaware.appmng;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

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
    private List<Integer> mIntList2;
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
        Integer mUserid;

        public AppCleanInfo(Parcel source) {
            this.mPkgName = source.readString();
            this.mUserid = Integer.valueOf(source.readInt());
            this.mCleanType = Integer.valueOf(source.readInt());
            this.mTaskId = Integer.valueOf(source.readInt());
        }

        public AppCleanInfo(String pkgName, Integer userid, Integer cleanType) {
            this.mPkgName = pkgName;
            this.mUserid = userid;
            this.mCleanType = cleanType;
        }

        public String getPkgName() {
            return this.mPkgName;
        }

        public Integer getUserId() {
            return this.mUserid;
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

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mPkgName);
            dest.writeInt(this.mUserid.intValue());
            dest.writeInt(this.mCleanType.intValue());
            dest.writeInt(this.mTaskId.intValue());
        }
    }

    public static class Builder {
        private int mAction;
        private List<AppCleanInfo> mAppCleanInfoList;
        private Bundle mBundle;
        private List<Integer> mIntList;
        private List<Integer> mIntList2;
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
            this.mIntList2 = intList;
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
        this.mIntList2 = builder.mIntList2;
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

    public List<Integer> getIntList2() {
        if (this.mIntList2 == null) {
            this.mIntList2 = new ArrayList();
        }
        return this.mIntList2;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSource);
        dest.writeInt(this.mAction);
        dest.writeList(this.mStringList);
        dest.writeList(this.mIntList);
        dest.writeList(this.mIntList2);
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
        if (this.mIntList2 == null) {
            this.mIntList2 = new ArrayList();
        }
        in.readList(this.mIntList2, List.class.getClassLoader());
        if (this.mAppCleanInfoList == null) {
            this.mAppCleanInfoList = new ArrayList();
        }
        in.readList(this.mAppCleanInfoList, List.class.getClassLoader());
        this.mLevel = in.readInt();
        this.mTimeStamp = in.readLong();
        this.mKilledCount = in.readInt();
        this.mBundle = in.readBundle();
    }

    public String toString() {
        return "Source = " + this.mSource + ", Action = " + this.mAction + ", Level = " + this.mLevel + ", TimeStamp = " + this.mTimeStamp + ", KilledCount = " + this.mKilledCount + ", Bundle = " + this.mBundle;
    }
}
