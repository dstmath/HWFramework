package ohos.app;

import java.io.File;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.ability.IDataAbility;
import ohos.aafwk.ability.TaskInformation;
import ohos.aafwk.content.Intent;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ApplicationInfo;
import ohos.bundle.ElementName;
import ohos.bundle.IBundleManager;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.solidxml.Pattern;
import ohos.global.resource.solidxml.Theme;
import ohos.utils.net.Uri;

public interface Context {
    public static final int CONTEXT_IGNORE_SECURITY = 2;
    public static final int CONTEXT_INCLUDE_CODE = 1;
    public static final int CONTEXT_RESOUCE_ONLY = 8;
    public static final int CONTEXT_RESTRICTED = 4;
    public static final int MODE_APPEND = 32768;
    public static final int MODE_PRIVATE = 0;

    void authUriPermission(String str, Uri uri, int i);

    boolean canRequestPermission(String str);

    boolean connectAbility(Intent intent, IAbilityConnection iAbilityConnection);

    Context createBundleContext(String str, int i);

    TaskDispatcher createParallelTaskDispatcher(String str, TaskPriority taskPriority);

    TaskDispatcher createSerialTaskDispatcher(String str, TaskPriority taskPriority);

    boolean deleteFile(String str);

    void disconnectAbility(IAbilityConnection iAbilityConnection);

    AbilityInfo getAbilityInfo();

    IAbilityManager getAbilityManager();

    String getAppType();

    Context getApplicationContext();

    ApplicationInfo getApplicationInfo();

    String getBundleCodePath();

    IBundleManager getBundleManager();

    String getBundleName();

    String getBundleResourcePath();

    File getCacheDir();

    ElementName getCallingAbility();

    String getCallingBundle();

    ClassLoader getClassloader();

    File getCodeCacheDir();

    Pattern getCombinedPattern(Pattern pattern);

    Theme getCombinedTheme(Theme theme);

    IDataAbility getDataAbility(Uri uri);

    IDataAbility getDataAbility(Uri uri, boolean z);

    File getDataDir();

    File getDatabaseDir();

    File getDir(String str, int i);

    int getDisplayOrientation();

    File getDistributedDir();

    ElementName getElementName();

    File getExternalCacheDir();

    File[] getExternalCacheDirs();

    File getExternalFilesDir(String str);

    File[] getExternalFilesDirs(String str);

    File[] getExternalMediaDirs();

    File getFilesDir();

    TaskDispatcher getGlobalTaskDispatcher(TaskPriority taskPriority);

    Object getHarmonyAbilityPkg(AbilityInfo abilityInfo);

    Object getHarmonyosApp();

    Object getHostContext();

    Object getHostProtectedStorageContext();

    Object getLayoutScatter();

    String getLocalClassName();

    TaskDispatcher getMainTaskDispatcher();

    File getNoBackupFilesDir();

    File getObbDir();

    File[] getObbDirs();

    Pattern getPattern();

    File getPreferencesDir();

    ProcessInfo getProcessInfo();

    ResourceManager getResourceManager();

    int getTaskId();

    Theme getTheme();

    TaskDispatcher getUITaskDispatcher();

    boolean isAllowClassMap();

    boolean isCredentialProtectedStorage();

    boolean isDeviceEncryptedStorage();

    boolean isFirstInTask();

    boolean isUpdatingConfigurations();

    boolean moveTaskToEnd(boolean z);

    boolean releaseDataAbility(IDataAbility iDataAbility);

    void requestPermissionsFromUser(String[] strArr, int i);

    void restart();

    void setDisplayOrientation(AbilityInfo.DisplayOrientation displayOrientation);

    void setPattern(int i);

    void setResult(int i, Intent intent);

    void setShowOnLockScreen(boolean z);

    boolean setTaskInformation(TaskInformation taskInformation);

    void setTheme(int i);

    void setTransitionAnimation(int i, int i2);

    void setWakeUpScreen(boolean z);

    void startAbilities(Intent[] intentArr);

    void startAbility(Intent intent, int i);

    boolean stopAbility(Intent intent);

    void switchToCredentialEncryptedStorageContext();

    void switchToDeviceEncryptedStorageContext();

    void terminateAbility();

    void terminateAbility(int i);

    boolean terminateAbilityResult(int i);

    void terminateAndRemoveTask();

    void unauthUriPermission(String str, Uri uri, int i);

    int verifyCallingOrSelfPermission(String str);

    int verifyCallingPermission(String str);

    int verifyPermission(String str, int i, int i2);

    int verifySelfPermission(String str);

    int verifyUriPermission(Uri uri, int i, int i2, int i3);
}
