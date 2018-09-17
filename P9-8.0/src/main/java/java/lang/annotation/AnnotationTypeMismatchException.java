package java.lang.annotation;

import java.lang.reflect.Method;

public class AnnotationTypeMismatchException extends RuntimeException {
    private static final long serialVersionUID = 8125925355765570191L;
    private final Method element;
    private final String foundType;

    public AnnotationTypeMismatchException(Method element, String foundType) {
        super("Incorrectly typed data found for annotation element " + element + " (Found data of type " + foundType + ")");
        this.element = element;
        this.foundType = foundType;
    }

    public Method element() {
        return this.element;
    }

    public String foundType() {
        return this.foundType;
    }
}
