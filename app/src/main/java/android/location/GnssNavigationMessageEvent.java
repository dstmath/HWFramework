package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.InvalidParameterException;

public final class GnssNavigationMessageEvent implements Parcelable {
    public static final Creator<GnssNavigationMessageEvent> CREATOR = null;
    public static final int STATUS_GNSS_LOCATION_DISABLED = 2;
    public static final int STATUS_NOT_SUPPORTED = 0;
    public static final int STATUS_READY = 1;
    private final GnssNavigationMessage mNavigationMessage;

    public static abstract class Callback {
        public void onGnssNavigationMessageReceived(GnssNavigationMessageEvent event) {
        }

        public void onStatusChanged(int status) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GnssNavigationMessageEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.GnssNavigationMessageEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.GnssNavigationMessageEvent.<clinit>():void");
    }

    public GnssNavigationMessageEvent(GnssNavigationMessage message) {
        if (message == null) {
            throw new InvalidParameterException("Parameter 'message' must not be null.");
        }
        this.mNavigationMessage = message;
    }

    public GnssNavigationMessage getNavigationMessage() {
        return this.mNavigationMessage;
    }

    public int describeContents() {
        return STATUS_NOT_SUPPORTED;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mNavigationMessage, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ GnssNavigationMessageEvent:\n\n");
        builder.append(this.mNavigationMessage.toString());
        builder.append("\n]");
        return builder.toString();
    }
}
