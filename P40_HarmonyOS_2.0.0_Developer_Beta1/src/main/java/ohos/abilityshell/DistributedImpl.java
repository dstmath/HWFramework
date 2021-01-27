package ohos.abilityshell;

import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.HiViewUtil;
import ohos.bundle.AbilityInfo;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.net.Uri;

public class DistributedImpl implements IDistributedManager {
    private static final int DISTRIBUTED_SERVICE_ID = 1401;
    private static final Object LOCK = new Object();
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static IDistributedManager distributedMgrProxy = null;
    private static SysAbilityFactory sysAbilityFactory = new DefaultSysAbilityFactory();

    public static void setDistributedManager(IDistributedManager iDistributedManager) {
        synchronized (LOCK) {
            distributedMgrProxy = iDistributedManager;
        }
    }

    public static void setSysAbilityFactory(SysAbilityFactory sysAbilityFactory2) {
        sysAbilityFactory = sysAbilityFactory2;
    }

    public static SysAbilityFactory getSysAbilityFactory() {
        SysAbilityFactory sysAbilityFactory2 = sysAbilityFactory;
        return sysAbilityFactory2 == null ? new DefaultSysAbilityFactory() : sysAbilityFactory2;
    }

    private boolean initDistributedServiceProxy() {
        if (distributedMgrProxy != null) {
            return true;
        }
        IRemoteObject sysAbility = getSysAbilityFactory().getSysAbility(1401);
        if (sysAbility == null) {
            AppLog.e(SHELL_LABEL, "DistributedImpl::initDistributedServiceProxy DMS not started", new Object[0]);
            HiViewUtil.sendDmsEvent();
            return false;
        }
        distributedMgrProxy = new DistributedManager(sysAbility);
        sysAbility.addDeathRecipient($$Lambda$DistributedImpl$vlDSkDffBRWpIc3r4l25p6Ol05w.INSTANCE, 0);
        return true;
    }

    static /* synthetic */ void lambda$initDistributedServiceProxy$0() {
        AppLog.w(SHELL_LABEL, "DistributedImpl::initDistributedServiceProxy receive death notify", new Object[0]);
        synchronized (LOCK) {
            distributedMgrProxy = null;
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int startRemoteAbility(Intent intent, AbilityInfo abilityInfo, int i) throws RemoteException {
        if (intent == null) {
            AppLog.e(SHELL_LABEL, "DistributedImpl::startRemoteAbility param invalid", new Object[0]);
            return -1;
        }
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::startRemoteAbility get proxy failed", new Object[0]);
                return -1;
            }
            return distributedMgrProxy.startRemoteAbility(intent, abilityInfo, i);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int stopRemoteAbility(Intent intent, AbilityInfo abilityInfo) throws RemoteException {
        if (intent == null) {
            AppLog.e(SHELL_LABEL, "DistributedImpl::stopRemoteAbility param invalid", new Object[0]);
            return -1;
        }
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::stopRemoteAbility get proxy failed", new Object[0]);
                return -1;
            }
            return distributedMgrProxy.stopRemoteAbility(intent, abilityInfo);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int connectRemoteAbility(Intent intent, AbilityInfo abilityInfo, IRemoteObject iRemoteObject) throws RemoteException {
        if (intent == null || iRemoteObject == null) {
            AppLog.e(SHELL_LABEL, "DistributedImpl::connectRemoteAbility param invalid", new Object[0]);
            return -1;
        }
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::startRemoteAbility get DMS proxy failed", new Object[0]);
                return -1;
            }
            return distributedMgrProxy.connectRemoteAbility(intent, abilityInfo, iRemoteObject);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int disconnectRemoteAbility(IRemoteObject iRemoteObject) throws RemoteException {
        if (iRemoteObject == null) {
            AppLog.e(SHELL_LABEL, "DistributedImpl::disconnectRemoteAbility param invalid", new Object[0]);
            return -1;
        }
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::disconnectRemoteAbility get DMS proxy failed", new Object[0]);
                return -1;
            }
            return distributedMgrProxy.disconnectRemoteAbility(iRemoteObject);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public AbilityShellData selectAbility(Intent intent) throws RemoteException {
        if (intent == null) {
            AppLog.e(SHELL_LABEL, "DistributedImpl::selectAbility param invalid ", new Object[0]);
            return null;
        }
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::selectAbility get proxy failed", new Object[0]);
                return null;
            }
            return distributedMgrProxy.selectAbility(intent);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public List<AbilityShellData> fetchAbilities(Intent intent) throws RemoteException {
        if (intent == null) {
            AppLog.e(SHELL_LABEL, "DistributedImpl::fetchAbilities param invalid ", new Object[0]);
            return null;
        }
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::fetchAbilities get proxy failed", new Object[0]);
                return null;
            }
            return distributedMgrProxy.fetchAbilities(intent);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer registerAbilityToken(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2) throws RemoteException {
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::registerAbilityToken get proxy failed", new Object[0]);
                return null;
            }
            return distributedMgrProxy.registerAbilityToken(iRemoteObject, iRemoteObject2);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer unregisterAbilityToken(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2) throws RemoteException {
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::unregisterAbilityToken get proxy failed", new Object[0]);
                return null;
            }
            return distributedMgrProxy.unregisterAbilityToken(iRemoteObject, iRemoteObject2);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer continueAbility(IRemoteObject iRemoteObject, String str, Intent intent) throws RemoteException {
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::continueAbility get proxy failed", new Object[0]);
                return null;
            }
            return distributedMgrProxy.continueAbility(iRemoteObject, str, intent);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer startContinuation(Intent intent, AbilityInfo abilityInfo, IRemoteObject iRemoteObject) throws RemoteException {
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::startContinuation get proxy failed", new Object[0]);
                return null;
            }
            return distributedMgrProxy.startContinuation(intent, abilityInfo, iRemoteObject);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public void notifyCompleteContinuation(String str, int i, boolean z, IRemoteObject iRemoteObject) throws RemoteException {
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::notifyCompleteContinuation get proxy failed", new Object[0]);
            } else {
                distributedMgrProxy.notifyCompleteContinuation(str, i, z, iRemoteObject);
            }
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int selectUri(Uri uri) throws RemoteException {
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::selectUri get proxy failed", new Object[0]);
                return -1;
            }
            return distributedMgrProxy.selectUri(uri);
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int getRemoteDataAbility(Uri uri, IRemoteObject iRemoteObject) throws RemoteException {
        synchronized (LOCK) {
            if (!initDistributedServiceProxy()) {
                AppLog.e(SHELL_LABEL, "DistributedImpl::getRemoteDataAbility get proxy failed", new Object[0]);
                return -1;
            }
            return distributedMgrProxy.getRemoteDataAbility(uri, iRemoteObject);
        }
    }

    /* access modifiers changed from: private */
    public static class DefaultSysAbilityFactory implements SysAbilityFactory {
        private DefaultSysAbilityFactory() {
        }

        @Override // ohos.abilityshell.SysAbilityFactory
        public IRemoteObject getSysAbility(int i) {
            return SysAbilityManager.getSysAbility(i);
        }
    }
}
