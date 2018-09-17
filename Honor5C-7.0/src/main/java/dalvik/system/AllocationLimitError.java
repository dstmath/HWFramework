package dalvik.system;

public class AllocationLimitError extends VirtualMachineError {
    public AllocationLimitError(String detailMessage) {
        super(detailMessage);
    }
}
