package ohos.com.sun.org.apache.xml.internal.resolver;

public class CatalogException extends Exception {
    public static final int INVALID_ENTRY = 2;
    public static final int INVALID_ENTRY_TYPE = 3;
    public static final int NO_XML_PARSER = 4;
    public static final int PARSE_FAILED = 7;
    public static final int UNENDED_COMMENT = 8;
    public static final int UNKNOWN_FORMAT = 5;
    public static final int UNPARSEABLE = 6;
    public static final int WRAPPER = 1;
    private Exception exception;
    private int exceptionType;

    public CatalogException(int i, String str) {
        super(str);
        this.exception = null;
        this.exceptionType = 0;
        this.exceptionType = i;
        this.exception = null;
    }

    public CatalogException(int i) {
        super("Catalog Exception " + i);
        this.exception = null;
        this.exceptionType = 0;
        this.exceptionType = i;
        this.exception = null;
    }

    public CatalogException(Exception exc) {
        this.exception = null;
        this.exceptionType = 0;
        this.exceptionType = 1;
        this.exception = exc;
    }

    public CatalogException(String str, Exception exc) {
        super(str);
        this.exception = null;
        this.exceptionType = 0;
        this.exceptionType = 1;
        this.exception = exc;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        Exception exc;
        String message = super.getMessage();
        return (message != null || (exc = this.exception) == null) ? message : exc.getMessage();
    }

    public Exception getException() {
        return this.exception;
    }

    public int getExceptionType() {
        return this.exceptionType;
    }

    @Override // java.lang.Throwable, java.lang.Object
    public String toString() {
        Exception exc = this.exception;
        if (exc != null) {
            return exc.toString();
        }
        return super.toString();
    }
}
