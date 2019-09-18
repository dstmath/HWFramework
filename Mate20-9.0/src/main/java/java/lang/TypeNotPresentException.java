package java.lang;

public class TypeNotPresentException extends RuntimeException {
    private static final long serialVersionUID = -5101214195716534496L;
    private String typeName;

    public TypeNotPresentException(String typeName2, Throwable cause) {
        super("Type " + typeName2 + " not present", cause);
        this.typeName = typeName2;
    }

    public String typeName() {
        return this.typeName;
    }
}
