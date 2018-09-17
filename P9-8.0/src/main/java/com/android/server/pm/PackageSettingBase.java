package com.android.server.pm;

import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageUserState;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

abstract class PackageSettingBase extends SettingBase {
    static final PackageUserState DEFAULT_USER_STATE = new PackageUserState();
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    static final int PKG_INSTALL_COMPLETE = 1;
    static final int PKG_INSTALL_INCOMPLETE = 0;
    int categoryHint = -1;
    List<String> childPackageNames;
    File codePath;
    String codePathString;
    String cpuAbiOverrideString;
    long firstInstallTime;
    boolean installPermissionsFixed;
    int installStatus = 1;
    String installerPackageName;
    boolean isOrphaned;
    PackageKeySetData keySetData = new PackageKeySetData();
    long lastUpdateTime;
    @Deprecated
    String legacyNativeLibraryPathString;
    float maxAspectRatio;
    final String name;
    Set<String> oldCodePaths;
    PackageSettingBase origPackage;
    String parentPackageName;
    String primaryCpuAbiString;
    final String realName;
    File resourcePath;
    String resourcePathString;
    String secondaryCpuAbiString;
    PackageSignatures signatures;
    long timeStamp;
    boolean uidError;
    boolean updateAvailable;
    private final SparseArray<PackageUserState> userState = new SparseArray();
    String[] usesStaticLibraries;
    int[] usesStaticLibrariesVersions;
    IntentFilterVerificationInfo verificationInfo;
    int versionCode;
    String volumeUuid;

    PackageSettingBase(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int pVersionCode, int pkgFlags, int pkgPrivateFlags, String parentPackageName, List<String> childPackageNames, String[] usesStaticLibraries, int[] usesStaticLibrariesVersions) {
        super(pkgFlags, pkgPrivateFlags);
        this.name = name;
        this.realName = realName;
        this.parentPackageName = parentPackageName;
        this.childPackageNames = childPackageNames != null ? new ArrayList(childPackageNames) : null;
        this.usesStaticLibraries = usesStaticLibraries;
        this.usesStaticLibrariesVersions = usesStaticLibrariesVersions;
        init(codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, pVersionCode);
    }

    PackageSettingBase(PackageSettingBase base, String realName) {
        super(base);
        this.name = base.name;
        this.realName = realName;
        doCopy(base);
    }

    void init(File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int pVersionCode) {
        this.codePath = codePath;
        this.codePathString = codePath.toString();
        this.resourcePath = resourcePath;
        this.resourcePathString = resourcePath.toString();
        this.legacyNativeLibraryPathString = legacyNativeLibraryPathString;
        this.primaryCpuAbiString = primaryCpuAbiString;
        this.secondaryCpuAbiString = secondaryCpuAbiString;
        this.cpuAbiOverrideString = cpuAbiOverrideString;
        this.versionCode = pVersionCode;
        this.signatures = new PackageSignatures();
    }

    public void setInstallerPackageName(String packageName) {
        this.installerPackageName = packageName;
    }

    public String getInstallerPackageName() {
        return this.installerPackageName;
    }

    public void setVolumeUuid(String volumeUuid) {
        this.volumeUuid = volumeUuid;
    }

    public String getVolumeUuid() {
        return this.volumeUuid;
    }

    public void setInstallStatus(int newStatus) {
        this.installStatus = newStatus;
    }

    public int getInstallStatus() {
        return this.installStatus;
    }

    public void setTimeStamp(long newStamp) {
        this.timeStamp = newStamp;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }

    public boolean isUpdateAvailable() {
        return this.updateAvailable;
    }

    public void copyFrom(PackageSettingBase orig) {
        super.copyFrom(orig);
        doCopy(orig);
    }

    private void doCopy(PackageSettingBase orig) {
        List arrayList;
        String[] strArr;
        int[] iArr = null;
        if (orig.childPackageNames != null) {
            arrayList = new ArrayList(orig.childPackageNames);
        } else {
            arrayList = null;
        }
        this.childPackageNames = arrayList;
        this.codePath = orig.codePath;
        this.codePathString = orig.codePathString;
        this.cpuAbiOverrideString = orig.cpuAbiOverrideString;
        this.firstInstallTime = orig.firstInstallTime;
        this.installPermissionsFixed = orig.installPermissionsFixed;
        this.installStatus = orig.installStatus;
        this.installerPackageName = orig.installerPackageName;
        this.isOrphaned = orig.isOrphaned;
        this.keySetData = orig.keySetData;
        this.lastUpdateTime = orig.lastUpdateTime;
        this.legacyNativeLibraryPathString = orig.legacyNativeLibraryPathString;
        this.origPackage = orig.origPackage;
        this.parentPackageName = orig.parentPackageName;
        this.primaryCpuAbiString = orig.primaryCpuAbiString;
        this.resourcePath = orig.resourcePath;
        this.resourcePathString = orig.resourcePathString;
        this.secondaryCpuAbiString = orig.secondaryCpuAbiString;
        this.signatures = orig.signatures;
        this.timeStamp = orig.timeStamp;
        this.uidError = orig.uidError;
        this.userState.clear();
        for (int i = 0; i < orig.userState.size(); i++) {
            this.userState.put(orig.userState.keyAt(i), (PackageUserState) orig.userState.valueAt(i));
        }
        this.verificationInfo = orig.verificationInfo;
        this.versionCode = orig.versionCode;
        this.volumeUuid = orig.volumeUuid;
        this.categoryHint = orig.categoryHint;
        if (orig.usesStaticLibraries != null) {
            strArr = (String[]) Arrays.copyOf(orig.usesStaticLibraries, orig.usesStaticLibraries.length);
        } else {
            strArr = null;
        }
        this.usesStaticLibraries = strArr;
        if (orig.usesStaticLibrariesVersions != null) {
            iArr = Arrays.copyOf(orig.usesStaticLibrariesVersions, orig.usesStaticLibrariesVersions.length);
        }
        this.usesStaticLibrariesVersions = iArr;
        this.updateAvailable = orig.updateAvailable;
        this.maxAspectRatio = orig.maxAspectRatio;
    }

    private PackageUserState modifyUserState(int userId) {
        PackageUserState state = (PackageUserState) this.userState.get(userId);
        if (state != null) {
            return state;
        }
        state = new PackageUserState();
        this.userState.put(userId, state);
        return state;
    }

    public PackageUserState readUserState(int userId) {
        PackageUserState state = (PackageUserState) this.userState.get(userId);
        if (state == null) {
            return DEFAULT_USER_STATE;
        }
        state.categoryHint = this.categoryHint;
        return state;
    }

    void setEnabled(int state, int userId, String callingPackage) {
        PackageUserState st = modifyUserState(userId);
        st.enabled = state;
        st.lastDisableAppCaller = callingPackage;
    }

    int getEnabled(int userId) {
        return readUserState(userId).enabled;
    }

    void setMaxAspectRatio(float ar) {
        this.maxAspectRatio = ar;
    }

    float getMaxAspectRatio() {
        return this.maxAspectRatio;
    }

    String getLastDisabledAppCaller(int userId) {
        return readUserState(userId).lastDisableAppCaller;
    }

    void setInstalled(boolean inst, int userId) {
        modifyUserState(userId).installed = inst;
    }

    boolean getInstalled(int userId) {
        return readUserState(userId).installed;
    }

    int getInstallReason(int userId) {
        return readUserState(userId).installReason;
    }

    void setInstallReason(int installReason, int userId) {
        modifyUserState(userId).installReason = installReason;
    }

    void setOverlayPaths(List<String> overlayPaths, int userId) {
        String[] strArr = null;
        PackageUserState modifyUserState = modifyUserState(userId);
        if (overlayPaths != null) {
            strArr = (String[]) overlayPaths.toArray(new String[overlayPaths.size()]);
        }
        modifyUserState.overlayPaths = strArr;
    }

    String[] getOverlayPaths(int userId) {
        return readUserState(userId).overlayPaths;
    }

    SparseArray<PackageUserState> getUserState() {
        return this.userState;
    }

    boolean isAnyInstalled(int[] users) {
        for (int user : users) {
            if (readUserState(user).installed) {
                return true;
            }
        }
        return false;
    }

    int[] queryInstalledUsers(int[] users, boolean installed) {
        int user;
        int i = 0;
        int num = 0;
        for (int user2 : users) {
            if (getInstalled(user2) == installed) {
                num++;
            }
        }
        int[] res = new int[num];
        num = 0;
        int length = users.length;
        while (i < length) {
            user2 = users[i];
            if (getInstalled(user2) == installed) {
                res[num] = user2;
                num++;
            }
            i++;
        }
        return res;
    }

    long getCeDataInode(int userId) {
        return readUserState(userId).ceDataInode;
    }

    void setCeDataInode(long ceDataInode, int userId) {
        modifyUserState(userId).ceDataInode = ceDataInode;
    }

    boolean getStopped(int userId) {
        return readUserState(userId).stopped;
    }

    void setStopped(boolean stop, int userId) {
        modifyUserState(userId).stopped = stop;
    }

    boolean getNotLaunched(int userId) {
        return readUserState(userId).notLaunched;
    }

    void setNotLaunched(boolean stop, int userId) {
        modifyUserState(userId).notLaunched = stop;
    }

    boolean getHidden(int userId) {
        return readUserState(userId).hidden;
    }

    void setHidden(boolean hidden, int userId) {
        modifyUserState(userId).hidden = hidden;
    }

    boolean getSuspended(int userId) {
        return readUserState(userId).suspended;
    }

    void setSuspended(boolean suspended, int userId) {
        modifyUserState(userId).suspended = suspended;
    }

    boolean getInstantApp(int userId) {
        return readUserState(userId).instantApp;
    }

    void setInstantApp(boolean instantApp, int userId) {
        modifyUserState(userId).instantApp = instantApp;
    }

    void setUserState(int userId, long ceDataInode, int enabled, boolean installed, boolean stopped, boolean notLaunched, boolean hidden, boolean suspended, boolean instantApp, String lastDisableAppCaller, ArraySet<String> enabledComponents, ArraySet<String> disabledComponents, int domainVerifState, int linkGeneration, int installReason) {
        PackageUserState state = modifyUserState(userId);
        state.ceDataInode = ceDataInode;
        state.enabled = enabled;
        state.installed = installed;
        state.stopped = stopped;
        state.notLaunched = notLaunched;
        state.hidden = hidden;
        state.suspended = suspended;
        state.lastDisableAppCaller = lastDisableAppCaller;
        state.enabledComponents = enabledComponents;
        state.disabledComponents = disabledComponents;
        state.domainVerificationStatus = domainVerifState;
        state.appLinkGeneration = linkGeneration;
        state.installReason = installReason;
        state.instantApp = instantApp;
    }

    ArraySet<String> getEnabledComponents(int userId) {
        return readUserState(userId).enabledComponents;
    }

    ArraySet<String> getDisabledComponents(int userId) {
        return readUserState(userId).disabledComponents;
    }

    void setEnabledComponents(ArraySet<String> components, int userId) {
        modifyUserState(userId).enabledComponents = components;
    }

    void setDisabledComponents(ArraySet<String> components, int userId) {
        modifyUserState(userId).disabledComponents = components;
    }

    void setEnabledComponentsCopy(ArraySet<String> components, int userId) {
        ArraySet arraySet = null;
        PackageUserState modifyUserState = modifyUserState(userId);
        if (components != null) {
            arraySet = new ArraySet(components);
        }
        modifyUserState.enabledComponents = arraySet;
    }

    void setDisabledComponentsCopy(ArraySet<String> components, int userId) {
        ArraySet arraySet = null;
        PackageUserState modifyUserState = modifyUserState(userId);
        if (components != null) {
            arraySet = new ArraySet(components);
        }
        modifyUserState.disabledComponents = arraySet;
    }

    PackageUserState modifyUserStateComponents(int userId, boolean disabled, boolean enabled) {
        PackageUserState state = modifyUserState(userId);
        if (disabled && state.disabledComponents == null) {
            state.disabledComponents = new ArraySet(1);
        }
        if (enabled && state.enabledComponents == null) {
            state.enabledComponents = new ArraySet(1);
        }
        return state;
    }

    void addDisabledComponent(String componentClassName, int userId) {
        modifyUserStateComponents(userId, true, false).disabledComponents.add(componentClassName);
    }

    void addEnabledComponent(String componentClassName, int userId) {
        modifyUserStateComponents(userId, false, true).enabledComponents.add(componentClassName);
    }

    boolean enableComponentLPw(String componentClassName, int userId) {
        PackageUserState state = modifyUserStateComponents(userId, false, true);
        return (state.disabledComponents != null ? state.disabledComponents.remove(componentClassName) : false) | state.enabledComponents.add(componentClassName);
    }

    boolean disableComponentLPw(String componentClassName, int userId) {
        PackageUserState state = modifyUserStateComponents(userId, true, false);
        return (state.enabledComponents != null ? state.enabledComponents.remove(componentClassName) : false) | state.disabledComponents.add(componentClassName);
    }

    boolean restoreComponentLPw(String componentClassName, int userId) {
        PackageUserState state = modifyUserStateComponents(userId, true, true);
        return (state.disabledComponents != null ? state.disabledComponents.remove(componentClassName) : false) | (state.enabledComponents != null ? state.enabledComponents.remove(componentClassName) : 0);
    }

    int getCurrentEnabledStateLPr(String componentName, int userId) {
        PackageUserState state = readUserState(userId);
        if (state.enabledComponents != null && state.enabledComponents.contains(componentName)) {
            return 1;
        }
        if (state.disabledComponents == null || !state.disabledComponents.contains(componentName)) {
            return 0;
        }
        return 2;
    }

    void removeUser(int userId) {
        this.userState.delete(userId);
    }

    public int[] getNotInstalledUserIds() {
        int i;
        int count = 0;
        int userStateCount = this.userState.size();
        for (i = 0; i < userStateCount; i++) {
            if (!((PackageUserState) this.userState.valueAt(i)).installed) {
                count++;
            }
        }
        if (count == 0) {
            return EMPTY_INT_ARRAY;
        }
        int[] excludedUserIds = new int[count];
        i = 0;
        int idx = 0;
        while (i < userStateCount) {
            int idx2;
            if (((PackageUserState) this.userState.valueAt(i)).installed) {
                idx2 = idx;
            } else {
                idx2 = idx + 1;
                excludedUserIds[idx] = this.userState.keyAt(i);
            }
            i++;
            idx = idx2;
        }
        return excludedUserIds;
    }

    IntentFilterVerificationInfo getIntentFilterVerificationInfo() {
        return this.verificationInfo;
    }

    void setIntentFilterVerificationInfo(IntentFilterVerificationInfo info) {
        this.verificationInfo = info;
    }

    long getDomainVerificationStatusForUser(int userId) {
        PackageUserState state = readUserState(userId);
        return ((long) state.appLinkGeneration) | (((long) state.domainVerificationStatus) << 32);
    }

    void setDomainVerificationStatusForUser(int status, int generation, int userId) {
        PackageUserState state = modifyUserState(userId);
        state.domainVerificationStatus = status;
        if (status == 2) {
            state.appLinkGeneration = generation;
        }
    }

    void clearDomainVerificationStatusForUser(int userId) {
        modifyUserState(userId).domainVerificationStatus = 0;
    }

    protected void writeUsersInfoToProto(ProtoOutputStream proto, long fieldId) {
        int count = this.userState.size();
        for (int i = 0; i < count; i++) {
            int installType;
            long userToken = proto.start(fieldId);
            PackageUserState state = (PackageUserState) this.userState.valueAt(i);
            proto.write(1112396529665L, this.userState.keyAt(i));
            if (state.instantApp) {
                installType = 2;
            } else if (state.installed) {
                installType = 1;
            } else {
                installType = 0;
            }
            proto.write(1168231104514L, installType);
            proto.write(1155346202627L, state.hidden);
            proto.write(1155346202628L, state.suspended);
            proto.write(1155346202629L, state.stopped);
            proto.write(1155346202630L, state.notLaunched ^ 1);
            proto.write(1168231104519L, state.enabled);
            proto.write(1159641169928L, state.lastDisableAppCaller);
            proto.end(userToken);
        }
    }
}
