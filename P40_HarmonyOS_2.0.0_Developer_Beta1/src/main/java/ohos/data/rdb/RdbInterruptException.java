package ohos.data.rdb;

public class RdbInterruptException extends RdbException {
    private static final String EXCEPTION_INFO = "Operation Canceled";
    private static final long serialVersionUID = -7163889717766253452L;

    public RdbInterruptException() {
        super(EXCEPTION_INFO);
    }
}
