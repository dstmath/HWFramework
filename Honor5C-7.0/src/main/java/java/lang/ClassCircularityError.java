package java.lang;

public class ClassCircularityError extends LinkageError {
    private static final long serialVersionUID = 1054362542914539689L;

    public ClassCircularityError(String s) {
        super(s);
    }
}
