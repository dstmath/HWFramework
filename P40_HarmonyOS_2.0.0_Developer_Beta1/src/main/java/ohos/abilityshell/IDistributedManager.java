package ohos.abilityshell;

import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.bundle.AbilityInfo;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.net.Uri;

public interface IDistributedManager {
    int connectRemoteAbility(Intent intent, AbilityInfo abilityInfo, IRemoteObject iRemoteObject) throws RemoteException;

    Integer continueAbility(IRemoteObject iRemoteObject, String str, Intent intent) throws RemoteException;

    int disconnectRemoteAbility(IRemoteObject iRemoteObject) throws RemoteException;

    List<AbilityShellData> fetchAbilities(Intent intent) throws RemoteException;

    int getRemoteDataAbility(Uri uri, IRemoteObject iRemoteObject) throws RemoteException;

    void notifyCompleteContinuation(String str, int i, boolean z, IRemoteObject iRemoteObject) throws RemoteException;

    Integer registerAbilityToken(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2) throws RemoteException;

    AbilityShellData selectAbility(Intent intent) throws RemoteException;

    int selectUri(Uri uri) throws RemoteException;

    Integer startContinuation(Intent intent, AbilityInfo abilityInfo, IRemoteObject iRemoteObject) throws RemoteException;

    int startRemoteAbility(Intent intent, AbilityInfo abilityInfo, int i) throws RemoteException;

    int stopRemoteAbility(Intent intent, AbilityInfo abilityInfo) throws RemoteException;

    Integer unregisterAbilityToken(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2) throws RemoteException;
}
