package ohos.security.dpermissionkit;

import java.util.Optional;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;
import ohos.security.permission.BundleLabelInfo;
import ohos.security.permission.OnPermissionUsedRecord;
import ohos.security.permission.OnUsingPermissionReminder;
import ohos.security.permission.QueryPermissionUsedRequest;
import ohos.security.permission.QueryPermissionUsedResult;

public interface IDPermissionKit extends IRemoteBroker {
    public static final int INVALID_DEVICEID = -3;
    public static final int INVALID_PERMISSION_NAME = -6;
    public static final int INVALID_RUID = -4;

    void addPermissionUsedRecord(String str, String str2, int i, int i2) throws RemoteException;

    int allocateDuid(String str, int i) throws RemoteException;

    int checkDPermission(int i, String str) throws RemoteException;

    int delDuid(String str) throws RemoteException;

    int delDuid(String str, int i) throws RemoteException;

    Optional<BundleLabelInfo> getBundleLabelInfo(int i) throws RemoteException;

    int getPermissionUsedRecords(QueryPermissionUsedRequest queryPermissionUsedRequest, QueryPermissionUsedResult queryPermissionUsedResult) throws RemoteException;

    int getPermissionUsedRecordsAsync(QueryPermissionUsedRequest queryPermissionUsedRequest, OnPermissionUsedRecord onPermissionUsedRecord) throws RemoteException;

    void maintainCommand(String str, String str2) throws RemoteException;

    int notifyAppStatusChanged(int i) throws RemoteException;

    int notifyUidPermissionChanged(int i) throws RemoteException;

    int queryDuid(String str, int i) throws RemoteException;

    int registerUsingPermissionReminder(OnUsingPermissionReminder onUsingPermissionReminder) throws RemoteException;

    int unregisterUsingPermissionReminder(OnUsingPermissionReminder onUsingPermissionReminder) throws RemoteException;
}
