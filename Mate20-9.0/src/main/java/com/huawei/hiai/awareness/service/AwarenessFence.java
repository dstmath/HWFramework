package com.huawei.hiai.awareness.service;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.common.log.LogUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AwarenessFence implements Parcelable {
    public static final int AND_RELATION = 2;
    public static final Parcelable.Creator<AwarenessFence> CREATOR = new Parcelable.Creator<AwarenessFence>() {
        public AwarenessFence createFromParcel(Parcel in) {
            return new AwarenessFence(in);
        }

        public AwarenessFence[] newArray(int size) {
            return new AwarenessFence[size];
        }
    };
    public static final int IS_RELATION = 1;
    public static final int NOT_RELATION = 4;
    public static final int OR_RELATION = 3;
    private static final String TAG = "AwarenessFence";
    private int action = -1;
    private String checkKey = null;
    private Long condition = -1L;
    private String endTime = null;
    private List<String> excludeAppList = null;
    private String excludeAppsStr = null;
    private List<AwarenessFence> listFences = new ArrayList();
    private int logic = 1;
    private PendingIntent operationPI = null;
    private String packageName = null;
    private String secondAction = null;
    private String startTime = null;
    private int status = -1;
    private String topKey = null;
    private int type = -1;

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.status);
        dest.writeInt(this.action);
        dest.writeString(this.secondAction);
        dest.writeString(this.startTime);
        dest.writeString(this.endTime);
        dest.writeLong(this.condition.longValue());
        dest.writeString(this.excludeAppsStr);
        dest.writeString(this.packageName);
        dest.writeString(this.checkKey);
        dest.writeString(this.topKey);
        if (this.excludeAppList != null) {
            String[] appsArray = (String[]) this.excludeAppList.toArray(new String[0]);
            dest.writeInt(appsArray.length);
            dest.writeStringArray(appsArray);
            return;
        }
        dest.writeInt(0);
    }

    public void readFromParcel(Parcel in) {
        this.type = in.readInt();
        this.status = in.readInt();
        this.action = in.readInt();
        this.secondAction = in.readString();
        this.startTime = in.readString();
        this.endTime = in.readString();
        this.condition = Long.valueOf(in.readLong());
        this.excludeAppsStr = in.readString();
        this.packageName = in.readString();
        this.checkKey = in.readString();
        this.topKey = in.readString();
        int appsArrayLength = in.readInt();
        if (appsArrayLength != 0) {
            String[] appsArray = new String[appsArrayLength];
            in.readStringArray(appsArray);
            this.excludeAppList = Arrays.asList(appsArray);
        }
    }

    public AwarenessFence(Parcel in) {
        this.type = in.readInt();
        this.status = in.readInt();
        this.action = in.readInt();
        this.secondAction = in.readString();
        this.startTime = in.readString();
        this.endTime = in.readString();
        this.condition = Long.valueOf(in.readLong());
        this.excludeAppsStr = in.readString();
        this.packageName = in.readString();
        this.checkKey = in.readString();
        this.topKey = in.readString();
        int appsArrayLength = in.readInt();
        if (appsArrayLength != 0) {
            String[] appsArray = new String[appsArrayLength];
            in.readStringArray(appsArray);
            this.excludeAppList = Arrays.asList(appsArray);
        }
    }

    public AwarenessFence(int type2, int status2, int action2, String secondAction2) {
        this.type = type2;
        this.status = status2;
        this.action = action2;
        this.secondAction = secondAction2;
        if (!TextUtils.isEmpty(secondAction2)) {
            this.checkKey = type2 + "," + status2 + "," + action2 + AwarenessConstants.SECOND_ACTION_SPLITE_TAG + secondAction2;
        } else {
            this.checkKey = type2 + "," + status2 + "," + action2;
        }
    }

    public AwarenessFence(AwarenessFence awarenessFence) {
        this.type = awarenessFence.getType();
        this.status = awarenessFence.getStatus();
        this.action = awarenessFence.getAction();
        this.secondAction = awarenessFence.getSecondAction();
        this.startTime = awarenessFence.getStartTime();
        this.endTime = awarenessFence.getEndTime();
        this.condition = awarenessFence.getCondition();
        this.excludeAppsStr = awarenessFence.getExcludeAppsStr();
        this.packageName = awarenessFence.getPackageName();
        this.checkKey = awarenessFence.getCheckKey();
        this.topKey = awarenessFence.getTopKey();
        this.excludeAppList = awarenessFence.getExcludeAppList();
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status2) {
        this.status = status2;
    }

    public int getAction() {
        return this.action;
    }

    public void setAction(int action2) {
        this.action = action2;
    }

    public String getSecondAction() {
        return this.secondAction;
    }

    public void setSecondAction(String secondAction2) {
        this.secondAction = secondAction2;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime2) {
        this.startTime = startTime2;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(String endTime2) {
        this.endTime = endTime2;
    }

    public List<String> getExcludeAppList() {
        return this.excludeAppList;
    }

    public void setExcludeAppList(List<String> excludeAppList2) {
        this.excludeAppList = excludeAppList2;
    }

    public Long getCondition() {
        return this.condition;
    }

    public void setCondition(Long condition2) {
        this.condition = condition2;
    }

    public PendingIntent getOperationPI() {
        return this.operationPI;
    }

    public void setOperationPI(PendingIntent operationPI2) {
        this.operationPI = operationPI2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public String getCheckKey() {
        return this.checkKey;
    }

    public String getTopKey() {
        return this.topKey;
    }

    public void setTopKey(String topKey2) {
        this.topKey = topKey2;
    }

    public String getExcludeAppsStr() {
        return this.excludeAppsStr;
    }

    public void setExcludeAppsStr(String excludeAppsStr2) {
        this.excludeAppsStr = excludeAppsStr2;
    }

    public String getActionString() {
        if (!TextUtils.isEmpty(this.secondAction)) {
            return this.action + AwarenessConstants.SECOND_ACTION_SPLITE_TAG + this.secondAction;
        }
        return this.action + "";
    }

    public void build(Context context) {
        if (context != null) {
            this.packageName = context.getPackageName();
            if (this.packageName != null && this.packageName.contains(AwarenessConstants.PACKAGE_TOPKEY_SPLITE_TAG)) {
                this.packageName = this.packageName.replace(AwarenessConstants.PACKAGE_TOPKEY_SPLITE_TAG, "");
            }
            String key = AwarenessParseHelper.parseAwarenessKey(this);
            LogUtil.d(TAG, "build() key = " + key);
            this.topKey = this.packageName + AwarenessConstants.PACKAGE_TOPKEY_SPLITE_TAG + key;
            return;
        }
        LogUtil.e(TAG, "build() context == null");
    }

    public int describeContents() {
        return 0;
    }

    public int getLogic() {
        return this.logic;
    }

    public boolean hasRelation() {
        return this.logic > 1;
    }

    public List<AwarenessFence> getListFences() {
        return this.listFences;
    }

    public static AwarenessFence and(AwarenessFence... awarenessFences) {
        return new AwarenessFence(2, awarenessFences);
    }

    public static AwarenessFence not(AwarenessFence... awarenessFences) {
        return new AwarenessFence(4, awarenessFences);
    }

    public static AwarenessFence or(AwarenessFence... awarenessFences) {
        return new AwarenessFence(3, awarenessFences);
    }

    public AwarenessFence(int logic2, AwarenessFence... fences) {
        this.logic = logic2;
        this.listFences.clear();
        this.listFences.addAll(Arrays.asList(fences));
    }

    public AwarenessFence(int logic2, List<AwarenessFence> fences) {
        this.logic = logic2;
        this.listFences.clear();
        this.listFences.addAll(fences);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ excludeAppList:");
        if (this.excludeAppList != null) {
            for (String excludePackageName : this.excludeAppList) {
                builder.append(AwarenessConstants.SECOND_ACTION_SPLITE_TAG);
                builder.append(excludePackageName);
            }
        }
        builder.append("]");
        return String.format("AwarenessFence{type = %d, status = %d, action = %d, secondAction = %s, startTime = %s, endTime = %s, condition = %d, excludeAppList = %s, packageName = %s, checkKey = %s, topKey = %s}", new Object[]{Integer.valueOf(this.type), Integer.valueOf(this.status), Integer.valueOf(this.action), this.secondAction, this.startTime, this.endTime, this.condition, builder.toString(), this.packageName, this.checkKey, this.topKey});
    }
}
