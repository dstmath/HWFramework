package ohos.data.rdb;

public class RdbNoMemException extends RdbException {
    private static final long serialVersionUID = -823610654348603288L;

    public RdbNoMemException() {
    }

    public RdbNoMemException(String str) {
        super(str);
    }
}
