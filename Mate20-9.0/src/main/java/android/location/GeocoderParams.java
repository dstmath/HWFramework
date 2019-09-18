package android.location;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class GeocoderParams implements Parcelable {
    public static final Parcelable.Creator<GeocoderParams> CREATOR = new Parcelable.Creator<GeocoderParams>() {
        public GeocoderParams createFromParcel(Parcel in) {
            GeocoderParams gp = new GeocoderParams();
            Locale unused = gp.mLocale = new Locale(in.readString(), in.readString(), in.readString());
            String unused2 = gp.mPackageName = in.readString();
            return gp;
        }

        public GeocoderParams[] newArray(int size) {
            return new GeocoderParams[size];
        }
    };
    /* access modifiers changed from: private */
    public Locale mLocale;
    /* access modifiers changed from: private */
    public String mPackageName;

    private GeocoderParams() {
    }

    public GeocoderParams(Context context, Locale locale) {
        this.mLocale = locale;
        this.mPackageName = context.getPackageName();
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    public String getClientPackage() {
        return this.mPackageName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mLocale.getLanguage());
        parcel.writeString(this.mLocale.getCountry());
        parcel.writeString(this.mLocale.getVariant());
        parcel.writeString(this.mPackageName);
    }
}
