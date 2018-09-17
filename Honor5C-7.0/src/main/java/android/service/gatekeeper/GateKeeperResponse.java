package android.service.gatekeeper;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class GateKeeperResponse implements Parcelable {
    public static final Creator<GateKeeperResponse> CREATOR = null;
    public static final int RESPONSE_ERROR = -1;
    public static final int RESPONSE_OK = 0;
    public static final int RESPONSE_RETRY = 1;
    private byte[] mPayload;
    private final int mResponseCode;
    private boolean mShouldReEnroll;
    private int mTimeout;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.gatekeeper.GateKeeperResponse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.gatekeeper.GateKeeperResponse.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.service.gatekeeper.GateKeeperResponse.<clinit>():void");
    }

    private GateKeeperResponse(int responseCode) {
        this.mResponseCode = responseCode;
    }

    private GateKeeperResponse(int responseCode, int timeout) {
        this.mResponseCode = responseCode;
    }

    public int describeContents() {
        return RESPONSE_OK;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = RESPONSE_RETRY;
        dest.writeInt(this.mResponseCode);
        if (this.mResponseCode == RESPONSE_RETRY) {
            dest.writeInt(this.mTimeout);
        } else if (this.mResponseCode == 0) {
            if (!this.mShouldReEnroll) {
                i = RESPONSE_OK;
            }
            dest.writeInt(i);
            if (this.mPayload != null) {
                dest.writeInt(this.mPayload.length);
                dest.writeByteArray(this.mPayload);
            }
        }
    }

    public byte[] getPayload() {
        return this.mPayload;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public boolean getShouldReEnroll() {
        return this.mShouldReEnroll;
    }

    public int getResponseCode() {
        return this.mResponseCode;
    }

    private void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }

    private void setShouldReEnroll(boolean shouldReEnroll) {
        this.mShouldReEnroll = shouldReEnroll;
    }

    private void setPayload(byte[] payload) {
        this.mPayload = payload;
    }
}
