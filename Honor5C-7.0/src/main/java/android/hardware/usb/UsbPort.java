package android.hardware.usb;

import android.net.NetworkPolicyManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech;
import com.android.internal.util.Preconditions;

public final class UsbPort implements Parcelable {
    public static final Creator<UsbPort> CREATOR = null;
    public static final int DATA_ROLE_DEVICE = 2;
    public static final int DATA_ROLE_HOST = 1;
    public static final int MODE_DFP = 1;
    public static final int MODE_DUAL = 3;
    public static final int MODE_UFP = 2;
    private static final int NUM_DATA_ROLES = 3;
    public static final int POWER_ROLE_SINK = 2;
    public static final int POWER_ROLE_SOURCE = 1;
    private final String mId;
    private final int mSupportedModes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.usb.UsbPort.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.usb.UsbPort.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.usb.UsbPort.<clinit>():void");
    }

    public static java.lang.String roleCombinationsToString(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.usb.UsbPort.roleCombinationsToString(int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.usb.UsbPort.roleCombinationsToString(int):java.lang.String");
    }

    public UsbPort(String id, int supportedModes) {
        this.mId = id;
        this.mSupportedModes = supportedModes;
    }

    public String getId() {
        return this.mId;
    }

    public int getSupportedModes() {
        return this.mSupportedModes;
    }

    public static int combineRolesAsBit(int powerRole, int dataRole) {
        checkRoles(powerRole, dataRole);
        return POWER_ROLE_SOURCE << ((powerRole * NUM_DATA_ROLES) + dataRole);
    }

    public static String modeToString(int mode) {
        switch (mode) {
            case TextToSpeech.SUCCESS /*0*/:
                return NetworkPolicyManager.FIREWALL_CHAIN_NAME_NONE;
            case POWER_ROLE_SOURCE /*1*/:
                return "dfp";
            case POWER_ROLE_SINK /*2*/:
                return "ufp";
            case NUM_DATA_ROLES /*3*/:
                return "dual";
            default:
                return Integer.toString(mode);
        }
    }

    public static String powerRoleToString(int role) {
        switch (role) {
            case TextToSpeech.SUCCESS /*0*/:
                return "no-power";
            case POWER_ROLE_SOURCE /*1*/:
                return "source";
            case POWER_ROLE_SINK /*2*/:
                return "sink";
            default:
                return Integer.toString(role);
        }
    }

    public static String dataRoleToString(int role) {
        switch (role) {
            case TextToSpeech.SUCCESS /*0*/:
                return "no-data";
            case POWER_ROLE_SOURCE /*1*/:
                return "host";
            case POWER_ROLE_SINK /*2*/:
                return UsbManager.EXTRA_DEVICE;
            default:
                return Integer.toString(role);
        }
    }

    public static void checkRoles(int powerRole, int dataRole) {
        Preconditions.checkArgumentInRange(powerRole, 0, POWER_ROLE_SINK, "powerRole");
        Preconditions.checkArgumentInRange(dataRole, 0, POWER_ROLE_SINK, "dataRole");
    }

    public String toString() {
        return "UsbPort{id=" + this.mId + ", supportedModes=" + modeToString(this.mSupportedModes) + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeInt(this.mSupportedModes);
    }
}
