package ohos.updatemachine.innerkits;

import java.io.IOException;
import ohos.updatemachine.IUpdateMachineCallback;
import ohos.updatemachine.UpdateMachineException;
import ohos.updatemachine.interfaces.IUpdateMachineController;
import ohos.updatemachine.interfaces.impl.UpdateMachineController;

public final class UpdateMachineInnerKit implements AutoCloseable {
    private static IUpdateMachineController controller = UpdateMachineController.getInstance();

    public void installPackage(String str, long j, long j2, String[] strArr) throws UpdateMachineException {
        controller.installPackageController(str, j, j2, strArr);
    }

    public boolean bind(IUpdateMachineCallback iUpdateMachineCallback) throws UpdateMachineException {
        if (iUpdateMachineCallback == null) {
            return false;
        }
        return controller.bindController(iUpdateMachineCallback);
    }

    public boolean unbind(IUpdateMachineCallback iUpdateMachineCallback) throws UpdateMachineException {
        if (iUpdateMachineCallback == null) {
            return false;
        }
        return controller.unbindController(iUpdateMachineCallback);
    }

    public void suspend() throws UpdateMachineException {
        controller.suspendController();
    }

    public void resume() throws UpdateMachineException {
        controller.resumeController();
    }

    public void cancel() throws UpdateMachineException {
        controller.cancelController();
    }

    public int getProgress() throws UpdateMachineException {
        return controller.getProgressController();
    }

    public int getStatus() throws UpdateMachineException {
        return controller.getStatusController();
    }

    public void resetStatus() throws UpdateMachineException {
        controller.resetStatusController();
    }

    public boolean setSlot() throws UpdateMachineException {
        return controller.setSlotController();
    }

    public boolean verifyPayloadApplicable(String str) throws UpdateMachineException {
        return controller.verifyPayloadApplicableController(str);
    }

    @Override // java.lang.AutoCloseable
    public void close() throws IOException {
        controller.closeController();
    }
}
