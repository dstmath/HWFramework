package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class BatchedScanResult implements Parcelable {
    public static final Creator<BatchedScanResult> CREATOR = new Creator<BatchedScanResult>() {
        public BatchedScanResult createFromParcel(Parcel in) {
            boolean z = true;
            BatchedScanResult result = new BatchedScanResult();
            if (in.readInt() != 1) {
                z = false;
            }
            result.truncated = z;
            int count = in.readInt();
            while (true) {
                int count2 = count;
                count = count2 - 1;
                if (count2 <= 0) {
                    return result;
                }
                result.scanResults.add((ScanResult) ScanResult.CREATOR.createFromParcel(in));
            }
        }

        public BatchedScanResult[] newArray(int size) {
            return new BatchedScanResult[size];
        }
    };
    private static final String TAG = "BatchedScanResult";
    public final List<ScanResult> scanResults = new ArrayList();
    public boolean truncated;

    public BatchedScanResult(BatchedScanResult source) {
        this.truncated = source.truncated;
        for (ScanResult s : source.scanResults) {
            this.scanResults.add(new ScanResult(s));
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BatchedScanResult: ").append("truncated: ").append(String.valueOf(this.truncated)).append("scanResults: [");
        for (ScanResult s : this.scanResults) {
            sb.append(" <").append(s.toString()).append("> ");
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
