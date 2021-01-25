package com.huawei.nb.kv;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.gson.JsonObject;
import com.huawei.gson.JsonParser;
import com.huawei.nb.utils.JsonUtils;
import com.huawei.nb.utils.logger.DSLog;

public class VJson implements Value {
    public static final Parcelable.Creator<VJson> CREATOR = new Parcelable.Creator<VJson>() {
        /* class com.huawei.nb.kv.VJson.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VJson createFromParcel(Parcel parcel) {
            return new VJson(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public VJson[] newArray(int i) {
            return new VJson[i];
        }
    };
    private static final String EMPTY_JSON = "{}";
    private static final String SEPARATOR = "/";
    private String jsonVal;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public VJson(String str) {
        this.jsonVal = str;
    }

    @Override // com.huawei.nb.kv.Value
    public Integer dType() {
        return 4;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof VJson)) {
            return false;
        }
        VJson vJson = (VJson) obj;
        String str = this.jsonVal;
        if (str != null) {
            return str.equals(vJson.getValue());
        }
        if (vJson.getValue() == null) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        String str = this.jsonVal;
        if (str != null) {
            return str.hashCode();
        }
        return 0;
    }

    @Override // com.huawei.nb.kv.Value
    public boolean verify() {
        try {
            JsonObject asJsonObject = new JsonParser().parse(JsonUtils.sanitize(this.jsonVal)).getAsJsonObject();
            if (asJsonObject == null || EMPTY_JSON.equalsIgnoreCase(asJsonObject.toString())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid VJson value [");
            String str = this.jsonVal;
            if (str == null) {
                str = " ";
            }
            sb.append(str);
            sb.append("]: ");
            sb.append(e.getMessage());
            DSLog.e(sb.toString(), new Object[0]);
            return false;
        }
    }

    public boolean verify(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        try {
            JsonObject asJsonObject = new JsonParser().parse(JsonUtils.sanitize(this.jsonVal)).getAsJsonObject();
            if (asJsonObject == null) {
                return false;
            }
            String[] split = str.split(SEPARATOR);
            return asJsonObject.has(split[split.length - 1]);
        } catch (Exception e) {
            DSLog.e("verify exception: " + e.getMessage(), new Object[0]);
            return false;
        }
    }

    public String getValue() {
        return this.jsonVal;
    }

    public void setValue(String str) {
        this.jsonVal = str;
    }

    protected VJson(Parcel parcel) {
        this.jsonVal = parcel.readString();
        if (this.jsonVal.equals("")) {
            this.jsonVal = null;
        }
    }

    @Override // com.huawei.nb.kv.Value, java.lang.Object
    public String toString() {
        String str = this.jsonVal;
        return str == null ? "" : str;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        String str = this.jsonVal;
        if (str == null) {
            str = "";
        }
        parcel.writeString(str);
    }
}
