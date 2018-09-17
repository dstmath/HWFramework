package java.lang;

public class UnknownError extends VirtualMachineError {
    private static final long serialVersionUID = 2524784860676771849L;

    public UnknownError(String s) {
        super(s);
    }
}
