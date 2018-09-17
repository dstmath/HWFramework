package android.print;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public final class PrinterId implements Parcelable {
    public static final Creator<PrinterId> CREATOR = null;
    private final String mLocalId;
    private final ComponentName mServiceName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.print.PrinterId.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.print.PrinterId.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.print.PrinterId.<clinit>():void");
    }

    public PrinterId(ComponentName serviceName, String localId) {
        this.mServiceName = serviceName;
        this.mLocalId = localId;
    }

    private PrinterId(Parcel parcel) {
        this.mServiceName = (ComponentName) Preconditions.checkNotNull((ComponentName) parcel.readParcelable(null));
        this.mLocalId = (String) Preconditions.checkNotNull(parcel.readString());
    }

    public ComponentName getServiceName() {
        return this.mServiceName;
    }

    public String getLocalId() {
        return this.mLocalId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mServiceName, flags);
        parcel.writeString(this.mLocalId);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PrinterId other = (PrinterId) object;
        return this.mServiceName.equals(other.mServiceName) && this.mLocalId.equals(other.mLocalId);
    }

    public int hashCode() {
        return ((this.mServiceName.hashCode() + 31) * 31) + this.mLocalId.hashCode();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PrinterId{");
        builder.append("serviceName=").append(this.mServiceName.flattenToString());
        builder.append(", localId=").append(this.mLocalId);
        builder.append('}');
        return builder.toString();
    }
}
