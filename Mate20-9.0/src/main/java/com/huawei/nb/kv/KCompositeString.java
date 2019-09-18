package com.huawei.nb.kv;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;

public class KCompositeString implements Key {
    public static final Parcelable.Creator<KCompositeString> CREATOR = new Parcelable.Creator<KCompositeString>() {
        public KCompositeString createFromParcel(Parcel in) {
            return new KCompositeString(in);
        }

        public KCompositeString[] newArray(int size) {
            return new KCompositeString[size];
        }
    };
    private String primaryKey = null;
    private String secondaryKey = null;
    private String separator = File.separator;
    private int vType = 4;

    public KCompositeString(String pKey) {
        this.primaryKey = pKey;
        this.secondaryKey = null;
        this.vType = 4;
    }

    public KCompositeString(String pKey, String sKey) {
        this.primaryKey = pKey;
        this.secondaryKey = sKey;
        this.vType = 4;
    }

    private KCompositeString(String pKey, String sKey, String sep) {
        this.primaryKey = pKey;
        this.secondaryKey = sKey;
        this.separator = sep;
        this.vType = 4;
    }

    private KCompositeString(String pKey, String sKey, String sep, Integer vType2) {
        this.primaryKey = pKey;
        this.secondaryKey = sKey;
        this.separator = sep;
        this.vType = vType2.intValue();
    }

    protected KCompositeString(Parcel in) {
        if (in != null) {
            this.primaryKey = in.readString();
            if (this.primaryKey != null && this.primaryKey.equals("")) {
                this.primaryKey = null;
            }
            this.secondaryKey = in.readString();
            if (this.secondaryKey != null && this.secondaryKey.equals("")) {
                this.secondaryKey = null;
            }
            this.separator = in.readString();
            if (this.separator != null && this.separator.equals("")) {
                this.separator = null;
            }
            this.vType = in.readInt();
        }
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    public String getSecondaryKey() {
        return this.secondaryKey;
    }

    public String getSeparator() {
        return this.separator;
    }

    public String getGrantFieldName() {
        return (this.secondaryKey == null || this.secondaryKey.trim().length() == 0) ? this.primaryKey : this.primaryKey + this.separator + this.secondaryKey;
    }

    public Integer dType() {
        return 16;
    }

    public Integer vType() {
        return Integer.valueOf(this.vType);
    }

    public void vType(Integer vType2) {
        this.vType = vType2.intValue();
    }

    public boolean equals(Object aKey) {
        boolean primaryKeyEqual;
        boolean secondKeyEqual;
        boolean separatorEqual;
        if (aKey == null || !(aKey instanceof KCompositeString)) {
            return false;
        }
        KCompositeString otherKey = (KCompositeString) aKey;
        if (this.primaryKey != null) {
            primaryKeyEqual = this.primaryKey.equals(otherKey.getPrimaryKey());
        } else if (otherKey.getPrimaryKey() == null) {
            primaryKeyEqual = true;
        } else {
            primaryKeyEqual = false;
        }
        if (!primaryKeyEqual) {
            return false;
        }
        if (this.secondaryKey != null) {
            secondKeyEqual = this.secondaryKey.equals(otherKey.getSecondaryKey());
        } else if (otherKey.getSecondaryKey() == null) {
            secondKeyEqual = true;
        } else {
            secondKeyEqual = false;
        }
        if (!secondKeyEqual) {
            return false;
        }
        if (this.separator != null) {
            separatorEqual = this.separator.equals(otherKey.getSeparator());
        } else if (otherKey.getSeparator() == null) {
            separatorEqual = true;
        } else {
            separatorEqual = false;
        }
        return separatorEqual;
    }

    public int hashCode() {
        int result;
        int i;
        int i2 = 0;
        if (this.primaryKey != null) {
            result = this.primaryKey.hashCode();
        } else {
            result = 0;
        }
        int i3 = result * 31;
        if (this.secondaryKey != null) {
            i = this.secondaryKey.hashCode();
        } else {
            i = 0;
        }
        int i4 = (i3 + i) * 31;
        if (this.separator != null) {
            i2 = this.separator.hashCode();
        }
        return ((i4 + i2) * 31) + this.vType;
    }

    public boolean verify() {
        if (this.primaryKey == null || this.primaryKey.trim().length() == 0) {
            return false;
        }
        if (this.secondaryKey == null) {
            return true;
        }
        if (this.separator != null) {
            for (String key : this.secondaryKey.split(this.separator)) {
                if ("".equals(key.trim())) {
                    return false;
                }
            }
            return true;
        } else if (this.secondaryKey.trim().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public String toString() {
        return "Primary Key:(" + (this.primaryKey == null ? "" : this.primaryKey) + "), Secondary Key:(" + (this.secondaryKey == null ? "" : this.secondaryKey) + ")[Separator:(" + (this.separator == null ? "" : this.separator) + ")], Value Type:(" + this.vType + ")";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.primaryKey == null ? "" : this.primaryKey);
        parcel.writeString(this.secondaryKey == null ? "" : this.secondaryKey);
        parcel.writeString(this.separator == null ? "" : this.separator);
        parcel.writeInt(this.vType);
    }
}
