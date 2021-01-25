package com.huawei.hiai.awareness.service;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.log.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AwarenessFence implements Parcelable {
    public static final int AND_RELATION = 2;
    public static final Parcelable.Creator<AwarenessFence> CREATOR = new Parcelable.Creator<AwarenessFence>() {
        /* class com.huawei.hiai.awareness.service.AwarenessFence.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AwarenessFence createFromParcel(Parcel in) {
            return new AwarenessFence(in);
        }

        @Override // android.os.Parcelable.Creator
        public AwarenessFence[] newArray(int size) {
            return new AwarenessFence[size];
        }
    };
    public static final int IS_RELATION = 1;
    public static final int NOT_RELATION = 4;
    public static final int OR_RELATION = 3;
    private static final String TAG = "AwarenessFence";
    private int mAction = -1;
    private long mCondition = 0;
    private String mEndTime = null;
    private List<String> mExcludeAppList = null;
    private String mExcludeAppsStr = null;
    private String mFenceKey = null;
    private List<AwarenessFence> mListFences = new ArrayList(16);
    private int mLogic = 1;
    private String mPackageName = null;
    private PendingIntent mPendingOperation = null;
    private String mSecondAction = null;
    private String mStartTime = null;
    private int mStatus = -1;
    private String mTopKey = null;
    private int mType = 0;

    public AwarenessFence(Parcel in) {
        this.mType = in.readInt();
        this.mStatus = in.readInt();
        this.mAction = in.readInt();
        this.mSecondAction = in.readString();
        this.mStartTime = in.readString();
        this.mEndTime = in.readString();
        this.mCondition = in.readLong();
        this.mExcludeAppsStr = in.readString();
        this.mPackageName = in.readString();
        this.mFenceKey = in.readString();
        this.mTopKey = in.readString();
        int appsArrayLength = in.readInt();
        if (appsArrayLength != 0) {
            String[] appsArray = new String[appsArrayLength];
            in.readStringArray(appsArray);
            this.mExcludeAppList = Arrays.asList(appsArray);
        }
    }

    public AwarenessFence(int type, int status, int action, String secondAction) {
        this.mType = type;
        this.mStatus = status;
        this.mAction = action;
        this.mSecondAction = secondAction;
        if (!TextUtils.isEmpty(secondAction)) {
            this.mFenceKey = type + "," + status + "," + action + AwarenessConstants.SECOND_ACTION_SPLITE_TAG + secondAction;
            return;
        }
        this.mFenceKey = type + "," + status + "," + action;
    }

    public AwarenessFence(int logic, List<AwarenessFence> fences) {
        this.mLogic = logic;
        this.mListFences.clear();
        this.mListFences.addAll(fences);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeInt(this.mStatus);
        dest.writeInt(this.mAction);
        dest.writeString(this.mSecondAction);
        dest.writeString(this.mStartTime);
        dest.writeString(this.mEndTime);
        dest.writeLong(this.mCondition);
        dest.writeString(this.mExcludeAppsStr);
        dest.writeString(this.mPackageName);
        dest.writeString(this.mFenceKey);
        dest.writeString(this.mTopKey);
        List<String> list = this.mExcludeAppList;
        if (list != null) {
            String[] appsArray = (String[]) list.toArray(new String[list.size()]);
            dest.writeInt(appsArray.length);
            dest.writeStringArray(appsArray);
            return;
        }
        dest.writeInt(0);
    }

    public void readFromParcel(Parcel in) {
        this.mType = in.readInt();
        this.mStatus = in.readInt();
        this.mAction = in.readInt();
        this.mSecondAction = in.readString();
        this.mStartTime = in.readString();
        this.mEndTime = in.readString();
        this.mCondition = in.readLong();
        this.mExcludeAppsStr = in.readString();
        this.mPackageName = in.readString();
        this.mFenceKey = in.readString();
        this.mTopKey = in.readString();
        int appsArrayLength = in.readInt();
        if (appsArrayLength != 0) {
            String[] appsArray = new String[appsArrayLength];
            in.readStringArray(appsArray);
            this.mExcludeAppList = Arrays.asList(appsArray);
        }
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getAction() {
        return this.mAction;
    }

    public void setAction(int action) {
        this.mAction = action;
    }

    public String getSecondAction() {
        return this.mSecondAction;
    }

    public void setSecondAction(String secondAction) {
        this.mSecondAction = secondAction;
    }

    public String getStartTime() {
        return this.mStartTime;
    }

    public void setStartTime(String startTime) {
        this.mStartTime = startTime;
    }

    public String getEndTime() {
        return this.mEndTime;
    }

    public void setEndTime(String endTime) {
        this.mEndTime = endTime;
    }

    public List<String> getExcludeAppList() {
        return this.mExcludeAppList;
    }

    public void setExcludeAppList(List<String> excludeAppList) {
        this.mExcludeAppList = excludeAppList;
    }

    public long getCondition() {
        return this.mCondition;
    }

    public void setCondition(long condition) {
        this.mCondition = condition;
    }

    public PendingIntent getPendingOperation() {
        return this.mPendingOperation;
    }

    public void setPendingOperation(PendingIntent pendingOperation) {
        this.mPendingOperation = pendingOperation;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public String getFenceKey() {
        return this.mFenceKey;
    }

    public String getLogTopKey() {
        if (this.mType != 11 || TextUtils.isEmpty(this.mTopKey)) {
            return this.mTopKey;
        }
        return this.mTopKey.split(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)[0];
    }

    public String getTopKey() {
        return this.mTopKey;
    }

    public void setTopKey(String topKey) {
        this.mTopKey = topKey;
    }

    public String getExcludeAppsStr() {
        return this.mExcludeAppsStr;
    }

    public void setExcludeAppsStr(String excludeAppsStr) {
        this.mExcludeAppsStr = excludeAppsStr;
    }

    public String getActionString() {
        if (!TextUtils.isEmpty(this.mSecondAction)) {
            return this.mAction + AwarenessConstants.SECOND_ACTION_SPLITE_TAG + this.mSecondAction;
        }
        return this.mAction + "";
    }

    public void build(Context context) {
        if (context != null) {
            this.mPackageName = context.getPackageName();
            String str = this.mPackageName;
            if (str != null && str.contains("_")) {
                this.mPackageName = this.mPackageName.replace("_", "");
            }
            String key = AwarenessParseHelper.parseAwareness2String(this);
            Logger.d(TAG, "build() key = " + key);
            this.mTopKey = this.mPackageName + "_" + key;
            return;
        }
        Logger.e(TAG, "build() context == null");
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getLogic() {
        return this.mLogic;
    }

    public boolean hasRelation() {
        return this.mLogic > 1;
    }

    public List<AwarenessFence> getListFences() {
        return this.mListFences;
    }

    public static AwarenessFence and(List<AwarenessFence> fenceList) {
        return new AwarenessFence(2, fenceList);
    }

    public static AwarenessFence not(List<AwarenessFence> fenceList) {
        return new AwarenessFence(4, fenceList);
    }

    public static AwarenessFence or(List<AwarenessFence> fenceList) {
        return new AwarenessFence(3, fenceList);
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "AwarenessFence{%s, %s, %d, %s}", this.mStartTime, this.mEndTime, Long.valueOf(this.mCondition), getLogTopKey());
    }
}
