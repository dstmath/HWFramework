package java.sql;

import java.util.Arrays;

public class BatchUpdateException extends SQLException {
    private static final long serialVersionUID = 5977529877145521757L;
    private final int[] updateCounts;

    public BatchUpdateException(String reason, String SQLState, int vendorCode, int[] updateCounts) {
        int[] iArr = null;
        super(reason, SQLState, vendorCode);
        if (updateCounts != null) {
            iArr = Arrays.copyOf(updateCounts, updateCounts.length);
        }
        this.updateCounts = iArr;
    }

    public BatchUpdateException(String reason, String SQLState, int[] updateCounts) {
        this(reason, SQLState, 0, updateCounts);
    }

    public BatchUpdateException(String reason, int[] updateCounts) {
        this(reason, null, 0, updateCounts);
    }

    public BatchUpdateException(int[] updateCounts) {
        this(null, null, 0, updateCounts);
    }

    public BatchUpdateException() {
        this(null, null, 0, null);
    }

    public BatchUpdateException(Throwable cause) {
        this(cause == null ? null : cause.toString(), null, 0, null, cause);
    }

    public BatchUpdateException(int[] updateCounts, Throwable cause) {
        this(cause == null ? null : cause.toString(), null, 0, updateCounts, cause);
    }

    public BatchUpdateException(String reason, int[] updateCounts, Throwable cause) {
        this(reason, null, 0, updateCounts, cause);
    }

    public BatchUpdateException(String reason, String SQLState, int[] updateCounts, Throwable cause) {
        this(reason, SQLState, 0, updateCounts, cause);
    }

    public BatchUpdateException(String reason, String SQLState, int vendorCode, int[] updateCounts, Throwable cause) {
        int[] iArr = null;
        super(reason, SQLState, vendorCode, cause);
        if (updateCounts != null) {
            iArr = Arrays.copyOf(updateCounts, updateCounts.length);
        }
        this.updateCounts = iArr;
    }

    public int[] getUpdateCounts() {
        return this.updateCounts == null ? null : Arrays.copyOf(this.updateCounts, this.updateCounts.length);
    }
}
