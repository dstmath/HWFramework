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
            boolean z = false;
            if (N > 0) {
                HashMap unused = a.mAddressLines = new HashMap(N);
                for (int i = 0; i < N; i++) {
                    int index = in.readInt();
                    a.mAddressLines.put(Integer.valueOf(index), in.readString());
                    int unused2 = a.mMaxAddressLineIndex = Math.max(a.mMaxAddressLineIndex, index);
                }
            } else {
                HashMap unused3 = a.mAddressLines = null;
                int unused4 = a.mMaxAddressLineIndex = -1;
            }
            String unused5 = a.mFeatureName = in.readString();
            String unused6 = a.mAdminArea = in.readString();
            String unused7 = a.mSubAdminArea = in.readString();
            String unused8 = a.mLocality = in.readString();
            String unused9 = a.mSubLocality = in.readString();
            String unused10 = a.mThoroughfare = in.readString();
            String unused11 = a.mSubThoroughfare = in.readString();
            String unused12 = a.mPremises = in.readString();
            String unused13 = a.mPostalCode = in.readString();
            String unused14 = a.mCountryCode = in.readString();
            String unused15 = a.mCountryName = in.readString();
            boolean unused16 = a.mHasLatitude = in.readInt() != 0;
            if (a.mHasLatitude) {
                double unused17 = a.mLatitude = in.readDouble();
            }
            if (in.readInt() != 0) {
                z = true;
            }
            boolean unused18 = a.mHasLongitude = z;
            if (a.mHasLongitude) {
                double unused19 = a.mLongitude = in.readDouble();
            }
            String unused20 = a.mPhone = in.readString();
            String unused21 = a.mUrl = in.readString();
            Bundle unused22 = a.mExtras = in.readBundle();
            return a;
        }

        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
    /* access modifiers changed from: private */
    public HashMap<Integer, String> mAddressLines;
    /* access modifiers changed from: private */
    public String mAdminArea;
    /* access modifiers changed from: private */
    public String mCountryCode;
    /* access modifiers changed from: private */
    public String mCountryName;
    /* access modifiers changed from: private */
    public Bundle mExtras = null;
    /* access modifiers changed from: private */
    public String mFeatureName;
    /* access modifiers changed from: private */
    public boolean mHasLatitude = false;
    /* access modifiers changed from: private */
    public boolean mHasLongitude = false;
    /* access modifiers changed from: private */
    public double mLatitude;
    private Locale mLocale;
    /* access modifiers changed from: private */
    public String mLocality;
    /* access modifiers changed from: private */
    public double mLongitude;
    /* access modifiers changed from: private */
    public int mMaxAddressLineIndex = -1;
    /* access modifiers changed from: private */
    public String mPhone;
    /* access modifiers changed from: private */
    public String mPostalCode;
    /* access modifiers changed from: private */
    public String mPremises;
    /* access modifiers changed from: private */
    public String mSubAdminArea;
    /* access modifiers changed from: private */
    public String mSubLocality;
    /* access modifiers changed from: private */
    public String mSubThoroughfare;
    /* access modifiers changed from: private */
    public String mThoroughfare;
    /* access modifiers changed from: private */
    public String mUrl;

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
        if (index < 0) {
            throw new IllegalArgumentException("index = " + index + " < 0");
        } else if (this.mAddressLines == null) {
            return null;
        } else {
            return this.mAddressLines.get(Integer.valueOf(index));
        }
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

    public int describeContents() {
        if (this.mExtras != null) {
            return this.mExtras.describeContents();
        }
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mLocale.getLanguage());
        parcel.writeString(this.mLocale.getCountry());
        if (this.mAddressLines == null) {
            parcel.writeInt(0);
        } else {
            Set<Map.Entry<Integer, String>> entries = this.mAddressLines.entrySet();
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
