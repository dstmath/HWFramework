package android.location;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Address implements Parcelable {
    public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<Address>() {
        /* class android.location.Address.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Address createFromParcel(Parcel in) {
            Locale locale;
            String language = in.readString();
            String country = in.readString();
            if (country.length() > 0) {
                locale = new Locale(language, country);
            } else {
                locale = new Locale(language);
            }
            Address a = new Address(locale);
            int N = in.readInt();
            if (N > 0) {
                a.mAddressLines = new HashMap(N);
                for (int i = 0; i < N; i++) {
                    int index = in.readInt();
                    a.mAddressLines.put(Integer.valueOf(index), in.readString());
                    a.mMaxAddressLineIndex = Math.max(a.mMaxAddressLineIndex, index);
                }
            } else {
                a.mAddressLines = null;
                a.mMaxAddressLineIndex = -1;
            }
            a.mFeatureName = in.readString();
            a.mAdminArea = in.readString();
            a.mSubAdminArea = in.readString();
            a.mLocality = in.readString();
            a.mSubLocality = in.readString();
            a.mThoroughfare = in.readString();
            a.mSubThoroughfare = in.readString();
            a.mPremises = in.readString();
            a.mPostalCode = in.readString();
            a.mCountryCode = in.readString();
            a.mCountryName = in.readString();
            boolean z = false;
            a.mHasLatitude = in.readInt() != 0;
            if (a.mHasLatitude) {
                a.mLatitude = in.readDouble();
            }
            if (in.readInt() != 0) {
                z = true;
            }
            a.mHasLongitude = z;
            if (a.mHasLongitude) {
                a.mLongitude = in.readDouble();
            }
            a.mPhone = in.readString();
            a.mUrl = in.readString();
            a.mExtras = in.readBundle();
            return a;
        }

        @Override // android.os.Parcelable.Creator
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
    private HashMap<Integer, String> mAddressLines;
    private String mAdminArea;
    private String mCountryCode;
    private String mCountryName;
    private Bundle mExtras = null;
    private String mFeatureName;
    private boolean mHasLatitude = false;
    private boolean mHasLongitude = false;
    private double mLatitude;
    private Locale mLocale;
    private String mLocality;
    private double mLongitude;
    private int mMaxAddressLineIndex = -1;
    private String mPhone;
    private String mPostalCode;
    private String mPremises;
    private String mSubAdminArea;
    private String mSubLocality;
    private String mSubThoroughfare;
    private String mThoroughfare;
    private String mUrl;

    public Address(Locale locale) {
        this.mLocale = locale;
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    public int getMaxAddressLineIndex() {
        return this.mMaxAddressLineIndex;
    }

    public String getAddressLine(int index) {
        if (index >= 0) {
            HashMap<Integer, String> hashMap = this.mAddressLines;
            if (hashMap == null) {
                return null;
            }
            return hashMap.get(Integer.valueOf(index));
        }
        throw new IllegalArgumentException("index = " + index + " < 0");
    }

    public void setAddressLine(int index, String line) {
        if (index >= 0) {
            if (this.mAddressLines == null) {
                this.mAddressLines = new HashMap<>();
            }
            this.mAddressLines.put(Integer.valueOf(index), line);
            if (line == null) {
                this.mMaxAddressLineIndex = -1;
                for (Integer i : this.mAddressLines.keySet()) {
                    this.mMaxAddressLineIndex = Math.max(this.mMaxAddressLineIndex, i.intValue());
                }
                return;
            }
            this.mMaxAddressLineIndex = Math.max(this.mMaxAddressLineIndex, index);
            return;
        }
        throw new IllegalArgumentException("index = " + index + " < 0");
    }

    public String getFeatureName() {
        return this.mFeatureName;
    }

    public void setFeatureName(String featureName) {
        this.mFeatureName = featureName;
    }

    public String getAdminArea() {
        return this.mAdminArea;
    }

    public void setAdminArea(String adminArea) {
        this.mAdminArea = adminArea;
    }

    public String getSubAdminArea() {
        return this.mSubAdminArea;
    }

    public void setSubAdminArea(String subAdminArea) {
        this.mSubAdminArea = subAdminArea;
    }

    public String getLocality() {
        return this.mLocality;
    }

    public void setLocality(String locality) {
        this.mLocality = locality;
    }

    public String getSubLocality() {
        return this.mSubLocality;
    }

    public void setSubLocality(String sublocality) {
        this.mSubLocality = sublocality;
    }

    public String getThoroughfare() {
        return this.mThoroughfare;
    }

    public void setThoroughfare(String thoroughfare) {
        this.mThoroughfare = thoroughfare;
    }

    public String getSubThoroughfare() {
        return this.mSubThoroughfare;
    }

    public void setSubThoroughfare(String subthoroughfare) {
        this.mSubThoroughfare = subthoroughfare;
    }

    public String getPremises() {
        return this.mPremises;
    }

    public void setPremises(String premises) {
        this.mPremises = premises;
    }

    public String getPostalCode() {
        return this.mPostalCode;
    }

    public void setPostalCode(String postalCode) {
        this.mPostalCode = postalCode;
    }

    public String getCountryCode() {
        return this.mCountryCode;
    }

    public void setCountryCode(String countryCode) {
        this.mCountryCode = countryCode;
    }

    public String getCountryName() {
        return this.mCountryName;
    }

    public void setCountryName(String countryName) {
        this.mCountryName = countryName;
    }

    public boolean hasLatitude() {
        return this.mHasLatitude;
    }

    public double getLatitude() {
        if (this.mHasLatitude) {
            return this.mLatitude;
        }
        throw new IllegalStateException();
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
        this.mHasLatitude = true;
    }

    public void clearLatitude() {
        this.mHasLatitude = false;
    }

    public boolean hasLongitude() {
        return this.mHasLongitude;
    }

    public double getLongitude() {
        if (this.mHasLongitude) {
            return this.mLongitude;
        }
        throw new IllegalStateException();
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
        this.mHasLongitude = true;
    }

    public void clearLongitude() {
        this.mHasLongitude = false;
    }

    public String getPhone() {
        return this.mPhone;
    }

    public void setPhone(String phone) {
        this.mPhone = phone;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public void setUrl(String Url) {
        this.mUrl = Url;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public void setExtras(Bundle extras) {
        this.mExtras = extras == null ? null : new Bundle(extras);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Address[addressLines=[");
        for (int i = 0; i <= this.mMaxAddressLineIndex; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
            sb.append(':');
            String line = this.mAddressLines.get(Integer.valueOf(i));
            if (line == null) {
                sb.append("null");
            } else {
                sb.append('\"');
                sb.append(line);
                sb.append('\"');
            }
        }
        sb.append(']');
        sb.append(",feature=");
        sb.append(this.mFeatureName);
        sb.append(",admin=");
        sb.append(this.mAdminArea);
        sb.append(",sub-admin=");
        sb.append(this.mSubAdminArea);
        sb.append(",locality=");
        sb.append(this.mLocality);
        sb.append(",thoroughfare=");
        sb.append(this.mThoroughfare);
        sb.append(",postalCode=");
        sb.append(this.mPostalCode);
        sb.append(",countryCode=");
        sb.append(this.mCountryCode);
        sb.append(",countryName=");
        sb.append(this.mCountryName);
        sb.append(",hasLatitude=");
        sb.append(this.mHasLatitude);
        sb.append(",latitude=");
        sb.append(this.mLatitude);
        sb.append(",hasLongitude=");
        sb.append(this.mHasLongitude);
        sb.append(",longitude=");
        sb.append(this.mLongitude);
        sb.append(",phone=");
        sb.append(this.mPhone);
        sb.append(",url=");
        sb.append(this.mUrl);
        sb.append(",extras=");
        sb.append(this.mExtras);
        sb.append(']');
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        Bundle bundle = this.mExtras;
        if (bundle != null) {
            return bundle.describeContents();
        }
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mLocale.getLanguage());
        parcel.writeString(this.mLocale.getCountry());
        HashMap<Integer, String> hashMap = this.mAddressLines;
        if (hashMap == null) {
            parcel.writeInt(0);
        } else {
            Set<Map.Entry<Integer, String>> entries = hashMap.entrySet();
            parcel.writeInt(entries.size());
            for (Map.Entry<Integer, String> e : entries) {
                parcel.writeInt(e.getKey().intValue());
                parcel.writeString(e.getValue());
            }
        }
        parcel.writeString(this.mFeatureName);
        parcel.writeString(this.mAdminArea);
        parcel.writeString(this.mSubAdminArea);
        parcel.writeString(this.mLocality);
        parcel.writeString(this.mSubLocality);
        parcel.writeString(this.mThoroughfare);
        parcel.writeString(this.mSubThoroughfare);
        parcel.writeString(this.mPremises);
        parcel.writeString(this.mPostalCode);
        parcel.writeString(this.mCountryCode);
        parcel.writeString(this.mCountryName);
        parcel.writeInt(this.mHasLatitude ? 1 : 0);
        if (this.mHasLatitude) {
            parcel.writeDouble(this.mLatitude);
        }
        parcel.writeInt(this.mHasLongitude ? 1 : 0);
        if (this.mHasLongitude) {
            parcel.writeDouble(this.mLongitude);
        }
        parcel.writeString(this.mPhone);
        parcel.writeString(this.mUrl);
        parcel.writeBundle(this.mExtras);
    }
}
