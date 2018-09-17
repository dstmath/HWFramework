package java.security;

public class InvalidParameterException extends IllegalArgumentException {
    private static final long serialVersionUID = -857968536935667808L;

    public InvalidParameterException(String msg) {
        super(msg);
    }
}
