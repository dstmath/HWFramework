package android.app.admin;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class SystemUpdatePolicy implements Parcelable {
    public static final Creator<SystemUpdatePolicy> CREATOR = null;
    private static final String KEY_INSTALL_WINDOW_END = "install_window_end";
    private static final String KEY_INSTALL_WINDOW_START = "install_window_start";
    private static final String KEY_POLICY_TYPE = "policy_type";
    public static final int TYPE_INSTALL_AUTOMATIC = 1;
    public static final int TYPE_INSTALL_WINDOWED = 2;
    public static final int TYPE_POSTPONE = 3;
    private static final int TYPE_UNKNOWN = -1;
    private static final int WINDOW_BOUNDARY = 1440;
    private int mMaintenanceWindowEnd;
    private int mMaintenanceWindowStart;
    private int mPolicyType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.admin.SystemUpdatePolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.admin.SystemUpdatePolicy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.admin.SystemUpdatePolicy.<clinit>():void");
    }

    private SystemUpdatePolicy() {
        this.mPolicyType = TYPE_UNKNOWN;
    }

    public static SystemUpdatePolicy createAutomaticInstallPolicy() {
        SystemUpdatePolicy policy = new SystemUpdatePolicy();
        policy.mPolicyType = TYPE_INSTALL_AUTOMATIC;
        return policy;
    }

    public static SystemUpdatePolicy createWindowedInstallPolicy(int startTime, int endTime) {
        if (startTime < 0 || startTime >= WINDOW_BOUNDARY || endTime < 0 || endTime >= WINDOW_BOUNDARY) {
            throw new IllegalArgumentException("startTime and endTime must be inside [0, 1440)");
        }
        SystemUpdatePolicy policy = new SystemUpdatePolicy();
        policy.mPolicyType = TYPE_INSTALL_WINDOWED;
        policy.mMaintenanceWindowStart = startTime;
        policy.mMaintenanceWindowEnd = endTime;
        return policy;
    }

    public static SystemUpdatePolicy createPostponeInstallPolicy() {
        SystemUpdatePolicy policy = new SystemUpdatePolicy();
        policy.mPolicyType = TYPE_POSTPONE;
        return policy;
    }

    public int getPolicyType() {
        return this.mPolicyType;
    }

    public int getInstallWindowStart() {
        if (this.mPolicyType == TYPE_INSTALL_WINDOWED) {
            return this.mMaintenanceWindowStart;
        }
        return TYPE_UNKNOWN;
    }

    public int getInstallWindowEnd() {
        if (this.mPolicyType == TYPE_INSTALL_WINDOWED) {
            return this.mMaintenanceWindowEnd;
        }
        return TYPE_UNKNOWN;
    }

    public boolean isValid() {
        boolean z = true;
        if (this.mPolicyType == TYPE_INSTALL_AUTOMATIC || this.mPolicyType == TYPE_POSTPONE) {
            return true;
        }
        if (this.mPolicyType != TYPE_INSTALL_WINDOWED) {
            return false;
        }
        if (this.mMaintenanceWindowStart < 0 || this.mMaintenanceWindowStart >= WINDOW_BOUNDARY || this.mMaintenanceWindowEnd < 0) {
            z = false;
        } else if (this.mMaintenanceWindowEnd >= WINDOW_BOUNDARY) {
            z = false;
        }
        return z;
    }

    public String toString() {
        Object[] objArr = new Object[TYPE_POSTPONE];
        objArr[0] = Integer.valueOf(this.mPolicyType);
        objArr[TYPE_INSTALL_AUTOMATIC] = Integer.valueOf(this.mMaintenanceWindowStart);
        objArr[TYPE_INSTALL_WINDOWED] = Integer.valueOf(this.mMaintenanceWindowEnd);
        return String.format("SystemUpdatePolicy (type: %d, windowStart: %d, windowEnd: %d)", objArr);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPolicyType);
        dest.writeInt(this.mMaintenanceWindowStart);
        dest.writeInt(this.mMaintenanceWindowEnd);
    }

    public static SystemUpdatePolicy restoreFromXml(XmlPullParser parser) {
        try {
            SystemUpdatePolicy policy = new SystemUpdatePolicy();
            String value = parser.getAttributeValue(null, KEY_POLICY_TYPE);
            if (value != null) {
                policy.mPolicyType = Integer.parseInt(value);
                value = parser.getAttributeValue(null, KEY_INSTALL_WINDOW_START);
                if (value != null) {
                    policy.mMaintenanceWindowStart = Integer.parseInt(value);
                }
                value = parser.getAttributeValue(null, KEY_INSTALL_WINDOW_END);
                if (value != null) {
                    policy.mMaintenanceWindowEnd = Integer.parseInt(value);
                }
                return policy;
            }
        } catch (NumberFormatException e) {
        }
        return null;
    }

    public void saveToXml(XmlSerializer out) throws IOException {
        out.attribute(null, KEY_POLICY_TYPE, Integer.toString(this.mPolicyType));
        out.attribute(null, KEY_INSTALL_WINDOW_START, Integer.toString(this.mMaintenanceWindowStart));
        out.attribute(null, KEY_INSTALL_WINDOW_END, Integer.toString(this.mMaintenanceWindowEnd));
    }
}
