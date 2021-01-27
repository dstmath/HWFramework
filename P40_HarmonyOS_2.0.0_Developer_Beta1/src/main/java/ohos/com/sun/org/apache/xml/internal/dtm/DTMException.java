package ohos.com.sun.org.apache.xml.internal.dtm;

public class DTMException extends RuntimeException {
    static final long serialVersionUID = -775576419181334734L;

    public DTMException(String str) {
        super(str);
    }

    public DTMException(Throwable th) {
        super(th);
    }

    public DTMException(String str, Throwable th) {
        super(str, th);
    }
}
