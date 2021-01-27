package ohos.utils;

public class ParcelException extends RuntimeException {
    public static final String NO_CAPACITY_ERROR = "Insufficient capacity space";
    private static final long serialVersionUID = 2344160447914030868L;

    public ParcelException(String str) {
        super(str);
    }
}
