package android.net.wifi;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

@SystemApi
@Deprecated
public class BatchedScanResult implements Parcelable {
    public static final Parcelable.Creator<BatchedScanResult> CREATOR = new Parcelable.Creator<BatchedScanResult>() {
        public BatchedScanResult createFromParcel(Parcel in) {
            BatchedScanResult result = new BatchedScanResult();
            boolean z = true;
            if (in.readInt() != 1) {
                z = false;
            }
            result.truncated = z;
            int count = in.readInt();
            while (true) {
                int count2 = count - 1;
                if (count <= 0) {
                    return result;
                }
                result.scanResults.add(ScanResult.CREATOR.createFromParcel(in));
                count = count2;
            }
        }

        public BatchedScanResult[] newArray(int size) {
            return new BatchedScanResult[size];
        }
    };
    private static final String TAG = "BatchedScanResult";
    public final List<ScanResult> scanResults = new ArrayList();
    public boolean truncated;

    public BatchedScanResult() {
    }

    public BatchedScanResult(BatchedScanResult source) {
        this.truncated = source.truncated;
        for (ScanResult s : source.scanResults) {
            this.scanResults.add(new ScanResult(s));
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BatchedScanResult: ");
        sb.append("truncated: ");
        sb.append(String.valueOf(this.truncated));
        sb.append("scanResults: [");
        for (ScanResult s : this.scanResults) {
            sb.append(" <");
            sb.append(s.toString());
            sb.append("> ");
        }
        sb.append(" ]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.truncated ? 1 : 0);
        dest.writeInt(this.scanResults.size());
        for (ScanResult s : this.scanResults) {
            s.writeToParcel(dest, flags);
        }
    }
}
