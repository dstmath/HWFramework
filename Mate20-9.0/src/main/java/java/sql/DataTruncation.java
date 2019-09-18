package java.sql;

public class DataTruncation extends SQLWarning {
    private static final long serialVersionUID = 6464298989504059473L;
    private int dataSize;
    private int index;
    private boolean parameter;
    private boolean read;
    private int transferSize;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DataTruncation(int index2, boolean parameter2, boolean read2, int dataSize2, int transferSize2) {
        super("Data truncation", read2 ? "01004" : "22001");
        this.index = index2;
        this.parameter = parameter2;
        this.read = read2;
        this.dataSize = dataSize2;
        this.transferSize = transferSize2;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DataTruncation(int index2, boolean parameter2, boolean read2, int dataSize2, int transferSize2, Throwable cause) {
        super("Data truncation", read2 ? "01004" : "22001", cause);
        this.index = index2;
        this.parameter = parameter2;
        this.read = read2;
        this.dataSize = dataSize2;
        this.transferSize = transferSize2;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean getParameter() {
        return this.parameter;
    }

    public boolean getRead() {
        return this.read;
    }

    public int getDataSize() {
        return this.dataSize;
    }

    public int getTransferSize() {
        return this.transferSize;
    }
}
