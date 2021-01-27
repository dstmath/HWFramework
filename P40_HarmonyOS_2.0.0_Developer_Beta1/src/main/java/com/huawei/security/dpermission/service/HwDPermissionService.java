package com.huawei.security.dpermission.service;

import android.content.Context;
import android.os.Binder;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.IHwDPermission;
import com.huawei.security.dpermission.permissionaccessrecord.parcel.BundlePermissionRecordParcel;
import com.huawei.security.dpermission.permissionaccessrecord.parcel.PermissionRecordParcel;
import com.huawei.security.dpermission.permissionaccessrecord.parcel.PermissionRecordRequestParcel;
import com.huawei.security.dpermission.permissionaccessrecord.parcel.PermissionRecordResponseParcel;
import com.huawei.security.dpermission.permissionusingremind.IOnUsingPermissionReminder;
import com.huawei.security.dpermission.permissionusingremind.PermUsingReminderRegisterAdapter;
import java.util.ArrayList;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.dpermissionkit.DPermissionKit;
import ohos.security.permission.BundlePermissionUsedRecord;
import ohos.security.permission.PermissionInner;
import ohos.security.permission.PermissionUsedRecord;
import ohos.security.permission.QueryPermissionUsedRequest;
import ohos.security.permission.QueryPermissionUsedResult;

public class HwDPermissionService extends IHwDPermission.Stub {
    public static final int AID_ROOT = 0;
    public static final int AID_SYSTEM = 1000;
    public static final int COMMAND_PERMISSION_DENIED = -7;
    private static final int FLAG_DISABLE = 0;
    private static final int FLAG_ENABLE = 1;
    private static final int FLAG_PERMISSION_USAGE_DETAIL = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "HwDPermissionService");
    public static final String MANAGE_DISTRIBUTED_PERMISSION = "com.huawei.permission.MANAGE_DISTRIBUTED_PERMISSION";
    private static final int PER_USER_RANGE = 100000;
    protected Context mContext;

    private static int getDetailFlag(int i) {
        return (i & 1) != 0 ? 1 : 0;
    }

    public HwDPermissionService() {
        this(null);
        HiLog.info(LABEL, "Starting HwDPermissionService without context", new Object[0]);
    }

    public HwDPermissionService(Context context) {
        HiLog.info(LABEL, "Starting HwDPermissionService", new Object[0]);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public boolean checkPermissionDenied() {
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        HiLog.debug(LABEL, "HwDPermissionService::CheckPermission pid = %{public}d, uid = %{public}d", new Object[]{Integer.valueOf(callingPid), Integer.valueOf(callingUid)});
        int i = callingUid % PER_USER_RANGE;
        if (i == 0 || i == 1000) {
            HiLog.debug(LABEL, "HwDPermissionService::CheckPermission root or system user is granted", new Object[0]);
            return false;
        }
        Context context = this.mContext;
        if (context == null) {
            HiLog.debug(LABEL, "HwDPermissionService::CheckPermission mContext is null", new Object[0]);
            return true;
        }
        try {
            context.enforceCallingOrSelfPermission(MANAGE_DISTRIBUTED_PERMISSION, "HwDPermissionService");
            HiLog.debug(LABEL, "HwDPermissionService::CheckPermission permission is granted", new Object[0]);
            return false;
        } catch (SecurityException unused) {
            HiLog.debug(LABEL, "HwDPermissionService::CheckPermission permission is denied", new Object[0]);
            return true;
        }
    }

    @Override // com.huawei.security.dpermission.IHwDPermission
    public int allocateDuid(String str, int i) {
        if (checkPermissionDenied()) {
            return -7;
        }
        return DPermissionKit.getInstance().allocateDuid(str, i);
    }

    @Override // com.huawei.security.dpermission.IHwDPermission
    public int queryDuid(String str, int i) {
        if (checkPermissionDenied()) {
            return -7;
        }
        return DPermissionKit.getInstance().queryDuid(str, i);
    }

    @Override // com.huawei.security.dpermission.IHwDPermission
    public int checkDPermission(int i, String str) {
        return DPermissionKit.getInstance().checkDPermission(i, str);
    }

    @Override // com.huawei.security.dpermission.IHwDPermission
    public int notifyUidPermissionChanged(int i) {
        if (checkPermissionDenied()) {
            return -7;
        }
        return DPermissionKit.getInstance().notifyUidPermissionChanged(i);
    }

    @Override // com.huawei.security.dpermission.IHwDPermission
    public int notifyPermissionChanged(int i, String str, int i2) {
        if (checkPermissionDenied()) {
            return -7;
        }
        return DPermissionKit.getInstance().notifyUidPermissionChanged(i);
    }

    @Override // com.huawei.security.dpermission.IHwDPermission
    public int registerOnUsingPermissionReminder(IOnUsingPermissionReminder iOnUsingPermissionReminder) {
        if (!checkPermissionDenied()) {
            return PermUsingReminderRegisterAdapter.getInstance().registerOnUsingPermissionReminder(iOnUsingPermissionReminder);
        }
        HiLog.error(LABEL, "HwDPermissionService::registerOnUsingPermissionReminder permission denied.", new Object[0]);
        return -7;
    }

    @Override // com.huawei.security.dpermission.IHwDPermission
    public int unregisterOnUsingPermissionReminder(IOnUsingPermissionReminder iOnUsingPermissionReminder) {
        if (!checkPermissionDenied()) {
            return PermUsingReminderRegisterAdapter.getInstance().unregisterOnUsingPermissionReminder(iOnUsingPermissionReminder);
        }
        HiLog.error(LABEL, "HwDPermissionService::unregisterOnUsingPermissionReminder permission denied.", new Object[0]);
        return -7;
    }

    @Override // com.huawei.security.dpermission.IHwDPermission
    public int getPermissionUsedRecord(PermissionRecordRequestParcel permissionRecordRequestParcel, PermissionRecordResponseParcel permissionRecordResponseParcel) {
        HiLog.debug(LABEL, "getPermissionUsedRecord called", new Object[0]);
        if (checkPermissionDenied()) {
            return -7;
        }
        HiLog.debug(LABEL, "request: %{public}s", new Object[]{permissionRecordRequestParcel});
        QueryPermissionUsedRequest queryPermissionUsedRequest = new QueryPermissionUsedRequest();
        queryPermissionUsedRequest.setDeviceLabel(permissionRecordRequestParcel.getDeviceName());
        queryPermissionUsedRequest.setBundleName(permissionRecordRequestParcel.getBundleName());
        queryPermissionUsedRequest.setPermissionNames(permissionRecordRequestParcel.getPermissionNames());
        queryPermissionUsedRequest.setBeginTimeMillis(permissionRecordRequestParcel.getBeginTimeMillis());
        queryPermissionUsedRequest.setEndTimeMillis(permissionRecordRequestParcel.getEndTimeMillis());
        queryPermissionUsedRequest.setFlag(getDetailFlag(permissionRecordRequestParcel.getFlag()));
        HiLog.debug(LABEL, "harmonyRequest: %{public}s", new Object[]{queryPermissionUsedRequest});
        QueryPermissionUsedResult queryPermissionUsedResult = new QueryPermissionUsedResult();
        int permissionUsedRecords = PermissionInner.getPermissionUsedRecords(queryPermissionUsedRequest, queryPermissionUsedResult);
        HiLog.debug(LABEL, "errorCode: %{public}d, harmonyResult: %{private}s", new Object[]{Integer.valueOf(permissionUsedRecords), queryPermissionUsedResult});
        permissionRecordResponseParcel.setBeginTimeMillis(queryPermissionUsedRequest.getBeginTimeMillis());
        permissionRecordResponseParcel.setEndTimeMillis(queryPermissionUsedRequest.getEndTimeMillis());
        ArrayList arrayList = new ArrayList(queryPermissionUsedResult.getBundlePermissionUsedRecords().size());
        for (BundlePermissionUsedRecord bundlePermissionUsedRecord : queryPermissionUsedResult.getBundlePermissionUsedRecords()) {
            BundlePermissionRecordParcel bundlePermissionRecordParcel = new BundlePermissionRecordParcel();
            bundlePermissionRecordParcel.setDeviceName(bundlePermissionUsedRecord.getDeviceLabel());
            bundlePermissionRecordParcel.setBundleName(bundlePermissionUsedRecord.getBundleName());
            bundlePermissionRecordParcel.setBundleLabel(bundlePermissionUsedRecord.getBundleLabel());
            ArrayList arrayList2 = new ArrayList(bundlePermissionUsedRecord.getPermissionUsedRecords().size());
            for (PermissionUsedRecord permissionUsedRecord : bundlePermissionUsedRecord.getPermissionUsedRecords()) {
                PermissionRecordParcel permissionRecordParcel = new PermissionRecordParcel();
                permissionRecordParcel.setPermissionName(permissionUsedRecord.getPermissionName());
                permissionRecordParcel.setAccessCountFg(permissionUsedRecord.getAccessCountFg());
                permissionRecordParcel.setRejectCountFg(permissionUsedRecord.getRejectCountFg());
                permissionRecordParcel.setAccessCountBg(permissionUsedRecord.getAccessCountBg());
                permissionRecordParcel.setRejectCountBg(permissionUsedRecord.getRejectCountBg());
                permissionRecordParcel.setLastAccessTime(permissionUsedRecord.getLastAccessTime());
                permissionRecordParcel.setLastRejectTime(permissionUsedRecord.getLastRejectTime());
                arrayList2.add(permissionRecordParcel);
            }
            bundlePermissionRecordParcel.setPermissionRecords(arrayList2);
            arrayList.add(bundlePermissionRecordParcel);
        }
        permissionRecordResponseParcel.setBundlePermissionRecords(arrayList);
        HiLog.debug(LABEL, "response: %{private}s", new Object[]{permissionRecordResponseParcel});
        return permissionUsedRecords;
    }
}
