package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.InvalidParameterException;

public class GpsNavigationMessageEvent implements Parcelable {
    public static final Creator<GpsNavigationMessageEvent> CREATOR = null;
    public static int STATUS_GPS_LOCATION_DISABLED;
    public static int STATUS_NOT_SUPPORTED;
    public static int STATUS_READY;
    private final GpsNavigationMessage mNavigationMessage;

    public interface Listener {
        void onGpsNavigationMessageReceived(GpsNavigationMessageEvent gpsNavigationMessageEvent);

        void onStatusChanged(int i);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GpsNavigationMessageEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.GpsNavigationMessageEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.GpsNavigationMessageEvent.<clinit>():void");
    }

    public GpsNavigationMessageEvent(GpsNavigationMessage message) {
        if (message == null) {
            throw new InvalidParameterException("Parameter 'message' must not be null.");
        }
        this.mNavigationMessage = message;
    }

    public GpsNavigationMessage getNavigationMessage() {
        return this.mNavigationMessage;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mNavigationMessage, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ GpsNavigationMessageEvent:\n\n");
        builder.append(this.mNavigationMessage.toString());
        builder.append("\n]");
        return builder.toString();
    }
}
