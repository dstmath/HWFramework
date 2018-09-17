package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IccOpenLogicalChannelResponse implements Parcelable {
    public static final Creator<IccOpenLogicalChannelResponse> CREATOR = null;
    public static final int INVALID_CHANNEL = -1;
    public static final int STATUS_MISSING_RESOURCE = 2;
    public static final int STATUS_NO_ERROR = 1;
    public static final int STATUS_NO_SUCH_ELEMENT = 3;
    public static final int STATUS_UNKNOWN_ERROR = 4;
    private final int mChannel;
    private final byte[] mSelectResponse;
    private final int mStatus;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.IccOpenLogicalChannelResponse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.IccOpenLogicalChannelResponse.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.IccOpenLogicalChannelResponse.<clinit>():void");
    }

    public IccOpenLogicalChannelResponse(int channel, int status, byte[] selectResponse) {
        this.mChannel = channel;
        this.mStatus = status;
        this.mSelectResponse = selectResponse;
    }

    private IccOpenLogicalChannelResponse(Parcel in) {
        this.mChannel = in.readInt();
        this.mStatus = in.readInt();
        int arrayLength = in.readInt();
        if (arrayLength > 0) {
            this.mSelectResponse = new byte[arrayLength];
            in.readByteArray(this.mSelectResponse);
            return;
        }
        this.mSelectResponse = null;
    }

    public int getChannel() {
        return this.mChannel;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public byte[] getSelectResponse() {
        return this.mSelectResponse;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mChannel);
        out.writeInt(this.mStatus);
        if (this.mSelectResponse == null || this.mSelectResponse.length <= 0) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.mSelectResponse.length);
        out.writeByteArray(this.mSelectResponse);
    }

    public String toString() {
        return "Channel: " + this.mChannel + " Status: " + this.mStatus;
    }
}
