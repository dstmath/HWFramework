package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.CallFailCause;
import com.huawei.internal.telephony.HwRadarUtils;

public class SmsCbMessage implements Parcelable {
    public static final Creator<SmsCbMessage> CREATOR = null;
    public static final int GEOGRAPHICAL_SCOPE_CELL_WIDE = 3;
    public static final int GEOGRAPHICAL_SCOPE_CELL_WIDE_IMMEDIATE = 0;
    public static final int GEOGRAPHICAL_SCOPE_LA_WIDE = 2;
    public static final int GEOGRAPHICAL_SCOPE_PLMN_WIDE = 1;
    protected static final String LOG_TAG = "SMSCB";
    public static final int MESSAGE_FORMAT_3GPP = 1;
    public static final int MESSAGE_FORMAT_3GPP2 = 2;
    public static final int MESSAGE_PRIORITY_EMERGENCY = 3;
    public static final int MESSAGE_PRIORITY_INTERACTIVE = 1;
    public static final int MESSAGE_PRIORITY_NORMAL = 0;
    public static final int MESSAGE_PRIORITY_URGENT = 2;
    private final String mBody;
    private final SmsCbCmasInfo mCmasWarningInfo;
    private final SmsCbEtwsInfo mEtwsWarningInfo;
    private final int mGeographicalScope;
    private final String mLanguage;
    private final SmsCbLocation mLocation;
    private final int mMessageFormat;
    private final int mPriority;
    private final int mSerialNumber;
    private final int mServiceCategory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.SmsCbMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.SmsCbMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SmsCbMessage.<clinit>():void");
    }

    public SmsCbMessage(int messageFormat, int geographicalScope, int serialNumber, SmsCbLocation location, int serviceCategory, String language, String body, int priority, SmsCbEtwsInfo etwsWarningInfo, SmsCbCmasInfo cmasWarningInfo) {
        this.mMessageFormat = messageFormat;
        this.mGeographicalScope = geographicalScope;
        this.mSerialNumber = serialNumber;
        this.mLocation = location;
        this.mServiceCategory = serviceCategory;
        this.mLanguage = language;
        this.mBody = body;
        this.mPriority = priority;
        this.mEtwsWarningInfo = etwsWarningInfo;
        this.mCmasWarningInfo = cmasWarningInfo;
    }

    public SmsCbMessage(Parcel in) {
        this.mMessageFormat = in.readInt();
        this.mGeographicalScope = in.readInt();
        this.mSerialNumber = in.readInt();
        this.mLocation = new SmsCbLocation(in);
        this.mServiceCategory = in.readInt();
        this.mLanguage = in.readString();
        this.mBody = in.readString();
        this.mPriority = in.readInt();
        switch (in.readInt()) {
            case HwRadarUtils.RADAR_LEVEL_C /*67*/:
                this.mEtwsWarningInfo = null;
                this.mCmasWarningInfo = new SmsCbCmasInfo(in);
            case CallFailCause.REQUESTED_FACILITY_NOT_IMPLEMENTED /*69*/:
                this.mEtwsWarningInfo = new SmsCbEtwsInfo(in);
                this.mCmasWarningInfo = null;
            default:
                this.mEtwsWarningInfo = null;
                this.mCmasWarningInfo = null;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMessageFormat);
        dest.writeInt(this.mGeographicalScope);
        dest.writeInt(this.mSerialNumber);
        this.mLocation.writeToParcel(dest, flags);
        dest.writeInt(this.mServiceCategory);
        dest.writeString(this.mLanguage);
        dest.writeString(this.mBody);
        dest.writeInt(this.mPriority);
        if (this.mEtwsWarningInfo != null) {
            dest.writeInt(69);
            this.mEtwsWarningInfo.writeToParcel(dest, flags);
        } else if (this.mCmasWarningInfo != null) {
            dest.writeInt(67);
            this.mCmasWarningInfo.writeToParcel(dest, flags);
        } else {
            dest.writeInt(48);
        }
    }

    public int getGeographicalScope() {
        return this.mGeographicalScope;
    }

    public int getSerialNumber() {
        return this.mSerialNumber;
    }

    public SmsCbLocation getLocation() {
        return this.mLocation;
    }

    public int getServiceCategory() {
        return this.mServiceCategory;
    }

    public String getLanguageCode() {
        return this.mLanguage;
    }

    public String getMessageBody() {
        return this.mBody;
    }

    public int getMessageFormat() {
        return this.mMessageFormat;
    }

    public int getMessagePriority() {
        return this.mPriority;
    }

    public SmsCbEtwsInfo getEtwsWarningInfo() {
        return this.mEtwsWarningInfo;
    }

    public SmsCbCmasInfo getCmasWarningInfo() {
        return this.mCmasWarningInfo;
    }

    public boolean isEmergencyMessage() {
        return this.mPriority == MESSAGE_PRIORITY_EMERGENCY;
    }

    public boolean isEtwsMessage() {
        return this.mEtwsWarningInfo != null;
    }

    public boolean isCmasMessage() {
        return this.mCmasWarningInfo != null;
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("SmsCbMessage{geographicalScope=").append(this.mGeographicalScope).append(", serialNumber=").append(this.mSerialNumber).append(", location=").append(this.mLocation).append(", serviceCategory=").append(this.mServiceCategory).append(", language=").append(this.mLanguage).append(", body=").append(this.mBody).append(", priority=").append(this.mPriority).append(this.mEtwsWarningInfo != null ? ", " + this.mEtwsWarningInfo.toString() : "");
        if (this.mCmasWarningInfo != null) {
            str = ", " + this.mCmasWarningInfo.toString();
        } else {
            str = "";
        }
        return append.append(str).append('}').toString();
    }

    public int describeContents() {
        return MESSAGE_PRIORITY_NORMAL;
    }
}
