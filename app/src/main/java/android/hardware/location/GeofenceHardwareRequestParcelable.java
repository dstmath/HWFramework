package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class GeofenceHardwareRequestParcelable implements Parcelable {
    public static final Creator<GeofenceHardwareRequestParcelable> CREATOR = null;
    private int mId;
    private GeofenceHardwareRequest mRequest;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.location.GeofenceHardwareRequestParcelable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.location.GeofenceHardwareRequestParcelable.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardwareRequestParcelable.<clinit>():void");
    }

    public GeofenceHardwareRequestParcelable(int id, GeofenceHardwareRequest request) {
        this.mId = id;
        this.mRequest = request;
    }

    public int getId() {
        return this.mId;
    }

    public double getLatitude() {
        return this.mRequest.getLatitude();
    }

    public double getLongitude() {
        return this.mRequest.getLongitude();
    }

    public double getRadius() {
        return this.mRequest.getRadius();
    }

    public int getMonitorTransitions() {
        return this.mRequest.getMonitorTransitions();
    }

    public int getUnknownTimer() {
        return this.mRequest.getUnknownTimer();
    }

    public int getNotificationResponsiveness() {
        return this.mRequest.getNotificationResponsiveness();
    }

    public int getLastTransition() {
        return this.mRequest.getLastTransition();
    }

    int getType() {
        return this.mRequest.getType();
    }

    int getSourceTechnologies() {
        return this.mRequest.getSourceTechnologies();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("id=");
        builder.append(this.mId);
        builder.append(", type=");
        builder.append(this.mRequest.getType());
        builder.append(", latitude=");
        builder.append(this.mRequest.getLatitude());
        builder.append(", longitude=");
        builder.append(this.mRequest.getLongitude());
        builder.append(", radius=");
        builder.append(this.mRequest.getRadius());
        builder.append(", lastTransition=");
        builder.append(this.mRequest.getLastTransition());
        builder.append(", unknownTimer=");
        builder.append(this.mRequest.getUnknownTimer());
        builder.append(", monitorTransitions=");
        builder.append(this.mRequest.getMonitorTransitions());
        builder.append(", notificationResponsiveness=");
        builder.append(this.mRequest.getNotificationResponsiveness());
        builder.append(", sourceTechnologies=");
        builder.append(this.mRequest.getSourceTechnologies());
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(getType());
        parcel.writeDouble(getLatitude());
        parcel.writeDouble(getLongitude());
        parcel.writeDouble(getRadius());
        parcel.writeInt(getLastTransition());
        parcel.writeInt(getMonitorTransitions());
        parcel.writeInt(getUnknownTimer());
        parcel.writeInt(getNotificationResponsiveness());
        parcel.writeInt(getSourceTechnologies());
        parcel.writeInt(getId());
    }
}
