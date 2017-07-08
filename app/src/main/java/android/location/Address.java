package android.location;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

public class Address implements Parcelable {
    public static final Creator<Address> CREATOR = null;
    private HashMap<Integer, String> mAddressLines;
    private String mAdminArea;
    private String mCountryCode;
    private String mCountryName;
    private Bundle mExtras;
    private String mFeatureName;
    private boolean mHasLatitude;
    private boolean mHasLongitude;
    private double mLatitude;
    private Locale mLocale;
    private String mLocality;
    private double mLongitude;
    private int mMaxAddressLineIndex;
    private String mPhone;
    private String mPostalCode;
    private String mPremises;
    private String mSubAdminArea;
    private String mSubLocality;
    private String mSubThoroughfare;
    private String mThoroughfare;
    private String mUrl;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.Address.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.Address.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.location.Address.<clinit>():void");
    }

    public Address(Locale locale) {
        this.mMaxAddressLineIndex = -1;
        this.mHasLatitude = false;
        this.mHasLongitude = false;
        this.mExtras = null;
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
            return (String) this.mAddressLines.get(Integer.valueOf(index));
        }
    }

    public void setAddressLine(int index, String line) {
        if (index < 0) {
            throw new IllegalArgumentException("index = " + index + " < 0");
        }
        if (this.mAddressLines == null) {
            this.mAddressLines = new HashMap();
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
        Bundle bundle = null;
        if (extras != null) {
            bundle = new Bundle(extras);
        }
        this.mExtras = bundle;
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
            String line = (String) this.mAddressLines.get(Integer.valueOf(i));
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
        return this.mExtras != null ? this.mExtras.describeContents() : 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2 = 1;
        parcel.writeString(this.mLocale.getLanguage());
        parcel.writeString(this.mLocale.getCountry());
        if (this.mAddressLines == null) {
            parcel.writeInt(0);
        } else {
            Set<Entry<Integer, String>> entries = this.mAddressLines.entrySet();
            parcel.writeInt(entries.size());
            for (Entry<Integer, String> e : entries) {
                parcel.writeInt(((Integer) e.getKey()).intValue());
                parcel.writeString((String) e.getValue());
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
        if (this.mHasLatitude) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mHasLatitude) {
            parcel.writeDouble(this.mLatitude);
        }
        if (!this.mHasLongitude) {
            i2 = 0;
        }
        parcel.writeInt(i2);
        if (this.mHasLongitude) {
            parcel.writeDouble(this.mLongitude);
        }
        parcel.writeString(this.mPhone);
        parcel.writeString(this.mUrl);
        parcel.writeBundle(this.mExtras);
    }
}
