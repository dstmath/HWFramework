package java.lang;

public class TypeNotPresentException extends RuntimeException {
    private static final long serialVersionUID = -5101214195716534496L;
    private String typeName;

    public TypeNotPresentException(String typeName, Throwable cause) {
        super("Type " + typeName + " not present", cause);
        this.typeName = typeName;
    }

    public String typeName() {
        return this.typeName;
    }
}
