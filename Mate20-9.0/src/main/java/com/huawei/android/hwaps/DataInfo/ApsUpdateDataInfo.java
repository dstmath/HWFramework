package com.huawei.android.hwaps.DataInfo;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;

public class ApsUpdateDataInfo implements Parcelable {
    public static final Parcelable.Creator<ApsUpdateDataInfo> CREATOR = new Parcelable.Creator<ApsUpdateDataInfo>() {
        public ApsUpdateDataInfo createFromParcel(Parcel in) {
            return new ApsUpdateDataInfo(in.readString(), in.readHashMap(null), in.readString(), (String[]) in.readArray(null));
        }

        public ApsUpdateDataInfo[] newArray(int size) {
            return new ApsUpdateDataInfo[size];
        }
    };
    public String[] mSelectionArgs = null;
    public String mUri = null;
    public HashMap<String, Object> mValues = null;
    public String mWhere = null;

    public ApsUpdateDataInfo(String strUri, HashMap<String, Object> values, String where, String[] selectionArgs) {
        this.mUri = strUri;
        this.mValues = values;
        this.mWhere = where;
        if (selectionArgs != null) {
            this.mSelectionArgs = (String[]) selectionArgs.clone();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        synchronized (this) {
            dest.writeString(this.mUri);
            dest.writeString(this.mWhere);
            dest.writeMap(this.mValues);
            dest.writeArray(this.mSelectionArgs);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.mUri != null) {
            sb.append("ApsUpdateDataInfo: Uri:" + this.mUri);
        }
        if (this.mValues != null) {
            sb.append(" Values:");
            for (String name : this.mValues.keySet()) {
                Object obj = this.mValues.get(r1.next());
                String value = obj != null ? obj.toString() : null;
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(name + "=" + value);
            }
        }
        if (this.mWhere != null) {
            sb.append(" where:" + this.mWhere);
        }
        if (this.mSelectionArgs != null) {
            sb.append(" SelectionArgs:");
            for (String str : this.mSelectionArgs) {
                sb.append(str);
            }
        }
        return sb.toString();
    }
}
