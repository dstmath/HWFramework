package android.security.keymaster;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KeyCharacteristics implements Parcelable {
    public static final Parcelable.Creator<KeyCharacteristics> CREATOR = new Parcelable.Creator<KeyCharacteristics>() {
        /* class android.security.keymaster.KeyCharacteristics.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public KeyCharacteristics createFromParcel(Parcel in) {
            return new KeyCharacteristics(in);
        }

        @Override // android.os.Parcelable.Creator
        public KeyCharacteristics[] newArray(int length) {
            return new KeyCharacteristics[length];
        }
    };
    public KeymasterArguments hwEnforced;
    public KeymasterArguments swEnforced;

    @UnsupportedAppUsage
    public KeyCharacteristics() {
    }

    protected KeyCharacteristics(Parcel in) {
        readFromParcel(in);
    }

    public void shallowCopyFrom(KeyCharacteristics other) {
        this.swEnforced = other.swEnforced;
        this.hwEnforced = other.hwEnforced;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        this.swEnforced.writeToParcel(out, flags);
        this.hwEnforced.writeToParcel(out, flags);
    }

    @UnsupportedAppUsage
    public void readFromParcel(Parcel in) {
        this.swEnforced = KeymasterArguments.CREATOR.createFromParcel(in);
        this.hwEnforced = KeymasterArguments.CREATOR.createFromParcel(in);
    }

    public Integer getEnum(int tag) {
        if (this.hwEnforced.containsTag(tag)) {
            return Integer.valueOf(this.hwEnforced.getEnum(tag, -1));
        }
        if (this.swEnforced.containsTag(tag)) {
            return Integer.valueOf(this.swEnforced.getEnum(tag, -1));
        }
        return null;
    }

    public List<Integer> getEnums(int tag) {
        List<Integer> result = new ArrayList<>();
        result.addAll(this.hwEnforced.getEnums(tag));
        result.addAll(this.swEnforced.getEnums(tag));
        return result;
    }

    public long getUnsignedInt(int tag, long defaultValue) {
        if (this.hwEnforced.containsTag(tag)) {
            return this.hwEnforced.getUnsignedInt(tag, defaultValue);
        }
        return this.swEnforced.getUnsignedInt(tag, defaultValue);
    }

    public List<BigInteger> getUnsignedLongs(int tag) {
        List<BigInteger> result = new ArrayList<>();
        result.addAll(this.hwEnforced.getUnsignedLongs(tag));
        result.addAll(this.swEnforced.getUnsignedLongs(tag));
        return result;
    }

    public Date getDate(int tag) {
        Date result = this.swEnforced.getDate(tag, null);
        if (result != null) {
            return result;
        }
        return this.hwEnforced.getDate(tag, null);
    }

    public boolean getBoolean(int tag) {
        if (this.hwEnforced.containsTag(tag)) {
            return this.hwEnforced.getBoolean(tag);
        }
        return this.swEnforced.getBoolean(tag);
    }
}
