package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.text.TextUtils;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DevicePackageManager {
    private static final String ADD_FLAG = "addItem";
    public static final String APP_NOTIFY_WHITE_LIST = "notification-app-bt-white-list";
    private static final int LEGAL_RECORD_NUM = 4;
    private static final String PRIV_FLAG = "privPermission";
    private static final String SEPARATOR = ":";
    private static final String TAG = "DevicePackageManager";
    private static final String TERMINATOR = ";";
    private static final String UNDETACHABLE_FLAG = "undetachable";
    public static final String UPDATE_SYS_APP_INSTALL_LIST = "update-sys-app-install-list";
    public static final String UPDATE_SYS_APP_UNDETACHABLE_INSTALL_LIST = "update-sys-app-undetachable-install-list";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public void installPackage(ComponentName admin, String packagePath) {
        this.mDpm.installPackage(admin, packagePath);
    }

    public void uninstallPackage(ComponentName admin, String packageName, boolean keepData) {
        this.mDpm.uninstallPackage(admin, packageName, keepData);
    }

    public void clearPackageData(ComponentName admin, String packageName) {
        this.mDpm.clearPackageData(admin, packageName);
    }

    public void enableInstallPackage(ComponentName admin) {
        this.mDpm.enableInstallPackage(admin);
    }

    public void disableInstallSource(ComponentName admin, List<String> whitelist) {
        this.mDpm.disableInstallSource(admin, whitelist);
    }

    public boolean isInstallSourceDisabled(ComponentName admin) {
        return this.mDpm.isInstallSourceDisabled(admin);
    }

    public List<String> getInstallPackageSourceWhiteList(ComponentName admin) {
        return this.mDpm.getInstallPackageSourceWhiteList(admin);
    }

    public void addInstallPackageWhiteList(ComponentName admin, List<String> packageNames) {
        this.mDpm.addInstallPackageWhiteList(admin, packageNames);
    }

    public void removeInstallPackageWhiteList(ComponentName admin, List<String> packageNames) {
        this.mDpm.removeInstallPackageWhiteList(admin, packageNames);
    }

    public List<String> getInstallPackageWhiteList(ComponentName admin) {
        return this.mDpm.getInstallPackageWhiteList(admin);
    }

    public void addDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        this.mDpm.addDisallowedUninstallPackages(admin, packageNames);
    }

    public void removeDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) {
        this.mDpm.removeDisallowedUninstallPackages(admin, packageNames);
    }

    public List<String> getDisallowedUninstallPackageList(ComponentName admin) {
        return this.mDpm.getDisallowedUninstallPackageList(admin);
    }

    public void addDisabledDeactivateMdmPackages(ComponentName admin, List<String> packageNames) {
        this.mDpm.addDisabledDeactivateMdmPackages(admin, packageNames);
    }

    public void removeDisabledDeactivateMdmPackages(ComponentName admin, List<String> packageNames) {
        this.mDpm.removeDisabledDeactivateMdmPackages(admin, packageNames);
    }

    public List<String> getDisabledDeactivateMdmPackageList(ComponentName admin) {
        return this.mDpm.getDisabledDeactivateMdmPackageList(admin);
    }

    public boolean addAllowNotificationApps(ComponentName admin, ArrayList<String> packageNames) {
        if (packageNames == null || packageNames.isEmpty()) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", filterListPkg(admin, packageNames));
        return this.mDpm.setPolicy(admin, APP_NOTIFY_WHITE_LIST, bundle);
    }

    public boolean removeAllowNotificationApps(ComponentName admin, ArrayList<String> packageNames) {
        if (packageNames == null || packageNames.isEmpty()) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", packageNames);
        return this.mDpm.removePolicy(admin, APP_NOTIFY_WHITE_LIST, bundle);
    }

    public ArrayList<String> getAllowNotificationApps(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, APP_NOTIFY_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public void setSysAppList(ComponentName admin, Map<String, String> maps, Bundle bundle) {
        String policyName;
        if (maps != null && maps.size() != 0) {
            Bundle newBundle = new Bundle();
            StringBuilder builder = new StringBuilder();
            boolean isPrivPermission = false;
            boolean isUndetachable = false;
            boolean isAddItem = true;
            if (bundle != null) {
                isPrivPermission = bundle.getBoolean(PRIV_FLAG, false);
                isUndetachable = bundle.getBoolean(UNDETACHABLE_FLAG, false);
                isAddItem = bundle.getBoolean(ADD_FLAG, true);
            }
            if (isUndetachable) {
                policyName = UPDATE_SYS_APP_UNDETACHABLE_INSTALL_LIST;
            } else {
                policyName = UPDATE_SYS_APP_INSTALL_LIST;
            }
            for (String pkgName : maps.keySet()) {
                String pkgSig = maps.get(pkgName);
                if (!TextUtils.isEmpty(pkgName) && !TextUtils.isEmpty(pkgSig)) {
                    builder.append(pkgName);
                    builder.append(SEPARATOR);
                    builder.append(pkgSig);
                    builder.append(SEPARATOR);
                    builder.append(isPrivPermission ? "true" : "false");
                    builder.append(SEPARATOR);
                    builder.append(isUndetachable ? "true" : "false");
                    builder.append(";");
                }
            }
            Bundle oldBundle = this.mDpm.getPolicy(admin, policyName);
            if (oldBundle != null) {
                String oldValue = oldBundle.getString("value");
                String newValue = formatSysAppData(builder.toString(), oldValue, isAddItem);
                if (newValue != null && !newValue.equals(oldValue)) {
                    newBundle.putString("value", newValue);
                } else {
                    return;
                }
            } else if (isAddItem) {
                newBundle.putString("value", builder.toString());
            } else {
                return;
            }
            this.mDpm.setPolicy(admin, policyName, newBundle);
        }
    }

    private String formatSysAppData(String newValue, String currentValue, boolean isAddOrUpdate) {
        String pkgName;
        String str = newValue;
        if (!TextUtils.isEmpty(currentValue)) {
            String result = currentValue;
            for (String sysPkg : str.split(";")) {
                String[] infoList = sysPkg.split(SEPARATOR);
                if (infoList.length != 4) {
                    return result;
                }
                if (result.contains(pkgName + SEPARATOR + infoList[1] + SEPARATOR)) {
                    int pkgIndex = result.indexOf(pkgName, 0);
                    String singleValue = result.substring(pkgIndex, result.indexOf(";", pkgIndex));
                    if (isAddOrUpdate) {
                        result = result.replace(singleValue, sysPkg);
                    } else {
                        result = result.replace(singleValue + ";", "");
                    }
                } else if (isAddOrUpdate) {
                    result = result + sysPkg + ";";
                }
            }
            return result;
        } else if (isAddOrUpdate) {
            return str;
        } else {
            return "";
        }
    }

    public List<String> getSysAppList(ComponentName who, List<String> pkgNames) {
        if (pkgNames == null || pkgNames.size() < 1) {
            return null;
        }
        List<String> pkgList = new ArrayList<>(pkgNames.size());
        Bundle detachableBundle = this.mDpm.getPolicy(who, UPDATE_SYS_APP_INSTALL_LIST);
        Bundle undetachableBundle = this.mDpm.getPolicy(who, UPDATE_SYS_APP_UNDETACHABLE_INSTALL_LIST);
        for (String pkgName : pkgNames) {
            String foundItem = findValueFromBundle(detachableBundle, pkgName);
            if (!TextUtils.isEmpty(foundItem)) {
                pkgList.add(foundItem);
            } else {
                String foundItem2 = findValueFromBundle(undetachableBundle, pkgName);
                if (!TextUtils.isEmpty(foundItem2)) {
                    pkgList.add(foundItem2);
                }
            }
        }
        return pkgList;
    }

    private String findValueFromBundle(Bundle bundle, String pkgName) {
        if (bundle != null) {
            String value = bundle.getString("value");
            if (value != null) {
                for (String sysPkg : value.split(";")) {
                    String[] infoList = sysPkg.split(SEPARATOR);
                    if (infoList.length == 4 && pkgName.equals(infoList[0])) {
                        return pkgName + SEPARATOR + infoList[1] + SEPARATOR + infoList[2] + SEPARATOR + infoList[3];
                    }
                }
            }
        }
        return null;
    }

    private ArrayList<String> filterListPkg(ComponentName admin, ArrayList<String> addListPkg) {
        List<String> listPkg = getAllowNotificationApps(admin);
        ArrayList<String> tempList = new ArrayList<>();
        if (listPkg == null || listPkg.isEmpty()) {
            return addListPkg;
        }
        int size = addListPkg.size();
        for (int i = 0; i < size; i++) {
            String pkgName = addListPkg.get(i);
            if (!listPkg.contains(pkgName)) {
                tempList.add(pkgName);
            }
        }
        return tempList;
    }
}
