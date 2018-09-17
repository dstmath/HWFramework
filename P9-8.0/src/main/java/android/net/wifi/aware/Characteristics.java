package android.net.wifi.aware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class Characteristics implements Parcelable {
    public static final Creator<Characteristics> CREATOR = new Creator<Characteristics>() {
        public Characteristics createFromParcel(Parcel in) {
            return new Characteristics(in.readBundle());
        }

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

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(this.mCharacteristics);
    }

    public int describeContents() {
        return 0;
    }
}
