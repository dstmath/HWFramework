package ohos.location;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import ohos.telephony.TelephoneNumberUtils;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class GeoAddress implements Sequenceable {
    private static final double DEFAULT_LAT = 0.0d;
    private static final double DEFAULT_LON = 0.0d;
    private static final double PARCEL_INT_SIZE = 64.0d;
    private String mAddressUrl;
    private String mAdministrativeArea;
    private String mCountryCode;
    private String mCountryName;
    private HashMap<Integer, String> mDescriptions;
    private int mDescriptionsSize = 0;
    private boolean mHasLatitude = false;
    private boolean mHasLongitude = false;
    private double mLatitude = 0.0d;
    private Locale mLocale;
    private String mLocality;
    private double mLongitude = 0.0d;
    private String mPhoneNumber;
    private String mPlaceName;
    private String mPostalCode;
    private String mPremises;
    private String mRoadName;
    private String mSubAdministrativeArea;
    private String mSubLocality;
    private String mSubRoadName;

    public GeoAddress(Locale locale) {
        this.mLocale = locale;
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    public int getDescriptionsSize() {
        return this.mDescriptionsSize;
    }

    public String getDescriptions(int i) {
        HashMap<Integer, String> hashMap;
        if (i >= 0 && (hashMap = this.mDescriptions) != null) {
            return hashMap.get(Integer.valueOf(i));
        }
        return null;
    }

    public void setDescriptions(int i, String str) throws IllegalArgumentException {
        if (i >= 0) {
            if (this.mDescriptions == null) {
                this.mDescriptions = new HashMap<>();
            }
            this.mDescriptions.put(Integer.valueOf(i), str);
            if (str == null) {
                this.mDescriptionsSize = -1;
                for (Integer num : this.mDescriptions.keySet()) {
                    this.mDescriptionsSize = Math.max(this.mDescriptionsSize, num.intValue());
                }
                return;
            }
            this.mDescriptionsSize = Math.max(this.mDescriptionsSize, i);
            return;
        }
        throw new IllegalArgumentException("index = " + i + " is illegal");
    }

    public String getPlaceName() {
        return this.mPlaceName;
    }

    public void setPlaceName(String str) {
        this.mPlaceName = str;
    }

    public String getAdministrativeArea() {
        return this.mAdministrativeArea;
    }

    public void setAdministrativeArea(String str) {
        this.mAdministrativeArea = str;
    }

    public String getSubAdministrativeArea() {
        return this.mSubAdministrativeArea;
    }

    public void setSubAdministrativeArea(String str) {
        this.mSubAdministrativeArea = str;
    }

    public String getLocality() {
        return this.mLocality;
    }

    public void setLocality(String str) {
        this.mLocality = str;
    }

    public String getSubLocality() {
        return this.mSubLocality;
    }

    public void setSubLocality(String str) {
        this.mSubLocality = str;
    }

    public String getRoadName() {
        return this.mRoadName;
    }

    public void setRoadName(String str) {
        this.mRoadName = str;
    }

    public String getSubRoadName() {
        return this.mSubRoadName;
    }

    public void setSubRoadName(String str) {
        this.mSubRoadName = str;
    }

    public String getPremises() {
        return this.mPremises;
    }

    public void setPremises(String str) {
        this.mPremises = str;
    }

    public String getPostalCode() {
        return this.mPostalCode;
    }

    public void setPostalCode(String str) {
        this.mPostalCode = str;
    }

    public String getCountryCode() {
        return this.mCountryCode;
    }

    public void setCountryCode(String str) {
        this.mCountryCode = str;
    }

    public String getCountryName() {
        return this.mCountryName;
    }

    public void setCountryName(String str) {
        this.mCountryName = str;
    }

    public boolean hasLatitude() {
        return this.mHasLatitude;
    }

    public double getLatitude() {
        if (this.mHasLatitude) {
            return this.mLatitude;
        }
        return 0.0d;
    }

    public void setLatitude(double d) throws IllegalArgumentException {
        if (d < -90.0d || d > 90.0d) {
            throw new IllegalArgumentException("latitude should be in range [-90,90]");
        }
        this.mLatitude = d;
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
        return 0.0d;
    }

    public void setLongitude(double d) throws IllegalArgumentException {
        if (d < -180.0d || d > 180.0d) {
            throw new IllegalArgumentException("longitude should be in range [-180,180]");
        }
        this.mLongitude = d;
        this.mHasLongitude = true;
    }

    public void clearLongitude() {
        this.mHasLongitude = false;
    }

    public String getPhoneNumber() {
        return this.mPhoneNumber;
    }

    public void setPhoneNumber(String str) {
        this.mPhoneNumber = str;
    }

    public String getAddressUrl() {
        return this.mAddressUrl;
    }

    public void setAddressUrl(String str) {
        this.mAddressUrl = str;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Address:descriptions=[");
        if (this.mDescriptions == null) {
            sb.append("null");
        } else {
            for (int i = 0; i <= this.mDescriptionsSize; i++) {
                if (i > 0) {
                    sb.append(TelephoneNumberUtils.PAUSE);
                }
                sb.append(i);
                sb.append(':');
                String str = this.mDescriptions.get(Integer.valueOf(i));
                if (str == null) {
                    sb.append("null");
                } else {
                    sb.append('\"');
                    sb.append(str);
                    sb.append('\"');
                }
            }
        }
        sb.append(']');
        sb.append(",place=");
        sb.append(this.mPlaceName);
        sb.append(",administrative=");
        sb.append(this.mAdministrativeArea);
        sb.append(",sub-administrative=");
        sb.append(this.mSubAdministrativeArea);
        sb.append(",locality=");
        sb.append(this.mLocality);
        sb.append(",roadName=");
        sb.append(this.mRoadName);
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
        sb.append(",phone number=");
        sb.append(this.mPhoneNumber);
        sb.append(",address url=");
        sb.append(this.mAddressUrl);
        sb.append(']');
        return sb.toString();
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        Locale locale;
        String readString = parcel.readString();
        String readString2 = parcel.readString();
        if (readString2.length() > 0) {
            locale = new Locale(readString, readString2);
        } else {
            locale = new Locale(readString);
        }
        this.mLocale = locale;
        int readInt = parcel.readInt();
        boolean z = false;
        if (readInt > 0) {
            this.mDescriptions = new HashMap<>();
            for (int i = 0; i < readInt && ((double) parcel.getWritableBytes()) >= PARCEL_INT_SIZE; i++) {
                int readInt2 = parcel.readInt();
                this.mDescriptions.put(Integer.valueOf(readInt2), parcel.readString());
                this.mDescriptionsSize = Math.max(this.mDescriptionsSize, readInt2);
            }
        } else {
            this.mDescriptions = null;
            this.mDescriptionsSize = -1;
        }
        this.mPlaceName = parcel.readString();
        this.mAdministrativeArea = parcel.readString();
        this.mSubAdministrativeArea = parcel.readString();
        this.mLocality = parcel.readString();
        this.mSubLocality = parcel.readString();
        this.mRoadName = parcel.readString();
        this.mSubRoadName = parcel.readString();
        this.mPremises = parcel.readString();
        this.mPostalCode = parcel.readString();
        this.mCountryCode = parcel.readString();
        this.mCountryName = parcel.readString();
        this.mHasLatitude = parcel.readInt() != 0;
        if (this.mHasLatitude) {
            this.mLatitude = parcel.readDouble();
        }
        if (parcel.readInt() != 0) {
            z = true;
        }
        this.mHasLongitude = z;
        if (this.mHasLongitude) {
            this.mLongitude = parcel.readDouble();
        }
        this.mPhoneNumber = parcel.readString();
        this.mAddressUrl = parcel.readString();
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.mLocale.getLanguage());
        parcel.writeString(this.mLocale.getCountry());
        HashMap<Integer, String> hashMap = this.mDescriptions;
        if (hashMap == null) {
            parcel.writeInt(0);
        } else {
            Set<Map.Entry<Integer, String>> entrySet = hashMap.entrySet();
            parcel.writeInt(entrySet.size());
            for (Map.Entry<Integer, String> entry : entrySet) {
                parcel.writeInt(entry.getKey().intValue());
                parcel.writeString(entry.getValue());
            }
        }
        parcel.writeString(this.mPlaceName);
        parcel.writeString(this.mAdministrativeArea);
        parcel.writeString(this.mSubAdministrativeArea);
        parcel.writeString(this.mLocality);
        parcel.writeString(this.mSubLocality);
        parcel.writeString(this.mRoadName);
        parcel.writeString(this.mSubRoadName);
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
        parcel.writeString(this.mPhoneNumber);
        parcel.writeString(this.mAddressUrl);
        return true;
    }
}
