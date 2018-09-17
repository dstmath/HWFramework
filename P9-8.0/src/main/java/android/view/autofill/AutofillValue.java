package android.view.autofill;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public final class AutofillValue implements Parcelable {
    public static final Creator<AutofillValue> CREATOR = new Creator<AutofillValue>() {
        public AutofillValue createFromParcel(Parcel source) {
            return new AutofillValue(source, null);
        }

        public AutofillValue[] newArray(int size) {
            return new AutofillValue[size];
        }
    };
    private final int mType;
    private final Object mValue;

    private AutofillValue(int type, Object value) {
        this.mType = type;
        this.mValue = value;
    }

    public CharSequence getTextValue() {
        Preconditions.checkState(isText(), "value must be a text value, not type=" + this.mType);
        return (CharSequence) this.mValue;
    }

    public boolean isText() {
        return this.mType == 1;
    }

    public boolean getToggleValue() {
        Preconditions.checkState(isToggle(), "value must be a toggle value, not type=" + this.mType);
        return ((Boolean) this.mValue).booleanValue();
    }

    public boolean isToggle() {
        return this.mType == 2;
    }

    public int getListValue() {
        Preconditions.checkState(isList(), "value must be a list value, not type=" + this.mType);
        return ((Integer) this.mValue).intValue();
    }

    public boolean isList() {
        return this.mType == 3;
    }

    public long getDateValue() {
        Preconditions.checkState(isDate(), "value must be a date value, not type=" + this.mType);
        return ((Long) this.mValue).longValue();
    }

    public boolean isDate() {
        return this.mType == 4;
    }

    public boolean isEmpty() {
        return isText() && ((CharSequence) this.mValue).length() == 0;
    }

    public int hashCode() {
        return this.mType + this.mValue.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AutofillValue other = (AutofillValue) obj;
        if (this.mType != other.mType) {
            return false;
        }
        if (isText()) {
            return this.mValue.toString().equals(other.mValue.toString());
        }
        return Objects.equals(this.mValue, other.mValue);
    }

    public String toString() {
        if (!Helper.sDebug) {
            return super.toString();
        }
        StringBuilder string = new StringBuilder().append("[type=").append(this.mType).append(", value=");
        if (isText()) {
            string.append(((CharSequence) this.mValue).length()).append("_chars");
        } else {
            string.append(this.mValue);
        }
        return string.append(']').toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mType);
        switch (this.mType) {
            case 1:
                parcel.writeCharSequence((CharSequence) this.mValue);
                return;
            case 2:
                parcel.writeInt(((Boolean) this.mValue).booleanValue() ? 1 : 0);
                return;
            case 3:
                parcel.writeInt(((Integer) this.mValue).intValue());
                return;
            case 4:
                parcel.writeLong(((Long) this.mValue).longValue());
                return;
            default:
                return;
        }
    }

    private AutofillValue(Parcel parcel) {
        boolean z = false;
        this.mType = parcel.readInt();
        switch (this.mType) {
            case 1:
                this.mValue = parcel.readCharSequence();
                return;
            case 2:
                if (parcel.readInt() != 0) {
                    z = true;
                }
                this.mValue = Boolean.valueOf(z);
                return;
            case 3:
                this.mValue = Integer.valueOf(parcel.readInt());
                return;
            case 4:
                this.mValue = Long.valueOf(parcel.readLong());
                return;
            default:
                throw new IllegalArgumentException("type=" + this.mType + " not valid");
        }
    }

    public static AutofillValue forText(CharSequence value) {
        return value == null ? null : new AutofillValue(1, TextUtils.trimNoCopySpans(value));
    }

    public static AutofillValue forToggle(boolean value) {
        return new AutofillValue(2, Boolean.valueOf(value));
    }

    public static AutofillValue forList(int value) {
        return new AutofillValue(3, Integer.valueOf(value));
    }

    public static AutofillValue forDate(long value) {
        return new AutofillValue(4, Long.valueOf(value));
    }
}
