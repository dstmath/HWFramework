package java.sql;

public class DataTruncation extends SQLWarning {
    private static final long serialVersionUID = 6464298989504059473L;
    private int dataSize;
    private int index;
    private boolean parameter;
    private boolean read;
    private int transferSize;

    public DataTruncation(int index, boolean parameter, boolean read, int dataSize, int transferSize) {
        super("Data truncation", read ? "01004" : "22001");
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;
    }

    public DataTruncation(int index, boolean parameter, boolean read, int dataSize, int transferSize, Throwable cause) {
        super("Data truncation", read ? "01004" : "22001", cause);
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;
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
