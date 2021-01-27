package com.huawei.nb.searchmanager.client.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.searchmanager.utils.logger.DSLog;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class IndexForm implements Parcelable {
    public static final Parcelable.Creator<IndexForm> CREATOR = new Parcelable.Creator<IndexForm>() {
        /* class com.huawei.nb.searchmanager.client.model.IndexForm.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IndexForm createFromParcel(Parcel parcel) {
            return new IndexForm(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public IndexForm[] newArray(int i) {
            return new IndexForm[i];
        }
    };
    private static final String TAG = "IndexForm";
    private String indexFieldName;
    private String indexType;
    private boolean isPrimaryKey;
    private boolean isSearch;
    private boolean isStore;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public IndexForm(String str) {
        this(str, IndexType.ANALYZED, false, true, true);
    }

    public IndexForm(String str, String str2, boolean z, boolean z2, boolean z3) {
        this.isPrimaryKey = false;
        this.indexType = IndexType.ANALYZED;
        this.isStore = true;
        this.isSearch = true;
        this.indexFieldName = str;
        this.indexType = str2;
        this.isPrimaryKey = z;
        this.isStore = z2;
        this.isSearch = z3;
    }

    public IndexForm() {
        this(null, IndexType.ANALYZED, false, true, true);
    }

    public IndexForm(Parcel parcel) {
        boolean z = false;
        this.isPrimaryKey = false;
        this.indexType = IndexType.ANALYZED;
        this.isStore = true;
        this.isSearch = true;
        this.indexFieldName = parcel.readString();
        this.indexType = parcel.readString();
        this.isPrimaryKey = parcel.readByte() != 0;
        this.isStore = parcel.readByte() != 0;
        this.isSearch = parcel.readByte() != 0 ? true : z;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.indexFieldName);
        parcel.writeString(this.indexType);
        parcel.writeByte(this.isPrimaryKey ? (byte) 1 : 0);
        parcel.writeByte(this.isStore ? (byte) 1 : 0);
        parcel.writeByte(this.isSearch ? (byte) 1 : 0);
    }

    @Override // java.lang.Object
    public String toString() {
        return "IndexForm[indexFieldName=" + this.indexFieldName + ",indexType=" + this.indexType + ",isPrimaryKey=" + this.isPrimaryKey + ",isStore=" + this.isStore + ",isSearch=" + this.isSearch + "]";
    }

    public String getIndexFieldName() {
        return this.indexFieldName;
    }

    public void setIndexFieldName(String str) {
        this.indexFieldName = str;
    }

    public boolean isPrimaryKey() {
        return this.isPrimaryKey;
    }

    public void setPrimaryKey(boolean z) {
        this.isPrimaryKey = z;
    }

    public String getIndexType() {
        return this.indexType;
    }

    public void setIndexType(String str) {
        this.indexType = str;
    }

    public boolean isStore() {
        return this.isStore;
    }

    public void setStore(boolean z) {
        this.isStore = z;
    }

    public boolean isSearch() {
        return this.isSearch;
    }

    public void setSearch(boolean z) {
        this.isSearch = z;
    }

    public JSONObject toJsonObj() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("indexFieldName", getIndexFieldName());
            jSONObject.put("isPrimaryKey", isPrimaryKey());
            jSONObject.put("indexType", getIndexType());
            jSONObject.put("isStore", isStore());
            jSONObject.put("isSearch", isSearch());
        } catch (JSONException e) {
            DSLog.et(TAG, "toJsonObj err: %s", e.getMessage());
        }
        return jSONObject;
    }

    public IndexForm fromJsonObj(JSONObject jSONObject) {
        if (jSONObject == null) {
            return this;
        }
        try {
            setIndexFieldName(jSONObject.getString("indexFieldName"));
            setPrimaryKey(jSONObject.getBoolean("isPrimaryKey"));
            setIndexType(jSONObject.getString("indexType"));
            setStore(jSONObject.getBoolean("isStore"));
            setSearch(jSONObject.getBoolean("isSearch"));
        } catch (JSONException e) {
            DSLog.et(TAG, "fromJsonObj err: %s", e.getMessage());
        }
        return this;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof IndexForm)) {
            return false;
        }
        IndexForm indexForm = (IndexForm) obj;
        return this.isPrimaryKey == indexForm.isPrimaryKey && this.isStore == indexForm.isStore && this.isSearch == indexForm.isSearch && Objects.equals(this.indexFieldName, indexForm.indexFieldName) && Objects.equals(this.indexType, indexForm.indexType);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.indexFieldName, Boolean.valueOf(this.isPrimaryKey), this.indexType, Boolean.valueOf(this.isStore), Boolean.valueOf(this.isSearch));
    }
}
