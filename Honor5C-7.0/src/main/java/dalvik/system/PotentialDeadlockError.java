package dalvik.system;

public class PotentialDeadlockError extends VirtualMachineError {
    public PotentialDeadlockError(String detailMessage) {
        super(detailMessage);
    }
}
