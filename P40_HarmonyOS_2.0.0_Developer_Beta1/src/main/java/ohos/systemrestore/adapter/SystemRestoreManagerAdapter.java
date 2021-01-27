package ohos.systemrestore.adapter;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.IRecoverySystem;
import android.os.IUserManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.startup.utils.StartUpStringUtil;
import ohos.systemrestore.SystemRestoreException;

public class SystemRestoreManagerAdapter {
    private static final int SEND_RESTOREUSERDATA_BROADCAST_TIMEOUT = 1000;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, SystemRestoreManagerAdapter.class.getSimpleName());
    private static SystemRestoreManagerAdapter adapterInstance;
    private static IRecoverySystem recoveryService;
    private static IUserManager userManagerService;

    private SystemRestoreManagerAdapter() {
        StartUpStringUtil.printDebug(TAG, "SystemRestoreManagerAdapter Construction.");
    }

    public static SystemRestoreManagerAdapter getInstance() {
        SystemRestoreManagerAdapter systemRestoreManagerAdapter;
        synchronized (SystemRestoreManagerAdapter.class) {
            if (adapterInstance == null) {
                try {
                    if (recoveryService == null) {
                        recoveryService = IRecoverySystem.Stub.asInterface(ServiceManager.getServiceOrThrow("recovery"));
                    }
                } catch (ServiceManager.ServiceNotFoundException e) {
                    recoveryService = null;
                    StartUpStringUtil.printException(TAG, e, "SystemRestoreManagerAdapter not found recoveryService.", false);
                }
                try {
                    if (userManagerService == null) {
                        userManagerService = IUserManager.Stub.asInterface(ServiceManager.getServiceOrThrow("user"));
                    }
                } catch (ServiceManager.ServiceNotFoundException e2) {
                    userManagerService = null;
                    StartUpStringUtil.printException(TAG, e2, "SystemRestoreManagerAdapter not found user manager service.", false);
                }
                adapterInstance = new SystemRestoreManagerAdapter();
            }
            systemRestoreManagerAdapter = adapterInstance;
        }
        return systemRestoreManagerAdapter;
    }

    public boolean rebootRestoreCache(String str) throws SystemRestoreException {
        return rebootRestorePartition(str);
    }

    public boolean rebootRestoreUserData(String str) throws SystemRestoreException {
        return rebootRestorePartition(str);
    }

    private boolean rebootRestorePartition(String str) throws SystemRestoreException {
        if (recoveryService != null) {
            StartUpStringUtil.printDebug(TAG, "rebootRestorePartition before exec in adapter");
            if (!StartUpStringUtil.isEmpty(str)) {
                try {
                    HiLog.info(TAG, "rebootRestorePartition exec begin in adapter", new Object[0]);
                    recoveryService.rebootRecoveryWithCommand(str);
                    HiLog.info(TAG, "rebootRestorePartition exec end in adapter", new Object[0]);
                    return false;
                } catch (RemoteException e) {
                    StartUpStringUtil.printException(TAG, e);
                    throw new SystemRestoreException(e.getMessage());
                }
            } else {
                HiLog.error(TAG, "rebootRestorePartition exec  commond is empty.", new Object[0]);
                throw new SystemRestoreException("get recovery service is null.");
            }
        } else {
            HiLog.error(TAG, "rebootRestorePartition get recovery service is null.", new Object[0]);
            throw new SystemRestoreException("get recovery service is null.");
        }
    }

    public boolean passedAuthenticationRestoreCache(Context context) throws SystemRestoreException {
        return passedAuthenticationRestore(context);
    }

    public boolean passedAuthenticationRestoreUserData(Context context) throws SystemRestoreException {
        if (!passedAuthenticationRestore(context)) {
            return true;
        }
        try {
            if (userManagerService != null) {
                return userManagerService.hasUserRestriction("no_factory_reset", Process.myUserHandle().getIdentifier());
            }
            HiLog.error(TAG, "passedAuthenticationRestoreUserData get user manager service is null.", new Object[0]);
            throw new SystemRestoreException("get user manager service is null.");
        } catch (RemoteException e) {
            StartUpStringUtil.printException(TAG, e);
            throw new SystemRestoreException(e.getMessage());
        }
    }

    private boolean passedAuthenticationRestore(Context context) throws SystemRestoreException {
        android.content.Context adapterContext = getAdapterContext(context);
        try {
            adapterContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            adapterContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
            StartUpStringUtil.printDebug(TAG, "reboot restore passed authentication.");
            return true;
        } catch (SecurityException e) {
            StartUpStringUtil.printException(TAG, e, "enforceCallingOrSelfPermission exception");
            throw new SystemRestoreException(e.getMessage());
        }
    }

    private android.content.Context getAdapterContext(Context context) throws SystemRestoreException {
        Context applicationContext = context.getApplicationContext();
        if (applicationContext != null) {
            Object hostContext = applicationContext.getHostContext();
            if (hostContext == null) {
                HiLog.error(TAG, "getAdapterContext get Context is null.", new Object[0]);
                throw new SystemRestoreException("get adapterContext is null.");
            } else if (hostContext instanceof android.content.Context) {
                return (android.content.Context) hostContext;
            } else {
                HiLog.error(TAG, "getAdapterContext get getHostContext is error.", new Object[0]);
                throw new SystemRestoreException("get getHostContext is error.");
            }
        } else {
            HiLog.error(TAG, "getAdapterContext get applicationContext is null.", new Object[0]);
            throw new SystemRestoreException("get applicationContext is null.");
        }
    }

    public void sendRestoreUserDataBroadcast(Context context) throws SystemRestoreException {
        StartUpStringUtil.printDebug(TAG, "sendRestoreUserDataBroadcast start.");
        android.content.Context adapterContext = getAdapterContext(context);
        ConditionVariable conditionVariable = new ConditionVariable();
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION");
        intent.addFlags(285212672);
        StartUpStringUtil.printDebug(TAG, "sendRestoreUserDataBroadcast before send.");
        adapterContext.sendOrderedBroadcastAsUser(intent, UserHandle.SYSTEM, "android.permission.MASTER_CLEAR", new SystemRestoreBroadcastReceiver(conditionVariable), null, 0, null, null);
        StartUpStringUtil.printDebug(TAG, "sendRestoreUserDataBroadcast wait block.");
        conditionVariable.block(1000);
        StartUpStringUtil.printDebug(TAG, "sendRestoreUserDataBroadcast end.");
    }

    public void passedAuthenticationInstallPackage(Context context) throws SystemRestoreException {
        passedAuthenticationRestore(context);
    }

    public void installUpdateCommands(String str) throws SystemRestoreException {
        rebootRestorePartition(str);
    }

    private static class SystemRestoreBroadcastReceiver extends BroadcastReceiver {
        private ConditionVariable condition;

        public SystemRestoreBroadcastReceiver(ConditionVariable conditionVariable) {
            this.condition = conditionVariable;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(android.content.Context context, Intent intent) {
            StartUpStringUtil.printDebug(SystemRestoreManagerAdapter.TAG, "sendRestoreUserDataBroadcast receive.");
            this.condition.open();
        }
    }
}
