package android.net.wifi.aware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class Characteristics implements Parcelable {
    public static final Parcelable.Creator<Characteristics> CREATOR = new Parcelable.Creator<Characteristics>() {
        /* class android.net.wifi.aware.Characteristics.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Characteristics createFromParcel(Parcel in) {
            return new Characteristics(in.readBundle());
        }

        @Override // android.os.Parcelable.Creator
        public Characteristics[] newArray(int size) {
            return new Characteristics[size];
        }
    };
    public static final String KEY_MAX_MATCH_FILTER_LENGTH = "key_max_match_filter_length";
    public static final String KEY_MAX_SERVICE_NAME_LENGTH = "key_max_service_name_length";
    public static final String KEY_MAX_SERVICE_SPECIFIC_INFO_LENGTH = "key_max_service_specific_info_length";
    private Bundle mCharacteristics = new Bundle();

    public Characteristics(Bundle characteristics) {
        this.mCharacteristics = characteristics;
    }

    public int getMaxServiceNameLength() {
        return this.mCharacteristics.getInt(KEY_MAX_SERVICE_NAME_LENGTH);
    }

    public int getMaxServiceSpecificInfoLength() {
        return this.mCharacteristics.getInt(KEY_MAX_SERVICE_SPECIFIC_INFO_LENGTH);
    }

    public int getMaxMatchFilterLength() {
        return this.mCharacteristics.getInt(KEY_MAX_MATCH_FILTER_LENGTH);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(this.mCharacteristics);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
