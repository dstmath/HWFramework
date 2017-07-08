package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class DataUsageRequest implements Parcelable {
    public static final Creator<DataUsageRequest> CREATOR = null;
    public static final String PARCELABLE_KEY = "DataUsageRequest";
    public static final int REQUEST_ID_UNSET = 0;
    public final int requestId;
    public final NetworkTemplate template;
    public final long thresholdInBytes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.DataUsageRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.DataUsageRequest.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.DataUsageRequest.<clinit>():void");
    }

    public DataUsageRequest(int requestId, NetworkTemplate template, long thresholdInBytes) {
        this.requestId = requestId;
        this.template = template;
        this.thresholdInBytes = thresholdInBytes;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.requestId);
        dest.writeParcelable(this.template, flags);
        dest.writeLong(this.thresholdInBytes);
    }

    public String toString() {
        return "DataUsageRequest [ requestId=" + this.requestId + ", networkTemplate=" + this.template + ", thresholdInBytes=" + this.thresholdInBytes + " ]";
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DataUsageRequest)) {
            return false;
        }
        DataUsageRequest that = (DataUsageRequest) obj;
        if (that.requestId == this.requestId && Objects.equals(that.template, this.template) && that.thresholdInBytes == this.thresholdInBytes) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.requestId), this.template, Long.valueOf(this.thresholdInBytes)});
    }
}
