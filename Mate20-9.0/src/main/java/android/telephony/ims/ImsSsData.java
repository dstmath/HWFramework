package android.telephony.ims;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class ImsSsData implements Parcelable {
    public static final Parcelable.Creator<ImsSsData> CREATOR = new Parcelable.Creator<ImsSsData>() {
        public ImsSsData createFromParcel(Parcel in) {
            return new ImsSsData(in);
        }

        public ImsSsData[] newArray(int size) {
            return new ImsSsData[size];
        }
    };
    public static final int RESULT_SUCCESS = 0;
    public static final int SERVICE_CLASS_DATA = 2;
    public static final int SERVICE_CLASS_DATA_ASYNC = 32;
    public static final int SERVICE_CLASS_DATA_SYNC = 16;
    public static final int SERVICE_CLASS_FAX = 4;
    public static final int SERVICE_CLASS_NONE = 0;
    public static final int SERVICE_CLASS_PACKET = 64;
    public static final int SERVICE_CLASS_PAD = 128;
    public static final int SERVICE_CLASS_SMS = 8;
    public static final int SERVICE_CLASS_VOICE = 1;
    public static final int SS_ACTIVATION = 0;
    public static final int SS_ALL_BARRING = 18;
    public static final int SS_ALL_DATA_TELESERVICES = 3;
    public static final int SS_ALL_TELESERVICES_EXCEPT_SMS = 5;
    public static final int SS_ALL_TELESEVICES = 1;
    public static final int SS_ALL_TELE_AND_BEARER_SERVICES = 0;
    public static final int SS_BAIC = 16;
    public static final int SS_BAIC_ROAMING = 17;
    public static final int SS_BAOC = 13;
    public static final int SS_BAOIC = 14;
    public static final int SS_BAOIC_EXC_HOME = 15;
    public static final int SS_CFU = 0;
    public static final int SS_CFUT = 6;
    public static final int SS_CF_ALL = 4;
    public static final int SS_CF_ALL_CONDITIONAL = 5;
    public static final int SS_CF_BUSY = 1;
    public static final int SS_CF_NOT_REACHABLE = 3;
    public static final int SS_CF_NO_REPLY = 2;
    public static final int SS_CLIP = 7;
    public static final int SS_CLIR = 8;
    public static final int SS_CNAP = 11;
    public static final int SS_COLP = 9;
    public static final int SS_COLR = 10;
    public static final int SS_DEACTIVATION = 1;
    public static final int SS_ERASURE = 4;
    public static final int SS_INCOMING_BARRING = 20;
    public static final int SS_INCOMING_BARRING_ANONYMOUS = 22;
    public static final int SS_INCOMING_BARRING_DN = 21;
    public static final int SS_INTERROGATION = 2;
    public static final int SS_OUTGOING_BARRING = 19;
    public static final int SS_REGISTRATION = 3;
    public static final int SS_SMS_SERVICES = 4;
    public static final int SS_TELEPHONY = 2;
    public static final int SS_WAIT = 12;
    private ImsCallForwardInfo[] mCfInfo;
    private ImsSsInfo[] mImsSsInfo;
    private int[] mSsInfo;
    public int requestType;
    public final int result;
    public int serviceClass;
    public int serviceType;
    public int teleserviceType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ServiceClass {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ServiceType {
    }

    public ImsSsData(int serviceType2, int requestType2, int teleserviceType2, int serviceClass2, int result2) {
        this.serviceType = serviceType2;
        this.requestType = requestType2;
        this.teleserviceType = teleserviceType2;
        this.serviceClass = serviceClass2;
        this.result = result2;
    }

    private ImsSsData(Parcel in) {
        this.serviceType = in.readInt();
        this.requestType = in.readInt();
        this.teleserviceType = in.readInt();
        this.serviceClass = in.readInt();
        this.result = in.readInt();
        this.mSsInfo = in.createIntArray();
        this.mCfInfo = (ImsCallForwardInfo[]) in.readParcelableArray(getClass().getClassLoader());
        this.mImsSsInfo = (ImsSsInfo[]) in.readParcelableArray(getClass().getClassLoader());
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.serviceType);
        out.writeInt(this.requestType);
        out.writeInt(this.teleserviceType);
        out.writeInt(this.serviceClass);
        out.writeInt(this.result);
        out.writeIntArray(this.mSsInfo);
        out.writeParcelableArray(this.mCfInfo, 0);
        out.writeParcelableArray(this.mImsSsInfo, 0);
    }

    public int describeContents() {
        return 0;
    }

    public boolean isTypeCF() {
        return this.serviceType == 0 || this.serviceType == 1 || this.serviceType == 2 || this.serviceType == 3 || this.serviceType == 4 || this.serviceType == 5;
    }

    public boolean isTypeCf() {
        return isTypeCF();
    }

    public boolean isTypeUnConditional() {
        return this.serviceType == 0 || this.serviceType == 4;
    }

    public boolean isTypeCW() {
        return this.serviceType == 12;
    }

    public boolean isTypeCw() {
        return isTypeCW();
    }

    public boolean isTypeClip() {
        return this.serviceType == 7;
    }

    public boolean isTypeColr() {
        return this.serviceType == 10;
    }

    public boolean isTypeColp() {
        return this.serviceType == 9;
    }

    public boolean isTypeClir() {
        return this.serviceType == 8;
    }

    public boolean isTypeIcb() {
        return this.serviceType == 21 || this.serviceType == 22;
    }

    public boolean isTypeBarring() {
        return this.serviceType == 13 || this.serviceType == 14 || this.serviceType == 15 || this.serviceType == 16 || this.serviceType == 17 || this.serviceType == 18 || this.serviceType == 19 || this.serviceType == 20;
    }

    public boolean isTypeInterrogation() {
        return this.serviceType == 2;
    }

    public void setSuppServiceInfo(int[] ssInfo) {
        this.mSsInfo = ssInfo;
    }

    public void setImsSpecificSuppServiceInfo(ImsSsInfo[] imsSsInfo) {
        this.mImsSsInfo = imsSsInfo;
    }

    public void setCallForwardingInfo(ImsCallForwardInfo[] cfInfo) {
        this.mCfInfo = cfInfo;
    }

    public int[] getSuppServiceInfo() {
        return this.mSsInfo;
    }

    public ImsSsInfo[] getImsSpecificSuppServiceInfo() {
        return this.mImsSsInfo;
    }

    public ImsCallForwardInfo[] getCallForwardInfo() {
        return this.mCfInfo;
    }

    public String toString() {
        return "[ImsSsData] ServiceType: " + this.serviceType + " RequestType: " + this.requestType + " TeleserviceType: " + this.teleserviceType + " ServiceClass: " + this.serviceClass + " Result: " + this.result;
    }
}
