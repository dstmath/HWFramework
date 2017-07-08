package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class GnssMeasurementsEvent implements Parcelable {
    public static final Creator<GnssMeasurementsEvent> CREATOR = null;
    public static final int STATUS_GNSS_LOCATION_DISABLED = 2;
    public static final int STATUS_NOT_SUPPORTED = 0;
    public static final int STATUS_READY = 1;
    private final GnssClock mClock;
    private final Collection<GnssMeasurement> mReadOnlyMeasurements;

    public static abstract class Callback {
        public static final int STATUS_LOCATION_DISABLED = 2;
        public static final int STATUS_NOT_SUPPORTED = 0;
        public static final int STATUS_READY = 1;

        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
        }

        public void onStatusChanged(int status) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.GnssMeasurementsEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.GnssMeasurementsEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.GnssMeasurementsEvent.<clinit>():void");
    }

    public GnssMeasurementsEvent(GnssClock clock, GnssMeasurement[] measurements) {
        if (clock == null) {
            throw new InvalidParameterException("Parameter 'clock' must not be null.");
        } else if (measurements == null || measurements.length == 0) {
            throw new InvalidParameterException("Parameter 'measurements' must not be null or empty.");
        } else {
            this.mClock = clock;
            this.mReadOnlyMeasurements = Collections.unmodifiableCollection(Arrays.asList(measurements));
        }
    }

    public GnssClock getClock() {
        return this.mClock;
    }

    public Collection<GnssMeasurement> getMeasurements() {
        return this.mReadOnlyMeasurements;
    }

    public int describeContents() {
        return STATUS_NOT_SUPPORTED;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mClock, flags);
        GnssMeasurement[] measurementsArray = (GnssMeasurement[]) this.mReadOnlyMeasurements.toArray(new GnssMeasurement[this.mReadOnlyMeasurements.size()]);
        parcel.writeInt(measurementsArray.length);
        parcel.writeTypedArray(measurementsArray, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ GnssMeasurementsEvent:\n\n");
        builder.append(this.mClock.toString());
        builder.append("\n");
        for (GnssMeasurement measurement : this.mReadOnlyMeasurements) {
            builder.append(measurement.toString());
            builder.append("\n");
        }
        builder.append("]");
        return builder.toString();
    }
}
