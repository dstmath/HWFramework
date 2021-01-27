package ohos.updatemachine;

public interface IUpdateMachineCallback {
    void onCompleteUpdate(UpdateMachineUpdateError updateMachineUpdateError);

    void onStatusUpdate(UpdateMachineUpdateStatus updateMachineUpdateStatus, float f);
}
