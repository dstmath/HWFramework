package com.android.server.om;

import android.content.om.OverlayInfo;
import android.content.pm.PackageInfo;
import android.hwtheme.HwThemeManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class OverlayManagerServiceImpl {
    private final Set<String> mDefaultOverlays;
    private final IdmapManager mIdmapManager;
    private final OverlayChangeListener mListener;
    private final PackageManagerHelper mPackageManager;
    private final OverlayManagerSettings mSettings;

    interface OverlayChangeListener {
        void onOverlaysChanged(String str, int i);

        void onPackageUpgraded(String str, int i);
    }

    interface PackageManagerHelper {
        List<PackageInfo> getOverlayPackages(int i);

        PackageInfo getPackageInfo(String str, int i);

        boolean signaturesMatching(String str, String str2, int i);
    }

    OverlayManagerServiceImpl(PackageManagerHelper packageManager, IdmapManager idmapManager, OverlayManagerSettings settings, Set<String> defaultOverlays, OverlayChangeListener listener) {
        this.mPackageManager = packageManager;
        this.mIdmapManager = idmapManager;
        this.mSettings = settings;
        this.mDefaultOverlays = defaultOverlays;
        this.mListener = listener;
    }

    private static boolean isPackageStaticOverlay(PackageInfo packageInfo) {
        if (packageInfo.overlayTarget == null || (packageInfo.overlayFlags & 2) == 0) {
            return false;
        }
        return true;
    }

    ArrayList<String> updateOverlaysForUser(int newUserId) {
        int i;
        OverlayInfo oi;
        boolean isHonorType = HwThemeManager.isHonorProduct();
        Set<String> packagesToUpdateAssets = new ArraySet();
        ArrayMap<String, List<OverlayInfo>> tmp = this.mSettings.getOverlaysForUser(newUserId);
        int tmpSize = tmp.size();
        ArrayMap<String, OverlayInfo> arrayMap = new ArrayMap(tmpSize);
        for (i = 0; i < tmpSize; i++) {
            List<OverlayInfo> chunk = (List) tmp.valueAt(i);
            int chunkSize = chunk.size();
            for (int j = 0; j < chunkSize; j++) {
                oi = (OverlayInfo) chunk.get(j);
                if (!(OverlayManagerSettings.FWK_DARK_TAG.equals(oi.packageName) && isHonorType) && (!OverlayManagerSettings.FWK_HONOR_TAG.equals(oi.packageName) || (isHonorType ^ 1) == 0)) {
                    arrayMap.put(oi.packageName, oi);
                }
            }
        }
        List<PackageInfo> overlayPackages = this.mPackageManager.getOverlayPackages(newUserId);
        int overlayPackagesSize = overlayPackages.size();
        for (i = 0; i < overlayPackagesSize; i++) {
            PackageInfo overlayPackage = (PackageInfo) overlayPackages.get(i);
            if (!(OverlayManagerSettings.FWK_DARK_TAG.equals(overlayPackage.packageName) && isHonorType) && (!OverlayManagerSettings.FWK_HONOR_TAG.equals(overlayPackage.packageName) || (isHonorType ^ 1) == 0)) {
                oi = (OverlayInfo) arrayMap.get(overlayPackage.packageName);
                if (oi == null || (oi.targetPackageName.equals(overlayPackage.overlayTarget) ^ 1) != 0) {
                    this.mSettings.init(overlayPackage.packageName, newUserId, overlayPackage.overlayTarget, overlayPackage.applicationInfo.getBaseCodePath(), isPackageStaticOverlay(overlayPackage), overlayPackage.overlayPriority);
                    if (oi != null) {
                        packagesToUpdateAssets.add(oi.targetPackageName);
                    } else if (isPackageStaticOverlay(overlayPackage) || this.mDefaultOverlays.contains(overlayPackage.packageName)) {
                        this.mSettings.setEnabled(overlayPackage.packageName, newUserId, true);
                    }
                }
                try {
                    updateState(this.mPackageManager.getPackageInfo(overlayPackage.overlayTarget, newUserId), overlayPackage, newUserId);
                } catch (BadKeyException e) {
                    Slog.e("OverlayManager", "failed to update settings", e);
                    this.mSettings.remove(overlayPackage.packageName, newUserId);
                }
                packagesToUpdateAssets.add(overlayPackage.overlayTarget);
                arrayMap.remove(overlayPackage.packageName);
            }
        }
        int storedOverlayInfosSize = arrayMap.size();
        for (i = 0; i < storedOverlayInfosSize; i++) {
            oi = (OverlayInfo) arrayMap.valueAt(i);
            this.mSettings.remove(oi.packageName, oi.userId);
            removeIdmapIfPossible(oi);
            packagesToUpdateAssets.add(oi.targetPackageName);
        }
        Iterator<String> iter = packagesToUpdateAssets.iterator();
        while (iter.hasNext()) {
            if (this.mPackageManager.getPackageInfo((String) iter.next(), newUserId) == null) {
                iter.remove();
            }
        }
        return new ArrayList(packagesToUpdateAssets);
    }

    void onUserRemoved(int userId) {
        this.mSettings.removeUser(userId);
    }

    void onTargetPackageAdded(String packageName, int userId) {
        updateAllOverlaysForTarget(packageName, userId, this.mPackageManager.getPackageInfo(packageName, userId));
        this.mListener.onOverlaysChanged(packageName, userId);
    }

    void onTargetPackageChanged(String packageName, int userId) {
        if (updateAllOverlaysForTarget(packageName, userId, this.mPackageManager.getPackageInfo(packageName, userId))) {
            this.mListener.onOverlaysChanged(packageName, userId);
        }
    }

    void onTargetPackageUpgrading(String packageName, int userId) {
        if (updateAllOverlaysForTarget(packageName, userId, null)) {
            this.mListener.onOverlaysChanged(packageName, userId);
        }
    }

    void onTargetPackageUpgraded(String packageName, int userId) {
        if (updateAllOverlaysForTarget(packageName, userId, this.mPackageManager.getPackageInfo(packageName, userId))) {
            this.mListener.onOverlaysChanged(packageName, userId);
        } else {
            this.mListener.onPackageUpgraded(packageName, userId);
        }
    }

    void onTargetPackageRemoved(String packageName, int userId) {
        updateAllOverlaysForTarget(packageName, userId, null);
    }

    private boolean updateAllOverlaysForTarget(String packageName, int userId, PackageInfo targetPackage) {
        boolean modified = false;
        List<OverlayInfo> ois = this.mSettings.getOverlaysForTarget(packageName, userId);
        int N = ois.size();
        for (int i = 0; i < N; i++) {
            OverlayInfo oi = (OverlayInfo) ois.get(i);
            PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(oi.packageName, userId);
            if (overlayPackage == null) {
                modified |= this.mSettings.remove(oi.packageName, oi.userId);
                removeIdmapIfPossible(oi);
            } else {
                try {
                    modified |= updateState(targetPackage, overlayPackage, userId);
                } catch (BadKeyException e) {
                    Slog.e("OverlayManager", "failed to update settings", e);
                    modified |= this.mSettings.remove(oi.packageName, userId);
                }
            }
        }
        return modified;
    }

    void onOverlayPackageAdded(String packageName, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null) {
            Slog.w("OverlayManager", "overlay package " + packageName + " was added, but couldn't be found");
            onOverlayPackageRemoved(packageName, userId);
            return;
        }
        PackageInfo targetPackage = this.mPackageManager.getPackageInfo(overlayPackage.overlayTarget, userId);
        this.mSettings.init(packageName, userId, overlayPackage.overlayTarget, overlayPackage.applicationInfo.getBaseCodePath(), isPackageStaticOverlay(overlayPackage), overlayPackage.overlayPriority);
        try {
            if (updateState(targetPackage, overlayPackage, userId)) {
                this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
            }
        } catch (BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
            this.mSettings.remove(packageName, userId);
        }
    }

    void onOverlayPackageChanged(String packageName, int userId) {
        Slog.wtf("OverlayManager", "onOverlayPackageChanged called, but only pre-installed overlays supported");
    }

    void onOverlayPackageUpgrading(String packageName, int userId) {
        Slog.wtf("OverlayManager", "onOverlayPackageUpgrading called, but only pre-installed overlays supported");
    }

    void onOverlayPackageUpgraded(String packageName, int userId) {
        Slog.wtf("OverlayManager", "onOverlayPackageUpgraded called, but only pre-installed overlays supported");
    }

    void onOverlayPackageRemoved(String packageName, int userId) {
        Slog.wtf("OverlayManager", "onOverlayPackageRemoved called, but only pre-installed overlays supported");
    }

    OverlayInfo getOverlayInfo(String packageName, int userId) {
        try {
            return this.mSettings.getOverlayInfo(packageName, userId);
        } catch (BadKeyException e) {
            return null;
        }
    }

    List<OverlayInfo> getOverlayInfosForTarget(String targetPackageName, int userId) {
        return this.mSettings.getOverlaysForTarget(targetPackageName, userId);
    }

    Map<String, List<OverlayInfo>> getOverlaysForUser(int userId) {
        return this.mSettings.getOverlaysForUser(userId);
    }

    boolean setEnabled(String packageName, boolean enable, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null || isPackageStaticOverlay(overlayPackage)) {
            return false;
        }
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            if (this.mSettings.setEnabled(packageName, userId, enable) | updateState(this.mPackageManager.getPackageInfo(oi.targetPackageName, userId), overlayPackage, userId)) {
                this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            }
            return true;
        } catch (BadKeyException e) {
            return false;
        }
    }

    boolean setEnabledExclusive(String packageName, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null) {
            return false;
        }
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            PackageInfo targetPackage = this.mPackageManager.getPackageInfo(oi.targetPackageName, userId);
            List<OverlayInfo> allOverlays = getOverlayInfosForTarget(oi.targetPackageName, userId);
            int modified = 0;
            allOverlays.remove(oi);
            for (int i = 0; i < allOverlays.size(); i++) {
                String disabledOverlayPackageName = ((OverlayInfo) allOverlays.get(i)).packageName;
                PackageInfo disabledOverlayPackageInfo = this.mPackageManager.getPackageInfo(disabledOverlayPackageName, userId);
                if (disabledOverlayPackageInfo == null) {
                    modified |= this.mSettings.remove(disabledOverlayPackageName, userId);
                } else if (!isPackageStaticOverlay(disabledOverlayPackageInfo)) {
                    modified = (modified | this.mSettings.setEnabled(disabledOverlayPackageName, userId, false)) | updateState(targetPackage, disabledOverlayPackageInfo, userId);
                }
            }
            if ((modified | this.mSettings.setEnabled(packageName, userId, true)) | updateState(targetPackage, overlayPackage, userId)) {
                this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            }
            return true;
        } catch (BadKeyException e) {
            return false;
        }
    }

    private boolean isPackageUpdatableOverlay(String packageName, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null || isPackageStaticOverlay(overlayPackage)) {
            return false;
        }
        return true;
    }

    boolean setPriority(String packageName, String newParentPackageName, int userId) {
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

    boolean setHighestPriority(String packageName, int userId) {
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

    boolean setLowestPriority(String packageName, int userId) {
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

    void onDump(PrintWriter pw) {
        this.mSettings.dump(pw);
        pw.println("Default overlays: " + TextUtils.join(";", this.mDefaultOverlays));
    }

    List<String> getEnabledOverlayPackageNames(String targetPackageName, int userId) {
        List<OverlayInfo> overlays = this.mSettings.getOverlaysForTarget(targetPackageName, userId);
        List<String> paths = new ArrayList(overlays.size());
        int N = overlays.size();
        for (int i = 0; i < N; i++) {
            OverlayInfo oi = (OverlayInfo) overlays.get(i);
            if (oi.isEnabled()) {
                paths.add(oi.packageName);
            }
        }
        return paths;
    }

    private boolean updateState(PackageInfo targetPackage, PackageInfo overlayPackage, int userId) throws BadKeyException {
        if (targetPackage != null) {
            int isPackageStaticOverlay;
            if ("android".equals(targetPackage.packageName)) {
                isPackageStaticOverlay = isPackageStaticOverlay(overlayPackage);
            } else {
                isPackageStaticOverlay = 0;
            }
            if ((isPackageStaticOverlay ^ 1) != 0) {
                this.mIdmapManager.createIdmap(targetPackage, overlayPackage, userId);
            }
        }
        boolean modified = this.mSettings.setBaseCodePath(overlayPackage.packageName, userId, overlayPackage.applicationInfo.getBaseCodePath());
        int currentState = this.mSettings.getState(overlayPackage.packageName, userId);
        int newState = calculateNewState(targetPackage, overlayPackage, userId);
        if (currentState != newState) {
            return modified | this.mSettings.setState(overlayPackage.packageName, userId, newState);
        }
        return modified;
    }

    private int calculateNewState(PackageInfo targetPackage, PackageInfo overlayPackage, int userId) throws BadKeyException {
        if (targetPackage == null) {
            return 0;
        }
        if (!this.mIdmapManager.idmapExists(overlayPackage, userId)) {
            return 1;
        }
        return this.mSettings.getEnabled(overlayPackage.packageName, userId) ? 3 : 2;
    }

    private void removeIdmapIfPossible(OverlayInfo oi) {
        if (this.mIdmapManager.idmapExists(oi)) {
            for (int userId : this.mSettings.getUsers()) {
                try {
                    OverlayInfo tmp = this.mSettings.getOverlayInfo(oi.packageName, userId);
                    if (tmp != null && tmp.isEnabled()) {
                        return;
                    }
                } catch (BadKeyException e) {
                }
            }
            this.mIdmapManager.removeIdmap(oi, oi.userId);
        }
    }
}
