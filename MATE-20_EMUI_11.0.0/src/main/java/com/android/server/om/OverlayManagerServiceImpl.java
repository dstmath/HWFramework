package com.android.server.om;

import android.content.om.OverlayInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.UserInfo;
import android.hwtheme.HwThemeManager;
import android.os.IUserManager;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.om.OverlayManagerSettings;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/* access modifiers changed from: package-private */
public final class OverlayManagerServiceImpl {
    private static final String[] EXT_WHILTLIST_APP = {OverlayManagerSettings.FWK_HONOR_TAG};
    private static final int FLAG_OVERLAY_IS_BEING_REPLACED = 2;
    @Deprecated
    private static final int FLAG_TARGET_IS_BEING_REPLACED = 1;
    private final String[] mDefaultOverlays;
    private final IdmapManager mIdmapManager;
    private final OverlayChangeListener mListener;
    private final PackageManagerHelper mPackageManager;
    private final OverlayManagerSettings mSettings;
    private UserManagerService mUserManager;

    /* access modifiers changed from: package-private */
    public interface OverlayChangeListener {
        void onOverlaysChanged(String str, int i);
    }

    /* access modifiers changed from: package-private */
    public interface PackageManagerHelper {
        List<PackageInfo> getOverlayPackages(int i);

        PackageInfo getPackageInfo(String str, int i);

        boolean signaturesMatching(String str, String str2, int i);
    }

    private static boolean mustReinitializeOverlay(PackageInfo theTruth, OverlayInfo oldSettings) {
        if (oldSettings == null || !Objects.equals(theTruth.overlayTarget, oldSettings.targetPackageName) || !Objects.equals(theTruth.targetOverlayableName, oldSettings.targetOverlayableName) || theTruth.isStaticOverlayPackage() != oldSettings.isStatic) {
            return true;
        }
        if (!theTruth.isStaticOverlayPackage() || theTruth.overlayPriority == oldSettings.priority) {
            return false;
        }
        return true;
    }

    OverlayManagerServiceImpl(PackageManagerHelper packageManager, IdmapManager idmapManager, OverlayManagerSettings settings, String[] defaultOverlays, OverlayChangeListener listener) {
        this.mPackageManager = packageManager;
        this.mIdmapManager = idmapManager;
        this.mSettings = settings;
        this.mDefaultOverlays = defaultOverlays;
        this.mListener = listener;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<String> updateOverlaysForUser(int newUserId) {
        boolean isHiddenSpace;
        int i;
        String[] strArr;
        ArraySet<String> enabledCategories;
        String str;
        OverlayInfo oi;
        boolean isHonorType;
        ArrayMap<String, OverlayInfo> storedOverlayInfos;
        boolean isHiddenSpace2;
        boolean isHonorType2;
        int i2;
        OverlayManagerSettings.BadKeyException e;
        ArrayMap<String, List<OverlayInfo>> tmp;
        boolean isHiddenSpace3;
        boolean isHonorType3;
        String str2;
        int tmpSize;
        int overlayPackagesSize;
        List<PackageInfo> overlayPackages;
        ArrayMap<String, OverlayInfo> storedOverlayInfos2;
        PackageInfo overlayPackage;
        String str3 = "' for user ";
        boolean isHonorType4 = HwThemeManager.isHonorProduct();
        Set<String> packagesToUpdateAssets = new ArraySet<>();
        ArrayMap<String, List<OverlayInfo>> tmp2 = this.mSettings.getOverlaysForUser(newUserId);
        int tmpSize2 = tmp2.size();
        UserInfo newUserInfo = getUserManager().getUserInfo(newUserId);
        boolean isHiddenSpace4 = newUserInfo == null ? false : newUserInfo.isHwHiddenSpace();
        ArrayMap<String, OverlayInfo> storedOverlayInfos3 = new ArrayMap<>(tmpSize2);
        for (int i3 = 0; i3 < tmpSize2; i3++) {
            List<OverlayInfo> chunk = tmp2.valueAt(i3);
            int chunkSize = chunk.size();
            for (int j = 0; j < chunkSize; j++) {
                OverlayInfo oi2 = chunk.get(j);
                if (!filterOverlayinfos(isHonorType4, oi2.packageName, isHiddenSpace4)) {
                    storedOverlayInfos3.put(oi2.packageName, oi2);
                }
            }
        }
        List<PackageInfo> overlayPackages2 = this.mPackageManager.getOverlayPackages(newUserId);
        int overlayPackagesSize2 = overlayPackages2.size();
        int i4 = 0;
        while (i4 < overlayPackagesSize2) {
            PackageInfo overlayPackage2 = overlayPackages2.get(i4);
            if (filterOverlayinfos(isHonorType4, overlayPackage2.packageName, isHiddenSpace4)) {
                storedOverlayInfos2 = storedOverlayInfos3;
                isHiddenSpace3 = isHiddenSpace4;
                tmpSize = tmpSize2;
                str2 = str3;
                isHonorType3 = isHonorType4;
                tmp = tmp2;
                overlayPackagesSize = overlayPackagesSize2;
                overlayPackages = overlayPackages2;
            } else {
                OverlayInfo oi3 = storedOverlayInfos3.get(overlayPackage2.packageName);
                if (mustReinitializeOverlay(overlayPackage2, oi3)) {
                    if (oi3 != null) {
                        packagesToUpdateAssets.add(oi3.targetPackageName);
                    }
                    tmp = tmp2;
                    overlayPackagesSize = overlayPackagesSize2;
                    str2 = str3;
                    overlayPackages = overlayPackages2;
                    storedOverlayInfos2 = storedOverlayInfos3;
                    isHonorType3 = isHonorType4;
                    isHiddenSpace3 = isHiddenSpace4;
                    overlayPackage = overlayPackage2;
                    tmpSize = tmpSize2;
                    this.mSettings.init(overlayPackage2.packageName, newUserId, overlayPackage2.overlayTarget, overlayPackage2.targetOverlayableName, overlayPackage2.applicationInfo.getBaseCodePath(), overlayPackage2.isStaticOverlayPackage(), overlayPackage2.overlayPriority, overlayPackage2.overlayCategory);
                } else {
                    storedOverlayInfos2 = storedOverlayInfos3;
                    isHiddenSpace3 = isHiddenSpace4;
                    tmpSize = tmpSize2;
                    str2 = str3;
                    isHonorType3 = isHonorType4;
                    tmp = tmp2;
                    overlayPackage = overlayPackage2;
                    overlayPackagesSize = overlayPackagesSize2;
                    overlayPackages = overlayPackages2;
                }
                storedOverlayInfos2.remove(overlayPackage.packageName);
            }
            i4++;
            storedOverlayInfos3 = storedOverlayInfos2;
            overlayPackages2 = overlayPackages;
            overlayPackagesSize2 = overlayPackagesSize;
            tmpSize2 = tmpSize;
            str3 = str2;
            isHonorType4 = isHonorType3;
            isHiddenSpace4 = isHiddenSpace3;
            tmp2 = tmp;
        }
        ArrayMap<String, OverlayInfo> storedOverlayInfos4 = storedOverlayInfos3;
        boolean isHiddenSpace5 = isHiddenSpace4;
        String str4 = str3;
        boolean isHonorType5 = isHonorType4;
        int storedOverlayInfosSize = storedOverlayInfos4.size();
        for (int i5 = 0; i5 < storedOverlayInfosSize; i5++) {
            OverlayInfo oi4 = storedOverlayInfos4.valueAt(i5);
            this.mSettings.remove(oi4.packageName, oi4.userId);
            removeIdmapIfPossible(oi4);
            packagesToUpdateAssets.add(oi4.targetPackageName);
        }
        int i6 = 0;
        while (i6 < overlayPackagesSize2) {
            PackageInfo overlayPackage3 = overlayPackages2.get(i6);
            if (!overlayPackage3.isStaticOverlayPackage() || !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(overlayPackage3.overlayTarget)) {
                isHonorType2 = isHonorType5;
                isHiddenSpace2 = isHiddenSpace5;
                if (filterOverlayinfos(isHonorType2, overlayPackage3.packageName, isHiddenSpace2)) {
                    storedOverlayInfos = storedOverlayInfos4;
                } else {
                    try {
                        i2 = newUserId;
                        storedOverlayInfos = storedOverlayInfos4;
                        try {
                            updateState(overlayPackage3.overlayTarget, overlayPackage3.packageName, i2, 0);
                        } catch (OverlayManagerSettings.BadKeyException e2) {
                            e = e2;
                        }
                    } catch (OverlayManagerSettings.BadKeyException e3) {
                        e = e3;
                        i2 = newUserId;
                        storedOverlayInfos = storedOverlayInfos4;
                        Slog.e("OverlayManager", "failed to update settings", e);
                        this.mSettings.remove(overlayPackage3.packageName, i2);
                        packagesToUpdateAssets.add(overlayPackage3.overlayTarget);
                        i6++;
                        isHonorType5 = isHonorType2;
                        isHiddenSpace5 = isHiddenSpace2;
                        storedOverlayInfos4 = storedOverlayInfos;
                    }
                    packagesToUpdateAssets.add(overlayPackage3.overlayTarget);
                }
            } else {
                storedOverlayInfos = storedOverlayInfos4;
                isHonorType2 = isHonorType5;
                isHiddenSpace2 = isHiddenSpace5;
            }
            i6++;
            isHonorType5 = isHonorType2;
            isHiddenSpace5 = isHiddenSpace2;
            storedOverlayInfos4 = storedOverlayInfos;
        }
        boolean isHonorType6 = isHonorType5;
        boolean isHiddenSpace6 = isHiddenSpace5;
        Iterator<String> iter = packagesToUpdateAssets.iterator();
        while (iter.hasNext()) {
            if (this.mPackageManager.getPackageInfo(iter.next(), newUserId) == null) {
                iter.remove();
            }
        }
        ArraySet<String> enabledCategories2 = new ArraySet<>();
        ArrayMap<String, List<OverlayInfo>> userOverlays = this.mSettings.getOverlaysForUser(newUserId);
        int userOverlayTargetCount = userOverlays.size();
        int i7 = 0;
        while (i7 < userOverlayTargetCount) {
            List<OverlayInfo> overlayList = userOverlays.valueAt(i7);
            int j2 = 0;
            for (int overlayCount = overlayList != null ? overlayList.size() : 0; j2 < overlayCount; overlayCount = overlayCount) {
                OverlayInfo oi5 = overlayList.get(j2);
                if (oi5.isEnabled()) {
                    isHonorType = isHonorType6;
                    enabledCategories2.add(oi5.category);
                } else {
                    isHonorType = isHonorType6;
                }
                j2++;
                iter = iter;
                isHonorType6 = isHonorType;
            }
            i7++;
            iter = iter;
            storedOverlayInfosSize = storedOverlayInfosSize;
        }
        String[] strArr2 = this.mDefaultOverlays;
        int length = strArr2.length;
        int i8 = 0;
        while (i8 < length) {
            String defaultOverlay = strArr2[i8];
            try {
                OverlayInfo oi6 = this.mSettings.getOverlayInfo(defaultOverlay, newUserId);
                strArr = strArr2;
                try {
                    if (!enabledCategories2.contains(oi6.category)) {
                        StringBuilder sb = new StringBuilder();
                        i = length;
                        try {
                            sb.append("Enabling default overlay '");
                            sb.append(defaultOverlay);
                            sb.append("' for target '");
                            sb.append(oi6.targetPackageName);
                            sb.append("' in category '");
                            sb.append(oi6.category);
                            str = str4;
                            try {
                                sb.append(str);
                                sb.append(newUserId);
                                Slog.w("OverlayManager", sb.toString());
                                enabledCategories = enabledCategories2;
                                try {
                                    isHiddenSpace = isHiddenSpace6;
                                } catch (OverlayManagerSettings.BadKeyException e4) {
                                    oi = e4;
                                    isHiddenSpace = isHiddenSpace6;
                                    Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                                    i8++;
                                    enabledCategories2 = enabledCategories;
                                    strArr2 = strArr;
                                    isHiddenSpace6 = isHiddenSpace;
                                    str4 = str;
                                    length = i;
                                }
                            } catch (OverlayManagerSettings.BadKeyException e5) {
                                oi = e5;
                                enabledCategories = enabledCategories2;
                                isHiddenSpace = isHiddenSpace6;
                                Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                                i8++;
                                enabledCategories2 = enabledCategories;
                                strArr2 = strArr;
                                isHiddenSpace6 = isHiddenSpace;
                                str4 = str;
                                length = i;
                            }
                            try {
                                this.mSettings.setEnabled(oi6.packageName, newUserId, true);
                                try {
                                    if (updateState(oi6.targetPackageName, oi6.packageName, newUserId, 0)) {
                                        packagesToUpdateAssets.add(oi6.targetPackageName);
                                    }
                                } catch (OverlayManagerSettings.BadKeyException e6) {
                                    oi = e6;
                                    Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                                    i8++;
                                    enabledCategories2 = enabledCategories;
                                    strArr2 = strArr;
                                    isHiddenSpace6 = isHiddenSpace;
                                    str4 = str;
                                    length = i;
                                }
                            } catch (OverlayManagerSettings.BadKeyException e7) {
                                oi = e7;
                                Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                                i8++;
                                enabledCategories2 = enabledCategories;
                                strArr2 = strArr;
                                isHiddenSpace6 = isHiddenSpace;
                                str4 = str;
                                length = i;
                            }
                        } catch (OverlayManagerSettings.BadKeyException e8) {
                            oi = e8;
                            isHiddenSpace = isHiddenSpace6;
                            str = str4;
                            enabledCategories = enabledCategories2;
                            Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                            i8++;
                            enabledCategories2 = enabledCategories;
                            strArr2 = strArr;
                            isHiddenSpace6 = isHiddenSpace;
                            str4 = str;
                            length = i;
                        }
                    } else {
                        i = length;
                        isHiddenSpace = isHiddenSpace6;
                        str = str4;
                        enabledCategories = enabledCategories2;
                    }
                } catch (OverlayManagerSettings.BadKeyException e9) {
                    oi = e9;
                    i = length;
                    isHiddenSpace = isHiddenSpace6;
                    str = str4;
                    enabledCategories = enabledCategories2;
                    Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                    i8++;
                    enabledCategories2 = enabledCategories;
                    strArr2 = strArr;
                    isHiddenSpace6 = isHiddenSpace;
                    str4 = str;
                    length = i;
                }
            } catch (OverlayManagerSettings.BadKeyException e10) {
                oi = e10;
                strArr = strArr2;
                i = length;
                isHiddenSpace = isHiddenSpace6;
                str = str4;
                enabledCategories = enabledCategories2;
                Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                i8++;
                enabledCategories2 = enabledCategories;
                strArr2 = strArr;
                isHiddenSpace6 = isHiddenSpace;
                str4 = str;
                length = i;
            }
            i8++;
            enabledCategories2 = enabledCategories;
            strArr2 = strArr;
            isHiddenSpace6 = isHiddenSpace;
            str4 = str;
            length = i;
        }
        return new ArrayList<>(packagesToUpdateAssets);
    }

    private UserManagerService getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        }
        return this.mUserManager;
    }

    private boolean filterOverlayinfos(boolean isHonorType, String packageName, boolean isHiddenSpaceUser) {
        if (isHonorType) {
            return false;
        }
        boolean isHiddenSpaceEmulation = false;
        if (isHiddenSpaceUser) {
            isHiddenSpaceEmulation = OverlayManagerSettings.FWK_EMULATION_NARROW_TAG.equals(packageName) || OverlayManagerSettings.FWK_EMULATION_TALL_TAG.equals(packageName) || OverlayManagerSettings.FWK_EMULATION_WIDE_TAG.equals(packageName);
        }
        if (OverlayManagerSettings.FWK_HONOR_TAG.equals(packageName) || isHiddenSpaceEmulation) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onUserRemoved(int userId) {
        this.mSettings.removeUser(userId);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageAdded(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageChanged(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageReplacing(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageReplaced(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageRemoved(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    private void updateAndRefreshOverlaysForTarget(String targetPackageName, int userId, int flags) {
        List<OverlayInfo> targetOverlays = this.mSettings.getOverlaysForTarget(targetPackageName, userId);
        boolean modified = false;
        for (OverlayInfo oi : targetOverlays) {
            if (this.mPackageManager.getPackageInfo(oi.packageName, userId) == null) {
                modified |= this.mSettings.remove(oi.packageName, oi.userId);
                removeIdmapIfPossible(oi);
            } else {
                try {
                    modified |= updateState(targetPackageName, oi.packageName, userId, flags);
                } catch (OverlayManagerSettings.BadKeyException e) {
                    Slog.e("OverlayManager", "failed to update settings", e);
                    modified |= this.mSettings.remove(oi.packageName, userId);
                }
            }
        }
        if (!modified) {
            List<String> enabledOverlayPaths = new ArrayList<>(targetOverlays.size());
            for (OverlayInfo oi2 : this.mSettings.getOverlaysForTarget(PackageManagerService.PLATFORM_PACKAGE_NAME, userId)) {
                if (oi2.isEnabled()) {
                    enabledOverlayPaths.add(oi2.baseCodePath);
                }
            }
            for (OverlayInfo oi3 : this.mSettings.getOverlaysForTarget("androidhwext", userId)) {
                if (oi3.isEnabled()) {
                    enabledOverlayPaths.add(oi3.baseCodePath);
                }
            }
            for (OverlayInfo oi4 : targetOverlays) {
                if (oi4.isEnabled()) {
                    enabledOverlayPaths.add(oi4.baseCodePath);
                }
            }
            PackageInfo packageInfo = this.mPackageManager.getPackageInfo(targetPackageName, userId);
            String[] resourceDirs = null;
            ApplicationInfo appInfo = packageInfo == null ? null : packageInfo.applicationInfo;
            if (appInfo != null) {
                resourceDirs = appInfo.resourceDirs;
            }
            if (ArrayUtils.size(resourceDirs) != enabledOverlayPaths.size()) {
                modified = true;
            } else if (resourceDirs != null) {
                int index = 0;
                while (true) {
                    if (index >= resourceDirs.length) {
                        break;
                    } else if (!resourceDirs[index].equals(enabledOverlayPaths.get(index))) {
                        modified = true;
                        break;
                    } else {
                        index++;
                    }
                }
            }
        }
        if (modified) {
            this.mListener.onOverlaysChanged(targetPackageName, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageAdded(String packageName, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null) {
            Slog.w("OverlayManager", "overlay package " + packageName + " was added, but couldn't be found");
            onOverlayPackageRemoved(packageName, userId);
            return;
        }
        this.mSettings.init(packageName, userId, overlayPackage.overlayTarget, overlayPackage.targetOverlayableName, overlayPackage.applicationInfo.getBaseCodePath(), overlayPackage.isStaticOverlayPackage(), overlayPackage.overlayPriority, overlayPackage.overlayCategory);
        try {
            if (updateState(overlayPackage.overlayTarget, packageName, userId, 0)) {
                this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
            this.mSettings.remove(packageName, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageChanged(String packageName, int userId) {
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            if (updateState(oi.targetPackageName, packageName, userId, 0)) {
                this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageReplacing(String packageName, int userId) {
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            if (updateState(oi.targetPackageName, packageName, userId, 2)) {
                removeIdmapIfPossible(oi);
                this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageReplaced(String packageName, int userId) {
        PackageInfo pkg = this.mPackageManager.getPackageInfo(packageName, userId);
        if (pkg == null) {
            Slog.w("OverlayManager", "overlay package " + packageName + " was replaced, but couldn't be found");
            onOverlayPackageRemoved(packageName, userId);
            return;
        }
        try {
            OverlayInfo oldOi = this.mSettings.getOverlayInfo(packageName, userId);
            if (mustReinitializeOverlay(pkg, oldOi)) {
                if (oldOi != null && !oldOi.targetPackageName.equals(pkg.overlayTarget)) {
                    this.mListener.onOverlaysChanged(pkg.overlayTarget, userId);
                }
                this.mSettings.init(packageName, userId, pkg.overlayTarget, pkg.targetOverlayableName, pkg.applicationInfo.getBaseCodePath(), pkg.isStaticOverlayPackage(), pkg.overlayPriority, pkg.overlayCategory);
            }
            if (updateState(pkg.overlayTarget, packageName, userId, 0)) {
                this.mListener.onOverlaysChanged(pkg.overlayTarget, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageRemoved(String packageName, int userId) {
        try {
            OverlayInfo overlayInfo = this.mSettings.getOverlayInfo(packageName, userId);
            if (this.mSettings.remove(packageName, userId)) {
                removeIdmapIfPossible(overlayInfo);
                this.mListener.onOverlaysChanged(overlayInfo.targetPackageName, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to remove overlay", e);
        }
    }

    /* access modifiers changed from: package-private */
    public OverlayInfo getOverlayInfo(String packageName, int userId) {
        try {
            return this.mSettings.getOverlayInfo(packageName, userId);
        } catch (OverlayManagerSettings.BadKeyException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public List<OverlayInfo> getOverlayInfosForTarget(String targetPackageName, int userId) {
        return this.mSettings.getOverlaysForTarget(targetPackageName, userId);
    }

    /* access modifiers changed from: package-private */
    public Map<String, List<OverlayInfo>> getOverlaysForUser(int userId) {
        return this.mSettings.getOverlaysForUser(userId);
    }

    /* access modifiers changed from: package-private */
    public boolean setEnabled(String packageName, boolean enable, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null || overlayPackage.isStaticOverlayPackage()) {
            return false;
        }
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            if (!(this.mSettings.setEnabled(packageName, userId, enable) | updateState(oi.targetPackageName, oi.packageName, userId, 0)) && !checkWhiteExtList(packageName)) {
                return true;
            }
            this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            return true;
        } catch (OverlayManagerSettings.BadKeyException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setEnabledExclusive(String packageName, boolean withinCategory, int userId) {
        if (this.mPackageManager.getPackageInfo(packageName, userId) == null) {
            return false;
        }
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            String targetPackageName = oi.targetPackageName;
            List<OverlayInfo> allOverlays = getOverlayInfosForTarget(targetPackageName, userId);
            boolean modified = false;
            allOverlays.remove(oi);
            for (int i = 0; i < allOverlays.size(); i++) {
                String disabledOverlayPackageName = allOverlays.get(i).packageName;
                PackageInfo disabledOverlayPackageInfo = this.mPackageManager.getPackageInfo(disabledOverlayPackageName, userId);
                if (disabledOverlayPackageInfo == null) {
                    modified |= this.mSettings.remove(disabledOverlayPackageName, userId);
                } else if (!disabledOverlayPackageInfo.isStaticOverlayPackage()) {
                    if (!withinCategory || Objects.equals(disabledOverlayPackageInfo.overlayCategory, oi.category)) {
                        modified = modified | this.mSettings.setEnabled(disabledOverlayPackageName, userId, false) | updateState(targetPackageName, disabledOverlayPackageName, userId, 0);
                    }
                }
            }
            if (((modified | this.mSettings.setEnabled(packageName, userId, true)) || updateState(targetPackageName, packageName, userId, 0)) || checkWhiteExtList(packageName)) {
                this.mListener.onOverlaysChanged(targetPackageName, userId);
            }
            return true;
        } catch (OverlayManagerSettings.BadKeyException e) {
            return false;
        }
    }

    private boolean checkWhiteExtList(String packageName) {
        int length = EXT_WHILTLIST_APP.length;
        for (int i = 0; i < length; i++) {
            if (packageName != null && packageName.equals(EXT_WHILTLIST_APP[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isPackageUpdatableOverlay(String packageName, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null || overlayPackage.isStaticOverlayPackage()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setPriority(String packageName, String newParentPackageName, int userId) {
        PackageInfo overlayPackage;
        if (!isPackageUpdatableOverlay(packageName, userId) || (overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId)) == null) {
            return false;
        }
        if (!this.mSettings.setPriority(packageName, newParentPackageName, userId)) {
            return true;
        }
        this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setHighestPriority(String packageName, int userId) {
        PackageInfo overlayPackage;
        if (!isPackageUpdatableOverlay(packageName, userId) || (overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId)) == null) {
            return false;
        }
        if (!this.mSettings.setHighestPriority(packageName, userId)) {
            return true;
        }
        this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setLowestPriority(String packageName, int userId) {
        PackageInfo overlayPackage;
        if (!isPackageUpdatableOverlay(packageName, userId) || (overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId)) == null) {
            return false;
        }
        if (!this.mSettings.setLowestPriority(packageName, userId)) {
            return true;
        }
        this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, DumpState dumpState) {
        this.mSettings.dump(pw, dumpState);
        if (dumpState.getPackageName() == null) {
            pw.println("Default overlays: " + TextUtils.join(";", this.mDefaultOverlays));
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getDefaultOverlayPackages() {
        return this.mDefaultOverlays;
    }

    /* access modifiers changed from: package-private */
    public List<String> getEnabledOverlayPackageNames(String targetPackageName, int userId) {
        List<OverlayInfo> overlays = this.mSettings.getOverlaysForTarget(targetPackageName, userId);
        List<String> paths = new ArrayList<>(overlays.size());
        int n = overlays.size();
        for (int i = 0; i < n; i++) {
            OverlayInfo oi = overlays.get(i);
            if (oi.isEnabled()) {
                paths.add(oi.packageName);
            }
        }
        return paths;
    }

    private boolean updateState(String targetPackageName, String overlayPackageName, int userId, int flags) throws OverlayManagerSettings.BadKeyException {
        PackageInfo targetPackage = this.mPackageManager.getPackageInfo(targetPackageName, userId);
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(overlayPackageName, userId);
        if (!(targetPackage == null || overlayPackage == null || (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(targetPackageName) && overlayPackage.isStaticOverlayPackage()))) {
            this.mIdmapManager.createIdmap(targetPackage, overlayPackage, userId);
        }
        boolean modified = false;
        if (overlayPackage != null) {
            modified = false | this.mSettings.setBaseCodePath(overlayPackageName, userId, overlayPackage.applicationInfo.getBaseCodePath()) | this.mSettings.setCategory(overlayPackageName, userId, overlayPackage.overlayCategory);
        }
        int currentState = this.mSettings.getState(overlayPackageName, userId);
        int newState = calculateNewState(targetPackage, overlayPackage, userId, flags);
        if (currentState != newState) {
            return modified | this.mSettings.setState(overlayPackageName, userId, newState);
        }
        return modified;
    }

    private int calculateNewState(PackageInfo targetPackage, PackageInfo overlayPackage, int userId, int flags) throws OverlayManagerSettings.BadKeyException {
        if ((flags & 1) != 0) {
            return 4;
        }
        if ((flags & 2) != 0) {
            return 5;
        }
        if (targetPackage == null) {
            return 0;
        }
        if (overlayPackage == null || overlayPackage.applicationInfo == null || !this.mIdmapManager.idmapExists(overlayPackage, userId)) {
            Slog.i("OverlayManager", "overlayPackage == null, return STATE_NO_IDMAP");
            return 1;
        } else if (overlayPackage.isStaticOverlayPackage()) {
            return 6;
        } else {
            return this.mSettings.getEnabled(overlayPackage.packageName, userId) ? 3 : 2;
        }
    }

    private void removeIdmapIfPossible(OverlayInfo oi) {
        if (this.mIdmapManager.idmapExists(oi)) {
            for (int userId : this.mSettings.getUsers()) {
                try {
                    OverlayInfo tmp = this.mSettings.getOverlayInfo(oi.packageName, userId);
                    if (tmp != null && tmp.isEnabled()) {
                        return;
                    }
                } catch (OverlayManagerSettings.BadKeyException e) {
                }
            }
            this.mIdmapManager.removeIdmap(oi, oi.userId);
        }
    }
}
