package ohos.updatemachine.interfaces;

import ohos.updatemachine.IUpdateMachineCallback;
import ohos.updatemachine.UpdateMachineException;

public interface IUpdateMachineController {
    boolean bindController(IUpdateMachineCallback iUpdateMachineCallback) throws UpdateMachineException;

    void cancelController() throws UpdateMachineException;

    void closeController();

    int getProgressController() throws UpdateMachineException;

    int getStatusController() throws UpdateMachineException;

    void installPackageController(String str, long j, long j2, String[] strArr) throws UpdateMachineException;

    void resetStatusController() throws UpdateMachineException;

    void resumeController() throws UpdateMachineException;

    boolean setCurrentSlotController() throws UpdateMachineException;

    boolean setSlotController() throws UpdateMachineException;

    void suspendController() throws UpdateMachineException;

    boolean unbindController(IUpdateMachineCallback iUpdateMachineCallback) throws UpdateMachineException;

    boolean verifyPayloadApplicableController(String str) throws UpdateMachineException;
}
