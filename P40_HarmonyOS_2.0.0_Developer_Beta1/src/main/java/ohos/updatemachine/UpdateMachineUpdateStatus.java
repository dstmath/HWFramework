package ohos.updatemachine;

public enum UpdateMachineUpdateStatus {
    IDLE(0),
    CHECKING_FOR_UPDATE(1),
    UPDATE_AVAILABLE(2),
    DOWNLOADING(3),
    VERIFYING(4),
    FINALIZING(5),
    UPDATED_NEED_REBOOT(6),
    REPORTING_ERROR_EVENT(7),
    ATTEMPTING_ROLLBACK(8),
    DISABLED(9);
    
    private int statusCode;

    private UpdateMachineUpdateStatus(int i) {
        this.statusCode = i;
    }

    public static UpdateMachineUpdateStatus fromStatusCode(int i) {
        UpdateMachineUpdateStatus[] values = values();
        for (UpdateMachineUpdateStatus updateMachineUpdateStatus : values) {
            if (i == updateMachineUpdateStatus.getStatusCode()) {
                return updateMachineUpdateStatus;
            }
        }
        return DISABLED;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
