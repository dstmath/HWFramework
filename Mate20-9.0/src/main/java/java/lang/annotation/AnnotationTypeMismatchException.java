package java.lang.annotation;

import java.lang.reflect.Method;

public class AnnotationTypeMismatchException extends RuntimeException {
    private static final long serialVersionUID = 8125925355765570191L;
    private final Method element;
    private final String foundType;

    public AnnotationTypeMismatchException(Method element2, String foundType2) {
        super("Incorrectly typed data found for annotation element " + element2 + " (Found data of type " + foundType2 + ")");
        this.element = element2;
        this.foundType = foundType2;
    }

    public Method element() {
        return this.element;
    }

    public String foundType() {
        return this.foundType;
    }
}
