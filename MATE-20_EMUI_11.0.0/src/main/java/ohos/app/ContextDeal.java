package ohos.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.ability.IDataAbility;
import ohos.aafwk.ability.TaskInformation;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.AbilityShellData;
import ohos.abilityshell.ApplicationDataAbility;
import ohos.abilityshell.BundleMgrBridge;
import ohos.abilityshell.DataAbilityCallback;
import ohos.abilityshell.DistributedConnection;
import ohos.abilityshell.DistributedImpl;
import ohos.abilityshell.HarmonyResources;
import ohos.abilityshell.utils.AbilityResolver;
import ohos.abilityshell.utils.AbilityResolverSingleDevice;
import ohos.abilityshell.utils.AbilityShellConverterUtils;
import ohos.abilityshell.utils.IntentConverter;
import ohos.abilityshell.utils.SelectAbilityUtils;
import ohos.agp.components.LayoutScatter;
import ohos.app.dispatcher.SpecDispatcherConfig;
import ohos.app.dispatcher.SpecTaskDispatcher;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.TaskDispatcherContext;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.app.dispatcher.threading.AndroidTaskLooper;
import ohos.app.dispatcher.threading.TaskLooper;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.FileUtils;
import ohos.appexecfwk.utils.HiViewUtil;
import ohos.appexecfwk.utils.JLogUtil;
import ohos.appexecfwk.utils.StringUtils;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ApplicationInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.BundleManager;
import ohos.bundle.ElementName;
import ohos.bundle.HapModuleInfo;
import ohos.bundle.IBundleManager;
import ohos.bundle.InstallerCallback;
import ohos.bundle.ShellInfo;
import ohos.data.dataability.RemoteDataAbilityProxy;
import ohos.data.distributed.file.DistributedFileManager;
import ohos.data.distributed.file.DistributedFileManagerImpl;
import ohos.devtools.JLogConstants;
import ohos.global.configuration.Configuration;
import ohos.global.configuration.DeviceCapability;
import ohos.global.innerkit.asset.Package;
import ohos.global.resource.Element;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.ResourceManagerInner;
import ohos.global.resource.ResourcePath;
import ohos.global.resource.ResourceUtils;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Pattern;
import ohos.global.resource.solidxml.Theme;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.net.UriConverter;
import ohos.os.ProcessManager;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IPCSkeleton;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.security.dpermissionkit.DPermissionKit;
import ohos.security.permission.PermissionConversion;
import ohos.security.permissionkitinner.PermissionKitInner;
import ohos.tools.Bytrace;
import ohos.utils.net.Uri;

public class ContextDeal implements Context, IAbilityLoader {
    private static final HiLogLabel APPKIT_LABEL = new HiLogLabel(3, 218108160, "AppKit");
    private static final String CALLER_BUNDLE_NAME = "callerBundleName";
    private static final Object CREATE_FILE_LOCK = new Object();
    private static final String DATABASE_DIRECTORY_NAME = "databases";
    private static final int DEFAULT_REQUEST_CODE = 0;
    private static final String DIRECTORY_DATA_DATA = "/data/data/";
    private static final String DISTRIBUTE_DATABASE_DIRECTORY_NAME = "distribute_databases";
    private static final int ERROR_TYPR_OF_ABILITY = -10;
    private static final int FERR_INSTALL_QUERY_ABILITY_RESULT_SIZE = 1;
    private static final int GET_REMOTE_DATA_ABILITY_TIMEOUT = 2000;
    private static final int INIT_APPLICATION_RESOUCE_TIMEOUT = 1000;
    private static final int INSTALLER_CALLBACK_DOWNLOAD_FAILURE = 12;
    private static final int INVALID_REQUEST_CODE = -1;
    private static final String NULL_DEVICE_ID = "";
    private static final int PER_USER_RANGE = 100000;
    private static final String PREFERENCES_DIRECTORY_NAME = "preferences";
    private static final String RESOURCE_MANAGER_CONFIGURATION = "Configuration";
    private static final String RESOURCE_MANAGER_DEVICECAPABILITY = "DeviceCapability";
    private static final String TRANSITION_ANIMATION_SUFFIX = ".resource.Resource$Animator";
    private static final int URI_LOCAL = 0;
    private static final String WEB_ABILITY_DEEPLINK_SCHEME = "hwfastapp://";
    private static boolean isInitResouceDone = false;
    private static CountDownLatch resourceLatch = new CountDownLatch(1);
    private final HashMap<IAbilityConnection, Object> abilityConnectionMap = new HashMap<>();
    private AbilityInfo abilityInfo;
    private AbilityManager abilityManager = new AbilityManager(new AbilityManagerImpl(this));
    private Context abilityShellContext;
    private volatile Activity activityContext = null;
    private Application application;
    private BundleMgrBridge bundleMgrImpl = new BundleMgrBridge();
    private ClassLoader classLoader;
    private String databasePath;
    private DistributedFileManager distributedFileManager;
    private final DistributedImpl distributedImpl = new DistributedImpl();
    private String distributedPath;
    private Element element;
    private final CountDownLatch getRemoteDataAbilityLatch = new CountDownLatch(1);
    private HapModuleInfo hapModuleInfo;
    private boolean isDatabasePathExits = false;
    private boolean isDistributedPathExits = false;
    private LayoutScatter layoutScatter;
    private int mFlags = 16;
    private TaskLooper mainLooper;
    private TaskDispatcher mainTaskDispatcher;
    private Pattern pPattern;
    private Theme pTheme;
    private ResourceManager resourceManager;
    private ResourceManagerInner resourceManagerInner;
    private int resourceState = 0;

    private boolean isFlagExists(int i, int i2) {
        return (i & i2) == i;
    }

    private boolean isRequestCodeValid(int i) {
        return i != -1;
    }

    @Override // ohos.app.IAbilityLoader
    public void onLoadAbility() {
    }

    private void resetPathInit() {
        synchronized (CREATE_FILE_LOCK) {
            this.databasePath = null;
            this.isDatabasePathExits = false;
            this.distributedPath = null;
            this.isDistributedPathExits = false;
        }
    }

    public ContextDeal(Context context, ClassLoader classLoader2) {
        this.abilityShellContext = context;
        if (context instanceof Activity) {
            this.activityContext = (Activity) this.abilityShellContext;
        }
        this.classLoader = classLoader2;
    }

    @Override // ohos.app.Context
    public Object getHarmonyosApp() {
        Application application2 = this.application;
        if (application2 != null) {
            return application2.getHarmonyosApplication();
        }
        return null;
    }

    @Override // ohos.app.Context
    public Object getHarmonyAbilityPkg(AbilityInfo abilityInfo2) {
        if (this.application == null || abilityInfo2 == null || abilityInfo2.getModuleName() == null) {
            return null;
        }
        return this.application.getAbilityPackage(abilityInfo2.getModuleName());
    }

    public void setApplication(Application application2) {
        this.application = application2;
    }

    public void setAbilityInfo(AbilityInfo abilityInfo2) {
        this.abilityInfo = abilityInfo2;
    }

    public void setHapModuleInfo(HapModuleInfo hapModuleInfo2) {
        this.hapModuleInfo = hapModuleInfo2;
    }

    public void setMainLooper(TaskLooper taskLooper) {
        this.mainLooper = taskLooper;
    }

    public void setResourceManagerInner(ResourceManagerInner resourceManagerInner2) {
        this.resourceManagerInner = resourceManagerInner2;
    }

    public void setBundleMgrBridge(BundleMgrBridge bundleMgrBridge) {
        this.bundleMgrImpl = bundleMgrBridge;
    }

    public void setDistributedFileManager(DistributedFileManager distributedFileManager2) {
        synchronized (CREATE_FILE_LOCK) {
            this.distributedFileManager = distributedFileManager2;
        }
    }

    public boolean initApplicationResourceManager(String str) {
        Bytrace.startTrace(2147483648L, "ContextDeal initResource");
        boolean initResourceManager = initResourceManager(str);
        Bytrace.finishTrace(2147483648L, "ContextDeal initResource");
        isInitResouceDone = true;
        resourceLatch.countDown();
        return initResourceManager;
    }

    public TaskDispatcherContext getTaskDispatcherContext() {
        return this.application.getTaskDispatcherContext();
    }

    @Override // ohos.app.Context
    public Context getHostContext() {
        return this.abilityShellContext;
    }

    @Override // ohos.app.Context
    public Context getApplicationContext() {
        return this.application.getContext();
    }

    @Override // ohos.app.Context
    public AbilityInfo getAbilityInfo() {
        return this.abilityInfo;
    }

    @Override // ohos.app.Context
    public ApplicationInfo getApplicationInfo() {
        return this.application.getApplicationInfo();
    }

    @Override // ohos.app.Context
    public ProcessInfo getProcessInfo() {
        return this.application.getProcessInfo();
    }

    @Override // ohos.app.Context
    public ResourceManager getResourceManager() {
        if (this.resourceManager != null) {
            if (!initResourceManager(getBundleName())) {
                AppLog.e(APPKIT_LABEL, "ContextDeal::getResourceManager failed, initResourceManager failed", new Object[0]);
                return null;
            }
        } else if (equals(this.application.getContext())) {
            if (!isInitResouceDone) {
                try {
                    if (!resourceLatch.await(1000, TimeUnit.MILLISECONDS)) {
                        AppLog.e(APPKIT_LABEL, "init appliaciton resouce exceed time", new Object[0]);
                    }
                } catch (InterruptedException unused) {
                    AppLog.e(APPKIT_LABEL, "init application resouce InterruptedException occur", new Object[0]);
                }
            }
            if (isInitResouceDone) {
                return this.resourceManager;
            }
            throw new IllegalStateException("init application resource timeout!");
        } else {
            this.resourceManager = this.application.getResourceManager();
        }
        return this.resourceManager;
    }

    @Override // ohos.app.Context
    public TaskDispatcher getMainTaskDispatcher() {
        if (this.mainTaskDispatcher == null) {
            if (this.mainLooper != null) {
                this.mainTaskDispatcher = new SpecTaskDispatcher(SpecDispatcherConfig.MAIN, this.mainLooper);
            } else {
                AppLog.w(APPKIT_LABEL, "ContextDeal::getMainTaskDispatcher Cannot create dispatcher due to looper is not set", new Object[0]);
            }
        }
        return this.mainTaskDispatcher;
    }

    @Override // ohos.app.Context
    public final TaskDispatcher getUITaskDispatcher() {
        return getMainTaskDispatcher();
    }

    @Override // ohos.app.Context
    public TaskDispatcher createParallelTaskDispatcher(String str, TaskPriority taskPriority) {
        return this.application.getTaskDispatcherContext().createParallelDispatcher(str, taskPriority);
    }

    @Override // ohos.app.Context
    public TaskDispatcher createSerialTaskDispatcher(String str, TaskPriority taskPriority) {
        return this.application.getTaskDispatcherContext().createSerialDispatcher(str, taskPriority);
    }

    @Override // ohos.app.Context
    public TaskDispatcher getGlobalTaskDispatcher(TaskPriority taskPriority) {
        return this.application.getTaskDispatcherContext().getGlobalTaskDispatcher(taskPriority);
    }

    @Override // ohos.app.Context
    public ClassLoader getClassloader() {
        return this.classLoader;
    }

    @Override // ohos.app.Context
    public File getPreferencesDir() {
        String preferenceDirectory = getPreferenceDirectory(this.abilityInfo);
        if (preferenceDirectory == null) {
            return null;
        }
        synchronized (CREATE_FILE_LOCK) {
            if (FileUtils.createDirectory(preferenceDirectory)) {
                return new File(preferenceDirectory);
            }
            AppLog.e(APPKIT_LABEL, "ContextDeal::getPreferencesPath failed, create directory failed", new Object[0]);
            return null;
        }
    }

    @Override // ohos.app.Context
    public File getDatabaseDir() {
        String databaseDirectory = getDatabaseDirectory(this.abilityInfo);
        if (databaseDirectory == null) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::getDatabaseDir failed, getDatabaseDirectory failed", new Object[0]);
            return null;
        }
        synchronized (CREATE_FILE_LOCK) {
            if (!this.isDatabasePathExits) {
                this.isDatabasePathExits = FileUtils.createDirectory(databaseDirectory);
                if (!this.isDatabasePathExits) {
                    return null;
                }
            }
            return new File(databaseDirectory);
        }
    }

    @Override // ohos.app.Context
    public File getDistributedDir() {
        String distributeDirectory = getDistributeDirectory(this.abilityInfo);
        if (distributeDirectory == null) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::getDistributedDir failed, getDistributeDirectory failed", new Object[0]);
            return null;
        }
        synchronized (CREATE_FILE_LOCK) {
            if (!this.isDistributedPathExits) {
                this.isDistributedPathExits = FileUtils.createDirectory(distributeDirectory);
                if (!this.isDistributedPathExits) {
                    return null;
                }
            }
            return new File(distributeDirectory);
        }
    }

    @Override // ohos.app.Context
    public IDataAbility getDataAbility(Uri uri) {
        return getDataAbility(uri, false);
    }

    @Override // ohos.app.Context
    public IDataAbility getDataAbility(Uri uri, boolean z) {
        try {
            int selectUri = this.distributedImpl.selectUri(uri);
            if (selectUri == -1) {
                AppLog.e(APPKIT_LABEL, "ContextDeal::getDataAbility selectUri failed, result is %{public}d", Integer.valueOf(selectUri));
                return null;
            } else if (selectUri == 0) {
                return ApplicationDataAbility.creator(this.abilityShellContext, uri, z);
            } else {
                IRemoteObject dataAbilityCallback = new DataAbilityCallback(this.getRemoteDataAbilityLatch);
                try {
                    int remoteDataAbility = this.distributedImpl.getRemoteDataAbility(uri, dataAbilityCallback);
                    if (remoteDataAbility != 0) {
                        AppLog.e(APPKIT_LABEL, "ContextDeal::getDataAbility getRemoteDataAbility failed %{public}d", Integer.valueOf(remoteDataAbility));
                        return null;
                    }
                    try {
                        if (!this.getRemoteDataAbilityLatch.await(2000, TimeUnit.MILLISECONDS)) {
                            AppLog.w(APPKIT_LABEL, "ContextDeal::getDataAbility await exceed time", new Object[0]);
                        }
                        IRemoteObject remoteDataAbility2 = dataAbilityCallback.getRemoteDataAbility();
                        if (remoteDataAbility2 != null) {
                            return new RemoteDataAbilityProxy(remoteDataAbility2);
                        }
                        AppLog.e(APPKIT_LABEL, "ContextDeal::getDataAbility wait timeout", new Object[0]);
                        return null;
                    } catch (InterruptedException unused) {
                        AppLog.e(APPKIT_LABEL, "ContextDeal::getDataAbility InterruptedException occur", new Object[0]);
                        return null;
                    }
                } catch (RemoteException unused2) {
                    AppLog.e(APPKIT_LABEL, "ContextDeal::getDataAbility RemoteException occur", new Object[0]);
                    return null;
                }
            }
        } catch (RemoteException unused3) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::getDataAbility RemoteException occur", new Object[0]);
            return null;
        }
    }

    @Override // ohos.app.Context
    public boolean releaseDataAbility(IDataAbility iDataAbility) {
        if (iDataAbility == null) {
            AppLog.e("ContextDeal::releaseDataAbility dataAbility is null", new Object[0]);
            return false;
        }
        iDataAbility.close();
        return true;
    }

    @Override // ohos.app.Context
    public void switchToDeviceEncryptedStorageContext() {
        this.mFlags = (this.mFlags & -17) | 8;
        resetPathInit();
    }

    @Override // ohos.app.Context
    public void switchToCredentialEncryptedStorageContext() {
        this.mFlags = (this.mFlags & -9) | 16;
        resetPathInit();
    }

    @Override // ohos.app.Context
    public boolean isDeviceEncryptedStorage() {
        return (this.mFlags & 8) != 0;
    }

    @Override // ohos.app.Context
    public boolean isCredentialProtectedStorage() {
        return (this.mFlags & 16) != 0;
    }

    @Override // ohos.app.Context
    public void authUriPermission(String str, Uri uri, int i) {
        if (str == null) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::authUriPermission targetBundleName is null", new Object[0]);
            throw new IllegalArgumentException("null targetBundleName");
        } else if (uri == null) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::authUriPermission uri is null", new Object[0]);
            throw new IllegalArgumentException("null uri");
        } else if (i <= 0 || (i & 195) == 0) {
            throw new IllegalArgumentException("Requested flags 0x" + Integer.toHexString(i) + "not allowed ");
        } else {
            this.abilityShellContext.grantUriPermission(str, UriConverter.convertToAndroidContentUri(uri), i);
        }
    }

    @Override // ohos.app.Context
    public void unauthUriPermission(String str, Uri uri, int i) {
        if (str == null) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::unauthUriPermission targetBundleName is null", new Object[0]);
            throw new IllegalArgumentException("null targetBundleName");
        } else if (uri == null) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::unauthUriPermission uri is null", new Object[0]);
            throw new IllegalArgumentException("null uri");
        } else if (i <= 0 || (i & 195) == 0) {
            throw new IllegalArgumentException("Requested flags 0x" + Integer.toHexString(i) + "not allowed ");
        } else {
            this.abilityShellContext.revokeUriPermission(str, UriConverter.convertToAndroidContentUri(uri), i);
        }
    }

    @Override // ohos.app.Context
    public int verifyUriPermission(Uri uri, int i, int i2, int i3) {
        if (uri == null) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::verifyUriPermission uri is null", new Object[0]);
            throw new IllegalArgumentException("null uri");
        } else if (i <= 0) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::verifyUriPermission pid is illegal", new Object[0]);
            return -1;
        } else {
            return this.abilityShellContext.checkUriPermission(UriConverter.convertToAndroidContentUri(uri), i, i2, i3);
        }
    }

    @Override // ohos.app.Context
    public int verifyCallingPermission(String str) {
        String aosPermissionNameIfPossible = PermissionConversion.getAosPermissionNameIfPossible(str);
        if (aosPermissionNameIfPossible == null) {
            return -1;
        }
        String callingDeviceID = IPCSkeleton.getCallingDeviceID();
        int callingPid = IPCSkeleton.getCallingPid();
        int callingUid = IPCSkeleton.getCallingUid();
        if (callingDeviceID == null || callingPid <= 0 || callingPid == ProcessManager.getPid()) {
            return -1;
        }
        if (callingDeviceID.isEmpty()) {
            return this.abilityShellContext.checkPermission(aosPermissionNameIfPossible, callingPid, callingUid);
        }
        return DPermissionKit.getInstance().checkDPermission(DPermissionKit.getInstance().allocateDuid(callingDeviceID, callingUid), aosPermissionNameIfPossible);
    }

    @Override // ohos.app.Context
    public int verifySelfPermission(String str) {
        String aosPermissionNameIfPossible = PermissionConversion.getAosPermissionNameIfPossible(str);
        if (aosPermissionNameIfPossible == null) {
            return -1;
        }
        return this.abilityShellContext.checkSelfPermission(aosPermissionNameIfPossible);
    }

    @Override // ohos.app.Context
    public int verifyCallingOrSelfPermission(String str) {
        String aosPermissionNameIfPossible = PermissionConversion.getAosPermissionNameIfPossible(str);
        if (aosPermissionNameIfPossible == null) {
            return -1;
        }
        String callingDeviceID = IPCSkeleton.getCallingDeviceID();
        int callingPid = IPCSkeleton.getCallingPid();
        int callingUid = IPCSkeleton.getCallingUid();
        if (callingDeviceID == null || callingPid <= 0) {
            return -1;
        }
        if (callingDeviceID.isEmpty()) {
            return this.abilityShellContext.checkPermission(aosPermissionNameIfPossible, callingPid, callingUid);
        }
        return DPermissionKit.getInstance().checkDPermission(DPermissionKit.getInstance().allocateDuid(callingDeviceID, callingUid), aosPermissionNameIfPossible);
    }

    @Override // ohos.app.Context
    public int verifyPermission(String str, int i, int i2) {
        String aosPermissionNameIfPossible = PermissionConversion.getAosPermissionNameIfPossible(str);
        if (aosPermissionNameIfPossible == null || i <= 0) {
            return -1;
        }
        return this.abilityShellContext.checkPermission(aosPermissionNameIfPossible, i, i2);
    }

    @Override // ohos.app.Context
    public void terminateAbility() {
        Context context = this.abilityShellContext;
        if (context instanceof Activity) {
            ((Activity) context).finish();
        } else if (context instanceof Service) {
            ((Service) context).stopSelf();
        } else {
            AppLog.w(APPKIT_LABEL, "ContextDeal::terminateAbility only support Activity and Service", new Object[0]);
        }
    }

    @Override // ohos.app.Context
    public void terminateAbility(int i) {
        Context context = this.abilityShellContext;
        if (!(context instanceof Activity)) {
            AppLog.w(APPKIT_LABEL, "ContextDeal::terminateAbility only support Activity", new Object[0]);
        } else {
            ((Activity) context).finishActivity(i);
        }
    }

    @Override // ohos.app.Context
    public final boolean terminateAbilityResult(int i) {
        Context context = this.abilityShellContext;
        if (context instanceof Service) {
            return ((Service) context).stopSelfResult(i);
        }
        AppLog.w(APPKIT_LABEL, "ContextDeal::terminateAbilityResult only support service", new Object[0]);
        return false;
    }

    @Override // ohos.app.Context
    public void terminateAndRemoveTask() {
        Context context = this.abilityShellContext;
        if (!(context instanceof Activity)) {
            AppLog.w(APPKIT_LABEL, "ContextDeal::terminateAndRemoveTask only support Activity", new Object[0]);
        } else {
            ((Activity) context).finishAndRemoveTask();
        }
    }

    @Override // ohos.app.Context
    public String getLocalClassName() {
        AppLog.i(APPKIT_LABEL, "ContextDeal::getLocalClassName called", new Object[0]);
        Context context = this.abilityShellContext;
        if (context instanceof Activity) {
            return AbilityShellConverterUtils.convertToHarmonyClassName(((Activity) context).getLocalClassName());
        }
        AppLog.w(APPKIT_LABEL, "ContextDeal::getLocalClassName only support Activity", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public ElementName getElementName() {
        AppLog.i(APPKIT_LABEL, "ContextDeal::getElementName called", new Object[0]);
        Context context = this.abilityShellContext;
        if (context instanceof Activity) {
            return convertComponentNameToElementName(((Activity) context).getComponentName());
        }
        AppLog.w(APPKIT_LABEL, "ContextDeal::getElementName only support Activity", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public ElementName getCallingAbility() {
        AppLog.i(APPKIT_LABEL, "ContextDeal::getCallingAbility called", new Object[0]);
        Context context = this.abilityShellContext;
        if (context instanceof Activity) {
            return convertComponentNameToElementName(((Activity) context).getCallingActivity());
        }
        AppLog.w(APPKIT_LABEL, "ContextDeal::getCallingAbility only support Activity", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public String getCallingBundle() {
        AppLog.i(APPKIT_LABEL, "ContextDeal::getCallingBundle called", new Object[0]);
        Context context = this.abilityShellContext;
        if (context instanceof Activity) {
            return ((Activity) context).getCallingPackage();
        }
        AppLog.w(APPKIT_LABEL, "ContextDeal::getCallingBundle only support Activity", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public void startAbility(Intent intent, int i) {
        if (intent == null) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::startAbility failed, intent is null", new Object[0]);
        } else if (isFlagExists(2048, intent.getFlags())) {
            startFreeInstallAbility(intent, i);
        } else {
            startAbilityInner(intent, i);
        }
    }

    @Override // ohos.app.Context
    public void startAbilities(Intent[] intentArr) {
        for (Intent intent : intentArr) {
            startAbility(intent, 0);
        }
    }

    @Override // ohos.app.Context
    public Context createBundleContext(String str, int i) {
        if (StringUtils.isBlank(str)) {
            AppLog.e("ContextDeal::createBundleContext bundle name is empty", new Object[0]);
            return null;
        } else if (str.equals(getBundleName())) {
            AppLog.i("ContextDeal::createBundleContext bundleName is same as the current application bundle name", new Object[0]);
            return getApplicationContext();
        } else {
            try {
                Context createPackageContext = this.abilityShellContext.createPackageContext(str, i);
                if (createPackageContext != null || i == 8) {
                    if (i == 8) {
                        createPackageContext = this.abilityShellContext;
                    }
                    ContextDeal contextDeal = new ContextDeal(createPackageContext, createPackageContext.getClassLoader());
                    contextDeal.setMainLooper(new AndroidTaskLooper(Looper.getMainLooper()));
                    if (!contextDeal.initResourceManager(str)) {
                        AppLog.e("ContextDeal::createBundleContext initResourceManager failed", new Object[0]);
                        return null;
                    }
                    Application application2 = new Application();
                    application2.attachBaseContext(contextDeal);
                    application2.setProcessInfo(new ProcessInfo(createPackageContext.getApplicationInfo().processName, Process.myPid()));
                    application2.setApplicationInfo(this.bundleMgrImpl.getApplicationInfo(str, 0, 0));
                    return application2;
                }
                AppLog.e("ContextDeal::createBundleContext failed", new Object[0]);
                return null;
            } catch (PackageManager.NameNotFoundException unused) {
                AppLog.e("ContextDeal::createBundleContext cannot find bundle name", new Object[0]);
                return null;
            }
        }
    }

    @Override // ohos.app.Context
    public boolean stopAbility(Intent intent) {
        return stopAbilityInner(intent);
    }

    @Override // ohos.app.Context
    public boolean connectAbility(Intent intent, IAbilityConnection iAbilityConnection) {
        boolean connectAbilityInner = connectAbilityInner(intent, iAbilityConnection);
        if (!connectAbilityInner) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::connectAbility called failed", new Object[0]);
        }
        return connectAbilityInner;
    }

    @Override // ohos.app.Context
    public void disconnectAbility(IAbilityConnection iAbilityConnection) {
        AppLog.i(APPKIT_LABEL, "ContextDeal::disconnectAbility called", new Object[0]);
        disconnectAbilityInner(this.abilityConnectionMap, iAbilityConnection);
    }

    @Override // ohos.app.Context
    public void setResult(int i, Intent intent) {
        if (!(this.abilityShellContext instanceof Activity)) {
            AppLog.e(APPKIT_LABEL, "ContextDeal::setResult failed ability is not instance of AbilityShellActivity", new Object[0]);
            return;
        }
        android.content.Intent intent2 = null;
        if (intent != null) {
            Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, null);
            if (createAndroidIntent.isPresent()) {
                intent2 = createAndroidIntent.get();
            }
        }
        ((Activity) this.abilityShellContext).setResult(i, intent2);
    }

    @Override // ohos.app.Context
    public void setDisplayOrientation(AbilityInfo.DisplayOrientation displayOrientation) {
        AppLog.i(APPKIT_LABEL, "ContextDeal::setDisplayOrientation called", new Object[0]);
        Context context = this.abilityShellContext;
        if (!(context instanceof Activity)) {
            AppLog.w(APPKIT_LABEL, "AbilityShellActivity::setDisplayOrientation only Activity support this operation", new Object[0]);
            return;
        }
        Activity activity = (Activity) context;
        int i = AnonymousClass7.$SwitchMap$ohos$bundle$AbilityInfo$DisplayOrientation[displayOrientation.ordinal()];
        if (i == 1) {
            activity.setRequestedOrientation(0);
        } else if (i == 2) {
            activity.setRequestedOrientation(1);
        } else if (i == 3) {
            activity.setRequestedOrientation(3);
        } else if (i == 4) {
            activity.setRequestedOrientation(-1);
        }
    }

    /* renamed from: ohos.app.ContextDeal$7  reason: invalid class name */
    static /* synthetic */ class AnonymousClass7 {
        static final /* synthetic */ int[] $SwitchMap$ohos$bundle$AbilityInfo$DisplayOrientation = new int[AbilityInfo.DisplayOrientation.values().length];

        static {
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$DisplayOrientation[AbilityInfo.DisplayOrientation.LANDSCAPE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$DisplayOrientation[AbilityInfo.DisplayOrientation.PORTRAIT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$DisplayOrientation[AbilityInfo.DisplayOrientation.FOLLOWRECENT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$DisplayOrientation[AbilityInfo.DisplayOrientation.UNSPECIFIED.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    @Override // ohos.app.Context
    public boolean canRequestPermission(String str) {
        AppLog.i(APPKIT_LABEL, "Context::canRequestPermission called permission: %{private}s", str);
        if (!(this.abilityShellContext instanceof Activity)) {
            AppLog.w(APPKIT_LABEL, "ContextDeal::canRequestPermission can't requestPermission,ability is not instance of AbilityShellActivity", new Object[0]);
            return false;
        }
        String bundleName = getBundleName();
        int uid = getUid() / PER_USER_RANGE;
        if (bundleName != null && uid != -1) {
            return PermissionKitInner.getInstance().canRequestPermission(str, bundleName, uid);
        }
        AppLog.e(APPKIT_LABEL, "ContextDeal::canRequestPermission false, bundleName %{private}s or userId %{private}d is invalid", bundleName, Integer.valueOf(uid));
        return false;
    }

    @Override // ohos.app.Context
    public void requestPermissionsFromUser(String[] strArr, int i) {
        if (!(this.abilityShellContext instanceof Activity)) {
            AppLog.e("ContextDeal::requestPermissionsFromUser failed, ability is not AbilityShellActivity", new Object[0]);
        } else if (strArr == null) {
            AppLog.e("ContextDeal::requestPermissionsFromUser failed, permissions is null", new Object[0]);
        } else {
            int length = strArr.length;
            String[] strArr2 = new String[length];
            for (int i2 = 0; i2 < length; i2++) {
                strArr2[i2] = PermissionConversion.getAosPermissionNameIfPossible(strArr[i2]);
            }
            PermissionConversion.registerRequestPermssions(i, strArr);
            ((Activity) this.abilityShellContext).requestPermissions(strArr2, i);
        }
    }

    @Override // ohos.app.Context
    public Object getHostProtectedStorageContext() {
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext();
        }
        return this.abilityShellContext.createCredentialProtectedStorageContext();
    }

    @Override // ohos.app.Context
    public String getBundleName() {
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 != null) {
            return abilityInfo2.getBundleName();
        }
        Context context = this.abilityShellContext;
        if (context != null) {
            return context.getPackageName();
        }
        return null;
    }

    @Override // ohos.app.Context
    public String getBundleCodePath() {
        Application application2 = this.application;
        if (!(application2 == null || application2.getBundleInfo() == null || this.application.getBundleInfo().getAppInfo() == null)) {
            List<String> moduleSourceDirs = this.application.getBundleInfo().getAppInfo().getModuleSourceDirs();
            if (!moduleSourceDirs.isEmpty()) {
                return moduleSourceDirs.get(0);
            }
        }
        return null;
    }

    @Override // ohos.app.Context
    public File getDataDir() {
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext().getDataDir();
        }
        return this.abilityShellContext.getDataDir();
    }

    @Override // ohos.app.Context
    public String getBundleResourcePath() {
        return getBundleCodePath();
    }

    @Override // ohos.app.Context
    public File getCacheDir() {
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext().getCacheDir();
        }
        return this.abilityShellContext.getCacheDir();
    }

    @Override // ohos.app.Context
    public File getFilesDir() {
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext().getFilesDir();
        }
        return this.abilityShellContext.getFilesDir();
    }

    @Override // ohos.app.Context
    public File getCodeCacheDir() {
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext().getCodeCacheDir();
        }
        return this.abilityShellContext.getCodeCacheDir();
    }

    @Override // ohos.app.Context
    public File[] getExternalMediaDirs() {
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext().getExternalMediaDirs();
        }
        return this.abilityShellContext.getExternalMediaDirs();
    }

    @Override // ohos.app.Context
    public File getNoBackupFilesDir() {
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext().getNoBackupFilesDir();
        }
        return this.abilityShellContext.getNoBackupFilesDir();
    }

    @Override // ohos.app.Context
    public File getDir(String str, int i) {
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext().getDir(str, i);
        }
        return this.abilityShellContext.getDir(str, i);
    }

    @Override // ohos.app.Context
    public File getExternalCacheDir() {
        return this.abilityShellContext.getExternalCacheDir();
    }

    @Override // ohos.app.Context
    public File[] getExternalCacheDirs() {
        return this.abilityShellContext.getExternalCacheDirs();
    }

    @Override // ohos.app.Context
    public File getExternalFilesDir(String str) {
        return this.abilityShellContext.getExternalFilesDir(str);
    }

    @Override // ohos.app.Context
    public File[] getExternalFilesDirs(String str) {
        return this.abilityShellContext.getExternalFilesDirs(str);
    }

    @Override // ohos.app.Context
    public File getObbDir() {
        return this.abilityShellContext.getObbDir();
    }

    @Override // ohos.app.Context
    public File[] getObbDirs() {
        return this.abilityShellContext.getObbDirs();
    }

    @Override // ohos.app.Context
    public boolean deleteFile(String str) {
        if (str == null) {
            return false;
        }
        if (isDeviceEncryptedStorage()) {
            return this.abilityShellContext.createDeviceProtectedStorageContext().deleteFile(str);
        }
        return this.abilityShellContext.deleteFile(str);
    }

    @Override // ohos.app.Context
    public boolean moveTaskToEnd(boolean z) {
        if (this.activityContext == null) {
            return false;
        }
        return this.activityContext.moveTaskToBack(z);
    }

    @Override // ohos.app.Context
    public boolean isFirstInTask() {
        if (this.activityContext == null) {
            return false;
        }
        return this.activityContext.isTaskRoot();
    }

    @Override // ohos.app.Context
    public int getTaskId() {
        if (this.activityContext == null) {
            return -1;
        }
        return this.activityContext.getTaskId();
    }

    @Override // ohos.app.Context
    public boolean setTaskInformation(TaskInformation taskInformation) {
        if (this.activityContext == null || taskInformation == null) {
            return false;
        }
        this.activityContext.setTaskDescription(convertTaskDescriptionToAndroid(taskInformation));
        return true;
    }

    private ActivityManager.TaskDescription convertTaskDescriptionToAndroid(TaskInformation taskInformation) {
        if (taskInformation == null) {
            return new ActivityManager.TaskDescription();
        }
        Bitmap bitmap = null;
        if (taskInformation.getIcon() != null) {
            bitmap = ImageDoubleFwConverter.createShadowBitmap(taskInformation.getIcon());
        }
        return new ActivityManager.TaskDescription(taskInformation.getLabel(), bitmap, taskInformation.getColorPrimary());
    }

    private int getUid() {
        Application application2 = this.application;
        if (application2 != null && application2.getBundleInfo() != null) {
            return this.application.getBundleInfo().getUid();
        }
        BundleInfo bundleInfo = this.bundleMgrImpl.getBundleInfo(getBundleName(), 0);
        if (bundleInfo != null) {
            return bundleInfo.getUid();
        }
        AppLog.w(APPKIT_LABEL, "ContextDeal::getUserId failed, bundleInfo is null", new Object[0]);
        return -1;
    }

    @Override // ohos.app.Context
    public IAbilityManager getAbilityManager() {
        return this.abilityManager;
    }

    @Override // ohos.app.Context
    public int getDisplayOrientation() {
        if (this.activityContext == null) {
            return -10;
        }
        return this.activityContext.getRequestedOrientation();
    }

    @Override // ohos.app.Context
    public void setShowOnLockScreen(boolean z) {
        if (this.activityContext != null) {
            this.activityContext.setShowWhenLocked(z);
        }
    }

    @Override // ohos.app.Context
    public void setWakeUpScreen(boolean z) {
        if (this.activityContext != null) {
            this.activityContext.setTurnScreenOn(z);
        }
    }

    @Override // ohos.app.Context
    public void restart() {
        if (this.activityContext != null) {
            this.activityContext.recreate();
        }
    }

    @Override // ohos.app.Context
    public void setTransitionAnimation(int i, int i2) {
        if (!(this.abilityShellContext instanceof Activity)) {
            AppLog.i("ContextDeal::setTransitionAnimation, ability is not instance of AbilityShellActivity", new Object[0]);
            return;
        }
        try {
            Class<?> cls = Class.forName(this.abilityShellContext.getPackageName() + TRANSITION_ANIMATION_SUFFIX, false, this.abilityShellContext.getClassLoader());
            ((Activity) this.abilityShellContext).overridePendingTransition(ResourceManagerInner.getAResId(i, cls, this.abilityShellContext), ResourceManagerInner.getAResId(i2, cls, this.abilityShellContext));
        } catch (ClassNotFoundException unused) {
            AppLog.e("ContextDeal::setTransitionAnimation failed, get class error", new Object[0]);
        }
    }

    @Override // ohos.app.Context
    public void setTheme(int i) {
        ResourceManager resourceManager2 = this.resourceManager;
        if (resourceManager2 == null) {
            AppLog.w("resourceManager is null", new Object[0]);
            return;
        }
        try {
            this.pTheme = resourceManager2.getTheme(i);
        } catch (NotExistException unused) {
            AppLog.e("ContextDeal::setTheme NotExistException happens", new Object[0]);
        } catch (IOException unused2) {
            AppLog.e("ContextDeal::setTheme IOException happens", new Object[0]);
        } catch (WrongTypeException unused3) {
            AppLog.e("ContextDeal::setTheme WrongTypeException happens", new Object[0]);
        }
    }

    @Override // ohos.app.Context
    public Theme getTheme() {
        return this.pTheme;
    }

    @Override // ohos.app.Context
    public Theme getCombinedTheme(Theme theme) {
        Theme theme2 = this.pTheme;
        if (theme2 != null) {
            return theme2.getCombinedTheme(theme);
        }
        AppLog.w("ContextDeal::getCombinedTheme pTheme is null, return child.", new Object[0]);
        return theme;
    }

    @Override // ohos.app.Context
    public void setPattern(int i) {
        ResourceManager resourceManager2 = this.resourceManager;
        if (resourceManager2 == null) {
            AppLog.w("ContextDeal::setPattern resourceManager is null", new Object[0]);
            return;
        }
        try {
            this.element = resourceManager2.getElement(i);
            this.pPattern = this.element.getPattern();
        } catch (NotExistException unused) {
            AppLog.e("ContextDeal::setPattern NotExistException happens", new Object[0]);
        } catch (IOException unused2) {
            AppLog.e("ContextDeal::setPattern IOException happens", new Object[0]);
        } catch (WrongTypeException unused3) {
            AppLog.e("ContextDeal::setPattern WrongTypeException happens", new Object[0]);
        }
    }

    @Override // ohos.app.Context
    public Pattern getPattern() {
        return this.pPattern;
    }

    @Override // ohos.app.Context
    public Pattern getCombinedPattern(Pattern pattern) {
        Pattern pattern2 = this.pPattern;
        if (pattern2 != null) {
            return pattern2.getCombinedPattern(pattern);
        }
        AppLog.w("ContextDeal::getCombinedPattern pPattern is null, return child.", new Object[0]);
        return pattern;
    }

    private Object getTopAbility() {
        return this.application.getTopAbility();
    }

    private String getAppDataPath() {
        if (isDeviceEncryptedStorage()) {
            return this.application.getDeviceProtectedPath();
        }
        return this.application.getAppDataPath();
    }

    private boolean initResourceManager(String str) {
        BundleInfo bundleInfo;
        if (this.resourceManager != null) {
            if (this.resourceState == HarmonyResources.getNewResourceState()) {
                AppLog.w("ContextDeal::initResourceManager resource manager has been initialized successfully, don't need to be initialized again", new Object[0]);
                updateResourceManager(this.resourceManager);
                return true;
            }
            AppLog.i("ContextDeal::initResourceManager resource manager is invalid, need reinitialize", new Object[0]);
            ResourceManagerInner resourceManagerInner2 = this.resourceManagerInner;
            if (resourceManagerInner2 != null) {
                resourceManagerInner2.release();
            }
            this.resourceManagerInner = null;
            this.resourceManager = null;
        }
        if (StringUtils.isBlank(str)) {
            AppLog.e("ContextDeal::initResourceManager failed, bundleName is empty", new Object[0]);
            return false;
        }
        Application application2 = this.application;
        if (application2 != null) {
            bundleInfo = application2.getBundleInfo();
        } else {
            bundleInfo = this.bundleMgrImpl.getBundleInfo(str, 0);
        }
        Package r5 = HarmonyResources.getPackage(bundleInfo);
        if (r5 == null) {
            AppLog.e("ContextDeal::initResourceManager failed, setResources is false", new Object[0]);
            return false;
        }
        ResourcePath[] resourcePath = HarmonyResources.getResourcePath(bundleInfo);
        if (resourcePath == null || resourcePath.length == 0) {
            AppLog.e("ContextDeal::initResourceManager failed, resourcePaths is empty", new Object[0]);
            return false;
        }
        if (this.resourceManagerInner == null) {
            this.resourceManagerInner = new ResourceManagerInner();
        }
        try {
            HashMap<String, Object> createHarmonyosConfiguration = createHarmonyosConfiguration();
            if (createHarmonyosConfiguration.get(RESOURCE_MANAGER_CONFIGURATION) instanceof Configuration) {
                if (createHarmonyosConfiguration.get(RESOURCE_MANAGER_DEVICECAPABILITY) instanceof DeviceCapability) {
                    if (this.resourceManagerInner.init(r5, resourcePath, (Configuration) createHarmonyosConfiguration.get(RESOURCE_MANAGER_CONFIGURATION), (DeviceCapability) createHarmonyosConfiguration.get(RESOURCE_MANAGER_DEVICECAPABILITY))) {
                        AppLog.w("ContextDeal::initResourceManager successfully", new Object[0]);
                        this.resourceManager = this.resourceManagerInner.getResourceManager();
                        this.resourceState = HarmonyResources.getNewResourceState();
                        return true;
                    }
                    AppLog.e("ContextDeal::initResourceManager result is false", new Object[0]);
                    HiViewUtil.sendGlobalEvent("resourceManagerInner.init", str, 0);
                    return tryInitResourceManager(bundleInfo, r5);
                }
            }
            AppLog.w("ContextDeal::initResourceManager failed, class transform failed", new Object[0]);
            return false;
        } catch (IOException e) {
            AppLog.e("ContextDeal::initResourceManager failed : %{public}s", e.getMessage());
        }
    }

    private boolean tryInitResourceManager(BundleInfo bundleInfo, Package r11) {
        if (bundleInfo == null || bundleInfo.getAppInfo() == null) {
            AppLog.e("ContextDeal::tryInitResourceManager failed, bundleInfo is empty", new Object[0]);
            return false;
        }
        List<String> moduleSourceDirs = bundleInfo.getAppInfo().getModuleSourceDirs();
        if (moduleSourceDirs.isEmpty()) {
            AppLog.e("ContextDeal::tryInitResourceManager failed, moduleSourceDirs is empty", new Object[0]);
            return false;
        }
        int size = moduleSourceDirs.size();
        ResourcePath[] resourcePathArr = new ResourcePath[size];
        for (int i = 0; i < size; i++) {
            String str = moduleSourceDirs.get(i);
            if (str == null || str.isEmpty()) {
                AppLog.i("ContextDeal::tryInitResourceManager moduleSourceDir is null", new Object[0]);
            } else {
                AppLog.w("ContextDeal::tryInitResourceManager moduleSourceDir: %{private}s", str);
                ResourcePath resourcePath = new ResourcePath();
                resourcePath.setResourcePath(str, (String) null);
                resourcePathArr[i] = resourcePath;
            }
        }
        if (this.resourceManagerInner == null) {
            this.resourceManagerInner = new ResourceManagerInner();
        }
        try {
            HashMap<String, Object> createHarmonyosConfiguration = createHarmonyosConfiguration();
            if (createHarmonyosConfiguration.get(RESOURCE_MANAGER_CONFIGURATION) instanceof Configuration) {
                if (createHarmonyosConfiguration.get(RESOURCE_MANAGER_DEVICECAPABILITY) instanceof DeviceCapability) {
                    if (this.resourceManagerInner.init(r11, resourcePathArr, (Configuration) createHarmonyosConfiguration.get(RESOURCE_MANAGER_CONFIGURATION), (DeviceCapability) createHarmonyosConfiguration.get(RESOURCE_MANAGER_DEVICECAPABILITY))) {
                        AppLog.w("ContextDeal::tryInitResourceManager successfully", new Object[0]);
                        this.resourceManager = this.resourceManagerInner.getResourceManager();
                        this.resourceState = HarmonyResources.getNewResourceState();
                        return true;
                    }
                    AppLog.e("ContextDeal::tryInitResourceManager result is false", new Object[0]);
                    return false;
                }
            }
            AppLog.w("ContextDeal::initResourceManager failed, class transform failed", new Object[0]);
            return false;
        } catch (IOException e) {
            AppLog.e("ContextDeal::tryInitResourceManager failed : %{public}s", e.getMessage());
        }
    }

    private void updateResourceManager(ResourceManager resourceManager2) {
        if (resourceManager2 == null) {
            AppLog.w("ContextDeal::updateResourceManager resourceManager is null", new Object[0]);
            return;
        }
        HashMap<String, Object> createHarmonyosConfiguration = createHarmonyosConfiguration();
        if (!(createHarmonyosConfiguration.get(RESOURCE_MANAGER_CONFIGURATION) instanceof Configuration) || !(createHarmonyosConfiguration.get(RESOURCE_MANAGER_DEVICECAPABILITY) instanceof DeviceCapability)) {
            AppLog.w("ContextDeal::initResourceManager failed, class transform failed", new Object[0]);
        } else {
            resourceManager2.updateConfiguration((Configuration) createHarmonyosConfiguration.get(RESOURCE_MANAGER_CONFIGURATION), (DeviceCapability) createHarmonyosConfiguration.get(RESOURCE_MANAGER_DEVICECAPABILITY));
        }
    }

    private HashMap<String, Object> createHarmonyosConfiguration() {
        HashMap<String, Object> hashMap = new HashMap<>();
        Context context = this.abilityShellContext;
        if (context == null) {
            hashMap.put(RESOURCE_MANAGER_CONFIGURATION, new Configuration());
            hashMap.put(RESOURCE_MANAGER_DEVICECAPABILITY, new DeviceCapability());
            return hashMap;
        }
        android.content.res.Configuration configuration = context.getResources().getConfiguration();
        hashMap.put(RESOURCE_MANAGER_CONFIGURATION, ResourceUtils.convert(configuration));
        hashMap.put(RESOURCE_MANAGER_DEVICECAPABILITY, ResourceUtils.convertToDeviceCapability(configuration));
        return hashMap;
    }

    private String getPreferenceDirectory(AbilityInfo abilityInfo2) {
        return getExpectedDirectory(abilityInfo2, PREFERENCES_DIRECTORY_NAME);
    }

    private String getDatabaseDirectory(AbilityInfo abilityInfo2) {
        String str;
        synchronized (CREATE_FILE_LOCK) {
            if (this.databasePath == null) {
                this.databasePath = getExpectedDirectory(abilityInfo2, DATABASE_DIRECTORY_NAME);
            }
            str = this.databasePath;
        }
        return str;
    }

    private String getDistributeDatabaseDirectory(AbilityInfo abilityInfo2) {
        return getExpectedDirectory(abilityInfo2, DISTRIBUTE_DATABASE_DIRECTORY_NAME);
    }

    private String getDistributeDirectory(AbilityInfo abilityInfo2) {
        synchronized (CREATE_FILE_LOCK) {
            if (this.distributedPath == null) {
                if (abilityInfo2 == null) {
                    AppLog.e("ContextDeal::getDistributeDirectory failed, abilityInfo is null", new Object[0]);
                    return null;
                }
                String bundleName = abilityInfo2.getBundleName();
                if (StringUtils.isBlank(bundleName)) {
                    AppLog.e("ContextDeal::getDistributeDirectory failed, bundleName is illegal", new Object[0]);
                    return null;
                }
                if (this.distributedFileManager == null) {
                    this.distributedFileManager = new DistributedFileManagerImpl();
                }
                String bundleDistributedDir = this.distributedFileManager.getBundleDistributedDir(bundleName);
                if (StringUtils.isBlank(bundleDistributedDir)) {
                    AppLog.e("ContextDeal::getDistributeDirectory failed, distributePath is illegal", new Object[0]);
                    HiViewUtil.sendDistributedDataEvent("getBundleDistributedDir", 0);
                    return null;
                }
                String[] split = abilityInfo2.getClassName().split("\\.");
                if (split.length == 0) {
                    AppLog.e("ContextDeal::getDistributeDirectory failed, ability className is illegal", new Object[0]);
                    return null;
                }
                String str = split[split.length - 1];
                if (!FileUtils.isLegalFileName(str)) {
                    AppLog.e("ContextDeal::getDistributeDirectory failed, ability directory name is illegal", new Object[0]);
                    return null;
                }
                this.distributedPath = FileUtils.getExpectedPath(bundleDistributedDir, File.separator, str);
            }
            return this.distributedPath;
        }
    }

    private String getExpectedDirectory(AbilityInfo abilityInfo2, String str) {
        if (abilityInfo2 == null) {
            AppLog.e("abilityInfo is null, this is application context", new Object[0]);
            String packageName = this.abilityShellContext.getPackageName();
            return DIRECTORY_DATA_DATA + packageName + File.separator + str;
        }
        String[] split = abilityInfo2.getClassName().split("\\.");
        if (split.length == 0) {
            AppLog.e("ContextDeal::getDistributeDirectory failed, ability className is illegal", new Object[0]);
            return null;
        }
        String str2 = split[split.length - 1];
        if (!FileUtils.isLegalFileName(str2)) {
            AppLog.e("ContextDeal::getExpectedDirectory failed, ability directory name is illegal", new Object[0]);
            return null;
        }
        String appDataPath = getAppDataPath();
        if (!StringUtils.isBlank(appDataPath)) {
            return FileUtils.getExpectedPath(appDataPath, File.separator, str2, File.separator, str);
        }
        AppLog.e("ContextDeal::getExpectedDirectory failed, dataPath is illegal", new Object[0]);
        return null;
    }

    private boolean stopAbilityInner(Intent intent) {
        AbilityShellData selectAbility = selectAbility(intent);
        if (selectAbility == null) {
            AppLog.e("ContextDeal::stopAbility selectAbility failed", new Object[0]);
            return false;
        } else if (selectAbility.getLocal()) {
            return stopLocalAbility(selectAbility, intent);
        } else {
            int stopRemoteAbility = stopRemoteAbility(intent, selectAbility.getAbilityInfo());
            if (stopRemoteAbility != 0) {
                AppLog.e("ContextDeal::stopRemoteAbility failed code is %{public}d", Integer.valueOf(stopRemoteAbility));
            }
            if (stopRemoteAbility == 0) {
                return true;
            }
            return false;
        }
    }

    private AbilityShellData selectAbility(Intent intent) {
        return selectAbilityInner(intent);
    }

    private AbilityShellData getAbilityShellDataLocal(Intent intent) {
        BundleInfo bundleInfo;
        Application application2 = this.application;
        if (application2 != null) {
            bundleInfo = application2.getBundleInfo();
        } else {
            bundleInfo = this.bundleMgrImpl.getBundleInfo(getBundleName(), 1);
        }
        if (bundleInfo == null) {
            AppLog.e("ContextDeal::getAbilityShellDataLocal failed, bundleInfo is empty", new Object[0]);
            return null;
        } else if (intent.getElement() == null || !this.abilityShellContext.getPackageName().equals(intent.getElement().getBundleName())) {
            List<AbilityInfo> queryAbilityByIntent = new BundleMgrBridge().queryAbilityByIntent(intent);
            if (queryAbilityByIntent == null || queryAbilityByIntent.size() <= 0) {
                return null;
            }
            return new AbilityShellData(true, queryAbilityByIntent.get(0), AbilityShellConverterUtils.convertToShellInfo(queryAbilityByIntent.get(0)));
        } else {
            AbilityInfo abilityInfoByName = bundleInfo.getAbilityInfoByName(getFullClassName(intent.getElement().getBundleName(), intent.getElement().getAbilityName()));
            if (abilityInfoByName != null) {
                return new AbilityShellData(true, abilityInfoByName, AbilityShellConverterUtils.convertToShellInfoSupportDiffPkg(abilityInfoByName, bundleInfo));
            }
            AppLog.e("ContextDeal::tmpInfo is null;AbilityName: %{public}s.", intent.getElement().getAbilityName());
            return null;
        }
    }

    private AbilityShellData getAbilityShellDataRemote(Intent intent) {
        try {
            return this.distributedImpl.selectAbility(intent);
        } catch (RemoteException e) {
            AppLog.e("ContextDeal::selectAbility RemoteException: %{public}s", e.getMessage());
            return null;
        }
    }

    private AbilityShellData selectAbilityInner(Intent intent) {
        AbilityShellData abilityShellData = null;
        if (intent == null) {
            AppLog.e("ContextDeal::selectAbilityInner failed, intent is empty.", new Object[0]);
            return null;
        } else if (intent.getElement() == null || intent.getElement().getDeviceId() == null || intent.getElement().getDeviceId().isEmpty() || intent.getElement().getDeviceId().equals("")) {
            if (!isFlagExists(256, intent.getFlags())) {
                abilityShellData = getAbilityShellDataLocal(intent);
            }
            if (abilityShellData != null) {
                return abilityShellData;
            }
            if (AbilityShellConverterUtils.isAndroidComponent(this.abilityShellContext, intent)) {
                return AbilityShellConverterUtils.createAbilityShellData(intent, true);
            }
            return getAbilityShellDataRemote(intent);
        } else if (intent.getElement().getDeviceId().equals(this.application.getLocalDeviceId())) {
            return getAbilityShellDataLocal(intent);
        } else {
            return getAbilityShellDataRemote(intent);
        }
    }

    private static String getFullClassName(String str, String str2) {
        if (!str2.isEmpty() && str2.charAt(0) == '.') {
            return str + str2;
        } else if (str2.startsWith(str)) {
            return str2;
        } else {
            return str + '.' + str2;
        }
    }

    private boolean stopLocalAbility(AbilityShellData abilityShellData, Intent intent) {
        ShellInfo shellInfo = abilityShellData.getShellInfo();
        if (shellInfo.getType() != ShellInfo.ShellType.SERVICE) {
            AppLog.e("ContextDeal::stopLocalAbility ShellType not SERVICE", new Object[0]);
            return false;
        }
        Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
        if (!createAndroidIntent.isPresent()) {
            AppLog.e("ContextDeal::stopLocalAbility createAndroidIntent failed", new Object[0]);
            return false;
        }
        this.abilityShellContext.stopService(createAndroidIntent.get());
        return true;
    }

    private int stopRemoteAbility(Intent intent, AbilityInfo abilityInfo2) {
        int i;
        try {
            i = this.distributedImpl.stopRemoteAbility(intent, abilityInfo2);
        } catch (RemoteException e) {
            AppLog.e("ContextDeal::stopRemoteAbility RemoteException: %{public}s", e.getMessage());
            i = -1;
        }
        checkDmsInterfaceResult(i, "stopRemoteAbility");
        return i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<AbilityShellData> getAbilityShellDatasLocal(Intent intent) {
        BundleInfo bundleInfo;
        ArrayList arrayList = new ArrayList();
        Application application2 = this.application;
        if (application2 != null) {
            bundleInfo = application2.getBundleInfo();
        } else {
            bundleInfo = this.bundleMgrImpl.getBundleInfo(getBundleName(), 1);
        }
        if (bundleInfo == null) {
            AppLog.e("ContextDeal::getAbilityShellDatasLocal failed, bundleInfo is empty", new Object[0]);
            return arrayList;
        }
        if (intent.getElement() != null && this.abilityShellContext.getPackageName().equals(intent.getElement().getBundleName())) {
            AbilityInfo abilityInfoByName = bundleInfo.getAbilityInfoByName(getFullClassName(intent.getElement().getBundleName(), intent.getElement().getAbilityName()));
            if (abilityInfoByName != null) {
                AppLog.e("ContextDeal::tmpInfo is null;AbilityName: %{public}s.", intent.getElement().getAbilityName());
                arrayList.add(new AbilityShellData(true, abilityInfoByName, AbilityShellConverterUtils.convertToShellInfoSupportDiffPkg(abilityInfoByName, bundleInfo)));
                return arrayList;
            } else if (!isFlagExists(2048, intent.getFlags())) {
                return arrayList;
            }
        }
        List<AbilityInfo> queryAbilityByIntent = new BundleMgrBridge().queryAbilityByIntent(intent);
        if (queryAbilityByIntent != null) {
            for (AbilityInfo abilityInfo2 : queryAbilityByIntent) {
                arrayList.add(new AbilityShellData(true, abilityInfo2, AbilityShellConverterUtils.convertToShellInfoSupportDiffPkg(abilityInfo2, bundleInfo)));
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private InstallerCallback createFreeInstallCallback(final Intent intent, final int i, final AbilityShellData abilityShellData, final IBundleManager iBundleManager) {
        return new InstallerCallback() {
            /* class ohos.app.ContextDeal.AnonymousClass1 */

            @Override // ohos.bundle.InstallerCallback, ohos.bundle.IInstallerCallback
            public void onFinished(int i, String str) {
                if (i != 0) {
                    try {
                        iBundleManager.showErrorMessage(abilityShellData.getAbilityInfo().getBundleName(), i == 12 ? 2 : 3);
                    } catch (RemoteException e) {
                        AppLog.e(ContextDeal.APPKIT_LABEL, "ContextDeal::startFreeInstallAbility show error message failed, errormsg = %{public}s", e.getMessage());
                    }
                } else {
                    ContextDeal.this.startLocalAbility(abilityShellData, intent, i);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getIntentUri(AbilityShellData abilityShellData, Intent intent) {
        Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, abilityShellData.getShellInfo());
        if (!createAndroidIntent.isPresent()) {
            AppLog.e("ContextDeal::getIntentUri createAndroidIntent failed", new Object[0]);
            return null;
        } else if (createAndroidIntent.get().getComponent() == null) {
            AppLog.e("ContextDeal::getIntentUri ComponentName is null", new Object[0]);
            return null;
        } else {
            handleForwardFlag(intent, createAndroidIntent.get());
            if (this.abilityShellContext instanceof Service) {
                createAndroidIntent.get().addFlags(268435456);
            }
            return createAndroidIntent.get().toUri(0);
        }
    }

    private void startFreeInstallAbility(final Intent intent, final int i) {
        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(new Runnable() {
            /* class ohos.app.ContextDeal.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                List abilityShellDatasLocal = ContextDeal.this.getAbilityShellDatasLocal(intent);
                if (abilityShellDatasLocal.size() != 1) {
                    AppLog.e(ContextDeal.APPKIT_LABEL, "ContextDeal::startFreeInstallAbility getAbilityShellDatasLocal error, result size(=%{public}d) != 1", Integer.valueOf(abilityShellDatasLocal.size()));
                    return;
                }
                Intent intent = new Intent(intent);
                intent.removeFlags(2048);
                AbilityShellData abilityShellData = (AbilityShellData) abilityShellDatasLocal.get(0);
                String deviceId = abilityShellData.getAbilityInfo().getDeviceId();
                if (deviceId == null || deviceId.isEmpty()) {
                    IBundleManager bundleManager = ContextDeal.this.getBundleManager();
                    if (bundleManager == null) {
                        AppLog.e(ContextDeal.APPKIT_LABEL, "ContextDeal::getBundleInstaller getBundleManager failed", new Object[0]);
                        return;
                    }
                    try {
                        bundleManager.downloadAndInstallWithParam(abilityShellData.getAbilityInfo(), false, ContextDeal.this.createFreeInstallCallback(intent, i, abilityShellData, bundleManager), ContextDeal.this.getIntentUri(abilityShellData, intent));
                    } catch (RemoteException e) {
                        AppLog.e(ContextDeal.APPKIT_LABEL, "ContextDeal::startFreeInstallAbility download and install failed, errormsg = %{public}s", e.getMessage());
                    }
                } else {
                    ContextDeal.this.startLocalAbility(abilityShellData, intent, i);
                }
            }
        });
    }

    private void cancelDownload(final Intent intent) {
        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(new Runnable() {
            /* class ohos.app.ContextDeal.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                List abilityShellDatasLocal = ContextDeal.this.getAbilityShellDatasLocal(intent);
                if (abilityShellDatasLocal.size() != 1) {
                    AppLog.e("ContextDeal::cancelDownload getAbilityShellDatasLocal error, result size(=%{public}d) != 1", Integer.valueOf(abilityShellDatasLocal.size()));
                    return;
                }
                AbilityShellData abilityShellData = (AbilityShellData) abilityShellDatasLocal.get(0);
                if (abilityShellData.getAbilityInfo() == null) {
                    AppLog.e("ContextDeal::cancelDownload get AbilityInfo failed", new Object[0]);
                    return;
                }
                IBundleManager bundleManager = ContextDeal.this.getBundleManager();
                if (bundleManager == null) {
                    AppLog.e("ContextDeal::getBundleInstaller getBundleManager failed", new Object[0]);
                    return;
                }
                try {
                    bundleManager.cancelDownload(abilityShellData.getAbilityInfo());
                } catch (RemoteException e) {
                    AppLog.e("ContextDeal::cancelDownload failed, errormsg = %{public}s", e.getMessage());
                }
            }
        });
    }

    private void startAbilityInner(final Intent intent, final int i) {
        List<AbilityShellData> arrayList = new ArrayList<>();
        if (intent.getElement() == null || intent.getElement().getDeviceId() == null || intent.getElement().getDeviceId().isEmpty() || intent.getElement().getDeviceId().equals("")) {
            if (!isFlagExists(256, intent.getFlags())) {
                arrayList = getAbilityShellDatasLocal(intent);
            }
            if (arrayList.isEmpty()) {
                arrayList = SelectAbilityUtils.fetchAbilities(this.abilityShellContext, intent);
            }
        } else if (intent.getElement().getDeviceId().equals(this.application.getLocalDeviceId())) {
            arrayList = getAbilityShellDatasLocal(intent);
        } else {
            arrayList = SelectAbilityUtils.fetchAbilities(this.abilityShellContext, intent);
        }
        if (arrayList == null || arrayList.isEmpty()) {
            AppLog.e("ContextDeal::startAbility fetchAbilities failed", new Object[0]);
        } else if (arrayList.size() == 1) {
            performStartAblilityInner(intent, i, arrayList.get(0));
        } else if (isFlagExists(256, intent.getFlags())) {
            new AbilityResolver(this.abilityShellContext, arrayList, new AbilityResolver.IResolveResult() {
                /* class ohos.app.ContextDeal.AnonymousClass4 */

                @Override // ohos.abilityshell.utils.AbilityResolver.IResolveResult
                public void onResolveResult(AbilityShellData abilityShellData) {
                    ContextDeal.this.performStartAblilityInner(intent, i, abilityShellData);
                }
            }).show();
        } else {
            new AbilityResolverSingleDevice(this.abilityShellContext, arrayList, new AbilityResolverSingleDevice.IResolveResult() {
                /* class ohos.app.ContextDeal.AnonymousClass5 */

                @Override // ohos.abilityshell.utils.AbilityResolverSingleDevice.IResolveResult
                public void onResolveResult(AbilityShellData abilityShellData) {
                    ContextDeal.this.performStartAblilityInner(intent, i, abilityShellData);
                }
            }).show();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performStartAblilityInner(Intent intent, int i, AbilityShellData abilityShellData) {
        if (abilityShellData != null) {
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 != null) {
                intent.setParam(CALLER_BUNDLE_NAME, abilityInfo2.getBundleName());
            }
            if (abilityShellData.getLocal()) {
                long currentTimeMillis = System.currentTimeMillis();
                startLocalAbility(abilityShellData, intent, i);
                AbilityInfo abilityInfo3 = this.abilityInfo;
                if (abilityInfo3 != null) {
                    JLogUtil.debugLog(JLogConstants.JLID_ABILITY_SHELL_START_REMOTE_ABILITY, abilityInfo3.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
                    return;
                }
                return;
            }
            long currentTimeMillis2 = System.currentTimeMillis();
            int startRemoteAbility = startRemoteAbility(intent, abilityShellData.getAbilityInfo(), i);
            JLogUtil.printStartAbilityInfo(intent, currentTimeMillis2, startRemoteAbility);
            if (startRemoteAbility != 0) {
                AppLog.e("ContextDeal::startRemoteAbility failed code is %{public}d", Integer.valueOf(startRemoteAbility));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startLocalAbility(AbilityShellData abilityShellData, Intent intent, int i) {
        ShellInfo shellInfo = abilityShellData.getShellInfo();
        Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
        if (!createAndroidIntent.isPresent()) {
            AppLog.e("ContextDeal::startLocalAbility createAndroidIntent failed", new Object[0]);
        } else if (createAndroidIntent.get().getComponent() == null) {
            AppLog.e("ContextDeal::startLocalAbility ComponentName is null", new Object[0]);
        } else {
            handleForwardFlag(intent, createAndroidIntent.get());
            if (this.abilityShellContext instanceof Service) {
                createAndroidIntent.get().addFlags(268435456);
            }
            if (shellInfo.getType() == ShellInfo.ShellType.ACTIVITY) {
                if (isRequestCodeValid(i)) {
                    startLocalActivityForResult(createAndroidIntent.get(), i);
                } else {
                    startLocalActivity(createAndroidIntent.get());
                }
            } else if (shellInfo.getType() == ShellInfo.ShellType.SERVICE) {
                if (isFlagExists(512, intent.getFlags())) {
                    startForegroundService(createAndroidIntent.get());
                } else {
                    startLocalService(createAndroidIntent.get());
                }
            } else if (shellInfo.getType() == ShellInfo.ShellType.WEB) {
                android.content.Intent intent2 = new android.content.Intent();
                intent2.setAction("android.intent.action.VIEW");
                String str = WEB_ABILITY_DEEPLINK_SCHEME + intent.getElement().getAbilityName();
                intent2.setData(android.net.Uri.parse(str));
                AppLog.i("ContextDeal::startLocalAbility deepLinkUri %{public}s, intent %{public}s", str, intent.toString());
                startLocalActivity(intent2);
            } else {
                AppLog.w("ContextDeal::startLocalAbility Unknown ShellType", new Object[0]);
            }
        }
    }

    private void handleForwardFlag(Intent intent, android.content.Intent intent2) {
        if ((intent.getFlags() & 4) != 0) {
            AppLog.i("ContextDeal::handleForwardFlag have FLAG_ABILITY_FORWARD_RESULT", new Object[0]);
            intent2.setFlags(intent2.getFlags() | 33554432);
        }
    }

    private void startLocalActivity(android.content.Intent intent) {
        AppLog.i("ContextDeal::startLocalActivity called", new Object[0]);
        try {
            this.abilityShellContext.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            AppLog.e("ContextDeal::startLocalActivity ability is not found, intent %{public}s", intent.toString());
        }
    }

    private void startLocalActivityForResult(android.content.Intent intent, int i) {
        try {
            if (this.abilityShellContext instanceof Activity) {
                ((Activity) this.abilityShellContext).startActivityForResult(intent, i);
            } else if (this.abilityShellContext instanceof Application) {
                ((Application) this.abilityShellContext).startActivity(intent);
            } else {
                AppLog.w("ContextDeal::startLocalActivityForResult only Activity support", new Object[0]);
            }
        } catch (ActivityNotFoundException unused) {
            AppLog.e("ContextDeal::startLocalActivityForResult Ability not found", new Object[0]);
        }
    }

    private void startLocalService(android.content.Intent intent) {
        try {
            this.abilityShellContext.startService(intent);
        } catch (SecurityException unused) {
            AppLog.e("ContextDeal::startLocalAbility ability not found", new Object[0]);
        }
    }

    private void startForegroundService(android.content.Intent intent) {
        try {
            this.abilityShellContext.startForegroundService(intent);
        } catch (SecurityException unused) {
            AppLog.e("ContextDeal::startLocalAbility ability not found", new Object[0]);
        }
    }

    private int startRemoteAbility(Intent intent, AbilityInfo abilityInfo2, int i) {
        HiTraceId begin = HiTrace.begin("startRemoteAbility", 1);
        int startRemoteAbilityInner = startRemoteAbilityInner(intent, abilityInfo2, i);
        HiTrace.end(begin);
        return startRemoteAbilityInner;
    }

    private int startRemoteAbilityInner(Intent intent, AbilityInfo abilityInfo2, int i) {
        int i2;
        try {
            i2 = this.distributedImpl.startRemoteAbility(intent, abilityInfo2, i);
        } catch (RemoteException e) {
            AppLog.e("ContextDeal::startRemoteAbility RemoteException: %{public}s", e.getMessage());
            i2 = -1;
        }
        checkDmsInterfaceResult(i2, "startRemoteAbility");
        return i2;
    }

    private boolean connectAbilityInner(Intent intent, IAbilityConnection iAbilityConnection) {
        AbilityShellData abilityShellData;
        if (isFlagExists(32, intent.getFlags())) {
            abilityShellData = selectFormAbility(intent, ShellInfo.ShellType.SERVICE);
        } else {
            abilityShellData = selectAbility(intent);
        }
        if (abilityShellData == null) {
            AppLog.e("ContextDeal::connectAbility selectAbility failed", new Object[0]);
            return false;
        }
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 != null) {
            intent.setParam(CALLER_BUNDLE_NAME, abilityInfo2.getBundleName());
        }
        if (abilityShellData.getLocal()) {
            return connectLocalAbility(abilityShellData, intent, iAbilityConnection);
        }
        return connectRemoteAbility(abilityShellData, intent, iAbilityConnection);
    }

    private boolean connectLocalAbility(AbilityShellData abilityShellData, Intent intent, IAbilityConnection iAbilityConnection) {
        ServiceConnection serviceConnection;
        ShellInfo shellInfo = abilityShellData.getShellInfo();
        AbilityInfo abilityInfo2 = abilityShellData.getAbilityInfo();
        if (shellInfo.getType() != ShellInfo.ShellType.SERVICE) {
            AppLog.e("ContextDeal::connectLocalAbility Ability not SERVICE", new Object[0]);
            return false;
        }
        Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
        if (!createAndroidIntent.isPresent()) {
            AppLog.e("ContextDeal::connectLocalAbility createAndroidIntent failed", new Object[0]);
            return false;
        } else if (createAndroidIntent.get().getComponent() == null) {
            AppLog.e("ContextDeal::connectLocalAbility ComponentName is null", new Object[0]);
            return false;
        } else {
            if (this.abilityConnectionMap.containsKey(iAbilityConnection)) {
                Object obj = this.abilityConnectionMap.get(iAbilityConnection);
                if (obj instanceof ServiceConnection) {
                    serviceConnection = (ServiceConnection) obj;
                } else {
                    AppLog.e("ContextDeal::connectAbility not ServiceConnection type", new Object[0]);
                    return false;
                }
            } else {
                serviceConnection = createServiceConnection(iAbilityConnection, abilityInfo2);
                this.abilityConnectionMap.put(iAbilityConnection, serviceConnection);
            }
            return this.abilityShellContext.bindService(createAndroidIntent.get(), serviceConnection, 1);
        }
    }

    private ServiceConnection createServiceConnection(final IAbilityConnection iAbilityConnection, final AbilityInfo abilityInfo2) {
        return new ServiceConnection() {
            /* class ohos.app.ContextDeal.AnonymousClass6 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                AppLog.i("ContextDeal::onServiceConnected", new Object[0]);
                Optional<ElementName> createElementName = IntentConverter.createElementName(componentName, abilityInfo2);
                if (!createElementName.isPresent()) {
                    AppLog.e("ContextDeal::onServiceConnected createElementName failed", new Object[0]);
                    return;
                }
                Bytrace.startTrace(2147483648L, "binderConverter_z");
                Optional translateToIRemoteObject = IPCAdapter.translateToIRemoteObject(iBinder);
                Bytrace.finishTrace(2147483648L, "binderConverter_z");
                translateToIRemoteObject.ifPresent(new Consumer(createElementName) {
                    /* class ohos.app.$$Lambda$ContextDeal$6$uCqJsmUEH3wTOEeWVwoJimMWhQ */
                    private final /* synthetic */ Optional f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        IAbilityConnection.this.onAbilityConnectDone((ElementName) this.f$1.get(), (IRemoteObject) obj, 0);
                    }
                });
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                AppLog.i("ContextDeal::onServiceDisconnected", new Object[0]);
                Optional<ElementName> createElementName = IntentConverter.createElementName(componentName, abilityInfo2);
                if (!createElementName.isPresent()) {
                    AppLog.e("ContextDeal::onServiceConnected createElementName failed", new Object[0]);
                } else {
                    iAbilityConnection.onAbilityDisconnectDone(createElementName.get(), 0);
                }
            }
        };
    }

    private boolean connectRemoteAbility(AbilityShellData abilityShellData, Intent intent, IAbilityConnection iAbilityConnection) {
        HiTraceId begin = HiTrace.begin("connectRemoteAbility", 1);
        boolean connectRemoteAbilityInner = connectRemoteAbilityInner(abilityShellData, intent, iAbilityConnection);
        HiTrace.end(begin);
        return connectRemoteAbilityInner;
    }

    private boolean connectRemoteAbilityInner(AbilityShellData abilityShellData, Intent intent, IAbilityConnection iAbilityConnection) {
        DistributedConnection distributedConnection;
        AppLog.i("ContextDeal::connectRemoteAbility called", new Object[0]);
        AbilityInfo abilityInfo2 = abilityShellData.getAbilityInfo();
        if (this.abilityConnectionMap.containsKey(iAbilityConnection)) {
            Object obj = this.abilityConnectionMap.get(iAbilityConnection);
            if (obj instanceof DistributedConnection) {
                distributedConnection = (DistributedConnection) obj;
            } else {
                AppLog.e("ContextDeal::connectRemoteAbility not DistributedConnection type", new Object[0]);
                return false;
            }
        } else {
            DistributedConnection createDistributedConnection = createDistributedConnection(iAbilityConnection, new Handler(Looper.getMainLooper()));
            this.abilityConnectionMap.put(iAbilityConnection, createDistributedConnection);
            distributedConnection = createDistributedConnection;
        }
        int i = -1;
        try {
            i = this.distributedImpl.connectRemoteAbility(intent, abilityInfo2, distributedConnection.asObject());
        } catch (RemoteException e) {
            AppLog.e("ContextDeal::connectRemoteAbility RemoteException: %{public}s", e.getMessage());
        }
        if (i == 0) {
            return true;
        }
        AppLog.e("Context::connectRemoteAbility failed, errorCode is %{public}d", Integer.valueOf(i));
        return false;
    }

    private DistributedConnection createDistributedConnection(IAbilityConnection iAbilityConnection, Handler handler) {
        return new DistributedConnection(iAbilityConnection, handler);
    }

    private AbilityShellData selectFormAbility(Intent intent, ShellInfo.ShellType shellType) {
        List<AbilityInfo> queryAbilityByIntent = this.bundleMgrImpl.queryAbilityByIntent(intent);
        if (queryAbilityByIntent == null || queryAbilityByIntent.isEmpty()) {
            AppLog.e("ContextDeal::selectFormAbility failed", new Object[0]);
            return null;
        }
        AbilityInfo abilityInfo2 = queryAbilityByIntent.get(0);
        ShellInfo convertToFormShellInfo = AbilityShellConverterUtils.convertToFormShellInfo(abilityInfo2, shellType);
        if (convertToFormShellInfo != null) {
            return new AbilityShellData(true, abilityInfo2, convertToFormShellInfo);
        }
        AppLog.e("ContextDeal::selectFormAbility failed", new Object[0]);
        return null;
    }

    private void disconnectAbilityInner(HashMap<IAbilityConnection, Object> hashMap, IAbilityConnection iAbilityConnection) {
        Object obj = hashMap.get(iAbilityConnection);
        if (obj == null) {
            AppLog.e("ContextDeal::disconnectAbility IAbilityConnection not found", new Object[0]);
            return;
        }
        if (obj instanceof ServiceConnection) {
            disconnectLocalAbility(obj);
        }
        if (obj instanceof DistributedConnection) {
            disconnectRemoteAbility(obj);
        }
        hashMap.remove(iAbilityConnection);
    }

    private void disconnectLocalAbility(Object obj) {
        if (!(obj instanceof ServiceConnection)) {
            AppLog.e("ContextDeal::disconnectLocalAbility param not ServiceConnection", new Object[0]);
        } else {
            this.abilityShellContext.unbindService((ServiceConnection) obj);
        }
    }

    private void disconnectRemoteAbility(Object obj) {
        if (!(obj instanceof IRemoteObject)) {
            AppLog.e("ContextDeal::disconnectRemoteAbility param not IRemoteObject", new Object[0]);
            return;
        }
        int i = -1;
        try {
            i = this.distributedImpl.disconnectRemoteAbility((IRemoteObject) obj);
        } catch (RemoteException e) {
            AppLog.e("ContextDeal::connectRemoteAbility RemoteException: %{public}s", e.getMessage());
        }
        checkDmsInterfaceResult(i, "disconnectRemoteAbility");
    }

    private void checkDmsInterfaceResult(int i, String str) {
        if (i != 0) {
            AppLog.e("ContextDeal::checkDmsInterfaceResult %{private}s failed, result is %{private}d", str, Integer.valueOf(i));
        }
    }

    private ElementName convertComponentNameToElementName(ComponentName componentName) {
        if (componentName == null) {
            return null;
        }
        ShellInfo shellInfo = new ShellInfo();
        shellInfo.setPackageName(componentName.getPackageName());
        shellInfo.setName(componentName.getClassName());
        shellInfo.setType(ShellInfo.ShellType.ACTIVITY);
        return IntentConverter.createElementName(null, AbilityShellConverterUtils.convertToAbilityInfo(shellInfo)).orElse(null);
    }

    @Override // ohos.app.Context
    public IBundleManager getBundleManager() {
        BundleManager instance = BundleManager.getInstance();
        if (instance != null) {
            instance.setContext(getApplicationContext());
        }
        return instance;
    }

    @Override // ohos.app.Context
    public boolean isUpdatingConfigurations() {
        Context context = this.abilityShellContext;
        if (context instanceof Activity) {
            return ((Activity) context).isChangingConfigurations();
        }
        AppLog.i("ContextDeal::setTurnScreenOn, ability is not instance of AbilityShellActivity", new Object[0]);
        return false;
    }

    @Override // ohos.app.Context
    public String getAppType() {
        IBundleManager bundleManager = getBundleManager();
        if (bundleManager == null) {
            AppLog.e("get bundleManager failed", new Object[0]);
            return null;
        }
        try {
            return bundleManager.getAppType(getBundleName());
        } catch (RemoteException unused) {
            AppLog.e("remote exception", new Object[0]);
            return null;
        }
    }

    @Override // ohos.app.Context
    public Object getLayoutScatter() {
        if (this.layoutScatter == null) {
            this.layoutScatter = LayoutScatter.getInstance(this);
        }
        return this.layoutScatter;
    }

    @Override // ohos.app.Context
    public final boolean isAllowClassMap() {
        HapModuleInfo hapModuleInfo2 = this.hapModuleInfo;
        if (hapModuleInfo2 != null) {
            return hapModuleInfo2.isAllowClassMap();
        }
        return false;
    }
}
