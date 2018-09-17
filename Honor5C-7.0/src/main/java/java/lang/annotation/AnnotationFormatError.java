package java.lang.annotation;

public class AnnotationFormatError extends Error {
    private static final long serialVersionUID = -4256701562333669892L;

    public AnnotationFormatError(String message) {
        super(message);
    }

    public AnnotationFormatError(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationFormatError(Throwable cause) {
        super(cause);
    }
}
