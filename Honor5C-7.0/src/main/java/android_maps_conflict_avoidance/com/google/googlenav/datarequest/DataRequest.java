package android_maps_conflict_avoidance.com.google.googlenav.datarequest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface DataRequest {
    int getRequestType();

    boolean isCancelled();

    boolean isForeground();

    boolean isImmediate();

    boolean isSubmission();

    void onServerFailure();

    boolean readResponseData(DataInput dataInput) throws IOException;

    boolean retryOnFailure();

    void writeRequestData(DataOutput dataOutput) throws IOException;
}
