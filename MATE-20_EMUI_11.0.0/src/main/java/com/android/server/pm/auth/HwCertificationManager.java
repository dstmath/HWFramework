package com.android.server.pm.auth;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageParser;
import android.os.Process;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Flog;
import com.android.server.pm.HwMdmDFTUtilImpl;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.processor.HwCertificationProcessor;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.json.JSONException;
import org.json.JSONObject;

public class HwCertificationManager {
    private static final String BDREPORT_MDM_KEY_API_NAME = "apiName";
    private static final String BDREPORT_MDM_KEY_PACKAGE = "package";
    private static final String BDREPORT_MDM_VALUE_API_NAME = "check_hwcert";
    private static final int CORE_POOL_SIZE = 0;
    private static final int GET_SIGNATURE_OF_CERT = 0;
    private static final boolean HAS_FEATURE = true;
    private static final int KEEP_ALIVE = 5;
    private static final Object LOCK = new Object();
    private static final int MAXIMUM_POOL_SIZE = 1;
    private static final int QUEUE_SIZE = 10;
    private static final String TAG = "HwCertificationManager";
    private static Context sContext;
    private static HwCertificationManager sInstance;
    private ConcurrentHashMap<String, HwCertification> mCertMap = new ConcurrentHashMap<>();
    private ExecutorService mHwCertExecutor = new ThreadPoolExecutor(0, 1, 5, TimeUnit.SECONDS, new LinkedBlockingQueue(10));
    private final HwCertXmlHandler mHwCertXmlHandler = new HwCertXmlHandler();
    private AtomicBoolean mIsSystemReady = new AtomicBoolean(false);
    private List<String> mMdmCertBlackList = new ArrayList();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.pm.auth.HwCertificationManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                HwCertificationManager.this.handlePackagesChanged(intent);
            }
        }
    };

    private HwCertificationManager() {
        readHwCertXml();
        readMdmCertBlacklist();
    }

    public static void initialize(Context context) {
        sContext = context;
    }

    public static boolean isInitialized() {
        return sContext != null;
    }

    public static synchronized HwCertificationManager getInstance() {
        synchronized (HwCertificationManager.class) {
            if (sContext == null) {
                HwAuthLogger.error("HwCertificationManager", "getInstance context is null!");
                return null;
            }
            int uid = Process.myUid();
            if (uid == 1000 || uid == 2000 || uid == 0) {
                if (sInstance == null) {
                    sInstance = new HwCertificationManager();
                }
                return sInstance;
            }
            HwAuthLogger.error("HwCertificationManager", "getInstance from uid:" + uid + ", not system return null!");
            return null;
        }
    }

    public static boolean isSupportHwCertification(PackageParser.Package pkg) {
        if (pkg == null || pkg.requestedPermissions == null) {
            return false;
        }
        if (pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM") || pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM.v2")) {
            return true;
        }
        return false;
    }

    public static boolean hasFeature() {
        return true;
    }

    private void scheduleWriteStateLocked(boolean isRemoveCache) {
        this.mHwCertExecutor.execute(new Runnable(isRemoveCache) {
            /* class com.android.server.pm.auth.$$Lambda$HwCertificationManager$Dc_b9iE8kRfz7K84umfKA9nlf0 */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwCertificationManager.this.lambda$scheduleWriteStateLocked$0$HwCertificationManager(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$scheduleWriteStateLocked$0$HwCertificationManager(boolean isRemoveCache) {
        this.mHwCertXmlHandler.updateHwCert(new ArrayList(this.mCertMap.values()));
        if (isRemoveCache) {
            removeCertCache();
        }
    }

    private void readHwCertXml() {
        syncHwCertCache();
    }

    private void readMdmCertBlacklist() {
        this.mMdmCertBlackList.clear();
        this.mHwCertXmlHandler.readMdmCertBlacklist(this.mMdmCertBlackList);
        HwAuthLogger.info("HwCertificationManager", "readMdmCertBlacklist, mMdmCertBlackList size:" + this.mMdmCertBlackList.size());
    }

    private HwCertification parseAndVerify(PackageParser.Package pkg) {
        HwCertification hwCertification = new HwCertification();
        HwCertificationProcessor hwCertProcessor = new HwCertificationProcessor();
        try {
            hwCertProcessor.createZipFile(pkg.baseCodePath);
            if (!hwCertProcessor.readCert(pkg.baseCodePath, hwCertification)) {
                HwAuthLogger.error("HwCertificationManager", "read cert failed!");
                HwCertification hwCertification2 = null;
                if (0 != 0) {
                    hwCertification2.resetZipFile();
                }
                hwCertProcessor.releaseZipFileResource();
                return null;
            } else if (!hwCertProcessor.parseCert(hwCertification)) {
                HwAuthLogger.error("HwCertificationManager", "parse cert failed!");
                HwCertification hwCertification3 = null;
                if (0 != 0) {
                    hwCertification3.resetZipFile();
                }
                hwCertProcessor.releaseZipFileResource();
                return null;
            } else if (hwCertProcessor.verifyCert(pkg, hwCertification)) {
                return hwCertification;
            } else {
                HwAuthLogger.error("HwCertificationManager", "verify cert failed!");
                HwCertification hwCertification4 = null;
                if (0 != 0) {
                    hwCertification4.resetZipFile();
                }
                hwCertProcessor.releaseZipFileResource();
                return null;
            }
        } finally {
            hwCertification.resetZipFile();
            hwCertProcessor.releaseZipFileResource();
        }
    }

    private void addHwPermission(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg.requestedPermissions != null) {
            List<String> permissions = hwCert.getPermissions();
            int size = permissions.size();
            for (int i = 0; i < size; i++) {
                String permission = permissions.get(i);
                if (!pkg.requestedPermissions.contains(permission)) {
                    pkg.requestedPermissions.add(permission);
                }
            }
        }
    }

    public Context getContext() {
        return sContext;
    }

    public boolean checkHwCertification(PackageParser.Package pkg) {
        if (pkg == null) {
            return false;
        }
        synchronized (LOCK) {
            HwCertification hwCertification = null;
            try {
                HwCertification hwCertification2 = parseAndVerify(pkg);
                if (hwCertification2 != null) {
                    if (isSystemReady()) {
                        syncHwCertCache();
                    }
                    this.mCertMap.put(hwCertification2.getPackageName().trim(), hwCertification2);
                    addHwPermission(pkg, hwCertification2);
                    if (isSystemReady()) {
                        scheduleWriteStateLocked(false);
                    }
                    if (getHwCertificateType(pkg.packageName) == 6) {
                        JSONObject obj = new JSONObject();
                        obj.put(BDREPORT_MDM_KEY_PACKAGE, pkg.packageName);
                        obj.put(BDREPORT_MDM_KEY_API_NAME, BDREPORT_MDM_VALUE_API_NAME);
                        Flog.bdReport(sContext, 991310127, obj.toString());
                    }
                    if (isContainHwCertification(pkg.packageName)) {
                        HwMdmDFTUtilImpl.getMdmInstallInfoDft(sContext, pkg);
                    }
                    return true;
                }
                HwAuthLogger.error("HwCertificationManager", "check HwCertification error, cert is null!");
                removeExistedCert(pkg, isSystemReady());
                return false;
            } catch (JSONException e) {
                HwAuthLogger.error("HwCertificationManager", "JSONException can not put on obj!");
                return false;
            } catch (Exception e2) {
                if (0 != 0) {
                    hwCertification.setPermissions(new ArrayList(0));
                }
                HwAuthLogger.error("HwCertificationManager", "check HwCertification error!");
                return false;
            }
        }
    }

    public boolean isSystemReady() {
        return this.mIsSystemReady.get();
    }

    public void systemReady() {
        this.mIsSystemReady.set(true);
        try {
            removeNotExist(false);
        } catch (IllegalStateException e) {
            HwAuthLogger.error("HwCertificationManager", "remove invalid package list illegal state!");
        } catch (Exception e2) {
            HwAuthLogger.error("HwCertificationManager", "remove invalid package list error!");
        }
        registerBroadcastReceiver();
    }

    private void removeNotExist(boolean isSync) {
        if (isSync) {
            syncHwCertCache();
        }
        Iterator<String> iterator = this.mCertMap.keySet().iterator();
        while (iterator.hasNext()) {
            String pkgName = iterator.next();
            if (!Utils.isPackageInstalled(pkgName, sContext) && this.mCertMap.get(pkgName) != null) {
                iterator.remove();
            }
        }
        scheduleWriteStateLocked(true);
    }

    private void removeExistedCert(PackageParser.Package pkg, boolean shouldUpdate) {
        if (shouldUpdate) {
            syncHwCertCache();
        }
        if (this.mCertMap.get(pkg.packageName) != null) {
            this.mCertMap.remove(pkg.packageName);
        }
        if (shouldUpdate) {
            scheduleWriteStateLocked(false);
        }
    }

    public void cleanUp(PackageParser.Package pkg) {
        if (pkg != null) {
            HwAuthLogger.info("HwCertificationManager", "clean up the cert list xml.");
            removeExistedCert(pkg, isSystemReady());
        }
    }

    public void cleanUp() {
        HwAuthLogger.info("HwCertificationManager", "removeNotExist, clean up the cert list xml.");
        removeNotExist(true);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addDataScheme(BDREPORT_MDM_KEY_PACKAGE);
        sContext.registerReceiver(this.mReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackagesChanged(Intent intent) {
        String action;
        if (intent != null && intent.getData() != null && (action = intent.getAction()) != null) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                onPackageRemoved(packageName);
            }
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                removeCertCache();
            }
        }
    }

    private void onPackageRemoved(String packageName) {
        syncHwCertCache();
        if (packageName != null && this.mCertMap.containsKey(packageName)) {
            if (Utils.isPackageInstalled(packageName, sContext)) {
                HwAuthLogger.warn("HwCertificationManager", "package " + packageName + " installed!");
                return;
            }
            this.mCertMap.remove(packageName);
            scheduleWriteStateLocked(false);
            HwAuthLogger.info("HwCertificationManager", "package:" + packageName + ", remove from the cert list xml.");
        }
    }

    public boolean getHwCertificationPermission(boolean isAllowed, PackageParser.Package pkg, String permission) {
        HwCertification hwCert;
        List<String> permissions;
        if (isAllowed || pkg == null || TextUtils.isEmpty(permission) || (hwCert = this.mCertMap.get(pkg.packageName)) == null || (permissions = hwCert.getPermissions()) == null || !permissions.contains(permission) || pkg.requestedPermissions == null || !pkg.requestedPermissions.contains(permission)) {
            return isAllowed;
        }
        HwAuthLogger.info("HwCertificationManager", "[package]:" + pkg.packageName + ", perm:" + permission);
        return true;
    }

    public int getHwCertificateType(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return 5;
        }
        HwCertification hwCert = this.mCertMap.get(packageName);
        if (hwCert == null) {
            HwAuthLogger.info("HwCertificationManager", "getHwCertificateType cert is null, pkg name is " + packageName);
            return 5;
        }
        String certificate = hwCert.getCertificate();
        if (certificate == null) {
            return 6;
        }
        if (certificate.equals(HwCertification.SIGNATURE_PLATFORM)) {
            return 1;
        }
        if (certificate.equals(HwCertification.SIGNATURE_TESTKEY)) {
            return 2;
        }
        if (certificate.equals(HwCertification.SIGNATURE_SHARED)) {
            return 3;
        }
        if (certificate.equals(HwCertification.SIGNATURE_MEDIA)) {
            return 4;
        }
        if (certificate.equals("null")) {
            return 0;
        }
        return -1;
    }

    public int getHwCertSignatureVersion(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        HwCertification hwCert = this.mCertMap.get(packageName);
        if (hwCert == null) {
            HwAuthLogger.info("HwCertificationManager", "getSignatureVersion cert is null, pkg name is " + packageName);
            return -1;
        } else if (!TextUtils.isEmpty(hwCert.getSignatureV3())) {
            return 3;
        } else {
            if (!TextUtils.isEmpty(hwCert.getSignatureV2())) {
                return 2;
            }
            return 1;
        }
    }

    public boolean isContainHwCertification(String packageName) {
        if (!TextUtils.isEmpty(packageName) && this.mCertMap.get(packageName) != null) {
            return true;
        }
        return false;
    }

    public boolean isDevCertification(String packageName) {
        HwCertification certification;
        if (TextUtils.isEmpty(packageName) || (certification = this.mCertMap.get(packageName)) == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("isDevCertification ");
        sb.append(!certification.isReleased());
        HwAuthLogger.info("HwCertificationManager", sb.toString());
        return !certification.isReleased();
    }

    public int getHwCertificateTypeNotMdm() {
        return 5;
    }

    public void updateMdmCertBlacklist() {
        readMdmCertBlacklist();
        Context context = sContext;
        if (context != null) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
            PackageManagerService packageManager = (PackageManagerService) ServiceManager.getService(BDREPORT_MDM_KEY_PACKAGE);
            syncHwCertCache();
            HashMap<String, String> certMap = new HashMap<>(this.mCertMap.size());
            for (String packageName : this.mCertMap.keySet()) {
                String signature = getSignatureOfCert(packageName, 0);
                if (!TextUtils.isEmpty(signature)) {
                    certMap.put(signature, packageName);
                }
            }
            synchronized (LOCK) {
                boolean isUpdated = false;
                int size = this.mMdmCertBlackList.size();
                for (int i = 0; i < size; i++) {
                    if (removeBlackCertPackageName(this.mMdmCertBlackList.get(i), devicePolicyManager, packageManager, certMap)) {
                        isUpdated = true;
                    }
                }
                if (isUpdated) {
                    scheduleWriteStateLocked(true);
                }
            }
        }
    }

    private boolean removeBlackCertPackageName(String certificate, DevicePolicyManager devicePolicyManager, PackageManagerService packageManager, HashMap<String, String> certMap) {
        String packageName;
        HwCertification certification;
        if (TextUtils.isEmpty(certificate) || packageManager == null || certMap == null || !certMap.containsKey(certificate) || (certification = this.mCertMap.get((packageName = certMap.get(certificate)))) == null) {
            return false;
        }
        List<String> permissions = certification.getPermissions();
        if (!(permissions == null || permissions.size() == 0)) {
            packageManager.getHwPMSEx().revokePermissionsFromApp(packageName, permissions);
        }
        removeActiveAdmin(packageName, devicePolicyManager);
        this.mCertMap.remove(packageName);
        certMap.remove(certificate);
        HwAuthLogger.info("HwCertificationManager", packageName + " removed from the cert list xml.");
        return true;
    }

    private void removeActiveAdmin(String packageName, DevicePolicyManager devicePolicyManager) {
        List<ComponentName> activeAdmins;
        if (!(TextUtils.isEmpty(packageName) || devicePolicyManager == null || (activeAdmins = devicePolicyManager.getActiveAdmins()) == null)) {
            int size = activeAdmins.size();
            for (int i = 0; i < size; i++) {
                ComponentName component = activeAdmins.get(i);
                if (packageName.equals(component.getPackageName())) {
                    devicePolicyManager.removeActiveAdmin(component);
                    HwAuthLogger.info("HwCertificationManager", component + " removed from activeAdmins.");
                    return;
                }
            }
        }
    }

    public String getSignatureOfCert(String packageName, int flag) {
        HwCertification certification;
        if (flag == 0 && !TextUtils.isEmpty(packageName) && (certification = this.mCertMap.get(packageName)) != null) {
            return certification.getSignatureV3();
        }
        return "";
    }

    public boolean checkMdmCertBlacklist(String signature) {
        if (!TextUtils.isEmpty(signature) && this.mMdmCertBlackList.contains(signature)) {
            return true;
        }
        return false;
    }

    private void removeCertCache() {
        if (this.mIsSystemReady.get()) {
            synchronized (LOCK) {
                this.mCertMap.forEach(new BiConsumer() {
                    /* class com.android.server.pm.auth.$$Lambda$HwCertificationManager$pjuD7lF0Hg1FfZoRonbSSNgk6aE */

                    @Override // java.util.function.BiConsumer
                    public final void accept(Object obj, Object obj2) {
                        HwCertificationManager.this.lambda$removeCertCache$1$HwCertificationManager((String) obj, (HwCertification) obj2);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: removeCertCacheOfPackage */
    public void lambda$removeCertCache$1$HwCertificationManager(String packageName, HwCertification hwCert) {
        if (hwCert != null) {
            hwCert.setSignatureV1("");
            hwCert.setDeveloperKey("");
            hwCert.setApkHash("");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setSignatureV1("");
                certData.setDeveloperKey("");
                certData.setApkHash("");
                if (!hwCert.isContainSpecialPermissions()) {
                    hwCert.setSignatureV2("");
                    certData.setSignatureV2("");
                    hwCert.setSignatureV3("");
                    certData.setSignatureV3("");
                }
            }
        }
    }

    private void syncHwCertCache() {
        synchronized (LOCK) {
            this.mCertMap.clear();
            this.mHwCertXmlHandler.readHwCertXml(this.mCertMap);
        }
    }
}
