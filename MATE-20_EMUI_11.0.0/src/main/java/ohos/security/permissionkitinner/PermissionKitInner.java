package ohos.security.permissionkitinner;

import android.content.UriPermission;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.UriConverter;
import ohos.rpc.RemoteException;
import ohos.security.permission.PermissionDef;
import ohos.security.permission.PermissionGroupDef;
import ohos.security.permission.PermissionInner;
import ohos.security.permission.UriPermissionDef;
import ohos.utils.Parcel;
import ohos.utils.net.Uri;

public final class PermissionKitInner {
    private static final HashMap<String, String> ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS = new HashMap<>(16);
    private static final int DEFAULT_SIZE = 16;
    private static final int FAILTURE_CODE = -1;
    private static final HashMap<String, String> HARMONYOS_PERMISSION_GROUP_NAME_TO_ANDROID = new HashMap<>(16);
    private static final Object INSTANCE_LOCK = new Object();
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY_PERMISSION, "PermissionKitInner");
    private static final int SUB_DOMAIN_SECURITY_PERMISSION = 218115841;
    private static volatile PermissionKitInner sInstance;
    private final PermissionKitInnerProxy mPermissionKitInnerProxy = PermissionKitInnerProxy.getInstance();

    static {
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.UNDEFINED", "ohos.permgroup.UNDEFINED");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.CONTACTS", "ohos.permgroup.CONTACTS");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.CALENDAR", "ohos.permgroup.CALENDAR");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.SMS", "ohos.permgroup.MESSAGE");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.STORAGE", "ohos.permgroup.STORAGE");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.LOCATION", "ohos.permgroup.LOCATION");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.CALL_LOG", "ohos.permgroup.CALL_LOG");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.PHONE", "ohos.permgroup.PHONE");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.MICROPHONE", "ohos.permgroup.MICROPHONE");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.ACTIVITY_RECOGNITION", "ohos.permgroup.MOTION");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.CAMERA", "ohos.permgroup.CAMERA");
        ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.put("android.permission-group.SENSORS", "ohos.permgroup.HEALTH");
        for (Map.Entry<String, String> entry : ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.entrySet()) {
            HARMONYOS_PERMISSION_GROUP_NAME_TO_ANDROID.put(entry.getValue(), entry.getKey());
        }
    }

    private PermissionKitInner() {
    }

    public static PermissionKitInner getInstance() {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new PermissionKitInner();
                }
            }
        }
        return sInstance;
    }

    public boolean canRequestPermission(String str, String str2, int i) {
        try {
            return this.mPermissionKitInnerProxy.canRequestPermission(str, str2, i);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to canRequestPermission because RemoteException", new Object[0]);
            return false;
        }
    }

    public Optional<PermissionDef> translatePermission(PermissionInfo permissionInfo, String str) {
        if (permissionInfo == null || TextUtils.isEmpty(str)) {
            HiLog.error(LABEL, "PermissionKitInner translatePermission info or permissionName is null", new Object[0]);
            return Optional.empty();
        }
        PermissionDef permissionDef = new PermissionDef();
        permissionDef.name = str;
        permissionDef.labelRes = permissionInfo.labelRes;
        permissionDef.descriptionRes = permissionInfo.descriptionRes;
        permissionDef.group = getHarmonyosPermGroupName(permissionInfo.group);
        permissionDef.usageInfo = 0;
        permissionDef.reminderIcon = 0;
        permissionDef.reminderDesc = "";
        if (permissionInfo.isRuntime()) {
            permissionDef.grantMode = 1;
        } else {
            permissionDef.grantMode = 0;
        }
        if (permissionInfo.getProtection() == 2) {
            permissionDef.availableScope |= 1;
        }
        if ((permissionInfo.getProtectionFlags() & 16) != 0) {
            permissionDef.availableScope |= 8;
        }
        if ((permissionInfo.getProtectionFlags() & 1024) != 0) {
            permissionDef.availableScope |= 4;
        }
        if (PermissionInner.isRestrictedPermission(str)) {
            permissionDef.availableScope |= 2;
        }
        if ((permissionInfo.flags & 2) != 0) {
            permissionDef.permissionFlags |= 1;
        }
        return Optional.of(new PermissionDef(permissionDef));
    }

    public Optional<PermissionGroupDef> translatePermissionGroup(PermissionGroupInfo permissionGroupInfo, String str) {
        if (permissionGroupInfo == null || TextUtils.isEmpty(str)) {
            HiLog.error(LABEL, "PermissionKitInner translatePermissionGroup info or permissionName is null", new Object[0]);
            return Optional.empty();
        }
        PermissionGroupDef permissionGroupDef = new PermissionGroupDef();
        permissionGroupDef.name = str;
        permissionGroupDef.iconRes = permissionGroupInfo.icon;
        permissionGroupDef.labelRes = permissionGroupInfo.labelRes;
        permissionGroupDef.descriptionRes = permissionGroupInfo.descriptionRes;
        permissionGroupDef.order = permissionGroupInfo.priority;
        permissionGroupDef.requestRes = permissionGroupInfo.requestRes;
        return Optional.of(new PermissionGroupDef(permissionGroupDef));
    }

    public String getHarmonyosPermGroupName(String str) {
        return ANDROID_PERMISSION_GROUP_NAME_TO_HARMONYOS.getOrDefault(str, str);
    }

    public String getAndroidPermGroupName(String str) {
        return HARMONYOS_PERMISSION_GROUP_NAME_TO_ANDROID.getOrDefault(str, str);
    }

    public Optional<UriPermissionDef> translateUriPermission(UriPermission uriPermission) {
        if (uriPermission == null) {
            HiLog.error(LABEL, "PermissionKitInner translateUriPermission param error", new Object[0]);
            return Optional.empty();
        }
        int i = uriPermission.isReadPermission() ? 1 : 0;
        if (uriPermission.isWritePermission()) {
            i |= 2;
        }
        long persistedTime = uriPermission.getPersistedTime();
        try {
            Uri convertToZidaneContentUri = UriConverter.convertToZidaneContentUri(uriPermission.getUri(), "");
            Parcel create = Parcel.create();
            create.writeSequenceable(convertToZidaneContentUri);
            create.writeInt(i);
            create.writeLong(persistedTime);
            return Optional.of(UriPermissionDef.PRODUCER.createFromParcel(create));
        } catch (NullPointerException unused) {
            HiLog.error(LABEL, "PermissionKitInner translateUriPermission null point error", new Object[0]);
            return Optional.empty();
        } catch (IllegalArgumentException unused2) {
            HiLog.error(LABEL, "PermissionKitInner translateUriPermission illeagal argu error", new Object[0]);
            return Optional.empty();
        }
    }
}
