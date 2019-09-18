package java.sql;

import java.util.Arrays;

public class BatchUpdateException extends SQLException {
    private static final long serialVersionUID = 5977529877145521757L;
    private final int[] updateCounts;

    public BatchUpdateException(String reason, String SQLState, int vendorCode, int[] updateCounts2) {
        super(reason, SQLState, vendorCode);
        this.updateCounts = updateCounts2 == null ? null : Arrays.copyOf(updateCounts2, updateCounts2.length);
    }

    public BatchUpdateException(String reason, String SQLState, int[] updateCounts2) {
        this(reason, SQLState, 0, updateCounts2);
    }

    public BatchUpdateException(String reason, int[] updateCounts2) {
        this(reason, (String) null, 0, updateCounts2);
    }

    public BatchUpdateException(int[] updateCounts2) {
        this((String) null, (String) null, 0, updateCounts2);
    }

    public BatchUpdateException() {
        this((String) null, (String) null, 0, (int[]) null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public BatchUpdateException(Throwable cause) {
        this(cause == null ? null : cause.toString(), null, 0, null, cause);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public BatchUpdateException(int[] updateCounts2, Throwable cause) {
        this(cause == null ? null : cause.toString(), null, 0, updateCounts2, cause);
    }

    public BatchUpdateException(String reason, int[] updateCounts2, Throwable cause) {
        this(reason, null, 0, updateCounts2, cause);
    }

    public BatchUpdateException(String reason, String SQLState, int[] updateCounts2, Throwable cause) {
        this(reason, SQLState, 0, updateCounts2, cause);
    }

    public BatchUpdateException(String reason, String SQLState, int vendorCode, int[] updateCounts2, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
        this.updateCounts = updateCounts2 == null ? null : Arrays.copyOf(updateCounts2, updateCounts2.length);
    }

    public int[] getUpdateCounts() {
        if (this.updateCounts == null) {
            return null;
        }
        return Arrays.copyOf(this.updateCounts, this.updateCounts.length);
    }
}
