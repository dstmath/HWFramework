package ohos.updatemachine;

public class UpdateMachineException extends Exception {
    private static final long serialVersionUID = 4253592238121757117L;

    public enum UpdateMachineExceptionType {
        UPDATE_MACHINE_SERVICE_NOT_EXIST,
        UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION
    }

    public UpdateMachineException(UpdateMachineExceptionType updateMachineExceptionType) {
        super(updateMachineExceptionType.toString());
    }

    public UpdateMachineException(UpdateMachineExceptionType updateMachineExceptionType, String str) {
        super(updateMachineExceptionType.toString() + "(" + str + ")");
    }
}
