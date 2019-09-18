package com.android.server.om;

import android.content.om.OverlayInfo;
import android.content.pm.PackageInfo;
import android.content.pm.UserInfo;
import android.hwtheme.HwThemeManager;
import android.os.IUserManager;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.om.OverlayManagerSettings;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserManagerService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class OverlayManagerServiceImpl {
    private static final String AMOLED = "AMOLED";
    private static final String DARK = "dark";
    private static final String[] EXT_WHILTLIST_APP = {OverlayManagerSettings.FWK_DARK_TAG, OverlayManagerSettings.FWK_HONOR_TAG, OverlayManagerSettings.FWK_NOVA_TAG};
    private static final String FILEPATH = "/sys/class/graphics/fb0/panel_info";
    private static final int FLAG_OVERLAY_IS_UPGRADING = 2;
    private static final int FLAG_TARGET_IS_UPGRADING = 1;
    private static final String NO_DARK = "noDark";
    private static final String PREX = "lcdtype:";
    private static final String UN_KNOWN = "Unknown";
    private static String sThemetype = null;
    private final String[] mDefaultOverlays;
    private final IdmapManager mIdmapManager;
    private final OverlayChangeListener mListener;
    private final PackageManagerHelper mPackageManager;
    private final OverlayManagerSettings mSettings;
    private UserManagerService mUserManager;

    interface OverlayChangeListener {
        void onOverlaysChanged(String str, int i);
    }

    interface PackageManagerHelper {
        List<PackageInfo> getOverlayPackages(int i);

        PackageInfo getPackageInfo(String str, int i);

        boolean signaturesMatching(String str, String str2, int i);
    }

    private static boolean mustReinitializeOverlay(PackageInfo theTruth, OverlayInfo oldSettings) {
        if (oldSettings == null || !Objects.equals(theTruth.overlayTarget, oldSettings.targetPackageName) || theTruth.isStaticOverlayPackage() != oldSettings.isStatic) {
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
        Iterator<String> iter;
        ArraySet<String> enabledCategories;
        int i;
        String[] strArr;
        int storedOverlayInfosSize;
        Iterator<String> iter2;
        boolean isDarkTheme;
        ArrayMap<String, OverlayInfo> storedOverlayInfos;
        boolean isHiddenSpace;
        boolean isDarkTheme2;
        ArrayMap<String, OverlayInfo> storedOverlayInfos2;
        int i2;
        boolean isHiddenSpace2;
        boolean isDarkTheme3;
        int tmpSize;
        ArrayMap<String, List<OverlayInfo>> tmp;
        UserInfo newUserInfo;
        int tmpSize2;
        List<PackageInfo> overlayPackages;
        ArrayMap<String, OverlayInfo> storedOverlayInfos3;
        PackageInfo overlayPackage;
        int i3 = newUserId;
        if (sThemetype == null || UN_KNOWN.equals(sThemetype)) {
            boolean isAmoledPanel = isAmoledPanel();
            if (sThemetype == null) {
                setThemetype(isAmoledPanel ? DARK : NO_DARK);
            }
            Slog.d("OverlayManager", "updateOverlaysForUser newUserId=" + i3 + ",sThemetype=" + sThemetype);
        }
        boolean isDarkTheme4 = DARK.equals(sThemetype);
        boolean isHonorType = HwThemeManager.isHonorProduct();
        Set<String> packagesToUpdateAssets = new ArraySet<>();
        ArrayMap<String, List<OverlayInfo>> tmp2 = this.mSettings.getOverlaysForUser(i3);
        int overlayPackagesSize = tmp2.size();
        UserInfo newUserInfo2 = getUserManager().getUserInfo(i3);
        boolean isHiddenSpace3 = newUserInfo2 == null ? false : newUserInfo2.isHwHiddenSpace();
        ArrayMap<String, OverlayInfo> storedOverlayInfos4 = new ArrayMap<>(overlayPackagesSize);
        for (int i4 = 0; i4 < overlayPackagesSize; i4++) {
            List<OverlayInfo> chunk = tmp2.valueAt(i4);
            int chunkSize = chunk.size();
            for (int j = 0; j < chunkSize; j++) {
                OverlayInfo oi = chunk.get(j);
                if (!filterOverlayinfos(isHonorType, isDarkTheme4, oi.packageName, isHiddenSpace3)) {
                    storedOverlayInfos4.put(oi.packageName, oi);
                }
            }
        }
        List<PackageInfo> overlayPackages2 = this.mPackageManager.getOverlayPackages(i3);
        int overlayPackagesSize2 = overlayPackages2.size();
        int i5 = 0;
        while (i5 < overlayPackagesSize2) {
            PackageInfo overlayPackage2 = overlayPackages2.get(i5);
            if (filterOverlayinfos(isHonorType, isDarkTheme4, overlayPackage2.packageName, isHiddenSpace3)) {
                storedOverlayInfos3 = storedOverlayInfos4;
                isHiddenSpace2 = isHiddenSpace3;
                newUserInfo = newUserInfo2;
                isDarkTheme3 = isDarkTheme4;
                tmp = tmp2;
                tmpSize = overlayPackagesSize;
                tmpSize2 = overlayPackagesSize2;
                overlayPackages = overlayPackages2;
            } else {
                OverlayInfo oi2 = storedOverlayInfos4.get(overlayPackage2.packageName);
                if (mustReinitializeOverlay(overlayPackage2, oi2)) {
                    if (oi2 != null) {
                        packagesToUpdateAssets.add(oi2.targetPackageName);
                    }
                    OverlayManagerSettings overlayManagerSettings = this.mSettings;
                    OverlayInfo overlayInfo = oi2;
                    OverlayInfo oi3 = overlayPackage2.packageName;
                    int overlayPackagesSize3 = overlayPackagesSize2;
                    String str = overlayPackage2.overlayTarget;
                    ArrayMap<String, OverlayInfo> storedOverlayInfos5 = storedOverlayInfos4;
                    String baseCodePath = overlayPackage2.applicationInfo.getBaseCodePath();
                    boolean isHiddenSpace4 = overlayPackage2.isStaticOverlayPackage();
                    List<PackageInfo> overlayPackages3 = overlayPackages2;
                    int i6 = overlayPackage2.overlayPriority;
                    UserInfo newUserInfo3 = newUserInfo2;
                    UserInfo newUserInfo4 = overlayPackage2.overlayCategory;
                    tmp = tmp2;
                    overlayPackage = overlayPackage2;
                    int i7 = i3;
                    tmpSize = overlayPackagesSize;
                    tmpSize2 = overlayPackagesSize3;
                    storedOverlayInfos3 = storedOverlayInfos5;
                    isDarkTheme3 = isDarkTheme4;
                    isHiddenSpace2 = isHiddenSpace3;
                    overlayPackages = overlayPackages3;
                    newUserInfo = newUserInfo3;
                    overlayManagerSettings.init(oi3, i7, str, baseCodePath, isHiddenSpace4, i6, newUserInfo4);
                } else {
                    storedOverlayInfos3 = storedOverlayInfos4;
                    isHiddenSpace2 = isHiddenSpace3;
                    newUserInfo = newUserInfo2;
                    isDarkTheme3 = isDarkTheme4;
                    tmp = tmp2;
                    tmpSize = overlayPackagesSize;
                    overlayPackage = overlayPackage2;
                    tmpSize2 = overlayPackagesSize2;
                    overlayPackages = overlayPackages2;
                }
                storedOverlayInfos3.remove(overlayPackage.packageName);
            }
            i5++;
            storedOverlayInfos4 = storedOverlayInfos3;
            overlayPackages2 = overlayPackages;
            overlayPackagesSize2 = tmpSize2;
            newUserInfo2 = newUserInfo;
            tmp2 = tmp;
            overlayPackagesSize = tmpSize;
            isDarkTheme4 = isDarkTheme3;
            isHiddenSpace3 = isHiddenSpace2;
            i3 = newUserId;
        }
        ArrayMap<String, OverlayInfo> storedOverlayInfos6 = storedOverlayInfos4;
        boolean isHiddenSpace5 = isHiddenSpace3;
        UserInfo userInfo = newUserInfo2;
        boolean isDarkTheme5 = isDarkTheme4;
        ArrayMap<String, List<OverlayInfo>> arrayMap = tmp2;
        int i8 = overlayPackagesSize;
        int tmpSize3 = overlayPackagesSize2;
        List<PackageInfo> overlayPackages4 = overlayPackages2;
        int storedOverlayInfosSize2 = storedOverlayInfos6.size();
        for (int i9 = 0; i9 < storedOverlayInfosSize2; i9++) {
            OverlayInfo oi4 = storedOverlayInfos6.valueAt(i9);
            this.mSettings.remove(oi4.packageName, oi4.userId);
            removeIdmapIfPossible(oi4);
            packagesToUpdateAssets.add(oi4.targetPackageName);
        }
        int i10 = 0;
        while (true) {
            int i11 = i10;
            if (i11 >= tmpSize3) {
                break;
            }
            PackageInfo overlayPackage3 = overlayPackages4.get(i11);
            if (!overlayPackage3.isStaticOverlayPackage() || !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(overlayPackage3.overlayTarget)) {
                isDarkTheme2 = isDarkTheme5;
                isHiddenSpace = isHiddenSpace5;
                if (filterOverlayinfos(isHonorType, isDarkTheme2, overlayPackage3.packageName, isHiddenSpace)) {
                    storedOverlayInfos = storedOverlayInfos6;
                } else {
                    try {
                        storedOverlayInfos2 = storedOverlayInfos6;
                        i2 = newUserId;
                        try {
                            updateState(overlayPackage3.overlayTarget, overlayPackage3.packageName, i2, 0);
                        } catch (OverlayManagerSettings.BadKeyException e) {
                            e = e;
                        }
                    } catch (OverlayManagerSettings.BadKeyException e2) {
                        e = e2;
                        storedOverlayInfos2 = storedOverlayInfos6;
                        i2 = newUserId;
                        Slog.e("OverlayManager", "failed to update settings", e);
                        this.mSettings.remove(overlayPackage3.packageName, i2);
                        packagesToUpdateAssets.add(overlayPackage3.overlayTarget);
                        i10 = i11 + 1;
                        isDarkTheme5 = isDarkTheme2;
                        isHiddenSpace5 = isHiddenSpace;
                        storedOverlayInfos6 = storedOverlayInfos;
                    }
                    packagesToUpdateAssets.add(overlayPackage3.overlayTarget);
                    i10 = i11 + 1;
                    isDarkTheme5 = isDarkTheme2;
                    isHiddenSpace5 = isHiddenSpace;
                    storedOverlayInfos6 = storedOverlayInfos;
                }
            } else {
                storedOverlayInfos = storedOverlayInfos6;
                isDarkTheme2 = isDarkTheme5;
                isHiddenSpace = isHiddenSpace5;
            }
            int i12 = newUserId;
            i10 = i11 + 1;
            isDarkTheme5 = isDarkTheme2;
            isHiddenSpace5 = isHiddenSpace;
            storedOverlayInfos6 = storedOverlayInfos;
        }
        boolean isDarkTheme6 = isDarkTheme5;
        boolean z = isHiddenSpace5;
        int i13 = newUserId;
        Iterator<String> iter3 = packagesToUpdateAssets.iterator();
        while (true) {
            iter = iter3;
            if (!iter.hasNext()) {
                break;
            }
            if (this.mPackageManager.getPackageInfo(iter.next(), i13) == null) {
                iter.remove();
            }
            iter3 = iter;
        }
        ArraySet<String> enabledCategories2 = new ArraySet<>();
        ArrayMap<String, List<OverlayInfo>> userOverlays = this.mSettings.getOverlaysForUser(i13);
        int userOverlayTargetCount = userOverlays.size();
        int i14 = 0;
        while (i14 < userOverlayTargetCount) {
            List<OverlayInfo> overlayList = userOverlays.valueAt(i14);
            int overlayCount = overlayList != null ? overlayList.size() : 0;
            int j2 = 0;
            while (true) {
                storedOverlayInfosSize = storedOverlayInfosSize2;
                iter2 = iter;
                int overlayCount2 = overlayCount;
                int j3 = j2;
                if (j3 >= overlayCount2) {
                    break;
                }
                int overlayCount3 = overlayCount2;
                OverlayInfo oi5 = overlayList.get(j3);
                if (oi5.isEnabled()) {
                    isDarkTheme = isDarkTheme6;
                    enabledCategories2.add(oi5.category);
                } else {
                    isDarkTheme = isDarkTheme6;
                }
                j2 = j3 + 1;
                storedOverlayInfosSize2 = storedOverlayInfosSize;
                iter = iter2;
                overlayCount = overlayCount3;
                isDarkTheme6 = isDarkTheme;
            }
            i14++;
            storedOverlayInfosSize2 = storedOverlayInfosSize;
            iter = iter2;
        }
        Iterator<String> it = iter;
        boolean z2 = isDarkTheme6;
        String[] strArr2 = this.mDefaultOverlays;
        int length = strArr2.length;
        int i15 = 0;
        while (i15 < length) {
            String defaultOverlay = strArr2[i15];
            try {
                OverlayInfo oi6 = this.mSettings.getOverlayInfo(defaultOverlay, i13);
                strArr = strArr2;
                try {
                    if (!enabledCategories2.contains(oi6.category)) {
                        i = length;
                        try {
                            StringBuilder sb = new StringBuilder();
                            enabledCategories = enabledCategories2;
                            try {
                                sb.append("Enabling default overlay '");
                                sb.append(defaultOverlay);
                                sb.append("' for target '");
                                sb.append(oi6.targetPackageName);
                                sb.append("' in category '");
                                sb.append(oi6.category);
                                sb.append("' for user ");
                                sb.append(i13);
                                Slog.w("OverlayManager", sb.toString());
                                this.mSettings.setEnabled(oi6.packageName, i13, true);
                            } catch (OverlayManagerSettings.BadKeyException e3) {
                                e = e3;
                                Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + "' for user " + i13, e);
                                i15++;
                                strArr2 = strArr;
                                length = i;
                                enabledCategories2 = enabledCategories;
                            }
                        } catch (OverlayManagerSettings.BadKeyException e4) {
                            e = e4;
                            enabledCategories = enabledCategories2;
                            Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + "' for user " + i13, e);
                            i15++;
                            strArr2 = strArr;
                            length = i;
                            enabledCategories2 = enabledCategories;
                        }
                        try {
                            if (updateState(oi6.targetPackageName, oi6.packageName, i13, 0)) {
                                packagesToUpdateAssets.add(oi6.targetPackageName);
                            }
                        } catch (OverlayManagerSettings.BadKeyException e5) {
                            e = e5;
                            Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + "' for user " + i13, e);
                            i15++;
                            strArr2 = strArr;
                            length = i;
                            enabledCategories2 = enabledCategories;
                        }
                    } else {
                        i = length;
                        enabledCategories = enabledCategories2;
                    }
                } catch (OverlayManagerSettings.BadKeyException e6) {
                    e = e6;
                    i = length;
                    enabledCategories = enabledCategories2;
                    Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + "' for user " + i13, e);
                    i15++;
                    strArr2 = strArr;
                    length = i;
                    enabledCategories2 = enabledCategories;
                }
            } catch (OverlayManagerSettings.BadKeyException e7) {
                e = e7;
                strArr = strArr2;
                i = length;
                enabledCategories = enabledCategories2;
                Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + "' for user " + i13, e);
                i15++;
                strArr2 = strArr;
                length = i;
                enabledCategories2 = enabledCategories;
            }
            i15++;
            strArr2 = strArr;
            length = i;
            enabledCategories2 = enabledCategories;
        }
        return new ArrayList<>(packagesToUpdateAssets);
    }

    private UserManagerService getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        }
        return this.mUserManager;
    }

    private boolean filterOverlayinfos(boolean isHonorType, boolean isAmoledPanel, String packageName, boolean isHiddenSpaceUser) {
        boolean z = true;
        if (isHonorType) {
            if (!OverlayManagerSettings.FWK_DARK_TAG.equals(packageName) && !OverlayManagerSettings.FWK_NOVA_TAG.equals(packageName) && !OverlayManagerSettings.FWK_DARK_OVERLAY_TAG.equals(packageName)) {
                z = false;
            }
            return z;
        } else if (HwThemeManager.isNovaProduct()) {
            if (!OverlayManagerSettings.FWK_DARK_TAG.equals(packageName) && !OverlayManagerSettings.FWK_DARK_OVERLAY_TAG.equals(packageName) && !OverlayManagerSettings.FWK_HONOR_TAG.equals(packageName)) {
                z = false;
            }
            return z;
        } else if (isAmoledPanel) {
            if (!OverlayManagerSettings.FWK_HONOR_TAG.equals(packageName) && !OverlayManagerSettings.FWK_NOVA_TAG.equals(packageName)) {
                z = false;
            }
            return z;
        } else {
            boolean isHiddenSpaceEmulation = false;
            if (isHiddenSpaceUser) {
                isHiddenSpaceEmulation = OverlayManagerSettings.FWK_EMULATION_NARROW_TAG.equals(packageName) || OverlayManagerSettings.FWK_EMULATION_TALL_TAG.equals(packageName) || OverlayManagerSettings.FWK_EMULATION_WIDE_TAG.equals(packageName);
            }
            if (!OverlayManagerSettings.FWK_HONOR_TAG.equals(packageName) && !OverlayManagerSettings.FWK_NOVA_TAG.equals(packageName) && !OverlayManagerSettings.FWK_DARK_TAG.equals(packageName) && !OverlayManagerSettings.FWK_DARK_OVERLAY_TAG.equals(packageName) && !isHiddenSpaceEmulation) {
                z = false;
            }
            return z;
        }
    }

    private boolean isAmoledPanel() {
        String lineValue = readFileByChars(FILEPATH);
        if (TextUtils.isEmpty(lineValue)) {
            return false;
        }
        for (String temp : lineValue.trim().split(",")) {
            if (temp.startsWith(PREX) && temp.split(":")[1].equalsIgnoreCase(AMOLED)) {
                return true;
            }
        }
        return false;
    }

    private static void setThemetype(String type) {
        sThemetype = type;
    }

    private String readFileByChars(String fileName) {
        File file = new File(fileName);
        if (!file.exists() || !file.canRead()) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        Reader reader = null;
        char[] tempChars = new char[512];
        StringBuilder sb = new StringBuilder();
        try {
            Reader reader2 = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            while (true) {
                int read = reader2.read(tempChars, 0, tempChars.length);
                int charRead = read;
                if (read != -1) {
                    sb.append(tempChars, 0, charRead);
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        Slog.e("OverlayManager", "Failed to close " + fileName);
                    }
                }
            }
            reader2.close();
            setThemetype(null);
            return sb.toString();
        } catch (IOException e2) {
            setThemetype(UN_KNOWN);
            Slog.e("OverlayManager", "Failed to read " + fileName);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Slog.e("OverlayManager", "Failed to close " + fileName);
                }
            }
            return null;
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    Slog.e("OverlayManager", "Failed to close " + fileName);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void onUserRemoved(int userId) {
        this.mSettings.removeUser(userId);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageAdded(String packageName, int userId) {
        if (updateAllOverlaysForTarget(packageName, userId, 0)) {
            this.mListener.onOverlaysChanged(packageName, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageChanged(String packageName, int userId) {
        updateAllOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageUpgrading(String packageName, int userId) {
        updateAllOverlaysForTarget(packageName, userId, 1);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageUpgraded(String packageName, int userId) {
        if (updateAllOverlaysForTarget(packageName, userId, 0)) {
            this.mListener.onOverlaysChanged(packageName, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageRemoved(String packageName, int userId) {
        if (updateAllOverlaysForTarget(packageName, userId, 0)) {
            this.mListener.onOverlaysChanged(packageName, userId);
        }
    }

    private boolean updateAllOverlaysForTarget(String targetPackageName, int userId, int flags) {
        List<OverlayInfo> ois = this.mSettings.getOverlaysForTarget(targetPackageName, userId);
        int N = ois.size();
        boolean modified = false;
        boolean modified2 = false;
        for (int i = 0; i < N; i++) {
            OverlayInfo oi = ois.get(i);
            if (this.mPackageManager.getPackageInfo(oi.packageName, userId) == null) {
                modified2 |= this.mSettings.remove(oi.packageName, oi.userId);
                removeIdmapIfPossible(oi);
            } else {
                try {
                    modified2 |= updateState(targetPackageName, oi.packageName, userId, flags);
                } catch (OverlayManagerSettings.BadKeyException e) {
                    Slog.e("OverlayManager", "failed to update settings", e);
                    modified2 |= this.mSettings.remove(oi.packageName, userId);
                }
            }
        }
        if (modified2 || !getEnabledOverlayPackageNames(PackageManagerService.PLATFORM_PACKAGE_NAME, userId).isEmpty()) {
            modified = true;
        }
        return modified;
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageAdded(String packageName, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null) {
            Slog.w("OverlayManager", "overlay package " + packageName + " was added, but couldn't be found");
            onOverlayPackageRemoved(packageName, userId);
            return;
        }
        this.mSettings.init(packageName, userId, overlayPackage.overlayTarget, overlayPackage.applicationInfo.getBaseCodePath(), overlayPackage.isStaticOverlayPackage(), overlayPackage.overlayPriority, overlayPackage.overlayCategory);
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
    public void onOverlayPackageUpgrading(String packageName, int userId) {
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
    public void onOverlayPackageUpgraded(String packageName, int userId) {
        PackageInfo pkg = this.mPackageManager.getPackageInfo(packageName, userId);
        if (pkg == null) {
            Slog.w("OverlayManager", "overlay package " + packageName + " was upgraded, but couldn't be found");
            onOverlayPackageRemoved(packageName, userId);
            return;
        }
        try {
            OverlayInfo oldOi = this.mSettings.getOverlayInfo(packageName, userId);
            if (mustReinitializeOverlay(pkg, oldOi)) {
                if (oldOi != null && !oldOi.targetPackageName.equals(pkg.overlayTarget)) {
                    this.mListener.onOverlaysChanged(pkg.overlayTarget, userId);
                }
                this.mSettings.init(packageName, userId, pkg.overlayTarget, pkg.applicationInfo.getBaseCodePath(), pkg.isStaticOverlayPackage(), pkg.overlayPriority, pkg.overlayCategory);
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
                if (overlayInfo.isEnabled()) {
                    this.mListener.onOverlaysChanged(overlayInfo.targetPackageName, userId);
                }
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
            if ((this.mSettings.setEnabled(packageName, userId, enable) | updateState(oi.targetPackageName, oi.packageName, userId, 0)) || checkWhiteExtList(packageName)) {
                this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            }
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
            allOverlays.remove(oi);
            boolean modified = false;
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
            if ((this.mSettings.setEnabled(packageName, userId, true) | modified | updateState(targetPackageName, packageName, userId, 0)) || checkWhiteExtList(packageName)) {
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
        if (!isPackageUpdatableOverlay(packageName, userId)) {
            return false;
        }
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null) {
            return false;
        }
        if (this.mSettings.setPriority(packageName, newParentPackageName, userId)) {
            this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setHighestPriority(String packageName, int userId) {
        if (!isPackageUpdatableOverlay(packageName, userId)) {
            return false;
        }
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null) {
            return false;
        }
        if (this.mSettings.setHighestPriority(packageName, userId)) {
            this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setLowestPriority(String packageName, int userId) {
        if (!isPackageUpdatableOverlay(packageName, userId)) {
            return false;
        }
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null) {
            return false;
        }
        if (this.mSettings.setLowestPriority(packageName, userId)) {
            this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onDump(PrintWriter pw) {
        this.mSettings.dump(pw);
        pw.println("Default overlays: " + TextUtils.join(";", this.mDefaultOverlays));
    }

    /* access modifiers changed from: package-private */
    public List<String> getEnabledOverlayPackageNames(String targetPackageName, int userId) {
        List<OverlayInfo> overlays = this.mSettings.getOverlaysForTarget(targetPackageName, userId);
        List<String> paths = new ArrayList<>(overlays.size());
        int N = overlays.size();
        for (int i = 0; i < N; i++) {
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
