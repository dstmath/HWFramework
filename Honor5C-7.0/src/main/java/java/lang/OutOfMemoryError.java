package java.lang;

public class OutOfMemoryError extends VirtualMachineError {
    private static final long serialVersionUID = 8228564086184010517L;

    public OutOfMemoryError(String s) {
        super(s);
    }
}
