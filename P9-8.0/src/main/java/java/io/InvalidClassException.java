package java.io;

public class InvalidClassException extends ObjectStreamException {
    private static final long serialVersionUID = -4333316296251054416L;
    public String classname;

    public InvalidClassException(String reason) {
        super(reason);
    }

    public InvalidClassException(String cname, String reason) {
        super(reason);
        this.classname = cname;
    }

    public String getMessage() {
        if (this.classname == null) {
            return super.getMessage();
        }
        return this.classname + "; " + super.getMessage();
    }
}
