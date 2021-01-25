package ohos.data.rdb;

public class RdbReadOnlyException extends RdbException {
    private static final long serialVersionUID = 5689098903660941797L;

    public RdbReadOnlyException() {
    }

    public RdbReadOnlyException(String str) {
        super(str);
    }
}
