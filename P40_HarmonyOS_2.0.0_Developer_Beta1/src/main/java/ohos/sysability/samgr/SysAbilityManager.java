package ohos.sysability.samgr;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public final class SysAbilityManager {
    private static final Object LOCK = new Object();
    private static final int SAM_ERROR = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218109952, "SysAbilityManager");
    private static ISysAbilityRegistry sysAbilityManager;

    private static boolean checkInputSysAbilityId(int i) {
        return i >= 1 && i <= 16777215;
    }

    private SysAbilityManager() {
    }

    private static ISysAbilityRegistry getSysAbilityManager() {
        ISysAbilityRegistry iSysAbilityRegistry;
        synchronized (LOCK) {
            if (sysAbilityManager == null) {
                HiLog.info(TAG, "Trying to get native system Ability Manager ", new Object[0]);
                sysAbilityManager = SysAbilityRegistry.getRegistry();
            }
            iSysAbilityRegistry = sysAbilityManager;
        }
        return iSysAbilityRegistry;
    }

    private static ISysAbilityRegistry getSystemAbilityManager() {
        ISysAbilityRegistry iSysAbilityRegistry;
        synchronized (LOCK) {
            if (sysAbilityManager == null) {
                HiLog.info(TAG, "Trying to get native system Ability Manager by loding samgr_proxy.z", new Object[0]);
                sysAbilityManager = SysAbilityRegistry.getSystemAbilityManagerRegistry();
            }
            iSysAbilityRegistry = sysAbilityManager;
        }
        return iSysAbilityRegistry;
    }

    public static IRemoteObject getSysAbility(int i) {
        try {
            return getSystemAbilityManager().getSysAbility(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "get system ability by sysAbilityId remote exception", new Object[0]);
            return null;
        }
    }

    public static IRemoteObject getSysAbility(int i, String str) {
        try {
            return getSysAbilityManager().getSysAbility(i, str);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "get remote system ability exception", new Object[0]);
            return null;
        }
    }

    public static IRemoteObject checkSysAbility(int i) {
        try {
            return getSystemAbilityManager().checkSysAbility(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "get system ability by sysAbilityId remote exception", new Object[0]);
            return null;
        }
    }

    public static int addSysAbility(int i, IRemoteObject iRemoteObject) {
        if (!checkInputSysAbilityId(i)) {
            HiLog.error(TAG, "input sysAbilityId is invalid", new Object[0]);
            return 1;
        }
        try {
            return getSystemAbilityManager().addSysAbility(i, iRemoteObject);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "get system ability by sysAbilityId remote exception", new Object[0]);
            return 1;
        }
    }

    public static int addSysAbility(int i, IRemoteObject iRemoteObject, boolean z, int i2) {
        if (!checkInputSysAbilityId(i)) {
            HiLog.error(TAG, "sysAbilityId is invalid", new Object[0]);
            return 1;
        }
        try {
            return getSystemAbilityManager().addSysAbility(i, iRemoteObject, z, i2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "get system ability by sysAbilityId remote exception", new Object[0]);
            return 1;
        }
    }

    public static int removeSysAbility(int i) {
        try {
            return getSystemAbilityManager().removeSysAbility(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "removeSysAbility by sysAbilityId remote Exception.", new Object[0]);
            return 1;
        }
    }

    public static String[] listSysAbilities() {
        try {
            return getSysAbilityManager().listSysAbilities(15);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "listSysAbilities remote Exception", new Object[0]);
            return new String[0];
        }
    }

    public static IRemoteObject asSystemAbilityManagerRemoteObject() {
        return getSystemAbilityManager().asObject();
    }

    public static IRemoteObject asRemoteObject() {
        return getSysAbilityManager().asObject();
    }

    public static int addSysAbility(int i, IRemoteObject iRemoteObject, boolean z, int i2, String str) {
        if (!checkInputSysAbilityId(i)) {
            HiLog.error(TAG, "sysAbilityId is invalid", new Object[0]);
            return 1;
        }
        try {
            return getSystemAbilityManager().addSysAbility(i, iRemoteObject, z, i2, str);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "add system ability by sysAbilityId remote exception", new Object[0]);
            return 1;
        }
    }
}
