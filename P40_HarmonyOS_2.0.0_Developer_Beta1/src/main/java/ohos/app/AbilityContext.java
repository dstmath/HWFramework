package ohos.app;

import java.io.File;
import java.util.List;
import java.util.Optional;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.ability.IDataAbility;
import ohos.aafwk.ability.MissionInformation;
import ohos.aafwk.ability.startsetting.AbilityStartSetting;
import ohos.aafwk.content.Intent;
import ohos.annotation.SystemApi;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ApplicationInfo;
import ohos.bundle.ElementName;
import ohos.bundle.HapModuleInfo;
import ohos.bundle.IBundleManager;
import ohos.global.configuration.Configuration;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.solidxml.Pattern;
import ohos.global.resource.solidxml.Theme;
import ohos.security.permission.UriPermissionDef;
import ohos.utils.net.Uri;

public abstract class AbilityContext implements Context, IAbilityLoader {
    private Context context;
    private Context serviceContext;
    private Object serviceShell;
    private Object shell;

    public AbilityContext() {
    }

    public AbilityContext(Context context2) {
        init(context2);
    }

    @Deprecated
    public void init(Context context2, Object obj) {
        if (obj != null) {
            this.shell = obj;
            init(context2);
            return;
        }
        throw new IllegalStateException("shell is null");
    }

    public void attachBaseContext(Context context2) {
        this.context = context2;
    }

    public void setAbilityShell(Object obj) {
        this.shell = obj;
    }

    public final Object getAbilityShell() {
        return this.shell;
    }

    public final Context getContext() {
        return this.context;
    }

    public final void saveServiceContext() {
        this.serviceContext = this.context;
        this.serviceShell = this.shell;
    }

    public final void restoreServiceContext() {
        Context context2 = this.serviceContext;
        if (context2 != null) {
            this.context = context2;
        }
        Object obj = this.serviceShell;
        if (obj != null) {
            this.shell = obj;
        }
    }

    @Deprecated
    public void init(Context context2) {
        onLoadAbility();
    }

    public void init() {
        onLoadAbility();
    }

    @Override // ohos.app.Context
    public final TaskDispatcher getUITaskDispatcher() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getUITaskDispatcher();
        }
        AppLog.e("AbilityContext::getUITaskDispatcher context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public final TaskDispatcher getMainTaskDispatcher() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getMainTaskDispatcher();
        }
        AppLog.e("AbilityContext::getMainTaskDispatcher context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public TaskDispatcher createParallelTaskDispatcher(String str, TaskPriority taskPriority) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.createParallelTaskDispatcher(str, taskPriority);
        }
        AppLog.e("AbilityContext::createParallelTaskDispatcher context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public TaskDispatcher createSerialTaskDispatcher(String str, TaskPriority taskPriority) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.createSerialTaskDispatcher(str, taskPriority);
        }
        AppLog.e("AbilityContext::createSerialTaskDispatcher context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public TaskDispatcher getGlobalTaskDispatcher(TaskPriority taskPriority) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getGlobalTaskDispatcher(taskPriority);
        }
        AppLog.e("AbilityContext::getGlobalTaskDispatcher context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public ApplicationInfo getApplicationInfo() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getApplicationInfo();
        }
        AppLog.e("AbilityContext::getApplicationInfo context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public AbilityInfo getAbilityInfo() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getAbilityInfo();
        }
        AppLog.e("AbilityContext::getAbilityInfo context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public ProcessInfo getProcessInfo() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getProcessInfo();
        }
        AppLog.e("AbilityContext::getProcessInfo context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.IAbilityLoader
    public void onLoadAbility() {
        Context context2 = this.context;
        if (context2 instanceof IAbilityLoader) {
            ((IAbilityLoader) context2).onLoadAbility();
        } else {
            AppLog.w("AbilityContext::onLoadAbility failed, context is not ContextDeal", new Object[0]);
        }
    }

    @Override // ohos.app.Context
    public ResourceManager getResourceManager() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getResourceManager();
        }
        AppLog.e("AbilityContext::getResourceManager context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public IDataAbility getDataAbility(Uri uri) {
        if (this.context == null) {
            AppLog.e("AbilityContext::getDataAbility one param context is null", new Object[0]);
            return null;
        } else if (uri != null) {
            return getDataAbility(uri, false);
        } else {
            AppLog.e("AbilityContext::getDataAbility one param invalid", new Object[0]);
            return null;
        }
    }

    @Override // ohos.app.Context
    public IDataAbility getDataAbility(Uri uri, boolean z) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::getDataAbility two param context is null", new Object[0]);
            return null;
        } else if (uri != null) {
            return context2.getDataAbility(uri, z);
        } else {
            AppLog.e("AbilityContext::getDataAbility two param invalid", new Object[0]);
            return null;
        }
    }

    @Override // ohos.app.Context
    public boolean releaseDataAbility(IDataAbility iDataAbility) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::releaseDataAbility context is null", new Object[0]);
            return false;
        } else if (iDataAbility != null) {
            return context2.releaseDataAbility(iDataAbility);
        } else {
            AppLog.e("AbilityContext::releaseDataAbility dataAbility is null", new Object[0]);
            return false;
        }
    }

    @Override // ohos.app.Context
    public File getPreferencesDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getPreferencesDir();
        }
        AppLog.e("AbilityContext::getPreferencesDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File getDatabaseDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getDatabaseDir();
        }
        AppLog.e("AbilityContext::getDatabaseDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File getDistributedDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getDistributedDir();
        }
        AppLog.e("AbilityContext::getDistributedDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public void switchToDeviceEncryptedStorageContext() {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::switchToDeviceEncryptedStorageContext context is null", new Object[0]);
        } else {
            context2.switchToDeviceEncryptedStorageContext();
        }
    }

    @Override // ohos.app.Context
    public void switchToCredentialEncryptedStorageContext() {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::switchToCredentialEncryptedStorageContext context is null", new Object[0]);
        } else {
            context2.switchToCredentialEncryptedStorageContext();
        }
    }

    @Override // ohos.app.Context
    public boolean isDeviceEncryptedStorage() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.isDeviceEncryptedStorage();
        }
        AppLog.e("AbilityContext::isDeviceEncryptedStorage context is null", new Object[0]);
        return false;
    }

    @Override // ohos.app.Context
    public boolean isCredentialEncryptedStorage() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.isCredentialEncryptedStorage();
        }
        AppLog.e("AbilityContext::isCredentialEncryptedStorage context is null", new Object[0]);
        return false;
    }

    @Override // ohos.app.Context
    @SystemApi
    public void makePermanentUriPermission(Uri uri, int i) throws DataAbilityRemoteException {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::makePermanentUriPermission context is null", new Object[0]);
        } else {
            context2.makePermanentUriPermission(uri, i);
        }
    }

    @Override // ohos.app.Context
    @SystemApi
    public void giveUpPermanentUriPermission(Uri uri, int i) throws DataAbilityRemoteException {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::giveUpPermanentUriPermission context is null", new Object[0]);
        } else {
            context2.giveUpPermanentUriPermission(uri, i);
        }
    }

    @Override // ohos.app.Context
    @SystemApi
    public int verifyCallingUriPermission(Uri uri, int i) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.verifyCallingUriPermission(uri, i);
        }
        AppLog.e("AbilityContext::authUriPermission context is null", new Object[0]);
        return -1;
    }

    @Override // ohos.app.Context
    @SystemApi
    public List<UriPermissionDef> getGrantedPermanentUriPermissionDefs() throws DataAbilityRemoteException {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getGrantedPermanentUriPermissionDefs();
        }
        AppLog.e("AbilityContext::getGrantedPermanentUriPermissionDefs context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    @SystemApi
    public List<UriPermissionDef> getHostedPermanentUriPermissionDefs() throws DataAbilityRemoteException {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getHostedPermanentUriPermissionDefs();
        }
        AppLog.e("AbilityContext::getHostedPermanentUriPermissionDefs context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    @SystemApi
    public void authUriPermission(String str, Uri uri, int i) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::authUriPermission context is null", new Object[0]);
        } else {
            context2.authUriPermission(str, uri, i);
        }
    }

    @Override // ohos.app.Context
    @SystemApi
    public void unauthUriPermission(String str, Uri uri, int i) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::authUriPermission context is null", new Object[0]);
        } else {
            context2.unauthUriPermission(str, uri, i);
        }
    }

    @Override // ohos.app.Context
    @SystemApi
    public int verifyUriPermission(Uri uri, int i, int i2, int i3) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.verifyUriPermission(uri, i, i2, i3);
        }
        AppLog.e("AbilityContext::authUriPermission context is null", new Object[0]);
        return -1;
    }

    @Override // ohos.app.Context
    public int verifyCallingPermission(String str) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::verifyCallingPermission context is null", new Object[0]);
            return -1;
        } else if (str != null) {
            return context2.verifyCallingPermission(str);
        } else {
            AppLog.e("AbilityContext::verifyCallingPermission param invalid", new Object[0]);
            return -1;
        }
    }

    @Override // ohos.app.Context
    public int verifySelfPermission(String str) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::verifySelfPermission context is null", new Object[0]);
            return -1;
        } else if (str != null) {
            return context2.verifySelfPermission(str);
        } else {
            AppLog.e("AbilityContext::verifySelfPermission param invalid", new Object[0]);
            return -1;
        }
    }

    @Override // ohos.app.Context
    public int verifyCallingOrSelfPermission(String str) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::verifyCallingOrSelfPermission context is null", new Object[0]);
            return -1;
        } else if (str != null) {
            return context2.verifyCallingOrSelfPermission(str);
        } else {
            AppLog.e("AbilityContext::verifyCallingOrSelfPermission param invalid", new Object[0]);
            return -1;
        }
    }

    @Override // ohos.app.Context
    public int verifyPermission(String str, int i, int i2) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::verifyPermission context is null", new Object[0]);
            return -1;
        } else if (str != null) {
            return context2.verifyPermission(str, i, i2);
        } else {
            AppLog.e("AbilityContext::permission param invalid", new Object[0]);
            return -1;
        }
    }

    @Override // ohos.app.Context
    public ClassLoader getClassloader() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getClassloader();
        }
        AppLog.e("AbilityContext::getClassloader context is null", new Object[0]);
        return null;
    }

    @Deprecated
    public Optional<ClassLoader> getClassLoader(AbilityInfo abilityInfo) {
        ClassLoader classloader = getClassloader();
        if (classloader != null) {
            return Optional.of(classloader);
        }
        return Optional.empty();
    }

    @Override // ohos.app.Context
    public Object getHostContext() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getHostContext();
        }
        AppLog.e("AbilityContext::getHostContext context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public Context getApplicationContext() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getApplicationContext();
        }
        AppLog.e("AbilityContext::getApplicationContext context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public void terminateAbility() {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::terminateAbility context is null", new Object[0]);
        } else {
            context2.terminateAbility();
        }
    }

    @Override // ohos.app.Context
    public void terminateAbility(int i) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::terminateAbility context is null", new Object[0]);
        } else {
            context2.terminateAbility(i);
        }
    }

    @Override // ohos.app.Context
    public void displayUnlockMissionMessage() {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::displayUnlockMissionMessage context is null", new Object[0]);
        } else {
            context2.displayUnlockMissionMessage();
        }
    }

    @Override // ohos.app.Context
    public void lockMission() {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::lockMission context is null", new Object[0]);
        } else {
            context2.lockMission();
        }
    }

    @Override // ohos.app.Context
    public void unlockMission() {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::beginLockTask context is null", new Object[0]);
        } else {
            context2.unlockMission();
        }
    }

    @Override // ohos.app.Context
    public final boolean terminateAbilityResult(int i) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.terminateAbilityResult(i);
        }
        AppLog.e("AbilityContext::terminateAbilityResult context is null", new Object[0]);
        return false;
    }

    @Override // ohos.app.Context
    public String getLocalClassName() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getLocalClassName();
        }
        AppLog.e("AbilityContext::getLocalClassName context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public ElementName getElementName() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getElementName();
        }
        AppLog.e("AbilityContext::getElementName context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public ElementName getCallingAbility() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getCallingAbility();
        }
        AppLog.e("AbilityContext::getCallingAbility context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public String getCallingBundle() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getCallingBundle();
        }
        AppLog.e("AbilityContext::getCallingBundle context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public boolean stopAbility(Intent intent) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::stopAbility context is null", new Object[0]);
            return false;
        } else if (intent != null) {
            return context2.stopAbility(intent);
        } else {
            AppLog.e("AbilityContext::stopAbility param invalid", new Object[0]);
            return false;
        }
    }

    @Override // ohos.app.Context
    public void startAbility(Intent intent, int i) {
        startAbility(intent, i, AbilityStartSetting.getEmptySetting());
    }

    @Override // ohos.app.Context
    public void startAbility(Intent intent, int i, AbilityStartSetting abilityStartSetting) {
        if (this.context == null) {
            AppLog.e("AbilityContext::startAbility context is null", new Object[0]);
        } else if (intent == null) {
            AppLog.e("AbilityContext::startAbility param invalid", new Object[0]);
        } else {
            if (intent.getPicker() != null) {
                intent = intent.getPicker();
            }
            this.context.startAbility(intent, i, abilityStartSetting);
        }
    }

    @Override // ohos.app.Context
    public void startAbilities(Intent[] intentArr) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::startAbilities context is null", new Object[0]);
        } else if (intentArr == null || intentArr.length == 0) {
            AppLog.e("AbilityContext::startAbilities param invalid", new Object[0]);
        } else {
            context2.startAbilities(intentArr);
        }
    }

    @Override // ohos.app.Context
    public Context createBundleContext(String str, int i) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.createBundleContext(str, i);
        }
        AppLog.e("AbilityContext::createBundleContext context is null, the ability is not initialized.", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File getDataDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getDataDir();
        }
        AppLog.e("AbilityContext::getDataDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public String getBundleResourcePath() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getBundleResourcePath();
        }
        AppLog.e("AbilityContext::getBundleResourcePath context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public boolean canRequestPermission(String str) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::canRequestPermission context is null", new Object[0]);
            return false;
        } else if (str != null) {
            return context2.canRequestPermission(str);
        } else {
            AppLog.e("AbilityContext::canRequestPermission param invalid", new Object[0]);
            return false;
        }
    }

    @Override // ohos.app.Context
    public void requestPermissionsFromUser(String[] strArr, int i) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::requestPermissionsFromUser context is null", new Object[0]);
        } else if (strArr == null || strArr.length == 0) {
            AppLog.e("AbilityContext::requestPermissionsFromUser param invalid, permissions is null or empty", new Object[0]);
        } else if (i < 0) {
            AppLog.e("AbilityContext::requestPermissionsFromUser param invalid, requestCode is negative", new Object[0]);
        } else {
            context2.requestPermissionsFromUser(strArr, i);
        }
    }

    @Override // ohos.app.Context
    public void setResult(int i, Intent intent) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::setResult context is null", new Object[0]);
        } else {
            context2.setResult(i, intent);
        }
    }

    @Override // ohos.app.Context
    public boolean connectAbility(Intent intent, IAbilityConnection iAbilityConnection) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::connectAbility context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        } else if (intent != null && iAbilityConnection != null) {
            return context2.connectAbility(intent, iAbilityConnection);
        } else {
            AppLog.e("AbilityContext::connectAbility param invalid", new Object[0]);
            throw new IllegalArgumentException("connectAbility param invalid.");
        }
    }

    @Override // ohos.app.Context
    public void disconnectAbility(IAbilityConnection iAbilityConnection) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::disconnectAbility context is null", new Object[0]);
            throw new IllegalArgumentException("context is null");
        } else if (iAbilityConnection != null) {
            context2.disconnectAbility(iAbilityConnection);
        } else {
            AppLog.e("AbilityContext::disconnectAbility param invalid", new Object[0]);
            throw new IllegalArgumentException("conn param invalid");
        }
    }

    @Override // ohos.app.Context
    public void setDisplayOrientation(AbilityInfo.DisplayOrientation displayOrientation) {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("AbilityContext::setDisplayOrientation context is null", new Object[0]);
        } else if (displayOrientation == null) {
            AppLog.e("AbilityContext::setDisplayOrientation param invalid", new Object[0]);
        } else {
            context2.setDisplayOrientation(displayOrientation);
        }
    }

    @Override // ohos.app.Context
    public IBundleManager getBundleManager() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getBundleManager();
        }
        AppLog.e("AbilityContext::getBundleManager context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public Object getHostProtectedStorageContext() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getHostProtectedStorageContext();
        }
        AppLog.e("AbilityContext::getHostProtectedStorageContext context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public String getBundleName() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getBundleName();
        }
        AppLog.e("AbilityContext::getBundleName context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public String getBundleCodePath() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getBundleCodePath();
        }
        AppLog.e("AbilityContext::getBundleCodePath context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File getCacheDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getCacheDir();
        }
        AppLog.e("AbilityContext::getCacheDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File getCodeCacheDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getCodeCacheDir();
        }
        AppLog.e("AbilityContext::getCodeCacheDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File[] getExternalMediaDirs() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getExternalMediaDirs();
        }
        AppLog.e("AbilityContext::getExternalMediaDirs context is null", new Object[0]);
        return new File[0];
    }

    @Override // ohos.app.Context
    public File getNoBackupFilesDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getNoBackupFilesDir();
        }
        AppLog.e("AbilityContext::getNoBackupFilesDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File getFilesDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getFilesDir();
        }
        AppLog.e("AbilityContext::getFilesDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File getDir(String str, int i) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getDir(str, i);
        }
        AppLog.e("AbilityContext::getDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File getExternalCacheDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getExternalCacheDir();
        }
        AppLog.e("AbilityContext::getExternalCacheDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File[] getExternalCacheDirs() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getExternalCacheDirs();
        }
        AppLog.e("AbilityContext::getExternalCacheDirs context is null", new Object[0]);
        return new File[0];
    }

    @Override // ohos.app.Context
    public File getExternalFilesDir(String str) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getExternalFilesDir(str);
        }
        AppLog.e("AbilityContext::getExternalFilesDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File[] getExternalFilesDirs(String str) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getExternalFilesDirs(str);
        }
        AppLog.e("AbilityContext::getExternalFilesDirs context is null", new Object[0]);
        return new File[0];
    }

    @Override // ohos.app.Context
    public File getObbDir() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getObbDir();
        }
        AppLog.e("AbilityContext::getObbDir context is null", new Object[0]);
        return null;
    }

    @Override // ohos.app.Context
    public File[] getObbDirs() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getObbDirs();
        }
        AppLog.e("AbilityContext::getObbDirs context is null", new Object[0]);
        return new File[0];
    }

    @Override // ohos.app.Context
    public boolean deleteFile(String str) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.deleteFile(str);
        }
        AppLog.e("AbilityContext::deleteFile context is null", new Object[0]);
        return false;
    }

    @Override // ohos.app.Context
    public IAbilityManager getAbilityManager() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getAbilityManager();
        }
        AppLog.e("AbilityContext::getAbilityManager context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    @SystemApi
    public boolean moveMissionToEnd(boolean z) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.moveMissionToEnd(z);
        }
        AppLog.e("AbilityContext::moveMissionToBack context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    @SystemApi
    public boolean isFirstInMission() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.isFirstInMission();
        }
        AppLog.e("AbilityContext::isFirstInMission context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    @SystemApi
    public int getMissionId() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getMissionId();
        }
        AppLog.e("AbilityContext::getMissionId context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    @SystemApi
    public boolean setMissionInformation(MissionInformation missionInformation) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.setMissionInformation(missionInformation);
        }
        AppLog.e("AbilityContext::setMissionInformation context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public int getDisplayOrientation() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getDisplayOrientation();
        }
        AppLog.e("AbilityContext::getRequestedOrientation context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public void setShowOnLockScreen(boolean z) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.setShowOnLockScreen(z);
        } else {
            AppLog.e("AbilityContext::setShowWhenLocked context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void setWakeUpScreen(boolean z) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.setWakeUpScreen(z);
        } else {
            AppLog.e("AbilityContext::setTurnScreenOn context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void restart() {
        Context context2 = this.context;
        if (context2 != null) {
            context2.restart();
        } else {
            AppLog.e("AbilityContext::restart context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void setTransitionAnimation(int i, int i2) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.setTransitionAnimation(i, i2);
        } else {
            AppLog.e("AbilityContext::setTransitionAnimation context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public boolean isUpdatingConfigurations() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.isUpdatingConfigurations();
        }
        AppLog.e("AbilityContext::restart context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public void setTheme(int i) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.setTheme(i);
        } else {
            AppLog.e("AbilityContext::setTheme context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public Theme getTheme() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getTheme();
        }
        AppLog.e("AbilityContext::getTheme context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public Theme getCombinedTheme(Theme theme) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getCombinedTheme(theme);
        }
        AppLog.e("AbilityContext::setTheme context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public void setPattern(int i) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.setPattern(i);
        } else {
            AppLog.e("AbilityContext::setPattern context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public Pattern getPattern() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getPattern();
        }
        AppLog.e("AbilityContext::getPattern context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public Pattern getCombinedPattern(Pattern pattern) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getCombinedPattern(pattern);
        }
        AppLog.e("AbilityContext::setPattern context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public String getAppType() {
        return this.context.getAppType();
    }

    @Override // ohos.app.Context
    @SystemApi
    public void terminateAndRemoveMission() {
        this.context.terminateAndRemoveMission();
    }

    @Override // ohos.app.Context
    public Object getLayoutScatter() {
        return this.context.getLayoutScatter();
    }

    @Override // ohos.app.Context
    public final boolean isAllowClassMap() {
        return this.context.isAllowClassMap();
    }

    @Override // ohos.app.Context
    public Object getHarmonyosApp() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getHarmonyosApp();
        }
        return null;
    }

    @Override // ohos.app.Context
    public Object getHarmonyAbilityPkg(AbilityInfo abilityInfo) {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getHarmonyAbilityPkg(abilityInfo);
        }
        return null;
    }

    @Override // ohos.app.Context
    public ResourceManager getResourceManager(Configuration configuration) {
        return this.context.getResourceManager(configuration);
    }

    @Override // ohos.app.Context
    public Object getLastStoredDataWhenConfigChanged() {
        return this.context.getLastStoredDataWhenConfigChanged();
    }

    @Override // ohos.app.Context
    public void printDrawnCompleted() {
        this.context.printDrawnCompleted();
    }

    @Override // ohos.app.Context
    public void compelVerifyPermission(String str, String str2) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.compelVerifyPermission(str, str2);
        } else {
            AppLog.e("AbilityContext::context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void compelVerifyUriPermission(Uri uri, int i, String str) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.compelVerifyUriPermission(uri, i, str);
        } else {
            AppLog.e("AbilityContext::context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void compelVerifyCallerPermission(String str, String str2) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.compelVerifyCallerPermission(str, str2);
        } else {
            AppLog.e("AbilityContext::context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void compelVerifyCallerUriPermission(Uri uri, int i, String str) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.compelVerifyCallerUriPermission(uri, i, str);
        } else {
            AppLog.e("AbilityContext::context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void compelVerifyPermission(String str, int i, int i2, String str2) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.compelVerifyPermission(str, i, i2, str2);
        } else {
            AppLog.e("AbilityContext::context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void compelVerifyUriPermission(Uri uri, int i, int i2, int i3, String str) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.compelVerifyUriPermission(uri, i, i2, i3, str);
        } else {
            AppLog.e("AbilityContext::context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public void compelVerifyUriPermission(Uri uri, String str, String str2, int i, int i2, int i3, String str3) {
        Context context2 = this.context;
        if (context2 != null) {
            context2.compelVerifyUriPermission(uri, str, str2, i, i2, i3, str3);
        } else {
            AppLog.e("AbilityContext::context is null", new Object[0]);
            throw new IllegalArgumentException("Ability is not initialized.");
        }
    }

    @Override // ohos.app.Context
    public String getProcessName() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getProcessName();
        }
        AppLog.e("AbilityContext::context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public Context getAbilityPackageContext() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getAbilityPackageContext();
        }
        AppLog.e("AbilityContext::context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public HapModuleInfo getHapModuleInfo() {
        Context context2 = this.context;
        if (context2 != null) {
            return context2.getHapModuleInfo();
        }
        AppLog.e("AbilityContext::context is null", new Object[0]);
        throw new IllegalArgumentException("Ability is not initialized.");
    }

    @Override // ohos.app.Context
    public Uri getCaller() {
        return this.context.getCaller();
    }
}
