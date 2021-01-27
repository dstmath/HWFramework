package ohos.updatemachine;

public enum UpdateMachineUpdateError {
    SUCCESS(0),
    ERROR(1),
    FILESYSTEM_COPIER_ERROR(4),
    POST_INSTALL_RUNNER_ERROR(5),
    PAYLOAD_MISMATCHED_TYPE_ERROR(6),
    INSTALL_DEVICE_OPEN_ERROR(7),
    KERNEL_DEVICE_OPEN_ERROR(8),
    DOWNLOAD_TRANSFER_ERROR(9),
    PAYLOAD_HASH_MISMATCH_ERROR(10),
    PAYLOAD_SIZE_MISMATCH_ERROR(11),
    DOWNLOAD_PAYLOAD_VERIFICATION_ERROR(12),
    PAYLOAD_TIMESTAMP_ERROR(51),
    UPDATED_BUT_NOT_ACTIVE(52);
    
    private int errorCode;

    private UpdateMachineUpdateError(int i) {
        this.errorCode = i;
    }

    public static UpdateMachineUpdateError fromErrorCode(int i) {
        UpdateMachineUpdateError[] values = values();
        for (UpdateMachineUpdateError updateMachineUpdateError : values) {
            if (i == updateMachineUpdateError.getErrorCode()) {
                return updateMachineUpdateError;
            }
        }
        return ERROR;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
