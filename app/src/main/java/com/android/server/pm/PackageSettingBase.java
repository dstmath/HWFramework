package com.android.server.pm;

import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageUserState;
import android.util.ArraySet;
import android.util.SparseArray;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract class PackageSettingBase extends SettingBase {
    private static final PackageUserState DEFAULT_USER_STATE = null;
    static final int PKG_INSTALL_COMPLETE = 1;
    static final int PKG_INSTALL_INCOMPLETE = 0;
    List<String> childPackageNames;
    File codePath;
    String codePathString;
    String cpuAbiOverrideString;
    long firstInstallTime;
    boolean installPermissionsFixed;
    int installStatus;
    String installerPackageName;
    boolean isOrphaned;
    PackageKeySetData keySetData;
    long lastUpdateTime;
    @Deprecated
    String legacyNativeLibraryPathString;
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
    private final SparseArray<PackageUserState> userState;
    IntentFilterVerificationInfo verificationInfo;
    int versionCode;
    String volumeUuid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.PackageSettingBase.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.PackageSettingBase.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.PackageSettingBase.<clinit>():void");
    }

    PackageSettingBase(String name, String realName, File codePath, File resourcePath, String legacyNativeLibraryPathString, String primaryCpuAbiString, String secondaryCpuAbiString, String cpuAbiOverrideString, int pVersionCode, int pkgFlags, int pkgPrivateFlags, String parentPackageName, List<String> childPackageNames) {
        super(pkgFlags, pkgPrivateFlags);
        this.signatures = new PackageSignatures();
        this.keySetData = new PackageKeySetData();
        this.userState = new SparseArray();
        this.installStatus = PKG_INSTALL_COMPLETE;
        this.name = name;
        this.realName = realName;
        this.parentPackageName = parentPackageName;
        this.childPackageNames = childPackageNames != null ? new ArrayList(childPackageNames) : null;
        init(codePath, resourcePath, legacyNativeLibraryPathString, primaryCpuAbiString, secondaryCpuAbiString, cpuAbiOverrideString, pVersionCode);
    }

    PackageSettingBase(PackageSettingBase base) {
        super(base);
        this.signatures = new PackageSignatures();
        this.keySetData = new PackageKeySetData();
        this.userState = new SparseArray();
        this.installStatus = PKG_INSTALL_COMPLETE;
        this.name = base.name;
        this.realName = base.realName;
        this.codePath = base.codePath;
        this.codePathString = base.codePathString;
        this.resourcePath = base.resourcePath;
        this.resourcePathString = base.resourcePathString;
        this.legacyNativeLibraryPathString = base.legacyNativeLibraryPathString;
        this.primaryCpuAbiString = base.primaryCpuAbiString;
        this.secondaryCpuAbiString = base.secondaryCpuAbiString;
        this.cpuAbiOverrideString = base.cpuAbiOverrideString;
        this.timeStamp = base.timeStamp;
        this.firstInstallTime = base.firstInstallTime;
        this.lastUpdateTime = base.lastUpdateTime;
        this.versionCode = base.versionCode;
        this.uidError = base.uidError;
        this.signatures = new PackageSignatures(base.signatures);
        this.installPermissionsFixed = base.installPermissionsFixed;
        this.userState.clear();
        for (int i = 0; i < base.userState.size(); i += PKG_INSTALL_COMPLETE) {
            this.userState.put(base.userState.keyAt(i), new PackageUserState((PackageUserState) base.userState.valueAt(i)));
        }
        this.installStatus = base.installStatus;
        this.origPackage = base.origPackage;
        this.installerPackageName = base.installerPackageName;
        this.isOrphaned = base.isOrphaned;
        this.volumeUuid = base.volumeUuid;
        this.keySetData = new PackageKeySetData(base.keySetData);
        this.parentPackageName = base.parentPackageName;
        this.childPackageNames = base.childPackageNames != null ? new ArrayList(base.childPackageNames) : null;
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

    public void copyFrom(PackageSettingBase base) {
        this.mPermissionsState.copyFrom(base.mPermissionsState);
        this.primaryCpuAbiString = base.primaryCpuAbiString;
        this.secondaryCpuAbiString = base.secondaryCpuAbiString;
        this.cpuAbiOverrideString = base.cpuAbiOverrideString;
        this.timeStamp = base.timeStamp;
        this.firstInstallTime = base.firstInstallTime;
        this.lastUpdateTime = base.lastUpdateTime;
        this.signatures = base.signatures;
        this.installPermissionsFixed = base.installPermissionsFixed;
        this.userState.clear();
        for (int i = 0; i < base.userState.size(); i += PKG_INSTALL_COMPLETE) {
            this.userState.put(base.userState.keyAt(i), (PackageUserState) base.userState.valueAt(i));
        }
        this.installStatus = base.installStatus;
        this.keySetData = base.keySetData;
        this.verificationInfo = base.verificationInfo;
        this.installerPackageName = base.installerPackageName;
        this.volumeUuid = base.volumeUuid;
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
        if (state != null) {
            return state;
        }
        return DEFAULT_USER_STATE;
    }

    void setEnabled(int state, int userId, String callingPackage) {
        PackageUserState st = modifyUserState(userId);
        st.enabled = state;
        st.lastDisableAppCaller = callingPackage;
    }

    int getEnabled(int userId) {
        return readUserState(userId).enabled;
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

    boolean isAnyInstalled(int[] users) {
        int length = users.length;
        for (int i = 0; i < length; i += PKG_INSTALL_COMPLETE) {
            if (readUserState(users[i]).installed) {
                return true;
            }
        }
        return false;
    }

    int[] queryInstalledUsers(int[] users, boolean installed) {
        int i;
        int i2 = 0;
        int num = 0;
        int length = users.length;
        for (i = 0; i < length; i += PKG_INSTALL_COMPLETE) {
            if (getInstalled(users[i]) == installed) {
                num += PKG_INSTALL_COMPLETE;
            }
        }
        int[] res = new int[num];
        num = 0;
        i = users.length;
        while (i2 < i) {
            int user = users[i2];
            if (getInstalled(user) == installed) {
                res[num] = user;
                num += PKG_INSTALL_COMPLETE;
            }
            i2 += PKG_INSTALL_COMPLETE;
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

    boolean getBlockUninstall(int userId) {
        return readUserState(userId).blockUninstall;
    }

    void setBlockUninstall(boolean blockUninstall, int userId) {
        modifyUserState(userId).blockUninstall = blockUninstall;
    }

    void setUserState(int userId, long ceDataInode, int enabled, boolean installed, boolean stopped, boolean notLaunched, boolean hidden, boolean suspended, String lastDisableAppCaller, ArraySet<String> enabledComponents, ArraySet<String> disabledComponents, boolean blockUninstall, int domainVerifState, int linkGeneration) {
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
        state.blockUninstall = blockUninstall;
        state.domainVerificationStatus = domainVerifState;
        state.appLinkGeneration = linkGeneration;
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
            state.disabledComponents = new ArraySet(PKG_INSTALL_COMPLETE);
        }
        if (enabled && state.enabledComponents == null) {
            state.enabledComponents = new ArraySet(PKG_INSTALL_COMPLETE);
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
            return PKG_INSTALL_COMPLETE;
        }
        if (state.disabledComponents == null || !state.disabledComponents.contains(componentName)) {
            return 0;
        }
        return 2;
    }

    void removeUser(int userId) {
        this.userState.delete(userId);
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
}
