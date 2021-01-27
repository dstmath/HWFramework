package ohos.app;

import java.io.File;
import java.util.List;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.ability.IDataAbility;
import ohos.aafwk.ability.MissionInformation;
import ohos.aafwk.ability.startsetting.AbilityStartSetting;
import ohos.aafwk.content.Intent;
import ohos.annotation.SystemApi;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.task.TaskPriority;
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

public interface Context {
    public static final int CONTEXT_IGNORE_SECURITY = 2;
    public static final int CONTEXT_INCLUDE_CODE = 1;
    public static final int CONTEXT_RESOUCE_ONLY = 8;
    public static final int CONTEXT_RESTRICTED = 4;
    public static final int MODE_APPEND = 32768;
    public static final int MODE_PRIVATE = 0;

    @SystemApi
    void authUriPermission(String str, Uri uri, int i);

    boolean canRequestPermission(String str);

    void compelVerifyCallerPermission(String str, String str2);

    void compelVerifyCallerUriPermission(Uri uri, int i, String str);

    void compelVerifyPermission(String str, int i, int i2, String str2);

    void compelVerifyPermission(String str, String str2);

    void compelVerifyUriPermission(Uri uri, int i, int i2, int i3, String str);

    void compelVerifyUriPermission(Uri uri, int i, String str);

    void compelVerifyUriPermission(Uri uri, String str, String str2, int i, int i2, int i3, String str3);

    boolean connectAbility(Intent intent, IAbilityConnection iAbilityConnection);

    Context createBundleContext(String str, int i);

    TaskDispatcher createParallelTaskDispatcher(String str, TaskPriority taskPriority);

    TaskDispatcher createSerialTaskDispatcher(String str, TaskPriority taskPriority);

    boolean deleteFile(String str);

    void disconnectAbility(IAbilityConnection iAbilityConnection);

    void displayUnlockMissionMessage();

    AbilityInfo getAbilityInfo();

    IAbilityManager getAbilityManager();

    Context getAbilityPackageContext();

    String getAppType();

    Context getApplicationContext();

    ApplicationInfo getApplicationInfo();

    String getBundleCodePath();

    IBundleManager getBundleManager();

    String getBundleName();

    String getBundleResourcePath();

    File getCacheDir();

    Uri getCaller();

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

    @SystemApi
    List<UriPermissionDef> getGrantedPermanentUriPermissionDefs() throws DataAbilityRemoteException;

    HapModuleInfo getHapModuleInfo();

    Object getHarmonyAbilityPkg(AbilityInfo abilityInfo);

    Object getHarmonyosApp();

    Object getHostContext();

    Object getHostProtectedStorageContext();

    @SystemApi
    List<UriPermissionDef> getHostedPermanentUriPermissionDefs() throws DataAbilityRemoteException;

    Object getLastStoredDataWhenConfigChanged();

    Object getLayoutScatter();

    String getLocalClassName();

    TaskDispatcher getMainTaskDispatcher();

    @SystemApi
    int getMissionId();

    File getNoBackupFilesDir();

    File getObbDir();

    File[] getObbDirs();

    Pattern getPattern();

    File getPreferencesDir();

    ProcessInfo getProcessInfo();

    String getProcessName();

    ResourceManager getResourceManager();

    ResourceManager getResourceManager(Configuration configuration);

    Theme getTheme();

    TaskDispatcher getUITaskDispatcher();

    @SystemApi
    void giveUpPermanentUriPermission(Uri uri, int i) throws DataAbilityRemoteException;

    boolean isAllowClassMap();

    boolean isCredentialEncryptedStorage();

    boolean isDeviceEncryptedStorage();

    @SystemApi
    boolean isFirstInMission();

    boolean isUpdatingConfigurations();

    void lockMission();

    @SystemApi
    void makePermanentUriPermission(Uri uri, int i) throws DataAbilityRemoteException;

    @SystemApi
    boolean moveMissionToEnd(boolean z);

    void printDrawnCompleted();

    boolean releaseDataAbility(IDataAbility iDataAbility);

    void requestPermissionsFromUser(String[] strArr, int i);

    void restart();

    void setDisplayOrientation(AbilityInfo.DisplayOrientation displayOrientation);

    @SystemApi
    boolean setMissionInformation(MissionInformation missionInformation);

    void setPattern(int i);

    void setResult(int i, Intent intent);

    void setShowOnLockScreen(boolean z);

    void setTheme(int i);

    void setTransitionAnimation(int i, int i2);

    void setWakeUpScreen(boolean z);

    void startAbilities(Intent[] intentArr);

    void startAbility(Intent intent, int i);

    void startAbility(Intent intent, int i, AbilityStartSetting abilityStartSetting);

    boolean stopAbility(Intent intent);

    void switchToCredentialEncryptedStorageContext();

    void switchToDeviceEncryptedStorageContext();

    void terminateAbility();

    void terminateAbility(int i);

    boolean terminateAbilityResult(int i);

    @SystemApi
    void terminateAndRemoveMission();

    @SystemApi
    void unauthUriPermission(String str, Uri uri, int i);

    void unlockMission();

    int verifyCallingOrSelfPermission(String str);

    int verifyCallingPermission(String str);

    @SystemApi
    int verifyCallingUriPermission(Uri uri, int i);

    int verifyPermission(String str, int i, int i2);

    int verifySelfPermission(String str);

    @SystemApi
    int verifyUriPermission(Uri uri, int i, int i2, int i3);
}
