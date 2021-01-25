package ohos.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.StringUtils;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ShellInfo;
import ohos.bundlemgr.PackageManagerAdapter;
import ohos.bundlemgr.ShortcutParam;
import ohos.global.resource.ResourceManager;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.security.permission.PermissionDef;
import ohos.security.permission.PermissionGroupDef;
import ohos.sysability.samgr.SysAbilityManager;

public class BundleManager implements IBundleManager {
    private static final String ACTION_QUERY_ABILITY_NEED_UPDATE = "ability.intent.QUERY_ABILITY_NEED_UPDATE";
    private static final int ATTACH_APPLICATION = 16;
    private static final int CANCEL_DOWNLOAD = 43;
    private static final int CHECK_SIGNATURES = 29;
    private static final int DEATH_RECIPIENT_FLAG = 0;
    private static final int DOWNLOAD_AND_INSTALL = 41;
    private static final int DOWNLOAD_AND_INSTALL_WITH_PARAM = 50;
    private static final int ERROR_CODE_BUNDLE_MANAGER_OFFSET = 8519680;
    private static final int GET_ABILITY_DIRECT_INFOS = 33;
    private static final int GET_ABILITY_INFO_BY_NAME = 15;
    private static final int GET_APPLICATION_INFO = 17;
    private static final int GET_APPLICATION_INFOS = 28;
    private static final int GET_APP_FEATURE = 39;
    private static final int GET_BUNDLE_INFO = 12;
    private static final int GET_BUNDLE_INFOS = 11;
    private static final int GET_BUNDLE_INSTALLER = 14;
    private static final int GET_BUNDLE_NAMES_BY_UID = 20;
    private static final int GET_NAME_FOR_UID = 30;
    private static final int GET_UID_BY_BUNDLE_NAME = 10;
    private static final Object INSTANCE_LOCK = new Object();
    private static final int INVALID_UID = -1;
    private static final int MAX_ABILITY_SIZE = 20;
    private static final int MAX_BUNDLE_SIZE = 1000;
    private static final int MAX_BUNDLE_SIZE_FOR_UID = 50;
    private static final int MAX_COMMON_EVENT_SIZE = 1000;
    private static final int MAX_SHORTCUT_SIZE = 4;
    private static final int NOTIFY_PERMISSIONS_CHANGED = 21;
    private static final String PAGE_SHELL_SUFFIX = "ShellActivity";
    private static final int PERMISSION_DENIED_ERR_CODE = 8519811;
    private static final String PROVIDER_SHELL_SUFFIX = "ShellProvider";
    private static final int QUERY_ABILITY_BY_FLAGS = 19;
    private static final int QUERY_ABILITY_BY_INTENT = 5;
    private static final int QUERY_COMMON_EVENT = 38;
    private static final int QUERY_COMMON_EVENT_BY_UID = 37;
    private static final int QUERY_LAUNCH_INTENT = 32;
    private static final int REGISTER_ALL_PERMISSIONS_CHANGED = 22;
    private static final int REGISTER_PERMISSIONS_CHANGED = 23;
    private static final String SERVICE_SHELL_SUFFIX = "ShellService";
    private static final int SHOW_ERROR_MESSAGE = 42;
    private static final int START_SHORTCUT = 34;
    private static final int UNREGISTER_PERMISSIONS_CHANGED = 24;
    private static volatile BundleManager instance;
    private static volatile boolean resetFlag = false;
    private Context appContext;
    private IBundleInstaller installer;
    private IRemoteObject remote;

    public static BundleManager getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    IRemoteObject sysAbility = SysAbilityManager.getSysAbility(401);
                    if (sysAbility == null) {
                        AppLog.e("BundleManager getInstance failed, remote is null", new Object[0]);
                        return null;
                    }
                    if (!sysAbility.addDeathRecipient(new BundleManagerDeathRecipient(), 0)) {
                        AppLog.w("BundleManager register BundleManagerDeathRecipient failed", new Object[0]);
                    }
                    instance = new BundleManager(sysAbility);
                }
            }
        }
        return instance;
    }

    protected static void resetFlag() {
        resetFlag = true;
    }

    public void resetRemoteObject() {
        synchronized (INSTANCE_LOCK) {
            if (resetFlag) {
                IRemoteObject sysAbility = SysAbilityManager.getSysAbility(401);
                if (sysAbility == null) {
                    AppLog.e("BundleManager reset remoteObject failed, remote is null", new Object[0]);
                    return;
                }
                if (!sysAbility.addDeathRecipient(new BundleManagerDeathRecipient(), 0)) {
                    AppLog.w("BundleManager register BundleManagerDeathRecipient failed", new Object[0]);
                }
                this.remote = sysAbility;
                resetFlag = false;
            }
        }
    }

    public BundleManager(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.bundle.IBundleManager
    public List<AbilityInfo> queryAbilityByIntent(Intent intent) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::queryAbilityByIntent remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            obtain.writeSequenceable(intent);
            if (!obtain.writeInt(0) || !obtain.writeInt(0)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(5, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::queryAbilityByIntent sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt != 0) {
                    AppLog.e("BundleManager::queryAbilityByIntent failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                int readInt2 = obtain2.readInt();
                if (readInt2 > 20 || readInt2 < 0) {
                    AppLog.e("BundleManager::queryAbilityByIntent failed, wrong length", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                ArrayList arrayList = new ArrayList();
                for (int i = 0; i < readInt2; i++) {
                    AbilityInfo abilityInfo = new AbilityInfo();
                    if (!obtain2.readSequenceable(abilityInfo)) {
                        reclaimParcel(obtain, obtain2);
                        return null;
                    }
                    arrayList.add(abilityInfo);
                }
                reclaimParcel(obtain, obtain2);
                return arrayList;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public List<CommonEventInfo> queryCommonEventInfos(int i) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::queryCommonEventInfos remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            obtain.writeInt(i);
            if (!this.remote.sendRequest(37, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::queryCommonEventInfos sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            }
            int readInt = obtain2.readInt();
            if (readInt != 0) {
                AppLog.e("BundleManager::queryCommonEventInfos failed, result is %{public}d", Integer.valueOf(readInt));
                reclaimParcel(obtain, obtain2);
                return null;
            }
            int readInt2 = obtain2.readInt();
            if (readInt2 > 1000 || readInt2 < 0) {
                AppLog.e("BundleManager::queryCommonEventInfos failed, wrong length", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            }
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < readInt2; i2++) {
                CommonEventInfo commonEventInfo = new CommonEventInfo();
                if (!obtain2.readSequenceable(commonEventInfo)) {
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                arrayList.add(commonEventInfo);
            }
            reclaimParcel(obtain, obtain2);
            return arrayList;
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public List<CommonEventInfo> queryCommonEventInfos() throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::queryCommonEventInfos remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!this.remote.sendRequest(38, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::queryCommonEventInfos sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            }
            int readInt = obtain2.readInt();
            if (readInt != 0) {
                AppLog.e("BundleManager::queryCommonEventInfos failed, result is %{public}d", Integer.valueOf(readInt));
                reclaimParcel(obtain, obtain2);
                return null;
            }
            int readInt2 = obtain2.readInt();
            if (readInt2 > 1000 || readInt2 < 0) {
                AppLog.e("BundleManager::queryCommonEventInfos failed, wrong length", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            }
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < readInt2; i++) {
                CommonEventInfo commonEventInfo = new CommonEventInfo();
                if (!obtain2.readSequenceable(commonEventInfo)) {
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                arrayList.add(commonEventInfo);
            }
            reclaimParcel(obtain, obtain2);
            return arrayList;
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public AbilityInfo getAbilityByShell(ShellInfo shellInfo) {
        if (shellInfo == null) {
            AppLog.e("BundleManager::getAbilityByShell shellInfo is null", new Object[0]);
            return null;
        }
        String packageName = shellInfo.getPackageName();
        if (packageName == null || packageName.isEmpty()) {
            AppLog.e("BundleManager::getAbilityByShell packageName of shellInfo is null or empty", new Object[0]);
            return null;
        }
        String name = shellInfo.getName();
        if (name == null || name.isEmpty()) {
            AppLog.e("BundleManager::getAbilityByShell className of shellInfo is null or empty", new Object[0]);
            return null;
        }
        AbilityInfo abilityInfo = new AbilityInfo();
        abilityInfo.setBundleName(packageName);
        if (shellInfo.getType() == ShellInfo.ShellType.ACTIVITY) {
            int lastIndexOf = name.lastIndexOf("ShellActivity");
            if (lastIndexOf == -1) {
                AppLog.e("BundleManager::getAbilityByShell invalid activity className", new Object[0]);
                return null;
            }
            abilityInfo.setClassName(name.substring(0, lastIndexOf));
            abilityInfo.setType(AbilityInfo.AbilityType.PAGE);
        } else if (shellInfo.getType() == ShellInfo.ShellType.SERVICE) {
            int lastIndexOf2 = name.lastIndexOf("ShellService");
            if (lastIndexOf2 == -1) {
                AppLog.e("BundleManager::getAbilityByShell invalid service className", new Object[0]);
                return null;
            }
            abilityInfo.setClassName(name.substring(0, lastIndexOf2));
            abilityInfo.setType(AbilityInfo.AbilityType.SERVICE);
        } else if (shellInfo.getType() == ShellInfo.ShellType.PROVIDER) {
            int lastIndexOf3 = name.lastIndexOf("ShellProvider");
            if (lastIndexOf3 == -1) {
                AppLog.e("BundleManager::getAbilityByShell invalid provider className", new Object[0]);
                return null;
            }
            abilityInfo.setClassName(name.substring(0, lastIndexOf3));
            abilityInfo.setType(AbilityInfo.AbilityType.PROVIDER);
        } else {
            AppLog.e("BundleManager::getAbilityByShell unknown shell type", new Object[0]);
            return null;
        }
        return abilityInfo;
    }

    @Override // ohos.bundle.IBundleManager
    public BundleInfo getBundleInfo(String str, int i) throws RemoteException {
        BundleInfo bundleInfoFromBms = getBundleInfoFromBms(str, i);
        if (bundleInfoFromBms != null || (i & -65536) != -65536) {
            return bundleInfoFromBms;
        }
        boolean z = true;
        if ((i & 1) != 1) {
            z = false;
        }
        return PackageManagerAdapter.getInstance().getBundleInfoFromPms(str, z);
    }

    private BundleInfo getBundleInfoFromBms(String str, int i) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getBundleInfoFromBms remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!obtain.writeString(str)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!obtain.writeInt(i)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(12, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getBundleInfoFromBms sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt == PERMISSION_DENIED_ERR_CODE) {
                    throw new SecurityException("permission denied");
                } else if (readInt != 0) {
                    AppLog.e("BundleManager::getBundleInfoFromBms failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                } else {
                    BundleInfo bundleInfo = new BundleInfo();
                    if (!obtain2.readSequenceable(bundleInfo)) {
                        AppLog.e("BundleManager::getBundleInfoFromBms read sequenceable failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                        return null;
                    }
                    reclaimParcel(obtain, obtain2);
                    return bundleInfo;
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public IBundleInstaller getBundleInstaller() throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getBundleInstaller remote is null", new Object[0]);
            return null;
        }
        IBundleInstaller iBundleInstaller = this.installer;
        if (iBundleInstaller != null) {
            return iBundleInstaller;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!this.remote.sendRequest(14, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getBundleInstaller sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            }
            int readInt = obtain2.readInt();
            if (readInt == PERMISSION_DENIED_ERR_CODE) {
                throw new SecurityException("permission denied");
            } else if (readInt != 0) {
                AppLog.e("BundleManager::getBundleInstaller failed, result is %{public}d", Integer.valueOf(readInt));
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                IRemoteObject readRemoteObject = obtain2.readRemoteObject();
                if (readRemoteObject == null) {
                    AppLog.e("BundleManager::getBundleInstaller service return installer failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                this.installer = new BundleInstaller(readRemoteObject);
                this.installer.setContext(this.appContext);
                IBundleInstaller iBundleInstaller2 = this.installer;
                reclaimParcel(obtain, obtain2);
                return iBundleInstaller2;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public AbilityInfo getAbilityInfo(String str, String str2) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getAbilityInfo remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!obtain.writeString(str)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!obtain.writeString(str2)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(15, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getAbilityInfo sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt != 0) {
                    AppLog.e("BundleManager::getAbilityInfo failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                AbilityInfo abilityInfo = new AbilityInfo();
                if (!obtain2.readSequenceable(abilityInfo)) {
                    AppLog.e("BundleManager::getAbilityInfo read sequenceable failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                reclaimParcel(obtain, obtain2);
                return abilityInfo;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public BundleInfo attachApplication(String str, IRemoteObject iRemoteObject) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getAbilityInfo remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!obtain.writeString(str)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!obtain.writeRemoteObject(iRemoteObject)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(16, obtain, obtain2, messageOption)) {
                AppLog.w("BundleManager::attachApplication transact failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (obtain2.readInt() != 0) {
                AppLog.w("BundleManager::attachApplication failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                BundleInfo bundleInfo = new BundleInfo();
                if (!obtain2.readSequenceable(bundleInfo)) {
                    AppLog.w("BundleManager::attachApplication read sequenceable failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                reclaimParcel(obtain, obtain2);
                return bundleInfo;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public ApplicationInfo getApplicationInfo(String str, int i, int i2) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getApplicationInfo remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!obtain.writeString(str)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!obtain.writeInt(i)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!obtain.writeInt(i2)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(17, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getApplicationInfo sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt == PERMISSION_DENIED_ERR_CODE) {
                    throw new SecurityException("permission denied");
                } else if (readInt != 0) {
                    AppLog.e("BundleManager::getApplicationInfo failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                } else {
                    ApplicationInfo applicationInfo = new ApplicationInfo();
                    if (!obtain2.readSequenceable(applicationInfo)) {
                        AppLog.e("BundleManager::getApplicationInfo read sequenceable failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                        return null;
                    }
                    reclaimParcel(obtain, obtain2);
                    return applicationInfo;
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public int checkPermission(String str, String str2) {
        if (str2 != null && !str2.isEmpty() && str != null && !str.isEmpty()) {
            return PackageManagerAdapter.getInstance().checkPermission(str2, str);
        }
        AppLog.e("BundleManager::checkPermission permissionName or bundleName is null or empty", new Object[0]);
        return -1;
    }

    @Override // ohos.bundle.IBundleManager
    public List<AbilityInfo> queryAbilityByIntent(Intent intent, int i, int i2) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::queryAbilityByIntent and flags remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            obtain.writeSequenceable(intent);
            if (!obtain.writeInt(i) || !obtain.writeInt(i2)) {
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(19, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::queryAbilityByIntent and flags sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt == PERMISSION_DENIED_ERR_CODE) {
                    throw new SecurityException("permission denied");
                } else if (readInt != 0) {
                    AppLog.e("BundleManager::queryAbilityByIntent and flags failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                } else {
                    int readInt2 = obtain2.readInt();
                    if (readInt2 > 20 || readInt2 < 0) {
                        AppLog.e("BundleManager::queryAbilityByIntent failed, wrong length", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                        return null;
                    }
                    ArrayList arrayList = new ArrayList();
                    for (int i3 = 0; i3 < readInt2; i3++) {
                        AbilityInfo abilityInfo = new AbilityInfo();
                        if (!obtain2.readSequenceable(abilityInfo)) {
                            reclaimParcel(obtain, obtain2);
                            return null;
                        }
                        arrayList.add(abilityInfo);
                    }
                    reclaimParcel(obtain, obtain2);
                    return arrayList;
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public List<BundleInfo> getBundleInfos(int i) throws RemoteException {
        List<BundleInfo> bundleInfosFromBms = getBundleInfosFromBms(i);
        if ((i & -65536) == -65536) {
            boolean z = true;
            if ((i & 1) != 1) {
                z = false;
            }
            List<BundleInfo> bundleInfosFromPms = PackageManagerAdapter.getInstance().getBundleInfosFromPms(z);
            if (bundleInfosFromPms != null) {
                bundleInfosFromBms.addAll(bundleInfosFromPms);
            }
        }
        return bundleInfosFromBms;
    }

    private List<BundleInfo> getBundleInfosFromBms(int i) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getBundleInfosFromBms remote is null", new Object[0]);
            return new ArrayList();
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return new ArrayList();
            }
            if (!obtain.writeInt(i)) {
                ArrayList arrayList = new ArrayList();
                reclaimParcel(obtain, obtain2);
                return arrayList;
            } else if (!this.remote.sendRequest(11, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getBundleInfosFromBms sendRequest failed", new Object[0]);
                ArrayList arrayList2 = new ArrayList();
                reclaimParcel(obtain, obtain2);
                return arrayList2;
            } else {
                int readInt = obtain2.readInt();
                if (readInt == PERMISSION_DENIED_ERR_CODE) {
                    throw new SecurityException("permission denied");
                } else if (readInt != 0) {
                    AppLog.e("BundleManager::getBundleInfosFromBms failed, result is %{public}d", Integer.valueOf(readInt));
                    ArrayList arrayList3 = new ArrayList();
                    reclaimParcel(obtain, obtain2);
                    return arrayList3;
                } else {
                    int readInt2 = obtain2.readInt();
                    if (readInt2 > 1000 || readInt2 < 0) {
                        AppLog.e("BundleManager::getBundleInfosFromBms failed, wrong length", new Object[0]);
                        ArrayList arrayList4 = new ArrayList();
                        reclaimParcel(obtain, obtain2);
                        return arrayList4;
                    }
                    ArrayList arrayList5 = new ArrayList();
                    for (int i2 = 0; i2 < readInt2; i2++) {
                        BundleInfo bundleInfo = new BundleInfo();
                        if (!obtain2.readSequenceable(bundleInfo)) {
                            ArrayList arrayList6 = new ArrayList();
                            reclaimParcel(obtain, obtain2);
                            return arrayList6;
                        }
                        arrayList5.add(bundleInfo);
                    }
                    reclaimParcel(obtain, obtain2);
                    return arrayList5;
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public int getUidByBundleName(String str, int i) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::GetUidByBundleName remote is null", new Object[0]);
            return -1;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return -1;
            }
            if (!obtain.writeString(str) || !obtain.writeInt(i)) {
                AppLog.e("BundleManager::GetUidByBundleName write parcel failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return -1;
            } else if (!this.remote.sendRequest(10, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::GetUidByBundleName sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return -1;
            } else {
                int readInt = obtain2.readInt();
                if (readInt == PERMISSION_DENIED_ERR_CODE) {
                    throw new SecurityException("permission denied");
                } else if (readInt != 0) {
                    AppLog.e("BundleManager::GetUidByBundleName failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return -1;
                } else {
                    int readInt2 = obtain2.readInt();
                    reclaimParcel(obtain, obtain2);
                    return readInt2;
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public List<String> getBundlesForUid(int i) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getBundlesForUid remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!obtain.writeInt(i)) {
                AppLog.e("BundleManager::getBundlesForUid write parcel failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(20, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getBundlesForUid sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt == PERMISSION_DENIED_ERR_CODE) {
                    throw new SecurityException("permission denied");
                } else if (readInt != 0) {
                    AppLog.e("BundleManager::getBundlesForUid failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                } else {
                    int readInt2 = obtain2.readInt();
                    if (readInt2 > 50 || readInt2 < 0) {
                        AppLog.e("BundleManager::getBundlesForUid failed, wrong length", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                        return null;
                    }
                    ArrayList arrayList = new ArrayList();
                    for (int i2 = 0; i2 < readInt2; i2++) {
                        arrayList.add(obtain2.readString());
                    }
                    reclaimParcel(obtain, obtain2);
                    return arrayList;
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void notifyPermissionsChanged(int i) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::notifyPermissionsChanged remote is null", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                if (!obtain.writeInt(i)) {
                    AppLog.e("BundleManager::notifyPermissionsChanged write parcel failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else if (!this.remote.sendRequest(21, obtain, obtain2, messageOption)) {
                    AppLog.e("BundleManager::notifyPermissionsChanged sendRequest failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else {
                    int readInt = obtain2.readInt();
                    if (readInt == PERMISSION_DENIED_ERR_CODE) {
                        throw new SecurityException("permission denied");
                    } else if (readInt != 0) {
                        AppLog.w("BundleManager::notifyPermissionsChanged failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                    } else {
                        reclaimParcel(obtain, obtain2);
                    }
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void registerAllPermissionsChanged(IRemoteObject iRemoteObject) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::registerAllPermissionsChanged remote is null", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                if (!obtain.writeRemoteObject(iRemoteObject)) {
                    AppLog.e("BundleManager::registerAllPermissionsChanged write parcel failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else if (!this.remote.sendRequest(22, obtain, obtain2, messageOption)) {
                    AppLog.e("BundleManager::registerAllPermissionsChanged transact failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else {
                    int readInt = obtain2.readInt();
                    if (readInt == PERMISSION_DENIED_ERR_CODE) {
                        throw new SecurityException("permission denied");
                    } else if (readInt != 0) {
                        AppLog.e("BundleManager::registerAllPermissionsChanged failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                    } else {
                        reclaimParcel(obtain, obtain2);
                    }
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void registerPermissionsChanged(int[] iArr, IRemoteObject iRemoteObject) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::registerPermissionsChanged remote is null", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                if (!obtain.writeIntArray(iArr) || !obtain.writeRemoteObject(iRemoteObject)) {
                    AppLog.e("BundleManager::registerPermissionsChanged write parcel failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else if (!this.remote.sendRequest(23, obtain, obtain2, messageOption)) {
                    AppLog.e("BundleManager::registerPermissionsChanged transact failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else {
                    int readInt = obtain2.readInt();
                    if (readInt == PERMISSION_DENIED_ERR_CODE) {
                        throw new SecurityException("permission denied");
                    } else if (readInt != 0) {
                        AppLog.e("BundleManager::registerPermissionsChanged failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                    } else {
                        reclaimParcel(obtain, obtain2);
                    }
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void unregisterPermissionsChanged(IRemoteObject iRemoteObject) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::unRegisterPermissionsChanged remote is null", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                if (!obtain.writeRemoteObject(iRemoteObject)) {
                    AppLog.e("BundleManager::unRegisterPermissionsChanged write remote object failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else if (!this.remote.sendRequest(24, obtain, obtain2, messageOption)) {
                    AppLog.e("BundleManager::unRegisterPermissionsChanged transact failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else {
                    int readInt = obtain2.readInt();
                    if (readInt == PERMISSION_DENIED_ERR_CODE) {
                        throw new SecurityException("permission denied");
                    } else if (readInt != 0) {
                        AppLog.e("BundleManager::unRegisterPermissionsChanged failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                    } else {
                        reclaimParcel(obtain, obtain2);
                    }
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.bundle.IBundleManager
    public List<ApplicationInfo> getApplicationInfos(int i, int i2) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getApplicationInfos remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR) && obtain.writeInt(i)) {
                if (obtain.writeInt(i2)) {
                    if (!this.remote.sendRequest(28, obtain, obtain2, messageOption)) {
                        AppLog.e("BundleManager::getApplicationInfos sendRequest failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                        return null;
                    }
                    int readInt = obtain2.readInt();
                    if (readInt == PERMISSION_DENIED_ERR_CODE) {
                        throw new SecurityException("permission denied");
                    } else if (readInt != 0) {
                        AppLog.e("BundleManager::getApplicationInfos failed, result is %{public}d", Integer.valueOf(readInt));
                        reclaimParcel(obtain, obtain2);
                        return null;
                    } else {
                        int readInt2 = obtain2.readInt();
                        if (readInt2 > 1000 || readInt2 < 0) {
                            AppLog.e("BundleManager::getApplicationInfos failed, wrong length", new Object[0]);
                            reclaimParcel(obtain, obtain2);
                            return null;
                        }
                        ArrayList arrayList = new ArrayList();
                        for (int i3 = 0; i3 < readInt2; i3++) {
                            ApplicationInfo applicationInfo = new ApplicationInfo();
                            if (!obtain2.readSequenceable(applicationInfo)) {
                                AppLog.e("BundleManager::getApplicationInfos read parcel failed", new Object[0]);
                                reclaimParcel(obtain, obtain2);
                                return null;
                            }
                            arrayList.add(applicationInfo);
                        }
                        reclaimParcel(obtain, obtain2);
                        return arrayList;
                    }
                }
            }
            AppLog.e("BundleManager::getApplicationInfos write parcel failed", new Object[0]);
            return null;
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public List<String> getSystemAvailableCapabilities() {
        return PackageManagerAdapter.getInstance().getSystemAvailableFeatures();
    }

    @Override // ohos.bundle.IBundleManager
    public boolean hasSystemCapability(String str) {
        if (str != null && !str.isEmpty()) {
            return PackageManagerAdapter.getInstance().hasSystemFeature(str);
        }
        AppLog.e("BundleManager::hasSystemCapability capability name is null or empty", new Object[0]);
        return false;
    }

    @Override // ohos.bundle.IBundleManager
    public int[] getBundleGids(String str) {
        int[] iArr = new int[0];
        if (str != null && !str.isEmpty()) {
            return PackageManagerAdapter.getInstance().getPackageGids(str);
        }
        AppLog.e("BundleManager::getBundleGids bunleName is null or empty", new Object[0]);
        return iArr;
    }

    @Override // ohos.bundle.IBundleManager
    public int checkSignatures(String str, String str2) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::checkSignatures remote is null", new Object[0]);
            return 1;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return 1;
            }
            if (!obtain.writeString(str) || !obtain.writeString(str2)) {
                AppLog.e("BundleManager::checkSignatures write parcel failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return 1;
            } else if (!this.remote.sendRequest(29, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::checkSignatures sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return 1;
            } else {
                int readInt = obtain2.readInt();
                if (!(readInt == 0 || readInt == 2)) {
                    readInt = 1;
                }
                reclaimParcel(obtain, obtain2);
                return readInt;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public BundleInfo getBundleArchiveInfo(String str, int i) {
        return ParseProfile.parse(this.appContext, str, i);
    }

    @Override // ohos.bundle.IBundleManager
    public String getNameForUid(int i) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getNameForUid remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!obtain.writeInt(i)) {
                AppLog.e("BundleManager::getNameForUid write parcel failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(30, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getNameForUid sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt != 0) {
                    AppLog.e("BundleManager::getNameForUid failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                String readString = obtain2.readString();
                reclaimParcel(obtain, obtain2);
                return readString;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public boolean isSafeMode() {
        return PackageManagerAdapter.getInstance().isSafeMode();
    }

    @Override // ohos.bundle.IBundleManager
    public Intent getLaunchIntentForBundle(String str) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getLaunchIntentForBundle remote is null", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!obtain.writeString(str)) {
                AppLog.e("BundleManager::getLaunchIntentForBundle write parcel failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(32, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getLaunchIntentForBundle sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt == PERMISSION_DENIED_ERR_CODE) {
                    throw new SecurityException("permission denied");
                } else if (readInt != 0) {
                    AppLog.e("BundleManager::getLaunchIntentForBundle readInt failed, ret is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                } else {
                    Intent intent = new Intent();
                    if (!obtain2.readSequenceable(intent)) {
                        AppLog.e("BundleManager::getLaunchIntentForBundle readSequenceable failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                        return null;
                    }
                    reclaimParcel(obtain, obtain2);
                    return intent;
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void setApplicationEnabled(String str, boolean z) throws IllegalArgumentException {
        if (str == null || str.isEmpty()) {
            AppLog.e("BundleManager::SetApplicationEnabled bundleName is null or empty", new Object[0]);
        } else {
            PackageManagerAdapter.getInstance().setApplicationEnabled(str, z);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void setAbilityEnabled(AbilityInfo abilityInfo, boolean z) throws IllegalArgumentException {
        if (abilityInfo == null) {
            AppLog.e("BundleManager::setAbilityEnabled abilityInfo is null", new Object[0]);
            return;
        }
        PackageManagerAdapter.getInstance().setComponentEnabled(abilityInfo.getBundleName(), getShellClassName(abilityInfo), z);
    }

    @Override // ohos.bundle.IBundleManager
    public boolean isApplicationEnabled(String str) throws IllegalArgumentException {
        if (str != null && !str.isEmpty()) {
            return PackageManagerAdapter.getInstance().isPackageEnabled(str);
        }
        AppLog.e("BundleManager::isApplicationEnabled bundleName is null or empty", new Object[0]);
        throw new IllegalArgumentException("bundleName is null or empty");
    }

    @Override // ohos.bundle.IBundleManager
    public boolean isAbilityEnabled(AbilityInfo abilityInfo) throws IllegalArgumentException {
        if (abilityInfo != null) {
            return PackageManagerAdapter.getInstance().isComponentEnabled(abilityInfo.getBundleName(), getShellClassName(abilityInfo));
        }
        AppLog.e("BundleManager::isAbilityEnabled abilityInfo is null", new Object[0]);
        throw new IllegalArgumentException("abilityInfo is null");
    }

    @Override // ohos.bundle.IBundleManager
    public List<ShortcutInfo> getShortcutInfos(String str) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getShortcutInfos remote is null", new Object[0]);
            return new ArrayList();
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return null;
            }
            if (!obtain.writeString(str)) {
                AppLog.e("BundleManager::getShortcutInfos write bundleName failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else if (!this.remote.sendRequest(33, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getShortcutInfos sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return null;
            } else {
                int readInt = obtain2.readInt();
                if (readInt == PERMISSION_DENIED_ERR_CODE) {
                    throw new SecurityException("permission denied");
                } else if (readInt != 0) {
                    AppLog.e("BundleManager::getShortcutInfos failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return null;
                } else {
                    int readInt2 = obtain2.readInt();
                    if (readInt2 > 4 || readInt2 < 0) {
                        AppLog.e("BundleManager::getShortcutInfos failed, wrong length is %{public}d", Integer.valueOf(readInt2));
                        reclaimParcel(obtain, obtain2);
                        return null;
                    }
                    ArrayList arrayList = new ArrayList();
                    for (int i = 0; i < readInt2; i++) {
                        ShortcutInfo shortcutInfo = new ShortcutInfo();
                        if (!obtain2.readSequenceable(shortcutInfo)) {
                            AppLog.e("BundleManager::getShortcutInfos readSequenceable failed", new Object[0]);
                            reclaimParcel(obtain, obtain2);
                            return null;
                        }
                        arrayList.add(shortcutInfo);
                    }
                    reclaimParcel(obtain, obtain2);
                    return arrayList;
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void startShortcut(String str, String str2) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::startShortcut remote is null", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                if (!obtain.writeString(str)) {
                    reclaimParcel(obtain, obtain2);
                } else if (!obtain.writeString(str2)) {
                    reclaimParcel(obtain, obtain2);
                } else if (!this.remote.sendRequest(34, obtain, obtain2, messageOption)) {
                    AppLog.e("BundleManager::startShortcut sendRequest failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                } else {
                    int readInt = obtain2.readInt();
                    if (readInt == PERMISSION_DENIED_ERR_CODE) {
                        throw new SecurityException("permission denied");
                    } else if (readInt != 0) {
                        AppLog.e("BundleManager::startShortcut failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                    } else {
                        reclaimParcel(obtain, obtain2);
                    }
                }
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    private void reclaimParcel(MessageParcel messageParcel, MessageParcel messageParcel2) {
        messageParcel.reclaim();
        messageParcel2.reclaim();
    }

    private String getShellClassName(AbilityInfo abilityInfo) {
        if (abilityInfo == null) {
            AppLog.e("BundleManager::getShellClassName abilityInfo is null", new Object[0]);
            return "";
        }
        AbilityInfo.AbilityType type = abilityInfo.getType();
        if (type == AbilityInfo.AbilityType.PAGE) {
            return abilityInfo.getClassName() + "ShellActivity";
        } else if (type == AbilityInfo.AbilityType.SERVICE) {
            return abilityInfo.getClassName() + "ShellService";
        } else if (type == AbilityInfo.AbilityType.PROVIDER) {
            return abilityInfo.getClassName() + "ShellProvider";
        } else {
            AppLog.e("BundleManager::getShellClassName unknown ability type", new Object[0]);
            return "";
        }
    }

    private ShortcutParam convertShortcutInfo(ShortcutInfo shortcutInfo) {
        return new ShortcutParam(shortcutInfo);
    }

    private List<ShortcutParam> convertShortcutInfos(List<ShortcutInfo> list) {
        ArrayList arrayList = new ArrayList();
        for (ShortcutInfo shortcutInfo : list) {
            if (shortcutInfo != null) {
                arrayList.add(convertShortcutInfo(shortcutInfo));
            }
        }
        return arrayList;
    }

    @Override // ohos.bundle.IBundleManager
    public int startBackupSession(IBackupSessionCallback iBackupSessionCallback) {
        return PackageManagerAdapter.getInstance().startBackupSession(iBackupSessionCallback);
    }

    @Override // ohos.bundle.IBundleManager
    public int executeBackupTask(int i, String str) {
        return PackageManagerAdapter.getInstance().executeBackupTask(i, str);
    }

    @Override // ohos.bundle.IBundleManager
    public int finishBackupSession(int i) {
        return PackageManagerAdapter.getInstance().finishBackupSession(i);
    }

    @Override // ohos.bundle.IBundleManager
    public PermissionDef getPermissionDef(String str) {
        if (str != null && !str.isEmpty()) {
            return PackageManagerAdapter.getInstance().getPermissionInfo(str);
        }
        AppLog.e("BundleManager::getPermissionDef permissionName is null or empty", new Object[0]);
        return null;
    }

    @Override // ohos.bundle.IBundleManager
    public Optional<PermissionGroupDef> getPermissionGroupDef(String str) {
        if (str != null && !str.isEmpty()) {
            return PackageManagerAdapter.getInstance().getPermissionGroupInfo(str);
        }
        AppLog.e("BundleManager::getPermissionGroupDef permissionGroupName is null or empty", new Object[0]);
        return Optional.empty();
    }

    @Override // ohos.bundle.IBundleManager
    public Optional<List<PermissionDef>> getPermissionDefByGroup(String str) {
        if (str != null && !str.isEmpty()) {
            return PackageManagerAdapter.getInstance().getPermissionInfoByGroup(str);
        }
        AppLog.e("BundleManager::getPermissionDefByGroup permissionGroupName is null or empty", new Object[0]);
        return Optional.empty();
    }

    @Override // ohos.bundle.IBundleManager
    public void cleanBundleCacheFiles(String str, ICleanCacheCallback iCleanCacheCallback) {
        if (StringUtils.isEmpty(str)) {
            AppLog.e("BundleManager::cleanBundleCacheFiles bundleName is null", new Object[0]);
        } else {
            PackageManagerAdapter.getInstance().cleanBundleCacheFiles(str, iCleanCacheCallback);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public String getAppType(String str) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::getAppType remote is null", new Object[0]);
            return "";
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return "";
            }
            if (!obtain.writeString(str)) {
                AppLog.e("BundleManager::getAppType write string failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return "";
            } else if (!this.remote.sendRequest(39, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::getAppType sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return "";
            } else {
                int readInt = obtain2.readInt();
                if (readInt != 0) {
                    AppLog.e("BundleManager::getAppType failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return "";
                }
                String readString = obtain2.readString();
                reclaimParcel(obtain, obtain2);
                return readString;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public boolean downloadAndInstall(AbilityInfo abilityInfo, boolean z, InstallerCallback installerCallback) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::downloadAndInstall remote is null", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return false;
            }
            obtain.writeSequenceable(abilityInfo);
            obtain.writeBoolean(z);
            if (!obtain.writeRemoteObject(installerCallback)) {
                reclaimParcel(obtain, obtain2);
                return false;
            } else if (!this.remote.sendRequest(41, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::downloadAndInstall sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return false;
            } else {
                int readInt = obtain2.readInt();
                if (readInt != 0) {
                    AppLog.e("BundleManager::downloadAndInstall failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return false;
                }
                reclaimParcel(obtain, obtain2);
                return true;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public boolean downloadAndInstallWithParam(AbilityInfo abilityInfo, boolean z, InstallerCallback installerCallback, String str) throws RemoteException {
        AppLog.d("BundleManager::downloadAndInstallWithParam called", new Object[0]);
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::downloadAndInstallWithParam remote is null", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return false;
            }
            obtain.writeSequenceable(abilityInfo);
            obtain.writeBoolean(z);
            if (!obtain.writeRemoteObject(installerCallback)) {
                reclaimParcel(obtain, obtain2);
                return false;
            } else if (!obtain.writeString(str)) {
                AppLog.e("BundleManager::downloadAndInstallWithParam write param failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return false;
            } else if (!this.remote.sendRequest(50, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::downloadAndInstallWithParam sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return false;
            } else {
                int readInt = obtain2.readInt();
                if (readInt != 0) {
                    AppLog.e("BundleManager::downloadAndInstallWithParam failed, result is %{public}d", Integer.valueOf(readInt));
                    reclaimParcel(obtain, obtain2);
                    return false;
                }
                AppLog.d("BundleManager::downloadAndInstallWithParam success", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return true;
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public boolean cancelDownload(AbilityInfo abilityInfo) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::cancelDownload remote is null", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                return false;
            }
            obtain.writeSequenceable(abilityInfo);
            if (!this.remote.sendRequest(43, obtain, obtain2, messageOption)) {
                AppLog.e("BundleManager::cancelDownload sendRequest failed", new Object[0]);
                reclaimParcel(obtain, obtain2);
                return false;
            }
            int readInt = obtain2.readInt();
            if (readInt != 0) {
                AppLog.e("BundleManager::cancelDownload failed, result is %{public}d", Integer.valueOf(readInt));
                reclaimParcel(obtain, obtain2);
                return false;
            }
            reclaimParcel(obtain, obtain2);
            return true;
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void showErrorMessage(String str, int i) throws RemoteException {
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote == null) {
            AppLog.e("BundleManager::showErrorMessage remote is null", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (obtain.writeInterfaceToken(IBundleManager.DESCRIPTOR)) {
                if (!obtain.writeString(str)) {
                    AppLog.e("BundleManager::showErrorMessage write bundleName failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return;
                }
                obtain.writeInt(i);
                if (!this.remote.sendRequest(42, obtain, obtain2, messageOption)) {
                    AppLog.e("BundleManager::showErrorMessage sendRequest failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return;
                }
                int readInt = obtain2.readInt();
                if (readInt != 0) {
                    AppLog.e("BundleManager::showErrorMessage failed, result is %{public}d", Integer.valueOf(readInt));
                }
                reclaimParcel(obtain, obtain2);
            }
        } finally {
            reclaimParcel(obtain, obtain2);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public int isShortcutExist(String str, int i) {
        if (str != null && !str.isEmpty()) {
            return PackageManagerAdapter.getInstance().isPinnedShortcutExist(str, i);
        }
        AppLog.w("BundleManager::isShortcutExist invalid id", new Object[0]);
        return 1;
    }

    @Override // ohos.bundle.IBundleManager
    public boolean isHomeShortcutSupported() {
        return PackageManagerAdapter.getInstance().isRequestPinShortcutSupported();
    }

    @Override // ohos.bundle.IBundleManager
    public boolean addHomeShortcut(ShortcutInfo shortcutInfo) throws IllegalArgumentException {
        if (shortcutInfo == null) {
            AppLog.e("BundleManager::addHomeShortcut failed due to shortcutInfo is null", new Object[0]);
            return false;
        }
        Context context = this.appContext;
        if (context == null) {
            AppLog.e("BundleManager::addHomeShortcut getResourceManager appContext is null", new Object[0]);
            return false;
        }
        ResourceManager resourceManager = context.getResourceManager();
        if (resourceManager != null) {
            return PackageManagerAdapter.getInstance().requestPinShortcut(shortcutInfo, resourceManager);
        }
        AppLog.e("BundleManager::addHomeShortcut failed due to resourceManager is null", new Object[0]);
        return false;
    }

    @Override // ohos.bundle.IBundleManager
    public boolean updateShortcuts(List<ShortcutInfo> list) {
        if (list == null || list.isEmpty()) {
            AppLog.w("BundleManager::shortcutInfos is null", new Object[0]);
            return false;
        }
        Context context = this.appContext;
        if (context == null) {
            AppLog.e("BundleManager::updateShortcuts getResourceManager appContext is null", new Object[0]);
            return false;
        }
        ResourceManager resourceManager = context.getResourceManager();
        if (resourceManager != null) {
            return PackageManagerAdapter.getInstance().updateShortcuts(list, resourceManager);
        }
        AppLog.e("BundleManager::requestPinShortcut failed due to resourceManager is null", new Object[0]);
        return false;
    }

    @Override // ohos.bundle.IBundleManager
    public void disableHomeShortcuts(List<String> list) {
        if (list == null || list.isEmpty()) {
            AppLog.e("BundleManager::disableHomeShortcuts invalid parameters", new Object[0]);
        } else {
            PackageManagerAdapter.getInstance().disablePinShortcuts(list);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void enableHomeShortcuts(List<String> list) {
        if (list == null || list.isEmpty()) {
            AppLog.e("BundleManager::enableHomeShortcuts invalid parameters", new Object[0]);
        } else {
            PackageManagerAdapter.getInstance().enablePinShortcuts(list);
        }
    }

    @Override // ohos.bundle.IBundleManager
    public void setContext(Context context) {
        this.appContext = context;
    }

    public List<AbilityInfo> getNeedUpdatedInfo(String str) throws RemoteException {
        Intent intent = new Intent();
        intent.setBundle(str);
        intent.setAction(ACTION_QUERY_ABILITY_NEED_UPDATE);
        return queryAbilityByIntent(intent);
    }
}
