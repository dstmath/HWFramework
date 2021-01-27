package com.huawei.nb.kv;

import android.os.Parcel;
import android.os.Parcelable;

public class KCompositeString implements Key {
    public static final Parcelable.Creator<KCompositeString> CREATOR = new Parcelable.Creator<KCompositeString>() {
        /* class com.huawei.nb.kv.KCompositeString.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public KCompositeString createFromParcel(Parcel parcel) {
            return new KCompositeString(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public KCompositeString[] newArray(int i) {
            return new KCompositeString[i];
        }
    };
    private static final int HASH_CODE_NUM = 31;
    private static final String SEPARATOR = "/";
    private String primaryKey = null;
    private String secondaryKey = null;
    private int valueType = 4;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getSeparator() {
        return SEPARATOR;
    }

    public KCompositeString(String str) {
        this.primaryKey = str;
        this.secondaryKey = null;
        this.valueType = 4;
    }

    public KCompositeString(String str, String str2) {
        this.primaryKey = str;
        this.secondaryKey = str2;
        this.valueType = 4;
    }

    protected KCompositeString(Parcel parcel) {
        if (parcel != null) {
            this.primaryKey = parcel.readString();
            String str = this.primaryKey;
            if (str != null && str.equals("")) {
                this.primaryKey = null;
            }
            this.secondaryKey = parcel.readString();
            String str2 = this.secondaryKey;
            if (str2 != null && str2.equals("")) {
                this.secondaryKey = null;
            }
            this.valueType = parcel.readInt();
        }
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    public String getSecondaryKey() {
        return this.secondaryKey;
    }

    public String getGrantFieldName() {
        String str = this.secondaryKey;
        if (str == null || str.trim().length() == 0) {
            return this.primaryKey;
        }
        return this.primaryKey + SEPARATOR + this.secondaryKey;
    }

    @Override // com.huawei.nb.kv.Key
    public Integer dType() {
        return 16;
    }

    @Override // com.huawei.nb.kv.Key
    public Integer vType() {
        return Integer.valueOf(this.valueType);
    }

    @Override // com.huawei.nb.kv.Key
    public void vType(Integer num) {
        this.valueType = num.intValue();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        boolean z;
        if (obj == null || !(obj instanceof KCompositeString)) {
            return false;
        }
        KCompositeString kCompositeString = (KCompositeString) obj;
        String str = this.primaryKey;
        if (str == null) {
            z = kCompositeString.getPrimaryKey() == null;
        } else {
            z = str.equals(kCompositeString.getPrimaryKey());
        }
        if (!z) {
            return false;
        }
        String str2 = this.secondaryKey;
        if (str2 != null) {
            return str2.equals(kCompositeString.getSecondaryKey());
        }
        if (kCompositeString.getSecondaryKey() == null) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        String str = this.primaryKey;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * HASH_CODE_NUM;
        String str2 = this.secondaryKey;
        if (str2 != null) {
            i = str2.hashCode();
        }
        return ((((hashCode + i) * HASH_CODE_NUM) + SEPARATOR.hashCode()) * HASH_CODE_NUM) + this.valueType;
    }

    @Override // com.huawei.nb.kv.Key
    public boolean verify() {
        String str = this.primaryKey;
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        String str2 = this.secondaryKey;
        if (str2 == null) {
            return true;
        }
        for (String str3 : str2.split(SEPARATOR)) {
            if ("".equals(str3.trim())) {
                return false;
            }
        }
        return true;
    }

    @Override // com.huawei.nb.kv.Key, java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Primary Key:(");
        String str = this.primaryKey;
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append("), Secondary Key:(");
        String str2 = this.secondaryKey;
        if (str2 == null) {
            str2 = "";
        }
        sb.append(str2);
        sb.append(")[Separator:(");
        sb.append(SEPARATOR);
        sb.append(")], Value Type:(");
        sb.append(this.valueType);
        sb.append(")");
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        String str = this.primaryKey;
        if (str == null) {
            str = "";
        }
        parcel.writeString(str);
        String str2 = this.secondaryKey;
        if (str2 == null) {
            str2 = "";
        }
        parcel.writeString(str2);
        parcel.writeInt(this.valueType);
    }
}
