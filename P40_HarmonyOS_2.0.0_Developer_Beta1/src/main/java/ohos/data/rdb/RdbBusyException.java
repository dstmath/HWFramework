package ohos.data.rdb;

public class RdbBusyException extends RdbException {
    private static final long serialVersionUID = -6667649187848433587L;

    public RdbBusyException() {
    }

    public RdbBusyException(String str) {
        super(str);
    }
}
