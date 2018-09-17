package java.io;

import android.util.Log;
import android.util.LogException;

public class FilterFD extends FilterInputStream {
    private static final String TAG = "FilterFD";

    public FilterFD(FilterInputStream filter) {
        super(filter.in);
    }

    public FileDescriptor getFD() {
        if (this.in instanceof FileInputStream) {
            try {
                return this.in.getFD();
            } catch (Exception e) {
                Log.e(TAG, LogException.NO_VALUE, e);
            }
        } else {
            Log.e(TAG, "Can't convert input stream to FileInputStream!");
            return null;
        }
    }
}
