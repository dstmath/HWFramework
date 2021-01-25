package ohos.updatemachine.adapter;

import android.os.IUpdateEngine;
import android.os.IUpdateEngineCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.light.bean.LightEffect;
import ohos.startup.utils.StartUpStringUtil;
import ohos.system.Parameters;
import ohos.updatemachine.IUpdateMachineCallback;
import ohos.updatemachine.UpdateMachineException;
import ohos.updatemachine.UpdateMachineUpdateError;
import ohos.updatemachine.UpdateMachineUpdateStatus;

public class UpdateMachineAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, UpdateMachineAdapter.class.getSimpleName());
    private static final int UPDATE_MACHINE_BIND_TIME_SPAN = 200;
    private static final int UPDATE_MACHINE_CLOSE_RETRY_TIMES = 5;
    private static final String UPDATE_MACHINE_PROP_KEY_SWITCH = "hwouc.update_engine.up";
    private static final String UPDATE_MACHINE_PROP_KEY_TIMES = "hwouc.update_engine.times";
    private static final String UPDATE_MACHINE_PROP_VALUE_SWITCH = "true";
    private static final int UPDATE_MACHINE_RETRY_TIMES = 10;
    private static final String UPDATE_MACHINE_SERVICE = "android.os.UpdateEngineService";
    private static UpdateMachineAdapter adapterInstance;
    private Map<IUpdateMachineCallback, IUpdateEngineCallback> callbackMap = new ConcurrentHashMap();
    private IUpdateEngine updateMachineService = null;

    private UpdateMachineAdapter() {
        StartUpStringUtil.printDebug(TAG, "UpdateMachineAdapter Construction.");
        try {
            getUpdateMachineService();
        } catch (UpdateMachineException unused) {
            HiLog.debug(TAG, "UpdateMachineAdapter getUpdateMachineService in getInstance is null.", new Object[0]);
        }
    }

    public static UpdateMachineAdapter getInstance() {
        synchronized (UpdateMachineAdapter.class) {
            if (adapterInstance != null) {
                return adapterInstance;
            }
            adapterInstance = new UpdateMachineAdapter();
            return adapterInstance;
        }
    }

    private void getUpdateMachineService() throws UpdateMachineException {
        if (this.updateMachineService != null) {
            HiLog.debug(TAG, "UpdateMachineAdapter getUpdateMachineService success by memory variable.", new Object[0]);
            return;
        }
        int updateMachineProp = getUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_TIMES);
        if (updateMachineProp == 0) {
            waitUpdateMachineServiceClose();
        }
        increaseUpdateMachineTimes(updateMachineProp);
        this.updateMachineService = IUpdateEngine.Stub.asInterface(ServiceManager.getService(UPDATE_MACHINE_SERVICE));
        if (this.updateMachineService != null) {
            HiLog.warn(TAG, "UpdateMachineAdapter getUpdateMachineService success by stub.", new Object[0]);
            return;
        }
        HiLog.info(TAG, "initUpdateMachine is null, init it", new Object[0]);
        if (!startUpdateMachineService()) {
            HiLog.error(TAG, "UpdateMachineAdapter getUpdateMachineService is null.", new Object[0]);
            decreaseUpdateMachineTimes(updateMachineProp);
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public void installPackageAdapter(String str, long j, long j2, String[] strArr) throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter installPackageAdapter begin.", new Object[0]);
            this.updateMachineService.applyPayload(str, j, j2, strArr);
            HiLog.debug(TAG, "UpdateMachineAdapter installPackageAdapter end.", new Object[0]);
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter installPackageAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter installPackageAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public boolean bindAdapter(final IUpdateMachineCallback iUpdateMachineCallback) throws UpdateMachineException {
        getUpdateMachineService();
        HiLog.debug(TAG, "UpdateMachineAdapter bindAdapter begin.", new Object[0]);
        if (this.callbackMap.get(iUpdateMachineCallback) != null) {
            HiLog.debug(TAG, "UpdateMachineAdapter bindAdapter use before.", new Object[0]);
            return true;
        }
        IUpdateEngineCallback r0 = new IUpdateEngineCallback.Stub() {
            /* class ohos.updatemachine.adapter.UpdateMachineAdapter.AnonymousClass1 */

            public void onStatusUpdate(int i, float f) {
                HiLog.debug(UpdateMachineAdapter.TAG, "UpdateMachineAdapter bindAdapter callbackAdapter onStatusUpdate: status=%{private}d percent=%{private}d", Integer.valueOf(i), Float.valueOf(f));
                iUpdateMachineCallback.onStatusUpdate(UpdateMachineUpdateStatus.fromStatusCode(i), f);
            }

            public void onPayloadApplicationComplete(int i) {
                HiLog.debug(UpdateMachineAdapter.TAG, "UpdateMachineAdapter bindAdapter callbackAdapter onCompleteUpdate:errorCode=%{private}d", Integer.valueOf(i));
                iUpdateMachineCallback.onCompleteUpdate(UpdateMachineUpdateError.fromErrorCode(i));
            }
        };
        try {
            boolean bind = this.updateMachineService.bind(r0);
            this.callbackMap.put(iUpdateMachineCallback, r0);
            HiLog.debug(TAG, "UpdateMachineAdapter bindAdapter end. bind=%{public}s", Boolean.valueOf(bind));
            return bind;
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter bindAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter bindAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public boolean unbindAdapter(IUpdateMachineCallback iUpdateMachineCallback) throws UpdateMachineException {
        getUpdateMachineService();
        HiLog.debug(TAG, "UpdateMachineAdapter unbindAdapter begin.", new Object[0]);
        if (this.callbackMap.get(iUpdateMachineCallback) == null) {
            HiLog.debug(TAG, "UpdateMachineAdapter unbindAdapter not use before.", new Object[0]);
            return true;
        }
        try {
            boolean unbind = this.updateMachineService.unbind(this.callbackMap.get(iUpdateMachineCallback));
            this.callbackMap.remove(iUpdateMachineCallback);
            HiLog.debug(TAG, "UpdateMachineAdapter unbindAdapter end. unbind=%{public}s", Boolean.valueOf(unbind));
            return unbind;
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter unbindAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter unbindAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public void suspendAdapter() throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter suspendAdapter begin.", new Object[0]);
            this.updateMachineService.suspend();
            HiLog.debug(TAG, "UpdateMachineAdapter suspendAdapter end.", new Object[0]);
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter suspendAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter suspendAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public void resumeAdapter() throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter resumeAdapter begin.", new Object[0]);
            this.updateMachineService.resume();
            HiLog.debug(TAG, "UpdateMachineAdapter resumeAdapter end.", new Object[0]);
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter resumeAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter resumeAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public void cancelAdapter() throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter cancelAdapter begin.", new Object[0]);
            this.updateMachineService.cancel();
            HiLog.debug(TAG, "UpdateMachineAdapter cancelAdapter end.", new Object[0]);
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter cancelAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter cancelAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public int getProgressAdapter() throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter getProgressAdapter begin.", new Object[0]);
            int progress = this.updateMachineService.getProgress();
            HiLog.debug(TAG, "UpdateMachineAdapter getProgressAdapter end. getProgress=%{public}d", Integer.valueOf(progress));
            return progress;
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter getProgressAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter getProgressAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public int getStatusAdapter() throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter getStatusAdapter begin.", new Object[0]);
            int status = this.updateMachineService.getStatus();
            HiLog.debug(TAG, "UpdateMachineAdapter getStatusAdapter end. getStatus=%{public}d", Integer.valueOf(status));
            return status;
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter getStatusAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter getStatusAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public void resetStatusAdapter() throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter resetStatusAdapter begin.", new Object[0]);
            this.updateMachineService.resetStatus();
            HiLog.debug(TAG, "UpdateMachineAdapter resetStatusAdapter end.", new Object[0]);
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter resetStatusAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter resetStatusAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public boolean setSlotAdapter() throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter setSlotAdapter begin.", new Object[0]);
            boolean slot = this.updateMachineService.setSlot();
            HiLog.debug(TAG, "UpdateMachineAdapter setSlotAdapter end. setSlot=%{public}s", Boolean.valueOf(slot));
            return slot;
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter setSlotAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter setSlotAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    public boolean verifyPayloadApplicableAdapter(String str) throws UpdateMachineException {
        getUpdateMachineService();
        try {
            HiLog.debug(TAG, "UpdateMachineAdapter verifyPayloadApplicableAdapter begin.", new Object[0]);
            boolean verifyPayloadApplicable = this.updateMachineService.verifyPayloadApplicable(str);
            HiLog.debug(TAG, "UpdateMachineAdapter verifyPayloadApplicableAdapter end. verifyPayloadApplicable=%{public}s", Boolean.valueOf(verifyPayloadApplicable));
            return verifyPayloadApplicable;
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e, "UpdateMachineAdapter verifyPayloadApplicableAdapter:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_REMOTE_EXCEPTION);
        } catch (ServiceSpecificException e2) {
            StartUpStringUtil.printException(TAG, e2, "UpdateMachineAdapter verifyPayloadApplicableAdapter ServiceSpecificException:");
            throw new UpdateMachineException(UpdateMachineException.UpdateMachineExceptionType.UPDATE_MACHINE_SERVICE_NOT_EXIST);
        }
    }

    private int getUpdateMachineProp(String str) {
        HiLog.debug(TAG, "getUpdateMachineProp->%{private}s", str);
        String str2 = Parameters.get(str, LightEffect.LIGHT_ID_LED);
        HiLog.debug(TAG, "getUpdateMachineProp->%{private}s value->%{public}s", str, str2);
        try {
            return Integer.parseInt(str2);
        } catch (NumberFormatException unused) {
            HiLog.error(TAG, "NumberFormatException getUpdateMachineProp->%{private}s value->%{public}s", str, str2);
            return 0;
        }
    }

    private void increaseUpdateMachineTimes(int i) {
        HiLog.info(TAG, "increaseUpdateMachineTimes->times=%{public}d", Integer.valueOf(i));
        if (i >= 0) {
            setUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_TIMES, String.valueOf(i + 1));
        } else {
            setUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_TIMES, String.valueOf(1));
        }
    }

    private void decreaseUpdateMachineTimes(int i) {
        HiLog.info(TAG, "decreaseUpdateMachineTimes->times=%{public}d", Integer.valueOf(i));
        if (i > 0) {
            setUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_TIMES, String.valueOf(i - 1));
        }
    }

    private void setUpdateMachineProp(String str, String str2) {
        HiLog.debug(TAG, "setUpdateMachineProp->%{private}s value->%{public}s", str, str2);
        Parameters.set(str, str2);
    }

    private void waitUpdateMachineServiceClose() {
        long j = 0;
        while (j < 5) {
            j++;
            try {
                Thread.sleep(200 * j);
            } catch (InterruptedException unused) {
                HiLog.error(TAG, "UpdateMachineAdapter waitUpdateMachineServiceClose sleep interrupted.", new Object[0]);
            }
            this.updateMachineService = IUpdateEngine.Stub.asInterface(ServiceManager.getService(UPDATE_MACHINE_SERVICE));
            if (this.updateMachineService == null) {
                HiLog.info(TAG, "UpdateMachineAdapter waitUpdateMachineServiceClose UpdateMachine is close", new Object[0]);
                return;
            }
        }
    }

    private boolean startUpdateMachineService() {
        long j = 0;
        while (j < 10) {
            j++;
            HiLog.warn(TAG, "UpdateMachineAdapter startUpdateMachineService retry:%{public}d", Long.valueOf(j));
            setUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_SWITCH, "true");
            try {
                Thread.sleep(200 * j);
            } catch (InterruptedException unused) {
                HiLog.error(TAG, "UpdateMachineAdapter startUpdateMachineService sleep interrupted.", new Object[0]);
            }
            this.updateMachineService = IUpdateEngine.Stub.asInterface(ServiceManager.getService(UPDATE_MACHINE_SERVICE));
            if (this.updateMachineService != null) {
                HiLog.warn(TAG, "UpdateMachineAdapter startUpdateMachineService success by set Parameters.", new Object[0]);
                return true;
            }
        }
        return false;
    }

    public void closeAdapter() {
        if (this.updateMachineService != null) {
            closeUpdateMachine(getUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_TIMES));
        }
    }

    private void closeUpdateMachine(int i) {
        HiLog.info(TAG, "closeUpdateMachine->%{public}d", Integer.valueOf(i));
        this.updateMachineService = null;
        int i2 = i - 1;
        if (i2 > 0) {
            HiLog.info(TAG, "closeUpdateMachine->restTimes=%{public}d, set update machine times - 1.", Integer.valueOf(i2));
            setUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_TIMES, String.valueOf(i2));
            return;
        }
        HiLog.info(TAG, "closeUpdateMachine->restTimes=%{public}d, setUpdateMachineProp false.", Integer.valueOf(i2));
        setUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_TIMES, String.valueOf(0));
        setUpdateMachineProp(UPDATE_MACHINE_PROP_KEY_SWITCH, "false");
        waitUpdateMachineServiceClose();
    }
}
