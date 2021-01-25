package ohos.data.rdb;

public class RdbCantOpenException extends RdbException {
    private static final long serialVersionUID = 7960758166964120662L;

    public RdbCantOpenException() {
    }

    public RdbCantOpenException(String str) {
        super(str);
    }
}
