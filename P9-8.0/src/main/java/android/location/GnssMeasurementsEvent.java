package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class GnssMeasurementsEvent implements Parcelable {
    public static final Creator<GnssMeasurementsEvent> CREATOR = new Creator<GnssMeasurementsEvent>() {
        public GnssMeasurementsEvent createFromParcel(Parcel in) {
            GnssClock clock = (GnssClock) in.readParcelable(getClass().getClassLoader());
            GnssMeasurement[] measurementsArray = new GnssMeasurement[in.readInt()];
            in.readTypedArray(measurementsArray, GnssMeasurement.CREATOR);
            return new GnssMeasurementsEvent(clock, measurementsArray);
        }

        public GnssMeasurementsEvent[] newArray(int size) {
            return new GnssMeasurementsEvent[size];
        }
    };
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

    public GnssMeasurementsEvent(GnssClock clock, GnssMeasurement[] measurements) {
        if (clock == null) {
            throw new InvalidParameterException("Parameter 'clock' must not be null.");
        }
        if (measurements == null || measurements.length == 0) {
            this.mReadOnlyMeasurements = Collections.emptyList();
        } else {
            this.mReadOnlyMeasurements = Collections.unmodifiableCollection(Arrays.asList(measurements));
        }
        this.mClock = clock;
    }

    public GnssClock getClock() {
        return this.mClock;
    }

    public Collection<GnssMeasurement> getMeasurements() {
        return this.mReadOnlyMeasurements;
    }

    public int describeContents() {
        return 0;
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
