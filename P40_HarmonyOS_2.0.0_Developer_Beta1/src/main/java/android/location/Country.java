package android.location;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import java.util.Locale;

public class Country implements Parcelable {
    public static final int COUNTRY_SOURCE_LOCALE = 3;
    public static final int COUNTRY_SOURCE_LOCATION = 1;
    public static final int COUNTRY_SOURCE_NETWORK = 0;
    public static final int COUNTRY_SOURCE_SIM = 2;
    public static final Parcelable.Creator<Country> CREATOR = new Parcelable.Creator<Country>() {
        /* class android.location.Country.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Country createFromParcel(Parcel in) {
            return new Country(in.readString(), in.readInt(), in.readLong());
        }

        @Override // android.os.Parcelable.Creator
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
    private final String mCountryIso;
    private int mHashCode;
    private final int mSource;
    private final long mTimestamp;

    @UnsupportedAppUsage
    public Country(String countryIso, int source) {
        if (countryIso == null || source < 0 || source > 3) {
            throw new IllegalArgumentException();
        }
        this.mCountryIso = countryIso.toUpperCase(Locale.US);
        this.mSource = source;
        this.mTimestamp = SystemClock.elapsedRealtime();
    }

    private Country(String countryIso, int source, long timestamp) {
        if (countryIso == null || source < 0 || source > 3) {
            throw new IllegalArgumentException();
        }
        this.mCountryIso = countryIso.toUpperCase(Locale.US);
        this.mSource = source;
        this.mTimestamp = timestamp;
    }

    public Country(Country country) {
        this.mCountryIso = country.mCountryIso;
        this.mSource = country.mSource;
        this.mTimestamp = country.mTimestamp;
    }

    @UnsupportedAppUsage
    public final String getCountryIso() {
        return this.mCountryIso;
    }

    @UnsupportedAppUsage
    public final int getSource() {
        return this.mSource;
    }

    public final long getTimestamp() {
        return this.mTimestamp;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mCountryIso);
        parcel.writeInt(this.mSource);
        parcel.writeLong(this.mTimestamp);
    }

    public boolean equals(Object object) {
        Country c;
        String str;
        if (object == this) {
            return true;
        }
        if (!(object instanceof Country) || (c = (Country) object) == null || (str = this.mCountryIso) == null) {
            return false;
        }
        if (!str.equals(c.getCountryIso()) || this.mSource != c.getSource()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        if (this.mHashCode == 0) {
            this.mHashCode = (((17 * 13) + this.mCountryIso.hashCode()) * 13) + this.mSource;
        }
        return this.mHashCode;
    }

    public boolean equalsIgnoreSource(Country country) {
        return country != null && this.mCountryIso.equals(country.getCountryIso());
    }

    public String toString() {
        return "Country {ISO=" + this.mCountryIso + ", source=" + this.mSource + ", time=" + this.mTimestamp + "}";
    }
}
