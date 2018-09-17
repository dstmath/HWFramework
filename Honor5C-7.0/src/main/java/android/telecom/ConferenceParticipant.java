package android.telecom;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ConferenceParticipant implements Parcelable {
    public static final Creator<ConferenceParticipant> CREATOR = null;
    private final String mDisplayName;
    private final Uri mEndpoint;
    private final Uri mHandle;
    private final int mState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.ConferenceParticipant.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.ConferenceParticipant.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConferenceParticipant.<clinit>():void");
    }

    public ConferenceParticipant(Uri handle, String displayName, Uri endpoint, int state) {
        this.mHandle = handle;
        this.mDisplayName = displayName;
        this.mEndpoint = endpoint;
        this.mState = state;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mHandle, 0);
        dest.writeString(this.mDisplayName);
        dest.writeParcelable(this.mEndpoint, 0);
        dest.writeInt(this.mState);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ConferenceParticipant Handle: ");
        sb.append("XXX");
        sb.append(" DisplayName: ");
        sb.append(this.mDisplayName);
        sb.append(" Endpoint: ");
        sb.append("XXX");
        sb.append(" State: ");
        sb.append(this.mState);
        sb.append("]");
        return sb.toString();
    }

    public Uri getHandle() {
        return this.mHandle;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    public Uri getEndpoint() {
        return this.mEndpoint;
    }

    public int getState() {
        return this.mState;
    }
}
