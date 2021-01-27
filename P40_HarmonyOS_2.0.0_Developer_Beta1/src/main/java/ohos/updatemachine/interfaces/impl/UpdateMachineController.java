package ohos.updatemachine.interfaces.impl;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.updatemachine.IUpdateMachineCallback;
import ohos.updatemachine.UpdateMachineException;
import ohos.updatemachine.adapter.UpdateMachineAdapter;
import ohos.updatemachine.interfaces.IUpdateMachineController;

public class UpdateMachineController implements IUpdateMachineController {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, UpdateMachineController.class.getSimpleName());
    private static UpdateMachineAdapter adapter = null;
    private static IUpdateMachineController instance;

    private UpdateMachineController() {
        HiLog.info(TAG, "UpdateMachineController constructor.", new Object[0]);
    }

    public static IUpdateMachineController getInstance() {
        IUpdateMachineController iUpdateMachineController;
        HiLog.info(TAG, "UpdateMachineController getInstance.", new Object[0]);
        synchronized (UpdateMachineController.class) {
            if (instance == null) {
                instance = new UpdateMachineController();
                adapter = UpdateMachineAdapter.getInstance();
            }
            iUpdateMachineController = instance;
        }
        return iUpdateMachineController;
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public void installPackageController(String str, long j, long j2, String[] strArr) throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController installPackageController begin.", new Object[0]);
        adapter.installPackageAdapter(str, j, j2, strArr);
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public boolean bindController(IUpdateMachineCallback iUpdateMachineCallback) throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController bindController begin.", new Object[0]);
        return adapter.bindAdapter(iUpdateMachineCallback);
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public boolean unbindController(IUpdateMachineCallback iUpdateMachineCallback) throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController unbindController begin.", new Object[0]);
        return adapter.unbindAdapter(iUpdateMachineCallback);
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public void suspendController() throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController suspendController begin.", new Object[0]);
        adapter.suspendAdapter();
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public void resumeController() throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController resumeController begin.", new Object[0]);
        adapter.resumeAdapter();
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public void cancelController() throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController cancelController begin.", new Object[0]);
        adapter.cancelAdapter();
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public int getProgressController() throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController getProgressController begin.", new Object[0]);
        return adapter.getProgressAdapter();
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public int getStatusController() throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController getStatusController begin.", new Object[0]);
        return adapter.getStatusAdapter();
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public void resetStatusController() throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController resetStatusController begin.", new Object[0]);
        adapter.resetStatusAdapter();
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public boolean setSlotController() throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController setSlotController begin.", new Object[0]);
        return adapter.setSlotAdapter();
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public boolean setCurrentSlotController() throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController setCurrentSlotController begin.", new Object[0]);
        return adapter.setCurrentSlotAdapter();
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public boolean verifyPayloadApplicableController(String str) throws UpdateMachineException {
        HiLog.info(TAG, "UpdateMachineController verifyPayloadApplicableController begin.", new Object[0]);
        return adapter.verifyPayloadApplicableAdapter(str);
    }

    @Override // ohos.updatemachine.interfaces.IUpdateMachineController
    public void closeController() {
        HiLog.info(TAG, "UpdateMachineController closeController begin.", new Object[0]);
        adapter.closeAdapter();
    }
}
