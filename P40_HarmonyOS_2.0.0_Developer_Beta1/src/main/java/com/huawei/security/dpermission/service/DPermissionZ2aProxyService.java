package com.huawei.security.dpermission.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.ServiceManager;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.fetcher.AndroidPermissionFetcher;
import com.huawei.security.dpermission.model.ResultWrapper;
import com.huawei.security.dpermission.model.SubjectUidPackageBo;
import huawei.android.security.HwFrameworkSecurityPartsFactoryImpl;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCSkeleton;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.security.dpermissionkit.DPermissionKit;
import ohos.security.permission.PermissionConversion;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.fastjson.JSONException;
import ohos.utils.zson.ZSONObject;

public class DPermissionZ2aProxyService extends RemoteObject implements IRemoteBroker {
    private static final int AID_ROOT = 0;
    private static final int AID_SYSTEM = 1000;
    private static final String APPLICATION_LIST_EMPTY = "failed to get application list";
    private static final String DESCRIPTOR = "com.huawei.security.dpermission.service.DPermissionZ2aProxyService";
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "DPermissionZ2aProxyService");
    private static final int FAILED_CHECK = -3;
    private static final int FLAGS = 0;
    private static final Object INSTANCE_LOCK = new Object();
    private static final int MESSAGE_PARCEL_LENGTH_THRESHOLD = 25600;
    private static final int PER_USER_RANGE = 100000;
    private static final int SA_ID_DISTRIBUTE_PERMISSION_Z2A_PROXY_SERVICE = 3503;
    private static final int SUCCESS = 0;
    private static final int TRANSACTION_CAN_REQUEST_PERMISSION = 2;
    private static final int TRANSACTION_CHECK_D_PERMISSION = 4;
    private static final int TRANSACTION_GET_REGRANTED_PERMISSION = 3;
    private static final int TRANSACTION_GET_SYSTEM_PERMISSION = 1;
    private static final int TRANSACTION_GET_UID_PERMISSION = 0;
    private static final int UID_FOR_A = 126;
    private static volatile DPermissionZ2aProxyService sInstance;
    private Context mContext;
    private AndroidPermissionFetcher permissionFetcher;

    public IRemoteObject asObject() {
        return this;
    }

    private DPermissionZ2aProxyService(Context context) {
        super(DESCRIPTOR);
        if (context != null) {
            this.mContext = context.getApplicationContext();
            this.permissionFetcher = new AndroidPermissionFetcher(this.mContext);
        }
    }

    public static DPermissionZ2aProxyService getInstance(Context context) {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new DPermissionZ2aProxyService(context);
                }
            }
        }
        return sInstance;
    }

    public void start() {
        if (this.mContext == null) {
            HiLog.error(DPERMISSION_LABEL, "DPermissionZ2aProxyService start context is null.", new Object[0]);
            return;
        }
        HiLog.info(DPERMISSION_LABEL, "Starting add DPermissionZ2aProxyService %{public}d", new Object[]{Integer.valueOf((int) SA_ID_DISTRIBUTE_PERMISSION_Z2A_PROXY_SERVICE)});
        this.permissionFetcher.init();
        SysAbilityManager.addSysAbility((int) SA_ID_DISTRIBUTE_PERMISSION_Z2A_PROXY_SERVICE, asObject());
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            HiLog.error(DPERMISSION_LABEL, "onRemoteRequest data or reply null!", new Object[0]);
            return false;
        }
        String readInterfaceToken = messageParcel.readInterfaceToken();
        if (!DESCRIPTOR.equals(readInterfaceToken)) {
            HiLog.error(DPERMISSION_LABEL, "receive unexpected descriptor: %{public}s", new Object[]{readInterfaceToken});
            return false;
        } else if (i == 0) {
            return getPermissionsInner(messageParcel, messageParcel2);
        } else {
            if (i == 1) {
                return getSystemPermissionsInner(messageParcel2);
            }
            if (i == 2) {
                return canRequestPermissionInner(messageParcel, messageParcel2);
            }
            if (i == 3) {
                return getRegrantedPermissionInner(messageParcel, messageParcel2);
            }
            if (i == 4) {
                return checkDPermissionInner(messageParcel, messageParcel2);
            }
            HiLog.debug(DPERMISSION_LABEL, "forward request to super class", new Object[0]);
            return DPermissionZ2aProxyService.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkPermissionDenied() {
        int callingPid = IPCSkeleton.getCallingPid();
        int callingUid = IPCSkeleton.getCallingUid();
        HiLog.debug(DPERMISSION_LABEL, "DPermissionZ2aProxyService::CheckPermission pid = %{public}d, uid = %{public}d", new Object[]{Integer.valueOf(callingPid), Integer.valueOf(callingUid)});
        int i = callingUid % PER_USER_RANGE;
        if (i != 0 && i != 1000) {
            return true;
        }
        HiLog.debug(DPERMISSION_LABEL, "DPermissionZ2aProxyService::CheckPermission root or system user is granted", new Object[0]);
        return false;
    }

    private boolean getPermissionsInner(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (messageParcel == null || messageParcel2 == null) {
            HiLog.error(DPERMISSION_LABEL, "getPermissionsInner data or reply null!", new Object[0]);
            return false;
        } else if (checkPermissionDenied()) {
            HiLog.debug(DPERMISSION_LABEL, "getPermissionsInner permission denied!", new Object[0]);
            return false;
        } else {
            int readInt = messageParcel.readInt();
            HiLog.debug(DPERMISSION_LABEL, "onRemoteRequest getPermissionsInner uid -> %{public}d", new Object[]{Integer.valueOf(readInt)});
            String uidPermissions = getUidPermissions(readInt);
            setMessageParcelCapacity(messageParcel2, uidPermissions.length());
            if (!messageParcel2.writeString(uidPermissions)) {
                HiLog.error(DPERMISSION_LABEL, "getPermissionsInner writeString failed!", new Object[0]);
            }
            return true;
        }
    }

    private boolean getSystemPermissionsInner(MessageParcel messageParcel) {
        if (messageParcel == null) {
            HiLog.error(DPERMISSION_LABEL, "getSystemPermissionsInner data or reply null!", new Object[0]);
            return false;
        } else if (checkPermissionDenied()) {
            HiLog.debug(DPERMISSION_LABEL, "getSystemPermissionsInner permission denied!", new Object[0]);
            return false;
        } else {
            HiLog.debug(DPERMISSION_LABEL, "onRemoteRequest getSystemPermissionsInner.", new Object[0]);
            String systemPermissions = getSystemPermissions();
            setMessageParcelCapacity(messageParcel, systemPermissions.length());
            if (messageParcel.writeString(systemPermissions)) {
                return true;
            }
            HiLog.error(DPERMISSION_LABEL, "getSystemPermissionsInner writeString failed!", new Object[0]);
            return true;
        }
    }

    private void setMessageParcelCapacity(MessageParcel messageParcel, int i) {
        if (i >= MESSAGE_PARCEL_LENGTH_THRESHOLD && !messageParcel.setCapacity(i * 3)) {
            HiLog.error(DPERMISSION_LABEL, "setParcelCapacity failed!", new Object[0]);
        }
    }

    private boolean canRequestPermissionInner(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (messageParcel == null || messageParcel2 == null) {
            HiLog.error(DPERMISSION_LABEL, "canRequestPermissionInner data or reply null!", new Object[0]);
            return false;
        } else if (messageParcel2.writeBoolean(canRequestPermission(messageParcel.readString(), messageParcel.readString(), messageParcel.readInt()))) {
            return true;
        } else {
            HiLog.error(DPERMISSION_LABEL, "canRequestPermissionInner write return failed!", new Object[0]);
            return true;
        }
    }

    private boolean checkDPermissionInner(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (messageParcel == null || messageParcel2 == null) {
            HiLog.error(DPERMISSION_LABEL, "checkDPermissionInner data or reply null!", new Object[0]);
            return false;
        } else if (checkPermissionDenied()) {
            HiLog.debug(DPERMISSION_LABEL, "checkDPermissionInner permission denied!", new Object[0]);
            return false;
        } else {
            String readString = messageParcel.readString();
            int readInt = messageParcel.readInt();
            HiLog.debug(DPERMISSION_LABEL, "onRemoteRequest checkDPermissionInner duid -> %{public}d and permissionName -> %{public}s", new Object[]{Integer.valueOf(readInt), readString});
            if (!messageParcel2.writeInt(checkDPermission(readString, readInt))) {
                HiLog.error(DPERMISSION_LABEL, "checkDPermissionInner writeInt failed!", new Object[0]);
            }
            return true;
        }
    }

    private boolean getRegrantedPermissionInner(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (messageParcel == null || messageParcel2 == null) {
            HiLog.error(DPERMISSION_LABEL, "getRegrantedPermissionInner data or reply null!", new Object[0]);
            return false;
        } else if (checkPermissionDenied()) {
            HiLog.debug(DPERMISSION_LABEL, "getRegrantedPermissionInner permission denied!", new Object[0]);
            return false;
        } else {
            String readString = messageParcel.readString();
            HiLog.debug(DPERMISSION_LABEL, "onRemoteRequest getRegrantedPermissionInner permissions -> %{public}s", new Object[]{readString});
            String regrantedPermissions = getRegrantedPermissions(readString);
            setMessageParcelCapacity(messageParcel2, regrantedPermissions.length());
            if (!messageParcel2.writeString(regrantedPermissions)) {
                HiLog.error(DPERMISSION_LABEL, "getRegrantedPermissionInner writeString failed!", new Object[0]);
            }
            return true;
        }
    }

    private String getUidPermissions(int i) {
        ResultWrapper<SubjectUidPackageBo> permissions = this.permissionFetcher.getPermissions(i);
        if (permissions.getCode() != 0) {
            return permissions.getMessage();
        }
        return ZSONObject.toZSONString(permissions.getData());
    }

    private String getRegrantedPermissions(String str) {
        try {
            return ZSONObject.toZSONString(this.permissionFetcher.getRegrantedPermissions((SubjectUidPackageBo) ZSONObject.stringToClass(str, SubjectUidPackageBo.class)).getData());
        } catch (JSONException e) {
            HiLog.error(DPERMISSION_LABEL, "getRegrantedPermissions JSONException: %{private}s", new Object[]{e.getMessage()});
            return "failed to getRegrantedPermissions with JSONException";
        } catch (Exception e2) {
            HiLog.error(DPERMISSION_LABEL, "getRegrantedPermissions otherException: %{private}s", new Object[]{e2.getMessage()});
            return "failed to getRegrantedPermissions with otherException";
        }
    }

    private String getSystemPermissions() {
        List<ApplicationInfo> appInfoList = getAppInfoList();
        if (appInfoList.isEmpty()) {
            return APPLICATION_LIST_EMPTY;
        }
        StringBuilder sb = new StringBuilder("[");
        HashSet hashSet = new HashSet();
        for (ApplicationInfo applicationInfo : appInfoList) {
            int i = applicationInfo.uid;
            if (i < 10000 && !hashSet.contains(Integer.valueOf(i))) {
                String uidPermissions = getUidPermissions(i);
                sb.append(uidPermissions + ",");
                hashSet.add(Integer.valueOf(i));
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    private List<ApplicationInfo> getAppInfoList() {
        Context context = this.mContext;
        PackageManager packageManager = context != null ? context.getPackageManager() : null;
        if (packageManager != null) {
            return packageManager.getInstalledApplications(0);
        }
        HiLog.error(DPERMISSION_LABEL, "getAppList packageManager is null", new Object[0]);
        return Collections.emptyList();
    }

    private boolean canRequestPermission(String str, String str2, int i) {
        try {
            String aosPermissionNameIfPossible = PermissionConversion.getAosPermissionNameIfPossible(str);
            IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            if (asInterface == null) {
                HiLog.error(DPERMISSION_LABEL, "canRequestPermission packageManager is null", new Object[0]);
                return false;
            }
            PermissionInfo permissionInfo = asInterface.getPermissionInfo(aosPermissionNameIfPossible, str2, 0);
            if (permissionInfo != null && permissionInfo.isRuntime() && asInterface.checkPermission(aosPermissionNameIfPossible, str2, i) != 0 && (asInterface.getPermissionFlags(aosPermissionNameIfPossible, str2, i) & 22) == 0) {
                return true;
            }
            return false;
        } catch (android.os.RemoteException unused) {
            HiLog.error(DPERMISSION_LABEL, "failed to canRequestPermission RemoteException", new Object[0]);
            return false;
        }
    }

    private int checkDPermission(String str, int i) {
        int i2;
        if (i / PER_USER_RANGE == 126) {
            i2 = new HwFrameworkSecurityPartsFactoryImpl().getDPermissionManager().checkDPermission(i, str);
            HiLog.debug(DPERMISSION_LABEL, "check duid permission from A and the result -> %{public}d", new Object[]{Integer.valueOf(i2)});
        } else {
            i2 = DPermissionKit.getInstance().checkDPermission(i, str);
            HiLog.debug(DPERMISSION_LABEL, "check duid permission from Z and the result -> %{public}d", new Object[]{Integer.valueOf(i2)});
        }
        if (i2 == -3) {
            HiLog.error(DPERMISSION_LABEL, "failed to checkDpermission", new Object[0]);
        }
        return i2;
    }
}
