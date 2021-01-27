package com.huawei.security.dpermission.fetcher;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Base64;
import com.huawei.android.content.pm.PackageManagerEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.model.GrantedUidPermissionBo;
import com.huawei.security.dpermission.model.PackageBo;
import com.huawei.security.dpermission.model.PermissionBo;
import com.huawei.security.dpermission.model.ResultWrapper;
import com.huawei.security.dpermission.model.SignBo;
import com.huawei.security.dpermission.model.SubjectUidPackageBo;
import com.huawei.security.dpermission.utils.DataValidUtil;
import com.huawei.security.dpermission.utils.PermissionUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.LightweightSet;

public class AndroidPermissionFetcher {
    private static final String APP_PERMISSION_TYPE = "app";
    private static final int CANNOT_GET_PACKAGE_FOR_UID = -12;
    private static final int CANNOT_GRANT_D_PERMISSION = 2;
    private static final String CAN_RETRY_FLAG = "the operation is failed but it can retry";
    private static final int DEFAULT_SIZE = 3;
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "AndroidPermissionFetcher");
    private static final int FAILURE_BUT_CAN_RETRY = -2;
    private static final String HASH_SHA256 = "SHA-256";
    private static final int MAX_PERMISSION_SIZE = 1024;
    private static final String PACKAGE = "package";
    private static final String PERMISSION = "permission";
    private static final int PERMISSION_DEFAULT_STATUS = 0;
    private static final int PERMISSION_DENIED_STATUS = 2;
    private static final int PERMISSION_GRANTED_STATUS = 1;
    private static final String PROTECTION_LEVEL = "protectionLevel";
    private static final String RESULTS = "results";
    private static final String SIGN = "sign";
    private static final Set<String> SYSTEM_PACKAGE_SET = new LightweightSet(3);
    private static final String SYSTEM_PERMISSION_TYPE = "system";
    private static final int UNKNOWN_PROTECTION_LEVEL = -1;
    private Context mContext;
    private final Set<String> systemPermissions = new HashSet();

    private boolean isPermissionGranted(int i) {
        return (i & 2) != 0;
    }

    /* access modifiers changed from: protected */
    public boolean isAppPermissionGranted(int i, int i2, int i3) {
        if (i == 2) {
            return false;
        }
        if (i2 != 1) {
            return true;
        }
        return i3 == 1;
    }

    static {
        SYSTEM_PACKAGE_SET.add("android");
        SYSTEM_PACKAGE_SET.add("androidhwext");
        SYSTEM_PACKAGE_SET.add("com.huawei.harmonyos.foundation");
    }

    public AndroidPermissionFetcher(Context context) {
        if (context != null) {
            this.mContext = context;
        }
    }

    public void init() {
        Optional<PackageManager> packageManager = getPackageManager();
        if (!packageManager.isPresent()) {
            HiLog.error(DPERMISSION_LABEL, "init get packageManager failed!", new Object[0]);
            return;
        }
        for (String str : SYSTEM_PACKAGE_SET) {
            initByPackage(str, packageManager.get());
        }
        HiLog.info(DPERMISSION_LABEL, "Init ok, systemPermissions size: %{public}d", new Object[]{Integer.valueOf(this.systemPermissions.size())});
    }

    private void initByPackage(String str, PackageManager packageManager) {
        try {
            for (PermissionInfo permissionInfo : packageManager.getPackageInfo(str, 4096).permissions) {
                this.systemPermissions.add(permissionInfo.name);
            }
        } catch (PackageManager.NameNotFoundException unused) {
            HiLog.error(DPERMISSION_LABEL, "initByPackage PackageManager.NameNotFoundException: packageName-> %{public}s", new Object[]{str});
        }
    }

    public ResultWrapper<SubjectUidPackageBo> getPermissions(int i) {
        if (!DataValidUtil.isUidValid(i)) {
            return ResultWrapper.wrap(-1, "invalid uid", new SubjectUidPackageBo());
        }
        Optional<PackageManager> packageManager = getPackageManager();
        if (!packageManager.isPresent()) {
            HiLog.error(DPERMISSION_LABEL, "getPermissions packageManager is null.", new Object[0]);
            return ResultWrapper.wrap(-2, "failed to get packageManager, the operation is failed but it can retry", new SubjectUidPackageBo());
        }
        String[] packagesForUid = packageManager.get().getPackagesForUid(i);
        if (packagesForUid == null || packagesForUid.length == 0) {
            HiLog.error(DPERMISSION_LABEL, "getPermissionsForUid pkgNames is empty.", new Object[0]);
            return ResultWrapper.wrap(-12, "cannot get package for uid: " + i, new SubjectUidPackageBo());
        }
        SubjectUidPackageBo constructSubjectUidPackageBo = constructSubjectUidPackageBo(i, packagesForUid, packageManager.get());
        if (constructSubjectUidPackageBo.getPackages().stream().map($$Lambda$gvDSpBgE_wuLZpCJMkZtNH4b5WM.INSTANCE).flatMap($$Lambda$seyL25CSW2NInOydsTbSDrNW6pM.INSTANCE).map($$Lambda$GoJHzCCOTqoayGld4KDWKjz_18Y.INSTANCE).distinct().count() > 1024) {
            return ResultWrapper.wrap(-1, "uid have too many permission", new SubjectUidPackageBo());
        }
        return ResultWrapper.wrap(0, "success", constructSubjectUidPackageBo);
    }

    public ResultWrapper<GrantedUidPermissionBo> getRegrantedPermissions(SubjectUidPackageBo subjectUidPackageBo) {
        boolean z;
        if (!DataValidUtil.isSubjectUidPackageBoValid(subjectUidPackageBo)) {
            return ResultWrapper.wrap(-1, "subjectUidPackageBo is not valid", new GrantedUidPermissionBo());
        }
        Optional<PackageManager> packageManager = getPackageManager();
        if (!packageManager.isPresent()) {
            return ResultWrapper.wrap(-1, "Cannot get packageManager", new GrantedUidPermissionBo());
        }
        int uid = subjectUidPackageBo.getUid();
        List<PackageBo> packages = subjectUidPackageBo.getPackages();
        int appAttribute = subjectUidPackageBo.getAppAttribute();
        if (appAttribute == -2) {
            HiLog.warn(DPERMISSION_LABEL, "getRegrantedPermissions: old version, deal with non-third-party.", new Object[0]);
            z = false;
        } else {
            z = isThirdPartyApp(uid, appAttribute);
        }
        HiLog.debug(DPERMISSION_LABEL, "getRegrantedPermissions: uid: %{public}d, isThirdParty: %{public}b", new Object[]{Integer.valueOf(uid), Boolean.valueOf(z)});
        int size = packages.size();
        Bundle[] bundleArr = new Bundle[size];
        ArraySet arraySet = new ArraySet();
        HashMap hashMap = new HashMap(size);
        for (int i = 0; i < size; i++) {
            bundleArr[i] = filterOutAppPermissions(packages.get(i), arraySet, hashMap, packageManager.get(), z);
        }
        addAllGrantedAppPermission(arraySet, bundleArr, hashMap);
        return ResultWrapper.wrap(0, "success", constructGrantedUidPermissionBo(uid, arraySet));
    }

    private Bundle filterOutAppPermissions(PackageBo packageBo, Set<String> set, Map<String, List<Integer>> map, PackageManager packageManager, boolean z) {
        String name = packageBo.getName();
        List<SignBo> sign = packageBo.getSign();
        List<PermissionBo> permissions = packageBo.getPermissions();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        for (PermissionBo permissionBo : permissions) {
            String name2 = permissionBo.getName();
            if (!z || getProtectionLevel(packageManager, name2) != 1) {
                String type = permissionBo.getType();
                int level = permissionBo.getLevel();
                int status = permissionBo.getStatus();
                if ("app".equals(type)) {
                    arrayList.add(name2);
                    arrayList2.add(Integer.valueOf(level));
                    arrayList3.add(Integer.valueOf(status));
                } else {
                    set.add(name2);
                }
            } else {
                HiLog.debug(DPERMISSION_LABEL, "getRegrantedPermissions: filter %{public}s", new Object[]{name2});
            }
        }
        Bundle assembleBundle = assembleBundle(name, (String[]) arrayList.toArray(new String[0]), getSignatureCodes(sign), arrayList2.stream().mapToInt($$Lambda$AndroidPermissionFetcher$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray());
        map.put(name, arrayList3);
        return assembleBundle;
    }

    private void addAllGrantedAppPermission(Set<String> set, Bundle[] bundleArr, Map<String, List<Integer>> map) {
        Bundle[] canGrantDPermissions = PackageManagerEx.canGrantDPermissions(bundleArr);
        if (canGrantDPermissions == null) {
            HiLog.error(DPERMISSION_LABEL, "addAllGrantedAppPermission bundleResult is null", new Object[0]);
            return;
        }
        for (int i = 0; i < canGrantDPermissions.length; i++) {
            addGrantedAppPermission(set, canGrantDPermissions[i], bundleArr[i], map);
        }
    }

    private GrantedUidPermissionBo constructGrantedUidPermissionBo(int i, Set<String> set) {
        GrantedUidPermissionBo grantedUidPermissionBo = new GrantedUidPermissionBo();
        ArrayList arrayList = new ArrayList();
        for (String str : set) {
            PermissionBo permissionBo = new PermissionBo();
            permissionBo.setName(str);
            arrayList.add(permissionBo);
        }
        grantedUidPermissionBo.setUid(i);
        grantedUidPermissionBo.setPermissions(arrayList);
        return grantedUidPermissionBo;
    }

    private String[] getSignatureCodes(List<SignBo> list) {
        int size = list.size();
        String[] strArr = new String[size];
        for (int i = 0; i < size; i++) {
            strArr[i] = list.get(i).getSha256();
        }
        return strArr;
    }

    private Bundle assembleBundle(String str, String[] strArr, String[] strArr2, int[] iArr) {
        Bundle bundle = new Bundle();
        bundle.putString(PACKAGE, str);
        bundle.putStringArray(PERMISSION, strArr);
        bundle.putStringArray(SIGN, strArr2);
        bundle.putIntArray(PROTECTION_LEVEL, iArr);
        return bundle;
    }

    /* access modifiers changed from: protected */
    public void addGrantedAppPermission(Set<String> set, Bundle bundle, Bundle bundle2, Map<String, List<Integer>> map) {
        if (bundle == null) {
            HiLog.error(DPERMISSION_LABEL, "addGrantedAppPermission bundleResult is null", new Object[0]);
            return;
        }
        String string = bundle.getString(PACKAGE);
        int[] iArr = (int[]) Optional.ofNullable(bundle.getIntArray(RESULTS)).orElse(new int[0]);
        List<Integer> list = map.get(string);
        String[] strArr = (String[]) Optional.ofNullable(bundle2.getStringArray(PERMISSION)).orElse(new String[0]);
        int[] iArr2 = (int[]) Optional.ofNullable(bundle2.getIntArray(PROTECTION_LEVEL)).orElse(new int[0]);
        int length = iArr.length;
        for (int i = 0; i < length; i++) {
            if (isAppPermissionGranted(iArr[i], iArr2[i], list.get(i).intValue())) {
                set.add(strArr[i]);
            }
        }
    }

    private SubjectUidPackageBo constructSubjectUidPackageBo(int i, String[] strArr, PackageManager packageManager) {
        SubjectUidPackageBo subjectUidPackageBo = new SubjectUidPackageBo();
        ArrayList arrayList = new ArrayList();
        int appAttribute = AppInfoFetcher.getInstance().getAppAttribute(i);
        boolean isThirdPartyApp = isThirdPartyApp(i, appAttribute);
        HiLog.debug(DPERMISSION_LABEL, "getPermissions: uid: %{public}d, isThirdParty: %{public}b", new Object[]{Integer.valueOf(i), Boolean.valueOf(isThirdPartyApp)});
        for (String str : strArr) {
            arrayList.add(createPackageBo(i, str, packageManager, isThirdPartyApp));
        }
        subjectUidPackageBo.setUid(i);
        subjectUidPackageBo.setAppAttribute(appAttribute);
        subjectUidPackageBo.setPackages(arrayList);
        return subjectUidPackageBo;
    }

    private PackageBo createPackageBo(int i, String str, PackageManager packageManager, boolean z) {
        try {
            int userId = UserHandleEx.getUserId(i);
            HiLog.debug(DPERMISSION_LABEL, "Current uid corresponding userId is: %{public}d", new Object[]{Integer.valueOf(userId)});
            PackageInfo packageInfoAsUser = PackageManagerEx.getPackageInfoAsUser(packageManager, str, 134221824, userId);
            String[] strArr = packageInfoAsUser.requestedPermissions;
            int[] iArr = packageInfoAsUser.requestedPermissionsFlags;
            List<SignBo> convertToSignBos = convertToSignBos(packageInfoAsUser.signingInfo);
            ArrayList arrayList = new ArrayList();
            if (strArr == null) {
                return constructPackageBo(str, convertToSignBos, arrayList);
            }
            for (int i2 = 0; i2 < strArr.length; i2++) {
                int protectionLevel = getProtectionLevel(packageManager, strArr[i2]);
                if (z && protectionLevel == 1) {
                    HiLog.debug(DPERMISSION_LABEL, "getPermissions: filter %{public}s", new Object[]{strArr[i2]});
                } else if (!isSystemPermission(strArr[i2])) {
                    arrayList.add(constructPermissionBo(strArr[i2], "app", protectionLevel, getAppPermissionStatus(iArr[i2], protectionLevel)));
                } else if (isGranted(i, str, strArr[i2], iArr[i2])) {
                    arrayList.add(constructPermissionBo(strArr[i2], SYSTEM_PERMISSION_TYPE, protectionLevel, 1));
                }
            }
            return constructPackageBo(str, convertToSignBos, arrayList);
        } catch (PackageManager.NameNotFoundException unused) {
            HiLog.error(DPERMISSION_LABEL, "createPackageBo: NameNotFoundException:packageName-> %{public}s ", new Object[]{str});
            return new PackageBo();
        }
    }

    private List<SignBo> convertToSignBos(SigningInfo signingInfo) {
        Signature[] apkContentsSigners = signingInfo.getApkContentsSigners();
        if (apkContentsSigners == null || apkContentsSigners.length <= 0) {
            HiLog.warn(DPERMISSION_LABEL, "getPackageSignature signatures is null", new Object[0]);
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (Signature signature : apkContentsSigners) {
            arrayList.add(constructSignBo(calHash256(signature.toByteArray())));
        }
        return arrayList;
    }

    private PackageBo constructPackageBo(String str, List<SignBo> list, List<PermissionBo> list2) {
        PackageBo packageBo = new PackageBo();
        packageBo.setName(str);
        packageBo.setSign(list);
        packageBo.setPermissions(list2);
        return packageBo;
    }

    private int getProtectionLevel(PackageManager packageManager, String str) {
        try {
            return packageManager.getPermissionInfo(str, 128).getProtection();
        } catch (PackageManager.NameNotFoundException unused) {
            return -1;
        }
    }

    private boolean isSystemPermission(String str) {
        return this.systemPermissions.contains(str);
    }

    private int getAppPermissionStatus(int i, int i2) {
        if (i2 != 1) {
            return 0;
        }
        return isPermissionGranted(i) ? 1 : 2;
    }

    private PermissionBo constructPermissionBo(String str, String str2, int i, int i2) {
        PermissionBo permissionBo = new PermissionBo();
        permissionBo.setName(str);
        permissionBo.setType(str2);
        permissionBo.setLevel(i);
        permissionBo.setStatus(i2);
        return permissionBo;
    }

    private boolean isGranted(int i, String str, String str2, int i2) {
        if (PermissionUtil.isBlockPermission(str2)) {
            return false;
        }
        if (PermissionUtil.isTrustOp(str2)) {
            return isOpGranted(str2, i, str);
        }
        return isPermissionGranted(i2);
    }

    private String calHash256(byte[] bArr) {
        if (bArr == null) {
            return "";
        }
        try {
            MessageDigest instance = MessageDigest.getInstance(HASH_SHA256);
            instance.update(bArr);
            return Base64.encodeToString(instance.digest(), 0).trim();
        } catch (NoSuchAlgorithmException unused) {
            HiLog.error(DPERMISSION_LABEL, "calculate sha256 failed", new Object[0]);
            return "";
        }
    }

    private SignBo constructSignBo(String str) {
        SignBo signBo = new SignBo();
        signBo.setSha256(str);
        return signBo;
    }

    private boolean isOpGranted(String str, int i, String str2) {
        Context context = this.mContext;
        if (context == null) {
            HiLog.error(DPERMISSION_LABEL, "isOpGranted mContext is null!", new Object[0]);
            return false;
        }
        Object systemService = context.getSystemService("appops");
        AppOpsManager appOpsManager = null;
        if (systemService instanceof AppOpsManager) {
            appOpsManager = (AppOpsManager) systemService;
        }
        if (appOpsManager == null) {
            HiLog.error(DPERMISSION_LABEL, "isOpPermGranted get appOpsManager fail!", new Object[0]);
            return false;
        }
        String permissionToOp = AppOpsManager.permissionToOp(str);
        if (permissionToOp == null) {
            HiLog.error(DPERMISSION_LABEL, "permission: %{public}s convert to op failed!", new Object[]{str});
            return false;
        } else if (appOpsManager.noteOpNoThrow(permissionToOp, i, str2) == 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isThirdPartyApp(int i, int i2) {
        return i % 100000 >= 10000 && (i2 & 4) == 0;
    }

    private Optional<PackageManager> getPackageManager() {
        Context context = this.mContext;
        if (context == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(context.getPackageManager());
    }
}
