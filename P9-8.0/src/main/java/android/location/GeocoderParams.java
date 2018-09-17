package android.location;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Locale;

public class GeocoderParams implements Parcelable {
    public static final Creator<GeocoderParams> CREATOR = new Creator<GeocoderParams>() {
        public GeocoderParams createFromParcel(Parcel in) {
            GeocoderParams gp = new GeocoderParams();
            gp.mLocale = new Locale(in.readString(), in.readString(), in.readString());
            gp.mPackageName = in.readString();
            return gp;
        }

        public GeocoderParams[] newArray(int size) {
            return new GeocoderParams[size];
        }
    };
    private Locale mLocale;
    private String mPackageName;

    /* synthetic */ GeocoderParams(GeocoderParams -this0) {
        this();
    }

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
