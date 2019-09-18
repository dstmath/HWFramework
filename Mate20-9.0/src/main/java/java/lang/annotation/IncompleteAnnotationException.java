package java.lang.annotation;

public class IncompleteAnnotationException extends RuntimeException {
    private static final long serialVersionUID = 8445097402741811912L;
    private Class<? extends Annotation> annotationType;
    private String elementName;

    public IncompleteAnnotationException(Class<? extends Annotation> annotationType2, String elementName2) {
        super(annotationType2.getName() + " missing element " + elementName2.toString());
        this.annotationType = annotationType2;
        this.elementName = elementName2;
    }

    public Class<? extends Annotation> annotationType() {
        return this.annotationType;
    }

    public String elementName() {
        return this.elementName;
    }
}
