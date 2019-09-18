package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ValidationProbeEvent implements Parcelable {
    public static final Parcelable.Creator<ValidationProbeEvent> CREATOR = new Parcelable.Creator<ValidationProbeEvent>() {
        public ValidationProbeEvent createFromParcel(Parcel in) {
            return new ValidationProbeEvent(in);
        }

        public ValidationProbeEvent[] newArray(int size) {
            return new ValidationProbeEvent[size];
        }
    };
    public static final int DNS_FAILURE = 0;
    public static final int DNS_SUCCESS = 1;
    private static final int FIRST_VALIDATION = 256;
    public static final int PROBE_DNS = 0;
    public static final int PROBE_FALLBACK = 4;
    public static final int PROBE_HTTP = 1;
    public static final int PROBE_HTTPS = 2;
    public static final int PROBE_PAC = 3;
    private static final int REVALIDATION = 512;
    public long durationMs;
    public int probeType;
    public int returnCode;

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{ValidationProbeEvent.class}, new String[]{"PROBE_", "FIRST_", "REVALIDATION"});

        Decoder() {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ReturnCode {
    }

    public ValidationProbeEvent() {
    }

    private ValidationProbeEvent(Parcel in) {
        this.durationMs = in.readLong();
        this.probeType = in.readInt();
        this.returnCode = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.durationMs);
        out.writeInt(this.probeType);
        out.writeInt(this.returnCode);
    }

    public int describeContents() {
        return 0;
    }

    public static int makeProbeType(int probeType2, boolean firstValidation) {
        return (probeType2 & 255) | (firstValidation ? 256 : 512);
    }

    public static String getProbeName(int probeType2) {
        return Decoder.constants.get(probeType2 & 255, "PROBE_???");
    }

    public static String getValidationStage(int probeType2) {
        return Decoder.constants.get(65280 & probeType2, "UNKNOWN");
    }

    public String toString() {
        return String.format("ValidationProbeEvent(%s:%d %s, %dms)", new Object[]{getProbeName(this.probeType), Integer.valueOf(this.returnCode), getValidationStage(this.probeType), Long.valueOf(this.durationMs)});
    }
}
