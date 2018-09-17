package com.android.server.pm;

import android.content.Intent;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageParser.Package;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.FileObserver;
import com.android.server.job.JobSchedulerShellCommand;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class AbsPackageManagerService extends Stub {
    public static final String CUST_PRE_DEL_DIR = "/data/cust/";
    public static final String CUST_PRE_DEL_FILE = "delapp";
    public static final String DEL_APK_DIR = "/data/cust/xml/";
    public static final String DEL_APK_NAME = "unstall_apk.xml";
    public static final String FIRST_BOOT_TAG_DIR = "/data/data/";
    public static final String FIRST_BOOT_TAG_FILE = "firstbooted";
    public static final String NFCTAG_SAVE_PATCH = "/system/app/HwNfcTag.apk";
    public static final String NFC_DEVICE_PATH = null;
    public static final String NFC_FEATURE = "android.hardware.nfc";
    public static final String NFC_HCEF_FEATURE = "android.hardware.nfc.hcef";
    public static final String NFC_HCE_FEATURE = "android.hardware.nfc.hce";
    public static final String NFC_SAVE_PATCH = "/system/app/NfcNci_45.apk";
    public static final String PREINSTALLED_APK_LIST_DIR = "/data/system/";
    public static final String PREINSTALLED_APK_LIST_FILE = "preinstalled_app_list_file.xml";
    public static final String SYSTEM_APP_DIR = "/system/app";
    public static final String SYSTEM_PRE_DEL_DIR = "/system/";
    public static final String SYSTEM_PRE_DEL_FILE = "delapp";
    public static final String SYSTEM_PRIV_APP_DIR = "/system/priv-app";
    public static final String UNINSTALLED_DELAPP_DIR = "/data/system/";
    public static final String UNINSTALLED_DELAPP_FILE = "uninstalled_delapp.xml";
    public static final String mAPKInstallList_DIR = "/data/cust/xml";
    public static final String mAPKInstallList_FILE = "APKInstallListEMUI5Release.txt";
    public static final String mDelAPKInstallList_FILE = "DelAPKInstallListEMUI5Release.txt";
    protected long mDexOptTotalTime;
    public FileObserver[] mRemovableAppDirObserver;
    boolean mSetupDisabled;
    public boolean mUninstallUpdate;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.AbsPackageManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.AbsPackageManagerService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.AbsPackageManagerService.<clinit>():void");
    }

    public AbsPackageManagerService() {
        this.mSetupDisabled = false;
        this.mRemovableAppDirObserver = null;
        this.mUninstallUpdate = false;
        this.mDexOptTotalTime = 0;
    }

    public void scanCustDir(int scanMode) {
    }

    public void getUninstallApk() {
    }

    public void scanDataDir(int scanMode) {
    }

    public void scanNonSystemPartitionDir(int scanMode) {
    }

    public void scanRemovableAppDir(int scanMode) {
    }

    public void recordUninstalledDelapp(String s) {
    }

    public boolean isDelapp(PackageSetting ps) {
        return false;
    }

    public boolean isDelappInData(PackageSetting ps) {
        return false;
    }

    public boolean isSystemPathApp(PackageSetting ps) {
        return false;
    }

    public void addFlagsForRemovablePreApk(Package pkg, int parseFlags) {
    }

    public boolean needInstallRemovablePreApk(Package pkg, int parseFlags) {
        return true;
    }

    public void scanHwCustAppDir(int scanMode) {
    }

    public boolean isCustApkRecorded(File file) {
        return true;
    }

    public boolean isDelappInCust(PackageSetting ps) {
        return false;
    }

    public boolean isUninstallApk(String filePath) {
        return false;
    }

    public void setGMSPackage(Package pkg) {
    }

    public boolean getGMSPackagePermission(Package pkg) {
        return false;
    }

    protected void readCloudApkConfig() {
    }

    protected int appendPkgFlagsForCloudApk(String packageName, String path, int parseFlags, int defaultFlags, Package pkg) {
        return defaultFlags;
    }

    protected int removePkgFlagsForCloudApk(String packageName, String path, int defaultFlags, int hwFlags) {
        return defaultFlags;
    }

    protected int appendParseFlagsForCloudApk(int pFlags, int defaultFlags) {
        return defaultFlags;
    }

    protected boolean isCloudApk(String packageName) {
        return false;
    }

    public void custScanPrivDir(File dir, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
    }

    protected void parseInstallerInfo(int uid, String packageUri) {
    }

    protected void parseInstalledPkgInfo(String pkgUri, String pkgName, String pkgVerName, int pkgVerCode, int resultCode, boolean pkgUpdate) {
    }

    public boolean containDelPath(String sensePath) {
        return false;
    }

    public void addUpdatedRemoveableAppFlag(String scanFileString, String packageName) {
    }

    public boolean needAddUpdatedRemoveableAppFlag(String packageName) {
        return false;
    }

    public void addFlagsForUpdatedRemovablePreApk(Package pkg, int hwFlags) {
    }

    protected boolean isOdexMode() {
        return true;
    }

    protected boolean notDexOptForBootingSpeedup(boolean adjustCpuAbi) {
        return false;
    }

    protected boolean filterForceNotDexApps(Package pkg, boolean adjustCpuAbi) {
        return false;
    }

    protected boolean filterDexoptInBootupApps(Package pkg) {
        return false;
    }

    protected ArrayList<Package> sortRecentlyUsedApps(Collection<Package> collection) {
        return null;
    }

    protected boolean makeSetupDisabled(String pname) {
        return false;
    }

    protected boolean skipSetupEnable(String pname) {
        return false;
    }

    public boolean isSetupDisabled() {
        return this.mSetupDisabled;
    }

    public HwCustPackageManagerService getCust() {
        return null;
    }

    protected ResolveInfo hwFindPreferredActivity(Intent intent, String resolvedType, int flags, List<ResolveInfo> list, int priority, boolean always, boolean removeMatches, boolean debug, int userId) {
        return null;
    }

    protected void filterShellApps(ArrayList<Package> arrayList, LinkedList<Package> linkedList) {
    }

    protected void performBootDexOptThermalControl(boolean resume) {
    }

    protected void checkHwCertification(Package pkg, boolean isUpdate) {
    }

    protected void hwCertCleanUp() {
    }

    protected boolean getHwCertificationPermission(boolean allowed, Package pkg, String perm) {
        return allowed;
    }

    protected boolean isHwCustHiddenInfoPackage(Package pkgInfo) {
        return false;
    }

    protected boolean hasOtaUpdate() {
        return false;
    }

    protected void updatePackageBlackListInfo(String packageName) {
    }

    protected boolean isInMultiWinWhiteList(String packageName) {
        return false;
    }

    protected boolean isInMWPortraitWhiteList(String packageName) {
        return false;
    }

    protected boolean shouldPreventInstallForAllUsers() {
        return false;
    }

    protected void initHwCertificationManager() {
    }

    protected int getHwCertificateType(Package pkg) {
        return 0;
    }

    protected boolean isContainHwCertification(Package pkg) {
        return false;
    }

    protected void replaceSignatureIfNeeded(PackageSetting ps, Package pkg, boolean isBootScan, boolean isUpdate) {
    }

    protected void resetSharedUserSignaturesIfNeeded() {
    }

    protected void initCertCompatSettings() {
    }

    protected void writeCertCompatPackages(boolean update) {
    }

    protected void updateCertCompatPackage(Package pkg, PackageSetting ps) {
    }

    protected boolean isSystemSignatureUpdated(Signature[] previous, Signature[] current) {
        return false;
    }

    protected Signature[] getRealSignature(Package pkg) {
        return new Signature[0];
    }

    protected void setRealSignature(Package pkg, Signature[] sign) {
    }

    protected void sendIncompatibleNotificationIfNeeded(String packageName) {
    }

    protected void updateCloneAppList(String removedPackage, boolean replacing, int[] removedUsers) {
    }

    protected void readLastedAbi(Package pkg, File scanFile, String cpuAbiOverride) throws PackageManagerException {
    }

    protected void readPackagesAbiLPw() {
    }

    protected void writePackagesAbi() {
    }

    protected int getPackagesAbi(String name) {
        return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
    }

    protected int getPackageVersion(String name) {
        return 0;
    }

    protected void removePackageAbiLPw(String name) {
    }

    protected void addPackagesAbiLPw(String name, int code, boolean flag, int versionCode) {
    }

    protected boolean isPackageAbiRestored(String name) {
        return false;
    }

    protected void deletePackagesAbiFile() {
    }

    protected boolean isPackagePathWithNoSysFlag(File filePath) {
        return false;
    }

    protected void tryToReboot() {
    }

    protected void resetRebootTimes() {
    }

    protected void computeMetaHash(Package pkg) {
    }

    protected void recordInstallAppInfo(String pkgName, long endTime, int installFlags) {
    }

    protected void addPreinstalledPkgToList(Package pkg) {
    }

    protected void writePreinstalledApkListToFile() {
    }

    protected boolean checkIllegalGmsCoreApk(Package pkg) {
        return false;
    }
}
