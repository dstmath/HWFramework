package java.lang;

public class StackOverflowError extends VirtualMachineError {
    private static final long serialVersionUID = 8609175038441759607L;

    public StackOverflowError(String s) {
        super(s);
    }
}
