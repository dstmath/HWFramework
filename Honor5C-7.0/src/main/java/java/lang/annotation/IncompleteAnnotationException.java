package java.lang.annotation;

public class IncompleteAnnotationException extends RuntimeException {
    private static final long serialVersionUID = 8445097402741811912L;
    private Class annotationType;
    private String elementName;

    public IncompleteAnnotationException(Class<? extends Annotation> annotationType, String elementName) {
        super(annotationType.getName() + " missing element " + elementName);
        this.annotationType = annotationType;
        this.elementName = elementName;
    }

    public Class<? extends Annotation> annotationType() {
        return this.annotationType;
    }

    public String elementName() {
        return this.elementName;
    }
}
