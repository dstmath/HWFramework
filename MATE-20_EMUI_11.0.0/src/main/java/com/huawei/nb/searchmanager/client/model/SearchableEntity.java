package com.huawei.nb.searchmanager.client.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.searchmanager.utils.logger.DSLog;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchableEntity implements Parcelable {
    public static final Parcelable.Creator<SearchableEntity> CREATOR = new Parcelable.Creator<SearchableEntity>() {
        /* class com.huawei.nb.searchmanager.client.model.SearchableEntity.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SearchableEntity createFromParcel(Parcel parcel) {
            return new SearchableEntity(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public SearchableEntity[] newArray(int i) {
            return new SearchableEntity[0];
        }
    };
    private static final String TAG = "SearchableEntity";
    private String appId;
    private String componentName;
    private String intentAction;
    private boolean isAllowGlobalSearch;
    private String packageName;
    private String permission;
    private int versionCode;
    private String versionName;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public SearchableEntity(String str, String str2, String str3, String str4, String str5, boolean z, int i, String str6) {
        this.packageName = str;
        this.appId = str2;
        this.permission = str3;
        this.intentAction = str4;
        this.componentName = str5;
        this.isAllowGlobalSearch = z;
        this.versionCode = i;
        this.versionName = str6;
    }

    public SearchableEntity() {
    }

    private SearchableEntity(Parcel parcel) {
        this.packageName = parcel.readString();
        this.appId = parcel.readString();
        this.permission = parcel.readString();
        this.intentAction = parcel.readString();
        this.componentName = parcel.readString();
        this.isAllowGlobalSearch = parcel.readInt() != 0;
        this.versionCode = parcel.readInt();
        this.versionName = parcel.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.packageName);
        parcel.writeString(this.appId);
        parcel.writeString(this.permission);
        parcel.writeString(this.intentAction);
        parcel.writeString(this.componentName);
        parcel.writeInt(this.isAllowGlobalSearch ? (byte) 1 : 0);
        parcel.writeInt(this.versionCode);
        parcel.writeString(this.versionName);
    }

    @Override // java.lang.Object
    public String toString() {
        return "SearchableEntity{" + this.packageName + "," + this.appId + "," + this.permission + "," + this.intentAction + "," + this.componentName + "," + this.isAllowGlobalSearch + "," + this.versionCode + "," + this.versionName + "}";
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String str) {
        this.appId = str;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String str) {
        this.permission = str;
    }

    public String getIntentAction() {
        return this.intentAction;
    }

    public void setIntentAction(String str) {
        this.intentAction = str;
    }

    public String getComponentName() {
        return this.componentName;
    }

    public void setComponentName(String str) {
        this.componentName = str;
    }

    public boolean isAllowGlobalSearch() {
        return this.isAllowGlobalSearch;
    }

    public void setAllowGlobalSearch(boolean z) {
        this.isAllowGlobalSearch = z;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(int i) {
        this.versionCode = i;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String str) {
        this.versionName = str;
    }

    public JSONObject toJsonObj() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("packageName", this.packageName);
            jSONObject.put("appId", this.appId);
            jSONObject.put("permission", this.permission);
            jSONObject.put("intentAction", this.intentAction);
            jSONObject.put("componentName", this.componentName);
            jSONObject.put("isAllowGlobalSearch", this.isAllowGlobalSearch);
            jSONObject.put("versionCode", this.versionCode);
            jSONObject.put("versionName", this.versionName);
        } catch (JSONException e) {
            DSLog.et(TAG, "toJsonObj exception " + e.getMessage(), new Object[0]);
        }
        return jSONObject;
    }

    public SearchableEntity fromJsonObj(JSONObject jSONObject) {
        if (jSONObject == null) {
            return this;
        }
        try {
            setPackageName(jSONObject.getString("packageName"));
            setAppId(jSONObject.getString("appId"));
            setPermission(jSONObject.getString("permission"));
            setIntentAction(jSONObject.getString("intentAction"));
            setComponentName(jSONObject.getString("componentName"));
            setAllowGlobalSearch(jSONObject.getBoolean("isAllowGlobalSearch"));
            setVersionCode(jSONObject.getInt("versionCode"));
            setVersionName(jSONObject.getString("versionName"));
        } catch (JSONException e) {
            DSLog.et(TAG, "fromJsonObj exception " + e.getMessage(), new Object[0]);
        }
        return this;
    }
}
