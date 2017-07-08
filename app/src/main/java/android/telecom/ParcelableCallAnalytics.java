package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ParcelableCallAnalytics implements Parcelable {
    public static final int CALLTYPE_INCOMING = 1;
    public static final int CALLTYPE_OUTGOING = 2;
    public static final int CALLTYPE_UNKNOWN = 0;
    public static final int CDMA_PHONE = 1;
    public static final Creator<ParcelableCallAnalytics> CREATOR = null;
    public static final int GSM_PHONE = 2;
    public static final int IMS_PHONE = 4;
    public static final long MILLIS_IN_1_SECOND = 1000;
    public static final long MILLIS_IN_5_MINUTES = 300000;
    public static final int SIP_PHONE = 8;
    public static final int STILL_CONNECTED = -1;
    public static final int THIRD_PARTY_PHONE = 16;
    private final long callDurationMillis;
    private final int callTechnologies;
    private final int callTerminationCode;
    private final int callType;
    private final String connectionService;
    private final boolean isAdditionalCall;
    private final boolean isCreatedFromExistingConnection;
    private final boolean isEmergencyCall;
    private final boolean isInterrupted;
    private final long startTimeMillis;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.ParcelableCallAnalytics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.ParcelableCallAnalytics.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.ParcelableCallAnalytics.<clinit>():void");
    }

    public ParcelableCallAnalytics(long startTimeMillis, long callDurationMillis, int callType, boolean isAdditionalCall, boolean isInterrupted, int callTechnologies, int callTerminationCode, boolean isEmergencyCall, String connectionService, boolean isCreatedFromExistingConnection) {
        this.startTimeMillis = startTimeMillis;
        this.callDurationMillis = callDurationMillis;
        this.callType = callType;
        this.isAdditionalCall = isAdditionalCall;
        this.isInterrupted = isInterrupted;
        this.callTechnologies = callTechnologies;
        this.callTerminationCode = callTerminationCode;
        this.isEmergencyCall = isEmergencyCall;
        this.connectionService = connectionService;
        this.isCreatedFromExistingConnection = isCreatedFromExistingConnection;
    }

    public ParcelableCallAnalytics(Parcel in) {
        this.startTimeMillis = in.readLong();
        this.callDurationMillis = in.readLong();
        this.callType = in.readInt();
        this.isAdditionalCall = readByteAsBoolean(in);
        this.isInterrupted = readByteAsBoolean(in);
        this.callTechnologies = in.readInt();
        this.callTerminationCode = in.readInt();
        this.isEmergencyCall = readByteAsBoolean(in);
        this.connectionService = in.readString();
        this.isCreatedFromExistingConnection = readByteAsBoolean(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.startTimeMillis);
        out.writeLong(this.callDurationMillis);
        out.writeInt(this.callType);
        writeBooleanAsByte(out, this.isAdditionalCall);
        writeBooleanAsByte(out, this.isInterrupted);
        out.writeInt(this.callTechnologies);
        out.writeInt(this.callTerminationCode);
        writeBooleanAsByte(out, this.isEmergencyCall);
        out.writeString(this.connectionService);
        writeBooleanAsByte(out, this.isCreatedFromExistingConnection);
    }

    public long getStartTimeMillis() {
        return this.startTimeMillis;
    }

    public long getCallDurationMillis() {
        return this.callDurationMillis;
    }

    public int getCallType() {
        return this.callType;
    }

    public boolean isAdditionalCall() {
        return this.isAdditionalCall;
    }

    public boolean isInterrupted() {
        return this.isInterrupted;
    }

    public int getCallTechnologies() {
        return this.callTechnologies;
    }

    public int getCallTerminationCode() {
        return this.callTerminationCode;
    }

    public boolean isEmergencyCall() {
        return this.isEmergencyCall;
    }

    public String getConnectionService() {
        return this.connectionService;
    }

    public boolean isCreatedFromExistingConnection() {
        return this.isCreatedFromExistingConnection;
    }

    public int describeContents() {
        return CALLTYPE_UNKNOWN;
    }

    private static void writeBooleanAsByte(Parcel out, boolean b) {
        out.writeByte((byte) (b ? CDMA_PHONE : CALLTYPE_UNKNOWN));
    }

    private static boolean readByteAsBoolean(Parcel in) {
        return in.readByte() == (byte) 1;
    }
}
