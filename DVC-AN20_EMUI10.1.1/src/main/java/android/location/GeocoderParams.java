package android.location;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class GeocoderParams implements Parcelable {
    public static final Parcelable.Creator<GeocoderParams> CREATOR = new Parcelable.Creator<GeocoderParams>() {
        /* class android.location.GeocoderParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GeocoderParams createFromParcel(Parcel in) {
            GeocoderParams gp = new GeocoderParams();
            gp.mLocale = new Locale(in.readString(), in.readString(), in.readString());
            gp.mPackageName = in.readString();
            return gp;
        }

        @Override // android.os.Parcelable.Creator
        public GeocoderParams[] newArray(int size) {
            return new GeocoderParams[size];
        }
    };
    private Locale mLocale;
    private String mPackageName;

    private GeocoderParams() {
    }

    public GeocoderParams(Context context, Locale locale) {
        this.mLocale = locale;
        this.mPackageName = context.getPackageName();
    }

    @UnsupportedAppUsage
    public Locale getLocale() {
        return this.mLocale;
    }

    @UnsupportedAppUsage
    public String getClientPackage() {
        return this.mPackageName;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mLocale.getLanguage());
        parcel.writeString(this.mLocale.getCountry());
        parcel.writeString(this.mLocale.getVariant());
        parcel.writeString(this.mPackageName);
    }
}
